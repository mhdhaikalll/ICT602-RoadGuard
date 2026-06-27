package com.example.roadguard.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.roadguard.data.local.entity.UserProfile;
import com.example.roadguard.data.repository.AuthRepository;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final LiveData<UserProfile> userProfile;
    private final MutableLiveData<Boolean> logoutEvent = new MutableLiveData<>();
    public ProfileViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        userProfile = authRepository.getLocalUserProfile();
    }
    public LiveData<UserProfile> getUserProfile() {
        return userProfile;
    }
    public void refreshUserProfile() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uid != null) {
            authRepository.fetchAndCacheUserProfile(uid, profile -> {});
        }
    }
    public void updateAlertsEnabled(boolean enabled) {
        UserProfile profile = userProfile.getValue();
        if (profile != null) {
            authRepository.updateUserPreferences(profile.uid, enabled, profile.notificationRadiusKm, profile.ttsEnabled);
        }
    }
    public void updateNotificationRadius(int radiusKm) {
        UserProfile profile = userProfile.getValue();
        if (profile != null) {
            authRepository.updateUserPreferences(profile.uid, profile.alertsEnabled, radiusKm, profile.ttsEnabled);
        }
    }
    public void updateTtsEnabled(boolean enabled) {
        UserProfile profile = userProfile.getValue();
        if (profile != null) {
            authRepository.updateUserPreferences(profile.uid, profile.alertsEnabled, profile.notificationRadiusKm, enabled);
        }
    }
    public void logout() {
        authRepository.signOut();
        logoutEvent.setValue(true);
    }
    public LiveData<Boolean> getLogoutEvent() {
        return logoutEvent;
    }
}
