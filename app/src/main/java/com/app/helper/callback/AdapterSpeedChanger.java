package com.app.helper.callback;

public interface AdapterSpeedChanger {
    void onItemPlaybackSpeed(Object o,int position,String videoId,float speed,String type);

    void onItemPlaybackSpeed(Object o,int position,String videoId,String speed,String type);

    void onItemReposted(Object o,int position,String videoId,String action, String desc);
}
