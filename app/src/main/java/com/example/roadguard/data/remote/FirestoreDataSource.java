package com.example.roadguard.data.remote;

import com.example.roadguard.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class FirestoreDataSource {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public Task<Void> saveUser(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("email", user.getEmail());
        map.put("displayName", user.getDisplayName());
        map.put("fcmToken", user.getFcmToken());
        map.put("alertsEnabled", user.isAlertsEnabled());
        map.put("notificationRadiusKm", user.getNotificationRadiusKm());
        map.put("geohash", user.getGeohash());
        map.put("lastLatitude", user.getLastLatitude());
        map.put("lastLongitude", user.getLastLongitude());
        map.put("ttsEnabled", user.isTtsEnabled());
        return db.collection("users").document(user.getUid()).set(map, SetOptions.merge());
    }
    public Task<DocumentSnapshot> getUser(String uid) {
        return db.collection("users").document(uid).get();
    }
    public Task<Void> updateUser(String uid, Map<String, Object> updates) {
        return db.collection("users").document(uid).update(updates);
    }

    public Task<DocumentReference> addReport(String userId, double latitude, double longitude,
                                             String geohash, String severity, String notes) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        map.put("geohash", geohash);
        map.put("severity", severity);
        map.put("notes", notes);
        map.put("timestamp", FieldValue.serverTimestamp());
        map.put("status", "reported");
        map.put("upvotes", 0);
        map.put("downvotes", 0);
        return db.collection("reports").add(map);
    }

    public Task<QuerySnapshot> getAllReports() {
        return db.collection("reports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(200)   // or more, adjust as needed
                .get();
    }
    // Real-time listener for map updates
    public ListenerRegistration addReportsListener(EventListener<QuerySnapshot> listener) {
        return db.collection("reports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }
    // Get all reports submitted by a specific user
    public Task<QuerySnapshot> getReportsByUser(String userId) {
        return db.collection("reports")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }

    // Update specific fields of a report
    public Task<Void> updateReport(String reportId, Map<String, Object> updates) {
        return db.collection("reports").document(reportId).update(updates);
    }
    public Task<Void> deleteReport(String reportId) {
        return db.collection("reports").document(reportId).delete();
    }
    public Task<Void> updateFCMToken(String uid, String token) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fcmToken", token);
        return db.collection("users").document(uid).update(updates);
    }

    public static void updateFCMTokenForCurrentUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put("fcmToken", token);
                    FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                            .update(update);
                });
    }
}
