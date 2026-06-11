package com.project.appmusic.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
//playlist
@Entity(tableName = "playlists")
public class PlaylistEntity {
    @PrimaryKey(autoGenerate = true)
    public int playlistId;
    public String name;
    public int userId;
    public boolean isFavorites;

}
