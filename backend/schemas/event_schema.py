from pydantic import BaseModel


class InferenceRequest(BaseModel):
    device_id: int
    video_path: str


class InferenceResponse(BaseModel):
    detected_species: str
    species_confidence: float

    predicted_behaviour: str
    behaviour_confidence: float

    risk_level: str
    alert_type: str

    actions: list[str]
    message: str