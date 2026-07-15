"""
IoT Deterrence Controller for FaunaBahav.

Provides an abstraction layer for controlling deterrence devices (sirens, etc.)
attached to camera traps. Supports mock mode (for development) and can be
extended to real hardware or Wokwi simulation.

Usage:
    controller = get_deterrence_controller()
    controller.activate(device_id=1)
    controller.deactivate(device_id=1)
    controller.status(device_id=1)  # -> {"active": True, "device_id": 1}
"""

import os
import logging
from abc import ABC, abstractmethod
from typing import Optional

logger = logging.getLogger(__name__)


class DeterrenceController(ABC):
    """Abstract interface for deterrence device control."""

    @abstractmethod
    def activate(self, device_id: int) -> dict:
        """Turn on the deterrence mechanism for a device."""
        ...

    @abstractmethod
    def deactivate(self, device_id: int) -> dict:
        """Turn off the deterrence mechanism for a device."""
        ...

    @abstractmethod
    def status(self, device_id: int) -> dict:
        """Get the current deterrence state for a device."""
        ...


class MockDeterrenceController(DeterrenceController):
    """In-memory mock controller — logs actions, no hardware needed."""

    def __init__(self):
        self._states: dict[int, bool] = {}

    def activate(self, device_id: int) -> dict:
        self._states[device_id] = True
        logger.info("DETERRENCE ON  | device_id=%d", device_id)
        return {"device_id": device_id, "action": "on", "success": True}

    def deactivate(self, device_id: int) -> dict:
        self._states[device_id] = False
        logger.info("DETERRENCE OFF | device_id=%d", device_id)
        return {"device_id": device_id, "action": "off", "success": True}

    def status(self, device_id: int) -> dict:
        active = self._states.get(device_id, False)
        return {"device_id": device_id, "active": active}


class WokwiDeterrenceController(DeterrenceController):
    """
    Controller that sends HTTP requests to a Wokwi-simulated ESP32.

    To use:
        1. Set env var ``WOKWI_ESP32_URL`` to the base URL of the ESP32
           (e.g. ``http://192.168.1.100:80``).
        2. Set env var ``DETERRENCE_MODE=wokwi``.

    The ESP32 should expose:
        GET  /control/{device_id}/on   → turns siren on
        GET  /control/{device_id}/off  → turns siren off
        GET  /control/{device_id}/status → returns {"active": true/false}
    """

    def __init__(self, base_url: str = None):
        import httpx

        self.base_url = (base_url or os.getenv("WOKWI_ESP32_URL", "http://localhost:8080")).rstrip("/")
        self._client = httpx.Client(timeout=5.0)

    def activate(self, device_id: int) -> dict:
        url = f"{self.base_url}/control/{device_id}/on"
        try:
            resp = self._client.get(url)
            resp.raise_for_status()
            logger.info("Wokwi ON  | device_id=%d → %s", device_id, url)
            return {"device_id": device_id, "action": "on", "success": True}
        except Exception as exc:
            logger.error("Wokwi ON  | device_id=%d FAILED: %s", device_id, exc)
            return {"device_id": device_id, "action": "on", "success": False, "error": str(exc)}

    def deactivate(self, device_id: int) -> dict:
        url = f"{self.base_url}/control/{device_id}/off"
        try:
            resp = self._client.get(url)
            resp.raise_for_status()
            logger.info("Wokwi OFF | device_id=%d → %s", device_id, url)
            return {"device_id": device_id, "action": "off", "success": True}
        except Exception as exc:
            logger.error("Wokwi OFF | device_id=%d FAILED: %s", device_id, exc)
            return {"device_id": device_id, "action": "off", "success": False, "error": str(exc)}

    def status(self, device_id: int) -> dict:
        url = f"{self.base_url}/control/{device_id}/status"
        try:
            resp = self._client.get(url)
            resp.raise_for_status()
            data = resp.json()
            return {"device_id": device_id, "active": data.get("active", False)}
        except Exception as exc:
            logger.error("Wokwi STATUS | device_id=%d FAILED: %s", device_id, exc)
            return {"device_id": device_id, "active": False, "error": str(exc)}


# ── Singleton factory ──────────────────────────────────────────────────────────

_controller: Optional[DeterrenceController] = None


def get_deterrence_controller() -> DeterrenceController:
    """Return the configured deterrence controller (singleton)."""
    global _controller
    if _controller is None:
        mode = os.getenv("DETERRENCE_MODE", "mock").lower()
        if mode == "wokwi":
            _controller = WokwiDeterrenceController()
            logger.info("Deterrence controller: Wokwi (ESP32)")
        else:
            _controller = MockDeterrenceController()
            logger.info("Deterrence controller: Mock (in-memory)")
    return _controller
