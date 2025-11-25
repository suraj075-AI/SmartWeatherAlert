package com.example.smartweatheralertearlywarningsystem;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

// OOP: Inheritance
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // OOP: Exception Handling helper
    protected void handleError(Exception e) {
        e.printStackTrace();
        runOnUiThread(() -> 
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    // OOP: Polymorphism (Abstract method)
    protected abstract void setupViews();
}
