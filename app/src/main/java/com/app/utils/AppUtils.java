package com.app.utils;

import static android.content.Context.FINGERPRINT_SERVICE;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.drawable.GradientDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.text.Html;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.app.Zego.internal.utils.Base64;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.app.alhawanajd.BuildConfig;
import com.app.alhawanajd.R;
import com.app.external.CryptLib;
import com.app.external.hashtagview.HashtagView;
import com.app.helper.LocaleManager;
import com.app.model.GetSet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.crypto.NoSuchPaddingException;

import timber.log.Timber;

public class AppUtils {

    private static final String TAG = AppUtils.class.getSimpleName();
    private static Snackbar snackbar;
    public static final String SMILEY_PATH = "images/lottie/smileys/";
    public static List<String> callerList = new ArrayList<>();
    public static List<String> filterLocation = new ArrayList<>();
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    public static final String UTC_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static boolean isExternalPlay = false;
    private static String currentStatus = Constants.ONLINE;

    public static List<String> smileyList = new ArrayList<String>() {{
        add("smile");
        add("angry");
        add("cry");
        add("lol");
        add("tired");
        add("wow");
        add("smile");
        add("angry");
        add("cry");
        add("lol");
        add("tired");
        add("wow");
        add("smile");
        add("angry");
        add("cry");
        add("lol");
        add("tired");
        add("wow");
        add("smile");
        add("angry");
        add("cry");
        add("lol");
        add("tired");
        add("wow");
        add("smile");
        add("angry");
        add("cry");
        add("lol");
        add("tired");
        add("wow");
    }};
    private final Context context;

    public AppUtils(Context context) {
        this.context = context;
    }

    public static int calculateAge(Date date) {
        Calendar birth = Calendar.getInstance();
        birth.setTime(date);
        Calendar today = Calendar.getInstance();

        int yearDifference = today.get(Calendar.YEAR)
                - birth.get(Calendar.YEAR);

        if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH)) {
            yearDifference--;
        } else {
            if (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH)
                    && today.get(Calendar.DAY_OF_MONTH) < birth
                    .get(Calendar.DAY_OF_MONTH)) {
                yearDifference--;
            }

        }

        return yearDifference;
    }

    /**
     * function for avoiding emoji typing in keyboard
     **/
    public static InputFilter EMOJI_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int index = start; index < end; index++) {

                int type = Character.getType(source.charAt(index));

                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL || Character.isSpaceChar(source.charAt(index))) {
                    return "";
                }
            }
            return null;
        }
    };

    /**
     * function for avoiding special characters typing in keyboard
     **/
    public static InputFilter SPECIAL_CHARACTERS_FILTER = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    /**
     * To convert the given dp value to pixel
     **/
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static int getScreenWidth(FragmentActivity activity) {
        int displayWidth = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            displayWidth = windowMetrics.getBounds().width() - insets.left - insets.right;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            displayWidth = displayMetrics.widthPixels;
        }
        return displayWidth;
    }

    public static int getScreenHeight(FragmentActivity activity) {
        int displayHeight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            displayHeight = windowMetrics.getBounds().height() - insets.top - insets.bottom;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            displayHeight = displayMetrics.heightPixels;
        }
        return displayHeight;
    }

    public static int getAdWidth(FragmentActivity activity) {
        float widthPixels;
        float density;
        int adWidth;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            widthPixels = windowMetrics.getBounds().width() - insets.left - insets.right;
            Display display = activity.getDisplay();
            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
            display.getRealMetrics(displayMetrics);
            density = displayMetrics.densityDpi;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            widthPixels = displayMetrics.widthPixels;
            density = displayMetrics.density;
        }
        adWidth = (int) (widthPixels / density);
        Log.d(TAG, "getAdWidth: " + widthPixels);
//        Log.d(TAG, "getAdWidth: " + density);
//        Log.d(TAG, "getAdWidth: " + adWidth);
        return adWidth;
    }

    public static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
        int buffSize = 1024;
        byte[] buff = new byte[buffSize];
        int len = 0;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }
        return byteBuff.toByteArray();
    }

    public static InputStream convertBase64ToInputStream(String base64Image) {
        // Remove the data URL prefix if it exists
        if (base64Image.contains(",")) {
            base64Image = base64Image.split(",")[1];
        }

        // Decode the Base64 string to a byte array
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        // Convert byte array to InputStream
        return new ByteArrayInputStream(imageBytes);
    }

    /**
     * Returns the consumer friendly device name
     */
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        Logging.i(TAG, "getDeviceName: " + manufacturer + " " + model);
        return manufacturer + " " + model;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void hideStatusBar(Activity activity) {
        View decorView = activity.getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = activity.getActionBar();
        actionBar.hide();
    }

    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * Returns {@code null} if this couldn't be determined.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @SuppressLint("PrivateApi")
    public static Boolean hasNavigationBar() {
        try {
            Class<?> serviceManager = Class.forName("android.os.ServiceManager");
            IBinder serviceBinder = (IBinder) serviceManager.getMethod("getService", String.class).invoke(serviceManager, "window");
            Class<?> stub = Class.forName("android.view.IWindowManager$Stub");
            Object windowManagerService = stub.getMethod("asInterface", IBinder.class).invoke(stub, serviceBinder);
            Method hasNavigationBar = windowManagerService.getClass().getMethod("hasNavigationBar");
            return (boolean) hasNavigationBar.invoke(windowManagerService);
        } catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Log.w("YOUR_TAG_HERE", "Couldn't determine whether the device has a navigation bar", e);
            return false;
        }
    }

    public static String getPrimeTitle(Context context) {
        if (GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
            return context.getString(R.string.premium_member);
        } else {
            return context.getString(R.string.get_prime_benefits);
        }
    }

    public static String getPrimeContent(Context context) {
        if (GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
            return context.getString(R.string.valid_until) + " " + getPremiumDateFromUTC(context, GetSet.getPremiumExpiry());
        } else {
            String price = SharedPref.getString(SharedPref.IN_APP_PRICE, Constants.DEFAULT_SUBSCRIPTION);
            String validity = SharedPref.getString(SharedPref.IN_APP_VALIDITY, Constants.DEFAULT_VALIDITY);
            switch (validity) {
                case "P1W":
                    validity = context.getString(R.string.one_week_subs);
                    break;
                case "P1M":
                    validity = context.getString(R.string.one_month_subs);
                    break;
                case "P3M":
                    validity = context.getString(R.string.three_month_subs);
                    break;
                case "P6M":
                    validity = context.getString(R.string.six_month_subs);
                    break;
                case "P1Y":
                    validity = context.getString(R.string.one_year_subs);
                    break;
            }
            return context.getString(R.string.subscription_price, price, validity);
        }
    }

    public static String getPackageTitle(String validity, Context context) {
        switch (validity) {
            case "1W":
                validity = context.getString(R.string.one_week);
                break;
            case "1M":
                validity = context.getString(R.string.one_month);
                break;
            case "3M":
                validity = context.getString(R.string.three_months);
                break;
            case "6M":
                validity = context.getString(R.string.six_months);
                break;
            case "1Y":
                validity = context.getString(R.string.one_year);
                break;
        }
        return validity;
    }

    public static String getGemsCount(Context context, Long gems) {
        if (gems != null && gems > 999) {
            return (gems / 1000) + context.getString(R.string.k);
        } else {
            return "" + gems;
        }
    }

    private static String getPremiumDateFromUTC(Context context, String premiumExpiry) {
        DateFormat utcFormat = new SimpleDateFormat(UTC_DATE_PATTERN, LocaleManager.getLocale(context.getResources()));
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", LocaleManager.getLocale(context.getResources()));
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date premiumDate = new Date();
        try {
            premiumDate = utcFormat.parse(premiumExpiry);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateFormat.format(premiumDate);
    }
    public Task<ShortDynamicLink> getStreamShareLinkNew(String shareLink,String urlimage) {
        String url = Constants.APP_SHARE_URL + "/" + shareLink;
        Log.e(TAG, "getStreamShareLink: " + url+" , "+urlimage);
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(url))
                .setDomainUriPrefix("https://" + context.getString(R.string.dynamic_link_filter) + "/")
                .setIosParameters(new DynamicLink.IosParameters.Builder(context.getString(R.string.ios_bundle_id))
                        .setAppStoreId(context.getString(R.string.ios_app_store_id))
                        .setCustomScheme(context.getString(R.string.ios_bundle_id))
                        .build())
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder(BuildConfig.APPLICATION_ID)
                        .build())
                .setSocialMetaTagParameters(
                        new DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle("Bring The Noise")
                                .setDescription("This link works whether the app is installed or not!")
                                //   .setImageUrl(Uri.parse("https://firebasestorage.googleapis.com/v0/b/hiddy-89381.appspot.com/o/Untitled%202.png?alt=media&token=5662a93b-1764-43ab-8aee-9e12ab64337b"))
                                //.setImageUrl(Uri.parse("https://bringthenoiseappnew.s3.amazonaws.com/img/accounts/profile-image-1689921063542"))
                                .setImageUrl(Uri.parse(urlimage))
                                .build())
                .buildShortDynamicLink();

        return shortLinkTask;
    }
    public static String getCurrentUTCTime(Context context) {
        DateFormat utcFormat = new SimpleDateFormat(UTC_DATE_PATTERN, Locale.ENGLISH);
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        return utcFormat.format(myDate);
    }

    public static String getUTCFromTimeStamp(Context context, long timeStamp) {
        DateFormat utcFormat = new SimpleDateFormat(UTC_DATE_PATTERN, LocaleManager.getLocale(context.getResources()));
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        myDate.setTime(timeStamp);
        return utcFormat.format(myDate);
    }

    public static Long getTimeFromUTC(Context context, String date) {
        DateFormat utcFormat = new SimpleDateFormat(UTC_DATE_PATTERN, Locale.ENGLISH);
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        try {
            myDate = utcFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        Log.i(TAG, "getTimeFromUTC: " + myDate.getTime());
        return myDate.getTime();
    }

    public static String getDateFromUTC(Context context, String date) {
        DateFormat utcFormat = new SimpleDateFormat(UTC_DATE_PATTERN, Locale.ENGLISH);
        DateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd",  Locale.ENGLISH);
        TimeZone utcZone = TimeZone.getTimeZone("UTC");
        utcFormat.setTimeZone(utcZone);
        Date myDate = new Date();
        String newDate = "";
        try {
            myDate = utcFormat.parse(date);
            newDate = newFormat.format(myDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        Log.i(TAG, "getDateFromUTC: " + newDate);
        return newDate;
    }

    public static String getRecentDate(Context context, long smsTimeInMillis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeZone(TimeZone.getDefault());
        smsTime.setTimeInMillis(smsTimeInMillis);

        Calendar now = Calendar.getInstance();
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm", Locale.ENGLISH);
            SimpleDateFormat hourFormat = new SimpleDateFormat("aa", LocaleManager.getLocale(context.getResources()));
            String recent = timeFormat.format(new Date(smsTime.getTimeInMillis())) + " " + hourFormat.format(new Date(smsTime.getTimeInMillis()));
            return context.getString(R.string.today) + ", " + recent;
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
            SimpleDateFormat hourFormat = new SimpleDateFormat("aa", LocaleManager.getLocale(context.getResources()));
            Date date = new Date(smsTime.getTimeInMillis());
            String recent = timeFormat.format(date) + " " + hourFormat.format(date);
            return context.getString(R.string.yesterday) + ", " + recent;
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
            SimpleDateFormat hourFormat = new SimpleDateFormat("aa", LocaleManager.getLocale(context.getResources()));
            Date date = new Date(smsTime.getTimeInMillis());
            String recent = dateFormat.format(date) + ", " + timeFormat.format(date) + " " + hourFormat.format(date);
            return recent;
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
            SimpleDateFormat hourFormat = new SimpleDateFormat("aa", LocaleManager.getLocale(context.getResources()));
            Date date = new Date(smsTime.getTimeInMillis());
            String recent = dateFormat.format(date) + ", " + timeFormat.format(date) + " " + hourFormat.format(date);
            return recent;
        }
    }

    public static String getRecentHistory(Context context, long smsTimeInMillis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeZone(TimeZone.getDefault());
        smsTime.setTimeInMillis(smsTimeInMillis);

        Calendar now = Calendar.getInstance();
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm", Locale.ENGLISH);
            SimpleDateFormat hourFormat = new SimpleDateFormat("aa", LocaleManager.getLocale(context.getResources()));
            String recent = timeFormat.format(new Date(smsTime.getTimeInMillis())) + " " + hourFormat.format(new Date(smsTime.getTimeInMillis()));
            return context.getString(R.string.today) + ", " + recent;
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
            SimpleDateFormat hourFormat = new SimpleDateFormat("aa", LocaleManager.getLocale(context.getResources()));
            Date date = new Date(smsTime.getTimeInMillis());
            String recent = timeFormat.format(date) + " " + hourFormat.format(date);
            return context.getString(R.string.yesterday) + ", " + recent;
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", LocaleManager.getLocale(context.getResources()));
            SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
            SimpleDateFormat hourFormat = new SimpleDateFormat("aa", LocaleManager.getLocale(context.getResources()));
            Date date = new Date(smsTime.getTimeInMillis());
            String recent = monthFormat.format(date) + " " + dayFormat.format(date) + ", " + timeFormat.format(date) + " " + hourFormat.format(date);
            return recent;
        } else {
            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", LocaleManager.getLocale(context.getResources()));
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd yyyy", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
            SimpleDateFormat hourFormat = new SimpleDateFormat("aa", LocaleManager.getLocale(context.getResources()));
            Date date = new Date(smsTime.getTimeInMillis());
            String recent = monthFormat.format(date) + " " + dayFormat.format(date) + ", " + timeFormat.format(date) +
                    " " + hourFormat.format(date);
            return recent;
        }
    }

    public static String getRecentChatTime(Context context, String utcTime) {
        Date inputDate = new Date();
        if (utcTime != null && !utcTime.isEmpty()) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(UTC_DATE_PATTERN);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                inputDate = simpleDateFormat.parse(utcTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeZone(TimeZone.getDefault());
        smsTime.setTime(inputDate);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "MMMM d, hh:mm aa";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return "" + android.text.format.DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return context.getString(R.string.yesterday);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return android.text.format.DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return android.text.format.DateFormat.format("MMMM/dd/yyyy", smsTime).toString();
        }
    }

    public static String getChatTime(Context context, String date) {

      /*  String languageToLoad  = "en"; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());*/
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeZone(TimeZone.getDefault());
        smsTime.setTimeInMillis(getTimeFromUTC(context, date));
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm", Locale.ENGLISH);
        SimpleDateFormat hourFormat = new SimpleDateFormat("aa",Locale.ENGLISH);
        String time = timeFormat.format(new Date(smsTime.getTimeInMillis())) + " " + hourFormat.format(new Date(smsTime.getTimeInMillis()));
        return time;
    }

    public static String getChatDate(Context context, String date) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeZone(TimeZone.getDefault());
        smsTime.setTimeInMillis(getTimeFromUTC(context, date));
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", LocaleManager.getLocale(context.getResources()));
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd yyyy", Locale.ENGLISH);
        return monthFormat.format(new Date(smsTime.getTimeInMillis())) + " " + dayFormat.format(new Date(smsTime.getTimeInMillis()));
    }


    public static long utcToMillis(String utc) {
        SimpleDateFormat format = new SimpleDateFormat(UTC_DATE_PATTERN, Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date;
        try {
            date = format.parse(utc);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getTimeAgo(Context context, String utcTime) {

        Date inputDate = new Date();
        if (utcTime != null && !utcTime.isEmpty()) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(UTC_DATE_PATTERN);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                inputDate = simpleDateFormat.parse(utcTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        Calendar smsTime = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        smsTime.setTimeZone(TimeZone.getDefault());
        smsTime.setTime(inputDate);

        long now = System.currentTimeMillis();
        if (calendar.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm", Locale.ENGLISH);
            SimpleDateFormat timeFormat = new SimpleDateFormat("a", Locale.ENGLISH);
            String timesAgo = dateFormat.format(smsTime.getTimeInMillis()) + " " + timeFormat.format(smsTime.getTimeInMillis());
            return timesAgo;
        } else if (calendar.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return context.getString(R.string.yesterday);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            return dateFormat.format(smsTime.getTimeInMillis());
        }
    }

    public static String convertToSeconds(String getTime) {
        String[] units = getTime.split(":"); //will break the string up into an array
        int minutes = Integer.parseInt(units[0]); //first element
        int seconds = Integer.parseInt(units[1]); //second element
        int duration = 60 * minutes + seconds; //add up our values
        return String.valueOf(duration);
    }

    public static String formatWord(String word) {
        if (word != null && !word.isEmpty()) {
            word = String.valueOf(word.charAt(0)).toUpperCase() + word.subSequence(1, word.length());
            return word;
        }
        return "";
    }

    public static String getCurrentStatus() {
        return currentStatus;
    }

    public static void setCurrentStatus(String currentStatus) {
        AppUtils.currentStatus = currentStatus;
    }

    public void showKeyboard(EditText editText, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.showSoftInput(editText, 0);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void resetFilter() {
//        HomeFragment.filterApplied = false;
        GetSet.setFilterApplied(false);
        GetSet.setLocationApplied(false);
    }

    // Showing network status in Snackbar
    public static void showSnack(final Context context, View view, boolean isConnected) {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
        if (snackbar == null) {
            snackbar = Snackbar
                    .make(view, context.getString(R.string.no_internet_connection), Snackbar.LENGTH_INDEFINITE)
                    .setAction(context.getString(R.string.settings).toUpperCase(), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                            context.startActivity(intent);
                        }
                    })
                    .setActionTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
        }

        if (isConnected) {
            snackbar.dismiss();
            snackbar = null;
        } else {
            if (!snackbar.isShown()) {
                snackbar.show();
            }
        }
    }

    public static void dismissSnack() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
            snackbar = null;
        }
    }

    public Task<ShortDynamicLink> getDynamicLink() {

        String url = Constants.APP_SHARE_URL + "/" + GetSet.getReferalLink();
        Log.d(TAG, "getDynamicLink: "+url);
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(url))
                .setDomainUriPrefix("https://" + context.getString(R.string.dynamic_link_filter) + "/")
                .setIosParameters(new DynamicLink.IosParameters.Builder(context.getString(R.string.ios_bundle_id))
                        .setAppStoreId(context.getString(R.string.ios_app_store_id))
                        .setCustomScheme(context.getString(R.string.ios_bundle_id))
                        .build())
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder(BuildConfig.APPLICATION_ID)
                        .build())
                .buildShortDynamicLink();

        return shortLinkTask;
    }

    public static Task<ShortDynamicLink> getStreamShareLink(Context context, String shareLink) {

        String url = Constants.APP_SHARE_URL + "/" + shareLink;
        Log.i(TAG, "getStreamShareLink: " + url);
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(url))
                .setDomainUriPrefix("https://" + context.getString(R.string.dynamic_link_filter) + "/")
                .setIosParameters(new DynamicLink.IosParameters.Builder(context.getString(R.string.ios_bundle_id))
                        .setAppStoreId(context.getString(R.string.ios_app_store_id))
                        .setCustomScheme(context.getString(R.string.ios_bundle_id))
                        .build())
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder(BuildConfig.APPLICATION_ID)
                        .build())
                .buildShortDynamicLink();

        return shortLinkTask;
    }

 /*   public Task<ShortDynamicLink> getStreamShareLink(String videoId,String strem_id) {
//        String url = Constants.APP_SHARE_URL + "/videos?stream_id=" + videoId;
        String url = Constants.APP_SHARE_URL + strem_id + videoId;
        Log.d(TAG, "getStreamShareLink: "+url);
        Task<ShortDynamicLink> shortLinkTask = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(url))
                .setDomainUriPrefix("https://" + context.getString(R.string.dynamic_link_filter) + "/")
                *//* .setIosParameters(new DynamicLink.IosParameters.Builder(mContext.getString(R.string.ios_bundle_id))
                         .setAppStoreId(mContext.getString(R.string.ios_app_store_id))
                         .setCustomScheme(mContext.getString(R.string.ios_bundle_id))
                         .build())*//*
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder(BuildConfig.APPLICATION_ID)
                        .build())
                .buildShortDynamicLink();

        return shortLinkTask;
    }*/

    public Task<ShortDynamicLink> getStreamShareLink(String videoId, String stream_id_path) {
        String url = "https://service.alhawanajd.com" + stream_id_path + videoId;

        Log.d("TAG", "getStreamShareLink: " + url);

        return FirebaseDynamicLinks.getInstance()
                .createDynamicLink()
                .setLink(Uri.parse(url)) // Must be allowlisted in Firebase Console
                .setDomainUriPrefix("https://alhawanajdd.page.link/") // Replace with your Firebase domain
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder(BuildConfig.APPLICATION_ID).build())
                .buildShortDynamicLink();
    }


    public static String encryptMessage(String message) {
        String encryptedMsg = "";
        try {
            CryptLib cryptLib = new CryptLib();
            encryptedMsg = cryptLib.encryptPlainTextWithRandomIV(message, Constants.MESSAGE_CRYPT_KEY);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedMsg;
    }

    public static String decryptMessage(String message) {
        try {
            CryptLib cryptLib = new CryptLib();
            message = cryptLib.decryptCipherTextWithRandomIV(message, Constants.MESSAGE_CRYPT_KEY);
        } catch (NoSuchAlgorithmException e) {
            Timber.e("decryptMessage: %s", e.getMessage());
//            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            Timber.e("decryptMessage: %s", e.getMessage());
//            e.printStackTrace();
        } catch (Exception e) {
            Timber.e("decryptMessage: %s", e.getMessage());
//            e.printStackTrace();
        }
        return message;
    }

    public static String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    public static String twoDigitString(long number) {
        if (number == 0) {
            return "00";
        } else if (number / 10 == 0) {
            return "0" + number;
        }
        return String.valueOf(number);
    }

    public static void startAlphaAnimation(View v, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(ALPHA_ANIMATIONS_DURATION);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    public String getFormattedDuration(long currentMilliSeconds) {
        String duration;
        if (TimeUnit.MILLISECONDS.toHours(currentMilliSeconds) != 0)
            duration = String.format(Locale.ENGLISH, "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(currentMilliSeconds),
                    TimeUnit.MILLISECONDS.toMinutes(currentMilliSeconds) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(currentMilliSeconds) % TimeUnit.MINUTES.toSeconds(1));
        else
            duration = String.format(Locale.ENGLISH, "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(currentMilliSeconds) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(currentMilliSeconds) % TimeUnit.MINUTES.toSeconds(1));
        return duration;
    }

    public String getCommentsDuration(long currentMilliSeconds) {
        String duration;
        duration = String.format(Locale.ENGLISH, "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(currentMilliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(currentMilliSeconds) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(currentMilliSeconds) % TimeUnit.MINUTES.toSeconds(1));
        return duration;
    }

    /*public String getFormattedDuration(long currentMilliSeconds) {
        String duration;
        SimpleDateFormat simpleDateFormat;
        if (TimeUnit.valueOf().toHours(currentMilliSeconds) != 0) {
            simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        } else {
            simpleDateFormat = new SimpleDateFormat("mm:ss", Locale.ENGLISH);
        }
        duration = simpleDateFormat.format(new Date(currentMilliSeconds));
        return duration;
    }

    public String getCommentsDuration(long currentMilliSeconds) {
        String duration;
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        duration = simpleDateFormat.format(new Date(currentMilliSeconds));
        return duration;
    }*/

    public void pauseExternalAudio() {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (manager.isMusicActive()) {
            isExternalPlay = true;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            context.sendBroadcast(i);
        }
    }

    public void resumeExternalAudio() {
        if (isExternalPlay) {
            isExternalPlay = false;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "play");
            context.sendBroadcast(i);
        }
    }

    public boolean checkIsDeviceEnabled(Context context) {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (km.isKeyguardSecure() && BiometricManager.from(context).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                return true;
            } else return km.isKeyguardSecure();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if we're running on Android 6.0 (M) or higher
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);
            if (fingerprintManager != null) {
                if (fingerprintManager.isHardwareDetected()) {
                    if (fingerprintManager.hasEnrolledFingerprints()) {
                        return true;
                    } else
                        return km.isKeyguardSecure();
                } else {
                    return km.isKeyguardSecure();
                }
            } else return km.isKeyguardSecure();
        } else return km.isKeyguardSecure();
    }

    public static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    public void setHashTagItemPadding(HashtagView hashtagView) {
        hashtagView.setItemPadding(dpToPx(context, 15), dpToPx(context, 15), dpToPx(context, 5), dpToPx(context, 5));
    }

    public List<GradientDrawable> getCountryBGList() {
        Random rnd = new Random();
        List<GradientDrawable> countryBgList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (i == 0) {
                int topColor = Color.rgb(81, 9, 163);
                int midColor = Color.rgb(113, 32, 209);
                int bottomColor = Color.rgb(142, 52, 250);
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{topColor, midColor, bottomColor});
                countryBgList.add(gd);
            } else if (i == 1) {
                int topColor = Color.rgb(128, 16, 252);
                int midColor = Color.rgb(204, 61, 195);
                int bottomColor = Color.rgb(254, 78, 152);
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{topColor, midColor, bottomColor});
                countryBgList.add(gd);
            } else if (i == 2) {
                int topColor = Color.rgb(236, 3, 139);
                int midColor = Color.rgb(244, 54, 120);
                int bottomColor = Color.rgb(254, 185, 106);
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{topColor, midColor, bottomColor});
                countryBgList.add(gd);
            } else if (i == 3) {
                int topColor = Color.rgb(176, 40, 175);
                int midColor = Color.rgb(210, 57, 187);
                int bottomColor = Color.rgb(248, 77, 202);
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{topColor, midColor, bottomColor});
                countryBgList.add(gd);
            } else if (i == 4) {
                int topColor = Color.rgb(28, 184, 184);
                int midColor = Color.rgb(132, 219, 147);
                int bottomColor = Color.rgb(234, 253, 111);
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{topColor, midColor, bottomColor});
                countryBgList.add(gd);
            } else if (i == 5) {
                int topColor = Color.rgb(28, 184, 184);
                int midColor = Color.rgb(106, 48, 142);
                int midColor2 = Color.rgb(77, 37, 129);
                int bottomColor = Color.rgb(233, 30, 122);
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{topColor, midColor, midColor2, bottomColor});
                countryBgList.add(gd);
            } else if (i == 6) {
                int topColor = Color.rgb(106, 48, 142);
                int midColor = Color.rgb(236, 70, 197);
                int midColor2 = Color.rgb(158, 132, 254);
                int bottomColor = Color.rgb(38, 233, 241);
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{topColor, midColor, midColor2, bottomColor});
                countryBgList.add(gd);
            } else if (i == 7) {
                int topColor = Color.rgb(46, 203, 116);
                int midColor = Color.rgb(123, 216, 150);
                int bottomColor = Color.rgb(231, 252, 112);
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{topColor, midColor, bottomColor});
                countryBgList.add(gd);
            } else if (i == 8) {
                int topColor = Color.rgb(253, 170, 110);
                int midColor = Color.rgb(254, 194, 103);
                int bottomColor = Color.rgb(255, 218, 97);
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{topColor, midColor, bottomColor});
                countryBgList.add(gd);
            } else {
                int topColor = Color.rgb(255, 100, 153);
                int midColor = Color.rgb(255, 136, 136);
                int bottomColor = Color.rgb(255, 212, 98);
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{topColor, midColor, bottomColor});
                countryBgList.add(gd);
            }
        }
        return countryBgList;
    }

    public static String formateMilliSeccond(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public static String formateMilliSecconds(long milliseconds) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        // return seconds
        return String.valueOf(seconds);
    }
}
