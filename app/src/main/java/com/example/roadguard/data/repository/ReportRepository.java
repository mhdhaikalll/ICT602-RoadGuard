package com.example.roadguard.data.repository;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;

import com.example.roadguard.data.local.RoadGuardDatabase;
import com.example.roadguard.data.local.dao.CachedReportDao;
import com.example.roadguard.data.local.dao.PendingReportDao;
import com.example.roadguard.data.local.entity.CachedReport;
import com.example.roadguard.data.local.entity.PendingReport;
import com.example.roadguard.data.remote.FirestoreDataSource;
import com.example.roadguard.model.Report;
import com.example.roadguard.util.GeohashHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportRepository {
    private final FirestoreDataSource firestoreDataSource;
    private final PendingReportDao pendingReportDao;
    private final CachedReportDao cachedReportDao;

    public ReportRepository(Application application) {
        firestoreDataSource = new FirestoreDataSource();
        RoadGuardDatabase db = RoadGuardDatabase.getInstance(application);
        pendingReportDao = db.pendingReportDao();
        cachedReportDao = db.cachedReportDao();
    }

    // Submit to Firestore directly (online)
    public Task<DocumentReference> submitOnline(String userId, double lat, double lng,
                                                String geohash, String severity, String notes) {
        return firestoreDataSource.addReport(userId, lat, lng, geohash, severity, notes);
    }

    // Save offline pending report
    public void saveOffline(String userId, double lat, double lng, String geohash,
                            String severity, String notes) {
        PendingReport pr = new PendingReport();
        pr.userId = userId;
        pr.latitude = lat;
        pr.longitude = lng;
        pr.geohash = geohash;
        pr.severity = severity;
        pr.notes = notes;
        pr.timestamp = System.currentTimeMillis();
        pr.isUploaded = false;
        RoadGuardDatabase.databaseWriteExecutor.execute(() -> {
            pendingReportDao.insert(pr);
        });
    }

    public void syncReportsFromServer() {
        firestoreDataSource.getAllReports().addOnSuccessListener(querySnapshot -> {
            List<CachedReport> list = new ArrayList<>();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Report report = doc.toObject(Report.class);
                if (report == null) continue;

                CachedReport cr = new CachedReport();
                cr.reportId = doc.getId();
                cr.userId = report.getUserId();
                cr.latitude = report.getLatitude();
                cr.longitude = report.getLongitude();
                cr.geohash = report.getGeohash();
                cr.severity = report.getSeverity();
                cr.notes = report.getNotes() != null ? report.getNotes() : "";

                // Safe timestamp handling
                if (report.getTimestamp() != null) {
                    cr.timestamp = report.getTimestamp().getTime();
                } else {
                    cr.timestamp = System.currentTimeMillis();  // fallback
                }

                cr.status = report.getStatus() != null ? report.getStatus() : "reported";
                cr.upvotes = report.getUpvotes();
                cr.downvotes = report.getDownvotes();
                cr.lastSyncTimestamp = System.currentTimeMillis();
                list.add(cr);
            }
            RoadGuardDatabase.databaseWriteExecutor.execute(() -> {
                cachedReportDao.deleteAll();
                cachedReportDao.insertAll(list);
                cachedReportDao.trimOldReports(200);
            });
        }).addOnFailureListener(e -> {
            // optional: log error
        });
    }
    // Get pending reports for offline sync (used by worker)
    public List<PendingReport> getPendingReportsSync() {
        return pendingReportDao.getPendingReportsSync();
    }
    public LiveData<List<PendingReport>> getPendingReports() {
        return pendingReportDao.getPendingReports();
    }
    public void markAsUploaded(int id) {
        RoadGuardDatabase.databaseWriteExecutor.execute(() -> {
            pendingReportDao.markUploaded(id);
        });
    }
    public LiveData<List<CachedReport>> getCachedReports(int limit) {
        return cachedReportDao.getRecentReports(limit);
    }
    public ListenerRegistration startReportListener(EventListener<QuerySnapshot> listener) {
        return firestoreDataSource.addReportsListener(listener);
    }
    public Task<QuerySnapshot> getUserReports(String userId) {
        return firestoreDataSource.getReportsByUser(userId);
    }
    public Task<Void> updateReport(String reportId, Map<String, Object> updates) {
        return firestoreDataSource.updateReport(reportId, updates);
    }
    public Task<Void> deleteReport(String reportId) {
        return firestoreDataSource.deleteReport(reportId);
    }
    public static void updateUserGeohash(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        client.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null) return;
                    String geohash = GeohashHelper.encode(location.getLatitude(), location.getLongitude(), 9);
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) return;
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("geohash", geohash);
                    updates.put("lastLatitude", location.getLatitude());
                    updates.put("lastLongitude", location.getLongitude());
                    FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                            .update(updates);
                });
    }
}
