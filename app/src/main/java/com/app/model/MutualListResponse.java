package com.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MutualListResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("users_list")
    private List<ProfileResponse> mutualList;
    @SerializedName("message")
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ProfileResponse> getMutualList() {
        return mutualList;
    }

    public void setMutualList(List<ProfileResponse> mutualList) {
        this.mutualList = mutualList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
