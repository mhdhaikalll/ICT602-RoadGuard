package com.example.roadguard.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.roadguard.data.local.entity.UserProfile;

@Dao
public interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(UserProfile userProfile);

    @Query("SELECT * FROM user_profile LIMIT 1")
    LiveData<UserProfile> getUserProfile();

    @Query("DELETE FROM user_profile")
    void deleteAll();

    @Query("SELECT * FROM user_profile LIMIT 1")
    UserProfile getUserProfileSync();
}
