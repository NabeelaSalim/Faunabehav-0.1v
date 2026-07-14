from pydantic import BaseModel


class AlertCreate(BaseModel):
    event_id: int
    risk_level: str
    deterrence_action: str
    status: str = "Active"


class AlertResponse(BaseModel):
    event_id: int
    risk_level: str
    deterrence_action: str
    status: str