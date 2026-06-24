package com.project.appmusic.objetos;

public class Playlist {
    private long id;
    private String name;

    private String coverUrl;

    public long getId() {
        return id;
    }

    public void setId(int playlistId) {
        this.id = playlistId;
    }

    public String getName() {
        return name;
    }

    public void setName(String playlistName) {
        this.name = playlistName;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }
}
