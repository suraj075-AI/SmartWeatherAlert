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
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smartweatheralertearlywarningsystem.data.WeatherApi;
import com.example.smartweatheralertearlywarningsystem.data.WeatherResponse;
import com.example.smartweatheralertearlywarningsystem.db.FirebaseManager;
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
    private ImageView ivEdit, ivAdd, ivBack;
    private View emptyStateLayout;
    private RecyclerView rvCityList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvNoCities;
    private View ivIllustration;
    private CityAdapter adapter;
    private List<WeatherResponse> cityWeatherList = new ArrayList<>();

    private final WeatherDao weatherDao; 
    private FirebaseManager firebaseManager;
    private ExecutorService dbExecutor;
    private ScheduledExecutorService scheduler;

    private String currentCity = "";
    private boolean isEditMode = false;

    private static final String API_KEY = "f3b8ee0f6e075312892c53259579829a"; 
    private static final String BASE_URL = "https://api.openweathermap.org/";

    public MainActivity() {
        this.weatherDao = null; 
    }

    private final Object dbLock = new Object();
    private WeatherDao daoInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        daoInstance = new WeatherDao(this);
        try {
            daoInstance.open();
        } catch (Exception e) {
            handleError(e);
        }
        
        try {
            firebaseManager = new FirebaseManager();
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed: " + e.getMessage());
        }

        dbExecutor = Executors.newSingleThreadExecutor();

        setupViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        
        // Toolbar Icons
        ivEdit = findViewById(R.id.ivEdit);
        ivAdd = findViewById(R.id.ivAdd);
        ivBack = findViewById(R.id.ivBack);

        ivIllustration = findViewById(R.id.ivIllustration);
        tvNoCities = findViewById(R.id.tvNoCities);
        
        rvCityList = findViewById(R.id.rvCityList);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        rvCityList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CityAdapter(cityWeatherList);
        rvCityList.setAdapter(adapter);

        // Click Listeners for Main Features
        btnFetch.setOnClickListener(v -> {
            String city = etCity.getText().toString().trim();
            if (!city.isEmpty()) {
                currentCity = city;
                fetchWeatherData(city, false);
            } else {
                Toast.makeText(MainActivity.this, "Please enter a city", Toast.LENGTH_SHORT).show();
            }
        });

        // Click Listeners for Toolbar Icons
        ivBack.setOnClickListener(v -> {
            onBackPressed();
        });

        ivAdd.setOnClickListener(v -> {
            etCity.requestFocus();
            // Show keyboard implicitly
            Toast.makeText(MainActivity.this, "Enter city name to add", Toast.LENGTH_SHORT).show();
        });

        ivEdit.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            adapter.setEditMode(isEditMode);
            if (isEditMode) {
                Toast.makeText(MainActivity.this, "Edit Mode ON: Tap delete icon to remove city", Toast.LENGTH_SHORT).show();
                ivEdit.setAlpha(0.5f); // Visual feedback
            } else {
                Toast.makeText(MainActivity.this, "Edit Mode OFF", Toast.LENGTH_SHORT).show();
                ivEdit.setAlpha(1.0f);
            }
        });


        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (!currentCity.isEmpty()) {
                fetchWeatherData(currentCity, true);
            } else if (!cityWeatherList.isEmpty()) {
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
            scheduler.scheduleWithFixedDelay(() -> {
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
                            Log.e(TAG, "Error parsing error response", e);
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
        if (ivIllustration != null) ivIllustration.setVisibility(View.GONE);
        if (tvNoCities != null) tvNoCities.setVisibility(View.GONE);
        
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
                synchronized (dbLock) { 
                    if (daoInstance != null) {
                        daoInstance.insertWeather(city, temp);
                    }
                }
            } catch (Exception e) {
                handleError(e);
            }
        });
        
        if (firebaseManager != null) {
            firebaseManager.saveWeatherData(city, temp, description);
        }
    }

    @Override
    public void onFetchError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (daoInstance != null) {
            daoInstance.close();
        }
        dbExecutor.shutdown();
        stopAutoRefresh();
    }

    class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {
        private List<WeatherResponse> cities;
        private boolean editMode = false;

        public CityAdapter(List<WeatherResponse> cities) {
            this.cities = cities;
        }

        public void setEditMode(boolean editMode) {
            this.editMode = editMode;
            notifyDataSetChanged();
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

            // Toggle Delete Icon
            holder.ivDelete.setVisibility(editMode ? View.VISIBLE : View.GONE);
            
            holder.ivDelete.setOnClickListener(v -> {
                cities.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cities.size());
                
                if (cities.isEmpty()) {
                    rvCityList.setVisibility(View.GONE);
                    if (ivIllustration != null) ivIllustration.setVisibility(View.VISIBLE);
                    if (tvNoCities != null) tvNoCities.setVisibility(View.VISIBLE);
                }
            });

            holder.itemView.setOnClickListener(v -> {
                if (!editMode) {
                    Intent intent = new Intent(MainActivity.this, WeatherDetailActivity.class);
                    intent.putExtra(WeatherDetailActivity.EXTRA_CITY, weather.name);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return cities.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCity, tvTemp, tvDesc;
            ImageView ivDelete;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCity = itemView.findViewById(R.id.tvCityName);
                tvTemp = itemView.findViewById(R.id.tvTemp);
                tvDesc = itemView.findViewById(R.id.tvDescription);
                ivDelete = itemView.findViewById(R.id.ivDelete);
            }
        }
    }
}
