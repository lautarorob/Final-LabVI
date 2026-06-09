package com.project.appmusic.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.project.appmusic.data.entity.UserEntity;
import com.project.appmusic.data.dao.UserDao;

@Database(entities = {UserEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    // Patrón Singleton para evitar fugas de memoria y múltiples conexiones abiertas
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "appmusic_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}