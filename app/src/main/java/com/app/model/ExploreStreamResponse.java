package com.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ExploreStreamResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;
    @SerializedName("result")
    private Result result;

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

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public static class Result {
        @SerializedName("total")
        private String total;
        @SerializedName("streams")
        private List<StreamDetails> streams;

        public List<StreamDetails> getStreams() {
            return streams;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public void setStreams(List<StreamDetails> result) {
            this.streams = result;
        }
    }
}
