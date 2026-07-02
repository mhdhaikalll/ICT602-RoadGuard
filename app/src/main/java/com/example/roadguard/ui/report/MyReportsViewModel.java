package com.example.roadguard.ui.report;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.roadguard.data.local.entity.CachedReport;
import com.example.roadguard.data.repository.ReportRepository;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyReportsViewModel extends AndroidViewModel {
    private final ReportRepository reportRepository;
    private final MutableLiveData<List<CachedReport>> userReports = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public MyReportsViewModel(@NonNull Application application) {
        super(application);
        reportRepository = new ReportRepository(application);
        loadReports();
    }

    public LiveData<List<CachedReport>> getUserReports() {
        return userReports;
    }
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadReports() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            errorMessage.setValue("You must be logged in");
            return;
        }
        String uid = currentUser.getUid();
        reportRepository.getUserReports(uid)
                .addOnSuccessListener(querySnapshot -> {
                    List<CachedReport> list = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        CachedReport report = new CachedReport();
                        report.reportId = doc.getId();
                        report.userId = doc.getString("userId");
                        report.latitude = doc.getDouble("latitude") != null ? doc.getDouble("latitude") : 0;
                        report.longitude = doc.getDouble("longitude") != null ? doc.getDouble("longitude") : 0;
                        report.geohash = doc.getString("geohash");
                        report.severity = doc.getString("severity");
                        report.notes = doc.getString("notes");
                        // Safe timestamp conversion
                        com.google.firebase.Timestamp timestamp = doc.getTimestamp("timestamp");
                        report.timestamp = (timestamp != null) ? timestamp.toDate().getTime() : System.currentTimeMillis();
                        report.status = doc.getString("status");
                        report.upvotes = doc.getLong("upvotes") != null ? doc.getLong("upvotes").intValue() : 0;
                        report.downvotes = doc.getLong("downvotes") != null ? doc.getLong("downvotes").intValue() : 0;
                        list.add(report);
                    }
                    userReports.setValue(list);
                })
                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
    }
}
