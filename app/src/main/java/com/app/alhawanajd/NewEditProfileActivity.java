package com.app.alhawanajd;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.app.alhawanajd.databinding.ActivityNewEditProfileBinding;
import com.app.db.DBHelper;
import com.app.external.ImagePicker;
import com.app.helper.LocaleManager;
import com.app.helper.PermissionsUtils;
import com.app.helper.StorageUtils;
import com.app.helper.callback.OnOkCancelClickListener;
import com.app.model.GetSet;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.FileUtil;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class NewEditProfileActivity extends BaseFragmentActivity {

    private ActivityNewEditProfileBinding binding;
    private File currentPhotoFile;
    private Uri mCurrentPhotoUri;
    private ActivityResultLauncher<String[]> cameraPermissionResult;
    private ActivityResultLauncher<String[]> storagePermissionResult;
    private ActivityResultLauncher<Intent> galleryLauncherResult;
    private ActivityResultLauncher<Intent> cameraLauncherResult;   // <-- NEW
    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int EDIT_FIELD_REQUEST_CODE = 2000; // Use the same as in EditFieldActivity
    private ApiInterface apiInterface;
    Context mContext;
    String TAG = NewEditProfileActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mContext = this;
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView();
        initListeners();
        loadProfileData();
        initPermissions();
        initActivityResult();
        getProfile();
    }

    private void initView() {
        binding.topBar.findViewById(R.id.backArrow).setOnClickListener(v -> onBackPressed());
        binding.topBar.findViewById(R.id.moreOptions).setVisibility(View.VISIBLE);
        if (LocaleManager.isRTL()) {
            binding.topBar.findViewById(R.id.backArrow).setRotation(180);
            binding.nameInfoLayout.findViewById(R.id.nameArrow).setRotation(180);
            binding.nameInfoLayout.findViewById(R.id.userNameArrow).setRotation(180);
            binding.basicInfoLayout.findViewById(R.id.bioArrow).setRotation(180);
            binding.basicInfoLayout.findViewById(R.id.linkArrow).setRotation(180);
        } else {
            binding.topBar.findViewById(R.id.backArrow).setRotation(0);
            binding.nameInfoLayout.findViewById(R.id.nameArrow).setRotation(0);
            binding.nameInfoLayout.findViewById(R.id.userNameArrow).setRotation(0);
            binding.basicInfoLayout.findViewById(R.id.bioArrow).setRotation(0);
            binding.basicInfoLayout.findViewById(R.id.linkArrow).setRotation(0);
        }
    }

    private void initListeners() {
        binding.moreOptions.setOnClickListener(v -> {
            App.preventMultipleClick(binding.moreOptions);
            Intent primeIntent = new Intent(this, SettingsActivity.class);
            primeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(primeIntent);
        });
        // Name click listener
        binding.nameInfoLayout.findViewById(R.id.nameValue).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_NAME);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getName());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });

        binding.nameInfoLayout.findViewById(R.id.nameArrow).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_NAME);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getName());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });

        binding.nameInfoLayout.findViewById(R.id.userNameArrowNew).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("CopiedText", GetSet.getUserName());
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show();
        });

        binding.nameInfoLayout.findViewById(R.id.userNameValue).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_USER_NAME);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getUserName());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });

        binding.nameInfoLayout.findViewById(R.id.userNameArrow).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_USER_NAME);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getUserName());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });

        // Username click listener
        binding.nameInfoLayout.findViewById(R.id.userNameValueNew).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_USER_NAME);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getUserName());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });

        // Bio click listener
        binding.basicInfoLayout.findViewById(R.id.bioValue).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_BIO);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getBio());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });

        binding.basicInfoLayout.findViewById(R.id.bioArrow).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_BIO);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getBio());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });

        // Mobile Number click listener
        /*binding.nameInfoLayout.findViewById(R.id.mobileNumberValue).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_MOBILE_NUMBER);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getGetPhonenumber());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });*/

      /*  binding.nameInfoLayout.findViewById(R.id.emailValue).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_EMAIL_TEXT);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getEmail());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });
*/

        // Gender click listener
        binding.basicInfoLayout.findViewById(R.id.genderValue).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_GENDER);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getGender());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });

        // Date of Birth click listener
      /*  binding.basicInfoLayout.findViewById(R.id.dobValue).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_DOB);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getDob());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });*/

        // PayPal ID click listener
        binding.othersLayout.findViewById(R.id.paypalLabel).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_PAYPAL_ID);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, GetSet.getPaypal_id());
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });


        binding.basicInfoLayout.findViewById(R.id.linkValue).setOnClickListener(v -> {
            App.preventMultipleClick(binding.basicInfoLayout);
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_LINK);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, "link");
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });

        binding.basicInfoLayout.findViewById(R.id.linkArrow).setOnClickListener(v -> {
            App.preventMultipleClick(binding.basicInfoLayout);
            Intent intent = new Intent(this, EditFieldActivity.class);
            intent.putExtra(EditFieldActivity.FIELD_TYPE, Constants.TAG_LINK);
            intent.putExtra(EditFieldActivity.FIELD_VALUE, "link");
            startActivityForResult(intent, EDIT_FIELD_REQUEST_CODE);
        });

        binding.changePhotoText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.preventMultipleClick(binding.changePhotoText);
                openPickerDialog();
            }
        });

    }

    private void loadProfileData() {
        // Load data from GetSet and set to respective TextViews
        ((TextView) binding.topBar.findViewById(R.id.title)).setText(GetSet.getName());
        ((TextView) binding.nameInfoLayout.findViewById(R.id.nameValue)).setText(GetSet.getName());
        ((TextView) binding.nameInfoLayout.findViewById(R.id.userNameValue)).setText(GetSet.getUserName());
        ((TextView) binding.nameInfoLayout.findViewById(R.id.userNameValueNew)).setText(GetSet.getUserName());
        ((TextView) binding.basicInfoLayout.findViewById(R.id.bioValue)).setText(GetSet.getBio());
        ((TextView) binding.nameInfoLayout.findViewById(R.id.mobileNumberValue)).setText(GetSet.getGetPhonenumber());
        ((TextView) binding.basicInfoLayout.findViewById(R.id.genderValue)).setText(GetSet.getGender());
        ((TextView) binding.basicInfoLayout.findViewById(R.id.dobValue)).setText(GetSet.getDob());
        ((TextView) binding.othersLayout.findViewById(R.id.paypalValue)).setText(GetSet.getPaypal_id());
        Glide.with(mContext)
                .load(GetSet.getUserImage())
                .error(R.drawable.default_profile_image)
                .placeholder(R.drawable.default_profile_image)
                .into(binding.profileImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == EDIT_FIELD_REQUEST_CODE && data != null) {
            String fieldType = data.getStringExtra(EditFieldActivity.FIELD_TYPE);
            String updatedValue = data.getStringExtra(EditFieldActivity.FIELD_VALUE);

            if (fieldType != null && updatedValue != null) {
                switch (fieldType) {
                    case Constants.TAG_NAME:
                        ((TextView) binding.nameInfoLayout.findViewById(R.id.nameValue)).setText(updatedValue);
                        break;
                    case Constants.TAG_USER_NAME:
                        ((TextView) binding.nameInfoLayout.findViewById(R.id.userNameValueNew)).setText(updatedValue);
                        ((TextView) binding.nameInfoLayout.findViewById(R.id.userNameValue)).setText(updatedValue);
                        break;
                    case Constants.TAG_BIO:
                        ((TextView) binding.basicInfoLayout.findViewById(R.id.bioValue)).setText(updatedValue);
                        break;
                    case Constants.TAG_MOBILE_NUMBER:
                        ((TextView) binding.nameInfoLayout.findViewById(R.id.mobileNumberValue)).setText(updatedValue);
                        break;
                    case Constants.TAG_GENDER:
                        ((TextView) binding.basicInfoLayout.findViewById(R.id.genderValue)).setText(updatedValue);
                        break;
                    case Constants.TAG_DOB:
                        ((TextView) binding.basicInfoLayout.findViewById(R.id.dobValue)).setText(updatedValue);
                        break;
                    case Constants.TAG_PAYPAL_ID:
                        ((TextView) binding.othersLayout.findViewById(R.id.paypalLabel)).setText(updatedValue);
                        break;
                }
            }
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        // Handle network changes if needed
    }

    private void openPickerDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(mContext, CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(mContext, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(mContext, READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission();
            } else {
                pickImage();
            }
        } else {
            // For Android 12 and below just go to picker (you already handle other perms)
            pickImage();
        }
    }

    private void requestCameraPermission() {
        cameraPermissionResult.launch(new String[]{CAMERA, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO});
    }

    private void pickImage() {

        View contentView = LayoutInflater.from(mContext)
                .inflate(R.layout.bottom_sheet_profile_photo, null);

        BottomSheetDialog pickerOptionsSheet =
                new BottomSheetDialog(NewEditProfileActivity.this, R.style.SimpleBottomDialog);

        pickerOptionsSheet.setCanceledOnTouchOutside(true);
        pickerOptionsSheet.setContentView(contentView);
        pickerOptionsSheet.setDismissWithAnimation(true);

        LinearLayout layoutCamera = contentView.findViewById(R.id.container_camera_option);
        LinearLayout layoutGallery = contentView.findViewById(R.id.container_gallery_option);
        LinearLayout layoutView = contentView.findViewById(R.id.container_photo_view);

        // View current photo
        layoutView.setOnClickListener(v -> {
            App.preventMultipleClick(binding.profileImage);
            Intent intent = new Intent(mContext, ImageViewActivity.class);
            intent.putExtra(Constants.TAG_USER_IMAGE, GetSet.getUserImage());
            intent.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
            startActivityForResult(intent, Constants.INTENT_REQUEST_CODE);
            animateZoom(mContext);
            pickerOptionsSheet.dismiss();
        });

        // Camera
        layoutCamera.setOnClickListener(v -> {
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (captureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                File photoFile = null;
                try {
                    currentPhotoFile = FileUtil.createImageFile(mContext);
                    photoFile = currentPhotoFile;
                } catch (IOException ex) {
                    Timber.e(ex);
                }

                if (photoFile != null) {
                    mCurrentPhotoUri = FileProvider.getUriForFile(
                            mContext,
                            getString(R.string.file_provider_authority),
                            photoFile);

                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri);

                    // ✅ use Activity Result API instead of old startActivityForResult
                    cameraLauncherResult.launch(captureIntent);
                }
            }
            pickerOptionsSheet.dismiss();
        });

        // Gallery via FilePicker
        layoutGallery.setOnClickListener(v -> {
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
                    .pickPhoto(this, galleryLauncherResult);  // <-- this was NULL before

            pickerOptionsSheet.dismiss();
        });

        pickerOptionsSheet.show();
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

                } else {
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(NewEditProfileActivity.this, x.getKey())) {
                                cameraPermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(mContext, new OnOkCancelClickListener() {
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
                            if (ActivityCompat.shouldShowRequestPermissionRationale(NewEditProfileActivity.this, x.getKey())) {
                                storagePermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(mContext, new OnOkCancelClickListener() {
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

        // Handles result from FilePickerBuilder.pickPhoto(...)
        galleryLauncherResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            ArrayList<Uri> selectedPhotos =
                                    result.getData().getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                            if (selectedPhotos != null && !selectedPhotos.isEmpty()) {
                                Uri selectedImageUri = selectedPhotos.get(0);
                                compressImage(selectedImageUri);
                            }
                        }
                    }
                });

        // Handles result from cameraLauncherResult.launch(captureIntent)
        cameraLauncherResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK && mCurrentPhotoUri != null) {
                            compressImage(mCurrentPhotoUri);
                        }
                    }
                });
    }

    private void compressImage(Uri imageUri) {
        try {
            // Decode the image from the given URI
            InputStream inputStream = mContext.getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            if (originalBitmap != null) {
                // Resize to at most 800x800
                Bitmap resizedBitmap = resizeBitmap(originalBitmap, 800, 800);

                // Compress to JPEG (80% quality)
                String fileName = "TEMP_" + System.currentTimeMillis() + ".jpg";
                File compressedFile = new File(mContext.getCacheDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(compressedFile);

                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

                outputStream.flush();
                outputStream.close();

                byte[] imageBytes = readFileToByteArray11(compressedFile);

                // Upload to server
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

    private void uploadImage(byte[] imageBytes) {
        showLoading();

        RequestBody requestFile =
                RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData(Constants.TAG_PROFILE_IMAGE, "image.jpg", requestFile);
        RequestBody userid =
                RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());

        Call<Map<String, String>> call3 = apiInterface.uploadProfileImage(body, userid);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call,
                                   @NotNull Response<Map<String, String>> response) {
                hideLoading();
                Map<String, String> data = response.body();
                Log.e(TAG,"response success : " + new Gson().toJson(response.body()));
                if (data != null &&
                        data.get(Constants.TAG_STATUS) != null &&
                        data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                    // Save new URL in GetSet + SharedPref   (same as ImageViewActivity)
                    GetSet.setUserImage(Constants.USER_IMAGE_URL + data.get(Constants.TAG_USER_IMAGE));
                    SharedPref.putString(Constants.TAG_USER_IMAGE, GetSet.getUserImage());

                    Timber.d("getUserImage: %s", GetSet.getUserImage());

                    // Update the UI profile image
                    if (mContext != null) {
                        Glide.with(mContext)
                                .load(GetSet.getUserImage())
                                .error(R.drawable.default_profile_image)
                                .placeholder(R.drawable.default_profile_image)
                                .into(binding.profileImage);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                call.cancel();
                hideLoading();
                Log.e(TAG,"response success : " + new Gson().toJson(t.getMessage()));
                Timber.e(t, "uploadProfileImage failed");
            }
        });
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
    
    public static void animateZoom(Context context) {
        ((Activity) context).
                overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out);
    }

    private void getProfile() {
        if (GetSet.getUserId() != null && apiInterface != null) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());
            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        ProfileResponse profile = response.body();
                        Log.d("getProfile",new Gson().toJson(profile));
                        try {
                            if (profile.getLoginViaNumber().equals("")) {
                                binding.layMobileNumberShow.setVisibility(View.GONE);
                                binding.layEmail.setVisibility(View.VISIBLE);
                                binding.emailValue.setText(profile.getEmail());
                            } else {
                                binding.layMobileNumberShow.setVisibility(View.VISIBLE);
                                binding.mobileNumberValue.setText(profile.getLoginViaNumber());
                                binding.layEmail.setVisibility(View.GONE);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    Log.e(TAG, "getProfile: " + t.getMessage());
                    call.cancel();
                }
            });
        }
    }

}


