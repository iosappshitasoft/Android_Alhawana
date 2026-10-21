package com.app.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class LiveStreamResponse implements Serializable {

    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;

    @SerializedName("result")
    public ArrayList<StreamDetails> result = new ArrayList<>();

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<StreamDetails> getResult() {
        return result;
    }

    public void setResult(ArrayList<StreamDetails> result) {
        this.result = result;
    }

}
