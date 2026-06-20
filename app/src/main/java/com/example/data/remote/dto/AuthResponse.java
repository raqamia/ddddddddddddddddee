package com.example.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("expires_in")
    private long expiresIn;

    @SerializedName("user")
    private UserDto user;

    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public long getExpiresIn() { return expiresIn; }
    public UserDto getUser() { return user; }

    public static class UserDto {
        @SerializedName("id")
        private String id;

        @SerializedName("email")
        private String email;

        public String getId() { return id; }
        public String getEmail() { return email; }
    }
}
