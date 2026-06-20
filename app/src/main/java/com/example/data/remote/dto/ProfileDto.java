package com.example.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ProfileDto {
    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("name")
    private String name;

    @SerializedName("track")
    private String track;

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getTrack() { return track; }
}
