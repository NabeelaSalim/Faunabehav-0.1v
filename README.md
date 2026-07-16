# FaunaBahav — AI-Powered Wildlife Monitoring & Deterrence

AI-powered wildlife behavior monitoring platform. Detects animals (Monkey, Wild Boar, Bird) from camera trap footage, classifies behavior, assigns risk levels, and controls siren deterrence.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | FastAPI + SQLAlchemy + PostgreSQL |
| ML | PyTorch R3D-18 (video classification) + YOLO (species detection) |
| Frontend | Kotlin Multiplatform + Compose Multiplatform |
| Targets | Android, Web (Wasm), iOS, Desktop (JVM) |
| IoT | Mock controller (Wokwi-ready) |

## Architecture

| Path | Purpose |
|------|---------|
| `/app/shared` | Shared Compose UI & logic (all platforms) |
| `/app/androidApp` | Android app entry point |
| `/app/webApp` | Web app (Wasm) |
| `/app/iosApp` | iOS app (requires Xcode) |
| `/app/desktopApp` | Desktop (JVM) app |
| `/core` | Domain models (User, Farm, Device, Observation, Alert, etc.) |
| `/backend` | FastAPI + ML inference (Python) |
| `/server` | Ktor server (Kotlin) — optional alternative backend |

---

## Current System Implementation Status

### ✅ Backend (FastAPI) — Complete

- **Authentication** — JWT-based login/register with role-based access (farmer, admin, super_admin)
- **SQLAlchemy models** — User, Farm, Device, Observation, Alert, Feedback, Upload
- **RESTful routers** — `/auth`, `/users`, `/devices`, `/farms`, `/observations`, `/alerts`, `/analytics`, `/dashboard`, `/events`, `/feedback`
- **ML inference pipeline** — PyTorch R3D-18 video classification + YOLO species detection, bounding box output
- **Deterrence engine** — Siren control logic triggered by risk level (HIGH / CRITICAL)
- **IoT controller** — Mock siren endpoint (`/devices/{id}/siren`)
- **Seed script** — `seed_demo.py` runs ML pipeline on sample images to populate the DB
- **Static file serving** — `/frames` mount serves uploaded images

### ✅ Frontend (KMP + Compose Multiplatform) — Complete

- **Login / Sign-up screens** — Email + password auth with role-based redirect
- **Dashboard** — Stats cards (total observations, alerts, active devices), live detection grid, recent alerts panel, device status panel, recent observations table, detection history table
- **Observations screen** — Filterable grid of observation cards with pagination, species/behaviour/risk badges, detail modal with bounding box overlay
- **Alerts screen** — Filterable alert list with severity/category/status filters, detail panel with action controls
- **Analytics screen** — Charts: activity trends, risk distribution, category breakdown, events over time, deterrence overview (via KoalaPlot)
- **Devices screen** — Device management with status indicators
- **Farms screen** — Farm management UI
- **Inference screen** — Upload media for ML analysis with results panel
- **Feedback screen** — Submit and view feedback on observations
- **Settings screen** — User profile and preferences
- **Responsive layout** — Bottom nav on mobile, sidebar on tablet/desktop
- **Platform-specific implementations** — File picker, session storage, settings storage, notifications (Android, iOS, JVM, Web, Wasm)

### ✅ Android App — Buildable

- AndroidManifest with INTERNET + POST_NOTIFICATIONS permissions
- `minSdk = 34`, `targetSdk = 35`, `compileSdk = 35`
- Network security config allows cleartext for `10.0.2.2` (Android emulator → host loopback)
- Activity Compose with Material 3 theme
- APK can be produced via `./gradlew :app:androidApp:assembleDebug`

### 🔧 Web App — Buildable

- Wasm target compiles and runs in the browser via `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`

### 🔧 Backend API — Fully functional

- Swagger UI at `http://localhost:8000/docs`
- Health check at `GET /health`

---

## Running the Android App

You can run the Android app via **Android Studio** (recommended) or **Genymotion** (alternative emulator).

### Prerequisites (all options)

1. **JDK 17+** installed
2. **Android Studio** installed with Android SDK (API 35)
3. Clone the repo and open it in Android Studio — Gradle will sync automatically

### Option A — Android Studio (official emulator)

1. Open the project in **Android Studio**
2. Wait for Gradle sync to finish
3. Create an Android Virtual Device (AVD) via **Device Manager**:
   - Pixel 8 or similar with API 35
4. Select the `app.androidApp` run configuration
5. Click **Run ▶** (or `^R`)

   > **Note:** The app connects to `10.0.2.2:8000` by default (standard Android emulator → host loopback). Make sure the backend is running on port 8000.

### Option B — Genymotion (alternative emulator)

[Genymotion](https://www.genymotion.com/) is a faster, feature-rich Android emulator.

1. **Install Genymotion**:
   - Download from [genymotion.com](https://www.genymotion.com/download/)
   - Install and launch Genymotion Desktop
   - (Optional) Install the [Genymotion plugin](https://www.genymotion.com/plugins/) in Android Studio for seamless integration

2. **Create a virtual device**:
   - Open Genymotion Desktop
   - Click **Add** → choose a device (e.g., Google Pixel 8, API 35)
   - Download and start the device

3. **Connect the running Genymotion device to Android Studio**:

   **Via ADB (automatic):**
   ```bash
   # Genymotion devices are usually auto-detected by ADB
   adb devices
   ```
   You should see `192.168.56.101:5555` or similar. If not:
   ```bash
   adb connect 192.168.56.101:5555
   ```

   **Via the Genymotion Android Studio plugin:**
   - Install the plugin: *Android Studio → Settings → Plugins → Marketplace → "Genymotion"*
   - A Genymotion icon appears in the toolbar — click it to see running devices
   - Select your device and click **Run**

4. **Run the app**:
   - Select the Genymotion device from the dropdown in Android Studio
   - Click **Run ▶**

   > **Important:** Genymotion uses a different IP than the AVD. Update the backend URL in `app/shared/src/androidMain/kotlin/com/example/faunabahav/data/remote/BaseUrl.android.kt`:
   > ```kotlin
   > // For Genymotion, use the host machine's actual LAN IP (e.g., 192.168.x.x) or:
   > actual fun baseUrl(): String = "http://10.0.3.2:8000"  // Genymotion's host loopback
   > ```
   > - `10.0.3.2` is Genymotion's default gateway to the host
   > - Alternatively, use your machine's LAN IP if the backend is on the same network

### Option C — APK install (no Android Studio)

Build the APK and install it on any connected device/emulator:

```bash
# Build debug APK
./gradlew :app:androidApp:assembleDebug

# Find the APK at:
# app/androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Install on connected device
adb install app/androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

---

## Running the Full System

### Start the Backend (required for all frontends)

```bash
cd backend
python3 -m venv venv           # first time only
source venv/bin/activate
pip install -r requirements.txt  # first time only
TORCH_USE_RTLD_GLOBAL=1 uvicorn app.main:app --reload --port 8000
```

> **macOS note:** `TORCH_USE_RTLD_GLOBAL=1` is set automatically by the app — the manual export above is a fallback.

### Seed the Database (optional)

```bash
cd backend && source venv/bin/activate && python seed_demo.py
```

This populates the database with sample observations, devices, farms, and alerts.

### Start a Frontend

| Target | Command |
|--------|---------|
| **Web app** (port 8080) | `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun` |
| **Android** | See "Running the Android App" above |
| **Desktop** (JVM) | `./gradlew :app:desktopApp:run` |
| **Ktor Server** | `./gradlew :server:run` |

### Demo Credentials

| Role | Email | Password |
|------|-------|----------|
| Farmer | `demo@faunabehav.com` | `password123` |
| Admin | `manager@faunabehav.com` | `manager123` |
| Super Admin | `admin@faunabehav.com` | `admin123` |

## API Docs

Swagger UI at http://localhost:8000/docs (when backend is running).

## Key Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/auth/login` | Login (returns JWT) |
| POST | `/auth/register` | Register new user |
| GET | `/dashboard/summary` | Dashboard stats |
| GET | `/observations` | List observations (paginated, filterable) |
| GET | `/alerts` | List alerts (filterable) |
| GET | `/analytics/summary` | Analytics aggregations |
| POST | `/devices/{id}/siren` | Trigger siren deterrence |
| POST | `/inference/analyze` | Run ML analysis on uploaded media |

Built with [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform),
[FastAPI](https://fastapi.tiangolo.com/).
