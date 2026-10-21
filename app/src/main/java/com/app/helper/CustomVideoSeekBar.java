package com.app.helper;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.ContextCompat;

import com.app.alhawanajd.R;

public class CustomVideoSeekBar extends AppCompatSeekBar {
    private boolean isTracking = false;
    private OnSeekBarChangeListener externalListener;

    public CustomVideoSeekBar(Context context) {
        super(context);
        init();
    }

    public CustomVideoSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomVideoSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Hide thumb by default
        setThumb(null);

        // Set up internal touch listener
        super.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Call external listener first
                if (externalListener != null) {
                    externalListener.onProgressChanged(seekBar, progress, fromUser);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTracking = true;
                // Show custom thumb when tracking starts
                setThumb(ContextCompat.getDrawable(getContext(), R.drawable.custom_seekbar_thumb));

                // Call external listener
                if (externalListener != null) {
                    externalListener.onStartTrackingTouch(seekBar);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTracking = false;
                // Hide thumb when tracking stops
                setThumb(null);

                // Call external listener
                if (externalListener != null) {
                    externalListener.onStopTrackingTouch(seekBar);
                }
            }
        });
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        // Store the external listener instead of setting it directly
        this.externalListener = listener;
    }

    public boolean isTracking() {
        return isTracking;
    }
}
