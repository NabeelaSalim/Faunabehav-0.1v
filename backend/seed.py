"""Seed script for FaunaBehav database.

Creates demo users (farmer, admin, super_admin), farms, devices,
observations, alerts, and feedback for development/testing.

Usage:
    cd /Users/karimmaige/Downloads/FaunaBahav/backend
    venv/bin/python seed.py
"""

from datetime import datetime, timedelta, timezone

from app.database import SessionLocal, engine, Base
from app.auth import hash_password
from app.models import User, Farm, Device, Observation, Alert, Feedback


def seed():
    Base.metadata.create_all(bind=engine)

    session = SessionLocal()

    try:
        # ── Users ──────────────────────────────────────────────────────────────
        if not session.query(User).first():
            demo_farmer = User(
                email="demo@faunabehav.com",
                display_name="Demo Farmer",
                password_hash=hash_password("password123"),
                role="farmer",
            )
            demo_admin = User(
                email="manager@faunabehav.com",
                display_name="Farm Manager",
                password_hash=hash_password("manager123"),
                role="admin",
            )
            demo_super = User(
                email="admin@faunabehav.com",
                display_name="Super Admin",
                password_hash=hash_password("admin123"),
                role="super_admin",
            )
            session.add_all([demo_farmer, demo_admin, demo_super])
            session.flush()
            print(f"Seeded 3 users (farmer, admin, super_admin)")
        else:
            demo_farmer = session.query(User).filter_by(email="demo@faunabehav.com").first()
            session.query(User).filter_by(email="manager@faunabehav.com").first()
            session.query(User).filter_by(email="admin@faunabehav.com").first()
            print("Users already exist, skipping")

        # ── Farms ──────────────────────────────────────────────────────────────
        if not session.query(Farm).first() and demo_farmer:
            farms = [
                Farm(farm_name="Shamba la Mbugani", location="Sector 7G", owner_id=demo_farmer.user_id),
                Farm(farm_name="Shamba la Kilimo", location="Sector 4F", owner_id=demo_farmer.user_id),
            ]
            session.add_all(farms)
            session.flush()
            print(f"Seeded {len(farms)} farms")
        else:
            print("Farms already exist, skipping")

        # ── Devices (under farms) ──────────────────────────────────────────────
        if not session.query(Device).first():
            farm_1 = session.query(Farm).filter_by(farm_name="Shamba la Mbugani").first()
            farm_2 = session.query(Farm).filter_by(farm_name="Shamba la Kilimo").first()
            if farm_1 and farm_2:
                devices = [
                    Device(
                        device_id=1, farm_id=farm_1.farm_id,
                        device_name="Camera Trap Alpha", location="Sector 7G - North", status="active",
                    ),
                    Device(
                        device_id=2, farm_id=farm_2.farm_id,
                        device_name="Camera Trap Beta", location="Sector 4F - East", status="active",
                    ),
                ]
                session.add_all(devices)
                session.flush()
                print(f"Seeded {len(devices)} devices")
            else:
                print("Farms not found for device seeding")
        else:
            print("Devices already exist, skipping")

        # ── Observations ──────────────────────────────────────────────────────
        now = datetime.now(timezone.utc)

        if not session.query(Observation).first():
            observations = [
                Observation(
                    device_id=1, animal="monkey", behaviour="feeding_foraging",
                    confidence=0.92, risk_level="low", deterrence_action="monitor",
                    timestamp=now - timedelta(hours=3),
                ),
                Observation(
                    device_id=1, animal="wild_boar", behaviour="locomotion",
                    confidence=0.88, risk_level="low", deterrence_action="monitor",
                    timestamp=now - timedelta(hours=5),
                ),
                Observation(
                    device_id=2, animal="bird", behaviour="feeding_foraging",
                    confidence=0.95, risk_level="low", deterrence_action="monitor",
                    timestamp=now - timedelta(hours=6),
                ),
                Observation(
                    device_id=1, animal="monkey", behaviour="aggressive_destructive",
                    confidence=0.78, risk_level="high", deterrence_action="active_deterrence",
                    timestamp=now - timedelta(hours=8),
                ),
                Observation(
                    device_id=2, animal="wild_boar", behaviour="vigilance_alert",
                    confidence=0.85, risk_level="medium", deterrence_action="warning",
                    timestamp=now - timedelta(hours=10),
                ),
                Observation(
                    device_id=1, animal="bird", behaviour="locomotion",
                    confidence=0.72, risk_level="low", deterrence_action="monitor",
                    timestamp=now - timedelta(hours=12),
                ),
                Observation(
                    device_id=1, animal="monkey", behaviour="social_interaction",
                    confidence=0.91, risk_level="medium", deterrence_action="warning",
                    timestamp=now - timedelta(days=1),
                ),
                Observation(
                    device_id=2, animal="wild_boar", behaviour="aggressive_destructive",
                    confidence=0.82, risk_level="high", deterrence_action="active_deterrence",
                    timestamp=now - timedelta(days=1),
                ),
                Observation(
                    device_id=2, animal="monkey", behaviour="resting_passive",
                    confidence=0.94, risk_level="low", deterrence_action="monitor",
                    timestamp=now - timedelta(days=2),
                ),
                Observation(
                    device_id=1, animal="bird", behaviour="feeding_foraging",
                    confidence=0.89, risk_level="low", deterrence_action="monitor",
                    timestamp=now - timedelta(days=2),
                ),
            ]
            session.add_all(observations)
            session.flush()
            print(f"Seeded {len(observations)} observations")
        else:
            print("Observations already exist, skipping")

        # ── Alerts ────────────────────────────────────────────────────────────
        if not session.query(Alert).first():
            obs_4 = session.query(Observation).filter_by(event_id=4).first()
            obs_8 = session.query(Observation).filter_by(event_id=8).first()
            obs_5 = session.query(Observation).filter_by(event_id=5).first()

            alerts = [
                Alert(
                    event_id=obs_4.event_id if obs_4 else 4,
                    animal="monkey", behaviour="aggressive_destructive",
                    risk_level="high", confidence=0.78,
                    location="Sector 7G", status="open",
                    deterrence_action="active_deterrence",
                ),
                Alert(
                    event_id=obs_8.event_id if obs_8 else 8,
                    animal="wild_boar", behaviour="aggressive_destructive",
                    risk_level="high", confidence=0.82,
                    location="Sector 4F", status="open",
                    deterrence_action="active_deterrence",
                ),
                Alert(
                    event_id=obs_5.event_id if obs_5 else 5,
                    animal="wild_boar", behaviour="vigilance_alert",
                    risk_level="medium", confidence=0.85,
                    location="Sector 4F", status="acknowledged",
                    deterrence_action="warning",
                ),
            ]
            session.add_all(alerts)
            session.flush()
            print(f"Seeded {len(alerts)} alerts")
        else:
            print("Alerts already exist, skipping")

        # ── Feedback ──────────────────────────────────────────────────────────
        if not session.query(Feedback).first() and demo_farmer:
            obs_for_feedback = session.query(Observation).filter_by(event_id=4).first()
            feedback = Feedback(
                event_id=obs_for_feedback.event_id if obs_for_feedback else 4,
                user_id=demo_farmer.user_id,
                corrected_behaviour="feeding_foraging",
            )
            session.add(feedback)
            session.flush()
            print("Seeded 1 feedback")
        else:
            print("Feedback already exists, skipping")

        session.commit()
        print("\nSeed complete!")

    except Exception:
        session.rollback()
        raise
    finally:
        session.close()


if __name__ == "__main__":
    seed()
