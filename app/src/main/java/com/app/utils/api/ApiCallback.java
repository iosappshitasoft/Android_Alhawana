package com.app.utils.api;

public interface ApiCallback {

    public void onResponse(Object apiResponse);

    void onFailure(Throwable t);

}
