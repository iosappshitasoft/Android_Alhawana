package com.app.addon;

//import com.google.android.gms.ads.AdError;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.FullScreenContentCallback;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.interstitial.InterstitialAd;
//import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.app.alhawanajd.R;


public class InterstitialAdUtils {

//    private static final String TAG = InterstitialAdUtils.class.getSimpleName();
//    public static InterstitialAd mInterstitialAd;
//
//    public static void loadInterstitialAd(FragmentActivity activity, String from, OnOkClickListener clickListener) {
//        AdRequest adRequest = new AdRequest.Builder().build();
//        InterstitialAd.load(activity, activity.getString(R.string.interstitial_ad_id), adRequest,
//                new InterstitialAdLoadCallback() {
//                    @Override
//                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                        // The mInterstitialAd reference will be null until
//                        // an ad is loaded.
//                        mInterstitialAd = interstitialAd;
//                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                            @Override
//                            public void onAdClicked() {
//                                super.onAdClicked();
//                            }
//
//                            @Override
//                            public void onAdDismissedFullScreenContent() {
//                                mInterstitialAd = null;
//                                onAdClosed(activity, from, clickListener);
//                            }
//
//                            @Override
//                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
//                                super.onAdFailedToShowFullScreenContent(adError);
//                                Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
//                                mInterstitialAd = null;
//                            }
//
//                            @Override
//                            public void onAdImpression() {
//                                super.onAdImpression();
//                            }
//
//                            @Override
//                            public void onAdShowedFullScreenContent() {
//                                super.onAdShowedFullScreenContent();
//                                mInterstitialAd = null;
//                            }
//                        });
//                        if (mInterstitialAd != null) {
//                            mInterstitialAd.show(activity);
//                        }
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        // Handle the error
//                        Timber.tag(TAG).e("onAdFailedToLoad: %s", loadAdError.getMessage());
//                        mInterstitialAd = null;
//                    }
//                });
//    }
//
//    private static void onAdClosed(FragmentActivity activity, String from, OnOkClickListener clickListener) {
//        if (from.equals("Gems")) {
//            Intent freeGems = new Intent(activity, GemsStoreActivity.class);
//            freeGems.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            activity.startActivity(freeGems);
//        } else if (from.equals(Constants.TAG_PROFILE)) {
//            clickListener.onOkClicked("", Constants.TAG_PROFILE);
//        } else {
//            Intent freeGems = new Intent(activity, SettingsActivity.class);
//            freeGems.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            activity.startActivity(freeGems);
//        }
//    }
//
//    public static void loadInterstitialAd(FragmentActivity activity, Bundle bundle) {
//        AdRequest adRequest = new AdRequest.Builder().build();
//        InterstitialAd.load(activity, activity.getString(R.string.interstitial_ad_id), adRequest,
//                new InterstitialAdLoadCallback() {
//                    @Override
//                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                        // The mInterstitialAd reference will be null until
//                        // an ad is loaded.
//                        mInterstitialAd = interstitialAd;
//                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
//                            @Override
//                            public void onAdClicked() {
//                                super.onAdClicked();
//                            }
//
//                            @Override
//                            public void onAdDismissedFullScreenContent() {
//                                mInterstitialAd = null;
//                                onAdClosed(activity, bundle);
//                            }
//
//                            @Override
//                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
//                                super.onAdFailedToShowFullScreenContent(adError);
//                                Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.getMessage());
//                                mInterstitialAd = null;
//                            }
//
//                            @Override
//                            public void onAdImpression() {
//                                super.onAdImpression();
//                            }
//
//                            @Override
//                            public void onAdShowedFullScreenContent() {
//                                super.onAdShowedFullScreenContent();
//                                mInterstitialAd = null;
//                            }
//                        });
//                        if (mInterstitialAd != null) {
//                            mInterstitialAd.show(activity);
//                        }
//                    }
//
//                    @Override
//                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                        // Handle the error
//                        Log.i(TAG, loadAdError.getMessage());
//                        mInterstitialAd = null;
//                    }
//                });
//    }
//
//    private static void onAdClosed(FragmentActivity activity, Bundle bundle) {
//        String blockStatus = bundle.getString(Constants.TAG_BLOCK_STATUS);
//        if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
//            App.makeToast(activity.getString(R.string.unblock_description));
//        } else {
//
//        }
//    }
}
