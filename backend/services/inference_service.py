from services.risk_engine import calculate_risk
from services.deterrence_engine import get_deterrence_action


def process_inference(
    animal: str,
    behaviour: str,
    confidence: float
):

    risk_level = calculate_risk(
        behaviour,
        confidence
    )

    deterrence_action = get_deterrence_action(risk_level)

    result = {
        "animal": animal,
        "behaviour": behaviour,
        "confidence": confidence,
        "risk_level": risk_level,
        "deterrence_action": deterrence_action
    }

    return result