package com.project.appmusic.data.entity;

import androidx.room.Entity;
//vinculacion entre playlist y cancion
@Entity(tableName = "playlist_track_cross_ref",
        primaryKeys = {"playlistId", "deezerId"})
public class PlaylistTrackCrossRef {
    public int playlistId;
    public long deezerId;
}
/*
Room necesita esta tabla oculta para vincular
qué canciones están en qué playlist sin duplicar datos.
 */