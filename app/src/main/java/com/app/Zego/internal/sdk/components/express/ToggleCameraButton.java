package com.app.Zego.internal.sdk.components.express;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.Zego.internal.sdk.ZEGOSDKManager;
import com.app.alhawanajd.R;

public class ToggleCameraButton extends ZImageButton {

    public ToggleCameraButton(@NonNull Context context) {
        super(context);
    }

    public ToggleCameraButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ToggleCameraButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        setImageResource(R.drawable.call_icon_camera_on, R.drawable.call_icon_camera_off);
    }

    @Override
    public void open() {
        super.open();
        ZEGOSDKManager.getInstance().expressService.openCamera(true);
    }

    @Override
    public void close() {
        super.close();
        ZEGOSDKManager.getInstance().expressService.openCamera(false);
    }
}
