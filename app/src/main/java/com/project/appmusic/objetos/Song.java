package com.project.appmusic.objetos;

import com.google.gson.annotations.SerializedName;

import java.util.List;

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

    private List<String> genres;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getUrlAudio() {
        return urlAudio;
    }

    public void setUrlAudio(String urlAudio) {
        this.urlAudio = urlAudio;
    }

    public String getNameArtist() {
        return artist != null ? artist.name : "Artista desconocido";
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public String getUrlPortada() {
        return album != null ? album.coverUrl : null;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public Album getAlbumData() {
        return album;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public static class Artist {
        @SerializedName("name")
        public String name;
    }

    public static class Album {
        @SerializedName("id")
        public long id;

        @SerializedName("cover_medium")
        public String coverUrl;
    }
}