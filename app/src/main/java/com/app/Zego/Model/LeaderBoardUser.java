package com.app.Zego.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LeaderBoardUser {

    @SerializedName("status")
    String status;

    @SerializedName("result")
    List<Result> result;
    @SerializedName("remaining_next_rounded_time")
    String  remaining_next_rounded_time;

    public String getRemaining_next_rounded_time() {
        return remaining_next_rounded_time;
    }

    public void setRemaining_next_rounded_time(String remaining_next_rounded_time) {
        this.remaining_next_rounded_time = remaining_next_rounded_time;
    }

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

        @SerializedName("stream")
        Stream stream;

        @SerializedName("user_id")
        String user_id;

        @SerializedName("user_name")
        public String user_name;

        @SerializedName("user_image")
        String user_image;

        @SerializedName("user_type")
        String user_type;

        @SerializedName("user_rank")
        String user_rank;

        @SerializedName("earned_point")
        String earned_point;

        @SerializedName("contribution")
        String contribution;

        @SerializedName("verified_user")
        String verified_user;
        @SerializedName("total_contribution")
        Integer total_contribution;

        String isLive;

        @SerializedName("level")
        Integer level;

        @SerializedName("contibutors")
        List<Result> contibutorsList;

        public String getContribution() {
            return contribution;
        }

        public Integer getTotal_contribution() {
            return total_contribution;
        }

        public void setTotal_contribution(Integer total_contribution) {
            this.total_contribution = total_contribution;
        }

        public void setContribution(String contribution) {
            this.contribution = contribution;
        }

        public List<Result> getContibutorsList() {
            return contibutorsList;
        }

        public void setContibutorsList(List<Result> contibutorsList) {
            this.contibutorsList = contibutorsList;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public String getVerified_user() {
            return verified_user;
        }

        public void setVerified_user(String verified_user) {
            this.verified_user = verified_user;
        }

        public String getUser_type() {
            return user_type;
        }

        public void setUser_type(String user_type) {
            this.user_type = user_type;
        }

        public String getIsLive() {
            return isLive;
        }

        public void setIsLive(String isLive) {
            this.isLive = isLive;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
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

        public String getUser_rank() {
            return user_rank;
        }

        public void setUser_rank(String user_rank) {
            this.user_rank = user_rank;
        }

        public String getEarned_point() {
            return earned_point;
        }

        public Stream getStream() {
            return stream;
        }

        public void setStream(Stream stream) {
            this.stream = stream;
        }

        public void setEarned_point(String earned_point) {
            this.earned_point = earned_point;
        }
    }

    public class Stream {
        @SerializedName("name")
        public String name;
        @SerializedName("mode")
        public String mode;
        @SerializedName("watch_count")
        public String watch_count;
        @SerializedName("streamed_on")
        public String streamed_on;
        @SerializedName("stream_id")
        public String stream_id;
        @SerializedName("publisher_id")
        public String publisher_id;
        @SerializedName("publisher_image")
        public String publisher_image;
        @SerializedName("stream_thumbnail")
        public String stream_thumbnail;
        @SerializedName("type")
        public String type;
        @SerializedName("likes")
        public String likes;
        @SerializedName("playback_duration")
        public String playback_duration;
        @SerializedName("playback_ready")
        public String playback_ready;
        @SerializedName("playback_url")
        public String playback_url;
        @SerializedName("stream_type")
        public String stream_type;
        @SerializedName("battle_ends_in")
        public String battle_ends_in;

        @SerializedName("posted_by")
        public String publisher_name;

        public String getPublisher_name() {
            return publisher_name;
        }

        public void setPublisher_name(String publisher_name) {
            this.publisher_name = publisher_name;
        }

        public Stream() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getWatch_count() {
            return watch_count;
        }

        public void setWatch_count(String watch_count) {
            this.watch_count = watch_count;
        }

        public String getStreamed_on() {
            return streamed_on;
        }

        public void setStreamed_on(String streamed_on) {
            this.streamed_on = streamed_on;
        }

        public String getStream_id() {
            return stream_id;
        }

        public void setStream_id(String stream_id) {
            this.stream_id = stream_id;
        }

        public String getPublisher_id() {
            return publisher_id;
        }

        public void setPublisher_id(String publisher_id) {
            this.publisher_id = publisher_id;
        }

        public String getPublisher_image() {
            return publisher_image;
        }

        public void setPublisher_image(String publisher_image) {
            this.publisher_image = publisher_image;
        }

        public String getStream_thumbnail() {
            return stream_thumbnail;
        }

        public void setStream_thumbnail(String stream_thumbnail) {
            this.stream_thumbnail = stream_thumbnail;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLikes() {
            return likes;
        }

        public void setLikes(String likes) {
            this.likes = likes;
        }

        public String getPlayback_duration() {
            return playback_duration;
        }

        public void setPlayback_duration(String playback_duration) {
            this.playback_duration = playback_duration;
        }

        public String getPlayback_ready() {
            return playback_ready;
        }

        public void setPlayback_ready(String playback_ready) {
            this.playback_ready = playback_ready;
        }

        public String getPlayback_url() {
            return playback_url;
        }

        public void setPlayback_url(String playback_url) {
            this.playback_url = playback_url;
        }

        public String getStream_type() {
            return stream_type;
        }

        public void setStream_type(String stream_type) {
            this.stream_type = stream_type;
        }

        public String getBattle_ends_in() {
            return battle_ends_in;
        }

        public void setBattle_ends_in(String battle_ends_in) {
            this.battle_ends_in = battle_ends_in;
        }
    }


}
