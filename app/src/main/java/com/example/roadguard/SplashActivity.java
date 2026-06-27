package com.example.roadguard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.roadguard.ui.auth.LoginActivity;
import com.example.roadguard.ui.auth.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(this, RegisterActivity.class));
                finish();
            }, 2000);
        } else {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }, 2000);
        }
        finish();
    }
}