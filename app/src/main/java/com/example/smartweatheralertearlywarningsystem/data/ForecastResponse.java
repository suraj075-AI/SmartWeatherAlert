package com.example.smartweatheralertearlywarningsystem.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastResponse {
    @SerializedName("cod")
    public String cod;
    @SerializedName("message")
    public int message;
    @SerializedName("cnt")
    public int cnt;
    @SerializedName("list")
    public List<ForecastItem> list;
    @SerializedName("city")
    public City city;

    public static class ForecastItem {
        @SerializedName("dt")
        public long dt;
        @SerializedName("main")
        public WeatherResponse.Main main;
        @SerializedName("weather")
        public List<WeatherResponse.Weather> weather;
        @SerializedName("clouds")
        public WeatherResponse.Clouds clouds;
        @SerializedName("wind")
        public WeatherResponse.Wind wind;
        @SerializedName("visibility")
        public int visibility;
        @SerializedName("pop")
        public double pop;
        @SerializedName("sys")
        public WeatherResponse.Sys sys;
        @SerializedName("dt_txt")
        public String dt_txt;
    }

    public static class City {
        @SerializedName("id")
        public int id;
        @SerializedName("name")
        public String name;
        @SerializedName("coord")
        public WeatherResponse.Coord coord;
        @SerializedName("country")
        public String country;
        @SerializedName("population")
        public int population;
        @SerializedName("timezone")
        public int timezone;
        @SerializedName("sunrise")
        public long sunrise;
        @SerializedName("sunset")
        public long sunset;
    }
}
