package com.app.utils;

//import com.google.android.gms.ads.AdListener;
//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.AdView;
//import com.google.android.gms.ads.LoadAdError;
//import com.google.android.gms.ads.MobileAds;
//import com.google.android.gms.ads.initialization.InitializationStatus;
//import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.app.alhawanajd.R;

public class BannerAdUtils {
//    static AdView adView = null;
//
//    public static void loadBannerAd(FragmentActivity activity, AdView adView) {
//        if (AdminData.isAdEnabled()) {
//            MobileAds.initialize(activity, new OnInitializationCompleteListener() {
//                @Override
//                public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
//                }
//            });
//
//            AdRequest adRequest = new AdRequest.Builder().build();
//            adView.setAdListener(new AdListener() {
//                @Override
//                public void onAdClicked() {
//                    super.onAdClicked();
//                }
//
//                @Override
//                public void onAdClosed() {
//                    super.onAdClosed();
//                }
//
//                @Override
//                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                    Log.e(activity.getLocalClassName(), "onAdFailedToLoad: " + loadAdError.getCode());
//                    Log.e(activity.getLocalClassName(), "onAdFailedToLoad: " + loadAdError.getMessage());
//                }
//
//                @Override
//                public void onAdImpression() {
//                    super.onAdImpression();
//                }
//
//                @Override
//                public void onAdLoaded() {
//                    super.onAdLoaded();
//                }
//
//                @Override
//                public void onAdOpened() {
//                    super.onAdOpened();
//                }
//            });
//            adView.loadAd(adRequest);
//
//        }
//    }
//
//    public static void loadBannerAd(FragmentActivity activity, FrameLayout adLay) {
////        if (AdminData.isAdEnabled() /*&& GetSet.getPremiumMember().equals(Constants.TAG_FALSE)*/) {
//            MobileAds.initialize(activity, new OnInitializationCompleteListener() {
//                @Override
//                public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
//                }
//            });
//            adView = new AdView(activity);
//            adView.setAdUnitId(activity.getString(R.string.banner_ad_id));
//            adLay.removeAllViews();
//            adLay.addView(adView);
//            adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, AppUtils.getAdWidth(activity)));
//            adView.setAdListener(new AdListener() {
//                @Override
//                public void onAdClicked() {
//                    super.onAdClicked();
//                }
//
//                @Override
//                public void onAdClosed() {
//                    super.onAdClosed();
//                }
//
//                @Override
//                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                    Log.e(activity.getLocalClassName(), "onAdFailedToLoad: " + loadAdError.getCode());
//                    Log.e(activity.getLocalClassName(), "onAdFailedToLoad: " + loadAdError.getMessage());
//                }
//
//                @Override
//                public void onAdImpression() {
//                    super.onAdImpression();
//                }
//
//                @Override
//                public void onAdLoaded() {
//                    super.onAdLoaded();
//                }
//
//                @Override
//                public void onAdOpened() {
//                    super.onAdOpened();
//                }
//            });
//            // Initiate a generic request to load it with an ad
//            /*List<String> testDevices = new ArrayList<>();
//            testDevices.add("8024116C4033A1FFE31F4B53022EE848");
//            testDevices.add("2F8ED1189BACAB56382F18AF1C810828");
//            RequestConfiguration requestConfiguration
//                    = new RequestConfiguration.Builder()
//                    .setTestDeviceIds(testDevices)
//                    .build();
//            MobileAds.setRequestConfiguration(requestConfiguration);*/
//
//            AdRequest adRequest = new AdRequest.Builder()
//                    .build();
//            adView.loadAd(adRequest);
////        }
//    }
//
//    public static void onResume(AdView adView) {
//        if (adView != null) {
//            adView.resume();
//        }
//    }
//
//    public static void onPause(AdView adView) {
//        if (adView != null) {
//            adView.pause();
//        }
//    }
//
//    public static void onDestroy(AdView adView) {
//        if (adView != null) {
//            adView.destroy();
//        }
//    }
//
//    public static void onResume() {
//        if (adView != null) {
//            adView.resume();
//        }
//    }
//
//    public static void onPause() {
//        if (adView != null) {
//            adView.pause();
//        }
//    }
//
//    public static void onDestroy() {
//        if (adView != null) {
//            adView.destroy();
//        }
//    }
}
