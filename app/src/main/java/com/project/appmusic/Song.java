package com.project.appmusic;

import com.google.gson.annotations.SerializedName;

public class Song {
    @SerializedName("title")
    private String titulo;

    @SerializedName("preview")
    private String urlAudio;

    @SerializedName("artist")
    private Artist artist;

    @SerializedName("album")
    private Album album;

    public String getTitulo() { return titulo; }
    public String getUrlAudio() { return urlAudio; }

    public String getNameArtist() {
        return artist != null ? artist.name : "Artista desconocido";
    }

    public String getUrlPortada() {
        return album != null ? album.coverUrl : null;
    }

    public static class Artist {
        @SerializedName("name")
        public String name;
    }

    public static class Album {
        @SerializedName("cover_medium")
        public String coverUrl;
    }
}