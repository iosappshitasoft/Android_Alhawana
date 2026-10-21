package com.app.addon.livestream.external.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.app.alhawanajd.R;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


import static android.content.Context.MODE_PRIVATE;


public class Utils {

    private static final String TAG = "Utils";

    private static Context context;
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    public static final String UTC_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public Utils(Context context) {
        this.context = context;
        pref = context.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
    }

    public static String getURLForResource(int resourceId) {
        return Uri.parse("android.resource://com.app.hiddy/" + resourceId).toString();
    }

    public static String getFormattedDate(Context context, long smsTimeUnixTimestamp) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeUnixTimestamp * 1000L);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEE, MMM d";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return getDate(timeFormatString, smsTime.getTimeInMillis(), null);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return context.getString(R.string.yesterday);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return getDate(dateTimeFormatString, smsTime.getTimeInMillis(), null);
        } else {
            return getDate("MMM dd yyyy", smsTime.getTimeInMillis(), null);
        }
    }

    public static String getFormattedDate(Context context, long smsTimeUnixTimestamp, @Nullable String tz) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeUnixTimestamp * 1000L);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEE, MMM d h:mm aa";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return getDate(timeFormatString, smsTime.getTimeInMillis(), tz);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return context.getString(R.string.yesterday);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return getDate(dateTimeFormatString, smsTime.getTimeInMillis(), tz);
        } else {
            return getDate("MMM dd yyyy h:mm aa", smsTime.getTimeInMillis(), tz);
        }
    }

    private static String getDate(String format, long time, @Nullable String tz) {
        DateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        if (tz != null) sdf.setTimeZone(TimeZone.getTimeZone(tz));
        Date netDate = (new Date(time));
        return sdf.format(netDate);
    }

    public static String getCreatedFormatDate(Context context, long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis * 1000L);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEE, MMM d h:mm aa";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return String.valueOf(context.getString(R.string.today) + " " + getDate(timeFormatString, smsTime.getTimeInMillis(), null));
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return context.getString(R.string.yesterday);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return getDate(dateTimeFormatString, smsTime.getTimeInMillis(), null);
        } else {
            return getDate("MMM dd yyyy h:mm aa", smsTime.getTimeInMillis(), null);
        }
    }

    public byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
        int buffSize = 1024;
        byte[] buff = new byte[buffSize];
        int len = 0;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }
        return byteBuff.toByteArray();
    }

    public static String isNetworkConnected(Context context) {
        return NetworkUtil.getConnectivityStatusString(context);
    }

    public static void networkSnack(CoordinatorLayout mainLay, Context context) {
        Snackbar snackbar = Snackbar
                .make(mainLay, context.getString(R.string.no_network_connectivity), Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public static Spanned fromHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(html);
        }
    }

/*
    public static boolean isUserAdminInChannel(ChannelResult.Result channelData) {
        if (channelData.channelAdminId != null && channelData.channelAdminId.equalsIgnoreCase(GetSet.getUserId())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isChannelAdmin(ChannelResult.Result channelData, String userId) {
        return channelData.channelAdminId != null && channelData.channelAdminId.equalsIgnoreCase(userId);
    }

    public static boolean isProfileEnabled(ContactsData.Result result) {
        if (result.privacy_profile_image.equalsIgnoreCase(TAG_MY_CONTACTS)) {
            return result.contactstatus != null && result.contactstatus.equalsIgnoreCase(TRUE);
        } else return !result.privacy_profile_image.equalsIgnoreCase(TAG_NOBODY);
    }

    public static boolean isLastSeenEnabled(ContactsData.Result result) {
        if (result.privacy_last_seen.equalsIgnoreCase(TAG_MY_CONTACTS)) {
            return result.contactstatus != null && result.contactstatus.equalsIgnoreCase(TRUE);
        } else return !result.privacy_last_seen.equalsIgnoreCase(TAG_NOBODY);
    }

    public static boolean isAboutEnabled(ContactsData.Result result) {
        if (result.privacy_about.equalsIgnoreCase(TAG_MY_CONTACTS)) {
            return result.contactstatus != null && result.contactstatus.equalsIgnoreCase(TRUE);
        } else return !result.privacy_about.equalsIgnoreCase(TAG_NOBODY);
    }
*/

    static void refreshGallery(final String TAG, Context context, final File file) {

        try {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            final Uri contentUri = Uri.fromFile(file);
            scanIntent.setData(contentUri);
            context.sendBroadcast(scanIntent);
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Log.e(TAG, "Finished scanning " + file.getAbsolutePath());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public int getNavigationBarHeight() {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            int height = resources.getDimensionPixelSize(resourceId);
            editor.putInt(Constants.TAG_NAV_HEIGHT, height);
            editor.commit();
            return height;
        }
        return 0;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        editor.putInt(Constants.TAG_STATUS_HEIGHT, result);
        editor.commit();
        return result;
    }*/

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

    public static String getRecentDate(long smsTimeInMillis) {
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

    public static Long getTimeFromUTC(String date) {
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

    public static List<String> getReportList() {
        List<String> reportList = new ArrayList<>();
/*        reportList.add(context.getString(R.string.abuse));
        reportList.add(context.getString(R.string.in_appropriate));
        reportList.add(context.getString(R.string.adult_content));*/
        return reportList;
    }

    public static String formatNumberToAbbreviation(long n) {
      /*  String numberString = "";
        if (Math.abs(n / 1000000) > 1) {
            numberString = (n / 1000000) + "m";

        } else if (Math.abs(n / 1000) > 1) {
            numberString = (n / 1000) + "k";

        } else {
            numberString = n+"";

        }
        return numberString;*/
        double number = (double) n;
        String sign = (number < 0) ? "-" : "";
        double num = Math.abs(number);

        // If it's only three digits:
        if (num < 1000.0) {
            return String.format("%s%.0f", sign, num);
        }

        // Otherwise
        int exp = (int) (Math.log10(num) / 3.0);
        String[] units = {"K", "M", "B", "T", "P", "E"};

        double roundedNum = Math.round(10 * num / Math.pow(1000.0, exp)) / 10.0;

        return String.format("%s%.1f%s", sign, roundedNum, units[exp - 1]);
    }
}
