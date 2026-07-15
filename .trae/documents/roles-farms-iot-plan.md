# Roles, Multi-Farm, Multi-Camera & IoT Deterrence Plan

## Summary
Add three-tier role-based access (Farmer, Admin, Super Admin), multi-farm registration, multi-camera device management per farm, and IoT siren/deterrence control for farmers.

---

## Current State Analysis

### Backend (`backend/`)
- **User model**: Only `email`, `display_name`, `password_hash` — no role field
- **Device model**: Standalone — no farm/owner relationship, no association to any user
- **Device → Observation**: Direct FK `device_id` on observations — no farm entity exists
- **Auth responses**: Return only `{token, user: {email, display_name}}` — no role
- **API endpoints**: All return unfiltered data — no role-based scoping (farmer sees everyone's data)
- **Alert model**: Has `location` as a string, not linked to a farm

### Frontend (`app/shared/` + `core/`)
- **User model** (`core/`): Only `email`, `displayName` — no role or id
- **Session model**: Wraps `token` + `User`
- **Navigation**: Single flat list of 8 destinations for all users
- **Dashboard**: Shows global stats (all devices, all events)
- **Devices screen**: Lists all devices — no farm grouping, no owner filtering
- **Settings**: Generic profile view — no farm management, no role-specific sections

---

## Proposed Changes

### PART 1: Backend — Database Schema Changes

#### 1a. User model — add role field
- **File**: `backend/app/models.py` — `User` table
- **Add column**: `role` = String(20), default "farmer", not null
- Valid roles: `"farmer"`, `"admin"`, `"super_admin"`

#### 1b. New Farm model
- **File**: `backend/app/models.py` — new `Farm` table
- **Columns**:
  - `farm_id`: Integer PK auto-increment
  - `farm_name`: String(255) not null
  - `location`: String(255) not null
  - `owner_id`: Integer FK → `users.user_id`, not null
  - `created_at`: DateTime(tz) server default now()
- **Relationships**: `owner` → User (back_populates="farms"), `devices` → Device

#### 1c. User → Farm relationship
- **File**: `backend/app/models.py` — `User` table
- **Add**: `farms = relationship("Farm", back_populates="owner")`

#### 1d. Device model — add farm_id FK
- **File**: `backend/app/models.py` — `Device` table
- **Add column**: `farm_id`: Integer FK → `farms.farm_id`
- **Add relationship**: `farm = relationship("Farm", back_populates="devices")`
- Keep existing `device_name`, `location`, `status` fields
- `location` on device becomes redundant with farm's location — keep for device-specific placement info

### PART 2: Backend — Auth Changes

#### 2a. Auth response — include role + user_id
- **File**: `backend/app/auth.py`
- **Signup/login responses**: Change from `{"token": ..., "user": {"email": ..., "display_name": ...}}`
- **New response**: `{"token": ..., "user": {"email": ..., "display_name": ..., "role": ..., "user_id": ...}}`
- `role` comes from the User model
- `user_id` comes from the User model

### PART 3: Backend — API Changes

#### 3a. Role-based data scoping
- **File**: `backend/app/api.py`
- **Add helper**: `get_accessible_device_ids(current_user, db)` — returns device IDs the user can see:
  - Super Admin: all devices
  - Admin: all devices
  - Farmer: only devices in farms where `owner_id = current_user.user_id`
- **Modify endpoints**: `/observations/`, `/alerts/`, `/analytics/`, `/dashboard/summary` — filter by accessible device IDs

#### 3b. New farm endpoints
- **File**: `backend/app/api.py` — new endpoints
- `GET /farms/` — list user's farms (farmers see own, admins/super admins see all)
- `POST /farms/` — create a farm (farmers only)
- `GET /farms/{id}/` — get farm details
- `PUT /farms/{id}/` — update farm (owner or admin+)
- `DELETE /farms/{id}/` — delete farm (owner or super admin)

#### 3c. New device endpoints (multi-camera)
- **File**: `backend/app/api.py` — modify existing + new
- `GET /devices/` — list devices scoped to user's accessible farms
- `POST /devices/` — register a new device under a farm (farmer can add to own farms)
- `PUT /devices/{id}/` — update device
- `DELETE /devices/{id}/` — delete device

#### 3d. IoT deterrence control endpoint
- **File**: `backend/app/api.py` — new endpoint
- `POST /devices/{id}/deterrence` — body: `{"action": "on" | "off"}`
  - Validates user has access to the device's farm
  - If action = "on": triggers the siren/deterrence mechanism
  - If action = "off": stops the siren/deterrence
  - **For MVP**: Simulates the action (logs it, returns success)
  - **IoT-ready**: Structured to forward to ESP32/Wokwi when integrated
- `GET /devices/{id}/deterrence/status` — returns current deterrence state

### PART 4: Backend — IoT Integration (Wokwi-ready)

#### 4a. IoT module
- **File**: `backend/app/iot.py` — new file
- **Interface**: `DeterrenceController` class with:
  - `activate(device_id)`: Activate siren/deterrence
  - `deactivate(device_id)`: Deactivate siren
  - `status(device_id)`: Get current state
- **Mock implementation** (`MockDeterrenceController`): Logs actions, stores state in memory
- **Wokwi implementation** (`WokwiDeterrenceController`): Sends HTTP requests to Wokwi ESP32 endpoint (future)
- **Config**: `DETERRENCE_MODE` env var = `"mock"` | `"wokwi"` | `"hardware"`

#### 4b. Wokwi wiring (documentation, not code)
- Wokwi ESP32 project with:
  - Wi-Fi connection to local network
  - HTTP server receiving on/off commands
  - GPIO pin driving a relay → siren/buzzer
- Backend sends POST to ESP32's IP when `/devices/{id}/deterrence` is called

### PART 5: Frontend — Model Changes

#### 5a. User model — add role + userId
- **File**: `core/src/commonMain/kotlin/com/example/faunabahav/model/User.kt`
- **Change**: Add `val userId: Int`, `val role: String`
- `role` values: `"farmer"`, `"admin"`, `"super_admin"`

#### 5b. New Farm model
- **File**: `core/src/commonMain/kotlin/com/example/faunabahav/model/Farm.kt`
- `data class Farm(val id: Int, val name: String, val location: String, val ownerId: Int)`

#### 5c. Device model — add farmId
- **File**: `core/src/commonMain/kotlin/com/example/faunabahav/model/Device.kt`
- **Add**: `val farmId: Int`

### PART 6: Frontend — API Client Changes

#### 6a. DTO updates
- **File**: `app/shared/.../data/remote/dto/` — update existing DTOs
- `AuthUserResponse` — add `role: String`, `user_id: Int` with @SerialName
- `DeviceDto` — add `@SerialName("farm_id") farmId: Int`
- New `FarmDto` — matching Farm model

#### 6b. API client — new endpoints
- **File**: `app/shared/.../data/remote/FaunaBehavApiClient.kt`
- Add methods: `getFarms()`, `createFarm(...)`, `getDevices()`, `createDevice(...)`, `controlDeterrence(deviceId, action)`, `getDeterrenceStatus(deviceId)`

#### 6c. New repositories
- `FarmRepository` + `FarmRepositoryImpl`
- Update `DeviceRepository` with create/delete methods
- New `DeterrenceRepository` (or add to DeviceRepository)

### PART 7: Frontend — Navigation & Role-Based UI

#### 7a. Destination filtering by role
- **File**: `app/shared/.../ui/navigation/Destination.kt`
- **Add**: `allowedRoles: List<String>` to each destination
- Dashboard, Observations, Alerts, Devices: all roles
- Analytics: farmer + admin (for their scope)
- New "Farm Management" destination: farmer only
- New "User Management" destination: admin + super_admin only
- Settings: all roles
- Filter the sidebar/nav based on `currentUser.role`

#### 7b. Dashboard — role-specific views
- **File**: `app/shared/.../ui/screens/dashboard/DashboardScreen.kt`
- **Farmer**: Shows only their farms' data — farm selector dropdown, stats for selected farm, device status per farm
- **Admin/Super Admin**: Shows all farms overview, per-farm breakdowns, user activity

#### 7c. New Farm Management screen
- **File**: `app/shared/.../ui/screens/farms/FarmManagementScreen.kt` — new
- List of farmer's farms
- Add/edit/delete farm
- Per-farm device list with "Add Device" button
- For each device: status indicator, siren on/off toggle

#### 7d. Devices screen — role-specific
- **File**: `app/shared/.../ui/screens/devices/DevicesScreen.kt`
- **Farmer**: Grouped by farm, each device shows farm name, siren control toggle
- **Admin/Super Admin**: All devices, filterable by farm/owner

#### 7e. Settings — role-specific sections
- **File**: `app/shared/.../ui/screens/settings/SettingsScreen.kt`
- **Farmer**: Profile, farm management link, device management
- **Admin/Super Admin**: Profile, user management link (list users, change roles), system settings

### PART 8: Seed Data Update

#### 8a. Seed script
- **File**: `backend/seed.py`
- Create farms: 2 farms owned by demo farmer user
- Assign existing devices to farms
- Add a Super Admin user: `admin@faunabehav.com` / `admin123`
- Add an Admin user: `manager@faunabehav.com` / `manager123`

---

## Implementation Order

```
Phase 1: Backend data model changes
  ├── User model (add role) + Farm model (new)
  ├── Device model (add farm_id)
  └── Seed data update

Phase 2: Backend auth + API changes  
  ├── Auth response (include role, user_id)
  ├── Role-based data scoping in existing endpoints
  ├── New farm CRUD endpoints
  ├── New device management endpoints
  └── IoT deterrence endpoint + mock controller

Phase 3: Frontend model + API client changes
  ├── User model (add role, userId)
  ├── Farm model (new) + Device model (add farmId)
  ├── DTO updates + new API client methods
  └── New repositories

Phase 4: Frontend UI changes
  ├── Role-based navigation filtering
  ├── Farm Management screen (CRUD)
  ├── Dashboard role-specific views
  ├── Devices screen (farm grouping + siren toggle)
  └── Settings role-specific sections
```

## Files Changed (Complete List)

### Backend — Modified
| File | Change |
|------|--------|
| `backend/app/models.py` | Add role to User, add Farm model, add farm_id to Device |
| `backend/app/auth.py` | Include role + user_id in auth response |
| `backend/app/api.py` | Role-based scoping, new farm/device/deterrence endpoints |
| `backend/seed.py` | Add farms, assign devices, add admin users |

### Backend — New
| File | Purpose |
|------|---------|
| `backend/app/iot.py` | Deterrence controller interface + mock + Wokwi stub |

### Frontend — Modified
| File | Change |
|------|--------|
| `core/.../model/User.kt` | Add role + userId fields |
| `core/.../model/Device.kt` | Add farmId field |
| `core/.../model/DashboardSummary.kt` | Potentially add per-farm fields |
| `app/shared/.../data/remote/dto/DeviceDto.kt` | Add farm_id field |
| `app/shared/.../data/remote/dto/AnalyticsSummaryDto.kt` | Potentially add farm-scoped fields |
| `app/shared/.../data/remote/FaunaBehavApiClient.kt` | Add farm/device/deterrence methods |
| `app/shared/.../data/repository/DeviceRepository.kt` | Add create/delete/control methods |
| `app/shared/.../di/AppContainer.kt` | Add FarmRepository wiring |
| `app/shared/.../ui/navigation/Destination.kt` | Add role filtering, new destinations |
| `app/shared/.../ui/navigation/AppSidebar.kt` | Filter by role |
| `app/shared/.../ui/navigation/AppBottomNav.kt` | Filter by role |
| `app/shared/.../ui/screens/dashboard/DashboardScreen.kt` | Role-specific views |
| `app/shared/.../ui/screens/dashboard/DashboardViewModel.kt` | Role-aware data loading |
| `app/shared/.../ui/screens/devices/DevicesScreen.kt` | Farm grouping + siren toggle |
| `app/shared/.../ui/screens/settings/SettingsScreen.kt` | Role-specific sections |

### Frontend — New
| File | Purpose |
|------|---------|
| `core/.../model/Farm.kt` | Farm domain model |
| `app/shared/.../data/remote/dto/FarmDto.kt` | Farm DTO |
| `app/shared/.../data/repository/FarmRepository.kt` | Farm repository interface |
| `app/shared/.../data/repository/FarmRepositoryImpl.kt` | Farm repository implementation |
| `app/shared/.../ui/screens/farms/FarmManagementScreen.kt` | Farm CRUD UI |
| `app/shared/.../ui/screens/farms/FarmManagementViewModel.kt` | Farm management state |
| `app/shared/.../ui/screens/admin/UserManagementScreen.kt` | Admin user management UI |

## Assumptions & Decisions

1. **Roles**: farmer → own farms/devices/data. admin → all farmers' data + user mgmt. super_admin → full system access.
2. **Farm ownership**: A farm has exactly one owner (farmer). A farmer can have many farms.
3. **Device ownership**: A device belongs to exactly one farm. A farm can have many devices.
4. **Data scoping**: Observations, alerts, analytics are scoped by device → farm. Farmers see only their farms' data.
5. **IoT for MVP**: Mock implementation (log + in-memory state). Wokwi-ready interface for future hardware integration. The siren on/off is a simple toggle that the frontend calls via REST.
6. **Wokwi integration**: Out of MVP scope but the backend interface is designed for it. Documented instructions for wiring Wokwi ESP32.
7. **Backward compatibility**: Existing seed data and API responses will change. Devices need to be reassigned to farms.

## Verification

1. Backend: `GET /devices/` returns only farmer's devices when logged in as farmer
2. Backend: `POST /farms/` creates farm owned by authenticated user
3. Backend: `POST /devices/{id}/deterrence` toggles siren state
4. Backend: Admin sees all farms, farmer sees only own farms
5. Frontend: Navigation shows only role-appropriate destinations
6. Frontend: Farmer dashboard shows only their farm data
7. Frontend: Devices screen groups by farm with siren toggle
8. Seed: `demo@faunabehav.com` is a farmer with 2 farms, `manager@faunabehav.com` is admin, `admin@faunabehav.com` is super_admin
