# RoadGuard – See the hazard before you feel it.

RoadGuard is a community‑driven road hazard alert system for Android.  
Drivers report broken roads, potholes, or other dangers, and nearby users receive **spoken push notifications** so they can slow down – even without looking at the screen.

This project is built with **Java**, **Firebase**, and a **Node.js Cloud Function**.  
It follows the **MVVM** architecture and uses traditional Android views (no data‑binding).

---

## ✨ Features (Modified from the original brief)

- **No image upload** – reports are pure text + location + severity.
- **Text‑to‑Speech (TTS)** – when a notification arrives, the phone speaks the alert aloud.
- **Periodic Wi‑Fi sync** – when you connect to Wi‑Fi, the app automatically downloads the latest hazards (like a cron job).
- **Offline queue** – reports submitted without internet are stored locally and uploaded when back online.
- **Severity‑coloured map markers** – low (green), medium (orange), high (red) with clustering.
- **Proximity alerts** – users choose a radius (1, 3, 5 km) and receive push notifications when a new hazard is reported nearby.
- **Google Sign‑In** – optional one‑tap login.
- **“My Reports” list** – view and edit reports you’ve created.

---

## 🧱 Architecture

- **Android App**: MVVM + Repository pattern, `LiveData`, Room (SQLite) for offline cache.
- **Backend**: Firebase Auth, Firestore, Cloud Messaging (FCM).
- **Push Engine**: Firebase Cloud Function (Node.js) triggered on new Firestore document.

```markdown
Android App (Java)
├─ Room (offline cache & pending queue)
├─ Firebase Auth, Firestore, FCM
└─ WorkManager (periodic sync & upload)

Cloud Function (Node.js)
└─ Firestore onCreate → geohash query → FCM multicast
```

---

## 📁 Project Structure (Java sources)

```markdown
com.roadguard.app/
├── RoadGuardApplication.java
├── data/
│ ├── local/
│ │ ├── dao/ # Room DAOs (User, Report, Pending)
│ │ ├── entity/ # Room entities (CachedReport, PendingReport, UserProfile)
│ │ └── RoadGuardDatabase.java
│ ├── remote/
│ │ ├── AuthDataSource.java # FirebaseAuth + Google Sign-In
│ │ ├── FirestoreDataSource.java # Firestore read/write methods
│ │ └── (No Storage – images removed)
│ └── repository/
│ ├── AuthRepository.java
│ ├── ReportRepository.java
│ └── UserRepository.java
├── model/ # POJOs (User, Report, Severity)
├── ui/
│ ├── auth/ # Login, Register, ForgotPassword Activities + ViewModel
│ ├── main/ # MainActivity (bottom nav host)
│ ├── map/ # MapFragment, MapViewModel, SeverityClusterRenderer
│ ├── report/ # ReportFormActivity, EditReportActivity, ReportViewModel
│ ├── myreports/ # MyReportsFragment, adapter, ViewModel
│ └── profile/ # ProfileFragment, ProfileViewModel (TTS toggle)
├── service/
│ └── RoadGuardFCMService.java # FCM + TextToSpeech
├── worker/
│ ├── LocationUpdateWorker.java # Periodic geohash upload
│ ├── ReportUploadWorker.java # Offline report syncer
│ └── SyncWorker.java # Wi‑Fi data refresh
└── util/
├── Constants.java
├── GeohashHelper.java
├── NetworkUtil.java
└── NotificationHelper.java
```
**Layouts** are in `res/layout/` (traditional XML, no ViewBinding).  
**Navigation** uses `res/navigation/bottom_nav_menu.xml` and `res/menu/bottom_nav_menu.xml`.

---

## 🚀 Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/mhdhaikalll/RoadGuard.git
cd RoadGuard
```

### 2. Firebase Project Setup

1. Go to Firebase Console and create a new project.
2. Register an Android app with the package `com.roadguard.app`.
3. Download `google-services.json` and place it in the app/ folder.
4. Enable Authentication → Sign-in method → Email/Password and Google.
5. Enable Cloud Firestore (start in production mode, write rules later).
6. Enable Cloud Messaging (no extra config needed).

### 3. Google Maps API

1. In Google Cloud Console, enable the Maps SDK for Android.
2. Create an API key (restrict it to Android apps with your package & SHA‑1).
3. Open `AndroidManifest.xml` and replace `YOUR_API_KEY_HERE` with your key.

### 4. Google Sign In

1. In Firebase Console → Authentication → Sign‑in method → Google → enable.
2. Note the Web client ID (it’s auto‑created).
3. Add it to `res/values/strings.xml` as `default_web_client_id`.

### 5. Deploy the Cloud Function

The function sends push notifications when a report is created.
1. Install the Firebase CLI:
```bash
npm install -g firebase-tools
firebase login
```

2. Inside the project root, initialize functions (if not already):
```bash
firebase init functions
```
Choose JavaScript, use an existing project, don’t install ESLint.

3. Replace the generated `functions/package.json` and `functions/index.js` with the files from this repository.

4. Install dependencies inside `functions/`:
```bash
cd functions
npm install
```

5. Upgrade your Firebase plan to Blaze (pay‑as‑you‑go, free tier is enough).
6. Deploy:
```bash
firebase deploy --only functions
```

7. Build and run the app
- Open the project in Android Studio.
- Sync Gradle, then Run on a device or emulator.

---

## 🔧 Running Notes

- TTS: To hear spoken alerts, the device’s media volume must be up and the phone must not be in silent mode. The TTS toggle is in the Profile tab.
- Location: The app requests fine location. Allow it to receive proximity alerts and submit accurate reports.
- Offline: Reports submitted offline will be uploaded the next time WorkManager runs (every ~15 minutes on any network).
- Wi‑Fi sync: On unmetered (Wi‑Fi) networks, the app periodically fetches the latest reports to keep the map fresh even when not actively used.

---

## Licence

This project is created for the subject ICT602: Mobile Technology & Development

---

## Developers / Contributors

- Muhammad Haikal Iman | [GitHub]() | [LinkedIn]()
- Majdiah | [GitHub]() | [LinkedIn]()
- Nur Inas | [GitHub]() | [LinkedIn]()
- Nur Aina Qistina | [GitHub]() | [LinkedIn]()