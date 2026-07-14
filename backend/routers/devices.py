from fastapi import APIRouter

from supabase import create_client
from dotenv import load_dotenv
import os

load_dotenv()

print("SUPABASE URL:", os.getenv("SUPABASE_URL"))

supabase = create_client(
    os.getenv("SUPABASE_URL"),
    os.getenv("SUPABASE_KEY")
)

router = APIRouter()


@router.get("/")
def get_devices():

    response = (
        supabase
        .table("devices")
        .select("*")
        .execute()
    )

    print("DEVICES RESPONSE:")
    print(response.data)

    return response.data


@router.get("/debug")
def debug():

    response = (
        supabase
        .table("events")
        .select("*")
        .execute()
    )

    print("EVENTS RESPONSE:")
    print(response.data)

    return response.data