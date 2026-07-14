print("RUN PIPELINE LOADED")
import json
import (cv2)
import numpy as np
import torch
import torch.nn as nn
import torchvision.models.video as video_models
from pathlib import Path
from ultralytics import YOLO

# ── Paths — update these to wherever the model files live on the server ──
YOLO_WEIGHTS_PATH = "outputs_corrected_v2/faunabehav_yolo_best.pt"
R3D18_WEIGHTS_PATH = "outputs_corrected_v2/faunabehav_r3d18_best.pth"
METADATA_PATH = "outputs_corrected_v2/metadata.json"

# ── Constants ──
SPECIES_CONF_THRESHOLD = 0.50
YOLO_TARGET_CLASSES    = {0: "monkey", 1: "wild_boar"}
NUM_FRAMES             = 16
IMAGE_SIZE             = 112
USE_AMP                = torch.cuda.is_available()

VID_MEAN = np.array([0.43216, 0.394666, 0.37645], dtype=np.float32)
VID_STD  = np.array([0.22803, 0.22145,  0.216989], dtype=np.float32)

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

# ── Load metadata ──
with open(METADATA_PATH) as f:
    metadata = json.load(f)
behaviour_classes = metadata["behaviour_classes"]
id_to_behaviour   = {int(k): v for k, v in metadata["id_to_behaviour"].items()}
NUM_CLASSES       = metadata["num_classes"]

# ── Load YOLO ──
print("Loading metadata...")
with open(METADATA_PATH) as f:
    metadata = json.load(f)

print("Loading YOLO...")
yolo_model = YOLO(YOLO_WEIGHTS_PATH)

print("YOLO classes:", yolo_model.names)

# ── Load R3D-18 ──
r3d_model = video_models.r3d_18(weights=None)
r3d_model.fc = nn.Sequential(
    nn.Dropout(p=0.5),
    nn.Linear(r3d_model.fc.in_features, NUM_CLASSES)
)
print("Loading R3D18...")
checkpoint = torch.load(R3D18_WEIGHTS_PATH, map_location=device, weights_only=True)
state = checkpoint["model_state"] if "model_state" in checkpoint else checkpoint
r3d_model.load_state_dict(state)
r3d_model = r3d_model.to(device)
r3d_model.eval()

# ── RISK MAP ──
RISK_MAP = {
    "feeding_foraging":       "high",
    "aggressive_destructive": "high",
    "vigilance_alert":        "medium",
    "social_interaction":     "medium",
    "locomotion":             "low",
    "resting_passive":        "low",
}
AGRICULTURAL_HIGH_RISK = {"rooting", "trampling", "grazing"}

def map_behaviour_to_risk(behaviour, raw_action=None):
    if raw_action and str(raw_action).lower().strip() in AGRICULTURAL_HIGH_RISK:
        return "high"
    return RISK_MAP.get(str(behaviour).lower().strip(), "medium")

# ── Species detection ──
def detect_species(frame):
    """
    Runs YOLO on a single frame. Returns (species, confidence, bounding_box) for the
    highest-confidence target-class detection, where bounding_box is a real
    {"x1","y1","x2","y2"} dict in pixel coordinates of `frame`, or None if nothing detected.
    """

    results = yolo_model.predict(frame, verbose=False)

    print("\n========== YOLO DETECTIONS ==========")

    best_conf = 0.0
    best_sp = "none"
    best_box = None

    for r in results:

        if r.boxes is None:
            continue

        for box in r.boxes:

            cls_id = int(box.cls[0].item())
            conf = float(box.conf[0].item())

            print(
                f"Class ID: {cls_id} | "
                f"Class Name: {yolo_model.names[cls_id]} | "
                f"Confidence: {conf:.3f}"
            )

            if cls_id in YOLO_TARGET_CLASSES and conf >= SPECIES_CONF_THRESHOLD:

                if conf > best_conf:
                    best_conf = conf
                    best_sp = YOLO_TARGET_CLASSES[cls_id]
                    x1, y1, x2, y2 = box.xyxy[0].tolist()
                    best_box = {"x1": x1, "y1": y1, "x2": x2, "y2": y2}

    print("Selected Species:", best_sp)
    print("Confidence:", best_conf)
    print("=====================================\n")

    return best_sp, best_conf, best_box

def detect_species_from_video(video_path, num_frames_to_check=8):
    try:
        cap   = cv2.VideoCapture(str(video_path))
        total = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
        if total <= 0:
            cap.release()
            return {"species": "none", "species_confidence": 0.0}
        idxs   = np.linspace(0, total - 1, num_frames_to_check, dtype=int)
        frames = []
        for idx in idxs:
            cap.set(cv2.CAP_PROP_POS_FRAMES, int(idx))
            ok, frame = cap.read()
            if ok:
                frames.append(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        cap.release()
    except Exception:
        return {"species": "none", "species_confidence": 0.0}

    votes, confs = {}, {}
    for frame in frames:
        sp, conf, _ = detect_species(frame)
        if sp != "none":
            votes[sp] = votes.get(sp, 0) + 1
            confs[sp] = confs.get(sp, []) + [conf]

    if not votes:
        return {"species": "none", "species_confidence": 0.0}
    winner    = max(votes, key=votes.get)
    mean_conf = float(np.mean(confs[winner]))
    return {"species": winner, "species_confidence": mean_conf}

# ── Behaviour prediction ──
def _video_to_tensor(video_path):
    cap   = cv2.VideoCapture(str(video_path))
    total = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    idxs  = np.linspace(0, max(total - 1, 0), NUM_FRAMES, dtype=int)
    frames = []
    for idx in idxs:
        cap.set(cv2.CAP_PROP_POS_FRAMES, int(idx))
        ok, frame = cap.read()
        if not ok:
            frame = np.zeros((IMAGE_SIZE, IMAGE_SIZE, 3), dtype=np.uint8)
        else:
            frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            frame = cv2.resize(frame, (IMAGE_SIZE, IMAGE_SIZE))
        frames.append(frame)
    cap.release()
    arr = np.stack(frames).astype(np.float32) / 255.0
    arr = (arr - VID_MEAN) / VID_STD
    return torch.tensor(arr, dtype=torch.float32).permute(3, 0, 1, 2).unsqueeze(0)

def predict_behaviour_from_video(video_path):
    tensor = _video_to_tensor(video_path).to(device)
    with torch.no_grad():
        outputs    = r3d_model(tensor)
        probs      = torch.softmax(outputs, dim=1)
        conf, pred = torch.max(probs, dim=1)
    behaviour = id_to_behaviour[pred.item()]
    return {
        "predicted_behaviour":  behaviour,
        "behaviour_confidence": conf.item(),
        "risk_level":           map_behaviour_to_risk(behaviour),
    }

# ── Outcome logic ──
# Deterministic label for what state the situation is left in after the deterrence
# decision above — NOT a physical confirmation (no sensor tells us the animal actually
# left), just a description of how the automated response resolved. "graduated_deterrence"
# is shared by two different cases (medium risk, or high risk with low confidence), so
# risk_level must be checked alongside alert_type to tell them apart.
def determine_outcome(alert_type, risk_level):
    if alert_type == "immediate_multi_modal":
        return "deterrence_activated"
    if alert_type == "graduated_deterrence" and str(risk_level).lower().strip() == "high":
        return "farmer_intervention_required"
    return "monitoring_continues"

# ── Alert logic ──
def fauna_behav_decision(risk_level, confidence, species="unknown"):
    risk_level = str(risk_level).lower().strip()
    if confidence < 0.50:
        return {"alert_type": "passive_monitoring",
                "actions": ["log_uncertain_event"],
                "message": "Uncertain detection — flagged for review",
                "risk_level": risk_level}
    if risk_level == "high" and confidence >= 0.70:
        return {"alert_type": "immediate_multi_modal",
                "actions": ["activate_siren","activate_strobe","send_sms","send_push_notification","log_event"],
                "message": f"HIGH RISK: {species} detected — immediate deterrence activated",
                "risk_level": risk_level}
    if risk_level == "high" and confidence < 0.70:
        return {"alert_type": "graduated_deterrence",
                "actions": ["activate_siren","send_push_notification","log_event"],
                "message": "HIGH RISK (low confidence): monitoring escalated",
                "risk_level": risk_level}
    if risk_level == "medium":
        return {"alert_type": "graduated_deterrence",
                "actions": ["activate_sound","send_push_notification","log_event"],
                "message": "MEDIUM RISK: graduated deterrence activated",
                "risk_level": risk_level}
    if risk_level == "low":
        return {"alert_type": "passive_monitoring",
                "actions": ["log_event"],
                "message": "LOW RISK: logged for review — no deterrent sent",
                "risk_level": risk_level}
    return {"alert_type": "passive_monitoring", "actions": ["log_event"],
            "message": "Unknown risk level — continuing monitoring",
            "risk_level": risk_level}

# ── Full pipeline ──
def run_pipeline(video_path):
    """
    Entry point for backend. Pass a video file path (str or Path).
    Returns a dict with all detection, behaviour, risk, and alert fields.
    If no target species detected, returns {"decision": "no_target_species_detected"}.
    """
    species_result = detect_species_from_video(video_path)
    if species_result["species"] == "none":
        return {"decision": "no_target_species_detected", "video_path": str(video_path)}

    behaviour_result = predict_behaviour_from_video(video_path)
    risk_level       = map_behaviour_to_risk(behaviour_result["predicted_behaviour"])
    alert            = fauna_behav_decision(
                           risk_level,
                           behaviour_result["behaviour_confidence"],
                           species=species_result["species"]
                       )
    return {
        "video_path":           str(video_path),
        "detected_species":     species_result["species"],
        "species_confidence":   round(species_result["species_confidence"], 4),
        "predicted_behaviour":  behaviour_result["predicted_behaviour"],
        "behaviour_confidence": round(behaviour_result["behaviour_confidence"], 4),
        "risk_level":           risk_level,
        "alert_type":           alert["alert_type"],
        "actions":              alert["actions"],
        "message":              alert["message"],
        "outcome":              determine_outcome(alert["alert_type"], risk_level),
    }

# ── Image-only pipeline ──
# A single still image cannot feed R3D-18 meaningfully (it needs 16 real temporal
# frames of motion) — so images only ever get YOLOv8 species detection. Behaviour,
# risk, deterrence, and outcome are all left absent rather than guessed from a
# duplicated static frame.
def run_image_pipeline(image_path):
    frame = cv2.imread(str(image_path))
    if frame is None:
        return {"decision": "no_target_species_detected", "video_path": str(image_path)}

    rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    species, confidence, box = detect_species(rgb_frame)

    if species == "none":
        return {"decision": "no_target_species_detected", "video_path": str(image_path)}

    return {
        "video_path":           str(image_path),
        "detected_species":     species,
        "species_confidence":   round(confidence, 4),
        "predicted_behaviour":  None,
        "behaviour_confidence": None,
        "risk_level":           None,
        "alert_type":           None,
        "actions":              [],
        "message":              "Image upload — behaviour analysis requires video.",
        "outcome":              None,
        "bounding_box":         box,
    }