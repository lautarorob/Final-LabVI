package com.project.appmusic;

import com.google.gson.annotations.SerializedName;

//modelo de datos
public class Song {
    @SerializedName("title")
    private String titulo;

    @SerializedName("preview")
    private String urlAudio;

    @SerializedName("name")
    private String nameArtist;

    @SerializedName("album")
    private Album album;

    // Getters
    public String getTitulo() { return titulo; }

    public String getNameArtist() { return nameArtist; }
    public String getUrlAudio() { return urlAudio; }

    public String getUrlPortada() {
        return album != null ? album.coverUrl : null;
    }

    public static class Album {
        @SerializedName("cover_medium")
        public String coverUrl;
    }

}
