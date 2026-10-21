package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchPageResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("result")

    @Expose
    private List<Result> result = null;

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

        @SerializedName("type")
        @Expose
        private String type;

        @SerializedName("title")
        @Expose
        private String title;

        @SerializedName("total")
        @Expose
        private String total;

        @SerializedName("banner")
        @Expose
        private List<Banner> banner;

        @SerializedName("users")
        @Expose
        private List<UserList.Result> users;

        @SerializedName("streams")
        @Expose
        private List<VideoListResponse.Result> streams = null;

        @SerializedName("music")
        @Expose
        private List<MusicListResponse.Result> music = null;

        @SerializedName("user_type")
        @Expose
        private String userType = null;
        @SerializedName("video_type")
        @Expose
        private String videoType = null;
        @SerializedName("music_type")
        @Expose
        private String musicType = null;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        public List<Banner> getBanner() {
            return banner;
        }

        public void setBanner(List<Banner> banner) {
            this.banner = banner;
        }

        public List<UserList.Result> getUsers() {
            return users;
        }

        public void setUsers(List<UserList.Result> users) {
            this.users = users;
        }

        public List<VideoListResponse.Result> getStreams() {
            return streams;
        }

        public void setStreams(List<VideoListResponse.Result> streams) {
            this.streams = streams;
        }

        public List<MusicListResponse.Result> getMusic() {
            return music;
        }

        public void setMusic(List<MusicListResponse.Result> music) {
            this.music = music;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getVideoType() {
            return videoType;
        }

        public void setVideoType(String videoType) {
            this.videoType = videoType;
        }

        public String getMusicType() {
            return musicType;
        }

        public void setMusicType(String musicType) {
            this.musicType = musicType;
        }

        public static class Banner {

            @SerializedName("id")
            @Expose
            private String id;
            @SerializedName("name")
            @Expose
            private String name;
            @SerializedName("type")
            @Expose
            private String type;
            @SerializedName("hashtag_name")
            @Expose
            private String hashtagName;
            @SerializedName("user_id")
            @Expose
            private String userId;
            @SerializedName("user_name")
            @Expose
            private String userName;
            @SerializedName("banner_image")
            @Expose
            private String bannerImage;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getHashtagName() {
                return hashtagName;
            }

            public void setHashtagName(String hashtagName) {
                this.hashtagName = hashtagName;
            }

            public String getUserId() {
                return userId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }

            public String getUserName() {
                return userName;
            }

            public void setUserName(String userName) {
                this.userName = userName;
            }

            public String getBannerImage() {
                return bannerImage;
            }

            public void setBannerImage(String bannerImage) {
                this.bannerImage = bannerImage;
            }
        }
    }
}
