package com.example.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "downloads")
public class DownloadEntity {
    @PrimaryKey
    @NonNull
    public String fileId = "";
    public String localPath;
    public String state; 
    public int progress;
    public long downloadedAt;
}
