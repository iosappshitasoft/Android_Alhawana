package com.app.alhawanajd;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.GONE;

//import static com.app.hiliteapp.R2.id.imageView;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.giphy.sdk.core.GiphyCore;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.GPHSettings;
import com.giphy.sdk.ui.themes.GPHTheme;
import com.giphy.sdk.ui.themes.GridType;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.io.ByteStreams;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.gson.Gson;


import com.app.Zego.internal.ZEGOLiveStreamingManager;
import com.app.addon.livestream.PlayerActivity;
import com.app.addon.livestream.external.model.StreamDetails;
import com.app.addon.livestream.external.utils.StreamConstants;
import com.app.db.DBHelper;
import com.app.external.CustomTypefaceSpan;
import com.app.external.EndlessRecyclerOnScrollListener;
import com.app.external.MediaPlayerUtils;
import com.app.external.ProgressWheel;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.helper.AppWebSocket;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.PermissionsUtils;
import com.app.helper.StorageUtils;
import com.app.helper.callback.OnOkCancelClickListener;
import com.app.helper.workers.FileDownloadWorker;
import com.app.livedata.MessageLiveModel;
import com.app.model.AdminMessageResponse;
import com.app.model.ChatResponse;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;

import com.app.model.event.AdminMessageEvent;
import com.app.utils.AdminData;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.FileUtil;
import com.app.utils.Logging;
import com.app.utils.SharedPref;
import com.app.utils.Util;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.makeramen.roundedimageview.RoundedImageView;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import es.dmoral.toasty.Toasty;
import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class ChatActivity extends BaseFragmentActivity implements AppWebSocket.WebSocketChannelEvents, AdapterView.OnItemClickListener, GiphyDialogFragment.GifSelectionListener  /*ReplyChipAdapter.ClickListener*/, MediaPlayerUtils.Listener, View.OnClickListener {

    private static final String TAG = ChatActivity.class.getSimpleName();

    private ImageView btnBack;
    private TextView txtTitle;
    private TextView txtSubTitle;
    private Toolbar toolbar;
    private ImageView btnGallery;
    private ImageView btnCamera;
    private LinearLayout attachLay;
    private ImageView btnAdd;
    private EditText edtMessage;
    private ImageView btnSend;
    private RelativeLayout edtLay;
    private RecyclerView recyclerView;
    private ImageView btnMenu;
    private TextView txtTyping;
    private Button btnContactUs;
    private LinearLayout profileLayout;
    private ProgressBar progressBar;
    public String partnerId = "";
    private Call<StreamDetails> streamApiCall;
    private ApiInterface apiInterface;
    private ProfileResponse profile;
    String delete_sts;
    private String partnerImage = "";
    private String partnerName = "";
    private DBHelper dbHelper;
    private MessageLiveModel liveModel;
    int currentPage = 0, limit = 20;
    private ChatAdapter adapter;
    public String chatId,shareLink;
    LinearLayoutManager mLayoutManager;
    private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;
    StorageUtils storageUtils;
    Handler mainHandler = new Handler();
    Runnable runnable;
    private String blockStatus = Constants.TAG_FALSE;
    Handler onlineHandler = new Handler();
    int delay = 5000; //milliseconds
    private PopupMenu popupMenu;
    PopupWindow popupWindow;
    boolean btnSendShow = false;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    public static boolean isAdminChat = false, isBackPressed = false;
    public static boolean socketmessages = false;
    private List<ChatResponse> chatList = new ArrayList<>();
    private DialogLoading dialogLoading;
    private AppUtils appUtils;
    private String currentMessageId = null;
    private Parcelable state;
    private ActivityResultLauncher<String[]> cameraPermissionResult;
    private ActivityResultLauncher<String[]> storagePermissionResult;
    private ActivityResultLauncher<Intent> cameraIntentLauncher;
    private ActivityResultLauncher<Intent> galleryIntentLauncher;

    ImageView   videoCall,audioCall;


    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private static final int MEDIA_PERMISSION_CODE = 103;
    private static final int ALLMEDIA_PERMISSION_CODE = 104;


    MediaRecorder mediaRecorder;
    String recordVoicePath = null, socketDuration, languageCode = "";

    private boolean isRecording = false;
    private boolean isPlayplaying = false;

    //   private Context context;
    private MediaPlayer player = new MediaPlayer();
    int playingPosition = -1;
    private SeekBar seekBar;
    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    //Addon Voice Chat
//    private RecordButton btnRecord;
//    private RecordView recordView;
    int recordAudioPermission;

    //Addon Gif
//    private ImageView btnGif;
    GiphyDialogFragment giphyDialogFragment;
    GPHSettings settings = new GPHSettings();

    //Addon Smart Replay
   /* private RecyclerView mSmartRepliesRecycler;
    ReplyChipAdapter mChipAdapter;
    private SmartReplyGenerator smartReply = SmartReply.getClient();*/

    /*Addon Chat Translation*/
//    private ImageView btnLanguage;
    boolean from = false;
    int firstcall = 0;

    private TextView txtAudioTime;
    private Handler seekHandler = new Handler();
    private Runnable moveSeekBarThread = new Runnable() {
        public void run() {
            if (player.isPlaying()) {
                long currentDuration = player.getCurrentPosition();
                if (txtAudioTime != null)
                    txtAudioTime.setText(milliSecondsToTimer(currentDuration));
                int mediaPos_new = player.getCurrentPosition();
                int mediaMax_new = player.getDuration();
                seekBar.setMax(mediaMax_new);
                seekBar.setProgress(mediaPos_new);
                seekHandler.postDelayed(this, 100);
            }
        }
    };

    Runnable onlineRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Constants.TAG_TYPE, Constants.TAG_PROFILE_LIVE);
                jsonObject.put(Constants.TAG_RECEIVER_ID, partnerId);
                Logging.i(TAG, "checkOnline: " + jsonObject);
                AppWebSocket.getInstance(ChatActivity.this).send(jsonObject.toString());
            } catch (JSONException e) {
                Logging.e(TAG, "checkOnline error: " + e.getMessage());
                e.printStackTrace();
            }
            onlineHandler.postDelayed(onlineRunnable, delay);
        }
    };
    private File currentPhotoFile;
    private Uri mCurrentPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        findViews();
        initPermission();
        initActivityResultLauncher();
        initListener();
        //this.context = getApplicationContext();

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        appUtils = new AppUtils(ChatActivity.this);
        from = false;


        if (getIntent().hasExtra(Constants.TAG_FROM)) {
            isAdminChat = getIntent().getStringExtra(Constants.TAG_FROM).equals(Constants.TAG_ADMIN);
        }

        partnerName = getIntent().getStringExtra(Constants.TAG_PARTNER_NAME);
        dbHelper = DBHelper.getInstance(this);
        storageUtils = StorageUtils.getInstance(this);
        liveModel = new ViewModelProvider(ChatActivity.this).get(MessageLiveModel.class);

        if (!isAdminChat) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(1);
            }
            partnerId = getIntent().getStringExtra(Constants.TAG_PARTNER_ID);
            partnerImage = getIntent().getStringExtra(Constants.TAG_PARTNER_IMAGE);
            shareLink = getIntent().getStringExtra("shareLink");
            if(shareLink!=null){
                edtMessage.setText(shareLink);
            }
            chatId = GetSet.getUserId() + partnerId;
            blockStatus = dbHelper.getBlockStatus(chatId);
            dbHelper.updateRecent(chatId, Constants.TAG_UNREAD_COUNT, "0");
            if (getIntent().hasExtra(Constants.TAG_BLOCKED_BY_ME)) {
                blockStatus = getIntent().getStringExtra(Constants.TAG_BLOCKED_BY_ME);
            }
            Log.e("checkChat","chatId "+chatId+" parter "+partnerId);

        } else {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(0);
            }
            chatId = GetSet.getUserId();
            dbHelper.updateRecent(chatId, Constants.TAG_UNREAD_COUNT, "0");
        }

        initView();
        initAddons();

        if (LocaleManager.isRTL()) {
            btnBack.setRotation(180);
        } else {
            btnBack.setRotation(0);
        }
        getProfile(GetSet.getUserId() , partnerId);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            checkBluetoothPermission(this);
        } else {

        }
    }

    private void findViews() {
        btnBack = findViewById(R.id.btnBack);
        txtTitle = findViewById(R.id.txtTitle);
        txtSubTitle = findViewById(R.id.txtSubTitle);
        toolbar = findViewById(R.id.toolbar);
        btnGallery = findViewById(R.id.btnGallery);
        btnCamera = findViewById(R.id.btnCamera);
        attachLay = findViewById(R.id.attachLay);
        btnAdd = findViewById(R.id.btnAdd);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        edtLay = findViewById(R.id.edtLay);
        recyclerView = findViewById(R.id.recyclerView);
        btnMenu = findViewById(R.id.btnMenu);
        txtTyping = findViewById(R.id.txtTyping);
        btnContactUs = findViewById(R.id.btnContactUs);
        profileLayout = findViewById(R.id.profileLayout);
        progressBar = findViewById(R.id.progressBar);
        //Addon Voice Chat
//        btnRecord = findViewById(R.id.btnRecord);
//        recordView = findViewById(R.id.recordView);
//        btnGif = findViewById(R.id.btnGif);
//        btnLanguage = findViewById(R.id.btnLanguage);
        videoCall=findViewById(R.id.video_call);
        audioCall=findViewById(R.id.audio_call);
        //mSmartRepliesRecycler = findViewById(R.id.smartRepliesRecycler);
    }

    private void initListener() {
        btnBack.setOnClickListener(this);
        btnMenu.setOnClickListener(this);
        btnGallery.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
//        btnGif.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnContactUs.setOnClickListener(this);
//        btnLanguage.setOnClickListener(this);
        videoCall.setOnClickListener(this);
        audioCall.setOnClickListener(this);
    }
    public void checkBluetoothPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    1001); // 1001 is your request code
        }
    }
    private void initAddons() {
        if (Constants.ADDON_GIF) {
            GiphyCore.INSTANCE.configure(this, Constants.KEY_GIPHY_API, false);
            settings.setGridType(GridType.waterfall);
            settings.setTheme(GPHTheme.Automatic);
            settings.setMediaTypeConfig(new GPHContentType[]{GPHContentType.sticker, GPHContentType.emoji, GPHContentType.gif, GPHContentType.text});
        }

    /*    if (Constants.ADDON_CHAT_TRANSLATION) {
            btnLanguage.setColorFilter(Color.parseColor("#FF7B66"), PorterDuff.Mode.SRC_IN);
            btnLanguage.setVisibility(GONE);
        }*/

        /*if (Constants.ADDON_SMART_REPLY) {
            LinearLayoutManager chipManager = new LinearLayoutManager(this);
            chipManager.setOrientation(RecyclerView.HORIZONTAL);
            mChipAdapter = new ReplyChipAdapter(this);
            mSmartRepliesRecycler.setLayoutManager(chipManager);
            mSmartRepliesRecycler.setAdapter(mChipAdapter);
        }*/

        /*Addon Voice Chat*/

//        if (Constants.ADDON_VOICE_CHAT) {
//            setVoiceRecorder();
//        }
    }

    private void initPermission() {
        storagePermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {

                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, x.getKey())) {
                                storagePermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(ChatActivity.this, new OnOkCancelClickListener() {
                                    @Override
                                    public void onOkClicked(Object o) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, getString(R.string.storage_error));
                            }
                            break;
                        }
                    }
                }

            }
        });

        cameraPermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                Timber.d("cameraPermissionResult: %s", result);
                boolean granted = true;
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }
                Timber.d("granted: %s", granted);

                if (granted) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        if (PermissionsUtils.checkStoragePermission(ChatActivity.this)) {
                            takeCameraPictures();
                        } else {
                            requestStoragePermissions();
                        }
                    } else {
                        takeCameraPictures();
                    }
                } else {
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, x.getKey())) {
                                cameraPermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Log.e("13", "onResume");
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }else {
                                    PermissionsUtils.openPermissionDialog(ChatActivity.this, new OnOkCancelClickListener() {
                                        @Override
                                        public void onOkClicked(Object o) {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onCancelClicked(Object o) {

                                        }
                                    }, getString(R.string.camera_permission_error));
                                }
                            }
                            break;
                        }
                    }
                }

            }
        });
    }

    private void requestPermissionIfNeeded(List<String> permissions, RequestCallback requestCallback) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }

        PermissionX.init(this).permissions(permissions).onExplainRequestReason((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(Manifest.permission.CAMERA)) {
                    message = this.getString(R.string.permission_explain_camera);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(Manifest.permission.CAMERA)) {
                        message = this.getString(R.string.permission_explain_camera);
                    }
                } else {
                    message = this.getString(R.string.permission_explain_camera);
                }
            }
            scope.showRequestReasonDialog(deniedList, message, getString(R.string.ok));
        }).onForwardToSettings((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(Manifest.permission.CAMERA)) {
                    message = this.getString(R.string.settings_camera);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(Manifest.permission.CAMERA)) {
                        message = this.getString(R.string.settings_camera);
                    }
                } else {
                    message = this.getString(R.string.permission_explain_camera);
                }
            }
            scope.showForwardToSettingsDialog(deniedList, message, getString(R.string.settings),
                    getString(R.string.cancel));
        }).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                                 @NonNull List<String> deniedList) {
                if (requestCallback != null) {
                    requestCallback.onResult(allGranted, grantedList, deniedList);
                }
            }
        });
    }

    private void initActivityResultLauncher() {
        cameraIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
//                    Uri finalImageUri = storageUtils.getUriFromFile(currentPhotoFile);
                    String fileName = getFileName(currentPhotoFile.getAbsolutePath());
                    JSONObject jsonObject = updateDB(Constants.TAG_IMAGE, currentPhotoFile.getAbsolutePath(), fileName, "", Constants.TAG_SENDING);
                    try {
                        Bitmap compressedBitmap = compressImage(currentPhotoFile.getAbsolutePath()); // Replace deprecated logic
                        deleteFileForUri(mCurrentPhotoUri);
                        mCurrentPhotoUri = null;
                        String savedPath = storageUtils.saveImageInStorage(compressedBitmap, fileName, Constants.TAG_SENT).getAbsolutePath();
                        byte[] imageBytes = ByteStreams.toByteArray(new FileInputStream(savedPath));
                        uploadChatImage(imageBytes, fileName, "", jsonObject);
                        compressImage(currentPhotoFile.getAbsolutePath());
                    } catch (Exception e) {
                        Timber.tag(TAG).e("Error during image compression: %s", e.getMessage());
                        Toasty.error(ChatActivity.this, R.string.something_went_wrong).show();
                    }

                }

            }
        });


        galleryIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    ArrayList<Uri> selectedPhotos = result.getData().getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                    Uri imageUri =selectedPhotos.get(0);

                    String fileName = getString(R.string.app_name) + System.currentTimeMillis() + Utility.IMAGE_FORMAT;
                    Bitmap bitmap = FileUtil.decodeBitmap(ChatActivity.this, imageUri);
                    String savedPath = storageUtils.saveImageInStorage(bitmap, fileName, Constants.TAG_SENT).getAbsolutePath();
                    try {
                        byte[] imageBytes = ByteStreams.toByteArray(new FileInputStream(savedPath));
                        JSONObject jsonObject = updateDB(Constants.TAG_IMAGE, fileName, fileName, "", Constants.TAG_SENDING);
                        uploadChatImage(imageBytes, fileName, "", jsonObject);
                    } catch (Exception e) {
                        Timber.tag(TAG).e("galleryIntentLauncher: %s", e.getMessage());
                        Toasty.error(ChatActivity.this, R.string.something_went_wrong).show();
                    }
                }

            }
        });
    }
    private Bitmap compressImage(String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // Only load image dimensions
        BitmapFactory.decodeFile(imagePath, options);

        // Calculate the sample size to resize the image
        options.inSampleSize = calculateInSampleSize(options, 1024, 1024); // Adjust target width/height as needed
        options.inJustDecodeBounds = false; // Load full image with reduced size

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

        // Further compress the bitmap to reduce file size
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outStream); // Adjust quality as needed
        return bitmap;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
    //Addon Smart Reply
   /* @Override
    public void onChipClick(@NonNull String chipText) {
        if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
            App.makeToast(getString(R.string.unblock_description));
        } else {
            sendChat(Constants.TAG_TEXT, "", "", "", chipText);
        }
    }*/

    private void initView() {
        if (!Constants.ADDON_VOICE_CHAT) {
            SlidrConfig config = new SlidrConfig.Builder()
                    .position(SlidrPosition.LEFT)
                    .sensitivity(1f)
                    .velocityThreshold(2400)
                    .distanceThreshold(0.25f)
                    .build();
            Slidr.attach(this, config);
        }

        initLoadingDialog();
        txtTitle.setText(partnerName);
        adapter = new ChatAdapter(ChatActivity.this);
        mLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);

        if (isAdminChat) {
            btnContactUs.setVisibility(View.VISIBLE);
            txtSubTitle.setVisibility(GONE);
            txtTyping.setVisibility(GONE);
            edtMessage.setVisibility(GONE);
            edtLay.setVisibility(GONE);
            btnMenu.setVisibility(GONE);
//            btnLanguage.setVisibility(GONE);
            videoCall.setVisibility(GONE);
            audioCall.setVisibility(GONE);
            getMessageList(currentPage = 0);
            getAdminMessages();
        } else {
        /*    btnLanguage.setVisibility(Constants.ADDON_CHAT_TRANSLATION ? View.VISIBLE : GONE);
            btnGif.setVisibility(Constants.ADDON_GIF ? GONE : GONE);*/
            //Addon Voice Chat
//            btnRecord.setVisibility(Constants.ADDON_VOICE_CHAT ? View.VISIBLE : GONE);
            if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                txtSubTitle.setVisibility(GONE);
            } else {
                txtSubTitle.setVisibility(View.VISIBLE);
            }
            profileLayout.setOnClickListener(view -> {
                if(isRecording)
                    return;
                Intent profile = new Intent(getApplicationContext(), OthersProfileActivity.class);
                profile.putExtra(Constants.TAG_USER_ID, partnerId);
                profile.putExtra(Constants.TAG_USER_IMAGE, partnerImage);
                profile.putExtra(Constants.TAG_FROM, Constants.TAG_MESSAGE);
                profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(profile);
            });
            getMessageList(currentPage = 0);
        }

        edtMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(edtMessage, InputMethodManager.SHOW_FORCED);
                hideAttachment();
            }
        });

        edtMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (runnable != null) {
                    mainHandler.removeCallbacks(runnable);
                }

                if (charSequence.length() > 0) {
                    setTyping(true);

                    if (isRTLText(charSequence)) {
                        Log.d(TAG, "Detected RTL");
                        edtMessage.setTextDirection(View.TEXT_DIRECTION_RTL);
                        edtMessage.setGravity(Gravity.END | Gravity.TOP);
                    } else {
                        Log.d(TAG, "Detected LTR");
                        edtMessage.setTextDirection(View.TEXT_DIRECTION_LTR);
                        edtMessage.setGravity(Gravity.START | Gravity.TOP);
                    }

                } else {
                    setTyping(false);
                    edtMessage.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
                    edtMessage.setGravity(Gravity.START | Gravity.TOP);
                }
            }


            @Override
            public void afterTextChanged(Editable editable) {
                runnable = new Runnable() {
                    public void run() {
                        setTyping(false);
                    }
                };
                mainHandler.postDelayed(runnable, 1000);

                if (editable.length() > 0 && !btnSendShow) {
                    btnSendShow = true;
//                    slideOutAnim();


                } else if (editable.length() == 0) {
                    btnSendShow = false;
//                    slideInAnim();
                }
            }
        });

        endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                getMessageList(currentPage = page);
            }
        };
        endlessRecyclerOnScrollListener.resetState();
        recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);
        recyclerView.scrollToPosition(0);

        if (!isAdminChat) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dbHelper.updateMessageReadStatus(chatId, GetSet.getUserId());
                    if (blockStatus != null && blockStatus.equals(Constants.TAG_FALSE)) {
                        sendReadStatus();
                    }
                }
            }, 2000);
        }
    }
    private boolean isRTLText(CharSequence text) {
        for (int i = 0; i < text.length(); i++) {
            byte dir = Character.getDirectionality(text.charAt(i));
            if (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                    dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC) {
                return true;
            } else if (dir == Character.DIRECTIONALITY_LEFT_TO_RIGHT) {
                return false;
            }
        }
        return false; // fallback
    }

    private void slideOutAnim() {
        //Addon Voice Chat
//        btnRecord.setVisibility(GONE);
        btnSend.setVisibility(View.VISIBLE);
        if (LocaleManager.isRTL()) {
            btnSend.setRotation(180);
            TranslateAnimation animate = new TranslateAnimation(
                    -50,
                    0,
                    0,
                    0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            btnSend.startAnimation(animate);
        } else {
            btnSend.setRotation(0);
            btnSend.setVisibility(View.VISIBLE);
            TranslateAnimation animate = new TranslateAnimation(
                    -50,
                    0,
                    0,
                    0);
            animate.setDuration(500);
            animate.setFillAfter(true);
            btnSend.startAnimation(animate);
        }
    }

    private void slideInAnim() {
        TranslateAnimation animate = new TranslateAnimation(
                0,
                -50,
                0,
                0);
        animate.setDuration(200);
        animate.setFillAfter(false);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                btnSend.setVisibility(GONE);
                //Addon Voice Chat
//                btnRecord.setVisibility(Constants.ADDON_VOICE_CHAT ? View.VISIBLE : GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        btnSend.startAnimation(animate);
    }

    private void getMessageList(int offset) {
        if (isAdminChat) {
            liveModel.getAdminMessages(this, chatId, limit, offset * limit)
                    .observe(this, new Observer<List<ChatResponse>>() {
                        @Override
                        public void onChanged(List<ChatResponse> chatResponses) {
                            adapter.setData(chatResponses);
                        }
                    });

        } else {
            liveModel.getMessages(this, chatId, limit, offset * limit).observe(this, new Observer<List<ChatResponse>>() {
                @Override
                public void onChanged(List<ChatResponse> chatResponses) {

                    Timber.i("getMessageList: %s", SharedPref.getString(SharedPref.CHAT_LANGUAGE, SharedPref.DEFAULT_CHAT_LANGUAGE+" =, "+new Gson().toJson(chatResponses)));
                    adapter.setData(chatResponses);

                    // smart replay
                  /*  if (chatResponses.size() > 0) {
                        for (ChatResponse messagesData : chatResponses) {
                            if (messagesData.getMessageType().equals(Constants.TAG_TEXT)) {
                                if (messagesData.getUserId() != null && !messagesData.getUserId().equals(chatId)) {
                                    Log.i(TAG, "generateRepliesonChanged: " + messagesData.getMessage());
                                    //  generateReplies(messagesData);
                                }
                                break;
                            } else {
                                //mSmartRepliesRecycler.setVisibility(View.GONE);
                            }
                        }
                    }*/

                    /*Chat Translation*/

                   /* if (chatResponses.size() > 0) {
                        for (ChatResponse messagesData : chatResponses) {
                            Log.d(TAG, "onChanged: "+chatResponses.size());
                            if (messagesData.getMessageEnd().equals(Constants.TAG_RECEIVED) && messagesData.getMessageType().equals(Constants.TAG_TEXT)) {
                                if (messagesData.getUserId() != null && !messagesData.getUserId().equals(chatId)) {
                                    if (SharedPref.getString(SharedPref.CHAT_LANGUAGE, SharedPref.DEFAULT_CHAT_LANGUAGE).equals("")) {
                                        messagesData.setMessage(messagesData.getMessage());
                                        adapter.setData(chatResponses);
                                    } else {
                                        String message = messagesData.getMessage();
                                        new TranslateLanguageTasks(ChatActivity.this, message, from) {
                                            @Override
                                            protected void onPostExecute(String translatedMsg) {
                                                messagesData.setMessage(translatedMsg);
                                                adapter.setData(chatResponses);
                                                adapter.notifyItemChanged(0);
                                                recyclerView.smoothScrollToPosition(0);
                                                //adapter.notifyDataSetChanged();
                                            }
                                        }.execute();
                                    }
                                } else
                                    adapter.setData(chatResponses);
                            } else {
                                adapter.setData(chatResponses);
                            }
                            break;
                        }
                    } else
                        adapter.setData(chatResponses);*/
                }
            });
        }
    }

    private void getAdminMessages() {
        if (NetworkReceiver.isConnected()) {
            long lastTime = 0L;
            ChatResponse lastMessage;
            lastMessage = dbHelper.getLastAdminMessage();
            if (lastMessage != null && lastMessage.getCreatedAt() != null) {
                lastTime = Long.parseLong(lastMessage.getCreatedAt());
            }
            Call<AdminMessageResponse> call = apiInterface.getAdminMessages(GetSet.getUserId(), Constants.TAG_ANDROID, GetSet.getCreatedAt(), lastTime);
            Log.d(TAG, "getAdminMessages: " + GetSet.getUserId() + Constants.TAG_ANDROID + GetSet.getCreatedAt() + "     " + lastTime);
            call.enqueue(new Callback<AdminMessageResponse>() {
                @Override
                public void onResponse(Call<AdminMessageResponse> call, Response<AdminMessageResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        for (AdminMessageResponse.MessageData messageData : response.body().getMessageData()) {
                            if (messageData.getMsgPlatform().equals(Constants.TAG_ANDROID)) {
                                dbHelper.addAdminMessage(messageData);
                                dbHelper.addAdminRecentMessage(messageData, 0);
                                ChatResponse chatResponse = new ChatResponse();
                                chatResponse.setChatId(GetSet.getUserId());
                                chatResponse.setReceiverId(GetSet.getUserId());
                                chatResponse.setMessageType(messageData.getMsgType());
                                chatResponse.setMessageEnd(Constants.TAG_RECEIVE);
                                chatResponse.setMessage(messageData.getMsgData());
                                chatResponse.setMessageId(messageData.getMsgId());
                                chatResponse.setChatTime(messageData.getCreateaAt());
                                chatResponse.setCreatedAt(messageData.getMsgAt());
                                chatResponse.setChatType(Constants.TAG_ADMIN);
                                liveModel.addAdminMessage(chatResponse);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<AdminMessageResponse> call, Throwable t) {

                }
            });
        } else {
            getMessageList(currentPage = 0);
        }
    }

    private void setTyping(boolean isTyping) {
        if (blockStatus != null && !blockStatus.equals(Constants.TAG_TRUE)) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Constants.TAG_TYPE, Constants.TAG_USER_TYPING);
                jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
                jsonObject.put(Constants.TAG_RECEIVER_ID, partnerId);
                jsonObject.put(Constants.TAG_TYPING_STATUS, isTyping ? Constants.TAG_TYPING : Constants.TAG_UNTYPING);
                Logging.i(TAG, "setTyping: " + jsonObject);
                AppWebSocket.getInstance(this).send(jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendReadStatus() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.TAG_TYPE, Constants.TAG_UPDATE_READ);
            jsonObject.put(Constants.TAG_USER_ID, GetSet.getUserId());
            jsonObject.put(Constants.TAG_RECEIVER_ID, partnerId);
            Log.i(TAG, "sendReadStatus: " + jsonObject.toString());
            AppWebSocket.getInstance(this).send(jsonObject.toString());
        } catch (JSONException e) {
            Log.i(TAG, "sendReadStatus: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
        if (isConnected) {
            AppWebSocket.getInstance(this).setWebSocketClient(null);
            AppWebSocket.setNullInstance();
            AppWebSocket.getInstance(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appUtils.resumeExternalAudio();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        AppWebSocket.setCallEvents(this);
        blockStatus = dbHelper.getBlockStatus(chatId);
        Log.i(TAG, "onResume: " + blockStatus);
        checkOnline();
        getProfile(GetSet.getUserId() , partnerId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        try {
            AppWebSocket.setCallEvents(null);
        } catch (Exception e) {
            Logging.e(TAG, "onDestroy: " + e.getMessage());
        }
        onlineHandler.removeCallbacks(onlineRunnable);
        AppUtils.hideKeyboard(ChatActivity.this);
        stopAudioViewHolder();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterNetworkReceiver();
    }

    private void checkOnline() {
        if (blockStatus != null && !blockStatus.equals(Constants.TAG_TRUE)) {
            onlineHandler.post(onlineRunnable);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdminMessageEvent(AdminMessageEvent adminMessageEvent) {
        getAdminMessages();
    }

    //Addon Gif
    @Override
    public void didSearchTerm(@NotNull String s) {

    }

    @Override
    public void onDismissed(@NotNull GPHContentType gphContentType) {
        hideAttachment();
    }

    @Override
    public void onGifSelected(@NotNull Media media, @org.jetbrains.annotations.Nullable String s, @NotNull GPHContentType gphContentType) {
        hideAttachment();

        JSONObject jsonObject = updateDB(Constants.TAG_GIF, "" + media.getImages().getFixedHeightDownsampled().getGifUrl(), "", "", Constants.TAG_SENDING);
        sendFileChat(jsonObject, jsonObject.optString(Constants.TAG_MESSAGE));
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = getString(R.string.app_name) + System.currentTimeMillis() + Utility.IMAGE_FORMAT;
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = storageUtils.getDirectory(StorageUtils.TAG_IMAGE_SENT, true);
        File image = new File(storageDir, imageFileName);
        if (!image.exists()) {
            image.createNewFile();
        }
        Timber.tag(TAG).d("createImageFile: %s", image.getName());

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoFile = image;
        return image;
    }

    private void pickImage() {

        View contentView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_pick_options, findViewById(R.id.parentLay), false);
        BottomSheetDialog pickerOptionsSheet = new BottomSheetDialog(this, R.style.SimpleBottomDialog);
        pickerOptionsSheet.setCanceledOnTouchOutside(true);
        pickerOptionsSheet.setContentView(contentView);
        pickerOptionsSheet.setDismissWithAnimation(true);

        View layoutCamera = contentView.findViewById(R.id.container_camera_option);
        View layoutGallery = contentView.findViewById(R.id.container_gallery_option);

        layoutCamera.setOnClickListener(v -> {
            pickerOptionsSheet.dismiss();
            if (PermissionsUtils.checkCameraPermission(ChatActivity.this)) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    if (PermissionsUtils.checkStoragePermission(ChatActivity.this)) {
                        takeCameraPictures();
                    } else {
                        requestStoragePermissions();
                    }
                } else {
                    takeCameraPictures();
                }
            } else {
                requestCameraPermissions();
            }
        });
        layoutGallery.setOnClickListener(v -> {
            pickerOptionsSheet.dismiss();
            if (PermissionsUtils.checkStoragePermission(ChatActivity.this)) {
                FilePickerBuilder.getInstance()
                        .setMaxCount(1)
                        .setActivityTheme(R.style.MainTheme)
                        .setActivityTitle(getString(R.string.please_select_media))
                        .enableVideoPicker(false)
                        .enableImagePicker(true)
                        .enableCameraSupport(false)
                        .showGifs(false)
                        .showFolderView(false)
                        .enableSelectAll(false)
                        .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .pickPhoto(this, galleryIntentLauncher);

//                pickGalleryPictures();
            } else {
                allrequestMediaPermissions();
            }
        });
        pickerOptionsSheet.show();
    }
    private void requestCameraPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Camera permission is needed to capture photos and videos", Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Storage permission is needed to save photos and videos", Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }
    private void requestMediaPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
        }, MEDIA_PERMISSION_CODE);
    }
    private void allrequestMediaPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
        }, ALLMEDIA_PERMISSION_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (PermissionsUtils.checkCameraPermission(ChatActivity.this)){
                        requestStoragePermission();
                    }else {
                        requestMediaPermissions();
                    }

                } else {
                    Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case STORAGE_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takeCameraPictures();
                } else {
                }
                break;
            case MEDIA_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takeCameraPictures();
                } else {
                    Toast.makeText(this, "Media Permissions Denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case ALLMEDIA_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickGalleryPictures();
                } else {
                    Toast.makeText(this, "Media Permissions Denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case 1001:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                break;
        }
    }
    private void getProfile(String getUserId, String userName) {
        if(!userName.isEmpty()){
            progressBar.setVisibility(View.VISIBLE);
            edtMessage.setEnabled(false);
            btnMenu.setEnabled(false);
            if (GetSet.getUserId() != null && apiInterface != null) {
                ProfileRequest request = new ProfileRequest();
                request.setUserId(GetSet.getUserId());
                request.setProfileId(partnerId);
                Timber.d("getProfilechat params: %s", App.getGsonPrettyInstance().toJson(request));
                Call<ProfileResponse> call = apiInterface.getProfile(request);

                call.enqueue(new Callback<ProfileResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                        if (response.isSuccessful()) {
                            progressBar.setVisibility(GONE);
                            edtMessage.setEnabled(true);
                            btnMenu.setEnabled(true);
                            profile = response.body();
                            if (profile.getStatus().equals(Constants.TAG_TRUE)) {
                               /* dbHelper.updateChat(chatId,Constants.TAG_USER_NAME,response.body().getName());
                                txtTitle.setText(response.body().getName());*/
                                if (delete_sts == null) {
                                    delete_sts = profile.getDeleted_status();
                                    Log.d(TAG, "onResponsedelete_sts: "+delete_sts);
                                    if (delete_sts.equals("1")){
                                        opendeleteDialog();
                                    }else{
                                        progressBar.setVisibility(GONE);
                                    }
                                }
                            } else if(profile.getStatus().equalsIgnoreCase(Constants.TAG_DEACTIVATED)){
                                App.showErrorDialog(ChatActivity.this,"Deactivated",getString(R.string.account_deactivated_by_admin));
                            }

                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ProfileResponse> call, @NotNull Throwable t) {
                        call.cancel();
                        progressBar.setVisibility(GONE);
                    }
                });
            }

        }

    }

    private void opendeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setMessage(getString(R.string.notification_del_acc));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_regular);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_regular);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);

    }
    private void takeCameraPictures() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureIntent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Timber.e(ex);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCurrentPhotoUri = FileProvider.getUriForFile(this,
                        getString(R.string.file_provider_authority),
                        photoFile);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri);
                cameraIntentLauncher.launch(captureIntent);
            }
        }
    }

    private void pickGalleryPictures() {
        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(collection, "image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        Intent chooserIntent = Intent.createChooser(pickIntent, "Select a picture");

        if (chooserIntent.resolveActivity(getPackageManager()) != null) {
            galleryIntentLauncher.launch(chooserIntent);
        }
    }

    @Override
    public void onBackPressed() {
        isBackPressed = true;

        if (streamApiCall != null) {
            streamApiCall.cancel();
        }
        isAdminChat = false;
        if (isTaskRoot()) {

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
//            EventBus.getDefault().post(new HideIcon(true));
            finish();
        } else {
            super.onBackPressed();
        }
    }


    private void sendEmail() {
        try {

            String reportContent = "";
            if (LocaleManager.isRTL()) {
                reportContent = "\n\n" +"\nيرجى كتابة ملاحظاتك فوق هذا السطر"+
                        "رمز إصدار نظام تشغيل الجهاز: " + Build.VERSION.SDK_INT + "\n" +
                        "اسم رمز إصدار الجهاز: " + Build.VERSION.CODENAME + "\n" +
                        "اسم الجهاز: " + AppUtils.getDeviceName() + "\n" +
                        "رمز الإصدار: " + BuildConfig.VERSION_CODE + "\n" +
                        "اسم الإصدار: " + BuildConfig.VERSION_NAME + "\n" +
                        "اسم العبوة: " + BuildConfig.APPLICATION_ID + "\n" +
                        "نوع المبنى: " + BuildConfig.BUILD_TYPE;
            }else {
                reportContent = "\n\n"+"Please write your feedback above this line.\n" +

                        "DEVICE OS VERSION CODE: " + Build.VERSION.SDK_INT + "\n" +
                        "DEVICE VERSION CODE NAME: " + Build.VERSION.CODENAME + "\n" +
                        "DEVICE NAME: " + AppUtils.getDeviceName() + "\n" +
                        "VERSION CODE: " + BuildConfig.VERSION_CODE + "\n" +
                        "VERSION NAME: " + BuildConfig.VERSION_NAME + "\n" +
                        "PACKAGE NAME: " + BuildConfig.APPLICATION_ID + "\n" +
                        "BUILD TYPE: " + BuildConfig.BUILD_TYPE;
            }


            final Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
            emailIntent.putExtra(Intent.EXTRA_EMAIL,
                    new String[]{"" + AdminData.contactEmail});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
            emailIntent.putExtra(Intent.EXTRA_TEXT, reportContent);
            try {
                //start email intent
                startActivity(Intent.createChooser(emailIntent, "Email"));
            } catch (Exception e) {
                //if any thing goes wrong for example no email client application or any exception
                //get and show exception message
                App.makeToast(e.getMessage());
            }
        } catch (Exception e) {
            Logging.e(TAG, "sendEmail: " + e.getMessage());
        }
    }

//    private void openMenu(View view) {
//        popupMenu = new PopupMenu(ChatActivity.this, btnMenu, R.style.CustomPopupMenu);
//        popupMenu.getMenuInflater().inflate(R.menu.chat_menu, popupMenu.getMenu());
////        PopupMenu popupMenu = new PopupMenu(this, btnMenu);
////        popupMenu.getMenuInflater().inflate(R.menu.chat_menu, popupMenu.getMenu());
//
    //// Show popup first
//        popupMenu.setGravity(Gravity.START);
//        if (blockStatus.equals(Constants.TAG_TRUE))
//            popupMenu.getMenu().getItem(1).setTitle(getString(R.string.unblock));
//        else popupMenu.getMenu().getItem(1).setTitle(getString(R.string.block));
//
//        Typeface typeface;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            typeface = getResources().getFont(R.font.font_light);
//        } else {
//            typeface = ResourcesCompat.getFont(this, R.font.font_light);
//        }
//        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
//            MenuItem menuItem = popupMenu.getMenu().getItem(i);
//            SpannableString mNewTitle = new SpannableString(menuItem.getTitle());
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                mNewTitle.setSpan(new TypefaceSpan(typeface), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//            } else {
//                mNewTitle.setSpan(new CustomTypefaceSpan("", typeface), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//            }
//            menuItem.setTitle(mNewTitle);
//        }
//
//        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            public boolean onMenuItemClick(MenuItem item) {
//                if (item.getTitle().toString().equals(getString(R.string.clear_chat))) {
//                    openClearChatDialog();
//                } else if (item.getTitle().toString().equals(getString(R.string.block))) {
//                    JSONObject json = new JSONObject();
//                    try {
//                        json.put(Constants.TAG_TYPE, Constants.TAG_BLOCK_USER);
//                        json.put(Constants.TAG_RECEIVER_ID, partnerId);
//                        json.put(Constants.TAG_BLOCKED, true);
//                        Logging.i(TAG, "blockUser: " + json);
//                        AppWebSocket.getInstance(ChatActivity.this).send(json.toString());
//                        blockUnBlockUser(partnerId);
//                        if (dbHelper.isChatIdExists(chatId)) {
//                            dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_TRUE);
//                            dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
//                            dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
//                        } else {
//                            dbHelper.insertBlockStatus(chatId, partnerId, partnerName, partnerImage, Constants.TAG_TRUE);
//                            if (dbHelper.isChatIdExists(chatId)) {
//                                dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
//                                dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
//                            }
//                        }
//                        EventBus.getDefault().postSticky(partnerId);
//                        blockStatus = Constants.TAG_TRUE;
//                        onlineHandler.removeCallbacks(onlineRunnable);
//                        txtSubTitle.setVisibility(GONE);
//                        txtTyping.setVisibility(GONE);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    popupMenu.dismiss();
//                    App.makeToast(getString(R.string.user_blocked_successfully));
//                } else if (item.getTitle().toString().equals(getString(R.string.unblock))) {
//                    JSONObject json = new JSONObject();
//                    try {
//                        json.put(Constants.TAG_TYPE, Constants.TAG_BLOCK_USER);
//                        json.put(Constants.TAG_RECEIVER_ID, partnerId);
//                        json.put(Constants.TAG_BLOCKED, false);
//                        Logging.i(TAG, "UnBlockUser: " + json);
//                        AppWebSocket.getInstance(ChatActivity.this).send(json.toString());
//                        blockUnBlockUser(partnerId);
//                        if (dbHelper.isChatIdExists(chatId)) {
//                            dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, Constants.TAG_FALSE);
//                        } else {
//                            dbHelper.insertBlockStatus(chatId, partnerId, partnerName, partnerImage, Constants.TAG_FALSE);
//                        }
//                        EventBus.getDefault().postSticky(partnerId);
//                        blockStatus = Constants.TAG_FALSE;
//                        txtSubTitle.setVisibility(View.VISIBLE);
//                        checkOnline();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    popupMenu.dismiss();
//                    App.makeToast(getString(R.string.unblocked_successfully));
//                }
//                return true;
//            }
//        });
//        popupMenu.show();
//
//        try {
//            Field mPopup = popupMenu.getClass().getDeclaredField("mPopup");
//            mPopup.setAccessible(true);
//            Object menuPopupHelper = mPopup.get(popupMenu);
//            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
//
//            Method getPopupWindow = classPopupHelper.getDeclaredMethod("getPopup");
//            getPopupWindow.setAccessible(true);
//            Object popupWindow = getPopupWindow.invoke(menuPopupHelper);
//
//            if (popupWindow instanceof PopupWindow) {
//                ((PopupWindow) popupWindow).setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_card_curve));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }


    private void openMenu(View anchorView) {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_custom_popup, null);
        final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        TextView clearChat = view.findViewById(R.id.menu_clear_chat);
        TextView block = view.findViewById(R.id.menu_block);

        // Set font if needed
        Typeface typeface = ResourcesCompat.getFont(this, R.font.font_light);
        clearChat.setTypeface(typeface);
        block.setTypeface(typeface);

        // Dynamic block/unblock text
        block.setText(blockStatus.equals(Constants.TAG_TRUE) ? getString(R.string.unblock) : getString(R.string.block));

        clearChat.setOnClickListener(v -> {
            popupWindow.dismiss();
            openClearChatDialog();
        });

        block.setOnClickListener(v -> {
            popupWindow.dismiss();
            boolean isBlocking = block.getText().toString().equals(getString(R.string.block));
            JSONObject json = new JSONObject();
            try {
                json.put(Constants.TAG_TYPE, Constants.TAG_BLOCK_USER);
                json.put(Constants.TAG_RECEIVER_ID, partnerId);
                json.put(Constants.TAG_BLOCKED, isBlocking);
                Logging.i(TAG, (isBlocking ? "blockUser: " : "unBlockUser: ") + json);
                AppWebSocket.getInstance(ChatActivity.this).send(json.toString());
                blockUnBlockUser(partnerId);

                if (dbHelper.isChatIdExists(chatId)) {
                    dbHelper.updateChatDB(chatId, Constants.TAG_BLOCKED_BY_ME, isBlocking ? Constants.TAG_TRUE : Constants.TAG_FALSE);
                    dbHelper.updateChatDB(chatId, Constants.TAG_ONLINE_STATUS, Constants.TAG_FALSE);
                    dbHelper.updateChatDB(chatId, Constants.TAG_TYPING_STATUS, Constants.TAG_UNTYPING);
                } else {
                    dbHelper.insertBlockStatus(chatId, partnerId, partnerName, partnerImage, isBlocking ? Constants.TAG_TRUE : Constants.TAG_FALSE);
                }

                EventBus.getDefault().postSticky(partnerId);
                blockStatus = isBlocking ? Constants.TAG_TRUE : Constants.TAG_FALSE;

                if (isBlocking) {
                    onlineHandler.removeCallbacks(onlineRunnable);
                    txtSubTitle.setVisibility(GONE);
                    txtTyping.setVisibility(GONE);
                    App.makeToast(getString(R.string.user_blocked_successfully));
                } else {
                    txtSubTitle.setVisibility(View.VISIBLE);
                    checkOnline();
                    App.makeToast(getString(R.string.unblocked_successfully));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        // Apply rounded background and show
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(10f);

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, location[0], location[1] + anchorView.getHeight());
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    private void openClearChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setMessage(getString(R.string.really_want_clear));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(isRecording)
                    return;
                dialog.cancel();
                hideKeyboard(ChatActivity.this);
                dbHelper.deleteMessages(chatId);
                if (MessageLiveModel.msgLiveData != null) {
                    MessageLiveModel.msgLiveData.setValue(new ArrayList<>());
                }
                AppUtils.hideKeyboard(ChatActivity.this);
                // popupMenu.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_light);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_light);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);

    }

    private void blockUnBlockUser(String partnerId) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_BLOCK_USER_ID, partnerId);
            Call<HashMap<String, String>> call = apiInterface.blockUser(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        HashMap<String, String> responseMap = response.body();
                        if (responseMap.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
//                            Log.i(TAG, "onResponse: " + responseMap);
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.LANGUAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "onActivityResult: " + data.getStringExtra("languagecode"));
                if(data.hasExtra("languagecode"))
                    languageCode = data.getStringExtra("languagecode");
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void deleteFileForUri(Uri uri) {
        if (uri != null) {
            ContentResolver contentResolver = getContentResolver();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                if (contentResolver.delete(uri, null, null) > 0) {
                    Timber.i("Save to gallery: deleted %s", uri.getPath());
                } else {
                    Timber.i("Save to gallery: failed deleting %s", uri.getPath());
                }
            } else {
                /* For Android 11 (R) and above, use this snippet */
                        /*PendingIntent trashPendingIntent = MediaStore.createTrashRequest(contentResolver, Collections.singleton(mTempSavedFileUri), true);
                        try {
                            startIntentSenderForResult(trashPendingIntent.getIntentSender(), 120, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            Timber.e("Save to gallery: failed deleting %s", mTempSavedFileUri.getPath());
                        }*/
            }
        }
    }

    private void uploadChatImage(byte[] imageBytes, String fileName, String thumbnail, JSONObject jsonObject) {

        RequestBody requestFile = RequestBody.create(imageBytes, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_CHAT_IMAGE, "image.jpg", requestFile);
        RequestBody userId = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
        ApiInterface uploadInterface = ApiClient.getUploadClient().create(ApiInterface.class);
        Call<Map<String, String>> call3 = uploadInterface.uploadChatImage(body, userId);

        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                Timber.d("uploadChatImage: %s", data);
                if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                    liveModel.updateImageUpload(jsonObject);
                    sendImageChat(jsonObject, data.get(Constants.TAG_USER_IMAGE), fileName);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    ////// Addon voice message ///////////

/*    private void uploadAudioChat(byte[] audioBytes, String fileName, JSONObject jsonObject){
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/*"), audioBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_CHAT_AUDIO, fileName, requestFile);
        RequestBody userId = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<Map<String, String>> call3 = apiInterface.uploadChatImage(body, userId);

        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                Log.d(TAG, "onResponse: "+data);
                if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                    liveModel.updateImageUpload(jsonObject);
                    sendAudioChat(jsonObject, data.get(Constants.TAG_USER_IMAGE), fileName,data.get("audio_duration"));
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }*/

    private void uploadAudio(byte[] audioBytes, String fileName, JSONObject jsonObject) {
        RequestBody requestFile = RequestBody.create(audioBytes, MediaType.parse("audio/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_CHAT_IMAGE, "audio.mp3", requestFile);
        RequestBody userId = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
        Log.d(TAG, "uploadAudio: "+new Gson().toJson(body));
        Call<Map<String, String>> call3 = apiInterface.uploadAudio(body, userId);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    Map<String, String> data = response.body();
                    if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        liveModel.updateImageUpload(jsonObject);
                        sendFileChat(jsonObject, data.get(Constants.TAG_USER_IMAGE));
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }


    private void sendChat(String type, String chatImage, String filePath, String thumbnail, String txt) {
        try {
            long unixStamp = System.currentTimeMillis() / 1000L;
            String utcTime = AppUtils.getCurrentUTCTime(this);
            String messageId = GetSet.getUserId() + unixStamp;
            String msg = "";
            switch (type) {
                case Constants.TAG_TEXT:
                    //msg = edtMessage.getText().toString();
                    msg = txt;
                    break;
                case Constants.TAG_IMAGE:
                    msg = chatImage;
                    break;
                case Constants.TAG_GIF:
                    msg = chatImage;
                    break;
                case Constants.TAG_AUDIO:
                    msg = getFileName(filePath);
                    break;
                case Constants.TAG_VIDEO:
                    msg = getString(R.string.video);
                    break;
            }

            JSONObject json = new JSONObject();
            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_CHAT);
            json.put(Constants.TAG_RECEIVER_ID, partnerId);
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_USER_NAME, GetSet.getName());
            json.put(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            json.put(Constants.TAG_CHAT_ID, chatId);
            json.put(Constants.TAG_CHAT_TYPE, Constants.TAG_USER_CHAT);
            json.put(Constants.TAG_MSG_TYPE, type);
            json.put(Constants.TAG_MESSAGE_END, Constants.TAG_SEND);
            json.put(Constants.TAG_MESSAGE, AppUtils.encryptMessage(msg));
            json.put(Constants.TAG_MSG_ID, messageId);
            json.put(Constants.TAG_CHAT_TIME, utcTime);
            Logging.i(TAG, "sendChat1: " + json);
            AppWebSocket.getInstance(this).send(json.toString());

            /*Add receiver info in DB*/
            json.put(Constants.TAG_USER_ID, partnerId);
            json.put(Constants.TAG_USER_NAME, partnerName);
            json.put(Constants.TAG_USER_IMAGE, partnerImage);
            /*Add original message to DB*/
            json.put(Constants.TAG_MESSAGE, msg);
            json.put(Constants.TAG_THUMBNAIL, thumbnail);
            json.put(Constants.TAG_DELIVERY_STATUS, Constants.TAG_SEND);
            json.put(Constants.TAG_PROGRESS, "");
            liveModel.addMessage(getApplicationContext(), json);
            liveModel.addRecentChat(json);
            recyclerView.smoothScrollToPosition(0);
            edtMessage.setText("");
        } catch (JSONException e) {
            Logging.e(TAG, "sendChat: " + e.getMessage());
        }
    }

    private void sendImageChat(JSONObject jsonObject, String chatImage, String fileName) {
        try {
            String utcTime = jsonObject.getString(Constants.TAG_CHAT_TIME);
            String messageId = jsonObject.getString(Constants.TAG_MSG_ID);
            String msg = "";
            String type = jsonObject.getString(Constants.TAG_MSG_TYPE);
            switch (type) {
                case Constants.TAG_TEXT:
                    msg = edtMessage.getText().toString();
                    break;
                case Constants.TAG_IMAGE:
                    msg = chatImage;
                    break;
                case Constants.TAG_GIF:
                    msg = chatImage;
                    break;
                case Constants.TAG_AUDIO:
                    msg = fileName;
                    break;
                case Constants.TAG_VIDEO:
                    msg = getString(R.string.video);
                    break;
            }

            JSONObject json = new JSONObject();
            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_CHAT);
            json.put(Constants.TAG_RECEIVER_ID, partnerId);
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_USER_NAME, GetSet.getName());
            json.put(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            json.put(Constants.TAG_CHAT_ID, chatId);
            json.put(Constants.TAG_CHAT_TYPE, Constants.TAG_USER_CHAT);
            json.put(Constants.TAG_MSG_ID, messageId);
            json.put(Constants.TAG_MSG_TYPE, type);
            json.put(Constants.TAG_MESSAGE, AppUtils.encryptMessage(msg));
            json.put(Constants.TAG_CHAT_TIME, utcTime);
            Logging.i(TAG, "sendImageChat: " + json);
            AppWebSocket.getInstance(this).send(json.toString());
            dbHelper.updateMessage(messageId, Constants.TAG_PROGRESS, Constants.TAG_COMPLETED);
            recyclerView.smoothScrollToPosition(0);
        } catch (JSONException e) {
            Logging.e(TAG, "sendImageChat: " + e.getMessage());
        }
    }

    //////Addon voice message ///////////

    private void sendFileChat(JSONObject jsonObject, String file) {
        try {
            String utcTime = jsonObject.getString(Constants.TAG_CHAT_TIME);
            String messageId = jsonObject.getString(Constants.TAG_MSG_ID);
            String msg = "";
            String type = jsonObject.getString(Constants.TAG_MSG_TYPE);
            switch (type) {
                case Constants.TAG_TEXT:
                    msg = edtMessage.getText().toString();
                    break;
                case Constants.TAG_IMAGE:
                    msg = file;
                    break;
                case Constants.TAG_GIF:
                    msg = file;
                    break;
                case Constants.TAG_AUDIO:
                    msg = file;
                    break;
                case Constants.TAG_VIDEO:
                    msg = getString(R.string.video);
                    break;
            }

            JSONObject json = new JSONObject();
            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_CHAT);
            json.put(Constants.TAG_RECEIVER_ID, partnerId);
            json.put(Constants.TAG_USER_ID, GetSet.getUserId());
            json.put(Constants.TAG_USER_NAME, GetSet.getUserName());
            json.put(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            json.put(Constants.TAG_CHAT_ID, chatId);
            json.put(Constants.TAG_CHAT_TYPE, Constants.TAG_USER_CHAT);
            json.put(Constants.TAG_MSG_ID, messageId);
            json.put(Constants.TAG_MSG_TYPE, type);
            json.put(Constants.TAG_MESSAGE, AppUtils.encryptMessage(msg));
            json.put(Constants.TAG_CHAT_TIME, utcTime);

            if (type.equals("audio"))
                json.put(Constants.TAG_SOUND_DURATION, Float.valueOf(socketDuration));

            Logging.i(TAG, "sendFileChat: " + json);
            AppWebSocket.getInstance(this).send(json.toString());
            dbHelper.updateMessage(messageId, Constants.TAG_PROGRESS, Constants.TAG_COMPLETED);
            recyclerView.smoothScrollToPosition(0);
        } catch (JSONException e) {
            Logging.e(TAG, "sendFileChat: " + e.getMessage());
        }
    }

    private JSONObject updateDB(String type, String chatImage, String filePath,
                                String thumbnail, String progress) {
        JSONObject json = new JSONObject();
        try {
            long unixStamp = System.currentTimeMillis() / 1000L;
            String utcTime = AppUtils.getCurrentUTCTime(this);
            String messageId = GetSet.getUserId() + unixStamp;
            String msg = "";
            switch (type) {
                case Constants.TAG_TEXT:
                    msg = edtMessage.getText().toString();
                    break;
                case Constants.TAG_IMAGE:
                    msg = chatImage;
                    break;
                case Constants.TAG_GIF:
                    msg = chatImage;
                    break;
                case Constants.TAG_AUDIO:
                    msg = getFileName(filePath);
                    break;
                case Constants.TAG_VIDEO:
                    msg = getString(R.string.video);
                    break;
            }

            json.put(Constants.TAG_TYPE, Constants.TAG_SEND_CHAT);
            json.put(Constants.TAG_RECEIVER_ID, partnerId);
            json.put(Constants.TAG_CHAT_ID, chatId);
            json.put(Constants.TAG_CHAT_TYPE, Constants.TAG_USER_CHAT);
            json.put(Constants.TAG_MSG_ID, messageId);
            json.put(Constants.TAG_MSG_TYPE, type);
            json.put(Constants.TAG_MESSAGE_END, Constants.TAG_SEND);
            json.put(Constants.TAG_CHAT_TIME, utcTime);
            json.put(Constants.TAG_USER_ID, partnerId);
            json.put(Constants.TAG_USER_NAME, partnerName);
            json.put(Constants.TAG_USER_IMAGE, partnerImage);
            /*Add original message to DB*/
            json.put(Constants.TAG_MESSAGE, msg);
            json.put(Constants.TAG_THUMBNAIL, thumbnail);
            json.put(Constants.TAG_DELIVERY_STATUS, Constants.TAG_UNREAD);
            json.put(Constants.TAG_PROGRESS, progress);
            liveModel.addMessage(getApplicationContext(), json);
            liveModel.addRecentChat(json);
            recyclerView.smoothScrollToPosition(0);
        } catch (JSONException e) {
            Logging.e(TAG, "updateDB: " + e.getMessage());
        }
        return json;
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1);
        }
        return imgSplit;
    }

    private void showAttachment() {
        // Prepare the View for the animation
        attachLay.setVisibility(View.VISIBLE);
        // Start the animation
        attachLay.animate()
                .translationX(0)
                .alpha(1.0f)
                .setDuration(100)
                .setListener(null);
    }

    private void hideAttachment() {
        attachLay.animate()
                .translationX(-100)
                .setDuration(100)
                .alpha(1.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        attachLay.setVisibility(GONE);
                    }
                });
    }

    public void showLoading() {
        /*Disable touch options*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        /*Enable touch options*/
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onWebSocketConnected() {

    }

    @Override
    public void onWebSocketMessage(String message) {
        Log.e("checkisReceive","-"+message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                socketmessages = true;
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(message);
                    String type = jsonObject.getString(Constants.TAG_TYPE);
                    if (!isAdminChat) {
                        firstcall = 0;
                        switch (type) {
                            case Constants.TAG_RECEIVE_CHAT:
                                recyclerView.smoothScrollToPosition(0);
                                if (blockStatus != null && blockStatus.equals(Constants.TAG_FALSE)) {
                                    sendReadStatus();
                                }
                                break;
                            case Constants.TAG_lISTEN_TYPING:
                                if (jsonObject.getString(Constants.TAG_RECEIVER_ID).equals(GetSet.getUserId())) {
                                    if (blockStatus.equals(Constants.TAG_FALSE)) {
                                        if (jsonObject.getString(Constants.TAG_TYPING_STATUS).equals(Constants.TAG_TYPING)) {
                                            txtSubTitle.setVisibility(GONE);
                                            txtTyping.setVisibility(View.VISIBLE);
                                        } else {
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    txtTyping.setVisibility(GONE);
                                                    txtSubTitle.setVisibility(View.VISIBLE);
                                                }
                                            }, 1000);
                                        }
                                    } else {
                                        txtTyping.setVisibility(GONE);
                                    }
                                }
                                break;
                            case Constants.TAG_PROFILE_STATUS:
                                if (jsonObject.optString(Constants.TAG_ONLINE_STATUS).equals(Constants.TAG_TRUE)) {

                                    /*chat translation*/
                                    from = true;

                                    if (txtTyping.getVisibility() != View.VISIBLE) {
                                        txtSubTitle.setVisibility(View.VISIBLE);
                                        txtSubTitle.setText(getString(R.string.online));
                                    } else {
                                        txtSubTitle.setVisibility(GONE);
                                    }
                                } else if (jsonObject.getString(Constants.TAG_ONLINE_STATUS).equals(Constants.TAG_FALSE)) {
                                    txtSubTitle.setVisibility(GONE);
                                } else {
                                    if (txtTyping.getVisibility() != View.VISIBLE) {
                                        txtSubTitle.setVisibility(View.VISIBLE);
                                        txtSubTitle.setText(getString(R.string.last_seen_at) + " " + AppUtils.getRecentDate(getApplicationContext(),
                                                AppUtils.getTimeFromUTC(getApplicationContext(), jsonObject.optString(Constants.TAG_ONLINE_STATUS))).toLowerCase());
                                    }
                                }
                                break;
                            case Constants.TAG_RECEIVE_READ_STATUS:
                                liveModel.changeSentStatus();
                                break;
                            case Constants.TAG_OFFLINE_READ_STATUS:
                                liveModel.changeSentStatus();
                                break;
                        }
                    }
                } catch (
                        JSONException e) {
                    Log.e(TAG, "onWebSocketMessage: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onWebSocketClose() {

    }

    @Override
    public void onWebSocketError(String description) {

    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cameraPermissionResult.launch(PermissionsUtils.CAMERA_STORAGE_PERMISSION_V10);
        } else {
            cameraPermissionResult.launch(PermissionsUtils.CAMERA_PERMISSION);
        }
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            storagePermissionResult.launch(PermissionsUtils.READ_STORAGE_PERMISSION);
        } else {
            storagePermissionResult.launch(PermissionsUtils.READ_WRITE_PERMISSIONS);
        }
    }

    public void requestAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, PermissionsUtils.AUDIO_RECORD_V11_PERMISSION, PermissionsUtils.RECORD_PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, PermissionsUtils.AUDIO_RECORD_PERMISSION, PermissionsUtils.RECORD_PERMISSION_REQUEST_CODE);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case Constants.STORAGE_REQUEST_CODE:
//                if (checkPermissions(permissions)) {
//                    pickImage();
//                } else {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        if (shouldShowRequestPermissionRationale(CAMERA) &&
//                                shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
//                            requestPermissions(permissions, Constants.STORAGE_REQUEST_CODE);
//                        } else {
//                            App.makeToast(getString(R.string.camera_storage_error));
//                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
//                            startActivity(i);
//                        }
//                    }
//                }
//                break;
//            case PermissionsUtils.RECORD_PERMISSION_REQUEST_CODE: {
//                int permissionAudio = ContextCompat.checkSelfPermission(ChatActivity.this,
//                        RECORD_AUDIO);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    int permissionStorage = ContextCompat.checkSelfPermission(ChatActivity.this,
//                            READ_EXTERNAL_STORAGE);
//                    if (permissionAudio != PackageManager.PERMISSION_GRANTED ||
//                            permissionStorage != PackageManager.PERMISSION_GRANTED) {
//                        if (shouldShowRequestPermissionRationale(RECORD_AUDIO) || shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
//                            requestAudioPermission();
//                        } else {
//                            App.makeToast(getString(R.string.audio_record_permission_error));
//                        }
//                    }
//                } else {
//                    int permissionStorage = ContextCompat.checkSelfPermission(ChatActivity.this,
//                            WRITE_EXTERNAL_STORAGE);
//                    if (permissionAudio != PackageManager.PERMISSION_GRANTED ||
//                            permissionStorage != PackageManager.PERMISSION_GRANTED) {
//                        if (ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, RECORD_AUDIO) ||
//                                ActivityCompat.shouldShowRequestPermissionRationale(ChatActivity.this, WRITE_EXTERNAL_STORAGE)) {
//                            requestAudioPermission();
//                        } else {
//                            App.makeToast(getString(R.string.audio_record_permission_error));
//                        }
//                    }
//                }
    ////                setVoiceRecorder();
//            }
//            break;
//        }
//    }

    public boolean checkPermissions(String[] permissions) {
        boolean permissionGranted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
                break;
            }
        }
        return permissionGranted;
    }

    public String getPartnerId() {
        return partnerId;
    }

    @Override
    public void onAudioComplete() {
        state = recyclerView.getLayoutManager().onSaveInstanceState();
        // Main position of RecyclerView when loaded again
        if (state != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(state);
        }
        if (currentMessageId != null) {
            ChatResponse tempMsgData = dbHelper.getSingleMessage(currentMessageId);
            int tempPosition = -1;
            if (chatList.contains(tempMsgData)) {
                tempPosition = chatList.indexOf(tempMsgData);
            }
            if (tempPosition != -1) {
                RecyclerView.ViewHolder tempViewHolder = recyclerView.findViewHolderForAdapterPosition(tempPosition);
                if (tempViewHolder instanceof ChatAdapter.SentAudioHolder) {
                    ChatAdapter.SentAudioHolder holder = (ChatAdapter.SentAudioHolder) tempViewHolder;
                    holder.songSeekbar.setProgress(0);
                    File file = storageUtils.getSrcFile(StorageUtils.TAG_AUDIO_SENT, getFileName(AppUtils.decryptMessage(tempMsgData.getMessage())), StorageUtils.TAG_AUDIO);
                    if (file != null) {
                        holder.txtDuration.setVisibility(View.VISIBLE);
                        holder.txtDuration.setText(milliSecondsToTimer(storageUtils.getMediaDuration(file)));
                    } else {
                        holder.txtDuration.setVisibility(View.INVISIBLE);
                    }
                    holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_play_white));
                } else if (tempViewHolder instanceof ChatAdapter.ReceiveAudioHolder) {
                    ChatAdapter.ReceiveAudioHolder holder = (ChatAdapter.ReceiveAudioHolder) tempViewHolder;
                    holder.songSeekbar.setProgress(0);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Uri fileUri = storageUtils.getFileUri(StorageUtils.TAG_AUDIO, AppUtils.decryptMessage(tempMsgData.getMessage()), StorageUtils.TAG_AUDIO);
                        if (fileUri != null) {
                            holder.txtDuration.setVisibility(View.VISIBLE);
                            holder.txtDuration.setText(milliSecondsToTimer(storageUtils.getMediaDuration(ChatActivity.this, fileUri)));
                        } else {
                            holder.txtDuration.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        File file = storageUtils.getSrcFile(StorageUtils.TAG_AUDIO, getFileName(AppUtils.decryptMessage(tempMsgData.getMessage())), StorageUtils.TAG_AUDIO);
                        if (file != null) {
                            holder.txtDuration.setVisibility(View.VISIBLE);
                            holder.txtDuration.setText(milliSecondsToTimer(storageUtils.getMediaDuration(file)));
                        } else {
                            holder.txtDuration.setVisibility(View.INVISIBLE);
                        }
                    }
                    holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_play_white));
                }
            }
        }
        appUtils.resumeExternalAudio();
    }

    @Override
    public void onAudioUpdate(int currentPosition) {
        if (currentMessageId != null) {
            ChatResponse tempMsgData = dbHelper.getSingleMessage(currentMessageId);
            int tempPosition = -1;

            if (chatList.contains(tempMsgData)) {
                tempPosition = chatList.indexOf(tempMsgData);
            }
            if (tempPosition != -1) {
                RecyclerView.ViewHolder tempViewHolder = recyclerView.findViewHolderForAdapterPosition(tempPosition);
                if (tempViewHolder instanceof ChatAdapter.SentAudioHolder) {
                    ChatAdapter.SentAudioHolder holder = (ChatAdapter.SentAudioHolder) tempViewHolder;
                    holder.songSeekbar.setProgress(currentPosition);
                    holder.txtDuration.setText(milliSecondsToTimer(currentPosition));
                    holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this, R.drawable.ic_pause_white));
                } else if (tempViewHolder instanceof ChatAdapter.ReceiveAudioHolder) {
                    ChatAdapter.ReceiveAudioHolder holder = (ChatAdapter.ReceiveAudioHolder) tempViewHolder;
                    holder.songSeekbar.setProgress(currentPosition);
                    holder.txtDuration.setText(milliSecondsToTimer(currentPosition));
                    holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this, R.drawable.ic_pause_white));
                }
            }
        }
    }

    @Override
    public void setMediaDuration(int duration) {

    }

    public void stopAudioViewHolder() {
        MediaPlayerUtils.pauseMediaPlayer();
        if (currentMessageId != null) {
            ChatResponse tempMsgData = dbHelper.getSingleMessage(currentMessageId);
            int tempPosition = -1;
            if (chatList.contains(tempMsgData)) {
                tempPosition = chatList.indexOf(tempMsgData);
            }
            if (tempPosition != -1) {
                adapter.notifyItemChanged(tempPosition);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(isRecording)
            return;
        switch (view.getId()) {
            case R.id.btnBack:
                App.preventMultipleClick(btnBack);
                hideKeyboard(ChatActivity.this);

                onBackPressed();
                break;
            case R.id.btnMenu:
                Log.d(TAG, "onnnn: ");
                App.preventMultipleClick(btnMenu);
                openMenu(view);
                break;
            case R.id.btnGallery:
                App.preventMultipleClick(btnGallery);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA},
                            Constants.STORAGE_REQUEST_CODE);
                } else if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                } else {
                    App.preventMultipleClick(btnGallery);
                    pickImage();
                }
                break;
            case R.id.btnCamera:
                App.preventMultipleClick(btnCamera);
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, CAMERA},
                            Constants.STORAGE_REQUEST_CODE);
                } else if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                } else {
                    App.preventMultipleClick(btnCamera);
                    pickImage();
                }
                break;
            case R.id.btnAdd:
                App.preventMultipleClick(btnAdd);
                if (!NetworkReceiver.isConnected()) {
                    App.makeToast(getString(R.string.no_internet_connection));
                } else if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {



                        // Android 14 or higher
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(getApplicationContext(), READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(getApplicationContext(), READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                            Log.d( "onViewClicked1234", "else");
                            storagePermissionResult.launch(new String[]{READ_MEDIA_IMAGES,READ_MEDIA_VIDEO,CAMERA});

//                            ActivityCompat.requestPermissions(MessageActivity.this, new String[]{CAMERA, READ_MEDIA_IMAGES}, 104);

                        } else {
                            Log.d(TAG, "onViewClicked: else");
                            if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                                App.makeToast(getString(R.string.unblock_description));
                            } else {
                                Log.d(TAG, "onViewClicked: ?????");
                                App.preventMultipleClick(btnCamera);
                                pickImage();
                            }
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                                || ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 104);
                        } else {
                            if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                                App.makeToast(getString(R.string.unblock_description));
                            } else {
                                App.preventMultipleClick(btnCamera);
                                pickImage();
                            }
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                                || ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 104);
                        } else {
                            if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                                App.makeToast(getString(R.string.unblock_description));
                            } else {
                                App.preventMultipleClick(btnCamera);
                                pickImage();
                            }
                        }
                    } else {
                        if (ContextCompat.checkSelfPermission(ChatActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                                || ContextCompat.checkSelfPermission(ChatActivity.this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ChatActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 104);
                        } else {
                            if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                                App.makeToast(getString(R.string.unblock_description));
                            } else {
                                App.preventMultipleClick(btnCamera);
                                pickImage();
                            }
                        }
                    }

                }
                break;
           /* case R.id.btnGif:
                App.preventMultipleClick(btnGif);
                //Addon Gif
                giphyDialogFragment = GiphyDialogFragment.Companion.newInstance(settings, Constants.KEY_GIPHY_API, false);
                giphyDialogFragment.show(getSupportFragmentManager(), "giphy_dialog");
                giphyDialogFragment.setGifSelectionListener(this);
                break;*/
            case R.id.btnSend:
                App.preventMultipleClick(btnSend);
                if (TextUtils.isEmpty("" + edtMessage.getText().toString().trim())) {
                    edtMessage.setError(getString(R.string.enter_your_message));
                } else if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_description));
                } else if (!NetworkReceiver.isConnected()) {
                    App.makeToast(getString(R.string.no_internet_connection));
                } else {
                    //sendChat(Constants.TAG_TEXT, "", "", "");
                    String textsend = edtMessage.getText().toString();
                    sendChat(Constants.TAG_TEXT, "", "", "", textsend);
                    edtMessage.setText("");
                }
                break;
            case R.id.btnContactUs:
                App.preventMultipleClick(btnContactUs);
                if (NetworkReceiver.isConnected()) {
                    sendEmail();
                } else {
                    App.makeToast(getString(R.string.no_internet_connection));
                }
                break;
          /*  case R.id.btnLanguage:
                //Addon Chat Translate
                App.preventMultipleClick(btnLanguage);
                Intent language = new Intent(getApplicationContext(), LanguageTranslationAct.class);
                language.putExtra(Constants.TAG_FROM, Constants.TAG_CHAT);
                *//*language.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                language.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);*//*
                startActivityForResult(language, Constants.LANGUAGE_REQUEST_CODE);
                break;*/
            case R.id.video_call:
                if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_call_description));
                } else {
                    if (GetSet.getGems() >= AdminData.videoCallsGems) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            messageCheck(true);
                        }, 1000);
                    }
                    else {
                        App.makeToast(getString(R.string.not_enough_gems));
                    }
                }
                break;
            case R.id.audio_call:
                if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
                    App.makeToast(getString(R.string.unblock_call_description));
                } else {
                    Log.d(TAG, "onClick: "+GetSet.getGems() +" _ "+ AdminData.videoCallsGems);
                    if (GetSet.getGems() >= AdminData.videoCallsGems) {
                        messageCheck(false);
                    }
                    else {
                        App.makeToast(getString(R.string.not_enough_gems));
                    }
                }
                break;
        }
    }


    private void messageCheck(boolean state) {

        if(state){
            if (NetworkReceiver.isConnected()) {
                AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());

                Log.d(TAG, "messageCheck: "+partnerName);
                Log.d(TAG, "messageCheck: "+partnerImage);
                Log.d(TAG, "messageCheck: "+GetSet.getUserId());
                Intent callIntent = appRTCUtils.connectToRoom(partnerId, Constants.TAG_SEND, Constants.TAG_VIDEO);
                callIntent.putExtra(Constants.TAG_USER_NAME, partnerName);
                callIntent.putExtra(Constants.TAG_USER_IMAGE, partnerImage);
                callIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(callIntent);
            } else {
                App.makeToast(getString(R.string.no_internet_connection));
            }

        }
        else {
            if (NetworkReceiver.isConnected()) {
                AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                Intent callIntent = appRTCUtils.connectToRoom(partnerId, Constants.TAG_SEND, Constants.TAG_AUDIO);
                callIntent.putExtra(Constants.TAG_USER_NAME, partnerName);
                callIntent.putExtra(Constants.TAG_USER_IMAGE, partnerImage);
                callIntent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(callIntent);
            } else {
                App.makeToast(getString(R.string.no_internet_connection));
            }

        }

    }

    public class ChatAdapter extends RecyclerView.Adapter {

        public static final int VIEW_TYPE_SENT_TEXT = 0;
        public static final int VIEW_TYPE_RECEIVE_TEXT = 1;
        public static final int VIEW_TYPE_SENT_IMAGE = 2;
        public static final int VIEW_TYPE_RECEIVE_IMAGE = 3;
        public static final int VIEW_TYPE_SENT_STREAM = 4;
        public static final int VIEW_TYPE_RECEIVE_STREAM = 5;
        public static final int VIEW_TYPE_FOOTER = 7;
        public static final int VIEW_TYPE_CALL = 8;
        public static final int VIEW_TYPE_DATE = 9;
        public static final int VIEW_TYPE_SENT_AUDIO = 10;
        public static final int VIEW_TYPE_RECEIVE_AUDIO = 11;

        /*Emoji and Gif in chat*/
        public static final int VIEW_TYPE_SENT_GIF = 12;
        public static final int VIEW_TYPE_RECEIVE_GIF = 13;

        private final Context mContext;
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;
        RequestOptions requestOptions;


        public ChatAdapter(Context con) {
            mContext = con;
            requestOptions = new RequestOptions().error(R.drawable.rounded_square_semi_transparent)
                    .dontAnimate().diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(new BlurTransformation(15, 1));
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_RECEIVE_TEXT) {
                View itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.chat_text_bubble_receive, parent, false);
                viewHolder = new ReceiveMessageHolder(itemView);
            } else if (viewType == VIEW_TYPE_RECEIVE_IMAGE) {
                View itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.chat_image_bubble_receive, parent, false);
                viewHolder = new ReceiveImageHolder(itemView);
            } else if (viewType == VIEW_TYPE_RECEIVE_GIF) {
                View itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.chat_gif_receive, parent, false);
                viewHolder = new ReceiveGifHolder(itemView);
            } else if (viewType == VIEW_TYPE_RECEIVE_STREAM) {
                View itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.chat_stream_bubble_receive, parent, false);
                viewHolder = new ReceiveStreamHolder(itemView);
            } else if (viewType == VIEW_TYPE_SENT_TEXT) {
                View itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.chat_text_bubble_sent, parent, false);
                viewHolder = new SentMessageHolder(itemView);
            } else if (viewType == VIEW_TYPE_SENT_IMAGE) {
                View itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.chat_image_bubble_sent, parent, false);
                viewHolder = new SentImageHolder(itemView);
            } else if (viewType == VIEW_TYPE_SENT_GIF) {
                View itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.chat_gif_sent, parent, false);
                viewHolder = new SentGifHolder(itemView);
            } else if (viewType == VIEW_TYPE_SENT_STREAM) {
                View itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.chat_stream_bubble_sent, parent, false);
                viewHolder = new SentStreamHolder(itemView);
            } else if (viewType == VIEW_TYPE_DATE) {
                View itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.chat_date_layout, parent, false);
                viewHolder = new DateHolder(itemView);
            } else if (viewType == VIEW_TYPE_CALL) {
                View itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.chat_call_layout, parent, false);
                viewHolder = new CallHolder(itemView);
            } else if (viewType == VIEW_TYPE_RECEIVE_AUDIO) {
                View itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.chat_voice_receive, parent, false);
                viewHolder = new ReceiveAudioHolder(itemView);
            } else if (viewType == VIEW_TYPE_SENT_AUDIO) {
                View itemView = LayoutInflater.from(mContext)
                        .inflate(R.layout.chat_voice_sent, parent, false);
                viewHolder = new SentAudioHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(mContext).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.@NotNull ViewHolder holder, int pos) {
            int position = holder.getAbsoluteAdapterPosition();
            if (holder instanceof ReceiveMessageHolder) {
                ChatResponse chat = chatList.get(position);
                ((ReceiveMessageHolder) holder).bind(chat);
            } else if (holder instanceof ReceiveImageHolder) {
                ChatResponse chat = chatList.get(position);
                ((ReceiveImageHolder) holder).bind(chat);
            } else if (holder instanceof ReceiveGifHolder) {
                ChatResponse chat = chatList.get(position);
                ((ReceiveGifHolder) holder).bind(chat);
            } else if (holder instanceof ReceiveStreamHolder) {
                ChatResponse chat = chatList.get(position);
                ((ReceiveStreamHolder) holder).bind(chat);
            } else if (holder instanceof SentMessageHolder) {
                ChatResponse chat = chatList.get(position);
                ((SentMessageHolder) holder).bind(chat);
            } else if (holder instanceof SentImageHolder) {
                ((SentImageHolder) holder).bind(chatList.get(position));
            } else if (holder instanceof SentGifHolder) {
                ((SentGifHolder) holder).bind(chatList.get(position));
            } else if (holder instanceof SentStreamHolder) {
                ((SentStreamHolder) holder).bind(chatList.get(position));
            } else if (holder instanceof DateHolder) {
                ChatResponse chat = chatList.get(position);
                ((DateHolder) holder).bind(chat);
            } else if (holder instanceof CallHolder) {
                ChatResponse chat = chatList.get(position);
                ((CallHolder) holder).bind(chat);
            } else if (holder instanceof ReceiveAudioHolder) {
                ((ReceiveAudioHolder) holder).bind(chatList.get(position));
            } else if (holder instanceof SentAudioHolder) {
                ((SentAudioHolder) holder).bind(chatList.get(position));
            } else if (holder instanceof ChatFragment.ChatAdapter.FooterViewHolder) {
                ChatFragment.ChatAdapter.FooterViewHolder footerHolder = (ChatFragment.ChatAdapter.FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemViewType(int position) {
            ChatResponse chat = chatList.get(position);
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            else if (chat.getMessageType().equals(Constants.TAG_DATE)) {
                return VIEW_TYPE_DATE;
            } else if (chat.getMessageType().equals(Constants.TAG_MISSED)) {
                return VIEW_TYPE_CALL;
            } else if (chat.getChatType().equals(Constants.TAG_ADMIN) || chat.getMessageEnd().equals(Constants.TAG_RECEIVED)) {
                switch (chat.getMessageType()) {
                    case Constants.TAG_TEXT:
                        return VIEW_TYPE_RECEIVE_TEXT;
                    case Constants.TAG_IMAGE:
                        return VIEW_TYPE_RECEIVE_IMAGE;
                    case Constants.TAG_GIF:
                        return VIEW_TYPE_RECEIVE_GIF;
                    case Constants.TAG_LIVE:
                    case Constants.TAG_RECORDED:
                        return VIEW_TYPE_RECEIVE_STREAM;
                    case Constants.TAG_AUDIO:
                        return VIEW_TYPE_RECEIVE_AUDIO;
                    default:
                        return VIEW_TYPE_RECEIVE_TEXT;
                }
            } else {
                switch (chat.getMessageType()) {
                    case Constants.TAG_TEXT:
                        return VIEW_TYPE_SENT_TEXT;
                    case Constants.TAG_IMAGE:
                        return VIEW_TYPE_SENT_IMAGE;
                    case Constants.TAG_GIF:
                        return VIEW_TYPE_SENT_GIF;
                    case Constants.TAG_LIVE:
                    case Constants.TAG_RECORDED:
                        return VIEW_TYPE_SENT_STREAM;
                    case Constants.TAG_AUDIO:
                        return VIEW_TYPE_SENT_AUDIO;
                    default:
                        return VIEW_TYPE_SENT_TEXT;
                }
            }
        }

        @Override
        public int getItemCount() {
            int itemCount = chatList.size();
            if (showLoading)
                itemCount++;
            return itemCount;
        }

        public boolean isPositionFooter(int position) {
            return position == getItemCount() - 1 && showLoading;
        }

        public void showLoading(boolean value) {
            showLoading = value;
        }

        public void setData(List<ChatResponse> newData) {
            chatList = newData;
            adapter.notifyDataSetChanged();
        }

        /*public void setData(List<ChatResponse> newData) {
            if (chatList == null) {
                chatList = newData;
                notifyItemRangeInserted(0, newData.size());
            } else {
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return chatList.size();
                    }

                    @Override
                    public int getNewListSize() {
                        return newData.size();
                    }

                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        if ((chatList.get(oldItemPosition).getMessageType().equals(Constants.TAG_DATE) &&
                                newData.get(newItemPosition).getMessageType().equals(Constants.TAG_DATE))) {
                            return true;
                        } else
                            return (chatList.get(oldItemPosition).getMessageId() != null && newData.get(newItemPosition).getMessageId() != null) &&
                                    (chatList.get(oldItemPosition).getMessageId().equals(newData.get(newItemPosition).getMessageId()));
                    }

                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        if ((chatList.get(oldItemPosition).getMessageType().equals(Constants.TAG_DATE) &&
                                newData.get(newItemPosition).getMessageType().equals(Constants.TAG_DATE))) {
                            return true;
                        } else {
                            return Objects.equals(chatList.get(oldItemPosition).getDeliveryStatus(), newData.get(newItemPosition).getDeliveryStatus());
                        }
                    }
                });
                chatList.clear();
                chatList.addAll(newData);
                result.dispatchUpdatesTo(adapter);
            }
        }*/

        public class DateHolder extends RecyclerView.ViewHolder {
            TextView txtMsgTime;

            public DateHolder(View itemView) {
                super(itemView);
                txtMsgTime = itemView.findViewById(R.id.txtMsgTime);
            }

            public void bind(ChatResponse chat) {
                txtMsgTime.setText(chat.getChatTime());
            }
        }

        public class CallHolder extends RecyclerView.ViewHolder {
            TextView txtMsg;

            public CallHolder(View itemView) {
                super(itemView);
                txtMsg = itemView.findViewById(R.id.txtMsg);
            }

            public void bind(ChatResponse chat) {
                txtMsg.setText(mContext.getString(R.string.missed_call_at) + " " +
                        AppUtils.getChatTime(mContext, chat.getChatTime()));
            }
        }

        public class SentMessageHolder extends RecyclerView.ViewHolder {
            TextView txtMessage;
            TextView txtChatTime;
            RoundedImageView userImage;
            int start = 0;

            public SentMessageHolder(View view) {
                super(view);
                txtMessage = view.findViewById(R.id.txtMessage);
                txtChatTime = view.findViewById(R.id.txtChatTime);
                userImage = view.findViewById(R.id.userImage);
            }

            public void bind(ChatResponse chat) {
                Spanned spannedText;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    spannedText = Html.fromHtml(chat.getMessage(), Html.FROM_HTML_MODE_LEGACY);
                } else {
                    spannedText = Html.fromHtml(chat.getMessage());
                }
                txtMessage.setText(spannedText);
                txtMessage.setMovementMethod(LinkMovementMethod.getInstance());

                txtChatTime.setText(AppUtils.getChatTime(mContext, chat.getChatTime()));
                userImage.setVisibility(GONE);
                ChatResponse chatResponse = dbHelper.getLastReadMessage(chatId);
                if (chatResponse.getMessageId() != null && chatResponse.getMessageId().equals(chat.getMessageId())) {
                    if (chat.getDeliveryStatus().equals(Constants.TAG_READ)) {
                        userImage.setVisibility(GONE);
                        Glide.with(mContext)
                                .load(partnerImage)
                                .apply(new RequestOptions().placeholder(R.drawable.alhawana).error(R.drawable.alhawana))
                                .into(userImage);
                    }
                }
            }

            private ChatResponse getLastSentMessage(String s) {
                for (ChatResponse chatResponse : chatList) {
                    if (chatResponse.getMessageEnd().equals(Constants.TAG_SEND)) {
                        return chatResponse;
                    }
                }
                return null;
            }
        }

        public class SentImageHolder extends RecyclerView.ViewHolder {
            RoundedImageView chatImage;
            RoundedImageView userImage;
            ImageView btnUpload;
            ProgressWheel uploadProgress;
            RelativeLayout uploadLay;
            TextView txtChatTime;
            ConstraintLayout itemLay;

            public SentImageHolder(View view) {
                super(view);
                chatImage = view.findViewById(R.id.chatImage);
                userImage = view.findViewById(R.id.userImage);
                btnUpload = view.findViewById(R.id.btnUpload);
                uploadProgress = view.findViewById(R.id.uploadProgress);
                uploadLay = view.findViewById(R.id.uploadLay);
                txtChatTime = view.findViewById(R.id.txtChatTime);
                itemLay = view.findViewById(R.id.itemLay);
            }

            public void bind(ChatResponse chat) {
                Log.d(TAG, "SentImageHolder: bind: " + new Gson().toJson(chat));

                txtChatTime.setText(AppUtils.getChatTime(mContext, chat.getChatTime()));
                File srcFile = storageUtils.getSrcFile(Constants.TAG_SENT, getFileName(chat.getMessage()));
                if (srcFile != null) {
                    uploadLay.setVisibility(GONE);
                    uploadProgress.stopSpinning();

                    Glide.with(getApplicationContext())
                            .load(srcFile)
                            .into(chatImage);


                    if (chat.getProgress().equals(Constants.TAG_COMPLETED)) {
                        uploadProgress.stopSpinning();
                        uploadLay.setVisibility(GONE);
                    } else {
                        btnUpload.setVisibility(View.VISIBLE);
                        uploadProgress.setVisibility(View.VISIBLE);
                    }
                } else {
                    Glide.with(getApplicationContext())
                            .load("")
                            .into(chatImage);
                    uploadLay.setVisibility(GONE);
                }

                userImage.setVisibility(GONE);
                ChatResponse chatResponse = dbHelper.getLastReadMessage(chatId);
                if (chatResponse.getMessageId() != null && chatResponse.getMessageId().equals(chat.getMessageId())) {
                    if (chat.getDeliveryStatus().equals(Constants.TAG_READ)) {
                        userImage.setVisibility(GONE);
                        Glide.with(mContext)
                                .load(partnerImage)
                                .apply(new RequestOptions().placeholder(R.drawable.alhawana).error(R.drawable.alhawana))
                                .into(userImage);
                    }
                }

                btnUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (NetworkReceiver.isConnected()) {
                            if (srcFile != null) {
                                File uploadedFile = srcFile;
                                String sentPath = uploadedFile.getAbsolutePath();
                                BufferedInputStream buf;
                                try {
                                    int size = (int) uploadedFile.length();
                                    byte[] bytes = new byte[size];
                                    buf = new BufferedInputStream(new FileInputStream(uploadedFile));
                                    buf.read(bytes, 0, bytes.length);
                                    buf.close();
                                    uploadProgress.spin();
                                    uploadChatImage(bytes, sentPath, "", new JSONObject());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            App.makeToast(getString(R.string.no_internet_connection));
                        }
                    }
                });

                chatImage.setOnClickListener(view -> {
                    if(isRecording)
                        return;
                    Intent imageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                    imageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    imageIntent.putExtra(Constants.TAG_USER_IMAGE, srcFile != null ? srcFile.getAbsolutePath() : "");
                    imageIntent.putExtra(Constants.TAG_FROM, Constants.TAG_SENT);
                    startActivity(imageIntent);
                });

            }
        }

        /*Emoji and Gif in chat*/
        public class SentGifHolder extends RecyclerView.ViewHolder {

            RoundedImageView userImage;
            ImageView btnUpload;
            ProgressWheel uploadProgress;
            RelativeLayout uploadLay;
            TextView txtChatTime;
            ImageView gifImage;

            public SentGifHolder(View view) {
                super(view);
                userImage = view.findViewById(R.id.userImage);
                btnUpload = view.findViewById(R.id.btnUpload);
                uploadProgress = view.findViewById(R.id.uploadProgress);
                uploadLay = view.findViewById(R.id.uploadLay);
                txtChatTime = view.findViewById(R.id.txtChatTime);
                gifImage = view.findViewById(R.id.gifImage);
            }

            public void bind(ChatResponse chat) {
                btnUpload.setVisibility(GONE);
                txtChatTime.setText(AppUtils.getChatTime(mContext, chat.getChatTime()));
                Glide
                        .with(mContext)
                        .asGif()
                        .load(chat.getMessage())
                        .into(gifImage);

                userImage.setVisibility(GONE);
                ChatResponse chatResponse = dbHelper.getLastReadMessage(chatId);
                if (chatResponse.getMessageId() != null && chatResponse.getMessageId().equals(chat.getMessageId())) {
                    if (chat.getDeliveryStatus().equals(Constants.TAG_READ)) {
                        userImage.setVisibility(GONE);
                        Glide.with(mContext)
                                .load(partnerImage)
                                .apply(new RequestOptions().placeholder(R.drawable.alhawana).error(R.drawable.alhawana))
                                .into(userImage);
                    }
                }

               /* gifImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isRecording)
                            return;
                        Intent imageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                        imageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        imageIntent.putExtra(Constants.TAG_USER_IMAGE, chat.getMessage());
                        imageIntent.putExtra(Constants.TAG_FROM, Constants.TAG_GIF);
                        startActivity(imageIntent);
                    }
                });*/
            }
        }

        public class SentStreamHolder extends RecyclerView.ViewHolder {
            RoundedImageView userImage;
            TextView txtMessage;
            TextView txtChatTime;

            public SentStreamHolder(View view) {
                super(view);
                userImage = view.findViewById(R.id.userImage);
                txtMessage = view.findViewById(R.id.txtMessage);
                txtChatTime = view.findViewById(R.id.txtChatTime);
            }

            public void bind(ChatResponse chat) {
                txtChatTime.setText(AppUtils.getChatTime(mContext, chat.getChatTime()));
                userImage.setVisibility(GONE);
                String[] link = chat.getMessage().split(Constants.TAG_HTTP);
                String message = null;
                if (link.length > 1) {
                    message = Constants.TAG_HTTP + link[1];

                    SpannableStringBuilder str = new SpannableStringBuilder(link[0] + message);
                    int startIndex = link[0].length();
                    int endIndex;
                    if (message.charAt(message.length() - 1) == '\"') {
                        endIndex = link[0].length() + message.length() - 1;
                    } else {
                        endIndex = link[0].length() + message.length();
                    }
//                    str.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    String finalMessage = message.trim();
                    ClickableSpan clickableSpan = new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View view) {
                            if (NetworkReceiver.isConnected()) {
                                showLoadingDialog();
                                Log.i(TAG, "onClick: " + finalMessage);
                                FirebaseDynamicLinks.getInstance()
                                        .getDynamicLink(Uri.parse(finalMessage))
                                        .addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
                                            @Override
                                            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                                                Uri deepLink = null;
                                                if (pendingDynamicLinkData != null) {
                                                    deepLink = pendingDynamicLinkData.getLink();

                                                    if (deepLink.getQueryParameter("stream_name") != null) {
                                                        String streamName = deepLink.getQueryParameter("stream_name");
                                                        getStreamDetails(streamName);
                                                    }
                                                }
                                                Log.i(TAG, "onSuccess: " + deepLink);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                hideLoading();
                                                Logging.e(TAG, "onSuccess: " + e.getMessage());
                                            }
                                        });
                            }
                        }
                    };
                    str.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    txtMessage.setText(str);
                } else {
                    message = link[0];
                    txtMessage.setText(message);
                }
                txtMessage.setMovementMethod(LinkMovementMethod.getInstance());
//                txtMessage.setText(chat.getMessage());
                ChatResponse chatResponse = dbHelper.getLastReadMessage(chatId);
                if (chatResponse.getMessageId() != null && chatResponse.getMessageId().equals(chat.getMessageId())) {
                    if (chat.getDeliveryStatus().equals(Constants.TAG_READ)) {
                        userImage.setVisibility(GONE);
                        Glide.with(mContext)
                                .load(partnerImage)
                                .apply(new RequestOptions().placeholder(R.drawable.alhawana).error(R.drawable.alhawana))
                                .into(userImage);
                    }
                }
            }
        }

        public class ReceiveMessageHolder extends RecyclerView.ViewHolder {
            RoundedImageView userImage;
            TextView txtMessage;
            TextView txtChatTime;
//            TextView translatebtn;

            public ReceiveMessageHolder(View view) {
                super(view);
                userImage = view.findViewById(R.id.userImage);
                txtMessage = view.findViewById(R.id.txtMessage);
                txtChatTime = view.findViewById(R.id.txtChatTime);
//                translatebtn = view.findViewById(R.id.translatebtn);
            }

            public void bind(ChatResponse chat) {
                Timber.tag(TAG).d("ReceiveMessageHolder: bind: %s", new Gson().toJson(chat));

                txtMessage.setText(chat.getMessage());
                txtMessage.setMovementMethod(LinkMovementMethod.getInstance());
                txtChatTime.setText(AppUtils.getChatTime(mContext, chat.getChatTime()));

                if (isAdminChat) {
                    userImage.setVisibility(GONE);
//                    translatebtn.setVisibility(GONE);
                } else {
                    Glide.with(mContext)
                            .load(partnerImage)
                            .apply(new RequestOptions().placeholder(R.drawable.alhawana).error(R.drawable.alhawana))
                            .into(userImage);
                    Log.e("checkisNull","-"+languageCode+" mes "+chat.getMessage());
                    if ((!SharedPref.getString(SharedPref.CHAT_LANGUAGE, SharedPref.DEFAULT_CHAT_LANGUAGE).equals("")
                            || !languageCode.equals("")) && !Util.isEmoji(chat.getMessage())) {
//                        translatebtn.setVisibility(View.VISIBLE);
                    } else {
//                        translatebtn.setVisibility(GONE);
                    }
                    userImage.setVisibility(GONE);
                }

                /* Chat Translation*/
//                translatebtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        String translatedMsg = null;
//                        new TranslateLanguageTasks(ChatActivity.this, chat.getMessage(), true) {
//
//                            @Override
//                            protected void onPostExecute(String translatedMsg) {
//                                chat.setMessage(translatedMsg);
//                                notifyItemChanged(getAdapterPosition());
//
//                            }
//                        }.execute();
//                        Log.i(TAG, "translatedMsgonClick2: " + translatedMsg);
//                    }
//                });
            }
        }

        public class ReceiveImageHolder extends RecyclerView.ViewHolder {
            RoundedImageView userImage;
            RoundedImageView chatImage;
            ImageView btnDownload;
            ProgressWheel downloadProgress;
            RelativeLayout downloadLay;
            TextView txtChatTime;

            public ReceiveImageHolder(View view) {
                super(view);
                userImage = view.findViewById(R.id.userImage);
                chatImage = view.findViewById(R.id.chatImage);
                btnDownload = view.findViewById(R.id.btnDownload);
                downloadProgress = view.findViewById(R.id.downloadProgress);
                downloadLay = view.findViewById(R.id.downloadLay);
                txtChatTime = view.findViewById(R.id.txtChatTime);
            }

            public void bind(ChatResponse chat) {
                Timber.tag(TAG).d("ReceiveImageHolder: %d, %s", getAbsoluteAdapterPosition(), new Gson().toJson(chat));
//                downloadLay.setAlpha(1f);
                downloadLay.setVisibility(GONE);
                downloadProgress.setVisibility(GONE);
                downloadProgress.stopSpinning();

                if (isAdminChat) {
                    userImage.setVisibility(GONE);
                } else {
                    Glide.with(mContext)
                            .load(partnerImage)
                            .apply(new RequestOptions().placeholder(R.drawable.alhawana).error(R.drawable.alhawana))
                            .into(userImage);
                    userImage.setVisibility(GONE);
                }

                txtChatTime.setText(AppUtils.getChatTime(mContext, chat.getChatTime()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Uri fileUri = storageUtils.getFileUri(Constants.TAG_IMAGE, getFileName(chat.getMessage()), Constants.TAG_IMAGE);
                    if (fileUri != null) {
                        setMediaImage(mContext, fileUri, null, chatImage);
                    } else {
                        downloadLay.setVisibility(View.VISIBLE);
                        loadBlurImage(chat, chatImage);
                    }
                } else {
                    File file = storageUtils.getSrcFile(Constants.TAG_IMAGE, getFileName(chat.getMessage()), Constants.TAG_IMAGE);
                    if (file != null && !TextUtils.isEmpty(file.getAbsolutePath()) && file.exists()) {
                        setMediaImage(mContext, null, file, chatImage);
                    } else {
                        downloadLay.setVisibility(View.VISIBLE);
                        loadBlurImage(chat, chatImage);
                    }
                }

                btnDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isRecording)
                            return;
                        if (!PermissionsUtils.checkStoragePermission(mContext)) {
                            requestStoragePermissions();
                        } else {
                            if (NetworkReceiver.isConnected()) {
                                downloadImageFile(chat, StorageUtils.TAG_IMAGE, StorageUtils.TAG_IMAGE);
                            } else {
                                App.makeToast(getString(R.string.no_internet_connection));
                            }
                        }
                    }
                });

                chatImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isRecording)
                            return;
                        boolean validFile = false;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Uri fileUri = storageUtils.getFileUri(Constants.TAG_IMAGE, getFileName(chat.getMessage()), StorageUtils.TAG_IMAGE);
                            if (fileUri != null) {
                                validFile = true;
                                stopAudioViewHolder();
                                App.openImage(mContext, fileUri, Constants.TAG_MESSAGE, chatImage);
                            }
                        } else {
                            File file = storageUtils.getSrcFile(StorageUtils.TAG_IMAGE, getFileName(chat.getMessage()), StorageUtils.TAG_IMAGE);
                            if (file != null) {
                                validFile = true;
                                stopAudioViewHolder();
                                App.openImage(mContext, file.getAbsolutePath(), Constants.TAG_MESSAGE, chatImage);
                            }
                        }
                        if (!validFile) {
                            btnDownload.performClick();
                        }
                    }
                });
            }

            private void downloadImageFile(ChatResponse chat, String from, String fileType) {
                downloadLay.setVisibility(View.VISIBLE);
                downloadProgress.setVisibility(View.VISIBLE);
                downloadProgress.spin();
                btnDownload.setVisibility(GONE);
                File tempFile = new File(StorageUtils.getInstance(mContext).getExtCacheDir(), getFileName(chat.getMessage()));
                Data.Builder builder = new Data.Builder();
                builder.putString(Constants.TAG_URL, chat.getMessage());
                builder.putString(Constants.TAG_FILE_NAME, tempFile.getName());
                builder.putString(Constants.TAG_OUTPUT_PATH, tempFile.getAbsolutePath());
                builder.putString(Constants.TAG_FROM, from);

                OneTimeWorkRequest downloadWorkRequest = new OneTimeWorkRequest.Builder(FileDownloadWorker.class)
                        .setConstraints(new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build())
                        .setInputData(builder.build())
                        .build();
                WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                workManager.enqueue(downloadWorkRequest);
                workManager.getWorkInfoByIdLiveData(downloadWorkRequest.getId()).observe(ChatActivity.this, workInfo -> {
                    if (workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            String downloadPath = workInfo.getOutputData().getString(Constants.TAG_OUTPUT_PATH);
                            Timber.tag(TAG).d("downloadImageFile: %s", downloadPath);
                            File inputFile = new File(downloadPath);
                            Uri outputUri;
                            File outputFile;
                            String durationStr;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                outputUri = StorageUtils.getInstance(ChatActivity.this).saveFileInStorageV10(inputFile, inputFile.getName(), fileType);
                                setMediaImage(mContext, outputUri, null, chatImage);
                            } else {
                                outputFile = StorageUtils.getInstance(ChatActivity.this).saveFileInStorage(inputFile, from, fileType);
                                setMediaImage(mContext, null, outputFile, chatImage);
                            }
                            downloadLay.setVisibility(GONE);
                            downloadProgress.setVisibility(GONE);
                            downloadProgress.stopSpinning();
                        } else {
                            downloadLay.setVisibility(View.VISIBLE);
                            btnDownload.setVisibility(View.VISIBLE);
                            downloadProgress.setVisibility(GONE);
                            downloadProgress.stopSpinning();
                            loadBlurImage(chat, chatImage);
                        }
                    }
                });
            }
        }

        private void setMediaImage(Context mContext, Uri imageUri, File imageFile, ImageView chatImage) {
            Glide.with(mContext)
                    .asBitmap()
                    .load(imageUri != null ? imageUri : imageFile)
                    .error(R.color.colorGrey)
                    .sizeMultiplier(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(chatImage);
        }

        private void setErrorImage(Context mContext, String imageUrl, ImageView chatImage) {
            if (TextUtils.isEmpty(imageUrl)) {
                Glide.with(mContext).load(R.color.colorGrey).sizeMultiplier(0.5f)
                        .into(chatImage);
            } else {
                Glide.with(mContext).load(imageUrl).error(R.color.colorGrey).sizeMultiplier(0.5f)
                        .transform(new BlurTransformation(30))
                        .into(chatImage);
            }
        }

        private void loadBlurImage(ChatResponse chat, ImageView chatImage) {
            Glide.with(mContext).load(chat.getMessage()).sizeMultiplier(0.5f)
                    .transform(new BlurTransformation(30))
                    .into(chatImage);
        }

        /*Emoji and Gif in chat*/
        public class ReceiveGifHolder extends RecyclerView.ViewHolder {
            RoundedImageView userImage;
            ImageView btnDownload;
            ProgressWheel downloadProgress;
            RelativeLayout downloadLay;
            TextView txtChatTime;
            ImageView gifImage;

            public ReceiveGifHolder(View view) {
                super(view);
                userImage = view.findViewById(R.id.userImage);
                btnDownload = view.findViewById(R.id.btnDownload);
                downloadProgress = view.findViewById(R.id.downloadProgress);
                downloadLay = view.findViewById(R.id.downloadLay);
                txtChatTime = view.findViewById(R.id.txtChatTime);
                gifImage = view.findViewById(R.id.gifImage);
            }

            public void bind(ChatResponse chat) {
                downloadLay.setVisibility(GONE);
                if (isAdminChat) {
                    userImage.setVisibility(GONE);
                } else {
                    Glide.with(mContext)
                            .load(partnerImage)
                            .apply(new RequestOptions().placeholder(R.drawable.alhawana).error(R.drawable.alhawana))
                            .into(userImage);
                    userImage.setVisibility(GONE);
                }
                txtChatTime.setText(AppUtils.getChatTime(mContext, chat.getChatTime()));
                Glide
                        .with(mContext)
                        .asGif()
                        .load(chat.getMessage())
                        .into(gifImage);
                downloadProgress.stopSpinning();
                downloadLay.setVisibility(GONE);

             /*   gifImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isRecording)
                            return;
                        Intent imageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
                        imageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        imageIntent.putExtra(Constants.TAG_USER_IMAGE, chat.getMessage());
                        imageIntent.putExtra(Constants.TAG_FROM, Constants.TAG_GIF);
                        startActivity(imageIntent);
                    }
                });*/
            }
        }

        private void setGifImage(Context mContext, String attachment, ImageView gifImage) {
            Glide
                    .with(mContext)
                    .asGif()
                    .transform(new GranularRoundedCorners(18, 18, 0, 0))
                    .load(attachment)
                    .into(gifImage);
        }

        public class ReceiveStreamHolder extends RecyclerView.ViewHolder {
            RoundedImageView userImage;
            TextView txtMessage;
            TextView txtChatTime;
            RelativeLayout itemLay;

            public ReceiveStreamHolder(View view) {
                super(view);
                userImage = view.findViewById(R.id.userImage);
                txtMessage = view.findViewById(R.id.txtMessage);
                txtChatTime = view.findViewById(R.id.txtChatTime);
                itemLay = view.findViewById(R.id.itemLay);
            }

            public void bind(ChatResponse chat) {
                Glide.with(mContext)
                        .load(partnerImage)
                        .apply(new RequestOptions().placeholder(R.drawable.alhawana).error(R.drawable.alhawana))
                        .into(userImage);
                userImage.setVisibility(GONE);
                String[] link = chat.getMessage().split(Constants.TAG_HTTP);
                String message = null;
                if (link.length > 1) {
                    message = Constants.TAG_HTTP + link[1];
                    SpannableStringBuilder str = new SpannableStringBuilder(link[0] + message);
                    int startIndex = link[0].length();
                    int endIndex;
                    if (message.charAt(message.length() - 1) == '\"') {
                        endIndex = link[0].length() + message.length() - 1;
                    } else {
                        endIndex = link[0].length() + message.length();
                    }
//                    str.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    String finalMessage = message.trim();
                    str.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View view) {
                            if (NetworkReceiver.isConnected()) {
                                showLoadingDialog();
                                Log.i(TAG, "onClick: " + finalMessage);
                                FirebaseDynamicLinks.getInstance()
                                        .getDynamicLink(Uri.parse(finalMessage))
                                        .addOnSuccessListener(new OnSuccessListener<PendingDynamicLinkData>() {
                                            @Override
                                            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                                                Uri deepLink = null;
                                                if (pendingDynamicLinkData != null) {
                                                    deepLink = pendingDynamicLinkData.getLink();
                                                    if (deepLink.getQueryParameter("stream_name") != null) {
                                                        String streamName = deepLink.getQueryParameter("stream_name");
                                                        getStreamDetails(streamName);
                                                    }
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Logging.e(TAG, "onSuccess: " + e.getMessage());
                                            }
                                        });
                            }
                        }
                    }, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    txtMessage.setText(str);
                } else {
                    message = link[0];
                    txtMessage.setText(message);
                }
                txtMessage.setMovementMethod(LinkMovementMethod.getInstance());
//                txtMessage.setText(chat.getMessage());
                txtChatTime.setText(AppUtils.getChatTime(mContext, chat.getChatTime()));
            }
        }

        public class SentAudioHolder extends RecyclerView.ViewHolder {
            ImageView btnUpload;
            SeekBar songSeekbar;
            TextView txtFileName;
            TextView txtDuration;
            ProgressWheel uploadProgress;
            ImageView btnPlay;
            RelativeLayout contentLay;
            FrameLayout uploadLay;
            RoundedImageView userImage;
            RelativeLayout itemLay;
            TextView txtChatTime;

            public SentAudioHolder(View view) {
                super(view);
                btnUpload = view.findViewById(R.id.btnUpload);
                songSeekbar = view.findViewById(R.id.song_seekbar);
                txtFileName = view.findViewById(R.id.txtFileName);
                txtDuration = view.findViewById(R.id.txtDuration);
                uploadProgress = view.findViewById(R.id.uploadProgress);
                btnPlay = view.findViewById(R.id.btnPlay);
                contentLay = view.findViewById(R.id.contentLay);
                uploadLay = view.findViewById(R.id.uploadLay);
                userImage = view.findViewById(R.id.userImage);
                itemLay = view.findViewById(R.id.itemLay);
                txtChatTime = view.findViewById(R.id.txtChatTime);
            }

            public void bind(ChatResponse chat) {
                Timber.tag(TAG).d("SentAudioHolder: %d, %s", getAbsoluteAdapterPosition(), new Gson().toJson(chat));
                txtDuration.setVisibility(GONE);
                if (currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                    if (MediaPlayerUtils.isPlaying()) {
                        btnPlay.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_play_white));
                        songSeekbar.setMax(MediaPlayerUtils.getTotalDuration());
                        songSeekbar.setProgress(MediaPlayerUtils.getCurrentDuration());
                    } else {
                        btnPlay.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_play_white));
                        File file = storageUtils.getSrcFile(StorageUtils.TAG_AUDIO_SENT, getFileName(chat.getMessage()), StorageUtils.TAG_AUDIO);
                        if (file != null) {
                            long totalDuration = storageUtils.getMediaDuration(file);
                            txtDuration.setText(milliSecondsToTimer(totalDuration));
                        }
                    }
                    songSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_white));
                                MediaPlayerUtils.applySeekBarValue(progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            if (MediaPlayerUtils.isPlaying() && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_white));
                            }
                        }
                    });
                } else {
                    btnPlay.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_play_white));
                    songSeekbar.setProgress(0);
                    File file = storageUtils.getSrcFile(StorageUtils.TAG_AUDIO_SENT, getFileName(chat.getMessage()), Constants.TAG_AUDIO);
                    if (file != null) {
                        long totalDuration = storageUtils.getMediaDuration(file);
                        songSeekbar.setMax((int) totalDuration);
                        txtDuration.setText(milliSecondsToTimer(totalDuration));
                        txtDuration.setVisibility(View.VISIBLE);
                        songSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if(isRecording)
                                    return;
                                if (fromUser && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                    btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_white));
                                    MediaPlayerUtils.applySeekBarValue(progress);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                if (MediaPlayerUtils.isPlaying() && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                    btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_white));
                                }
                            }
                        });
                    } else {
                        songSeekbar.setMax(0);
                        txtDuration.setVisibility(GONE);
                    }
                }
                txtChatTime.setText(AppUtils.getChatTime(mContext, chat.getChatTime()));
                btnUpload.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.upload));

                switch (chat.getProgress()) {
                    case "":
                        uploadProgress.setVisibility(View.VISIBLE);
                        uploadProgress.spin();
                        btnUpload.setVisibility(View.VISIBLE);
                        songSeekbar.setVisibility(View.INVISIBLE);
                        txtFileName.setText(R.string.uploading);
                        txtFileName.setVisibility(View.VISIBLE);
                        break;
                    case Constants.TAG_COMPLETED:
                        uploadProgress.setVisibility(GONE);
                        uploadProgress.stopSpinning();
                        btnUpload.setVisibility(GONE);
                        songSeekbar.setVisibility(View.VISIBLE);
                        btnPlay.setVisibility(View.VISIBLE);
                        txtFileName.setVisibility(GONE);
                        txtDuration.setVisibility(View.VISIBLE);
                        File tempFile = storageUtils.getSrcFile(StorageUtils.TAG_AUDIO_SENT, getFileName((chat.getMessage())), StorageUtils.TAG_AUDIO);
                        if (tempFile == null || !tempFile.exists()) {
                            txtDuration.setVisibility(GONE);
                            songSeekbar.setMax(0);
//                            btnUpload.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.download));
                        }
                        break;
                    case "error":
                        uploadProgress.setVisibility(View.VISIBLE);
                        uploadProgress.stopSpinning();
                        btnUpload.setVisibility(View.VISIBLE);
                        songSeekbar.setVisibility(View.INVISIBLE);
                        btnPlay.setVisibility(GONE);
                        txtFileName.setText(R.string.retry);
                        txtFileName.setVisibility(View.VISIBLE);
                        break;
                }

                userImage.setVisibility(GONE);
                ChatResponse chatResponse = dbHelper.getLastReadMessage(chatId);
                if (chatResponse.getMessageId() != null && chatResponse.getMessageId().equals(chat.getMessageId())) {
                    if (chat.getDeliveryStatus().equals(Constants.TAG_READ)) {
                        userImage.setVisibility(GONE);
                        Glide.with(mContext)
                                .load(Constants.USER_IMAGE_URL + partnerImage)
                                .apply(new RequestOptions().placeholder(R.drawable.alhawana).error(R.drawable.alhawana))
                                .into(userImage);
                    }
                }

                btnUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (NetworkReceiver.isConnected()) {
                            File savedFile = storageUtils.getSrcFile(StorageUtils.TAG_AUDIO_SENT, getFileName((chat.getMessage())), StorageUtils.TAG_AUDIO);
                            if (savedFile != null) {
                                File uploadedFile = storageUtils.getImage(StorageUtils.TAG_AUDIO_SENT, chat.getMessage());
                                uploadProgress.spin();
                                uploadAudio(storageUtils.getFileBytes(uploadedFile.getAbsolutePath()), getFileName(uploadedFile.getAbsolutePath()), new JSONObject());
                            }
                        } else {
                            App.makeToast(getString(R.string.no_internet_connection));
                        }
                    }
                });

                btnPlay.setOnClickListener(view -> {
                    if(isRecording)
                        return;
                    File file = storageUtils.getSrcFile(StorageUtils.TAG_AUDIO_SENT, getFileName(chat.getMessage()), StorageUtils.TAG_AUDIO);
                    Uri voiceURI = storageUtils.getUriFromFile(file);
                    Log.d(TAG, "bind: "+file);
                    Log.d(TAG, "bind1: "+voiceURI);
                    if (file != null) {
                        txtDuration.setVisibility(View.VISIBLE);
                        if (currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                            if (MediaPlayerUtils.isPlaying()) {
                                btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_white));
                                MediaPlayerUtils.pauseMediaPlayer();
                            } else {
                                songSeekbar.setMax(storageUtils.getMediaDuration(file));
                                btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_white));
                                startPlayer(voiceURI, songSeekbar.getProgress(), mContext);
                            }
                        } else {
                            stopAudioViewHolder();
                            currentMessageId = chat.getMessageId();
                            songSeekbar.setMax(storageUtils.getMediaDuration(file));
                            btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_white));
                            startPlayer(voiceURI, songSeekbar.getProgress(), mContext);
                        }
                    } else {
                        /*downloadFile(Constants.CHAT_IMG_PATH + message.attachment,
                                TAG_AUDIO_SENT, downloadLay, progressbar);*/
                        Toast.makeText(mContext, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
                    }
                });
                songSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(isRecording)
                            return;
                        if (fromUser && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                            btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_white));
                            MediaPlayerUtils.applySeekBarValue(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (MediaPlayerUtils.isPlaying() && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                            btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_white));
                        }
                    }
                });
            }
        }

        public class ReceiveAudioHolder extends RecyclerView.ViewHolder {
            RoundedImageView userImage;
            ImageView btnDownload;
            SeekBar songSeekbar;
            TextView txtFileName;
            TextView txtDuration;
            ProgressWheel downloadProgress;
            ImageView btnPlay;
            RelativeLayout contentLay;
            FrameLayout downloadLay;
            TextView txtChatTime;
            RelativeLayout itemLay;

            public ReceiveAudioHolder(View view) {
                super(view);
                userImage = view.findViewById(R.id.userImage);
                btnDownload = view.findViewById(R.id.btnDownload);
                songSeekbar = view.findViewById(R.id.song_seekbar);
                txtFileName = view.findViewById(R.id.txtFileName);
                txtDuration = view.findViewById(R.id.txtDuration);
                downloadProgress = view.findViewById(R.id.downloadProgress);
                btnPlay = view.findViewById(R.id.btnPlay);
                contentLay = view.findViewById(R.id.contentLay);
                downloadLay = view.findViewById(R.id.downloadLay);
                txtChatTime = view.findViewById(R.id.txtChatTime);
                itemLay = view.findViewById(R.id.itemLay);
            }

            public void bind(ChatResponse chat) {
                Timber.tag(TAG).d("ReceiveAudioHolder: %d, %s", getAbsoluteAdapterPosition(), new Gson().toJson(chat));
                Glide.with(mContext)
                        .load(Constants.USER_IMAGE_URL + partnerImage)
                        .apply(new RequestOptions().placeholder(R.drawable.alhawana).error(R.drawable.alhawana))
                        .into(userImage);
                userImage.setVisibility(GONE);
                txtChatTime.setText(AppUtils.getChatTime(mContext, chat.getChatTime()));
                if (currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                    if (MediaPlayerUtils.isPlaying()) {
                        btnPlay.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_pause_white));
                        songSeekbar.setMax(MediaPlayerUtils.getTotalDuration());
                        songSeekbar.setProgress(MediaPlayerUtils.getCurrentDuration());
                    } else {
                        btnPlay.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_play_white));
                        Uri voiceURI = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            voiceURI = storageUtils.getFileUri(StorageUtils.TAG_AUDIO, getFileName(chat.getMessage()), StorageUtils.TAG_AUDIO);
                        } else {
                            File file = storageUtils.getSrcFile(StorageUtils.TAG_AUDIO, getFileName(chat.getMessage()), StorageUtils.TAG_AUDIO);
                            voiceURI = storageUtils.getUriFromFile(file);
                        }
                        if (voiceURI != null) {
                            setAudioUri(chat, voiceURI);
                        }
                    }
                    songSeekbar.setEnabled(true);
                    songSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if(isRecording)
                                return;
                            if (fromUser && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_white));
                                MediaPlayerUtils.applySeekBarValue(progress);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            if (MediaPlayerUtils.isPlaying() && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_white));
                            }
                        }
                    });
                } else {
                    btnPlay.setImageResource(R.drawable.ic_play_white);
                    Uri voiceURI = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        voiceURI = storageUtils.getFileUri(StorageUtils.TAG_AUDIO, getFileName(chat.getMessage()), StorageUtils.TAG_AUDIO);
                    } else {
                        File file = storageUtils.getSrcFile(StorageUtils.TAG_AUDIO, getFileName(chat.getMessage()), StorageUtils.TAG_AUDIO);
                        voiceURI = storageUtils.getUriFromFile(file);
                    }
                    if (voiceURI != null) {
                        setAudioUri(chat, voiceURI);
                    } else {
                        setError();
                    }
                    songSeekbar.setProgress(0);
                }

                btnDownload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isRecording)
                            return;
                        if (!PermissionsUtils.checkStoragePermission(mContext)) {
                            requestStoragePermissions();
                        } else {
                            if (NetworkReceiver.isConnected()) {
                                downloadAudioFile(chat, StorageUtils.TAG_AUDIO, StorageUtils.TAG_AUDIO);
                            } else {
                                App.makeToast(getString(R.string.no_internet_connection));
                            }
                        }
                    }
                });

                btnPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isRecording)
                            return;
                        Uri voiceURI = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            voiceURI = storageUtils.getFileUri(StorageUtils.TAG_AUDIO, getFileName(chat.getMessage()), StorageUtils.TAG_AUDIO);
                            Log.d(TAG, "onClick: "+ voiceURI);
                        } else {
                            File file = storageUtils.getSrcFile(StorageUtils.TAG_AUDIO, getFileName(chat.getMessage()), StorageUtils.TAG_AUDIO);
                            voiceURI = storageUtils.getUriFromFile(file);
                        }

                        if (voiceURI != null) {
                            txtDuration.setVisibility(View.VISIBLE);
                            if (currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                                if (MediaPlayerUtils.isPlaying()) {
                                    btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_white));
                                    MediaPlayerUtils.pauseMediaPlayer();
                                } else {
                                    songSeekbar.setMax(storageUtils.getMediaDuration(mContext, voiceURI));
                                    btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_white));
                                    startPlayer(voiceURI, songSeekbar.getProgress(), mContext);
                                }
                            } else {
                                stopAudioViewHolder();
                                currentMessageId = chat.getMessageId();
                                songSeekbar.setMax(storageUtils.getMediaDuration(mContext, voiceURI));
                                btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_white));
                                startPlayer(voiceURI, songSeekbar.getProgress(), mContext);
                            }
                        } else {
                            Toast.makeText(mContext, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            private void downloadAudioFile(ChatResponse chat, String from, String fileType) {
                downloadProgress.spin();
                btnDownload.setVisibility(GONE);
                File tempFile = new File(StorageUtils.getInstance(mContext).getExtCacheDir(), getFileName(chat.getMessage()));
                Data.Builder builder = new Data.Builder();
                builder.putString(Constants.TAG_URL, chat.getMessage());
                builder.putString(Constants.TAG_FILE_NAME, tempFile.getName());
                builder.putString(Constants.TAG_OUTPUT_PATH, tempFile.getAbsolutePath());
                builder.putString(Constants.TAG_FROM, from);

                OneTimeWorkRequest downloadWorkRequest = new OneTimeWorkRequest.Builder(FileDownloadWorker.class)
                        .setConstraints(new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build())
                        .setInputData(builder.build())
                        .build();
                WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                workManager.enqueue(downloadWorkRequest);
                workManager.getWorkInfoByIdLiveData(downloadWorkRequest.getId()).observe(ChatActivity.this, workInfo -> {
                    if (workInfo.getState().isFinished()) {
                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                            String downloadPath = workInfo.getOutputData().getString(Constants.TAG_OUTPUT_PATH);
                            Timber.tag(TAG).d("downloadAudioFile: %s", downloadPath);
                            File inputFile = new File(downloadPath);
                            Uri outputUri;
                            File outputFile;
                            String durationStr;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                outputUri = StorageUtils.getInstance(ChatActivity.this).saveFileInStorageV10(inputFile, inputFile.getName(), fileType);
//                                long duration = storageUtils.getAudioDuration(ChatActivity.this, outputUri, StorageUtils.TAG_AUDIO);
//                                durationStr = AppUtils.formateMilliSeccond(duration);

                                setAudioUri(chat, outputUri);
                            } else {
                                outputFile = StorageUtils.getInstance(ChatActivity.this).saveFileInStorage(inputFile, from, fileType);
//                                int duration = storageUtils.getMediaDuration(outputFile);
//                                durationStr = AppUtils.formateMilliSeccond(duration);
                                outputUri = storageUtils.getUriFromFile(outputFile);
                                setAudioUri(chat, outputUri);
                            }
                        } else {
                            setError();
                        }
                    }
                });
            }

            private void setAudioUri(ChatResponse chat, Uri voiceURI) {
                downloadLay.setVisibility(GONE);
                btnDownload.setVisibility(GONE);
                downloadProgress.setVisibility(GONE);
                downloadProgress.stopSpinning();
                txtDuration.setVisibility(View.VISIBLE);
                long totalDuration = storageUtils.getAudioDuration(mContext, voiceURI, StorageUtils.TAG_AUDIO);
                txtDuration.setText(milliSecondsToTimer(totalDuration));
                songSeekbar.setEnabled(true);
                songSeekbar.setProgress(0);
                songSeekbar.setMax((int) totalDuration);
                btnPlay.setVisibility(View.VISIBLE);
                songSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(isRecording)
                            return;
                        if (fromUser && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                            btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_play_white));
                            MediaPlayerUtils.applySeekBarValue(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        if (MediaPlayerUtils.isPlaying() && currentMessageId != null && currentMessageId.equals(chat.getMessageId())) {
                            btnPlay.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_pause_white));
                        }
                    }
                });
            }

            public void setError() {
                downloadLay.setVisibility(View.VISIBLE);
                btnDownload.setVisibility(View.VISIBLE);
                downloadProgress.setVisibility(View.VISIBLE);
                downloadProgress.stopSpinning();
                songSeekbar.setEnabled(false);
                btnPlay.setVisibility(GONE);
                txtDuration.setVisibility(View.INVISIBLE);
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            ProgressBar progressBar;

            public FooterViewHolder(View parent) {
                super(parent);
                progressBar = parent.findViewById(R.id.progressBar);
            }
        }
    }

    private void startPlayer(Uri voiceURI, int progress, Context mContext) {
        try {
            isPlayplaying = true;
            App.pauseExternalAudio(mContext);
            MediaPlayerUtils.startAndPlayMediaPlayer(mContext, voiceURI, ChatActivity.this, progress);
        } catch (IOException e) {
            Log.e(TAG, "startPlayer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void getStreamDetails(String streamName) {
        Log.d(TAG, "getStreamDetails: "+streamName);
        ApiInterface apiInterfacelive;
        apiInterfacelive= ApiClient.getClient().create(ApiInterface.class);
        Call<StreamDetails> call3 = apiInterfacelive.getStreamDetails(SharedPref.getString(SharedPref.AUTH_TOKEN,""), SharedPref.getString(SharedPref.USER_ID,""), streamName);
        call3.enqueue(new Callback<StreamDetails>() {
            @Override
            public void onResponse(Call<StreamDetails> call, Response<StreamDetails> response) {
                if (response.isSuccessful()) {

                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        hideLoadingDialog();
                        StreamDetails streamData = response.body();


                        Log.d(TAG, "onResponse: "+new Gson().toJson(streamData));

                        if (!streamData.getType().equals(Constants.TAG_LIVE)) {
                            Intent intent = new Intent(ChatActivity.this, PlayerActivity.class);
                            intent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                            intent.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                            intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                            intent.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                            intent.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                            intent.putExtra(Constants.TAG_FROM, "MainActivity");
                            intent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                            intent.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                            intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                            intent.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                            intent.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                            intent.putExtra("stream_data",streamData);
                            startActivity(intent);
                        } else {
                            /*ZEGOLiveStreamingManager.getInstance().init();
                            Intent intent = new Intent(ChatActivity.this, PkLiveStreamingActivity.class);
                            intent.putExtra("from", "viewer"); //from this page
                            intent.putExtra("type", streamData.getStreamType());
                            intent.putExtra("publisherName",streamData.getPostedBy());
                            intent.putExtra(StreamConstants.TAG_TITLE, streamData.getTitle());
                            intent.putExtra(StreamConstants.TAG_STREAM_NAME, streamData.getName());
                            intent.putExtra(StreamConstants.TAG_INVITE_LINK, streamData.getInviteLink());
                            intent.putExtra(StreamConstants.TAG_STREAM_TOKEN, "");
                            intent.putExtra("hostId", streamData.getPublisherId());
                            intent.putExtra("hostName",streamData.getPostedBy());
                            intent.putExtra("hostImage",streamData.getPublisherImage());
                            intent.putExtra("host", false );
                            intent.putExtra("liveID", streamData.getName());
                            intent.putExtra(Constants.TAG_USER_ID, GetSet.getUserId());
                            intent.putExtra(Constants.TAG_USER_NAME, GetSet.getName());
                            intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                            intent.putExtra(Constants.TAG_TOKEN, GetSet.getAuthToken());
                            intent.putExtra(Constants.TAG_STREAM_BASE_URL, GetSet.getStreamBaseUrl());
                            Bundle arguments = new Bundle();
                            arguments.putSerializable(StreamConstants.TAG_STREAM_DATA, streamData);
                            intent.putExtras(arguments);
                            intent.putExtra(Constants.TAG_FROM, Constants.TAG_SUBSCRIBE);
                            startActivity(intent);*/
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<StreamDetails> call, Throwable t) {
                call.cancel();
                Log.e(TAG, "onFailure: " + t.getMessage());
                hideLoadingDialog();
            }
        });
    }

    private void initLoadingDialog() {
        dialogLoading = new DialogLoading();
        dialogLoading.setContext(this);
        dialogLoading.setCancelable(false);
    }

    private void showLoadingDialog() {
        if (!dialogLoading.isAdded())
            dialogLoading.show(getSupportFragmentManager(), TAG);
    }

    private void hideLoadingDialog() {
        dialogLoading.dismissAllowingStateLoss();
    }

    class PostDiffCallback extends DiffUtil.Callback {

        private final List<ChatResponse> oldPosts, newPosts;
        private ChatResponse temp;

        public PostDiffCallback(List<ChatResponse> oldPosts, List<ChatResponse> newPosts) {
            this.oldPosts = oldPosts;
            this.newPosts = newPosts;
        }

        @Override
        public int getOldListSize() {
            return oldPosts.size();
        }

        @Override
        public int getNewListSize() {
            return newPosts.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            if ((oldPosts.get(oldItemPosition).getMessageType().equals(Constants.TAG_DATE) &&
                    newPosts.get(newItemPosition).getMessageType().equals(Constants.TAG_DATE))) {
                return true;
            } else {
                return oldPosts.get(oldItemPosition).getMessageId().equals(newPosts.get(newItemPosition).getMessageId());
            }
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            if ((oldPosts.get(oldItemPosition).getMessageType().equals(Constants.TAG_DATE) &&
                    newPosts.get(newItemPosition).getMessageType().equals(Constants.TAG_DATE))) {
                return true;
            } else {
                ChatResponse newProduct = newPosts.get(newItemPosition);
                ChatResponse oldProduct = oldPosts.get(oldItemPosition);
                return newProduct.getMessageId().equals(oldProduct.getMessageId()) &&
                        newProduct.getDeliveryStatus().equals(oldProduct.getDeliveryStatus());


            }
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            newPosts.get(newItemPosition).setDeliveryStatus(Constants.TAG_READ);
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }


    /*private ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {
        @Override
        public void onInserted(int position, int count) {
            // custom observer methods
            adapter.notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            // custom observer methods
            adapter.notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            // custom observer methods
            adapter.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count, Object payload) {
            // custom observer methods
            adapter.notifyItemRangeChanged(position, count);
        }
    };*/

    @SuppressLint("StaticFieldLeak")
    private class DownloadImage extends AsyncTask<String, Integer, Bitmap> {
        String imagePath;
        private Bitmap rotatedBitmap;
        int orientation = 0;
        String fileName;
        int fileLength;
        DownloadListener downloadListener;

        public DownloadImage(String imagePath, DownloadListener downloadListener) {
            this.imagePath = imagePath;
            this.downloadListener = downloadListener;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = /*Constants.CHAT_IMAGE_URL +*/ imagePath;
            Bitmap bitmap = null;
            fileName = getFileName(imageURL);
            rotatedBitmap = null;
            try {
                try {
                    InputStream inputStream = new URL(imageURL).openStream();
                    /*ExifInterface exif = null;     //Since API Level 5
                    try {
                        exif = new ExifInterface(inputStream);
                        orientation = exif.getRotationDegrees();
                        Logging.e(TAG, " orientation " + orientation);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    if (bitmap == null) {
                        return null;
                    } else {
//                        rotatedBitmap = rotateImage(bitmap, orientation);
                        rotatedBitmap = bitmap;
                        storageUtils.saveToSDCard(rotatedBitmap, StorageUtils.TAG_IMAGE, fileName + Constants.IMAGE_EXTENSION);
                    }

                } catch (Exception e) {
                    Logging.e("Error Message", e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return rotatedBitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.e(TAG, "onProgressUpdate: " + values[0]);
        }

        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            downloadListener.onDownloaded(result);
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public interface DownloadListener {
        void onDownloading();

        void onDownloaded(Bitmap bitmap);

        void onAudioDownloaded(String filePath);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0);
    }

    //Addon Voice Chat
//    OnRecordListener listener = new OnRecordListener() {
//        @Override
//        public void onStart() {
//            MediaPlayerUtils.pauseMediaPlayer();
//            if (currentMessageId != null) {
//                ChatResponse tempMsgData = dbHelper.getSingleMessage(currentMessageId);
//                int tempPosition = -1;
//                if (chatList.contains(tempMsgData)) {
//                    tempPosition = chatList.indexOf(tempMsgData);
//                }
//                if (tempPosition != -1) {
//                    RecyclerView.ViewHolder tempViewHolder = recyclerView.findViewHolderForAdapterPosition(tempPosition);
//                    if (tempViewHolder instanceof ChatAdapter.SentAudioHolder) {
//                        ChatAdapter.SentAudioHolder holder = (ChatAdapter.SentAudioHolder) tempViewHolder;
//
//                        holder.btnPlay.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_play_white));
//                    }
//                }
//            }
//            isRecording = true;
//            edtMessage.setVisibility(View.VISIBLE);
//            attachLay.setVisibility(GONE);
//            btnAdd.setVisibility(GONE);
//            btnGif.setVisibility(GONE);
//            if (!PermissionsUtils.checkRecordPermission(ChatActivity.this)) {
//                recordView.setOnRecordListener(null);
//                btnAdd.setVisibility(View.VISIBLE);
//                btnGif.setVisibility(View.VISIBLE);
//                requestAudioPermission();
//            } else {
//                if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
//                    App.makeToast(getString(R.string.unblock_description));
//                } else {
//                    startVoice();
//                }
//            }
//        }
//
//        @Override
//        public void onCancel() {
//            isRecording = false;
//            edtMessage.requestFocus();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    AppUtils.hideKeyboard(ChatActivity.this);
//                    edtMessage.requestFocus();
//                    edtMessage.setVisibility(View.VISIBLE);
//                    btnAdd.setVisibility(View.VISIBLE);
///*
//                    btnGif.setVisibility(View.VISIBLE);
//*/
//                    recordView.setVisibility(GONE);
//                }
//            }, 1000);
//
//        }
//
//        @Override
//        public void onFinish(long recordTime) {
//            isRecording = false;
//
//            if (recordTime >= 2000) {
//
//                socketDuration = AppUtils.formateMilliSecconds(recordTime);
//
//                edtMessage.requestFocus();
//                if (!NetworkReceiver.isConnected()) {
//                    AppUtils.showSnack(ChatActivity.this, findViewById(R.id.parentLay), false);
//                } else if (blockStatus != null && blockStatus.equals(Constants.TAG_TRUE)) {
//                    Log.i(TAG, "onFinish: ");
//                    if (null != mediaRecorder) {
//                        try {
//                            mediaRecorder.stop();
//                        } catch (RuntimeException ignored) {
//                        }
//                    }
//                    edtMessage.setVisibility(View.VISIBLE);
//                    btnAdd.setVisibility(GONE);
//                    btnGif.setVisibility(GONE);
//                    recordView.setVisibility(GONE);
//                } else {
//                    if (null != mediaRecorder) {
//                        try {
//                            mediaRecorder.stop();
//                        } catch (RuntimeException ignored) {
//                        }
//                    }
//
//                    edtMessage.setVisibility(View.VISIBLE);
//                    btnAdd.setVisibility(View.VISIBLE);
///*
//                    btnGif.setVisibility(View.VISIBLE);
//*/
//                    recordView.setVisibility(GONE);
//                    String fileName = getFileName(recordVoicePath);
//                    JSONObject jsonObject = updateDB(Constants.TAG_AUDIO, fileName, recordVoicePath, "", Constants.TAG_SENDING);
//                    Log.e("checkpath", "-" + recordVoicePath);
//                    uploadAudio(storageUtils.getFileBytes(recordVoicePath), fileName, jsonObject);
//                }
//            } else if (!PermissionsUtils.checkRecordPermission(ChatActivity.this)) {
//                stopAudioViewHolder();
//                App.makeToast(getString(R.string.audio_record_permission_error));
//            } else {
//                stopAudioViewHolder();
//                edtMessage.setVisibility(View.VISIBLE);
//                btnAdd.setVisibility(View.VISIBLE);
///*
//                btnGif.setVisibility(View.VISIBLE);
//*/
//                recordView.setVisibility(GONE);
//                AppUtils.hideKeyboard(ChatActivity.this);
//                App.makeToast(getString(R.string.audio_length_error));
//            }
//
//
//        }
//
//        @Override
//        public void onLessThanSecond() {
//            stopAudioViewHolder();
//            edtMessage.setVisibility(View.VISIBLE);
//            btnAdd.setVisibility(View.VISIBLE);
    ///*
//            btnGif.setVisibility(View.VISIBLE);
//*/
//            recordView.setVisibility(GONE);
//            AppUtils.hideKeyboard(ChatActivity.this);
//            if (recordAudioPermission == 0) {
//                App.makeToast(getString(R.string.hold_audio));
//            }
//
//        }
//    };

    //Addon Voice Chat
//    private void setVoiceRecorder() {
//        recordView.setCounterTimeColor(Color.parseColor("#000000"));
//        recordView.setSmallMicColor(ContextCompat.getColor(ChatActivity.this, R.color.colorPrimary));
//        recordView.setSlideToCancelTextColor(Color.parseColor("#000000"));
//        recordView.setLessThanSecondAllowed(false);
//        recordView.setSlideToCancelText(getString(R.string.slide_to_cancel));
//
//        recordView.setSmallMicIcon(R.drawable.recv_ic_mic_white);
//
//        recordView.setCustomSounds(0, 0, 0);
//
//        recordView.setCancelBounds(80);
//
//        btnRecord.setOnRecordClickListener(new OnRecordClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//
//        recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
//            @Override
//            public void onAnimationEnd() {
//
//            }
//        });
//
//        btnRecord.setRecordView(recordView);
//        recordView.setOnRecordListener(listener);
//    }

    public void resumeExternalAudio(Context context) {
        if (AppUtils.isExternalPlay) {
            AppUtils.isExternalPlay = false;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "play");
            context.sendBroadcast(i);
        }
    }

    private String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public void MediaRecorderReady() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(recordVoicePath);
    }

    //Addon Voice Record
/*    private void startVoice() {
        btnAdd.setVisibility(GONE);
        btnGif.setVisibility(GONE);

//        recordView.setVisibility(View.VISIBLE);
        edtMessage.setVisibility(GONE);
        if (PermissionsUtils.checkRecordPermission(ChatActivity.this)) {
            if (!NetworkReceiver.isConnected()) {
                AppUtils.showSnack(ChatActivity.this, findViewById(R.id.parentLay), false);
            } else {
                String fileName = (getString(R.string.app_name) + "_" + System.currentTimeMillis() + Constants.AUDIO_EXTENSION).replaceAll(" ", "");
                File mDir = storageUtils.getDirectory(StorageUtils.TAG_AUDIO_SENT, true);
                recordVoicePath = new File(mDir, fileName).getAbsolutePath();
                MediaRecorderReady();
                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();

                } catch (Exception e) {
                    Log.e(TAG, "startVoice: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            requestAudioPermission();
        }
    }*/

    public void pauseExternalAudio(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        if (manager.isMusicActive()) {
            AppUtils.isExternalPlay = true;
            Intent i = new Intent("com.android.music.musicservicecommand");
            i.putExtra("command", "pause");
            context.sendBroadcast(i);
        }
    }

    private void playMedia(final Context context, final int position, final String from) {
        try {
            ImageView play;
            TextView time;
            SeekBar seek;
            View itemView;
            boolean isAudioPlay = false;
            if (playingPosition != position) {
                if (player.isPlaying() || playingPosition != -1) {
                    itemView = mLayoutManager.findViewByPosition(playingPosition);
                    assert itemView != null;
                    play = itemView.findViewById(R.id.btnPlay);
                    time = itemView.findViewById(R.id.txtDuration);
                    seek = itemView.findViewById(R.id.song_seekbar);
                    time.setText(milliSecondsToTimer(player.getDuration()));
                    play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_white));
                    player.pause();
                    if (seekBar != null)
                        seekBar.setProgress(0);
                    seekBar = null;
                }
                playingPosition = position;
                itemView = mLayoutManager.findViewByPosition(playingPosition);
                play = itemView.findViewById(R.id.btnPlay);
                time = itemView.findViewById(R.id.txtDuration);
                seek = itemView.findViewById(R.id.song_seekbar);
                play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause_white));
                isAudioPlay = true;
                if (player != null) {
                    player.stop();
                    player.reset();
                }
                if (seekBar != null) {
                    seekBar.setProgress(0);
                    seekBar = null;
                }

                Log.d(TAG, "getmessage res: " + chatList.get(position).getMessage());

                File file = storageUtils.getImage(from, getFileName(chatList.get(position).getMessage()));

                /*Log.d(TAG, "playMedia: "+file.getAbsolutePath());*/

                //Uri voiceURI = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
                seek.setTag(playingPosition);
                seekBar = seek;
                txtAudioTime = time;

                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    player.setDataSource(getApplicationContext(), Uri.fromFile(file.getAbsoluteFile()));
                    pauseExternalAudio(context);
                    player.prepareAsync();
                    player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                seekHandler.removeCallbacks(moveSeekBarThread);
            } else {
                itemView = mLayoutManager.findViewByPosition(playingPosition);
                play = itemView.findViewById(R.id.btnPlay);
                time = itemView.findViewById(R.id.txtDuration);
                seek = itemView.findViewById(R.id.song_seekbar);
                seekBar = seek;
                txtAudioTime = time;
                if (player.isPlaying()) {
                    isAudioPlay = false;
                    play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_white));
                    player.pause();
                    resumeExternalAudio(context);
                } else {
                    isAudioPlay = true;
                    pauseExternalAudio(context);
                    play.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause_white));
                    player.start();
                }
            }

            boolean finalIsAudioPlay = isAudioPlay;


            seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {

                        if (seekBar.getTag().toString().equals("" + playingPosition)) {
                            player.seekTo(progress);
                            seekBar.setProgress(progress);
                            long currentDuration = player.getCurrentPosition();
                            txtAudioTime.setText(milliSecondsToTimer(currentDuration));
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (seekBar.getTag().toString().equals("" + playingPosition)) {
                        if (!player.isPlaying() && finalIsAudioPlay) {
                            player.start();
                        }
                        player.seekTo(seekBar.getProgress());
                    }
                }
            });

            seekHandler.postDelayed(moveSeekBarThread, 100);

            ImageView finalPlay = play;
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    finalPlay.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_white));
                    seekBar.setProgress(0);
                }
            });
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    //Addon Smart Reply
    /*private void generateReplies(ChatResponse lastMessage) {
        // If the last message in the chat thread is not sent by the "other" user, don't generate
        // smart replies.
        Log.d(TAG, "generateReplies: txt" + lastMessage.getMessageType());
        Log.d(TAG, "generateReplies: received" + lastMessage.getMessageEnd());
        Log.d(TAG, "generateReplies: user id" + lastMessage.getUserId());
        Log.d(TAG, "generateReplies: chat id" + chatId);

        if (lastMessage.getMessageType().equals(Constants.TAG_TEXT) && lastMessage.getMessageEnd().equals(Constants.TAG_RECEIVED) && !lastMessage.getUserId().equals(chatId)) {
            mSmartRepliesRecycler.setVisibility(View.VISIBLE);
            List<TextMessage> chatHistory = new ArrayList<>();
            chatHistory.add(TextMessage.createForRemoteUser(lastMessage.getMessage(),
                    System.currentTimeMillis(), GetSet.getUserId()));

            smartReply.suggestReplies(chatHistory)
                    .addOnSuccessListener(smartReplySuggestionResult -> {
                        if (smartReplySuggestionResult.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                            // The conversation's language isn't supported, so
                            // the result doesn't contain any suggestions.
                            Log.e(TAG, "generateReplies: " + SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE);

                        } else if (smartReplySuggestionResult.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                            // Task completed successfully
                            // ...
                            List<SmartReplySuggestion> suggestion = smartReplySuggestionResult.getSuggestions();
                            Log.i(TAG, "generateRepliess: " + new Gson().toJson(suggestion));
                            mChipAdapter.setSuggestions(suggestion);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "generateRepliesfa: " + e.getMessage());
                        }
                    });
        } else {
            mSmartRepliesRecycler.setVisibility(View.GONE);
        }
    }*/

}