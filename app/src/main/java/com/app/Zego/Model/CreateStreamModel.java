package com.app.Zego.Model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CreateStreamModel {
    @SerializedName("share_stream_link")
    @Expose
    private String shareStreamLink;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("video_id")
    @Expose
    private String video_id;
    @SerializedName("stream_name")
    @Expose
    private String streamName;
    @SerializedName("invite_link")
    @Expose
    private String inviteLink;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("seat_type")
    @Expose
    private String seatType;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStream_token() {
        return stream_token;
    }

    public void setStream_token(String stream_token) {
        this.stream_token = stream_token;
    }

    @SerializedName("stream_token")
    @Expose
    private String stream_token;

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getInviteLink() {
        return inviteLink;
    }

    public void setInviteLink(String inviteLink) {
        this.inviteLink = inviteLink;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }

    public String getShareStreamLink() {
        return shareStreamLink;
    }

    public void setShareStreamLink(String shareStreamLink) {
        this.shareStreamLink = shareStreamLink;
    }

}