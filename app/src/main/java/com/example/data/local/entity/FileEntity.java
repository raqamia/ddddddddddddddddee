package com.example.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "files")
public class FileEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String subjectId;
    public String category;
    public String name;
    public String storagePath;
    public long sizeBytes;
    public int pageCount;
    public String uploadedAt;
}
