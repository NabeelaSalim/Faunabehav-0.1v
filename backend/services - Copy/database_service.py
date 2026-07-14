from supabase import create_client
from dotenv import load_dotenv
import os

load_dotenv()

supabase = create_client(
    os.getenv("SUPABASE_URL"),
    os.getenv("SUPABASE_KEY")
)


def save_observation(data):

    print("DATA RECEIVED:")
    print(data)

    response = (
        supabase
        .table("events")
        .insert(data)
        .execute()
    )

    print("SUPABASE RESPONSE:")
    print(response)

    return response


def save_alert(data):

    print("ALERT DATA:")
    print(data)

    response = (
        supabase
        .table("alerts")
        .insert(data)
        .execute()
    )

    print("ALERT RESPONSE:")
    print(response)

    return response

def save_feedback(data):

    response = (
        supabase
        .table("feedback")
        .insert(data)
        .execute()
    )

    return response

def get_devices():

    response = (
        supabase
        .table("devices")
        .select("*")
        .execute()
    )

    return response.data

def get_events():

    response = (
        supabase
        .table("events")
        .select("*")
        .execute()
    )

    return response.data


def get_alerts():

    response = (
        supabase
        .table("alerts")
        .select("*")
        .execute()
    )

    return response.data