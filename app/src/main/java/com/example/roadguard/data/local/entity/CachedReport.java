package com.example.roadguard.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;

@Entity(tableName = "cached_reports")
public class CachedReport implements ClusterItem, Serializable {
    @PrimaryKey
    @NonNull
    public String reportId;
    public String userId;
    public double latitude;
    public double longitude;
    public String geohash;
    public String severity;
    public String notes;
    public long timestamp;
    public String status;
    public int upvotes;
    public int downvotes;
    public long lastSyncTimestamp;

    public CachedReport(String reportId, String userId, double latitude, double longitude, String geohash, String severity, String notes, long timestamp, String status, int upvotes, int downvotes, long lastSyncTimestamp) {
        this.reportId = reportId;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.geohash = geohash;
        this.severity = severity;
        this.notes = notes;
        this.timestamp = timestamp;
        this.status = status;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.lastSyncTimestamp = lastSyncTimestamp;
    }

    public CachedReport() {}
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }
    @Override
    public String getTitle() {
        // Return a string that can be used as marker title (optional)
        return "Severity: " + severity;
    }
    @Override
    public String getSnippet() {
        // Return a short snippet (optional)
        return notes != null ? notes : "";
    }
    @Override
    public Float getZIndex() {
        return (float) 0;   // default z‑index; adjust if you want to layer markers
    }
}
