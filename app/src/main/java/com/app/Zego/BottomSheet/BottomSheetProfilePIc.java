package com.app.Zego.BottomSheet;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import com.app.addon.livestream.external.utils.ApiClient;
import com.app.addon.livestream.external.utils.Constants;
import com.app.alhawanajd.App;
import com.app.alhawanajd.BuildConfig;
import com.app.alhawanajd.R;
import com.app.alhawanajd.SingleImageActivity;
import com.app.alhawanajd.databinding.BtmSheetPictureBinding;
import com.app.helper.PermissionsUtils;
import com.app.helper.StorageUtils;
import com.app.model.GetSet;
import com.app.utils.AppUtils;
import com.app.utils.FileUtil;
import com.app.utils.api.ApiInterface;
import com.app.utils.api.CommonApi;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class BottomSheetProfilePIc extends BottomSheetDialogFragment {

    public static BottomSheetProfilePIc dialog;
    private static final String TAG = "BottomSheetProfilePIc";
    BtmSheetPictureBinding bind;
    Context context;
    onProfilePicChoosed onProfilePicChoosed;
    private ApiInterface apiInterface;
    private static final int REQUEST_CODE_PICK_IMAGE = 100;
    private static final int REQUEST_CODE_GALLERY = 101;
    StorageUtils storageUtils;
    private File currentPhotoFile;
    private Uri mCurrentPhotoUri;

    public BottomSheetProfilePIc(Context context) {
        this.context = context;

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            onProfilePicChoosed = (onProfilePicChoosed) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ::::::"+context.toString() + " must implement OnDataPassListener" );
        }
    }

    public static BottomSheetProfilePIc getInstance(FragmentManager fragmentManager,Context context) {
        if (dialog==null){
            dialog=new BottomSheetProfilePIc(context);
        }
        dialog.show(fragmentManager,TAG);
        return dialog;
    }

    public BottomSheetProfilePIc() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.reshootBottomDialog);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bind = BtmSheetPictureBinding.inflate(getLayoutInflater());
        View view = bind.getRoot();
        storageUtils = StorageUtils.getInstance(context);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        bind.txtCancel.setOnClickListener(v->dismiss());

        bind.cameraLay.setOnClickListener(v->{
            if (checkPermission()){
                ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA/*,
                                WRITE_EXTERNAL_STORAGE*/},
                        Constants.CAMERA_REQUEST_CODE);
            }else{
                if (isAdded()) {
                    openCamera();
                }
            }
        });
        bind.galleryLay.setOnClickListener(v->{
            if (checkPermission()){
                ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA/*,
                                WRITE_EXTERNAL_STORAGE*/},
                        REQUEST_CODE_GALLERY);
            }else{
                openGallery();
            }
        });

        bind.viewLay.setOnClickListener(v->{
            Intent singleImage = new Intent(context, SingleImageActivity.class);
            singleImage.putExtra("image", String.valueOf(GetSet.getUserImage()));
            startActivity(singleImage);
        });


        return view;
    }

    private void openGallery() {
        //sep1 newly added
        Context ctx = requireContext();

        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(collection, "image/*");
        Intent chooserIntent = Intent.createChooser(pickIntent, "Select a picture");
        if (chooserIntent.resolveActivity(/*context.*/ ctx.getPackageManager()) != null) {
            startActivityForResult(chooserIntent, REQUEST_CODE_PICK_IMAGE);
        }
    }

    private void openCamera() {
        //Aug9 added for crash
        if (!isAdded()) {
            Timber.w("Fragment not attached, cannot open camera");
            return;
        }

        Context ctx = requireContext();

        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureIntent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        if (captureIntent.resolveActivity(ctx.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                currentPhotoFile = FileUtil.createImageFile(ctx);
                photoFile = currentPhotoFile;
            } catch (IOException ex) {
                // Error occurred while creating the File
                Timber.e(ex);
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mCurrentPhotoUri = FileProvider.getUriForFile(ctx,
                        getString(R.string.file_provider_authority),
                        photoFile);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri);
                startActivityForResult(captureIntent, REQUEST_CODE_PICK_IMAGE);
            }
        }
    }

   /* public boolean checkPermission(){
        return  (ActivityCompat.checkSelfPermission(context,
                CAMERA) != PackageManager.PERMISSION_GRANTED *//*||
                ActivityCompat.checkSelfPermission(context,
                        WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED*//*);
    }*/

    public boolean checkPermission() {
        if (!isAdded()) return false;
        return ActivityCompat.checkSelfPermission(requireContext(), CAMERA) != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                boolean isCamera = (data == null
                        || data.getData() == null);
                Uri imageUri = null;
                String fileName = getString(R.string.app_name) + System.currentTimeMillis() + Constants.IMAGE_EXTENSION;
                if (isCamera) {     /** CAMERA **/
                    imageUri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(context,
                            mCurrentPhotoUri), Constants.TAG_TEMP, fileName);

                    //deleteFileForUri(mCurrentPhotoUri);
                    mCurrentPhotoUri = null;
                    Timber.d("Picked: %s fromCamera: %s", imageUri, true);
                } else {            /** ALBUM **/
                try{
                    String mimeType = context.getContentResolver().getType(data.getData());
                    if (mimeType != null && mimeType.startsWith("image")) {

                        imageUri = storageUtils.saveToSDCard(FileUtil.decodeBitmap(context,
                                data.getData()), Constants.TAG_TEMP, fileName);

                        Timber.d("Picked: %s", imageUri);
                    } else {
                        Toasty.info(context
                                , "Videos are not supported yet").show();
                        return;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                 }
                }

                if (imageUri != null) {
                    try {
                        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(imageUri, "r");

                        InputStream imageStream = new FileInputStream(pfd.getFileDescriptor());
                        uploadImage(AppUtils.getBytes(imageStream));
                        if (onProfilePicChoosed != null) {
                            onProfilePicChoosed.onDataPass(imageUri);
//                            dismiss();
                        }
//                        Toast.makeText(context, "image uri", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Timber.e(e);
                        Toasty.error(context, R.string.something_went_wrong).show();
                    }

                }
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String @NotNull [] permissions, int @NotNull [] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_GALLERY:
            case Constants.CAMERA_REQUEST_CODE:
                boolean permissionGranted = true;
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = false;
                        break;
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    if (PermissionsUtils.checkStoragePermissionEditprofile(context)) {

                        if (requestCode==REQUEST_CODE_GALLERY){

                        }else{
                            //Aug8
                            // openCamera();
                            if (isAdded()) {
                                openCamera();
                            }
                        }
                    }else{
                        App.makeToast(getString(R.string.need_chat_permission_to_access));
                        Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        startActivity(i);
                    }
                }else{
                    if (permissionGranted) {
//                    ImagePicker.pickImage(this, "Select your image:");

                        if (requestCode==REQUEST_CODE_GALLERY){

                        }else{
                            //Aug8
                            // openCamera();
                            if (isAdded()) {
                                openCamera();
                            }
                        }

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
    public void onDestroy() {
        super.onDestroy();
        dialog=null;
    }


    public void showLoading() {
        if (isAdded() && getActivity() != null) {
            bind.progressBar.setVisibility(View.VISIBLE);
            requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    public void hideLoading() {
        if (isAdded() && getActivity() != null) {
            bind.progressBar.setVisibility(View.GONE);
            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void uploadImage(byte[] imageBytes) {
        showLoading();
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_PROFILE_IMAGE, "image.jpg", requestFile);
        RequestBody userid = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        Call<Map<String, String>> call3 = apiInterface.uploadProfileImage(body, userid);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                //sep1 newly added
                if (!isAdded()) {
                    Log.w(TAG, "Fragment not attached, ignoring uploadImage response");
                    return;
                }

                if (response.isSuccessful()&&response.body()!=null) {
                    Map<String, String> data = response.body();
                    hideLoading();
                    if (data.get(Constants.TAG_STATUS) != null && data.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                        Log.e(TAG, "onResponse: ::::::::"+new Gson().toJson(response.body()));
                        CommonApi.getProfile(requireActivity(),GetSet.getUserId());
                        new Handler().postDelayed(() -> dismiss(), 500);
                    }
                }

            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                // crash log fixes
                //  hideLoading();
                Toast.makeText(context, "Filed..!!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public interface onProfilePicChoosed{
        void onDataPass(Uri uri);

    }
}
