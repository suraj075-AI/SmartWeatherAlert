package com.example.smartweatheralertearlywarningsystem.db;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

/**
 * FirebaseManager handles all interactions with the Firebase Realtime Database.
 * It follows the Singleton pattern implicitly by being instantiated once in MainActivity,
 * though a formal Singleton pattern could be applied if needed across multiple Activities.
 */
public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private DatabaseReference databaseReference;

    /**
     * Constructor initializes the Firebase Database instance and reference.
     * The "weather_alerts" node will contain all pushed weather data entries.
     */
    public FirebaseManager() {
        // Initialize Firebase Database
        // ensure google-services.json is correctly placed in the app directory
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("weather_alerts");
    }

    /**
     * Saves weather data to Firebase.
     * Generates a unique key for each entry using push().
     *
     * @param city        The name of the city
     * @param temp        The current temperature
     * @param description A brief description of the weather (e.g., "cloudy")
     */
    public void saveWeatherData(String city, double temp, String description) {
        // Create a unique key for the new entry
        String key = databaseReference.push().getKey();
        
        if (key != null) {
            Map<String, Object> weatherData = new HashMap<>();
            weatherData.put("city", city);
            weatherData.put("temp", temp);
            weatherData.put("description", description);
            weatherData.put("timestamp", System.currentTimeMillis());

            // Write data to the database
            databaseReference.child(key).setValue(weatherData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Data saved successfully to Firebase for city: " + city))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save data to Firebase for city: " + city, e));
        } else {
            Log.e(TAG, "Failed to generate a unique key for Firebase entry.");
        }
    }
    
    /**
     * Optional method to read alerts from the database.
     * This listens for real-time updates to the "weather_alerts" node.
     */
    public void readAlerts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "Alert received: " + postSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}
