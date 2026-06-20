package com.example.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.data.local.entity.SavedFileEntity;
import java.util.List;

@Dao
public interface SavedDao {
    @Query("SELECT * FROM saved")
    LiveData<List<SavedFileEntity>> getAllLive();

    @Query("SELECT fileId FROM saved")
    List<String> getAllIds();

    @Query("SELECT COUNT(*) FROM saved WHERE fileId = :fileId")
    LiveData<Integer> isSavedLive(String fileId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SavedFileEntity saved);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SavedFileEntity> saved);

    @Query("DELETE FROM saved WHERE fileId = :fileId")
    void delete(String fileId);

    @Query("DELETE FROM saved")
    void deleteAll();
}
