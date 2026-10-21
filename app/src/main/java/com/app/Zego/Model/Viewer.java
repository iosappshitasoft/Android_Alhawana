package com.app.Zego.Model;

public class Viewer {

    String user_name;
    String user_image;
    String user_id;
    String status;

    public Viewer(String user_name, String user_image, String user_id, String status) {
        this.user_name = user_name;
        this.user_image = user_image;
        this.user_id = user_id;
        this.status = status;
    }


    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
