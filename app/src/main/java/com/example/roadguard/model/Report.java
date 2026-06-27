package com.example.roadguard.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Report {
    private String reportId;
    private String userId;
    private double latitude;
    private double longitude;
    private String geohash;
    private String severity; // "low", "medium", "high"
    private String notes;
    @ServerTimestamp
    private Date timestamp;
    private String status;   // "reported", "verified", "fixed"
    private int upvotes;
    private int downvotes;
    public Report() {}

    public Report(String reportId, String userId, double latitude, double longitude, String geohash, String severity, String notes, Date timestamp, String status, int upvotes, int downvotes) {
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
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }
}
