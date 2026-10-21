package com.app.Zego.components.cohost;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.app.Zego.internal.ZEGOLiveStreamingManager;
import com.app.Zego.internal.ZEGOLiveStreamingManager.LiveStreamingListener;
import com.app.Zego.internal.business.pk.PKExtendedData;
import com.app.Zego.internal.business.pk.PKService.PKBattleInfo;
import com.app.Zego.internal.business.pk.PKUser;
import com.app.Zego.internal.sdk.ZEGOSDKManager;
import com.app.Zego.internal.sdk.basic.ZEGOSDKUser;
import com.app.Zego.internal.sdk.express.IExpressEngineEventHandler;
import com.app.Zego.internal.utils.ToastUtil;
import com.app.alhawanajd.R;
import com.app.alhawanajd.databinding.LayoutPkBattleBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import im.zego.zegoexpress.callback.IZegoMixerStartCallback;
import im.zego.zim.entity.ZIMCallInvitationCancelledInfo;
import im.zego.zim.entity.ZIMCallInvitationReceivedInfo;
import im.zego.zim.entity.ZIMCallInvitationTimeoutInfo;

/**
 * a mix layout in the bottom ,used when audience in room to pull host videos.
 * some cells in the top,used when
 * 1. live host to pull other host videos.
 * 2. host and audience to show status views or decorate views.
 *
 */
public class PKBattleLayout extends FrameLayout {

    public PKBattleLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public PKBattleLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PKBattleLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public PKBattleLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private LayoutPkBattleBinding binding;
    private AlertDialog startPKDialog;
    private Map<String, Boolean> timeoutPKUsers = new ConcurrentHashMap<>();

    private static final String TAG = "PKBattleLayout";


    private void initView() {
        binding = LayoutPkBattleBinding.inflate(LayoutInflater.from(getContext()), this, true);

        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onCameraOpen(String userID, boolean open) {
                PKBattleInfo pkInfo = ZEGOLiveStreamingManager.getInstance().getPKBattleInfo();
                if (pkInfo != null) {
                    for (PKUser pkUser : pkInfo.pkUserList) {
                        if (Objects.equals(userID, pkUser.userID)) {
                            pkUser.setCamera(open);
                            int childCount = binding.pkBattleUserLayout.getChildCount();
                            for (int i = 0; i < childCount; i++) {
                                PKBattleView pkBattleView = (PKBattleView) binding.pkBattleUserLayout.getChildAt(i);
                                if (Objects.equals(pkBattleView.getPkUser().userID, userID)) {
                                    pkBattleView.onCameraUpdate(open);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onMicrophoneOpen(String userID, boolean open) {
                PKBattleInfo pkInfo = ZEGOLiveStreamingManager.getInstance().getPKBattleInfo();
                if (pkInfo != null) {
                    for (PKUser pkUser : pkInfo.pkUserList) {
                        if (Objects.equals(userID, pkUser.userID)) {
                            pkUser.setMicrophone(open);
                            int childCount = binding.pkBattleUserLayout.getChildCount();
                            for (int i = 0; i < childCount; i++) {
                                PKBattleView pkBattleView = (PKBattleView) binding.pkBattleUserLayout.getChildAt(i);
                                if (Objects.equals(pkBattleView.getPkUser().userID, userID)) {
                                    pkBattleView.onMicrophoneUpdate(open);
                                }
                            }
                        }
                    }
                }
            }
        });
        ZEGOLiveStreamingManager.getInstance().addLiveStreamingListener(new LiveStreamingListener() {

            @Override
            public void onRoleChanged(String userID, int after) {
                if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
                    // host pull every single stream of other pk hosts
                    binding.pkBattleVideoMixLayout.setVisibility(GONE);
                } else {
                    // audience only pull mix stream
                    binding.pkBattleVideoMixLayout.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onPKBattleReceived(String requestID, ZIMCallInvitationReceivedInfo info) {
                android.util.Log.e("PKBattleLayout", "onPKBattleReceived: requestID = " + requestID);
                post(new Runnable() {
                    @Override
                    public void run() {
                        android.util.Log.e("PKBattleLayout", "onPKBattleReceived inside postRunnable: requestID = " + requestID);
                        PKExtendedData inviterExtendedData = PKExtendedData.parse(info.extendedData);
                        if (inviterExtendedData != null) {
                            android.util.Log.e("PKBattleLayout", "onPKBattleReceived parsed data: userName = " + inviterExtendedData.userName + ", autoAccept = " + inviterExtendedData.autoAccept);
                            if (!inviterExtendedData.autoAccept) {
                                if (startPKDialog != null && startPKDialog.isShowing()) {
                                    android.util.Log.e("PKBattleLayout", "onPKBattleReceived: startPKDialog is already showing, return");
                                    return;
                                }

                                android.util.Log.e("PKBattleLayout", "onPKBattleReceived: Creating and showing AlertDialog");
                                AlertDialog.Builder startPKBuilder = new AlertDialog.Builder(getContext());
                                startPKBuilder.setTitle(inviterExtendedData.userName + " invite you pkbattle");
                                startPKBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        ZEGOLiveStreamingManager.getInstance().acceptPKBattle(requestID);
                                    }
                                });
                                startPKBuilder.setNegativeButton(R.string.reject, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        ZEGOLiveStreamingManager.getInstance().rejectPKBattle(requestID);
                                    }
                                });
                                startPKDialog = startPKBuilder.create();
                                startPKDialog.setCanceledOnTouchOutside(false);
                                startPKDialog.show();
                            } else {
                                ZEGOLiveStreamingManager.getInstance().acceptPKBattle(requestID);
                            }
                        }
                    }
                });
            }

            @Override
            public void onInComingPKBattleTimeout(String requestID, ZIMCallInvitationTimeoutInfo info) {
                if (startPKDialog != null && startPKDialog.isShowing()) {
                    startPKDialog.dismiss();
                }
                ToastUtil.show(getContext(), "received pk battle,but no answer time out");
            }


            @Override
            public void onPKBattleCancelled(String requestID, ZIMCallInvitationCancelledInfo info) {
                if (startPKDialog != null && startPKDialog.isShowing()) {
                    startPKDialog.dismiss();
                }
                PKBattleInfo pkInfo = ZEGOLiveStreamingManager.getInstance().getPKBattleInfo();
                for (PKUser pkUser : pkInfo.pkUserList) {
                    if (Objects.equals(pkUser.userID, info.inviter)) {
                        ToastUtil.show(getContext(), pkUser.userName + " cancelled the pk battle");
                        break;
                    }
                }
            }

            @Override
            public void onPKBattleAccepted(String userID, String extendedData) {
                PKBattleInfo pkInfo = ZEGOLiveStreamingManager.getInstance().getPKBattleInfo();
                ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                for (PKUser pkUser : pkInfo.pkUserList) {
                    if (Objects.equals(pkUser.userID, userID) && !Objects.equals(currentUser.userID, userID)) {
                        ToastUtil.show(getContext(), pkUser.userName + " accept the pk battle");
                        break;
                    }
                }
            }

            @Override
            public void onPKMixStreamError(int errorCode, String data) {
                ToastUtil.show(getContext(), "mix stream error,errorCode:" + errorCode + "," + data);
            }

            @Override
            public void onPKBattleRejected(String userID, String extendedData) {
                PKBattleInfo pkInfo = ZEGOLiveStreamingManager.getInstance().getPKBattleInfo();

                for (PKUser pkUser : pkInfo.pkUserList) {
                    if (Objects.equals(pkUser.userID, userID)) {
                        String tips = pkUser.userName + " reject the pk battle";
                        try {
                            JSONObject jsonObject = new JSONObject(extendedData);
                            if (jsonObject.has("reason")) {
                                String reason = jsonObject.getString("reason");
                                if (!TextUtils.isEmpty(extendedData)) {
                                    PKExtendedData userData = PKExtendedData.parse(extendedData);
                                    if (userData != null) {
                                        pkUser.userName = userData.userName;
                                        pkUser.roomID = userData.roomID;
                                    }
                                }
                                if ("room".equals(reason)) {
                                    tips = pkUser.userName + " is not in any room";
                                } else if ("host".equals(reason)) {
                                    tips = pkUser.userName + " is not host";
                                } else if ("busy".equals(reason)) {
                                    tips = pkUser.userName + " is busy";
                                }
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        ToastUtil.show(getContext(), tips);
                        break;
                    }
                }
            }

            @Override
            public void onOutgoingPKBattleTimeout(String userID, String extendedData) {
                ToastUtil.show(getContext(), "no host answered the pk battle");
            }


            @Override
            public void onPKStarted() {
                onRoomPKStarted();
            }

            @Override
            public void onPKUserJoin(String userID, String extendedData) {
                updateAllPKUsers();
            }

            @Override
            public void onPKEnded() {
                onRoomPKEnded();
            }

            @Override
            public void onPKUserConnecting(String userID, long duration) {
                boolean timeout = duration > 5000;
                boolean stateChanged = false;
                if (timeoutPKUsers.containsKey(userID)) {
                    boolean lastState = timeoutPKUsers.get(userID);
                    stateChanged = (timeout != lastState);
                }
                timeoutPKUsers.put(userID, timeout);
                if (!stateChanged) {
                    return;
                }
                int childCount = binding.pkBattleUserLayout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    PKBattleView pkBattleView = (PKBattleView) binding.pkBattleUserLayout.getChildAt(i);
                    if (Objects.equals(pkBattleView.getPkUser().userID, userID)) {
                        pkBattleView.onTimeOut(timeout);
                        if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
                            if (timeout) {
                                boolean pkUserMuted = ZEGOLiveStreamingManager.getInstance().isPKUserMuted(userID);
                                if (!pkUserMuted) {
                                    ZEGOLiveStreamingManager.getInstance()
                                        .mutePKUser(Collections.singletonList(userID), true,
                                            new IZegoMixerStartCallback() {
                                                @Override
                                                public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                                                    if (errorCode == 0) {
                                                        pkBattleView.mutePlayAudio(true);
                                                    }
                                                }
                                            });
                                }
                            } else {
                                boolean pkUserMuted = ZEGOLiveStreamingManager.getInstance().isPKUserMuted(userID);
                                if (pkUserMuted) {
                                    ZEGOLiveStreamingManager.getInstance()
                                        .mutePKUser(Collections.singletonList(userID), false,
                                            new IZegoMixerStartCallback() {
                                                @Override
                                                public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                                                    if (errorCode == 0) {
                                                        pkBattleView.mutePlayAudio(false);
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onPKUserCameraOpen(String userID, boolean open) {
                int childCount = binding.pkBattleUserLayout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    PKBattleView pkBattleView = (PKBattleView) binding.pkBattleUserLayout.getChildAt(i);
                    if (Objects.equals(pkBattleView.getPkUser().userID, userID)) {
                        pkBattleView.onCameraUpdate(open);
                    }
                }
            }

            @Override
            public void onPKUserQuit(String userID, String extendedData) {
                PKBattleInfo pkInfo = ZEGOLiveStreamingManager.getInstance().getPKBattleInfo();

                int childCount = binding.pkBattleUserLayout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    PKBattleView pkBattleView = (PKBattleView) binding.pkBattleUserLayout.getChildAt(i);
                    PKUser pkUser = pkBattleView.getPkUser();
                    if (Objects.equals(pkUser.userID, userID)) {
                        pkBattleView.setPKUser(null, binding.pkBattleUserLayout);
                        ToastUtil.show(getContext(), pkUser.userName + " has quited the pk battle");
                        break;
                    }
                }
                binding.pkBattleUserLayout.removeAllViews();

                for (PKUser pkUser : pkInfo.pkUserList) {
                    if (pkUser.hasAccepted()) {
                        PKBattleView pkBattleView = new PKBattleView(getContext());
                        pkBattleView.setPKUser(pkUser, binding.pkBattleUserLayout);
                        binding.pkBattleUserLayout.addView(pkBattleView);
                    }
                }
                timeoutPKUsers.remove(userID);
            }

            @Override
            public void onPKUserUpdate() {
                updateAllPKUsers();
            }
        });
    }

    private void updateAllPKUsers() {
        PKBattleInfo pkInfo = ZEGOLiveStreamingManager.getInstance().getPKBattleInfo();

        binding.pkBattleUserLayout.removeAllViews();
        for (PKUser pkUser : pkInfo.pkUserList) {
            if (pkUser.hasAccepted()) {
                PKBattleView pkBattleView = new PKBattleView(getContext());
                pkBattleView.setPKUser(pkUser, binding.pkBattleUserLayout);
                binding.pkBattleUserLayout.addView(pkBattleView);
            }
        }
    }

    /**
     * a video view background to show mix-steam when is audience a frameLayout to show cell view of pk battle. contains
     * a video view in each cell when is host
     */
    private void onRoomPKStarted() {
        boolean isCurrentUserHost = ZEGOLiveStreamingManager.getInstance().isCurrentUserHost();

        if (!isCurrentUserHost) {
            String mixStreamID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID() + "_mix";
            binding.pkBattleVideoMixLayout.setStreamID(mixStreamID);
            binding.pkBattleVideoMixLayout.startPlayRemoteAudioVideo();
        }
    }

    private void onRoomPKEnded() {
        if (startPKDialog != null && startPKDialog.isShowing()) {
            startPKDialog.dismiss();
        }
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();

        int childCount = binding.pkBattleUserLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            PKBattleView pkBattleView = (PKBattleView) binding.pkBattleUserLayout.getChildAt(i);
            if (!Objects.equals(pkBattleView.getPkUser().userID, currentUser.userID)) {
                pkBattleView.setPKUser(null, binding.pkBattleUserLayout);
            }
        }
        binding.pkBattleUserLayout.removeAllViews();
    }

    public void checkPKState() {
        PKBattleInfo pkInfo = ZEGOLiveStreamingManager.getInstance().getPKBattleInfo();
        android.util.Log.e("PKBattleLayout", "checkPKState called, pkInfo = " + pkInfo);
        if (pkInfo != null) {
            updateAllPKUsers();
            onRoomPKStarted();
        }
    }
}
