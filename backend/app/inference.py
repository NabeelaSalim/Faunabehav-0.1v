"""
R3D-18 behaviour classifier + YOLO species detector for FaunaBahav.

Pipeline
--------
1. YOLOv8 species detection (image only) — identifies monkey / wild_boar / none
2. R3D-18 behaviour classification (image or video) — 6-class behaviour

Set environment variable TORCH_USE_RTLD_GLOBAL=1 before importing if
libtorch_global_deps.dylib is missing on macOS.
"""

import os
import logging
from typing import Optional

logger = logging.getLogger(__name__)

# ── R3D-18 constants (confirmed from metadata.json and checkpoint) ────────────
NUM_FRAMES = 16
FRAME_SIZE = 112
MEAN = [0.43216, 0.394666, 0.37645]
STD = [0.22803, 0.22145, 0.21699]
CONFIDENCE_THRESHOLD = 0.5
NUM_CLASSES = 6

ID_TO_BEHAVIOUR = {
    0: "aggressive_destructive",
    1: "feeding_foraging",
    2: "locomotion",
    3: "resting_passive",
    4: "social_interaction",
    5: "vigilance_alert",
}
BEHAVIOUR_LIST = list(ID_TO_BEHAVIOUR.values())

# ── YOLO constants ────────────────────────────────────────────────────────────
YOLO_MODEL_PATH = os.path.join(
    os.path.dirname(__file__),
    "..",
    "outputs_corrected_v2",
    "faunabehav_yolo_best.pt",
)
YOLO_SPECIES_MAP = {0: "monkey", 1: "wild_boar"}
YOLO_CONFIDENCE_THRESHOLD = 0.25

# ── Risk / deterrence mapping ─────────────────────────────────────────────────

HIGH_RISK_BEHAVIOURS = {"aggressive_destructive"}
MEDIUM_RISK_BEHAVIOURS = {"vigilance_alert", "social_interaction"}

IMAGE_EXTS = {".jpg", ".jpeg", ".png", ".bmp", ".tiff", ".webp"}


def risk_level_for(behaviour: str) -> str:
    if behaviour in HIGH_RISK_BEHAVIOURS:
        return "high"
    if behaviour in MEDIUM_RISK_BEHAVIOURS:
        return "medium"
    return "low"


def deterrence_action_for(risk_level: str) -> str:
    return {"high": "active_deterrence", "medium": "warning", "low": "monitor"}.get(
        risk_level, "monitor"
    )


# ── YOLO species detector ─────────────────────────────────────────────────────

_yolo_model: Optional["YOLO"] = None  # noqa: F821


def _get_yolo_model():
    """Lazy-load the YOLO model singleton."""
    global _yolo_model
    if _yolo_model is None:
        from ultralytics import YOLO

        _yolo_model = YOLO(YOLO_MODEL_PATH)
        logger.info("YOLO species detector loaded from %s", YOLO_MODEL_PATH)
    return _yolo_model


def detect_species(image_path: str) -> Optional[dict]:
    """
    Run YOLO species detection on an image file.

    Returns ``None`` when nothing is detected, otherwise a dict::

        {"species": "monkey" | "wild_boar",
         "confidence": 0.92,
         "bbox": [x1, y1, x2, y2]}   # normalized 0-1 coords
    """
    model = _get_yolo_model()
    results = model(image_path, verbose=False)
    if not results or len(results) == 0:
        return None

    boxes = results[0].boxes
    if boxes is None or len(boxes) == 0:
        return None

    # Pick highest-confidence detection
    best = boxes[0]
    cls_id = int(best.cls[0])
    confidence = float(best.conf[0])

    if confidence < YOLO_CONFIDENCE_THRESHOLD:
        return None

    species = YOLO_SPECIES_MAP.get(cls_id, "unknown")
    xyxy = best.xyxy[0].tolist()

    # Return normalized coords (0-1) and pixel coords
    img_w = results[0].orig_shape[1]
    img_h = results[0].orig_shape[0]

    return {
        "species": species,
        "confidence": round(confidence, 4),
        "bbox": [round(v, 2) for v in xyxy],
        "bbox_normalized": [
            round(xyxy[0] / img_w, 4),
            round(xyxy[1] / img_h, 4),
            round(xyxy[2] / img_w, 4),
            round(xyxy[3] / img_h, 4),
        ],
        "frame_width": img_w,
        "frame_height": img_h,
    }


# ── R3D-18 preprocessing (standalone, no torch needed) ────────────────────────


def _resize_frame(frame, size: int = FRAME_SIZE):
    import cv2

    return cv2.resize(frame, (size, size))


def _normalize_frame(frame, mean, std):
    return (frame - mean) / std


def _read_frames_from_video(video_path: str) -> list:
    import cv2
    import numpy as np

    cap = cv2.VideoCapture(video_path)
    total = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    if total < 1:
        cap.release()
        raise ValueError(f"Video has no readable frames: {video_path}")

    if total <= NUM_FRAMES:
        indices = list(range(total))
        indices += [indices[-1]] * (NUM_FRAMES - total)
    else:
        step = total / NUM_FRAMES
        indices = [int(i * step) for i in range(NUM_FRAMES)]

    frames = []
    for idx in indices:
        cap.set(cv2.CAP_PROP_POS_FRAMES, idx)
        ret, frame = cap.read()
        if not ret:
            frame = np.zeros((FRAME_SIZE, FRAME_SIZE, 3), dtype=np.uint8)
        frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        frame = _resize_frame(frame)
        frame = frame.astype(np.float32) / 255.0
        frame = _normalize_frame(frame, np.array(MEAN), np.array(STD))
        frames.append(frame)
    cap.release()
    return frames


def _tile_image_to_frames(image_path: str) -> list:
    import cv2
    import numpy as np

    frame = cv2.imread(image_path)
    if frame is None:
        raise ValueError(f"Cannot read image: {image_path}")
    frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    frame = _resize_frame(frame)
    frame = frame.astype(np.float32) / 255.0
    frame = _normalize_frame(frame, np.array(MEAN), np.array(STD))
    return [frame] * NUM_FRAMES


# ── R3D-18 model wrapper ──────────────────────────────────────────────────────


class FaunaBehavInference:
    """Loads and runs the trained R3D-18 behaviour classifier."""

    def __init__(self, model_path: str, device: str = "cpu"):
        self.device_str = device
        self.model_path = model_path
        self.model = None
        self._num_classes = None

    def _load(self):
        import torch
        import torchvision.models.video as models

        use_cuda = self.device_str == "cuda" and torch.cuda.is_available()
        self.device = torch.device("cuda" if use_cuda else "cpu")

        checkpoint = torch.load(
            self.model_path, map_location=self.device, weights_only=False
        )

        if isinstance(checkpoint, dict) and "model_state" in checkpoint:
            state_dict = checkpoint["model_state"]
        else:
            state_dict = checkpoint

        cleaned = {}
        for k, v in state_dict.items():
            cleaned[k[7:] if k.startswith("module.") else k] = v

        fc_weight_key = next(
            (k for k in cleaned if "fc" in k.lower() and "weight" in k), None
        )
        if fc_weight_key:
            self._num_classes = cleaned[fc_weight_key].shape[0]
        else:
            self._num_classes = NUM_CLASSES

        model = models.r3d_18(weights=None)
        in_features = model.fc.in_features
        model.fc = torch.nn.Sequential(
            torch.nn.Dropout(0.5),
            torch.nn.Linear(in_features, self._num_classes),
        )
        model.load_state_dict(cleaned, strict=False)
        model.to(self.device)
        model.eval()
        self.model = model
        logger.info(
            "R3D-18 loaded from %s on %s (%d classes)",
            self.model_path,
            self.device,
            self._num_classes,
        )

    @property
    def num_classes(self) -> int:
        if self._num_classes is None:
            self._load()
        return self._num_classes

    def _ensure_loaded(self):
        if self.model is None:
            self._load()

    def infer(self, file_path: str) -> dict:
        """
        Run full inference pipeline on an image or video file.

        For images the pipeline is:

            1. YOLO species detection (monkey / wild_boar / none)
            2. R3D-18 behaviour classification (always)

        For videos only R3D-18 runs (no YOLO).

        Returns a dict with:
            decision, detected_species, species_confidence,
            predicted_behaviour, behaviour_confidence,
            risk_level, alert_type, actions, message,
            bounding_box, frame_width, frame_height
        """
        import torch
        import numpy as np

        self._ensure_loaded()

        ext = os.path.splitext(file_path)[1].lower()
        is_image = ext in IMAGE_EXTS

        # ── Step 1: YOLO species detection (images only) ────────────────
        species_info = None
        if is_image:
            try:
                species_info = detect_species(file_path)
            except Exception:
                logger.warning("YOLO detection failed for %s", file_path, exc_info=True)

        detected_species = "unknown"
        species_confidence = 0.0
        bbox = None
        frame_w = None
        frame_h = None

        if species_info:
            detected_species = species_info["species"]
            species_confidence = species_info["confidence"]
            bbox = species_info["bbox"]
            frame_w = species_info.get("frame_width")
            frame_h = species_info.get("frame_height")
            logger.info(
                "YOLO detected %s (confidence=%.4f)",
                detected_species,
                species_confidence,
            )

        # ── Step 2: R3D-18 behaviour classification ─────────────────────
        if is_image:
            frames = _tile_image_to_frames(file_path)
        else:
            frames = _read_frames_from_video(file_path)

        input_tensor = (
            torch.from_numpy(np.stack(frames))
            .permute(3, 0, 1, 2)
            .float()
            .unsqueeze(0)
        )

        with torch.no_grad():
            outputs = self.model(input_tensor.to(self.device))

        probs = torch.softmax(outputs, dim=1).cpu().numpy()[0]
        top_idx = int(np.argmax(probs))
        behaviour_confidence = float(probs[top_idx])

        if behaviour_confidence < CONFIDENCE_THRESHOLD:
            return self._build_result(
                decision="no_target_species_detected",
                detected_species=detected_species,
                species_confidence=species_confidence,
                bbox=bbox,
                frame_w=frame_w,
                frame_h=frame_h,
            )

        predicted_behaviour = ID_TO_BEHAVIOUR.get(top_idx, "unknown_unclear")
        risk = risk_level_for(predicted_behaviour)
        action = deterrence_action_for(risk)

        return self._build_result(
            decision="detected",
            detected_species=detected_species,
            species_confidence=species_confidence,
            predicted_behaviour=predicted_behaviour,
            behaviour_confidence=round(behaviour_confidence, 4),
            risk_level=risk,
            actions=[action],
            message=(
                f"{detected_species.replace('_', ' ').title()} exhibiting "
                f"{predicted_behaviour.replace('_', ' ')} "
                f"(behaviour: {behaviour_confidence:.0%}, "
                f"species: {species_confidence:.0%})"
            ),
            bbox=bbox,
            frame_w=frame_w,
            frame_h=frame_h,
        )

    @staticmethod
    def _build_result(
        decision: str,
        detected_species: str = "unknown",
        species_confidence: float = 0.0,
        predicted_behaviour: str = "unknown_unclear",
        behaviour_confidence: float = 0.0,
        risk_level: str = "low",
        actions: Optional[list] = None,
        message: str = "",
        bbox: Optional[list] = None,
        frame_w: Optional[int] = None,
        frame_h: Optional[int] = None,
    ) -> dict:
        risk = risk_level
        alert_species = "unknown" if detected_species == "unknown" else detected_species
        return {
            "decision": decision,
            "detected_species": detected_species,
            "species_confidence": species_confidence,
            "predicted_behaviour": predicted_behaviour,
            "behaviour_confidence": behaviour_confidence,
            "risk_level": risk,
            "alert_type": f"{risk}_risk_{alert_species}",
            "actions": actions or ["monitor"],
            "message": message,
            "bounding_box": bbox,
            "frame_width": frame_w,
            "frame_height": frame_h,
        }


# ── Singleton accessor ─────────────────────────────────────────────────────────

_model_instance: Optional[FaunaBehavInference] = None


def get_model(model_path: Optional[str] = None) -> FaunaBehavInference:
    """Get or create the singleton model instance."""
    global _model_instance
    if _model_instance is None:
        if model_path is None:
            model_path = os.path.join(
                os.path.dirname(__file__),
                "..",
                "outputs_corrected_v2",
                "faunabehav_r3d18_best.pth",
            )
        _model_instance = FaunaBehavInference(model_path)
    return _model_instance
