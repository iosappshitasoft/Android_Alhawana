package com.app.alhawanajd.home;

import static com.app.alhawanajd.R.id.bottom_navigation;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import com.app.alhawanajd.LoginActivity;
import com.app.alhawanajd.MainActivity;
import com.app.alhawanajd.databinding.FragmentHomeBinding;
import com.app.alhawanajd.home.eventbus.FollowingSwipePrevent;
import com.app.alhawanajd.home.eventbus.VideoZoomEvent;
import com.app.alhawanajd.search.SearchActivity;
import com.app.db.DBHelper;
import com.app.model.GetSet;
import com.app.utils.SharedPref;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.app.alhawanajd.R;
import com.app.helper.callback.ScreenOffsetListener;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import timber.log.Timber;


public class HomeFragment extends Fragment {

    public interface OnFragmentInteractionListener {
        void onFragmentEvent(); // You can change the parameter as needed
    }

    private OnFragmentInteractionListener mListener;

    private static final String TAG = HomeFragment.class.getSimpleName();
    ApiInterface apiInterface;
    private Context context;

    LinearLayout tabLayout;
    View toplay;
    public ViewPager2 homeFragViewPager;

    public ImageView search;



    AppUtils appUtils;
    private int previousPosition = 0;

    public HomeFollowing homeFollowing;
    public HomeForYou homeForYou;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

  //  @BindView(bottom_navigation)
    BottomNavigationView bottomNavigation;

    TextView tvForYou, tvFollowing;

     RelativeLayout relativeLive;
     ImageView imageButton;
    private static int PAGER_COUNT = 2;

    private final ScreenOffsetListener mScreenOffsetListener = visible -> {
/*
        if (visible.equals("visible")) {
            bottomNavigation.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
            search.setVisibility(View.VISIBLE);
           // mainActivity.menuAddStory.setVisibility(View.VISIBLE);
        } else {
            bottomNavigation.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
            search.setVisibility(View.GONE);
            //mainActivity.menuAddStory.setVisibility(View.GONE);
        }*/
        if (getActivity() != null) {
            BottomNavigationView activityBottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (activityBottomNav != null) {
                if (visible.equals("visible")) {
                    activityBottomNav.setVisibility(View.VISIBLE);
                    tabLayout.setVisibility(View.VISIBLE);
                    search.setVisibility(View.VISIBLE);
                } else {
                    activityBottomNav.setVisibility(View.GONE);
                    tabLayout.setVisibility(View.GONE);
                    search.setVisibility(View.GONE);
                }
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    public HomeFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_home, container, false);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView(rootView);
        appUtils = new AppUtils(getActivity());
     //   ButterKnife.bind(this, requireActivity());
        mListener = (OnFragmentInteractionListener) context;
        return rootView;
    }

    @Override
    public void setTargetFragment(@Nullable Fragment fragment, int requestCode) {
        super.setTargetFragment(fragment, requestCode);
    }

    @Override
    public void onPrimaryNavigationFragmentChanged(boolean isPrimaryNavigationFragment) {
        super.onPrimaryNavigationFragmentChanged(isPrimaryNavigationFragment);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView(View rootView) {

        tabLayout = rootView.findViewById(R.id.tabLayout);
        toplay = rootView.findViewById(R.id.toplay);
        tvForYou = rootView.findViewById(R.id.btnRecent);
        tvFollowing = rootView.findViewById(R.id.btnFollowings);
        homeFragViewPager = rootView.findViewById(R.id.fragment_home_viewpager);
        relativeLive = rootView.findViewById(R.id.relativeLive);
        imageButton = rootView.findViewById(R.id.imageButton);
        search = rootView.findViewById(R.id.imgsearch);
        imageButton.setVisibility(View.VISIBLE);
        relativeLive.setVisibility(View.GONE);


//        homeFragViewPager.setPageTransformer(new ZoomInTransformer());
        homeFragViewPager.setUserInputEnabled(false);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentEvent();
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mListener.onFragmentEvent();
                Intent intent = new Intent(context, SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        tvForYou.setOnClickListener(view -> {
            if (homeForYou != null && homeForYou.getForYouVideoFragment() != null && homeFollowing.getFollowingVideoFragment() != null) {
                homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(false);
                homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setExoPlayView(View.GONE);
                homeFragViewPager.setCurrentItem(0);
                homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setExoPlayView(View.VISIBLE);
                homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(true);
            }
        });
        tvFollowing.setOnClickListener(view -> {
            if (homeForYou != null && homeForYou.getForYouVideoFragment() != null && homeFollowing.getFollowingVideoFragment() != null) {
                homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);
                homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.playVideo(false);
                homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setExoPlayView(View.GONE);
                homeFragViewPager.setCurrentItem(1);
                homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(true);
                homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setExoPlayView(View.VISIBLE);
            }
        });

        relativeLive.setOnClickListener(view ->{
            if (Constants.Login_method.equals("skip")) {
                loginview();
            } else {
               /* Intent liveStreaming = new Intent(context, StreamListActivity.class);
                liveStreaming.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                liveStreaming.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                liveStreaming.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                liveStreaming.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                liveStreaming.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                context.startActivity(liveStreaming);*/
            }
        });

        HomePagerAdapter pagerAdapter = new HomePagerAdapter(mScreenOffsetListener);
        homeFragViewPager.setAdapter(pagerAdapter);
        homeFragViewPager.setOffscreenPageLimit(1);

        homeFragViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


                if (previousPosition == 0) {
                    if (positionOffset > 1) {

                    } else {

                       // tvForYou.setTextColor(context.getResources().getColor(R.color.colorWhite));
                       // tvFollowing.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));

                        tvForYou.setBackgroundResource(R.drawable.bg_top_selection_1);
                        tvFollowing.setBackgroundColor(Color.TRANSPARENT);

                    }
                } else {
                    if (positionOffset > 1) {

                    } else {

                       // tvForYou.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                      //  tvFollowing.setTextColor(context.getResources().getColor(R.color.colorWhite));

                        tvForYou.setBackgroundColor(Color.TRANSPARENT);
                        tvFollowing.setBackgroundResource(R.drawable.bg_top_selection_1);

                    }
                }
            }

            @Override
            public void onPageSelected(int position) {


                if (position == 0) {
                    //tvForYou.setTextColor(context.getResources().getColor(R.color.colorWhite));
                   // tvFollowing.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                    tvForYou.setBackgroundResource(R.drawable.bg_top_selection_1);
                    tvFollowing.setBackgroundColor(Color.TRANSPARENT);

                } else if (position == 1) {

                    //tvForYou.setTextColor(context.getResources().getColor(R.color.colorTransparentWhite));
                   // tvFollowing.setTextColor(context.getResources().getColor(R.color.colorWhite));

                    tvForYou.setBackgroundColor(Color.TRANSPARENT);
                    tvFollowing.setBackgroundResource(R.drawable.bg_top_selection_1);

                }

                previousPosition = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }


        });


    }

    private void loginview(){
        GetSet.reset();
        SharedPref.clearAll();
        DBHelper.getInstance(context).clearDB();
        Intent logout = new Intent(context, LoginActivity.class);
        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(logout);

    }

    public class HomePagerAdapter extends FragmentStateAdapter {
        private final ScreenOffsetListener mScreenOffsetListener;


        public HomePagerAdapter(ScreenOffsetListener screenOffsetListener) {
            super(HomeFragment.this);
            this.mScreenOffsetListener = screenOffsetListener;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {


            Timber.d("getItem: %d", position);

            switch (position) {

                case 0:
                default: {
                    homeForYou = HomeForYou.newInstance(mScreenOffsetListener, "single");
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TAG_TYPE, Constants.TAG_FOR_YOU);
                    bundle.putString(Constants.TAG_VIDEO_ID, null);
                    bundle.putString(Constants.TAG_TAGID, null);
                    homeForYou.setArguments(bundle);
                    return homeForYou;

                }
                case 1: {
                    homeFollowing = HomeFollowing.newInstance(mScreenOffsetListener);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TAG_TYPE, Constants.TAG_FOLLOWING);
                    bundle.putString(Constants.TAG_VIDEO_ID, null);
                    bundle.putString(Constants.TAG_TAGID, null);
                    homeFollowing.setArguments(bundle);
                    return homeFollowing;
                }
            }
        }

        @Override
        public int getItemCount() {
            return PAGER_COUNT;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;

    }


    /*@Override
    public void onResume() {

        super.onResume();
        View imageButton = getView().findViewById(R.id.imageButton);
        View relativeLive = getView().findViewById(R.id.relativeLive);

        if (imageButton != null) imageButton.setVisibility(View.VISIBLE);
        if (relativeLive != null) relativeLive.setVisibility(View.GONE);
    }*/

    @Override
    public void onResume() {
        super.onResume();
        View imageButton = getView().findViewById(R.id.imageButton);
        View relativeLive = getView().findViewById(R.id.relativeLive);

        if (imageButton != null) imageButton.setVisibility(View.VISIBLE);
        if (relativeLive != null) relativeLive.setVisibility(View.GONE);

        if (homeFragViewPager != null) {
            int currentItem = homeFragViewPager.getCurrentItem();
            Timber.d("onResume -> ViewPager currentItem = %d", currentItem);

            if (currentItem == 0) {
                if (homeForYou != null && homeForYou.getForYouVideoFragment() != null) {
                    Timber.d("onResume -> Resuming ForYou videos");

                    // Set view visible
                    homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setExoPlayView(View.VISIBLE);

                    // Resume playback with a slight delay
                    new Handler().postDelayed(() -> {
                        if (homeForYou != null && homeForYou.getForYouVideoFragment() != null) {
                            homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.resumePlayer();
                            homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.checkVideoVisibility();
                        }
                    }, 500);
                } else {
                    Timber.e("onResume -> ForYou fragment or recycler is NULL");
                }
            } else if (currentItem == 1) {
                if (homeFollowing != null && homeFollowing.getFollowingVideoFragment() != null) {
                    Timber.d("onResume -> Resuming Following videos");
                    homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setExoPlayView(View.VISIBLE);

                    new Handler().postDelayed(() -> {
                        if (homeFollowing != null && homeFollowing.getFollowingVideoFragment() != null) {
//                            homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.resumePlayer();
//                            homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.checkVideoVisibility();
                        }
                    }, 500);
                } else {
                    Timber.e("onResume -> Following fragment or recycler is NULL");
                }
            }
        } else {
            Timber.e("onResume -> homeFragViewPager is NULL");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseVideoPlayed();
    }

    private void pauseVideoPlayed() {
        if (homeFragViewPager != null && homeFragViewPager.getCurrentItem() == 0) {
            if (homeForYou != null)
                homeForYou.getForYouVideoFragment().exoplayerRecyclerViewForYou.setPlayControl(false);
        } else if (homeFragViewPager != null && homeFragViewPager.getCurrentItem() == 1) {
            if (homeFollowing != null)
                homeFollowing.getFollowingVideoFragment().followingExoplayerRecyclerView.setPlayControl(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VideoZoomEvent event) {
        Timber.d("onMessageEvent zoomscale: %s", event.currentZoomScale);
       // if (event.getCurrentZoomScale() >1){
        if (event.currentZoomScale >1){
            toplay.setVisibility(View.GONE);
        }
        else {
            toplay.setVisibility(View.VISIBLE);
        }

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
}
