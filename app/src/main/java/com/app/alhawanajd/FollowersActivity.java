package com.app.alhawanajd;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.alhawanajd.databinding.ActivityFollowersBinding;
import com.facebook.shimmer.ShimmerFrameLayout;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
import com.app.alhawanajd.R;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.model.GetSet;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

@SuppressLint("NonConstantResourceId")
public class FollowersActivity extends BaseFragmentActivity {

    private static final String TAG = FollowersActivity.class.getSimpleName();

    private ActivityFollowersBinding binding;

    String partnerId;
    FollowingsFragment followingsFragment;
    FollowersFragment followersFragment;
    SlidrInterface slidrInterface;
//    @BindView(R.id.adView)
//    AdView adView;
    public static boolean hasInterestChange, hasFriendChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      /*  setContentView(R.layout.activity_followers);
        ButterKnife.bind(this);*/
        binding = ActivityFollowersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        partnerId = getIntent().getStringExtra(Constants.TAG_PARTNER_ID);
        SlidrConfig config = new SlidrConfig.Builder()
                .primaryColor(getResources().getColor(R.color.colorBlack))
                .secondaryColor(getResources().getColor(R.color.colorBlack))
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        slidrInterface = Slidr.attach(this, config);
        binding.shimmerLayout.startShimmer();
        initView();
    }

    private void initView() {
        setToolBarTitle(0);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        binding.viewpager.setOffscreenPageLimit(1);
        binding.viewpager.setAdapter(adapter);

        binding.viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setToolBarTitle(position);
                if (position == 0) {
                    enableSliding(true);
                } else {
                    enableSliding(false);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if ((getIntent().getStringExtra(Constants.TAG_ID) != null) &&
                (getIntent().getStringExtra(Constants.TAG_ID).equals(Constants.TAG_FOLLOWINGS))) {
            binding.viewpager.setCurrentItem(1);
        } else {
            binding.viewpager.setCurrentItem(0);
        }

        if (GetSet.getPremiumMember()!=null && GetSet.getPremiumMember().equals(Constants.TAG_FALSE))
//            loadAd();

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

        if (LocaleManager.isRTL()) {
            binding.btnBack.setRotation(180);
        } else {
            binding.btnBack.setRotation(0);
        }

    }

//    private void loadAd() {
//        if (AdminData.isAdEnabled()) {
////            MobileAds.initialize(this, AdminData.googleAdsId);
//            AdRequest adRequest = new AdRequest.Builder().build();
//            adView.loadAd(adRequest);
//            adView.setAdListener(new AdListener() {
//                @Override
//                public void onAdLoaded() {
//                    // Code to be executed when an ad finishes loading.
//                }
//
//                @Override
//                public void onAdOpened() {
//                    // Code to be executed when an ad opens an overlay that
//                    // covers the screen.
//                }
//
//                @Override
//                public void onAdClicked() {
//                    // Code to be executed when the user clicks on an ad.
//                }
//
//                @Override
//                public void onAdClosed() {
//                    // Code to be executed when the user is about to return
//                    // to the app after tapping on an ad.
//                }
//            });
//        }
//    }

    private void setToolBarTitle(int position) {
        if (position == 0) {
            if (binding.shimmerLayout.isShimmerStarted()) {
                binding.shimmerLayout.stopShimmer();
            }
            binding.shimmerLayout.startShimmer();
            binding.txtTitle.setText(getString(R.string.fans));
            binding.txtSubTitle.setText("");
            if (partnerId.equals(GetSet.getUserId())) {
                binding.txtSubTitle.setText(R.string.swipe_to_see_followings);
            } else {
                binding.txtSubTitle.setText(R.string.swipe_to_see_other_followings);
            }
            binding.txtSubTitle.setTextColor(getResources().getColor(R.color.colorWhite));
        } else {
            if (binding.shimmerLayout.isShimmerStarted()) {
                binding.shimmerLayout.stopShimmer();
            }
            binding.shimmerLayout.startShimmer();
            binding.txtTitle.setText(getString(R.string.followings));
            binding.txtSubTitle.setText("");
            if (partnerId.equals(GetSet.getUserId())) {
                binding.txtSubTitle.setText(R.string.swipe_to_see_followers);
            } else {
                binding.txtSubTitle.setText(R.string.swipe_to_see_other_followers);
            }
            binding.txtSubTitle.setTextColor(getResources().getColor(R.color.colorWhite));
        }
    }

   /* @SuppressLint("NonConstantResourceId")
    @OnClick(R.id.btnBack)
    public void onViewClicked() {
        onBackPressed();
    }
*/
    public class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_PAGES = 2;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                followersFragment = new FollowersFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TAG_PARTNER_ID, partnerId);
                followersFragment.setArguments(bundle);
                return followersFragment;
            } else {
                followingsFragment = new FollowingsFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TAG_PARTNER_ID, partnerId);
                followingsFragment.setArguments(bundle);
                return followingsFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    public void enableSliding(boolean enable) {
        if (enable)
            slidrInterface.unlock();
        else
            slidrInterface.lock();
    }

}
