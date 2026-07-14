from fastapi import APIRouter

from schemas.observation_schema import (
    ObservationCreate,
    ObservationResponse
)

from services.database_service import (
    save_observation,
    save_alert,
    get_events
)

from supabase import create_client
from dotenv import load_dotenv
import os

load_dotenv()

supabase = create_client(
    os.getenv("SUPABASE_URL"),
    os.getenv("SUPABASE_KEY")
)

router = APIRouter()


# ---------------------------------------
# GET ALL OBSERVATIONS
# ---------------------------------------
@router.get("/")
def get_observations():

    events = get_events()

    events.sort(
        key=lambda x: x["event_id"],
        reverse=True
    )

    return events


# ---------------------------------------
# CREATE OBSERVATION
# ---------------------------------------
@router.post(
    "/",
    response_model=ObservationResponse
)
def create_observation(
    observation: ObservationCreate
):

    save_observation(
        observation.model_dump()
    )

    if observation.risk_level == "High":

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
            "risk_level": observation.risk_level,
            "deterrence_action": observation.deterrence_action,
            "status": "Active"
        }

        save_alert(alert_data)

    return observation