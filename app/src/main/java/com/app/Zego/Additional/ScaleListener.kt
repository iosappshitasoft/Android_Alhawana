package com.app.Zego.Additional

import android.view.ScaleGestureDetector
import com.app.Zego.internal.ZEGOLiveStreamingManager
import com.app.Zego.internal.sdk.ZEGOSDKManager
import com.app.model.GetSet


class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    var scaleFactor = 1.0f
    private val MIN_SCALE_FACTOR = 1.0f
    private val MAX_SCALE_FACTOR = 5.0f
    private val SCALE_FACTOR_CHANGE = 0.1f

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        var newScaleFactor: Float = scaleFactor * detector.scaleFactor
        newScaleFactor = Math.max(
            MIN_SCALE_FACTOR,
            Math.min(newScaleFactor, MAX_SCALE_FACTOR)
        )

        if (ZEGOLiveStreamingManager.getInstance()
                .isHost(GetSet.getUserId()) || ZEGOLiveStreamingManager.getInstance().isCoHost(
                GetSet.getUserId()
            )
        ) {
            scaleFactor = newScaleFactor
            ZEGOSDKManager.getInstance().expressService.setCameraZoom(newScaleFactor)
            return true
        }
        return false
    }
}

