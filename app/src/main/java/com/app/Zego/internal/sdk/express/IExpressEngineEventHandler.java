package com.app.Zego.internal.sdk.express;

import com.app.Zego.internal.sdk.basic.ZEGOSDKUser;

import java.util.ArrayList;
import java.util.List;

import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;

public abstract class IExpressEngineEventHandler extends IZegoEventHandler {

    public void onRoomExtraInfoUpdate2(String roomID, ArrayList<ZegoRoomExtraInfo> roomExtraInfoList) {
    }

    public void onCameraOpen(String userID, boolean open) {
    }

    public void onMicrophoneOpen(String userID, boolean open) {
    }

    public void onReceiveStreamAdd(List<ZEGOSDKUser> userList) {
    }

    public void onReceiveStreamRemove(List<ZEGOSDKUser> userList) {
    }

    public void onUserEnter(List<ZEGOSDKUser> userList) {
    }

    public void onUserLeft(List<ZEGOSDKUser> userList) {
    }

    public void onIMSendBarrageMessageResult(int errorCode, String message, String messageID) {
    }
}
