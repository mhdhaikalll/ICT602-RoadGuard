package com.example.roadguard;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class RoadGuardApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        // Room database will be a singleton later
    }
}
