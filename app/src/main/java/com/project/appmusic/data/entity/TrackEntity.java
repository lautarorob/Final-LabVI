package com.project.appmusic.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.project.appmusic.data.converters.StringListConverters;

import java.util.List;

//metadata de la cancion
@Entity(tableName = "tracks")
public class TrackEntity {
    @PrimaryKey
    public long deezerId; // ID oficial de Deezer
    public String title;
    public String artistName;
    public String coverUrl;

    @TypeConverters(StringListConverters.class)
    public List<String> genres;
}