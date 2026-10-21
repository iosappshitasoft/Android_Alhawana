package com.app.model;

import java.util.List;

public class RecentUser {

    private String status;
    private String message;
    private List<UserData> data;

    public String getStatus() { return status; }
    public String getMessage() { return message; }
    public List<UserData> getData() { return data; }

    public static class UserData {
        private String user_id;
        private String name;
        private String profile_image;

        public String getUserId() { return user_id; }
        public String getName() { return name; }
        public String getProfileImage() { return profile_image; }
    }
}


