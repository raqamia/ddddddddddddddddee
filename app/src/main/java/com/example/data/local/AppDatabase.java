package com.example.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.example.data.local.dao.DownloadDao;
import com.example.data.local.dao.FileDao;
import com.example.data.local.dao.RecentDao;
import com.example.data.local.dao.SavedDao;
import com.example.data.local.dao.SubjectDao;
import com.example.data.local.entity.DownloadEntity;
import com.example.data.local.entity.FileEntity;
import com.example.data.local.entity.RecentEntity;
import com.example.data.local.entity.SavedFileEntity;
import com.example.data.local.entity.SubjectEntity;
import net.sqlcipher.database.SupportFactory;
import java.security.SecureRandom;

@Database(entities = {SubjectEntity.class, FileEntity.class, DownloadEntity.class, SavedFileEntity.class, RecentEntity.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    private static final String TAG = "AppDatabase";
    private static final String DB_PREFS = "manara_db_encryption";

    public abstract SubjectDao subjectDao();
    public abstract FileDao fileDao();
    public abstract DownloadDao downloadDao();
    public abstract SavedDao savedDao();
    public abstract RecentDao recentDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    byte[] passphrase = getOrCreatePassphrase(context.getApplicationContext());
                    SupportFactory factory = new SupportFactory(passphrase);
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "manara_database")
                            .openHelperFactory(factory)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static byte[] getOrCreatePassphrase(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            SharedPreferences prefs = EncryptedSharedPreferences.create(
                    context, DB_PREFS, masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            String encoded = prefs.getString("db_passphrase", null);
            if (encoded == null) {
                byte[] raw = new byte[32];
                new SecureRandom().nextBytes(raw);
                encoded = Base64.encodeToString(raw, Base64.NO_PADDING);
                prefs.edit().putString("db_passphrase", encoded).apply();
                Log.i(TAG, "Database encryption key generated and stored securely");
            }
            return Base64.decode(encoded, Base64.NO_PADDING);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize database encryption", e);
            throw new RuntimeException("Cannot initialize database encryption", e);
        }
    }
}
