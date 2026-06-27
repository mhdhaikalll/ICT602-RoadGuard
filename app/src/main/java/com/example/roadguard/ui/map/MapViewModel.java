package com.example.roadguard.ui.map;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.roadguard.data.local.entity.CachedReport;
import com.example.roadguard.data.repository.ReportRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class MapViewModel extends AndroidViewModel {
    private final ReportRepository reportRepository;
    private final LiveData<List<CachedReport>> cachedReports;
    private ListenerRegistration firestoreListener;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);

    public MapViewModel(@NonNull Application application) {
        super(application);
        reportRepository = new ReportRepository(application);

        // Load from Room initially
        cachedReports = reportRepository.getCachedReports(200);

        // Trigger a sync from server (online) and then start real-time listener
        reportRepository.syncReportsFromServer();
        startFirestoreListener();
    }

    private void startFirestoreListener() {
        firestoreListener = reportRepository.startReportListener((value, error) -> {
            if (error != null) {
                // Offline or error – Room already has old data
                isLoading.postValue(false);
                return;
            }
            if (value != null) {
                // When Firestore data changes, update the cache
                reportRepository.syncReportsFromServer();
            }
            isLoading.postValue(false);
        });
    }

    public LiveData<List<CachedReport>> getReports() {
        return cachedReports;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }
}
