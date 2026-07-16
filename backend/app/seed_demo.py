"""
Seed demo observations by running the full ML pipeline on sample images.

Usage::

    cd /Users/karimmaige/Downloads/FaunaBahav/backend
    TORCH_USE_RTLD_GLOBAL=1 venv/bin/python3 -m app.seed_demo
"""

import os
import sys
import json
import logging
from datetime import datetime, timezone

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("seed_demo")

# Ensure we can import from app
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))


def seed():
    from app.database import SessionLocal
    from app.models import Observation, Device, Alert, Upload
    from app.inference import get_model

    db = SessionLocal()

    # Get the first device (we need a valid device_id)
    device = db.query(Device).first()
    if not device:
        logger.error("No devices in DB. Run the app first to create seed data.")
        db.close()
        return

    device_id = device.device_id
    logger.info("Using device_id=%d (%s)", device_id, device.device_name)

    # Get the model
    model = get_model()

    # Find seed images that actually contain animals (check YOLO)
    seed_dir = os.path.join(os.path.dirname(__file__), "..", "seed_images")
    if not os.path.isdir(seed_dir):
        logger.error("seed_images directory not found at %s", seed_dir)
        db.close()
        return

    image_files = sorted(
        [f for f in os.listdir(seed_dir) if f.lower().endswith((".jpg", ".jpeg", ".png"))]
    )
    logger.info("Found %d seed images", len(image_files))

    created = 0
    for fname in image_files:
        fpath = os.path.join(seed_dir, fname)

        # Skip if already processed (check frame_path)
        existing = db.query(Observation).filter(Observation.frame_path == fpath).first()
        if existing:
            logger.info("  Skipped %s (already exists as event_id=%d)", fname, existing.event_id)
            continue

        # Run inference
        try:
            result = model.infer(fpath)
        except Exception as e:
            logger.warning("  Inference failed for %s: %s", fname, e)
            continue

        decision = result.get("decision")
        if decision != "detected":
            logger.info("  Skipped %s (decision=%s)", fname, decision)
            continue

        # Create observation
        bbox = result.get("bounding_box")
        obs = Observation(
            device_id=device_id,
            animal=result["detected_species"],
            behaviour=result["predicted_behaviour"],
            confidence=result.get("behaviour_confidence", 0.0),
            risk_level=result["risk_level"],
            deterrence_action=result["actions"][0] if result.get("actions") else "monitor",
            frame_path=fpath,
            bounding_box=json.dumps(bbox) if bbox else None,
            frame_width=result.get("frame_width"),
            frame_height=result.get("frame_height"),
            timestamp=datetime.now(timezone.utc),
        )
        db.add(obs)
        db.commit()
        db.refresh(obs)

        logger.info(
            "  Created observation %d: %s / %s (%.0f%%)",
            obs.event_id,
            result["detected_species"],
            result["predicted_behaviour"],
            result.get("behaviour_confidence", 0) * 100,
        )

        # Create alert for medium/high risk
        if result["risk_level"] in ("high", "medium"):
            alert = Alert(
                event_id=obs.event_id,
                animal=result["detected_species"],
                behaviour=result["predicted_behaviour"],
                risk_level=result["risk_level"],
                confidence=result.get("behaviour_confidence", 0.0),
                location=device.location,
                status="open",
                deterrence_action=result["actions"][0] if result.get("actions") else "monitor",
            )
            db.add(alert)
            db.commit()
            logger.info("    → Alert created (risk=%s)", result["risk_level"])

        created += 1

    db.close()
    logger.info("Done — created %d new observations", created)


if __name__ == "__main__":
    seed()
