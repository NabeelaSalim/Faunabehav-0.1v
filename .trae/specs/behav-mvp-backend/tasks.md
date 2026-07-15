# Tasks

## Overview
Build the FaunaBahav MVP backend — FastAPI server, PostgreSQL database, JWT auth, and R3D-18 model inference. Task 1-4 can run in parallel. Task 5-6 depend on Task 1-2. Task 7 depends on everything above.

---

- [ ] Task 1: Database schema — SQLAlchemy models + DB initialization
  - Create `backend/app/database.py` with engine, session, and Base
  - Create `backend/app/models.py` with all 6 tables (users, devices, observations, alerts, feedback, uploads)
  - Auto-create tables on startup

- [ ] Task 2: Authentication — JWT login/signup endpoints
  - Create `backend/app/auth.py` with password hashing (passlib/bcrypt), JWT creation/verification (python-jose)
  - Create `POST /auth/signup` endpoint
  - Create `POST /auth/login` endpoint
  - Create JWT dependency for protected routes

- [ ] Task 3: Model inference pipeline — R3D-18 loader + inference
  - Create `backend/app/inference.py` to load `faunabehav_r3d18_best.pth`
  - Implement preprocessing: resize 112x112, normalize, 16-frame clip (tile images, sample videos)
  - Implement class mapping: output logits → species + behaviour + risk level
  - Handle "no species detected" case (confidence < 0.5)

- [ ] Task 4: Seed data — demo dataset
  - Create `backend/seed.py` with 2 devices, 1 demo user, 10 observations, 3 alerts, 1 feedback
  - Observations cover all 3 species and multiple behaviours

- [ ] Task 5: API endpoints — all 8 data endpoints
  - Implement all endpoints from Requirement 3 using SQLAlchemy queries
  - Wire up inference endpoint to use the model pipeline from Task 3
  - Add file upload handling for `/events/inference`
  - Dependencies: Task 1, Task 2, Task 3

- [ ] Task 6: Main application — FastAPI app assembly
  - Create `backend/app/main.py` with app factory, CORS, router includes, startup events
  - Create `backend/requirements.txt` from venv
  - Dependencies: Task 1, Task 2

- [ ] Task 7: Frontend auth swap — replace mock with real auth
  - Implement `AuthRepositoryImpl` calling `POST /auth/login` and `/auth/signup`
  - Swap `MockAuthRepositoryImpl` for `AuthRepositoryImpl` in AppContainer
  - Dependencies: Task 2

## Task Dependencies
- [Task 5] depends on [Task 1, Task 2, Task 3]
- [Task 6] depends on [Task 1, Task 2]
- [Task 7] depends on [Task 2]
