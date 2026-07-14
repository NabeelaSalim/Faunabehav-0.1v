from fastapi import APIRouter

from supabase import create_client
from dotenv import load_dotenv
import os

load_dotenv()

supabase = create_client(
    os.getenv("SUPABASE_URL"),
    os.getenv("SUPABASE_KEY")
)

router = APIRouter()


@router.get("/")
def get_alerts():

    alerts_response = (
        supabase
        .table("alerts")
        .select("*")
        .order("alert_id", desc=True)
        .execute()
    )

    alerts = alerts_response.data

    formatted_alerts = []

    for alert in alerts:

        event_response = (
            supabase
            .table("events")
            .select("*")
            .eq("event_id", alert["event_id"])
            .single()
            .execute()
        )

        event = event_response.data

        device_response = (
            supabase
            .table("devices")
            .select("*")
            .eq("device_id", event["device_id"])
            .single()
            .execute()
        )

        device = device_response.data

        formatted_alerts.append({
            "alert_id": alert["alert_id"],
            "animal": event["animal"],
            "behaviour": event["behaviour"],
            "risk_level": alert["risk_level"],
            "confidence": event["confidence"],
            "location": device["location"],
            "timestamp": alert["triggered_at"],
            "status": alert["status"],
            "deterrence_action": alert["deterrence_action"]
        })

    return formatted_alerts