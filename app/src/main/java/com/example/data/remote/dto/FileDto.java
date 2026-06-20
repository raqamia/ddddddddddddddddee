package com.example.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class FileDto {
    @SerializedName("id")
    private String id;

    @SerializedName("subject_id")
    private String subjectId;

    @SerializedName("category")
    private String category;

    @SerializedName("name")
    private String name;

    @SerializedName("storage_path")
    private String storagePath;

    @SerializedName("size_bytes")
    private long sizeBytes;

    @SerializedName("page_count")
    private int pageCount;

    @SerializedName("uploaded_at")
    private String uploadedAt;

    public String getId() { return id; }
    public String getSubjectId() { return subjectId; }
    public String getCategory() { return category; }
    public String getName() { return name; }
    public String getStoragePath() { return storagePath; }
    public long getSizeBytes() { return sizeBytes; }
    public int getPageCount() { return pageCount; }
    public String getUploadedAt() { return uploadedAt; }
}
