package com.example;

import android.app.Application;
import com.example.data.prefs.SessionManager;

public class ManaraApplication extends Application {
    private SessionManager sessionManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager = new SessionManager(this);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
