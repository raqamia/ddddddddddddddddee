package com.example.ui.notifications;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.remote.dto.NotificationDto;
import com.example.data.repository.NotificationsRepository;

import java.util.List;

public class NotificationsViewModel extends AndroidViewModel {

    private final NotificationsRepository repository;

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        SessionManager sessionManager = new SessionManager(application);
        repository = new NotificationsRepository(SupabaseApiClient.getApi(sessionManager));
    }

    public LiveData<List<NotificationDto>> getNotifications() {
        return repository.getNotifications();
    }

    public LiveData<Boolean> getNetworkError() {
        return repository.getNetworkError();
    }

    public void load() {
        repository.fetch();
    }
}
