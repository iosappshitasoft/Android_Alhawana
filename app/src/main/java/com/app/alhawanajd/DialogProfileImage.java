package com.app.alhawanajd;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static com.app.alhawanajd.App.hideKeyboardFragment;
import static com.app.thread.ImageCompressionTask.calculateInSampleSize;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.app.helper.OkayCancelCallback;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.app.alhawanajd.BuildConfig;
import com.app.alhawanajd.R;
import com.app.external.ImagePicker;
import com.app.helper.PermissionsUtils;
import com.app.helper.StorageUtils;
import com.app.helper.callback.OnOkClickListener;
import com.app.model.GetSet;
import com.app.utils.Constants;
import com.app.utils.FileUtil;
import com.app.utils.SharedPref;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import es.dmoral.toasty.Toasty;
import timber.log.Timber;

public class DialogProfileImage extends DialogFragment implements View.OnClickListener {

    private static final int REQUEST_CODE_PICK_IMAGE = 100;

    private LinearLayout profileLayout;
    TextView txtTitle;
    ImageView profileImage;
    ImageView uploadImage;
    Button btnFinish;
    ProgressBar progressBar;
    RelativeLayout headerLay;
    EditText filledTextField;

    private OnOkClickListener callBack;
    private Context context;
    InputStream imageStream;

    private File currentPhotoFile;
    private Uri mCurrentPhotoUri, uploadUri;
    StorageUtils storageUtils;
    public static boolean usecamera = false;

    private ActivityResultLauncher<Intent> cameraLauncherResult;
    private ActivityResultLauncher<Intent> galleryLauncherResult;
    private ActivityResultLauncher<String[]> cameraResultLauncher;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorBlack)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.dialog_profile_image, container, false);
        storageUtils = StorageUtils.getInstance(getContext());
        findViews(itemView);
        initActivityResult();
        initView();
        return itemView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar.setIndeterminate(true);
        if (SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null) != null) {
            Glide.with(context)
                    .load(SharedPref.getString(SharedPref.FACEBOOK_IMAGE, null))
                    .apply(RequestOptions.circleCropTransform())
                    .apply(App.getProfileImageRequest())
                    .into(profileImage);
        }

        filledTextField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) filledTextField.setHint("");
        });
    }

    private void findViews(View itemView) {
        profileLayout = itemView.findViewById(R.id.profileLayout);
        txtTitle = itemView.findViewById(R.id.txtTitle);
        profileImage = itemView.findViewById(R.id.profileImage);
        uploadImage = itemView.findViewById(R.id.uploadImage);
        btnFinish = itemView.findViewById(R.id.btnFinish);
        progressBar = itemView.findViewById(R.id.progressBar);
        headerLay = itemView.findViewById(R.id.headerLay);
        filledTextField = itemView.findViewById(R.id.filledTextField);

        filledTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() >=50){
                    Toast.makeText(context, getString(R.string.reachlimt), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initActivityResult() {
       /* galleryLauncherResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            mCurrentPhotoUri = result.getData().getData();
                            *//** ALBUM **//*
                            String fileName = getString(R.string.app_name) + System.currentTimeMillis() + Constants.IMAGE_EXTENSION;

                            String mimeType = requireContext().getContentResolver().getType(mCurrentPhotoUri);
                            if (mimeType != null && mimeType.startsWith("image")) {
                                uploadUri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(context,
                                        mCurrentPhotoUri), Constants.TAG_IMAGE, fileName);
                                Glide.with(context)
                                        .load(uploadUri)
                                        .into(profileImage);
                                profileImage.setVisibility(View.VISIBLE);
                                try {
                                    imageStream = context.getContentResolver().openInputStream(uploadUri);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toasty.info(requireContext()
                                        , "Videos are not supported yet").show();
                                return;
                            }

                        }
                    }
                });*/

        galleryLauncherResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE){

                                ArrayList<Uri> selectedPhotos = result.getData().getParcelableArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
                                Log.d("TAG", "onActivityResult:222 "+selectedPhotos);
                                if (selectedPhotos != null && !selectedPhotos.isEmpty()) {
                                    Log.d("TAG", "onActivityResult1: "+new Gson().toJson(selectedPhotos));
                                    Uri selectedImageUri = selectedPhotos.get(0);
                                    Log.d("TAG", "onActivityResult33333333: "+selectedImageUri);
                                    if (selectedImageUri != null) {
//                                        handleImageSelection(selectedImageUri);
                                        mCurrentPhotoUri = selectedImageUri;
                                        Glide.with(context)
                                                .load(mCurrentPhotoUri)
                                                .into(profileImage);
                                        profileImage.setVisibility(View.VISIBLE);
                                        try {
                                            imageStream = context.getContentResolver().openInputStream(selectedImageUri);
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(context, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            else{
                                mCurrentPhotoUri = result.getData().getData();
                                /** ALBUM **/
                                String fileName = getString(R.string.app_name) + System.currentTimeMillis() + Constants.IMAGE_EXTENSION;

                                Log.d("TAG", "onActivityResult: "+mCurrentPhotoUri);
                                String mimeType = requireContext().getContentResolver().getType(mCurrentPhotoUri);
                                if (mimeType != null && mimeType.startsWith("image")) {
                                    uploadUri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(context,
                                            mCurrentPhotoUri), Constants.TAG_IMAGE, fileName);
                                    Glide.with(context)
                                            .load(uploadUri)
                                            .into(profileImage);
                                    profileImage.setVisibility(View.VISIBLE);
                                    try {
                                        imageStream = context.getContentResolver().openInputStream(uploadUri);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toasty.info(requireContext()
                                            , "Videos are not supported yet").show();
                                    return;
                                }

                            }



                        }
                    }
                });

        cameraLauncherResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            String fileName = getString(R.string.app_name) + System.currentTimeMillis() + Constants.IMAGE_EXTENSION;
                            /** CAMERA **/
                            uploadUri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(requireContext(),
                                    mCurrentPhotoUri), Constants.TAG_IMAGE, fileName);


                            mCurrentPhotoUri = null;
                            if (uploadUri != null) {
                                Bitmap bmp = FileUtil.decodeBitmap(context, uploadUri);
                                ExifInterface exif;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    try (InputStream inputStream = context.getContentResolver().openInputStream(uploadUri)) {
                                        exif = new ExifInterface(inputStream);
                                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                                        Matrix matrix = new Matrix();
                                        if (orientation == 6) {
                                            matrix.postRotate(90);
                                        } else if (orientation == 3) {
                                            matrix.postRotate(180);
                                        } else if (orientation == 8) {
                                            matrix.postRotate(270);
                                        }
                                        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (bmp != null) {
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    imageStream = new ByteArrayInputStream(stream.toByteArray());
                                    uploadImage.setVisibility(View.GONE);
                                    profileImage.setVisibility(View.VISIBLE);
                                    profileImage.setImageBitmap(bmp);
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
                });

        cameraResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d("TAG", "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {
                   /* Intent in = new Intent(EditProfile.this, AlbumSelectActivity.class);
                    in.putExtra("from","profile");
                    in.putExtra("fromEditProfile", true);
                    startActivityForResult(in, REQUEST_CODE);*/

                } else {
                    boolean neverAsked = false;
                    Log.d("TAG", "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), x.getKey())) {
                                cameraResultLauncher.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(context, new OkayCancelCallback() {
                                    @Override
                                    public void onOkayClicked(Object o) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, DialogProfileImage.this.getString(R.string.camera_storage_error));
                            }
                            break;
                        }
                    }
                }

            }
        });
    }

    private void initView() {
        profileLayout.setOnClickListener(this);
        btnFinish.setOnClickListener(this);
    }


    public void setCallBack(OnOkClickListener callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profileLayout:
                if (!PermissionsUtils.checkCameraPermission(context)) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        requestPermissions(PermissionsUtils.CAMERA_IMAGE_STORAGE_PERMISSION_V14, Constants.CAMERA_REQUEST_CODE);

                    }
                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissions(PermissionsUtils.CAMERA_IMAGE_STORAGE_PERMISSION_V13, Constants.CAMERA_REQUEST_CODE);
                    }
                     else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requestPermissions(PermissionsUtils.CAMERA_STORAGE_PERMISSION_V10, Constants.CAMERA_REQUEST_CODE);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(PermissionsUtils.CAMERA_STORAGE_PERMISSION, Constants.CAMERA_REQUEST_CODE);
                    }else{
                        pickImage();
                    }
                } else {
                    pickImage();
                }
                break;
            case R.id.btnFinish:
                hideKeyboardFragment(requireContext(), requireView());
                App.preventMultipleClick(btnFinish);
                callBack.onOkClicked(imageStream, filledTextField.getText().toString().trim());
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 && requestCode == 234) {
            try {
                uploadImage.setVisibility(View.GONE);
                profileImage.setVisibility(View.VISIBLE);

                imageStream = ImagePicker.getInputStreamFromResult(context, requestCode, resultCode, data);
                String filePath = ImagePicker.getImagePathFromResult(context, requestCode, resultCode, data);

                GetSet.setUserImage(filePath);
                Glide.with(context)
                        .load(filePath)
                        .apply(App.getProfileImageRequest())
                        .into(profileImage);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 500;
        int MAX_WIDTH = 500;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private void pickImage() {

        View contentView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_pick_options, requireView().findViewById(R.id.parentLay), false);
        BottomSheetDialog pickerOptionsSheet = new BottomSheetDialog(requireContext(), R.style.SimpleBottomDialog);
        pickerOptionsSheet.setCanceledOnTouchOutside(true);
        pickerOptionsSheet.setContentView(contentView);
        pickerOptionsSheet.setDismissWithAnimation(true);

        View layoutCamera = contentView.findViewById(R.id.container_camera_option);
        View layoutGallery = contentView.findViewById(R.id.container_gallery_option);

        layoutCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(context,CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                cameraResultLauncher.launch(new String[]{CAMERA});
            }
            else{
                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                if (captureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        currentPhotoFile = FileUtil.createImageFile(requireContext());
                        photoFile = currentPhotoFile;
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Timber.e(ex);
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCurrentPhotoUri = FileProvider.getUriForFile(requireContext(),
                                getString(R.string.file_provider_authority),
                                photoFile);
                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri);
                        cameraLauncherResult.launch(captureIntent);
                    }
                }
                pickerOptionsSheet.dismiss();
            }

        });
        layoutGallery.setOnClickListener(v -> {

            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                FilePickerBuilder.getInstance()
                        .setMaxCount(1)
//                      .setActivityTheme(R.style.AppTheme)
//                      .setActivityTitle(getString(R.string.please_select_media))
                        .enableVideoPicker(false)
                        .enableImagePicker(true)
                        .enableCameraSupport(false)
                        .showGifs(false)
                        .showFolderView(false)
                        .enableSelectAll(false)
                        .withOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .pickPhoto(DialogProfileImage.this, galleryLauncherResult);
//                openGallery();
                pickerOptionsSheet.dismiss();
            }
            else{
                Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                        : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                Intent pickIntent = new Intent(Intent.ACTION_PICK, collection);
                pickIntent.setDataAndType(collection, "image/*");
                String[] mimeTypes = {"image/jpeg", "image/png"};
                pickIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                Intent chooserIntent = Intent.createChooser(pickIntent, "Select a picture");

                if (chooserIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    galleryLauncherResult.launch(chooserIntent);
                }
                pickerOptionsSheet.dismiss();}
        });

        pickerOptionsSheet.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String @NotNull [] permissions, int @NotNull [] grantResults) {
        if (requestCode == Constants.CAMERA_REQUEST_CODE) {
            boolean permissionGranted = true;
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = false;
                    break;
                }
            }
            if (permissionGranted) {
                //ImagePicker.pickImage(getContext()
                // , "Select your image:");
                pickImage();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    if (/*shouldShowRequestPermissionRationale(CAMERA) &&*/
                            shouldShowRequestPermissionRationale(READ_MEDIA_IMAGES)) {
                        requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                    } else {
                        App.makeToast(context.getString(R.string.need_chat_permission_to_access));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivity(i);
                    }
                }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(READ_MEDIA_IMAGES)) {
                        requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                    } else {
                        App.makeToast(context.getString(R.string.need_chat_permission_to_access));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivity(i);
                    }
                }
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                        requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                    } else {
                        App.makeToast(context.getString(R.string.camera_storage_error));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivity(i);
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                        requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE);
                    } else {
                        App.makeToast(context.getString(R.string.camera_storage_error));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivity(i);
                    }
                }
            }
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        if (getActivity() != null)
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        btnFinish.setEnabled(false);
    }

    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnFinish.setEnabled(true);
        if (getActivity() != null)
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}
