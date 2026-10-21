package com.app.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
//import com.hendraanggrian.appcompat.widget.Hashtagable;

import java.util.List;

public class PostHashTagRes {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("result")
    @Expose
    private List<Result> result = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public static class Result /*implements Hashtagable*/ {

        @SerializedName("name")
        @Expose
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

//        @NonNull
//        @Override
//        public CharSequence getId() {
//            return name;
//        }
//
//        @Override
//        public int getCount() {
//            return 0;
//        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }
}
