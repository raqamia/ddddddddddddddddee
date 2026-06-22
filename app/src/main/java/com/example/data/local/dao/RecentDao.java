package com.example.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.data.local.entity.RecentEntity;
import java.util.List;

@Dao
public interface RecentDao {
    @Query("SELECT * FROM recent ORDER BY openedAt DESC LIMIT 10")
    LiveData<List<RecentEntity>> getRecentLive();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(RecentEntity recent);

    @Query("DELETE FROM recent WHERE fileId = :fileId")
    void delete(String fileId);
}
