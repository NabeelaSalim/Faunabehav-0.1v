from pydantic import BaseModel


class RegisterRequest(BaseModel):
    email: str
    password: str
    username: str


class LoginRequest(BaseModel):
    email: str
    password: str


class UserOut(BaseModel):
    user_id: int
    email: str
    username: str
    role: str


class AuthResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserOut
