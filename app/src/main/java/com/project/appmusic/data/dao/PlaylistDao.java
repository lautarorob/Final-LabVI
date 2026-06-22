package com.project.appmusic.data.dao;

import androidx.lifecycle.LiveData;
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


import java.util.List;

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

    @Query("SELECT t.* FROM tracks t " +
            "INNER JOIN playlist_track_cross_ref ref ON t.deezerId = ref.deezerId " +
            "INNER JOIN playlists p ON ref.playlistId = p.playlistId " +
            "WHERE p.userId = :userId AND p.isFavorites = 1 " +
            "AND (t.title LIKE '%' || :searchQuery || '%' OR t.artistName LIKE '%' || :searchQuery || '%')")
    List<TrackEntity> searchInFavoritesPlaylist(int userId, String searchQuery);

    @Query("SELECT * FROM playlists WHERE userId = :currentUserId")
    List<PlaylistEntity> getUserPlaylists(int currentUserId);

    @Transaction
    @Query("SELECT * FROM playlists WHERE userId = :currentUserId")
    List<PlaylistWithTracks> getUserPlaylistsWithTracks(int currentUserId);

    // Consulta relacional filtrada por ID y aproximación de texto
    @Query("SELECT t.* FROM tracks t " +
            "INNER JOIN playlist_track_cross_ref crossRef ON t.deezerId = crossRef.deezerId " +
            "WHERE crossRef.playlistId = :playlistId AND t.title LIKE '%' || :searchQuery || '%'")
    List<TrackEntity> searchInPlaylist(int playlistId, String searchQuery);

    // Eliminar una canción específica de una playlist
    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND deezerId = :songId")
    void removeTrackFromPlaylist(int playlistId, long songId);

    // Eliminar todos los vínculos de una playlist (Limpieza previa a eliminación)
    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    void clearAllTracksFromPlaylist(int playlistId);

    // Eliminar la entidad playlist de la base de datos
    @Query("DELETE FROM playlists WHERE playlistId = :playlistId")
    void deletePlaylist(int playlistId);

    @Query("SELECT * FROM playlists WHERE name LIKE '%' || :query || '%'")
    List<PlaylistEntity> searchPlaylists(String query);


    @Query("SELECT * FROM playlists WHERE name LIKE '%' || :query || '%'")
    List<PlaylistWithTracks> searchPlaylistsWithTracks(String query);


}
