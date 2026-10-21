package com.app.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VideoCommentResponse {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("commentcount")
    @Expose
    private Integer commentcount;
    @SerializedName("is_user_can_comment")
    @Expose
    private Boolean isUserCanComment;
    @SerializedName("result")
    @Expose
    private List<Result> result = null;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCommentcount() {
        return commentcount;
    }

    public void setCommentcount(Integer commentcount) {
        this.commentcount = commentcount;
    }

    public Boolean getIsUserCanComment() {
        return isUserCanComment;
    }

    public void setIsUserCanComment(Boolean isUserCanComment) {
        this.isUserCanComment = isUserCanComment;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public static class Result {

        @SerializedName("comment")
        @Expose
        private String comment;
        @SerializedName("comment_id")
        @Expose
        private String commentId;
        @SerializedName("tagged_user")
        @Expose
        private List<String> taggedUser = null;
        @SerializedName("user_id")
        @Expose
        private String userId;
        @SerializedName("user_image")
        @Expose
        private String userImage;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("age")
        @Expose
        private Integer age;
        @SerializedName("gender")
        @Expose
        private String gender;
        @SerializedName("comment_posted_time")
        @Expose
        private String commentPostedTime;

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getCommentId() {
            return commentId;
        }

        public void setCommentId(String commentId) {
            this.commentId = commentId;
        }

        public List<String> getTaggedUser() {
            return taggedUser;
        }

        public void setTaggedUser(List<String> taggedUser) {
            this.taggedUser = taggedUser;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getCommentPostedTime() {
            return commentPostedTime;
        }

        public void setCommentPostedTime(String commentPostedTime) {
            this.commentPostedTime = commentPostedTime;
        }

    }

}
