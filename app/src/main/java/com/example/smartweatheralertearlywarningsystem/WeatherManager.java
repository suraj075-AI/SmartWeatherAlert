package com.example.smartweatheralertearlywarningsystem;

// OOP: Interface
public interface WeatherManager {
    void onWeatherFetched(String city, double temp, String description, String icon);
    void onFetchError(String message);
}
