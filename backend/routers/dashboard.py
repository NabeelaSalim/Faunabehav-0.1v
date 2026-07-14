from fastapi import APIRouter

from services.database_service import (
    get_events,
    get_alerts,
    get_devices
)

router = APIRouter(
    prefix="/dashboard",
    tags=["Dashboard"]
)


@router.get("/summary")
def get_dashboard_summary():

    events = get_events()
    alerts = get_alerts()
    devices = get_devices()

    print("\n===== DASHBOARD DEBUG =====")
    print("EVENTS:", events)
    print("ALERTS:", alerts)
    print("DEVICES:", devices)
    print("==========================\n")

    total_events = len(events)

    high_risk_events = len([
        event for event in events
        if str(event.get("risk_level", "")).strip().lower() == "high"
    ])

    active_devices = len([
        device for device in devices
        if str(device.get("status", "")).strip().lower() == "active"
    ])

    deterrence_actions = len(alerts)

    return {
        "total_events": total_events,
        "high_risk_events": high_risk_events,
        "active_devices": active_devices,
        "deterrence_actions": deterrence_actions
    }