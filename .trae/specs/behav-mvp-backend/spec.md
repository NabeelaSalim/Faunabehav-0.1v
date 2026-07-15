# FaunaBahav MVP Backend Spec

## Why
The FaunaBahav KMP frontend has 8 API endpoints, a mock auth system, and zero backend to talk to. The trained ResNet3D-18 model sits unused on disk. This spec delivers a working FastAPI backend with PostgreSQL, JWT auth, and model inference so the full stack works end-to-end.

## What Changes

### Backend (NEW — `backend/` directory)
- **FastAPI application** (`app/main.py`) with 10 REST endpoints
- **PostgreSQL database** with 6 tables (users, devices, observations, alerts, feedback, uploads)
- **JWT authentication** (login/signup endpoints, middleware-protected routes)
- **R3D-18 model inference** pipeline loading `faunabehav_r3d18_best.pth`
- **File upload handling** to local `uploads/` directory
- **Seed data** for demo/development use
- **`requirements.txt`** generated from existing venv

### Frontend (MODIFIED — `app/shared/`)
- **Replace `MockAuthRepositoryImpl`** with real `AuthRepositoryImpl` calling `POST /auth/login` and `POST /auth/signup`
- **No other frontend changes** — existing API client, DTOs, and screens remain unchanged

## Impact
- Affected specs: Backend API, Data Storage, AI Inference, Authentication
- Affected code: `backend/` (new), `app/shared/` (auth repository swap)

---

## Requirements

### Requirement 1: Database Schema
The system SHALL use PostgreSQL with the following tables:

**users**
| Column | Type | Constraints |
|--------|------|-------------|
| user_id | SERIAL | PRIMARY KEY |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| display_name | VARCHAR(255) | NOT NULL |
| created_at | TIMESTAMPTZ | DEFAULT NOW() |

**devices**
| Column | Type | Constraints |
|--------|------|-------------|
| device_id | SERIAL | PRIMARY KEY |
| device_name | VARCHAR(255) | NOT NULL |
| location | VARCHAR(255) | NOT NULL |
| status | VARCHAR(50) | DEFAULT 'active' |
| created_at | TIMESTAMPTZ | DEFAULT NOW() |

**observations**
| Column | Type | Constraints |
|--------|------|-------------|
| event_id | SERIAL | PRIMARY KEY |
| device_id | INT | REFERENCES devices(device_id) |
| animal | VARCHAR(50) | NOT NULL |
| behaviour | VARCHAR(50) | NOT NULL |
| confidence | DOUBLE PRECISION | NOT NULL |
| risk_level | VARCHAR(20) | NOT NULL |
| deterrence_action | VARCHAR(255) | DEFAULT '' |
| frame_path | VARCHAR(500) | DEFAULT '' |
| timestamp | TIMESTAMPTZ | DEFAULT NOW() |

**alerts**
| Column | Type | Constraints |
|--------|------|-------------|
| alert_id | SERIAL | PRIMARY KEY |
| event_id | INT | REFERENCES observations(event_id) |
| animal | VARCHAR(50) | NOT NULL |
| behaviour | VARCHAR(50) | NOT NULL |
| risk_level | VARCHAR(20) | NOT NULL |
| confidence | DOUBLE PRECISION | NOT NULL |
| location | VARCHAR(255) | NOT NULL |
| status | VARCHAR(50) | DEFAULT 'open' |
| deterrence_action | VARCHAR(255) | DEFAULT '' |
| timestamp | TIMESTAMPTZ | DEFAULT NOW() |

**feedback**
| Column | Type | Constraints |
|--------|------|-------------|
| feedback_id | SERIAL | PRIMARY KEY |
| event_id | INT | REFERENCES observations(event_id) |
| user_id | INT | REFERENCES users(user_id) |
| corrected_behaviour | VARCHAR(50) | NOT NULL |
| created_at | TIMESTAMPTZ | DEFAULT NOW() |

**uploads**
| Column | Type | Constraints |
|--------|------|-------------|
| upload_id | SERIAL | PRIMARY KEY |
| device_id | INT | REFERENCES devices(device_id) |
| file_path | VARCHAR(500) | NOT NULL |
| original_name | VARCHAR(255) | NOT NULL |
| mime_type | VARCHAR(100) | DEFAULT '' |
| file_size | INT | DEFAULT 0 |
| uploaded_at | TIMESTAMPTZ | DEFAULT NOW() |

#### Scenario: Schema creation
- **WHEN** the application starts for the first time
- **THEN** all tables are created automatically via SQLAlchemy models

### Requirement 2: Authentication
The system SHALL provide JWT-based authentication.

#### Scenario: User sign-up
- **WHEN** a POST request is sent to `/auth/signup` with `{email, password, display_name}`
- **THEN** the system validates the email is unique, hashes the password with bcrypt, creates a user record, and returns `{token, user: {email, display_name}}`

#### Scenario: User login
- **WHEN** a POST request is sent to `/auth/login` with `{email, password}`
- **THEN** the system verifies credentials, generates a JWT token (24h expiry), and returns `{token, user: {email, display_name}}`

#### Scenario: Protected endpoint
- **WHEN** a request includes a missing or invalid `Authorization: Bearer <token>` header
- **THEN** the system returns 401 Unauthorized

### Requirement 3: API Endpoints
The system SHALL expose the following endpoints, all protected by JWT auth except `/auth/login` and `/auth/signup`.

#### Scenario: GET /observations/
- **WHEN** a GET request is sent to `/observations/`
- **THEN** the system returns a JSON array of observation objects matching `ObservationDto`

#### Scenario: POST /events/inference
- **WHEN** a multipart POST request is sent to `/events/inference` with fields `device_id` (int) and `file` (binary)
- **THEN** the system saves the file, runs the R3D-18 model, and returns an `InferenceResultDto`

#### Scenario: GET /alerts/
- **WHEN** a GET request is sent to `/alerts/`
- **THEN** the system returns a JSON array of alert objects matching `AlertDto`

#### Scenario: GET /feedback/
- **WHEN** a GET request is sent to `/feedback/`
- **THEN** the system returns a JSON array of feedback objects matching `FeedbackDto`

#### Scenario: POST /feedback/
- **WHEN** a POST request is sent to `/feedback/` with `{event_id, user_id, corrected_behaviour}`
- **THEN** the system creates a feedback record and returns the created `FeedbackDto` with `feedback_id`

#### Scenario: GET /analytics/
- **WHEN** a GET request is sent to `/analytics/`
- **THEN** the system returns an `AnalyticsSummaryDto` with event counts by risk level, animal breakdown, and behaviour breakdown

#### Scenario: GET /dashboard/summary
- **WHEN** a GET request is sent to `/dashboard/summary`
- **THEN** the system returns a `DashboardSummaryDto` with total events, high risk events, active devices, and deterrence action count

#### Scenario: GET /devices/
- **WHEN** a GET request is sent to `/devices/`
- **THEN** the system returns a JSON array of device objects matching `DeviceDto`

### Requirement 4: AI Model Inference
The system SHALL load and run the trained R3D-18 model.

#### Scenario: Video inference
- **WHEN** a video file is uploaded to `/events/inference`
- **THEN** the system samples 16 frames from the video, preprocesses them (resize to 112x112, normalize), runs the model, and maps output logits to species + behaviour classes

#### Scenario: Image inference
- **WHEN** a single image is uploaded to `/events/inference`
- **THEN** the system tiles the image into a 16-frame clip (same frame repeated), preprocesses, runs the model

#### Scenario: No species detected
- **WHEN** the model confidence for all species is below threshold (0.5)
- **THEN** the system returns `{decision: "no_target_species_detected", video_path: "..."}`

### Requirement 5: Seed Data
The system SHALL include a seed script that populates the database with demo data.

#### Scenario: Seed script
- **WHEN** `python seed.py` is run
- **THEN** the system creates 2 devices, 1 demo user (demo@faunabehav.com / password123), 10 observations, 3 alerts, and 1 feedback entry
