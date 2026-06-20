package com.example.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.data.local.dao.FileDao;
import com.example.data.local.dao.SavedDao;
import com.example.data.local.entity.FileEntity;
import com.example.data.local.entity.SavedFileEntity;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiService;
import com.example.data.remote.dto.FileDto;
import com.example.data.remote.dto.SavedFileDto;
import com.example.util.AppExecutors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Server-backed "saved / bookmarked" files. The local {@code saved} table is kept in sync with the
 * Supabase {@code saved_files} table so the favourites list works offline and updates reactively.
 */
public class SavedRepository {

    private final SupabaseApiService api;
    private final SavedDao savedDao;
    private final FileDao fileDao;
    private final SessionManager sessionManager;
    private final ExecutorService executor = AppExecutors.io();
    private final MutableLiveData<Boolean> networkError = new MutableLiveData<>();

    public SavedRepository(SupabaseApiService api, SavedDao savedDao, FileDao fileDao, SessionManager sessionManager) {
        this.api = api;
        this.savedDao = savedDao;
        this.fileDao = fileDao;
        this.sessionManager = sessionManager;
    }

    public LiveData<List<SavedFileEntity>> getSavedLive() {
        return savedDao.getAllLive();
    }

    public LiveData<Integer> isSavedLive(String fileId) {
        return savedDao.isSavedLive(fileId);
    }

    public LiveData<Boolean> getNetworkError() {
        return networkError;
    }

    /** Pulls the user's saved files from the server and replaces the local cache. */
    public void syncFromServer() {
        String userId = sessionManager.getUserId();
        String token = sessionManager.getAccessToken();
        if (api == null || userId == null || token == null) return;

        // Embed the related file row (saved_files -> files) so favourites display even if the
        // file metadata was never cached by browsing that subject.
        api.getSavedFiles("eq." + userId, "*,files(*)", "Bearer " + token).enqueue(new Callback<List<SavedFileDto>>() {
            @Override
            public void onResponse(Call<List<SavedFileDto>> call, Response<List<SavedFileDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SavedFileEntity> entities = new ArrayList<>();
                    List<FileEntity> files = new ArrayList<>();
                    for (SavedFileDto dto : response.body()) {
                        if (dto.getFileId() == null) continue;
                        entities.add(new SavedFileEntity(dto.getFileId()));
                        FileDto f = dto.getFile();
                        if (f != null && f.getId() != null) {
                            FileEntity fe = new FileEntity();
                            fe.id = f.getId();
                            fe.subjectId = f.getSubjectId();
                            fe.category = f.getCategory();
                            fe.name = f.getName();
                            fe.storagePath = f.getStoragePath();
                            fe.sizeBytes = f.getSizeBytes();
                            fe.pageCount = f.getPageCount();
                            fe.uploadedAt = f.getUploadedAt();
                            files.add(fe);
                        }
                    }
                    executor.execute(() -> {
                        if (!files.isEmpty()) fileDao.insertAll(files);
                        savedDao.deleteAll();
                        savedDao.insertAll(entities);
                    });
                    networkError.postValue(false);
                } else {
                    networkError.postValue(true);
                }
            }

            @Override
            public void onFailure(Call<List<SavedFileDto>> call, Throwable t) {
                networkError.postValue(true);
            }
        });
    }

    /** Toggles the saved state for a file, updating the local cache optimistically and the server. */
    public void setSaved(String fileId, boolean save) {
        String userId = sessionManager.getUserId();
        String token = sessionManager.getAccessToken();
        if (fileId == null) return;

        // Optimistic local update so the UI reacts immediately.
        executor.execute(() -> {
            if (save) savedDao.insert(new SavedFileEntity(fileId));
            else savedDao.delete(fileId);
        });

        if (api == null || userId == null || token == null) return;
        String bearer = "Bearer " + token;

        Callback<Void> callback = new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) revert();
                else networkError.postValue(false);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                revert();
            }

            private void revert() {
                // Roll back the optimistic change if the server rejected it.
                executor.execute(() -> {
                    if (save) savedDao.delete(fileId);
                    else savedDao.insert(new SavedFileEntity(fileId));
                });
                networkError.postValue(true);
            }
        };

        if (save) {
            Map<String, String> body = new HashMap<>();
            body.put("user_id", userId);
            body.put("file_id", fileId);
            api.saveFile(body, bearer).enqueue(callback);
        } else {
            api.unsaveFile("eq." + userId, "eq." + fileId, bearer).enqueue(callback);
        }
    }
}
