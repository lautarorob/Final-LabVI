package com.project.appmusic.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
//metadata de la cancion
@Entity(tableName = "tracks")
public class TrackEntity {
    @PrimaryKey
    public long deezerId; // ID oficial de Deezer
    public String title;
    public String artistName;
    public String coverUrl;

    public String gender;
}