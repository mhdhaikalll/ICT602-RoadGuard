package com.example.roadguard.worker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.roadguard.util.GeohashHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class LocationUpdateWorker extends Worker {

    public LocationUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Check permission
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Result.failure();   // can't get location without permission
        }

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(getApplicationContext());

        try {
            // Get last known location (coarse, battery efficient)
            Location location = Tasks.await(fusedLocationClient.getLastLocation());
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                String geohash = GeohashHelper.encode(lat, lng, 9);

                // Update Firestore user document
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (uid != null) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("geohash", geohash);
                    updates.put("lastLatitude", lat);
                    updates.put("lastLongitude", lng);
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .update(updates);
                }
                return Result.success();
            } else {
                return Result.retry();   // location not available, try later
            }
        } catch (ExecutionException | InterruptedException e) {
            return Result.retry();
        }
    }
}
