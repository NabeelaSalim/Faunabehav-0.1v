"""
FaunaBahav — FastAPI application entry point.

Run with::

    cd /Users/karimmaige/Downloads/FaunaBahav/backend
    TORCH_USE_RTLD_GLOBAL=1 venv/bin/uvicorn app.main:app --reload --port 8000
"""

import os
import sys

# macOS workaround: set before importing any module that may load torch
if sys.platform == "darwin":
    os.environ.setdefault("TORCH_USE_RTLD_GLOBAL", "1")

import logging

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from app.database import Base, engine
from app.auth import auth_router
from app.api import router as data_router

# ── Logging ─────────────────────────────────────────────────────────────────────

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger(__name__)

# ── App factory ─────────────────────────────────────────────────────────────────

app = FastAPI(
    title="FaunaBahav API",
    version="0.1.0",
    description="Wildlife behaviour monitoring & deterrence management backend",
)

# ── CORS — allow all origins in dev ─────────────────────────────────────────────

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── Routers ─────────────────────────────────────────────────────────────────────

app.include_router(auth_router)
app.include_router(data_router)

# ── Static files: serve uploaded images / frames ─────────────────────────────

uploads_dir = os.path.join(os.path.dirname(__file__), "..", "uploads")
os.makedirs(uploads_dir, exist_ok=True)
app.mount("/frames", StaticFiles(directory=uploads_dir), name="frames")

# ── Startup ─────────────────────────────────────────────────────────────────────


@app.on_event("startup")
def on_startup():
    """Create tables if they don't exist yet."""
    Base.metadata.create_all(bind=engine)
    logger.info("Database tables ready")

    # Warm the model (swallow errors if torch is unavailable)
    try:
        from app.inference import get_model

        get_model()
        logger.info("Model loaded on startup")
    except Exception:
        logger.warning("Model not loaded on startup (torch unavailable?)")


# ── Health check ───────────────────────────────────────────────────────────────


@app.get("/health")
def health():
    return {"status": "ok"}
