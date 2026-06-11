package com.project.appmusic.data.entity;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

//modelado de relacion entre playlist y cancion
public class PlaylistWithTracks {
    @Embedded
    public PlaylistEntity playlist;

    @Relation(
            parentColumn = "playlistId",
            entityColumn = "deezerId",
            associateBy = @Junction(PlaylistTrackCrossRef.class)
    )
    public List<TrackEntity> tracks;
}
/*
Para extraer una playlist completa con todas sus canciones
 desde la base de datos en una sola consulta,
 Room requiere una clase que modele la relación.
* */