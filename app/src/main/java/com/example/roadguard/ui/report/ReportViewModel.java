package com.example.roadguard.ui.report;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.roadguard.data.repository.ReportRepository;
import com.example.roadguard.util.GeohashHelper;
import com.example.roadguard.util.NetworkUtil;
import com.google.firebase.auth.FirebaseAuth;

public class ReportViewModel extends AndroidViewModel {
    private final ReportRepository reportRepository;
    private final MutableLiveData<Boolean> submitSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private Location selectedLocation;

    public ReportViewModel(@NonNull Application application) {
        super(application);
        reportRepository = new ReportRepository(application);
    }

    public LiveData<Boolean> getSubmitSuccess() { return submitSuccess; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void setLocation(Location location) {
        this.selectedLocation = location;
    }

    public void submitReport(String severity, String notes) {
        if (selectedLocation == null) {
            errorMessage.setValue("Location not set");
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            errorMessage.setValue("Not logged in");
            return;
        }

        double lat = selectedLocation.getLatitude();
        double lng = selectedLocation.getLongitude();
        String geohash = GeohashHelper.encode(lat, lng, 9); // precision 9

        if (NetworkUtil.isNetworkAvailable(getApplication())) {
            // Online: direct submit
            reportRepository.submitOnline(userId, lat, lng, geohash, severity, notes)
                    .addOnSuccessListener(documentReference -> submitSuccess.postValue(true))
                    .addOnFailureListener(e -> errorMessage.postValue(e.getMessage()));
        } else {
            // Offline: save locally
            reportRepository.saveOffline(userId, lat, lng, geohash, severity, notes);
            submitSuccess.postValue(true); // optimistic
        }
    }
}
