package com.example.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class NotificationDto {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getCreatedAt() { return createdAt; }
}
