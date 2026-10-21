package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchHashTagRes {

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

    public static class Result {

        @SerializedName("hashtag_name")
        @Expose
        private String hashtagName;
        @SerializedName("used_count")
        @Expose
        private String usedCount;
        @SerializedName("isFavorite")
        @Expose
        private Boolean isFavorite;

        public Boolean getTrend() {
            return isTrend;
        }

        public void setTrend(Boolean trend) {
            isTrend = trend;
        }

        @SerializedName("isTrend")
        @Expose
        private Boolean isTrend;

        public String getHashtagName() {
            return hashtagName;
        }

        public void setHashtagName(String hashtagName) {
            this.hashtagName = hashtagName;
        }

        public String getUsedCount() {
            return usedCount;
        }

        public void setUsedCount(String usedCount) {
            this.usedCount = usedCount;
        }

        public Boolean getIsFavorite() {
            return isFavorite;
        }

        public void setIsFavorite(Boolean isFavorite) {
            this.isFavorite = isFavorite;
        }

    }
}
