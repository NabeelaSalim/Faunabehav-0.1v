# FaunaBahav — AI-Powered Wildlife Monitoring & Deterrence

Kotlin Multiplatform project targeting Android, iOS, Web, Desktop (JVM), Server.

* [/app/shared](./app/shared/src) — Shared Compose Multiplatform UI code
* [/app/androidApp](./app/androidApp) — Android application
* [/app/webApp](./app/webApp) — Web application (Wasm/JS)
* [/app/iosApp](./app/iosApp/iosApp) — iOS application (requires Xcode)
* [/app/desktopApp](./app/desktopApp) — Desktop (JVM) application
* [/core](./core/src) — Shared domain models
* [/server](./server) — Ktor server application
* [/backend](./backend) — FastAPI + ML inference backend (Python)

### Running

- **Web app**: `./gradlew :app:webApp:wasmJsBrowserDevelopmentRun`
- **Backend**: `cd backend && venv/bin/python3 -m uvicorn app.main:app --reload --port 8000`
- **Desktop app**: `./gradlew :app:desktopApp:run`
- **Server**: `./gradlew :server:run`

Built with [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform),
[FastAPI](https://fastapi.tiangolo.com/).
