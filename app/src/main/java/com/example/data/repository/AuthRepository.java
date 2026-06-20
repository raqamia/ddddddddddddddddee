package com.example.data.repository;

import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiService;
import com.example.data.remote.dto.AuthResponse;
import com.example.data.remote.dto.LoginRequest;
import com.example.data.remote.dto.RegisterRequest;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final SupabaseApiService api;
    private final SessionManager sessionManager;

    public AuthRepository(SupabaseApiService api, SessionManager sessionManager) {
        this.api = api;
        this.sessionManager = sessionManager;
    }

    public void login(String email, String password, final AuthCallback callback) {
        api.login(new LoginRequest(email, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    AuthResponse auth = response.body();
                    sessionManager.saveTokens(
                            auth.getAccessToken(),
                            auth.getRefreshToken(),
                            auth.getUser().getId(),
                            auth.getExpiresIn()
                    );
                    callback.onSuccess(auth);
                } else {
                    String errorMsg = parseError(response);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("network_error");
            }
        });
    }

    public void register(String name, String email, String password, final AuthCallback callback) {
        RegisterRequest request = new RegisterRequest(email, password, name);
        api.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    AuthResponse auth = response.body();
                    sessionManager.saveTokens(
                            auth.getAccessToken(),
                            auth.getRefreshToken(),
                            auth.getUser().getId(),
                            auth.getExpiresIn()
                    );
                    callback.onSuccess(auth);
                } else {
                    String errorMsg = parseError(response);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("network_error");
            }
        });
    }

    public void logout() {
        String accessToken = sessionManager.getAccessToken();
        if (accessToken != null) {
            api.logout("Bearer " + accessToken).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    sessionManager.clearSession();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    sessionManager.clearSession();
                }
            });
        } else {
            sessionManager.clearSession();
        }
    }

    private String parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                org.json.JSONObject json = new org.json.JSONObject(errorJson);
                if (json.has("error_description")) {
                    return json.getString("error_description");
                }
                if (json.has("msg")) {
                    return json.getString("msg");
                }
                if (json.has("message")) {
                    return json.getString("message");
                }
                if (json.has("error")) {
                    return json.getString("error");
                }
            }
        } catch (Exception ignored) {}
        return "default";
    }

    public interface AuthCallback {
        void onSuccess(AuthResponse auth);
        void onError(String errorCode);
    }
}
