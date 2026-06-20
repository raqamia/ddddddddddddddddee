package com.example.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** A file the user bookmarked/saved to their account (mirrors the server saved_files table). */
@Entity(tableName = "saved")
public class SavedFileEntity {
    @PrimaryKey
    @NonNull
    public String fileId = "";

    public SavedFileEntity() {}

    public SavedFileEntity(@NonNull String fileId) {
        this.fileId = fileId;
    }
}
