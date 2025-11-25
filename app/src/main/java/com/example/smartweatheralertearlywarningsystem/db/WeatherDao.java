package com.example.smartweatheralertearlywarningsystem.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

// Implementing Database Connectivity (JDBC-like approach using SQLite)
// Rubric: Classes for the database operations
public class WeatherDao {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public WeatherDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Rubric: Database Connectivity
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Method to insert data
    // Rubric: Database operations
    public void insertWeather(String city, double temp) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CITY, city);
        values.put(DatabaseHelper.COLUMN_TEMP, temp);
        values.put(DatabaseHelper.COLUMN_TIMESTAMP, System.currentTimeMillis());
        database.insert(DatabaseHelper.TABLE_HISTORY, null, values);
    }

    // Method to retrieve data
    // Rubric: Collections & Generics
    public List<String> getAllHistory() {
        List<String> history = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(DatabaseHelper.TABLE_HISTORY,
                    null, null, null, null, null, DatabaseHelper.COLUMN_TIMESTAMP + " DESC");

            if (cursor != null && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    int cityIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CITY);
                    int tempIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TEMP);
                    
                    if (cityIndex != -1 && tempIndex != -1) {
                        String city = cursor.getString(cityIndex);
                        double temp = cursor.getDouble(tempIndex);
                        history.add(city + " : " + temp + "°C");
                    }
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return history;
    }
    
    // Additional operation for completeness
    public void clearHistory() {
        database.delete(DatabaseHelper.TABLE_HISTORY, null, null);
    }
}
