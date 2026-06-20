package com.example.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class SignedUrlResponse {
    @SerializedName("signedURL")
    private String signedUrl;

    public String getSignedUrl() { return signedUrl; }
}
