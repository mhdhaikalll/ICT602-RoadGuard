package com.example.roadguard.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.roadguard.model.User;

@Entity(tableName = "user_profile")
public class UserProfile {
    @PrimaryKey
    public String uid;
    public String email;
    public String displayName;
    public boolean alertsEnabled;
    public int notificationRadiusKm;
    public boolean ttsEnabled;
    public UserProfile() {}
    public UserProfile(User user) {
        this.uid = user.getUid();
        this.email = user.getEmail();
        this.displayName = user.getDisplayName();
        this.alertsEnabled = user.isAlertsEnabled();
        this.notificationRadiusKm = user.getNotificationRadiusKm();
        this.ttsEnabled = user.isTtsEnabled();
    }
}
