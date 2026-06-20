package com.example.ui.subject;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.data.local.AppDatabase;
import com.example.data.local.entity.FileEntity;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.repository.FileRepository;

import java.util.ArrayList;
import java.util.List;

public class SubjectFilesViewModel extends AndroidViewModel {

    private final FileRepository repository;
    private final MutableLiveData<String> currentSubjectId = new MutableLiveData<>();
    private final LiveData<List<FileEntity>> files;
    private String currentCategory;

    public SubjectFilesViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        SessionManager sessionManager = new SessionManager(application);
        repository = new FileRepository(
                SupabaseApiClient.getApi(sessionManager),
                db.fileDao()
        );
        files = Transformations.switchMap(currentSubjectId, subjectId ->
                repository.getFilesBySubjectLive(subjectId)
        );
    }

    public LiveData<List<FileEntity>> getFiles() {
        return Transformations.map(files, allFiles -> {
            List<FileEntity> filtered = new ArrayList<>();
            if (allFiles != null && currentCategory != null) {
                for (FileEntity f : allFiles) {
                    if (currentCategory.equals(f.category)) {
                        filtered.add(f);
                    }
                }
            }
            return filtered;
        });
    }

    public void loadFiles(String subjectId, String category) {
        currentCategory = category;
        currentSubjectId.setValue(subjectId);
        repository.fetchAndStoreFiles(subjectId, category);
    }
}
