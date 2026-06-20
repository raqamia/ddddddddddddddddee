package com.example.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class SavedFileDto {
    @SerializedName("user_id")
    private String userId;

    @SerializedName("file_id")
    private String fileId;

    @SerializedName("files")
    private FileDto file;

    public String getUserId() { return userId; }
    public String getFileId() { return fileId; }
    public FileDto getFile() { return file; }
}
