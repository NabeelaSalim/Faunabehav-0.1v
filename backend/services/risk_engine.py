def calculate_risk(behaviour: str, confidence: float):

    high_behaviours = [
        "aggressive_destructive",
        "vigilance_alert"
    ]

    medium_behaviours = [
        "feeding_foraging",
        "locomotion"
    ]

    # High Risk
    if behaviour in high_behaviours:
        return "High"

    # Medium Risk
    if behaviour in medium_behaviours:
        if confidence >= 0.70:
            return "Medium"
        return "Low"

    # Default
    return "Low"