package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FavSoundResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("sounds")
    @Expose
    private List<MusicListResponse.Result> sounds = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<MusicListResponse.Result> getSounds() {
        return sounds;
    }

    public void setSounds(List<MusicListResponse.Result> sounds) {
        this.sounds = sounds;
    }
}
