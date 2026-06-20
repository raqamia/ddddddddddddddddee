package com.example.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import com.example.util.Constants;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private SharedPreferences prefs;

    public SessionManager(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            prefs = EncryptedSharedPreferences.create(
                    Constants.PREFS_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Log.e(TAG, "فشل إنشاء التخزين المشفر", e);
            throw new RuntimeException("لا يمكن بدء التطبيق بدون تخزين آمن", e);
        }
    }

    public void saveTokens(String accessToken, String refreshToken, String userId, long expiresIn) {
        if (accessToken == null || refreshToken == null || userId == null) return;
        prefs.edit()
                .putString(Constants.KEY_ACCESS_TOKEN, accessToken)
                .putString(Constants.KEY_REFRESH_TOKEN, refreshToken)
                .putString(Constants.KEY_USER_ID, userId)
                .putLong("expires_at", System.currentTimeMillis() + (expiresIn * 1000L))
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(Constants.KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(Constants.KEY_REFRESH_TOKEN, null);
    }

    public String getUserId() {
        return prefs.getString(Constants.KEY_USER_ID, null);
    }

    public boolean hasValidSession() {
        String token = getAccessToken();
        long expiresAt = prefs.getLong("expires_at", 0);
        boolean hasRefreshToken = getRefreshToken() != null && !getRefreshToken().isEmpty();
        return token != null && !token.isEmpty() && hasRefreshToken && System.currentTimeMillis() < expiresAt;
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
