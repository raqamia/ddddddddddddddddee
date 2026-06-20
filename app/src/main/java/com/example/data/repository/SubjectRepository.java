package com.example.data.repository;

import com.example.data.local.dao.SubjectDao;
import com.example.data.local.entity.SubjectEntity;
import com.example.data.remote.SupabaseApiService;
import com.example.data.remote.dto.SubjectDto;
import com.example.util.AppExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubjectRepository {
    private final SupabaseApiService api;
    private final SubjectDao dao;
    private final ExecutorService executor = AppExecutors.io();
    private final MutableLiveData<Boolean> networkError = new MutableLiveData<>();

    public SubjectRepository(SupabaseApiService api, SubjectDao dao) {
        this.api = api;
        this.dao = dao;
    }

    public LiveData<List<SubjectEntity>> getSubjectsByTrackLive(String track) {
        return dao.getSubjectsByTrackLive(track);
    }

    /** Emits {@code true} when the last refresh failed so the UI can show a message. */
    public LiveData<Boolean> getNetworkError() {
        return networkError;
    }

    public void fetchAndStoreSubjects(String track) {
        if (api == null || track == null) return;
        String filter = track.equals("both") ? null : "eq." + track;
        Call<List<SubjectDto>> call = api.getSubjects(filter, "is.true", null);
        call.enqueue(new Callback<List<SubjectDto>>() {
            @Override
            public void onResponse(Call<List<SubjectDto>> call, Response<List<SubjectDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SubjectEntity> entities = new ArrayList<>();
                    for (SubjectDto dto : response.body()) {
                        SubjectEntity entity = new SubjectEntity();
                        entity.id = dto.getId();
                        entity.name = dto.getName();
                        entity.track = dto.getTrack();
                        entity.orderIndex = dto.getOrderIndex();
                        entities.add(entity);
                    }
                    executor.execute(() -> {
                        dao.deleteAll();
                        dao.insertAll(entities);
                    });
                    networkError.postValue(false);
                } else {
                    networkError.postValue(true);
                }
            }

            @Override
            public void onFailure(Call<List<SubjectDto>> call, Throwable t) {
                networkError.postValue(true);
            }
        });
    }

    public void insertAll(List<SubjectEntity> subjects) {
        executor.execute(() -> dao.insertAll(subjects));
    }
}
