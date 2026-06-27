package com.example.roadguard.model;

public class User {
    private String uid;
    private String email;
    private String displayName;
    private String fcmToken;
    private boolean alertsEnabled;
    private int notificationRadiusKm; // 1, 3, or 5
    private String geohash;
    private double lastLatitude;
    private double lastLongitude;
    private boolean ttsEnabled;

    public User() {}
    public User(String uid, String email, String displayName) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.alertsEnabled = true;
        this.notificationRadiusKm = 3;
        this.ttsEnabled = true;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public boolean isAlertsEnabled() {
        return alertsEnabled;
    }

    public void setAlertsEnabled(boolean alertsEnabled) {
        this.alertsEnabled = alertsEnabled;
    }

    public int getNotificationRadiusKm() {
        return notificationRadiusKm;
    }

    public void setNotificationRadiusKm(int notificationRadiusKm) {
        this.notificationRadiusKm = notificationRadiusKm;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public double getLastLatitude() {
        return lastLatitude;
    }

    public void setLastLatitude(double lastLatitude) {
        this.lastLatitude = lastLatitude;
    }

    public double getLastLongitude() {
        return lastLongitude;
    }

    public void setLastLongitude(double lastLongitude) {
        this.lastLongitude = lastLongitude;
    }

    public boolean isTtsEnabled() {
        return ttsEnabled;
    }

    public void setTtsEnabled(boolean ttsEnabled) {
        this.ttsEnabled = ttsEnabled;
    }


}
