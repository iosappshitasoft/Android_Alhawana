package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchSoundRes {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("result")
    @Expose
    private List<MusicListResponse.Result> result = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<MusicListResponse.Result> getResult() {
        return result;
    }

    public void setResult(List<MusicListResponse.Result> result) {
        this.result = result;
    }

}
