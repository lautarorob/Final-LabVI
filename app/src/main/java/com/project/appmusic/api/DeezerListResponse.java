package com.project.appmusic.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

//clase para recibir la lista de canciones
public class DeezerListResponse<T> {
    @SerializedName("data")
    private List<T> data;

    public List<T> getData() {
        return data;
    }
}
