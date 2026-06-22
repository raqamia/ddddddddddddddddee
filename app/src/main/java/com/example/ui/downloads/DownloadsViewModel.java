package com.example.ui.downloads;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.data.local.AppDatabase;
import com.example.data.local.entity.DownloadEntity;
import com.example.data.local.entity.FileEntity;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.repository.DownloadRepository;
import com.example.data.repository.FileRepository;

import java.util.ArrayList;
import java.util.List;

/** Offline downloads: the files the user has completed downloading, joined with their metadata. */
public class DownloadsViewModel extends AndroidViewModel {

    private final FileRepository fileRepository;
    private final DownloadRepository downloadRepository;
    private final LiveData<List<FileEntity>> downloads;

    public DownloadsViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        SessionManager sessionManager = new SessionManager(application);
        fileRepository = new FileRepository(SupabaseApiClient.getApi(sessionManager), db.fileDao());
        downloadRepository = new DownloadRepository(SupabaseApiClient.getApi(sessionManager), db.downloadDao());

        this.downloads = Transformations.switchMap(downloadRepository.getCompletedDownloadsLive(), dls -> {
            List<String> ids = new ArrayList<>();
            if (dls != null) for (DownloadEntity d : dls) ids.add(d.fileId);
            // Guard against an empty IN () query, which SQLite rejects.
            if (ids.isEmpty()) return new MutableLiveData<>(new ArrayList<>());
            return fileRepository.getFilesByIdsLive(ids);
        });
    }

    public LiveData<List<FileEntity>> getDownloads() {
        return downloads;
    }
}
