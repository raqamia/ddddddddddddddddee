package com.example.util;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import com.example.data.local.entity.DownloadEntity;
import java.io.File;

public class FileDownloadManager {

    public static String downloadPdf(Context context, String fileId, String fileName, String signedUrl) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) return null;

        Uri uri = Uri.parse(signedUrl);
        String dirName = "ManaraFiles";
        File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dirName);
        if (!dir.exists()) dir.mkdirs();

        String safeName = fileId + "_" + fileName.replaceAll("[^a-zA-Z0-9_.\\-]", "_") + ".pdf";
        String localPath = new File(dir, safeName).getAbsolutePath();

        DownloadManager.Request request = new DownloadManager.Request(uri)
                .setTitle(fileName)
                .setDescription("جارٍ التحميل من منارة…")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(new File(dir, safeName)))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false);

        downloadManager.enqueue(request);
        return localPath;
    }

    public static DownloadEntity queryStatus(Context context, long downloadId, String fileId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) return null;

        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        try (Cursor cursor = downloadManager.query(query)) {
            if (cursor != null && cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                int progress = 0;
                if (status == DownloadManager.STATUS_RUNNING) {
                    long total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    long downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    if (total > 0) progress = (int) (downloaded * 100 / total);
                }

                DownloadEntity entity = new DownloadEntity();
                entity.fileId = fileId;
                entity.progress = progress;
                entity.downloadedAt = System.currentTimeMillis();

                switch (status) {
                    case DownloadManager.STATUS_SUCCESSFUL:
                        entity.state = "COMPLETED";
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                        entity.localPath = path != null ? path.replace("file://", "") : "";
                        break;
                    case DownloadManager.STATUS_FAILED:
                        entity.state = "FAILED";
                        break;
                    case DownloadManager.STATUS_PAUSED:
                    case DownloadManager.STATUS_PENDING:
                    case DownloadManager.STATUS_RUNNING:
                        entity.state = "DOWNLOADING";
                        break;
                }
                return entity;
            }
        } catch (Exception ignored) {}
        return null;
    }
}
