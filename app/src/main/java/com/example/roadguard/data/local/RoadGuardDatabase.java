package com.example.roadguard.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.roadguard.data.local.dao.CachedReportDao;
import com.example.roadguard.data.local.dao.PendingReportDao;
import com.example.roadguard.data.local.dao.UserProfileDao;
import com.example.roadguard.data.local.entity.UserProfile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {UserProfile.class}, version = 1, exportSchema = false)
public abstract class RoadGuardDatabase extends RoomDatabase {
    public abstract UserProfileDao userProfileDao();
    public abstract PendingReportDao pendingReportDao();
    public abstract CachedReportDao cachedReportDao();
    private static volatile RoadGuardDatabase INSTANCE;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newSingleThreadExecutor();
    public static RoadGuardDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RoadGuardDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RoadGuardDatabase.class, "roadguard.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
