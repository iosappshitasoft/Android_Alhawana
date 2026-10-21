package com.app.Zego.CallBacks;

import android.view.View;

import com.app.Zego.Model.LeaderBoardUser;

import java.util.Map;

public interface onCoHostClicked {

    void onUserTapped(float x, float y);

    void onCoHostClick(String userId, String type, String userName, View v);

    void onFirstItemSizeCalculated(int width,int height,int viewtype);

    void onZoneTimeClicked(Map<String,String> str);

    void onLeaderBoardClick(LeaderBoardUser.Stream stream, String type, String userId);

    void onSendRequest(String from, String name, String userId, String position);
}
