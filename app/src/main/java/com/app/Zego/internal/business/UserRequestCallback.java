package com.app.Zego.internal.business;

public interface UserRequestCallback {

    void onUserRequestSend(int errorCode, String requestID);
}
