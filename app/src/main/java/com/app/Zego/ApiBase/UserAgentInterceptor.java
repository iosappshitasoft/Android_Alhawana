package com.app.Zego.ApiBase;

import static com.app.alhawanajd.App.getXDeviceInfo;

import android.util.Log;


import com.app.model.GetSet;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class UserAgentInterceptor implements Interceptor {


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        JSONObject userInfoJson = new JSONObject();
        try {
            userInfoJson.put("user_id", GetSet.getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String userInfo = userInfoJson.toString();
        Log.d("TAG", "Formatted User Info: " + userInfo);


        Request requestWithUserAgent = originalRequest.newBuilder()
                .header("x-device-info", getXDeviceInfo())
                .header("x-user-info", userInfo)
                .build();
        return chain.proceed(requestWithUserAgent);
    }

}