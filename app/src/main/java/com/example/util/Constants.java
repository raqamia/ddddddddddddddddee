package com.example.util;

public class Constants {
    // Configured via BuildConfig
    public static final String SUPABASE_URL = com.example.BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_ANON_KEY = com.example.BuildConfig.SUPABASE_ANON_KEY;
    
    public static final String SUPABASE_REST_URL = SUPABASE_URL + "/rest/v1/";
    public static final String SUPABASE_AUTH_URL = SUPABASE_URL + "/auth/v1/";
    public static final String SUPABASE_STORAGE_URL = SUPABASE_URL + "/storage/v1/";

    public static final String PDF_BUCKET = "pdfs";
    public static final String PREFS_NAME = "manara_secure_prefs";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";
    public static final String KEY_USER_ID = "user_id";
}
