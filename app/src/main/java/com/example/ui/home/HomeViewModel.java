package com.example.ui.home;

import android.app.Application;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.data.local.AppDatabase;
import com.example.data.local.entity.SubjectEntity;
import com.example.data.prefs.AppPreferences;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.remote.dto.ProfileDto;
import com.example.data.repository.ProfileRepository;
import com.example.data.repository.SubjectRepository;

public class HomeViewModel extends AndroidViewModel {

    private final SubjectRepository repository;
    private final ProfileRepository profileRepository;
    private final AppPreferences prefs;
    private final SessionManager sessionManager;
    private final MutableLiveData<String> trackGreeting = new MutableLiveData<>("مرحباً يا بطل");
    private final MutableLiveData<String> currentTrack = new MutableLiveData<>();
    private final LiveData<java.util.List<SubjectEntity>> subjects;
    private final LiveData<String> userName;
    private final MediatorLiveData<String> greeting = new MediatorLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        prefs = new AppPreferences(application);
        sessionManager = new SessionManager(application);
        repository = new SubjectRepository(
                SupabaseApiClient.getApi(sessionManager),
                db.subjectDao()
        );
        profileRepository = new ProfileRepository(
                SupabaseApiClient.getApi(sessionManager),
                sessionManager
        );
        subjects = Transformations.switchMap(currentTrack, track ->
                repository.getSubjectsByTrackLive(track)
        );
        userName = Transformations.map(profileRepository.getProfile(),
                p -> p != null ? p.getName() : null);

        // Greet by name once the profile loads, otherwise fall back to the track label.
        greeting.addSource(trackGreeting, g -> recomputeGreeting());
        greeting.addSource(userName, n -> recomputeGreeting());

        profileRepository.fetchProfile();
        profileRepository.markActive(); // updates last_seen for the admin "active users" metric
        // The hosting fragment triggers the initial subjects load in onViewCreated.
    }

    private void recomputeGreeting() {
        String name = userName.getValue();
        if (!TextUtils.isEmpty(name)) {
            greeting.setValue("مرحباً، " + name + " 👋");
        } else {
            greeting.setValue(trackGreeting.getValue());
        }
    }

    public LiveData<java.util.List<SubjectEntity>> getSubjects() {
        return subjects;
    }

    public LiveData<String> getGreeting() {
        return greeting;
    }

    public LiveData<ProfileDto> getProfile() {
        return profileRepository.getProfile();
    }

    public LiveData<Boolean> getNetworkError() {
        return repository.getNetworkError();
    }

    public void loadSubjects(String track) {
        currentTrack.setValue(track);
        if ("scientific".equals(track)) {
            trackGreeting.setValue("مسار علمي");
        } else if ("literary".equals(track)) {
            trackGreeting.setValue("مسار أدبي");
        } else {
            trackGreeting.setValue("اختر مسارك");
        }
        repository.fetchAndStoreSubjects(track);
    }
}
