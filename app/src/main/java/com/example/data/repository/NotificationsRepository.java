package com.example.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.data.remote.SupabaseApiService;
import com.example.data.remote.dto.NotificationDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Loads in-app announcements created from the admin panel (notifications table). */
public class NotificationsRepository {

    private final SupabaseApiService api;
    private final MutableLiveData<List<NotificationDto>> notifications = new MutableLiveData<>();
    private final MutableLiveData<Boolean> networkError = new MutableLiveData<>();

    public NotificationsRepository(SupabaseApiService api) {
        this.api = api;
    }

    public LiveData<List<NotificationDto>> getNotifications() {
        return notifications;
    }

    public LiveData<Boolean> getNetworkError() {
        return networkError;
    }

    public void fetch() {
        if (api == null) return;
        api.getNotifications("created_at.desc").enqueue(new Callback<List<NotificationDto>>() {
            @Override
            public void onResponse(Call<List<NotificationDto>> call, Response<List<NotificationDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    notifications.postValue(response.body());
                    networkError.postValue(false);
                } else {
                    networkError.postValue(true);
                }
            }

            @Override
            public void onFailure(Call<List<NotificationDto>> call, Throwable t) {
                networkError.postValue(true);
            }
        });
    }
}
