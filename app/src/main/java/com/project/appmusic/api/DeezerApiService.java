package com.project.appmusic.api;

import com.project.appmusic.Song;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

//interfaz de rutas
public interface DeezerApiService {
    @GET("search")
    Call<DeezerListResponse<Song>> searchSongs(@Query("q") String nombreCancion);
}
