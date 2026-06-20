package com.example.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "subjects")
public class SubjectEntity {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String name;
    public String track;
    public int orderIndex;
}
