package com.app.alhawanajd.Video;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

//import com.arthenica.mobileffmpeg.ExecuteCallback;
//import com.arthenica.mobileffmpeg.FFmpeg;

import com.app.alhawanajd.AudioCategory;
import com.app.alhawanajd.BaseActivity;
import com.app.alhawanajd.home.PrivacyBottomSheetDialog;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.Session;
import com.arthenica.ffmpegkit.ReturnCode;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;

import com.hendraanggrian.appcompat.socialview.widget.SocialArrayAdapter;
import com.hendraanggrian.appcompat.socialview.widget.SocialAutoCompleteTextView;
import com.app.alhawanajd.R;
import com.app.external.keyboard.HeightProvider;
import com.app.alhawanajd.App;
import com.app.alhawanajd.BaseFragmentActivity;
import com.app.alhawanajd.DialogAudioCategory;
import com.app.alhawanajd.MainActivity;
import com.app.alhawanajd.Utility;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.ProgressRequestBody;
import com.app.helper.StorageUtils;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.GetSet;
import com.app.model.PostHashTagRes;
import com.app.model.VideoPrivacyModel;
import com.app.utils.AdminData;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class PostVideoActivity extends /*BaseFragmentActivity*/BaseActivity implements AdapterItemClickListener {

    private static final String TAG = PostVideoActivity.class.getSimpleName();
    private static final Logger log = LoggerFactory.getLogger(PostVideoActivity.class);

    private SlidrInterface slidrInterface;
    private ImageView uploadingImagePreview , btnAudioCategory , btnBack,arrowid;
    private TextView txtTitle, video_cmt_result, video_view_result, txtpercentage, txtPreview;
    private TextView tvSavedFilePath;
    private ShapeableImageView imagePreview;
    private SwitchMaterial save_switch;
    private SwitchMaterial btnExtractAudio;
    private CheckBox btnAuthorize;
    private TextView txtAuthorize;
    private Button btnPost;
    private SocialAutoCompleteTextView edtDescription;
    private TextView txtAudioCategory;
    private LinearLayout audioCategoryLay;
    private LinearLayout extractLay;
    private LinearLayout noAudioLay;

    private HeightProvider heightProvider;
    private int selectedCategoryPosition = -1;
    private String getVideoId, soundId = null, soundUrl = null, gettype, getHashTag, audioCategory = null;
    private Boolean isUploading = false, hasAudio = true, useAudio = false;

    public ArrayList<String> oldHashList = new ArrayList<>();
    public ArrayList<String> newHashList = new ArrayList<>();
    private final List<VideoPrivacyModel> privacyData = new ArrayList<>();
    private final List<VideoPrivacyModel> commentPrivacyData = new ArrayList<>();
    private ApiInterface apiInterface;

    private BottomSheetDialog reShootDialog, uploadingDialog;
    private ProgressBar uploadingProgress;
    private Bitmap thumbBitmap;

    List<PostHashTagRes> hashTagList = new ArrayList<>();
    String tagged_user_name = "";
    ArrayAdapter<PostHashTagRes.Result> hashTagAdapter;
    private int dropDownHeight = 200;
    private int displayHeight = 0;
    private String postViewMode = "public",postCmtMode = "public", inputVideoPath = null;
    private File inputVideoFile, thumbFile;
    private File outputFile;
    private Uri outputUri;
    private boolean isEdit = false;

    ProgressDialog pdLoading;  ////
    private Handler mainHandler = new Handler(Looper.myLooper());

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_video);
        findViews();
//        apiInterface = ApiClient.INSTANCE.getClient(this).create(ApiInterface.class);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        if (getIntent().hasExtra(Constants.TAG_EDIT)) {
            isEdit = getIntent().getBooleanExtra(Constants.TAG_EDIT, false);
        }
        Log.d(TAG, "onCreate: isEdit: "+isEdit);

        initView();
        setPrivacyData(getPublishMode(getIntent().getStringExtra(Constants.TAG_PUBLISH_MODE)));
//        setCommentPrivacyData(getCommentMode(getIntent().getStringExtra(Constants.TAG_COMMENT_MODE)));

        pdLoading = new ProgressDialog(PostVideoActivity.this, R.style.CameraAlertDialog);
        pdLoading.setCancelable(false);
        ValueAnimator processingAnimator = ValueAnimator.ofInt(0, 4);
        processingAnimator.setRepeatCount(Animation.INFINITE);
        processingAnimator.setDuration(1000L);
        processingAnimator.addUpdateListener(animation -> {
            // noinspection SetTextI18n

            pdLoading.setMessage("Processing" + getDots((Integer) processingAnimator.getAnimatedValue(), ""));
        });
        processingAnimator.start();
        pdLoading.setMessage(getResources().getString(R.string.processing));
        pdLoading.setOnShowListener(dialog -> processingAnimator.start());
        pdLoading.setOnDismissListener(dialog -> processingAnimator.cancel());

        getVideoId = getIntent().getStringExtra(Constants.TAG_VIDEO_ID);
        if (!isEdit) {
            inputVideoPath = getIntent().getStringExtra(Constants.TAG_VIDEO_PATH);
            soundId = getIntent().getStringExtra(Constants.TAG_SOUND_ID);
            soundUrl = getIntent().getStringExtra(Constants.TAG_SOUND_URL);
            if (!TextUtils.isEmpty(inputVideoPath)) inputVideoFile = new File(inputVideoPath);
            gettype = getIntent().getStringExtra(Constants.TAG_TYPE);
            getHashTag = getIntent().getStringExtra(Constants.TAG_SELECT_HASH_TAG);
            StorageUtils.getInstance(PostVideoActivity.this);
            hasAudio = StorageUtils.isVideoHaveAudioTrack(inputVideoFile.getAbsolutePath());
            Log.e(TAG,"Audio Category initialuy hasAudio: " + hasAudio);
            Log.e(TAG,"Audio Category initialuy soundId: " + soundId);

            if (!hasAudio) {
                noAudioLay.setVisibility(View.GONE);
                Log.e(TAG, "onClick: ::::::::::"+new Gson().toJson(AdminData.audioCategory) +"TEST");
                if (AdminData.audioCategory != null) {
                    for (AudioCategory category : AdminData.audioCategory) {
                        if (Constants.TAG_MUTED.equals(category.getName())) {
                            audioCategory = category.getId(); // or category.id if you want
                            Log.e(TAG, "Audio Category initially: " + audioCategory);
                            break;
                        }
                    }
                }
                Timber.d("onCreate: %s", audioCategory);
                //OCT 14
                // extractLay.setVisibility(View.GONE);
                extractLay.setVisibility(View.VISIBLE);
                btnExtractAudio.setChecked(false);
            } else if (TextUtils.isEmpty(soundId)) {
                extractLay.setVisibility(View.VISIBLE);
                btnExtractAudio.setChecked(false);
                audioCategoryLay.setVisibility(View.VISIBLE);
            } else {
                //OCT 14
                // extractLay.setVisibility(View.GONE);
                // audioCategoryLay.setVisibility(View.GONE);
                 extractLay.setVisibility(View.VISIBLE);
                 audioCategoryLay.setVisibility(View.VISIBLE);
                btnExtractAudio.setChecked(false);
            }
            if (getHashTag != null) {
                tagged_user_name = getHashTag;
                edtDescription.setText("#" + tagged_user_name);
            }
            if (TextUtils.isEmpty(edtDescription.getText())) {
                edtDescription.requestFocus();
            }
            pdLoading.show();
            Log.d(TAG, "onCreate: processing...");
            new Handler().postDelayed(() -> {
//            File file = StorageUtils.getInstance(PostVideoActivity.this).getTempFile(PostVideoActivity.this, Constants.TAG_VIDEO_TRIM + ".mp4");
                Log.d(TAG, "onCreate: inputVideoPath: "+ inputVideoPath);
                File f = new File(inputVideoPath);
                Log.d(TAG, "onCreate: file lenth: "+f.length());
                thumbBitmap = StorageUtils.getInstance(PostVideoActivity.this).getThumbnailFromVideo(f);
                thumbFile = StorageUtils.getInstance(getApplicationContext()).getTempFile(getApplicationContext(), Constants.TAG_VIDEO_THUMBNAIL + Utility.IMAGE_FORMAT);
                StorageUtils.getInstance(PostVideoActivity.this).saveThumbBitmap(thumbBitmap, thumbFile);

                Log.d(TAG, "onCreate: thumb: "+ thumbBitmap);
                if (thumbBitmap != null) {
                    imagePreview.setImageBitmap(thumbBitmap);
                    pdLoading.dismiss();
                }

            }, 500);
        } else {
            extractLay.setVisibility(View.VISIBLE);
            audioCategoryLay.setVisibility(View.VISIBLE);
            if (getIntent().hasExtra(Constants.TAG_AUDIOCATEGORY)) {
                audioCategory = getIntent().getStringExtra(Constants.TAG_AUDIOCATEGORY);
            }

            if (audioCategory != null)
                txtAudioCategory.setText(audioCategory);

            if (getIntent().hasExtra(Constants.TAG_USE_AUDIO)) {
                useAudio = getIntent().getBooleanExtra(Constants.TAG_USE_AUDIO, false);
                btnExtractAudio.setChecked(useAudio);
            }
            edtDescription.setText(getIntent().getStringExtra(Constants.TAG_DESCRIPTION));
            video_view_result.setText(getPublishMode(getIntent().getStringExtra(Constants.TAG_PUBLISH_MODE)));
//            video_cmt_result.setText(getCommentMode(getIntent().getStringExtra(Constants.TAG_COMMENT_MODE)));
            inputVideoPath = getIntent().getStringExtra(Constants.TAG_VIDEO_PATH);
            Glide.with(this)
                    .load(getIntent().getStringExtra(Constants.TAG_VIDEO_THUMBANIL))
                    .error(R.drawable.video_placeholder)
                    .into(imagePreview);
            String hashString = serialialize(edtDescription.getHashtags()).trim();
            if (hashString.contains(",")) {
                String[] arr = hashString.split(",");
                for (int i = 0; i < arr.length; i++) {
                    oldHashList.add(arr[i]);
                }
            } else {
                oldHashList.add(hashString);
            }
        }

        hashTagAdapter = new HashTagArrayAdapter(PostVideoActivity.this, R.layout.item_post_hashtag, R.id.hashTagName);
        edtDescription.setHashtagAdapter(hashTagAdapter);
        edtDescription.setDropDownAnchor(R.id.dropDownAnchor);
        edtDescription.setDropDownVerticalOffset(AppUtils.dpToPx(PostVideoActivity.this, 35));
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        edtDescription.setThreshold(0);


        txtTitle.setText(getResources().getString(R.string.post));

        imagePreview.setOnClickListener(v -> {
            //if (!isEdit ) {
            Intent intent = new Intent(this, VideoPreviewActivity.class);
            intent.putExtra(Constants.TAG_VIDEO_PATH, inputVideoPath);
            intent.putExtra(Constants.TAG_EDIT, isEdit);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            overridePendingTransition(R.anim.anim_zoom_out, R.anim.anim_zoom_in);
            //}
        });

        save_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 4);
                valueAnimator.setRepeatCount(Animation.INFINITE);
                valueAnimator.setDuration(1000L);
                valueAnimator.addUpdateListener(animation -> {
                    // noinspection SetTextI18n
                    tvSavedFilePath.setText("Saving" + getDots((Integer) valueAnimator.getAnimatedValue(), ""));
                });
                valueAnimator.start();

                String outFileName = new SimpleDateFormat("'Post_'yyyy-MM-dd-HH-mm'.mp4'").format(new Date());
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    outputUri = StorageUtils.getInstance(PostVideoActivity.this).saveFileInStorageV10(inputVideoFile, outFileName, Constants.TAG_VIDEO);
                } else {
                    outputFile = StorageUtils.getInstance(PostVideoActivity.this).saveFileInStorage(inputVideoFile, Constants.TAG_VIDEO, Constants.TAG_VIDEO);
                }
                tvSavedFilePath.setText(String.format(getString(R.string.saved_to_gallery_filename), Environment.DIRECTORY_MOVIES + File.separatorChar + StorageUtils.getInstance(this).getFolderPath(Constants.TAG_VIDEO) + outFileName));
                tvSavedFilePath.setVisibility(View.VISIBLE);
                Toasty.success(this, R.string.video_saved_to_gallery).show();
                valueAnimator.cancel();
            } else {
                tvSavedFilePath.setText(null);
                tvSavedFilePath.setVisibility(View.GONE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && outputUri != null) {
                    ContentResolver contentResolver = getContentResolver();
                    /* For Android 11 (R) and above, use this snippet */
                    PendingIntent trashPendingIntent = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        trashPendingIntent = MediaStore.createTrashRequest(contentResolver, Collections.singleton(outputUri), true);
                        try {
                            startIntentSenderForResult(trashPendingIntent.getIntentSender(), 120, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            Timber.e("Save to gallery: failed deleting %s", outputUri.getPath());
                        }
                    } else {
                        if (contentResolver.delete(outputUri, null, null) > 0) {
                            Timber.i("Save to gallery: deleted %s", outputUri.getPath());
                        }
                    }
                } else if (outputFile != null && outputFile.exists()) {
                    outputFile.delete();
                }
            }
        });

        btnExtractAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    useAudio = true;
                } else {
                    useAudio = false;
                }
            }
        });

        audioCategoryLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: ::::::::::"+new Gson().toJson(AdminData.audioCategory) +"TEST");
                if (!isEdit) {
                    DialogAudioCategory dialogAudioCategory = new DialogAudioCategory(PostVideoActivity.this, selectedCategoryPosition);
                    dialogAudioCategory.openDialog(audioCategory, PostVideoActivity.this);
                }
             /*   DialogAudioCategory dialogAudioCategory = new DialogAudioCategory(PostVideoActivity.this, selectedCategoryPosition);
                dialogAudioCategory.openDialog(audioCategory, PostVideoActivity.this);
*/
            }
        });

        btnPost.setOnClickListener(v -> {
            Log.e(TAG,"BTNPOST ");
            if (NetworkReceiver.isConnected()) {
                setPostButton(false);
                App.hideKeyboardActivity(this);
                Log.e(TAG,"BTNPOST getVideoId :  " + getVideoId +"test");
                if (!TextUtils.isEmpty(getVideoId)) {
                    if (TextUtils.isEmpty(edtDescription.getText())) {
                        edtDescription.setError(getResources().getString(R.string.txt_warning));
                        setPostButton(true);
                    } else if (audioCategoryLay.getVisibility() == View.VISIBLE && audioCategory == null) {
                        txtAudioCategory.setError(getResources().getString(R.string.select_audio_category));
                        setPostButton(true);
                    } else {
                        /*if (!btnAuthorize.isChecked()) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.accept_author), Toast.LENGTH_LONG).show();
                            setPostButton(true);
                        } else*/
                        Log.e(TAG,"BTNPOST getVideoId :  " + getVideoId +"first else " +isEdit );
                        if (!isEdit) {
                            if (Constants.WATERMARK_ENABLED) {
                                addWaterMark(inputVideoFile);
                            } else {
                                Log.e(TAG,"BTNPOST upload :  " + getVideoId +"second else");
                                upload(edtDescription.getText().toString());
                            }
                        } else {
                            Log.e(TAG,"BTNPOST upload :  " + getVideoId +"updateVideo");
                            updateVideo();
                        }
                    }
                } else {
                    Log.e(TAG,"BTNPOST not got videoid :  ");

                    if (!btnAuthorize.isChecked()) {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.accept_author), Toast.LENGTH_LONG).show();
                        setPostButton(true);
                    } else {
                        Log.e(TAG,"BTNPOST not got videoid :  ");
                        getStreamName("upload");
                    }

                }
            } else {
                Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
            }
        });

       /* findViewById(R.id.who_can).setOnClickListener(v -> {
            Intent intent = new Intent(this, DynamicPrivacyActivity.class);
            intent.putExtra("data", (Serializable) privacyData);
            intent.putExtra(Constants.TAG_TITLE, getResources().getString(R.string.who_can_view));
            startActivityForResult(intent, Constants.VIDEO_PRIVACY);
            overridePendingTransition(R.anim.anim_zoom_out, R.anim.anim_zoom_in);

        });*/
        findViewById(R.id.who_can).setOnClickListener(v -> {
            PrivacyBottomSheetDialog dialog = new PrivacyBottomSheetDialog(this, privacyData, selectedModel -> {
                // Handle selected option
//                Toast.makeText(this, "Selected: " + selectedModel.getPrivacy(), Toast.LENGTH_SHORT).show();
                video_view_result.setText(selectedModel.getPrivacy());
            });
            dialog.show();
        });
     /*   findViewById(R.id.who_can_cmt).setOnClickListener(v -> {
            Intent intent = new Intent(this, DynamicPrivacyActivity.class);
            intent.putExtra("data", (Serializable) commentPrivacyData);
            intent.putExtra(Constants.TAG_TITLE, getResources().getString(R.string.who_can_cmt));
            startActivityForResult(intent, Constants.VIDEO_CMT_PRIVACY);
            overridePendingTransition(R.anim.anim_zoom_out, R.anim.anim_zoom_in);

        });*/


        //   findViewById(R.id.btnBack).setOnClickListener(v -> openReshootDialog());


        TextWatcher textWatcher = new TextWatcher() {

            boolean valid = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String text = s.toString();
                //beforeTextChanged = text;
                if (count == 0 && text.isEmpty()) {
                  /*  liveUser.clear();
                    commentUserAdapter.notifyDataSetChanged();
                    userRecyclerView.setVisibility(View.GONE);*/
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String abc = s.toString();

                final int index = abc.lastIndexOf("#");
                if (index >= 0) {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>=250){
                    Toast.makeText(PostVideoActivity.this, getString(R.string.reachlimt), Toast.LENGTH_SHORT).show();
                }
                String txt = s.toString();

                final int index = txt.lastIndexOf("#");
                final int lastIndex;

                if (txt.contains("#") && !txt.endsWith(" "))
                    if (index >= 0) {
                        String query = txt.substring(index + 1);
                        lastIndex = query.lastIndexOf(" ");
                        if (lastIndex == -1) {
                            Timber.d("afterTextChanged split: %s", query);
                            getHashTag(query);
                        } else {
                            //  hashTagRec.setVisibility(View.GONE);
                        }
                    }
            }
        };

        edtDescription.addTextChangedListener(textWatcher);

        edtDescription.setHashtagTextChangedListener((view, text) -> {

            Timber.d("setHashtagTextChangedListener: %s", text);

            getHashTag(String.valueOf(text));

        });

        edtDescription.setOnHashtagClickListener((view, text) -> {

            Timber.d("setOnHashtagClickListener: %s", text);

        });


    }

    private void findViews() {
        txtTitle = findViewById(R.id.txtTitle);
        imagePreview = findViewById(R.id.imagePreview);
        txtPreview = findViewById(R.id.txtPreview);
        save_switch = findViewById(R.id.save_switch);
        btnAuthorize = findViewById(R.id.btnAuthorize);
        txtAuthorize = findViewById(R.id.txtAuthorize);
        extractLay = findViewById(R.id.extractLay);
        btnExtractAudio = findViewById(R.id.btnExtractAudio);
        edtDescription = findViewById(R.id.inputDescriptionTxt);
        txtAudioCategory = findViewById(R.id.txtAudioCategory);
        audioCategoryLay = findViewById(R.id.audioCategoryLay);
        noAudioLay = findViewById(R.id.noAudioLay);
        video_view_result = findViewById(R.id.video_view_result);
//        video_cmt_result = findViewById(R.id.video_cmt_result);
        tvSavedFilePath = findViewById(R.id.tv_savedFilePath);
        btnPost = findViewById(R.id.btnPost);
        btnAudioCategory = findViewById(R.id.btnAudioCategory);
        arrowid = findViewById(R.id.arrowid);
        btnBack = findViewById(R.id.btnBack);
        if (LocaleManager.isRTL()) {
            btnAudioCategory.setRotation(180);
            btnBack.setRotation(180);
            arrowid.setRotation(180);
        } else {
            btnAudioCategory.setRotation(0);
            btnBack.setRotation(0);
            arrowid.setRotation(0);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openReshootDialog();
            }
        });
    }

    private void initView() {
        displayHeight = AppUtils.getScreenHeight(this);
//        setKeyBoardListener();
        if (isEdit) {
            txtPreview.setVisibility(View.GONE);
        } else {
            txtPreview.setVisibility(View.VISIBLE);
        }
        String completeString = getString(R.string.authorize_content);
        String authorized = getString(R.string.authorized);
        int startIndex = completeString.indexOf(authorized);
        int endIndex = startIndex + authorized.length();
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(completeString);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                showAuthorizeDialog();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(PostVideoActivity.this, R.color.colorPrimary));
            }
        };
        spannableStringBuilder.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtAuthorize.setText(spannableStringBuilder);
        txtAuthorize.setMovementMethod(LinkMovementMethod.getInstance());
        txtAuthorize.setHighlightColor(Color.TRANSPARENT);
        btnAuthorize.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //setPostButton(b);
            }
        });
    }

    private void setKeyBoardListener() {
        heightProvider = new HeightProvider(this);

        heightProvider.init().setHeightListener(new HeightProvider.HeightListener() {
            @Override
            public void onHeightChanged(int height) {
                Log.d(TAG, "onHeightChanged: " + height);
                if (height > 0) {
                    dropDownHeight = displayHeight - height;
//                    edtDescription.setDropDownHeight(dropDownHeight);
                } else {
//                    edtDescription.setDropDownHeight(WRAP_CONTENT);
                    dropDownHeight = 0;
                }
            }
        });
    }

    private void setPrivacyData(String publishMode) {
        if (TextUtils.isEmpty(publishMode)) {
            privacyData.add(0, new VideoPrivacyModel(getString(R.string.every_one), true));
            privacyData.add(1, new VideoPrivacyModel(getString(R.string.fans), false));
            privacyData.add(2, new VideoPrivacyModel(getString(R.string.only_me), false));
        } else if (publishMode.equals(getString(R.string.every_one))) {
            privacyData.add(0, new VideoPrivacyModel(getString(R.string.every_one), true));
            privacyData.add(1, new VideoPrivacyModel(getString(R.string.fans), false));
            privacyData.add(2, new VideoPrivacyModel(getString(R.string.only_me), false));
        } else if (publishMode.equals(getString(R.string.fans))) {
            privacyData.add(0, new VideoPrivacyModel(getString(R.string.every_one), false));
            privacyData.add(1, new VideoPrivacyModel(getString(R.string.fans), true));
            privacyData.add(2, new VideoPrivacyModel(getString(R.string.only_me), false));
        } else {
            privacyData.add(0, new VideoPrivacyModel(getString(R.string.every_one), false));
            privacyData.add(1, new VideoPrivacyModel(getString(R.string.fans), false));
            privacyData.add(2, new VideoPrivacyModel(getString(R.string.only_me), true));
        }
    }
    private void setCommentPrivacyData(String publishMode) {
        if (TextUtils.isEmpty(publishMode)) {
            commentPrivacyData.add(0, new VideoPrivacyModel(getString(R.string.every_one), true));
            commentPrivacyData.add(1, new VideoPrivacyModel(getString(R.string.fans), false));
            commentPrivacyData.add(2, new VideoPrivacyModel(getString(R.string.only_me), false));
        } else if (publishMode.equals(getString(R.string.every_one))) {
            commentPrivacyData.add(0, new VideoPrivacyModel(getString(R.string.every_one), true));
            commentPrivacyData.add(1, new VideoPrivacyModel(getString(R.string.fans), false));
            commentPrivacyData.add(2, new VideoPrivacyModel(getString(R.string.only_me), false));
        } else if (publishMode.equals(getString(R.string.fans))) {
            commentPrivacyData.add(0, new VideoPrivacyModel(getString(R.string.every_one), false));
            commentPrivacyData.add(1, new VideoPrivacyModel(getString(R.string.fans), true));
            commentPrivacyData.add(2, new VideoPrivacyModel(getString(R.string.only_me), false));
        } else {
            commentPrivacyData.add(0, new VideoPrivacyModel(getString(R.string.every_one), false));
            commentPrivacyData.add(1, new VideoPrivacyModel(getString(R.string.fans), false));
            commentPrivacyData.add(2, new VideoPrivacyModel(getString(R.string.only_me), true));
        }
    }

    private void showAuthorizeDialog() {
        final Dialog dialog = new Dialog(PostVideoActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title = dialog.findViewById(R.id.title);
        TextView txtDescription = dialog.findViewById(R.id.txtDescription);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);
        yes.setText(getString(R.string.confirm));
        title.setText(getString(R.string.confirm_ownership));
        txtDescription.setVisibility(View.VISIBLE);
        no.setVisibility(View.GONE);

        if (LocaleManager.isRTL()) {
            txtDescription.setText(getString(R.string.auth_text));
        }else{
            txtDescription.setText(AdminData.postAuthorizeMessage);
        }


        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void setPostButton(boolean enable) {
        btnPost.setEnabled(enable);
    }

    private void getStreamName(String uploading) {
        if (NetworkReceiver.isConnected()) {
            Call<HashMap<String, String>> call = apiInterface.getStreamName(GetSet.getUserId());
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<HashMap<String, String>> call, @NotNull Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        Timber.tag(TAG).d("getStreamName: %s", App.getGsonPrettyInstance().toJson(response.body()));
                        HashMap<String, String> map = response.body();
                       // if (Objects.requireNonNull(Objects.requireNonNull(map).get(Constants.TAG_STATUS)).equals(Constants.TAG_TRUE)) {
                        if (Constants.TAG_TRUE.equals(map.get(Constants.TAG_STATUS))) {
                            getVideoId = map.get("video_id");

                            // Sometimes bitmap null so implement manuval delay
                            Log.e(TAG,"uploading : " + uploading +"," +getVideoId) ;
                            if (uploading.equalsIgnoreCase("upload")) {
                                if (TextUtils.isEmpty(edtDescription.getText())) {
                                    setPostButton(true);
                                    edtDescription.setError(getResources().getString(R.string.txt_warning));
                                } else if (extractLay.getVisibility() == View.VISIBLE && audioCategoryLay.getVisibility() == View.VISIBLE && audioCategory == null) {
                                    Log.e(TAG,"uploading audioCategoryLay: " + audioCategoryLay +"") ;
                                    setPostButton(true);
                                    txtAudioCategory.setError(getResources().getString(R.string.select_audio_category));
                                } else {
                                    Log.e(TAG,"addWaterMark : " + Constants.WATERMARK_ENABLED);
                                    if (Constants.WATERMARK_ENABLED) {
                                        addWaterMark(inputVideoFile);
                                    } else {
                                        Log.e(TAG,"upload function calling : " );
                                        upload(edtDescription.getText().toString());
                                    }
                                }
                            } else {
                                setPostButton(true);
                                //upload(inputDescriptionTxt.getText().toString());
                                pdLoading.dismiss();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    Timber.tag(TAG).e("getStreamName: %s", t.getMessage());
                    setPostButton(true);
                }
            });
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
            pdLoading.dismiss();
            setPostButton(true);
        }
    }

    /*public void copyFile(File source, File destination) throws IOException {
        try {
            FileUtils.copy(new FileInputStream(source), new FileOutputStream(destination));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*private void copyFile(String inputPath, String inputFileName, String outputPath) {

        InputStream in;
        OutputStream out;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + "/" + inputFileName);
            out = new FileOutputStream(outputPath + "/" + inputFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file (You have now copied the file)

            Timber.d("copyFile: %s", outputPath + "/" + inputFileName);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage() != null) {
                Timber.d("ex: %s", e.getMessage());
            }
        }
    }*/


    private String serialialize(List<String> hashtags) {
//        Timber.d("getHashTags: %s", new Gson().toJson(hashtags));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < hashtags.size(); i++) {
            builder.append(hashtags.get(i));
            if (i < hashtags.size() - 1) builder.append(",");
        }
        Timber.d("serialialize: %s", builder.toString());
        return builder.toString();
    }


    public void getHashTag(String tag) {
        if (NetworkReceiver.isConnected()) {

            tag = tag.replaceAll("#", "");

            Map<String, String> map = new HashMap<>();
            map.put("user_id", GetSet.getUserId());
            map.put("type", Constants.TAG_SEARCH);
            map.put("search_key", tag);

            Timber.d("getHashTag: %s", App.getGsonPrettyInstance().toJson(map));
            Call<PostHashTagRes> call3 = apiInterface.getPostHashTags(map);

            call3.enqueue(new Callback<PostHashTagRes>() {
                @Override
                public void onResponse(@NotNull Call<PostHashTagRes> call, @NotNull Response<PostHashTagRes> response) {
                    try {
                        PostHashTagRes data = response.body();
                        if (data.getStatus().equals(Constants.TAG_TRUE)) {

                            Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(data.getResult()));

                            if (data.getStatus().contains(Constants.TAG_TRUE)) {
                                // hashTagRec.setVisibility(View.VISIBLE);
                                /*hashTagList.clear();
                                hashTagList.addAll(data.getResult());
                                hashTagAdapter.notifyDataSetChanged();*/
                                //arrayAdapter.notifyDataSetChanged();

                                //  ArrayAdapter<PostHashTagRes.Result> hashtagAdapter = new HashtagArrayAdapter(PostVideoActivity.this);

                                hashTagAdapter.clear();

                                hashTagAdapter.addAll(data.getResult());
                                if (data.getResult().size() == 1) {
                                    edtDescription.setDropDownHeight(100);
                                } else if (data.getResult().size() == 2) {
                                    edtDescription.setDropDownHeight(200);
                                } else if (data.getResult().size() == 3) {
                                    edtDescription.setDropDownHeight(300);
                                } else if (data.getResult().size() == 4) {
                                    edtDescription.setDropDownHeight(400);
                                } else {
                                    edtDescription.setDropDownHeight(500);
                                }
                                hashTagAdapter.notifyDataSetChanged();
                            }

                            //HashTagResponse data = response.body();
                            /* if (data.getStatus().equals("true")) {
                             *//*nullLay.setVisibility(View.GONE);
                                hashTagList.addAll(data.getResult().getVideos());*//*
                            }*/

                            /*if (hashTagList.size() == 0) {
                                nullText.setText(R.string.no_hashtags);
                                nullLay.setVisibility(View.VISIBLE);
                            }

                            relatedDataAdapter.notifyDataSetChanged();

                            if (mSwipeRefreshLayout.isRefreshing()) {
                                relatedDataAdapter.showLoading(false);
                                swipeRefresh(false);
                                isLoading = true;
                            } else {
                                relatedDataAdapter.showLoading(false);
                                relatedDataAdapter.notifyDataSetChanged();
                            }*/
                        } else {
                            // hashTagRec.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Timber.d("onResponse:error %s", e.getMessage());
                        /*hashTagList.clear();
                        recyclerView.stopScroll();
                        relatedDataAdapter.notifyDataSetChanged();*/
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(Call<PostHashTagRes> call, Throwable t) {
                    /*if (currentPage != 0)
                        currentPage--;

                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        relatedDataAdapter.showLoading(false);
                    } else {
                        if (hashTagList.size() == 0) {
                            nullText.setVisibility(View.VISIBLE);
                            nullText.setText(getString(R.string.something_went_wrong));
                        }
                        swipeRefresh(false);
                    }*/
                    call.cancel();
                }
            });
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void openReshootDialog() {

        View reShoot_sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_reshoot, null);
        reShootDialog = new BottomSheetDialog(this, R.style.reshootBottomDialog); // Style here
        reShootDialog.setContentView(reShoot_sheetView);
        reShootDialog.getBehavior().setHideable(false);

        TextView textView = reShoot_sheetView.findViewById(R.id.title);
        TextView txtReshoot = reShoot_sheetView.findViewById(R.id.reshoot);

        textView.setText(R.string.alert_close_this_video);

        if (isEdit) {
            txtReshoot.setVisibility(View.GONE);
        }
        txtReshoot.setOnClickListener(v -> {
            reShootDialog.dismiss();
            Intent intent = new Intent();
            if (!TextUtils.isEmpty(soundId)) {
                intent.putExtra(Constants.TAG_SOUND_ID, soundId);
            }
            if (!TextUtils.isEmpty(soundUrl)) {
                intent.putExtra(Constants.TAG_SOUND_URL, soundUrl);
            }
            intent.putExtra("reshoot", true);
            setResult(RESULT_CANCELED, intent);
            finish();
        });

        reShoot_sheetView.findViewById(R.id.txtcancel).setOnClickListener(v -> reShootDialog.dismiss());

        reShoot_sheetView.findViewById(R.id.exit).setOnClickListener(v -> {
            reShootDialog.dismiss();
            Intent intent = new Intent();
            intent.putExtra("reshoot", false);
            setResult(RESULT_CANCELED, intent);
            finish();
        });

        if (reShootDialog != null) reShootDialog.show();
    }

    private void addWaterMark(File inputVideoFile) {
        final File outputFile = StorageUtils.getInstance(this).getTempFile(this, System.currentTimeMillis() + Utility.VIDEO_FORMAT);
        List<String> command = new ArrayList<>();
        command.add("-i");
        command.add(inputVideoFile.getAbsolutePath());
        String watermarkUrl = "https://products.hitasoft.in/fundooUpdate/public/video/convert/30_1660805034text_logo.png";
        command.add("-i");
        command.add(watermarkUrl);
        command.add("-filter_complex");
        command.add("[1][0]scale2ref=w=oh*mdar:h=ih*0.07[logo][video];[logo]format=argb,colorchannelmixer=aa=0.4[logo transparent];[video][logo transparent]overlay=main_w-overlay_w-5:main_h-overlay_h-5:format=auto,format=yuv420p");
        command.add("-async");
        command.add("1");
        command.add("-y");
        command.add(outputFile.getAbsolutePath());
        Timber.tag(TAG).d("addWatermark: %s", Arrays.toString(command.toArray()));

        String[] commandArray = command.toArray(new String[0]);
        FFmpegKit.executeAsync(String.join(" ", commandArray), session -> {
            final int returnCode = session.getReturnCode().getValue();
            Timber.tag(TAG).d("addWatermark: %s, %s", session.getSessionId(), returnCode);

            if (ReturnCode.isSuccess(session.getReturnCode())) {
                // Execution was successful
                new Handler(Looper.getMainLooper()).post(() ->
                        upload(edtDescription.getText().toString())
                );
            } else {
                // Execution failed
                Timber.tag(TAG).e("FFmpeg execution failed with state %s and returnCode %s.",
                        session.getState(), returnCode);
            }
        });
    }


    private void upload(String des) {

        if (NetworkReceiver.isConnected()) {
            Log.e(TAG,"upload function inputVideoFile  : "  +inputVideoFile +"test");
            if (inputVideoFile == null) {
                setPostButton(true);
                return;
            }

            View reShoot_sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_uploading_progress, null);
            uploadingDialog = new BottomSheetDialog(this, R.style.reshootBottomDialog); // Style here
            uploadingDialog.getBehavior().setHideable(false);
            uploadingDialog.setContentView(reShoot_sheetView);

            txtpercentage = reShoot_sheetView.findViewById(R.id.percentage);
            uploadingProgress = reShoot_sheetView.findViewById(R.id.progress_bar);
            uploadingImagePreview = reShoot_sheetView.findViewById(R.id.uploadingImagePreview);

            uploadingImagePreview.setImageBitmap(thumbBitmap);
            uploadingProgress.setProgress(0);
            uploadingProgress.setMax(100);
            txtpercentage.setText("0");

            uploadingDialog.show();

            uploadingDialog.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            });

            ProgressRequestBody.UploadCallbacks uploadCallbacks =
                    new ProgressRequestBody.UploadCallbacks() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onProgressUpdate(int percentage) {
                            isUploading = true;
                            uploadingProgress.setProgress(percentage + 1);
                            txtpercentage.setText((percentage + 1) + " %");

                            Timber.tag(TAG).d("uploadVideoProgress: %s", percentage);
                        }

                        @Override
                        public void onError(String message) {
                            Timber.d("onError: %s", message);
                        }

                        @Override
                        public void onFinish() {


                        }
                    };

            postViewMode = getApiPublishMode(video_view_result.getText().toString());
//            postCmtMode = getApiPublishMode(video_cmt_result.getText().toString());
            ProgressRequestBody fileBody = new ProgressRequestBody(inputVideoFile, "video.mp4",
                    uploadCallbacks);

            MultipartBody.Part filePart =
                    MultipartBody.Part.createFormData("videodata", Constants.TAG_MIX_VIDEO + ".mp4", fileBody);

            RequestBody userId = RequestBody.create(GetSet.getUserId(), MediaType.parse("text/plain"));
            RequestBody devicetype = RequestBody.create("1", MediaType.parse("text/plain"));
            RequestBody video_id = RequestBody.create(getVideoId, MediaType.parse("text/plain"));
            RequestBody description = RequestBody.create(des, MediaType.parse("text/plain"));
            RequestBody type = RequestBody.create(gettype, MediaType.parse("text/plain"));
            RequestBody mode = RequestBody.create(postViewMode, MediaType.parse("text/plain"));
//            RequestBody cmt_mode = RequestBody.create(postCmtMode, MediaType.parse("text/plain"));
            RequestBody useAudioBody = RequestBody.create("" + useAudio, MediaType.parse("text/plain"));
            RequestBody audioCategoryBody = null;
            if (audioCategory != null && !TextUtils.isEmpty(audioCategory)) {
                audioCategoryBody = RequestBody.create(audioCategory, MediaType.parse("text/plain"));
            }

            Map<String, RequestBody> partMap = new HashMap<String, RequestBody>();
            partMap.put("description", description);
            partMap.put("user_id", userId);
            partMap.put("video_id", video_id);
            partMap.put("devicetype", devicetype);
            partMap.put("type", type);
            partMap.put("mode", mode);
//            partMap.put("comment_mode", cmt_mode);
            partMap.put("use_audio", useAudioBody);
            if (audioCategoryBody != null) {
                partMap.put("audio_category", audioCategoryBody);
            }

            String hashString = serialialize(edtDescription.getHashtags()).trim();
            if (!TextUtils.isEmpty(hashString)) {
                RequestBody hashtags = RequestBody.create(hashString, MediaType.parse("text/plain"));
                partMap.put("hashtags", hashtags);
            }
            if (!TextUtils.isEmpty(soundId)) {
                RequestBody sound_id = RequestBody.create(soundId, MediaType.parse("text/plain"));
                partMap.put("sound_id", sound_id);
                Timber.tag(TAG).d("soundId: %s ", soundId);
            }
            Timber.tag(TAG).d("description: %s ", des);
            Timber.tag(TAG).d("userId %s ", GetSet.getUserId());
            Timber.tag(TAG).d("videoId: %s ", getVideoId);
            Timber.tag(TAG).d("deviceType: %s ", "1");
            Timber.tag(TAG).d("type: %s ", gettype);
            Timber.tag(TAG).d("mode: %s ", postViewMode);
            Timber.tag(TAG).d("useAudio: %s ", useAudio);
            Timber.tag(TAG).d("hashString: %s", hashString);
            Timber.tag(TAG).d("audioCategory: %s", audioCategory);

//            ApiInterface apiInterface = ApiClient.INSTANCE.getUploadClient().create(ApiInterface.class);
            apiInterface = ApiClient.getClient().create(ApiInterface.class);

            Call<Map<String, String>> call = apiInterface.postVideo(filePart, partMap);
            Timber.tag(TAG).d("postUpload1: %s1", new Gson().toJson(partMap));
            Timber.tag(TAG).d("postUpload: %s", call.request().url());

            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Timber.tag(TAG).d("postUpload: %s", App.getGsonPrettyInstance().toJson(response.body()));
                        if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            uploadCoverPhoto();
                        } else
                            Toasty.warning(PostVideoActivity.this, getResources().getString(R.string.something_went_wrong));
                    } else {
                        setPostButton(true);
                        Toasty.warning(PostVideoActivity.this, getResources().getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    Timber.tag(TAG).e("uploadError: %s", t.getMessage());
                    dismissUploadDialog();
                    setPostButton(true);
                }
            });
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
            setPostButton(true);
        }
    }

    private String getApiPublishMode(String publishMode) {
        Timber.d("getApiPublishMode: %s", publishMode);
        if (publishMode.equalsIgnoreCase(getString(R.string.every_one))) return "public";
        else if (publishMode.equalsIgnoreCase(getString(R.string.fans))) return "fans";
        else return "private";
    }

    private String getPublishMode(String publishMode) {
        Timber.d("getPublishMode: %s", publishMode);
        if (TextUtils.isEmpty(publishMode)) return null;
        else if (publishMode.equals("public")) return getString(R.string.every_one);
        else if (publishMode.equals("fans")) return getString(R.string.fans);
        else return getString(R.string.only_me);
    }
    private String getCommentMode(String publishMode) {
        Timber.d("getCommentMode: %s", publishMode);
        if (TextUtils.isEmpty(publishMode)) return null;
        else if (publishMode.equals("public")) return getString(R.string.every_one);
        else if (publishMode.equals("fans")) return getString(R.string.fans);
        else return getString(R.string.only_me);
    }

    public void getUpdateList(String hashStr) {
        if (hashStr.equals("")) {
        } else if (hashStr.contains(",")) {
            String arr[] = hashStr.split(",");
            for (int i = 0; i < arr.length; i++) {
                newHashList.add(arr[i]);
            }
        } else newHashList.add(hashStr);
    }

    private String paramDeleted(List<String> hashtags) {
//        Timber.d("getHashTags: %s", new Gson().toJson(hashtags));
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < hashtags.size(); i++) {
            builder.append(hashtags.get(i));
            if (i < hashtags.size() - 1) builder.append(",");
        }
        Timber.d("paramdelete: %s", builder.toString());
        return builder.toString();
    }

    private void updateVideo() {
        if (NetworkReceiver.isConnected()) {
            setPostButton(false);
            showProgressDialog();
            Log.d(TAG, "updateVideo: processing... 3");
            postViewMode = getApiPublishMode(video_view_result.getText().toString());
//            postCmtMode = getApiPublishMode(video_cmt_result.getText().toString());

            String deletedParam = "";
            String hashString = serialialize(edtDescription.getHashtags()).trim();
            getUpdateList(hashString);

            ArrayList<String> deletedList = new ArrayList<>();

            for (int i = 0; i < oldHashList.size(); i++) {
                int count = 0;
                for (int y = 0; y < newHashList.size(); y++) {
                    if (!oldHashList.get(i).contains(newHashList.get(y))) {
                        count++;
                    }
                }
                if (count > 0)
                    deletedList.add(oldHashList.get(i));
            }

            if (newHashList.size() == 0 && oldHashList.size() > 0)
                deletedList.addAll(oldHashList);

            if (deletedList.size() > 0)
                deletedParam = paramDeleted(deletedList);

            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("user_id", GetSet.getUserId());
            requestMap.put("video_id", getVideoId);
            requestMap.put("description", edtDescription.getText().toString());
            requestMap.put("devicetype", "1");
            requestMap.put("mode", postViewMode);
//            requestMap.put("comment_mode", postCmtMode);
            requestMap.put("deleted_hashtags", deletedParam);
            requestMap.put("use_audio", useAudio.toString());
            ///String hashString = serialialize(inputDescriptionTxt.getHashtags()).trim();
            if (!TextUtils.isEmpty(hashString)) {
                requestMap.put("hashtags", hashString);
            }
            Timber.tag(TAG).d("updateVideo: %s", new Gson().toJson(requestMap));

//            Timber.tag(TAG).d("updateVideo: %s", call.request().url());

            Call<Map<String, String>> call = apiInterface.updateVideo(requestMap);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    Log.e(TAG , "updateVideo: %s" + new Gson().toJson(response.body()) +"test");
                    if (response.isSuccessful()) {
//                        Timber.tag(TAG).d("updateVideo: %s", App.getGsonPrettyInstance().toJson(response.body()));
                        if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            App.makeToast(getString(R.string.video_updated_successfully));
                            Intent intent = new Intent();
                            intent.putExtra(Constants.TAG_VIDEO_ID, getVideoId);
                            intent.putExtra(Constants.TAG_DESCRIPTION, edtDescription.getText().toString());
                            intent.putExtra(Constants.TAG_PUBLISH_MODE, postViewMode);
//                            intent.putExtra(Constants.TAG_COMMENT_MODE, postCmtMode);
                            intent.putExtra(Constants.TAG_USE_AUDIO, useAudio);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else
                            Toasty.warning(PostVideoActivity.this, getResources().getString(R.string.something_went_wrong));
                    } else {
                        setPostButton(true);
                        Toasty.warning(PostVideoActivity.this, getResources().getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    Timber.tag(TAG).e("uploadError: %s", t.getMessage());
                    dismissUploadDialog();
                    setPostButton(true);
                }
            });
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
            setPostButton(true);
        }
    }

    private void showProgressDialog() {
        if (pdLoading != null && !pdLoading.isShowing()) pdLoading.show();
    }

    private void dismimssProgressDialog() {
        if (pdLoading != null && pdLoading.isShowing()) pdLoading.dismiss();
    }

    private void dismissUploadDialog() {
        if (uploadingDialog != null && uploadingDialog.isShowing()) {
            uploadingDialog.dismiss();
        }
    }

    private void uploadCoverPhoto() {
        if (NetworkReceiver.isConnected()) {
            if (thumbFile == null || !thumbFile.exists()) {
                Timber.tag(TAG).d("uploadCoverPhoto: Thumb not found");
                dismissUploadDialog();
                setPostButton(true);
                return;
            }

            RequestBody requestFile = RequestBody.create(thumbFile, MediaType.parse("image/*"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("stream_image", "video_thumbnail" + ".jpeg", requestFile);
            RequestBody videoId = RequestBody.create(getVideoId, MediaType.parse("multipart/form-data"));
//            ApiInterface apiInterface = ApiClient.INSTANCE.getUploadClient().create(ApiInterface.class);
            apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<Map<String, String>> call = apiInterface.uploadCoverPhoto(body, videoId);

            Timber.tag(TAG).d("uploadCoverPhoto1: %s", call.request().url());
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Timber.tag(TAG).d("uploadCoverPhoto2: %s", App.getGsonPrettyInstance().toJson(response.body()));
                        Map<String, String> getRes = response.body();
                        if (getRes.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            mainHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dismissUploadDialog();
//                                    setResult(RESULT_OK);
                                    finish();
                                    Intent intent = new Intent(PostVideoActivity.this, MainActivity.class);
                                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                                    startActivity(intent);
                                }
                            }, 1000);
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    Timber.tag(TAG).e("uploadCoverPhoto3: %s", t.getMessage());
                    call.cancel();
                }
            });

        } else {
            dismissUploadDialog();
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private Bitmap getAlbumImage(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) return BitmapFactory.decodeByteArray(data, 0, data.length);
        return null;
    }

    public void animation() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        slidrInterface = Slidr.attach(this, config);
    }


    @Override
    public void onBackPressed() {
        /*super.onBackPressed();*/
        openReshootDialog();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

        Timber.i("onNetworkChanged isConnected %s", isConnected);
        Timber.i("onNetworkChanged isUploading %s", isUploading);

        if (!isConnected && !isUploading) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
            isUploading = false;
        } else if (!isConnected && isUploading) {
            Toasty.error(this, getResources().getString(R.string.internet_disturb), Toasty.LENGTH_SHORT).show();
            dismissUploadDialog();
            isUploading = false;
        } else {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), true);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.VIDEO_PRIVACY) {

            if (resultCode != RESULT_CANCELED) {

                privacyData.clear();

                String getData = Objects.requireNonNull(data).getStringExtra("privacyName");

                video_view_result.setText(getData);
                setPrivacyData(getData);
            }

        }
//        else if (requestCode == Constants.VIDEO_CMT_PRIVACY) {
//
//            if (resultCode != RESULT_CANCELED) {
//
//                commentPrivacyData.clear();
//
//                String getData = Objects.requireNonNull(data).getStringExtra("privacyName");
//
//                video_cmt_result.setText(getData);
//                setCommentPrivacyData(getData);
//            }
//
//        }
        else if (requestCode == 101) {
            if (resultCode == RESULT_OK) {

            }
        }
    }

  /*  @Override
    public void onItemClicked(Object o, int position, String type) {
      *//*  audioCategory = ((AudioCategory) o).getId();*//* //web team asking name instead of id
        audioCategory = ((AudioCategory) o).getName();
    //    audioCategory = (String) o;
        selectedCategoryPosition = position;
        txtAudioCategory.setError(null);
       // txtAudioCategory.setText(audioCategory); //original
        String categoryName = "";
        if (position >= 0 && position < AdminData.audioCategory.size()) {
          //  categoryName = AdminData.audioCategory.get(position);   // ← YES this gives the exact name at that position
           // categoryName = AdminData.audioCategory.get(position).getName();   // ← YES this gives the exact name at that position
        }

        txtAudioCategory.setText(categoryName);
    }*/

    @Override
    public void onItemClicked(Object o, int position, String type) {
        if (!(o instanceof AudioCategory)) return; // safety check
        AudioCategory category = (AudioCategory) o;

        // Send the English/Default name to API (since backend wants name, not ID)
       // audioCategory = category.getName();
        audioCategory = category.getId();

        // Remember the selected position
        selectedCategoryPosition = position;
        txtAudioCategory.setError(null);

        String lang = LocaleManager.getLanguageCode(PostVideoActivity.this);
        Log.e("AudioCategoryAdapter111", lang +"test");

        if (lang.equals("en")){
            txtAudioCategory.setText(category.getName());
        }else{
            txtAudioCategory.setText(category.getArabicName());
        }
        // UI display based on locale
       /* String categoryName = LocaleManager.isRTL() ? category.getArabicName() : category.getName();
        txtAudioCategory.setText(categoryName);*/
    }


    /*public class HashtagArrayAdapter<T extends Hashtagable> extends SocialArrayAdapter<T> {
        private final int countPluralRes;

        public HashtagArrayAdapter(@NonNull Context context) {
            this(context, com.hendraanggrian.appcompat.socialview.commons.R.plurals.posts);
        }

        public HashtagArrayAdapter(@NonNull Context context, @PluralsRes int countPluralRes) {
            super(context, com.hendraanggrian.appcompat.socialview.commons.R.layout.socialview_layout_hashtag, com.hendraanggrian.appcompat.socialview.commons.R.id.socialview_hashtag);
            this.countPluralRes = countPluralRes;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(com.hendraanggrian.appcompat.socialview.commons.R.layout.socialview_layout_hashtag, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final T item = getItem(position);
            if (item != null) {
                holder.hashtagView.setText(item.getId());

                if (item.getCount() > 0) {
                    holder.countView.setVisibility(View.VISIBLE);
                    final int count = item.getCount();
                    holder.countView.setText(getContext().getResources().getQuantityString(countPluralRes, count, count));
                } else {
                    holder.countView.setVisibility(View.GONE);
                }
            }
            return convertView;
        }

        private  class ViewHolder {
            private final TextView hashtagView;
            private final TextView countView;

            ViewHolder(View itemView) {
                hashtagView = itemView.findViewById(com.hendraanggrian.appcompat.socialview.commons.R.id.socialview_hashtag);
                countView = itemView.findViewById(com.hendraanggrian.appcompat.socialview.commons.R.id.socialview_hashtag_count);
            }
        }
    }*/


    public static class HashTagArrayAdapter extends SocialArrayAdapter<PostHashTagRes.Result> {

        public HashTagArrayAdapter(@NonNull Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            return super.getView(position, convertView, parent);

            // return new MyViewHolder(LayoutInflater.from(parent).inflate(R.layout.item_post_hashtag, parent, false));
        }

        @NonNull
        @Override
        public CharSequence convertToString(PostHashTagRes.Result object) {

            return object.getName();
        }

        @Override
        public void add(@Nullable PostHashTagRes.Result object) {
            Timber.d("add: %s", object);
            super.add(object);
        }

        @Override
        public void addAll(@NonNull Collection<? extends PostHashTagRes.Result> collection) {
            super.addAll(collection);
            Timber.d("addAll: %s", collection);
        }

        @Override
        public void remove(@Nullable PostHashTagRes.Result object) {
            super.remove(object);
        }

        @Override
        public void clear() {
            super.clear();
        }

        @NonNull
        @Override
        public Filter getFilter() {
            return super.getFilter();
        }

    }

    public static String getDots(int count, String outDots) {
        if (count <= 0) return outDots;
        return getDots(--count, outDots) + ".";
    }

    @Override
    protected void onResume() {
        super.onResume();

  /*      if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        }*/
    }
}
