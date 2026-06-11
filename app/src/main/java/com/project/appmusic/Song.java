package com.project.appmusic;

import com.google.gson.annotations.SerializedName;

public class Song {

    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String titulo;

    @SerializedName("preview")
    private String urlAudio;

    @SerializedName("artist")
    private Artist artist;

    @SerializedName("album")
    private Album album;

    public long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getUrlAudio() {
        return urlAudio;
    }

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

    public void setId(long id) {
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setUrlAudio(String urlAudio) {
        this.urlAudio = urlAudio;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }
}

