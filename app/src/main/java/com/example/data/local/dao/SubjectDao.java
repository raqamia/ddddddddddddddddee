package com.example.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.data.local.entity.SubjectEntity;
import java.util.List;

@Dao
public interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE track = :track OR track = 'both' ORDER BY orderIndex ASC")
    LiveData<List<SubjectEntity>> getSubjectsByTrackLive(String track);

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    LiveData<SubjectEntity> getSubjectByIdLive(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SubjectEntity> subjects);

    @Query("DELETE FROM subjects")
    void deleteAll();
}
