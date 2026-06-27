package com.example.roadguard.data.repository;

import android.app.Application;
import android.content.Intent;

import androidx.lifecycle.LiveData;

import com.example.roadguard.data.local.RoadGuardDatabase;
import com.example.roadguard.data.local.entity.UserProfile;
import com.example.roadguard.data.remote.AuthDataSource;
import com.example.roadguard.data.remote.FirestoreDataSource;
import com.example.roadguard.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {
    private final AuthDataSource authDataSource;
    private final FirestoreDataSource firestoreDataSource;
    private final RoadGuardDatabase localDb;
    public AuthRepository(Application application) {
        this.authDataSource = new AuthDataSource(application);
        firestoreDataSource = new FirestoreDataSource();
        localDb = RoadGuardDatabase.getInstance(application);
    }
    public Task<AuthResult> signUp(String email, String password) {
        return authDataSource.signUp(email, password);
    }
    public Task<AuthResult> signIn(String email, String password) {
        return authDataSource.signIn(email, password);
    }
    public FirebaseUser getCurrentUser() {
        return authDataSource.getCurrentUser();
    }
    public void signOut() {
        authDataSource.signOut();
    }
    public Task<Void> createUserProfile(User user) {
        return firestoreDataSource.saveUser(user);
    }
    public void fetchAndCacheUserProfile(String uid, OnProfileFetchedListener listener) {
        firestoreDataSource.getUser(uid).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    user.setUid(uid); // ensure UID set
                    UserProfile profile = new UserProfile(user);
                    // Cache in Room
                    RoadGuardDatabase.databaseWriteExecutor.execute(() -> {
                        localDb.userProfileDao().insertOrUpdate(profile);
                    });
                    listener.onProfileFetched(profile);
                }
            } else {
                listener.onProfileFetched(null);
            }
        }).addOnFailureListener(e -> listener.onProfileFetched(null));
    }
    public LiveData<UserProfile> getLocalUserProfile() {
        return localDb.userProfileDao().getUserProfile();
    }
    public void updateUserPreferences(String uid, boolean alertsEnabled, int radiusKm, boolean ttsEnabled) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("alertsEnabled", alertsEnabled);
        updates.put("notificationRadiusKm", radiusKm);
        updates.put("ttsEnabled", ttsEnabled);
        firestoreDataSource.updateUser(uid, updates).addOnSuccessListener(aVoid -> {
            // also update Room entity
            RoadGuardDatabase.databaseWriteExecutor.execute(() -> {
                UserProfile cached = localDb.userProfileDao().getUserProfileSync(); // need sync version
                if (cached != null) {
                    cached.alertsEnabled = alertsEnabled;
                    cached.notificationRadiusKm = radiusKm;
                    cached.ttsEnabled = ttsEnabled;
                    localDb.userProfileDao().insertOrUpdate(cached);
                }
            });
        });
    }
    public interface OnProfileFetchedListener {
        void onProfileFetched(UserProfile profile);
    }
    public Intent getGoogleSignInIntent() {
        return authDataSource.getGoogleSignInIntent();
    }
    public Task<FirebaseUser> handleGoogleSignInResult(Intent data) {
        return authDataSource.firebaseAuthWithGoogle(data).continueWithTask(task -> {
            FirebaseUser firebaseUser = task.getResult();
            if (firebaseUser != null) {
                // Check if user profile already exists; if not, create it
                return firestoreDataSource.getUser(firebaseUser.getUid()).continueWithTask(docTask -> {
                    if (!docTask.getResult().exists()) {
                        User user = new User(firebaseUser.getUid(),
                                firebaseUser.getEmail(),
                                firebaseUser.getDisplayName());
                        user.setAlertsEnabled(true);
                        user.setNotificationRadiusKm(3);
                        user.setTtsEnabled(true);
                        return createUserProfile(user);
                    }
                    return Tasks.forResult(null);
                }).continueWith(t -> firebaseUser);
            }
            return Tasks.forResult(null);
        });
    }
}
