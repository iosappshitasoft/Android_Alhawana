package com.app.alhawanajd.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.alhawanajd.R;
import com.app.alhawanajd.home.eventbus.FollowingSwipePrevent;
import com.app.helper.callback.ProfileImageClickListener;
import com.app.helper.callback.ScreenOffsetListener;
import com.app.utils.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import timber.log.Timber;


public class HomeFollowing extends Fragment {

    private static final String TAG = HomeFollowing.class.getSimpleName();
    private Context context;
    public CusViewPager viewPager;
    ScreenOffsetListener screenOffsetListener;
    private String from = null;

    int previousPosition = 0;
    FollowingFragmentAdapter pagerAdapter;


    private FollowingVideoFragment followingVideoFragment;
    private FollowingProfileFragment followingProfileFragment;

    public HomeFollowing() {
    }

    public static HomeFollowing newInstance(ScreenOffsetListener mScreenOffsetListener) {
        HomeFollowing fragment = new HomeFollowing();
        fragment.screenOffsetListener = mScreenOffsetListener;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(Constants.TAG_FROM)) {
            from = getArguments().getString(Constants.TAG_FROM);
        }
    }

    // Profile Image Click Callback from ForYouVideoFragment
    ProfileImageClickListener mProfileImageClickListener = new ProfileImageClickListener() {
        @Override
        public void onUserClicked(Boolean isClicked, FollowingProfileUpdate profileUpdate) {
            if (followingProfileFragment != null && profileUpdate != null) {
                followingProfileFragment.refreshProfile(profileUpdate);
            }
            if (isClicked) viewPager.setCurrentItem(1, true);
            else viewPager.setCurrentItem(0, true);
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(FollowingSwipePrevent event) {
//        Timber.d("onMessageEvent: %s", (event.swipe));
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
        Timber.i("resume called");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.following_homepage, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    public void setCallBack(ScreenOffsetListener screenOffsetListener) {
        this.screenOffsetListener = screenOffsetListener;
    }


    private void initView(View rootView) {

        viewPager = rootView.findViewById(R.id.viewpager_following);
        //pagerAdapter = new FollowingFragmentAdapter(requireActivity().getSupportFragmentManager(), mProfileImageClickListener);
        pagerAdapter = new FollowingFragmentAdapter(getChildFragmentManager(), mProfileImageClickListener);
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
                    if (followingVideoFragment.followingExoplayerRecyclerView != null)
                        followingVideoFragment.followingExoplayerRecyclerView.setPlayControl(false);
                } else {
                    screenOffsetListener.pageSwiped("visible");
//                    if (followingVideoFragment.followingExoplayerRecyclerView != null)
//                        followingVideoFragment.followingExoplayerRecyclerView.setPlayControl(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    /*@Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);

        Timber.d("onAttach: %s", childFragment);
    }*/

    public class FollowingFragmentAdapter extends FragmentPagerAdapter {

        ProfileImageClickListener profileImageClickListener;

        public FollowingFragmentAdapter(FragmentManager fm, ProfileImageClickListener mForYouProfileFragment) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.profileImageClickListener = mForYouProfileFragment;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                default: {
                    followingVideoFragment = FollowingVideoFragment.newInstance(profileImageClickListener);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TAG_TYPE, Constants.TAG_FOLLOWING);
                    bundle.putString(Constants.TAG_VIDEO_ID, null);
                    bundle.putString(Constants.TAG_TAGID, null);
                    followingVideoFragment.setArguments(bundle);
                    return followingVideoFragment;

                }
                case 1: {
                    followingProfileFragment = FollowingProfileFragment.newInstance(profileImageClickListener);
                    Bundle bundle = new Bundle();
                    if (from != null) {
                        bundle.putString(Constants.TAG_FROM, from);
                    }
                    followingProfileFragment.setArguments(bundle);
                    return followingProfileFragment;
                }
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public FollowingVideoFragment getFollowingVideoFragment() {
        return followingVideoFragment;
    }

}
