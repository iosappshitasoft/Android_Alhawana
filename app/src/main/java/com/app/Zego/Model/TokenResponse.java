package com.app.Zego.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TokenResponse {


    @SerializedName("stream_token")
    private String stream_token;

    public String getStream_token() {
        return stream_token;
    }

    public void setStream_token(String stream_token) {
        this.stream_token = stream_token;
    }

}
