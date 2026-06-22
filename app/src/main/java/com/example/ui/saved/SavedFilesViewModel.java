package com.example.ui.saved;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.data.local.AppDatabase;
import com.example.data.local.entity.FileEntity;
import com.example.data.local.entity.SavedFileEntity;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.repository.FileRepository;
import com.example.data.repository.SavedRepository;

import java.util.ArrayList;
import java.util.List;

public class SavedFilesViewModel extends AndroidViewModel {

    private final FileRepository fileRepository;
    private final SavedRepository savedRepository;
    private final LiveData<List<FileEntity>> savedFiles;

    public SavedFilesViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        SessionManager sessionManager = new SessionManager(application);
        fileRepository = new FileRepository(
                SupabaseApiClient.getApi(sessionManager),
                db.fileDao()
        );
        savedRepository = new SavedRepository(
                SupabaseApiClient.getApi(sessionManager),
                db.savedDao(),
                db.fileDao(),
                sessionManager
        );

        // The favourites screen joins the locally cached saved-ids with the cached file metadata.
        this.savedFiles = Transformations.switchMap(savedRepository.getSavedLive(), saved -> {
            List<String> ids = new ArrayList<>();
            if (saved != null) {
                for (SavedFileEntity s : saved) ids.add(s.fileId);
            }
            // Guard against an empty IN () query, which SQLite rejects.
            if (ids.isEmpty()) return new MutableLiveData<>(new ArrayList<>());
            return fileRepository.getFilesByIdsLive(ids);
        });
    }

    public LiveData<List<FileEntity>> getSavedFiles() {
        return savedFiles;
    }

    public LiveData<Boolean> getNetworkError() {
        return savedRepository.getNetworkError();
    }

    public void toggleSave(String fileId, boolean save) {
        savedRepository.setSaved(fileId, save);
    }

    public void loadSavedFiles() {
        savedRepository.syncFromServer();
    }
}
