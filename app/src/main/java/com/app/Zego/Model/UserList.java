package com.app.Zego.Model;


import com.app.addon.livestream.external.model.StreamDetails;

import java.util.ArrayList;

public class UserList {
    String user_name;
    String watch_count;
    String user_id;
    public ArrayList<StreamDetails.LiveViewers> liveViewers;

    public UserList(String user_name, String watch_count, String user_id, ArrayList<StreamDetails.LiveViewers> liveViewers) {
        this.user_name = user_name;
        this.watch_count = watch_count;
        this.user_id = user_id;
        this.liveViewers = liveViewers;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getWatch_count() {
        return watch_count;
    }

    public void setWatch_count(String watch_count) {
        this.watch_count = watch_count;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public ArrayList<StreamDetails.LiveViewers> getLiveViewers() {
        return liveViewers;
    }

    public void setLiveViewers(ArrayList<StreamDetails.LiveViewers> liveViewers) {
        this.liveViewers = liveViewers;
    }
}
