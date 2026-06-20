package com.example.data.repository;

import com.example.data.local.dao.SubjectDao;
import com.example.data.local.entity.SubjectEntity;
import com.example.data.remote.SupabaseApiService;
import com.example.data.remote.dto.SubjectDto;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.lifecycle.LiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubjectRepository {
    private final SupabaseApiService api;
    private final SubjectDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SubjectRepository(SupabaseApiService api, SubjectDao dao) {
        this.api = api;
        this.dao = dao;
    }

    public LiveData<List<SubjectEntity>> getSubjectsByTrackLive(String track) {
        return dao.getSubjectsByTrackLive(track);
    }

    public void fetchAndStoreSubjects(String track) {
        if (api == null) return;
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
                }
            }

            @Override
            public void onFailure(Call<List<SubjectDto>> call, Throwable t) {
            }
        });
    }

    public void insertAll(List<SubjectEntity> subjects) {
        executor.execute(() -> dao.insertAll(subjects));
    }
}
