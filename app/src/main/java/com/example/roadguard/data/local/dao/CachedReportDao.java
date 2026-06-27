package com.example.roadguard.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.roadguard.data.local.entity.CachedReport;

import java.util.List;

@Dao
public interface CachedReportDao {
    @Query("SELECT * FROM cached_reports ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<CachedReport>> getRecentReports(int limit);

    @Query("SELECT * FROM cached_reports")
    List<CachedReport> getAllReportsSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CachedReport> reports);

    @Query("DELETE FROM cached_reports WHERE reportId NOT IN (SELECT reportId FROM cached_reports ORDER BY timestamp DESC LIMIT :keepCount)")
    void trimOldReports(int keepCount);

    @Query("DELETE FROM cached_reports")
    void deleteAll();
}
