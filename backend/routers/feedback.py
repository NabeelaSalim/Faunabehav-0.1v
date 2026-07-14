from fastapi import APIRouter
from schemas.feedback_schema import (
    FeedbackCreate,
    FeedbackResponse
)
from services.database_service import (
    save_feedback,
    get_feedback
)

router = APIRouter()


@router.post(
    "/",
    response_model=FeedbackResponse
)
def create_feedback(
    feedback: FeedbackCreate
):

    save_feedback(
        feedback.model_dump()
    )

    return feedback

@router.get("/")
def read_feedback():

    return get_feedback()