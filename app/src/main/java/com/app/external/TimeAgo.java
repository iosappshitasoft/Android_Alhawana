package com.app.external;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;

import com.app.alhawanajd.R;
import com.app.addon.livestream.external.helper.LocaleManager;

import java.util.Date;

/**
 * Created by hitasoft on 9/1/17.
 */

public class TimeAgo {
    protected Context context;

    public TimeAgo(Context context) {
        this.context = context;
    }

    public String timeAgo(Date date) {
        return timeAgo(date.getTime());
    }

    @SuppressLint("StringFormatInvalid")
    public String timeAgo(long millis) {
        long diff = new Date().getTime() - millis;
        Resources r = context.getResources();
        //String suffix = r.getString(R.string.time_ago_suffix);
        double seconds = Math.abs(diff) / 1000;
        double minutes = seconds / 60;
        double hours = minutes / 60;
        double days = hours / 24;
        double years = days / 365;
        String words;
        if (seconds < 10) {
            if (LocaleManager.isRTL()) {
                words = r.getString(R.string.time_just_now_new, Math.round(seconds));
            }else{
                words = r.getString(R.string.time_just_now, Math.round(seconds));
            }

        } else if (seconds < 60) {
            words = r.getString(R.string.time_ago_seconds, Math.round(seconds));
        } else if (seconds < 90) {
            words = r.getString(R.string.time_ago_minute, 1);
        } else if (minutes < 45) {
            words = r.getString(R.string.time_ago_minutes, Math.round(minutes));
        } else if (minutes < 90) {
            words = r.getString(R.string.time_ago_hour, 1);
        } else if (hours < 24) {
            words = r.getString(R.string.time_ago_hours, Math.round(hours));
        } else if (hours < 42) {
            words = r.getString(R.string.time_ago_day, 1);
        } else if (days < 30) {
            words = r.getString(R.string.time_ago_days, Math.round(days));
        } else if (days < 45) {
            words = r.getString(R.string.time_ago_month, 1);
        } else if (days < 365) {
            words = r.getString(R.string.time_ago_months, Math.round(days / 30));
        } else if (years < 1.5) {
            words = r.getString(R.string.time_ago_year, 1);
        } else {
            words = r.getString(R.string.time_ago_years, Math.round(years));
        }
        /*StringBuilder sb = new StringBuilder();
        sb.append(words);*/
       /* if (suffix.length() > 0 && !words.equals(r.getString(R.string.time_ago_seconds))) {
            sb.append(" ").append(suffix);
        }*/
        return words.trim();
    }
}
