package com.example.roadguard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.roadguard.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);   // only inflate the layout, no EdgeToEdge

        new Handler().postDelayed(() -> {
            // Check authentication AFTER the splash delay
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(SplashActivity.this, com.example.roadguard.MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();   // finish INSIDE the delayed code, after starting the next activity
        }, 2000);
    }
}