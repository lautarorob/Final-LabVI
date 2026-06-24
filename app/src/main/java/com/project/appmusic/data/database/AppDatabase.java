package com.project.appmusic.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


import com.project.appmusic.data.dao.UserDao;
import com.project.appmusic.data.dao.PlaylistDao;


import com.project.appmusic.data.entity.UserEntity;
import com.project.appmusic.data.entity.PlaylistEntity;
import com.project.appmusic.data.entity.TrackEntity;
import com.project.appmusic.data.entity.PlaylistTrackCrossRef;


@Database(
        entities = {
                UserEntity.class,
                PlaylistEntity.class,
                TrackEntity.class,
                PlaylistTrackCrossRef.class
        },
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();

    public abstract PlaylistDao playlistDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "appmusic_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}