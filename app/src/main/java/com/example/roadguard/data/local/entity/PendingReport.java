package com.example.roadguard.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_reports")
public class PendingReport {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String userId;
    public double latitude;
    public double longitude;
    public String geohash;
    public String severity;
    public String notes;
    public long timestamp;
    public boolean isUploaded;

    public PendingReport(int id, String userId, double latitude, double longitude, String geohash, String severity, String notes, long timestamp, boolean isUploaded) {
        this.id = id;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geohash = geohash;
        this.severity = severity;
        this.notes = notes;
        this.timestamp = timestamp;
        this.isUploaded = isUploaded;
    }

    public PendingReport() {
        isUploaded = false;
    }


}
