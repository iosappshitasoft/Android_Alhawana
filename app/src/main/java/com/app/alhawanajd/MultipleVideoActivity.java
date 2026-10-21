package com.app.alhawanajd;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentContainerView;

import com.app.alhawanajd.R;
import com.app.alhawanajd.home.HomeForYou;
import com.app.alhawanajd.home.eventbus.VideoZoomEvent;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.ScreenOffsetListener;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import timber.log.Timber;

public class MultipleVideoActivity extends BaseFragmentActivity {

    private static final String TAG = MultipleVideoActivity.class.getSimpleName();
    FragmentContainerView fragmentContainer;
    private String type, videoId, tagId, comment_open = null, from = null;
    private int position = -1;
    private HomeForYou homeForYou;
    private SlidrInterface slidrInterface;

    private final ScreenOffsetListener mScreenOffsetListener = visible -> {
        if (visible.equals("visible")) {
            slidrInterface.unlock();
        } else {
            slidrInterface.lock();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  applyTransparentStatusBar();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);

        setContentView(R.layout.multiple_video_activity);
        type = getIntent().getStringExtra(Constants.TAG_TYPE);
        Log.e(TAG, "type: "+type);
        videoId = getIntent().getStringExtra(Constants.TAG_VIDEO_ID);

        Log.e(TAG, "onCreate: "+videoId);

        if (getIntent().hasExtra(Constants.TAG_TAGID)) {
            tagId = getIntent().getStringExtra(Constants.TAG_TAGID);
        }
        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            from = getIntent().getStringExtra(Constants.TAG_FROM);
        }
        if (getIntent().hasExtra(Constants.TAG_POSITION)) {
            position = getIntent().getIntExtra(Constants.TAG_POSITION, -1);
        }
        if (getIntent().hasExtra(Constants.TAG_COMMENT_OPEN)) {
            comment_open = getIntent().getStringExtra(Constants.TAG_COMMENT_OPEN);
        }

        fragmentContainer = findViewById(R.id.fragmentContainer);

        SlidrConfig config = new SlidrConfig.Builder()
                .primaryColor(getResources().getColor(R.color.colorBlack))
                .secondaryColor(getResources().getColor(R.color.colorBlack))
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
//        slidrInterface = Slidr.attach(this, config);  // slide to close functionality ;

        Bundle bundle = new Bundle();
        bundle.putString(Constants.TAG_TYPE, type);
        bundle.putString(Constants.TAG_FROM, from != null ? from : Constants.TAG_VIDEO);
        if (comment_open != null) {
            bundle.putString(Constants.TAG_COMMENT_OPEN, Constants.TAG_COMMENT_OPEN);
        }
        bundle.putString(Constants.TAG_VIDEO_ID, videoId);
        bundle.putString(Constants.TAG_TAGID, tagId);
        bundle.putInt(Constants.TAG_POSITION, position);
        homeForYou = HomeForYou.newInstance(mScreenOffsetListener, "multiple");
        homeForYou.setArguments(bundle);


        Log.d("BUNDLE_DATA",
                "type=" + type +
                        ", from=" + (from != null ? from : Constants.TAG_VIDEO) +
                        ", comment_open=" + comment_open +
                        ", videoId=" + videoId +
                        ", tagId=" + tagId +
                        ", position=" + position
        );


        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(false)
                .add(R.id.fragmentContainer, homeForYou)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VideoZoomEvent event) {
        Timber.d("onMessageEvent zoomscale: %s", (event.currentZoomScale));
       // applyTransparentStatusBar();

        /*if (event.getCurrentZoomScale() >1){

        }*/

    }

    private void applyTransparentStatusBar() {

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            getWindow().setStatusBarColor(Color.TRANSPARENT);

    }

    @Override
    public void onBackPressed() {
        if (homeForYou != null && homeForYou.viewPager != null && homeForYou.viewPager.getCurrentItem() != 0) {
            Log.d("MultipleVideoActivity", "onBackPressed: Setting ViewPager current item to 0.");
            homeForYou.viewPager.setCurrentItem(0);
        } else {
            Log.d("MultipleVideoActivity", "onBackPressed: Removing fragment and calling super.onBackPressed().");
           /* getSupportFragmentManager().beginTransaction().
                    remove(Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.fragmentContainer))).commit();
           */ super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }

}
