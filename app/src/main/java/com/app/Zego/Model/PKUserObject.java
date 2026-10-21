package com.app.Zego.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PKUserObject {

    @SerializedName("opp_host_id")
    @Expose
    private String hostID;

    @SerializedName("opp_host_streamId")
    @Expose
    private String hostStreamId;
    @SerializedName("opp_host_name")
    @Expose
    private String hostName;


    public PKUserObject(String hostID, String hostStreamId,String hostName) {
        this.hostID = hostID;
        this.hostName = hostName;
        this.hostStreamId = hostStreamId;
    }
}
