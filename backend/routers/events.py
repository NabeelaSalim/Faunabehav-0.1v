from fastapi import APIRouter, UploadFile, File, Form
from pathlib import Path
import shutil
import cv2
import os
import time
from dotenv import load_dotenv
from supabase import create_client

from services.run_pipeline import run_pipeline, run_image_pipeline, detect_species
from services.database_service import (
    save_observation,
    save_alert
)

IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png"}

router = APIRouter()

load_dotenv()

supabase = create_client(
    os.getenv("SUPABASE_URL"),
    os.getenv("SUPABASE_KEY")
)


@router.get("/")
def get_events():
    response = (
        supabase
        .table("events")
        .select("*")
        .order("event_id", desc=True)
        .execute()
    )

    return response.data


@router.post("/inference")
async def run_inference(
    file: UploadFile = File(...),
    device_id: int = Form(...)
):
    print("========== ENDPOINT HIT ==========")

    upload_dir = Path("uploads")
    upload_dir.mkdir(exist_ok=True)

    media_path = upload_dir / file.filename

    with open(media_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)

    is_image = media_path.suffix.lower() in IMAGE_EXTENSIONS

    if is_image:
        result = _run_image_inference(media_path, device_id)
    else:
        result = _run_video_inference(media_path, device_id)

    try:
        if media_path.exists():
            media_path.unlink()
            print(f"Deleted uploaded media: {media_path}")
    except Exception as e:
        print(f"Failed to delete uploaded media: {e}")

    return result


def _run_video_inference(video_path: Path, device_id: int):
    # ----------------------------
    # Extract first frame
    # ----------------------------
    frames_dir = Path("frames")
    frames_dir.mkdir(exist_ok=True)

    cap = cv2.VideoCapture(str(video_path))
    success, frame = cap.read()
    cap.release()

    frame_path = None
    frame_width = None
    frame_height = None

    if success:
        frame_height, frame_width = frame.shape[0], frame.shape[1]
        frame_name = f"{video_path.stem}.jpg"
        frame_path = frames_dir / frame_name
        cv2.imwrite(str(frame_path), frame)

    # ----------------------------
    # Run AI pipeline
    # ----------------------------
    inference_start = time.perf_counter()
    result = run_pipeline(str(video_path))
    result["inference_time_seconds"] = round(time.perf_counter() - inference_start, 3)
    result["media_type"] = "video"

    print("========== RESULT ==========")
    print(result)
    print("============================")

    result["frame_path"] = (
        str(frame_path).replace("\\", "/")
        if frame_path
        else None
    )

    if result.get("decision") == "no_target_species_detected":
        return result

    # ----------------------------
    # Real bounding box for the exact frame that was saved as the thumbnail (frame_path),
    # so the overlay the frontend draws lines up with what's actually displayed. Only attach
    # it if this single-frame detection agrees with the pipeline's official species — a
    # mismatch just means no reliable box for THIS frame, not a fabricated one.
    # ----------------------------
    bounding_box = None
    if success:
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        box_species, _box_conf, box = detect_species(rgb_frame)
        if box_species == result["detected_species"]:
            bounding_box = box

    result["bounding_box"] = bounding_box
    result["frame_width"] = frame_width
    result["frame_height"] = frame_height

    event_data = {
        "device_id": device_id,
        "animal": result["detected_species"],
        "behaviour": result["predicted_behaviour"],
        "confidence": result["behaviour_confidence"],
        "species_confidence": result["species_confidence"],
        "risk_level": result["risk_level"],
        "deterrence_action": ", ".join(result["actions"]),
        "outcome": result.get("outcome"),
        "frame_path": result["frame_path"],
        "bounding_box": bounding_box,
        "frame_width": frame_width,
        "frame_height": frame_height,
    }

    saved = save_observation(event_data)
    saved_row = saved.data[0] if saved.data else None
    if saved_row:
        result["timestamp"] = saved_row.get("timestamp")
        result["event_id"] = saved_row.get("event_id")

    if result["risk_level"] == "high":
        latest_event = (
            supabase
            .table("events")
            .select("event_id")
            .order("event_id", desc=True)
            .limit(1)
            .execute()
        )

        event_id = latest_event.data[0]["event_id"]

        alert_data = {
            "event_id": event_id,
            "status": "Active",
            "deterrence_action": ", ".join(result["actions"]),
            "acknowledged_by": None,
            "risk_level": result["risk_level"]
        }

        save_alert(alert_data)

    return result


def _run_image_inference(image_path: Path, device_id: int):
    # ----------------------------
    # Normalize the uploaded image into the same frames/ dir video thumbnails use
    # ----------------------------
    frames_dir = Path("frames")
    frames_dir.mkdir(exist_ok=True)

    frame = cv2.imread(str(image_path))
    frame_path = None
    frame_width = None
    frame_height = None

    if frame is not None:
        frame_height, frame_width = frame.shape[0], frame.shape[1]
        frame_name = f"{image_path.stem}.jpg"
        frame_path = frames_dir / frame_name
        cv2.imwrite(str(frame_path), frame)

    # ----------------------------
    # Run YOLO-only pipeline (no behaviour/risk/deterrence for a still image)
    # ----------------------------
    inference_start = time.perf_counter()
    result = run_image_pipeline(str(image_path))
    result["inference_time_seconds"] = round(time.perf_counter() - inference_start, 3)
    result["media_type"] = "image"

    print("========== IMAGE RESULT ==========")
    print(result)
    print("===================================")

    result["frame_path"] = (
        str(frame_path).replace("\\", "/")
        if frame_path
        else None
    )

    if result.get("decision") == "no_target_species_detected":
        return result

    result["frame_width"] = frame_width
    result["frame_height"] = frame_height

    event_data = {
        "device_id": device_id,
        "animal": result["detected_species"],
        "behaviour": None,
        "confidence": None,
        "species_confidence": result["species_confidence"],
        "risk_level": None,
        "deterrence_action": None,
        "outcome": None,
        "frame_path": result["frame_path"],
        "bounding_box": result.get("bounding_box"),
        "frame_width": frame_width,
        "frame_height": frame_height,
    }

    saved = save_observation(event_data)
    saved_row = saved.data[0] if saved.data else None
    if saved_row:
        result["timestamp"] = saved_row.get("timestamp")
        result["event_id"] = saved_row.get("event_id")

    return result