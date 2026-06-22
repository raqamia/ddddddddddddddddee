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
import com.example.data.local.entity.DownloadEntity;
import com.example.data.local.entity.FileEntity;
import com.example.data.local.entity.SubjectEntity;
import com.example.data.prefs.AppPreferences;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.remote.dto.ProfileDto;
import com.example.data.repository.ProfileRepository;
import com.example.data.repository.SubjectRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final LiveData<List<FileEntity>> allFiles;
    private final LiveData<List<DownloadEntity>> completedDownloads;
    private final MediatorLiveData<Map<String, Integer>> downloadedPerSubject = new MediatorLiveData<>();

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

        // Downloaded-files count per subject (for the progress label on subject cards).
        allFiles = db.fileDao().getAllFilesLive();
        completedDownloads = db.downloadDao().getCompletedDownloadsLive();
        downloadedPerSubject.addSource(allFiles, f -> recomputeDownloaded());
        downloadedPerSubject.addSource(completedDownloads, c -> recomputeDownloaded());

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

    private void recomputeDownloaded() {
        List<FileEntity> files = allFiles.getValue();
        List<DownloadEntity> dls = completedDownloads.getValue();
        Map<String, Integer> result = new HashMap<>();
        if (files != null && dls != null) {
            Map<String, String> fileToSubject = new HashMap<>();
            for (FileEntity f : files) fileToSubject.put(f.id, f.subjectId);
            for (DownloadEntity d : dls) {
                String subjectId = fileToSubject.get(d.fileId);
                if (subjectId != null) {
                    Integer c = result.get(subjectId);
                    result.put(subjectId, c == null ? 1 : c + 1);
                }
            }
        }
        downloadedPerSubject.setValue(result);
    }

    public LiveData<Map<String, Integer>> getDownloadedPerSubject() {
        return downloadedPerSubject;
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
