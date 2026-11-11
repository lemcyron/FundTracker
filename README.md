# FundTracker — Android Expenses Checklist Tracker

**FundTracker** is a modern, Emerald Green-themed Android application built with **Kotlin** and **Jetpack Compose / Material 3**. It allows users to track expenses in a checklist format, calculate totals in real-time, perform batch check/uncheck actions, and synchronize data directly with a **Google Drive JSON file**.

---

## Features

- **Item & Cost Entry**: Enter expense items with names, custom amounts/costs, and optional categories (Groceries, Utilities, Housing, etc.).
- **Interactive Checklist**: Check or uncheck individual items with smooth strikethrough animation and status indicators.
- **Settings & Batch Actions**:
  - **Check All Items**: Quickly mark all items as completed/settled.
  - **Uncheck All Items**: Reset all items to pending status.
  - **Clear Checklist**: Reset the entire list with a confirmation safety prompt.
- **Dynamic Bottom Summary Bar**:
  - **Total Amount**: Grand total sum of all items in the checklist.
  - **Total Checked**: Sum and count of all checked/paid items.
  - **Total Unchecked**: Sum and count of all remaining/pending items.
  - **Progress Bar**: Visual percentage indicator of completed expenses.
- **Google Drive JSON Referencing & Sync**:
  - Connect to a JSON file hosted in Google Drive via a public sharing link or file ID.
  - Auto-normalizes Google Drive sharing links into direct download links.
  - Offline fallback with local storage caching.
  - Export current checklist to JSON format for easy backup or uploading to Drive.
- **Emerald Green Theme**: Clean Material 3 palette featuring Forest Green (`#1B5E20`), Primary Green (`#2E7D32`), Accent Green (`#00C853`), and soft green surfaces.

---

## Google Drive JSON Format

Upload a `.json` file to your Google Drive with the following structure (a sample `sample_google_drive_expenses.json` is provided in the repository root):

```json
{
  "title": "Monthly Household Expenses",
  "currency": "$",
  "lastUpdated": "2026-11-11T10:00:00Z",
  "items": [
    {
      "id": "exp-001",
      "name": "Apartment Rent",
      "cost": 750.00,
      "checked": true,
      "category": "Housing"
    },
    {
      "id": "exp-002",
      "name": "Electric Utility Bill",
      "cost": 85.50,
      "checked": false,
      "category": "Utilities"
    },
    {
      "id": "exp-003",
      "name": "High-speed Internet & WiFi",
      "cost": 50.00,
      "checked": true,
      "category": "Utilities"
    }
  ]
}
```

### How to Link Your Google Drive File:
1. In Google Drive, right-click your JSON file and select **Share** > **Share**.
2. Under *General access*, select **"Anyone with the link"** and set the role to **Viewer**.
3. Click **Copy link** (e.g. `https://drive.google.com/file/d/1A2B3C4D5E.../view?usp=sharing`).
4. In FundTracker, tap the **Settings** gear icon > paste the link in the **Google Drive File Link** box > tap **Sync From Google Drive Now**.

---

## How to Build the Android APK

### Option 1: Using Android Studio (Recommended)
1. Open **Android Studio**.
2. Select **Open** and choose the `FundTracker/android` directory.
3. Allow Gradle to sync dependencies.
4. Go to **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
5. The generated APK will be located at:
   ```
   android/app/build/outputs/apk/debug/app-debug.apk
   ```

### Option 2: Using Command Line (Gradle)
Run the following in the `android` directory:
```bash
cd android
./gradlew assembleDebug
```
For Windows PowerShell:
```powershell
cd android
.\gradlew.bat assembleDebug
```

---

## Interactive Web Preview & Testing

A browser-ready preview is included to test all checklist functions, calculations, and Google Drive syncing:

```bash
# Run unit tests
npm test

# Start the interactive web preview server
npm start
```
Open [http://127.0.0.1:3000](http://127.0.0.1:3000) in your browser.

---

## Project Structure

```
FundTracker/
├── android/                               # Native Android Kotlin / Compose project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/fundtracker/expenses/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── model/                 # ExpenseItem & ExpenseData models
│   │   │   │   ├── data/                  # Repository & Google Drive sync service
│   │   │   │   ├── viewmodel/             # ExpenseViewModel & Totals calculations
│   │   │   │   └── ui/                    # Compose Green Theme, Components & Screens
│   │   │   ├── res/                       # Colors, Strings, Vector icons
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── web/                                   # Standalone interactive UI preview
│   ├── index.html
│   ├── styles.css
│   └── app.js
├── sample_google_drive_expenses.json      # Sample JSON file for Google Drive upload
├── server.js                              # Local HTTP server
├── test.js                                # Calculation & sync unit tests
└── README.md
```
