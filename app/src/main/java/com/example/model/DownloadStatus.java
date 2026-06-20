package com.example.model;

public class DownloadStatus {
    public enum State {
        NOT_DOWNLOADED, DOWNLOADING, COMPLETED, FAILED
    }

    private String fileId;
    private State state;
    private int progress;
    private long downloadedAt;

    public DownloadStatus() {}

    public DownloadStatus(String fileId, State state, int progress, long downloadedAt) {
        this.fileId = fileId;
        this.state = state;
        this.progress = progress;
        this.downloadedAt = downloadedAt;
    }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public long getDownloadedAt() { return downloadedAt; }
    public void setDownloadedAt(long downloadedAt) { this.downloadedAt = downloadedAt; }
}
