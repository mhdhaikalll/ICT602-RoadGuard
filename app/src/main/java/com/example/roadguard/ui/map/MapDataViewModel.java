package com.example.roadguard.ui.map;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;


public class MapDataViewModel extends AndroidViewModel {
    private final MutableLiveData<LatLng> notificationLocation = new MutableLiveData<>();
    public MapDataViewModel(@NonNull Application application) {
        super(application);
    }
    public void setNotificationLocation(double lat, double lng) {
        notificationLocation.setValue(new LatLng(lat, lng));
    }

    public LiveData<LatLng> getNotificationLocation() {
        return notificationLocation;
    }
}
