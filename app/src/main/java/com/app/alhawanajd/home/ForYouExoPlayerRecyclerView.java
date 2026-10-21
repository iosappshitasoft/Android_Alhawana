package com.app.alhawanajd.home;
import static com.app.alhawanajd.home.ForYouVideoFragment.updatedVideoList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.text.Layout;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.alhawanajd.home.eventbus.SpeedChangeEndEvent;
import com.app.alhawanajd.home.eventbus.SpeedChangeStartEvent;
import com.app.alhawanajd.home.eventbus.VideoZoomEvent;
import com.app.helper.CustomVideoSeekBar;
import com.app.helper.LocaleManager;
import com.bumptech.glide.RequestManager;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.TracksInfo;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.video.VideoSize;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.app.alhawanajd.R;
import com.app.db.DBHelper;
import com.app.helper.callback.PlayerRecyclerListener;
import com.app.model.HomeResponse;
import com.app.utils.Constants;
import com.app.utils.exoutils.ExoPlayerUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class ForYouExoPlayerRecyclerView extends RecyclerView {

    private static final String TAG = ForYouExoPlayerRecyclerView.class.getSimpleName();
    private boolean isResuming = false;

    /**
     * PlayerViewHolder UI component
     * Watch PlayerViewHolder class
     */
    private ImageView mediaCoverImage, volumeControl;
    private ProgressBar progressBar;
    private SeekBar progress_duration;
    private View viewHolderParent;
    private FrameLayout mediaContainer;
    private StyledPlayerView videoSurfaceView;
    private ExoPlayer videoPlayer;
    private TextView marqueeText;
    private TextView txt_comment_count;

    private CustomBottomTextViewHelper bottomTextHelper;
    /**
     * variable declaration
     */
    SharedPreferences watchPref;
    // Media List
    private ArrayList<HomeResponse.Result> homepageResponseResult = new ArrayList<>();
    private DBHelper dbHelper;
    private int videoSurfaceDefaultHeight = 0;
    private int screenDefaultHeight = 0;
    public int targetPosition;
    private Context mContext;
    private int playPosition = -1;
    private boolean isVideoViewAdded;
    private PlayerRecyclerListener listener;
    private RequestManager requestManager;
    // controlling volume state
    private VolumeState volumeState;

    public PlayState videoState; // video pause and play
    Handler mHandler = new Handler();
    Runnable runnable;
    Runnable updateView;
    int currentPage = 0, visibleItemCount, totalItemCount, firstVisibleItem, previousTotal, visibleThreshold;


    private final View.OnClickListener videoViewClickListener = v -> {
        /*togglePlay();*/
    };
    private boolean isUserSeeking;


    public ForYouExoPlayerRecyclerView(@NonNull Context context) {
        super(context);
        bottomTextHelper = new CustomBottomTextViewHelper(
                getContext(), (ViewGroup) ((Activity) context).findViewById(android.R.id.content));
        init(context);
    }

    public ForYouExoPlayerRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        bottomTextHelper = new CustomBottomTextViewHelper(
                getContext(), (ViewGroup) ((Activity) context).findViewById(android.R.id.content));
        init(context);
    }


    public void togglePlay() {

        if ((homepageResponseResult.get(targetPosition).getPlaytype().equals(Constants.TAG_VIDEO))) {
            if (videoPlayer != null) {
                if (videoState == PlayState.OFF) {
                    videoPlayer.setPlayWhenReady(true);
                    videoState = PlayState.ON;
                } else if (videoState == PlayState.ON) {
                    videoPlayer.setPlayWhenReady(false);
                    videoState = PlayState.OFF;
                }

                animatePlayControl();
            }
        } else {
           /* try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(homepageResponseResult.get(targetPosition).getAndroidUrl()));
                getContext().startActivity(browserIntent);
            } catch (Exception e) {
                Toasty.error(getContext(), R.string.not_valid_url, Toasty.LENGTH_SHORT).show();
            }*/

        }


    }

    private void init(Context context) {
        this.mContext = context.getApplicationContext();
        dbHelper = DBHelper.getInstance(context);
        watchPref = getContext().getSharedPreferences("WatchVideo", Context.MODE_PRIVATE);

        Display display = ((WindowManager) Objects.requireNonNull(
                getContext().getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        videoSurfaceDefaultHeight = point.x;
        screenDefaultHeight = point.y;

        videoSurfaceView = new StyledPlayerView(this.mContext);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);
        // Added new
        /*DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(5000, 10000, 5000, 5000)
                .build();*/
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd());
//        DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(context);
//        renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
        videoPlayer = new ExoPlayer.Builder(context)
//                .setRenderersFactory(renderersFactory)
                .setTrackSelector(trackSelector)
//                .setLoadControl(loadControl)
                .build();

        // Disable Player Control
        videoSurfaceView.setUseController(false);
        // Bind the player to the view.
        videoSurfaceView.setPlayer(videoPlayer);
        // Turn on Volume
        setVolumeControl(VolumeState.ON);


        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    Timber.i("SCROLL_STATE_IDLE");

                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic

                    if (volumeControl != null) volumeControl.setVisibility(GONE);

                    if (!recyclerView.canScrollVertically(1)) {
                        playVideo(true);
                    } else {
                        playVideo(false);
                        Timber.i("end of the list");
                    }
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    /*if (homepageResponseResult.size() > 0 && playPosition != -1)
                        EventBus.getDefault().post(new homeForYouSwipePrevent(true, "Ad")); // swipe disabled*/
                } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    Timber.i("SCROLL_STATE_SETTLING");
                    /*if (homepageResponseResult.size() > 0 && playPosition != -1 && homepageResponseResult.get(playPosition).getPlaytype().equals(Constants.TAG_VIDEO)) {
                        EventBus.getDefault().post(new homeForYouSwipePrevent(false, "")); // swipe enabled
                    } else
                        EventBus.getDefault().post(new homeForYouSwipePrevent(true, "Ad")); // swipe disabled*/
                }
            }


            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = ((LinearLayoutManager) getLayoutManager()).getItemCount();
                firstVisibleItem = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();


            }
        });


        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                if (viewHolderParent != null && viewHolderParent.equals(view)) {
                    resetVideoView();
                }
            }
        });

        videoPlayer.addListener(new Player.Listener() {


            @Override
            public void onTracksInfoChanged(TracksInfo tracksInfo) {
//                Timber.d("onTracksInfoChanged: %s", new Gson().toJson(tracksInfo));
                Player.Listener.super.onTracksInfoChanged(tracksInfo);
                if (videoPlayer.getCurrentPosition() > 0) {
                    updatedVideoList.clear();
                    String watchStr = watchPref.getString(Constants.TAG_ID, "");
                    ArrayList<String> list = new ArrayList<>();
                    if (watchStr.equals("")) {
                    } else {
                        JsonParser jsonParser = new JsonParser();
                        jsonParser.parse(watchStr);
                        Gson gson = new Gson();
                        // below line is to get the type of our array list.
                        Type type = new TypeToken<ArrayList<String>>() {
                        }.getType();
                        list = gson.fromJson(watchStr, type);
                        Log.e("updateList", "-" + updatedVideoList);
                    }
                    try {
                        SharedPreferences.Editor editor = watchPref.edit();
                        ArrayList<String> templist = new ArrayList<>();
                        templist.add(homepageResponseResult.get(targetPosition).getVideoId());
                        templist.addAll(removeAlreadyList(list, homepageResponseResult.get(targetPosition).getVideoId()));
                        Gson gson = new Gson();
                        String gsonStr = gson.toJson(templist);
                        editor.putString(Constants.TAG_ID, gsonStr);
                        editor.commit();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Timber.e("onTracksInfoChanged: %s", e.getMessage());
                    }
                }
            }


            @Override
            public void onIsLoadingChanged(boolean isLoading) {
                Timber.d("onLoadingChanged: %s", isLoading);
            }

            @Override
            public void onTimelineChanged(Timeline timeline, int reason) {
//                Timber.d("onTimelineChanged: %s", timeline);
            }

            @Override
            public void onVideoSizeChanged(VideoSize videoSize) {
                Player.Listener.super.onVideoSizeChanged(videoSize);
                Timber.d("onVideoSizeChanged: %s, %s, %s", videoSize.width, videoSize.height, videoSize.pixelWidthHeightRatio);
            }


            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Timber.d("onPlaybackStateChanged: %s", playbackState);
                switch (playbackState) {

                    case Player.STATE_BUFFERING:
                        //videoPlayer.seekTo(0);
                        mediaCoverImage.setAlpha(1f);
                        mediaCoverImage.setVisibility(VISIBLE);
                        break;
                    case Player.STATE_ENDED:
                        if (homepageResponseResult.size() > 0 && playPosition != -1) {
                            dbHelper.insertVideoStatus(homepageResponseResult.get(playPosition).getVideoId());
                        }
//                        videoPlayer.seekTo(0);

                        break;
                    case Player.STATE_IDLE:

                        break;
              /*      case Player.STATE_READY:
                        // hide via first-frame listener (we keep present just in case)
                        // mediaCoverImage will be hidden in onRenderedFirstFrame()

                        // Set resize mode and thumbnail scale depending on video aspect ratio
                        if (videoPlayer.getVideoFormat() != null) {
                            int videoWidth = videoPlayer.getVideoFormat().width;
                            int videoHeight = videoPlayer.getVideoFormat().height;

                            if (videoHeight > videoWidth) {
                                // Portrait — fill screen; thumbnail should crop to fill
                                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                                if (mediaCoverImage != null) mediaCoverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            } else {
                                // Landscape — show with letterbox; thumbnail should not be cropped to full screen
                                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                                if (mediaCoverImage != null) {
                                    // FIT_CENTER shows the full image without cropping and preserves aspect ratio
                                    mediaCoverImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                }
                            }
                        } else {
                            // Fallback (unknown video format) — use reasonable defaults
                            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                            if (mediaCoverImage != null) mediaCoverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }

                        // existing code that adds the surface view
                        if (!isVideoViewAdded) {
                            addVideoView();
                            progressBar();
                        }
                        break;*/

                    //original
                    case Player.STATE_READY:
                        mediaCoverImage.animate().alpha(0f)
                                .setDuration(300).setStartDelay(280);

                        videoState = PlayState.ON;
                        volumeState = VolumeState.ON;

                        // Set resize mode based on video orientation
                        if (videoPlayer.getVideoFormat() != null) {
                            int videoWidth = videoPlayer.getVideoFormat().width;
                            int videoHeight = videoPlayer.getVideoFormat().height;
                            Log.e(TAG,"VIDEO_DEBUG :  videoHeight" + videoHeight);
                            Log.e(TAG,"VIDEO_DEBUG :  videoWidth" + videoWidth);
                            // Check if video is portrait (height > width)
                            if (videoHeight > videoWidth) {
                                // Portrait video - fill the screen
                                //RESIZE_MODE_ZOOM
                                //RESIZE_MODE_FILL
                                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                                Timber.d("Portrait video detected - using ZOOM mode");
                                // if (mediaCoverImage != null) mediaCoverImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            } else {
                                // Landscape video - play as it is
                                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                                Timber.d("Landscape video detected - using FIT mode");
                               /* if (mediaCoverImage != null) {
                                    // FIT_CENTER shows the full image without cropping and preserves aspect ratio
                                    mediaCoverImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                }*/
                            }
                        }

                        if (!isVideoViewAdded) {
                            addVideoView();
                            progressBar();
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                Log.e("updateList", "-" + shuffleModeEnabled);
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Timber.tag(TAG).e("onPlayerError: %s, %s, %s", error.getMessage(), error.errorCode, error.getErrorCodeName());
                Player.Listener.super.onPlayerError(error);
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }
        });


        // Register EventBus when RecyclerView is created
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Make sure EventBus is registered when attached to window
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Unregister EventBus when detached from window to prevent memory leaks
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (bottomTextHelper != null) {
            bottomTextHelper.dismissBottomText();
        }
    }

    // EventBus subscriber methods
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSpeedChangeStart(SpeedChangeStartEvent event) {
        // Handle speed change start
//        isSpeedChanged = true;

        if (videoPlayer != null) {
            videoPlayer.setPlaybackSpeed(2.0f);

            // Optional: Show speed indicator
            showSpeedIndicator(true);
        }

        // Log for debugging
        android.util.Log.d("ForYouRecyclerView", "Speed changed to 2x for position: " + event.getPosition());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSpeedChangeEnd(SpeedChangeEndEvent event) {
        // Handle speed change end
//        isSpeedChanged = false;

        if (videoPlayer != null) {
            videoPlayer.setPlaybackSpeed(1.0f);

            // Optional: Hide speed indicator
            showSpeedIndicator(false);
        }

        // Log for debugging
        android.util.Log.d("ForYouRecyclerView", "Speed changed back to normal for position: " + event.getPosition());
    }

    // Your existing methods
    public void setVideoPlayer(ExoPlayer player) {
        this.videoPlayer = player;
    }
    // Updated speed indicator method using custom TextView
  /*  private void showSpeedIndicator(boolean show) {
        if (bottomTextHelper == null) return;

        if (show) {
            // Show 2x speed indicator at bottom
            CustomBottomTextViewHelper.TextViewConfig config = CustomBottomTextViewHelper.TextViewConfig.minimal();
            config.animationType = CustomBottomTextViewHelper.AnimationType.NONE;
            String spd=" 2x Speed";
            spd = LocaleManager.isRTL()? "2x: السرية":spd;
            bottomTextHelper.showBottomText(spd, 0.95f,config);
        } else {
            // Dismiss the indicator
            bottomTextHelper.dismissBottomText(CustomBottomTextViewHelper.AnimationType.NONE);
        }
    }
*/

   /* private void showSpeedIndicator(boolean show) {
        if (bottomTextHelper == null) return;

        if (show) {
            CustomBottomTextViewHelper.TextViewConfig config = CustomBottomTextViewHelper.TextViewConfig.minimal();
            config.animationType = CustomBottomTextViewHelper.AnimationType.NONE;

            SpannableStringBuilder sb = new SpannableStringBuilder();
            Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.fast_forward);

            if (drawable != null) {
                int size = (int) (16 * mContext.getResources().getDisplayMetrics().density);
                drawable.setBounds(0, 0, size, size);

                ImageSpan imageSpan = new CenteredImageSpan(drawable);

                if (LocaleManager.isRTL()) {
                    String text = " السرعة: 2x ";
                    sb.append(text);
                    sb.setSpan(imageSpan, sb.length() - 1, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    String text = "2x Speed ";
                    sb.append(text);
                    sb.append(" ");
                    sb.setSpan(imageSpan, sb.length() - 1, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            bottomTextHelper.showBottomText(sb, config);
        } else {
            bottomTextHelper.dismissBottomText(CustomBottomTextViewHelper.AnimationType.NONE);
        }
    }
*/

    private void showSpeedIndicator(boolean show) {
        if (bottomTextHelper == null) return;

        if (show) {
            CustomBottomTextViewHelper.TextViewConfig config =
                    CustomBottomTextViewHelper.TextViewConfig.minimal();
            config.animationType = CustomBottomTextViewHelper.AnimationType.NONE;

            SpannableStringBuilder sb = new SpannableStringBuilder();
            Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.fast_forward);

            if (drawable != null) {
                int size = (int) (16 * mContext.getResources().getDisplayMetrics().density);
                drawable.setBounds(0, 0, size, size);

                String lang = LocaleManager.getLanguageCode(mContext);
                if ("ar".equalsIgnoreCase(lang)) {
                    // 👉 Mirror the icon for Arabic (RTL)
                    drawable.setAutoMirrored(true);
                    drawable.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                }

                ImageSpan imageSpan = new CenteredImageSpan(drawable);

                if ("ar".equalsIgnoreCase(lang)) {
                    // Arabic: icon first, then text
                    sb.append("\uFFFC"); // placeholder for icon
                    sb.setSpan(imageSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.append(" السرعة: 2x");
                } else {
                    // English: text first, then icon
                    sb.append("2x Speed ");
                    int start = sb.length();
                    sb.append("\uFFFC"); // placeholder for icon
                    sb.setSpan(imageSpan, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            bottomTextHelper.showBottomText(sb, config);
        } else {
            bottomTextHelper.dismissBottomText(CustomBottomTextViewHelper.AnimationType.NONE);
        }
    }

    public class CenteredImageSpan extends ImageSpan {

        public CenteredImageSpan(Drawable drawable) {
            super(drawable, ALIGN_BOTTOM); // base alignment, will override
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text,
                         int start, int end, float x,
                         int top, int y, int bottom, @NonNull Paint paint) {

            Drawable b = getDrawable();
            canvas.save();

            int transY = top + ((bottom - top) - b.getBounds().bottom) / 2; // vertical center
            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }
    }

    private void showTrackIndicator(boolean show) {
        if (bottomTextHelper == null) return;

        if (show) {
            // Show 2x speed indicator at bottom
            CustomBottomTextViewHelper.TextViewConfig config = CustomBottomTextViewHelper.TextViewConfig.minimal();
            config.animationType = CustomBottomTextViewHelper.AnimationType.NONE;
            bottomTextHelper.showBottomText("0:00 / 0:00",0.7f, config);
        } else {
            // Dismiss the indicator
            bottomTextHelper.dismissBottomText(CustomBottomTextViewHelper.AnimationType.NONE);
        }
    }

    private void updateTrackIndicator(String s) {
        if (bottomTextHelper != null){
            bottomTextHelper.updateText(s);
        }

    }


    private void showCustomSpeedToast(String message) {
        Context context = getContext();
        if (context == null) return;

        Toast toast;

        toast = Toasty.normal(context, message, Toast.LENGTH_SHORT);


        // Center the toast with slight offset from center if needed
        toast.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }


    public ArrayList<String> removeAlreadyList(ArrayList<String> list, String Id) {

        int pos = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(Id)) {
                pos = i;
            }
        }
        if (pos != -1)
            list.remove(pos);
        return list;
    }

    private void progressBar() {

        runnable = () -> {
            if (videoPlayer != null) {
                progress_duration.setProgress((int) ((videoPlayer.getCurrentPosition() * 100) / videoPlayer.getDuration()));
                Log.d(TAG, "progressBar: Progress "+ (videoPlayer.getCurrentPosition() * 100) / videoPlayer.getDuration());
            }
            mHandler.postDelayed(runnable, 200);
        };
        runnable.run();

    }


    public void setPlayControl(boolean state) {
        Timber.d("setPlayControl: %s", state);
        videoPlayer.setPlayWhenReady(state);
        if (volumeControl != null && volumeControl.getVisibility() == VISIBLE)
            volumeControl.setVisibility(GONE);

    }

    public void setPlayBackSpeed(boolean speedChanged,float speed) {
        Timber.d("setPlayControl: %s", speed);
      /*  if(speedChanged){
        videoPlayer.setPlaybackSpeed(speed);

        }*/
        if (homepageResponseResult == null || homepageResponseResult.isEmpty()) {
            return;
        }

        if (targetPosition < 0 || targetPosition >= homepageResponseResult.size()) {
            return;
        }
        if ((homepageResponseResult.get(targetPosition).getPlaytype().equals(Constants.TAG_VIDEO))) {
            if (videoPlayer != null) {
           /*     if (videoState == PlayState.OFF) {
                    videoPlayer.setPlayWhenReady(true);
                    videoState = PlayState.ON;
                } else if (videoState == PlayState.ON) {
                    videoPlayer.setPlayWhenReady(false);
                    videoState = PlayState.OFF;
                }*/

//                if(videoId.equals(homepageResponseResult.get(targetPosition).getVideoId())){
                videoPlayer.setPlaybackSpeed(speed);
//                }


            }
        }

    }

    public void getCaption(String videoUrl,String captionText){
        try {
            File srtFile = ExoPlayerUtils.createSrtFile(captionText, mContext);

            Uri videoUri = Uri.parse(videoUrl);
            Uri subtitleUri = Uri.fromFile(srtFile);

            MediaItem.SubtitleConfiguration subtitleConfig =
                    new MediaItem.SubtitleConfiguration.Builder(subtitleUri)
                            .setMimeType(MimeTypes.APPLICATION_SUBRIP)
                            .setLanguage("en")
                            .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                            .build();

            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(videoUri)
                    .setSubtitleConfigurations(
                            Collections.singletonList(subtitleConfig)
                    )
                    .build();

            // IMPORTANT: Use your actual player variable here
            videoPlayer.setMediaItem(mediaItem);
            videoPlayer.prepare();
            videoPlayer.play();

        } catch (Exception e) {
            Log.e("CaptionError", "setVideoWithCaption: ", e);
        }
    }

//    public void playVideo(boolean isEndOfList) {
//        if (!isEndOfList) {
//            int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstCompletelyVisibleItemPosition();
//            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastCompletelyVisibleItemPosition();
////            Timber.d("playVideo: %s, %s", startPosition, endPosition);
//            // if there is more than 2 list-items on the screen, set the difference to be 1
//            if (endPosition - startPosition > 1) {
//                endPosition = startPosition + 1;
//            }
//
//            // something is wrong. return.
//            if (startPosition < 0 || endPosition < 0) {
//                return;
//            }
//
//            // if there is more than 1 list-item on the screen
//            if (startPosition != endPosition) {
//                int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
//                int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);
//
//                targetPosition =
//                        startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
//            } else {
//                targetPosition = startPosition;
//            }
//        } else {
//            targetPosition = homepageResponseResult.size() - 1;
//        }
//
//        Timber.d("playVideo: %s, %s", playPosition, targetPosition);
//        // video is already playing so return
//        if (targetPosition == playPosition) {
//            return;
//        }
//
//        // set the position of the list-item that is to be played
//        playPosition = targetPosition;
//
//        /*if (playPosition >= 0) {
//            if (homepageResponseResult.get(targetPosition).getPlaytype().equals(Constants.TAG_VIDEO)) {
//                String userId = "";
//                if (GetSet.getUserId() != null) {
//                    userId = homepageResponseResult.get(targetPosition).getPublisherId();
//                    Timber.i("userId: %s", userId);
//                    if (TextUtils.isEmpty(userId)) {
//                        EventBus.getDefault().post(new homeForYouSwipePrevent(true, "")); // swipe disabled
//                    } else {
//                        EventBus.getDefault().post(new homeForYouSwipePrevent(false, "")); // swipe enabled
////                        listener.onProfileUpdate(new FollowingProfileUpdate(homepageResponseResult.get(playPosition).getPublisherId(), homepageResponseResult.get(playPosition).getPublisherImage(), userId));
//                    }
//                }
//            } else {
//                EventBus.getDefault().post(new homeForYouSwipePrevent(true, "Ad")); // swipe disabled
//            }
//        }*/
//
//        if (videoSurfaceView == null) {
//            return;
//        }
//
//        // remove any old surface views from previously playing videos
//        videoSurfaceView.setVisibility(INVISIBLE);
//        removeVideoView(videoSurfaceView);
//
//        int currentPosition = targetPosition - ((LinearLayoutManager) Objects.requireNonNull(getLayoutManager())).findFirstCompletelyVisibleItemPosition();
//
//        View child = getChildAt(currentPosition);
//
//        if (child == null) {
//            return;
//        }
//
//        HomeVideoAdapter.VideoViewHolder holder = (HomeVideoAdapter.VideoViewHolder) child.getTag();
//
//
//        if (holder == null) {
//            playPosition = -1;
//            return;
//        }
//        HomeResponse.Result data = homepageResponseResult.get(targetPosition);
//        viewHolderParent = holder.itemView;
//        mediaContainer = holder.mediaContainer;
//        mediaCoverImage = holder.video_thumbnail;
//        progressBar = holder.progressBar;
//        volumeControl = holder.video_play_pause;
//        progress_duration = holder.progress_duration;
//        videoSurfaceView.setPlayer(videoPlayer);
//        setupProgressBar();
//
//        // Get video dimensions to determine orientation
//        if (videoPlayer.getVideoFormat() != null) {
//            int videoWidth = videoPlayer.getVideoFormat().width;
//            int videoHeight = videoPlayer.getVideoFormat().height;
//
//            // Check if video is portrait (height > width)
//            if (videoHeight > videoWidth) {
//                // Portrait video - fill the screen
//                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
//            } else {
//                // Landscape video - play as it is with black bars if needed
//                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
//            }
//        } else {
//            // Fallback if video format is not available yet
//            if (Objects.equals(data.getVideoType(), "gallery")) {
//                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
//            } else {
//                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
//            }
//        }
//        String mediaUrl = null;
//        Uri hlsUri = null;
//        if (homepageResponseResult.get(targetPosition).getPlaytype().equals(Constants.TAG_VIDEO)) {
//            mediaUrl = homepageResponseResult.get(targetPosition).getPlaybackUrl();
//            if (mediaUrl != null) {
//                hlsUri = Uri.parse(mediaUrl);
//            }
//        } else {
//            hlsUri = Uri.parse(homepageResponseResult.get(targetPosition).getAndroidVideo());
//        }
    ////        hlsUri = Uri.parse("https://vodbucket.sfo3.digitaloceanspaces.com/video/videodata-1658843495443/videodata-1658843495443.m3u8");
    ////        hlsUri = Uri.parse("https://demo-fundoo.fra1.digitaloceanspaces.com/video/videodata-1660132305673/videodata-1660132305673.m3u8");
//        Timber.d("getMediaSource: %s", homepageResponseResult.get(targetPosition).getVideoId());
//        Timber.d("getMediaSource: %s", hlsUri);
//
//        /** Play from HlsMediaSource*/
//        MediaSource mediaSource;
//        if (("" + hlsUri).toLowerCase().endsWith(".m3u8")) {
//            mediaSource = ExoPlayerUtils.getHlsMediaSource(mContext, hlsUri);
//        } else {
//            mediaSource = ExoPlayerUtils.getMp4MediaSource(hlsUri);
//        }
//
//        videoPlayer.setMediaSource(mediaSource);
//        videoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
//        videoPlayer.prepare();
//        videoPlayer.setPlayWhenReady(true);
//    }

    public void playVideo(boolean isEndOfList) {
        // Don't interfere if we're currently resuming
        if (isResuming) {
            Timber.d("playVideo: skipping because resume in progress");
            return;
        }

        if (!isEndOfList) {
            int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastCompletelyVisibleItemPosition();

            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1;
            }

            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            if (startPosition != endPosition) {
                int startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition);
                int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);

                targetPosition = startPositionVideoHeight > endPositionVideoHeight ? startPosition : endPosition;
            } else {
                targetPosition = startPosition;
            }
        } else {
            targetPosition = homepageResponseResult.size() - 1;
        }

        Timber.d("playVideo: %s, %s", playPosition, targetPosition);

        // Video is already playing so return
        if (targetPosition == playPosition) {
            return;
        }

        // Set the position of the list-item that is to be played
        playPosition = targetPosition;

        if (videoSurfaceView == null) {
            return;
        }

        // Only hide surface if we're not currently resuming
        if (!isResuming) {
            videoSurfaceView.setVisibility(INVISIBLE);
            removeVideoView(videoSurfaceView);
        }

        int currentPosition = targetPosition - ((LinearLayoutManager) Objects.requireNonNull(getLayoutManager())).findFirstCompletelyVisibleItemPosition();
        View child = getChildAt(currentPosition);

        if (child == null) {
            return;
        }

        HomeVideoAdapter.VideoViewHolder holder = (HomeVideoAdapter.VideoViewHolder) child.getTag();

        if (holder == null) {
            playPosition = -1;
            return;
        }
        if (homepageResponseResult == null || homepageResponseResult.isEmpty()) {
            return;
        }

        if (targetPosition < 0 || targetPosition >= homepageResponseResult.size()) {
            return;
        }
        HomeResponse.Result data = homepageResponseResult.get(targetPosition);
        viewHolderParent = holder.itemView;
        mediaContainer = holder.mediaContainer;
        mediaCoverImage = holder.video_thumbnail;
        progressBar = holder.progressBar;
        volumeControl = holder.video_play_pause;
        progress_duration = holder.progress_duration;
        marqueeText = holder.marqueeText;
        txt_comment_count = holder.txt_comment_count;

        // Only rebind if not resuming (resumePlayer handles this)
        if (!isResuming) {
            videoSurfaceView.setPlayer(videoPlayer);
        }

        marqueeText.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Enlarge + highlight SeekBar
                    setSeekBarHeight(progress_duration, 15);
                    progress_duration.setThumb(
                            ContextCompat.getDrawable(mContext, R.drawable.custom_thumb_tracking)
                    );
                    progress_duration.setProgressDrawable(
                            ContextCompat.getDrawable(mContext, R.drawable.video_seekbar_tracking)
                    );
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Reset SeekBar back to thin style
                    setSeekBarHeight(progress_duration, 5);
                    progress_duration.setThumb(
                            ContextCompat.getDrawable(mContext, R.drawable.custom_thumb)
                    );
                    progress_duration.setProgressDrawable(
                            ContextCompat.getDrawable(mContext, R.drawable.video_seekbar)
                    );
                    break;
            }
            // Pass event to SeekBar too, so scrubbing works
            return progress_duration.dispatchTouchEvent(event);
        });

        setupProgressBar();

        // Set resize mode based on video format
        if (videoPlayer.getVideoFormat() != null) {
            int videoWidth = videoPlayer.getVideoFormat().width;
            int videoHeight = videoPlayer.getVideoFormat().height;

            if (videoHeight > videoWidth) {
                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            } else {
                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            }
        } else {
            if (Objects.equals(data.getVideoType(), "gallery")) {
                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            } else {
                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            }
        }

        String mediaUrl = null;
        Uri hlsUri = null;
        if (homepageResponseResult.get(targetPosition).getPlaytype().equals(Constants.TAG_VIDEO)) {
            mediaUrl = homepageResponseResult.get(targetPosition).getPlaybackUrl();
            if (mediaUrl != null) {
                hlsUri = Uri.parse(mediaUrl);
            }
        } else {
            hlsUri = Uri.parse(homepageResponseResult.get(targetPosition).getAndroidVideo());
        }

        Timber.d("getMediaSource: %s", homepageResponseResult.get(targetPosition).getVideoId());
        Timber.d("getMediaSource: %s", hlsUri);

        // Play from HlsMediaSource
        MediaSource mediaSource;
        if (("" + hlsUri).toLowerCase().endsWith(".m3u8")) {
            mediaSource = ExoPlayerUtils.getHlsMediaSource(mContext, hlsUri);
        } else {
            mediaSource = ExoPlayerUtils.getMp4MediaSource(hlsUri);
        }

        videoPlayer.setMediaSource(mediaSource);
        videoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);
        videoPlayer.prepare();
        videoPlayer.setPlayWhenReady(true);
    }
    public void resumePlayer() {
        Timber.d("VIDEO_DBG resumePlayer: called, isResuming=%s", isCommentDialogOpen);

        if (isCommentDialogOpen) {
            // Comment UI is open: DO NOT resume full-screen playback nor re-attach in full-screen mode.
            // Instead: keep the player in the 'shrunk' state. If you already have on-screen shrink code,
            // call it here; otherwise just return to avoid changing the player.
            // Example (if you have a shrink helper):
            // setVideoShrunkByPercentage(true, 0.45f, false);
            try {
                if (videoSurfaceView.getPlayer() != videoPlayer) {
                    videoSurfaceView.setPlayer(videoPlayer);
                }
                try { videoSurfaceView.setResizeMode(com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT); } catch (Throwable ignored) {}
                videoPlayer.setPlayWhenReady(true);
            } catch (Throwable ignored) {}
            return;
        }

        // Prevent multiple simultaneous resume attempts
        if (isResuming) {
            Timber.d("resumePlayer: already in progress, skipping");
            return;
        }



        if (videoPlayer == null || videoSurfaceView == null) {
            Timber.e("resumePlayer: videoPlayer or videoSurfaceView is NULL");
            return;
        }



        isResuming = true;

        try {
            if (playPosition != -1) {
                Timber.d("resumePlayer: rebinding at playPosition=%d", playPosition);

                // Re-find current visible child
                int currentPosition = playPosition - ((LinearLayoutManager) Objects.requireNonNull(getLayoutManager()))
                        .findFirstCompletelyVisibleItemPosition();
                View child = getChildAt(currentPosition);

                if (child != null) {
                    HomeVideoAdapter.VideoViewHolder holder = (HomeVideoAdapter.VideoViewHolder) child.getTag();
                    if (holder != null) {
                        // Update all references
                        mediaContainer = holder.mediaContainer;
                        mediaCoverImage = holder.video_thumbnail;
                        progressBar = holder.progressBar;
                        volumeControl = holder.video_play_pause;
                        progress_duration = holder.progress_duration;
                        viewHolderParent = holder.itemView;

                        Timber.d("resumePlayer: Found holder, mediaContainer null: %s", mediaContainer == null);

                        if (mediaContainer != null) {
                            // CRITICAL: First, make sure the surface view is properly detached
                            if (videoSurfaceView.getParent() != null) {
                                ((ViewGroup) videoSurfaceView.getParent()).removeView(videoSurfaceView);
                            }

                            // Clear the isVideoViewAdded flag to ensure proper re-adding
                            isVideoViewAdded = false;

                            // Ensure container is visible
                            mediaContainer.setVisibility(VISIBLE);

                            // Add surface view to container
                            FrameLayout.LayoutParams playerLp = new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    Gravity.TOP
                            );
                            videoSurfaceView.setLayoutParams(playerLp);

                            mediaContainer.addView(videoSurfaceView);
                            isVideoViewAdded = true;

                            // Set surface properties
                            videoSurfaceView.setVisibility(VISIBLE);
                            // videoSurfaceView.setAlpha(1f);
                            videoSurfaceView.requestFocus();
                            videoSurfaceView.bringToFront();

                            // Hide cover image immediately
                            if (mediaCoverImage != null) {
                                mediaCoverImage.setAlpha(0f);
                                Timber.d("resumePlayer: Cover image alpha set to 0");
                            }

                            // Reattach player to surface
                            videoSurfaceView.setPlayer(videoPlayer);

                            // Set resize mode
                            if (videoPlayer.getVideoFormat() != null) {
                                int videoWidth = videoPlayer.getVideoFormat().width;
                                int videoHeight = videoPlayer.getVideoFormat().height;
                                if (videoHeight > videoWidth) {
                                    videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                                } else {
                                    videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                                }
                                Timber.d("resumePlayer: Set resize mode for %dx%d video", videoWidth, videoHeight);
                            }

                            // Resume playback
                            videoPlayer.setPlayWhenReady(true);

                            Timber.d("resumePlayer: Successfully reattached surface view");

                            // IMPORTANT: Don't let other methods interfere with our setup
                            // Delay the reset of isResuming flag to prevent playVideo() from interfering
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Double-check that our surface is still properly attached
                                    if (videoSurfaceView != null && videoSurfaceView.getParent() == null) {
                                        Timber.w("resumePlayer: Surface got detached, re-adding");
                                        if (mediaContainer != null) {
                                            // Add surface view to container
                                            FrameLayout.LayoutParams playerLp = new FrameLayout.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    Gravity.TOP
                                            );
                                            videoSurfaceView.setLayoutParams(playerLp);

                                            mediaContainer.addView(videoSurfaceView);
                                            isVideoViewAdded = true;
                                            videoSurfaceView.setVisibility(VISIBLE);
                                        }
                                    }

                                    if (mediaCoverImage != null && mediaCoverImage.getAlpha() > 0f) {
                                        Timber.w("resumePlayer: Cover image still visible, forcing hide");
                                        mediaCoverImage.setAlpha(0f);
                                    }

                                    Timber.d("resumePlayer: Post-resume check complete");
                                }
                            }, 100); // Shorter delay for surface check

                        } else {
                            Timber.e("resumePlayer: mediaContainer is null");
                        }
                    } else {
                        Timber.e("resumePlayer: holder is null for playPosition=%d", playPosition);
                        // Don't call playVideo here as it might interfere
                    }
                } else {
                    Timber.e("resumePlayer: child view is null");
                    // Don't call playVideo here as it might interfere
                }
            } else {
                Timber.d("resumePlayer: no playPosition, letting normal flow handle it");
            }
        } finally {
            // Reset the flag after a longer delay to prevent interference from playVideo
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    isResuming = false;
                    Timber.d("resumePlayer: reset isResuming flag");
                }
            }, 1000); // Longer delay to prevent playVideo interference
        }
    }
    public void checkVideoVisibility() {
        Timber.d("=== VIDEO VISIBILITY CHECK ===");
        Timber.d("Surface visibility: %d, alpha: %f",
                videoSurfaceView != null ? videoSurfaceView.getVisibility() : -1,
                videoSurfaceView != null ? videoSurfaceView.getAlpha() : -1);
        Timber.d("Surface parent: %s",
                videoSurfaceView != null ? (videoSurfaceView.getParent() != null ? videoSurfaceView.getParent().toString() : "null") : "null");
        Timber.d("Cover image visibility: %d, alpha: %f",
                mediaCoverImage != null ? mediaCoverImage.getVisibility() : -1,
                mediaCoverImage != null ? mediaCoverImage.getAlpha() : -1);
        Timber.d("Media container visibility: %d, child count: %d",
                mediaContainer != null ? mediaContainer.getVisibility() : -1,
                mediaContainer != null ? mediaContainer.getChildCount() : -1);
        Timber.d("Player state: %d, playing: %b, ready: %b",
                videoPlayer != null ? videoPlayer.getPlaybackState() : -1,
                videoPlayer != null && videoPlayer.isPlaying(),
                videoPlayer != null && videoPlayer.getPlaybackState() == Player.STATE_READY);
        Timber.d("=== END CHECK ===");
    }
    private void setupProgressBar() {
        progress_duration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && videoPlayer != null && videoPlayer.getDuration() > 0) {
                    // Calculate seek position more accurately
                    long duration = videoPlayer.getDuration();
                    long seekPosition = (long) ((progress / 100.0) * duration);
                    seekBar.setProgressDrawable(getResources().getDrawable(R.drawable.video_seekbar_tracking));
                    seekBar.setThumb(getResources().getDrawable(R.drawable.custom_thumb_tracking));
                    // Format mm:ss
                    String current = String.format("%02d:%02d",
                            (seekPosition / 1000) / 60,
                            (seekPosition / 1000) % 60);

                    String total = String.format("%02d:%02d",
                            (duration / 1000) / 60,
                            (duration / 1000) % 60);

                    // Update indicator in format mm:ss / mm:ss
                    updateTrackIndicator(current + " / " + total);

                    // Seek video to the selected position while dragging
                    videoPlayer.seekTo(seekPosition);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
                // Pause automatic progress updates while user is seeking
                if (runnable != null) {
                    mHandler.removeCallbacks(runnable);
                    showTrackIndicator(true);
                }

                setSeekBarHeight(seekBar, 15);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Final seek when user stops tracking
                if (videoPlayer != null && videoPlayer.getDuration() > 0) {
                    long duration = videoPlayer.getDuration();
                    long seekPosition = (long) ((seekBar.getProgress() / 100.0) * duration);
                    videoPlayer.seekTo(seekPosition);
                }

                isUserSeeking = false;
                // Resume automatic progress updates after a short delay
                mHandler.postDelayed(() -> startProgressUpdates(), 200);
                seekBar.setProgressDrawable(getResources().getDrawable(R.drawable.video_seekbar));
                seekBar.setThumb(getResources().getDrawable(R.drawable.custom_thumb));
                showTrackIndicator(false);

                setSeekBarHeight(seekBar, 5);
            }
        });

        // When screen first loads, keep it at 1dp
        setSeekBarHeight(progress_duration, 1);

        // Start progress updates when player is ready
        videoPlayer.addListener(new Player.Listener() {

            @Override
            public void onRenderedFirstFrame() {
                // This is called when the first video frame is rendered to the surface.
                // It's the best place to hide the thumbnail/cover so user sees the video instantly.
                if (mediaCoverImage != null) {
                    mediaCoverImage.animate()
                            .alpha(0f)
                            .setDuration(150)
                            .withEndAction(() -> {
                                try {
                                    mediaCoverImage.setVisibility(View.GONE);
                                    //mediaCoverImage.setAlpha(0f);
                                } catch (Throwable ignored) {}
                            }).start();
                }

                // mark video state playing
                videoState = PlayState.ON;
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    startProgressUpdates();
                }
            }

            @Override
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
                // Update seekbar when video position changes (including seeks)
                if (!isUserSeeking && videoPlayer.getDuration() > 0) {
                    long duration = videoPlayer.getDuration();
                    long currentPosition = videoPlayer.getCurrentPosition();
                    int progress = (int) ((currentPosition * 100) / duration);
                    progress_duration.setProgress(progress);
                }
            }
        });
    }

    private void setSeekBarHeight(SeekBar seekBar, int dp) {
        ViewGroup.LayoutParams params = seekBar.getLayoutParams();
        params.height = (int) (dp * seekBar.getContext().getResources().getDisplayMetrics().density);
        seekBar.setLayoutParams(params);
    }

    private void startProgressUpdates() {
        if (runnable != null) {
            mHandler.removeCallbacks(runnable);
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                if (videoPlayer != null && !isUserSeeking) {
                    long duration = videoPlayer.getDuration();
                    long currentPosition = videoPlayer.getCurrentPosition();

                    if (duration > 0) {
                        int progress = (int) ((currentPosition * 100) / duration);
                        progress_duration.setProgress(progress);
                    }
                }
                mHandler.postDelayed(this, 100);
            }
        };
        mHandler.post(runnable);
    }

    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     */
    private int getVisibleVideoSurfaceHeight(int playPosition) {

        int at = playPosition - ((LinearLayoutManager) Objects.requireNonNull(getLayoutManager())).findFirstCompletelyVisibleItemPosition();

        View child = getChildAt(at);
        if (child == null) {
            return 0;
        }

        int[] location = new int[2];
        child.getLocationInWindow(location);

        if (location[1] < 0) {
            return location[1] + videoSurfaceDefaultHeight;
        } else {
            return screenDefaultHeight - location[1];
        }
    }

    // Remove the old player
    private void removeVideoView(StyledPlayerView videoView) {
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent == null) {
            return;
        }

        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeViewAt(index);
            isVideoViewAdded = false;
            viewHolderParent.setOnClickListener(null);
        }
    }

    private void addVideoView() {
        // Add surface view to container
        FrameLayout.LayoutParams playerLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.TOP
        );
        videoSurfaceView.setLayoutParams(playerLp);

        mediaContainer.addView(videoSurfaceView);
        isVideoViewAdded = true;
        videoSurfaceView.requestFocus();
        videoSurfaceView.setVisibility(VISIBLE);
        //  videoSurfaceView.setAlpha(1);
        videoSurfaceView.bringToFront();
    }

    private void resetVideoView() {
        if (isVideoViewAdded) {
            //  mediaCoverImage.setAlpha(1f);
            playPosition = -1;
            removeVideoView(videoSurfaceView);
            videoSurfaceView.setVisibility(INVISIBLE);
        }
    }

    /*public void releasePlayer() {

        if (videoPlayer != null) {
            videoSurfaceView = null;
            videoPlayer.release();
            videoPlayer = null;
        }

        viewHolderParent = null;
    }*/

    public void onPausePlayer() {
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.clearMediaItems();
        }
    }


    private void animatePlayControl() {

        if (volumeControl != null) volumeControl.setVisibility(VISIBLE);

        try {
            if (videoPlayer != null) {
                if (videoState == PlayState.OFF) {

                    if (volumeControl != null) {
                        volumeControl.setImageResource(R.drawable.video_play);
                        volumeControl.animate().cancel();
                        volumeControl.setAlpha(1f);
                    }


                } else if (videoState == PlayState.ON) {
                    if (
                            volumeControl != null) {
                        volumeControl.setImageResource(R.drawable.video_play);

                        volumeControl.animate().cancel();
                        volumeControl.setAlpha(1f);
                        volumeControl.animate().alpha(0f)
                                .setDuration(300).setStartDelay(100);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //public void onRestartPlayer() {
    //  if (videoPlayer != null) {
    //   playVideo(true);
    //  }
    //}

    private void setVolumeControl(VolumeState state) {
        volumeState = state;
        if (state == VolumeState.OFF) {
            videoPlayer.setVolume(0f);
        } else if (state == VolumeState.ON) {
            videoPlayer.setVolume(1f);
        }
    }


    public void setMediaObjects(ArrayList<HomeResponse.Result> mHomepageResponseResult) {
        this.homepageResponseResult = mHomepageResponseResult;
    }

    public void setListener(PlayerRecyclerListener listener) {
        this.listener = listener;
    }

    public void resetPlayingPosition() {
        playPosition = -1;
    }

    public void setExoPlayView(int visible) {
        if (videoSurfaceView != null && videoSurfaceView.getVideoSurfaceView() != null) {
            videoSurfaceView.getVideoSurfaceView().setVisibility(visible);
        }
    }

    /**
     * Volume ENUM
     */
    private enum VolumeState {
        ON, OFF
    }

    public enum PlayState {ON, OFF}


    /**
     * EventBus subscriber: receive sheet drag events and keep the video fully visible
     * in the space above the bottom sheet (no cropping), and restore when sheet closed.
     */

    private static final String TAG_VIDEO_ZOOM = "VideoZoomEvent";

   /* @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoZoomEvent(final VideoZoomEvent event) {
        try {

            if (mediaContainer == null || videoSurfaceView == null) {
                Log.d(TAG_VIDEO_ZOOM, "mediaContainer or videoSurfaceView is null");
                return;
            }

            logViewChain(mediaContainer);

            // ----------- STEP 1: ensure container uses full width -------------
            ViewGroup.LayoutParams lp = mediaContainer.getLayoutParams();
            if (lp == null) {
                lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
            }
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;

            final int available = Math.max(0, event.availableHeightPx);
            if (available > 0) {
                lp.height = available; // height above comment sheet
            } else {
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            mediaContainer.setLayoutParams(lp);

            // ----------- STEP 2: make sure player view is full-width + top-aligned ----------
            FrameLayout.LayoutParams playerLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    Gravity.TOP);
            videoSurfaceView.setLayoutParams(playerLp);

            // ----------- STEP 3: choose correct resize mode -------------
            // While sheet is open => fill width (TikTok-like). When closed => restore fill/fit.
            try {
                if (available > 0) {
                    videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
                } else {
                    if (videoPlayer != null && videoPlayer.getVideoFormat() != null) {
                        int vw = videoPlayer.getVideoFormat().width;
                        int vh = videoPlayer.getVideoFormat().height;
                        if (vh > vw) {
                            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                        } else {
                            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                        }
                    } else {
                        videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                    }
                }
            } catch (Throwable ignore) {}

            // ----------- STEP 4: compute scale and apply transform -------------
            final float t = Math.max(0f, Math.min(1f, event.currentZoomScale)); // normalized progress 0..1
            final float minScale = 0.55f; // smallest size (adjust between 0.5–0.6)
            final float targetScale = 1f - (1f - minScale) * t;

            // wait for layout if needed
            if (mediaContainer.getWidth() == 0 || mediaContainer.getHeight() == 0) {
                mediaContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        try {
                            mediaContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } catch (Throwable ignored) {}
                        applyTopPivotAndScale(mediaContainer, targetScale, available);
                    }
                });
            } else {
                applyTopPivotAndScale(mediaContainer, targetScale, available);
            }

        } catch (Throwable ex) {
            Log.e(TAG_VIDEO_ZOOM, "onVideoZoomEvent error", ex);
        }
    }
*/

    // enforcement helper to beat other resize-mode resets (resumePlayer etc)
    private final Handler videoZoomHandler = new Handler();
    private Runnable enforceResizeModeRunnable = null;


    // keep a field to remember original (full-screen) container height:
    private int originalContainerHeight = 0;
    private volatile boolean isCommentDialogOpen = false;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVideoZoomEvent(VideoZoomEvent event) {
        if (mediaContainer == null || videoSurfaceView == null) return;

        // immediate synchronous reset when sheet is fully closed (t == 0)
        if (event.currentZoomScale <= 0.001f) {
            // cancel any running viewPropertyAnimator to avoid races
            mediaContainer.animate().cancel();
            mediaContainer.clearAnimation();

            // reset transforms immediately
            mediaContainer.setScaleX(1f);
            mediaContainer.setScaleY(1f);
            mediaContainer.setTranslationY(0f);

            // restore layout params to full screen
            ViewGroup.LayoutParams lp = mediaContainer.getLayoutParams();
            if (lp != null) {
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                mediaContainer.setLayoutParams(lp);
            } else {
                mediaContainer.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
            }

            // reset player resize mode synchronously (no side black bars)
            try {
                //RESIZE_MODE_FIXED_WIDTH
                videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            } catch (Throwable ignored) {}

            // ensure parent doesn't clip (so no sudden cut)
            ViewParent parent = mediaContainer.getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) parent;
                vg.setClipChildren(false);
                vg.setClipToPadding(false);
            }

            return; // done — avoid running the rest of the handler
        }

        // --- rest of handler for partial open / sliding ---

        int available = Math.max(0, event.availableHeightPx);
        float t = event.currentZoomScale; // typically 0 (closed) → 1 (fully opened)

        // Capture original full height if not stored
        if (originalContainerHeight <= 0) {
            originalContainerHeight = mediaContainer.getHeight();
            if (originalContainerHeight <= 0) {
                originalContainerHeight = mediaContainer.getContext()
                        .getResources().getDisplayMetrics().heightPixels;
            }
        }

        int screenHeight = originalContainerHeight;

        // Compute scale dynamically
        float minScale = 0.6f;
        float scale = 1f - ((1f - minScale) * t); // 1 → 0.6 based on t

        // Smooth transition up slightly
        float translateY = -(screenHeight * 0.1f * t);

        // If comment is fully closed (safety) – this branch should not be reached due to top check,
        // but keep it as an extra guard.
        if (t <= 0.001f || available >= screenHeight - 2) {
            mediaContainer.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        ViewGroup.LayoutParams lp2 = mediaContainer.getLayoutParams();
                        lp2.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        mediaContainer.setLayoutParams(lp2);
                        try {
                            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                        } catch (Throwable ignored) {}
                    })
                    .start();
            return;
        }

        // If sheet partially open → keep full width to avoid side black bars.
        // Only move the video up slightly; do NOT shrink width.
        mediaContainer.setPivotX(mediaContainer.getWidth() / 2f);
        mediaContainer.setPivotY(0f);

        mediaContainer.animate()
                .scaleX(1f)                // keep full width
                .scaleY(1f)                // keep height scale; cropping handled by resize mode
                .translationY(translateY)  // slide upward a bit as sheet opens
                .setDuration(120)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        // Force player to fill width (crop vertically if needed) — no side black bars
        try {
            //RESIZE_MODE_FIXED_WIDTH
            videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        } catch (Throwable ignored) {}

        // Disable clipping so scaled view shows properly
        ViewParent parent = mediaContainer.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).setClipChildren(false);
            ((ViewGroup) parent).setClipToPadding(false);
        }
    }


    /**
     * Called by host fragment to inform the player view that the comment dialog is open/closed.
     * While open=true, the view should avoid re-attaching/resuming full-screen playback.
     */
    public void setCommentDialogOpen(final boolean open) {
        // ensure runs on main thread
        post(new Runnable() {
            @Override
            public void run() {
                Log.d("VIDEO_DBG", "COMMENT_OPEN onForYou -> isCommentDialog = " + open);

                isCommentDialogOpen = open;
                if(open){
                    if(mediaContainer !=null){
                        // mediaCoverImage.setVisibility(GONE);
                    }
                }
                // If dialog closed, give a small delay then allow resume (optional)
               /* if (!open) {
                    // small delay to let UI settle before resume
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                resumePlayer(); // your existing method — it should check the flag if needed
                            } catch (Throwable ignored) {}
                        }
                    }, 120);
                }
                else {
                    // when dialog opens, ensure resumePlayer() will not force full-screen
                    // If you keep resumePlayer guarding on isCommentDialogOpen, that's enough.
                    try {
                        // Optionally keep the player attached in small mode:
                        if (videoSurfaceView != null && videoPlayer != null) {
                            if (videoSurfaceView.getPlayer() != videoPlayer) {
                                videoSurfaceView.setPlayer(videoPlayer);
                            }
                            try { videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH); } catch (Throwable ignored) {}
                            videoPlayer.setPlayWhenReady(true); // keep playing if you want
                        }
                    } catch (Throwable ignored) {}
                }*/
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCommentDialogState(CommentDialogStateEvent ev) {
        Log.e(TAG,"VIDEO_DEBUG : "+ ev.getCommentCount() +"   test");
        txt_comment_count.setText(ev.getCommentCount() +"");
    }


// ======================================================================
// Helper: top-anchored scaling (TikTok-like)
// ======================================================================

    private void applyTopPivotAndScale(View mediaContainer, float scale, int availableHeightPx) {
        if (mediaContainer == null) return;
        int w = mediaContainer.getMeasuredWidth();
        int h = mediaContainer.getMeasuredHeight();

        if (w > 0) mediaContainer.setPivotX(w / 2f);
        mediaContainer.setPivotY(0f); // top pivot so it shrinks upward

        mediaContainer.setScaleX(scale);
        mediaContainer.setScaleY(scale);

        float scaledH = h * scale;
        float targetY = 0f;
        if (availableHeightPx > 0) {
            targetY = Math.max(0f, (availableHeightPx - scaledH) / 2f);
        }
        mediaContainer.setTranslationY(targetY);

        mediaContainer.setTranslationX(0f);
        mediaContainer.setX(0f);
    }


    private void logViewChain(View v) {
        Log.d("VIDEO_DBG", "-------- VIEW HIERARCHY --------");
        View cur = v;
        while (cur != null) {
            String idName;
            try {
                idName = cur.getId() == View.NO_ID ? "no-id" : cur.getResources().getResourceEntryName(cur.getId());
            } catch (Throwable e) { idName = "unknown-id"; }
            Log.d("VIDEO_DBG", "view=" + cur.getClass().getSimpleName()
                    + " id=" + idName
                    + " width=" + cur.getWidth() + " measuredW=" + cur.getMeasuredWidth()
                    + "  height=" + cur.getHeight() + " measuredH=" + cur.getMeasuredHeight()
                    + "  paddingL=" + cur.getPaddingLeft() + " R=" + cur.getPaddingRight());
            if (cur.getParent() instanceof View) cur = (View) cur.getParent();
            else break;
        }
    }
}