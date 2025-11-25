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

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";
    private DatabaseReference databaseReference;

    public FirebaseManager() {
        // Initialize Firebase Database
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference("weather_alerts");
    }

    // Save weather data to Firebase
    public void saveWeatherData(String city, double temp, String description) {
        String key = databaseReference.push().getKey();
        if (key != null) {
            Map<String, Object> weatherData = new HashMap<>();
            weatherData.put("city", city);
            weatherData.put("temp", temp);
            weatherData.put("description", description);
            weatherData.put("timestamp", System.currentTimeMillis());

            databaseReference.child(key).setValue(weatherData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Data saved successfully to Firebase"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save data to Firebase", e));
        }
    }
    
    // Optional: Read data (for future features)
    public void readAlerts() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Log.d(TAG, "Alert: " + postSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}
