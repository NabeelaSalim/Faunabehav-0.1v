def get_deterrence_action(risk_level: str):

    if risk_level == "Low":
        return "Monitor"

    if risk_level == "Medium":
        return "Flash Light"

    if risk_level == "High":
        return "Siren"

    return "Monitor"