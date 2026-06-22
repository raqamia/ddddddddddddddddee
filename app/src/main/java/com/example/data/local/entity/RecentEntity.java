package com.example.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** A file the user recently opened, used for the "continue reading" section. */
@Entity(tableName = "recent")
public class RecentEntity {
    @PrimaryKey
    @NonNull
    public String fileId = "";
    public String name;
    public String localPath;
    public long openedAt;
    public int lastPage = 0;
}
