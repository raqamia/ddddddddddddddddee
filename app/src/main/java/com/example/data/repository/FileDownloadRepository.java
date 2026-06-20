package com.example.data.repository;

import android.content.Context;
import com.example.data.local.dao.DownloadDao;
import com.example.data.local.entity.DownloadEntity;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiService;
import com.example.util.AppExecutors;
import com.example.util.Constants;
import com.example.util.FileDownloadManager;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileDownloadRepository {
    private final SupabaseApiService api;
    private final DownloadDao downloadDao;
    private final SessionManager sessionManager;
    private final Context context;
    private final ExecutorService executor = AppExecutors.io();

    public FileDownloadRepository(SupabaseApiService api, DownloadDao downloadDao, SessionManager sessionManager, Context context) {
        this.api = api;
        this.downloadDao = downloadDao;
        this.sessionManager = sessionManager;
        this.context = context;
    }

    public interface DownloadCallback {
        void onSuccess(String localPath);
        void onError(String errorCode);
    }

    public void downloadFile(String fileId, String fileName, String storagePath, DownloadCallback callback) {
        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null) {
            callback.onError("token_expired");
            return;
        }

        String signedUrlEndpoint = Constants.SUPABASE_STORAGE_URL + "object/sign/" + Constants.PDF_BUCKET + "/" + storagePath;
        Map<String, Integer> body = new HashMap<>();
        body.put("expiresIn", 300);

        executor.execute(() -> {
            DownloadEntity pending = new DownloadEntity();
            pending.fileId = fileId;
            pending.state = "DOWNLOADING";
            pending.progress = 0;
            downloadDao.insert(pending);
        });

        String authHeader = "Bearer " + accessToken;
        Call<com.example.data.remote.dto.SignedUrlResponse> call = api.getSignedUrl(
                Constants.PDF_BUCKET, storagePath, body, authHeader
        );
        call.enqueue(new Callback<com.example.data.remote.dto.SignedUrlResponse>() {
            @Override
            public void onResponse(Call<com.example.data.remote.dto.SignedUrlResponse> call,
                                   Response<com.example.data.remote.dto.SignedUrlResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSignedUrl() != null) {
                    String fullUrl = Constants.SUPABASE_URL + response.body().getSignedUrl();
                    String localPath = FileDownloadManager.downloadPdf(context, fileId, fileName, fullUrl);
                    if (localPath != null) {
                        executor.execute(() -> {
                            DownloadEntity entity = new DownloadEntity();
                            entity.fileId = fileId;
                            entity.state = "COMPLETED";
                            entity.localPath = localPath;
                            entity.progress = 100;
                            entity.downloadedAt = System.currentTimeMillis();
                            downloadDao.insert(entity);
                        });
                        callback.onSuccess(localPath);
                    } else {
                        executor.execute(() -> {
                            DownloadEntity failed = new DownloadEntity();
                            failed.fileId = fileId;
                            failed.state = "FAILED";
                            downloadDao.insert(failed);
                        });
                        callback.onError("download_failed");
                    }
                } else {
                    executor.execute(() -> {
                        DownloadEntity failed = new DownloadEntity();
                        failed.fileId = fileId;
                        failed.state = "FAILED";
                        downloadDao.insert(failed);
                    });
                    callback.onError("signed_url_failed");
                }
            }

            @Override
            public void onFailure(Call<com.example.data.remote.dto.SignedUrlResponse> call, Throwable t) {
                executor.execute(() -> {
                    DownloadEntity failed = new DownloadEntity();
                    failed.fileId = fileId;
                    failed.state = "FAILED";
                    downloadDao.insert(failed);
                });
                callback.onError("network_error");
            }
        });
    }
}
