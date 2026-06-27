package com.example.roadguard.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.example.roadguard.data.local.entity.PendingReport;

import java.util.List;

@Dao
public interface PendingReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PendingReport report);

    @Query("SELECT * FROM pending_reports WHERE isUploaded = 0")
    LiveData<List<PendingReport>> getPendingReports();

    @Query("SELECT * FROM pending_reports WHERE isUploaded = 0")
    List<PendingReport> getPendingReportsSync();

    @Query("UPDATE pending_reports SET isUploaded = 1 WHERE id = :id")
    void markUploaded(int id);

    @Query("DELETE FROM pending_reports")
    void deleteAll();
}
