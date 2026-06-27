
# RoadGuard – Frontend Context & ID Mapping

## 1. Overview
- **Platform**: Android (Java, traditional XML views – **no ViewBinding**)
- **Architecture**: MVVM (Activity/Fragment → ViewModel → Repository)
- **Navigation**: Bottom Navigation (Map, My Reports, Profile) + separate Activities for auth and form flows
- **Map**: Google Maps SDK with clustering (utility library)
- **Notifications**: Firebase Cloud Messaging handled by a service; tapping opens map

> The frontend developer can work on layouts, styles, and interaction logic without touching Firebase, Room, or ViewModels. This document provides **all screen IDs, layout files, and navigation wiring**.

---

## 2. Screen List & Mapping

| # | Screen | Activity / Fragment | Layout XML | Notes |
|---|--------|-------------------|-------------|-------|
| 1 | Splash | `SplashActivity` | No layout (finishes immediately) | Redirects to login or main |
| 2 | Login | `LoginActivity` | `activity_login.xml` | Email, password, Google Sign‑In |
| 3 | Register | `RegisterActivity` | `activity_register.xml` | Name, email, password |
| 4 | Forgot Password | `ForgotPasswordActivity` | `activity_forgot_password.xml` | Email input, reset |
| 5 | Main container | `MainActivity` | `activity_main.xml` | Hosts bottom nav & NavHostFragment |
| 6 | Map | `MapFragment` | `fragment_map.xml` | Full map, FAB, markers |
| 7 | Report Form | `ReportFormActivity` | `activity_report_form.xml` | Map picker, severity, notes, submit |
| 8 | Edit Report | `EditReportActivity` | `activity_edit_report.xml` | Pre‑filled form, update |
| 9 | Report Detail (Bottom Sheet) | `ReportInfoBottomSheet` | `bottom_sheet_report_detail.xml` | Shown when a marker is tapped |
|10 | My Reports | `MyReportsFragment` | `fragment_my_reports.xml` | RecyclerView list |
|11 | Profile | `ProfileFragment` | `fragment_profile.xml` | User info, toggles (TTS, alerts, radius), logout |
|12 | (Implied) Navigation graph | – | `res/navigation/bottom_nav_menu.xml` | Defines the 3 tabs: map, my_reports, profile |
|13 | (Implied) Bottom menu | – | `res/menu/bottom_nav_menu.xml` | Icons & titles for tabs |

---

## 3. Navigation Flow

```
SplashActivity
 ├─ (authenticated) → MainActivity
 └─ (not authenticated) → LoginActivity
       ├─ RegisterActivity
       └─ ForgotPasswordActivity

MainActivity
 ├─ Tab 1: MapFragment
 │    ├─ FAB → ReportFormActivity
 │    └─ Marker tap → ReportInfoBottomSheet
 ├─ Tab 2: MyReportsFragment
 │    └─ Item tap → EditReportActivity
 └─ Tab 3: ProfileFragment
      └─ Logout → LoginActivity
```

- **MapFragment → ReportFormActivity**: simple `Intent` (no extras required).
- **MyReportsFragment → EditReportActivity**: passes a `CachedReport` object via `Intent` extra `"report"` (implements `Serializable`).
- **Notification → MainActivity**: extras `"notification_lat"` and `"notification_lng"` (double) are used by `MapDataViewModel` to centre the map.

---

## 4. Layout Files & View IDs

### 4.1 `activity_login.xml`
- **EditText**: `etEmail` (input type email)
- **EditText**: `etPassword` (input type password)
- **Button**: `btnLogin` (click → login)
- **Button**: `btnGoogleSignIn` (click → Google Sign‑In)
- **TextView**: `tvRegisterLink` (clickable, opens RegisterActivity)
- **TextView**: `tvForgotPassword` (clickable, opens ForgotPasswordActivity)
- **TextView**: `tvError` (displays error message)

### 4.2 `activity_register.xml`
- **EditText**: `etName`
- **EditText**: `etEmail`
- **EditText**: `etPassword`
- **Button**: `btnRegister`
- **TextView**: `tvError`

### 4.3 `activity_forgot_password.xml`
- **EditText**: `etEmail`
- **Button**: `btnReset`
- **TextView**: `tvMessage` (shows status/error)

### 4.4 `activity_main.xml`
- **Fragment**: `nav_host_fragment` (id = `R.id.nav_host_fragment`, name = `NavHostFragment`)
- **BottomNavigationView**: `bottom_navigation` (id = `R.id.bottom_navigation`)

### 4.5 `fragment_map.xml`
- **SupportMapFragment**: `map` (id = `R.id.map`)
- **FloatingActionButton**: `fab_add_report` (id = `R.id.fab_add_report`) – opens ReportFormActivity

### 4.6 `activity_report_form.xml`
- **SupportMapFragment**: `map_picker` (id = `R.id.map_picker`) – user adjusts pin
- **RadioGroup**: `radio_severity`
    - **RadioButton**: `radio_low` (text = "Low", checked by default)
    - **RadioButton**: `radio_medium` (text = "Medium")
    - **RadioButton**: `radio_high` (text = "High")
- **EditText**: `et_notes` (hint = "Notes (optional)")
- **Button**: `btn_submit` (text = "Submit Report")

### 4.7 `activity_edit_report.xml`
Same structure as `activity_report_form.xml`, but:
- Button is `btn_update` (text = "Update Report")
- Pre‑filled from intent extra

### 4.8 `bottom_sheet_report_detail.xml`
- **ImageView**: `iv_severity_icon` (optional, tinted marker icon)
- **TextView**: `tv_severity`
- **TextView**: `tv_distance`
- **TextView**: `tv_time`
- **TextView**: `tv_notes`
- **Button**: `btn_upvote` (text = "👍 Still There")
- **Button**: `btn_downvote` (text = "👎 Fixed")
- **TextView**: `tv_votes`
- **Button**: `btn_navigate` (text = "Navigate with Google Maps")

### 4.9 `fragment_my_reports.xml`
- **RecyclerView**: `rv_my_reports` (id = `R.id.rv_my_reports`)

**Item layout**: `item_my_report.xml`
- **TextView**: `tv_item_severity`
- **TextView**: `tv_item_notes`
- **TextView**: `tv_item_timestamp`

### 4.10 `fragment_profile.xml`
- **TextView**: `tv_display_name`
- **TextView**: `tv_email`
- **Switch**: `switch_alerts` (toggle “Enable Proximity Alerts”)
- **Switch**: `switch_tts` (toggle “Speak Alerts Aloud (TTS)”)
- **ChipGroup**: `chip_group_radius` (single selection)
    - **Chip**: `chip_radius_1` (text = "1 km")
    - **Chip**: `chip_radius_3` (text = "3 km")
    - **Chip**: `chip_radius_5` (text = "5 km")
- **Button**: `btn_logout` (text = "Logout")

---

## 5. Navigation & IDs
- **Bottom navigation menu** (`res/menu/bottom_nav_menu.xml`):
    - `navigation_map`, `navigation_my_reports`, `navigation_profile`
- **Navigation graph** (`res/navigation/bottom_nav_menu.xml`):
    - The same IDs as above are used as `android:id` for fragments.

---

## 6. Data Passed Between Screens (Extras)
| From | To | Key | Type | Notes |
|------|----|-----|------|-------|
| `MyReportsAdapter` | `EditReportActivity` | `"report"` | `CachedReport` (Serializable) | Contains all fields; edit screen pre‑fills |
| `MainActivity` (via notification) | `MapFragment` | `"notification_lat"` / `"notification_lng"` | `double` | Used to animate camera; managed by `MapDataViewModel` |

---

## 7. Theming & Colors (from original brief)
- **Primary Color**: `#FF6B35` (Safety Orange)
- **Secondary**: `#FFC107` (Amber Yellow)
- **Background**: `#F5F5F5` (Asphalt Light)
- **Text Primary**: `#2C2C2C`
- **Text Secondary**: `#757575`
- **Error**: `#D32F2F`
- **Severity colors**:
    - Low: `#4CAF50`
    - Medium: `#FF9800`
    - High: `#F44336`

Define these in `res/values/colors.xml` and apply via `@color/…` where needed.

---

## 8. Frontend Developer Instructions

### 8.1 Working on a Screen
1. **Locate the XML layout** from the table above.
2. Open the corresponding **Java Activity/Fragment** to see which IDs are referenced via `findViewById`.
3. All views are accessed using `findViewById(R.id.xxx)` – no ViewBinding.
4. When changing an ID, update it **both in XML and in Java**.

### 8.2 Adding a New Screen
- Create the XML layout in `res/layout/`.
- If it’s a full screen, create a new `Activity` (e.g., `NewActivity.java`).
- If it’s a tab, add a new `<fragment>` to the navigation graph and a new `<item>` to the bottom menu.
- Use the existing naming conventions (lowercase with underscores for IDs, PascalCase for classes).

### 8.3 Styling
- Use Material Design components (`MaterialButton`, `Chip`, `Switch`, `TextInputLayout`, etc.) where possible.
- For map markers, a vector drawable `ic_hazard_marker.xml` is used; the colour is tinted programmatically – you can change the base icon but keep the tint approach.

### 8.4 Testing UI Changes
- The app can be run without backend (reports won’t sync, but authentication and local Room cache will work if you are logged in).
- For testing notification tap handling, you can send a mock notification via Firebase Console or adb.

---

## 9. File Index
| Category | File Path |
|----------|-----------|
| Activity (Main) | `app/src/main/java/com/roadguard/app/ui/main/MainActivity.java` |
| Activity (Login) | `app/src/main/java/com/roadguard/app/ui/auth/LoginActivity.java` |
| Activity (Register) | `app/src/main/java/com/roadguard/app/ui/auth/RegisterActivity.java` |
| Activity (Forgot) | `app/src/main/java/com/roadguard/app/ui/auth/ForgotPasswordActivity.java` |
| Fragment (Map) | `app/src/main/java/com/roadguard/app/ui/map/MapFragment.java` |
| Fragment (My Reports) | `app/src/main/java/com/roadguard/app/ui/myreports/MyReportsFragment.java` |
| Fragment (Profile) | `app/src/main/java/com/roadguard/app/ui/profile/ProfileFragment.java` |
| Activity (Report Form) | `app/src/main/java/com/roadguard/app/ui/report/ReportFormActivity.java` |
| Activity (Edit Report) | `app/src/main/java/com/roadguard/app/ui/report/EditReportActivity.java` |
| Bottom Sheet | `app/src/main/java/com/roadguard/app/ui/map/ReportInfoBottomSheet.java` |
| Layouts | `app/src/main/res/layout/*.xml` (see above) |
| Navigation | `app/src/main/res/navigation/bottom_nav_menu.xml` |
| Menu (bottom) | `app/src/main/res/menu/bottom_nav_menu.xml` |
| Colors | `app/src/main/res/values/colors.xml` |
| Strings | `app/src/main/res/values/strings.xml` |
| Drawables (icons) | `app/src/main/res/drawable/` |

