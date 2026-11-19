# Tracker — Expense Tracker

An offline-first personal expense tracker built with **Kotlin**, **Jetpack Compose** and **Room**.
Set a budget, log your spending, edit or delete transactions, and share a summary with any app.

## Features

### 🔐 Accounts & session

- Sign up with a unique username, or log in to an existing account.
- The session is saved on the device, so the app reopens straight to the dashboard.
- Every user has their own transactions **and** their own budget.
- Logging out clears the session only — data and budget stay saved.

### 📊 Dashboard & budget

- Total spent, current budget, remaining amount and usage percentage at a glance.
- Progress bar that fills as you spend and turns red once you go over budget.
- Set or change the budget anytime — it is stored per user.
- A red warning banner tells you exactly how much you are over budget.

### ➕ Adding transactions

- Quick add with only a title and an amount, with input validation and friendly errors.
- Transactions are listed newest first.

### ✏️ Edit & delete

- Tap any transaction to open a screen pre-filled with its current title and amount.
- Update it or delete it permanently; the dashboard refreshes immediately.

### 💾 Offline & local

- All data lives on the device in a Room (SQLite) database — no server, no network permission.
- Budgets and the active session are kept in local `SharedPreferences`.

## Project Structure

```text
Tracker/
├── app/src/main/java/com/example/tracker/
│   ├── AuthActivity.kt          # Login / sign-up host
│   ├── MainActivity.kt          # Dashboard host, sharing & navigation
│   ├── EditExpenseActivity.kt   # Edit / delete a transaction
│   ├── data/                    # Room database, DAO, entities, session prefs
│   ├── ui/auth/                 # Auth screen + ViewModel
│   ├── ui/main/                 # Dashboard screen + ViewModel
│   ├── ui/common/               # Shared composables & money formatting
│   └── ui/theme/                # Colors, typography, Material 3 theme
├── docs/screenshots/            # ← your screenshots go here
└── gradle/libs.versions.toml    # Dependencies & versions
```

---

## Getting Started

**Requirements**

- Android Studio (latest stable)
- JDK 11 or newer
- Android SDK 37
- A device or emulator running Android 7.0 (API 24) or newer

**Run**

1. Clone the repository:
   ```bash
   git clone <your-repo-url>
   cd Tracker
   ```
2. Open the project in Android Studio.
3. Wait for the Gradle sync to finish.
4. Press **Run ▶** on the `app` configuration.
