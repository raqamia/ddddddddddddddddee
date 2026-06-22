package com.example.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private SharedPreferences prefs;

    public AppPreferences(Context context) {
        prefs = context.getSharedPreferences("manara_app_prefs", Context.MODE_PRIVATE);
    }

    public boolean isOnboardingDone() {
        return prefs.getBoolean("onboarding_done", false);
    }

    public void setOnboardingDone(boolean done) {
        prefs.edit().putBoolean("onboarding_done", done).apply();
    }

    public String getUserTrack() {
        return prefs.getString("user_track", null);
    }

    public void setUserTrack(String track) {
        prefs.edit().putString("user_track", track).apply();
    }

    public long getLastSeenNotif() {
        return prefs.getLong("last_seen_notif", 0);
    }

    public void setLastSeenNotif(long timeMillis) {
        prefs.edit().putLong("last_seen_notif", timeMillis).apply();
    }

    public float getFontScale() {
        return prefs.getFloat("font_scale", 1.0f);
    }

    public void setFontScale(float scale) {
        prefs.edit().putFloat("font_scale", scale).apply();
    }

    public boolean isNightMode() {
        return prefs.getBoolean("night_mode", false);
    }

    public void setNightMode(boolean enabled) {
        prefs.edit().putBoolean("night_mode", enabled).apply();
    }
}
