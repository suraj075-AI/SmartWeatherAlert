package com.example.smartweatheralertearlywarningsystem;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smartweatheralertearlywarningsystem.data.ForecastResponse;
import com.example.smartweatheralertearlywarningsystem.data.WeatherApi;
import com.example.smartweatheralertearlywarningsystem.data.WeatherResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherDetailActivity extends BaseActivity {

    public static final String EXTRA_CITY = "extra_city";
    private static final String TAG = "WeatherDetailActivity";

    private TextView tvCityName, tvDate, tvCurrentTemp, tvTempRange, tvHumidity, tvWind;
    // private ImageView ivCurrentIcon; // Removed as per new UI design
    private RecyclerView rvHourly, rvDaily;

    // IMPORTANT: Ensure this API Key is valid and active.
    private static final String API_KEY = "f3b8ee0f6e075312892c53259579829a";
    private static final String BASE_URL = "https://api.openweathermap.org/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_detail);

        setupViews();

        String city = getIntent().getStringExtra(EXTRA_CITY);
        if (city != null) {
            fetchWeatherData(city);
            fetchForecastData(city);
        }
    }

    @Override
    protected void setupViews() {
        tvCityName = findViewById(R.id.tvCityName);
        tvDate = findViewById(R.id.tvDate);
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        tvTempRange = findViewById(R.id.tvTempRange);
        tvHumidity = findViewById(R.id.tvHumidity);
        // tvClouds = findViewById(R.id.tvClouds); // Removed
        tvWind = findViewById(R.id.tvWind);
        // ivCurrentIcon = findViewById(R.id.ivCurrentIcon); // Removed
        rvHourly = findViewById(R.id.rvHourly);
        rvDaily = findViewById(R.id.rvDaily);

        // Horizontal scroll for lists
        rvHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // Daily list is now Vertical in the new design, but LinearLayoutManager defaults to vertical if not specified?
        // Wait, previous code was Horizontal. The new XML shows it inside a Card/Layout but the screenshot 2 shows Vertical list.
        // However, my XML for rvDaily has app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" which defaults to Vertical.
        // But in Java I was setting it to Horizontal. I should change it to Vertical or let XML handle it.
        // Let's enforce Vertical for Daily to match Screenshot 2.
        rvDaily.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    private void fetchWeatherData(String city) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi api = retrofit.create(WeatherApi.class);
        api.getWeather(city, API_KEY, "metric").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateCurrentWeather(response.body());
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Weather Fetch Failed", t);
                handleError(new Exception(t));
            }
        });
    }

    private void fetchForecastData(String city) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi api = retrofit.create(WeatherApi.class);
        api.getForecast(city, API_KEY, "metric").enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(@NonNull Call<ForecastResponse> call, @NonNull Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateForecast(response.body());
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Forecast Fetch Failed", t);
                handleError(new Exception(t));
            }
        });
    }

    private void handleApiError(Response<?> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            Log.e(TAG, "API Error: " + response.code() + " " + errorBody);
            
            if (response.code() == 401) {
                Toast.makeText(this, "Invalid API Key. Check configuration.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Error " + response.code() + ": " + errorBody, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCurrentWeather(WeatherResponse weather) {
        tvCityName.setText(weather.name + ", " + weather.sys.country);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM d, h:mm a", Locale.getDefault());
        String dateStr = dateFormat.format(new Date(weather.dt * 1000));
        String desc = weather.weather.get(0).main;
        tvDate.setText(desc + " • " + dateStr);

        tvCurrentTemp.setText((int) weather.main.temp + "°");
        tvTempRange.setText("High " + (int) weather.main.tempMax + "° • Low " + (int) weather.main.tempMin + "°");
        
        tvHumidity.setText(weather.main.humidity + "%");
        // tvClouds.setText(weather.clouds.all + "%"); // Removed
        tvWind.setText(weather.wind.speed + " KPH"); 

        // String iconUrl = "https://openweathermap.org/img/wn/" + weather.weather.get(0).icon + "@2x.png";
        // Glide.with(this).load(iconUrl).into(ivCurrentIcon); // Removed
    }

    private void updateForecast(ForecastResponse forecast) {
        // 1. Hourly Adapter (Using first 8 items ~ 24h)
        List<ForecastResponse.ForecastItem> hourlyList = new ArrayList<>();
        if (forecast.list != null) {
            for (int i = 0; i < Math.min(8, forecast.list.size()); i++) {
                hourlyList.add(forecast.list.get(i));
            }
        }
        HourlyAdapter hourlyAdapter = new HourlyAdapter(hourlyList);
        rvHourly.setAdapter(hourlyAdapter);

        // 2. Daily Adapter (Filtering for ~noon each day)
        List<ForecastResponse.ForecastItem> dailyList = new ArrayList<>();
        String lastDate = "";
        if (forecast.list != null) {
            for (ForecastResponse.ForecastItem item : forecast.list) {
                 String datePart = item.dt_txt.split(" ")[0];
                 if (!datePart.equals(lastDate) && item.dt_txt.contains("12:00:00")) {
                     dailyList.add(item);
                     lastDate = datePart;
                 }
            }
        }
        DailyAdapter dailyAdapter = new DailyAdapter(dailyList);
        rvDaily.setAdapter(dailyAdapter);
    }

    // --- Adapters ---

    class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {
        private List<ForecastResponse.ForecastItem> items;

        public HourlyAdapter(List<ForecastResponse.ForecastItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chart_point, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ForecastResponse.ForecastItem item = items.get(position);
            holder.tvTemp.setText((int) item.main.temp + "°");
            SimpleDateFormat sdf = new SimpleDateFormat("h a", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(item.dt * 1000)));
            
            // Simple dynamic height for the bar
            ViewGroup.LayoutParams params = holder.viewBar.getLayoutParams();
            params.height = (int) (item.main.temp * 5 + 50); // Arbitrary scaling for demo
            holder.viewBar.setLayoutParams(params);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTemp, tvTime;
            View viewBar;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTemp = itemView.findViewById(R.id.tvTempPoint);
                tvTime = itemView.findViewById(R.id.tvTimePoint);
                viewBar = itemView.findViewById(R.id.viewGraphBar);
            }
        }
    }

    class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.ViewHolder> {
        private List<ForecastResponse.ForecastItem> items;
        private int selectedPosition = 0; // For highlighting the first item

        public DailyAdapter(List<ForecastResponse.ForecastItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ForecastResponse.ForecastItem item = items.get(position);
            
            SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
            holder.tvDay.setText(sdf.format(new Date(item.dt * 1000)));
            
            holder.tvHigh.setText((int) item.main.tempMax + "°");
            holder.tvLow.setText((int) item.main.tempMin + "°");

            String iconUrl = "https://openweathermap.org/img/wn/" + item.weather.get(0).icon + ".png";
            Glide.with(WeatherDetailActivity.this).load(iconUrl).into(holder.ivIcon);

            if (position == selectedPosition) {
                 holder.itemView.setBackgroundResource(R.drawable.bg_selected_day);
            } else {
                 holder.itemView.setBackground(null);
            }
            
            holder.itemView.setOnClickListener(v -> {
                int prev = selectedPosition;
                selectedPosition = holder.getAdapterPosition();
                notifyItemChanged(prev);
                notifyItemChanged(selectedPosition);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDay, tvHigh, tvLow;
            ImageView ivIcon;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDay = itemView.findViewById(R.id.tvDayName);
                tvHigh = itemView.findViewById(R.id.tvHighTemp);
                tvLow = itemView.findViewById(R.id.tvLowTemp);
                ivIcon = itemView.findViewById(R.id.ivDailyIcon);
            }
        }
    }
}
