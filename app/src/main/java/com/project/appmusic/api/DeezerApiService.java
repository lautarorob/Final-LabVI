package com.project.appmusic.api;

import com.project.appmusic.Playlist;
import com.project.appmusic.Song;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

//interfaz de rutas
public interface DeezerApiService {
    @GET("search")
    Call<DeezerListResponse<Song>> searchSongs(@Query("q") String nombreCancion);

    @GET("chart/0/tracks?limit=100")
    Call<DeezerListResponse<Song>> getTopGlobalTracks();

    @GET("playlist/{id}/tracks?limit=100")
    Call<DeezerListResponse<Song>> getTopRegionalTracks(@Path("id") long playlistId);

    @GET("search/playlist")
    Call<DeezerListResponse<Playlist>> buscarPlaylistRegional(@Query("q") String terminoBusqueda);

    @GET("track/{id}")
    Call<Song> getTrackById(@Path("id") long trackId);
}
