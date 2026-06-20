package com.example.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.data.local.entity.FileEntity;
import java.util.List;

@Dao
public interface FileDao {
    @Query("SELECT * FROM files WHERE subjectId = :subjectId")
    LiveData<List<FileEntity>> getFilesBySubjectLive(String subjectId);

    @Query("SELECT * FROM files")
    LiveData<List<FileEntity>> getAllFilesLive();

    @Query("SELECT * FROM files WHERE id IN (:ids)")
    LiveData<List<FileEntity>> getFilesByIdsLive(List<String> ids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<FileEntity> files);

    @Query("DELETE FROM files WHERE subjectId = :subjectId")
    void deleteBySubject(String subjectId);
}
