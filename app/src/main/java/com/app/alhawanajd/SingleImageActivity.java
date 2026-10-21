package com.app.alhawanajd;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.app.alhawanajd.R;
import com.app.external.TouchImageView;

import timber.log.Timber;

public class SingleImageActivity extends BaseFragmentActivity {

    TouchImageView imgDisplay;
    ImageView close_button;
    String imageUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_image);


        imgDisplay = findViewById(R.id.imgDisplay);
        close_button = findViewById(R.id.close_button);
        imageUrl = getIntent().getStringExtra("image");

        Timber.d("onCreate: imageURL %s", imageUrl);

        Glide.with(SingleImageActivity.this).load(imageUrl)
                .placeholder(R.drawable.default_profile_image)
                .into(imgDisplay);

        close_button.setOnClickListener(v -> {
            finish();
            closeActivity();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeActivity();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    public void closeActivity() {
        overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
    }
}
