package com.example.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.data.local.entity.DownloadEntity;
import java.util.List;

@Dao
public interface DownloadDao {
    @Query("SELECT * FROM downloads WHERE state = 'COMPLETED'")
    LiveData<List<DownloadEntity>> getCompletedDownloadsLive();

    @Query("SELECT * FROM downloads WHERE fileId = :fileId LIMIT 1")
    LiveData<DownloadEntity> getDownloadStatusLive(String fileId);
    
    @Query("SELECT * FROM downloads WHERE fileId = :fileId LIMIT 1")
    DownloadEntity getDownloadStatus(String fileId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DownloadEntity download);

    @Query("DELETE FROM downloads WHERE fileId = :fileId")
    void delete(String fileId);

    @Query("DELETE FROM downloads")
    void deleteAll();
}
