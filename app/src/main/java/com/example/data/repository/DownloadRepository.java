package com.example.data.repository;

import com.example.data.local.dao.DownloadDao;
import com.example.data.local.entity.DownloadEntity;
import com.example.data.remote.SupabaseApiService;
import com.example.util.AppExecutors;
import java.util.List;
import java.util.concurrent.ExecutorService;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;

public class DownloadRepository {
    private final SupabaseApiService api;
    private final DownloadDao dao;
    private final ExecutorService executor = AppExecutors.io();

    public DownloadRepository(SupabaseApiService api, DownloadDao dao) {
        this.api = api;
        this.dao = dao;
    }

    public LiveData<List<DownloadEntity>> getCompletedDownloadsLive() {
        return dao.getCompletedDownloadsLive();
    }

    public LiveData<DownloadEntity> getDownloadStatusLive(String fileId) {
        return dao.getDownloadStatusLive(fileId);
    }

    public DownloadEntity getDownloadStatus(String fileId) {
        return dao.getDownloadStatus(fileId);
    }

    public interface StatusCallback {
        void onResult(DownloadEntity status);
    }

    /** Reads the download status off the main thread and delivers the result back on the main thread. */
    public void getDownloadStatusAsync(String fileId, StatusCallback callback) {
        executor.execute(() -> {
            DownloadEntity status = dao.getDownloadStatus(fileId);
            new Handler(Looper.getMainLooper()).post(() -> callback.onResult(status));
        });
    }

    public void insert(DownloadEntity download) {
        executor.execute(() -> dao.insert(download));
    }

    public void delete(String fileId) {
        executor.execute(() -> dao.delete(fileId));
    }
}
