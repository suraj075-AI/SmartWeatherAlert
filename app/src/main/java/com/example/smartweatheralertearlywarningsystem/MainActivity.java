package com.example.smartweatheralertearlywarningsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smartweatheralertearlywarningsystem.data.WeatherApi;
import com.example.smartweatheralertearlywarningsystem.data.WeatherResponse;
import com.example.smartweatheralertearlywarningsystem.db.WeatherDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// OOP: Inheritance (extends BaseActivity) & Interfaces (implements WeatherManager)
public class MainActivity extends BaseActivity implements WeatherManager {

    private static final String TAG = "WeatherApp";

    private EditText etCity;
    private Button btnFetch;
    private View emptyStateLayout;
    private RecyclerView rvCityList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CityAdapter adapter;
    private List<WeatherResponse> cityWeatherList = new ArrayList<>();

    private WeatherDao weatherDao;
    // Multithreading
    private ExecutorService dbExecutor;
    private ScheduledExecutorService scheduler;

    private String currentCity = "";

    // IMPORTANT: Ensure this API Key is valid and active.
    private static final String API_KEY = "f3b8ee0f6e075312892c53259579829a"; 
    private static final String BASE_URL = "https://api.openweathermap.org/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherDao = new WeatherDao(this);
        try {
            weatherDao.open();
        } catch (Exception e) {
            handleError(e);
        }

        dbExecutor = Executors.newSingleThreadExecutor();

        setupViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start refreshing data every minute
        startAutoRefresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    @Override
    protected void setupViews() {
        etCity = findViewById(R.id.etCity);
        btnFetch = findViewById(R.id.btnFetch);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        rvCityList = findViewById(R.id.rvCityList);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        rvCityList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CityAdapter(cityWeatherList);
        rvCityList.setAdapter(adapter);

        btnFetch.setOnClickListener(v -> {
            String city = etCity.getText().toString().trim();
            if (!city.isEmpty()) {
                currentCity = city;
                fetchWeatherData(city, false);
            } else {
                Toast.makeText(MainActivity.this, "Please enter a city", Toast.LENGTH_SHORT).show();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!currentCity.isEmpty()) {
                fetchWeatherData(currentCity, true);
            } else if (!cityWeatherList.isEmpty()) {
                // If we have a list but no currentCity set (e.g. after restart), use the top city
                currentCity = cityWeatherList.get(0).name;
                fetchWeatherData(currentCity, true);
            } else {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Search for a city first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startAutoRefresh() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                if (!currentCity.isEmpty()) {
                    runOnUiThread(() -> fetchWeatherData(currentCity, true));
                }
            }, 1, 1, TimeUnit.MINUTES);
        }
    }

    private void stopAutoRefresh() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private void fetchWeatherData(String city, boolean isAutoRefresh) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApi api = retrofit.create(WeatherApi.class);
        Call<WeatherResponse> call = api.getWeather(city, API_KEY, "metric");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeatherResponse> call, @NonNull Response<WeatherResponse> response) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    
                    if (isAutoRefresh) {
                        updateExistingCity(weather);
                    } else {
                        addCityCard(weather);
                    }
                    
                    onWeatherFetched(
                            weather.name,
                            weather.main.temp,
                            weather.weather.get(0).description,
                            weather.weather.get(0).icon
                    );
                    
                } else {
                    if (!isAutoRefresh) {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                            Log.e(TAG, "API Error: " + response.code() + " " + errorBody);
                            
                            if (response.code() == 401) {
                                onFetchError("Invalid API Key. Check configuration.");
                            } else if (response.code() == 404) {
                                onFetchError("City not found.");
                            } else {
                                onFetchError("Error " + response.code() + ": " + errorBody);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            onFetchError("Error parsing error response");
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                Log.e(TAG, "Network Failure: ", t);
                if (!isAutoRefresh) {
                    onFetchError("Network Error: " + t.getMessage());
                }
            }
        });
    }

    private void addCityCard(WeatherResponse weather) {
        emptyStateLayout.setVisibility(View.GONE);
        rvCityList.setVisibility(View.VISIBLE);
        
        cityWeatherList.add(0, weather); 
        adapter.notifyItemInserted(0);
        rvCityList.scrollToPosition(0);
        etCity.setText(""); 
    }

    private void updateExistingCity(WeatherResponse weather) {
        if (!cityWeatherList.isEmpty()) {
            WeatherResponse top = cityWeatherList.get(0);
            if (top.name.equalsIgnoreCase(weather.name)) {
                cityWeatherList.set(0, weather);
                adapter.notifyItemChanged(0);
            } else {
                addCityCard(weather);
            }
        } else {
            addCityCard(weather);
        }
    }

    @Override
    public void onWeatherFetched(String city, double temp, String description, String icon) {
        dbExecutor.execute(() -> {
            try {
                synchronized (weatherDao) { 
                    weatherDao.insertWeather(city, temp);
                }
            } catch (Exception e) {
                handleError(e);
            }
        });
    }

    @Override
    public void onFetchError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        weatherDao.close();
        dbExecutor.shutdown();
        stopAutoRefresh();
    }

    class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {
        private List<WeatherResponse> cities;

        public CityAdapter(List<WeatherResponse> cities) {
            this.cities = cities;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WeatherResponse weather = cities.get(position);
            holder.tvCity.setText(weather.name);
            holder.tvTemp.setText((int) weather.main.temp + "°C");
            
            String desc = weather.weather.get(0).main; 
            int low = (int) weather.main.tempMin;
            int high = (int) weather.main.tempMax;
            holder.tvDesc.setText(desc + " " + low + " ~ " + high + "°C");

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WeatherDetailActivity.class);
                intent.putExtra(WeatherDetailActivity.EXTRA_CITY, weather.name);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return cities.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCity, tvTemp, tvDesc;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCity = itemView.findViewById(R.id.tvCityName);
                tvTemp = itemView.findViewById(R.id.tvTemp);
                tvDesc = itemView.findViewById(R.id.tvDescription);
            }
        }
    }
}
