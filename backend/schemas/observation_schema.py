from pydantic import BaseModel


class ObservationCreate(BaseModel):
    device_id: int
    animal: str
    behaviour: str
    confidence: float
    risk_level: str
    deterrence_action: str
    frame_path: str


class ObservationResponse(BaseModel):
    device_id: int
    animal: str
    behaviour: str
    confidence: float
    risk_level: str
    deterrence_action: str
    frame_path: str