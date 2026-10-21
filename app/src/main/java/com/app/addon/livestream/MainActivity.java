package com.app.addon.livestream;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

//import com.app.hiliteapp.R;
import com.app.addon.livestream.broadcaster.liveVideoBroadcaster.PublishActivity;
import com.app.addon.livestream.liveVideoPlayer.SubscribeActivity;
import com.app.alhawanajd.R;


public class MainActivity extends AppCompatActivity {

    /**
     * PLEASE WRITE RTMP BASE URL of the your RTMP SERVER.
     */

//    public static final String RTMP_BASE_URL = "rtmp://media.hitasoft.in/LiveApp/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openVideoBroadcaster(View view) {
        Intent i = new Intent(this, PublishActivity.class);
        startActivity(i);
    }

    public void openVideoPlayer(View view) {
        Intent i = new Intent(this, SubscribeActivity.class);
        startActivity(i);
    }
}
