package com.example.data.remote;

import android.util.Log;
import com.example.data.prefs.SessionManager;
import com.example.util.Constants;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class AuthInterceptor implements Interceptor {
    private final SessionManager sessionManager;
    private static final String TAG = "AuthInterceptor";

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (sessionManager != null) {
            String refreshToken = sessionManager.getRefreshToken();
            if (refreshToken != null && !refreshToken.isEmpty() && !sessionManager.hasValidSession()) {
                synchronized (this) {
                    if (!sessionManager.hasValidSession()) {
                        try {
                            refreshAccessToken();
                        } catch (Exception e) {
                            Log.e(TAG, "فشل تحديث التوكن", e);
                        }
                    }
                }
            }
        }

        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder()
                .header("apikey", Constants.SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation");

        if (sessionManager != null) {
            String accessToken = sessionManager.getAccessToken();
            if (accessToken != null && !accessToken.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + accessToken);
            }
        }

        Request request = requestBuilder.build();
        Response response = chain.proceed(request);

        if (response.code() == 401 && sessionManager != null && sessionManager.getRefreshToken() != null) {
            synchronized (this) {
                try {
                    refreshAccessToken();
                } catch (Exception e) {
                    // Refresh failed: return the original 401 response (still open) to the caller.
                    Log.e(TAG, "فشل تحديث التوكن بعد 401", e);
                    return response;
                }
                // Refresh succeeded: discard the old response and retry the original request.
                response.close();
                Request retryRequest = original.newBuilder()
                        .header("apikey", Constants.SUPABASE_ANON_KEY)
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=representation")
                        .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                        .build();
                return chain.proceed(retryRequest);
            }
        }

        return response;
    }

    private void refreshAccessToken() throws Exception {
        OkHttpClient refreshClient = new OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        JSONObject payload = new JSONObject();
        payload.put("refresh_token", sessionManager.getRefreshToken());
        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));

        String refreshUrl = Constants.SUPABASE_URL;
        if (!refreshUrl.endsWith("/")) refreshUrl += "/";
        refreshUrl += "auth/v1/token?grant_type=refresh_token";

        Request refreshRequest = new Request.Builder()
                .url(refreshUrl)
                .post(body)
                .header("apikey", Constants.SUPABASE_ANON_KEY)
                .header("Content-Type", "application/json")
                .build();

        try (Response refreshResponse = refreshClient.newCall(refreshRequest).execute()) {
            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                String responseBody = refreshResponse.body().string();
                JSONObject jsonObject = new JSONObject(responseBody);
                String newAccessToken = jsonObject.getString("access_token");
                String newRefreshToken = jsonObject.getString("refresh_token");
                long expiresIn = jsonObject.getLong("expires_in");
                String userId = jsonObject.getJSONObject("user").getString("id");
                sessionManager.saveTokens(newAccessToken, newRefreshToken, userId, expiresIn);
            } else {
                sessionManager.clearSession();
            }
        }
    }
}
