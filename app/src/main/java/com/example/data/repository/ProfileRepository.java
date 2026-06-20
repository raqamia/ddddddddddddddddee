package com.example.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiService;
import com.example.data.remote.dto.ProfileDto;

import java.util.List;

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
}
