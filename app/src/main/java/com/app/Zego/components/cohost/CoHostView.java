package com.app.Zego.components.cohost;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.Zego.Adapter.VideoViewAdapter;
import com.app.Zego.CallBacks.onCoHostClicked;
import com.app.Zego.internal.sdk.basic.ZEGOSDKUser;
import com.app.addon.livestream.external.model.StreamDetails;

import java.util.List;

public class CoHostView extends RecyclerView {
    private static final String TAG = "CoHostView";
   // private CoHostAdapter coHostAdapter;
    private VideoViewAdapter coHostAdapter;
    onCoHostClicked onCoHostClick;

    public CoHostView(@NonNull Context context) {
        super(context);
       // initView();
        initView(context);
    }

    public CoHostView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //initView();
        initView(context);
    }

    public CoHostView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //initView();
        initView(context);
    }

    /*private void initView() {
        coHostAdapter = new CoHostAdapter();
        setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter(coHostAdapter);

        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onCameraOpen(String userID, boolean open) {
                if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
                    coHostAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onMicrophoneOpen(String userID, boolean open) {
                if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
                    coHostAdapter.notifyDataSetChanged();
                }
            }
        });
    }*/

    private void initView(Context context) {
        coHostAdapter = new VideoViewAdapter(context, null); // temporarily pass null, assign listener later
        setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter(coHostAdapter);
        setNestedScrollingEnabled(false);
    }
   /* public void addUser(List<ZEGOSDKUser> coHostUserList) {
        coHostAdapter.addUser(coHostUserList);
    }

    public void addUser(ZEGOSDKUser user) {
        coHostAdapter.addUser(user);
    }

    public void removeUser(List<ZEGOSDKUser> coHostUserList) {
        coHostAdapter.removeUser(coHostUserList);
    }

    public void removeUser(ZEGOSDKUser user) {
        coHostAdapter.removeUser(user);
    }*/

    public void addUser(List<ZEGOSDKUser> coHostUserList, List<StreamDetails.StreamCohost> gemList) {
        Log.d(TAG, "addUser() called with: coHostUserList = [" + coHostUserList + "]");
        coHostAdapter.removeUser(coHostUserList);
        coHostAdapter.addUser(coHostUserList,gemList);
    }

    public void addUser(ZEGOSDKUser user, List<StreamDetails.StreamCohost> gemDetails) {
        Log.d(TAG, "addUser() called with: user = [" + user + "]");
        coHostAdapter.removeUser(user);
        coHostAdapter.addUser(user,gemDetails);
    }

    public void removeUser(List<ZEGOSDKUser> coHostUserList) {
        coHostAdapter.removeUser(coHostUserList);
    }

    public void removeUser(ZEGOSDKUser user) {
        coHostAdapter.removeUser(user);
    }

    public void updateGemList(List<StreamDetails.StreamCohost> gemDetails) {
        Log.d(TAG, "updateGemList() called"+ gemDetails.toString());
        coHostAdapter.updateGems(gemDetails);
    }


    public void updateList() {
        Log.d(TAG, "updateList() called");
        coHostAdapter.notifyDataSetChanged();
    }

    public void updateCamera(String userId,boolean open){
        Log.e(TAG, "updateCamera: ::::::"+userId+"::::: "+open );
        coHostAdapter.cameraUpdate(userId,open);
    }

    public void updateMicrophone(String userId, boolean open){
        Log.e(TAG, "updateMicrophone: ::::::"+userId+"::::: "+open );
        coHostAdapter.microphoneUpdate(userId, open);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        return false;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return false;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return false;
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return false;
    }

    public void setOnCoHostClickListener(onCoHostClicked listener) {
        this.onCoHostClick = listener;
        if (coHostAdapter != null) {
            coHostAdapter.setOnCoHostClickListener(listener);
        }
    }
}
