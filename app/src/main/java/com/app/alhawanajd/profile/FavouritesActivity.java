package com.app.alhawanajd.profile;
import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.app.alhawanajd.AddFavourites;
import com.app.alhawanajd.LikedVideosFragment;
import com.app.model.GetSet;
import com.google.android.material.button.MaterialButton;
import com.app.alhawanajd.R;
import com.app.external.MediaPlayerUtils;
import com.app.alhawanajd.BaseActivity;
import com.app.helper.LocaleManager;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.google.android.material.tabs.TabLayout;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NonConstantResourceId")
public class FavouritesActivity extends BaseActivity {

    String from, profileId;

    ImageView btnBack;
    MaterialButton btnMyVideos;
    MaterialButton btnLikedVideos;
    ViewPager viewpager;
    TextView txtTitle;

    ViewPagerAdapter adapter;
    private SlidrInterface slidrInterface;
    private final int NUM_OF_TABS = 2;
    private HomePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        animation();
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        profileId = getIntent().getStringExtra(Constants.TAG_PROFILE_ID);
        findViews();

        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }

//        txtTitle.setText(R.string.favourites);

        setUpViewPager();


        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void findViews() {
        btnBack = findViewById(R.id.btnBack);
        btnMyVideos = findViewById(R.id.btnMyVideos);
        btnLikedVideos = findViewById(R.id.btnLikedVideos);
        viewpager = findViewById(R.id.viewpager);
//        txtTitle = findViewById(R.id.txtTitle);
    }

    private void setUpViewPager() {
        ViewPager viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FavVideoFragment(profileId, getString(R.string.videos_txt)), getString(R.string.videos_txt));
        adapter.addFragment(new FavSoundFragment(profileId), getString(R.string.sounds));
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager); // <-- Link TabLayout with ViewPager

        Typeface typeface = ResourcesCompat.getFont(this, R.font.outfit_semibold);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                TextView tabTextView = new TextView(this);
                tabTextView.setText(tab.getText());
                tabTextView.setTextSize(16);
                tabTextView.setTypeface(typeface);
                tabTextView.setGravity(Gravity.CENTER);
                tab.setCustomView(tabTextView);

                // Set default (unselected) color
                if (i == tabLayout.getSelectedTabPosition()) {
                    tabTextView.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary1)); // Selected
                } else {
                    tabTextView.setTextColor(ContextCompat.getColor(this, R.color.bottomsheet_viewline)); // Unselected
                }
            }
        }

// 🔁 Handle color change on selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(ContextCompat.getColor(FavouritesActivity.this, R.color.colorPrimary1));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(ContextCompat.getColor(FavouritesActivity.this, R.color.bottomsheet_viewline));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });




        // Optional: page change callback
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrolled(int pos, float posOffset, int posOffsetPx) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public class HomePagerAdapter extends FragmentStateAdapter {
        public HomePagerAdapter(FragmentActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                default: {
                    return new AddFavourites(GetSet.getUserId(), getString(R.string.videos_txt), Constants.TAG_OWN_PROFILE , "profile");
                }
                case 1: {
                    return new LikedVideosFragment(GetSet.getUserId(), getString(R.string.liked_videos), Constants.TAG_OWN_PROFILE);
                }
                case 2: {
                    return new FavSoundFragment(GetSet.getUserId());
                }

            }
        }

        @Override
        public int getItemCount() {
            return NUM_OF_TABS;
        }

        public void resetViewPager() {

        }
    }

    private void animation() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        slidrInterface = Slidr.attach(this, config);

    }


    public void enableSliding(boolean enable) {
        if (enable)
            slidrInterface.unlock();
        else
            slidrInterface.lock();
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public @NotNull Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MediaPlayerUtils.releaseMediaPlayer();
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
    }
}