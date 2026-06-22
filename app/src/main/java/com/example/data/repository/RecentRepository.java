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

    /** Records (or refreshes) a file as the most recently opened. */
    public void record(String fileId, String name, String localPath) {
        if (fileId == null) return;
        executor.execute(() -> {
            RecentEntity r = new RecentEntity();
            r.fileId = fileId;
            r.name = name;
            r.localPath = localPath;
            r.openedAt = System.currentTimeMillis();
            dao.upsert(r);
        });
    }
}
