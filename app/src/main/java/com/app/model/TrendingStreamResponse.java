
package com.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TrendingStreamResponse {

    @SerializedName("status")
    private String status;
    @SerializedName("result")
    private List<Result> result;
    @SerializedName("message")
    private String message;

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

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

    public static class Result {

        @SerializedName("_id")
        private String _id;
        @SerializedName("name")
        private String name;
        @SerializedName("created_at")
        private String createdAt;
        @SerializedName("points")
        private String points;
        @SerializedName("streams")
        private List<StreamDetails> streams;
        @SerializedName("total")
        private String total;
        @SerializedName("updated_at")
        private String updatedAt;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public List<StreamDetails> getStreams() {
            return streams;
        }

        public void setStreams(List<StreamDetails> streams) {
            this.streams = streams;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getPoints() {
            return points;
        }

        public void setPoints(String points) {
            this.points = points;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

}
