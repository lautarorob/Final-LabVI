package com.project.appmusic.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.project.appmusic.data.entity.PlaylistEntity;
import com.project.appmusic.data.entity.PlaylistTrackCrossRef;
import com.project.appmusic.data.entity.PlaylistWithTracks;
import com.project.appmusic.data.entity.TrackEntity;

@Dao
public interface PlaylistDao {
    // Crear nueva playlist
    @Insert
    long insertPlaylist(PlaylistEntity playlist);

    // Guardar la metadata de la canción en la base de datos
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTrack(TrackEntity track);

    // Vincular la canción a la playlist
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTrackIntoPlaylist(PlaylistTrackCrossRef crossRef);

    // Obtener una playlist con todas sus canciones
    @Transaction
    @Query("SELECT * FROM playlists WHERE playlistId = :id")
    PlaylistWithTracks getPlaylistWithTracks(int id);

    // Obtener la playlist especial "Me Gusta" del usuario logueado
    @Transaction
    @Query("SELECT * FROM playlists WHERE userId = :userId AND isFavorites = 1 LIMIT 1")
    PlaylistWithTracks getFavoritesPlaylist(int userId);

    // Elimina el vínculo entre la playlist y la canción
    @Delete
    void deleteTrackFromPlaylist(PlaylistTrackCrossRef crossRef);

    // Devuelve 1 si la canción ya está en la playlist, o 0 si no está
    @Query("SELECT COUNT(*) FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND deezerId = :deezerId")
    int isTrackInPlaylist(int playlistId, long deezerId);
}
