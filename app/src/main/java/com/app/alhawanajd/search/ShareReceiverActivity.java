package com.app.alhawanajd.search;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.app.alhawanajd.CameraActivity;

public class ShareReceiverActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        Intent incoming = getIntent();
        if (incoming != null && Intent.ACTION_SEND.equals(incoming.getAction()) &&
                incoming.getType() != null && incoming.getType().startsWith("video/")) {

            Uri shared = incoming.getParcelableExtra(Intent.EXTRA_STREAM);
            if (shared == null) shared = incoming.getData();

            if (shared != null) {
                Intent forward = new Intent(this, CameraActivity.class);
                forward.putExtra("shared_video_uri", shared);
                forward.putExtra("launched_from_share", true);
                forward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                forward.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                // copy incoming extras if you want: forward.putExtras(incoming.getExtras());
                startActivity(forward);
            }
        }
        finish(); // invisible, just forwards
    }
}

