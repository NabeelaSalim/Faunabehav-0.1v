from pydantic import BaseModel


class FeedbackCreate(BaseModel):
    event_id: int
    user_id: int
    corrected_behaviour: str

class FeedbackResponse(BaseModel):
    event_id: int
    user_id: int
    corrected_behaviour: str