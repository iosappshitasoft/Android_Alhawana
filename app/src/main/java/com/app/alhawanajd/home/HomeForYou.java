package com.app.alhawanajd.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.alhawanajd.MainActivity;
import com.app.alhawanajd.MultipleVideoActivity;
import com.app.alhawanajd.R;
import com.app.alhawanajd.home.eventbus.homeForYouSwipePrevent;
import com.app.helper.callback.ProfileImageClickListener;
import com.app.helper.callback.ScreenOffsetListener;
import com.app.utils.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import timber.log.Timber;


public class HomeForYou extends Fragment {

    private static final String TAG = HomeForYou.class.getSimpleName();
    public CusViewPager viewPager;

    private String apiType, videoId, tagId, from = null, from_key = "";
    ScreenOffsetListener screenOffsetListener;
    int previousPosition = 0, videoPosition = -1;

    private HomeForYouFragmentAdapter pagerAdapter;
    private ForYouVideoFragment mForYouVideoFragment;
    private FollowingProfileFragment mForYouProfileFragment;
    private ProfileImageClickListener mProfileImageClickListener = new ProfileImageClickListener() {
        @Override
        public void onUserClicked(Boolean isClicked, FollowingProfileUpdate profileUpdate) {
            if (profileUpdate != null) {
                mForYouProfileFragment.refreshProfile(profileUpdate);
            }

            if (isClicked) viewPager.setCurrentItem(1, true);
            else viewPager.setCurrentItem(0, true);
        }
    };


    public HomeForYou() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            apiType = bundle.getString(Constants.TAG_TYPE);
            if (bundle.containsKey(Constants.TAG_FROM)) {
                from = bundle.getString(Constants.TAG_FROM);
                Timber.d("onCreate:TAG_FROM: %s", from);
            }
            if (bundle.containsKey(Constants.TAG_POSITION)) {
                videoPosition = bundle.getInt(Constants.TAG_POSITION, -1);
            }
            if (bundle.containsKey(Constants.TAG_VIDEO_ID)) {
                videoId = bundle.getString(Constants.TAG_VIDEO_ID);
                tagId = bundle.getString(Constants.TAG_TAGID);
            }

            if (bundle.containsKey("from_key")) {
                from_key = bundle.getString("from_key");
            }

        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true  /*enabled by default*/) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                Timber.d("handleOnBackPressed:");

            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    public static HomeForYou newInstance(ScreenOffsetListener mScreenOffsetListener, String from_key) {
        HomeForYou fragment = new HomeForYou();
        fragment.screenOffsetListener = mScreenOffsetListener;
        Bundle args = new Bundle();
        args.putString("from_key", from_key);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.foryou_home, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        initViews();
    }

    private void findViews(View rootView) {
        viewPager = rootView.findViewById(R.id.viewpager_for_you);
        if (requireActivity() instanceof MultipleVideoActivity) {
            // do something only for MultipleVideoActivity
            Log.d(TAG, "Hihello MultipleVideoActivity:" );
            View parentLay = rootView.findViewById(R.id.parentLay);
            if (parentLay != null) {
                parentLay.setPadding(0, 0, 0, 0);
            }
        } else if (requireActivity() instanceof MainActivity) {
            // do something else for MainActivity
            Log.d(TAG, "Hihello MainActivity:" );
        }
    }

    private void initViews() {
        pagerAdapter = new HomeForYouFragmentAdapter(getChildFragmentManager(), mProfileImageClickListener);
        viewPager.setAdapter(pagerAdapter);
        viewPager.disableScroll(true);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                previousPosition = position;
                if (position == 1) {
                    screenOffsetListener.pageSwiped("gone");
                    mForYouVideoFragment.exoplayerRecyclerViewForYou.setPlayControl(false);
                } else {
                    screenOffsetListener.pageSwiped("visible");
                    mForYouVideoFragment.exoplayerRecyclerViewForYou.setPlayControl(true);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(homeForYouSwipePrevent event) {
//        Timber.d("onMessageEvent: homeForYouSwipePrevent: %s", event.swipe);
//        viewPager.disableScroll(event.swipe);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.i("HomeForYou onResume called");
        if (viewPager != null && pagerAdapter != null) {
            // Ensure the ViewPager is set to the first item (ForYouVideoFragment)
            viewPager.setCurrentItem(0, false); // false to avoid smooth scrolling animation on resume

            // Attempt to resume video playback if the fragment is available
            if (mForYouVideoFragment != null) {
                Timber.d("HomeForYou onResume: mForYouVideoFragment is NOT null.");
                if (mForYouVideoFragment.exoplayerRecyclerViewForYou != null) {
                    Timber.d("HomeForYou onResume: exoplayerRecyclerViewForYou is NOT null.");
                    mForYouVideoFragment.exoplayerRecyclerViewForYou.setPlayControl(true);
                } else {
                    Timber.e("HomeForYou onResume: ERROR: exoplayerRecyclerViewForYou is null in mForYouVideoFragment.");
                }
            } else {
                Timber.e("HomeForYou onResume: ERROR: mForYouVideoFragment is null.");
            }
        } else {
            Timber.e("HomeForYou onResume: ERROR: viewPager or pagerAdapter is null.");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.i("onPause called");
    }

    public class HomeForYouFragmentAdapter extends FragmentPagerAdapter {

        ProfileImageClickListener profileImageClickListener;

        public HomeForYouFragmentAdapter(FragmentManager fm, ProfileImageClickListener clickListener) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.profileImageClickListener = clickListener;
        }

        @Override
        public @NotNull
        Fragment getItem(int position) {
            switch (position) {

                case 0: {
                    mForYouVideoFragment = ForYouVideoFragment.newInstance(profileImageClickListener);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TAG_TYPE, apiType);
                    if (from != null) {
                        bundle.putString(Constants.TAG_FROM, from);
                    }
                    bundle.putInt(Constants.TAG_POSITION, videoPosition);
                    bundle.putString(Constants.TAG_VIDEO_ID, videoId);
                    bundle.putString(Constants.TAG_TAGID, tagId);
                    if (from_key != null) {
                        bundle.putString("from_key", from_key);
                    }
                    mForYouVideoFragment.setArguments(bundle);
                    return mForYouVideoFragment;
                }
                case 1: {
                    mForYouProfileFragment = FollowingProfileFragment.newInstance(profileImageClickListener);
                    Bundle bundle = new Bundle();
                    if (from != null) {
                        bundle.putString(Constants.TAG_FROM, from);
                    }
                    mForYouProfileFragment.setArguments(bundle);
                    return mForYouProfileFragment;
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }


    }


    public ForYouVideoFragment getForYouVideoFragment() {
        return mForYouVideoFragment;
    }

    //public ForYouProfileFragment getForYouProfileFragment() { return mForYouProfileFragment; }


}
