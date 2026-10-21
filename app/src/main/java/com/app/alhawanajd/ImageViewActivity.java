package com.app.alhawanajd;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;



import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.app.alhawanajd.BuildConfig;
import com.app.alhawanajd.R;
import com.app.addon.livestream.external.helper.StorageManager;

import com.app.external.ImagePicker;
import com.app.helper.NetworkReceiver;
import com.app.helper.OnSwipeTouchListener;
import com.app.helper.PermissionsUtils;
import com.app.helper.StorageUtils;
import com.app.helper.callback.OnOkCancelClickListener;
import com.app.model.GetSet;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.FileUtil;
import com.app.utils.SharedPref;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class ImageViewActivity extends BaseFragmentActivity {
    private static final String TAG = ImageViewActivity.class.getSimpleName();

    private static final int REQUEST_CODE_PICK_IMAGE = 100;

    ImageView imageView;
    ImageView btnEdit;
    LottieAnimationView lottieImage;
    ShimmerFrameLayout shimmerLayout;
    LinearLayout bottomLay;
    ProgressBar progressBar;
    ImageButton btnClose;
    FrameLayout parentLay;
    TextView txtChangePhoto;

    private ApiInterface apiInterface;
    int resultCode = 0;
    private RequestOptions profileImageRequest;
    StorageUtils storageUtils;
    String from;
    private String imagePath;
    private Uri imageUri;

    private File currentPhotoFile;
    private Uri mCurrentPhotoUri;
    private ActivityResultLauncher<String[]> cameraPermissionResult;
    private ActivityResultLauncher<String[]> storagePermissionResult;
    private ActivityResultLauncher<Intent> galleryLauncherResult;
    private ActivityResultLauncher<Intent> cameraLauncherResult;
    StorageManager storageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //     overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);
        setContentView(R.layout.activity_image_view);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        storageUtils = StorageUtils.getInstance(this);
        from = getIntent().getStringExtra(Constants.TAG_FROM);
        storageManager = StorageManager.getInstance(this);
        if (getIntent().hasExtra(Constants.TAG_USER_IMAGE)) {
            imagePath = getIntent().getExtras().getString(Constants.TAG_USER_IMAGE);
        } else {
            imageUri = getIntent().getData();
        }
        findViews();
        initClickListeners();
        initPermissions();
        initActivityResult();

        /*profileImageRequest = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)
                .dontAnimate();*/

        Timber.d("onCreate: %s", imagePath);
        if (from.equals(Constants.TAG_PROFILE)) {
            shimmerLayout.startShimmer();
            initListeners();
        } else {
            bottomLay.setVisibility(View.GONE);
        }

        if (from.equals(Constants.TAG_SENT) || from.equals(Constants.TAG_RECEIVED)
                || from.equals(Constants.TAG_MESSAGE)) {
            Glide.with(getApplicationContext())
                    .load(!TextUtils.isEmpty(imagePath) ? imagePath : imageUri != null ? imageUri : R.drawable.default_profile_image)
                    .error(R.drawable.default_profile_image)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            imageView.setImageDrawable(resource);
                            return false;
                        }
                    })
                    .into(imageView);

        } else {
            Glide.with(getApplicationContext())
                    /*.load(  image)*/
                    .load(imagePath)
                    .error(R.drawable.default_profile_image)
                    .placeholder(R.drawable.default_profile_image)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            imageView.setImageDrawable(resource);
                            return false;
                        }
                    })
                    // .apply(profileImageRequest)
                    .into(imageView);
        }

        SlidrConfig config = new SlidrConfig.Builder()
                .primaryColor(getResources().getColor(R.color.colorBlack))
                .secondaryColor(getResources().getColor(R.color.colorBlack))
                .position(SlidrPosition.TOP)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPickerDialog();
            }
        });

    }

    private void findViews() {
        imageView = findViewById(R.id.imageView);
        lottieImage = findViewById(R.id.lottieImage);
        shimmerLayout = findViewById(R.id.shimmerLayout);
        bottomLay = findViewById(R.id.bottomLay);
        progressBar = findViewById(R.id.progressBar);
        btnClose = findViewById(R.id.btnClose);
        parentLay = findViewById(R.id.parentLay);
        txtChangePhoto = findViewById(R.id.txtChangePhoto);
        btnEdit = findViewById(R.id.btnEdit);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListeners() {
        parentLay.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeUp() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Android 14 or higher
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getApplicationContext(), READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getApplicationContext(), READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {

//                        ActivityCompat.requestPermissions(ImageViewActivity.this,
//                                new String[]{CAMERA, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO},
//                                Constants.CAMERA_REQUEST_CODE);

                        requestCameraPermission();
                    } else {
                        // Permissions are already granted
//                        pickImage();
                        openPickerDialog();
                    }
                } else {
                    // For Android versions lower than 14, use the older permissions model
//                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
//                            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//
//                        ActivityCompat.requestPermissions(this,
//                                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                                REQUEST_CODE_PERMISSIONS);
//                    } else {
//                        // Permissions are already granted
//                        startYourFunctionality();
//                    }


                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(getApplicationContext(),
                                    WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ImageViewActivity.this, new String[]{CAMERA,
                                        WRITE_EXTERNAL_STORAGE},
                                Constants.CAMERA_REQUEST_CODE);
                    } else {
                        openPickerDialog();
                    }
                }
            }
        });
    }

    private void initPermissions() {

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
//                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//                        if (PermissionsUtils.checkStoragePermission(ImageViewActivity.this)) {
//                            takeCameraPictures();
//                        } else {
//                            requestCameraPermissions();
//                        }
//                    } else {
//                        takeCameraPictures();
//                    }
                } else {
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ImageViewActivity.this, x.getKey())) {
                                cameraPermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(ImageViewActivity.this, new OnOkCancelClickListener() {
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
                            break;
                        }
                    }
                }

            }
        });

        storagePermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {
//                    pickGalleryImage();
                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(ImageViewActivity.this, x.getKey())) {
                                storagePermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(ImageViewActivity.this, new OnOkCancelClickListener() {
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
    }
    private void initActivityResult() {
        galleryLauncherResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                         /*   mCurrentPhotoUri = result.getData().getData();
                            compressImage(mCurrentPhotoUri);*/
                            ArrayList<Uri> selectedPhotos = result.getData().getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                            if (selectedPhotos != null && !selectedPhotos.isEmpty()) {
                                Uri selectedImageUri = selectedPhotos.get(0);
                                compressImage(selectedImageUri);
                            }
                        }
                    }
                });

        cameraLauncherResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.d(TAG, "onActivityResult: " + mCurrentPhotoUri.getPath());
                        if (result.getResultCode() == RESULT_OK) {
                            compressImage(mCurrentPhotoUri);
                        }
                    }
                });
    }

    private void compressImage(Uri imageUri) {
        try {
            // Decode the image from the given URI
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            if (originalBitmap != null) {
                // Optional: Resize the bitmap to reduce the resolution (e.g., 50% of original size)
                Bitmap resizedBitmap = resizeBitmap(originalBitmap, 800, 800); // Resize to 800x800 max

                // Compress the bitmap to JPEG format
                String fileName = "TEMP_" + System.currentTimeMillis() + ".jpg";
                File compressedFile = new File(getCacheDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(compressedFile);

                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream); // 80% quality

                outputStream.flush();
                outputStream.close();


                byte[] imageBytes = readFileToByteArray11(compressedFile);


                uploadImage(imageBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Bitmap resizeBitmap(Bitmap originalBitmap, int maxWidth, int maxHeight) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        float aspectRatio = (float) width / height;

        if (width > maxWidth || height > maxHeight) {
            if (aspectRatio > 1) { // Landscape
                width = maxWidth;
                height = (int) (maxWidth / aspectRatio);
            } else { // Portrait
                height = maxHeight;
                width = (int) (maxHeight * aspectRatio);
            }
        }

        return Bitmap.createScaledBitmap(originalBitmap, width, height, true);
    }

    // Helper method to read a file into a byte array
    private byte[] readFileToByteArray11(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        fileInputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
    private void requestCameraPermission() {
        cameraPermissionResult.launch(new String[]{CAMERA, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO});
//
//        ActivityCompat.requestPermissions(ImageViewActivity.this,
//                new String[]{CAMERA, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO},
//                Constants.CAMERA_REQUEST_CODE);
    }


    private void openPickerDialog() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Android 14 or higher
            if (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getApplicationContext(), READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {


//        if (PermissionsUtils.checkStoragePermission(ImageViewActivity.this)) {
//                    if (isNetworkConnected().equals(NOT_CONNECT)) {
//                        networkSnack();
//                    } else if (results.blockedbyme.equals("block")) {
//                        blockChatConfirmDialog("unblock", "sent");
//                    } else {

                requestCameraPermission();

            } else {

                pickImage();

//                FilePickerBuilder.getInstance()
//                        .setMaxCount(1)
//                        .setActivityTheme(R.style.MainTheme)
//                        .setActivityTitle(getString(R.string.please_select_media))
//                        .enableVideoPicker(false)
//                        .enableImagePicker(true)
//                        .enableCameraSupport(true)
//                        .showGifs(false)
//                        .showFolderView(false)
//                        .enableSelectAll(false)
//                        .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
//                        .pickPhoto(this, galleryLauncherResult);
            }
        }
    }
    private void initClickListeners() {
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
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
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (captureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    currentPhotoFile = FileUtil.createImageFile(this);
                    photoFile = currentPhotoFile;
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
                    startActivityForResult(captureIntent, REQUEST_CODE_PICK_IMAGE);
                }
            }
            pickerOptionsSheet.dismiss();
        });
        layoutGallery.setOnClickListener(v -> {
//            Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
//                    ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
//                    : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//            Intent pickIntent = new Intent(Intent.ACTION_PICK);
//            pickIntent.setDataAndType(collection, "image/*");
//            Intent chooserIntent = Intent.createChooser(pickIntent, "Select a picture");
//            if (chooserIntent.resolveActivity(getPackageManager()) != null) {
//                startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE);
//            }

            /*Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
            pickIntent.setDataAndType(collection, "image/jpeg");
            pickIntent.addCategory(Intent.CATEGORY_OPENABLE);
            Intent chooserIntent = Intent.createChooser(pickIntent, "Select a picture");
            if (chooserIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE);
            }*/
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
                    .pickPhoto(this, galleryLauncherResult);
            pickerOptionsSheet.dismiss();
        });

        pickerOptionsSheet.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == 234) {
            try {
                InputStream imageStream = ImagePicker.getInputStreamFromResult(this, requestCode, resultCode, data);
                uploadImage(AppUtils.getBytes(imageStream));
                String filePath = ImagePicker.getImagePathFromResult(getApplicationContext(), requestCode, resultCode, data);
                Glide.with(getApplicationContext())
                        .load(filePath)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                imageView.setImageDrawable(resource);
                                return false;
                            }
                        })
                        //  .apply(profileImageRequest)
                        .into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                boolean isCamera = (data == null
                        || data.getData() == null);
                Uri imageUri = null;
                String fileName = getString(R.string.app_name) + System.currentTimeMillis() + Constants.IMAGE_EXTENSION;
                if (isCamera) {     /** CAMERA **/
                    imageUri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(this,
                            mCurrentPhotoUri), Constants.TAG_TEMP, fileName);

                    //deleteFileForUri(mCurrentPhotoUri);
                    mCurrentPhotoUri = null;
                    Timber.d("Picked: %s fromCamera: %s", imageUri, true);
                } else {            /** ALBUM **/
                    String mimeType = getContentResolver().getType(data.getData());
                    if (mimeType != null && mimeType.startsWith("image")) {

                        imageUri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(this,
                                data.getData()), Constants.TAG_TEMP, fileName);

                        Timber.d("Picked: %s", imageUri);
                    } else {
                        Toasty.info(this
                                , "Videos are not supported yet").show();
                        return;
                    }
                }

                if (imageUri != null) {
                    try {
                        ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(imageUri, "r");

                        InputStream imageStream = new FileInputStream(pfd.getFileDescriptor());
                        uploadImage(AppUtils.getBytes(imageStream));
                        Glide.with(imageView)
                                .load(imageUri)
                                .into(imageView);

                    } catch (Exception e) {
                        Timber.e(e);
                        Toasty.error(this, R.string.something_went_wrong).show();
                    }
                    /**
                     * The image is working fine without a compression.
                     */
                    /*Runnable compressionTask = new ImageCompressionTask(getContext()
                    , imageUri,
                            outUri -> {
                                try {
                                    ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(outUri, "r");
                                    byte[] imageBytes = ByteStreams.toByteArray(new FileInputStream(pfd.getFileDescriptor()));
                                    JSONObject jsonObject = updateDB(Constants.TAG_IMAGE, fileName, fileName, thumbnail, Constants.TAG_SENDING);
                                    uploadChatImage(imageBytes, fileName, thumbnail, jsonObject);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toasty.error(getContext()
                                    , R.string.something_went_wrong).show();
                                }
                            });
                    mExecutorService.execute(compressionTask);*/

                    /*@SuppressLint("StaticFieldLeak")
                    ImageCompression imageComimageCompressionpression = new ImageCompression(ChatActivity.getContext()
                    ) {
                        @Override
                        protected void onPostExecute(String imagePath) {
                            try {
                                JSONObject jsonObject = updateDB(Constants.TAG_IMAGE, fileName, fileName, thumbnail, Constants.TAG_SENDING);
                                uploadChatImage(AppUtils.getBytes(imageStream), fileName, thumbnail, jsonObject);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    };
                    imageCompression.execute(uploadedFile.getPath());*/
                }
            }
        }
    }
    public byte[] readFileToByteArray(File file) throws IOException {
        byte[] byteArray = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(byteArray);
        }
        return byteArray;
    }
    private void uploadImage(byte[] imageBytes) {
        lottieImage.setVisibility(View.GONE);
        txtChangePhoto.setText(R.string.uploading);
        showLoading();
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_PROFILE_IMAGE, "image.jpg", requestFile);
        RequestBody userid = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<Map<String, String>> call3 = apiInterface.uploadProfileImage(body, userid);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                hideLoading();
                resultCode = -1;
                if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                    GetSet.setUserImage(Constants.USER_IMAGE_URL + data.get(Constants.TAG_USER_IMAGE));

                    Timber.d("getUserImage: %s", GetSet.getUserImage());
                    Timber.d("getUserImage data: %s", data.get(Constants.TAG_USER_IMAGE));
                    SharedPref.putString(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
                    txtChangePhoto.setText(R.string.profile_image_updated);
                    new Handler().postDelayed(() -> onBackPressed(), 500);
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                hideLoading();
                txtChangePhoto.setText(R.string.swipe_change_photo);
                lottieImage.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String @NotNull [] permissions, int @NotNull [] grantResults) {
        switch (requestCode) {
            case Constants.CAMERA_REQUEST_CODE:
                boolean permissionGranted = true;
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = false;
                        break;
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    if (PermissionsUtils.checkStoragePermissionEditprofile(ImageViewActivity.this)) {
                        pickImage();
                    }else{
                        App.makeToast(getString(R.string.need_chat_permission_to_access));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivity(i);
                    }
                }else{
                    if (permissionGranted) {
//                    ImagePicker.pickImage(this, "Select your image:");
                        pickImage();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA) &&
                                    shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                                requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                            } else {
                                App.makeToast(getString(R.string.camera_storage_error));
                                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                                startActivity(i);
                            }
                        }
                    }
                }

                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(resultCode);
        super.onBackPressed();
        // overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_down);
        overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
    }


    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, parentLay, isConnected);
    }
}
