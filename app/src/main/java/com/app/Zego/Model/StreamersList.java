package com.app.Zego.Model;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class StreamersList {

   /* public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public StreamersList(String user_name, String user_id, String user_image) {
        this.user_name = user_name;
        this.user_id = user_id;
        this.user_image = user_image;
    }

    String user_name,user_id,user_image;*/

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("result")
    @Expose
    private List<Result> result;

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

        @SerializedName("streamname")
        @Expose
        private String streamname;
        @SerializedName("streamid")
        @Expose
        private String streamid;
        @SerializedName("host_id")
        @Expose
        private String hostId;
        @SerializedName("host_Image")
        @Expose
        private String hostImage;
        @SerializedName("host_name")
        @Expose
        private String hostName;

        public Result() {
        }

        public Result(String streamname, String streamid, String hostId, String hostImage, String hostName, String txtStatus) {
            this.streamname = streamname;
            this.streamid = streamid;
            this.hostId = hostId;
            this.hostImage = hostImage;
            this.hostName = hostName;
            this.txtStatus = txtStatus;
        }

        public String getTxtStatus() {
            return txtStatus;
        }

        public void setTxtStatus(String txtStatus) {
            this.txtStatus = txtStatus;
        }

        private String txtStatus;



        public String getStreamname() {
            return streamname;
        }

        public void setStreamname(String streamname) {
            this.streamname = streamname;
        }

        public String getStreamid() {
            return streamid;
        }

        public void setStreamid(String streamid) {
            this.streamid = streamid;
        }

        public String getHostId() {
            return hostId;
        }

        public void setHostId(String hostId) {
            this.hostId = hostId;
        }

        public String getHostImage() {
            return hostImage;
        }

        public void setHostImage(String hostImage) {
            this.hostImage = hostImage;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }
    }
}