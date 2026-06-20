package com.example.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.data.local.AppDatabase;
import com.example.data.local.entity.SubjectEntity;
import com.example.data.prefs.AppPreferences;
import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
import com.example.data.repository.SubjectRepository;

public class HomeViewModel extends AndroidViewModel {

    private final SubjectRepository repository;
    private final AppPreferences prefs;
    private final SessionManager sessionManager;
    private final MutableLiveData<String> greeting = new MutableLiveData<>("مرحباً يا بطل");
    private final MutableLiveData<String> currentTrack = new MutableLiveData<>();
    private final LiveData<java.util.List<SubjectEntity>> subjects;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        prefs = new AppPreferences(application);
        sessionManager = new SessionManager(application);
        repository = new SubjectRepository(
                SupabaseApiClient.getApi(sessionManager),
                db.subjectDao()
        );
        subjects = Transformations.switchMap(currentTrack, track ->
                repository.getSubjectsByTrackLive(track)
        );
        String savedTrack = prefs.getUserTrack();
        if (savedTrack != null) {
            loadSubjects(savedTrack);
        }
    }

    public LiveData<java.util.List<SubjectEntity>> getSubjects() {
        return subjects;
    }

    public LiveData<String> getGreeting() {
        return greeting;
    }

    public void loadSubjects(String track) {
        currentTrack.setValue(track);
        if ("scientific".equals(track)) {
            greeting.setValue("مسار علمي");
        } else if ("literary".equals(track)) {
            greeting.setValue("مسار أدبي");
        } else {
            greeting.setValue("اختر مسارك");
        }
        repository.fetchAndStoreSubjects(track);
    }
}
