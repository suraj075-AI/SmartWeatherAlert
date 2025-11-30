# Smart Weather Alert & Early Warning System

A comprehensive Android application for real-time weather monitoring, automatic alerts, and historical data tracking. Built using Java, Firebase, and OpenWeatherMap API.

## 🚀 Features

*   **Real-time Weather Tracking:** Fetches live weather data (Temperature, Humidity, Wind, etc.) for any city using OpenWeatherMap API.
*   **Hybrid Database:**
    *   **Local Storage:** Uses SQLite (JDBC-like DAO pattern) to save search history for offline access.
    *   **Cloud Sync:** Automatically pushes weather alerts to **Firebase Realtime Database** for remote monitoring.
*   **Automatic Updates:** Background service (`ScheduledExecutorService`) refreshes weather data every minute to ensure accuracy.
*   **User-Friendly UI:**
    *   Material Design interface with Dark Mode support.
    *   **Swipe-to-Refresh** functionality for manual updates.
    *   **Detailed Forecast:** View hourly and daily temperature trends.

## 🛠 Tech Stack

*   **Language:** Java (Android Native)
*   **Architecture:** MVC / OOP Principles
*   **Networking:** Retrofit 2 + GSON
*   **Database:** SQLite (Local) & Firebase (Cloud)
*   **Multithreading:** `ExecutorService` (Background Tasks)
*   **UI Components:** RecyclerView, Glide, SwipeRefreshLayout

## 📸 Screenshots

*![WhatsApp Image 2025-11-25 at 23 11 18_f241cee6]


## 🔧 Setup & Installation

1.  **Clone the Repository:**
2.  Open in Android Studio

Launch Android Studio and choose Open → select the project folder.

Let Gradle sync and download dependencies.

Add API keys and Firebase

OpenWeatherMap API: Create an account at OpenWeatherMap, get an API key, and add it to the project:

Recommended: create a local.properties entry or add it to res/values/secrets.xml (don't commit keys to git).

Example (in code where API key constant is used): const val OPEN_WEATHER_MAP_API_KEY = "YOUR_KEY_HERE"

Firebase:

Create a Firebase project and Realtime Database.

Download google-services.json and place it in app/.

Add Firebase Realtime Database rules appropriate for your use-case (read/write auth as needed).

Build & Run

Select an emulator or a connected Android device and run the app.

Grant any runtime permissions required (e.g., network / location if implemented).

Usage

Search for a city to view current weather and forecasts.

The app stores your search history locally (SQLite).

Severe weather conditions or thresholds (as implemented) will push alerts to Firebase so remote monitors can react.

Project Structure (high level)

app/ — Android application module (Java source, resources).

gradle/, build.gradle.kts — build configuration.

.idea/, .gitignore, Gradle wrappers — project configs. 
GitHub

Contributing

Fork the repository.

Create a feature branch: git checkout -b feature/your-feature

Commit your changes: git commit -m "Add some feature"

Push to your fork and open a Pull Request.

Please keep API keys out of commits. Use environment variables or local config files.

To-Do / Improvements

Add unit and integration tests for data fetch and DB operations.

Implement robust error handling and retry/backoff for network calls.

Add user-configurable alert thresholds and notification channels.

Add CI (GitHub Actions) to run lint/tests on PRs.

License

(No license file found in the repo. Add a LICENSE file — e.g., MIT — if you want to make this open source.) 
GitHub

Acknowledgements

OpenWeatherMap for weather APIs.

Firebase for realtime backend.

Retrofit, GSON, Glide and AndroidX libraries.
    
