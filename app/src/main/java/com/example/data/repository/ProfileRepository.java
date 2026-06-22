package com.example.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiService;
import com.example.data.remote.dto.ProfileDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Loads the signed-in user's profile (name/email/track) from the Supabase {@code profiles} table. */
public class ProfileRepository {

    private final SupabaseApiService api;
    private final SessionManager sessionManager;
    private final MutableLiveData<ProfileDto> profile = new MutableLiveData<>();

    public ProfileRepository(SupabaseApiService api, SessionManager sessionManager) {
        this.api = api;
        this.sessionManager = sessionManager;
    }

    public LiveData<ProfileDto> getProfile() {
        return profile;
    }

    public void fetchProfile() {
        String userId = sessionManager.getUserId();
        String token = sessionManager.getAccessToken();
        if (api == null || userId == null || token == null) return;

        api.getProfile("eq." + userId, "Bearer " + token).enqueue(new Callback<List<ProfileDto>>() {
            @Override
            public void onResponse(Call<List<ProfileDto>> call, Response<List<ProfileDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    profile.postValue(response.body().get(0));
                }
            }

            @Override
            public void onFailure(Call<List<ProfileDto>> call, Throwable t) {
                // Non-fatal: the UI falls back to a generic greeting.
            }
        });
    }

    public interface UpdateCallback {
        void onDone(boolean ok);
    }

    /** Updates the user's display name in the profiles table. */
    public void updateName(String name, UpdateCallback cb) {
        String userId = sessionManager.getUserId();
        String token = sessionManager.getAccessToken();
        if (api == null || userId == null || token == null) { cb.onDone(false); return; }
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        api.updateProfile("eq." + userId, updates, "Bearer " + token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                cb.onDone(response.isSuccessful());
                if (response.isSuccessful()) fetchProfile();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                cb.onDone(false);
            }
        });
    }

    /** Marks the user as active (updates profiles.last_seen) for the admin "active users" metric. */
    public void markActive() {
        String token = sessionManager.getAccessToken();
        if (api == null || token == null) return;
        api.touchLastSeen(new HashMap<String, String>()).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}
