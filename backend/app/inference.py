"""
R3D-18 model inference for FaunaBahav.
Loads the trained model checkpoint, preprocesses images/videos,
and maps model outputs to species + behaviour classifications.

NOTE: This module cannot be tested in the current sandboxed environment
due to macOS code signing restrictions. Verify on a normal machine.
"""

import os
import logging
from typing import Optional

logger = logging.getLogger(__name__)

# ── Constants (adjust after inspecting checkpoint on a normal machine) ──────────
NUM_FRAMES = 16
FRAME_SIZE = 112  # R3D-18 default; adjust if checkpoint uses 224
MEAN = [0.43216, 0.394666, 0.37645]
STD = [0.22803, 0.22145, 0.21699]
CONFIDENCE_THRESHOLD = 0.5

SPECIES_LIST = ["monkey", "wild_boar", "bird"]
BEHAVIOUR_LIST = [
    "feeding_foraging",
    "locomotion",
    "vigilance_alert",
    "aggressive_destructive",
    "resting_passive",
    "social_interaction",
    "unknown_unclear",
]

# ── Risk / deterrence mapping ───────────────────────────────────────────────────

HIGH_RISK_BEHAVIOURS = {"aggressive_destructive"}
MEDIUM_RISK_BEHAVIOURS = {"vigilance_alert", "social_interaction"}


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


# ── Image / video preprocessing (standalone, no torch needed) ──────────────────


def _resize_frame(frame, size: int = FRAME_SIZE):
    """Resize a single image frame to `size`×`size`."""
    import cv2

    return cv2.resize(frame, (size, size))


def _normalize_frame(frame, mean, std):
    """Normalize a float32 frame (0-1 range) using mean/std."""
    return (frame - mean) / std


def _read_frames_from_video(video_path: str) -> list:
    """Sample up to NUM_FRAMES evenly spaced frames from a video."""
    import cv2
    import numpy as np

    cap = cv2.VideoCapture(video_path)
    total = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    if total < 1:
        cap.release()
        raise ValueError(f"Video has no readable frames: {video_path}")

    # Compute 16 evenly-spaced indices
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
    """Repeat a single image NUM_FRAMES times to create a clip."""
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


# ── Model wrapper ──────────────────────────────────────────────────────────────


class FaunaBehavInference:
    """Loads and runs the trained R3D-18 model."""

    def __init__(self, model_path: str, device: str = "cpu"):
        self.device_str = device
        self.model_path = model_path
        self.model = None  # loaded lazily
        self._num_classes = None

    def _load(self):
        """Load the PyTorch model checkpoint."""
        import torch
        import torchvision.models.video as models

        # Determine device
        use_cuda = self.device_str == "cuda" and torch.cuda.is_available()
        self.device = torch.device("cuda" if use_cuda else "cpu")

        # Load checkpoint
        checkpoint = torch.load(
            self.model_path, map_location=self.device, weights_only=False
        )

        # Extract state dict (handle different key formats)
        state_dict = checkpoint
        if isinstance(checkpoint, dict):
            for key in ("state_dict", "model_state_dict", "model"):
                if key in checkpoint:
                    state_dict = checkpoint[key]
                    break

        # Remove 'module.' prefix from DataParallel saves
        cleaned = {}
        for k, v in state_dict.items():
            cleaned[k[7:] if k.startswith("module.") else k] = v

        # Determine number of output classes from fc layer
        fc_weight_key = next(
            (k for k in cleaned if "fc" in k.lower() and "weight" in k), None
        )
        if fc_weight_key:
            self._num_classes = cleaned[fc_weight_key].shape[0]
        else:
            self._num_classes = 10  # fallback if no fc found

        # Build model
        model = models.r3d_18(weights=None)
        in_features = model.fc.in_features
        model.fc = torch.nn.Linear(in_features, self._num_classes)
        model.load_state_dict(cleaned, strict=False)
        model.to(self.device)
        model.eval()
        self.model = model
        logger.info(
            "Model loaded from %s on %s (%d classes)",
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
        Run inference on an image or video file.

        Returns a dict matching the ``InferenceResultDto`` expected by the KMP client.
        """
        import torch
        import numpy as np

        self._ensure_loaded()

        # Preprocess ──
        ext = os.path.splitext(file_path)[1].lower()
        image_exts = {".jpg", ".jpeg", ".png", ".bmp", ".tiff", ".webp"}

        if ext in image_exts:
            frames = _tile_image_to_frames(file_path)
        else:
            frames = _read_frames_from_video(file_path)

        # Stack → (C, T, H, W) → batch dim
        # Each frame is (H, W, C) in float32
        input_tensor = (
            torch.from_numpy(np.stack(frames))  # (T, H, W, C)
            .permute(3, 0, 1, 2)  # (C, T, H, W)
            .float()
            .unsqueeze(0)
        )  # (1, C, T, H, W)

        # Forward ──
        with torch.no_grad():
            outputs = self.model(input_tensor.to(self.device))

        probs = torch.softmax(outputs, dim=1).cpu().numpy()[0]
        top_idx = int(np.argmax(probs))
        confidence = float(probs[top_idx])

        # Map output → species + behaviour ──
        # The model was trained on species+behaviour combinations.
        # Adjust this mapping after inspecting the actual class labels.
        # Fallback heuristic:
        if self._num_classes == 3:
            # Pure species classifier
            detected_species = SPECIES_LIST[top_idx] if top_idx < 3 else "unknown"
            predicted_behaviour = "unknown_unclear"
            species_conf = confidence
            behaviour_conf = 0.0
        elif self._num_classes == 10:
            # 3 species × 7 behaviours = 21 (but some combos missing)
            # Fallback: simple index-based split
            species_idx = top_idx % 3
            behaviour_idx = (top_idx // 3) % 7
            detected_species = SPECIES_LIST[species_idx]
            predicted_behaviour = BEHAVIOUR_LIST[behaviour_idx]
            species_conf = confidence
            behaviour_conf = confidence
        else:
            detected_species = "unknown"
            predicted_behaviour = "unknown_unclear"
            species_conf = confidence
            behaviour_conf = 0.0

        if confidence < CONFIDENCE_THRESHOLD:
            return {"decision": "no_target_species_detected"}

        risk = risk_level_for(predicted_behaviour)
        action = deterrence_action_for(risk)

        return {
            "decision": "detected",
            "detected_species": detected_species,
            "species_confidence": round(species_conf, 4),
            "predicted_behaviour": predicted_behaviour,
            "behaviour_confidence": round(behaviour_conf, 4),
            "risk_level": risk,
            "alert_type": f"{risk}_risk_{detected_species}",
            "actions": [action],
            "message": f"Detected {detected_species} exhibiting {predicted_behaviour.replace('_', ' ')}",
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
