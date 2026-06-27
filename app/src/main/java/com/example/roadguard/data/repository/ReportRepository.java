package com.example.roadguard.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.roadguard.data.local.RoadGuardDatabase;
import com.example.roadguard.data.local.dao.CachedReportDao;
import com.example.roadguard.data.local.dao.PendingReportDao;
import com.example.roadguard.data.local.entity.CachedReport;
import com.example.roadguard.data.local.entity.PendingReport;
import com.example.roadguard.data.remote.FirestoreDataSource;
import com.example.roadguard.model.Report;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
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
                CachedReport cr = new CachedReport();
                cr.reportId = doc.getId();
                cr.userId = report.getUserId();
                cr.latitude = report.getLatitude();
                cr.longitude = report.getLongitude();
                cr.geohash = report.getGeohash();
                cr.severity = report.getSeverity();
                cr.notes = report.getNotes();
                cr.timestamp = report.getTimestamp().getTime();
                cr.status = report.getStatus();
                cr.upvotes = report.getUpvotes();
                cr.downvotes = report.getDownvotes();
                cr.lastSyncTimestamp = System.currentTimeMillis();
                list.add(cr);
            }
            RoadGuardDatabase.databaseWriteExecutor.execute(() -> {
                cachedReportDao.deleteAll();               // clear old
                cachedReportDao.insertAll(list);           // insert fresh
                cachedReportDao.trimOldReports(200);       // keep last 200
            });
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
}
