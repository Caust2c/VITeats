# VITeats

A native Android client for VIT Chennai's mess ordering system built to replace the existing **Foodpark** web app with a faster, cleaner, mobile-first experience.

VITeats is a **frontend-only** app: it has no backend of its own.
> ⚠️ **Status: early development.** Login, menu browsing, wallet info, and order history are working. Cart/checkout, offline support, and local budgeting are not implemented yet see [Roadmap](#roadmap).

---

## Features

Currently working, end to end against the live Proodle API:

- 🔐 **Login** — authenticate with your application number and PIN/OTP
- 🏠 **Home** — tabbed view of Student info, Menu, and Orders
- 💳 **Student info** — wallet balance and account details
- 🍽️ **Menu** — browse items by category with live price and stock availability
- 🧾 **Order history** — past orders with status, and QR code view for pickup

## Roadmap

Not yet implemented:

- Cart & PIN-confirmed checkout
- Offline caching of menu, orders, and QR codes
- Favorites / one-click reorder
- Local budget tracking
- Encrypted local PIN storage (Android Keystore)

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| Networking | Retrofit, OkHttp, Gson |
| Image loading | Coil |
| Local storage | SharedPreferences (session only — no Room/DB yet) |

## Architecture

VITeats follows a layered MVVM structure, wired together manually (no DI framework):

```
Compose UI (screens)
        │
ViewModels (Auth, Student, Menu, Orders)
        │
Repositories  ──────────────  SessionManager
        │                     (SharedPreferences:
        │                      application number,
        │                      user identifier)
        │
Network layer (Retrofit + OkHttp → ProodleApi)
        │
        ▼
Foodpark Proodle REST API
(vit-proodle.expertsoftsys.com/api)
```

- `VITeatsApplication` builds the shared singletons (network client, repositories, `SessionManager`) once at startup.
- `ViewModelFactory` hands those singletons to each screen's ViewModel.
- The PIN is sent to the login endpoint but is **not** persisted locally; only the application number and an internal user identifier are cached, to keep the user signed in between launches.

See [`apidocs.md`](apidocs.md) for the full reverse-engineered API reference (endpoints, request/response shapes).

## Getting Started

### Prerequisites
- Android Studio (current stable)
- JDK 11
- An active Foodpark Proodle / mess account (application number + PIN) to log in against the live API

### Build & run
```bash
git clone https://github.com/Caust2c/VITeats.git
cd VITeats
./gradlew installDebug
```
Or open the project in Android Studio and run the `app` configuration on an emulator or device (minSdk 24).

## Project Structure

```
app/src/main/java/com/viteats/app/
├── MainActivity.kt
├── VITeatsApplication.kt
├── data/
│   ├── SessionManager.kt          # SharedPreferences-backed session state
│   ├── remote/                    # Retrofit API + network setup
│   └── repository/                # Auth / Student / Menu / Order repositories
└── ui/
    ├── auth/                      # Login screen
    ├── home/                      # Bottom-tab host (Student / Menu / Orders)
    ├── menu/
    ├── orders/                    # Order history + order/QR details
    ├── student/                   # Wallet & account info
    ├── navigation/                # NavGraph
    └── theme/
```

## Disclaimer

This project integrates with Foodpark Proodle's API through reverse engineering for educational purposes, as no public/official API is available. It is not affiliated with or endorsed by Foodpark Proodle or VIT. Use responsibly and only with your own account credentials.

## License

No license file is currently included. All rights reserved by the author unless a license is added.
