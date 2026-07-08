package com.example.util;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import com.example.data.local.entity.DownloadEntity;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FileDownloadManager {

    private static final String DIR_NAME = "ManaraFiles";
    private static final ExecutorService CHECK_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "UrlCheck");
        t.setDaemon(true);
        return t;
    });

    /** The directory where downloaded PDFs are stored. */
    public static File getDownloadsDir(Context context) {
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), DIR_NAME);
    }

    /** Total size in bytes of all downloaded files. */
    public static long getDownloadsSize(Context context) {
        File dir = getDownloadsDir(context);
        long total = 0;
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) total += f.isFile() ? f.length() : 0;
        return total;
    }

    /** Deletes all downloaded files from disk. Returns the number of files removed. */
    public static int clearDownloads(Context context) {
        File dir = getDownloadsDir(context);
        int n = 0;
        File[] files = dir.listFiles();
        if (files != null) for (File f : files) { if (f.isFile() && f.delete()) n++; }
        return n;
    }

    /** Formats a byte count into a human-readable string. */
    public static String formatSize(long bytes) {
        if (bytes <= 0) return "0 MB";
        String[] u = {"B", "KB", "MB", "GB"};
        int i = 0; double n = bytes;
        while (n >= 1024 && i < u.length - 1) { n /= 1024; i++; }
        return String.format(java.util.Locale.US, "%.1f %s", n, u[i]);
    }

    public static String downloadPdf(Context context, String fileId, String fileName, String signedUrl) {
        if (!isSignedUrlValid(signedUrl)) return null;

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) return null;

        Uri uri = Uri.parse(signedUrl);
        File dir = getDownloadsDir(context);
        if (!dir.exists()) dir.mkdirs();

        String safeId = fileId.replaceAll("[^a-zA-Z0-9_-]", "_");
        String safeName = safeId + "_" + fileName.replaceAll("[^a-zA-Z0-9_.\\-]", "_") + ".pdf";
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
                long total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                long downloaded = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                if (status == DownloadManager.STATUS_RUNNING && total > 0) {
                    progress = (int) (downloaded * 100 / total);
                }

                DownloadEntity entity = new DownloadEntity();
                entity.fileId = fileId;
                entity.progress = progress;
                entity.downloadedAt = System.currentTimeMillis();

                switch (status) {
                    case DownloadManager.STATUS_SUCCESSFUL:
                        String path = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                        String localPath = path != null ? path.replace("file://", "") : "";
                        if (!TextUtils.isEmpty(localPath)) {
                            File file = new File(localPath);
                            if (!file.exists() || file.length() == 0) {
                                entity.state = "FAILED";
                                file.delete();
                                break;
                            }
                        }
                        entity.state = "COMPLETED";
                        entity.localPath = localPath;
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

    /** Checks that the signed URL is non-null, parseable, and has a token query parameter. */
    private static boolean isSignedUrlValid(String signedUrl) {
        if (TextUtils.isEmpty(signedUrl)) return false;
        try {
            Uri uri = Uri.parse(signedUrl);
            if (uri.getScheme() == null || uri.getHost() == null) return false;
            String token = uri.getQueryParameter("token");
            return !TextUtils.isEmpty(token);
        } catch (Exception e) {
            return false;
        }
    }

    /** Checks whether a signed URL is reachable (HTTP 200) before enqueuing the download. */
    public static void checkUrlReachable(String signedUrl, UrlCheckCallback callback) {
        CHECK_EXECUTOR.execute(() -> {
            boolean reachable = false;
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .followRedirects(false)
                    .build();
            Request request = new Request.Builder()
                    .url(signedUrl)
                    .head()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                reachable = response.isSuccessful();
            } catch (Exception ignored) {}
            if (reachable) {
                callback.onReachable();
            } else {
                callback.onUnreachable();
            }
        });
    }

    public interface UrlCheckCallback {
        void onReachable();
        void onUnreachable();
    }
}
