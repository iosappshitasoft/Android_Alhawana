package com.app.Zego.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.app.Zego.Additional.ScaleListener;
import com.app.Zego.CallBacks.onCoHostClicked;
import com.app.Zego.internal.ZEGOLiveStreamingManager;
import com.app.Zego.internal.sdk.ZEGOSDKManager;
import com.app.Zego.internal.sdk.basic.ZEGOSDKUser;
import com.app.Zego.internal.sdk.components.express.ZEGOVideoView;
import com.app.Zego.internal.sdk.express.IExpressEngineEventHandler;
import com.app.addon.livestream.external.model.StreamDetails;
import com.app.addon.livestream.external.utils.Constants;
import com.app.alhawanajd.App;
import com.app.alhawanajd.R;
import com.app.alhawanajd.databinding.RvItem2Binding;
import com.app.alhawanajd.databinding.RvItem3Binding;
import com.app.alhawanajd.databinding.RvItem4Binding;
import com.app.alhawanajd.databinding.RvItem5Binding;
import com.app.alhawanajd.databinding.RvItem6Binding;
import com.app.alhawanajd.databinding.RvItem7Binding;
import com.app.alhawanajd.databinding.RvItem8Binding;
import com.app.alhawanajd.databinding.RvItem9Binding;
import com.app.alhawanajd.databinding.RvItem10Binding;
import com.app.alhawanajd.databinding.RvItem11Binding;
import com.app.alhawanajd.databinding.RvItem12Binding;
import com.app.alhawanajd.databinding.RvItem13Binding;
import com.app.alhawanajd.databinding.RvItem14Binding;
import com.app.alhawanajd.databinding.RvItem15Binding;
import com.app.alhawanajd.databinding.RvItemBinding;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.utils.AdminData;
import com.app.utils.Util;
import com.app.utils.api.ApiInterface;
import com.app.utils.api.ApiClient;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.makeramen.roundedimageview.RoundedImageView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class VideoViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int lastViewType = -1;
    private static final String TAG = "VideoViewAdapter";
    private final Map<Integer, RecyclerView.ViewHolder> viewHolderMap = new HashMap<>();
    ApiInterface apiInterface;
    boolean isUserAdded = false;
    int width = 0, height = 0;
    Context context;
    private final int currentViewType = VIEW_TYPE_1;
    private static final int VIEW_TYPE_1 = 1;
    private static final int VIEW_TYPE_2 = 2;
    private static final int VIEW_TYPE_3 = 3;
    private static final int VIEW_TYPE_4 = 4;
    private static final int VIEW_TYPE_5 = 5;
    private static final int VIEW_TYPE_6 = 6;
    private static final int VIEW_TYPE_7 = 7;
    private static final int VIEW_TYPE_8 = 8;
    private static final int VIEW_TYPE_9 = 9;
    private static final int VIEW_TYPE_10 = 10;
    private static final int VIEW_TYPE_11 = 11;
    private static final int VIEW_TYPE_12 = 12;
    private static final int VIEW_TYPE_13 = 13;
    private static final int VIEW_TYPE_14 = 14;
    private static final int VIEW_TYPE_15 = 15;

    public String useridfornullcondition;

    private final List<ZEGOSDKUser> userList = new ArrayList<>();
    private final List<StreamDetails.StreamCohost> gem = new ArrayList<>();
    int itemHeight, itemWidth, spanCount = 1;
    onCoHostClicked onCoHostClick;
    ScaleGestureDetector scaleGestureDetector ;

    int doubleTapPosition = 0;

    GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {

            /*doubleTapPosition=-1;*/
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            if (userList != null && !userList.isEmpty()) {
//                onCoHostClick.onCoHostClick(userList.get(doubleTapPosition).userID, "heart", userList.get(doubleTapPosition).userName);
            }
            return super.onSingleTapConfirmed(e);
        }
    });

    public VideoViewAdapter(Context context, onCoHostClicked onCoHostClick) {
        this.context = context;
        this.onCoHostClick = onCoHostClick;
    }

    public void addUser(List<ZEGOSDKUser> list, List<StreamDetails.StreamCohost> gemList) {
        if (list.isEmpty()) {
            return;
        }
        gem.addAll(gemList);
        userList.addAll(list);
        isUserAdded = true;
        notifyDataSetChanged();
    }

    public void addUser(ZEGOSDKUser user, List<StreamDetails.StreamCohost> gemDetails) {
        int position = userList.size();
        userList.add(user);
        gem.addAll(gemDetails);
        isUserAdded = true;
        notifyDataSetChanged();
    }

    public void updateGems(List<StreamDetails.StreamCohost> gemDetails) {
        gem.clear();
        gem.addAll(gemDetails);
        notifyItemChanged(0);
    }

    public void updateGemValue(String userId, int gemValue, int position, StreamDetails.StreamCohost streamCohost) {

        Log.e("TAG", "updateGemValue: :::::::::::::::" + new Gson().toJson(gem));
        notifyItemChanged(0);
    }

    public void cameraUpdate(String userId, boolean open) {
        for (int i = 0; i < userList.size(); i++) {
            ZEGOSDKUser user = userList.get(i);
            if (user.userID.equalsIgnoreCase(userId)) {
                user.setCameraOpen(open);
                notifyItemChanged(0);
                break;
            }
        }
    }

    public void microphoneUpdate(String userId, boolean open) {
        for (int i = 0; i < userList.size(); i++) {
            ZEGOSDKUser user = userList.get(i);
            if (user.userID.equalsIgnoreCase(userId)) {
                user.setMicrophoneOpen(open);
                notifyItemChanged(0);
                break;
            }
        }
    }

    public void removeUser(List<ZEGOSDKUser> list) {
        userList.removeAll(list);
        isUserAdded = true;
        notifyDataSetChanged();
    }

    public void removeUser(ZEGOSDKUser user) {
        int position = userList.size();
        userList.remove(user);
        isUserAdded = true;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_1) {
            RvItemBinding bind = RvItemBinding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH(bind);
        } else if (viewType == VIEW_TYPE_2) {
            RvItem2Binding bind = RvItem2Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH1(bind);
        } else if (viewType == VIEW_TYPE_3) {
            RvItem3Binding bind = RvItem3Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH2(bind);
        } else if (viewType == VIEW_TYPE_4) {
            RvItem4Binding bind = RvItem4Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH3(bind);
        } else if (viewType == VIEW_TYPE_5) {
            RvItem5Binding bind = RvItem5Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH4(bind);
        } else if (viewType == VIEW_TYPE_6) {
            RvItem6Binding bind = RvItem6Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH5(bind);
        } else if (viewType == VIEW_TYPE_7) {
            RvItem7Binding bind = RvItem7Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH6(bind);
        } else if (viewType == VIEW_TYPE_8) {
            RvItem8Binding bind = RvItem8Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH7(bind);
        } else if (viewType == VIEW_TYPE_9) {
            RvItem9Binding bind = RvItem9Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH8(bind);
        } else if (viewType == VIEW_TYPE_10) {
            RvItem10Binding bind = RvItem10Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH9(bind);
        } else if (viewType == VIEW_TYPE_11) {
            RvItem11Binding bind = RvItem11Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH10(bind);
        } else if (viewType == VIEW_TYPE_12) {
            RvItem12Binding bind = RvItem12Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH11(bind);
        } else if (viewType == VIEW_TYPE_13) {
            RvItem13Binding bind = RvItem13Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH12(bind);
        } else if (viewType == VIEW_TYPE_14) {
            RvItem14Binding bind = RvItem14Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH13(bind);
        } else if (viewType == VIEW_TYPE_15) {
            RvItem15Binding bind = RvItem15Binding.inflate(LayoutInflater.from(context), parent, false);
            View view = bind.getRoot();
            return new VH14(bind);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        int viewType = getItemViewType(position);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);


        //if you need three fix imageview in width
        int devicewidth = displaymetrics.widthPixels;

        //if you need 4-5-6 anything fix imageview in height
        int deviceheight = displaymetrics.heightPixels;
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();

        //   holder.itemView.getLayoutParams().width = devicewidth;
        if (viewType != 1) {
            //if you need same height as width you can set devicewidth in holder.image_view.getLayoutParams().height
//            holder.itemView.getLayoutParams().height = deviceheight / 2;
            holder.itemView.getLayoutParams().height = (deviceheight / 2) - 100;
        } else {
//            holder.itemView.getLayoutParams().height = (deviceheight );
        }

        // Define a custom comparator to rearrange the list based on mainStreamID

        scaleGestureDetector = new ScaleGestureDetector(holder.itemView.getContext(), new ScaleListener());

        if (userList != null && userList.size() > 1) {
            try {
                Comparator<ZEGOSDKUser> comparator = new Comparator<ZEGOSDKUser>() {
                    @Override
                    public int compare(ZEGOSDKUser user1, ZEGOSDKUser user2) {
                        //Todo - here crash is happening
                        boolean isMainHost1 = user1.getMainStreamID().endsWith("_main_host");
                        boolean isMainHost2 = user2.getMainStreamID().endsWith("_main_host");

                        if (isMainHost1 && !isMainHost2) {
                            return -1; // user1 comes first
                        } else if (!isMainHost1 && isMainHost2) {
                            return 1; // user2 comes first
                        } else {
                            return 0; // No change in order
                        }
                    }
                };

                Collections.sort(userList, comparator);

            } catch (Exception e) {
                Log.e("TAG", "onBindViewHolder: ::::::::::" + e);
            }

        }


        //commanded for SpeechWaveView ANR
        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {

            @Override
            public void onCapturedSoundLevelUpdate(float soundLevel) {
                super.onCapturedSoundLevelUpdate(soundLevel);
                // Log.e(TAG, "Audio onCapturedSoundLevelUpdate: ::::::"+soundLevel );
                try {
                    if(ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()||ZEGOLiveStreamingManager.getInstance().isCoHost(GetSet.getUserId())) {

                        if (userList != null && userList.size() > 1 /*&&soundLevel>0.5*/) {

                            if(holder instanceof VH){
                                if(userList.get(0).userID.equalsIgnoreCase(GetSet.getUserId())){
//                                ((VH) holder).binding.speechWaveView.setEndValue(soundLevel);
                                }
                            }

                            else if (holder instanceof VH1 ||holder instanceof VH2 || holder instanceof VH3
                                    || holder instanceof VH4 ||holder instanceof VH5 || holder instanceof VH6
                                    || holder instanceof VH7 ||holder instanceof VH8 || holder instanceof VH9
                                    || holder instanceof VH10 || holder instanceof VH11 || holder instanceof VH12
                                    || holder instanceof VH13 || holder instanceof VH14) {
                                for (int i = 0; i < userList.size(); i++) {
                                    if (userList.get(i).userID.equalsIgnoreCase(GetSet.getUserId())) {
                                        LottieAnimationView speechView = getSpeechView(holder, i);
                                        MaterialCardView cardView = getCardViewForUser(holder, i);
                                        if (speechView != null  && cardView != null) {

                                            if (soundLevel > 0.5) {
                                                if (!speechView.isAnimating()) {
                                                    // Apply red stroke only when animation starts
                                                    speechView.playAnimation();
                                                    cardView.setStrokeWidth((int) context.getResources().getDimension(R.dimen.card_border_width));
                                                    cardView.setStrokeColor(ContextCompat.getColor(context, R.color.call_red));

                                                }

                                            } else {
                                                // Hide the stroke when not animating
                                                cardView.setStrokeWidth(0);
                                            }

                                        }
                                        break; // Found current user, no need to continue loop
                                    }
                                }
                            }

                        }
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }

            }

            @Override
            public void onMixerSoundLevelUpdate(HashMap<Integer, Float> soundLevels) {
                super.onMixerSoundLevelUpdate(soundLevels);
//                Log.e(TAG, "onMixerSoundLevelUpdate: ::::::::"+new Gson().toJson(soundLevels)  );
            }

            @Override
            public void onRemoteSoundLevelUpdate(HashMap<String, Float> soundLevels) {
                super.onRemoteSoundLevelUpdate(soundLevels);
                //Stream_lopv0R0uEGTz_6521274ef544d7e6c46ff31c_main_cohost=1.4502132E-5
                // Log.e(TAG, "Audio onRemoteSoundLevelUpdate: ::::::"+soundLevels);
                Iterator<HashMap.Entry<String, Float>> it = soundLevels.entrySet().iterator();
                while(it.hasNext()){
                    try{
                        HashMap.Entry<String, Float> entry = it.next();
                        String streamid = entry.getKey();
                        Float value = entry.getValue();

                        if(streamid != null) {
                            if (userList != null && userList.size() > 1/*&&value>0.5*/) {

                                if (holder instanceof VH) {
                                    Log.e(TAG,"VH HOlder");
                                } else if (holder instanceof VH1) {
                                    if (userList.size() < 2) {
                                        return;
                                    }
                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                } else if (holder instanceof VH2) {
                                    if (userList.size() < 3) {
                                        return;
                                    }
                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                } else if (holder instanceof VH3) {
                                    if (userList.size() < 4) {
                                        return;
                                    }

                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                } else if (holder instanceof VH4) {
                                    if (userList.size() < 5) {
                                        return;
                                    }

                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                } else if (holder instanceof VH5) {
                                    if (userList.size() < 6) {
                                        return;
                                    }

                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                } else if (holder instanceof VH6) {
                                    if (userList.size() < 7) {
                                        return;
                                    }

                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                } else if (holder instanceof VH7) {
                                    if (userList.size() < 8) {
                                        return;
                                    }

                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                } else if (holder instanceof VH8) {
                                    if (userList.size() < 9) {
                                        return;
                                    }
                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                } else if (holder instanceof VH9) {
                                    if (userList.size() < 10) {
                                        return;
                                    }
                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                } else if (holder instanceof VH10) {
                                    if (userList.size() < 11) {
                                        return;
                                    }
                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                } else if (holder instanceof VH11) {
                                    if (userList.size() < 12) {
                                        return;
                                    }
                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                } else if (holder instanceof VH12) {
                                    if (userList.size() < 13) {
                                        return;
                                    }
                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                } else if (holder instanceof VH13) {
                                    if (userList.size() < 14) {
                                        return;
                                    }
                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                } else if (holder instanceof VH14) {
                                    if (userList.size() < 15) {
                                        return;
                                    }
                                    handleRemoteSoundAnimation(userList, streamid, value, holder);
                                }
                            }
                        }
                    } catch (NullPointerException nullPointerException){
                        nullPointerException.printStackTrace();
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }

            }

            @Override
            public void onCapturedAudioSpectrumUpdate(float[] audioSpectrum) {
                super.onCapturedAudioSpectrumUpdate(audioSpectrum);
            }

            @Override
            public void onRemoteAudioSpectrumUpdate(HashMap<String, float[]> audioSpectrums) {
                super.onRemoteAudioSpectrumUpdate(audioSpectrums);
                Iterator<HashMap.Entry<String, float[]>> it = audioSpectrums.entrySet().iterator();
                while(it.hasNext()){
                    HashMap.Entry<String, float[]> entry = it.next();
                    String streamid = entry.getKey();
                    float[] values = entry.getValue();

                }
            }
        });

        if (userList != null && !userList.isEmpty()) {
            if (holder instanceof VH) {

                ZEGOSDKUser user = userList.get(0);
                ((VH) holder).binding.hostView.setUserID(user.userID);

                holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        width = ((VH) holder).binding.hostView.getWidth();
                        height = ((VH) holder).binding.hostView.getHeight();
                        onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                    }
                });
                setVideoView(userList.get(0), ((VH) holder).binding.hostView);

                if (!user.isCameraOpen()) {
                    ((VH) holder).binding.blurLay.setVisibility(View.VISIBLE);

                    StreamDetails.StreamCohost userObj = getUserDetails(userList.get(0).userID);

                    if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
                        // Define the blur radius (e.g., 25 for a strong blur effect)
                        int blurRadius = 50;

                        // Create a BlurTransformation
                        BlurTransformation blurTransformation = new BlurTransformation(blurRadius);

                        String hostImageUrl = (GetSet.getUserImage() != null && (GetSet.getUserImage().startsWith("https://") || GetSet.getUserImage().startsWith("http://"))) ? GetSet.getUserImage() : (AdminData.mediaURL + GetSet.getUserImage());

                        Glide.with(context)
                                .load(hostImageUrl)
                                .error(R.drawable.alhawana)
                                .apply(RequestOptions.bitmapTransform(blurTransformation))
                                .into(((VH) holder).binding.blurNewchange);
                        Glide.with(context)
                                .load(hostImageUrl)
                                .error(R.drawable.alhawana)
//                            .apply(RequestOptions.bitmapTransform(blurTransformation))
                                .into(((VH) holder).binding.blurDP);
                        ((VH) holder).binding.blurDP.setVisibility(View.VISIBLE);

                    } else {
                        setCameraUpdate(userList.get(0).userID, userObj, ((VH) holder).binding.blurNewchange, ((VH) holder).binding.blurDP);
                    }


                } else {
                 ((VH) holder).binding.blurLay.setVisibility(View.GONE);
                }

                ((VH) holder).binding.blurLay.setOnClickListener(v -> {
                    Log.e("TAG LIKE : ", "1 else");
                    if (userList!=null&&!userList.isEmpty()) {
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                    }
                });


                ((VH) holder).binding.hostView.setOnClickListener(v -> {
                    Log.e("TAG LIKE : ", "1 else");
                    if (userList!=null&&!userList.isEmpty()) {
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                    }
                });
                ((VH) holder).binding.blurDP.setOnClickListener(v -> {
                    Log.e("TAG LIKE : ", "1 else");
                    if (userList!=null&&!userList.isEmpty()) {
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                    }
                });

            }
            else if (holder instanceof VH1) {
                Log.e("TAG", "onBindViewHolder: ::::::::::vh1:::::" + new Gson().toJson(gem));
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);
                    Log.e("TAG", "onBindViewHolder: :::::::::vh1 size  " + userList.size());
                    Log.e("TAG", "onBindViewHolder: :::::::::vh1 else  " + new Gson().toJson(userList.get(0)));

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH1) holder).binding.hostView.getWidth();
                            height = ((VH1) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });

                    if (ZEGOLiveStreamingManager.getInstance().isHost(GetSet.getUserId())) {
                        ((VH1) holder).binding.kickOut.setVisibility(View.GONE);
                    } else ((VH1) holder).binding.kickOut.setVisibility(View.GONE);

                    if(i==0) {
                        Log.e("TAG", "onBindViewHolder: :::::::::vh1 else2");
                        ((VH1) holder).binding.hostView.setUserID(userList.get(0).userID);

                        setVideoView(userList.get(0), ((VH1) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH1) holder).binding.userProfileImageNew1,
                                ((VH1) holder).binding.userName1,
                                ((VH1) holder).binding.tvDiamondHost,
                                userList.get(0).userName);
                        useridfornullcondition = userList.get(0).userID;
                        if (!userList.get(0).isCameraOpen()) {
                            ((VH1) holder).binding.blurLay.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH1) holder).binding.blurNewchange, ((VH1) holder).binding.blurDP);
                        } else {
                            ((VH1) holder).binding.blurLay.setVisibility(View.GONE);
                        }

                        ((VH1) holder).binding.hostView.setOnClickListener(v -> {
                            Log.e("TAG LIKE : ", "1 else");
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                        });
                        ((VH1) holder).binding.blurLay.setOnClickListener(v -> {
                            Log.e("TAG LIKE : ", "1 else");
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                        });
                        ((VH1) holder).binding.blurDP.setOnClickListener(v -> {
                            Log.e("TAG LIKE : ", "1 else");
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                        });
                        ((VH1) holder).binding.kickOut.setOnClickListener(v -> {
                            App.preventMultipleClick(v);
                            onCoHostClick.onCoHostClick(userList.get(1).userID, "kickout", userList.get(1).userName,v);
                        });
                    }
                    else  if (i == 1) {
                        Log.e("TAG", "onBindViewHolder: :::::::::vh1 else1  test" + userList.get(1).userID);
                        Log.e("TAG", "onBindViewHolder: :::::::::vh1 else1  test" + userList.get(1).userName);
                        Log.e("TAG", "onBindViewHolder: :::::::::vh1 GEMSIZE" + gem.size() +"," + userList.size());

                        useridfornullcondition = userList.get(1).userID;

                        setVideoView(userList.get(1), ((VH1) holder).binding.cohostView);
                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH1) holder).binding.userProfileImageNew1,
                                    ((VH1) holder).binding.userName2,
                                    ((VH1) holder).binding.tvDiamond1,
                                    userList.get(1).userName);


                            if (!userList.get(1).isCameraOpen()) {
                                ((VH1) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                                setCameraUpdate(userList.get(i).userID, coHost, ((VH1) holder).binding.blur1, ((VH1) holder).binding.blurDP1);
                            } else {
                                ((VH1) holder).binding.blurLay1.setVisibility(View.GONE);
                            }

                            Glide.with(context)
                                    .load((coHost.getUserImage()!=null&&coHost.getUserImage().startsWith("https://"))?coHost.getUserImage():(AdminData.mediaURL +coHost.getUserImage()))
                                    .placeholder(R.drawable.user)
                                    .error(R.drawable.user)
                                    .into(((VH1) holder).binding.userProfileImageNew1);
                        }else{
                            Log.e("TAG", "setGemView: :::::::else");

                            if (!userList.get(1).isCameraOpen()) {
                                ((VH1) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                            } else {
                                ((VH1) holder).binding.blurLay1.setVisibility(View.GONE);
                            }

                            setGemView2(userList.get(1).userID, ((VH1) holder).binding.userProfileImageNew1,
                                    ((VH1) holder).binding.userName2,
                                    ((VH1) holder).binding.tvDiamond1,
                                    userList.get(1).userName, ((VH1) holder).binding.blur1,((VH1) holder).binding.blurDP1 );
                        }

                        ((VH1) holder).binding.userName2.setOnClickListener(v -> {
                            Log.e("TAG1", "onBindViewHolder: ::::::::::::" + new Gson().toJson(gem));
                            App.preventMultipleClick(((VH1) holder).binding.gemLayHost);
                            onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName,v);
                        });

                        ((VH1) holder).binding.userName1.setOnClickListener(v -> {
                            Log.e("TAG1", "onBindViewHolder: ::::::::::::" + new Gson().toJson(gem));
                            App.preventMultipleClick(((VH1) holder).binding.gemLay);
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName,v);
                        });

                        ((VH1) holder).binding.blurLay1.setOnClickListener(v -> {
                            Log.e("TAG1", "onBindViewHolder: ::::::::::::" + new Gson().toJson(gem));
                            App.preventMultipleClick(((VH1) holder).binding.blurLay1);
                            onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                        });

                        ((VH1) holder).binding.cohostView.setOnClickListener(v -> {
                            App.preventMultipleClick(v);
                            onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                        });

                    }

                }

            }
            else if (holder instanceof VH2) {
                Log.e("TAG", "onBindViewHolder: ::::::::::vh2");
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);
                    if (ZEGOLiveStreamingManager.getInstance().isHost(GetSet.getUserId())) {
                        ((VH2) holder).binding.kickOut1.setVisibility(View.VISIBLE);
                        ((VH2) holder).binding.kickOut2.setVisibility(View.VISIBLE);
                    } else {
                        ((VH2) holder).binding.kickOut1.setVisibility(View.GONE);
                        ((VH2) holder).binding.kickOut2.setVisibility(View.GONE);
                    }

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH2) holder).binding.hostView.getWidth();
                            height = ((VH2) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });

                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);
                    if (i == 0) {
                        setVideoView(userList.get(0), ((VH2) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH2) holder).binding.userProfileImageNew,
                                ((VH2) holder).binding.txtViewHeadingNew,
                                ((VH2) holder).binding.tvDiamond,
                                userList.get(0).userName);
                        if (!userList.get(0).isCameraOpen()) {
                            ((VH2) holder).binding.blurLay.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH2) holder).binding.blurNewchange, ((VH2) holder).binding.blurDP);
                        } else {
                           ((VH2) holder).binding.blurLay1.setVisibility(View.GONE);
                        }

                    } else if (i == 1) {
                        setVideoView(userList.get(1), ((VH2) holder).binding.cohostView1);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH2) holder).binding.userProfileImageNew1,
                                ((VH2) holder).binding.txtViewHeadingNew1,
                                ((VH2) holder).binding.tvDiamond1,
                                userList.get(1).userName);
                        if (!userList.get(1).isCameraOpen()) {
                            ((VH2) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH2) holder).binding.blur1, ((VH2) holder).binding.blurDP1);
                        } else {
                            ((VH2) holder).binding.blurLay1.setVisibility(View.GONE);
                        }

                    } else {
                        setVideoView(userList.get(2), ((VH2) holder).binding.cohostView2);
                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(2).userID, ((VH2) holder).binding.userProfileImageNew2,
                                    ((VH2) holder).binding.txtViewHeadingNew2,
                                    ((VH2) holder).binding.tvDiamond2,
                                    userList.get(2).userName);
                            if (!userList.get(2).isCameraOpen()) {
                                ((VH2) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                                setCameraUpdate(userList.get(i).userID, coHost, ((VH2) holder).binding.blur2, ((VH2) holder).binding.blurDP2);
                            } else {
                                ((VH2) holder).binding.blurLay2.setVisibility(View.GONE);
                            }

                        }else{
                            Log.e("TAG", "setGemView: :::::::else");
                            if (!userList.get(2).isCameraOpen()) {
                                ((VH2) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                            } else {
                                ((VH2) holder).binding.blurLay2.setVisibility(View.GONE);
                            }
                            setGemView2(userList.get(2).userID, ((VH2) holder).binding.userProfileImageNew2,
                                    ((VH2) holder).binding.txtViewHeadingNew2,
                                    ((VH2) holder).binding.tvDiamond2,
                                    userList.get(2).userName, ((VH2) holder).binding.blur2,((VH2) holder).binding.blurDP2 );
                        }
                    }
                    ((VH2) holder).binding.hostView.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });
                    ((VH2) holder).binding.blurLay.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });

                    ((VH2) holder).binding.blurDP.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });

                    ((VH2) holder).binding.kickOut1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "kickout", userList.get(1).userName,v);
                    });
                    ((VH2) holder).binding.txtViewHeadingNew1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName,v);
                    });
                    ((VH2) holder).binding.txtViewHeadingNew.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName,v);
                    });
                    ((VH2) holder).binding.txtViewHeadingNew2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "profile", userList.get(2).userName,v);
                    });

                    ((VH2) holder).binding.cohostView1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                    });
                    ((VH2) holder).binding.blurLay1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                    });
                    ((VH2) holder).binding.kickOut2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "kickout", userList.get(2).userName,v);
                    });
                    ((VH2) holder).binding.cohostView2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName,v);
                    });
                    ((VH2) holder).binding.blurLay2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName,v);
                    });
                }
            }
            else if (holder instanceof VH3) {
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);

                    if (ZEGOLiveStreamingManager.getInstance().isHost(GetSet.getUserId())) {
                        ((VH3) holder).binding.kickOut1.setVisibility(View.VISIBLE);
                        ((VH3) holder).binding.kickOut2.setVisibility(View.VISIBLE);
                        ((VH3) holder).binding.kickOut3.setVisibility(View.VISIBLE);
                    } else {
                        ((VH3) holder).binding.kickOut1.setVisibility(View.GONE);
                        ((VH3) holder).binding.kickOut2.setVisibility(View.GONE);
                        ((VH3) holder).binding.kickOut3.setVisibility(View.GONE);
                    }

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH3) holder).binding.hostView.getWidth();
                            height = ((VH3) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);

                    if (i == 0) {
                        setVideoView(userList.get(0), ((VH3) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH3) holder).binding.userProfileImageNew,
                                ((VH3) holder).binding.txtViewHeadingNew,
                                ((VH3) holder).binding.tvDiamond,
                                userList.get(0).userName);
                        if (!userList.get(0).isCameraOpen()) {
                            ((VH3) holder).binding.blurLay.setVisibility(View.VISIBLE);
                             setCameraUpdate(userList.get(i).userID, coHost, ((VH3) holder).binding.blurNewchange, ((VH3) holder).binding.blurDP);
                        } else {
                            ((VH3) holder).binding.blurLay.setVisibility(View.GONE);
                        }
                    }
                    else if (i == 1) {
                        setVideoView(userList.get(1), ((VH3) holder).binding.cohostView1);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH3) holder).binding.userProfileImageNew1,
                                ((VH3) holder).binding.txtViewHeadingNew1,
                                ((VH3) holder).binding.tvDiamond1,
                                userList.get(1).userName);
                        if (!userList.get(1).isCameraOpen()) {
                            ((VH3) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH3) holder).binding.blur1, ((VH3) holder).binding.blurDP1);
                            ((VH3) holder).binding.cv1.setStrokeWidth((int) context.getResources().getDimension(R.dimen.card_border_width)); // e.g. 2dp
                            ((VH3) holder).binding.cv1.setStrokeColor(ContextCompat.getColor(context, R.color.call_red));

                        } else {
                            ((VH3) holder).binding.blurLay1.setVisibility(View.GONE);
                            ((VH3) holder).binding.cv1.setStrokeWidth(0); // e.g. 2dp
                        }
                    }
                    else if (i == 2) {
                        setVideoView(userList.get(2), ((VH3) holder).binding.cohostView2);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(2).userID, ((VH3) holder).binding.userProfileImageNew2,
                                ((VH3) holder).binding.txtViewHeadingNew2,
                                ((VH3) holder).binding.tvDiamond2,
                                userList.get(2).userName);
                        if (!userList.get(2).isCameraOpen()) {
                            ((VH3) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH3) holder).binding.blur2, ((VH3) holder).binding.blurDP2);
                            ((VH3) holder).binding.cv2.setStrokeWidth((int) context.getResources().getDimension(R.dimen.card_border_width)); // e.g. 2dp
                            ((VH3) holder).binding.cv2.setStrokeColor(ContextCompat.getColor(context, R.color.call_red));

                        } else {
                            ((VH3) holder).binding.blurLay2.setVisibility(View.GONE);
                            ((VH3) holder).binding.cv2.setStrokeWidth(0); // e.g. 2dp
                        }
                    }
                    else {

                        setVideoView(userList.get(3), ((VH3) holder).binding.cohostView3);
                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(3).userID, ((VH3) holder).binding.userProfileImageNew3,
                                    ((VH3) holder).binding.txtViewHeadingNew3,
                                    ((VH3) holder).binding.tvDiamond3,
                                    userList.get(3).userName);
                            if (!userList.get(3).isCameraOpen()) {
                                ((VH3) holder).binding.blurLay3.setVisibility(View.VISIBLE);
                                setCameraUpdate(userList.get(i).userID, coHost, ((VH3) holder).binding.blur3, ((VH3) holder).binding.blurDP3);
                                ((VH3) holder).binding.cv3.setStrokeWidth((int) context.getResources().getDimension(R.dimen.card_border_width)); // e.g. 2dp
                                ((VH3) holder).binding.cv3.setStrokeColor(ContextCompat.getColor(context, R.color.call_red));

                            } else {
                                ((VH3) holder).binding.blurLay3.setVisibility(View.GONE);
                                ((VH3) holder).binding.cv3.setStrokeWidth(0); // e.g. 2dp
                            }
                        }else{
                            if (!userList.get(3).isCameraOpen()) {
                                ((VH3) holder).binding.blurLay3.setVisibility(View.VISIBLE);
                            } else {
                                ((VH3) holder).binding.blurLay3.setVisibility(View.GONE);
                            }

                            setGemView2(userList.get(3).userID, ((VH3) holder).binding.userProfileImageNew3,
                                    ((VH3) holder).binding.txtViewHeadingNew3,
                                    ((VH3) holder).binding.tvDiamond3,
                                    userList.get(3).userName, ((VH3) holder).binding.blur3,((VH3) holder).binding.blurDP3 );

                        }

                    }
                    ((VH3) holder).binding.hostView.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });
                    ((VH3) holder).binding.txtViewHeadingNew.setOnClickListener(v -> {
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName,v);
                    });

                    ((VH3) holder).binding.blurLay.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });

                    ((VH3) holder).binding.blurDP.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });

                    ((VH3) holder).binding.kickOut1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "kickout", userList.get(1).userName,v);
                    });

                    ((VH3) holder).binding.cohostView1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                    });

                    ((VH3) holder).binding.blurLay1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                    });
                    ((VH3) holder).binding.txtViewHeadingNew1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName,v);
                    });
                    ((VH3) holder).binding.kickOut2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "kickout", userList.get(2).userName,v);
                    });

                    ((VH3) holder).binding.cohostView2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName,v);
                    });

                    ((VH3) holder).binding.blurLay2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName,v);
                    });
                    ((VH3) holder).binding.txtViewHeadingNew2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "profile", userList.get(2).userName,v);
                    });

                    ((VH3) holder).binding.kickOut3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "kickout", userList.get(3).userName,v);
                    });

                    ((VH3) holder).binding.cohostView3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName,v);
                    });

                    ((VH3) holder).binding.blurLay3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName,v);
                    });
                    ((VH3) holder).binding.txtViewHeadingNew3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "profile", userList.get(3).userName,v);
                    });
                }

            }
            else if (holder instanceof VH4) {
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH4) holder).binding.hostView.getWidth();
                            height = ((VH4) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);
                    if (i == 0) {
                        setVideoView(userList.get(0), ((VH4) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH4) holder).binding.userProfileImageNew,
                                ((VH4) holder).binding.txtViewHeadingNew,
                                ((VH4) holder).binding.tvDiamond,
                                userList.get(0).userName);
                        if (!userList.get(0).isCameraOpen()) {
                            ((VH4) holder).binding.blurLay.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH4) holder).binding.blurNewchange, ((VH4) holder).binding.blurDP);
                        } else {
                            ((VH4) holder).binding.blurLay.setVisibility(View.GONE);
                        }
                    } else if (i == 1) {
                        setVideoView(userList.get(1), ((VH4) holder).binding.cohostView1);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH4) holder).binding.userProfileImageNew1,
                                ((VH4) holder).binding.txtViewHeadingNew1,
                                ((VH4) holder).binding.tvDiamond1,
                                userList.get(1).userName);
                        if (!userList.get(1).isCameraOpen()) {
                            ((VH4) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH4) holder).binding.blur1, ((VH4) holder).binding.blurDP1);
                        } else {
                            ((VH4) holder).binding.blurLay1.setVisibility(View.GONE);
                        }
                    } else if (i == 2) {
                        setVideoView(userList.get(2), ((VH4) holder).binding.cohostView2);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(2).userID, ((VH4) holder).binding.userProfileImageNew2,
                                ((VH4) holder).binding.txtViewHeadingNew2,
                                ((VH4) holder).binding.tvDiamond2,
                                userList.get(2).userName);
                        if (!userList.get(2).isCameraOpen()) {
                            ((VH4) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH4) holder).binding.blur2, ((VH4) holder).binding.blurDP2);
                        } else {
                            ((VH4) holder).binding.blurLay2.setVisibility(View.GONE);
                        }
                    } else if (i == 3) {

                        setVideoView(userList.get(3), ((VH4) holder).binding.cohostView3);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(3).userID, ((VH4) holder).binding.userProfileImageNew3,
                                ((VH4) holder).binding.txtViewHeadingNew3,
                                ((VH4) holder).binding.tvDiamond3,
                                userList.get(3).userName);
                        if (!userList.get(3).isCameraOpen()) {
                            ((VH4) holder).binding.blurLay3.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH4) holder).binding.blur3, ((VH4) holder).binding.blurDP3);
                        } else {
                             ((VH4) holder).binding.blurLay3.setVisibility(View.GONE);
                        }
                    } else {

                        setVideoView(userList.get(4), ((VH4) holder).binding.cohostView4);
                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(4).userID, ((VH4) holder).binding.userProfileImageNew4,
                                    ((VH4) holder).binding.txtViewHeadingNew4,
                                    ((VH4) holder).binding.tvDiamond4,
                                    userList.get(4).userName);
                            if (!userList.get(4).isCameraOpen()) {
                                ((VH4) holder).binding.blurLay4.setVisibility(View.VISIBLE);
                                 setCameraUpdate(userList.get(i).userID, coHost, ((VH4) holder).binding.blur4, ((VH4) holder).binding.blurDP4);
                            } else {
                                ((VH4) holder).binding.blurLay4.setVisibility(View.GONE);
                            }
                        }else{
                            if (!userList.get(4).isCameraOpen()) {
                                ((VH4) holder).binding.blurLay4.setVisibility(View.VISIBLE);
                            } else {
                                ((VH4) holder).binding.blurLay4.setVisibility(View.GONE);
                            }

                            setGemView2(userList.get(4).userID, ((VH4) holder).binding.userProfileImageNew4,
                                    ((VH4) holder).binding.txtViewHeadingNew4,
                                    ((VH4) holder).binding.tvDiamond4,
                                    userList.get(4).userName, ((VH4) holder).binding.blur4,((VH4) holder).binding.blurDP4 );
                        }
                    }

                    ((VH4) holder).binding.hostView.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });
                    ((VH4) holder).binding.blurLay.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });
                    ((VH4) holder).binding.blurDP.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });
                    ((VH4) holder).binding.cohostView1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                    });
                    ((VH4) holder).binding.blurLay1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                    });

                    ((VH4) holder).binding.cohostView2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName,v);
                    });

                    ((VH4) holder).binding.blurLay2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName,v);
                    });

                    ((VH4) holder).binding.cohostView3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName,v);
                    });
                    ((VH4) holder).binding.blurLay3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName,v);
                    });

                    ((VH4) holder).binding.cohostView4.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName,v);
                    });

                    ((VH4) holder).binding.blurLay4.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName,v);
                    });

                    ((VH4) holder).binding.txtViewHeadingNew.setOnClickListener(v -> {
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName,v);
                    });

                    ((VH4) holder).binding.txtViewHeadingNew1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName,v);
                    });

                    ((VH4) holder).binding.txtViewHeadingNew2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "profile", userList.get(2).userName,v);
                    });

                    ((VH4) holder).binding.txtViewHeadingNew3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "profile", userList.get(3).userName,v);
                    });
                    ((VH4) holder).binding.txtViewHeadingNew4.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(4).userID, "profile", userList.get(4).userName,v);
                    });
                }

            }
            else if (holder instanceof VH5) {
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH5) holder).binding.hostView.getWidth();
                            height = ((VH5) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);
                    if (i == 0) {
                        setVideoView(userList.get(0), ((VH5) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH5) holder).binding.userProfileImageNew,
                                ((VH5) holder).binding.txtViewHeadingNew,
                                ((VH5) holder).binding.tvDiamond,
                                userList.get(0).userName);
                        if (!userList.get(0).isCameraOpen()) {
                            ((VH5) holder).binding.blurLay.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH5) holder).binding.blurNewchange, ((VH5) holder).binding.blurDP);
                        } else {
                            // ((VH5) holder).binding.hostSpeech.setVisibility(View.VISIBLE);
                            ((VH5) holder).binding.blurLay.setVisibility(View.GONE);
                            // ((VH5) holder).binding.hostSpeechView.setVisibility(View.GONE);
                            // ((VH5) holder).binding.cv.setStrokeWidth(0);
                        }
                    } else if (i == 1) {
                        setVideoView(userList.get(1), ((VH5) holder).binding.cohostView1);
                       /* ((VH5) holder).binding.cohostView1.setUserID(user.userID);
                        ((VH5) holder).binding.cohostView1.setStreamID(streamID);
                        ((VH5) holder).binding.cohostView1.startPlayRemoteAudioVideo();*/
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH5) holder).binding.userProfileImageNew1,
                                ((VH5) holder).binding.txtViewHeadingNew1,
                                ((VH5) holder).binding.tvDiamond1,
                                userList.get(1).userName);
                        if (!userList.get(1).isCameraOpen()) {
                            ((VH5) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH5) holder).binding.blur1, ((VH5) holder).binding.blurDP1);
                        } else {
                            ((VH5) holder).binding.blurLay1.setVisibility(View.GONE);
                        }
                    } else if (i == 2) {
                        setVideoView(userList.get(2), ((VH5) holder).binding.cohostView2);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(2).userID, ((VH5) holder).binding.userProfileImageNew2,
                                ((VH5) holder).binding.txtViewHeadingNew2,
                                ((VH5) holder).binding.tvDiamond2,
                                userList.get(2).userName);
                        if (!userList.get(2).isCameraOpen()) {
                            ((VH5) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH5) holder).binding.blur2, ((VH5) holder).binding.blurDP2);
                        } else {
                            ((VH5) holder).binding.blurLay2.setVisibility(View.GONE);
                        }
                    } else if (i == 3) {
                        setVideoView(userList.get(3), ((VH5) holder).binding.cohostView3);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(3).userID, ((VH5) holder).binding.userProfileImageNew3,
                                ((VH5) holder).binding.txtViewHeadingNew3,
                                ((VH5) holder).binding.tvDiamond3,
                                userList.get(3).userName);
                        if (!userList.get(3).isCameraOpen()) {
                            ((VH5) holder).binding.blurLay3.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH5) holder).binding.blur3, ((VH5) holder).binding.blurDP3);
                        } else {
                           ((VH5) holder).binding.blurLay3.setVisibility(View.GONE);
                        }
                    } else if (i == 4) {
                        setVideoView(userList.get(4), ((VH5) holder).binding.cohostView4);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(4).userID, ((VH5) holder).binding.userProfileImageNew4,
                                ((VH5) holder).binding.txtViewHeadingNew4,
                                ((VH5) holder).binding.tvDiamond4,
                                userList.get(4).userName);
                        if (!userList.get(4).isCameraOpen()) {
                            ((VH5) holder).binding.blurLay4.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH5) holder).binding.blur4, ((VH5) holder).binding.blurDP4);
                        } else {
                            ((VH5) holder).binding.blurLay4.setVisibility(View.GONE);
                        }
                    } else {
                        setVideoView(userList.get(5), ((VH5) holder).binding.cohostView5);
                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(5).userID, ((VH5) holder).binding.userProfileImageNew5,
                                    ((VH5) holder).binding.txtViewHeadingNew5,
                                    ((VH5) holder).binding.tvDiamond5,
                                    userList.get(5).userName);
                            if (!userList.get(5).isCameraOpen()) {
                                ((VH5) holder).binding.blurLay5.setVisibility(View.VISIBLE);
                                setCameraUpdate(userList.get(i).userID, coHost, ((VH5) holder).binding.blur5, ((VH5) holder).binding.blurDP5);
                            } else {
                               ((VH5) holder).binding.blurLay5.setVisibility(View.GONE);
                            }
                        }else{
                            if (!userList.get(5).isCameraOpen()) {
                                ((VH5) holder).binding.blurLay5.setVisibility(View.VISIBLE);
                            } else {
                                ((VH5) holder).binding.blurLay5.setVisibility(View.GONE);
                            }

                            setGemView2(userList.get(5).userID, ((VH5) holder).binding.userProfileImageNew5,
                                    ((VH5) holder).binding.txtViewHeadingNew5,
                                    ((VH5) holder).binding.tvDiamond5,
                                    userList.get(5).userName, ((VH5) holder).binding.blur5,((VH5) holder).binding.blurDP5 );
                        }
                    }

                    ((VH5) holder).binding.hostView.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });

                    ((VH5) holder).binding.blurLay.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });

                    ((VH5) holder).binding.blurDP.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });
                    ((VH5) holder).binding.cohostView1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                    });
                    ((VH5) holder).binding.blurLay1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                    });
                    ((VH5) holder).binding.cohostView2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName,v);
                    });


                    ((VH5) holder).binding.blurLay2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName,v);
                    });

                    ((VH5) holder).binding.cohostView3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName,v);
                    });

                    ((VH5) holder).binding.blurLay3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName,v);
                    });

                    ((VH5) holder).binding.cohostView4.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName,v);
                    });

                    ((VH5) holder).binding.blurLay4.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName,v);
                    });

                    ((VH5) holder).binding.cohostView5.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName,v);
                    });
                    ((VH5) holder).binding.blurLay5.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName,v);
                    });
                    ((VH5) holder).binding.txtViewHeadingNew.setOnClickListener(v -> {
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName,v);
                    });

                    ((VH5) holder).binding.txtViewHeadingNew1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName,v);
                    });

                    ((VH5) holder).binding.txtViewHeadingNew2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "profile", userList.get(2).userName,v);
                    });

                    ((VH5) holder).binding.txtViewHeadingNew3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "profile", userList.get(3).userName,v);
                    });
                    ((VH5) holder).binding.txtViewHeadingNew4.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(4).userID, "profile", userList.get(4).userName,v);
                    });
                    ((VH5) holder).binding.txtViewHeadingNew5.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(5).userID, "profile", userList.get(5).userName,v);
                    });
                }

            }
            else if (holder instanceof VH6) {
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH6) holder).binding.hostView.getWidth();
                            height = ((VH6) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);
                    if (i == 0) {
                        setVideoView(userList.get(0), ((VH6) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH6) holder).binding.userProfileImageNew,
                                ((VH6) holder).binding.txtViewHeadingNew,
                                ((VH6) holder).binding.tvDiamond,
                                userList.get(0).userName);
                        if (!userList.get(0).isCameraOpen()) {
                            ((VH6) holder).binding.blurLay.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH6) holder).binding.blurNewchange, ((VH6) holder).binding.blurDP);
                        } else {
                             ((VH6) holder).binding.blurLay.setVisibility(View.GONE);
                        }

                    } else if (i == 1) {
                        setVideoView(userList.get(1), ((VH6) holder).binding.cohostView1);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH6) holder).binding.userProfileImageNew1,
                                ((VH6) holder).binding.txtViewHeadingNew1,
                                ((VH6) holder).binding.tvDiamond1,
                                userList.get(1).userName);
                        if (!userList.get(1).isCameraOpen()) {
                            ((VH6) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                             setCameraUpdate(userList.get(i).userID, coHost, ((VH6) holder).binding.blur1, ((VH6) holder).binding.blurDP1);
                        } else {
                            ((VH6) holder).binding.blurLay1.setVisibility(View.GONE);
                        }
                    } else if (i == 2) {
                        setVideoView(userList.get(2), ((VH6) holder).binding.cohostView2);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(2).userID, ((VH6) holder).binding.userProfileImageNew2,
                                ((VH6) holder).binding.txtViewHeadingNew2,
                                ((VH6) holder).binding.tvDiamond2,
                                userList.get(2).userName);

                        if (!userList.get(2).isCameraOpen()) {
                            ((VH6) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH6) holder).binding.blur2, ((VH6) holder).binding.blurDP2);
                        } else {
                             ((VH6) holder).binding.blurLay2.setVisibility(View.GONE);
                        }

                    } else if (i == 3) {
                        setVideoView(userList.get(3), ((VH6) holder).binding.cohostView3);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(3).userID, ((VH6) holder).binding.userProfileImageNew3,
                                ((VH6) holder).binding.txtViewHeadingNew3,
                                ((VH6) holder).binding.tvDiamond3,
                                userList.get(3).userName);

                        if (!userList.get(3).isCameraOpen()) {
                            ((VH6) holder).binding.blurLay3.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH6) holder).binding.blur3, ((VH6) holder).binding.blurDP3);
                        } else {
                            ((VH6) holder).binding.blurLay3.setVisibility(View.GONE);
                        }

                    } else if (i == 4) {
                        setVideoView(userList.get(4), ((VH6) holder).binding.cohostView4);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(4).userID, ((VH6) holder).binding.userProfileImageNew4,
                                ((VH6) holder).binding.txtViewHeadingNew4,
                                ((VH6) holder).binding.tvDiamond4,
                                userList.get(4).userName);
                        if (!userList.get(4).isCameraOpen()) {
                            ((VH6) holder).binding.blurLay4.setVisibility(View.VISIBLE);
                             setCameraUpdate(userList.get(i).userID, coHost, ((VH6) holder).binding.blur4, ((VH6) holder).binding.blurDP4);
                        } else {
                            ((VH6) holder).binding.blurLay4.setVisibility(View.GONE);
                        }
                    } else if (i == 5) {
                        setVideoView(userList.get(5), ((VH6) holder).binding.cohostView5);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(5).userID, ((VH6) holder).binding.userProfileImageNew5,
                                ((VH6) holder).binding.txtViewHeadingNew5,
                                ((VH6) holder).binding.tvDiamond5,
                                userList.get(5).userName);
                        if (!userList.get(5).isCameraOpen()) {
                            ((VH6) holder).binding.blurLay5.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH6) holder).binding.blur5, ((VH6) holder).binding.blurDP5);
                        } else {
                            ((VH6) holder).binding.blurLay5.setVisibility(View.GONE);
                        }
                    } else {
                        setVideoView(userList.get(6), ((VH6) holder).binding.cohostView6);

                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(6).userID, ((VH6) holder).binding.userProfileImageNew6,
                                    ((VH6) holder).binding.txtViewHeadingNew6,
                                    ((VH6) holder).binding.tvDiamond6,
                                    userList.get(6).userName);
                            if (!userList.get(6).isCameraOpen()) {
                                ((VH6) holder).binding.blurLay6.setVisibility(View.VISIBLE);
                                setCameraUpdate(userList.get(i).userID, coHost, ((VH6) holder).binding.blur6, ((VH6) holder).binding.blurDP6);
                            } else {
                                ((VH6) holder).binding.blurLay6.setVisibility(View.GONE);
                            }

                        }else{
                            if (!userList.get(6).isCameraOpen()) {
                                ((VH6) holder).binding.blurLay6.setVisibility(View.VISIBLE);
                            } else {
                                ((VH6) holder).binding.blurLay6.setVisibility(View.GONE);
                            }

                            setGemView2(userList.get(6).userID, ((VH6) holder).binding.userProfileImageNew6,
                                    ((VH6) holder).binding.txtViewHeadingNew6,
                                    ((VH6) holder).binding.tvDiamond6,
                                    userList.get(6).userName, ((VH6) holder).binding.blur6,((VH6) holder).binding.blurDP6 );
                        }
                    }

                    ((VH6) holder).binding.hostView.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });

                    ((VH6) holder).binding.blurLay.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });

                    ((VH6) holder).binding.blurDP.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });

                    ((VH6) holder).binding.cohostView1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                    });

                    ((VH6) holder).binding.blurLay1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                    });

                    ((VH6) holder).binding.cohostView2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName,v);
                    });

                    ((VH6) holder).binding.blurLay2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName,v);
                    });

                    ((VH6) holder).binding.cohostView3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName,v);
                    });


                    ((VH6) holder).binding.blurLay3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName,v);
                    });

                    ((VH6) holder).binding.cohostView4.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName,v);
                    });


                    ((VH6) holder).binding.blurLay4.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName,v);
                    });

                    ((VH6) holder).binding.cohostView5.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName,v);
                    });
                    ((VH6) holder).binding.blurLay5.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName,v);
                    });

                    ((VH6) holder).binding.cohostView6.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName,v);
                    });
                    ((VH6) holder).binding.blurLay6.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName,v);
                    });


                    ((VH6) holder).binding.txtViewHeadingNew.setOnClickListener(v -> {
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName,v);
                    });

                    ((VH6) holder).binding.txtViewHeadingNew1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName,v);
                    });

                    ((VH6) holder).binding.txtViewHeadingNew2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "profile", userList.get(2).userName,v);
                    });

                    ((VH6) holder).binding.txtViewHeadingNew3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "profile", userList.get(3).userName,v);
                    });
                    ((VH6) holder).binding.txtViewHeadingNew4.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(4).userID, "profile", userList.get(4).userName,v);
                    });
                    ((VH6) holder).binding.txtViewHeadingNew5.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(5).userID, "profile", userList.get(5).userName,v);
                    });
                    ((VH6) holder).binding.txtViewHeadingNew6.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(6).userID, "profile", userList.get(6).userName,v);
                    });

                }

            }
            else if (holder instanceof VH7) {
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH7) holder).binding.hostView.getWidth();
                            height = ((VH7) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);
                    if (i == 0) {
                        setVideoView(userList.get(0), ((VH7) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH7) holder).binding.userProfileImageNew,
                                ((VH7) holder).binding.txtViewHeadingNew,
                                ((VH7) holder).binding.tvDiamond,
                                userList.get(0).userName);

                        if (!userList.get(0).isCameraOpen()) {
                            // ((VH7) holder).binding.hostSpeechView.setVisibility(View.VISIBLE);
                            ((VH7) holder).binding.blurLay.setVisibility(View.VISIBLE);
                            // ((VH7) holder).binding.hostSpeech.setVisibility(View.GONE);

                            setCameraUpdate(userList.get(i).userID, coHost, ((VH7) holder).binding.blurNewchange, ((VH7) holder).binding.blurDP);
                          /*  ((VH7) holder).binding.cv.setStrokeWidth((int) context.getResources().getDimension(R.dimen.card_border_width)); // e.g. 2dp
                            ((VH7) holder).binding.cv.setStrokeColor(ContextCompat.getColor(context, R.color.call_red));*/
                        } else {
                            // ((VH7) holder).binding.hostSpeechView.setVisibility(View.GONE);
                            ((VH7) holder).binding.blurLay.setVisibility(View.GONE);
                            // ((VH7) holder).binding.hostSpeech.setVisibility(View.VISIBLE);
                            //  ((VH7) holder).binding.cv.setStrokeWidth(0);
                        }

                    } else if (i == 1) {
                        setVideoView(userList.get(1), ((VH7) holder).binding.cohostView1);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH7) holder).binding.userProfileImageNew1,
                                ((VH7) holder).binding.txtViewHeadingNew1,
                                ((VH7) holder).binding.tvDiamond1,
                                userList.get(1).userName);

                        if (!userList.get(1).isCameraOpen()) {
                            ((VH7) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                            //((VH7) holder).binding.cohostSpeechView1.setVisibility(View.VISIBLE);
                            //((VH7) holder).binding.cohostSpeech1.setVisibility(View.GONE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH7) holder).binding.blur1, ((VH7) holder).binding.blurDP1);
                            // ((VH7) holder).binding.cv1.setStrokeWidth((int) context.getResources().getDimension(R.dimen.card_border_width)); // e.g. 2dp
                            // ((VH7) holder).binding.cv1.setStrokeColor(ContextCompat.getColor(context, R.color.call_red));
                        } else {
                            // ((VH7) holder).binding.cohostSpeech1.setVisibility(View.VISIBLE);
                            ((VH7) holder).binding.blurLay1.setVisibility(View.GONE);
                            //  ((VH7) holder).binding.cohostSpeechView1.setVisibility(View.GONE);
                            //   ((VH7) holder).binding.cv1.setStrokeWidth(0);
                        }

                    } else if (i == 2) {
                        setVideoView(userList.get(2), ((VH7) holder).binding.cohostView2);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(2).userID, ((VH7) holder).binding.userProfileImageNew2,
                                ((VH7) holder).binding.txtViewHeadingNew2,
                                ((VH7) holder).binding.tvDiamond2,
                                userList.get(2).userName);

                        if (!userList.get(2).isCameraOpen()) {
                            ((VH7) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                            //   ((VH7) holder).binding.cohostSpeechView2.setVisibility(View.VISIBLE);
                            //    ((VH7) holder).binding.cohostSpeech2.setVisibility(View.GONE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH7) holder).binding.blur2, ((VH7) holder).binding.blurDP2);
                            // ((VH7) holder).binding.cv2.setStrokeWidth((int) context.getResources().getDimension(R.dimen.card_border_width)); // e.g. 2dp
                            //  ((VH7) holder).binding.cv2.setStrokeColor(ContextCompat.getColor(context, R.color.call_red));
                        } else {
                            // ((VH7) holder).binding.cohostSpeech2.setVisibility(View.VISIBLE);
                            // ((VH7) holder).binding.cohostSpeechView2.setVisibility(View.GONE);
                            ((VH7) holder).binding.blurLay2.setVisibility(View.GONE);
                            //  ((VH7) holder).binding.cv2.setStrokeWidth(0);
                        }

                    } else if (i == 3) {
                        setVideoView(userList.get(3), ((VH7) holder).binding.cohostView3);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(3).userID, ((VH7) holder).binding.userProfileImageNew3,
                                ((VH7) holder).binding.txtViewHeadingNew3,
                                ((VH7) holder).binding.tvDiamond3,
                                userList.get(3).userName);

                        if (!userList.get(3).isCameraOpen()) {
                            ((VH7) holder).binding.blurLay3.setVisibility(View.VISIBLE);
                            // ((VH7) holder).binding.cohostSpeechView3.setVisibility(View.VISIBLE);
                            //  ((VH7) holder).binding.cohostSpeech3.setVisibility(View.GONE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH7) holder).binding.blur3, ((VH7) holder).binding.blurDP3);
                            // ((VH7) holder).binding.cv3.setStrokeWidth((int) context.getResources().getDimension(R.dimen.card_border_width)); // e.g. 2dp
                            // ((VH7) holder).binding.cv3.setStrokeColor(ContextCompat.getColor(context, R.color.call_red));
                        } else {
                            //  ((VH7) holder).binding.cohostSpeech3.setVisibility(View.VISIBLE);
                            ((VH7) holder).binding.blurLay3.setVisibility(View.GONE);
                            //  ((VH7) holder).binding.cohostSpeechView3.setVisibility(View.GONE);
                            // ((VH7) holder).binding.cv3.setStrokeWidth(0);
                        }

                    } else if (i == 4) {
                        setVideoView(userList.get(4), ((VH7) holder).binding.cohostView4);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(4).userID, ((VH7) holder).binding.userProfileImageNew4,
                                ((VH7) holder).binding.txtViewHeadingNew4,
                                ((VH7) holder).binding.tvDiamond4,
                                userList.get(4).userName);

                        if (!userList.get(4).isCameraOpen()) {
                            ((VH7) holder).binding.blurLay4.setVisibility(View.VISIBLE);
                            // ((VH7) holder).binding.cohostSpeechView4.setVisibility(View.VISIBLE);
                            // ((VH7) holder).binding.cohostSpeech4.setVisibility(View.GONE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH7) holder).binding.blur4, ((VH7) holder).binding.blurDP4);
                            // ((VH7) holder).binding.cv4.setStrokeWidth((int) context.getResources().getDimension(R.dimen.card_border_width)); // e.g. 2dp
                            // ((VH7) holder).binding.cv4.setStrokeColor(ContextCompat.getColor(context, R.color.call_red));
                        } else {
                            //((VH7) holder).binding.cohostSpeech4.setVisibility(View.GONE);
                            ((VH7) holder).binding.blurLay4.setVisibility(View.GONE);
                            //((VH7) holder).binding.cohostSpeechView4.setVisibility(View.GONE);
                            // ((VH7) holder).binding.cv4.setStrokeWidth(0);
                        }

                    } else if (i == 5) {
                        setVideoView(userList.get(5), ((VH7) holder).binding.cohostView5);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(5).userID, ((VH7) holder).binding.userProfileImageNew5,
                                ((VH7) holder).binding.txtViewHeadingNew5,
                                ((VH7) holder).binding.tvDiamond5,
                                userList.get(5).userName);

                        if (!userList.get(5).isCameraOpen()) {
                            ((VH7) holder).binding.blurLay5.setVisibility(View.VISIBLE);
                            //((VH7) holder).binding.cohostSpeechView5.setVisibility(View.VISIBLE);
                            //((VH7) holder).binding.cohostSpeech5.setVisibility(View.GONE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH7) holder).binding.blur5, ((VH7) holder).binding.blurDP5);
                            //  ((VH7) holder).binding.cv5.setStrokeWidth((int) context.getResources().getDimension(R.dimen.card_border_width)); // e.g. 2dp
                            //   ((VH7) holder).binding.cv5.setStrokeColor(ContextCompat.getColor(context, R.color.call_red));
                        } else {
                            //  ((VH7) holder).binding.cohostSpeech5.setVisibility(View.VISIBLE);
                            //  ((VH7) holder).binding.cohostSpeechView5.setVisibility(View.GONE);
                            ((VH7) holder).binding.blurLay5.setVisibility(View.GONE);
                            //  ((VH7) holder).binding.cv5.setStrokeWidth(0);
                        }

                    } else if (i == 6) {
                        setVideoView(userList.get(6), ((VH7) holder).binding.cohostView6);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(6).userID, ((VH7) holder).binding.userProfileImageNew6,
                                ((VH7) holder).binding.txtViewHeadingNew6,
                                ((VH7) holder).binding.tvDiamond6,
                                userList.get(6).userName);

                        if (!userList.get(6).isCameraOpen()) {
                            ((VH7) holder).binding.blurLay6.setVisibility(View.VISIBLE);
                            // ((VH7) holder).binding.cohostSpeechView6.setVisibility(View.VISIBLE);
                            // ((VH7) holder).binding.cohostSpeech6.setVisibility(View.GONE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH7) holder).binding.blur6, ((VH7) holder).binding.blurDP6);
                            // ((VH7) holder).binding.cv6.setStrokeWidth((int) context.getResources().getDimension(R.dimen.card_border_width)); // e.g. 2dp
                            //  ((VH7) holder).binding.cv6.setStrokeColor(ContextCompat.getColor(context, R.color.call_red));
                        } else {
                            // ((VH7) holder).binding.cohostSpeech6.setVisibility(View.VISIBLE);
                            ((VH7) holder).binding.blurLay6.setVisibility(View.GONE);
                            // ((VH7) holder).binding.cohostSpeechView6.setVisibility(View.GONE);
                            // ((VH7) holder).binding.cv6.setStrokeWidth(0);
                        }

                    } else {
                        setVideoView(userList.get(7), ((VH7) holder).binding.cohostView7);

                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(7).userID, ((VH7) holder).binding.userProfileImageNew7,
                                    ((VH7) holder).binding.txtViewHeadingNew7,
                                    ((VH7) holder).binding.tvDiamond7,
                                    userList.get(7).userName);

                            if (!userList.get(7).isCameraOpen()) {
                                ((VH7) holder).binding.blurLay7.setVisibility(View.VISIBLE);
                                setCameraUpdate(userList.get(i).userID, coHost, ((VH7) holder).binding.blur7, ((VH7) holder).binding.blurDP7);
                            } else {
                                ((VH7) holder).binding.blurLay7.setVisibility(View.GONE);
                            }

                        }else{
                            if (!userList.get(7).isCameraOpen()) {
                                ((VH7) holder).binding.blurLay7.setVisibility(View.VISIBLE);
                            } else {
                                ((VH7) holder).binding.blurLay7.setVisibility(View.GONE);
                            }

                            setGemView2(userList.get(7).userID, ((VH7) holder).binding.userProfileImageNew7,
                                    ((VH7) holder).binding.txtViewHeadingNew7,
                                    ((VH7) holder).binding.tvDiamond7,
                                    userList.get(7).userName, ((VH7) holder).binding.blur7,((VH7) holder).binding.blurDP7 );
                        }

                    }

                    ((VH7) holder).binding.hostView.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });


                    ((VH7) holder).binding.blurLay.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });


                    ((VH7) holder).binding.blurDP.setOnClickListener(v -> {
                        Log.e("TAG LIKE : ", "1 else");
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName,v);
                    });

                    ((VH7) holder).binding.cohostView1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                    });

                    ((VH7) holder).binding.blurLay1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName,v);
                    });
                    ((VH7) holder).binding.cohostView2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName,v);
                    });

                    ((VH7) holder).binding.blurLay2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName,v);
                    });

                    ((VH7) holder).binding.cohostView3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName,v);
                    });


                    ((VH7) holder).binding.blurLay3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName,v);
                    });

                    ((VH7) holder).binding.cohostView4.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName,v);
                    });

                    ((VH7) holder).binding.blurLay4.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName,v);
                    });

                    ((VH7) holder).binding.cohostView5.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName,v);
                    });

                    ((VH7) holder).binding.blurLay5.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName,v);
                    });

                    ((VH7) holder).binding.cohostView6.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName,v);
                    });


                    ((VH7) holder).binding.blurLay6.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName,v);
                    });

                    ((VH7) holder).binding.cohostView7.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName,v);
                    });
                    ((VH7) holder).binding.blurLay7.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName,v);
                    });


                    ((VH7) holder).binding.txtViewHeadingNew.setOnClickListener(v -> {
                        onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName,v);
                    });

                    ((VH7) holder).binding.txtViewHeadingNew1.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName,v);
                    });

                    ((VH7) holder).binding.txtViewHeadingNew2.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(2).userID, "profile", userList.get(2).userName,v);
                    });

                    ((VH7) holder).binding.txtViewHeadingNew3.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(3).userID, "profile", userList.get(3).userName,v);
                    });
                    ((VH7) holder).binding.txtViewHeadingNew4.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(4).userID, "profile", userList.get(4).userName,v);
                    });
                    ((VH7) holder).binding.txtViewHeadingNew5.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(5).userID, "profile", userList.get(5).userName,v);
                    });
                    ((VH7) holder).binding.txtViewHeadingNew6.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(6).userID, "profile", userList.get(6).userName,v);
                    });
                    ((VH7) holder).binding.txtViewHeadingNew7.setOnClickListener(v -> {
                        App.preventMultipleClick(v);
                        onCoHostClick.onCoHostClick(userList.get(7).userID, "profile", userList.get(7).userName,v);
                    });

                }

            }
else if (holder instanceof VH8) {
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH8) holder).binding.hostView.getWidth();
                            height = ((VH8) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);
                    if (i == 0) {
                        setVideoView(userList.get(0), ((VH8) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH8) holder).binding.userProfileImageNew,
                                ((VH8) holder).binding.txtViewHeadingNew,
                                ((VH8) holder).binding.tvDiamond,
                                userList.get(0).userName);

                        if (!userList.get(0).isCameraOpen()) {
                            ((VH8) holder).binding.blurLay.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH8) holder).binding.blurNewchange, ((VH8) holder).binding.blurDP);
                        } else {
                            ((VH8) holder).binding.blurLay.setVisibility(View.GONE);
                        }
                    } else if (i == 1) {
                        setVideoView(userList.get(1), ((VH8) holder).binding.cohostView1);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH8) holder).binding.userProfileImageNew1,
                                ((VH8) holder).binding.txtViewHeadingNew1,
                                ((VH8) holder).binding.tvDiamond1,
                                userList.get(1).userName);
                        if (!userList.get(1).isCameraOpen()) {
                            ((VH8) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH8) holder).binding.blur1, ((VH8) holder).binding.blurDP1);
                        } else {
                            ((VH8) holder).binding.blurLay1.setVisibility(View.GONE);
                        }
                    } else if (i == 2) {
                        setVideoView(userList.get(2), ((VH8) holder).binding.cohostView2);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(2).userID, ((VH8) holder).binding.userProfileImageNew2,
                                ((VH8) holder).binding.txtViewHeadingNew2,
                                ((VH8) holder).binding.tvDiamond2,
                                userList.get(2).userName);
                        if (!userList.get(2).isCameraOpen()) {
                            ((VH8) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH8) holder).binding.blur2, ((VH8) holder).binding.blurDP2);
                        } else {
                            ((VH8) holder).binding.blurLay2.setVisibility(View.GONE);
                        }
                    } else if (i == 3) {
                        setVideoView(userList.get(3), ((VH8) holder).binding.cohostView3);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(3).userID, ((VH8) holder).binding.userProfileImageNew3,
                                ((VH8) holder).binding.txtViewHeadingNew3,
                                ((VH8) holder).binding.tvDiamond3,
                                userList.get(3).userName);
                        if (!userList.get(3).isCameraOpen()) {
                            ((VH8) holder).binding.blurLay3.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH8) holder).binding.blur3, ((VH8) holder).binding.blurDP3);
                        } else {
                            ((VH8) holder).binding.blurLay3.setVisibility(View.GONE);
                        }
                    } else if (i == 4) {
                        setVideoView(userList.get(4), ((VH8) holder).binding.cohostView4);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(4).userID, ((VH8) holder).binding.userProfileImageNew4,
                                ((VH8) holder).binding.txtViewHeadingNew4,
                                ((VH8) holder).binding.tvDiamond4,
                                userList.get(4).userName);
                        if (!userList.get(4).isCameraOpen()) {
                            ((VH8) holder).binding.blurLay4.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH8) holder).binding.blur4, ((VH8) holder).binding.blurDP4);
                        } else {
                            ((VH8) holder).binding.blurLay4.setVisibility(View.GONE);
                        }
                    } else if (i == 5) {
                        setVideoView(userList.get(5), ((VH8) holder).binding.cohostView5);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(5).userID, ((VH8) holder).binding.userProfileImageNew5,
                                ((VH8) holder).binding.txtViewHeadingNew5,
                                ((VH8) holder).binding.tvDiamond5,
                                userList.get(5).userName);
                        if (!userList.get(5).isCameraOpen()) {
                            ((VH8) holder).binding.blurLay5.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH8) holder).binding.blur5, ((VH8) holder).binding.blurDP5);
                        } else {
                            ((VH8) holder).binding.blurLay5.setVisibility(View.GONE);
                        }
                    } else if (i == 6) {
                        setVideoView(userList.get(6), ((VH8) holder).binding.cohostView6);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(6).userID, ((VH8) holder).binding.userProfileImageNew6,
                                ((VH8) holder).binding.txtViewHeadingNew6,
                                ((VH8) holder).binding.tvDiamond6,
                                userList.get(6).userName);
                        if (!userList.get(6).isCameraOpen()) {
                            ((VH8) holder).binding.blurLay6.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH8) holder).binding.blur6, ((VH8) holder).binding.blurDP6);
                        } else {
                            ((VH8) holder).binding.blurLay6.setVisibility(View.GONE);
                        }
                    } else if (i == 7) {
                        setVideoView(userList.get(7), ((VH8) holder).binding.cohostView7);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(7).userID, ((VH8) holder).binding.userProfileImageNew7,
                                ((VH8) holder).binding.txtViewHeadingNew7,
                                ((VH8) holder).binding.tvDiamond7,
                                userList.get(7).userName);
                        if (!userList.get(7).isCameraOpen()) {
                            ((VH8) holder).binding.blurLay7.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH8) holder).binding.blur7, ((VH8) holder).binding.blurDP7);
                        } else {
                            ((VH8) holder).binding.blurLay7.setVisibility(View.GONE);
                        }
                    } else {
                        setVideoView(userList.get(8), ((VH8) holder).binding.cohostView8);
                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(8).userID, ((VH8) holder).binding.userProfileImageNew8,
                                    ((VH8) holder).binding.txtViewHeadingNew8,
                                    ((VH8) holder).binding.tvDiamond8,
                                    userList.get(8).userName);
                            if (!userList.get(8).isCameraOpen()) {
                                ((VH8) holder).binding.blurLay8.setVisibility(View.VISIBLE);
                                setCameraUpdate(userList.get(i).userID, coHost, ((VH8) holder).binding.blur8, ((VH8) holder).binding.blurDP8);
                            } else {
                                ((VH8) holder).binding.blurLay8.setVisibility(View.GONE);
                            }

                        }else{
                            if (!userList.get(8).isCameraOpen()) {
                                ((VH8) holder).binding.blurLay8.setVisibility(View.VISIBLE);
                            } else {
                                ((VH8) holder).binding.blurLay8.setVisibility(View.GONE);
                            }

                            setGemView2(userList.get(8).userID, ((VH8) holder).binding.userProfileImageNew8,
                                    ((VH8) holder).binding.txtViewHeadingNew8,
                                    ((VH8) holder).binding.tvDiamond8,
                                    userList.get(8).userName, ((VH8) holder).binding.blur8,((VH8) holder).binding.blurDP8 );
                        }
                    }
                }

                for (int i = 0; i < userList.size(); i++) {
                    final int idx = i;
                    if (idx == 0) {
                        ((VH8) holder).binding.hostView.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH8) holder).binding.blurLay.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH8) holder).binding.blurDP.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH8) holder).binding.txtViewHeadingNew.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName, v);
                        });
                    } else {
                        // for cohosts
                        // dynamically setting click listeners is easier by checking the exact index we are loops over, but since they are statically mapped views, we switch/if over idx:
                        switch (idx) {
                            case 1:
                                ((VH8) holder).binding.cohostView1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH8) holder).binding.blurLay1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH8) holder).binding.txtViewHeadingNew1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName, v);
                                });
                                break;
                            case 2:
                                ((VH8) holder).binding.cohostView2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH8) holder).binding.blurLay2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH8) holder).binding.txtViewHeadingNew2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "profile", userList.get(2).userName, v);
                                });
                                break;
                            case 3:
                                ((VH8) holder).binding.cohostView3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH8) holder).binding.blurLay3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH8) holder).binding.txtViewHeadingNew3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "profile", userList.get(3).userName, v);
                                });
                                break;
                            case 4:
                                ((VH8) holder).binding.cohostView4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH8) holder).binding.blurLay4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH8) holder).binding.txtViewHeadingNew4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "profile", userList.get(4).userName, v);
                                });
                                break;
                            case 5:
                                ((VH8) holder).binding.cohostView5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH8) holder).binding.blurLay5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH8) holder).binding.txtViewHeadingNew5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "profile", userList.get(5).userName, v);
                                });
                                break;
                            case 6:
                                ((VH8) holder).binding.cohostView6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH8) holder).binding.blurLay6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH8) holder).binding.txtViewHeadingNew6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "profile", userList.get(6).userName, v);
                                });
                                break;
                            case 7:
                                ((VH8) holder).binding.cohostView7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH8) holder).binding.blurLay7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH8) holder).binding.txtViewHeadingNew7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "profile", userList.get(7).userName, v);
                                });
                                break;
                            case 8:
                                ((VH8) holder).binding.cohostView8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH8) holder).binding.blurLay8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH8) holder).binding.txtViewHeadingNew8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "profile", userList.get(8).userName, v);
                                });
                                break;
                        }
                    }
                }
            }



            else if (holder instanceof VH9) {
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH9) holder).binding.hostView.getWidth();
                            height = ((VH9) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);
                    if (i == 0) {
                        setVideoView(userList.get(0), ((VH9) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH9) holder).binding.userProfileImageNew,
                                ((VH9) holder).binding.txtViewHeadingNew,
                                ((VH9) holder).binding.tvDiamond,
                                userList.get(0).userName);

                        if (!userList.get(0).isCameraOpen()) {
                            ((VH9) holder).binding.blurLay.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH9) holder).binding.blurNewchange, ((VH9) holder).binding.blurDP);
                        } else {
                            ((VH9) holder).binding.blurLay.setVisibility(View.GONE);
                        }
                    } else if (i == 1) {
                        setVideoView(userList.get(1), ((VH9) holder).binding.cohostView1);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH9) holder).binding.userProfileImageNew1,
                                ((VH9) holder).binding.txtViewHeadingNew1,
                                ((VH9) holder).binding.tvDiamond1,
                                userList.get(1).userName);
                        if (!userList.get(1).isCameraOpen()) {
                            ((VH9) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH9) holder).binding.blur1, ((VH9) holder).binding.blurDP1);
                        } else {
                            ((VH9) holder).binding.blurLay1.setVisibility(View.GONE);
                        }
                    } else if (i == 2) {
                        setVideoView(userList.get(2), ((VH9) holder).binding.cohostView2);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(2).userID, ((VH9) holder).binding.userProfileImageNew2,
                                ((VH9) holder).binding.txtViewHeadingNew2,
                                ((VH9) holder).binding.tvDiamond2,
                                userList.get(2).userName);
                        if (!userList.get(2).isCameraOpen()) {
                            ((VH9) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH9) holder).binding.blur2, ((VH9) holder).binding.blurDP2);
                        } else {
                            ((VH9) holder).binding.blurLay2.setVisibility(View.GONE);
                        }
                    } else if (i == 3) {
                        setVideoView(userList.get(3), ((VH9) holder).binding.cohostView3);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(3).userID, ((VH9) holder).binding.userProfileImageNew3,
                                ((VH9) holder).binding.txtViewHeadingNew3,
                                ((VH9) holder).binding.tvDiamond3,
                                userList.get(3).userName);
                        if (!userList.get(3).isCameraOpen()) {
                            ((VH9) holder).binding.blurLay3.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH9) holder).binding.blur3, ((VH9) holder).binding.blurDP3);
                        } else {
                            ((VH9) holder).binding.blurLay3.setVisibility(View.GONE);
                        }
                    } else if (i == 4) {
                        setVideoView(userList.get(4), ((VH9) holder).binding.cohostView4);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(4).userID, ((VH9) holder).binding.userProfileImageNew4,
                                ((VH9) holder).binding.txtViewHeadingNew4,
                                ((VH9) holder).binding.tvDiamond4,
                                userList.get(4).userName);
                        if (!userList.get(4).isCameraOpen()) {
                            ((VH9) holder).binding.blurLay4.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH9) holder).binding.blur4, ((VH9) holder).binding.blurDP4);
                        } else {
                            ((VH9) holder).binding.blurLay4.setVisibility(View.GONE);
                        }
                    } else if (i == 5) {
                        setVideoView(userList.get(5), ((VH9) holder).binding.cohostView5);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(5).userID, ((VH9) holder).binding.userProfileImageNew5,
                                ((VH9) holder).binding.txtViewHeadingNew5,
                                ((VH9) holder).binding.tvDiamond5,
                                userList.get(5).userName);
                        if (!userList.get(5).isCameraOpen()) {
                            ((VH9) holder).binding.blurLay5.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH9) holder).binding.blur5, ((VH9) holder).binding.blurDP5);
                        } else {
                            ((VH9) holder).binding.blurLay5.setVisibility(View.GONE);
                        }
                    } else if (i == 6) {
                        setVideoView(userList.get(6), ((VH9) holder).binding.cohostView6);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(6).userID, ((VH9) holder).binding.userProfileImageNew6,
                                ((VH9) holder).binding.txtViewHeadingNew6,
                                ((VH9) holder).binding.tvDiamond6,
                                userList.get(6).userName);
                        if (!userList.get(6).isCameraOpen()) {
                            ((VH9) holder).binding.blurLay6.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH9) holder).binding.blur6, ((VH9) holder).binding.blurDP6);
                        } else {
                            ((VH9) holder).binding.blurLay6.setVisibility(View.GONE);
                        }
                    } else if (i == 7) {
                        setVideoView(userList.get(7), ((VH9) holder).binding.cohostView7);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(7).userID, ((VH9) holder).binding.userProfileImageNew7,
                                ((VH9) holder).binding.txtViewHeadingNew7,
                                ((VH9) holder).binding.tvDiamond7,
                                userList.get(7).userName);
                        if (!userList.get(7).isCameraOpen()) {
                            ((VH9) holder).binding.blurLay7.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH9) holder).binding.blur7, ((VH9) holder).binding.blurDP7);
                        } else {
                            ((VH9) holder).binding.blurLay7.setVisibility(View.GONE);
                        }
                    } else if (i == 8) {
                        setVideoView(userList.get(8), ((VH9) holder).binding.cohostView8);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(8).userID, ((VH9) holder).binding.userProfileImageNew8,
                                ((VH9) holder).binding.txtViewHeadingNew8,
                                ((VH9) holder).binding.tvDiamond8,
                                userList.get(8).userName);
                        if (!userList.get(8).isCameraOpen()) {
                            ((VH9) holder).binding.blurLay8.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH9) holder).binding.blur8, ((VH9) holder).binding.blurDP8);
                        } else {
                            ((VH9) holder).binding.blurLay8.setVisibility(View.GONE);
                        }
                    } else {
                        setVideoView(userList.get(9), ((VH9) holder).binding.cohostView9);
                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(9).userID, ((VH9) holder).binding.userProfileImageNew9,
                                    ((VH9) holder).binding.txtViewHeadingNew9,
                                    ((VH9) holder).binding.tvDiamond9,
                                    userList.get(9).userName);
                            if (!userList.get(9).isCameraOpen()) {
                                ((VH9) holder).binding.blurLay9.setVisibility(View.VISIBLE);
                                setCameraUpdate(userList.get(i).userID, coHost, ((VH9) holder).binding.blur9, ((VH9) holder).binding.blurDP9);
                            } else {
                                ((VH9) holder).binding.blurLay9.setVisibility(View.GONE);
                            }

                        }else{
                            if (!userList.get(9).isCameraOpen()) {
                                ((VH9) holder).binding.blurLay9.setVisibility(View.VISIBLE);
                            } else {
                                ((VH9) holder).binding.blurLay9.setVisibility(View.GONE);
                            }

                            setGemView2(userList.get(9).userID, ((VH9) holder).binding.userProfileImageNew9,
                                    ((VH9) holder).binding.txtViewHeadingNew9,
                                    ((VH9) holder).binding.tvDiamond9,
                                    userList.get(9).userName, ((VH9) holder).binding.blur9,((VH9) holder).binding.blurDP9 );
                        }
                    }
                }

                for (int i = 0; i < userList.size(); i++) {
                    final int idx = i;
                    if (idx == 0) {
                        ((VH9) holder).binding.hostView.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH9) holder).binding.blurLay.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH9) holder).binding.blurDP.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH9) holder).binding.txtViewHeadingNew.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName, v);
                        });
                    } else {
                        // for cohosts
                        // dynamically setting click listeners is easier by checking the exact index we are loops over, but since they are statically mapped views, we switch/if over idx:
                        switch (idx) {
                            case 1:
                                ((VH9) holder).binding.cohostView1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH9) holder).binding.blurLay1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH9) holder).binding.txtViewHeadingNew1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName, v);
                                });
                                break;
                            case 2:
                                ((VH9) holder).binding.cohostView2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH9) holder).binding.blurLay2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH9) holder).binding.txtViewHeadingNew2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "profile", userList.get(2).userName, v);
                                });
                                break;
                            case 3:
                                ((VH9) holder).binding.cohostView3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH9) holder).binding.blurLay3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH9) holder).binding.txtViewHeadingNew3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "profile", userList.get(3).userName, v);
                                });
                                break;
                            case 4:
                                ((VH9) holder).binding.cohostView4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH9) holder).binding.blurLay4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH9) holder).binding.txtViewHeadingNew4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "profile", userList.get(4).userName, v);
                                });
                                break;
                            case 5:
                                ((VH9) holder).binding.cohostView5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH9) holder).binding.blurLay5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH9) holder).binding.txtViewHeadingNew5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "profile", userList.get(5).userName, v);
                                });
                                break;
                            case 6:
                                ((VH9) holder).binding.cohostView6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH9) holder).binding.blurLay6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH9) holder).binding.txtViewHeadingNew6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "profile", userList.get(6).userName, v);
                                });
                                break;
                            case 7:
                                ((VH9) holder).binding.cohostView7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH9) holder).binding.blurLay7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH9) holder).binding.txtViewHeadingNew7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "profile", userList.get(7).userName, v);
                                });
                                break;
                            case 8:
                                ((VH9) holder).binding.cohostView8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH9) holder).binding.blurLay8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH9) holder).binding.txtViewHeadingNew8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "profile", userList.get(8).userName, v);
                                });
                                break;
                            case 9:
                                ((VH9) holder).binding.cohostView9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "gift", userList.get(9).userName, v);
                                });
                                ((VH9) holder).binding.blurLay9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "gift", userList.get(9).userName, v);
                                });
                                ((VH9) holder).binding.txtViewHeadingNew9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "profile", userList.get(9).userName, v);
                                });
                                break;
                        }
                    }
                }
            }



            else if (holder instanceof VH10) {
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH10) holder).binding.hostView.getWidth();
                            height = ((VH10) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);
                    if (i == 0) {
                        setVideoView(userList.get(0), ((VH10) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH10) holder).binding.userProfileImageNew,
                                ((VH10) holder).binding.txtViewHeadingNew,
                                ((VH10) holder).binding.tvDiamond,
                                userList.get(0).userName);

                        if (!userList.get(0).isCameraOpen()) {
                            ((VH10) holder).binding.blurLay.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH10) holder).binding.blurNewchange, ((VH10) holder).binding.blurDP);
                        } else {
                            ((VH10) holder).binding.blurLay.setVisibility(View.GONE);
                        }
                    } else if (i == 1) {
                        setVideoView(userList.get(1), ((VH10) holder).binding.cohostView1);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH10) holder).binding.userProfileImageNew1,
                                ((VH10) holder).binding.txtViewHeadingNew1,
                                ((VH10) holder).binding.tvDiamond1,
                                userList.get(1).userName);
                        if (!userList.get(1).isCameraOpen()) {
                            ((VH10) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH10) holder).binding.blur1, ((VH10) holder).binding.blurDP1);
                        } else {
                            ((VH10) holder).binding.blurLay1.setVisibility(View.GONE);
                        }
                    } else if (i == 2) {
                        setVideoView(userList.get(2), ((VH10) holder).binding.cohostView2);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(2).userID, ((VH10) holder).binding.userProfileImageNew2,
                                ((VH10) holder).binding.txtViewHeadingNew2,
                                ((VH10) holder).binding.tvDiamond2,
                                userList.get(2).userName);
                        if (!userList.get(2).isCameraOpen()) {
                            ((VH10) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH10) holder).binding.blur2, ((VH10) holder).binding.blurDP2);
                        } else {
                            ((VH10) holder).binding.blurLay2.setVisibility(View.GONE);
                        }
                    } else if (i == 3) {
                        setVideoView(userList.get(3), ((VH10) holder).binding.cohostView3);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(3).userID, ((VH10) holder).binding.userProfileImageNew3,
                                ((VH10) holder).binding.txtViewHeadingNew3,
                                ((VH10) holder).binding.tvDiamond3,
                                userList.get(3).userName);
                        if (!userList.get(3).isCameraOpen()) {
                            ((VH10) holder).binding.blurLay3.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH10) holder).binding.blur3, ((VH10) holder).binding.blurDP3);
                        } else {
                            ((VH10) holder).binding.blurLay3.setVisibility(View.GONE);
                        }
                    } else if (i == 4) {
                        setVideoView(userList.get(4), ((VH10) holder).binding.cohostView4);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(4).userID, ((VH10) holder).binding.userProfileImageNew4,
                                ((VH10) holder).binding.txtViewHeadingNew4,
                                ((VH10) holder).binding.tvDiamond4,
                                userList.get(4).userName);
                        if (!userList.get(4).isCameraOpen()) {
                            ((VH10) holder).binding.blurLay4.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH10) holder).binding.blur4, ((VH10) holder).binding.blurDP4);
                        } else {
                            ((VH10) holder).binding.blurLay4.setVisibility(View.GONE);
                        }
                    } else if (i == 5) {
                        setVideoView(userList.get(5), ((VH10) holder).binding.cohostView5);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(5).userID, ((VH10) holder).binding.userProfileImageNew5,
                                ((VH10) holder).binding.txtViewHeadingNew5,
                                ((VH10) holder).binding.tvDiamond5,
                                userList.get(5).userName);
                        if (!userList.get(5).isCameraOpen()) {
                            ((VH10) holder).binding.blurLay5.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH10) holder).binding.blur5, ((VH10) holder).binding.blurDP5);
                        } else {
                            ((VH10) holder).binding.blurLay5.setVisibility(View.GONE);
                        }
                    } else if (i == 6) {
                        setVideoView(userList.get(6), ((VH10) holder).binding.cohostView6);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(6).userID, ((VH10) holder).binding.userProfileImageNew6,
                                ((VH10) holder).binding.txtViewHeadingNew6,
                                ((VH10) holder).binding.tvDiamond6,
                                userList.get(6).userName);
                        if (!userList.get(6).isCameraOpen()) {
                            ((VH10) holder).binding.blurLay6.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH10) holder).binding.blur6, ((VH10) holder).binding.blurDP6);
                        } else {
                            ((VH10) holder).binding.blurLay6.setVisibility(View.GONE);
                        }
                    } else if (i == 7) {
                        setVideoView(userList.get(7), ((VH10) holder).binding.cohostView7);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(7).userID, ((VH10) holder).binding.userProfileImageNew7,
                                ((VH10) holder).binding.txtViewHeadingNew7,
                                ((VH10) holder).binding.tvDiamond7,
                                userList.get(7).userName);
                        if (!userList.get(7).isCameraOpen()) {
                            ((VH10) holder).binding.blurLay7.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH10) holder).binding.blur7, ((VH10) holder).binding.blurDP7);
                        } else {
                            ((VH10) holder).binding.blurLay7.setVisibility(View.GONE);
                        }
                    } else if (i == 8) {
                        setVideoView(userList.get(8), ((VH10) holder).binding.cohostView8);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(8).userID, ((VH10) holder).binding.userProfileImageNew8,
                                ((VH10) holder).binding.txtViewHeadingNew8,
                                ((VH10) holder).binding.tvDiamond8,
                                userList.get(8).userName);
                        if (!userList.get(8).isCameraOpen()) {
                            ((VH10) holder).binding.blurLay8.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH10) holder).binding.blur8, ((VH10) holder).binding.blurDP8);
                        } else {
                            ((VH10) holder).binding.blurLay8.setVisibility(View.GONE);
                        }
                    } else if (i == 9) {
                        setVideoView(userList.get(9), ((VH10) holder).binding.cohostView9);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(9).userID, ((VH10) holder).binding.userProfileImageNew9,
                                ((VH10) holder).binding.txtViewHeadingNew9,
                                ((VH10) holder).binding.tvDiamond9,
                                userList.get(9).userName);
                        if (!userList.get(9).isCameraOpen()) {
                            ((VH10) holder).binding.blurLay9.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH10) holder).binding.blur9, ((VH10) holder).binding.blurDP9);
                        } else {
                            ((VH10) holder).binding.blurLay9.setVisibility(View.GONE);
                        }
                    } else {
                        setVideoView(userList.get(10), ((VH10) holder).binding.cohostView10);
                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(10).userID, ((VH10) holder).binding.userProfileImageNew10,
                                    ((VH10) holder).binding.txtViewHeadingNew10,
                                    ((VH10) holder).binding.tvDiamond10,
                                    userList.get(10).userName);
                            if (!userList.get(10).isCameraOpen()) {
                                ((VH10) holder).binding.blurLay10.setVisibility(View.VISIBLE);
                                setCameraUpdate(userList.get(i).userID, coHost, ((VH10) holder).binding.blur10, ((VH10) holder).binding.blurDP10);
                            } else {
                                ((VH10) holder).binding.blurLay10.setVisibility(View.GONE);
                            }

                        }else{
                            if (!userList.get(10).isCameraOpen()) {
                                ((VH10) holder).binding.blurLay10.setVisibility(View.VISIBLE);
                            } else {
                                ((VH10) holder).binding.blurLay10.setVisibility(View.GONE);
                            }

                            setGemView2(userList.get(10).userID, ((VH10) holder).binding.userProfileImageNew10,
                                    ((VH10) holder).binding.txtViewHeadingNew10,
                                    ((VH10) holder).binding.tvDiamond10,
                                    userList.get(10).userName, ((VH10) holder).binding.blur10,((VH10) holder).binding.blurDP10 );
                        }
                    }
                }

                for (int i = 0; i < userList.size(); i++) {
                    final int idx = i;
                    if (idx == 0) {
                        ((VH10) holder).binding.hostView.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH10) holder).binding.blurLay.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH10) holder).binding.blurDP.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH10) holder).binding.txtViewHeadingNew.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName, v);
                        });
                    } else {
                        // for cohosts
                        // dynamically setting click listeners is easier by checking the exact index we are loops over, but since they are statically mapped views, we switch/if over idx:
                        switch (idx) {
                            case 1:
                                ((VH10) holder).binding.cohostView1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH10) holder).binding.blurLay1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH10) holder).binding.txtViewHeadingNew1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName, v);
                                });
                                break;
                            case 2:
                                ((VH10) holder).binding.cohostView2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH10) holder).binding.blurLay2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH10) holder).binding.txtViewHeadingNew2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "profile", userList.get(2).userName, v);
                                });
                                break;
                            case 3:
                                ((VH10) holder).binding.cohostView3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH10) holder).binding.blurLay3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH10) holder).binding.txtViewHeadingNew3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "profile", userList.get(3).userName, v);
                                });
                                break;
                            case 4:
                                ((VH10) holder).binding.cohostView4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH10) holder).binding.blurLay4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH10) holder).binding.txtViewHeadingNew4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "profile", userList.get(4).userName, v);
                                });
                                break;
                            case 5:
                                ((VH10) holder).binding.cohostView5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH10) holder).binding.blurLay5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH10) holder).binding.txtViewHeadingNew5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "profile", userList.get(5).userName, v);
                                });
                                break;
                            case 6:
                                ((VH10) holder).binding.cohostView6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH10) holder).binding.blurLay6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH10) holder).binding.txtViewHeadingNew6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "profile", userList.get(6).userName, v);
                                });
                                break;
                            case 7:
                                ((VH10) holder).binding.cohostView7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH10) holder).binding.blurLay7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH10) holder).binding.txtViewHeadingNew7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "profile", userList.get(7).userName, v);
                                });
                                break;
                            case 8:
                                ((VH10) holder).binding.cohostView8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH10) holder).binding.blurLay8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH10) holder).binding.txtViewHeadingNew8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "profile", userList.get(8).userName, v);
                                });
                                break;
                            case 9:
                                ((VH10) holder).binding.cohostView9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "gift", userList.get(9).userName, v);
                                });
                                ((VH10) holder).binding.blurLay9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "gift", userList.get(9).userName, v);
                                });
                                ((VH10) holder).binding.txtViewHeadingNew9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "profile", userList.get(9).userName, v);
                                });
                                break;
                            case 10:
                                ((VH10) holder).binding.cohostView10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "gift", userList.get(10).userName, v);
                                });
                                ((VH10) holder).binding.blurLay10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "gift", userList.get(10).userName, v);
                                });
                                ((VH10) holder).binding.txtViewHeadingNew10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "profile", userList.get(10).userName, v);
                                });
                                break;
                        }
                    }
                }
            }



            else if (holder instanceof VH11) {
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH11) holder).binding.hostView.getWidth();
                            height = ((VH11) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);
                    if (i == 0) {
                        setVideoView(userList.get(0), ((VH11) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH11) holder).binding.userProfileImageNew,
                                ((VH11) holder).binding.txtViewHeadingNew,
                                ((VH11) holder).binding.tvDiamond,
                                userList.get(0).userName);

                        if (!userList.get(0).isCameraOpen()) {
                            ((VH11) holder).binding.blurLay.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH11) holder).binding.blurNewchange, ((VH11) holder).binding.blurDP);
                        } else {
                            ((VH11) holder).binding.blurLay.setVisibility(View.GONE);
                        }
                    } else if (i == 1) {
                        setVideoView(userList.get(1), ((VH11) holder).binding.cohostView1);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH11) holder).binding.userProfileImageNew1,
                                ((VH11) holder).binding.txtViewHeadingNew1,
                                ((VH11) holder).binding.tvDiamond1,
                                userList.get(1).userName);
                        if (!userList.get(1).isCameraOpen()) {
                            ((VH11) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH11) holder).binding.blur1, ((VH11) holder).binding.blurDP1);
                        } else {
                            ((VH11) holder).binding.blurLay1.setVisibility(View.GONE);
                        }
                    } else if (i == 2) {
                        setVideoView(userList.get(2), ((VH11) holder).binding.cohostView2);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(2).userID, ((VH11) holder).binding.userProfileImageNew2,
                                ((VH11) holder).binding.txtViewHeadingNew2,
                                ((VH11) holder).binding.tvDiamond2,
                                userList.get(2).userName);
                        if (!userList.get(2).isCameraOpen()) {
                            ((VH11) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH11) holder).binding.blur2, ((VH11) holder).binding.blurDP2);
                        } else {
                            ((VH11) holder).binding.blurLay2.setVisibility(View.GONE);
                        }
                    } else if (i == 3) {
                        setVideoView(userList.get(3), ((VH11) holder).binding.cohostView3);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(3).userID, ((VH11) holder).binding.userProfileImageNew3,
                                ((VH11) holder).binding.txtViewHeadingNew3,
                                ((VH11) holder).binding.tvDiamond3,
                                userList.get(3).userName);
                        if (!userList.get(3).isCameraOpen()) {
                            ((VH11) holder).binding.blurLay3.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH11) holder).binding.blur3, ((VH11) holder).binding.blurDP3);
                        } else {
                            ((VH11) holder).binding.blurLay3.setVisibility(View.GONE);
                        }
                    } else if (i == 4) {
                        setVideoView(userList.get(4), ((VH11) holder).binding.cohostView4);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(4).userID, ((VH11) holder).binding.userProfileImageNew4,
                                ((VH11) holder).binding.txtViewHeadingNew4,
                                ((VH11) holder).binding.tvDiamond4,
                                userList.get(4).userName);
                        if (!userList.get(4).isCameraOpen()) {
                            ((VH11) holder).binding.blurLay4.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH11) holder).binding.blur4, ((VH11) holder).binding.blurDP4);
                        } else {
                            ((VH11) holder).binding.blurLay4.setVisibility(View.GONE);
                        }
                    } else if (i == 5) {
                        setVideoView(userList.get(5), ((VH11) holder).binding.cohostView5);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(5).userID, ((VH11) holder).binding.userProfileImageNew5,
                                ((VH11) holder).binding.txtViewHeadingNew5,
                                ((VH11) holder).binding.tvDiamond5,
                                userList.get(5).userName);
                        if (!userList.get(5).isCameraOpen()) {
                            ((VH11) holder).binding.blurLay5.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH11) holder).binding.blur5, ((VH11) holder).binding.blurDP5);
                        } else {
                            ((VH11) holder).binding.blurLay5.setVisibility(View.GONE);
                        }
                    } else if (i == 6) {
                        setVideoView(userList.get(6), ((VH11) holder).binding.cohostView6);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(6).userID, ((VH11) holder).binding.userProfileImageNew6,
                                ((VH11) holder).binding.txtViewHeadingNew6,
                                ((VH11) holder).binding.tvDiamond6,
                                userList.get(6).userName);
                        if (!userList.get(6).isCameraOpen()) {
                            ((VH11) holder).binding.blurLay6.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH11) holder).binding.blur6, ((VH11) holder).binding.blurDP6);
                        } else {
                            ((VH11) holder).binding.blurLay6.setVisibility(View.GONE);
                        }
                    } else if (i == 7) {
                        setVideoView(userList.get(7), ((VH11) holder).binding.cohostView7);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(7).userID, ((VH11) holder).binding.userProfileImageNew7,
                                ((VH11) holder).binding.txtViewHeadingNew7,
                                ((VH11) holder).binding.tvDiamond7,
                                userList.get(7).userName);
                        if (!userList.get(7).isCameraOpen()) {
                            ((VH11) holder).binding.blurLay7.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH11) holder).binding.blur7, ((VH11) holder).binding.blurDP7);
                        } else {
                            ((VH11) holder).binding.blurLay7.setVisibility(View.GONE);
                        }
                    } else if (i == 8) {
                        setVideoView(userList.get(8), ((VH11) holder).binding.cohostView8);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(8).userID, ((VH11) holder).binding.userProfileImageNew8,
                                ((VH11) holder).binding.txtViewHeadingNew8,
                                ((VH11) holder).binding.tvDiamond8,
                                userList.get(8).userName);
                        if (!userList.get(8).isCameraOpen()) {
                            ((VH11) holder).binding.blurLay8.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH11) holder).binding.blur8, ((VH11) holder).binding.blurDP8);
                        } else {
                            ((VH11) holder).binding.blurLay8.setVisibility(View.GONE);
                        }
                    } else if (i == 9) {
                        setVideoView(userList.get(9), ((VH11) holder).binding.cohostView9);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(9).userID, ((VH11) holder).binding.userProfileImageNew9,
                                ((VH11) holder).binding.txtViewHeadingNew9,
                                ((VH11) holder).binding.tvDiamond9,
                                userList.get(9).userName);
                        if (!userList.get(9).isCameraOpen()) {
                            ((VH11) holder).binding.blurLay9.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH11) holder).binding.blur9, ((VH11) holder).binding.blurDP9);
                        } else {
                            ((VH11) holder).binding.blurLay9.setVisibility(View.GONE);
                        }
                    } else if (i == 10) {
                        setVideoView(userList.get(10), ((VH11) holder).binding.cohostView10);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(10).userID, ((VH11) holder).binding.userProfileImageNew10,
                                ((VH11) holder).binding.txtViewHeadingNew10,
                                ((VH11) holder).binding.tvDiamond10,
                                userList.get(10).userName);
                        if (!userList.get(10).isCameraOpen()) {
                            ((VH11) holder).binding.blurLay10.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH11) holder).binding.blur10, ((VH11) holder).binding.blurDP10);
                        } else {
                            ((VH11) holder).binding.blurLay10.setVisibility(View.GONE);
                        }
                    } else {
                        setVideoView(userList.get(11), ((VH11) holder).binding.cohostView11);
                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(11).userID, ((VH11) holder).binding.userProfileImageNew11,
                                    ((VH11) holder).binding.txtViewHeadingNew11,
                                    ((VH11) holder).binding.tvDiamond11,
                                    userList.get(11).userName);
                            if (!userList.get(11).isCameraOpen()) {
                                ((VH11) holder).binding.blurLay11.setVisibility(View.VISIBLE);
                                setCameraUpdate(userList.get(i).userID, coHost, ((VH11) holder).binding.blur11, ((VH11) holder).binding.blurDP11);
                            } else {
                                ((VH11) holder).binding.blurLay11.setVisibility(View.GONE);
                            }

                        }else{
                            if (!userList.get(11).isCameraOpen()) {
                                ((VH11) holder).binding.blurLay11.setVisibility(View.VISIBLE);
                            } else {
                                ((VH11) holder).binding.blurLay11.setVisibility(View.GONE);
                            }

                            setGemView2(userList.get(11).userID, ((VH11) holder).binding.userProfileImageNew11,
                                    ((VH11) holder).binding.txtViewHeadingNew11,
                                    ((VH11) holder).binding.tvDiamond11,
                                    userList.get(11).userName, ((VH11) holder).binding.blur11,((VH11) holder).binding.blurDP11 );
                        }
                    }
                }

                for (int i = 0; i < userList.size(); i++) {
                    final int idx = i;
                    if (idx == 0) {
                        ((VH11) holder).binding.hostView.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH11) holder).binding.blurLay.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH11) holder).binding.blurDP.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH11) holder).binding.txtViewHeadingNew.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName, v);
                        });
                    } else {
                        // for cohosts
                        // dynamically setting click listeners is easier by checking the exact index we are loops over, but since they are statically mapped views, we switch/if over idx:
                        switch (idx) {
                            case 1:
                                ((VH11) holder).binding.cohostView1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH11) holder).binding.blurLay1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH11) holder).binding.txtViewHeadingNew1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName, v);
                                });
                                break;
                            case 2:
                                ((VH11) holder).binding.cohostView2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH11) holder).binding.blurLay2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH11) holder).binding.txtViewHeadingNew2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "profile", userList.get(2).userName, v);
                                });
                                break;
                            case 3:
                                ((VH11) holder).binding.cohostView3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH11) holder).binding.blurLay3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH11) holder).binding.txtViewHeadingNew3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "profile", userList.get(3).userName, v);
                                });
                                break;
                            case 4:
                                ((VH11) holder).binding.cohostView4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH11) holder).binding.blurLay4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH11) holder).binding.txtViewHeadingNew4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "profile", userList.get(4).userName, v);
                                });
                                break;
                            case 5:
                                ((VH11) holder).binding.cohostView5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH11) holder).binding.blurLay5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH11) holder).binding.txtViewHeadingNew5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "profile", userList.get(5).userName, v);
                                });
                                break;
                            case 6:
                                ((VH11) holder).binding.cohostView6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH11) holder).binding.blurLay6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH11) holder).binding.txtViewHeadingNew6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "profile", userList.get(6).userName, v);
                                });
                                break;
                            case 7:
                                ((VH11) holder).binding.cohostView7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH11) holder).binding.blurLay7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH11) holder).binding.txtViewHeadingNew7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "profile", userList.get(7).userName, v);
                                });
                                break;
                            case 8:
                                ((VH11) holder).binding.cohostView8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH11) holder).binding.blurLay8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH11) holder).binding.txtViewHeadingNew8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "profile", userList.get(8).userName, v);
                                });
                                break;
                            case 9:
                                ((VH11) holder).binding.cohostView9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "gift", userList.get(9).userName, v);
                                });
                                ((VH11) holder).binding.blurLay9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "gift", userList.get(9).userName, v);
                                });
                                ((VH11) holder).binding.txtViewHeadingNew9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "profile", userList.get(9).userName, v);
                                });
                                break;
                            case 10:
                                ((VH11) holder).binding.cohostView10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "gift", userList.get(10).userName, v);
                                });
                                ((VH11) holder).binding.blurLay10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "gift", userList.get(10).userName, v);
                                });
                                ((VH11) holder).binding.txtViewHeadingNew10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "profile", userList.get(10).userName, v);
                                });
                                break;
                            case 11:
                                ((VH11) holder).binding.cohostView11.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(11).userID, "gift", userList.get(11).userName, v);
                                });
                                ((VH11) holder).binding.blurLay11.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(11).userID, "gift", userList.get(11).userName, v);
                                });
                                ((VH11) holder).binding.txtViewHeadingNew11.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(11).userID, "profile", userList.get(11).userName, v);
                                });
                                break;
                        }
                    }
                }
            }



            else if (holder instanceof VH12) {
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH12) holder).binding.hostView.getWidth();
                            height = ((VH12) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);
                    if (i == 0) {
                        setVideoView(userList.get(0), ((VH12) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH12) holder).binding.userProfileImageNew,
                                ((VH12) holder).binding.txtViewHeadingNew,
                                ((VH12) holder).binding.tvDiamond,
                                userList.get(0).userName);

                        if (!userList.get(0).isCameraOpen()) {
                            ((VH12) holder).binding.blurLay.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH12) holder).binding.blurNewchange, ((VH12) holder).binding.blurDP);
                        } else {
                            ((VH12) holder).binding.blurLay.setVisibility(View.GONE);
                        }
                    } else if (i == 1) {
                        setVideoView(userList.get(1), ((VH12) holder).binding.cohostView1);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH12) holder).binding.userProfileImageNew1,
                                ((VH12) holder).binding.txtViewHeadingNew1,
                                ((VH12) holder).binding.tvDiamond1,
                                userList.get(1).userName);
                        if (!userList.get(1).isCameraOpen()) {
                            ((VH12) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH12) holder).binding.blur1, ((VH12) holder).binding.blurDP1);
                        } else {
                            ((VH12) holder).binding.blurLay1.setVisibility(View.GONE);
                        }
                    } else if (i == 2) {
                        setVideoView(userList.get(2), ((VH12) holder).binding.cohostView2);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(2).userID, ((VH12) holder).binding.userProfileImageNew2,
                                ((VH12) holder).binding.txtViewHeadingNew2,
                                ((VH12) holder).binding.tvDiamond2,
                                userList.get(2).userName);
                        if (!userList.get(2).isCameraOpen()) {
                            ((VH12) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH12) holder).binding.blur2, ((VH12) holder).binding.blurDP2);
                        } else {
                            ((VH12) holder).binding.blurLay2.setVisibility(View.GONE);
                        }
                    } else if (i == 3) {
                        setVideoView(userList.get(3), ((VH12) holder).binding.cohostView3);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(3).userID, ((VH12) holder).binding.userProfileImageNew3,
                                ((VH12) holder).binding.txtViewHeadingNew3,
                                ((VH12) holder).binding.tvDiamond3,
                                userList.get(3).userName);
                        if (!userList.get(3).isCameraOpen()) {
                            ((VH12) holder).binding.blurLay3.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH12) holder).binding.blur3, ((VH12) holder).binding.blurDP3);
                        } else {
                            ((VH12) holder).binding.blurLay3.setVisibility(View.GONE);
                        }
                    } else if (i == 4) {
                        setVideoView(userList.get(4), ((VH12) holder).binding.cohostView4);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(4).userID, ((VH12) holder).binding.userProfileImageNew4,
                                ((VH12) holder).binding.txtViewHeadingNew4,
                                ((VH12) holder).binding.tvDiamond4,
                                userList.get(4).userName);
                        if (!userList.get(4).isCameraOpen()) {
                            ((VH12) holder).binding.blurLay4.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH12) holder).binding.blur4, ((VH12) holder).binding.blurDP4);
                        } else {
                            ((VH12) holder).binding.blurLay4.setVisibility(View.GONE);
                        }
                    } else if (i == 5) {
                        setVideoView(userList.get(5), ((VH12) holder).binding.cohostView5);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(5).userID, ((VH12) holder).binding.userProfileImageNew5,
                                ((VH12) holder).binding.txtViewHeadingNew5,
                                ((VH12) holder).binding.tvDiamond5,
                                userList.get(5).userName);
                        if (!userList.get(5).isCameraOpen()) {
                            ((VH12) holder).binding.blurLay5.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH12) holder).binding.blur5, ((VH12) holder).binding.blurDP5);
                        } else {
                            ((VH12) holder).binding.blurLay5.setVisibility(View.GONE);
                        }
                    } else if (i == 6) {
                        setVideoView(userList.get(6), ((VH12) holder).binding.cohostView6);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(6).userID, ((VH12) holder).binding.userProfileImageNew6,
                                ((VH12) holder).binding.txtViewHeadingNew6,
                                ((VH12) holder).binding.tvDiamond6,
                                userList.get(6).userName);
                        if (!userList.get(6).isCameraOpen()) {
                            ((VH12) holder).binding.blurLay6.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH12) holder).binding.blur6, ((VH12) holder).binding.blurDP6);
                        } else {
                            ((VH12) holder).binding.blurLay6.setVisibility(View.GONE);
                        }
                    } else if (i == 7) {
                        setVideoView(userList.get(7), ((VH12) holder).binding.cohostView7);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(7).userID, ((VH12) holder).binding.userProfileImageNew7,
                                ((VH12) holder).binding.txtViewHeadingNew7,
                                ((VH12) holder).binding.tvDiamond7,
                                userList.get(7).userName);
                        if (!userList.get(7).isCameraOpen()) {
                            ((VH12) holder).binding.blurLay7.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH12) holder).binding.blur7, ((VH12) holder).binding.blurDP7);
                        } else {
                            ((VH12) holder).binding.blurLay7.setVisibility(View.GONE);
                        }
                    } else if (i == 8) {
                        setVideoView(userList.get(8), ((VH12) holder).binding.cohostView8);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(8).userID, ((VH12) holder).binding.userProfileImageNew8,
                                ((VH12) holder).binding.txtViewHeadingNew8,
                                ((VH12) holder).binding.tvDiamond8,
                                userList.get(8).userName);
                        if (!userList.get(8).isCameraOpen()) {
                            ((VH12) holder).binding.blurLay8.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH12) holder).binding.blur8, ((VH12) holder).binding.blurDP8);
                        } else {
                            ((VH12) holder).binding.blurLay8.setVisibility(View.GONE);
                        }
                    } else if (i == 9) {
                        setVideoView(userList.get(9), ((VH12) holder).binding.cohostView9);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(9).userID, ((VH12) holder).binding.userProfileImageNew9,
                                ((VH12) holder).binding.txtViewHeadingNew9,
                                ((VH12) holder).binding.tvDiamond9,
                                userList.get(9).userName);
                        if (!userList.get(9).isCameraOpen()) {
                            ((VH12) holder).binding.blurLay9.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH12) holder).binding.blur9, ((VH12) holder).binding.blurDP9);
                        } else {
                            ((VH12) holder).binding.blurLay9.setVisibility(View.GONE);
                        }
                    } else if (i == 10) {
                        setVideoView(userList.get(10), ((VH12) holder).binding.cohostView10);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(10).userID, ((VH12) holder).binding.userProfileImageNew10,
                                ((VH12) holder).binding.txtViewHeadingNew10,
                                ((VH12) holder).binding.tvDiamond10,
                                userList.get(10).userName);
                        if (!userList.get(10).isCameraOpen()) {
                            ((VH12) holder).binding.blurLay10.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH12) holder).binding.blur10, ((VH12) holder).binding.blurDP10);
                        } else {
                            ((VH12) holder).binding.blurLay10.setVisibility(View.GONE);
                        }
                    } else if (i == 11) {
                        setVideoView(userList.get(11), ((VH12) holder).binding.cohostView11);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(11).userID, ((VH12) holder).binding.userProfileImageNew11,
                                ((VH12) holder).binding.txtViewHeadingNew11,
                                ((VH12) holder).binding.tvDiamond11,
                                userList.get(11).userName);
                        if (!userList.get(11).isCameraOpen()) {
                            ((VH12) holder).binding.blurLay11.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH12) holder).binding.blur11, ((VH12) holder).binding.blurDP11);
                        } else {
                            ((VH12) holder).binding.blurLay11.setVisibility(View.GONE);
                        }
                    } else {
                        setVideoView(userList.get(12), ((VH12) holder).binding.cohostView12);
                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(12).userID, ((VH12) holder).binding.userProfileImageNew12,
                                    ((VH12) holder).binding.txtViewHeadingNew12,
                                    ((VH12) holder).binding.tvDiamond12,
                                    userList.get(12).userName);
                            if (!userList.get(12).isCameraOpen()) {
                                ((VH12) holder).binding.blurLay12.setVisibility(View.VISIBLE);
                                setCameraUpdate(userList.get(i).userID, coHost, ((VH12) holder).binding.blur12, ((VH12) holder).binding.blurDP12);
                            } else {
                                ((VH12) holder).binding.blurLay12.setVisibility(View.GONE);
                            }

                        }else{
                            if (!userList.get(12).isCameraOpen()) {
                                ((VH12) holder).binding.blurLay12.setVisibility(View.VISIBLE);
                            } else {
                                ((VH12) holder).binding.blurLay12.setVisibility(View.GONE);
                            }

                            setGemView2(userList.get(12).userID, ((VH12) holder).binding.userProfileImageNew12,
                                    ((VH12) holder).binding.txtViewHeadingNew12,
                                    ((VH12) holder).binding.tvDiamond12,
                                    userList.get(12).userName, ((VH12) holder).binding.blur12,((VH12) holder).binding.blurDP12 );
                        }
                    }
                }

                for (int i = 0; i < userList.size(); i++) {
                    final int idx = i;
                    if (idx == 0) {
                        ((VH12) holder).binding.hostView.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH12) holder).binding.blurLay.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH12) holder).binding.blurDP.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH12) holder).binding.txtViewHeadingNew.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName, v);
                        });
                    } else {
                        // for cohosts
                        // dynamically setting click listeners is easier by checking the exact index we are loops over, but since they are statically mapped views, we switch/if over idx:
                        switch (idx) {
                            case 1:
                                ((VH12) holder).binding.cohostView1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH12) holder).binding.blurLay1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH12) holder).binding.txtViewHeadingNew1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName, v);
                                });
                                break;
                            case 2:
                                ((VH12) holder).binding.cohostView2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH12) holder).binding.blurLay2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH12) holder).binding.txtViewHeadingNew2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "profile", userList.get(2).userName, v);
                                });
                                break;
                            case 3:
                                ((VH12) holder).binding.cohostView3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH12) holder).binding.blurLay3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH12) holder).binding.txtViewHeadingNew3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "profile", userList.get(3).userName, v);
                                });
                                break;
                            case 4:
                                ((VH12) holder).binding.cohostView4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH12) holder).binding.blurLay4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH12) holder).binding.txtViewHeadingNew4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "profile", userList.get(4).userName, v);
                                });
                                break;
                            case 5:
                                ((VH12) holder).binding.cohostView5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH12) holder).binding.blurLay5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH12) holder).binding.txtViewHeadingNew5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "profile", userList.get(5).userName, v);
                                });
                                break;
                            case 6:
                                ((VH12) holder).binding.cohostView6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH12) holder).binding.blurLay6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH12) holder).binding.txtViewHeadingNew6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "profile", userList.get(6).userName, v);
                                });
                                break;
                            case 7:
                                ((VH12) holder).binding.cohostView7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH12) holder).binding.blurLay7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH12) holder).binding.txtViewHeadingNew7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "profile", userList.get(7).userName, v);
                                });
                                break;
                            case 8:
                                ((VH12) holder).binding.cohostView8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH12) holder).binding.blurLay8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH12) holder).binding.txtViewHeadingNew8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "profile", userList.get(8).userName, v);
                                });
                                break;
                            case 9:
                                ((VH12) holder).binding.cohostView9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "gift", userList.get(9).userName, v);
                                });
                                ((VH12) holder).binding.blurLay9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "gift", userList.get(9).userName, v);
                                });
                                ((VH12) holder).binding.txtViewHeadingNew9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "profile", userList.get(9).userName, v);
                                });
                                break;
                            case 10:
                                ((VH12) holder).binding.cohostView10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "gift", userList.get(10).userName, v);
                                });
                                ((VH12) holder).binding.blurLay10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "gift", userList.get(10).userName, v);
                                });
                                ((VH12) holder).binding.txtViewHeadingNew10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "profile", userList.get(10).userName, v);
                                });
                                break;
                            case 11:
                                ((VH12) holder).binding.cohostView11.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(11).userID, "gift", userList.get(11).userName, v);
                                });
                                ((VH12) holder).binding.blurLay11.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(11).userID, "gift", userList.get(11).userName, v);
                                });
                                ((VH12) holder).binding.txtViewHeadingNew11.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(11).userID, "profile", userList.get(11).userName, v);
                                });
                                break;
                            case 12:
                                ((VH12) holder).binding.cohostView12.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(12).userID, "gift", userList.get(12).userName, v);
                                });
                                ((VH12) holder).binding.blurLay12.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(12).userID, "gift", userList.get(12).userName, v);
                                });
                                ((VH12) holder).binding.txtViewHeadingNew12.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(12).userID, "profile", userList.get(12).userName, v);
                                });
                                break;
                        }
                    }
                }
            }



            else if (holder instanceof VH13) {
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH13) holder).binding.hostView.getWidth();
                            height = ((VH13) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);
                    if (i == 0) {
                        setVideoView(userList.get(0), ((VH13) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH13) holder).binding.userProfileImageNew,
                                ((VH13) holder).binding.txtViewHeadingNew,
                                ((VH13) holder).binding.tvDiamond,
                                userList.get(0).userName);

                        if (!userList.get(0).isCameraOpen()) {
                            ((VH13) holder).binding.blurLay.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blurNewchange, ((VH13) holder).binding.blurDP);
                        } else {
                            ((VH13) holder).binding.blurLay.setVisibility(View.GONE);
                        }
                    } else if (i == 1) {
                        setVideoView(userList.get(1), ((VH13) holder).binding.cohostView1);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH13) holder).binding.userProfileImageNew1,
                                ((VH13) holder).binding.txtViewHeadingNew1,
                                ((VH13) holder).binding.tvDiamond1,
                                userList.get(1).userName);
                        if (!userList.get(1).isCameraOpen()) {
                            ((VH13) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blur1, ((VH13) holder).binding.blurDP1);
                        } else {
                            ((VH13) holder).binding.blurLay1.setVisibility(View.GONE);
                        }
                    } else if (i == 2) {
                        setVideoView(userList.get(2), ((VH13) holder).binding.cohostView2);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(2).userID, ((VH13) holder).binding.userProfileImageNew2,
                                ((VH13) holder).binding.txtViewHeadingNew2,
                                ((VH13) holder).binding.tvDiamond2,
                                userList.get(2).userName);
                        if (!userList.get(2).isCameraOpen()) {
                            ((VH13) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blur2, ((VH13) holder).binding.blurDP2);
                        } else {
                            ((VH13) holder).binding.blurLay2.setVisibility(View.GONE);
                        }
                    } else if (i == 3) {
                        setVideoView(userList.get(3), ((VH13) holder).binding.cohostView3);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(3).userID, ((VH13) holder).binding.userProfileImageNew3,
                                ((VH13) holder).binding.txtViewHeadingNew3,
                                ((VH13) holder).binding.tvDiamond3,
                                userList.get(3).userName);
                        if (!userList.get(3).isCameraOpen()) {
                            ((VH13) holder).binding.blurLay3.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blur3, ((VH13) holder).binding.blurDP3);
                        } else {
                            ((VH13) holder).binding.blurLay3.setVisibility(View.GONE);
                        }
                    } else if (i == 4) {
                        setVideoView(userList.get(4), ((VH13) holder).binding.cohostView4);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(4).userID, ((VH13) holder).binding.userProfileImageNew4,
                                ((VH13) holder).binding.txtViewHeadingNew4,
                                ((VH13) holder).binding.tvDiamond4,
                                userList.get(4).userName);
                        if (!userList.get(4).isCameraOpen()) {
                            ((VH13) holder).binding.blurLay4.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blur4, ((VH13) holder).binding.blurDP4);
                        } else {
                            ((VH13) holder).binding.blurLay4.setVisibility(View.GONE);
                        }
                    } else if (i == 5) {
                        setVideoView(userList.get(5), ((VH13) holder).binding.cohostView5);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(5).userID, ((VH13) holder).binding.userProfileImageNew5,
                                ((VH13) holder).binding.txtViewHeadingNew5,
                                ((VH13) holder).binding.tvDiamond5,
                                userList.get(5).userName);
                        if (!userList.get(5).isCameraOpen()) {
                            ((VH13) holder).binding.blurLay5.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blur5, ((VH13) holder).binding.blurDP5);
                        } else {
                            ((VH13) holder).binding.blurLay5.setVisibility(View.GONE);
                        }
                    } else if (i == 6) {
                        setVideoView(userList.get(6), ((VH13) holder).binding.cohostView6);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(6).userID, ((VH13) holder).binding.userProfileImageNew6,
                                ((VH13) holder).binding.txtViewHeadingNew6,
                                ((VH13) holder).binding.tvDiamond6,
                                userList.get(6).userName);
                        if (!userList.get(6).isCameraOpen()) {
                            ((VH13) holder).binding.blurLay6.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blur6, ((VH13) holder).binding.blurDP6);
                        } else {
                            ((VH13) holder).binding.blurLay6.setVisibility(View.GONE);
                        }
                    } else if (i == 7) {
                        setVideoView(userList.get(7), ((VH13) holder).binding.cohostView7);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(7).userID, ((VH13) holder).binding.userProfileImageNew7,
                                ((VH13) holder).binding.txtViewHeadingNew7,
                                ((VH13) holder).binding.tvDiamond7,
                                userList.get(7).userName);
                        if (!userList.get(7).isCameraOpen()) {
                            ((VH13) holder).binding.blurLay7.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blur7, ((VH13) holder).binding.blurDP7);
                        } else {
                            ((VH13) holder).binding.blurLay7.setVisibility(View.GONE);
                        }
                    } else if (i == 8) {
                        setVideoView(userList.get(8), ((VH13) holder).binding.cohostView8);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(8).userID, ((VH13) holder).binding.userProfileImageNew8,
                                ((VH13) holder).binding.txtViewHeadingNew8,
                                ((VH13) holder).binding.tvDiamond8,
                                userList.get(8).userName);
                        if (!userList.get(8).isCameraOpen()) {
                            ((VH13) holder).binding.blurLay8.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blur8, ((VH13) holder).binding.blurDP8);
                        } else {
                            ((VH13) holder).binding.blurLay8.setVisibility(View.GONE);
                        }
                    } else if (i == 9) {
                        setVideoView(userList.get(9), ((VH13) holder).binding.cohostView9);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(9).userID, ((VH13) holder).binding.userProfileImageNew9,
                                ((VH13) holder).binding.txtViewHeadingNew9,
                                ((VH13) holder).binding.tvDiamond9,
                                userList.get(9).userName);
                        if (!userList.get(9).isCameraOpen()) {
                            ((VH13) holder).binding.blurLay9.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blur9, ((VH13) holder).binding.blurDP9);
                        } else {
                            ((VH13) holder).binding.blurLay9.setVisibility(View.GONE);
                        }
                    } else if (i == 10) {
                        setVideoView(userList.get(10), ((VH13) holder).binding.cohostView10);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(10).userID, ((VH13) holder).binding.userProfileImageNew10,
                                ((VH13) holder).binding.txtViewHeadingNew10,
                                ((VH13) holder).binding.tvDiamond10,
                                userList.get(10).userName);
                        if (!userList.get(10).isCameraOpen()) {
                            ((VH13) holder).binding.blurLay10.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blur10, ((VH13) holder).binding.blurDP10);
                        } else {
                            ((VH13) holder).binding.blurLay10.setVisibility(View.GONE);
                        }
                    } else if (i == 11) {
                        setVideoView(userList.get(11), ((VH13) holder).binding.cohostView11);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(11).userID, ((VH13) holder).binding.userProfileImageNew11,
                                ((VH13) holder).binding.txtViewHeadingNew11,
                                ((VH13) holder).binding.tvDiamond11,
                                userList.get(11).userName);
                        if (!userList.get(11).isCameraOpen()) {
                            ((VH13) holder).binding.blurLay11.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blur11, ((VH13) holder).binding.blurDP11);
                        } else {
                            ((VH13) holder).binding.blurLay11.setVisibility(View.GONE);
                        }
                    } else if (i == 12) {
                        setVideoView(userList.get(12), ((VH13) holder).binding.cohostView12);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(12).userID, ((VH13) holder).binding.userProfileImageNew12,
                                ((VH13) holder).binding.txtViewHeadingNew12,
                                ((VH13) holder).binding.tvDiamond12,
                                userList.get(12).userName);
                        if (!userList.get(12).isCameraOpen()) {
                            ((VH13) holder).binding.blurLay12.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blur12, ((VH13) holder).binding.blurDP12);
                        } else {
                            ((VH13) holder).binding.blurLay12.setVisibility(View.GONE);
                        }
                    } else {
                        setVideoView(userList.get(13), ((VH13) holder).binding.cohostView13);
                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(13).userID, ((VH13) holder).binding.userProfileImageNew13,
                                    ((VH13) holder).binding.txtViewHeadingNew13,
                                    ((VH13) holder).binding.tvDiamond13,
                                    userList.get(13).userName);
                            if (!userList.get(13).isCameraOpen()) {
                                ((VH13) holder).binding.blurLay13.setVisibility(View.VISIBLE);
                                setCameraUpdate(userList.get(i).userID, coHost, ((VH13) holder).binding.blur13, ((VH13) holder).binding.blurDP13);
                            } else {
                                ((VH13) holder).binding.blurLay13.setVisibility(View.GONE);
                            }

                        }else{
                            if (!userList.get(13).isCameraOpen()) {
                                ((VH13) holder).binding.blurLay13.setVisibility(View.VISIBLE);
                            } else {
                                ((VH13) holder).binding.blurLay13.setVisibility(View.GONE);
                            }

                            setGemView2(userList.get(13).userID, ((VH13) holder).binding.userProfileImageNew13,
                                    ((VH13) holder).binding.txtViewHeadingNew13,
                                    ((VH13) holder).binding.tvDiamond13,
                                    userList.get(13).userName, ((VH13) holder).binding.blur13,((VH13) holder).binding.blurDP13 );
                        }
                    }
                }

                for (int i = 0; i < userList.size(); i++) {
                    final int idx = i;
                    if (idx == 0) {
                        ((VH13) holder).binding.hostView.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH13) holder).binding.blurLay.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH13) holder).binding.blurDP.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH13) holder).binding.txtViewHeadingNew.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName, v);
                        });
                    } else {
                        // for cohosts
                        // dynamically setting click listeners is easier by checking the exact index we are loops over, but since they are statically mapped views, we switch/if over idx:
                        switch (idx) {
                            case 1:
                                ((VH13) holder).binding.cohostView1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH13) holder).binding.blurLay1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH13) holder).binding.txtViewHeadingNew1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName, v);
                                });
                                break;
                            case 2:
                                ((VH13) holder).binding.cohostView2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH13) holder).binding.blurLay2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH13) holder).binding.txtViewHeadingNew2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "profile", userList.get(2).userName, v);
                                });
                                break;
                            case 3:
                                ((VH13) holder).binding.cohostView3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH13) holder).binding.blurLay3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH13) holder).binding.txtViewHeadingNew3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "profile", userList.get(3).userName, v);
                                });
                                break;
                            case 4:
                                ((VH13) holder).binding.cohostView4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH13) holder).binding.blurLay4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH13) holder).binding.txtViewHeadingNew4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "profile", userList.get(4).userName, v);
                                });
                                break;
                            case 5:
                                ((VH13) holder).binding.cohostView5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH13) holder).binding.blurLay5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH13) holder).binding.txtViewHeadingNew5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "profile", userList.get(5).userName, v);
                                });
                                break;
                            case 6:
                                ((VH13) holder).binding.cohostView6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH13) holder).binding.blurLay6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH13) holder).binding.txtViewHeadingNew6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "profile", userList.get(6).userName, v);
                                });
                                break;
                            case 7:
                                ((VH13) holder).binding.cohostView7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH13) holder).binding.blurLay7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH13) holder).binding.txtViewHeadingNew7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "profile", userList.get(7).userName, v);
                                });
                                break;
                            case 8:
                                ((VH13) holder).binding.cohostView8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH13) holder).binding.blurLay8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH13) holder).binding.txtViewHeadingNew8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "profile", userList.get(8).userName, v);
                                });
                                break;
                            case 9:
                                ((VH13) holder).binding.cohostView9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "gift", userList.get(9).userName, v);
                                });
                                ((VH13) holder).binding.blurLay9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "gift", userList.get(9).userName, v);
                                });
                                ((VH13) holder).binding.txtViewHeadingNew9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "profile", userList.get(9).userName, v);
                                });
                                break;
                            case 10:
                                ((VH13) holder).binding.cohostView10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "gift", userList.get(10).userName, v);
                                });
                                ((VH13) holder).binding.blurLay10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "gift", userList.get(10).userName, v);
                                });
                                ((VH13) holder).binding.txtViewHeadingNew10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "profile", userList.get(10).userName, v);
                                });
                                break;
                            case 11:
                                ((VH13) holder).binding.cohostView11.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(11).userID, "gift", userList.get(11).userName, v);
                                });
                                ((VH13) holder).binding.blurLay11.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(11).userID, "gift", userList.get(11).userName, v);
                                });
                                ((VH13) holder).binding.txtViewHeadingNew11.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(11).userID, "profile", userList.get(11).userName, v);
                                });
                                break;
                            case 12:
                                ((VH13) holder).binding.cohostView12.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(12).userID, "gift", userList.get(12).userName, v);
                                });
                                ((VH13) holder).binding.blurLay12.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(12).userID, "gift", userList.get(12).userName, v);
                                });
                                ((VH13) holder).binding.txtViewHeadingNew12.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(12).userID, "profile", userList.get(12).userName, v);
                                });
                                break;
                            case 13:
                                ((VH13) holder).binding.cohostView13.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(13).userID, "gift", userList.get(13).userName, v);
                                });
                                ((VH13) holder).binding.blurLay13.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(13).userID, "gift", userList.get(13).userName, v);
                                });
                                ((VH13) holder).binding.txtViewHeadingNew13.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(13).userID, "profile", userList.get(13).userName, v);
                                });
                                break;
                        }
                    }
                }
            }

            else if (holder instanceof VH14) {
                for (int i = 0; i < userList.size(); i++) {
                    ZEGOSDKUser user = userList.get(i);

                    holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            width = ((VH14) holder).binding.hostView.getWidth();
                            height = ((VH14) holder).binding.hostView.getHeight();
                            onCoHostClick.onFirstItemSizeCalculated(width, height,viewType);
                        }
                    });
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);
                    if (i == 0) {
                        setVideoView(userList.get(0), ((VH14) holder).binding.hostView);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(0).userID, ((VH14) holder).binding.userProfileImageNew,
                                ((VH14) holder).binding.txtViewHeadingNew,
                                ((VH14) holder).binding.tvDiamond,
                                userList.get(0).userName);

                        if (!userList.get(0).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blurNewchange, ((VH14) holder).binding.blurDP);
                        } else {
                            ((VH14) holder).binding.blurLay.setVisibility(View.GONE);
                        }
                    } else if (i == 1) {
                        setVideoView(userList.get(1), ((VH14) holder).binding.cohostView1);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(1).userID, ((VH14) holder).binding.userProfileImageNew1,
                                ((VH14) holder).binding.txtViewHeadingNew1,
                                ((VH14) holder).binding.tvDiamond1,
                                userList.get(1).userName);
                        if (!userList.get(1).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay1.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur1, ((VH14) holder).binding.blurDP1);
                        } else {
                            ((VH14) holder).binding.blurLay1.setVisibility(View.GONE);
                        }
                    } else if (i == 2) {
                        setVideoView(userList.get(2), ((VH14) holder).binding.cohostView2);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(2).userID, ((VH14) holder).binding.userProfileImageNew2,
                                ((VH14) holder).binding.txtViewHeadingNew2,
                                ((VH14) holder).binding.tvDiamond2,
                                userList.get(2).userName);
                        if (!userList.get(2).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay2.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur2, ((VH14) holder).binding.blurDP2);
                        } else {
                            ((VH14) holder).binding.blurLay2.setVisibility(View.GONE);
                        }
                    } else if (i == 3) {
                        setVideoView(userList.get(3), ((VH14) holder).binding.cohostView3);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(3).userID, ((VH14) holder).binding.userProfileImageNew3,
                                ((VH14) holder).binding.txtViewHeadingNew3,
                                ((VH14) holder).binding.tvDiamond3,
                                userList.get(3).userName);
                        if (!userList.get(3).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay3.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur3, ((VH14) holder).binding.blurDP3);
                        } else {
                            ((VH14) holder).binding.blurLay3.setVisibility(View.GONE);
                        }
                    } else if (i == 4) {
                        setVideoView(userList.get(4), ((VH14) holder).binding.cohostView4);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(4).userID, ((VH14) holder).binding.userProfileImageNew4,
                                ((VH14) holder).binding.txtViewHeadingNew4,
                                ((VH14) holder).binding.tvDiamond4,
                                userList.get(4).userName);
                        if (!userList.get(4).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay4.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur4, ((VH14) holder).binding.blurDP4);
                        } else {
                            ((VH14) holder).binding.blurLay4.setVisibility(View.GONE);
                        }
                    } else if (i == 5) {
                        setVideoView(userList.get(5), ((VH14) holder).binding.cohostView5);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(5).userID, ((VH14) holder).binding.userProfileImageNew5,
                                ((VH14) holder).binding.txtViewHeadingNew5,
                                ((VH14) holder).binding.tvDiamond5,
                                userList.get(5).userName);
                        if (!userList.get(5).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay5.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur5, ((VH14) holder).binding.blurDP5);
                        } else {
                            ((VH14) holder).binding.blurLay5.setVisibility(View.GONE);
                        }
                    } else if (i == 6) {
                        setVideoView(userList.get(6), ((VH14) holder).binding.cohostView6);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(6).userID, ((VH14) holder).binding.userProfileImageNew6,
                                ((VH14) holder).binding.txtViewHeadingNew6,
                                ((VH14) holder).binding.tvDiamond6,
                                userList.get(6).userName);
                        if (!userList.get(6).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay6.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur6, ((VH14) holder).binding.blurDP6);
                        } else {
                            ((VH14) holder).binding.blurLay6.setVisibility(View.GONE);
                        }
                    } else if (i == 7) {
                        setVideoView(userList.get(7), ((VH14) holder).binding.cohostView7);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(7).userID, ((VH14) holder).binding.userProfileImageNew7,
                                ((VH14) holder).binding.txtViewHeadingNew7,
                                ((VH14) holder).binding.tvDiamond7,
                                userList.get(7).userName);
                        if (!userList.get(7).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay7.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur7, ((VH14) holder).binding.blurDP7);
                        } else {
                            ((VH14) holder).binding.blurLay7.setVisibility(View.GONE);
                        }
                    } else if (i == 8) {
                        setVideoView(userList.get(8), ((VH14) holder).binding.cohostView8);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(8).userID, ((VH14) holder).binding.userProfileImageNew8,
                                ((VH14) holder).binding.txtViewHeadingNew8,
                                ((VH14) holder).binding.tvDiamond8,
                                userList.get(8).userName);
                        if (!userList.get(8).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay8.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur8, ((VH14) holder).binding.blurDP8);
                        } else {
                            ((VH14) holder).binding.blurLay8.setVisibility(View.GONE);
                        }
                    } else if (i == 9) {
                        setVideoView(userList.get(9), ((VH14) holder).binding.cohostView9);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(9).userID, ((VH14) holder).binding.userProfileImageNew9,
                                ((VH14) holder).binding.txtViewHeadingNew9,
                                ((VH14) holder).binding.tvDiamond9,
                                userList.get(9).userName);
                        if (!userList.get(9).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay9.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur9, ((VH14) holder).binding.blurDP9);
                        } else {
                            ((VH14) holder).binding.blurLay9.setVisibility(View.GONE);
                        }
                    } else if (i == 10) {
                        setVideoView(userList.get(10), ((VH14) holder).binding.cohostView10);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(10).userID, ((VH14) holder).binding.userProfileImageNew10,
                                ((VH14) holder).binding.txtViewHeadingNew10,
                                ((VH14) holder).binding.tvDiamond10,
                                userList.get(10).userName);
                        if (!userList.get(10).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay10.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur10, ((VH14) holder).binding.blurDP10);
                        } else {
                            ((VH14) holder).binding.blurLay10.setVisibility(View.GONE);
                        }
                    } else if (i == 11) {
                        setVideoView(userList.get(11), ((VH14) holder).binding.cohostView11);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(11).userID, ((VH14) holder).binding.userProfileImageNew11,
                                ((VH14) holder).binding.txtViewHeadingNew11,
                                ((VH14) holder).binding.tvDiamond11,
                                userList.get(11).userName);
                        if (!userList.get(11).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay11.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur11, ((VH14) holder).binding.blurDP11);
                        } else {
                            ((VH14) holder).binding.blurLay11.setVisibility(View.GONE);
                        }
                    } else if (i == 12) {
                        setVideoView(userList.get(12), ((VH14) holder).binding.cohostView12);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(12).userID, ((VH14) holder).binding.userProfileImageNew12,
                                ((VH14) holder).binding.txtViewHeadingNew12,
                                ((VH14) holder).binding.tvDiamond12,
                                userList.get(12).userName);
                        if (!userList.get(12).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay12.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur12, ((VH14) holder).binding.blurDP12);
                        } else {
                            ((VH14) holder).binding.blurLay12.setVisibility(View.GONE);
                        }
                    } else if (i == 13) {
                        setVideoView(userList.get(13), ((VH14) holder).binding.cohostView13);
                        StreamDetails.StreamCohost coHost = setGemView(userList.get(13).userID, ((VH14) holder).binding.userProfileImageNew13,
                                ((VH14) holder).binding.txtViewHeadingNew13,
                                ((VH14) holder).binding.tvDiamond13,
                                userList.get(13).userName);
                        if (!userList.get(13).isCameraOpen()) {
                            ((VH14) holder).binding.blurLay13.setVisibility(View.VISIBLE);
                            setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur13, ((VH14) holder).binding.blurDP13);
                        } else {
                            ((VH14) holder).binding.blurLay13.setVisibility(View.GONE);
                        }
                    } else {
                        setVideoView(userList.get(14), ((VH14) holder).binding.cohostView14);
                        if(gem.size() == userList.size()) {
                            StreamDetails.StreamCohost coHost = setGemView(userList.get(14).userID, ((VH14) holder).binding.userProfileImageNew14,
                                    ((VH14) holder).binding.txtViewHeadingNew14,
                                    ((VH14) holder).binding.tvDiamond14,
                                    userList.get(14).userName);
                            if (!userList.get(14).isCameraOpen()) {
                                ((VH14) holder).binding.blurLay14.setVisibility(View.VISIBLE);
                                setCameraUpdate(userList.get(i).userID, coHost, ((VH14) holder).binding.blur14, ((VH14) holder).binding.blurDP14);
                            } else {
                                ((VH14) holder).binding.blurLay14.setVisibility(View.GONE);
                            }

                        }else{
                            if (!userList.get(14).isCameraOpen()) {
                                ((VH14) holder).binding.blurLay14.setVisibility(View.VISIBLE);
                            } else {
                                ((VH14) holder).binding.blurLay14.setVisibility(View.GONE);
                            }

                            setGemView2(userList.get(14).userID, ((VH14) holder).binding.userProfileImageNew14,
                                    ((VH14) holder).binding.txtViewHeadingNew14,
                                    ((VH14) holder).binding.tvDiamond14,
                                    userList.get(14).userName, ((VH14) holder).binding.blur14,((VH14) holder).binding.blurDP14 );
                        }
                    }
                }

                for (int i = 0; i < userList.size(); i++) {
                    final int idx = i;
                    if (idx == 0) {
                        ((VH14) holder).binding.hostView.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH14) holder).binding.blurLay.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH14) holder).binding.blurDP.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "heart", userList.get(0).userName, v);
                        });
                        ((VH14) holder).binding.txtViewHeadingNew.setOnClickListener(v -> {
                            onCoHostClick.onCoHostClick(userList.get(0).userID, "profile", userList.get(0).userName, v);
                        });
                    } else {
                        // for cohosts
                        // dynamically setting click listeners is easier by checking the exact index we are loops over, but since they are statically mapped views, we switch/if over idx:
                        switch (idx) {
                            case 1:
                                ((VH14) holder).binding.cohostView1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH14) holder).binding.blurLay1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "gift", userList.get(1).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew1.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(1).userID, "profile", userList.get(1).userName, v);
                                });
                                break;
                            case 2:
                                ((VH14) holder).binding.cohostView2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH14) holder).binding.blurLay2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "gift", userList.get(2).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew2.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(2).userID, "profile", userList.get(2).userName, v);
                                });
                                break;
                            case 3:
                                ((VH14) holder).binding.cohostView3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH14) holder).binding.blurLay3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "gift", userList.get(3).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew3.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(3).userID, "profile", userList.get(3).userName, v);
                                });
                                break;
                            case 4:
                                ((VH14) holder).binding.cohostView4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH14) holder).binding.blurLay4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "gift", userList.get(4).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew4.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(4).userID, "profile", userList.get(4).userName, v);
                                });
                                break;
                            case 5:
                                ((VH14) holder).binding.cohostView5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH14) holder).binding.blurLay5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "gift", userList.get(5).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew5.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(5).userID, "profile", userList.get(5).userName, v);
                                });
                                break;
                            case 6:
                                ((VH14) holder).binding.cohostView6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH14) holder).binding.blurLay6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "gift", userList.get(6).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew6.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(6).userID, "profile", userList.get(6).userName, v);
                                });
                                break;
                            case 7:
                                ((VH14) holder).binding.cohostView7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH14) holder).binding.blurLay7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "gift", userList.get(7).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew7.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(7).userID, "profile", userList.get(7).userName, v);
                                });
                                break;
                            case 8:
                                ((VH14) holder).binding.cohostView8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH14) holder).binding.blurLay8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "gift", userList.get(8).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew8.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(8).userID, "profile", userList.get(8).userName, v);
                                });
                                break;
                            case 9:
                                ((VH14) holder).binding.cohostView9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "gift", userList.get(9).userName, v);
                                });
                                ((VH14) holder).binding.blurLay9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "gift", userList.get(9).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew9.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(9).userID, "profile", userList.get(9).userName, v);
                                });
                                break;
                            case 10:
                                ((VH14) holder).binding.cohostView10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "gift", userList.get(10).userName, v);
                                });
                                ((VH14) holder).binding.blurLay10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "gift", userList.get(10).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew10.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(10).userID, "profile", userList.get(10).userName, v);
                                });
                                break;
                            case 11:
                                ((VH14) holder).binding.cohostView11.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(11).userID, "gift", userList.get(11).userName, v);
                                });
                                ((VH14) holder).binding.blurLay11.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(11).userID, "gift", userList.get(11).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew11.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(11).userID, "profile", userList.get(11).userName, v);
                                });
                                break;
                            case 12:
                                ((VH14) holder).binding.cohostView12.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(12).userID, "gift", userList.get(12).userName, v);
                                });
                                ((VH14) holder).binding.blurLay12.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(12).userID, "gift", userList.get(12).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew12.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(12).userID, "profile", userList.get(12).userName, v);
                                });
                                break;
                            case 13:
                                ((VH14) holder).binding.cohostView13.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(13).userID, "gift", userList.get(13).userName, v);
                                });
                                ((VH14) holder).binding.blurLay13.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(13).userID, "gift", userList.get(13).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew13.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(13).userID, "profile", userList.get(13).userName, v);
                                });
                                break;
                            case 14:
                                ((VH14) holder).binding.cohostView14.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(14).userID, "gift", userList.get(14).userName, v);
                                });
                                ((VH14) holder).binding.blurLay14.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(14).userID, "gift", userList.get(14).userName, v);
                                });
                                ((VH14) holder).binding.txtViewHeadingNew14.setOnClickListener(v -> {
                                    App.preventMultipleClick(v);
                                    onCoHostClick.onCoHostClick(userList.get(14).userID, "profile", userList.get(14).userName, v);
                                });
                                break;
                        }
                    }
                }
            }
        } else {
            Log.e("TAG", "onBindViewHolder: ::::::::else");
        }
    }

    private MaterialCardView getCardViewForUser(Object holder, int userIndex) {
       /* if (holder instanceof VH1 vh) {
            switch (userIndex) {
                case 0: return vh.binding.cv;
                case 1: return vh.binding.cv1;
            }
        } else if (holder instanceof VH2 vh) {
            switch (userIndex) {
                case 0: return vh.binding.cv;
                case 1: return vh.binding.cv1;
                case 2: return vh.binding.cv2;
            }
        } else if (holder instanceof VH3 vh) {
            switch (userIndex) {
                case 0: return vh.binding.cv;
                case 1: return vh.binding.cv1;
                case 2: return vh.binding.cv2;
                case 3: return vh.binding.cv3;
            }
        } else if (holder instanceof VH4 vh) {
            switch (userIndex) {
                case 0: return vh.binding.cv;
                case 1: return vh.binding.cv1;
                case 2: return vh.binding.cv2;
                case 3: return vh.binding.cv3;
                case 4: return vh.binding.cv4;
            }
        } else if (holder instanceof VH5 vh) {
            switch (userIndex) {
                case 0: return vh.binding.cv;
                case 1: return vh.binding.cv1;
                case 2: return vh.binding.cv2;
                case 3: return vh.binding.cv3;
                case 4: return vh.binding.cv4;
                case 5: return vh.binding.cv5;
            }
        } else if (holder instanceof VH6 vh) {
            switch (userIndex) {
                case 0: return vh.binding.cv;
                case 1: return vh.binding.cv1;
                case 2: return vh.binding.cv2;
                case 3: return vh.binding.cv3;
                case 4: return vh.binding.cv4;
                case 5: return vh.binding.cv5;
                case 6: return vh.binding.cv6;
            }
        } else if (holder instanceof VH7 vh) {
            switch (userIndex) {
                case 0: return vh.binding.cv;
                case 1: return vh.binding.cv1;
                case 2: return vh.binding.cv2;
                case 3: return vh.binding.cv3;
                case 4: return vh.binding.cv4;
                case 5: return vh.binding.cv5;
                case 6: return vh.binding.cv6;
                case 7: return vh.binding.cv7;
            }
        } else if (holder instanceof VH8 vh) {
            switch (userIndex) {
                case 0: return vh.binding.cv;
                case 1: return vh.binding.cv1;
                case 2: return vh.binding.cv2;
                case 3: return vh.binding.cv3;
                case 4: return vh.binding.cv4;
                case 5: return vh.binding.cv5;
                case 6: return vh.binding.cv6;
                case 7: return vh.binding.cv7;
                case 8: return vh.binding.cv8;
            }
        }*/
        return null;
    }

    private void handleRemoteSoundAnimation(List<ZEGOSDKUser> userList, String streamId, float value, RecyclerView.ViewHolder holder) {
        for (int i = 0; i < userList.size(); i++) {
            String mainStreamId = userList.get(i).getMainStreamID();
            if (mainStreamId != null && !mainStreamId.isEmpty() && streamId != null){
                if (userList.get(i).getMainStreamID().equalsIgnoreCase(streamId)) {
                    LottieAnimationView animationView = getSpeechView(holder, i);
                    MaterialCardView cardView = getCardViewForUser(holder, i);
                    if (animationView != null && cardView != null) {
                        if (value > 0.5f) {
                            if (!animationView.isAnimating()) {
                                animationView.playAnimation();
                                cardView.setStrokeWidth((int) context.getResources().getDimension(R.dimen.card_border_width));
                                cardView.setStrokeColor(ContextCompat.getColor(context, R.color.colorRed));
                            }
                        } else {
                            cardView.setStrokeWidth(0);
                        }
                    }
                    break;
                }
            }

        }
    }

    private LottieAnimationView getSpeechView(Object holder, int index) {
      /*  if (holder instanceof VH1 vh) {
            switch (index) {
                case 0: return vh.binding.hostSpeech;
                case 1: return vh.binding.cohostSpeech;
            }
        }else if (holder instanceof VH2 vh1) {
            switch (index) {
                case 0: return vh1.binding.hostSpeech;
                case 1: return vh1.binding.cohostSpeech1;
                case 2: return vh1.binding.cohostSpeech2;
            }
        } else if (holder instanceof VH3 vh2) {
            switch (index) {
                case 0: return vh2.binding.hostSpeech;
                case 1: return vh2.binding.cohostSpeech1;
                case 2: return vh2.binding.cohostSpeech2;
                case 3: return vh2.binding.cohostSpeech3;
            }
        } else if (holder instanceof VH4 vh3) {
            switch (index) {
                case 0: return vh3.binding.hostSpeech;
                case 1: return vh3.binding.cohostSpeech1;
                case 2: return vh3.binding.cohostSpeech2;
                case 3: return vh3.binding.cohostSpeech3;
                case 4: return vh3.binding.cohostSpeech4;
            }
        } else if (holder instanceof VH5 vh4) {
            switch (index) {
                case 0: return vh4.binding.hostSpeech;
                case 1: return vh4.binding.cohostSpeech1;
                case 2: return vh4.binding.cohostSpeech2;
                case 3: return vh4.binding.cohostSpeech3;
                case 4: return vh4.binding.cohostSpeech4;
                case 5: return vh4.binding.cohostSpeech5;
            }
        } else if (holder instanceof VH6 vh5) {
            switch (index) {
                case 0: return vh5.binding.hostSpeech;
                case 1: return vh5.binding.cohostSpeech1;
                case 2: return vh5.binding.cohostSpeech2;
                case 3: return vh5.binding.cohostSpeech3;
                case 4: return vh5.binding.cohostSpeech4;
                case 5: return vh5.binding.cohostSpeech5;
                case 6: return vh5.binding.cohostSpeech6;
            }
        } else if (holder instanceof VH7 vh6) {
            switch (index) {
                case 0: return vh6.binding.hostSpeech;
                case 1: return vh6.binding.cohostSpeech1;
                case 2: return vh6.binding.cohostSpeech2;
                case 3: return vh6.binding.cohostSpeech3;
                case 4: return vh6.binding.cohostSpeech4;
                case 5: return vh6.binding.cohostSpeech5;
                case 6: return vh6.binding.cohostSpeech6;
                case 7: return vh6.binding.cohostSpeech7;
            }
        } else if (holder instanceof VH8 vh7) {
            switch (index) {
                case 0: return vh7.binding.hostSpeech;
                case 1: return vh7.binding.cohostSpeech1;
                case 2: return vh7.binding.cohostSpeech2;
                case 3: return vh7.binding.cohostSpeech3;
                case 4: return vh7.binding.cohostSpeech4;
                case 5: return vh7.binding.cohostSpeech5;
                case 6: return vh7.binding.cohostSpeech6;
                case 7: return vh7.binding.cohostSpeech7;
                case 8: return vh7.binding.cohostSpeech8;
            }
        }*/
        return null;
    }

    private void setVideoView(ZEGOSDKUser userList, ZEGOVideoView videoView) {
        try{
            if (ZEGOSDKManager.getInstance().expressService.isCurrentUser(userList.userID)) {
                if (userList.isCameraOpen()) {
                    videoView.startPreviewOnly();
                }
                if (TextUtils.isEmpty(userList.getMainStreamID())) {
                    ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOLiveStreamingManager.getInstance()
                            .generateUserStreamID(currentUser.userID, currentRoomID);
                    videoView.setStreamID(streamID);
                    videoView.startPublishAudioVideo();
                }
            } else {
                String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(userList.userID, currentRoomID);
                videoView.setStreamID(streamID);
                videoView.startPlayRemoteAudioVideo();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setCameraUpdate(String userID, StreamDetails.StreamCohost coHost, ImageView blur, RoundedImageView blurDP) {
        android.util.Log.d("setCameraUpdate", "setCameraUpdate called for userID: " + userID + ", coHost: " + (coHost != null ? new com.google.gson.Gson().toJson(coHost) : "null"));
        if (coHost != null && coHost.getUserImage() != null && !coHost.getUserImage().isEmpty()) {
            android.util.Log.d("setCameraUpdate", "Loading image from coHost cache: " + coHost.getUserImage());
            int blurRadius = 50;
            BlurTransformation blurTransformation = new BlurTransformation(blurRadius);
            String imageUrl = (coHost.getUserImage().startsWith("https://") || coHost.getUserImage().startsWith("http://")) ? coHost.getUserImage() : (AdminData.mediaURL + coHost.getUserImage());
            
            Glide.with(context)
                    .load(imageUrl)
                    .error(R.drawable.alhawana)
                    .apply(RequestOptions.bitmapTransform(blurTransformation))
                    .into(blur);
            Glide.with(context)
                    .load(imageUrl)
                    .error(R.drawable.alhawana)
                    .into(blurDP);
            blurDP.setVisibility(View.VISIBLE);
        } else {
            android.util.Log.d("setCameraUpdate", "No local cache or empty image, querying API for userID: " + userID);
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(userID);
            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                    try {
                        android.util.Log.d("setCameraUpdate", "API response code: " + response.code() + ", body: " + (response.body() != null ? new com.google.gson.Gson().toJson(response.body()) : "null"));
                        if (response.isSuccessful() && response.body() != null && response.body().getStatus() != null) {
                            if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                                ProfileResponse profile = response.body();
                                String imageUrl = profile.getUserImage();
                                android.util.Log.d("setCameraUpdate", "API image URL: " + imageUrl);
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    String absoluteUrl = (imageUrl.startsWith("https://") || imageUrl.startsWith("http://")) ? imageUrl : (AdminData.mediaURL + imageUrl);
                                    android.util.Log.d("setCameraUpdate", "Loading absolute URL into Glide: " + absoluteUrl);
                                    if (context instanceof Activity) {
                                        Activity activity = (Activity) context;
                                        if (activity.isFinishing() || activity.isDestroyed()) {
                                            android.util.Log.d("setCameraUpdate", "Activity destroyed, aborting Glide load");
                                            return;
                                        }
                                    }
                                    
                                    int blurRadius = 50;
                                    BlurTransformation blurTransformation = new BlurTransformation(blurRadius);
                                    Glide.with(context)
                                            .load(absoluteUrl)
                                            .error(R.drawable.alhawana)
                                            .apply(RequestOptions.bitmapTransform(blurTransformation))
                                            .into(blur);
                                    Glide.with(context)
                                            .load(absoluteUrl)
                                            .error(R.drawable.alhawana)
                                            .into(blurDP);
                                    blurDP.setVisibility(View.VISIBLE);
                                } else {
                                    android.util.Log.e("setCameraUpdate", "API imageUrl is null or empty");
                                }
                            } else {
                                android.util.Log.e("setCameraUpdate", "API returned status: " + response.body().getStatus());
                            }
                        } else {
                            android.util.Log.e("setCameraUpdate", "API response not successful or body null");
                        }
                    } catch (Exception e) {
                        android.util.Log.e("setCameraUpdate", "Exception in API onResponse: " + e.getMessage(), e);
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    android.util.Log.e("setCameraUpdate", "API onFailure: " + t.getMessage(), t);
                    call.cancel();
                }
            });
        }
    }


    private StreamDetails.StreamCohost getUserDetails(String userId) {
        StreamDetails.StreamCohost userDetails = new StreamDetails.StreamCohost();
        for (int i = 0; i < gem.size(); i++) {

            if (Objects.equals(gem.get(i).getUserId(), userId)) {
                userDetails = gem.get(i);
                Log.e("TAG", "setGemView: ::::::::for::" + i + "::::" + new Gson().toJson(userDetails));

                break;
            }
        }
        return userDetails;
    }

    private StreamDetails.StreamCohost setGemView(String userID, RoundedImageView userProfileImageNew, TextView txtViewHeadingNew, TextView tvDiamond, String userName) {

        Log.e("TAG", "setGemView: :::::::" + new Gson().toJson(gem));
        StreamDetails.StreamCohost gemDetails = new StreamDetails.StreamCohost();
        if (userName.length() > 7) {
            // If it exceeds, truncate the text to the maximum length
            String truncatedText = userName.substring(0, 7);
            txtViewHeadingNew.setText(truncatedText + "...");
        } else {
            // If it doesn't exceed, simply set the text
            txtViewHeadingNew.setText(userName);
        }

        for (int i = 0; i < gem.size(); i++) {

            if (Objects.equals(gem.get(i).getUserId(), userID)) {
                gemDetails = gem.get(i);
                Log.e("TAG", "setGemView: ::::::::for::" + i + "::::" + new Gson().toJson(gemDetails));
                Glide.with(context)
                        .load((gemDetails.getUserImage()!=null&&gemDetails.getUserImage().startsWith("https://"))?gemDetails.getUserImage():(AdminData.mediaURL +gemDetails.getUserImage()))
                        .placeholder(R.drawable.alhawana)
                        .error(R.drawable.alhawana)
                        .into(userProfileImageNew);
                try{
                    if (gemDetails.getUserName().length() > 7) {
                        // If it exceeds, truncate the text to the maximum length
                        String truncatedText = gemDetails.getUserName().substring(0, 7);
                        txtViewHeadingNew.setText(truncatedText + "...");
                    } else {
                        // If it doesn't exceed, simply set the text
                        txtViewHeadingNew.setText(userName);
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

                txtViewHeadingNew.setEllipsize(TextUtils.TruncateAt.END); // Add this line
                txtViewHeadingNew.setSingleLine(true); // Add this line
                tvDiamond.setText(Util.formatNumberToAbbreviation(gemDetails.getGemEarned()));

                userProfileImageNew.setOnClickListener(cv -> {
                    App.preventMultipleClick(cv);
                    onCoHostClick.onCoHostClick(userID, "profile", userName,cv);
                });

                break;
            }
        }
        return gemDetails;
    }


    private void setGemView2(String userID, RoundedImageView userProfileImageNew1,
                             TextView txtViewHeadingNew1, TextView tvDiamond1,
                             String userName, ImageView blur1, RoundedImageView blurDP1) {
        if (userName !=null && userName.isEmpty() && userName.length() > 7) {
            String truncatedText = userName.substring(0, 7);
            txtViewHeadingNew1.setText(truncatedText + "...");
        } else {
            txtViewHeadingNew1.setText(userName);
        }
        Log.e("TAG", "setGemView:" + gem.size());

        txtViewHeadingNew1.setEllipsize(TextUtils.TruncateAt.END); // Add this line
        txtViewHeadingNew1.setSingleLine(true); // Add this line

        userProfileImageNew1.setOnClickListener(cv -> {
            App.preventMultipleClick(cv);
            onCoHostClick.onCoHostClick(userID, "profile", userName,cv);
        });

        ProfileRequest request = new ProfileRequest();
        request.setUserId(GetSet.getUserId());
        request.setProfileId(userID);
        Timber.d("getProfile:vh1 %s", new Gson().toJson(request));
        Call<ProfileResponse> call = apiInterface.getProfile(request);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                try{
                    if (response.isSuccessful() && response.body() != null && response.body().getStatus() != null) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            Timber.d("getProfile:vh1 %s", new Gson().toJson(response.body()));
                            ProfileResponse profile = response.body();
                            String imageUrl = (profile.getUserImage() != null && (profile.getUserImage().startsWith("https://") || profile.getUserImage().startsWith("http://"))) ? profile.getUserImage() : (AdminData.mediaURL + profile.getUserImage());
                            Timber.d("getProfile:vh1 imageUrl = %s", imageUrl);

                            // ✅ Context validity check
                            if (context instanceof Activity) {
                                Activity activity = (Activity) context;
                                if (activity.isFinishing() || activity.isDestroyed()) {
                                    Timber.w("Glide load aborted — context is destroyed");
                                    return;
                                }
                            }

                            int blurRadius = 50;
                            BlurTransformation blurTransformation = new BlurTransformation(blurRadius);
                            RequestBuilder<Drawable> thumbnailRequest = Glide.with(context).load(imageUrl);

                            // Load blur image with thumbnail shown first
                            Glide.with(context)
                                    .load(imageUrl)
                                    .thumbnail(thumbnailRequest)
                                    .error(R.drawable.alhawana)
                                    .apply(RequestOptions.bitmapTransform(blurTransformation))
                                    .into(blur1);

                            // Load original image with thumbnail
                            Glide.with(context)
                                    .load(imageUrl)
                                    .thumbnail(thumbnailRequest)
                                    .error(R.drawable.alhawana)
                                    .into(blurDP1);

                            blurDP1.setVisibility(View.VISIBLE);
                        }
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Log.e(TAG, "getProfile: " + t.getMessage());
                call.cancel();
            }
        });

    }

    @Override
    public int getItemViewType(int position) {

        switch (String.valueOf(userList.size())) {
            case "1":
                return VIEW_TYPE_1;
            case "2":
                return VIEW_TYPE_2;
            case "3":
                return VIEW_TYPE_3;
            case "4":
                return VIEW_TYPE_4;
            case "5":
                return VIEW_TYPE_5;
            case "6":
                return VIEW_TYPE_6;
            case "7":
                return VIEW_TYPE_7;
            case "8":
                return VIEW_TYPE_8;
            case "9":
                return VIEW_TYPE_9;
            case "10":
                return VIEW_TYPE_10;
            case "11":
                return VIEW_TYPE_11;
            case "12":
                return VIEW_TYPE_12;
            case "13":
                return VIEW_TYPE_13;
            case "14":
                return VIEW_TYPE_14;
            case "15":
                return VIEW_TYPE_15;
        }
        return VIEW_TYPE_1;

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class VH extends RecyclerView.ViewHolder {
        RvItemBinding binding;

        public VH(@NonNull RvItemBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);
        }
    }

    public class VH1 extends RecyclerView.ViewHolder {
        RvItem2Binding binding;

        public VH1(@NonNull RvItem2Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);
            setZoomControlListener(binding.cohostView);
        }
    }

    public class VH2 extends RecyclerView.ViewHolder {
        RvItem3Binding binding;

        public VH2(@NonNull RvItem3Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);
            setZoomControlListener(binding.cohostView1);
            setZoomControlListener(binding.cohostView2);
        }
    }

    public class VH3 extends RecyclerView.ViewHolder {
        RvItem4Binding binding;

        public VH3(@NonNull RvItem4Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);
            setZoomControlListener(binding.cohostView1);
            setZoomControlListener(binding.cohostView2);
            setZoomControlListener(binding.cohostView3);
        }
    }

    public class VH4 extends RecyclerView.ViewHolder {
        RvItem5Binding binding;

        public VH4(@NonNull RvItem5Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);

            setZoomControlListener(binding.cohostView1);
            setZoomControlListener(binding.cohostView2);
            setZoomControlListener(binding.cohostView3);
            setZoomControlListener(binding.cohostView4);
        }
    }

    public class VH5 extends RecyclerView.ViewHolder {
        RvItem6Binding binding;

        public VH5(@NonNull RvItem6Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);
            setZoomControlListener(binding.cohostView1);
            setZoomControlListener(binding.cohostView2);
            setZoomControlListener(binding.cohostView3);
            setZoomControlListener(binding.cohostView4);
            setZoomControlListener(binding.cohostView5);
        }
    }

    public class VH6 extends RecyclerView.ViewHolder {
        RvItem7Binding binding;

        public VH6(@NonNull RvItem7Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);
            setZoomControlListener(binding.cohostView1);
            setZoomControlListener(binding.cohostView2);
            setZoomControlListener(binding.cohostView3);
            setZoomControlListener(binding.cohostView4);
            setZoomControlListener(binding.cohostView5);
            setZoomControlListener(binding.cohostView6);
        }
    }

    public class VH7 extends RecyclerView.ViewHolder {
        RvItem8Binding binding;

        public VH7(@NonNull RvItem8Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);
            setZoomControlListener(binding.cohostView1);
            setZoomControlListener(binding.cohostView2);
            setZoomControlListener(binding.cohostView3);
            setZoomControlListener(binding.cohostView4);
            setZoomControlListener(binding.cohostView5);
            setZoomControlListener(binding.cohostView6);
            setZoomControlListener(binding.cohostView7);
        }
    }

    public class VH8 extends RecyclerView.ViewHolder {
        RvItem9Binding binding;

        public VH8(@NonNull RvItem9Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);

            setZoomControlListener(binding.hostView);
            setZoomControlListener(binding.cohostView1);
            setZoomControlListener(binding.cohostView2);
            setZoomControlListener(binding.cohostView3);
            setZoomControlListener(binding.cohostView4);
            setZoomControlListener(binding.cohostView5);
            setZoomControlListener(binding.cohostView6);
            setZoomControlListener(binding.cohostView7);
            setZoomControlListener(binding.cohostView8);
        }
    }


    private void applyBlurEffect() {

    }

    private void setZoomControlListener(ZEGOVideoView videoView){
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scaleGestureDetector.onTouchEvent(event);
                return false;
            }
        });

    }

    public void setOnCoHostClickListener(onCoHostClicked listener) {
        this.onCoHostClick = listener;
    }


public class VH9 extends RecyclerView.ViewHolder {
        RvItem10Binding binding;

        public VH9(@NonNull RvItem10Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);
            setZoomControlListener(binding.cohostView1);
            setZoomControlListener(binding.cohostView2);
            setZoomControlListener(binding.cohostView3);
            setZoomControlListener(binding.cohostView4);
            setZoomControlListener(binding.cohostView5);
            setZoomControlListener(binding.cohostView6);
            setZoomControlListener(binding.cohostView7);
            setZoomControlListener(binding.cohostView8);
            setZoomControlListener(binding.cohostView9);
        }
    }

    public class VH10 extends RecyclerView.ViewHolder {
        RvItem11Binding binding;

        public VH10(@NonNull RvItem11Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);
            setZoomControlListener(binding.cohostView1);
            setZoomControlListener(binding.cohostView2);
            setZoomControlListener(binding.cohostView3);
            setZoomControlListener(binding.cohostView4);
            setZoomControlListener(binding.cohostView5);
            setZoomControlListener(binding.cohostView6);
            setZoomControlListener(binding.cohostView7);
            setZoomControlListener(binding.cohostView8);
            setZoomControlListener(binding.cohostView9);
            setZoomControlListener(binding.cohostView10);
        }
    }

    public class VH11 extends RecyclerView.ViewHolder {
        RvItem12Binding binding;

        public VH11(@NonNull RvItem12Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);
            setZoomControlListener(binding.cohostView1);
            setZoomControlListener(binding.cohostView2);
            setZoomControlListener(binding.cohostView3);
            setZoomControlListener(binding.cohostView4);
            setZoomControlListener(binding.cohostView5);
            setZoomControlListener(binding.cohostView6);
            setZoomControlListener(binding.cohostView7);
            setZoomControlListener(binding.cohostView8);
            setZoomControlListener(binding.cohostView9);
            setZoomControlListener(binding.cohostView10);
            setZoomControlListener(binding.cohostView11);
        }
    }

    public class VH12 extends RecyclerView.ViewHolder {
        RvItem13Binding binding;

        public VH12(@NonNull RvItem13Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);
            setZoomControlListener(binding.cohostView1);
            setZoomControlListener(binding.cohostView2);
            setZoomControlListener(binding.cohostView3);
            setZoomControlListener(binding.cohostView4);
            setZoomControlListener(binding.cohostView5);
            setZoomControlListener(binding.cohostView6);
            setZoomControlListener(binding.cohostView7);
            setZoomControlListener(binding.cohostView8);
            setZoomControlListener(binding.cohostView9);
            setZoomControlListener(binding.cohostView10);
            setZoomControlListener(binding.cohostView11);
            setZoomControlListener(binding.cohostView12);
        }
    }

    public class VH13 extends RecyclerView.ViewHolder {
        RvItem14Binding binding;

        public VH13(@NonNull RvItem14Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);
            setZoomControlListener(binding.cohostView1);
            setZoomControlListener(binding.cohostView2);
            setZoomControlListener(binding.cohostView3);
            setZoomControlListener(binding.cohostView4);
            setZoomControlListener(binding.cohostView5);
            setZoomControlListener(binding.cohostView6);
            setZoomControlListener(binding.cohostView7);
            setZoomControlListener(binding.cohostView8);
            setZoomControlListener(binding.cohostView9);
            setZoomControlListener(binding.cohostView10);
            setZoomControlListener(binding.cohostView11);
            setZoomControlListener(binding.cohostView12);
            setZoomControlListener(binding.cohostView13);
        }
    }

    public class VH14 extends RecyclerView.ViewHolder {
        RvItem15Binding binding;

        public VH14(@NonNull RvItem15Binding itemView) {
            super(itemView.getRoot());
            binding = itemView;

            setZoomControlListener(binding.hostView);
            setZoomControlListener(binding.cohostView1);
            setZoomControlListener(binding.cohostView2);
            setZoomControlListener(binding.cohostView3);
            setZoomControlListener(binding.cohostView4);
            setZoomControlListener(binding.cohostView5);
            setZoomControlListener(binding.cohostView6);
            setZoomControlListener(binding.cohostView7);
            setZoomControlListener(binding.cohostView8);
            setZoomControlListener(binding.cohostView9);
            setZoomControlListener(binding.cohostView10);
            setZoomControlListener(binding.cohostView11);
            setZoomControlListener(binding.cohostView12);
            setZoomControlListener(binding.cohostView13);
            setZoomControlListener(binding.cohostView14);
        }
    }
}
