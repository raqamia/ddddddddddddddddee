package com.example.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class SubjectDto {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("track")
    private String track;

    @SerializedName("order_index")
    private int orderIndex;

    @SerializedName("is_active")
    private boolean isActive;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getTrack() { return track; }
    public int getOrderIndex() { return orderIndex; }
    public boolean isActive() { return isActive; }
}
