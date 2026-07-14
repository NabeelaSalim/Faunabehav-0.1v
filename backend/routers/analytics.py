from collections import Counter

from fastapi import APIRouter

from services.database_service import get_events

router = APIRouter()


@router.get("/")
def get_analytics():

    events = get_events() or []

    low_risk = len([
        e for e in events
        if str(e.get("risk_level", "")).lower() == "low"
    ])

    medium_risk = len([
        e for e in events
        if str(e.get("risk_level", "")).lower() == "medium"
    ])

    high_risk = len([
        e for e in events
        if str(e.get("risk_level", "")).lower() == "high"
    ])

    animal_counter = Counter()
    behaviour_counter = Counter()

    for event in events:
        animal = event.get("animal")
        behaviour = event.get("behaviour")

        if animal:
            animal_counter[animal] += 1

        if behaviour:
            behaviour_counter[behaviour] += 1

    animal_breakdown = [
        {
            "animal": animal,
            "count": count
        }
        for animal, count in animal_counter.items()
    ]

    behaviour_breakdown = [
        {
            "behaviour": behaviour,
            "count": count
        }
        for behaviour, count in behaviour_counter.items()
    ]

    return {
        "total_events": len(events),
        "low_risk": low_risk,
        "medium_risk": medium_risk,
        "high_risk": high_risk,
        "animal_breakdown": animal_breakdown,
        "behaviour_breakdown": behaviour_breakdown,
    }