package com.app.alhawanajd;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

//import com.google.android.gms.ads.AdError;
//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.FullScreenContentCallback;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.OnUserEarnedRewardListener;
//import com.google.android.gms.ads.rewarded.RewardItem;
//import com.google.android.gms.ads.rewarded.RewardedAd;
//import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.app.alhawanajd.databinding.ActivityEarnGemsBinding;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.app.alhawanajd.R;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.model.GetSet;
import com.app.utils.AdminData;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.Map;
import java.util.concurrent.TimeUnit;


import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("NonConstantResourceId")
public class EarnGemsActivity extends BaseFragmentActivity {

    private static final String TAG = EarnGemsActivity.class.getSimpleName();
    ApiInterface apiInterface;
    private ActivityEarnGemsBinding binding;
//    @BindView(R.id.adView)
//    AdView adView;
    boolean watchVideoButton = false;
    private LinearLayout.LayoutParams linearParams;
    private RewardedAd mRewardedVideoAd;
    private CountDownTimer timer;
    private AppUtils appUtils;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorBlack));
        }
     /*   setContentView(R.layout.activity_earn_gems);
        ButterKnife.bind(this);*/
        binding = ActivityEarnGemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(this);
        progressDialog = new ProgressDialog(this, R.style.CustomProgressDialog);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.loading));
        initView();
    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);
        binding.includeHeader.btnSettings.setVisibility(View.VISIBLE);
        binding.includeHeader.btnSettings.setImageResource(R.drawable.copy);
       /* if (LocaleManager.isRTL()) {
            txtGemsDesc.setText(String.format(getString(R.string.free_gems_content)+AdminData.inviteCredits));
        }else{
            txtGemsDesc.setText(String.format(getString(R.string.free_gems_content), getString(R.string.empty)+AdminData.inviteCredits));
        }*/


        linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearParams.height = (int) (getDisplayHeight() * 0.35);
        //lottieImage.setLayoutParams(linearParams);

        if (SystemClock.elapsedRealtime() > SharedPref.getLong(SharedPref.VIDEO_END_TIME, 0)) {
            binding.btnWatchVideo.setEnabled(true);
            binding.btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
            binding.txtVideoTime.setText("");
            binding.txtVideoTime.setVisibility(View.INVISIBLE);
        } else {
            binding.txtVideoTime.setVisibility(View.VISIBLE);
            long endTime = SharedPref.getLong(SharedPref.VIDEO_END_TIME, 0);
            timer = new CountDownTimer(endTime - SystemClock.elapsedRealtime(), 1000) {
                @Override
                public void onTick(long l) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            long hours = (((endTime - SystemClock.elapsedRealtime()) / 1000) / 60) / 60;
                            long minutes = ((endTime - SystemClock.elapsedRealtime()) / 1000) / 60;
                            long seconds = ((endTime - SystemClock.elapsedRealtime()) / 1000) % 60;
                            String videoTime = getString(R.string.video_time) + " " + AppUtils.twoDigitString(hours) +
                                    getString(R.string.h).toLowerCase() + " " + AppUtils.twoDigitString(minutes) + getString(R.string.m).toLowerCase() +
                                    " " + AppUtils.twoDigitString(seconds) + getString(R.string.s).toLowerCase();
                            binding.txtVideoTime.setText(videoTime);
                            binding.btnWatchVideo.setEnabled(false);
                            binding.btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorGrey));
                        }
                    });
                }

                @Override
                public void onFinish() {
                    runOnUiThread(() -> {
                        timer.cancel();
                        binding.txtVideoTime.setText("");
                        binding.txtVideoTime.setVisibility(View.INVISIBLE);
                        binding.btnWatchVideo.setEnabled(true);
                        binding.btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                    });
                }
            };
            timer.start();
        }

        binding.includeHeader.btnSettings.setImageDrawable(getDrawable(R.drawable.copy));
        binding.includeHeader.txtTitle.setText(getString(R.string.invite_friends));

        if (!AdminData.showVideoAd.equals("1")) {
            binding.btnWatchVideo.setVisibility(View.INVISIBLE);
        }
//        loadAd();

        if (LocaleManager.isRTL()) {
            binding.includeHeader.btnBack.setRotation(180);
        } else {
            binding.includeHeader.btnBack.setRotation(0);
        }

        binding.includeHeader.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.includeHeader.btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.preventMultipleClick(binding.includeHeader.btnSettings);
                copyReferCode();
            }
        });

        binding.btnWatchVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.preventMultipleClick(binding.btnWatchVideo);
                binding.btnWatchVideo.setEnabled(false);
                watchVideoButton = true;
                initRewardedVideo();
            }
        });

        binding.btnRefer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.preventMultipleClick(binding.btnRefer);
                openShare();
            }
        });

    }

//    private void loadAd() {
//        Log.d(TAG, "loadAd: " + AdminData.isAdEnabled());
//        if (AdminData.isAdEnabled()) {
////            MobileAds.initialize(this, AdminData.googleAdsId);
//            AdRequest adRequest = new AdRequest.Builder().build();
//            adView.loadAd(adRequest);
//            adView.setAdListener(new AdListener() {
//                @Override
//                public void onAdLoaded() {
//                    // Code to be executed when an ad finishes loading.
//                    Log.d(TAG, "onAdLoaded: ");
//                    dismissLoading();
//                }
//
//
//                @Override
//                public void onAdFailedToLoad(LoadAdError loadAdError) {
//                    super.onAdFailedToLoad(loadAdError);
//                    Log.d(TAG, "onAdFailedToLoad: " + loadAdError.getMessage());
//                }
//
//                @Override
//                public void onAdOpened() {
//                    // Code to be executed when an ad opens an overlay that
//                    // covers the screen.
//                    Log.d(TAG, "onAdOpened: ");
//                }
//
//                @Override
//                public void onAdClicked() {
//                    // Code to be executed when the user clicks on an ad.
//                    Log.d(TAG, "onAdClicked: ");
//                }
//
//                /*@Override
//                public void onAdLeftApplication() {
//                    // Code to be executed when the user has left the app.
//                    Log.d(TAG, "onAdLeftApplication: ");
//                }*/
//
//                @Override
//                public void onAdClosed() {
//                    // Code to be executed when the user is about to return
//                    // to the app after tapping on an ad.
//                    Log.d(TAG, "onAdClosed: ");
//                }
//            });
//        }
//    }

    private void showLoading() {
        if (progressDialog != null && !progressDialog.isShowing())
            progressDialog.show();
    }

    private void dismissLoading() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }

   /* @OnClick({R.id.btnBack, R.id.btnSettings, R.id.btnWatchVideo, R.id.btnRefer})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnBack:
                onBackPressed();
                break;
            case R.id.btnSettings:
                App.preventMultipleClick(btnCopy);
                copyReferCode();
                break;
            case R.id.btnWatchVideo:
                App.preventMultipleClick(btnWatchVideo);
                btnWatchVideo.setEnabled(false);
                watchVideoButton = true;
                initRewardedVideo();
                break;
            case R.id.btnRefer:
                App.preventMultipleClick(btnRefer);
                openShare();
                break;
        }
    }
*/
    private void openShare() {
        Task<ShortDynamicLink> shortLinkTask = appUtils.getDynamicLink();

        shortLinkTask.addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Short link created
                Uri shortLink = task.getResult().getShortLink();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String msg = String.format(getString(R.string.invite_description), getString(R.string.app_name), shortLink);
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                startActivity(Intent.createChooser(intent, getString(R.string.share_link)));
            } else {
                // Error
                // ...
                Log.e(TAG, "onComplete else: ");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "onFailure: " + e.getMessage()));
    }

    private void copyReferCode() {

        Task<ShortDynamicLink> shortLinkTask = appUtils.getDynamicLink();

        shortLinkTask.addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Short link created
                // Short link created
                Uri shortLink = task.getResult().getShortLink();
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String msg = String.format(getString(R.string.invite_description), getString(R.string.app_name), shortLink);
                ClipData clip = ClipData.newPlainText("label", "" + msg);
                clipboard.setPrimaryClip(clip);
                Toasty.success(EarnGemsActivity.this, getString(R.string.referral_code_copied), Toasty.LENGTH_SHORT).show();
            } else {
                // Error
                // ...
                Log.e(TAG, "onComplete: ");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "onFailure: " + e.getMessage()));

    }

    private void initRewardedVideo() {
        showLoading();
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, getString(R.string.video_ad_id),
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.e(TAG, "initRewardedVideo: " + loadAdError.getMessage());
//                        mRewardedVideoAd = null;
                        dismissLoading();
                        binding.btnWatchVideo.setVisibility(View.VISIBLE);
                        binding.btnWatchVideo.setEnabled(true);
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedVideoAd = rewardedAd;
                        openVideo();
                    }
                });
    }

    private void openVideo() {
        if (mRewardedVideoAd != null) {
            mRewardedVideoAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    super.onAdFailedToShowFullScreenContent(adError);
                }

                @Override
                public void onAdImpression() {
                    super.onAdImpression();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent();
                    dismissLoading();
                    binding.btnWatchVideo.setVisibility(View.VISIBLE);
                }
            });

            mRewardedVideoAd.show(this, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    watchVideoButton = false;
                    Log.d(TAG, "onRewarded: ");
                    updateRewards(rewardItem.getAmount());
                    SharedPref.putLong(SharedPref.VIDEO_END_TIME, (SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(AdminData.videoAdsDuration)));
                    Long endTime = SharedPref.getLong(SharedPref.VIDEO_END_TIME, 0);
                    binding.btnWatchVideo.setEnabled(false);
                    binding.btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorGrey));
                    binding.txtVideoTime.setVisibility(View.VISIBLE);
                    timer = new CountDownTimer(TimeUnit.MINUTES.toMillis(AdminData.videoAdsDuration), 1000) {
                        @Override
                        public void onTick(long l) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    binding.btnWatchVideo.setEnabled(false);
                                    long hours = (((endTime - SystemClock.elapsedRealtime()) / 1000) / 60) / 60;
                                    long minutes = ((endTime - SystemClock.elapsedRealtime()) / 1000) / 60;
                                    long seconds = ((endTime - SystemClock.elapsedRealtime()) / 1000) % 60;
                                    String videoTime = getString(R.string.video_time) + " " + AppUtils.twoDigitString(hours) + " " +
                                            getString(R.string.h).toLowerCase() + " " + AppUtils.twoDigitString(minutes) + getString(R.string.m).toLowerCase() +
                                            " " + AppUtils.twoDigitString(seconds) + getString(R.string.s).toLowerCase();
                                    binding.txtVideoTime.setText(videoTime);
                                }
                            });
                        }

                        @Override
                        public void onFinish() {
                            runOnUiThread(() -> {
                                timer.cancel();
                                binding.txtVideoTime.setVisibility(View.INVISIBLE);
                                binding.btnWatchVideo.setEnabled(true);
                                binding.btnWatchVideo.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                            });
                        }
                    };
                    timer.start();
                }
            });
        } else {
            binding.btnWatchVideo.setEnabled(true);
            binding.btnWatchVideo.setVisibility(View.VISIBLE);
            dismissLoading();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.btnWatchVideo.setEnabled(true);
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
//        mRewardedVideoAd.resume(this);
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
//        mRewardedVideoAd.destroy(this);
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
//        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    private void updateRewards(int amount) {
        Call<Map<String, String>> call = apiInterface.updateVideoGems(GetSet.getUserId());
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, String> map = response.body();
                    if (map.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        GetSet.setGems(Float.valueOf((map.get(Constants.TAG_TOTAL_GEMS))));
                        SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                        App.makeToast(getString(R.string.gems_added_to_your_account));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                call.cancel();
            }
        });
    }
}
