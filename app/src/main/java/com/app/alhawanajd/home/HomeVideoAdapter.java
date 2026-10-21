package com.app.alhawanajd.home;


import static android.view.View.GONE;
import static android.view.View.VISIBLE;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.app.Zego.activity.StartLiveActivity;
import com.app.Zego.internal.sdk.ZEGOSDKManager;
import com.app.Zego.internal.sdk.basic.ZEGOSDKCallBack;

import com.app.alhawanajd.RepostUsersAdapter;
import com.app.alhawanajd.home.eventbus.ExpandableTextView;
import com.app.alhawanajd.home.eventbus.SpeedChangeEndEvent;
import com.app.alhawanajd.home.eventbus.SpeedChangeStartEvent;
import com.app.alhawanajd.home.eventbus.VideoZoomEvent;
import com.app.helper.LocaleManager;
import com.app.helper.callback.AdapterSpeedChanger;
import com.app.model.RepostResponse;
import com.app.utils.TranslateUtil;
import com.app.utils.api.CommonApi;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.common.base.CharMatcher;
import com.google.gson.Gson;


import com.hendraanggrian.appcompat.socialview.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.socialview.widget.SocialAutoCompleteTextView;
import com.app.alhawanajd.R;
//import com.app.addon.livestream.StreamListActivity;
import com.app.data.model.Mention2;
import com.app.data.model.Tagged;
import com.app.db.DBHelper;
import com.app.alhawanajd.App;
import com.app.alhawanajd.LoginActivity;
import com.app.alhawanajd.VideoListActivity;
import com.app.alhawanajd.home.eventbus.FollowingUpdateGiftMessageCount;
import com.app.alhawanajd.home.eventbus.ForYouVideoLike;
import com.app.alhawanajd.profile.MyProfileActivity;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.helper.LinkEllipseTextView;
import com.app.helper.MySpannable;
import com.app.helper.NetworkReceiver;
import com.app.helper.OnVerticalScrollListener;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.helper.callback.NetworkResultCallback;

import com.app.model.FollowRequest;
import com.app.model.GetSet;
import com.app.model.Gift;
import com.app.model.HomeResponse;
import com.app.model.MusicListResponse;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.model.UserList;

import com.app.model.UserMention;
import com.app.model.VideoCommentResponse;
import com.app.utils.AdminData;
import com.app.utils.Constants;
import com.app.utils.DevicePref;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.makeramen.roundedimageview.RoundedImageView;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class HomeVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements AdapterItemClickListener, AdapterSpeedChanger {

    private boolean globalAssetsVisible = true;
    private final String TAG = HomeVideoAdapter.class.getSimpleName();
    FragmentActivity mActivity;
    private final int VIEW_TYPE_NORMAL = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
    private final static int MAX_LINES_COLLAPSED = 3;
    private final boolean INITIAL_IS_COLLAPSED = true;
    private boolean isCollapsed = INITIAL_IS_COLLAPSED;
    private boolean longpress = false;
    private ProfileResponse profile;
    String block_status;
    private VideoAdapterListener adapterListener;
    private AdapterItemClickListener clickListener;
    private AdapterSpeedChanger speecChangeListener;
    ArrayList<HomeResponse.Result> mHomeApiResponse = new ArrayList<>();
    public int targetPosition;
    RequestManager requestManager;
    public Boolean likePrevent = false;
    private boolean loading;
    private String apiType;
    private String from, from_key;

    /*GiftLayout*/
    public LinearLayoutManager layoutManager;
    private LinearLayout sendLay;
    private TextView txtAttachmentName, txtSend, sheettitle,respostDesc;
    private int giftPosition = 0;
    private BottomSheetDialog giftDialog;
    private ViewPager viewPager;
    private CircleIndicator pagerIndicator;
    private final int ITEM_LIMIT = 8;
    RelativeLayout mainLay;
    FrameLayout likeLay;
    private RecyclerView recyclerView;
    private RelativeLayout giftNullLay;
    private ImageView giftNullImage;
    private TextView giftNullText;
    List<Gift> tempGiftList = new ArrayList<>();
    GiftAdapter giftAdapter;
    LinearLayoutManager stickerLayoutManager;
    private int SelectedVideoPosition = -1;

    /*Comment List*/
    private ArrayList<VideoCommentResponse.Result> commentList = new ArrayList<>();
    private boolean searchUser = true;
    private String tagged_user_id = null, tagged_user_name = null, tagcomment = null;
    public ArrayList<String> list_tagged_user_names = new ArrayList<>();
    public ArrayList<String> list_tagged_user_ids = new ArrayList<>();
    private LinearLayout noCommentLay;
    private TextView commentCount, sendComment, noCommands, addComment;
    public BottomSheetDialog commentListDialog, commentEditDialog;
    private SocialAutoCompleteTextView mCommentsEditText;
    private RecyclerView commentRec;
    private CommentAdapter commendAdapter;
    private LinearLayoutManager linearLayoutManager;
    private int lastAvailableHeight = 0;
    View viewEmptyPlace;

    // Bottomsheet
    private RelativeLayout sendCommCommentsEditTextLay, sendCommentLay;
    //    private LinearLayout rootView;
    private LinearLayout headerBottom;
    private SwipeRefreshLayout mSwipeVideoComment = null;
    private OnVerticalScrollListener videoScrollListener;
    private int lastOffsetVideo = -1;
    private String loadMoreCheckVideoCommand = "initial";
    private int currentPage = 0;
    private final ArrayList<Mention2> mentionsCache = new ArrayList<>();

    RoundedImageView profileImageNew;

    String repostEditImage;


    public HomeVideoAdapter(FragmentActivity mActivity, ArrayList<HomeResponse.Result> homeApiResponse, RequestManager requestManager, String apiType, AdapterItemClickListener clickListener, String from_key,AdapterSpeedChanger speecChangeListener) {
        this.mActivity = mActivity;
        this.mHomeApiResponse = homeApiResponse;
        this.requestManager = requestManager;
        this.clickListener = clickListener;
        this.apiType = apiType;
        this.from_key = from_key;
        this.speecChangeListener = speecChangeListener;
    }

    public HomeVideoAdapter(FragmentActivity mActivity, ArrayList<HomeResponse.Result> homeApiResponse, RequestManager requestManager, String apiType, AdapterItemClickListener clickListener, String from, String from_key,AdapterSpeedChanger speecChangeListener) {
        this.mActivity = mActivity;
        this.mHomeApiResponse = homeApiResponse;
        this.requestManager = requestManager;
        this.clickListener = clickListener;
        this.apiType = apiType;
        this.from = from;
        this.from_key = from_key;
        this.speecChangeListener = speecChangeListener;
    }

    public boolean isLoading() {
        return loading;
    }

    public void showLoader() {
        if (isLoading()) {
            IllegalStateException e = new IllegalStateException("Already loading");
            Log.w(TAG, e.getMessage(), e);
            return;
        }
        loading = true;
        // Hide after loaded
        notifyItemInserted(mHomeApiResponse.size());
    }

    public void hideLoader() {
        if (!loading) {
            IllegalStateException e = new IllegalStateException("Not currently loading");
            Log.w(TAG, e.getMessage(), e);
            return;
        }
        loading = false;
        notifyItemRemoved(mHomeApiResponse.size() + 1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_NORMAL) {
            return new VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_foryou, parent, false));
        } else {
            return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading_holder, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadingView(position)) {
            return VIEW_TYPE_LOADING;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        int position = holder.getAbsoluteAdapterPosition();
        if (holder instanceof VideoViewHolder) {
            isCollapsed = INITIAL_IS_COLLAPSED;
            ((VideoViewHolder) holder).onBind(mHomeApiResponse.get(position));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos, @NonNull List<Object> payloads) {
        // Skip full rebind on payload updates so the playing video surface is not detached.
        if (payloads == null || payloads.isEmpty()) {
            super.onBindViewHolder(holder, pos, payloads);
        }
        Log.d("HomeVideoAdapter", "Coming from class: " + mActivity.getClass().getSimpleName());
        //MainActivity or MultipleVideoActivity
        Log.d(TAG, "onBindViewHolderassss: " + payloads +" from_key : " + from_key);
        ImageView btnBack = holder.itemView.findViewById(R.id.btnBack);
//        if (btnBack != null) {
//            if (mActivity.getClass().getSimpleName().equals("MultipleVideoActivity")) {
//                btnBack.setVisibility(View.VISIBLE);
//
//                btnBack.setOnClickListener(v -> {
//                    Log.d(TAG, "Back clicked from adapter");
//                    mActivity.finish(); // Go back to previous screen
//                });
//
//            } else {
//                btnBack.setVisibility(View.GONE);
//            }
//        }


        if (holder instanceof VideoViewHolder) {
            int position = holder.getAbsoluteAdapterPosition();
            Log.d(TAG, "onBindViewHolder11111111: "+new Gson().toJson(mHomeApiResponse));
            VideoViewHolder videoViewHolder = ((VideoViewHolder) holder);


            if (mHomeApiResponse.get(position).getPlaytype() == null || mHomeApiResponse.get(position).getPlaytype().equals("ad")) {

                if (mHomeApiResponse.get(position).getLink_type().equals("Images")) {
                    if (mHomeApiResponse.get(position).getImages() != null && mHomeApiResponse.get(position).getImages().size() > 0) {
                        videoViewHolder.bottomPagerAdapter = new RecyclerViewAdapter(mHomeApiResponse.get(position).getImages(), mHomeApiResponse.get(position).getAndroidUrl());
                        layoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
                        videoViewHolder.bottomPager.setLayoutManager(layoutManager);
                        videoViewHolder.bottomPager.setAdapter(videoViewHolder.bottomPagerAdapter);
                        videoViewHolder.bottomPager.setHasFixedSize(true);
                        videoViewHolder.bottomPagerAdapter.notifyDataSetChanged();
                    }
                }

            }
            if (payloads.size() > 0) {
                Bundle payload = (Bundle) payloads.get(0);

                Timber.tag(TAG).d("onBindViewHolder pay: %s", payload);

                final String likesCount = payload.getString("likes_count", null);
                final String repostCount = payload.getString("repost_count", null);
                final String likesStatus = payload.getString("likes_status", null);
                final String giftCount = payload.getString("gift_count", null);
                final String updateGiftCount = payload.getString("update_gift_count", null);
                final String videoCommentCount = payload.getString("video_comment_count", null);
                final String follow = payload.getString("follow", null);
                final String Report = payload.getString("Report", null);
                final String Fav = payload.getString("Fav", null);
                final String videoDescription = payload.getString("video_description", null);
                final String publishMode = payload.getString("mode", null);

               /* if (likesCount != null) {
                    if (likesStatus.equals("true")) {
                        ((VideoViewHolder) holder).heart.setImageResource(R.drawable.heart_new_selected_2);
                    } else {
                        ((VideoViewHolder) holder).heart.setImageResource(R.drawable.heart_new_unselected_2);
                    }
                    ((VideoViewHolder) holder).heartAni.cancelAnimation();
                    ((VideoViewHolder) holder).heartAni.clearAnimation();
                    ((VideoViewHolder) holder).heartAni.setVisibility(View.GONE);
                    ((VideoViewHolder) holder).txt_heart_count.setText(likesCount);
                    ((VideoViewHolder) holder).heart.setVisibility(View.VISIBLE);
                    ((VideoViewHolder) holder).txt_heart_count.setVisibility(View.VISIBLE);

                }*/

           /*     Log.d(TAG, "onBindViewHolder1234: "+mHomeApiResponse.get(position).getRepostedData());
                if(mHomeApiResponse.get(position).getRepostedData()!=null){
                    if (!mHomeApiResponse.get(position).getRepostedData().isEmpty()){
                        repostTextLay.setVisibility(VISIBLE);
                        respostDesc.setText(mHomeApiResponse.get(position).getRepostedData().get(0).getDescription());

                        Glide.with(mActivity)
                                .load(mHomeApiResponse.get(position).getRepostedData().get(0).getUserImage())
                                .placeholder(R.drawable.default_profile_image)
                                .into(respostProfileImage);
//                        respostProfileImage
                    }
                }*/
                if (likesCount != null) {
                    VideoViewHolder vh = (VideoViewHolder) holder;

                    // update heart drawable safely
                    boolean liked = "true".equalsIgnoreCase(likesStatus);
                    int newIcon = liked ? R.drawable.heart_new_selected_2 : R.drawable.heart_new_unselected_2;

                    // only change icon if needed (prevents flicker)
                    if (vh.heart.getTag() == null || (int) vh.heart.getTag() != newIcon) {
                        vh.heart.setImageResource(newIcon);
                        vh.heart.setTag(newIcon);
                    }

                    // cancel any animation currently running
                    vh.heartAni.cancelAnimation();
                    vh.heartAni.clearAnimation();
                    vh.heartAni.setVisibility(View.GONE);

                    // update like count only if different (prevents relayout flicker)
                    CharSequence currentLikes = vh.txt_heart_count.getText();
                    if (!TextUtils.equals(currentLikes, likesCount)) {
                        vh.txt_heart_count.setText(likesCount);
                    }

                    vh.heart.setVisibility(View.VISIBLE);
                    vh.txt_heart_count.setVisibility(View.VISIBLE);
                } else if (repostCount !=null) {
                    ((VideoViewHolder) holder).repostcount.setText(repostCount);

                } else if (giftCount != null) {

                    ((VideoViewHolder) holder).giftLay.clearAnimation();

                    ObjectAnimator anim = ObjectAnimator
                            .ofFloat(((VideoViewHolder) holder).giftLay, "translationX", 0, 25, -25, 25, -25, 15, -15, 10, -6, 0)
                            .setDuration(2000);
                    anim.addListener(new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            ((VideoViewHolder) holder).giftLay.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.anim_zoom_out_length));
                            Glide.with(mActivity)
                                    .load(tempGiftList.get(giftPosition).getGiftIcon())
                                    .into(((VideoViewHolder) holder).giftImageView);
                        }

                        @SuppressLint("UseCompatLoadingForDrawables")
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);

                            /*   holder.giftLay.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));*/

                            ((VideoViewHolder) holder).giftImageView.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.gift));

                        }
                    });
                    anim.start();
                    ((VideoViewHolder) holder).txt_gift_count.setText(giftCount);

                } else if (videoCommentCount != null) {
                    ((VideoViewHolder) holder).txt_comment_count.setText(videoCommentCount);
                } else if (follow != null) {
                    if (follow.equals("true")) {
                        ((VideoViewHolder) holder).profileFollowIcon.setVisibility(View.GONE);
//                        ((VideoViewHolder) holder).followButton.setVisibility(View.GONE);
                        ((VideoViewHolder) holder).followButton2.setVisibility(View.GONE);
                    } else {
                        ((VideoViewHolder) holder).profileFollowIcon.setVisibility(View.VISIBLE);
//                        ((VideoViewHolder) holder).followButton.setVisibility(View.VISIBLE);
                        ((VideoViewHolder) holder).followButton2.setVisibility(View.GONE);
                    }
                } else if (updateGiftCount != null)
                    ((VideoViewHolder) holder).txt_gift_count.setText(updateGiftCount);
                else if (!TextUtils.isEmpty(videoDescription)) {
                    String getHash = videoDescription;
                   /*   ((VideoViewHolder) holder).txt_description.setIsLinkable(true);
                   ((VideoViewHolder) holder).txt_description.setText(getHash);
                    MovementMethod m = ((VideoViewHolder) holder).txt_description.getMovementMethod();
                    if ((m == null) || !(m instanceof LinkMovementMethod)) {
                        if (((VideoViewHolder) holder).txt_description.getLinksClickable()) {
                            ((VideoViewHolder) holder).txt_description.setMovementMethod(LinkMovementMethod.getInstance());
                        }
                    }

                    ((VideoViewHolder) holder).txt_description.setOnTextLinkClickListener(new LinkEllipseTextView.TextLinkClickListener() {
                        @Override
                        public void onTextLinkClick(View textView, String clickedString) {
                            if (clickedString.contains("@")) {
                            } else if (clickedString.contains("#")) {
                                String user_param = clickedString.replace("#", "");
                                Intent intent = new Intent(mActivity, VideoListActivity.class);
                                intent.putExtra(Constants.TAG_TITLE, user_param);
                                intent.putExtra(Constants.TAG_FROM, Constants.TAG_RELATED_HASHTAG);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                mActivity.startActivity(intent);
                                clickListener.onItemClicked(false, position, Constants.TAG_PLAYBACK);
                            }
                        }
                    });

                    ((VideoViewHolder) holder).txt_description.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            view.getParent().requestDisallowInterceptTouchEvent(true);
                            switch (motionEvent.getAction()) {
                                case MotionEvent.ACTION_UP:
                                    view.getParent().requestDisallowInterceptTouchEvent(false);
                                    break;
                            }
                            return false;
                        }
                    });

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

                    int screenHalf = (displayMetrics.widthPixels * 35 / 100) - App.dpToPx(mActivity, 0);
                    ((VideoViewHolder) holder).txt_description.setMaxHeight(screenHalf); */
                }
            }
        }
    }

    private boolean isLoadingView(int position) {
        return loading && position == mHomeApiResponse.size();
    }

    @Override
    public int getItemCount() {
        return loading ? mHomeApiResponse.size() + 1 : mHomeApiResponse.size();
    }

    public void setLikePrevent(boolean b) {
        likePrevent = b;
    }

    @Override
    public void onItemClicked(Object o, int position, String type) {
        Log.d(TAG, "onItemClicked111111111111: "+type);
        if (type.equals(Constants.TAG_GIFT)) {
            Gift gift = (Gift) o;
            giftPosition = position;
            sendLay.setVisibility(View.VISIBLE);
            txtSend.setVisibility(View.VISIBLE);
            sheettitle.setVisibility(View.GONE);
            txtAttachmentName.setText(gift.getGiftTitle());
        } else if (type.equals(Constants.TAG_COMMENT)) {
            String tagItem = (String) o;
            Intent profile = new Intent(mActivity, OthersProfileActivity.class);
            profile.putExtra(Constants.TAG_USER_NAME, tagItem);
            profile.putExtra(Constants.TAG_USER_IMAGE, "");
            mActivity.startActivityForResult(profile, Constants.COMMENT_MUSIC_REQUEST_CODE);
        } else if (type.equals("refresh")) {
            mSwipeVideoComment.setEnabled(false);
            mSwipeVideoComment.setRefreshing(false);

            commentCount.setText(commentList.size() + " " + mActivity.getApplicationContext().getResources().getString(R.string.comments));
            commentCount.startAnimation(AnimationUtils.loadAnimation(mActivity.getApplicationContext(), R.anim.anim_zoom_out));
            /*Bundle payload = new Bundle();
            mHomeApiResponse.get(position).setVideoCommentCounts(String.valueOf(commentList.size()));
            payload.putString("video_comment_count", String.valueOf(commentList.size()));
            notifyItemChanged(position, payload);*/
            mSwipeVideoComment.setEnabled(true);
            mSwipeVideoComment.setRefreshing(true);
        } else if (type.equals(Constants.TAG_USERS)) {
            VideoCommentResponse.Result item = (VideoCommentResponse.Result) o;
            Intent profile;
            if (commentList.get(position).getUserId().equals(GetSet.getUserId())) {
                profile = new Intent(mActivity, MyProfileActivity.class);
            } else {
                profile = new Intent(mActivity, OthersProfileActivity.class);
                profile.putExtra(Constants.TAG_USER_ID, item.getUserId());
                profile.putExtra(Constants.TAG_USER_IMAGE, item.getUserImage());
            }
            mActivity.startActivityForResult(profile, Constants.COMMENT_MUSIC_REQUEST_CODE);
        }
    }

    @Override
    public void onItemPlaybackSpeed(Object o, int position, String videoId, float speed, String type) {


        if(type.equals("playbackspeed")){

        }
    }

    @Override
    public void onItemPlaybackSpeed(Object o, int position, String videoId, String speed, String type) {
        if(type.equals("caption")){

        }
    }

    @Override
    public void onItemReposted(Object o, int position, String videoId, String action, String desc) {

//        speecChangeListener.onItemReposted(o,position, videoId,action, desc);


    }

  /*  @Override
    public void onItemPlaybackSpeed(Object o, int position, float speed, String type) {

        if(type.equals("playbackspeed")){

        }

    }*/

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
        }
    }


    public class VideoViewHolder extends RecyclerView.ViewHolder implements VideoAdapterListener {

        //private final ScaleAnimation scaleAnimation;
        public ConstraintLayout parentLay;
        public ProgressBar progressBar;
        public SeekBar progress_duration;
        public ImageView video_thumbnail, profileImage, video_play_pause, profileFollowIcon,repostEditIcon;
        public TextView txt_toggle,userName, txt_title, txt_heart_count, txt_gift_count, txt_comment_count, txtAudioTitle;
        public LinkEllipseTextView txt_description,txt_description_new,txt_full_description, txt_translated_content;
        public ExpandableTextView txt_description_1;
        public Button add_button;
        public LinearLayout heartLay, videoCommentLay,repostLay, giftLay, moreLay, bottomLay, audioLay, musicLay;
        public FrameLayout mediaContainer, frame;
        public LinearLayout leftSideView, leftSideVieww;
        public View videoTextLay;

        String repostDescription,repostId;
        String updatedText,repostUserImage;
        ConstraintLayout repostTextLay;
        RoundedImageView respostProfileImage;
        public LottieAnimationView heartAni;
        public LottieAnimationView lottieAudio;
        public ShapeableImageView heart, giftImageView;
        public LottieAnimationView icFavHeart;
        public RoundedImageView liveStreaming;
        public String audio_url;
        RecyclerView bottomPager;
        RecyclerViewAdapter bottomPagerAdapter;

        //        AppCompatButton followButton;
        TextView followButton, followButton2;
        // BlurView blurredBackground;
        // RelativeLayout blurframe;
        // BlurView blurView;
        TextView marqueeText;
        LinearLayout newadlayout;
        FrameLayout likeLay;
        LinearLayout teststreaming;
        ImageView repostIcon;

        Boolean isReposted = false;

        Dialog repostDialog;

        MaterialTextView repostcount;
        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setTag(this);

            icFavHeart = itemView.findViewById(R.id.ic_fav_heart);
            repostIcon = itemView.findViewById(R.id.repostIcon);
            newadlayout = itemView.findViewById(R.id.newadlayout);
            mediaContainer = itemView.findViewById(R.id.mediaContainer);
            video_thumbnail = itemView.findViewById(R.id.mediaCoverImage);
           // video_thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            txt_description = itemView.findViewById(R.id.txt_description);
            txt_description_1 = itemView.findViewById(R.id.txt_description_1);
            txt_description_new = itemView.findViewById(R.id.txt_description_new);
            txt_toggle = itemView.findViewById(R.id.txt_toggle);
            txt_full_description = itemView.findViewById(R.id.txt_full_description);
            txt_translated_content = itemView.findViewById(R.id.txt_translated_content);
            add_button = itemView.findViewById(R.id.add_button);
            frame = itemView.findViewById(R.id.frame);
            profileImage = itemView.findViewById(R.id.profileImage);
            profileImageNew = itemView.findViewById(R.id.profileImageNew);
            repostEditIcon = itemView.findViewById(R.id.repostEditIcon);
            repostTextLay = itemView.findViewById(R.id.repostTextLay);
            respostDesc = itemView.findViewById(R.id.respostDesc);
            respostProfileImage = itemView.findViewById(R.id.respostProfileImage);
            txt_title = itemView.findViewById(R.id.txt_title);
            userName = itemView.findViewById(R.id.userName);
            txt_heart_count = itemView.findViewById(R.id.txt_heart_count);
            repostcount = itemView.findViewById(R.id.repostcount);
            txt_gift_count = itemView.findViewById(R.id.txt_gift_count);
            txt_comment_count = itemView.findViewById(R.id.txt_comment_count);
            giftLay = itemView.findViewById(R.id.giftLay);
            heartLay = itemView.findViewById(R.id.heartLay);
            videoCommentLay = itemView.findViewById(R.id.commentLay);
            repostLay = itemView.findViewById(R.id.repostLay);
            video_play_pause = itemView.findViewById(R.id.video_play_pause);
            progressBar = itemView.findViewById(R.id.progressBar);
            parentLay = itemView.findViewById(R.id.parentLay);
            progress_duration = itemView.findViewById(R.id.progress_duration);
            profileFollowIcon = itemView.findViewById(R.id.profileFollowIcon);
            followButton = itemView.findViewById(R.id.followButton);
            followButton2 = itemView.findViewById(R.id.followButton2);
            leftSideView = itemView.findViewById(R.id.leftSideView);
            videoTextLay = itemView.findViewById(R.id.videoTextLay);
            leftSideVieww = itemView.findViewById(R.id.leftSideVieww);
            moreLay = itemView.findViewById(R.id.moreLay);
            heartAni = itemView.findViewById(R.id.heartAni);
            heart = itemView.findViewById(R.id.imgheart);
            giftImageView = itemView.findViewById(R.id.giftImageView);
            txtAudioTitle = itemView.findViewById(R.id.txtAudioTitle);
            liveStreaming = itemView.findViewById(R.id.liveStreaming);
            lottieAudio = itemView.findViewById(R.id.lottieAudio);
            audioLay = itemView.findViewById(R.id.audioLay);
            musicLay = itemView.findViewById(R.id.musicLay);
            bottomLay = itemView.findViewById(R.id.bottomLay);
            bottomPager = itemView.findViewById(R.id.bottom_pager);
            mainLay = itemView.findViewById(R.id.mainLay);
            likeLay = itemView.findViewById(R.id.likeLay);
            // blurView = itemView.findViewById(R.id.blurView);
            //  blurredBackground = itemView.findViewById(R.id.blurredBackground);
            marqueeText = itemView.findViewById(R.id.marqueeText);
            //  blurframe = itemView.findViewById(R.id.blurframe);
            likeLay = itemView.findViewById(R.id.likeLay);
            teststreaming = itemView.findViewById(R.id.teststreaming);
            marqueeText.setSelected(true);

        }

        public void makeTextViewResizable(final TextView tv, final int maxLine, final String expandText, final boolean viewMore) {

            if (tv.getTag() == null) {
                tv.setTag(tv.getText());
            }
            ViewTreeObserver vto = tv.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                @SuppressWarnings("deprecation")
                @Override
                public void onGlobalLayout() {

                    ViewTreeObserver obs = tv.getViewTreeObserver();
                    obs.removeGlobalOnLayoutListener(this);
                    if (maxLine == 0) {
                        int lineEndIndex = tv.getLayout().getLineEnd(0);
                        String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                        tv.setText(text);
                        tv.setMovementMethod(LinkMovementMethod.getInstance());
                        tv.setText(
                                addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
                                        viewMore), TextView.BufferType.SPANNABLE);
                    } else if (maxLine > 0 && tv.getLineCount() >= maxLine) {
                        int lineEndIndex = tv.getLayout().getLineEnd(maxLine - 1);
                        String text = tv.getText().subSequence(0, lineEndIndex - expandText.length() + 1) + " " + expandText;
                        tv.setText(text);
                        tv.setMovementMethod(LinkMovementMethod.getInstance());
                        tv.setText(
                                addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, maxLine, expandText,
                                        viewMore), TextView.BufferType.SPANNABLE);
                    } else {
                        int lineEndIndex = tv.getLayout().getLineEnd(tv.getLayout().getLineCount() - 1);
                        String text = tv.getText().subSequence(0, lineEndIndex) + " " + expandText;
                        tv.setText(text);
                        tv.setMovementMethod(LinkMovementMethod.getInstance());
                        tv.setText(
                                addClickablePartTextViewResizable(Html.fromHtml(tv.getText().toString()), tv, lineEndIndex, expandText,
                                        viewMore), TextView.BufferType.SPANNABLE);
                    }
                }
            });

        }


        private SpannableStringBuilder addClickablePartTextViewResizable(final Spanned strSpanned, final TextView tv,
                                                                         final int maxLine, final String spanableText, final boolean viewMore) {
            String str = strSpanned.toString();
            SpannableStringBuilder ssb = new SpannableStringBuilder(strSpanned);

            if (str.contains(spanableText)) {


                ssb.setSpan(new MySpannable(false) {
                    @Override
                    public void onClick(View widget) {
                        if (viewMore) {
                            tv.setLayoutParams(tv.getLayoutParams());
                            tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                            tv.invalidate();
                            makeTextViewResizable(tv, -1, mActivity.getResources().getString(R.string.read_less1), false);
                        } else {
                            tv.setLayoutParams(tv.getLayoutParams());
                            tv.setText(tv.getTag().toString(), TextView.BufferType.SPANNABLE);
                            tv.invalidate();
                            makeTextViewResizable(tv, 3,  mActivity.getResources().getString(R.string.read_more1), true);
                        }
                    }
                }, str.indexOf(spanableText), str.indexOf(spanableText) + spanableText.length(), 0);

            }
            return ssb;

        }


        void onBind(HomeResponse.Result videoItem) {

            Log.d(TAG, "onBind1111111: "+new Gson().toJson(videoItem));
            if (videoItem.getPlaytype() == null || videoItem.getPlaytype().equals("ad")) {
                if (videoItem.getLink_type().equals("Images")) {
                    if (videoItem.getImages() != null && videoItem.getImages().size() > 0) {
                        bottomPagerAdapter = new RecyclerViewAdapter(videoItem.getImages(), videoItem.getAndroidUrl());
                        layoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
                        bottomPager.setLayoutManager(layoutManager);
                        bottomPager.setAdapter(bottomPagerAdapter);
                        bottomPager.setHasFixedSize(true);
                        bottomPagerAdapter.notifyDataSetChanged();
                    }
                }
            }

            Log.d(TAG, "onBind_repost_Data: "+new Gson().toJson(videoItem.getRepostedData()));
            if (videoItem.getRepostedData() != null && !videoItem.getRepostedData().isEmpty()) {

                repostTextLay.setVisibility(View.VISIBLE);

                respostDesc.setText("");
                String currentUserId = GetSet.getUserId();
                HomeResponse.Result.RepostedData matchedData = null;
                final boolean[] isUserReposted = {false};

                Log.d("REPOST_CHECK", "Total Repost List = " + videoItem.getRepostedData().size());

//                repostcount.setText(videoItem.getRepostedData().size());
                for (HomeResponse.Result.RepostedData data : videoItem.getRepostedData()) {

                    Log.d("REPOST_CHECK",
                            "Checking User = " + currentUserId +
                                    " | repostUser = " + data.getUserId() +
                                    " | Desc = " + data.getDescription());

                    if (data.getUserId() != null && data.getUserId().equals(currentUserId)) {
                        isReposted = true;
                        repostIcon.setImageDrawable(mActivity.getDrawable(R.drawable.repost_home_filled));
                        matchedData = data;
                        isUserReposted[0] = true;
                        break;
                    }
                }

                // Default to first item if no matching user
                if (matchedData == null) {
                    matchedData = videoItem.getRepostedData().get(0);
                }

                repostDescription = matchedData.getDescription();
                repostId = matchedData.getRepostId();
                repostUserImage = matchedData.getUserImage();


                if (matchedData.getDescription() == null || matchedData.getDescription().trim().isEmpty()) {
                    Log.d(TAG, "onBindifffffffff: ");
                    if (matchedData.getUserId() != null && matchedData.getUserId().equals(currentUserId)) {
                        Log.d(TAG, "onBindifffffffff111111111111: ");
                        respostDesc.setText(mActivity.getString(R.string.you_reposted));
                        repostEditImage = matchedData.getUserImage();
                        Glide.with(mActivity)
                                .load(matchedData.getUserImage())
                                .placeholder(R.drawable.default_profile_image)
                                .into(respostProfileImage);
                    }
                    else{
                        Log.d(TAG, "onBindelseeeeeeee: ");

                    respostDesc.setText(mActivity.getString(R.string.user_reposted, matchedData.getDisplayName()));}
                } else {
                    Log.d(TAG, "onBindelseeeeeeee111111111: ");
                    respostDesc.setText(matchedData.getDescription());
                }
                Log.d(TAG, "onBinduserImage: "+matchedData.getUserImage());

                repostEditImage = matchedData.getUserImage();
                // Load user image
                Glide.with(mActivity)
                        .load(matchedData.getUserImage())
                        .placeholder(R.drawable.default_profile_image)
                        .into(respostProfileImage);

                if (isUserReposted[0]) {
                    repostEditIcon.setVisibility(View.VISIBLE);
                } else {
                    repostEditIcon.setVisibility(View.GONE);
                }

                repostTextLay.setOnClickListener(v -> {
//                    openRepostUsersBottomSheet(videoItem.getRepostedData());
                    if (isUserReposted[0]) {
                        Log.d("REPOST_CLICK", "User clicked their own repost → No action");
                        return; // stop here
                    }

                    if (videoItem.getPublisherId() != null) {
                        // ✅ Easy way to hide views directly
                        View imageButton = ((Activity) v.getContext()).findViewById(R.id.imageButton);
                        View relativeLive = ((Activity) v.getContext()).findViewById(R.id.relativeLive);
                        View toplay = ((Activity) v.getContext()).findViewById(R.id.toplay);

                        if (imageButton != null) imageButton.setVisibility(View.GONE);
                        if (relativeLive != null) relativeLive.setVisibility(View.GONE);


                        // ✅ Open profile activity
                        Intent intent = new Intent(v.getContext(), OthersProfileActivity.class);
                        intent.putExtra(Constants.TAG_USER_ID, videoItem.getPublisherId());
                        intent.putExtra(Constants.TAG_USER_NAME, videoItem.getPostedBy());
                        v.getContext().startActivity(intent);
                    }
                });

            } else {

                Log.d(TAG, "onBind2222222222222222: ");
                repostIcon.setImageDrawable(mActivity.getDrawable(R.drawable.repost_home));
                repostTextLay.setVisibility(View.GONE);
                repostEditIcon.setVisibility(View.GONE);
            }




            Timber.tag(TAG).d("apiType: onBind: %s", apiType);

            if (/*videoItem.isAssetsVisible()*/globalAssetsVisible) {
                leftSideView.setVisibility(View.VISIBLE);
                bottomLay.setVisibility(View.VISIBLE);
                newadlayout.setVisibility(View.VISIBLE);
                progress_duration.setVisibility(View.VISIBLE);
            } else {
                leftSideView.setVisibility(View.GONE);
                bottomLay.setVisibility(View.GONE);
                newadlayout.setVisibility(View.GONE);
                progress_duration.setVisibility(View.GONE);
            }

            if (mActivity.getClass().getSimpleName().equals("MultipleVideoActivity")) {
                video_thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                /*video_thumbnail.setScaleType(ImageView.ScaleType.FIT_XY);*/
                video_thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }


            if ((videoItem.getPlaytype() == null || videoItem.getPlaytype().equals(Constants.TAG_VIDEO))) {
                if (videoItem.getVideoType().equals("gallery") || videoItem.getVideoType().equals("ad")) {
                    video_thumbnail.setVisibility(View.VISIBLE);
                    video_thumbnail.setAlpha(1f);
                    Glide.with(mActivity)
                            .load(videoItem.getStreamThumbnail())
                            .into(video_thumbnail);
                } else {
                    video_thumbnail.setVisibility(View.VISIBLE);
                    video_thumbnail.setAlpha(1f);
                    Glide.with(mActivity)
                            .load(videoItem.getStreamThumbnail())
                            .into(video_thumbnail);
                }
                //  leftSideView.setVisibility(View.VISIBLE);
                //  leftSideVieww.setVisibility(View.VISIBLE);
                add_button.setVisibility(View.GONE);
                frame.setVisibility(View.GONE);

                if (GetSet.getUserId() != null && GetSet.getUserId().equals(videoItem.getPublisherId())) {
                    profileFollowIcon.setVisibility(View.GONE);
                    followButton.setVisibility(View.GONE);
                    // followButton2.setVisibility(View.GONE);
                }
                else {
                    if (videoItem.getFollowedUser() == null) {
                        profileFollowIcon.setVisibility(View.VISIBLE);
                        followButton.setVisibility(View.GONE);
                        // followButton2.setVisibility(View.VISIBLE);
                    } else {
                        if (videoItem.getFollowedUser()) {// If API come true that user already follow
                            profileFollowIcon.setVisibility(View.GONE);
                            followButton.setVisibility(View.GONE);
                            // followButton2.setVisibility(View.GONE);
                        } else {
                            profileFollowIcon.setVisibility(View.VISIBLE);
                            followButton.setVisibility(View.GONE);
                            //  followButton2.setVisibility(View.VISIBLE);


                        }
                    }
                }

                Glide.with(itemView.getContext())
                        .load(videoItem.getPublisherImage())
                        .placeholder(R.drawable.default_profile_image)
                        .into(profileImage);

                Glide.with(itemView.getContext())
                        .load(videoItem.getPublisherImage())
                        .placeholder(R.drawable.default_profile_image)
                        .into(profileImageNew);

//                respostDesc.setText("You reposted");

         /*       Glide.with(itemView.getContext())
                        .load(videoItem.getPublisherImage())
                        .placeholder(R.drawable.default_profile_image)
                        .into(respostProfileImage);*/

                videoTextLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), OthersProfileActivity.class);
                        intent.putExtra(Constants.TAG_USER_ID, videoItem.getPublisherId());
                        intent.putExtra(Constants.TAG_USER_NAME, videoItem.getPostedBy());
                        v.getContext().startActivity(intent);
                    }
                });


                String currentLanguageCode = Locale.getDefault().getLanguage();

                OnSwipeTouchListener swipeListener = new OnSwipeTouchListener(mActivity, mediaContainer) {
                    @Override
                    public void onSwipeRight(long data) {
                        // Show views or handle data
                        Log.e(TAG,"onSwipeRight : ");
                        globalAssetsVisible = true;
                        notifyDataSetChanged();
                        EventBus.getDefault().post(new VideoZoomEvent(0));
                        Toasty.info(mActivity,  mActivity.getResources().getString(R.string.show_all_asset), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSwipeLeft(long data) {
                        // Hide views or handle data
                        Log.e(TAG,"onSwipeLeft : ");
                        globalAssetsVisible = false;
                        notifyDataSetChanged();
                        EventBus.getDefault().post(new VideoZoomEvent(2));
                        Toasty.info(mActivity, mActivity.getResources().getString(R.string.hide_all_assets), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void singleTab() {
                        // handle single tap
                        Log.e(TAG,"SINGLE TAP");
                    }

                    @Override
                    public void doubleTab() {
                        // handle double tap
                        Log.e(TAG,"SINGLE TAP");
                    }

                    @Override
                    public void onZoomChanged(float zoomLevel) {
                        super.onZoomChanged(zoomLevel);
                        Log.d(TAG, "onZoomChanged: " + zoomLevel);

                        EventBus.getDefault().post(new VideoZoomEvent(zoomLevel));
                        hideViewsOnZoom(zoomLevel);


                    }

                    @Override
                    public void onSpeedChangeStart() {
                        super.onSpeedChangeStart();
                        // Post event to EventBus

                        hideViewsOnZoom(2);
                        EventBus.getDefault().post(new VideoZoomEvent(2)); //  > 1 for hide bottom tab,
                        EventBus.getDefault().post(new SpeedChangeStartEvent(getAdapterPosition()));


                    }

                    @Override
                    public void onSpeedChangeEnd() {
                        super.onSpeedChangeEnd();
                        // Post event to EventBus
                        hideViewsOnZoom(0);
                        EventBus.getDefault().post(new VideoZoomEvent(0));  // <1 for show bottom tab,
                        EventBus.getDefault().post(new SpeedChangeEndEvent(getAdapterPosition()));

                    }
                };

                // swipeListener.setSwipeData(120L); // Pass your long value here
                swipeListener.setSwipeData(300L);
                mainLay.setOnTouchListener(swipeListener);
                marqueeText.setSelected(true); // Required for marquee to start
                Log.e(TAG,"currentlangugae code :"+ currentLanguageCode +" addstatus :" + AdminData.ad_status);
                String lang = LocaleManager.getLanguageCode(mActivity.getApplicationContext());
                Log.e(TAG,"currentlangugae lang :"+ lang +" addstatus :" + AdminData.ad_status);

                if ("1".equals(AdminData.ad_status)) {
                    newadlayout.setVisibility(View.VISIBLE);

                    if ("ar".equalsIgnoreCase(lang)) {
                        // Arabic → Right-to-left marquee
                        marqueeText.setText(AdminData.arabic_ad_key);
                        marqueeText.setTextDirection(View.TEXT_DIRECTION_RTL);
                        // marqueeText.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                    } else {
                        // English and others → Left-to-right marquee
                        marqueeText.setText(AdminData.english_ad_key);
                        marqueeText.setTextDirection(View.TEXT_DIRECTION_LTR);
                        //marqueeText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                    }

                    // Common marquee properties
                    marqueeText.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                    marqueeText.setSingleLine(true);
                    marqueeText.setMarqueeRepeatLimit(-1);

                    // 👇 Key part: request focus to make marquee actually scroll
                    marqueeText.setSelected(true);

                } else {
                    newadlayout.setVisibility(View.GONE);
                }



                if (videoItem.getIsLiked()) {
                    heart.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.heart_new_selected_2));
                } else {
                    heart.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.heart_new_unselected_2));
                }

                //bottomLay
                //video_thumbnail
                //videoItem.getStreamThumbnail()
                Log.e(TAG, "VIDEOtHUMBNAIL : " + videoItem.getStreamThumbnail());
                Log.e(TAG, "VideoUrl : " + videoItem.getPlaybackUrl());
                Log.e(TAG, "Videourl1 : " + videoItem.getAndroidUrl());
                Log.e(TAG, "Videourl2 : " + videoItem.getStreamThumbnail());


                txt_title.setText(String.format("@%s", videoItem.getPostedBy()));
                userName.setText(videoItem.getPostedBy());

                txt_heart_count.setText(videoItem.getLikes());
                txt_gift_count.setText(videoItem.getVideoGiftCounts());
                txt_comment_count.setText(videoItem.getVideoCommentCounts());

                if(videoItem.getRepostCount()!=null){
                    repostcount.setText("" +videoItem.getRepostCount());
                }
                else{
                    repostcount.setText("0");
                }

                DisplayMetrics displayMetrics = new DisplayMetrics();
                mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
               /* android.view.ViewGroup.LayoutParams layoutParams = txt_description.getLayoutParams();
                layoutParams.height = 80;
                txt_description.setLayoutParams(layoutParams);*/
                int screenHalf = (displayMetrics.widthPixels * 35 / 100) - App.dpToPx(mActivity, 0);
                txt_description.setMaxHeight(screenHalf);
                // ExpandableSpannableHelper.setExpandableText(txt_description_new, videoItem.getVideoDescription(), 2);
                String getHash = videoItem.getVideoDescription();
                String fullText = videoItem.getVideoDescription();
                //   boolean isExpanded = videoItem.isExpanded();
                // txt_description.setIsLinkable(true);



                // txt_description.setText(getHash); - commentes on oct17


                // Check if text setup was already done for this view (using tag)
                String setupTag = (String) txt_description.getTag(R.id.txt_description);
                String currentDescription = videoItem.getVideoDescription();

                if (setupTag == null || !setupTag.equals(currentDescription)) {
                    // Mark that we're setting up this description
                    txt_description.setTag(R.id.txt_description, currentDescription);

                    String originalDescription = videoItem.getVideoDescription();
                    final String[] detectedLang = {""};        // store detected language globally
                    final String[] translatedText = {""};      // store translated text
                    final String[] originalText = {originalDescription};
                    final boolean[] showingOriginal = {true};  // start with original shown

                    txt_description.setIsLinkable(true);
                    txt_description.setIsEndEllipsable(true);
                    txt_description.setMaxLines(2);


                    TranslateUtil.translateText(originalDescription, "en", new TranslateUtil.TranslationCallback() {
                        @Override
                        public void onTranslated(String translated, String original, String lang) {
                            if (translated != null) {
                                Log.d("TranslatedText", "Translated: " + translated);
                                Log.d("TranslatedText", "Original: " + original);
                                Log.d("TranslatedText", "Detected Language: " + lang);

                                detectedLang[0] = lang;
                                translatedText[0] = translated;
                                originalText[0] = original;

                                // UI updates on main thread
                                mActivity.runOnUiThread(() -> {
                                    // Always show original text initially
                                    //txt_description.setText(originalText[0]);
                                    // applyTextDirection(txt_description, lang);
                                    applyReadMoreIfNeeded(txt_description, originalText[0]);

                                    if ("en".equals(lang)) {
                                        // English → hide toggle button
                                        txt_translated_content.setVisibility(View.GONE);
                                    } else {
                                        // Arabic or other → show toggle button
                                        txt_translated_content.setVisibility(View.VISIBLE);
                                        txt_translated_content.setText(mActivity.getString(R.string.show_translated_content));
                                    }

                                    // Apply "See More / See Less"
                              /*  txt_description.post(() -> {
                                    Layout layout = txt_description.getLayout();
                                    if (layout != null && layout.getLineCount() > 2) {
                                        addReadMore(txt_description.getText().toString(), txt_description);
                                    }
                                });*/
                                });
                            } else {
                                Log.e("TranslatedText", "Translation failed");
                            }
                        }
                    });

                    // Translation toggle click
                    txt_translated_content.setOnClickListener(v -> {
                        if (showingOriginal[0]) {
                            // Show translated text
                            //txt_description.setText(translatedText[0]);
                            // applyTextDirection(txt_description, detectedLang[0]);
                            applyReadMoreIfNeeded(txt_description, translatedText[0]);
                            txt_translated_content.setText(mActivity.getString(R.string.show_original_content));
                            showingOriginal[0] = false;
                        } else {
                            // Show original text
                            //txt_description.setText(originalText[0]);
                            // applyTextDirection(txt_description, detectedLang[0]);
                            applyReadMoreIfNeeded(txt_description, originalText[0]);
                            txt_translated_content.setText(mActivity.getString(R.string.show_translated_content));
                            showingOriginal[0] = true;
                        }

                    });

                    txt_description.setOnTextLinkClickListener((textView, clickedString) -> {
                        if (clickedString.contains("#") && clickedString.startsWith("#")) {
                            String hashtag = clickedString.substring(1);
                            Intent intent = new Intent(mActivity, VideoListActivity.class);
                            intent.putExtra(Constants.TAG_TITLE, hashtag);
                            intent.putExtra(Constants.TAG_FROM, Constants.TAG_RELATED_HASHTAG);
                            mActivity.startActivity(intent);
                            clickListener.onItemClicked(false, getAbsoluteAdapterPosition(), Constants.TAG_PLAYBACK);
                        }
                    });

                    txt_description.setOnTextMoreClickListener((textView, clickedString) -> {
                        txt_description.setIsEndEllipsable(false);
                        txt_description.setText(originalText[0]);
                    });
                }

               /* String setupTag = (String) txt_description.getTag(R.id.txt_description);
                String currentDescription = videoItem.getVideoDescription();

                if (setupTag == null || !setupTag.equals(currentDescription)) {
                    txt_description.setTag(R.id.txt_description, currentDescription);

                    String originalDescription = videoItem.getVideoDescription();
                    final String[] detectedLang = {""};
                    final String[] translatedText = {""};
                    final String[] originalText = {originalDescription};
                    final boolean[] showingOriginal = {true};
                    final boolean[] isExpanded = {false};

                    txt_description.setIsLinkable(true);
                    txt_description.setIsEndEllipsable(true);
                    txt_description.setMaxLines(2);

                    // helper to remove previously appended markers (More / Less)
                    final java.util.function.Function<String, String> stripMarkers = (s) -> {
                        if (s == null) return "";
                        // Remove trailing "..More", " More", " Less", "Less", "…More" etc.
                        return s.replaceAll("\\s*(\\.\\.|…)?\\s*(More|Less)\\s*$", "");
                    };

                    // Helper method to update description text with More/Less toggle
                    Runnable updateDescription = () -> {
                        String baseText = showingOriginal[0] ? originalText[0] : translatedText[0];
                        if (baseText == null) {
                            txt_description.setText("");
                            return;
                        }

                        // Ensure we start from clean base (no leftover " More"/" Less")
                        String cleanBase = stripMarkers.apply(baseText);

                        txt_description.setIsEndEllipsable(true);
                        txt_description.setMaxLines(isExpanded[0] ? Integer.MAX_VALUE : 2);
                        txt_description.setText(cleanBase);

                        // measure and then append marker if needed
                        txt_description.post(() -> {
                            Layout layout = txt_description.getLayout();
                            if (layout != null && layout.getLineCount() > 2) {
                                if (isExpanded[0]) {
                                    txt_description.setIsEndEllipsable(false);
                                    txt_description.setMaxLines(Integer.MAX_VALUE);
                                    txt_description.setText(cleanBase + " Less");
                                } else {
                                    txt_description.setIsEndEllipsable(true);
                                    txt_description.setMaxLines(2);
                                    txt_description.setText(cleanBase + " More");
                                }
                            } else {
                                // fits without truncation -> show plain base text
                                txt_description.setIsEndEllipsable(false);
                                txt_description.setMaxLines(Integer.MAX_VALUE);
                                txt_description.setText(cleanBase);
                            }
                        });
                    };

                    // Translation API call
                    TranslateUtil.translateText(originalDescription, "en", new TranslateUtil.TranslationCallback() {
                        @Override
                        public void onTranslated(String translated, String original, String lang) {
                            mActivity.runOnUiThread(() -> {
                                detectedLang[0] = lang;
                                translatedText[0] = (translated != null) ? translated : "";
                                originalText[0] = original != null ? original : originalText[0];
                                showingOriginal[0] = true;
                                isExpanded[0] = false;

                                if ("en".equals(lang)) {
                                    txt_translated_content.setVisibility(View.GONE);
                                } else {
                                    txt_translated_content.setVisibility(View.VISIBLE);
                                    txt_translated_content.setText(mActivity.getString(R.string.show_translated_content));
                                }

                                updateDescription.run();
                            });
                        }
                    });

                    // Toggle translation click
                    txt_translated_content.setOnClickListener(v -> {
                        showingOriginal[0] = !showingOriginal[0];
                        isExpanded[0] = false;

                        if (showingOriginal[0]) {
                            txt_translated_content.setText(mActivity.getString(R.string.show_translated_content));
                        } else {
                            txt_translated_content.setText(mActivity.getString(R.string.show_original_content));
                        }

                        updateDescription.run();
                    });

                    // Handle "More" / "Less" toggle
                    txt_description.setOnTextMoreClickListener((textView, clickedString) -> {
                        isExpanded[0] = !isExpanded[0];
                        updateDescription.run();
                    });

                    // Hashtag click
                    txt_description.setOnTextLinkClickListener((textView, clickedString) -> {
                        if (clickedString.contains("#") && clickedString.startsWith("#")) {
                            String hashtag = clickedString.substring(1);
                            Intent intent = new Intent(mActivity, VideoListActivity.class);
                            intent.putExtra(Constants.TAG_TITLE, hashtag);
                            intent.putExtra(Constants.TAG_FROM, Constants.TAG_RELATED_HASHTAG);
                            mActivity.startActivity(intent);
                            clickListener.onItemClicked(false, getAbsoluteAdapterPosition(), Constants.TAG_PLAYBACK);
                        }
                    });

                    // Show original description immediately while translation loads
                    updateDescription.run();
                }*/

                final GestureDetector gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        if(GetSet.isLogged()) {
                            if (!videoItem.getIsLiked()) {
                                if (!likePrevent) {
                                    likePrevent = true;
                                    clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), Constants.TAG_LIKE);
                                    heartFun(getAbsoluteAdapterPosition());
                                }

                            }
                            icFavHeart.setVisibility(VISIBLE);

                            icFavHeart.addAnimatorListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    Timber.i("onAnimationStart:");
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    Timber.i("onAnimationEnd");
                                    icFavHeart.clearAnimation();
                                    icFavHeart.setVisibility(GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                    Timber.i("onAnimationCancel");
                                    icFavHeart.clearAnimation();
                                    icFavHeart.setVisibility(GONE);

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {
                                    Timber.i("onAnimationRepeat");
                                }
                            });

                            icFavHeart.post(() -> {
                                icFavHeart.playAnimation();
                                icFavHeart.startAnimation(AnimationUtils.loadAnimation(itemView.getContext(), R.anim.heart_animation)); // need clear animation
                            });
                        }

                        return super.onDoubleTap(e);
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (icFavHeart.getVisibility() != VISIBLE) {
                            clickListener.onItemClicked(null, 0, Constants.TAG_SINGLE_TAP);
                        }
                        return super.onSingleTapConfirmed(e);
                    }
                });


                //Added for sep9 for livestreaming
                teststreaming.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //For testing I added static username
                        Log.e(TAG, "TestLiveStreaming ");
                        Log.e(TAG, "userId :  " + GetSet.getUserId());
                        Log.e(TAG, "userName :  " + GetSet.getName());
                        signInZEGOSDK(GetSet.getUserId(), GetSet.getName(), (errorCode, message) -> {
                            Log.e(TAG, "Error code : " + errorCode + "," + message);
                            if (errorCode == 0 || errorCode == 6000111) {
                                Intent giftIntent = new Intent(mActivity, StartLiveActivity.class);
                                giftIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                mActivity.startActivity(giftIntent);
                            }
                        });
                       /* Intent giftIntent = new Intent(mActivity, PkLiveStreamingActivity.class);
                        mActivity.startActivity(giftIntent);*/
                    }
                });


                likeLay.setOnTouchListener((v, event) -> {
                    gestureDetector.onTouchEvent(event);
                    return true;
                });

                audioLay.setVisibility(View.GONE);
                musicLay.setVisibility(View.GONE);

                txtAudioTitle.setSelected(false);

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bottomLay.getLayoutParams();
                if (!TextUtils.isEmpty(videoItem.getSoundtracks().getSoundId())) {
                    params.bottomMargin = 0;
                    audioLay.setVisibility(GONE);
                    musicLay.setVisibility(View.VISIBLE);
                    txtAudioTitle.setSelected(true);
                    String soundName;
                    if (!TextUtils.isEmpty(videoItem.getSoundtracks().getSoundBy())) {
                        soundName = mActivity.getResources().getString(R.string.original_audio_by) + " " +
                                videoItem.getSoundtracks().getSoundBy();
                    } else {
                        soundName = videoItem.getSoundtracks().getTitle();
                    }
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) txtAudioTitle.getLayoutParams();
                    if (soundName.length() <= 30) {
                        layoutParams.width = (int) (DevicePref.getInstance(mActivity).getInt(DevicePref.SCREEN_WIDTH, 0) * 0.4);
//                        txtAudioTitle.setMaxWidth((int) (DevicePref.getInstance(mActivity).getInt(DevicePref.SCREEN_WIDTH, 0) * 0.4));
                    } else {
                        layoutParams.width = (int) (DevicePref.getInstance(mActivity).getInt(DevicePref.SCREEN_WIDTH, 0) * 0.6);
//                        txtAudioTitle.setMaxWidth((int) (DevicePref.getInstance(mActivity).getInt(DevicePref.SCREEN_WIDTH, 0)));
                    }
                    txtAudioTitle.setLayoutParams(layoutParams);
                    txtAudioTitle.setText(soundName);
                } else {
                    //commented on jan9  params.bottomMargin = (int) mActivity.getResources().getDimension(R.dimen.activity_vertical_margin);
                }
                //commented on jan9 bottomLay.setLayoutParams(params);

                giftLay.setOnClickListener(v -> {
                    if (GetSet.getUserId().equals(videoItem.getPublisherId())) {
                        /*Intent giftIntent = new Intent(mActivity, ConvertGiftActivity.class);
                        giftIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        mActivity.startActivity(giftIntent);*/
                    } else {
                        App.preventMultipleClick(giftLay);
                        clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), Constants.TAG_GIFT);
                        SelectedVideoPosition = getAbsoluteAdapterPosition();
                        //openGiftDialog(); - old method
                        openGiftDialogNew();
                    }
                });

                videoCommentLay.setOnClickListener(v -> {
                    App.preventMultipleClick(videoCommentLay);
                    if (clickListener != null) {
                        App.preventMultipleClick(videoCommentLay);
                        // Do not force shrink on click; let onSlide drive scaling continuously
                        clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), Constants.TAG_COMMENT);
                        openCommentList(videoItem.getPublisherId(), videoItem.getVideoId(), getAbsoluteAdapterPosition(), leftSideView);
                    }
                });

                repostLay.setOnClickListener(v->{
                    App.preventMultipleClick(repostLay);

                    if(GetSet.isIsLogged()){
                    if(clickListener!=null){
                        App.preventMultipleClick(repostLay);
                        if(isReposted){
                            callRepostAPI(videoItem.getVideoId(),updatedText,"delete");
                        }
                        else{
                            callRepostAPI(videoItem.getVideoId(),updatedText,"create");
                        }

                    }}
                    else{
                        loginview();
                    }
                });

                progressBar.setVisibility(GONE);
             /*   heartLay.setOnClickListener(v -> {

                    if (!likePrevent) {
                        String abc = videoItem.getLikes();
                        int inum = Integer.parseInt(abc);
                        clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), Constants.TAG_LIKE);
                        likePrevent = true;
                        heartFun(getAbsoluteAdapterPosition());
                        if (!videoItem.getIsLiked()) {
                            // txt_heart_count.setVisibility(View.INVISIBLE);

                            // txt_heart_count.setVisibility(View.INVISIBLE);

                            if (heart.getDrawable().equals(mActivity.getResources().getDrawable(R.drawable.heart_new_unselected_2))) {

                                String aq = String.valueOf(inum + 1);
                                heart.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.heart_new_selected_2));
                                txt_heart_count.setText(aq);
                            } else {
                                heart.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.heart_new_unselected_2));
                                txt_heart_count.setText(String.valueOf(inum));
                            }


                        } else {
                            if (heart.getDrawable().equals(mActivity.getResources().getDrawable(R.drawable.heart_new_selected_2))) {
                                String aq = String.valueOf(inum - 1);
                                heart.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.heart_new_unselected_2));
                                txt_heart_count.setText(aq);
                            } else {
                                heart.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.heart_new_selected_2));
                                txt_heart_count.setText(String.valueOf(inum));

                            }
                        }

                    } else {
                        Log.d(TAG, "onBind: not_view");
                        if (Constants.Login_method.equals("skip")) {
                            loginview();
                        }

                    }
                });
*/

                heartLay.setOnClickListener(v -> {
                    int adapterPosition = getAbsoluteAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) return;

                    if (!likePrevent) {
                        String abc = videoItem.getLikes();
                        int inum = 0;
                        try {
                            inum = Integer.parseInt(abc);
                        } catch (NumberFormatException e) {
                            inum = 0;
                        }

                        // Notify external listener (your existing network / analytics entry point)
                        clickListener.onItemClicked(videoItem, adapterPosition, Constants.TAG_LIKE);

                        // Prevent rapid double taps (keeps your existing guard)
                        likePrevent = true;

                        // Local helper that you already use
                        heartFun(adapterPosition);

                        // Compute new liked state and new count based on current model
                        boolean currentlyLiked = false;
                        try {
                            currentlyLiked = videoItem.getIsLiked();
                        } catch (Exception ignored) {}

                        boolean newLikedState = !currentlyLiked;
                        int newCount = inum;
                        if (newLikedState) newCount = inum + 1;
                        else newCount = Math.max(inum - 1, 0);

                        // Update model (so later views / logic read correct state)
                        try {
                            videoItem.setIsLiked(newLikedState);
                            videoItem.setLikes(String.valueOf(newCount));
                        } catch (Exception ignored) {
                            // If setters aren't present, we still update UI and send payload below.
                        }

                        // ---- Update UI immediately (same look-and-feel as your existing code) ----
                        try {
                            if (!currentlyLiked) {
                                // was not liked
                                if (heart.getDrawable().getConstantState() != mActivity.getResources().getDrawable(R.drawable.heart_new_unselected_2).getConstantState()) {
                                    // if drawable differs, still set to selected and increment
                                    heart.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.heart_new_selected_2));
                                    txt_heart_count.setText(String.valueOf(newCount));
                                } else {
                                    // same branch as your old logic: toggle to selected and show increment
                                    heart.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.heart_new_selected_2));
                                    txt_heart_count.setText(String.valueOf(newCount));
                                }
                            } else {
                                // was liked
                                if (heart.getDrawable().getConstantState() != mActivity.getResources().getDrawable(R.drawable.heart_new_selected_2).getConstantState()) {
                                    heart.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.heart_new_selected_2));
                                    txt_heart_count.setText(String.valueOf(inum));
                                } else {
                                    heart.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.heart_new_unselected_2));
                                    txt_heart_count.setText(String.valueOf(newCount));
                                }
                            }
                        } catch (Exception e) {
                            // Fallback: just set icon and count
                            heart.setImageDrawable(newLikedState ? mActivity.getResources().getDrawable(R.drawable.heart_new_selected_2)
                                    : mActivity.getResources().getDrawable(R.drawable.heart_new_unselected_2));
                            txt_heart_count.setText(String.valueOf(newCount));
                        }

                        // Keep visibility same as before
                        heart.setVisibility(View.VISIBLE);
                        txt_heart_count.setVisibility(View.VISIBLE);

                        // ---- Send partial update payload so adapter does NOT full-rebind the item ----
                        Bundle payload = new Bundle();
                        payload.putString("likes_count", String.valueOf(newCount));
                        payload.putString("likes_status", String.valueOf(newLikedState));
                        // IMPORTANT: use adapterPosition from getAbsoluteAdapterPosition() to be safe
                        notifyItemChanged(adapterPosition, payload);

                        // Note: your clickListener already triggers server request. When server confirms,
                        // it should also call notifyItemChanged(pos, payloadFromServer) — that will also
                        // be handled as a partial update and won't rebind description.
                    } else {
                        Log.d(TAG, "onBind: not_view");
                        if (Constants.Login_method.equals("skip")) {
                            loginview();
                        }
                    }
                });

                repostEditIcon.setOnClickListener(v->{
//                    clickListener.onItemClicked(videoItem.getVideoId(), getAbsoluteAdapterPosition(), Constants.TAG_REPOST);
                    openRepostEditSheet(videoItem.getVideoId(), repostDescription,repostEditImage);
                });

                parentLay.setOnLongClickListener(v -> {
                    moreLay.performClick();
                    return false;
                });

                liveStreaming.setVisibility(Constants.ADDON_LIVE_STREAM ? View.VISIBLE : View.GONE);
                //Addon Live Stream
                liveStreaming.setOnClickListener(v -> {

                    if (Constants.Login_method.equals("skip")) {
                        loginview();
                    } else {
                        /*Intent liveStreaming = new Intent(mActivity, StreamListActivity.class);
                        liveStreaming.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                        liveStreaming.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                        liveStreaming.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                        liveStreaming.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                        liveStreaming.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                        mActivity.startActivity(liveStreaming);*/
                    }
                });

                moreLay.setOnClickListener(v -> {
                    if (Constants.Login_method.equals("skip")) {
                        loginview();
                    } else {
                        App.preventMultipleClick(moreLay);
                        HomeBottomDialog bottomDialog = new HomeBottomDialog(mActivity);
                        bottomDialog.openMenu(mHomeApiResponse, getAbsoluteAdapterPosition(), this);
                    }
                });

               /*commented by shankari
                if (videoItem.getSoundtracks().getSoundId() == null)
                    lottieAudio.setVisibility(View.GONE);
                else if (videoItem.getSoundtracks().getSoundId().equals(""))
                    lottieAudio.setVisibility(View.GONE);
                else
                    lottieAudio.setVisibility(View.VISIBLE);*/

                audioLay.setOnClickListener(v -> {
                    App.preventMultipleClick(audioLay);
                    /*if (videoItem.getPublisherId() != null) {
                        clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), "sound");
                    }*/

                    if (videoItem.getSoundtracks().isUseAudio()) {
                        HomeResponse.Result item = videoItem;
                        //    clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), Constants.TAG_SOUND);
                        MusicListResponse.Result musicItem = new MusicListResponse().new Result();
                        musicItem.setSoundId(item.getSoundtracks().getSoundId());
                        musicItem.setSoundBy(item.getSoundtracks().getSoundBy());
                        musicItem.setTitle(item.getSoundtracks().getTitle());
                        musicItem.setSoundUrl(item.getSoundtracks().getSoundUrl());
                        musicItem.setIsFavorite(item.getSoundtracks().getIsFavorite());
                        musicItem.setCoverImage(item.getSoundtracks().getCoverImage());
                        musicItem.setDuration(item.getSoundtracks().getDuration());
                        Intent intent = new Intent(mActivity, VideoListActivity.class);
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_MUSIC);
                        intent.putExtra(Constants.TAG_INTENT_DATA, new Gson().toJson(musicItem));
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        mActivity.startActivityForResult(intent, Constants.MUSIC_REQUEST_CODE);
                    } else {
                        App.makeToast(mActivity.getString(R.string.audio_disabled_description));
                    }
                });

                musicLay.setOnClickListener(v -> {
                    App.preventMultipleClick(musicLay);
                    /*if (videoItem.getPublisherId() != null) {
                        clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), "sound");
                    }*/

                    if (videoItem.getSoundtracks().isUseAudio()) {
                        HomeResponse.Result item = videoItem;
                        //    clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), Constants.TAG_SOUND);
                        MusicListResponse.Result musicItem = new MusicListResponse().new Result();
                        musicItem.setSoundId(item.getSoundtracks().getSoundId());
                        musicItem.setSoundBy(item.getSoundtracks().getSoundBy());
                        musicItem.setTitle(item.getSoundtracks().getTitle());
                        musicItem.setSoundUrl(item.getSoundtracks().getSoundUrl());
                        musicItem.setIsFavorite(item.getSoundtracks().getIsFavorite());
                        musicItem.setCoverImage(item.getSoundtracks().getCoverImage());
                        musicItem.setDuration(item.getSoundtracks().getDuration());
                        Intent intent = new Intent(mActivity, VideoListActivity.class);
                        intent.putExtra(Constants.TAG_FROM, Constants.TAG_MUSIC);
                        intent.putExtra(Constants.TAG_INTENT_DATA, new Gson().toJson(musicItem));
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        mActivity.startActivityForResult(intent, Constants.MUSIC_REQUEST_CODE);
                    } else {
                        App.makeToast(mActivity.getString(R.string.audio_disabled_description));
                    }
                });

                profileFollowIcon.setOnClickListener(v -> {
                    Log.d(TAG, "yessss: ");
                    getProfile(GetSet.getUserId(), videoItem.getPublisherId(), profileFollowIcon, videoItem, getAbsoluteAdapterPosition(), followButton);
                    // getProfile(GetSet.getUserId(), videoItem.getPublisherId(), profileFollowIcon, videoItem, getAbsoluteAdapterPosition(), followButton2);

                });

                followButton.setOnClickListener(v -> {
                    getProfile(GetSet.getUserId(), videoItem.getPublisherId(), profileFollowIcon, videoItem, getAbsoluteAdapterPosition(), followButton);
                });

//                followButton2.setOnClickListener(v -> {
//                    getProfile(GetSet.getUserId(), videoItem.getPublisherId(), profileFollowIcon, videoItem, getAbsoluteAdapterPosition(), followButton2);
//
//                });

                profileImage.setOnClickListener(v -> {
                    if (videoItem.getPublisherId() != null) {
                        clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), Constants.TAG_PROFILE);
                    }
                });

//                profileImageNew.setOnClickListener(v -> {
//                    if (videoItem.getPublisherId() != null) {
//                        clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), Constants.TAG_PROFILE);
//                    }
//                });
                profileImageNew.setOnClickListener(v -> {
                    if (videoItem.getPublisherId() != null) {

                        // ✅ Easy way to hide views directly
                        View imageButton = ((Activity) v.getContext()).findViewById(R.id.imageButton);
                        View relativeLive = ((Activity) v.getContext()).findViewById(R.id.relativeLive);
                        View toplay = ((Activity) v.getContext()).findViewById(R.id.toplay);

                        if (imageButton != null) imageButton.setVisibility(View.GONE);
                        if (relativeLive != null) relativeLive.setVisibility(View.GONE);


                        // ✅ Open profile activity
                        Intent intent = new Intent(v.getContext(), OthersProfileActivity.class);
                        intent.putExtra(Constants.TAG_USER_ID, videoItem.getPublisherId());
                        intent.putExtra(Constants.TAG_USER_NAME, videoItem.getPostedBy());
                        v.getContext().startActivity(intent);
                    }
                });

                profileImage.setOnClickListener(v -> {
                    if (videoItem.getPublisherId() != null) {
                        // ✅ Easy way to hide views directly
                        View imageButton = ((Activity) v.getContext()).findViewById(R.id.imageButton);
                        View relativeLive = ((Activity) v.getContext()).findViewById(R.id.relativeLive);
                        View toplay = ((Activity) v.getContext()).findViewById(R.id.toplay);

                        if (imageButton != null) imageButton.setVisibility(View.GONE);
                        if (relativeLive != null) relativeLive.setVisibility(View.GONE);

                        // ✅ Open profile activity
                        Intent intent = new Intent(v.getContext(), OthersProfileActivity.class);
                        intent.putExtra(Constants.TAG_USER_ID, videoItem.getPublisherId());
                        intent.putExtra(Constants.TAG_USER_NAME, videoItem.getPostedBy());
                        v.getContext().startActivity(intent);
                    }
                });


                txt_title.setOnClickListener(v -> {
                    if (apiType.equals(Constants.FOR_YOU)) {
                        if (videoItem.getPlaytype().equals("video"))
                            clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), Constants.TAG_PROFILE);
                    } else {
                        clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), Constants.TAG_PROFILE);
                    }
                });

//                userName.setOnClickListener(v -> {
//                    if (apiType.equals(Constants.FOR_YOU)) {
//                        if (videoItem.getPlaytype().equals("video"))
//                            clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), Constants.TAG_PROFILE);
//                    } else {
//                        clickListener.onItemClicked(videoItem, getAbsoluteAdapterPosition(), Constants.TAG_PROFILE);
//                    }
//                });

            }
            else {
                icFavHeart.setVisibility(View.GONE);
                audioLay.setVisibility(View.GONE);
                /*requestManager
                        .load(videoItem.getStreamThumbnail() != null ? videoItem.getStreamThumbnail() : R.drawable.novideo_placeholder)
                        .fitCenter()
                        .into(video_thumbnail);*/
            /*    Glide.with(mActivity).load(videoItem.getStreamThumbnail() != null ? videoItem.getStreamThumbnail() : R.drawable.novideo_placeholder)
                        .fitCenter()
                        .into(video_thumbnail);*/

                Glide.with(mActivity).load(videoItem.getStreamThumbnail())
                        .into(video_thumbnail);

                leftSideView.setVisibility(View.GONE);
                //  leftSideVieww.setVisibility(View.GONE);

                if (videoItem.getLink_type().equals("Button")) {
                    parentLay.setOnLongClickListener(v -> {
                        return false;
                    });
                    txt_title.setOnClickListener(v -> {
                        return;
                    });
                    userName.setOnClickListener(v -> {
                        return;
                    });
                    add_button.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onBind: textcolor" + videoItem.getText_color());
                    add_button.setTextColor(Color.parseColor(videoItem.getText_color()));
                    add_button.setBackgroundColor(Color.parseColor(videoItem.getButton_color()));
                    frame.setVisibility(View.GONE);
                } else {
                    parentLay.setOnLongClickListener(v -> {
                        return false;
                    });
                    txt_title.setOnClickListener(v -> {
                        return;
                    });
                    userName.setOnClickListener(v -> {
                        return;
                    });
                    frame.setVisibility(View.VISIBLE);
                    add_button.setVisibility(View.GONE);
                }

                txt_title.setText(videoItem.getPostedBy());
                userName.setText(videoItem.getPostedBy());
                //  txt_description.setText(videoItem.getVideoDescription());

                audio_url = videoItem.getAndroidUrl();
                Log.d(TAG, "onBind_audio_url: " + audio_url);


            }


              /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    Bitmap bitmap = Bitmap.createBitmap(blurframe.getWidth(), blurframe.getHeight(), Bitmap.Config.ARGB_8888);
                    blurframe.draw(new Canvas(bitmap));

                    RenderScript renderScript = RenderScript.create(mActivity);
                    Allocation input = Allocation.createFromBitmap(renderScript, bitmap);
                    Allocation output = Allocation.createTyped(renderScript, input.getType());
                    ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
                    blurScript.setRadius(10f); // Set blur radius
                    blurScript.setInput(input);
                    blurScript.forEach(output);
                    output.copyTo(bitmap);

                    blurframe.setBackground(new BitmapDrawable(mActivity.getResources(), bitmap));
                    renderScript.destroy();
                }*/

           /* float radius = 20f;
            View decorView = mActivity.getWindow().getDecorView();
            ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
            Drawable windowBackground = decorView.getBackground();
            blurredBackground.setupWith(rootView) // Optionally pass RenderEffectBlur or RenderScriptBlur as the second parameter
                    .setFrameClearDrawable(windowBackground) // Optional. Useful when your root has a lot of transparent background, which results in semi-transparent blurred content. This will make the background opaque
                    .setBlurRadius(radius)
                    .setBlurEnabled(true);*/

            Log.e(TAG, "SHANKARI : " + videoItem.getStreamThumbnail());
           /* Glide.with(mActivity).load(videoItem.getStreamThumbnail())
                    .centerCrop()
                    .into(video_thumbnail);*/

       /*     Glide.with(mActivity).load(videoItem.getStreamThumbnail())
                    .fitCenter()
                    .into(video_thumbnail);
*/

            Glide.with(mActivity).load(videoItem.getStreamThumbnail())
                    .into(video_thumbnail);
         /*   Blurry.with(mActivity)
                    .radius(10) // Adjust the radius for desired blur effect
                    .sampling(2) // Adjust for better performance if needed
                    .async()
                    .capture(video_thumbnail)
                    .into(blurredBackground);*/

            /*video_thumbnail.setDrawingCacheEnabled(true);
            video_thumbnail.buildDrawingCache(true);

            video_thumbnail.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    video_thumbnail.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    Bitmap bitmap = getBitmapFromView(video_thumbnail);
                    if (bitmap != null) {
                        Bitmap blurredBitmap = BlurBuilder.blur(mActivity, bitmap);
                        blurredBackground.setImageBitmap(blurredBitmap);
                    }
                }
            });
*/

            add_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoItem.getAndroidUrl()));
                        mActivity.startActivity(browserIntent);
                        Log.d(TAG, "onClick_browserIntent: " + browserIntent);
                    } catch (Exception e) {
                        Toasty.error(mActivity, R.string.not_valid_url, Toasty.LENGTH_SHORT).show();
                    }
                }
            });
        }

        private void openRepostEditSheet(String videoId, String repostText,String UserImage) {
            repostDialog = new Dialog(mActivity);
            repostDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            repostDialog.setContentView(R.layout.repost_edit_sheet);

            Window window = repostDialog.getWindow();
            if (window != null) {


                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER);

                // NO DIM BEHIND VIDEO (TikTok style)
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                window.setDimAmount(0f);
            }

            EditText repostEditText = repostDialog.findViewById(R.id.repostEditText);
            TextView saveBtn = repostDialog.findViewById(R.id.repostAddBtn);
            RoundedImageView repostUserImage = repostDialog.findViewById(R.id.repostUserImage);

            Log.d(TAG, "openRepostEditSheet: "+UserImage);
            Glide.with(mActivity)
                    .load(UserImage)
                    .placeholder(R.drawable.default_profile_image)
                    .into(repostUserImage);

            repostEditText.setText(repostText);

            saveBtn.setOnClickListener(v -> {
                updatedText = repostEditText.getText().toString().trim();
                callRepostAPI(videoId,updatedText,"edit");

//                repostDialog.dismiss();
                App.hideKeyboardActivity(mActivity);
            });

            repostDialog.setCanceledOnTouchOutside(true);
            repostDialog.show();
        }

        private void callRepostAPI(String videoId, String desc,String action) {

            showLoader();

            String repostDes = desc;

            Call<RepostResponse> call = apiInterface.repostVideo(
                    GetSet.getToken(),
                    videoId,
                    GetSet.getUserId(),
                    action,
                    repostDes,
                    repostId
            );
            Log.e(TAG,"Repost params for edit : " + GetSet.getToken() + "\n"  +videoId + "\n " +  GetSet.getUserId() +"\n" +action +" \n" + repostDes +"\n " + repostId);

            call.enqueue(new Callback<RepostResponse>() {
                @Override
                public void onResponse(Call<RepostResponse> call, Response<RepostResponse> response) {
                    Log.e(TAG,"Repost res for edit : " + new Gson().toJson(response.body()));
                    if (response.isSuccessful() && response.body() != null) {

                        RepostResponse data = response.body();

                        hideLoader();
                        if (data.getStatus().equals("true")) {
//                            Toast.makeText(mActivity, data.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onResponse11111111: "+action);
                            if(action.equals("create")){

                                // find my own repost_id and save to repostId variable
                                if (data.getRepostData() != null) {
                                    for (RepostResponse.RepostData item : data.getRepostData()) {
                                        if (item.getUser_id().equals(GetSet.getUserId())) {
                                            repostId = item.getRepost_id();   // ← SAVE IT HERE
                                            Log.d(TAG, "Saved repostId: " + repostId+" "+item.getUser_image());


                                            Glide.with(mActivity)
                                                    .load(item.getUser_image())
                                                    .placeholder(R.drawable.default_profile_image)
                                                    .into(respostProfileImage);
                                            break;
                                        }
                                    }
                                }


                                repostTextLay.setVisibility(VISIBLE);
                                isReposted = true;
                                respostDesc.setText("");
//                                repostId = data.getRepostData()
                                Log.d(TAG, "onResponse11111: "+respostDesc.getText().toString());
                                respostDesc.setText(mActivity.getString(R.string.you_reposted));
                                repostEditIcon.setVisibility(VISIBLE);
                                repostIcon.setImageDrawable(mActivity.getDrawable(R.drawable.repost_home_filled));


                                if (data.getRepost_count() != null)
                                    repostcount.setText("" + data.getRepost_count());
                                else
                                    repostcount.setText("0");

                            }
                            else if(action.equals("edit")){
                                repostDialog.dismiss();
                                App.hideKeyboardActivity(mActivity);
                                repostTextLay.setVisibility(VISIBLE);
                                isReposted = true;
                                repostIcon.setImageDrawable(mActivity.getDrawable(R.drawable.repost_home_filled));
                                repostEditIcon.setVisibility(VISIBLE);
                                String name = GetSet.getUserName(); // your current user's name

                                if (updatedText == null || updatedText.trim().isEmpty()) {
                                    respostDesc.setText(mActivity.getString(R.string.user_reposted, name));
                                } else {
                                    repostDescription = updatedText;
                                    respostDesc.setText(updatedText);
                                }

                                if (data.getRepostData() != null) {
                                    for (RepostResponse.RepostData item : data.getRepostData()) {
                                        if (item.getUser_id().equals(GetSet.getUserId())) {
                                            repostId = item.getRepost_id();   // ← SAVE IT HERE
                                            Log.d(TAG, "Saved repostId: " + repostId+" "+item.getUser_image());


                                            Glide.with(mActivity)
                                                    .load(item.getUser_image())
                                                    .placeholder(R.drawable.default_profile_image)
                                                    .into(respostProfileImage);
                                            break;
                                        }
                                    }
                                }


                            } else if (action.equals("delete")) {
                                isReposted = false;
                                repostEditIcon.setVisibility(GONE);
                                repostIcon.setImageDrawable(mActivity.getDrawable(R.drawable.repost_home));

                                List<RepostResponse.RepostData> list = data.getRepostData();

                                if (list != null && !list.isEmpty()) {

                                    repostTextLay.setVisibility(VISIBLE);

                                    RepostResponse.RepostData first = list.get(0);
                                    if(data.getRepost_count()!=null){
                                    repostcount.setText(""+data.getRepost_count());}
                                    else{
                                        repostcount.setText("0");
                                    }
                                    respostDesc.setText("");
                                    String otherDesc = first.getDescription();

                                    String name = first.getDisplayName();

                                    Log.d(TAG, "Description: "+otherDesc+"  name: "+name);
                                    if (otherDesc == null || otherDesc.trim().isEmpty()) {

                                        respostDesc.setText(mActivity.getString(R.string.user_reposted, name));
                                    } else {
                                        respostDesc.setText(otherDesc);
                                    }

                                    Log.d(TAG, "onResponse1111111: "+first.getUser_image());
                                    Glide.with(mActivity)
                                            .load(first.getUser_image())
                                            .placeholder(R.drawable.default_profile_image)
                                            .into(respostProfileImage);
                            }


                                else
                                {
                                    if(data.getRepost_count()!=null){
                                        repostcount.setText(""+data.getRepost_count());}
                                    else{
                                        repostcount.setText("0");
                                    }
//                                    repostcount.setText("0");
                                    repostTextLay.setVisibility(GONE);
                                }
                            }


                        } else {
                            Toast.makeText(mActivity, data.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        hideLoader();
                        Toast.makeText(mActivity, "Repost failed!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<RepostResponse> call, Throwable t) {
                    Log.e(TAG,"Repost res for edit : " + t.getMessage());

                    hideLoader();
                    Toast.makeText(mActivity, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void toggleAssets(boolean show) {
            int visibility = show ? View.VISIBLE : View.GONE;
            leftSideView.setVisibility(visibility);
            bottomLay.setVisibility(visibility);
            newadlayout.setVisibility(visibility);
            progress_duration.setVisibility(visibility);
        }

        void hideViewsOnZoom(float scaleLeve) {
            int visibility;
            if (scaleLeve > 1)
                visibility = GONE;
            else
                visibility = VISIBLE;

            progress_duration.setVisibility(visibility);
            leftSideView.setVisibility(visibility);
            videoTextLay.setVisibility(visibility);
            newadlayout.setVisibility(visibility);

        }

        private Bitmap getBitmapFromView(View view) {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        }

        private void loginview() {
            if (icFavHeart.getVisibility() != View.VISIBLE) {
                clickListener.onItemClicked(null, 0, Constants.TAG_SINGLE_TAP);
                GetSet.reset();
                SharedPref.clearAll();
                DBHelper.getInstance(mActivity).clearDB();
                Intent logout = new Intent(mActivity, LoginActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(logout);
            }

        }


        private void addReadMoreWithHashtags(LinkEllipseTextView textView, String fullText, int maxLines) {
            textView.post(() -> {
                Layout layout = textView.getLayout();
                if (layout == null) return;

                int lineCount = layout.getLineCount();
                if (lineCount <= maxLines) return;

                int endIndex = layout.getLineEnd(maxLines - 1);
                String visibleText = fullText.substring(0, Math.min(endIndex, fullText.length()));
                String moreText = "... more";

                SpannableStringBuilder ssb = new SpannableStringBuilder(visibleText + moreText);

                // 🔹 clickable "more"
                ssb.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        showFullTextWithLess(textView, fullText, maxLines);
                    }
                    @Override
                    public void updateDrawState(@NonNull TextPaint ds) {
                        ds.setColor(Color.parseColor("#FF4081"));
                        ds.setUnderlineText(false);
                    }
                }, ssb.length() - "more".length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                textView.setText(ssb);
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            });
        }

        private void showFullTextWithLess(LinkEllipseTextView textView, String fullText, int maxLines) {
            String lessText = " less";
            SpannableStringBuilder ssb = new SpannableStringBuilder(fullText + lessText);

            // 🔹 clickable "less"
            ssb.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    textView.setText(fullText);
                    addReadMoreWithHashtags(textView, fullText, maxLines);
                }
                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    ds.setColor(Color.parseColor("#FF4081"));
                    ds.setUnderlineText(false);
                }
            }, ssb.length() - "less".length(), ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            textView.setText(ssb);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }


        private void addReadMore(final String text, final LinkEllipseTextView textView) {
            int maxLength = 100;
            String displayText;

            // If text is shorter than 100, just show it without "Read More"
            if (text.length() <= maxLength) {
                displayText = text;
                textView.setText(displayText);
                return;
            }

            // Otherwise, show truncated text with "Read More"
            displayText = text.substring(0, maxLength) + mActivity.getResources().getString(R.string.read_more1);
            SpannableString ss = new SpannableString(displayText);

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    addReadLess(text, textView);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    ds.setColor(mActivity.getResources().getColor(R.color.colorYellow));
                }
            };

            // Apply span only on the "Read More" part
            String readMoreText = mActivity.getResources().getString(R.string.read_more1);
            int start = ss.length() - readMoreText.length();
            ss.setSpan(clickableSpan, start, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            textView.setText(ss);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        private void addReadLess(final String text, final LinkEllipseTextView textView) {
            SpannableString ss = new SpannableString(text + mActivity.getResources().getString(R.string.read_less1));
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    addReadMore(text, textView);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ds.setColor(mActivity.getResources().getColor(R.color.colorYellow));
                    } else {
                        ds.setColor(mActivity.getResources().getColor(R.color.colorYellow));
                    }
                }
            };
            ss.setSpan(clickableSpan, ss.length() - 6, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(ss);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        @Override
        public void setFavourite(boolean favourite, ArrayList<HomeResponse.Result> result, int adapterPosition) {
            mHomeApiResponse.get(adapterPosition).setVideoIsFavorite(favourite);
//            notifyItemChanged(adapterPosition);
        }

        @Override
        public void setPlayback(boolean b, int adapterPosition, String tagPlayback) {
            clickListener.onItemClicked(b, adapterPosition, Constants.TAG_PLAYBACK);
        }

        @Override
        public void setVideoReport(boolean b, int adapterPosition) {
            mHomeApiResponse.get(adapterPosition).setIsVideoReported(b);
//            notifyItemChanged(adapterPosition);
        }

        @Override
        public void checkStoragePermissions() {
            clickListener.onItemClicked(true, getAbsoluteAdapterPosition(), Constants.TAG_PERMISSION);
        }

        @Override
        public void moveToIntent(Intent intent) {
//            clickListener.onItemClicked(intent, getAbsoluteAdapterPosition(), Constants.TAG_INTENT);
            if (intent.hasExtra(Constants.TAG_EDIT)) {
                clickListener.onItemClicked(intent, getAbsoluteAdapterPosition(), Constants.TAG_EDIT);
            } else {
                mActivity.startActivity(intent);
            }
        }

        @Override
        public void setVideoDeleted(boolean isDeleted, String videoId) {
            if (isDeleted) {
                clickListener.onItemClicked(videoId, getAbsoluteAdapterPosition(), Constants.TAG_DELETE);
            }
        }

        @Override
        public void setVideoPlayBackSpeed(boolean speedChanged, String videoId, int adapterPosition, String playbackSpeed, float tagPlaybackSpeed) {
            if(speedChanged){
                speecChangeListener.onItemPlaybackSpeed(true,adapterPosition,videoId,tagPlaybackSpeed,playbackSpeed);
            }
        }

        @Override
        public void setVideoCaption(boolean captionEnabled, String videoId, int adapterPosition, String typeCaption, String playBackUrl) {

            if(captionEnabled){
                speecChangeListener.onItemPlaybackSpeed(captionEnabled,adapterPosition,videoId,typeCaption,playBackUrl);
            }
        }

        @Override
        public void reposted(boolean reposted, String videoId, int adapterPosition, String action) {

            speecChangeListener.onItemReposted(reposted,adapterPosition,videoId,action,respostDesc.getText().toString());

            Log.d(TAG, "reposted11111111111111111: ");
            repostTextLay.setVisibility(VISIBLE);


        }



      /*  @Override
        public void reposted(boolean reposted, String videoId, int adapterPosition, String action) {
            clickListener.onItemClicked(videoId, getAbsoluteAdapterPosition(), Constants.TAG_REPOST);

        }*/


    }

    private void openRepostUsersBottomSheet(List<HomeResponse.Result.RepostedData> originalList) {

        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        View view = LayoutInflater.from(mActivity)
                .inflate(R.layout.repost_users_bottom_sheet, null);

        RecyclerView rv = view.findViewById(R.id.repostUserListRv);
        rv.setLayoutManager(new LinearLayoutManager(mActivity));

        String currentUserId = GetSet.getUserId();

        List<HomeResponse.Result.RepostedData> sortedList = new ArrayList<>();

        // Add current user first
        for (HomeResponse.Result.RepostedData data : originalList) {
            if (data.getUserId().equals(currentUserId)) {
                sortedList.add(data);
                break;
            }
        }

        // Add other users
        for (HomeResponse.Result.RepostedData data : originalList) {
            if (!data.getUserId().equals(currentUserId)) {
                sortedList.add(data);
            }
        }

        rv.setAdapter(new RepostUsersAdapter(mActivity, sortedList));

        dialog.setContentView(view);
        dialog.show();
    }







    private void openGiftDialogNew() {

        CommonApi.getGiftsList(mActivity);

        View sheetView = mActivity.getLayoutInflater().inflate(R.layout.bottom_sheet_gift_home, null);
        giftDialog = new BottomSheetDialog(mActivity, R.style.VideoCommentNewDialog); // Style here
        giftDialog.setContentView(sheetView);

        giftDialog.setOnDismissListener(dialogInterface -> {

        });

        sendLay = sheetView.findViewById(R.id.sendLay);
        txtAttachmentName = sheetView.findViewById(R.id.txtAttachmentName);
        txtSend = sheetView.findViewById(R.id.txtSend);
        sheettitle = sheetView.findViewById(R.id.sheettitle);
        recyclerView = sheetView.findViewById(R.id.recyclerView);
        giftNullLay = sheetView.findViewById(R.id.nullLay);
        giftNullImage = sheetView.findViewById(R.id.nullImage);
        giftNullText = sheetView.findViewById(R.id.nullText);

        giftNullText.setText(mActivity.getString(R.string.no_gift_founded));
        giftNullText.setTextColor(ContextCompat.getColor(mActivity, R.color.colorWhite));
        giftNullImage.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.gift));
        sendLay.setVisibility(View.GONE);
        sheettitle.setVisibility(View.VISIBLE);

        giftDialog.show();

        if (giftNullLay != null) {
            if (!AdminData.giftList.isEmpty()) {
                recyclerView.setVisibility(View.VISIBLE);
                giftNullLay.setVisibility(View.GONE);
            } else {
                giftNullLay.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }

        /*tempGiftList used only for display, For send AdminData.giftList used*/
        /*tempGiftList = new ArrayList<>();
        for (int i = 0; i < AdminData.giftList.size(); i++) {
            tempGiftList.add(AdminData.giftList.get(i));
        }
*/

        // Prepare tempGiftList
        tempGiftList = new ArrayList<>(AdminData.giftList);


        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();

        int screenWidth = displayMetrics.widthPixels;
        int spanCount = 4;
        int spacing = 100;
        // int spacing = mActivity.getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        int totalSpacing = spacing * (spanCount - 1);
        int itemWidth = (screenWidth - totalSpacing) / spanCount;
        Log.e(TAG, itemWidth + "test");

        GiftAdapter giftAdapter = new GiftAdapter(mActivity, tempGiftList, this, itemWidth);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, spanCount);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(giftAdapter);
        //recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));

        txtSend.setOnClickListener(view -> {
            sendGift();
            sheettitle.setVisibility(View.VISIBLE);
            sendLay.setVisibility(View.GONE);
            txtAttachmentName.setText("");
        });
    }


    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private final int spanCount;
        private final int spacing;
        private final boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            // Debugging the offsets and spacing
            // Log.d("GridSpacing", "Position: " + position + ", Column: " + column);

            if (includeEdge) {
                // Left spacing
                outRect.left = spacing - column * spacing / spanCount;

                // Right spacing
                outRect.right = (column + 1) * spacing / spanCount;

                // Top spacing (for the first row)
                if (position < spanCount) {
                    outRect.top = spacing;
                }
                // Bottom spacing for all rows
                outRect.bottom = spacing;
            } else {
                // Left spacing for items not in the first column
                outRect.left = column * spacing / spanCount;

                // Right spacing for items not in the last column
                outRect.right = spacing - (column + 1) * spacing / spanCount;

                // Top spacing for all except the first row
                if (position >= spanCount) {
                    outRect.top = spacing;
                }
            }
        }
    }


    private void openGiftDialog() {

        View sheetView = mActivity.getLayoutInflater().inflate(R.layout.bottom_sheet_gift_home, null);
        giftDialog = new BottomSheetDialog(mActivity, R.style.BottomSheetDialog); // Style here
        giftDialog.setContentView(sheetView);

        giftDialog.setOnDismissListener(dialogInterface -> {

        });

        viewPager = sheetView.findViewById(R.id.viewPager);
        sendLay = sheetView.findViewById(R.id.sendLay);
        txtAttachmentName = sheetView.findViewById(R.id.txtAttachmentName);
        txtSend = sheetView.findViewById(R.id.txtSend);
        pagerIndicator = sheetView.findViewById(R.id.pagerIndicator);
        giftNullLay = sheetView.findViewById(R.id.nullLay);
        giftNullImage = sheetView.findViewById(R.id.nullImage);
        giftNullText = sheetView.findViewById(R.id.nullText);

        giftNullText.setText(mActivity.getString(R.string.no_gift_founded));
        giftNullText.setTextColor(ContextCompat.getColor(mActivity, R.color.colorWhite));
        giftNullImage.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.gift));
        sendLay.setVisibility(View.GONE);

        setViewPager();
        giftDialog.show();

        txtSend.setOnClickListener(view -> {
            sendGift();
            sendLay.setVisibility(View.GONE);
            txtAttachmentName.setText("");
        });
    }

    private void setViewPager() {
        int count;
        if (AdminData.giftList != null && AdminData.giftList.size() <= ITEM_LIMIT) {
            count = 1;
        } else {
            count = AdminData.giftList.size() % ITEM_LIMIT == 0 ? AdminData.giftList.size() / ITEM_LIMIT : (AdminData.giftList.size() / ITEM_LIMIT) + 1;
        }


        if (giftNullLay != null) {
            if (!AdminData.giftList.isEmpty()) {
                viewPager.setVisibility(View.VISIBLE);
                giftNullLay.setVisibility(View.GONE);
                pagerIndicator.setVisibility(View.VISIBLE);
            } else {
                giftNullLay.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.INVISIBLE);
                pagerIndicator.setVisibility(View.GONE);
            }
        }

        ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(mActivity, count, Constants.TYPE_GIFTS);
        viewPager.setAdapter(pagerAdapter);
        pagerIndicator.setViewPager(viewPager);
        viewPager.setCurrentItem(0);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                loadItems();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public class ViewPagerAdapter extends PagerAdapter {
        private final Context context;
        private int count = 0;
        private String type = "";

        public ViewPagerAdapter(Context context, int count, String type) {
            this.context = context;
            this.type = type;
            this.count = count;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View itemView = LayoutInflater.from(context)
                    .inflate(R.layout.layout_stickers, container, false);
            recyclerView = itemView.findViewById(R.id.recyclerView);

            loadItems();

            container.addView(itemView, 0);
            return itemView;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup view, int position, @NonNull Object object) {
            view.removeView((View) object);
        }
    }

    private void loadItems() {
        /*Set Array list by 8*/
        int start = viewPager.getCurrentItem() * ITEM_LIMIT;
        int end = start + (ITEM_LIMIT - 1);

        if (end > AdminData.giftList.size()) {
            end = AdminData.giftList.size() - 1;
        } else if (AdminData.giftList.size() <= end) {
            end = AdminData.giftList.size() - 1;
        }
        loadGifts(start, end);
    }

    private void loadGifts(int start, int end) {

        tempGiftList = new ArrayList<>();

        /*tempGiftList used only for display, For send AdminData.giftList used*/

        for (int i = start; i <= end; i++) {
            tempGiftList.add(AdminData.giftList.get(i));
        }
        giftAdapter = new GiftAdapter(mActivity, tempGiftList, this, 70);
        Timber.d("loadGifts: %s", new Gson().toJson(tempGiftList));
        stickerLayoutManager = new GridLayoutManager(mActivity, 2, GridLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(stickerLayoutManager);
        ((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);
        recyclerView.setAdapter(giftAdapter);
        giftAdapter.notifyDataSetChanged();

    }

    public void sendGift() {
        if (NetworkReceiver.isConnected()) {
            if (SelectedVideoPosition >= 0) {
                HashMap<String, String> map = new HashMap<>();
                map.put(Constants.TAG_USER_ID, GetSet.getUserId());
                map.put(Constants.TAG_VIDEO_ID, String.valueOf(mHomeApiResponse.get(SelectedVideoPosition).getVideoId()));
                map.put(Constants.TAG_GIFT_ID, AdminData.giftList.get(giftPosition).getGiftId());
                map.put(Constants.TAG_GEMS_EARNINGS, "" + AdminData.giftList.get(giftPosition).getGiftGems());
                map.put(Constants.TAG_GEMS_COUNT, "" + (SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false").equals("true") ?
                        AdminData.giftList.get(giftPosition).getGiftGemsPrime() : AdminData.giftList.get(giftPosition).getGiftGems()));
                Timber.d("sendGift: %s", App.getGsonPrettyInstance().toJson(map));

                Call<Map<String, String>> call = apiInterface.sendGift(map);
                call.enqueue(new Callback<Map<String, String>>() {

                    @Override
                    public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
//                                Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(response.body()));
                                giftDialog.dismiss();
                                // update home following video
                                EventBus.getDefault().post(new FollowingUpdateGiftMessageCount(mHomeApiResponse.get(SelectedVideoPosition).getVideoId(), "gift", response.body().get("giftcount")));

                                Bundle payload = new Bundle();
                                payload.putString("gift_count", response.body().get("giftcount"));
                                mHomeApiResponse.get(SelectedVideoPosition).setVideoGiftCounts(response.body().get("giftcount"));
                                notifyItemChanged(SelectedVideoPosition, payload);


                            } else if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                                App.makeToast("Account is blocked");
                                GetSet.reset();
                                SharedPref.clearAll();
                                DBHelper.getInstance(mActivity).clearDB();
                                Intent logout = new Intent(mActivity, LoginActivity.class);
                                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                mActivity.startActivity(logout);
                            }
                        }
                    }

                    @Override

                    public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {

                    }
                });
            }

        } else {
            Toasty.error(mActivity, mActivity.getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    /*private void openCommentList(String publisherId, String videoId, int adapterPosition) {

        commentList.clear();
        tagged_user_id = "";
        tagged_user_name = "";
        list_tagged_user_ids.clear();
        list_tagged_user_names.clear();

        View sheetView = mActivity.getLayoutInflater().inflate(R.layout.bottom_sheet_video_commend_view, null);
        // commentListDialog = new BottomSheetDialog(mActivity, R.style.VideoCommentDialog); // Style here
        commentListDialog = new BottomSheetDialog(mActivity, R.style.VideoCommentNewDialog); // Style here
        commentListDialog.setContentView(sheetView);

        commentListDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        addComment = sheetView.findViewById(R.id.addComment);
        commentCount = sheetView.findViewById(R.id.commentCount);
        commentRec = sheetView.findViewById(R.id.commentRec);
        sendCommentLay = sheetView.findViewById(R.id.sendCommentLay);
//        rootView = sheetView.findViewById(R.id.rootView);
        headerBottom = sheetView.findViewById(R.id.headerBottom);
        mSwipeVideoComment = sheetView.findViewById(R.id.mSwipeVideoComment);
        ImageView btnClose = sheetView.findViewById(R.id.btnClose);
        noCommands = sheetView.findViewById(R.id.noCommands);
        noCommentLay = sheetView.findViewById(R.id.noCommentLay);


        mSwipeVideoComment.setColorSchemeColors(ContextCompat.getColor(mActivity.getApplicationContext(), R.color.colorPrimary));

        commentListDialog.show();


        commendAdapter = new CommentAdapter(mActivity.getApplicationContext(), commentList, publisherId, videoId, adapterPosition, new AdapterItemClickListener() {
            @Override
            public void onItemClicked(Object o, int position, String type) {
                if (type.equals(Constants.TAG_USER_NAME)) {
                    String userName = (String) o;
                    if (userName.equals(GetSet.getUserName())) {
                        Intent profile = new Intent(mActivity.getApplicationContext(), MyProfileActivity.class);
                        profile.putExtra(Constants.TAG_EDIT, true);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        mActivity.startActivity(profile);
                    } else {
                        Intent profile = new Intent(mActivity.getApplicationContext(), OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_NAME, userName);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        mActivity.startActivity(profile);
                    }
                    clickListener.onItemClicked(false, adapterPosition, Constants.TAG_PLAYBACK);
                } else {
                    clickListener.onItemClicked(false, adapterPosition, Constants.TAG_PLAYBACK);
                    if (type.equals(Constants.TAG_USERS)) {
                        String userId = (String) o;
                        if (userId.equals(GetSet.getUserId())) {
                            Intent profile = new Intent(mActivity, MyProfileActivity.class);
                            profile.putExtra(Constants.TAG_EDIT, true);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mActivity.startActivity(profile);
                        } else {
                            Intent profile = new Intent(mActivity, OthersProfileActivity.class);
                            profile.putExtra(Constants.TAG_USER_ID, userId);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mActivity.startActivity(profile);
                        }
                    }
                }
            }
        });
        linearLayoutManager = new LinearLayoutManager(mActivity.getApplicationContext());
        commentRec.setLayoutManager(linearLayoutManager);
        commentRec.setAdapter(commendAdapter);

        mSwipeVideoComment.setOnRefreshListener(() -> swipeRefresh(true, videoId, adapterPosition));


        //addComment.setVisibility(View.INVISIBLE/);
        videoScrollListener = new OnVerticalScrollListener() {
            @Override
            public void onScrolledToBottom() {
                super.onScrolledToBottom();
                if (commentList.size() <= lastOffsetVideo) return;
                else lastOffsetVideo = commentList.size();

                loadMoreCheckVideoCommand = "loadmore";
                Log.e("videoDialog", "-" + "cmmentapicall");
                getCommand(lastOffsetVideo, videoId, adapterPosition);

            }
        };
        commentRec.addOnScrollListener(videoScrollListener);


        sendCommentLay.setVisibility(View.INVISIBLE);
        swipeRefresh(true, videoId, adapterPosition);

        btnClose.setOnClickListener(v -> {
            if (mCommentsEditText != null)
                addComment.setText(mCommentsEditText.getText().toString());
            loadMoreCheckVideoCommand = "initial";
            commentList.clear();
            clickListener.onItemClicked(true, adapterPosition, Constants.TAG_PLAYBACK);
            commentListDialog.setDismissWithAnimation(true);
            if (commentListDialog.isShowing()) commentListDialog.dismiss();
            if (commentEditDialog != null && commentEditDialog.isShowing())
                commentEditDialog.dismiss();
        });

        sendCommentLay.setOnClickListener(v -> {
            topBottomSheet(videoId, adapterPosition, addComment.getText().toString());
        });

        commentListDialog.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    Timber.i("keyCode check %s" , VideoCommentSentDialog.isShowing());
                if (commentEditDialog != null && commentEditDialog.isShowing()) {
                    commentEditDialog.dismiss();
                    return true;
                } else {
                    return false;
                }
            }
            Timber.i("keyCode check %s", "return");
            return false;
        });

        commentListDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                clickListener.onItemClicked(true, adapterPosition, Constants.TAG_PLAYBACK);
            }
        });

        commentListDialog.setOnDismissListener(dialogInterface -> {
            EventBus.getDefault().post(new VideoZoomEvent(0f, 0));
            clickListener.onItemClicked(true, adapterPosition, Constants.TAG_PLAYBACK);      });
    }
*/

    /*@SuppressLint("InflateParams")
    private void openCommentList(String publisherId, String videoId, int adapterPosition) {
        // reset state
        commentList.clear();
        tagged_user_id = "";
        tagged_user_name = "";
        list_tagged_user_ids.clear();
        list_tagged_user_names.clear();

        // inflate sheet layout
        View sheetView = mActivity.getLayoutInflater().inflate(R.layout.bottom_sheet_video_commend_view, null);

        // create dialog
        final BottomSheetDialog commentListDialog = new BottomSheetDialog(mActivity, R.style.VideoCommentNewDialog);
        commentListDialog.setContentView(sheetView);

        // ------------- IMPORTANT: force dialog to be half-screen -------------
        // compute desired half-height (50% of screen)
        int screenHeight = mActivity.getResources().getDisplayMetrics().heightPixels;
        final int halfHeightPx = (int) (screenHeight * 0.50f); // change 0.50f to 0.55f for 55% etc.

        // Get the bottom sheet view (the parent of sheetView)
        final View bottomSheet = commentListDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            // set explicit height so the sheet doesn't expand to full screen
            ViewGroup.LayoutParams lp = bottomSheet.getLayoutParams();
            lp.height = halfHeightPx;
            bottomSheet.setLayoutParams(lp);
        }

        // Grab behavior
        final BottomSheetBehavior<FrameLayout> behavior = commentListDialog.getBehavior();
        // make it draggable and skip collapsed not used here so user can expand if needed
        behavior.setDraggable(true);

        // Set the peek height to half screen and start collapsed (so initial shown = half)
        behavior.setPeekHeight(halfHeightPx);
        behavior.setSkipCollapsed(false);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // ---------------- Bind views ----------------
        addComment = sheetView.findViewById(R.id.addComment);
        commentCount = sheetView.findViewById(R.id.commentCount);
        commentRec = sheetView.findViewById(R.id.commentRec);
        sendCommentLay = sheetView.findViewById(R.id.sendCommentLay);
        headerBottom = sheetView.findViewById(R.id.headerBottom);
        mSwipeVideoComment = sheetView.findViewById(R.id.mSwipeVideoComment);
        ImageView btnClose = sheetView.findViewById(R.id.btnClose);
        noCommands = sheetView.findViewById(R.id.noCommands);
        noCommentLay = sheetView.findViewById(R.id.noCommentLay);

        mSwipeVideoComment.setColorSchemeColors(ContextCompat.getColor(mActivity.getApplicationContext(), R.color.colorPrimary));
        commentCount.setText(mActivity.getString(R.string.comments)); // or set real count later

        // Setup Recycler
        linearLayoutManager = new LinearLayoutManager(mActivity.getApplicationContext());
        commendAdapter = new CommentAdapter(mActivity.getApplicationContext(), commentList, publisherId, videoId, adapterPosition, new AdapterItemClickListener() {
            @Override
            public void onItemClicked(Object o, int position, String type) {
                // reuse your logic (navigate to profile etc.)
                if (type.equals(Constants.TAG_USER_NAME)) {
                    String userName = (String) o;
                    if (userName.equals(GetSet.getUserName())) {
                        Intent profile = new Intent(mActivity.getApplicationContext(), MyProfileActivity.class);
                        profile.putExtra(Constants.TAG_EDIT, true);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        mActivity.startActivity(profile);
                    } else {
                        Intent profile = new Intent(mActivity.getApplicationContext(), OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_NAME, userName);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        mActivity.startActivity(profile);
                    }
                    clickListener.onItemClicked(false, adapterPosition, Constants.TAG_PLAYBACK);
                } else {
                    clickListener.onItemClicked(false, adapterPosition, Constants.TAG_PLAYBACK);
                    if (type.equals(Constants.TAG_USERS)) {
                        String userId = (String) o;
                        if (userId.equals(GetSet.getUserId())) {
                            Intent profile = new Intent(mActivity, MyProfileActivity.class);
                            profile.putExtra(Constants.TAG_EDIT, true);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mActivity.startActivity(profile);
                        } else {
                            Intent profile = new Intent(mActivity, OthersProfileActivity.class);
                            profile.putExtra(Constants.TAG_USER_ID, userId);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mActivity.startActivity(profile);
                        }
                    }
                }
            }
        });
        commentRec.setLayoutManager(linearLayoutManager);
        commentRec.setAdapter(commendAdapter);

        // scroll listener for pagination
        videoScrollListener = new OnVerticalScrollListener() {
            @Override
            public void onScrolledToBottom() {
                super.onScrolledToBottom();
                if (commentList.size() <= lastOffsetVideo) return;
                else lastOffsetVideo = commentList.size();

                loadMoreCheckVideoCommand = "loadmore";
                Timber.d("Loading more comments");
                getCommand(lastOffsetVideo, videoId, adapterPosition);
            }
        };
        commentRec.addOnScrollListener(videoScrollListener);

        // send comment
        sendCommentLay.setVisibility(View.INVISIBLE);
        sendCommentLay.setOnClickListener(v -> {
            String text = addComment.getText().toString().trim();
            if (!text.isEmpty()) topBottomSheet(videoId, adapterPosition, text);
        });

        // close button
        btnClose.setOnClickListener(v -> {
            if (mCommentsEditText != null)
                addComment.setText(mCommentsEditText.getText().toString());
            loadMoreCheckVideoCommand = "initial";
            commentList.clear();
            clickListener.onItemClicked(true, adapterPosition, Constants.TAG_PLAYBACK);
            commentListDialog.setDismissWithAnimation(true);
            if (commentListDialog.isShowing()) commentListDialog.dismiss();
            if (commentEditDialog != null && commentEditDialog.isShowing())
                commentEditDialog.dismiss();
        });

        // handle back key when dialog is open (keep existing behavior)
        commentListDialog.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (commentEditDialog != null && commentEditDialog.isShowing()) {
                    commentEditDialog.dismiss();
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        });

        // ----------------- Sync video animation with sheet -----------------
        // Live update while dragging: slideOffset ranges 0.0 (collapsed) -> 1.0 (expanded)
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheetView, int newState) {
                // When user fully expands/collapses we may want to ensure final state
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    // collapsed = half-screen initial
                    EventBus.getDefault().post(new VideoZoomEvent(1f));
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_DRAGGING) {
                    // nothing
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    // fully expanded (if user drags up) -> you may want to shrink video more
                    EventBus.getDefault().post(new VideoZoomEvent(1f));
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheetView, float slideOffset) {
                // slideOffset: 0 -> collapsed (half), 1 -> fully expanded (if user pulls)
                // We want mapping 0 -> 1f (video shrunk), and if user drags further maybe keep same.
                // To allow a reversed mapping (so at collapsed video is shrunk),
                // we transform the offset: when sheet is shown collapsed -> slideOffset is 0.0,
                // but we still want video shrunk. So we will map offsetRange [-1..1] to zoomValue.
                // Simpler: treat slideOffset 0..1 as progressive and post (1 - (1 - minZoom) * slideOffset)
                // BUT to ensure at collapsed (0) the video is shrunk, just post 1f on show,
                // and here we post a value between 1f and 0f only if needed.
                float normalized = slideOffset; // 0..1
                // We want videoScaleFactor t such that t=1 (shrunk) when collapsed (normalized=0),
                // and t=0 (full-screen) when sheet dismissed. But onSlide occurs only while visible.
                // We'll invert normalized: t = 1 - normalized; then clamp.
                float t = 1f - Math.max(0f, Math.min(1f, normalized));
                // However this will produce t near 1 when collapsed and approaching 0 when fully expanded (if sheet covers full screen)
                // To ensure we don't send exactly 0 on expand, clamp min to 0f
                EventBus.getDefault().post(new VideoZoomEvent(t));
            }
        });

        // When the sheet is shown, shrink video (we want animation immediately)
        commentListDialog.setOnShowListener(dialog -> {
            int screenHe = mActivity.getResources().getDisplayMetrics().heightPixels;
            int availableVideoHeight = screenHe - halfHeightPx;
            // send zoom event with scale=1f (shrunk) and available height in px
            EventBus.getDefault().post(new VideoZoomEvent(1f, availableVideoHeight));
        });

        // When dismissed -> restore video and clean up
        commentListDialog.setOnDismissListener(dialogInterface -> {
            EventBus.getDefault().post(new VideoZoomEvent(0f, 0));
            clickListener.onItemClicked(true, adapterPosition, Constants.TAG_PLAYBACK);
            // remove callback to avoid leaks
            try {
                behavior.removeBottomSheetCallback(null); // no-op; if you kept a reference store it and remove
            } catch (Exception ignored) {}
        });

        // finally show
        commentListDialog.show();

        // initial load
        swipeRefresh(true, videoId, adapterPosition);
    }
*/

    //working on center
 /*   @SuppressLint("InflateParams")
    private void openCommentList(String publisherId, String videoId, int adapterPosition) {
        // reset state
        commentList.clear();
        tagged_user_id = "";
        tagged_user_name = "";
        list_tagged_user_ids.clear();
        list_tagged_user_names.clear();

        // inflate sheet layout
        View sheetView = mActivity.getLayoutInflater().inflate(R.layout.bottom_sheet_video_commend_view, null);

        // create dialog
        final BottomSheetDialog commentListDialog = new BottomSheetDialog(mActivity, R.style.VideoCommentNewDialog);
        commentListDialog.setContentView(sheetView);

        // ------------- IMPORTANT: force dialog to be half-screen -------------
        final int screenHeight = mActivity.getResources().getDisplayMetrics().heightPixels;
        final int halfHeightPx = (int) (screenHeight * 0.50f); // adjust 0.50f -> 0.55f if you want larger sheet

        // Get the bottom sheet view (the parent of sheetView)
        final View bottomSheet = commentListDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            // set explicit height so the sheet doesn't expand to full screen initially
            ViewGroup.LayoutParams lp = bottomSheet.getLayoutParams();
            lp.height = halfHeightPx;
            bottomSheet.setLayoutParams(lp);
        }

        // Grab behavior
        final BottomSheetBehavior<FrameLayout> behavior = commentListDialog.getBehavior();
        behavior.setDraggable(true);
        behavior.setPeekHeight(halfHeightPx);
        behavior.setSkipCollapsed(false);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // ---------------- Bind views ----------------
        addComment = sheetView.findViewById(R.id.addComment);
        commentCount = sheetView.findViewById(R.id.commentCount);
        commentRec = sheetView.findViewById(R.id.commentRec);
        sendCommentLay = sheetView.findViewById(R.id.sendCommentLay);
        headerBottom = sheetView.findViewById(R.id.headerBottom);
        mSwipeVideoComment = sheetView.findViewById(R.id.mSwipeVideoComment);
        ImageView btnClose = sheetView.findViewById(R.id.btnClose);
        noCommands = sheetView.findViewById(R.id.noCommands);
        noCommentLay = sheetView.findViewById(R.id.noCommentLay);

        mSwipeVideoComment.setColorSchemeColors(ContextCompat.getColor(mActivity.getApplicationContext(), R.color.colorPrimary));
        commentCount.setText(mActivity.getString(R.string.comments)); // or set real count later

        // Setup Recycler
        linearLayoutManager = new LinearLayoutManager(mActivity.getApplicationContext());
        commendAdapter = new CommentAdapter(mActivity.getApplicationContext(), commentList, publisherId, videoId, adapterPosition, new AdapterItemClickListener() {
            @Override
            public void onItemClicked(Object o, int position, String type) {
                // reuse your logic (navigate to profile etc.)
                if (type.equals(Constants.TAG_USER_NAME)) {
                    String userName = (String) o;
                    if (userName.equals(GetSet.getUserName())) {
                        Intent profile = new Intent(mActivity.getApplicationContext(), MyProfileActivity.class);
                        profile.putExtra(Constants.TAG_EDIT, true);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        mActivity.startActivity(profile);
                    } else {
                        Intent profile = new Intent(mActivity.getApplicationContext(), OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_NAME, userName);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        mActivity.startActivity(profile);
                    }
                    clickListener.onItemClicked(false, adapterPosition, Constants.TAG_PLAYBACK);
                } else {
                    clickListener.onItemClicked(false, adapterPosition, Constants.TAG_PLAYBACK);
                    if (type.equals(Constants.TAG_USERS)) {
                        String userId = (String) o;
                        if (userId.equals(GetSet.getUserId())) {
                            Intent profile = new Intent(mActivity, MyProfileActivity.class);
                            profile.putExtra(Constants.TAG_EDIT, true);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mActivity.startActivity(profile);
                        } else {
                            Intent profile = new Intent(mActivity, OthersProfileActivity.class);
                            profile.putExtra(Constants.TAG_USER_ID, userId);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mActivity.startActivity(profile);
                        }
                    }
                }
            }
        });
        commentRec.setLayoutManager(linearLayoutManager);
        commentRec.setAdapter(commendAdapter);

        // scroll listener for pagination
        videoScrollListener = new OnVerticalScrollListener() {
            @Override
            public void onScrolledToBottom() {
                super.onScrolledToBottom();
                if (commentList.size() <= lastOffsetVideo) return;
                else lastOffsetVideo = commentList.size();

                loadMoreCheckVideoCommand = "loadmore";
                Timber.d("Loading more comments");
                getCommand(lastOffsetVideo, videoId, adapterPosition);
            }
        };
        commentRec.addOnScrollListener(videoScrollListener);

        // send comment
        sendCommentLay.setVisibility(View.INVISIBLE);
        sendCommentLay.setOnClickListener(v -> {
            String text = addComment.getText().toString().trim();
            if (!text.isEmpty()) topBottomSheet(videoId, adapterPosition, text);
        });

        // close button
        btnClose.setOnClickListener(v -> {
            if (mCommentsEditText != null)
                addComment.setText(mCommentsEditText.getText().toString());
            loadMoreCheckVideoCommand = "initial";
            commentList.clear();
            clickListener.onItemClicked(true, adapterPosition, Constants.TAG_PLAYBACK);
            commentListDialog.setDismissWithAnimation(true);
            if (commentListDialog.isShowing()) commentListDialog.dismiss();
            if (commentEditDialog != null && commentEditDialog.isShowing())
                commentEditDialog.dismiss();
        });

        // handle back key when dialog is open (keep existing behavior)
        commentListDialog.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (commentEditDialog != null && commentEditDialog.isShowing()) {
                    commentEditDialog.dismiss();
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        });

        // ----------------- Sync video animation with sheet -----------------
        // Live update while dragging: compute available height above the sheet using bsv.getTop()
        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheetView, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_EXPANDED) {
                    // When collapsed/expanded send a final event: t=1 (shrunk) and accurate avail height
                    int avail = (bottomSheetView.getTop() > 0) ? bottomSheetView.getTop() : Math.max(0, screenHeight - halfHeightPx);
                    EventBus.getDefault().post(new VideoZoomEvent(1f, avail));
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheetView, float slideOffset) {
                // slideOffset: 0 -> collapsed(peek) , 1 -> fully expanded
                float normalized = Math.max(0f, Math.min(1f, slideOffset));
                // t: 1 (collapsed/peek) -> 0 (fully expanded)
                float t = 1f - normalized;
                // available height above sheet is the sheet's top Y coordinate
                int availableVideoHeight = Math.max(0, bottomSheetView.getTop());
                EventBus.getDefault().post(new VideoZoomEvent(t, availableVideoHeight));
            }
        });

        // When the sheet is shown, post initial available height (screen - peek)
        commentListDialog.setOnShowListener(dialog -> {
            int availableVideoHeight = Math.max(0, screenHeight - halfHeightPx);
            EventBus.getDefault().post(new VideoZoomEvent(1f, availableVideoHeight));
        });

        // When dismissed -> restore video and clean up
        commentListDialog.setOnDismissListener(dialogInterface -> {
            EventBus.getDefault().post(new VideoZoomEvent(0f, 0));
            clickListener.onItemClicked(true, adapterPosition, Constants.TAG_PLAYBACK);
            // No direct removeBottomSheetCallback() here because we used an anonymous; GC will clear when dialog dismissed
        });

        // finally show
        commentListDialog.show();

        // initial load
        swipeRefresh(true, videoId, adapterPosition);
    }
*/

    @SuppressLint("InflateParams")
    private void openCommentList(String publisherId, String videoId, int adapterPosition, LinearLayout leftSideView) {
        // reset state
        commentList.clear();
        tagged_user_id = "";
        tagged_user_name = "";
        list_tagged_user_ids.clear();
        list_tagged_user_names.clear();

        // inflate sheet layout
        View sheetView = mActivity.getLayoutInflater().inflate(R.layout.bottom_sheet_video_commend_view, null);

        // create dialog
        final BottomSheetDialog commentListDialog = new BottomSheetDialog(mActivity, R.style.VideoCommentNewDialog1);
        commentListDialog.setContentView(sheetView);

        // ------------- IMPORTANT: force dialog to be 50/50 split like screenshot - no gaps, video fills top half completely -------------
        final int screenHeight = mActivity.getResources().getDisplayMetrics().heightPixels;
        final int halfHeightPx = (int) (screenHeight * 0.50f); // 50% for comments, 50% for video - no gaps, video fills top half completely
        final int fullScreenHeight = screenHeight;

        // Get the bottom sheet view (the parent of sheetView)
        final View bottomSheet = commentListDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            // set explicit height so the sheet doesn't expand to full screen initially
            ViewGroup.LayoutParams lp = bottomSheet.getLayoutParams();
            lp.height = halfHeightPx;
            bottomSheet.setLayoutParams(lp);

            // Remove any padding/margin gaps between video and dialog
            bottomSheet.setPadding(0, 0, 0, 0);
            if (bottomSheet instanceof ViewGroup) {
                ((ViewGroup) bottomSheet).setClipToPadding(false);
                ((ViewGroup) bottomSheet).setClipChildren(false);
            }

            // Ensure dialog fills to bottom with no gaps
            if (bottomSheet.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) bottomSheet.getLayoutParams();
                marginParams.setMargins(0, 0, 0, 0);
                marginParams.bottomMargin = 0;
                bottomSheet.setLayoutParams(marginParams);
            }

            // Remove gaps from parent container (CoordinatorLayout)
            ViewParent parent = bottomSheet.getParent();
            if (parent instanceof View) {
                View parentView = (View) parent;
                parentView.setPadding(0, 0, 0, 0);
                if (parentView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) parentView.getLayoutParams();
                    params.setMargins(0, 0, 0, 0);
                    params.bottomMargin = 0;
                    parentView.setLayoutParams(params);
                }
            }
        }

        // Also remove padding from the sheet view itself
        sheetView.setPadding(0, 0, 0, 0);

        // Aggressively remove all bottom gaps
        if (sheetView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) sheetView.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            params.bottomMargin = 0;
            sheetView.setLayoutParams(params);
        }

        // Remove gaps from the root container
        View rootContainer = sheetView.findViewById(R.id.headerBottom);
        if (rootContainer != null) {
            rootContainer.setPadding(0, 0, 0, 0);
            if (rootContainer.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rootContainer.getLayoutParams();
                params.setMargins(0, 0, 0, 0);
                params.bottomMargin = 0;
                rootContainer.setLayoutParams(params);
            }
        }


        // Remove gaps from sendCommentLay at the bottom
        View sendCommentLayView = sheetView.findViewById(R.id.sendCommentLay);
        if (sendCommentLayView != null) {
            sendCommentLayView.setPadding(sendCommentLayView.getPaddingLeft(), sendCommentLayView.getPaddingTop(),
                    sendCommentLayView.getPaddingRight(), 0); // Remove bottom padding
            if (sendCommentLayView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) sendCommentLayView.getLayoutParams();
                params.bottomMargin = 0;
                sendCommentLayView.setLayoutParams(params);
            }
        }

        // Grab behavior
        final BottomSheetBehavior<FrameLayout> behavior = commentListDialog.getBehavior();
        behavior.setPeekHeight(halfHeightPx);
        behavior.setSkipCollapsed(false); // Allow collapsed state
        behavior.setDraggable(false); // Allow dragging like Instagram
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // Start collapsed at 50% to show video
        behavior.setFitToContents(false); // Allow flexible sizing
        behavior.setHalfExpandedRatio(0.7f); // Set half-expanded to 50% like screenshot
        // ---------------- Bind views ----------------
        addComment = sheetView.findViewById(R.id.addComment);
        commentCount = sheetView.findViewById(R.id.commentCount);
        commentRec = sheetView.findViewById(R.id.commentRec);
        sendCommentLay = sheetView.findViewById(R.id.sendCommentLay);
        headerBottom = sheetView.findViewById(R.id.headerBottom);
        mSwipeVideoComment = sheetView.findViewById(R.id.mSwipeVideoComment);
        ImageView btnClose = sheetView.findViewById(R.id.btnClose);
        noCommands = sheetView.findViewById(R.id.noCommands);
        noCommentLay = sheetView.findViewById(R.id.noCommentLay);
        viewEmptyPlace = sheetView.findViewById(R.id.viewEmptyPlace);

        mSwipeVideoComment.setColorSchemeColors(ContextCompat.getColor(mActivity.getApplicationContext(), R.color.colorPrimary));
        commentCount.setText(mActivity.getString(R.string.comments)); // or set real count later

        // Setup Recycler
        linearLayoutManager = new LinearLayoutManager(mActivity.getApplicationContext());
        commendAdapter = new CommentAdapter(mActivity.getApplicationContext(), commentList, publisherId, videoId, adapterPosition, new AdapterItemClickListener() {
            @Override
            public void onItemClicked(Object o, int position, String type) {
                // reuse your logic (navigate to profile etc.)
                if (type.equals(Constants.TAG_USER_NAME)) {
                    String userName = (String) o;
                    if (userName.equals(GetSet.getUserName())) {
                        Intent profile = new Intent(mActivity.getApplicationContext(), MyProfileActivity.class);
                        profile.putExtra(Constants.TAG_EDIT, true);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        mActivity.startActivity(profile);
                    } else {
                        Intent profile = new Intent(mActivity.getApplicationContext(), OthersProfileActivity.class);
                        profile.putExtra(Constants.TAG_USER_NAME, userName);
                        profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        mActivity.startActivity(profile);
                    }
                    clickListener.onItemClicked(false, adapterPosition, Constants.TAG_PLAYBACK);
                } else {
                    clickListener.onItemClicked(false, adapterPosition, Constants.TAG_PLAYBACK);
                    if (type.equals(Constants.TAG_USERS)) {
                        String userId = (String) o;
                        if (userId.equals(GetSet.getUserId())) {
                            Intent profile = new Intent(mActivity, MyProfileActivity.class);
                            profile.putExtra(Constants.TAG_EDIT, true);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mActivity.startActivity(profile);
                        } else {
                            Intent profile = new Intent(mActivity, OthersProfileActivity.class);
                            profile.putExtra(Constants.TAG_USER_ID, userId);
                            profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mActivity.startActivity(profile);
                        }
                    }
                }
            }
        });
        // commentRec.setLayoutManager(linearLayoutManager);
        // commentRec.setAdapter(commendAdapter);

// set the adapter once
        if (commentRec.getAdapter() == null) {
            commentRec.setLayoutManager(linearLayoutManager);
            commentRec.setAdapter(commendAdapter);
            commentRec.setNestedScrollingEnabled(false);
            commentRec.setHasFixedSize(true);
            RecyclerView.ItemAnimator animator = commentRec.getItemAnimator();
            if (animator != null) {
                animator.setChangeDuration(0);
                animator.setAddDuration(0);
                animator.setRemoveDuration(0);
            }
        } else {
            // adapter already set: do not re-set it
        }

        // scroll listener for pagination
        videoScrollListener = new OnVerticalScrollListener() {
            @Override
            public void onScrolledToBottom() {
                super.onScrolledToBottom();
                Log.e(TAG, "VIDEO_DBG : onScrolledToBottom1 ");
                if (commentList.size() <= lastOffsetVideo) return;
                else lastOffsetVideo = commentList.size();

                loadMoreCheckVideoCommand = "loadmore";
                Timber.d("Loading more comments");
                getCommand(lastOffsetVideo, videoId, adapterPosition);
            }
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.e(TAG, "VIDEO_DBG : onScrollStateChanged1 ");
            }
        };
        commentRec.addOnScrollListener(videoScrollListener);

        sendCommentLay.setVisibility(View.INVISIBLE);
        swipeRefresh(true, videoId, adapterPosition);

        // send comment
        sendCommentLay.setOnClickListener(v -> {
            topBottomSheet(videoId, adapterPosition, addComment.getText().toString());
        });

        // close button
        btnClose.setOnClickListener(v -> {
            if (mCommentsEditText != null)
                addComment.setText(mCommentsEditText.getText().toString());
            loadMoreCheckVideoCommand = "initial";
            commentList.clear();
            clickListener.onItemClicked(true, adapterPosition, Constants.TAG_PLAYBACK);
            commentListDialog.setDismissWithAnimation(true);
            if (commentListDialog.isShowing()) commentListDialog.dismiss();
            if (commentEditDialog != null && commentEditDialog.isShowing())
                commentEditDialog.dismiss();
        });

        // handle back key when dialog is open (keep existing behavior)
        commentListDialog.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (commentEditDialog != null && commentEditDialog.isShowing()) {
                    commentEditDialog.dismiss();
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        });

        behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheetView, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    int avail = Math.max(0, bottomSheetView.getTop());
                    // when fully expanded, tell player to shrink (t == 1)
                    EventBus.getDefault().post(new VideoZoomEvent(1f, avail));
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    // When collapsed (sheet still visible as half-sheet) post collapsed state if needed
                    int avail = Math.max(0, bottomSheetView.getTop());
                    EventBus.getDefault().post(new VideoZoomEvent(1f, avail));
                }
                // DO NOT handle STATE_DRAGGING, STATE_HIDDEN, or onSlide -> prevents drag races
            }

            @Override
            public void onSlide(@NonNull View bottomSheetView, float slideOffset) {
                // Intentionally empty: we disabled dragging, so no continuous slide events
            }
        });
        commentListDialog.setOnShowListener(dialog -> {
          /*  int availableVideoHeight = Math.max(0, screenHeight - halfHeightPx);
            lastAvailableHeight = availableVideoHeight; // store initial value
            EventBus.getDefault().post(new VideoZoomEvent(1f, availableVideoHeight));
*/
            Window window = commentListDialog.getWindow();
            if (window != null) {
                // defensive: remove the dim flag and set dim amount to 0
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.dimAmount = 0f;
                window.setAttributes(lp);
            }

            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(Color.TRANSPARENT);
                // also remove background drawable if any
                bottomSheet.setBackground(null);
                // If you want rounded corners from your layout, ensure the root view inside bottom sheet
                // provides that shape; otherwise bottomSheet background must be transparent.
            }

            // 3) Also ensure the content root has no opaque background
            View content = commentListDialog.findViewById(android.R.id.content);
            if (content != null) {
                content.setBackgroundColor(Color.TRANSPARENT);
            }

            // 4) Post the initial zoom event (as you already do)
            int availableVideoHeight = Math.max(0, screenHeight - halfHeightPx);
            lastAvailableHeight = availableVideoHeight; // store initial value
            EventBus.getDefault().post(new VideoZoomEvent(1f, availableVideoHeight));
        });

        // When dismissed -> restore video and clean up
        commentListDialog.setOnDismissListener(dialogInterface -> {
            //  EventBus.getDefault().post(new VideoZoomEvent(0f, 0));
            // EventBus.getDefault().post(new CommentDialogStateEvent(false));

            clickListener.onItemClicked(false, adapterPosition, "COMMENT_CLOSE");
            leftSideView.setVisibility(VISIBLE);
            EventBus.getDefault().post(new VideoZoomEvent(0f, fullScreenHeight));
            clickListener.onItemClicked(true, adapterPosition, Constants.TAG_PLAYBACK);
            // No direct removeBottomSheetCallback() here because we used an anonymous; GC will clear when dialog dismissed
        });

        viewEmptyPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickListener.onItemClicked(true, adapterPosition, Constants.TAG_PLAYBACK);
                commentListDialog.setDismissWithAnimation(true);
                if (commentListDialog.isShowing()) commentListDialog.dismiss();
                if (commentEditDialog != null && commentEditDialog.isShowing())
                    commentEditDialog.dismiss();

                leftSideView.setVisibility(VISIBLE);
                EventBus.getDefault().post(new VideoZoomEvent(0f, fullScreenHeight));
                clickListener.onItemClicked(true, adapterPosition, Constants.TAG_PLAYBACK);
            }
        });


        // initial load
       // swipeRefresh(true, videoId, adapterPosition);

        //EventBus.getDefault().post(new CommentDialogStateEvent(true));
        clickListener.onItemClicked(true, adapterPosition, "COMMENT_OPEN");
        // finally show
        commentListDialog.show();
        leftSideView.setVisibility(View.GONE);
    }

    private void shrinkVideo(FrameLayout mediaContainer, float ratio) {
        int screenHeight = mediaContainer.getContext().getResources().getDisplayMetrics().heightPixels;
        int targetHeight = (int) (screenHeight * ratio);
        animateVideoResize(mediaContainer, targetHeight);
    }

    private void expandVideo(FrameLayout mediaContainer) {
        int screenHeight = mediaContainer.getContext().getResources().getDisplayMetrics().heightPixels;
        animateVideoResize(mediaContainer, screenHeight);
    }

    private void animateVideoResize(FrameLayout mediaContainer, int targetHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(mediaContainer.getHeight(), targetHeight);
        animator.setDuration(250);
        animator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = mediaContainer.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            mediaContainer.setLayoutParams(params);
        });
        animator.start();
    }



    private void topBottomSheet(String videoId, int adapterPosition, String setText) {

        View commentSentDialog = mActivity.getLayoutInflater().inflate(R.layout.bottom_sheet_comment_sent, null);
        commentEditDialog = new BottomSheetDialog(mActivity, R.style.SecondaryBottomSheet);

        commentEditDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        commentEditDialog.setContentView(commentSentDialog);
        commentEditDialog.getBehavior().setHideable(true);
        commentEditDialog.setCanceledOnTouchOutside(true);

        mCommentsEditText = commentSentDialog.findViewById(R.id.socialAutoCompleteTextView);
        sendComment = commentSentDialog.findViewById(R.id.sendComment);

        mCommentsEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                commentEditDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            } else {
                commentEditDialog.dismiss();
            }
        });
        lastOffsetVideo = 0;
        mCommentsEditText.requestFocus();
        MentionArrayAdapter<UserMention> adapter = new MentionArrayAdapter<>(mActivity);
//        mCommentsEditText.setMentionAdapter(adapter);
        ListView mentionsList = commentSentDialog.findViewById(R.id.userListView);
        mentionsList.setAdapter(adapter);
        Toast[] singleTonToast = new Toast[1];

        Handler mainHandler = new Handler(Looper.getMainLooper());
        final long[] lastEdited = {0};
        Runnable inputFinishChecker = new Runnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() > lastEdited[0] + 500 - 200) {
                    if (!TextUtils.isEmpty(mCommentsEditText.getText()) && searchUser) {
                        String s = mCommentsEditText.getText().toString();
                        if ((s.length() - s.replaceAll("@", "").length()) > 5) {
                            if (singleTonToast.length > 0 && singleTonToast[0] != null)
                                singleTonToast[0].cancel();
                            singleTonToast[0] = Toasty.info(mActivity.getApplicationContext(),
                                    "Only 5 tags allowed");
                            singleTonToast[0].show();
                            return;
                        }
                        final String q = getMentionsSuggestionQuery(s, mCommentsEditText.getSelectionEnd());
                        if (q.length() > 0) {
                            // TODO: fetch users for q
                            getLiveUsersForQuery(q, result -> {
//                                mentionsCache.clear();
                                adapter.clear();
                                adapter.notifyDataSetChanged();
                                mentionsCache.addAll(
                                        Stream.of(result)
                                                .map(result1 -> {
                                                            // for some difficulties we have to send the Mention
                                                            adapter.add(new UserMention(result1.getUserId(), result1.getUsername(),
                                                                    result1.getDisplayname(), result1.getAvatar()));
                                                            return new Mention2(result1.getUserId(), result1.getUsername(),
                                                                    result1.getName(), result1.getAvatar());
                                                        }
                                                )
                                                .collect(Collectors.toList())
                                );
                                Timber.d("getLiveUsersForQuery: %s", new Gson().toJson(mentionsCache));
                                if (adapter.getCount() > 0) {
                                    adapter.getFilter().filter(q);
                                    adapter.notifyDataSetChanged();
                                    mentionsList.setVisibility(View.VISIBLE);
                                } else {
                                    mentionsList.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            adapter.clear();
                            adapter.notifyDataSetChanged();
                            mentionsList.setVisibility(View.GONE);
                        }
                    } else {
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                        mentionsList.setVisibility(View.GONE);
                    }
                }
            }
        };

            /*mCommentsEditText.setThreshold(1);
            mCommentsEditText.setDropDownAnchor(R.id.rootView);
            mCommentsEditText.setDropDownHeight(0);  // don't show popup*/

        mCommentsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mainHandler.removeCallbacks(inputFinishChecker);
                if (s.toString().length() >= 250) {
                    Toasty.info(mActivity.getApplicationContext(), R.string.max_reached, Toasty.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                lastEdited[0] = System.currentTimeMillis();
                mainHandler.postDelayed(inputFinishChecker, 500);
            }
        });

        mentionsList.setOnItemClickListener((parent, view, position, id) -> {
            final String selected = adapter.getItem(position).toString();
//            Timber.d("setOnItemClickListener: selected %s, %s", position, selected);
            if (mCommentsEditText.getMentions().contains(selected)) {
                Toasty.info(mActivity.getApplicationContext(),
                        mActivity.getString(R.string.already_tagged)).show();
                return;
            }
            getReplacedText(mCommentsEditText, selected, mCommentsEditText.getSelectionEnd());
            mentionsList.setVisibility(View.GONE);
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    searchUser = true;
                }
            }, 1000);
        });

//            userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//            commentUserAdapter = new CommentUserAdapter(liveUser);
//            userRecyclerView.setAdapter(commentUserAdapter);
//            userRecyclerView.setHasFixedSize(true);

        mCommentsEditText.setText(setText);
        InputMethodManager imm = (InputMethodManager) mActivity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mCommentsEditText, InputMethodManager.SHOW_IMPLICIT);
        lastOffsetVideo = 0;
        if (mActivity != null && !mActivity.isFinishing()) {
            commentEditDialog.show();
        }
        mCommentsEditText.requestFocus();

        sendComment.setOnClickListener(v -> {

            if (Constants.Login_method.equals("skip")) {
                GetSet.reset();
                SharedPref.clearAll();
                DBHelper.getInstance(mActivity).clearDB();
                Intent logout = new Intent(mActivity, LoginActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(logout);
            } else {
                if (!Objects.requireNonNull(mCommentsEditText.getText()).toString().isEmpty()) {
                    postComments(mCommentsEditText.getText().toString(), videoId, adapterPosition);
                    mCommentsEditText.setText("");
                    addComment.setText("");
                }
            }
        });

        commentEditDialog.setOnCancelListener(dialog -> {
            if (mCommentsEditText != null)
                addComment.setText(mCommentsEditText.getText().toString());
            loadMoreCheckVideoCommand = "initial";
            commentList.clear();
            App.hideKeyboardActivity(mActivity);
            /*videoCommandBottomDialog.setDismissWithAnimation(true);
            videoCommandBottomDialog.dismiss();*/
            commentEditDialog.dismiss();
        });
    }

    @NonNull
    private String getMentionsSuggestionQuery(@NonNull String s, int position) {
        final int end = position;
        int start = -1;
        position--;
        while (position >= 0) {
            char c = s.charAt(position);
            if (CharMatcher.whitespace().matches(c)) {
                return "";  // User just typed space, we don't show suggestions
            } else if (c == '@') {
                if (start != -1) return s.substring(start, end);
                else return "";
            } else {
                start = position;
                position--;
            }
        }
        return "";
    }

    public void getLiveUsersForQuery(
            @NonNull String q,
            @NonNull NetworkResultCallback<List<UserList.Result>> callback
    ) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<>();
            map.put("user_id", GetSet.getUserId());
            map.put("token", GetSet.getAuthToken());
            map.put("search_key", q);

            Call<UserList> call = apiInterface.getUsers(map);
            call.enqueue(new Callback<UserList>() {
                @Override
                public void onResponse(@NonNull Call<UserList> call, @NonNull Response<UserList> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Success
                        if (response.body().getResult() != null)
                            callback.onResult(response.body().getResult());
                    } else {
                        onFailure(call, new Exception(String.format(Locale.ENGLISH, "Unexpected response %d", response.code())));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserList> call, @NonNull Throwable t) {
                    call.cancel();
                    // Handle err
                    Timber.e(t);
                }
            });
        }
    }

    public void getReplacedText(EditText editText, String selected,
                                int lastCursorPosition) {
        String toString = editText.getText().toString();
        StringBuilder builder = new StringBuilder(toString);
        int end = lastCursorPosition;
        int start = -1;
        lastCursorPosition--;
        while (lastCursorPosition >= 0) {
            char c = toString.charAt(lastCursorPosition);
            if (c == '@') {
                if (start == -1) start = lastCursorPosition;
                break;
            } else {
                start = lastCursorPosition;
                lastCursorPosition--;
            }
        }
        builder.replace(start, end, selected);
        editText.setText(builder.toString());
//        editText.setSelection(builder.toString().length());
        searchUser = false;
    }

    public void getCommand(int currentPage, String videoId, int adapterPosition) {

        if (noCommentLay.getVisibility() == View.VISIBLE) noCommentLay.setVisibility(View.GONE);

        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            if (Constants.Login_method.equals("skip")) {
                map.put(Constants.TAG_USER_ID, "");
            } else {
                map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            }
            map.put(Constants.TAG_VIDEO_ID, videoId);
            map.put(Constants.TAG_OFFSET, String.valueOf(currentPage));
            map.put(Constants.TAG_LIMIT, "20");

            Timber.d("getCommentParams: %s", new Gson().toJson(map));

            Call<VideoCommentResponse> call = apiInterface.getVideoComments(map);
            call.enqueue(new Callback<VideoCommentResponse>() {
                @Override
                public void onResponse(@NotNull Call<VideoCommentResponse> call, @NotNull Response<VideoCommentResponse> response) {

                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {

                        noCommentLay.setVisibility(View.GONE);
                        commentRec.setVisibility(View.VISIBLE);

                        if (response.body().getIsUserCanComment()) {
                            sendCommentLay.setVisibility(View.VISIBLE);
                        } else {
                            sendCommentLay.setVisibility(View.INVISIBLE);
                        }

                        if (loadMoreCheckVideoCommand.equals("initial")) {
                            commentList.clear();
                            commentList.addAll(response.body().getResult());
                            swipeRefresh(false, videoId, adapterPosition);
                            mSwipeVideoComment.setEnabled(false);
                            commentRec.addOnScrollListener(videoScrollListener);

                          /*  Bundle payload = new Bundle();
                            mHomeApiResponse.get(adapterPosition).setVideoCommentCounts(String.valueOf(response.body().getCommentcount()));
                            payload.putString("video_comment_count", String.valueOf(response.body().getCommentcount()));
                            notifyItemChanged(adapterPosition, payload);
*/
                           // ((VideoViewHolder) holder).txt_comment_count.setText(String.valueOf(response.body().getCommentcount()));
                            Log.e(TAG,"VIDEO_DEBUG : "+ response.body().getCommentcount().toString()  +"   test");
                            EventBus.getDefault().post(new CommentDialogStateEvent(response.body().getCommentcount().toString()));

                            commentCount.setText(response.body().getCommentcount() + " " + mActivity.getResources().getString(R.string.comments));
                            //commentCount.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.anim_zoom_out));

                        } else if (loadMoreCheckVideoCommand.equals("loadmore")) {
                            commentList.addAll(response.body().getResult());
                        }

                        commendAdapter.notifyDataSetChanged();

                    } else if (response.body().getStatus().equals(Constants.TAG_FALSE)) {
                        swipeRefresh(false, videoId, adapterPosition);
                        mSwipeVideoComment.setEnabled(false);

                        if (response.body().getIsUserCanComment()) {
                            sendCommentLay.setVisibility(View.VISIBLE);
                            Timber.d("getcommand onResponse: true");
                        } else {
                            Timber.d("getcommand  onResponse: false");
                            sendCommentLay.setVisibility(View.INVISIBLE);
                        }

                        if (commentList.size() == 0) {
                            noCommentLay.setVisibility(View.VISIBLE);
                            commentRec.setVisibility(View.GONE);
                        }
                    } else if (response.body().getStatus().equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                        App.makeToast("Account is blocked");
                        GetSet.reset();
                        SharedPref.clearAll();
                        DBHelper.getInstance(mActivity).clearDB();
                        Intent logout = new Intent(mActivity, LoginActivity.class);
                        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        mActivity.startActivity(logout);
                    }

                }

                @Override
                public void onFailure(@NotNull Call<VideoCommentResponse> call, @NotNull Throwable t) {
                    call.cancel();
                    App.makeToast(mActivity.getString(R.string.something_went_wrong));
                }
            });
        } else {
            Toasty.error(mActivity.getApplicationContext(), mActivity.getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void swipeRefresh(final boolean refresh, String videoId, int adapterPosition) {

        mSwipeVideoComment.setRefreshing(refresh);

        if (refresh) {
            lastOffsetVideo = 0;
            currentPage = 0;
            loadMoreCheckVideoCommand = "initial";
            getCommand(currentPage, videoId, adapterPosition);
        }
    }

    public void postComments(final String comments, String videoId, int adapterPosition) {

        if (NetworkReceiver.isConnected()) {
            Tagged tagged = getTagged(mentionsCache, mCommentsEditText.getMentions());
            Timber.d("Mentions: ids=%s names=%s", tagged.getCommaSeparatedTaggedIdList(),
                    tagged.getCommaSeparatedTaggedNameList());
                    /*tagged_user_id = getTaggedUserIds(mentionsCache, mCommentsEditText.getMentions());
                    tagged_user_name = getTaggedUserNames(mentionsCache, mCommentsEditText.getMentions());*/
            Map<String, String> map = new HashMap<>();
            map.put("user_id", GetSet.getUserId());
            map.put("video_id", videoId);
            map.put("tagged_user_id", tagged.getCommaSeparatedTaggedNameList());
            map.put("tagged_id", tagged.getCommaSeparatedTaggedIdList());
            map.put("token", GetSet.getAuthToken());
            map.put("comment", comments);
            Timber.d("postComments: params %s", App.getGsonPrettyInstance().toJson(map));

            loadMoreCheckVideoCommand = "initial";
            mSwipeVideoComment.setEnabled(true);
            mSwipeVideoComment.setRefreshing(true);
            commentEditDialog.dismiss();

            Call<Map<String, String>> call3 = apiInterface.postComments(map);
            call3.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> data = response.body();
//                        Timber.d("onResponse: %s", new Gson().toJson(response.body()));
                        if (data != null && data.get(Constants.TAG_STATUS).equals("true")) {
                            list_tagged_user_ids.clear();
                            list_tagged_user_names.clear();
                            tagcomment = "";
                            tagcomment = "";
                            tagged_user_name = "";
                            tagged_user_id = "";

                            lastOffsetVideo = 0;
                            currentPage = 0;

                         /*   Bundle payload = new Bundle();
                            mHomeApiResponse.get(adapterPosition).setVideoCommentCounts(data.get("commentcount"));
                            payload.putString("video_comment_count", data.get("commentcount"));
                            notifyItemChanged(adapterPosition, payload);*/

                            commentRec.clearOnScrollListeners();
                            commentRec.scrollToPosition(0);
                            loadMoreCheckVideoCommand = "initial";
                            getCommand(0, videoId, adapterPosition);

                            commentCount.setText(data.get("commentcount") + " " + mActivity.getApplicationContext().getResources().getString(R.string.comments));
                            commentCount.startAnimation(AnimationUtils.loadAnimation(mActivity.getApplicationContext(), R.anim.anim_zoom_out));

                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                    Timber.d("onFailure: %s", "response- failure");
                }
            });

        } else {
            Toasty.error(mActivity.getApplicationContext(), mActivity.getApplicationContext().getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    public static Tagged getTagged(ArrayList<Mention2> mentionsCache, List<String> mentions) {
        Timber.d("getTagged: %s %s", new Gson().toJson(mentionsCache), mentions);
        final Tagged tagged = new Tagged();
        ArrayList<Mention2> tempList = new ArrayList<Mention2>(new LinkedHashSet<Mention2>(mentionsCache));
        for (String name : mentions) {
            Timber.d("Comparing: %s with %s", tempList, name);
            for (Mention2 mention2 : tempList) {
                if (name.equals(mention2.getUsername().toString().trim())) {
                    if (!tagged.getTaggedIdsList().contains(mention2)) {
                        final Mention2 m2 = mention2;
                        tagged.addTaggedEntry(m2.getId(), m2.getUsername().toString());
                    }
                }
            }
        }
        return tagged;
    }

    private void followAPI(String publisherId, String publisherImage, ImageView profileFollowIcon, HomeResponse.Result videoItem, int position, TextView followButton2) {

        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            followRequest.setUserId(publisherId);
            followRequest.setFollowerId(GetSet.getUserId());
            followRequest.setType(Constants.TAG_FOLLOW_USER);


            Timber.d("followUnfollowUser: %s", App.getGsonPrettyInstance().toJson(followRequest));

            Call<Map<String, String>> call = apiInterface.followUser(followRequest);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    Map<String, String> followResponse = response.body();

                    if (followResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                        String userType = "";
                        if (GetSet.getUserId() != null) {
                            userType = (publisherId).equals(GetSet.getUserId()) ? "" : publisherId;
                            Timber.i("userType %s", userType);
                        }

                        EventBus.getDefault().post(new FollowingProfileUpdate(publisherId, publisherImage, userType));

                        Timber.d("onResponse: %s=> ", App.getGsonPrettyInstance().toJson(followResponse));
                        clickListener.onItemClicked(videoItem, position, Constants.TAG_FOLLOWFROM_OTHERS);
                        profileFollowIcon.clearAnimation();
                        profileFollowIcon.setVisibility(View.GONE);
                        followButton2.setVisibility(View.GONE);

                        Bundle payload = new Bundle();
                        for (int i = 0; i < mHomeApiResponse.size(); i++) {
                            if (mHomeApiResponse.get(i).getPublisherId().equals(publisherId)) {
                                mHomeApiResponse.get(i).setFollowedUser(true);
                                payload.putString("follow", "true");
                                notifyItemChanged(i, payload);
                            }
                        }
                    }
                }

                @Override
                public void onFailure
                        (@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        } else {
            Toasty.error(mActivity.getApplicationContext(), mActivity.getApplicationContext().getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void getProfile(String getUserId, String publisherId, ImageView
            profileFollowIcon, HomeResponse.Result videoItem, int position, TextView followButton2) {

        if (GetSet.getUserId() != null && apiInterface != null) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(publisherId);
            Timber.d("getProfilechat params: %s", App.getGsonPrettyInstance().toJson(request));
            Call<ProfileResponse> call = apiInterface.getProfile(request);

            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                    if (response.isSuccessful()) {
                        profile = response.body();
                        if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                            block_status = profile.getBlockedByMe();
                            Log.d(TAG, "onResponse_block_status: " + block_status);
                            if (block_status != null) {
                                if (block_status.equals("true")) {
                                    Toast.makeText(mActivity, "User Blocked...", Toast.LENGTH_SHORT).show();
                                } else {
                                    profileFollowIcon.startAnimation(AnimationUtils.loadAnimation(mActivity, R.anim.anim_zoom_out));
                                    followAPI(videoItem.getPublisherId(), videoItem.getPublisherImage(), profileFollowIcon, videoItem, position, followButton2);
                                    clickListener.onItemClicked(videoItem, position, Constants.TAG_FOLLOW);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ProfileResponse> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void heartFun(int adapterPosition) {
        if (NetworkReceiver.isConnected()) {

            Map<String, String> map = new HashMap<>();

            map.put("user_id", GetSet.getUserId());
            map.put("video_id", mHomeApiResponse.get(adapterPosition).getVideoId());
            map.put("token", GetSet.getAuthToken());

            Call<Map<String, String>> call = apiInterface.getHeartStatus(map);

            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                    Timber.d("onResponse: %s", response.body());

                    if (response.body() != null && response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {


                        Timber.d("onResponse: %s", response.body());
                        likePrevent = false;

                        String userType = "", publisherId = mHomeApiResponse.get(adapterPosition).getPublisherId();

                        if (GetSet.getUserId() != null) {
                            userType = (publisherId).equals(GetSet.getUserId()) ? "" : publisherId;
                            Timber.i("userType %s", userType);
                        }

                        Bundle payload = new Bundle();
                        if (response.body().get("message").equals("UnLiked Successfully")) {
                            EventBus.getDefault().post(new ForYouVideoLike((mHomeApiResponse.get(adapterPosition).getVideoId()), "unliked", response.body().get("likecount")));
                            mHomeApiResponse.get(adapterPosition).setIsLiked(false);
                            payload.putString("likes_status", "false");
                        } else {
                            EventBus.getDefault().post(new ForYouVideoLike((mHomeApiResponse.get(adapterPosition).getVideoId()), "liked", response.body().get("likecount")));
                            mHomeApiResponse.get(adapterPosition).setIsLiked(true);
                            payload.putString("likes_status", "true");
                        }
                        payload.putString("likes_count", response.body().get("likecount"));
                        mHomeApiResponse.get(adapterPosition).setLikes(response.body().get("likecount"));
                        notifyItemChanged(adapterPosition, payload);
                        EventBus.getDefault().post(new FollowingProfileUpdate(mHomeApiResponse.get(adapterPosition).getPublisherId(), mHomeApiResponse.get(adapterPosition).getPublisherImage(), userType));
                    } else if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                        App.makeToast("Account is blocked");
                        GetSet.reset();
                        SharedPref.clearAll();
                        DBHelper.getInstance(mActivity).clearDB();
                        Intent logout = new Intent(mActivity, LoginActivity.class);
                        logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        mActivity.startActivity(logout);
                    }
                }

                @Override
                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {


                }
            });

        } else {
            Toasty.error(mActivity.getApplicationContext(), mActivity.getApplicationContext().getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void addReadMore(final String text, final LinkEllipseTextView textView) {
        SpannableString ss = new SpannableString(text.substring(0, 100) + " " + mActivity.getString(R.string.read_more));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                addReadLess(text, textView);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ds.setColor(ContextCompat.getColor(mActivity, R.color.colorPrimary));
                } else {
                    ds.setColor(mActivity.getResources().getColor(R.color.colorPrimary));
                }
            }
        };
        ss.setSpan(clickableSpan, ss.length() - 10, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void addReadLess(final String text, final LinkEllipseTextView textView) {
        SpannableString ss = new SpannableString(text + " " + mActivity.getString(R.string.read_less));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                addReadMore(text, textView);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ds.setColor(ContextCompat.getColor(mActivity, R.color.colorPrimary));
                } else {
                    ds.setColor(mActivity.getResources().getColor(R.color.colorPrimary));
                }
            }
        };
        ss.setSpan(clickableSpan, ss.length() - 10, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Log.d(TAG, "addReadLess: " + ss);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public interface VideoAdapterListener {

        void setFavourite(boolean favourite, ArrayList<HomeResponse.Result> result, int adapterPosition);

        void setPlayback(boolean b, int adapterPosition, String tagPlayback);

        void setVideoReport(boolean b, int adapterPosition);

        void checkStoragePermissions();

        void moveToIntent(Intent intent);

        void setVideoDeleted(boolean isDeleted, String videoId);

        void setVideoPlayBackSpeed(boolean speedChanged,String videoId,int adapterPosition,String playbackSpeed, float tagPlaybackSpeed);

        void setVideoCaption(boolean captionEnabled,String videoId,int adapterPosition,String typeCaption,String playBackUrl);
        void reposted(boolean reposted,String videoId,int adapterPosition,String action);
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
        List<HomeResponse.Image> mHomeApiResponse = new ArrayList<>();

        String androidUrl = "";

        public RecyclerViewAdapter(List<HomeResponse.Image> mHomeApiResponse, String url) {
            this.mHomeApiResponse = mHomeApiResponse;
            androidUrl = url;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycleview_bottom, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int pos) {
            int position = holder.getAbsoluteAdapterPosition();
            ((MyViewHolder) holder).onBind(mHomeApiResponse.get(position));
            //  HomeResponse.Result item = mHomeApiResponse.get(position);
            if (holder instanceof MyViewHolder) {
                HomeResponse.Image itemm = mHomeApiResponse.get(holder.getAbsoluteAdapterPosition());


                holder.add_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "onClick: " + androidUrl);
                        try {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(androidUrl));
                            mActivity.startActivity(browserIntent);
                            //  Log.d(TAG, "onClick_item: "+item.getAndroidUrl());
                        } catch (Exception e) {
                            Toasty.error(mActivity, R.string.not_valid_url, Toasty.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

        private final int limit = 2;

        @Override
        public int getItemCount() {
          /*  if(mHomeApiResponse.size() > limit){
                return limit;
            }
            else
            {*/
            return mHomeApiResponse.size();
            //  }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView add_image;

            public MyViewHolder(View view) {
                super(view);

                add_image = view.findViewById(R.id.add_image);
            }


            @Override
            public void onClick(View view) {

            }

            void onBind(HomeResponse.Image videoItem) {

                Glide.with(mActivity)
                        .load(videoItem.getImage_url())
                        .placeholder(R.drawable.image_placeholderads)
                        .error(R.drawable.image_placeholderads)
                        .override(200, 150) // resizes the image to these dimensions (in pixel)
                        .centerCrop()
                        //   .apply(new RequestOptions().override(150, 150))
                        .into(add_image);
                Log.d(TAG, "onBind_getImage_url: " + videoItem.getImage_url());
                /*if(videoItem.getLink_type()!=null && videoItem.getLink_type().equals("Images")){
                    Log.d(TAG, "onBindViewHolder:imagesize " + videoItem.getImages().size());
                    if(videoItem.getImages().size() > 0){
                        for (int i=0; i < videoItem.getImages().size(); i++){
                            Log.d(TAG, "onBindViewHolder:image " + videoItem.getImages().get(i).getImage_url());

                        }
                    }
                }*/
            }
        }
    }

    private void signInZEGOSDK(String userID, String userName, ZEGOSDKCallBack callback) {
        if (userID != null && !userID.trim().isEmpty()) {
            Log.e(TAG, "TestLiveStreaming : 1");
            ZEGOSDKManager.getInstance().connectUser(userID, userName, callback);
        }
    }


    private void applyTextDirection(TextView textView, String langCode) {
        if (langCode != null && (langCode.startsWith("ar") )) {
            // Arabic, Farsi, Urdu, Hebrew → RTL
            textView.setTextDirection(View.TEXT_DIRECTION_RTL);
            textView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            textView.setGravity(Gravity.RIGHT);
        } else {
            // Default → LTR
            textView.setTextDirection(View.TEXT_DIRECTION_LTR);
            textView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            textView.setGravity(Gravity.LEFT);
        }
    }


    // place this helper in the same class (adapt types if using a custom LinkableTextView)
    private void applyReadMoreIfNeeded(final LinkEllipseTextView tv, final String fullText) {
        // reset text first (avoids duplicate appended "See more")
        tv.setText(fullText);
        // ensure ellipsize + maxlines are set for the collapsed state
        tv.setIsEndEllipsable(true);
        tv.setMaxLines(2);

        // wait until layout is ready to inspect line count
        tv.post(() -> {
            Layout layout = tv.getLayout();
            if (layout != null && layout.getLineCount() > 2) {
                // call your existing addReadMore implementation
                // make sure addReadMore replaces/creates the "See more" only once
                addReadMore(fullText, tv);
            } else {
                // no need for "See more" — make sure any previous behavior is cleared
                // If addReadMore added spans, re-set plain text to remove them:
                tv.setText(fullText);
            }
        });
    }

}