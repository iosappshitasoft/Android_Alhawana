package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SingleVideoResponse extends BaseResponse {


    @SerializedName("result")
    @Expose
    private ArrayList<HomeResponse.Result> result;

    public ArrayList<HomeResponse.Result> getResult() {
        return result;
    }

    public void setResult(ArrayList<HomeResponse.Result> result) {
        this.result = result;
    }
}