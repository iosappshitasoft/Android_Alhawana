package com.app.Zego.internal.sdk.zim;

public interface RoomCommandCallback {

    void onSendRoomCommand(int errorCode, String errorMessage, String command);
}
