package com.project.appmusic.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DeezerAlbum {

    @SerializedName("id")
    public long id;

    @SerializedName("genres")
    public GenresContainer genres;

    // Clase interna para el nodo "genres" del JSON
    public static class GenresContainer {
        @SerializedName("data")
        public List<GenreData> data;
    }

    // Clase interna para los objetos dentro de la lista "data"
    public static class GenreData {
        @SerializedName("name")
        public String name;
    }
}