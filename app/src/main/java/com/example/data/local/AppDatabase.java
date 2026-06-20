package com.example.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.data.local.dao.DownloadDao;
import com.example.data.local.dao.FileDao;
import com.example.data.local.dao.SubjectDao;
import com.example.data.local.entity.DownloadEntity;
import com.example.data.local.entity.FileEntity;
import com.example.data.local.entity.SubjectEntity;

@Database(entities = {SubjectEntity.class, FileEntity.class, DownloadEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract SubjectDao subjectDao();
    public abstract FileDao fileDao();
    public abstract DownloadDao downloadDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "manara_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
