package com.app.Zego.components.cohost;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.app.Zego.activity.PkLiveStreamingActivity;
import com.app.Zego.components.LetterIconView;
import com.app.Zego.internal.ZEGOLiveStreamingManager;
import com.app.Zego.internal.business.pk.PKService;
import com.app.Zego.internal.business.pk.PKUser;
import com.app.Zego.internal.sdk.ZEGOSDKManager;
import com.app.Zego.internal.sdk.basic.ZEGOSDKUser;
import com.app.Zego.internal.sdk.components.express.ZEGOVideoView;
import com.app.alhawanajd.R;
import com.app.model.GetSet;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import im.zego.zegoexpress.callback.IZegoMixerStartCallback;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMUserFullInfo;

/**
 * cell view of each pk host,you can modify to add your custom widget
 */
public class PKBattleView extends FrameLayout {

    private FrameLayout connectTipsView;
    private LetterIconView iconView;
    private PKUser pkUser;
    private TextView defaultTips;
    private Button muteButton;

    // Custom UI Components from layout_pk_battle_view
    private ImageView bgProfileImageView;
    private View bgProfileOverlay;
    private FrameLayout videoContainer;
    private LetterIconView centerAvatarView;
    private ImageView soundIcon;
    private ImageView micIcon;
    private TextView userNameText;

    public PKBattleView(@NonNull Context context) {
        super(context);
        initView();
    }

    public PKBattleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PKBattleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public PKBattleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_pk_battle_view, this, true);

        bgProfileImageView = findViewById(R.id.pk_bg_profile_image);
        bgProfileOverlay = findViewById(R.id.pk_bg_profile_overlay);
        videoContainer = findViewById(R.id.pk_video_container);
        centerAvatarView = findViewById(R.id.pk_center_avatar);
        soundIcon = findViewById(R.id.pk_sound_icon);
        micIcon = findViewById(R.id.pk_mic_icon);
        userNameText = findViewById(R.id.pk_user_name_text);

        // Keep fallback reference for backward compatibility
        iconView = centerAvatarView;

        // Legacy/fallback reconnecting views creation
        connectTipsView = new FrameLayout(getContext());
        addView(connectTipsView);
        defaultTips = new TextView(getContext());
        defaultTips.setGravity(Gravity.CENTER);
        defaultTips.setText("host reconnecting");
        defaultTips.setTextColor(Color.WHITE);
        defaultTips.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        defaultTips.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_444));
        connectTipsView.addView(defaultTips, new LayoutParams(-1, -1));
        connectTipsView.setVisibility(GONE);

        // Hide legacy mute button
        muteButton = new Button(getContext());
        muteButton.setVisibility(GONE);
        muteButton.setText("Mute User");
        addView(muteButton, new LayoutParams(-2, -2));
    }

    /**
     * @param pkUser
     * @param parent
     */
    public void setPKUser(PKUser pkUser, ViewGroup parent) {
        this.pkUser = pkUser;
        int mixVideoWidth = PKService.MIX_VIDEO_WIDTH;
        int mixVideoHeight = PKService.MIX_VIDEO_HEIGHT;
        if (pkUser != null) {
            float rateLeft = pkUser.rect.left / (mixVideoWidth * 1f);
            float rateTop = pkUser.rect.top / (mixVideoHeight * 1f);
            int left = (int) (parent.getWidth() * rateLeft);
            int top = (int) (parent.getHeight() * rateTop);

            int width = (int) (pkUser.rect.width() * parent.getWidth() / (mixVideoWidth * 1f));
            int height = (int) (pkUser.rect.height() * parent.getHeight() / (mixVideoHeight * 1f));

            LayoutParams layoutParam = new LayoutParams(width, height);
            layoutParam.setMargins(left, top, 0, 0);
            setLayoutParams(layoutParam);

            // Hide legacy mute button
            muteButton.setVisibility(GONE);

            // host play single stream of each pk user,need attach video view and hide mix video
            boolean isCurrentUserHost = ZEGOLiveStreamingManager.getInstance().isCurrentUserHost();
            ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
            if (isCurrentUserHost) {
                videoContainer.removeAllViews();
                ZEGOVideoView videoView = new ZEGOVideoView(getContext());
                videoView.setUserID(pkUser.userID);
                videoView.setStreamID(pkUser.getPKUserStream());
                videoContainer.addView(videoView);

                if (Objects.equals(pkUser.userID, currentUser.userID)) {
                    videoView.startPreviewOnly();
                    videoView.startPublishAudioVideo();
                } else {
                    videoView.startPlayRemoteAudioVideo();
                }
            } else {
                videoContainer.removeAllViews();
            }

            defaultTips.setText(pkUser.userName + " reconnecting");
            
            // Check if local host
            ZEGOSDKUser hostUser = ZEGOLiveStreamingManager.getInstance().getHostUser();
            boolean isLocalHost = hostUser != null && Objects.equals(pkUser.userID, hostUser.userID);

            // Set dynamic size for center avatar icon (symmetrical 110dp)
            ViewGroup.LayoutParams avatarParams = centerAvatarView.getLayoutParams();
            if (avatarParams != null) {
                int sizeDp = 110; 
                int sizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeDp, getResources().getDisplayMetrics());
                avatarParams.width = sizePx;
                avatarParams.height = sizePx;
                centerAvatarView.setLayoutParams(avatarParams);
                centerAvatarView.setCircleBackgroundRadius(sizePx / 2);
            }

            if (isLocalHost) {
                // Host side: hide username text, hide speaker icon
                userNameText.setVisibility(GONE);
                soundIcon.setVisibility(GONE);
                
                // Show mic icon with click listener to toggle microphone
                updateMicStatus(pkUser.isMicrophoneOpen());
                boolean isMe = Objects.equals(pkUser.userID, GetSet.getUserId());
                if (isMe) {
                    micIcon.setOnClickListener(v -> {
                        boolean open = ZEGOSDKManager.getInstance().expressService.isMicrophoneOpen();
                        ZEGOSDKManager.getInstance().expressService.openMicrophone(!open);
                    });
                } else {
                    micIcon.setOnClickListener(null);
                }
            } else {
                // Co-host side: hide mic icon
                micIcon.setVisibility(GONE);
                
                // Set up name capsule if not local host
                if (!TextUtils.isEmpty(pkUser.userName)) {
                    userNameText.setText(pkUser.userName);
                    userNameText.setVisibility(VISIBLE);
                } else {
                    userNameText.setVisibility(GONE);
                }

                // Show speaker icon, toggle mute when clicked (if current user is host)
                updateSoundStatus();
                if (isCurrentUserHost) {
                    soundIcon.setOnClickListener(v -> {
                        boolean isMuted = ZEGOLiveStreamingManager.getInstance().isPKUserMuted(pkUser.userID);
                        ZEGOLiveStreamingManager.getInstance()
                            .mutePKUser(Collections.singletonList(pkUser.userID), !isMuted, new IZegoMixerStartCallback() {
                                @Override
                                public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                                    if (errorCode == 0) {
                                        post(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateSoundStatus();
                                            }
                                        });
                                    }
                                }
                            });
                    });
                } else {
                    soundIcon.setOnClickListener(null);
                }
            }

            // Load user profile image URL asynchronously (blurred for background)
            loadUserAvatar(pkUser);

            onCameraUpdate(pkUser.isCameraOpen());
        } else {
            if (getVideoView() != null) {
                getVideoView().stopPlayRemoteAudioVideo();
            }
            videoContainer.removeAllViews();
        }
    }

    public PKUser getPkUser() {
        return pkUser;
    }

    public void updatePKUser(PKUser pkUser) {
        LayoutParams layoutParam = new LayoutParams(pkUser.rect.width(), pkUser.rect.height());
        layoutParam.setMargins(pkUser.rect.left, pkUser.rect.top, 0, 0);
        setLayoutParams(layoutParam);
    }

    public ZEGOVideoView getVideoView() {
        if (videoContainer != null && videoContainer.getChildCount() > 0) {
            View childAt = videoContainer.getChildAt(0);
            if (childAt instanceof ZEGOVideoView) {
                return (ZEGOVideoView) childAt;
            }
        }
        return null;
    }

    private static final String TAG = "PKBattleView";

    /**
     * host user or other pk user
     *
     * @param open
     */
    public void onCameraUpdate(boolean open) {
        if (getVisibility() == VISIBLE) {
            if (open) {
                centerAvatarView.setVisibility(INVISIBLE);
                bgProfileImageView.setVisibility(GONE);
                bgProfileOverlay.setVisibility(GONE);
                if (getVideoView() != null) {
                    getVideoView().setVisibility(VISIBLE);
                }
            } else {
                centerAvatarView.setVisibility(VISIBLE);
                bgProfileImageView.setVisibility(VISIBLE);
                bgProfileOverlay.setVisibility(VISIBLE);
                if (getVideoView() != null) {
                    getVideoView().setVisibility(GONE);
                }
            }
        }
    }

    public void onMicrophoneUpdate(boolean open) {
        if (pkUser != null) {
            pkUser.setMicrophone(open);
            ZEGOSDKUser hostUser = ZEGOLiveStreamingManager.getInstance().getHostUser();
            boolean isLocalHost = hostUser != null && Objects.equals(pkUser.userID, hostUser.userID);
            if (isLocalHost) {
                updateMicStatus(open);
            }
        }
    }

    private void updateMicStatus(boolean open) {
        micIcon.setVisibility(VISIBLE);
        if (open) {
            micIcon.setImageResource(R.drawable.ic_mic);
        } else {
            micIcon.setImageResource(R.drawable.ic_mic_off);
        }
    }

    private void updateSoundStatus() {
        soundIcon.setVisibility(VISIBLE);
        if (pkUser != null) {
            boolean isMuted = ZEGOLiveStreamingManager.getInstance().isPKUserMuted(pkUser.userID);
            if (isMuted) {
                soundIcon.setImageResource(R.drawable.mute);
            } else {
                soundIcon.setImageResource(R.drawable.unmute);
            }
        }
    }

    private String formatImageUrl(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            return null;
        }
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            return imagePath;
        }
        return com.app.utils.AdminData.mediaURL + imagePath;
    }

    private void loadUserAvatar(PKUser user) {
        String rawAvatar = null;
        String userName = user.userName;

        if (Objects.equals(user.userID, GetSet.getUserId())) {
            rawAvatar = GetSet.getUserImage();
        } else {
            // 1. Try to get avatar and userName from PkLiveStreamingActivity.gemList
            if (PkLiveStreamingActivity.gemList != null) {
                for (com.app.addon.livestream.external.model.StreamDetails.StreamCohost cohost : PkLiveStreamingActivity.gemList) {
                    if (cohost != null && Objects.equals(cohost.getUserId(), user.userID)) {
                        rawAvatar = cohost.getUserImage();
                        if (TextUtils.isEmpty(userName) && !TextUtils.isEmpty(cohost.getUserName())) {
                            userName = cohost.getUserName();
                        }
                        break;
                    }
                }
            }

            // 2. Try ZIM cache
            if (TextUtils.isEmpty(rawAvatar)) {
                ZIMUserFullInfo zimUserInfo = ZEGOSDKManager.getInstance().zimService.getUserInfo(user.userID);
                if (zimUserInfo != null) {
                    rawAvatar = zimUserInfo.userAvatarUrl;
                    if (TextUtils.isEmpty(userName) && zimUserInfo.baseInfo != null && !TextUtils.isEmpty(zimUserInfo.baseInfo.userName)) {
                        userName = zimUserInfo.baseInfo.userName;
                    }
                }
            }
        }

        String userAvatarUrl = formatImageUrl(rawAvatar);

        if (!TextUtils.isEmpty(userAvatarUrl)) {
            // Load blurred background image with Glide
            com.bumptech.glide.Glide.with(getContext())
                .load(userAvatarUrl)
                .placeholder(R.drawable.alhawana)
                .error(R.drawable.alhawana)
                .apply(com.bumptech.glide.request.RequestOptions.bitmapTransform(new jp.wasabeef.glide.transformations.BlurTransformation(25, 3)))
                .into(bgProfileImageView);

            centerAvatarView.setIconUrl(userAvatarUrl);
        } else {
            bgProfileImageView.setImageResource(R.drawable.alhawana);
            centerAvatarView.setIconUrl(null);
        }
        
        if (!TextUtils.isEmpty(userName)) {
            centerAvatarView.setLetter(userName);
            ZEGOSDKUser hostUser = ZEGOLiveStreamingManager.getInstance().getHostUser();
            boolean isLocalHost = hostUser != null && Objects.equals(user.userID, hostUser.userID);
            if (!isLocalHost) {
                userNameText.setText(userName);
                userNameText.setVisibility(VISIBLE);
            } else {
                userNameText.setVisibility(GONE);
            }
        }

        // Query asynchronously if avatar url is still empty
        if (TextUtils.isEmpty(userAvatarUrl)) {
            List<String> userList = Collections.singletonList(user.userID);
            ZEGOSDKManager.getInstance().zimService.queryUsersInfo(userList, new ZIMUsersInfoQueriedCallback() {
                @Override
                public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList, ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                    if (userList != null && !userList.isEmpty()) {
                        ZIMUserFullInfo info = userList.get(0);
                        String queriedAvatarUrl = formatImageUrl(info.userAvatarUrl);
                        String queriedName = info.baseInfo != null ? info.baseInfo.userName : null;
                        post(new Runnable() {
                            @Override
                            public void run() {
                                if (PKBattleView.this.pkUser != null && Objects.equals(PKBattleView.this.pkUser.userID, info.baseInfo.userID)) {
                                    if (!TextUtils.isEmpty(queriedAvatarUrl)) {
                                        com.bumptech.glide.Glide.with(getContext())
                                            .load(queriedAvatarUrl)
                                            .placeholder(R.drawable.alhawana)
                                            .error(R.drawable.alhawana)
                                            .apply(com.bumptech.glide.request.RequestOptions.bitmapTransform(new jp.wasabeef.glide.transformations.BlurTransformation(25, 3)))
                                            .into(bgProfileImageView);

                                        centerAvatarView.setIconUrl(queriedAvatarUrl);
                                    }
                                    if (!TextUtils.isEmpty(queriedName)) {
                                        centerAvatarView.setLetter(queriedName);
                                        ZEGOSDKUser hostUser = ZEGOLiveStreamingManager.getInstance().getHostUser();
                                        boolean isLocalHost = hostUser != null && Objects.equals(PKBattleView.this.pkUser.userID, hostUser.userID);
                                        if (!isLocalHost) {
                                            userNameText.setText(queriedName);
                                            userNameText.setVisibility(VISIBLE);
                                        } else {
                                            userNameText.setVisibility(GONE);
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            });
            
            // Invoke profile API as fallback to load cohost image
            fetchProfileFromApi(user.userID);
        }
    }

    private void fetchProfileFromApi(String userId) {
        com.app.model.ProfileRequest request = new com.app.model.ProfileRequest();
        request.setUserId(GetSet.getUserId());
        request.setProfileId(userId);

        com.app.addon.livestream.external.utils.ApiInterface api = com.app.Zego.ApiBase.ZegoApiClient.INSTANCE.getClient(getContext())
            .create(com.app.addon.livestream.external.utils.ApiInterface.class);

        api.getProfile(request).enqueue(new retrofit2.Callback<com.app.model.ProfileResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.app.model.ProfileResponse> call, retrofit2.Response<com.app.model.ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && "true".equals(response.body().getStatus())) {
                    com.app.model.ProfileResponse profile = response.body();
                    String userAvatarUrl = formatImageUrl(profile.getUserImage());
                    String userName = profile.getName();
                    post(new Runnable() {
                        @Override
                        public void run() {
                            if (pkUser != null && Objects.equals(pkUser.userID, userId)) {
                                if (!TextUtils.isEmpty(userAvatarUrl)) {
                                    com.bumptech.glide.Glide.with(getContext())
                                        .load(userAvatarUrl)
                                        .placeholder(R.drawable.alhawana)
                                        .error(R.drawable.alhawana)
                                        .apply(com.bumptech.glide.request.RequestOptions.bitmapTransform(new jp.wasabeef.glide.transformations.BlurTransformation(25, 3)))
                                        .into(bgProfileImageView);

                                    centerAvatarView.setIconUrl(userAvatarUrl);
                                }
                                if (!TextUtils.isEmpty(userName)) {
                                    centerAvatarView.setLetter(userName);
                                    ZEGOSDKUser hostUser = ZEGOLiveStreamingManager.getInstance().getHostUser();
                                    boolean isLocalHost = hostUser != null && Objects.equals(pkUser.userID, hostUser.userID);
                                    if (!isLocalHost) {
                                        userNameText.setText(userName);
                                        userNameText.setVisibility(VISIBLE);
                                    }
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.app.model.ProfileResponse> call, Throwable t) {
                call.cancel();
            }
        });
    }

    public void onTimeOut(boolean timeout) {
        if (timeout) {
            connectTipsView.setVisibility(VISIBLE);
        } else {
            connectTipsView.setVisibility(GONE);
        }
    }

    public void mutePlayAudio(boolean mute) {
        if (getVideoView() != null) {
            getVideoView().mutePlayAudio(mute);
        }
        
        ZEGOSDKUser hostUser = ZEGOLiveStreamingManager.getInstance().getHostUser();
        boolean isLocalHost = hostUser != null && pkUser != null && Objects.equals(pkUser.userID, hostUser.userID);
        if (!isLocalHost && pkUser != null) {
            updateSoundStatus();
        }
    }
}
