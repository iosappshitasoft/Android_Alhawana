package com.app.alhawanajd.home.eventbus;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;

public final class ForYouPlayerAttachmentHelper {

    private ForYouPlayerAttachmentHelper() { }

    public static void attachPlayerViewToContainer(ViewGroup mediaContainer,
                                                   StyledPlayerView playerView,
                                                   int insertAtIndex) {
        if (mediaContainer == null || playerView == null) return;

        try {
            if (playerView.getParent() != null) {
                ((ViewGroup) playerView.getParent()).removeView(playerView);
            }

            FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            flp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL; // important
            playerView.setLayoutParams(flp);

            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            playerView.setUseController(false);

            if (insertAtIndex >= 0 && insertAtIndex <= mediaContainer.getChildCount()) {
                mediaContainer.addView(playerView, insertAtIndex);
            } else {
                mediaContainer.addView(playerView, 0);
            }

            playerView.setVisibility(View.VISIBLE);
            playerView.bringToFront();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public static void attachPlayerViewToContainer(ViewGroup mediaContainer,
                                                   StyledPlayerView playerView) {
        attachPlayerViewToContainer(mediaContainer, playerView, 0);
    }

    public static void detachPlayerView(StyledPlayerView playerView) {
        if (playerView == null) return;
        try {
            if (playerView.getParent() != null) {
                ((ViewGroup) playerView.getParent()).removeView(playerView);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
