package com.app.alhawanajd.home;

import static com.app.external.MediaPlayerUtils.mediaPlayer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.app.alhawanajd.ActGifVideoEditor;
import com.app.alhawanajd.ChatActivity;
import com.app.alhawanajd.ChatFragment;
import com.app.alhawanajd.GifEditorActivity;
import com.app.alhawanajd.R;
import com.app.alhawanajd.Video.DuetRecordActivity;
import com.app.alhawanajd.adapter.ShareAdapter;
import com.app.helper.workers.TempVideoWorker;
import com.app.livedata.MessageLiveModel;
import com.app.model.ChatResponse;
import com.app.model.CommonResponse;
import com.app.model.RecentUser;
import com.app.model.ReportResponceNew;
import com.app.model.RepostResponse;
import com.app.model.ShareItem;
import com.app.model.UserList;
import com.app.utils.exoutils.ExoPlayerUtils;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.app.db.DBHelper;
import com.app.alhawanajd.App;
import com.app.alhawanajd.BaseFragmentActivity;
import com.app.alhawanajd.LoginActivity;
import com.app.alhawanajd.PrimeActivity;
import com.app.alhawanajd.Video.PostVideoActivity;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.PermissionsUtils;
import com.app.helper.workers.FFmpegVideoDownloadWorker;
import com.app.model.GetSet;
import com.app.model.HomeResponse;
import com.app.utils.AdminData;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiCallback;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.api.CommonApi;
import com.app.utils.api.ReportApiCallback;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class HomeBottomDialog extends BaseFragmentActivity implements ReportApiCallback  {

    private static final String TAG = HomeBottomDialog.class.getSimpleName();
    private FragmentActivity mActivity;
    private BottomSheetDialog menuDialog, reportDialog;
    private LinearLayout shareLay, favLay, reportLay, blockLay, downloadLay, editLay, deleteLay;
    private TextView txtFav, txtReport, txtBlock, txtEdit, txtDownload;
    private HomeVideoAdapter.VideoAdapterListener adapterListener;
    private ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
    ProgressDialog pdLoading;
    /*Report*/
    private ReportAdapter reportAdapter;
    private RecyclerView reportsView;
    private ImageView btnBack;
    private ImageView btnClose;
    private TextView txtTitle;
    private RecyclerView reportsView1;
    private RecyclerView reportsView2;
    private RecyclerView reasonsView;
    private View selectReasonLay;
    private View selectSubReasonLay;
    private View selectSubReasonLay2;
    private View submitLay;
    private View textBoxLay;
    private View personBoxLay;
    private TextView reportReason;
    private TextView reportTitle;
    private Button btnReport;
    private String lanCode;
    MainReportAdapter mainReportAdapter;
    SubReportAdapter subReportAdapter;
    SubReportAdapter subReportAdapter2;
    EditText reportEditTxt;
    AutoCompleteTextView personEditTxt;
    String reportReasonId, enterReportText,entertedPersonName;
    ArrayList<HomeResponse.Result> mHomeApiResponse;
    ReportResponceNew reportResponceNew;
    int adapterPosition;
    ReportResponceNew.Categories selectedCategory;
    ReportResponceNew.Subcategories selectedSubCategory;
    ReportResponceNew.Subcategories selectedSubCategory2;

    BottomSheetDialog dialog;

    private ProgressDialog progressDialog;

    private boolean repost = false;

    private ShareAdapter shareAppsAdapter;
    private static final int VESDK_GIF_EDITOR = 991;

    String actions,repost_desc;

    public static final String TRIM_VIDEO_OPTION = "trim_video_option",
            TRIM_VIDEO_URI = "trim_video_uri", TRIMMED_VIDEO_PATH = "trimmed_video_path";

    private MessageLiveModel liveModel;

    public HomeBottomDialog(FragmentActivity mActivity) {
        this.mActivity = mActivity;
    }

    public void openMenu(ArrayList<HomeResponse.Result> mHomeApiResponse, int adapterPosition, HomeVideoAdapter.VideoAdapterListener listener) {
        this.adapterListener = listener;
        this.mHomeApiResponse = mHomeApiResponse;
        this.adapterPosition = adapterPosition;
        liveModel = new ViewModelProvider(mActivity).get(MessageLiveModel.class);


       /* View sheetView = mActivity.getLayoutInflater().inflate(R.layout.bottmsheet_home_longpress, null);
        //R.style.SimpleBottomDialog - previously used this
        menuDialog = new BottomSheetDialog(mActivity, R.style.VideoCommentNewDialog); // Style here
        menuDialog.setContentView(sheetView);

        shareLay = sheetView.findViewById(R.id.shareLay);
        blockLay = sheetView.findViewById(R.id.blockLay);
        favLay = sheetView.findViewById(R.id.favLay);
        txtFav = sheetView.findViewById(R.id.txtFav);
        reportLay = sheetView.findViewById(R.id.reportLay);
        txtReport = sheetView.findViewById(R.id.txtReport);
        txtBlock = sheetView.findViewById(R.id.txtBlock);
        downloadLay = sheetView.findViewById(R.id.downloadLay);
        txtDownload = sheetView.findViewById(R.id.txtDownload);
        deleteLay = sheetView.findViewById(R.id.deleteLay);
        editLay = sheetView.findViewById(R.id.editLay);
        txtEdit = sheetView.findViewById(R.id.txtEdit);

        pdLoading = new ProgressDialog(mActivity, R.style.CustomProgressDialog);
        pdLoading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pdLoading.setMessage(mActivity.getString(R.string.msg_please_wait));
        pdLoading.setCancelable(false);

        if (mHomeApiResponse.get(adapterPosition).getPublisherId().equals(GetSet.getUserId())) {
            favLay.setVisibility(View.GONE);
            reportLay.setVisibility(View.GONE);
            editLay.setVisibility(View.VISIBLE);
            deleteLay.setVisibility(View.VISIBLE);
        } else {
            editLay.setVisibility(View.GONE);
            deleteLay.setVisibility(View.GONE);
            favLay.setVisibility(View.VISIBLE);
            reportLay.setVisibility(View.VISIBLE);
        }

        if (mHomeApiResponse.get(adapterPosition).getVideoIsFavorite())
            txtFav.setText(R.string.unFav);
        else txtFav.setText(R.string.addafav);

        if (mHomeApiResponse.get(adapterPosition).getIsVideoReported())
            txtReport.setText(R.string.unreported);
        else txtReport.setText(R.string.report_user);

        favLay.setOnClickListener(v -> favApi(mHomeApiResponse, adapterPosition, "video", adapterListener));

        reportLay.setOnClickListener(v -> {
            if (txtReport.getText().toString().trim().equalsIgnoreCase(mActivity.getResources().getString(R.string.report_user))) {
                adapterListener.setPlayback(false, adapterPosition, Constants.TAG_PLAYBACK);
                openReportDialog(mHomeApiResponse, adapterPosition, adapterListener);
            } else {
                sendReport(mHomeApiResponse, "getSelectedReport", false, adapterPosition);
            }
        });
       *//* downloadLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AdminData.downloadVideo) {
                    downloadUrl(mHomeApiResponse.get(adapterPosition).getPlaybackUrl());
                } else if (SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false").equals("true") || mHomeApiResponse.get(adapterPosition).getPublisherId().equals(GetSet.getUserId())) {
                    downloadUrl(mHomeApiResponse.get(adapterPosition).getPlaybackUrl());
                } else {
                    App.makeToast(mActivity.getApplicationContext().getString(R.string.prime_alert));
                    Intent intent = new Intent(mActivity, PrimeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    adapterListener.moveToIntent(intent);
                }
            }
        });*//*

        downloadLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHomeApiResponse.get(adapterPosition).getPublisherId().equals(GetSet.getUserId())){
                    downloadUrl(mHomeApiResponse.get(adapterPosition).getPlaybackUrl());
                }
                else if (SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false").equals("true") || AdminData.downloadVideo) {
                    App.makeToast(mActivity.getApplicationContext().getString(R.string.prime_alert));
                    Intent intent = new Intent(mActivity, PrimeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    adapterListener.moveToIntent(intent);
                }
                else {
                    downloadUrl(mHomeApiResponse.get(adapterPosition).getPlaybackUrl());
                }
            }
        });

        editLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHomeApiResponse.get(adapterPosition).getPublisherId().equals(GetSet.getUserId())) {
                    Intent intent = new Intent(mActivity, PostVideoActivity.class);
                    intent.putExtra(Constants.TAG_VIDEO_ID, mHomeApiResponse.get(adapterPosition).getVideoId());
                    intent.putExtra(Constants.TAG_DESCRIPTION, mHomeApiResponse.get(adapterPosition).getVideoDescription());
                    intent.putExtra(Constants.TAG_PUBLISH_MODE, mHomeApiResponse.get(adapterPosition).getMode());
                    intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, mHomeApiResponse.get(adapterPosition).getStreamThumbnail());
                    intent.putExtra(Constants.TAG_VIDEO_PATH, mHomeApiResponse.get(adapterPosition).getPlaybackUrl());
                    intent.putExtra(Constants.TAG_AUDIOCATEGORY, mHomeApiResponse.get(adapterPosition).getAudioCategory());
                    intent.putExtra(Constants.TAG_USE_AUDIO, mHomeApiResponse.get(adapterPosition).getSoundtracks().isUseAudio());
                    intent.putExtra(Constants.TAG_EDIT, true);
                    adapterListener.moveToIntent(intent);
                    dismissMenuDialog();
                }
            }
        });

        deleteLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHomeApiResponse.get(adapterPosition).getPublisherId().equals(GetSet.getUserId())) {
                    adapterListener.setPlayback(false, adapterPosition, Constants.TAG_PLAYBACK);
                    openDeleteDialog(mHomeApiResponse.get(adapterPosition).getVideoname(), adapterPosition);
                    Log.d(TAG, "onClick_getVideoname: "+mHomeApiResponse.get(adapterPosition).getVideoname());

                }
            }
        });

        shareLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.preventMultipleClick(shareLay);
//                shareVideoDialog(mHomeApiResponse.get(adapterPosition).getShareLink());

                Log.d(TAG, "onClick1111: "+mHomeApiResponse.get(adapterPosition).getShareLink());
                String fullUrl = mHomeApiResponse.get(adapterPosition).getVideoId();
                String videoId = fullUrl.substring(fullUrl.lastIndexOf("/") + 1);
*//*
                Uri shortLink = Uri.parse(mHomeApiResponse.get(adapterPosition).getShareLink());
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String msg = (GetSet.getUserName() != null ? GetSet.getUserName() : mActivity.getString(R.string.app_name)) + " " + (mActivity.getString(R.string.shared_a) + " " + mActivity.getString(R.string.video)).toLowerCase() + "\n\n" + "" + shortLink;
//                                Timber.d("shortLinkTask: %s", msg);
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                mActivity.startActivity(Intent.createChooser(intent, mActivity.getString(R.string.share_video)));*//*


                String shareLink = mHomeApiResponse.get(adapterPosition).getShareLink();
                showCustomShareSheet(shareLink,videoId,repost);


                *//*Task<ShortDynamicLink> shortLinkTask = new AppUtils(mActivity).getStreamShareLink(videoId,"/videos?stream_id=");
                shortLinkTask.addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            // Short link created
                            Uri shortLink = task.getResult().getShortLink();
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            String msg = (GetSet.getUserName() != null ? GetSet.getUserName() : mActivity.getString(R.string.app_name)) + " " + (mActivity.getString(R.string.shared_a) + " " + mActivity.getString(R.string.video)).toLowerCase() + "\n\n" + "" + shortLink;
//                                Timber.d("shortLinkTask: %s", msg);
                            intent.putExtra(Intent.EXTRA_TEXT, msg);
                            mActivity.startActivity(Intent.createChooser(intent, mActivity.getString(R.string.share_video)));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.d(TAG, "onFailure: "+e);

                    }
                });*//*
            }
        });

        menuDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            @SuppressLint("CutPasteId") View bottomSheet1 = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet1).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet1).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet1).setHideable(true);
        });

        menuDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
                View bottomSheet12 = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet12).setState(BottomSheetBehavior.STATE_COLLAPSED);
                BottomSheetBehavior.from(bottomSheet12).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet12).setHideable(true);
            }
        });

        menuDialog.show();*/
        String fullUrl = mHomeApiResponse.get(adapterPosition).getVideoId();
        String videoId = fullUrl.substring(fullUrl.lastIndexOf("/") + 1);

        String shareLink = mHomeApiResponse.get(adapterPosition).getShareLink();
        showCustomShareSheet(shareLink,videoId,repost);
    }

    private void downloadUrl(String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            downloadFile(url);
        }else{
            if (PermissionsUtils.checkStoragePermission(mActivity)) {
                downloadFile(url);
            } else {
                adapterListener.checkStoragePermissions();
            }
        }

    }

    public void showCustomShareSheet(String shareLink,String videoId,boolean isReposted) {
        this.repost = isReposted;
         dialog = new BottomSheetDialog(mActivity, R.style.VideoCommentNewDialog);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.share_bottom_sheet, null);

        dialog.setContentView(view);
        dialog.show();

        RecyclerView userList = view.findViewById(R.id.userList);
        RecyclerView appsList = view.findViewById(R.id.appsList);
        RecyclerView infoList = view.findViewById(R.id.infoList);


        userList.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));

        loadRecentUsers(userList, videoId,shareLink);


        appsList.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
        shareAppsAdapter = new ShareAdapter(getShareApps(), item -> {
            if (item.getPackageName().equals("repost")) {

                if (!isReposted) {
                    callRepostAPI(videoId,true);
                } else {
                    callRepostAPI(videoId,false);
                }
                return;
            } else {
                shareToApp(item.getPackageName(), shareLink);
            }
        });

        appsList.setAdapter(shareAppsAdapter);


        infoList.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
        

        infoList.setAdapter(new ShareAdapter(getMoreOptions(), item -> {

            switch (item.getPackageName()) {

                case "report":
                    handleReport();
                    break;

                case "edit":
                    handleEdit();
                    break;

                case "delete":
                    openDeleteDialog(videoId,adapterPosition);
                    break;

                case "notinterested":
                    dialog.dismiss();
                    adapterListener.setVideoDeleted(true,videoId);
                    Toast.makeText(mActivity, "Marked Not Interested", Toast.LENGTH_SHORT).show();
                    break;
                    case "fav":
                    dialog.dismiss();
                    favApi(mHomeApiResponse, adapterPosition, "video", adapterListener);

                    break;

                case "download":
                    downloadVideo(videoId);
                    break;

                case "addtostory":
                    addToInstagramStory(videoId);
                    break;

                case "cast":
                    castVideo(videoId);
                    break;

                case "whythispost":
                    openWhyThisPost(videoId);
                    break;

                case "caption":
                    showCaptionDialog(videoId,mHomeApiResponse.get(adapterPosition).getPlaybackUrl());
                    break;

                case "duet":
                    startDuet(videoId);
                    break;

                case "stitch":
                    startStitch(videoId);
                    break;

                case "createsticker":
                    shareAsGif(videoId,"sticker");
                    break;

                case "playbackspeed":
                    openPlaybackSpeed(videoId);
                    dialog.dismiss();
                    break;

                case "setaswallpaper":
                    setVideoAsWallpaper(videoId);
                    break;

                case "shareasgif":
                    shareAsGif(videoId,"gif");
                    break;
            }
        }));

        // close button
        view.findViewById(R.id.cancelBtn).setOnClickListener(v -> dialog.dismiss());
    }

    private void handleEdit() {
        Intent intent = new Intent(mActivity, PostVideoActivity.class);
        intent.putExtra(Constants.TAG_VIDEO_ID, mHomeApiResponse.get(adapterPosition).getVideoId());
        intent.putExtra(Constants.TAG_DESCRIPTION, mHomeApiResponse.get(adapterPosition).getVideoDescription());
        intent.putExtra(Constants.TAG_PUBLISH_MODE, mHomeApiResponse.get(adapterPosition).getMode());
        intent.putExtra(Constants.TAG_VIDEO_THUMBANIL, mHomeApiResponse.get(adapterPosition).getStreamThumbnail());
        intent.putExtra(Constants.TAG_VIDEO_PATH, mHomeApiResponse.get(adapterPosition).getPlaybackUrl());
        intent.putExtra(Constants.TAG_AUDIOCATEGORY, mHomeApiResponse.get(adapterPosition).getAudioCategory());
        intent.putExtra(Constants.TAG_USE_AUDIO, mHomeApiResponse.get(adapterPosition).getSoundtracks().isUseAudio());
        intent.putExtra(Constants.TAG_EDIT, true);
        adapterListener.moveToIntent(intent);
    }

    private void shareAsGif(String videoId,String from) {
        String videoUrl = mHomeApiResponse.get(adapterPosition).getPlaybackUrl();
        downloadTempVideoForGif(videoUrl,from);
    }
    private void downloadTempVideoForGif(String playbackUrl,String from) {

//        menuDialog.dismiss();
        dialog.dismiss();
        // SHOW LOADER
        progressDialog = new ProgressDialog(mActivity);
        progressDialog.setMessage("Preparing video…");
        progressDialog.setCancelable(false);
        progressDialog.show();

        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(TempVideoWorker.class)
                .setInputData(
                        new Data.Builder()
                                .putString("url", playbackUrl)
                                .build()
                )
                .build();

        WorkManager workManager = WorkManager.getInstance(mActivity);
        workManager.enqueue(req);

        workManager.getWorkInfoByIdLiveData(req.getId())
                .observe(mActivity, info -> {

                    if (info == null) return;

                    if (info.getState() == WorkInfo.State.SUCCEEDED) {

                        // CLOSE LOADER
                        if (progressDialog.isShowing()) progressDialog.dismiss();

                        String tempPath = info.getOutputData().getString("temp_path");

                        if(from.equals("gif")){
                        openGifEditor(tempPath);} else if (from.equals("sticker")) {
                            createSticker(tempPath,from);
                        }
                    }

                    else if (info.getState() == WorkInfo.State.FAILED ||
                            info.getState() == WorkInfo.State.CANCELLED) {

                        if (progressDialog.isShowing()) progressDialog.dismiss();

                        Toast.makeText(mActivity, "Failed to prepare video!", Toast.LENGTH_SHORT).show();
                    }
                });
    }




/*
    private void openGifEditor(String filePath) {
        Intent intent = new Intent(mActivity, GifEditorActivity.class);
        intent.putExtra("VIDEO_PATH", filePath);
        adapterListener.moveToIntent(intent);
    }
*/

    private void openGifEditor(String filePath) {

        File file = new File(filePath);
        Uri videoUri = Uri.fromFile(file);

        Intent intent = new Intent(mActivity, ActGifVideoEditor.class);

        Gson gson = new Gson();
        Bundle bundle = new Bundle();

        // language
        if (LocaleManager.isRTL()) {
            bundle.putString("lan", "ar");
        } else {
            bundle.putString("lan", "en");
        }

        // required by ActVideoTrimmer
        bundle.putString(TRIM_VIDEO_URI, videoUri.toString());
//        bundle.putString(TRIM_VIDEO_OPTION, gson.toJson(options));

        intent.putExtras(bundle);

        adapterListener.moveToIntent(intent);
    }



 /*   private void openGifEditor(String filePath) {

        File file = new File(filePath);
        Uri videoUri = Uri.fromFile(file);

        // Settings for VESDK
        SettingsList settingsList = new SettingsList();

        // Load the video file
        settingsList.getSettingsModel(VideoEditorSettings.class)
                .setSource(videoUri);

        // Export as GIF
        settingsList.getSettingsModel(VideoEditorSaveSettings.class)
                .setOutputFileFormat(VideoEditorSaveSettings.GIF)
                .setOutputFileUri(createGifOutputUri())   // your output path
                .setQuality(VideoEditorSaveSettings.QUALITY_MID);

        // Optional language
        settingsList.getSettingsModel(UiConfigMainMenu.class)
                .setLocale(LocaleManager.isRTL() ? "ar" : "en");

        // Start VESDK Editor
        try {
            Intent intent = new Intent(mActivity, VideoEditorActivity.class);
            intent.putExtra(VideoEditorActivity.SETTINGS_LIST, settingsList);
            startActivityForResult(intent, VESDK_GIF_EDITOR);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/





    private void setVideoAsWallpaper(String videoId) {
    }

    private void openPlaybackSpeed(String videoId) {

        BottomSheetDialog dialog = new BottomSheetDialog(mActivity, R.style.VideoCommentNewDialog);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.playback_speed_sheet, null);
        dialog.setContentView(view);

        RadioButton speed05 = view.findViewById(R.id.speed05);
        RadioButton speed1 = view.findViewById(R.id.speed1);
        RadioButton speed15 = view.findViewById(R.id.speed15);
        RadioButton speed2 = view.findViewById(R.id.speed2);

        // Load last saved speed
        SharedPreferences prefs = mActivity.getSharedPreferences("speed_prefs", Context.MODE_PRIVATE);
        float lastSpeed = prefs.getFloat("speed_value", 1.0f); // default = 1x

        // Select the correct radio button
        if (lastSpeed == 0.5f) speed05.setChecked(true);
        else if (lastSpeed == 1.0f) speed1.setChecked(true);
        else if (lastSpeed == 1.5f) speed15.setChecked(true);
        else if (lastSpeed == 2.0f) speed2.setChecked(true);

        // Listeners
        speed05.setOnClickListener(v -> { setSpeedValue(0.5f, videoId); dialog.dismiss(); });
        speed1.setOnClickListener(v -> { setSpeedValue(1.0f, videoId); dialog.dismiss(); });
        speed15.setOnClickListener(v -> { setSpeedValue(1.5f, videoId); dialog.dismiss(); });
        speed2.setOnClickListener(v -> { setSpeedValue(2.0f, videoId); dialog.dismiss(); });

        dialog.show();
    }


    private void setSpeedValue(float speed,String videoId) {
   /*     if (mediaPlayer == null) {
            Log.e("Speed", "MediaPlayer is NULL — cannot change speed");
            Toast.makeText(mActivity, "Video not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                PlaybackParams params = mediaPlayer.getPlaybackParams();
                params.setSpeed(speed);
                mediaPlayer.setPlaybackParams(params);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

        SharedPreferences prefs = mActivity.getSharedPreferences("speed_prefs", Context.MODE_PRIVATE);
        prefs.edit().putFloat("speed_value", speed).apply();

        adapterListener.setVideoPlayBackSpeed(true,videoId,adapterPosition,"playbackspeed",speed);


    }




    private void createSticker(String filePath,String from) {
        File file = new File(filePath);
        Uri videoUri = Uri.fromFile(file);

        Intent intent = new Intent(mActivity, ActGifVideoEditor.class);

        Gson gson = new Gson();
        Bundle bundle = new Bundle();

        // language
        if (LocaleManager.isRTL()) {
            bundle.putString("lan", "ar");
        } else {
            bundle.putString("lan", "en");
        }
        bundle.putBoolean("isStickerMode", true);
        // required by ActVideoTrimmer
        bundle.putString(TRIM_VIDEO_URI, videoUri.toString());
        bundle.putString(Constants.TAG_FROM, from);
//        bundle.putString(TRIM_VIDEO_OPTION, gson.toJson(options));

        intent.putExtras(bundle);

        adapterListener.moveToIntent(intent);

    }

    private void startStitch(String videoId) {
    }

    private void startDuet(String videoId) {
        HomeResponse.Result item = mHomeApiResponse.get(adapterPosition);
        String videoUrl = item.getAndroidVideo();
        if (TextUtils.isEmpty(videoUrl)) {
            videoUrl = item.getPlaybackUrl();
        }
        if (TextUtils.isEmpty(videoUrl)) {
            Toast.makeText(mActivity, "error", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent duetIntent = new Intent(mActivity, DuetRecordActivity.class);
        duetIntent.putExtra(Constants.TAG_VIDEO_ID, videoId);
        duetIntent.putExtra(Constants.TAG_VIDEO_PATH, videoUrl);
        mActivity.startActivity(duetIntent);
    }

    private void showCaptionDialog(String videoId,String videoUrl) {

        setVideoWithCaption(videoId,videoUrl, "Enjoy the moment ✨");
    }

    private void openWhyThisPost(String videoId) {

        reportDialog.dismiss();
        BottomSheetDialog dialog  =new BottomSheetDialog(mActivity, R.style.VideoCommentNewDialog);
        View view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_why_this_post, null);


        ImageView tvClose = view.findViewById(R.id.ivCancel);
        TextView adjustForYoy = view.findViewById(R.id.tvAdjustForYou);
        ConstraintLayout layoutWhyThisPost = view.findViewById(R.id.layoutWhyThisPost);
        ConstraintLayout layoutAdjustForYou = view.findViewById(R.id.layoutAdjustForYou);

        adjustForYoy.setOnClickListener(v->{
            layoutWhyThisPost.setVisibility(View.GONE);
            layoutAdjustForYou.setVisibility(View.VISIBLE);
        });

        tvClose.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
    }

    private void castVideo(String videoId) {
    }

    private void addToInstagramStory(String videoId) {
    }

    private void downloadVideo(String videoId) {
        if (mHomeApiResponse.get(adapterPosition).getPublisherId().equals(GetSet.getUserId())){
            downloadUrl(mHomeApiResponse.get(adapterPosition).getPlaybackUrl());
        }
        else if (SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false").equals("true") || AdminData.downloadVideo) {
            App.makeToast(mActivity.getApplicationContext().getString(R.string.prime_alert));
            Intent intent = new Intent(mActivity, PrimeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            adapterListener.moveToIntent(intent);
        }
        else {
            downloadUrl(mHomeApiResponse.get(adapterPosition).getPlaybackUrl());
        }
        dialog.dismiss();
    }

    private void handleReport() {

//        if (txtReport.getText().toString().trim().equalsIgnoreCase(mActivity.getResources().getString(R.string.report_user))) {
            adapterListener.setPlayback(false, adapterPosition, Constants.TAG_PLAYBACK);
            openReportDialog(mHomeApiResponse, adapterPosition, adapterListener);
//        } else {
//            sendReport(mHomeApiResponse, "getSelectedReport", false, adapterPosition);
//        }
    }

    private List<ShareItem> getRecentUsers() {
        List<ShareItem> list = new ArrayList<>();

        // Dummy loop (replace with real users)
        for (int i = 0; i < 10; i++) {
            list.add(new ShareItem(com.hendraanggrian.appcompat.socialview.autocomplete.R.drawable.socialview_ic_mention_placeholder, "User " + (i + 1), ""));
        }

        return list;
    }
    private void loadRecentUsers(RecyclerView userList, String videoId,String shareLink) {

        liveModel.getRecentChats(20, 0).observe((LifecycleOwner) mActivity, chatList -> {

            if (chatList == null || chatList.isEmpty()) {
                userList.setAdapter(new ShareAdapter(new ArrayList<>(), item -> {}));
                return;
            }

            if (chatList.size() > 0) {
                chatList.remove(0);
            }
            else{

            }

            List<ShareItem> shareItems = new ArrayList<>();

            for (ChatResponse chat : chatList) {
                shareItems.add(
                        new ShareItem(
                                chat.getUserImage(),     // image url
                                chat.getUserName(),             // user name
                                chat.getUserId()            // user id
                        )
                );
            }

            shareItems.add(
                    new ShareItem(
                            R.drawable.invie_friends,      // your profile image
                            "Invite",                  // name below image
                            "invite_friend"            // SPECIAL ID
                    )
            );

            // Set adapter
            userList.setAdapter(
                    new ShareAdapter(shareItems, item -> {

                        if (item.getPackageName().equals("invite_friend")) {
                            shareInvite(shareLink);
                            return;
                        }

                        Intent chatIntent = new Intent(mActivity, ChatActivity.class);
                        chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                        chatIntent.putExtra(Constants.TAG_PARTNER_ID, item.getPackageName());  // userId
                        chatIntent.putExtra(Constants.TAG_PARTNER_IMAGE, item.getImageUrl());      // userImage
                        chatIntent.putExtra(Constants.TAG_PARTNER_NAME, item.getName());       // userName


                        chatIntent.putExtra("shareLink", shareLink);
                        chatIntent.putExtra("from", "homebottom");

                        mActivity.startActivity(chatIntent);

                    })
            );
        });
    }

    private void shareInvite(String shareLink) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareLink);

        Intent shareIntent = Intent.createChooser(sendIntent, "Invite Friend");
        mActivity.startActivity(shareIntent);
    }


    private void shareToApp(String pkg, String msg) {

        try {
            Log.d(TAG, "shareToApp: " + pkg + "  " + msg);

            // ---------- COPY ----------
            if (pkg.equals("copy")) {
                ClipboardManager cm = (ClipboardManager)
                        mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("link", msg));
                Toast.makeText(mActivity, "Link Copied", Toast.LENGTH_SHORT).show();
                return;
            }

            // ---------- SMS ----------
            if (pkg.equals("sms")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:")); // no number
                intent.putExtra("sms_body", msg);
                mActivity.startActivity(intent);
                return;
            }

            // ---------- EMAIL ----------
            if (pkg.equals("email")) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Shared Video");
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                mActivity.startActivity(intent);
                return;
            }

            // ---------- GENERIC SHARE ----------
            if (pkg.equals("share")) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, msg);
                mActivity.startActivity(Intent.createChooser(intent, "Share via"));
                return;
            }


            // ---------- INSTALLED APP SHARE (WhatsApp, Instagram, etc.) ----------
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, msg);
            intent.setPackage(pkg); // Only for real installed package names

            mActivity.startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(mActivity, "App not installed", Toast.LENGTH_SHORT).show();
        }
    }



    private List<ShareItem> getShareApps() {
        List<ShareItem> list = new ArrayList<>();

        /*list.add(new ShareItem(
                repost ? R.drawable.reposted_svg : R.drawable.repost_svg,
                repost ? "Undo Repost" : "Repost",
                "repost"));*/
        list.add(new ShareItem(R.drawable.whatsapp, "WhatsApp", "com.whatsapp"));
        list.add(new ShareItem(R.drawable.whatsapp, "Status", "com.whatsapp"));
        list.add(new ShareItem(R.drawable.link_svg, "Copy Link", "copy"));
        list.add(new ShareItem(R.drawable.messenger, "Messenger", "com.facebook.orca"));
        list.add(new ShareItem(R.drawable.instagram_message, "Instagram Direct", "com.instagram.android"));
        list.add(new ShareItem(R.drawable.sms, "SMS", "sms"));
        list.add(new ShareItem(R.drawable.email, "Email", "email"));
        list.add(new ShareItem(R.drawable.x, "X", "com.twitter.android"));
        list.add(new ShareItem(R.drawable.instagram_stories, "Stories", "com.instagram.android"));
        list.add(new ShareItem(R.drawable.facebook, "Facebook", "com.facebook.katana"));
        list.add(new ShareItem(R.drawable.more, "More", "share"));

        return list;
    }


    private List<ShareItem> getMoreOptions() {
        List<ShareItem> list = new ArrayList<>();
        if (!mHomeApiResponse.get(adapterPosition).getPublisherId().equals(GetSet.getUserId())) {
        list.add(new ShareItem(R.drawable.report_svg, "Report", "report"));}
        if (mHomeApiResponse.get(adapterPosition).getPublisherId().equals(GetSet.getUserId())) {
            list.add(new ShareItem(R.drawable.edit_icon1, "Edit", "edit"));}

        if (!mHomeApiResponse.get(adapterPosition).getPublisherId().equals(GetSet.getUserId())) {
//            list.add(new ShareItem(R.drawable.fav_svg, "favourite", "fav"));
            if(mHomeApiResponse.get(adapterPosition).getVideoIsFavorite()){
                list.add(new ShareItem(R.drawable.fav_filled, "Favourite", "fav"));
            }
            else{
                list.add(new ShareItem(R.drawable.fav_svg, "Favourite", "fav"));
            }
        }
        list.add(new ShareItem(R.drawable.duet_svg, "Duet", "duet"));

        if (mHomeApiResponse.get(adapterPosition).getPublisherId().equals(GetSet.getUserId())) {
//            list.add(new ShareItem(R.drawable.fav_svg, "favourite", "fav"));
//            if(mHomeApiResponse.get(adapterPosition).getVideoIsFavorite()){
                list.add(new ShareItem(R.drawable.delete_svg, "Delete", "delete"));
//            }
//            else{
//                list.add(new ShareItem(R.drawable.fav_svg, "favourite", "fav"));
//            }
        }

        list.add(new ShareItem(R.drawable.not_interested_svg, "Not Interested", "notinterested"));
        list.add(new ShareItem(R.drawable.download_svg, "Download", "download"));
//        list.add(new ShareItem(R.drawable.addtostory, "Add to Story", "addtostory"));
//        list.add(new ShareItem(R.drawable.cast_svg, "Cast", "cast"));
//        list.add(new ShareItem(R.drawable.whythispost_svg, "Why this post", "whythispost"));
//        list.add(new ShareItem(R.drawable.caption, "Caption", "caption"));
//        list.add(new ShareItem(R.drawable.duet_svg, "Duet", "duet"));
//        list.add(new ShareItem(R.drawable.stitch_svg, "Stitch", "stitch"));
//        list.add(new ShareItem(R.drawable.create_sticker_svg, "Create sticker", "createsticker"));
        list.add(new ShareItem(R.drawable.playback_svg, "Playback speed", "playbackspeed"));
//        list.add(new ShareItem(R.drawable.setaswallpaper, "Set as wallpaper", "setaswallpaper"));
        list.add(new ShareItem(R.drawable.shareasgif_svg, "Share as GIF", "shareasgif"));
        return list;
    }

    private void callRepostAPI(String videoId, boolean shouldCreate) {

        String action = shouldCreate ? "create" : "delete";
        String repostDesc = "";

        Call<RepostResponse> call = apiInterface.repostVideo(
                GetSet.getToken(),
                videoId,
                GetSet.getUserId(),
                action,
                repostDesc,
                ""
        );

        call.enqueue(new Callback<RepostResponse>() {
            @Override
            public void onResponse(Call<RepostResponse> call, Response<RepostResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    RepostResponse data = response.body();

                    if (data.getStatus().equals("true")) {

//                        menuDialog.dismiss();
                        dialog.dismiss();
                        // Detect action based on server message
                        if (data.getMessage().toLowerCase().contains("successfully")) {
                            adapterListener.reposted(repost,videoId,adapterPosition,action);
                            if (action.equals("create")) {
                                repost = true;              // Mark as reposted
                                Toast.makeText(mActivity, getString(R.string.Reposted_Successfully), Toast.LENGTH_SHORT).show();
                            } else {
                                repost = false;             // Mark as removed
                                Toast.makeText(mActivity, getString(R.string.Reposted_removed), Toast.LENGTH_SHORT).show();
                            }
                        }

                        changeRepostIcon(repost);

                    } else {
                        Toast.makeText(mActivity, data.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(mActivity, getString(R.string.Reposted_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RepostResponse> call, Throwable t) {
                Toast.makeText(mActivity, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }


    private void changeRepostIcon(boolean repost) {

        List<ShareItem> list = shareAppsAdapter.list;

    /*    for (ShareItem item : list) {
            if (item.getPackageName().equals("repost")) {
                if (repost) {
                    item.setIcon(R.drawable.reposted_svg); // new active icon
                    item.setName("Reposted");
                } else {
                    item.setIcon(R.drawable.repost_svg);
                    item.setName("Repost");
                }
                break;
            }
        }*/

        shareAppsAdapter.notifyDataSetChanged();
    }



    private void openDeleteDialog(String videoId, int adapterPos) {
        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(mActivity.getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title = dialog.findViewById(R.id.title);
        // TextView txtDescription = dialog.findViewById(R.id.txtDescription);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);
        yes.setText(mActivity.getString(R.string.yes_txt));
        no.setText(mActivity.getString(R.string.no_txt));
        title.setText(mActivity.getString(R.string.delete_this_video));
        //   txtDescription.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        //   txtDescription.setText(mActivity.getString(R.string.delete_video_description));

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissMenuDialog();
                deleteVideo(videoId);
                dialog.dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterListener.setPlayback(true, adapterPos, Constants.TAG_PLAYBACK);
                dialog.dismiss();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                adapterListener.setPlayback(true, adapterPos, Constants.TAG_PLAYBACK);
            }
        });
        dialog.show();

    }

    private void deleteVideo(String videoId) {
        if (NetworkReceiver.isConnected()) {
            showProgressDialog();
            Call<Map<String, String>> call = apiInterface.deleteVideoId(GetSet.getUserId(), videoId);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> reportResponse = response.body();
                        if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            adapterListener.setVideoDeleted(true, videoId);
                            App.makeToast(mActivity.getString(R.string.video_deleted_successfully));
                            dialog.dismiss();
                            dismissProgressDialog();
                        } else
                            App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
                        dialog.dismiss();
                    } else {
                        App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
                    }

                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                    App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
                }
            });
        } else {
            Toasty.error(mActivity.getApplicationContext(), mActivity.getApplicationContext().getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void showProgressDialog() {
        if (pdLoading != null && !pdLoading.isShowing()) pdLoading.show();
    }

    private void dismissProgressDialog() {
        if (pdLoading != null && pdLoading.isShowing()) pdLoading.dismiss();
    }

    private void dismissMenuDialog() {
        if (menuDialog != null && menuDialog.isShowing()) menuDialog.dismiss();
    }

    private void favApi(ArrayList<HomeResponse.Result> mHomeApiResponse, int adapterPosition, String type, HomeVideoAdapter.VideoAdapterListener adapterListener) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            map.put(Constants.TAG_ADDIDTOFAV, mHomeApiResponse.get(adapterPosition).getVideoId());
            map.put(Constants.TAG_FAV_TYPE, type);
            Timber.d("sendReport: %s", new Gson().toJson(map));

            Call<Map<String, String>> call = apiInterface.setFav(map);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        Map<String, String> reportResponse = response.body();
                        if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            if (reportResponse.get(Constants.TAG_MESSAGE).equals(Constants.TAG_UNN_FAV)) {
                                adapterListener.setFavourite(false, mHomeApiResponse, adapterPosition);
                                App.dialog(mActivity, "", mActivity.getApplicationContext().getResources().getString(R.string.add_fav_video_removed), mActivity.getApplicationContext().getResources().getColor(R.color.colorWhite));
                            } else {
                                adapterListener.setFavourite(true, mHomeApiResponse, adapterPosition);
                                App.dialog(mActivity, "", mActivity.getApplicationContext().getResources().getString(R.string.add_fav_video_success), mActivity.getApplicationContext().getResources().getColor(R.color.colorWhite));
                            }
                           /* if (menuDialog.isShowing()) {
                                menuDialog.dismiss();
                            }*/
                        } else if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                            App.makeToast("Account is block");
                            GetSet.reset();
                            SharedPref.clearAll();
                            DBHelper.getInstance(mActivity).clearDB();
                            Intent logout = new Intent(mActivity, LoginActivity.class);
                            logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mActivity.startActivity(logout);
                        }
                        else
                            App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
                    } else {
                        App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    call.cancel();
                    App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
                }
            });
        } else {
            Toasty.error(mActivity.getApplicationContext(), mActivity.getApplicationContext().getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void downloadFile(String playbackUrl) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            menuDialog.dismiss();
            Toasty.success(mActivity.getApplicationContext(), mActivity.getString(R.string.video_will_save_in_gallery)).show();
            Data.Builder builder = new Data.Builder();
            builder.putString(Constants.TAG_URL, playbackUrl);
            OneTimeWorkRequest mergeWorkRequest = new OneTimeWorkRequest.Builder(FFmpegVideoDownloadWorker.class)
                    .setConstraints(new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .setInputData(builder.build())
                    .build();
            WorkManager workManager = WorkManager.getInstance(mActivity);
            workManager.enqueue(mergeWorkRequest);
            workManager.getWorkInfoByIdLiveData(mergeWorkRequest.getId()).observe(mActivity, workInfo -> {
                if (workInfo.getState().isFinished()) {
                    if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {

                        String filePath = workInfo.getOutputData().getString(Constants.TAG_FILE_PATH);
//                                    String duration = workInfo.getOutputData().getString(Constants.TAG_AUDIO_DURATION);
                        Timber.tag(TAG).d("downloadFile: %s", filePath);
//                        Toasty.success(mActivity.getApplicationContext(), mActivity.getString(R.string.video_saved_to_gallery)).show();
                    }
                }
            });
        }else{
            if (PermissionsUtils.checkStoragePermission(mActivity)) {
//                menuDialog.dismiss();
                Toasty.success(mActivity.getApplicationContext(), mActivity.getString(R.string.video_will_save_in_gallery)).show();
                Data.Builder builder = new Data.Builder();
                builder.putString(Constants.TAG_URL, playbackUrl);
                OneTimeWorkRequest mergeWorkRequest = new OneTimeWorkRequest.Builder(FFmpegVideoDownloadWorker.class)
                        .setConstraints(new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build())
                        .setInputData(builder.build())
                        .build();
                WorkManager workManager = WorkManager.getInstance(mActivity);
                workManager.enqueue(mergeWorkRequest);
                workManager.getWorkInfoByIdLiveData(mergeWorkRequest.getId()).observe(mActivity, workInfo -> {
                    if (workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {

                            String filePath = workInfo.getOutputData().getString(Constants.TAG_FILE_PATH);
//                                    String duration = workInfo.getOutputData().getString(Constants.TAG_AUDIO_DURATION);
                            Timber.tag(TAG).d("downloadFile: %s", filePath);
//                        Toasty.success(mActivity.getApplicationContext(), mActivity.getString(R.string.video_saved_to_gallery)).show();
                        }
                    }
                });
            }
        }

    }


    private void openReportDialog(ArrayList<HomeResponse.Result> mHomeApiResponse, int adapterPosition, HomeVideoAdapter.VideoAdapterListener adapterListener) {
        View bottomSheet = mActivity.getLayoutInflater().inflate(R.layout.bottom_sheet_report, null);
        reportDialog = new BottomSheetDialog(mActivity, R.style.Bottom_ReportDialog); // Style here
        reportDialog.setContentView(bottomSheet);
        bottomSheet.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        WindowManager.LayoutParams params = reportDialog.getWindow().getAttributes();
        params.x = 0;
        reportDialog.getWindow().setAttributes(params);
        bottomSheet.requestLayout();
        lanCode = LocaleManager.getLanguageCode(mActivity);
        reportsView = bottomSheet.findViewById(R.id.reportView);
        reportsView1 = bottomSheet.findViewById(R.id.reportView1);
        reportsView2 = bottomSheet.findViewById(R.id.reportView2);
        btnBack = bottomSheet.findViewById(R.id.btnBack);
        btnClose = bottomSheet.findViewById(R.id.btnClose);
        txtTitle = bottomSheet.findViewById(R.id.txtTitle);
        selectReasonLay = bottomSheet.findViewById(R.id.selectResonLay);
        selectSubReasonLay = bottomSheet.findViewById(R.id.selectSubResonLay);
        selectSubReasonLay2 = bottomSheet.findViewById(R.id.selectSubResonLay2);
        submitLay = bottomSheet.findViewById(R.id.submitLay);
        textBoxLay = bottomSheet.findViewById(R.id.textBoxLay);
        personBoxLay = bottomSheet.findViewById(R.id.personBoxLay);
        reportEditTxt = bottomSheet.findViewById(R.id.reportEditTxt);
        personEditTxt = bottomSheet.findViewById(R.id.personEditTxt);
        reportReason = bottomSheet.findViewById(R.id.reportReason);
        reportTitle = bottomSheet.findViewById(R.id.reportTitle);
        btnReport = bottomSheet.findViewById(R.id.btnReport);

        btnBack.setVisibility(View.VISIBLE);
        txtTitle.setVisibility(View.VISIBLE);
        txtTitle.setText(R.string.report_video);

        List<UserList.Result> userList = new ArrayList<>();
        UserAdapter adapter = new UserAdapter(mActivity, userList);

        personEditTxt.setAdapter(adapter);

        // API call on text change
        personEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 1) { // only call API after 2+ characters
                    searchUsers(s.toString(), adapter);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

// When user selects a suggestion
        personEditTxt.setOnItemClickListener((parent, view, position, id) -> {
            UserList.Result selectedUser = adapter.getItem(position);
            if (selectedUser != null) {
                personEditTxt.setText(selectedUser.getUserName()); // set inside EditText
                personEditTxt.setSelection(selectedUser.getUserName().length()); // move cursor to end
            }
        });

        btnBack.setOnClickListener(v -> {
//            adapterListener.setPlayback(true, adapterPosition, Constants.TAG_PLAYBACK);
//            reportDialog.dismiss();



            if (submitLay.getVisibility() == View.VISIBLE){
                showSubReportLay();
                if (GetSet.report_subcategory == null){
                    showMainReportLay();
                }
            }
            else if (selectSubReasonLay.getVisibility() == View.VISIBLE){
                showMainReportLay();
            }
            else if (selectSubReasonLay2.getVisibility() == View.VISIBLE){
                showSubReportLay();
            }


        });
        btnClose.setOnClickListener(v -> {
            reportDialog.dismiss();
        });



        btnReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendReport(mHomeApiResponse, reportReasonId, true, adapterPosition,reportEditTxt.getText().toString(),personEditTxt.getText().toString());
            }
        });

        reportDialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            @SuppressLint("CutPasteId") View bottomSheet1 = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet1).setState(BottomSheetBehavior.STATE_EXPANDED);
            BottomSheetBehavior.from(bottomSheet1).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet1).setHideable(true);
        });

        reportDialog.setOnDismissListener(dialogInterface -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet12 = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet12).setState(BottomSheetBehavior.STATE_COLLAPSED);
            BottomSheetBehavior.from(bottomSheet12).setSkipCollapsed(true);
            BottomSheetBehavior.from(bottomSheet12).setHideable(true);
            adapterListener.setPlayback(true, adapterPosition, Constants.TAG_PLAYBACK);
        });

        loadReports(mHomeApiResponse, adapterPosition);
    }

    private void loadReports(ArrayList<HomeResponse.Result> mHomeApiResponse, int adapterPosition) {
        reportAdapter = new ReportAdapter(mActivity, AdminData.reportList, mHomeApiResponse.get(adapterPosition).getVideoname(), adapterPosition, this);
        reportsView.setLayoutManager(new LinearLayoutManager(mActivity.getApplicationContext()));
        reportsView.setAdapter(reportAdapter);
        reportAdapter.notifyDataSetChanged();
        reportDialog.show();
        getReportListFromApi();
    }
    void showMainReportLay(){
        btnBack.setVisibility(View.INVISIBLE);
        selectReasonLay.setVisibility(View.VISIBLE);
        selectSubReasonLay.setVisibility(View.GONE);
        selectSubReasonLay2.setVisibility(View.GONE);
        submitLay.setVisibility(View.GONE);
        GetSet.report_category = null;
        GetSet.report_subcategory = null;
        GetSet.report_childcategory = null;
    }

    void showSubReportLay(){
        btnBack.setVisibility(View.VISIBLE);
        selectReasonLay.setVisibility(View.GONE);
        selectSubReasonLay.setVisibility(View.VISIBLE);
        selectSubReasonLay2.setVisibility(View.GONE);
        submitLay.setVisibility(View.GONE);

    }

    void showSubReportLay2(){
        btnBack.setVisibility(View.VISIBLE);
        selectReasonLay.setVisibility(View.GONE);
        selectSubReasonLay.setVisibility(View.GONE);
        selectSubReasonLay2.setVisibility(View.VISIBLE);
        submitLay.setVisibility(View.GONE);

    }
    void showSubmitLay(){
        btnBack.setVisibility(View.VISIBLE);
        selectReasonLay.setVisibility(View.GONE);
        selectSubReasonLay.setVisibility(View.GONE);
        selectSubReasonLay2.setVisibility(View.GONE);
        submitLay.setVisibility(View.VISIBLE);
    }

    private void searchUsers(String query, UserAdapter adapter) {
        Map<String, String> map = new HashMap<>();
        map.put("token",  GetSet.getAuthToken());
        map.put("offset", "0");
        map.put("type", "username");
        map.put("limit", "30");
        map.put("text", query);
        map.put("user_id", GetSet.getUserId());

        Call<UserList> call = apiInterface.getSearchUser(map);
        call.enqueue(new Callback<UserList>() {
            @Override
            public void onResponse(Call<UserList> call, Response<UserList> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    adapter.updateList(response.body().getResult());
                    personEditTxt.post(() -> {
                        if (!adapter.isEmpty()) {
                            personEditTxt.showDropDown();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<UserList> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    void getReportListFromApi(){

        if (NetworkReceiver.isConnected()) {
            showProgressDialog();
            Call<ReportResponceNew> call = apiInterface.getReportList( lanCode);
            call.enqueue(new Callback<ReportResponceNew>() {
                @Override
                public void onResponse(@NotNull Call<ReportResponceNew> call, @NotNull Response<ReportResponceNew> response) {
                    if (response.isSuccessful()) {
                        dismissProgressDialog();
                        ReportResponceNew reportResponse = response.body();
                        reportResponceNew  = response.body();
                        if (reportResponse.getCategories() != null ) {

                            Log.d(TAG, "onResponse: report list: "+ reportResponse.getCategories());

                            showMainReportLay();
                            mainReportAdapter = new MainReportAdapter(mActivity, reportResponse.getCategories(), mHomeApiResponse.get(adapterPosition).getVideoname(), new OnItemClickLisner() {
                                @Override
                                public void onClick(View v, int position1) {

                                    selectedCategory = reportResponceNew.getCategories().get(position1);
                                    if (selectedCategory.getSubcategoryCount() == 0){

                                        showSubmitLay();
                                        reportTitle.setText(selectedCategory.getTitle());
                                        displayHtmlInTextView(reportReason,selectedCategory.getReasons());
                                        textBoxLay.setVisibility(View.GONE);
                                        personBoxLay.setVisibility(View.GONE);
                                        GetSet.report_category = selectedCategory.getId();
                                        return;


                                    }



                                    showSubReportLay();

                                    GetSet.report_category = selectedCategory.getId();

                                            subReportAdapter = new SubReportAdapter(mActivity, reportResponceNew.getCategories().get(position1).getSubcategories(), new OnItemClickLisner() {
                                        @Override
                                        public void onClick(View v, int position2) {


                                            selectedSubCategory = reportResponceNew.getCategories().get(position1).getSubcategories().get(position2);
                                            GetSet.report_subcategory = selectedSubCategory.getId();
                                            if(!selectedSubCategory.getItsLink().isEmpty()){
                                                String url = selectedSubCategory.getItsLink() ; // your specific link
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                                mActivity.startActivity(intent);
                                                return;
                                            }

                                            if (reportResponceNew.getCategories().get(position1).getSubcategories().get(position2).getSubcategories2().isEmpty()){
                                                showSubmitLay();
                                                reportReasonId = selectedSubCategory.getId();
                                                GetSet.report_subcategory = selectedSubCategory.getId();
                                                if (selectedSubCategory.getTextbox().equals(Constants.TAG_TRUE)){
                                                    textBoxLay.setVisibility(View.VISIBLE);
                                                }
                                                else {
                                                    textBoxLay.setVisibility(View.GONE);
                                                }

                                                if (selectedSubCategory.getPersonSearch().equals(Constants.TAG_TRUE)){
                                                    personBoxLay.setVisibility(View.VISIBLE);
                                                }
                                                else {
                                                    personBoxLay.setVisibility(View.GONE);
                                                }
                                                reportTitle.setText(selectedSubCategory.getTitle());

                                                displayHtmlInTextView(reportReason,reportResponceNew.getCategories().get(position1).getSubcategories().get(position2).getReasons());

                                            }
                                            else {
                                                showSubReportLay2();
                                                subReportAdapter2 = new SubReportAdapter(mActivity, reportResponceNew.getCategories().get(position1).getSubcategories().get(position2).getSubcategories2(), new OnItemClickLisner() {
                                                    @Override
                                                    public void onClick(View v, int position3) {

                                                        selectedSubCategory2 = reportResponceNew.getCategories().get(position1).getSubcategories().get(position2).getSubcategories2().get(position3);
                                                        reportReasonId = selectedSubCategory2.getId();
                                                        GetSet.report_childcategory = selectedSubCategory2.getId();
                                                        if(!selectedSubCategory2.getItsLink().isEmpty()){
                                                            String url = selectedSubCategory2.getItsLink() ; // your specific link
                                                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                                            mActivity.startActivity(intent);
                                                            return;
                                                        }
                                                        showSubmitLay();
                                                        reportTitle.setText(selectedSubCategory2.getTitle());
                                                        if (selectedSubCategory2.getTextbox().equals(Constants.TAG_TRUE)){
                                                            textBoxLay.setVisibility(View.VISIBLE);
                                                        }
                                                        else {
                                                            textBoxLay.setVisibility(View.GONE);
                                                        }
                                                        if (selectedSubCategory2.getPersonSearch().equals(Constants.TAG_TRUE)){
                                                            personBoxLay.setVisibility(View.VISIBLE);
                                                        }
                                                        else {
                                                            personBoxLay.setVisibility(View.GONE);
                                                        }

                                                        displayHtmlInTextView(reportReason,reportResponceNew.getCategories().get(position1).getSubcategories().get(position2).getReasons());



                                                    }
                                                });

                                                reportsView2.setAdapter(subReportAdapter2);

                                            }



                                        }
                                    });

                                    reportsView1.setAdapter(subReportAdapter);


                                }
                            });
                            reportsView.setAdapter(mainReportAdapter);





//                            adapterListener.setVideoDeleted(true, videoId);
//                            App.makeToast(mActivity.getString(R.string.video_deleted_successfully));
//                            dismissProgressDialog();

                        } else{
                            App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
                            dismissProgressDialog();
                        }
                    } else {
                        dismissProgressDialog();
                        App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
                    }

                }

                @Override
                public void onFailure(Call<ReportResponceNew> call, Throwable t) {
                    call.cancel();
                    dismissProgressDialog();
                    Log.d(TAG, "onFailure: somthing went wrong!!!!!!!");
                    App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
                }
            });
        } else {
            Toasty.error(mActivity.getApplicationContext(), mActivity.getApplicationContext().getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }

    }

    public static void displayHtmlInTextView(TextView textView, String htmlContent) {
        Spanned spanned;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(htmlContent);
        }
        textView.setText(spanned);
    }

    @Override
    public void onResponse(Object apiResponse, int adapterPosition) {
        updateReportResponse(apiResponse, adapterPosition);
    }

    private void updateReportResponse(Object apiResponse, int adapterPosition) {
        Response<Map<String, String>> response = (Response<Map<String, String>>) apiResponse;
        if (response.isSuccessful()) {
            Map<String, String> reportResponse = response.body();
            if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                if (reportResponse.get(Constants.TAG_MESSAGE).equals("Reported successfully")) {
                    if (LocaleManager.isRTL()){
                        App.dialog(mActivity, "", mActivity.getApplicationContext().getResources().getString(R.string.reportt_success_alert), mActivity.getApplicationContext().getResources().getColor(R.color.color_green));
                    }else{
                        App.dialog(mActivity, "", mActivity.getApplicationContext().getResources().getString(R.string.report_success_alert), mActivity.getApplicationContext().getResources().getColor(R.color.color_green));

                    }
                    adapterListener.setVideoReport(true, adapterPosition);
//                    adapterListener.setPlayback(true, adapterPosition, Constants.TAG_PLAYBACK);
                } else if (reportResponse.get(Constants.TAG_MESSAGE).equals("Report Undo successfully") || reportResponse.get(Constants.TAG_MESSAGE).equals("Video successfully unreported")) {
                    if(LocaleManager.isRTL()){
                        App.dialog(mActivity, "", mActivity.getApplicationContext().getResources().getString(R.string.undo_reportt_successfully), mActivity.getApplicationContext().getResources().getColor(R.color.colorWhite));

                    }else{
                        App.dialog(mActivity, "", mActivity.getApplicationContext().getResources().getString(R.string.undo_report_successfully), mActivity.getApplicationContext().getResources().getColor(R.color.colorWhite));

                    }
                    adapterListener.setVideoReport(false, adapterPosition);
                }

                if (reportDialog != null && reportDialog.isShowing())
                    reportDialog.dismiss();

                if(dialog.isShowing()){
                    dialog.dismiss();
                }
               /* if (menuDialog.isShowing())
                    menuDialog.dismiss();*/
            } else if (reportResponse.get(Constants.TAG_STATUS).equals(Constants.TAG_ACCOUNT_BLOCKED)) {
                App.makeToast("Account is block");
                GetSet.reset();
                SharedPref.clearAll();
                DBHelper.getInstance(mActivity).clearDB();
                Intent logout = new Intent(mActivity, LoginActivity.class);
                logout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(logout);
            }
            else
                App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
        } else {
            App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
        }
    }

    @Override
    public void onFailure(Throwable t, int adapterPosition) {
        App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
    }

    private void sendReport(ArrayList<HomeResponse.Result> mHomeApiResponse, String title, boolean report, int adapterPosition, String reportEnteredTxt , String personName) {
        if (NetworkReceiver.isConnected()) {
            CommonApi.reportVideo(mHomeApiResponse.get(adapterPosition).getVideoname(), title, report,reportEnteredTxt,personName, new ApiCallback() {
                @Override
                public void onResponse(Object response) {
                    updateReportResponse(response, adapterPosition);
                }

                @Override
                public void onFailure(Throwable t) {
                    Timber.e("sendReport: %s", t.getMessage());
                    App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
                }
            });
        } else {
            Toasty.error(mActivity.getApplicationContext(), mActivity.getApplicationContext().getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }
    private void sendReport(ArrayList<HomeResponse.Result> mHomeApiResponse, String title, boolean report, int adapterPosition) {
        if (NetworkReceiver.isConnected()) {
            CommonApi.reportVideo(mHomeApiResponse.get(adapterPosition).getVideoname(), title, report, new ApiCallback() {
                @Override
                public void onResponse(Object response) {
                    updateReportResponse(response, adapterPosition);
                }

                @Override
                public void onFailure(Throwable t) {
                    Timber.e("sendReport: %s", t.getMessage());
                    App.makeToast(mActivity.getApplicationContext().getString(R.string.something_went_wrong));
                }
            });
        } else {
            Toasty.error(mActivity.getApplicationContext(), mActivity.getApplicationContext().getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
    }

    private void shareVideoDialog(String link) {
        Log.d(TAG, "shareVideoDialog: "+link);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, link);
        mActivity.startActivity(Intent.createChooser(intent, mActivity.getString(R.string.share_link)));
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {

    }

    private void setVideoWithCaption(String videoId,String videoUrl, String captionText) {

        adapterListener.setVideoCaption(true,videoId,adapterPosition,"caption",videoUrl);

    }


}
