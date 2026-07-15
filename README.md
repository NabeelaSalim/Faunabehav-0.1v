# FaunaBahav — AI-Powered Wildlife Monitoring & Deterrence

AI-powered wildlife behavior monitoring platform. Detects animals (Monkey, Wild Boar, Bird) from camera trap footage, classifies behavior, assigns risk levels, and controls siren deterrence.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | FastAPI + SQLAlchemy + PostgreSQL |
| ML | PyTorch R3D-18 (video classification) |
| Frontend | Kotlin Multiplatform + Compose Multiplatform |
| Targets | Android, Web (Wasm), iOS, Desktop (JVM) |
| IoT | Mock controller (Wokwi-ready) |

## Architecture

`/app/shared` — Shared Compose UI & logic (all platforms)
`/app/androidApp` — Android app
`/app/webApp` — Web app (Wasm)
`/app/iosApp` — iOS app (requires Xcode)
`/app/desktopApp` — Desktop (JVM) app
`/core` — Domain models (User, Farm, Device, Observation, Alert, etc.)
`/backend` — FastAPI + ML inference (Python)
`/server` — Ktor server (Kotlin)

## Running

### Prerequisites
- **Backend**: Python 3.9+ virtual env with `requirements.txt`
- **Frontend**: JDK 17+, Android SDK (for Android builds)

### Start both services

```bash
# Terminal 1 — Backend (port 8000)
cd backend && source venv/bin/activate && uvicorn app.main:app --reload --port 8000

# Terminal 2 — Web app (port 8080)
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun
```

### Commands

| Target | Command |
|--------|---------|
| Web app | `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun` |
| Android | `./gradlew :app:androidApp:assembleDebug` |
| Desktop | `./gradlew :app:desktopApp:run` |
| Server | `./gradlew :server:run` |
| Backend | `cd backend && uvicorn app.main:app --reload --port 8000` |
| Seed DB | `cd backend && python seed.py` |

### Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Farmer | `demo@faunabehav.com` | `password123` |
| Admin | `manager@faunabehav.com` | `manager123` |
| Super Admin | `admin@faunabehav.com` | `admin123` |

## API Docs

Swagger UI at http://localhost:8000/docs (when backend is running).

Built with [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform),
[FastAPI](https://fastapi.tiangolo.com/).
