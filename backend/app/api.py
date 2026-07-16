"""
FaunaBahav data API endpoints.

All routes are protected by JWT auth (dependency: ``get_current_user``)
except where noted.
"""

import os
import uuid
import logging
from datetime import datetime, timezone
from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form, status
from pydantic import BaseModel
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import func

from app.database import get_db
from app.models import Observation, Alert, Feedback, Device, Farm, Upload, User
from app.auth import get_current_user, get_db as _auth_db
from app.iot import get_deterrence_controller

logger = logging.getLogger(__name__)

# Re-export get_db to avoid confusion
get_db = _auth_db

router = APIRouter(prefix="", tags=["data"])


# ── Schemas ────────────────────────────────────────────────────────────────────


class FeedbackCreate(BaseModel):
    event_id: int
    user_id: int
    corrected_behaviour: str


class FarmCreate(BaseModel):
    farm_name: str
    location: str


class FarmUpdate(BaseModel):
    farm_name: Optional[str] = None
    location: Optional[str] = None


class DeviceCreate(BaseModel):
    farm_id: int
    device_name: str
    location: str


class DeviceUpdate(BaseModel):
    device_name: Optional[str] = None
    location: Optional[str] = None
    status: Optional[str] = None


class DeterrenceRequest(BaseModel):
    action: str  # "on" or "off"


# ── Role helpers ───────────────────────────────────────────────────────────────


def is_admin(user: User) -> bool:
    return user.role in ("admin", "super_admin")


def get_accessible_farm_ids(user: User, db: Session) -> List[int]:
    """Return farm IDs the user can access."""
    if user.role in ("admin", "super_admin"):
        return [f.farm_id for f in db.query(Farm.farm_id).all()]
    # Farmer: only own farms
    return [f.farm_id for f in db.query(Farm.farm_id).filter(Farm.owner_id == user.user_id).all()]


def get_accessible_device_ids(user: User, db: Session) -> List[int]:
    """Return device IDs the user can access (via farm ownership)."""
    farm_ids = get_accessible_farm_ids(user, db)
    if not farm_ids:
        return []
    return [
        d.device_id
        for d in db.query(Device.device_id).filter(Device.farm_id.in_(farm_ids)).all()
    ]


def _observation_to_dict(o: Observation) -> dict:
    import json
    bbox_raw = o.bounding_box
    return {
        "event_id": o.event_id,
        "device_id": o.device_id,
        "animal": o.animal,
        "behaviour": o.behaviour,
        "confidence": o.confidence,
        "risk_level": o.risk_level,
        "deterrence_action": o.deterrence_action,
        "frame_path": o.frame_path or "",
        "timestamp": o.timestamp.isoformat() if o.timestamp else "",
        "bounding_box": json.loads(bbox_raw) if bbox_raw else None,
        "frame_width": o.frame_width,
        "frame_height": o.frame_height,
    }


def _alert_to_dict(a: Alert) -> dict:
    return {
        "alert_id": a.alert_id,
        "event_id": a.event_id,
        "animal": a.animal,
        "behaviour": a.behaviour,
        "risk_level": a.risk_level,
        "confidence": a.confidence,
        "location": a.location,
        "status": a.status,
        "deterrence_action": a.deterrence_action,
        "timestamp": a.timestamp.isoformat() if a.timestamp else "",
    }


def _feedback_to_dict(f: Feedback) -> dict:
    return {
        "feedback_id": f.feedback_id,
        "event_id": f.event_id,
        "user_id": f.user_id,
        "corrected_behaviour": f.corrected_behaviour,
    }


def _device_to_dict(d: Device) -> dict:
    return {
        "device_id": d.device_id,
        "farm_id": d.farm_id,
        "device_name": d.device_name,
        "location": d.location,
        "status": d.status,
    }


def _farm_to_dict(f: Farm) -> dict:
    device_count = len(f.devices) if f.devices else 0
    return {
        "farm_id": f.farm_id,
        "farm_name": f.farm_name,
        "location": f.location,
        "owner_id": f.owner_id,
        "device_count": device_count,
        "created_at": f.created_at.isoformat() if f.created_at else "",
    }


# ── Existing data endpoints (role-scoped) ──────────────────────────────────────


@router.get("/observations/")
def list_observations(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Return observations scoped to user's accessible devices."""
    device_ids = get_accessible_device_ids(current_user, db)
    obs = (
        db.query(Observation)
        .filter(Observation.device_id.in_(device_ids))
        .order_by(Observation.timestamp.desc())
        .all()
        if device_ids
        else []
    )
    return [_observation_to_dict(o) for o in obs]


@router.get("/alerts/")
def list_alerts(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Return alerts scoped to user's accessible devices."""
    device_ids = get_accessible_device_ids(current_user, db)
    alerts = (
        db.query(Alert)
        .join(Observation, Alert.event_id == Observation.event_id)
        .filter(Observation.device_id.in_(device_ids))
        .order_by(Alert.timestamp.desc())
        .all()
        if device_ids
        else []
    )
    return [_alert_to_dict(a) for a in alerts]


@router.get("/feedback/")
def list_feedback(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Return all feedback entries."""
    fb = db.query(Feedback).order_by(Feedback.created_at.desc()).all()
    return [_feedback_to_dict(f) for f in fb]


@router.post("/feedback/", status_code=status.HTTP_201_CREATED)
def create_feedback(
    body: FeedbackCreate,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    fb = Feedback(
        event_id=body.event_id,
        user_id=body.user_id,
        corrected_behaviour=body.corrected_behaviour,
    )
    db.add(fb)
    db.commit()
    db.refresh(fb)
    return _feedback_to_dict(fb)


@router.get("/analytics/")
def get_analytics(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Return aggregated analytics scoped to user."""
    device_ids = get_accessible_device_ids(current_user, db)

    def _base_query():
        q = db.query(func.count(Observation.event_id))
        if device_ids:
            q = q.filter(Observation.device_id.in_(device_ids))
        return q

    total = _base_query().scalar() or 0
    low = (
        _base_query().filter(Observation.risk_level == "low").scalar()
        if device_ids else 0
    ) or 0
    medium = (
        _base_query().filter(Observation.risk_level == "medium").scalar()
        if device_ids else 0
    ) or 0
    high = (
        _base_query().filter(Observation.risk_level == "high").scalar()
        if device_ids else 0
    ) or 0

    # Animal breakdown
    animal_rows = (
        db.query(Observation.animal, func.count(Observation.event_id))
        .filter(Observation.device_id.in_(device_ids))
        .group_by(Observation.animal)
        .all()
        if device_ids
        else []
    )
    animal_breakdown = [{"animal": a, "count": c} for a, c in animal_rows]

    # Behaviour breakdown
    behaviour_rows = (
        db.query(Observation.behaviour, func.count(Observation.event_id))
        .filter(Observation.device_id.in_(device_ids))
        .group_by(Observation.behaviour)
        .all()
        if device_ids
        else []
    )
    behaviour_breakdown = [{"behaviour": b, "count": c} for b, c in behaviour_rows]

    return {
        "total_events": total,
        "low_risk": low,
        "medium_risk": medium,
        "high_risk": high,
        "animal_breakdown": animal_breakdown,
        "behaviour_breakdown": behaviour_breakdown,
    }


@router.get("/dashboard/summary")
def get_dashboard_summary(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Return dashboard summary scoped to user."""
    device_ids = get_accessible_device_ids(current_user, db)

    def _count_in_devices(col):
        q = db.query(func.count(col))
        if device_ids:
            q = q.filter(col.in_(device_ids))
        return q.scalar() or 0

    total_events = _count_in_devices(Observation.device_id)
    high_risk = (
        db.query(func.count(Observation.event_id))
        .filter(
            Observation.device_id.in_(device_ids),
            Observation.risk_level == "high",
        )
        .scalar()
        or 0
        if device_ids
        else 0
    )
    active_devices = (
        db.query(func.count(Device.device_id))
        .filter(Device.device_id.in_(device_ids), Device.status == "active")
        .scalar()
        or 0
        if device_ids
        else 0
    )
    deterrence_actions = (
        db.query(func.count(Observation.event_id))
        .filter(
            Observation.device_id.in_(device_ids),
            Observation.deterrence_action != "",
            Observation.deterrence_action != "monitor",
        )
        .scalar()
        or 0
        if device_ids
        else 0
    )

    return {
        "total_events": total_events,
        "high_risk_events": high_risk,
        "active_devices": active_devices,
        "deterrence_actions": deterrence_actions,
    }


# ── Farm endpoints ─────────────────────────────────────────────────────────────


@router.get("/farms/")
def list_farms(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """List farms accessible to the user."""
    farm_ids = get_accessible_farm_ids(current_user, db)
    farms = (
        db.query(Farm)
        .options(joinedload(Farm.devices))
        .filter(Farm.farm_id.in_(farm_ids))
        .all()
        if farm_ids
        else []
    )
    return [_farm_to_dict(f) for f in farms]


@router.post("/farms/", status_code=status.HTTP_201_CREATED)
def create_farm(
    body: FarmCreate,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Create a new farm (any user can create as farmer)."""
    farm = Farm(
        farm_name=body.farm_name,
        location=body.location,
        owner_id=current_user.user_id,
    )
    db.add(farm)
    db.commit()
    db.refresh(farm)
    return _farm_to_dict(farm)


@router.get("/farms/{farm_id}")
def get_farm(
    farm_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Get farm details."""
    farm = db.query(Farm).options(joinedload(Farm.devices)).filter(Farm.farm_id == farm_id).first()
    if not farm:
        raise HTTPException(status_code=404, detail="Farm not found")
    if farm.owner_id != current_user.user_id and not is_admin(current_user):
        raise HTTPException(status_code=403, detail="Not authorized")
    return _farm_to_dict(farm)


@router.put("/farms/{farm_id}")
def update_farm(
    farm_id: int,
    body: FarmUpdate,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Update a farm (owner or admin+)."""
    farm = db.query(Farm).filter(Farm.farm_id == farm_id).first()
    if not farm:
        raise HTTPException(status_code=404, detail="Farm not found")
    if farm.owner_id != current_user.user_id and not is_admin(current_user):
        raise HTTPException(status_code=403, detail="Not authorized")
    if body.farm_name is not None:
        farm.farm_name = body.farm_name
    if body.location is not None:
        farm.location = body.location
    db.commit()
    db.refresh(farm)
    return _farm_to_dict(farm)


@router.delete("/farms/{farm_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_farm(
    farm_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Delete a farm (owner or super_admin)."""
    farm = db.query(Farm).filter(Farm.farm_id == farm_id).first()
    if not farm:
        raise HTTPException(status_code=404, detail="Farm not found")
    if farm.owner_id != current_user.user_id and current_user.role != "super_admin":
        raise HTTPException(status_code=403, detail="Not authorized")
    db.delete(farm)
    db.commit()


# ── Device endpoints ───────────────────────────────────────────────────────────


@router.get("/devices/")
def list_devices(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Return devices scoped to user's accessible farms."""
    farm_ids = get_accessible_farm_ids(current_user, db)
    devices = (
        db.query(Device).filter(Device.farm_id.in_(farm_ids)).all()
        if farm_ids
        else []
    )
    return [_device_to_dict(d) for d in devices]


@router.post("/devices/", status_code=status.HTTP_201_CREATED)
def create_device(
    body: DeviceCreate,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Register a new device under a farm."""
    # Verify farm exists and user has access
    farm = db.query(Farm).filter(Farm.farm_id == body.farm_id).first()
    if not farm:
        raise HTTPException(status_code=404, detail="Farm not found")
    if farm.owner_id != current_user.user_id and not is_admin(current_user):
        raise HTTPException(status_code=403, detail="Not authorized")
    device = Device(
        farm_id=body.farm_id,
        device_name=body.device_name,
        location=body.location,
    )
    db.add(device)
    db.commit()
    db.refresh(device)
    return _device_to_dict(device)


@router.put("/devices/{device_id}")
def update_device(
    device_id: int,
    body: DeviceUpdate,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Update a device."""
    device = db.query(Device).filter(Device.device_id == device_id).first()
    if not device:
        raise HTTPException(status_code=404, detail="Device not found")
    # Check access via farm
    farm = db.query(Farm).filter(Farm.farm_id == device.farm_id).first()
    if farm and farm.owner_id != current_user.user_id and not is_admin(current_user):
        raise HTTPException(status_code=403, detail="Not authorized")
    if body.device_name is not None:
        device.device_name = body.device_name
    if body.location is not None:
        device.location = body.location
    if body.status is not None:
        device.status = body.status
    db.commit()
    db.refresh(device)
    return _device_to_dict(device)


@router.delete("/devices/{device_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_device(
    device_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Delete a device."""
    device = db.query(Device).filter(Device.device_id == device_id).first()
    if not device:
        raise HTTPException(status_code=404, detail="Device not found")
    farm = db.query(Farm).filter(Farm.farm_id == device.farm_id).first()
    if farm and farm.owner_id != current_user.user_id and current_user.role != "super_admin":
        raise HTTPException(status_code=403, detail="Not authorized")
    db.delete(device)
    db.commit()


# ── IoT deterrence endpoints ───────────────────────────────────────────────────


@router.post("/devices/{device_id}/deterrence")
def control_deterrence(
    device_id: int,
    body: DeterrenceRequest,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Turn siren/deterrence on or off for a device."""
    device = db.query(Device).filter(Device.device_id == device_id).first()
    if not device:
        raise HTTPException(status_code=404, detail="Device not found")
    # Check access via farm
    farm = db.query(Farm).filter(Farm.farm_id == device.farm_id).first()
    if farm and farm.owner_id != current_user.user_id and not is_admin(current_user):
        raise HTTPException(status_code=403, detail="Not authorized")

    action = body.action.lower()
    controller = get_deterrence_controller()
    if action == "on":
        return controller.activate(device_id)
    elif action == "off":
        return controller.deactivate(device_id)
    else:
        raise HTTPException(status_code=400, detail="Action must be 'on' or 'off'")


@router.get("/devices/{device_id}/deterrence/status")
def deterrence_status(
    device_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Get the current deterrence state for a device."""
    device = db.query(Device).filter(Device.device_id == device_id).first()
    if not device:
        raise HTTPException(status_code=404, detail="Device not found")
    farm = db.query(Farm).filter(Farm.farm_id == device.farm_id).first()
    if farm and farm.owner_id != current_user.user_id and not is_admin(current_user):
        raise HTTPException(status_code=403, detail="Not authorized")

    controller = get_deterrence_controller()
    return controller.status(device_id)


# ── Admin endpoints ────────────────────────────────────────────────────────────


@router.get("/admin/users")
def list_users(
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """List all users (admin/super_admin only)."""
    if current_user.role not in ("admin", "super_admin"):
        raise HTTPException(status_code=403, detail="Not authorized")
    users = db.query(User).all()
    return [
        {
            "user_id": u.user_id,
            "email": u.email,
            "display_name": u.display_name,
            "role": u.role,
            "created_at": u.created_at.isoformat() if u.created_at else "",
        }
        for u in users
    ]


@router.put("/admin/users/{user_id}/role")
def update_user_role(
    user_id: int,
    body: dict,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Change a user's role (super_admin only)."""
    if current_user.role != "super_admin":
        raise HTTPException(status_code=403, detail="Not authorized")
    new_role = body.get("role")
    if new_role not in ("farmer", "admin", "super_admin"):
        raise HTTPException(status_code=400, detail="Invalid role")
    user = db.query(User).filter(User.user_id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    user.role = new_role
    db.commit()
    return {"user_id": user_id, "role": new_role}


# ── Inference (unchanged logic, minor scoping) ────────────────────────────────


@router.post("/events/inference")
async def run_inference(
    device_id: int = Form(...),
    file: UploadFile = File(...),
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    """Upload an image/video for ML inference."""
    # Verify device access
    device = db.query(Device).filter(Device.device_id == device_id).first()
    if not device:
        raise HTTPException(status_code=404, detail="Device not found")
    farm = db.query(Farm).filter(Farm.farm_id == device.farm_id).first()
    if farm and farm.owner_id != current_user.user_id and not is_admin(current_user):
        raise HTTPException(status_code=403, detail="Not authorized")

    # Save uploaded file
    upload_dir = os.path.join(os.path.dirname(__file__), "..", "uploads")
    os.makedirs(upload_dir, exist_ok=True)

    ext = os.path.splitext(file.filename or "upload")[1] or ".bin"
    safe_name = f"{uuid.uuid4().hex}{ext}"
    dest = os.path.join(upload_dir, safe_name)

    content = await file.read()
    with open(dest, "wb") as f:
        f.write(content)

    # Record the upload
    upload_record = Upload(
        device_id=device_id,
        file_path=dest,
        original_name=file.filename or safe_name,
        mime_type=file.content_type or "",
        file_size=len(content),
    )
    db.add(upload_record)
    db.commit()

    # Run inference
    from app.inference import get_model

    try:
        model = get_model()
        result = model.infer(dest)
    except Exception as exc:
        logger.exception("Inference failed for %s", dest)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Inference error: {exc}",
        )

    # If a species was detected, create an observation + alert
    if result.get("decision") == "detected":
        import json
        bbox = result.get("bounding_box")
        obs = Observation(
            device_id=device_id,
            animal=result["detected_species"],
            behaviour=result["predicted_behaviour"],
            confidence=result.get("behaviour_confidence", 0.0),
            risk_level=result["risk_level"],
            deterrence_action=result["actions"][0] if result.get("actions") else "monitor",
            frame_path=os.path.basename(dest),
            bounding_box=json.dumps(bbox) if bbox else None,
            frame_width=result.get("frame_width"),
            frame_height=result.get("frame_height"),
            timestamp=datetime.now(timezone.utc),
        )
        db.add(obs)
        db.commit()
        db.refresh(obs)

        if result["risk_level"] in ("high", "medium"):
            location = device.location
            alert = Alert(
                event_id=obs.event_id,
                animal=result["detected_species"],
                behaviour=result["predicted_behaviour"],
                risk_level=result["risk_level"],
                confidence=result.get("behaviour_confidence", 0.0),
                location=location,
                status="open",
                deterrence_action=result["actions"][0] if result.get("actions") else "monitor",
            )
            db.add(alert)
            db.commit()

        return {
            "decision": "detected",
            "event_id": obs.event_id,
            "video_path": dest,
            "detected_species": result.get("detected_species"),
            "species_confidence": result.get("species_confidence"),
            "predicted_behaviour": result.get("predicted_behaviour"),
            "behaviour_confidence": result.get("behaviour_confidence"),
            "risk_level": result.get("risk_level"),
            "alert_type": result.get("alert_type"),
            "actions": result.get("actions"),
            "message": result.get("message"),
            "frame_path": dest,
            "bounding_box": result.get("bounding_box"),
            "frame_width": result.get("frame_width"),
            "frame_height": result.get("frame_height"),
        }

    if result.get("decision") == "no_target_species_detected":
        return {
            "decision": "no_target_species_detected",
            "video_path": dest,
            "bounding_box": result.get("bounding_box"),
            "frame_width": result.get("frame_width"),
            "frame_height": result.get("frame_height"),
        }

    return {
        "decision": None,
        "video_path": dest,
        "detected_species": result.get("detected_species"),
        "species_confidence": result.get("species_confidence"),
        "predicted_behaviour": result.get("predicted_behaviour"),
        "behaviour_confidence": result.get("behaviour_confidence"),
        "risk_level": result.get("risk_level"),
        "alert_type": result.get("alert_type"),
        "actions": result.get("actions"),
        "message": result.get("message"),
        "frame_path": dest,
        "bounding_box": result.get("bounding_box"),
        "frame_width": result.get("frame_width"),
        "frame_height": result.get("frame_height"),
    }
