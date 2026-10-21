package com.app.alhawanajd.profile;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.fragment.app.FragmentContainerView;

import com.bumptech.glide.Glide;
import com.app.alhawanajd.R;
import com.app.alhawanajd.BaseFragmentActivity;
import com.app.helper.NetworkReceiver;
import com.app.model.GetSet;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

public class MyProfileActivity extends BaseFragmentActivity {

    private static String TAG = MyProfileActivity.class.getSimpleName();
    FragmentContainerView fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .primaryColor(getResources().getColor(R.color.colorBlack))
                .secondaryColor(getResources().getColor(R.color.colorBlack))
                .build();
        SlidrInterface slidrInterface = Slidr.attach(this, config);

        if (savedInstanceState == null) {
            Bundle profile = new Bundle();
            profile.putString(Constants.TAG_USER_ID, GetSet.getUserId());
            profile.putBoolean(Constants.TAG_EDIT, getIntent().hasExtra(Constants.TAG_EDIT) && getIntent().getBooleanExtra(Constants.TAG_EDIT, false));
            profile.putString(Constants.TAG_TYPE,"MyProfile");
          /*  getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(false)
                    .add(R.id.fragmentContainer, ProfileFragment.class, profile)
                    .commit();*/
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(false)
                    .add(R.id.fragmentContainer, NewProfileFragment.class, profile)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
        if (!this.isDestroyed()) {
            Glide.with(getApplicationContext()).pauseRequests();
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        //AppUtils.showSnack(this, parentLay, isConnected);
    }
//
//    public void showLoading() {
//        /*Disable touch options*/
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//    }
//
//    public void hideLoading() {
//        /*Enable touch options*/
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//    }

    @Override
    public void onBackPressed() {
        /*        super.onBackPressed();*/
        backPressed();
    }


    public void backPressed() {
        setResult(RESULT_OK);
        finish();
        overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
    }

}


