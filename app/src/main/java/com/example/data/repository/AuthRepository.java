package com.example.data.repository;

import com.example.data.prefs.SessionManager;
import com.example.data.remote.SupabaseApiClient;
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
                    SupabaseApiClient.resetClient();
                    callback.onSuccess(auth);
                } else {
                    callback.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("network_error");
            }
        });
    }

    public void register(String name, String email, String password, final AuthCallback callback) {
        api.register(new RegisterRequest(email, password, name)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                // خطأ حقيقي من الـ API (مثل: إيميل مكرر، باسورد ضعيف)
                if (!response.isSuccessful()) {
                    callback.onError(parseError(response));
                    return;
                }

                AuthResponse auth = response.body();
                if (auth == null) {
                    callback.onError("default");
                    return;
                }

                // تسجيل ناجح + Supabase أرجع token مباشرة (Email Confirmation معطّل)
                if (auth.getAccessToken() != null && auth.getUser() != null) {
                    sessionManager.saveTokens(
                            auth.getAccessToken(),
                            auth.getRefreshToken(),
                            auth.getUser().getId(),
                            auth.getExpiresIn()
                    );
                    SupabaseApiClient.resetClient();
                    callback.onSuccess(auth);
                } else {
                    // تسجيل ناجح لكن يحتاج تأكيد الإيميل
                    callback.onError("email_confirmation_required");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("network_error");
            }
        });
    }

    /** Sends a password-reset email via Supabase Auth. */
    public void resetPassword(String email, final AuthCallback callback) {
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        api.recoverPassword(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError(parseError(response));
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
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
                    SupabaseApiClient.resetClient();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    sessionManager.clearSession();
                    SupabaseApiClient.resetClient();
                }
            });
        } else {
            sessionManager.clearSession();
            SupabaseApiClient.resetClient();
        }
    }

    private String parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                org.json.JSONObject json = new org.json.JSONObject(errorJson);
                if (json.has("error_description")) return json.getString("error_description");
                if (json.has("msg"))               return json.getString("msg");
                if (json.has("message"))           return json.getString("message");
                if (json.has("error"))             return json.getString("error");
            }
        } catch (Exception ignored) {}
        return "default";
    }

    public interface AuthCallback {
        void onSuccess(AuthResponse auth);
        void onError(String errorCode);
    }
}