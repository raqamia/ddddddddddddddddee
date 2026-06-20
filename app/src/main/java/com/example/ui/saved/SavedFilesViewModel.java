package com.example.ui.saved;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
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

public class SavedFilesViewModel extends AndroidViewModel {

    private final FileRepository fileRepository;
    private final DownloadRepository downloadRepository;
    private final LiveData<List<FileEntity>> savedFiles;

    public SavedFilesViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        SessionManager sessionManager = new SessionManager(application);
        fileRepository = new FileRepository(
                SupabaseApiClient.getApi(sessionManager),
                db.fileDao()
        );
        downloadRepository = new DownloadRepository(
                SupabaseApiClient.getApi(sessionManager),
                db.downloadDao()
        );

        LiveData<List<DownloadEntity>> downloads = downloadRepository.getCompletedDownloadsLive();
        LiveData<List<FileEntity>> allFiles = fileRepository.getAllFilesLive();

        this.savedFiles = Transformations.switchMap(downloads, dl ->
            Transformations.map(allFiles, files -> {
                if (dl == null || files == null) return new ArrayList<>();
                List<String> downloadedFileIds = new ArrayList<>();
                for (DownloadEntity d : dl) {
                    downloadedFileIds.add(d.fileId);
                }
                List<FileEntity> result = new ArrayList<>();
                for (FileEntity f : files) {
                    if (downloadedFileIds.contains(f.id)) {
                        result.add(f);
                    }
                }
                return result;
            })
        );
    }

    public LiveData<List<FileEntity>> getSavedFiles() {
        return savedFiles;
    }

    public void loadSavedFiles() {
    }
}
