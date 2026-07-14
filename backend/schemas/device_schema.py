from pydantic import BaseModel

class DeviceCreate(BaseModel):
    device_name: str
    location: str

class DeviceResponse(BaseModel):
    device_id: int
    device_name: str
    location: str
    status: str