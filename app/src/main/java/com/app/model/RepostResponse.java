package com.app.model;

import java.util.List;

public class RepostResponse {

    private String status;
    private String message;
    private String repost_count;
    private List<RepostData> repost_data;

    public String getRepost_count() {
        return repost_count;
    }

    public void setRepost_count(String repost_count) {
        this.repost_count = repost_count;
    }

    public String getStatus() { return status; }
    public String getMessage() { return message; }

    // existing method (your JSON maps to this)
    public List<RepostData> getRepost_data() { return repost_data; }

    // convenience camelCase getter — use this in your UI code to avoid the underscore name
    public List<RepostData> getRepostData() { return repost_data; }

    public static class RepostData {
        private String repost_id;
        private String user_id;
        private String user_image;
        private String display_name;
        private String description;

        public String getRepost_id() { return repost_id; }
        public String getUser_id() { return user_id; }
        public String getUser_image() { return user_image; }
        public String getDisplay_name() { return display_name; }
        public String getDescription() { return description; }

        // convenience camelCase getters (optional)
        public String getRepostId() { return repost_id; }
        public String getUserId() { return user_id; }
        public String getUserImage() { return user_image; }
        public String getDisplayName() { return display_name; }
    }
}

