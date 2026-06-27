package com.example.roadguard.ui.report;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.roadguard.data.local.entity.CachedReport;
import com.example.roadguard.data.repository.ReportRepository;
import com.google.firebase.auth.FirebaseAuth;
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
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uid == null) {
            errorMessage.setValue("Not logged in");
            return;
        }
        reportRepository.getUserReports(uid)
                .addOnSuccessListener(querySnapshot -> {
                    List<CachedReport> list = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        CachedReport report = doc.toObject(CachedReport.class);
                        if (report != null) {
                            report.reportId = doc.getId();
                            list.add(report);
                        }
                    }
                    userReports.setValue(list);
                })
                .addOnFailureListener(e -> errorMessage.setValue(e.getMessage()));
    }
}
