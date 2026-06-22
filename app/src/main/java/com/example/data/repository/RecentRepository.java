package com.example.data.repository;

import androidx.lifecycle.LiveData;

import com.example.data.local.dao.RecentDao;
import com.example.data.local.entity.RecentEntity;
import com.example.util.AppExecutors;

import java.util.List;
import java.util.concurrent.ExecutorService;

/** Tracks recently opened files for the home "continue reading" section. */
public class RecentRepository {

    private final RecentDao dao;
    private final ExecutorService executor = AppExecutors.io();

    public RecentRepository(RecentDao dao) {
        this.dao = dao;
    }

    public LiveData<List<RecentEntity>> getRecentLive() {
        return dao.getRecentLive();
    }

    /** Records (or refreshes) a file as the most recently opened (preserving its last page). */
    public void record(String fileId, String name, String localPath) {
        if (fileId == null) return;
        executor.execute(() -> {
            Integer page = dao.getLastPage(fileId);
            RecentEntity r = new RecentEntity();
            r.fileId = fileId;
            r.name = name;
            r.localPath = localPath;
            r.openedAt = System.currentTimeMillis();
            r.lastPage = page != null ? page : 0;
            dao.upsert(r);
        });
    }

    public interface PageCallback {
        void onPage(int page);
    }

    /** Reads the last page the user reached in a file (0 if none), off the main thread. */
    public void getLastPageAsync(String fileId, PageCallback cb) {
        if (fileId == null) { cb.onPage(0); return; }
        executor.execute(() -> {
            Integer page = dao.getLastPage(fileId);
            int p = page != null ? page : 0;
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> cb.onPage(p));
        });
    }

    public void saveLastPage(String fileId, int page) {
        if (fileId == null) return;
        executor.execute(() -> dao.setLastPage(fileId, page));
    }
}
