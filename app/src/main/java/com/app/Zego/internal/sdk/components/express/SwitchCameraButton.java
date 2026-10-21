package com.app.Zego.internal.sdk.components.express;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.Zego.internal.sdk.ZEGOSDKManager;
import com.app.alhawanajd.R;

public class SwitchCameraButton extends ZImageButton {

    public SwitchCameraButton(@NonNull Context context) {
        super(context);
    }

    public SwitchCameraButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchCameraButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        setImageResource(R.drawable.call_icon_camera_flip, R.drawable.call_icon_camera_flip);
    }

    @Override
    public void open() {
        super.open();
        ZEGOSDKManager.getInstance().expressService.useFrontCamera(true);
    }

    @Override
    public void close() {
        super.close();
        ZEGOSDKManager.getInstance().expressService.useFrontCamera(false);
    }
}
