package com.example;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.data.prefs.AppPreferences;
import com.example.data.prefs.SessionManager;

public class ManaraApplication extends Application {
    private SessionManager sessionManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager = new SessionManager(this);
        // Apply the user's saved dark-mode preference before any activity is shown.
        AppPreferences prefs = new AppPreferences(this);
        AppCompatDelegate.setDefaultNightMode(prefs.isNightMode()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
