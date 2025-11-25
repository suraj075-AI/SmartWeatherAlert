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

*![WhatsApp Image 2025-11-25 at 23 11 18_f241cee6](https://github.com/user-attachments/assets/0d8a1e3a-63c1-43c2-9568-ef6a5560be99)


## 🔧 Setup & Installation

1.  **Clone the Repository:**
    
