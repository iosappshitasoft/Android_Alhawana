package com.app.model;


import java.io.Serializable;

public class VideoPrivacyModel implements Serializable{

    String privacy;
    Boolean state;

    public VideoPrivacyModel(String privacy, Boolean state) {
        this.privacy = privacy;
        this.state = state;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }


}
