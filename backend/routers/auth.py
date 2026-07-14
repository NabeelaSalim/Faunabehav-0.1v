from fastapi import APIRouter, HTTPException

from schemas.auth_schema import RegisterRequest, LoginRequest, AuthResponse, UserOut
from services.database_service import get_user_by_email, create_user
from services.auth_service import hash_password, verify_password, create_access_token

router = APIRouter()


def _to_auth_response(user_row: dict) -> AuthResponse:
    token = create_access_token(user_row["user_id"], user_row["email"])
    return AuthResponse(
        access_token=token,
        user=UserOut(
            user_id=user_row["user_id"],
            email=user_row["email"],
            username=user_row["username"],
            role=user_row["role"],
        ),
    )


@router.post("/register", response_model=AuthResponse, status_code=201)
def register(payload: RegisterRequest):

    if get_user_by_email(payload.email):
        raise HTTPException(status_code=409, detail="An account with this email already exists")

    user_row = create_user({
        "email": payload.email,
        "username": payload.username,
        "password_hash": hash_password(payload.password),
        "role": "farmer",
    })

    return _to_auth_response(user_row)


@router.post("/login", response_model=AuthResponse)
def login(payload: LoginRequest):

    user_row = get_user_by_email(payload.email)

    if not user_row or not verify_password(payload.password, user_row["password_hash"]):
        raise HTTPException(status_code=401, detail="Invalid email or password")

    return _to_auth_response(user_row)
