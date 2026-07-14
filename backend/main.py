from fastapi.middleware.cors import CORSMiddleware
from fastapi import FastAPI
from routers.events import router as events_router
from routers.observations import router as observations_router
from routers.alerts import router as alerts_router
from routers.devices import router as devices_router
from routers.feedback import router as feedback_router
from routers.dashboard import router as dashboard_router
from routers import analytics
from routers.auth import router as auth_router
from fastapi.staticfiles import StaticFiles

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:8083",
        "http://127.0.0.1:8083",
        "http://localhost:8080",
        "http://127.0.0.1:8080",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.mount(
    "/frames",
    StaticFiles(directory="frames"),
    name="frames"
)

app.include_router(
    events_router,
    prefix="/events",
    tags=["Events"]
)
app.include_router(
    observations_router,
    prefix="/observations",
    tags=["Observations"]
)
app.include_router(
    alerts_router,
    prefix="/alerts",
    tags=["Alerts"]
)
app.include_router(
    devices_router,
    prefix="/devices",
    tags=["Devices"]
)
app.include_router(
    feedback_router,
    prefix="/feedback",
    tags=["Feedback"]
)
app.include_router(dashboard_router)
app.include_router(
    analytics.router,
    prefix="/analytics",
    tags=["Analytics"]
)
app.include_router(
    auth_router,
    prefix="/auth",
    tags=["Auth"]
)

@app.get("/")
def root():
    return {"message": "FaunaBehav Backend Running"}

@app.get("/health")
def health_check():

    return {
        "status": "healthy",
        "backend": "online",
        "database": "connected"
    }