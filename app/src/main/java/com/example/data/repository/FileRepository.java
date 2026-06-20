package com.example.data.repository;

import com.example.data.local.dao.FileDao;
import com.example.data.local.entity.FileEntity;
import com.example.data.remote.SupabaseApiService;
import com.example.data.remote.dto.FileDto;
import com.example.util.AppExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileRepository {
    private final SupabaseApiService api;
    private final FileDao dao;
    private final ExecutorService executor = AppExecutors.io();
    private final MutableLiveData<Boolean> networkError = new MutableLiveData<>();

    public FileRepository(SupabaseApiService api, FileDao dao) {
        this.api = api;
        this.dao = dao;
    }

    public LiveData<List<FileEntity>> getFilesBySubjectLive(String subjectId) {
        return dao.getFilesBySubjectLive(subjectId);
    }

    public LiveData<List<FileEntity>> getAllFilesLive() {
        return dao.getAllFilesLive();
    }

    /** Emits {@code true} when the last refresh failed so the UI can show a message. */
    public LiveData<Boolean> getNetworkError() {
        return networkError;
    }

    public void fetchAndStoreFiles(String subjectId, String category) {
        if (api == null || subjectId == null || category == null) return;
        Call<List<FileDto>> call = api.getFiles("eq." + subjectId, "eq." + category, null);
        call.enqueue(new Callback<List<FileDto>>() {
            @Override
            public void onResponse(Call<List<FileDto>> call, Response<List<FileDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FileEntity> entities = new ArrayList<>();
                    for (FileDto dto : response.body()) {
                        FileEntity entity = new FileEntity();
                        entity.id = dto.getId();
                        entity.subjectId = dto.getSubjectId();
                        entity.category = dto.getCategory();
                        entity.name = dto.getName();
                        entity.storagePath = dto.getStoragePath();
                        entity.sizeBytes = dto.getSizeBytes();
                        entity.pageCount = dto.getPageCount();
                        entity.uploadedAt = dto.getUploadedAt();
                        entities.add(entity);
                    }
                    executor.execute(() -> {
                        dao.deleteBySubject(subjectId);
                        dao.insertAll(entities);
                    });
                    networkError.postValue(false);
                } else {
                    networkError.postValue(true);
                }
            }

            @Override
            public void onFailure(Call<List<FileDto>> call, Throwable t) {
                networkError.postValue(true);
            }
        });
    }

    public void insertAll(List<FileEntity> files) {
        executor.execute(() -> dao.insertAll(files));
    }
}
