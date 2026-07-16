from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func

from app.database import Base


class User(Base):
    __tablename__ = "users"

    user_id = Column(Integer, primary_key=True, autoincrement=True)
    email = Column(String(255), unique=True, nullable=False)
    password_hash = Column(String(255), nullable=False)
    display_name = Column(String(255), nullable=False)
    role = Column(String(20), nullable=False, default="farmer")
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    farms = relationship("Farm", back_populates="owner")
    feedbacks = relationship("Feedback", back_populates="user")


class Farm(Base):
    __tablename__ = "farms"

    farm_id = Column(Integer, primary_key=True, autoincrement=True)
    farm_name = Column(String(255), nullable=False)
    location = Column(String(255), nullable=False)
    owner_id = Column(Integer, ForeignKey("users.user_id"), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    owner = relationship("User", back_populates="farms")
    devices = relationship("Device", back_populates="farm")


class Device(Base):
    __tablename__ = "devices"

    device_id = Column(Integer, primary_key=True, autoincrement=True)
    farm_id = Column(Integer, ForeignKey("farms.farm_id"), nullable=False)
    device_name = Column(String(255), nullable=False)
    location = Column(String(255), nullable=False)
    status = Column(String(50), default="active")
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    farm = relationship("Farm", back_populates="devices")
    observations = relationship("Observation", back_populates="device")
    uploads = relationship("Upload", back_populates="device")


class Observation(Base):
    __tablename__ = "observations"

    event_id = Column(Integer, primary_key=True, autoincrement=True)
    device_id = Column(Integer, ForeignKey("devices.device_id"), nullable=False)
    animal = Column(String(50), nullable=False)
    behaviour = Column(String(50), nullable=False)
    confidence = Column(Float, nullable=False)
    risk_level = Column(String(20), nullable=False)
    deterrence_action = Column(String(255), default="")
    frame_path = Column(String(500), default="")
    bounding_box = Column(String(255), nullable=True)  # JSON: [x1,y1,x2,y2]
    frame_width = Column(Integer, nullable=True)
    frame_height = Column(Integer, nullable=True)
    timestamp = Column(DateTime(timezone=True), server_default=func.now())

    device = relationship("Device", back_populates="observations")
    alerts = relationship("Alert", back_populates="observation")
    feedbacks = relationship("Feedback", back_populates="observation")


class Alert(Base):
    __tablename__ = "alerts"

    alert_id = Column(Integer, primary_key=True, autoincrement=True)
    event_id = Column(Integer, ForeignKey("observations.event_id"), nullable=True)
    animal = Column(String(50), nullable=False)
    behaviour = Column(String(50), nullable=False)
    risk_level = Column(String(20), nullable=False)
    confidence = Column(Float, nullable=False)
    location = Column(String(255), nullable=False)
    status = Column(String(50), default="open")
    deterrence_action = Column(String(255), default="")
    timestamp = Column(DateTime(timezone=True), server_default=func.now())

    observation = relationship("Observation", back_populates="alerts")


class Feedback(Base):
    __tablename__ = "feedback"

    feedback_id = Column(Integer, primary_key=True, autoincrement=True)
    event_id = Column(Integer, ForeignKey("observations.event_id"), nullable=False)
    user_id = Column(Integer, ForeignKey("users.user_id"), nullable=False)
    corrected_behaviour = Column(String(50), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    observation = relationship("Observation", back_populates="feedbacks")
    user = relationship("User", back_populates="feedbacks")


class Upload(Base):
    __tablename__ = "uploads"

    upload_id = Column(Integer, primary_key=True, autoincrement=True)
    device_id = Column(Integer, ForeignKey("devices.device_id"), nullable=False)
    file_path = Column(String(500), nullable=False)
    original_name = Column(String(255), nullable=False)
    mime_type = Column(String(100), default="")
    file_size = Column(Integer, default=0)
    uploaded_at = Column(DateTime(timezone=True), server_default=func.now())

    device = relationship("Device", back_populates="uploads")
