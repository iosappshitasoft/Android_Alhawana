package com.app.utils.api;

public interface ReportApiCallback {

    public void onResponse(Object apiResponse, int adapterPosition);

    void onFailure(Throwable t, int adapterPosition);

}
