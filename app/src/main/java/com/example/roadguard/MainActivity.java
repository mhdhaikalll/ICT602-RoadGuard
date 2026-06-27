package com.example.roadguard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavHostController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.roadguard.ui.map.MapDataViewModel;
import com.example.roadguard.worker.LocationUpdateWorker;
import com.example.roadguard.worker.ReportUploadWorker;
import com.example.roadguard.worker.SyncWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.example.roadguard.util.Constant;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView navigationView = findViewById(R.id.bottom_navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navHostController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navigationView, navHostController);

        scheduleLocationUpdates();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            scheduleBackgroundTasks();
        }
        // Check if launched from notification
        if (getIntent().getExtras() != null) {
            double lat = getIntent().getDoubleExtra("notification_lat", Double.NaN);
            double lng = getIntent().getDoubleExtra("notification_lng", Double.NaN);
            if (!Double.isNaN(lat) && !Double.isNaN(lng)) {
                // Use a shared ViewModel to communicate with MapFragment
                MapDataViewModel mapDataVM = new ViewModelProvider(this).get(MapDataViewModel.class);
                mapDataVM.setNotificationLocation(lat, lng);
            }
        }
    }

    private void scheduleLocationUpdates() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)   // need network to update Firestore
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest locationWork = new PeriodicWorkRequest.Builder(
                LocationUpdateWorker.class,
                30, TimeUnit.MINUTES)            // flexible interval
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "location_update",
                ExistingPeriodicWorkPolicy.KEEP,
                locationWork
        );
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }
    }

    private void scheduleBackgroundTasks() {
        // ---- ReportUploadWorker ----
        Constraints uploadConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)   // any network
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest uploadWork = new PeriodicWorkRequest.Builder(
                ReportUploadWorker.class,
                15, TimeUnit.MINUTES)    // checks every 15 minutes
                .setConstraints(uploadConstraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "report_upload",
                ExistingPeriodicWorkPolicy.KEEP,
                uploadWork
        );

        // ---- SyncWorker (Wi‑Fi only) ----
        Constraints syncConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)  // Wi‑Fi
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest syncWork = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                Constant.SYNC_WORKER_INTERVAL_MINUTES, TimeUnit.MINUTES)   // 15 min
                .setConstraints(syncConstraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "report_sync",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWork
        );

        // ---- LocationUpdateWorker ----
        Constraints locationConstraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest locationWork = new PeriodicWorkRequest.Builder(
                LocationUpdateWorker.class,
                30, TimeUnit.MINUTES)
                .setConstraints(locationConstraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "location_update",
                ExistingPeriodicWorkPolicy.KEEP,
                locationWork
        );
    }
}