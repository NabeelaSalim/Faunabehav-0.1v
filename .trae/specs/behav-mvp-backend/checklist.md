# Checklist

## Database
- [ ] All 6 database tables are created on app startup (users, devices, observations, alerts, feedback, uploads)
- [ ] Table columns match the spec exactly (names, types, constraints)
- [ ] Foreign key relationships are enforced (observations → devices, alerts → observations, feedback → users, uploads → devices)

## Authentication
- [ ] POST /auth/signup accepts email/password/display_name, hashes password, returns JWT + user
- [ ] POST /auth/login accepts email/password, verifies credentials, returns JWT + user
- [ ] JWT tokens expire after 24 hours
- [ ] Protected routes return 401 for missing/invalid tokens
- [ ] Duplicate email signup returns appropriate error

## API Endpoints
- [ ] GET /observations/ returns list of observations in snake_case JSON
- [ ] POST /events/inference accepts multipart file + device_id, returns InferenceResultDto
- [ ] GET /alerts/ returns list of alerts
- [ ] GET /feedback/ returns list of feedback
- [ ] POST /feedback/ creates feedback, returns created object with feedback_id
- [ ] GET /analytics/ returns AnalyticsSummaryDto with risk/animal/behaviour breakdowns
- [ ] GET /dashboard/summary returns DashboardSummaryDto with counts
- [ ] GET /devices/ returns list of devices
- [ ] All responses use snake_case field names matching the KMP DTOs
- [ ] Timestamps are ISO-8601 with timezone offset

## Model Inference
- [ ] Model loads successfully from faunabehav_r3d18_best.pth on startup
- [ ] Single image upload → inference returns result (no crash)
- [ ] Inference returns detected species, behaviour, confidence, risk level
- [ ] Low-confidence inference returns "no_target_species_detected"
- [ ] Deterrence action is assigned based on risk level (HIGH → "active_deterrence", MEDIUM → "warning", LOW → "monitor")

## Seed Data
- [ ] Running `python seed.py` populates the database with demo data
- [ ] Demo user (demo@faunabehav.com / password123) can log in
- [ ] Seed includes at least 2 devices, 10 observations, 3 alerts

## Frontend
- [ ] AuthRepositoryImpl calls real /auth/login and /auth/signup endpoints
- [ ] AppContainer uses AuthRepositoryImpl instead of MockAuthRepositoryImpl
- [ ] Login and sign-up flows work end-to-end with the real backend

## Integration
- [ ] Backend starts with `uvicorn app.main:app` on port 8000
- [ ] All endpoints return correct JSON for the KMP client to consume
- [ ] Frontend builds and connects to the running backend
