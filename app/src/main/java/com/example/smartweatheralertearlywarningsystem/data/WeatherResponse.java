package com.example.smartweatheralertearlywarningsystem.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {
    @SerializedName("coord")
    public Coord coord;
    @SerializedName("weather")
    public List<Weather> weather;
    @SerializedName("main")
    public Main main;
    @SerializedName("wind")
    public Wind wind;
    @SerializedName("clouds")
    public Clouds clouds;
    @SerializedName("sys")
    public Sys sys;
    @SerializedName("name")
    public String name;
    @SerializedName("cod")
    public int cod;
    @SerializedName("dt")
    public long dt;

    public static class Coord {
        public double lon;
        public double lat;
    }

    public static class Weather {
        public int id;
        public String main;
        public String description;
        public String icon;
    }

    public static class Main {
        public double temp;
        @SerializedName("feels_like")
        public double feelsLike;
        @SerializedName("temp_min")
        public double tempMin;
        @SerializedName("temp_max")
        public double tempMax;
        public double pressure;
        public int humidity;
    }

    public static class Wind {
        public double speed;
    }

    public static class Clouds {
        public int all;
    }

    public static class Sys {
        public String country;
        public long sunrise;
        public long sunset;
    }
}
