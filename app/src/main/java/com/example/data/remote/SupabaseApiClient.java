package com.example.data.remote;

import com.example.BuildConfig;
import com.example.data.prefs.SessionManager;
import com.example.util.Constants;
import java.util.concurrent.TimeUnit;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseApiClient {
    private static Retrofit retrofit = null;
    private static OkHttpClient httpClient = null;

    public static synchronized SupabaseApiService getApi(SessionManager sessionManager) {
        if (httpClient == null) {
            httpClient = createHttpClient(sessionManager);
        }
        if (retrofit == null) {
            String baseUrl = Constants.SUPABASE_URL;
            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(SupabaseApiService.class);
    }

    public static synchronized void resetClient() {
        retrofit = null;
        httpClient = null;
    }

    private static OkHttpClient createHttpClient(SessionManager sessionManager) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // Never print auth secrets to Logcat, even in debug header logging.
        logging.redactHeader("Authorization");
        logging.redactHeader("apikey");
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.HEADERS
                : HttpLoggingInterceptor.Level.NONE);

        int timeout = 30;

        CertificatePinner pinner = buildCertificatePinner();

        return new OkHttpClient.Builder()
                .certificatePinner(pinner)
                .addInterceptor(new AuthInterceptor(sessionManager))
                .addInterceptor(logging)
                .connectTimeout(timeout, TimeUnit.SECONDS)
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .build();
        }

    public static CertificatePinner buildCertificatePinner() {
        CertificatePinner.Builder builder = new CertificatePinner.Builder();

        // شهادة الموقع (leaf certificate) - مستخرجة عبر openssl s_client بتاريخ 2026-07-04
        builder.add(Constants.SUPABASE_CERT_HOST,
                    "sha256/ZcJbApTb7wyllleAjHw2vYAskqdT+DhMY9aPDFwAtf4=");
        // pin احتياطي: الشهادة الوسيطة (intermediate CA) - يحمي التطبيق عند تجديد شهادة الموقع
        builder.add(Constants.SUPABASE_CERT_HOST,
                    "sha256/kIdp6NNEd8wsugYyyIYFsi1ylMCED3hZbSR8ZFsa/A4=");

        return builder.build();
    }
}
