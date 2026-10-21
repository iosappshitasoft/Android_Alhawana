package com.app.alhawanajd.Video;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.audiotrimmer.ActivityEditor;
import com.app.alhawanajd.BaseFragmentActivity;
import com.app.alhawanajd.PrimeActivity;
import com.app.helper.FragmentObserver;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.CompositeOnNetworkChangedListener;
import com.app.model.GetSet;
import com.app.utils.AdminData;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class UploadSoundActivity extends BaseFragmentActivity {

    public static String searchQuery;

    ViewPager viewPager;
    ImageView btnBack;
    EditText edtSearch;
    CoordinatorLayout contentLay;
    ViewPagerAdapter adapter;
    private SlidrInterface slidrInterface;

    TextView btnFav, btnDiscover;
    View indicatorDiscover,indicatorFav;
    Button uploadButton;
    private String getSoundId = null, isUerPrime = "";
    private ApiInterface apiInterface;
    Bitmap videoThumbnail;
    ProgressDialog pdLoading;
    CompositeOnNetworkChangedListener compositeOnNetworkChangedListener = new CompositeOnNetworkChangedListener();
    private ActivityResultLauncher<Intent> audioSelectLauncher;
    private Handler handler = new Handler(Looper.myLooper());
    private long lastEdited;
    private Runnable inputFinishChecker = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() > lastEdited + Constants.TYPING_DELAY - 500) {
                searchQuery = edtSearch.getText().toString().trim();
                updateFragments();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackground(new ColorDrawable(getResources().getColor(R.color.colorBlack)));
        setContentView(R.layout.activity_upload_sound);

        overridePendingTransition(R.anim.anim_slide_up, R.anim.anim_stay);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        isUerPrime = SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false");
        if (getIntent().hasExtra(Constants.TAG_SOUND_ID)) {
            getSoundId = getIntent().getStringExtra(Constants.TAG_SOUND_ID);
        }

        initActivityResultLauncher();
        animation();

        searchQuery = "";

        viewPager = findViewById(R.id.fragment_home_viewpager);
        contentLay = findViewById(R.id.contentLay);
        edtSearch = findViewById(R.id.edtSearch);
        btnBack = findViewById(R.id.btnBack);
        btnFav = findViewById(R.id.btnFav);
        btnDiscover = findViewById(R.id.btnDiscover);
        indicatorDiscover = findViewById(R.id.indicatorDiscover);
        indicatorFav = findViewById(R.id.indicatorFav);

        uploadButton = findViewById(R.id.uplaodButton);

        if (LocaleManager.isRTL()) {
            btnBack.setScaleX(-1);
        } else {
            btnBack.setScaleX(1);
        }

        setupViewPager(viewPager);
        viewPager.setOffscreenPageLimit(1);

        btnDiscover.setOnClickListener(view -> {
            indicatorDiscover.setVisibility(View.VISIBLE);
            indicatorFav.setVisibility(View.GONE);
            enableSliding(true);
            viewPager.setCurrentItem(0);
        });
        btnFav.setOnClickListener(view -> {
            indicatorDiscover.setVisibility(View.GONE);
            indicatorFav.setVisibility(View.VISIBLE);
            enableSliding(false);
            viewPager.setCurrentItem(1);
        });

        /*uploadButton.setOnClickListener(v -> {
            if (AdminData.uploadSound) {
                launchAudioIntent();
            } else if (SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false").equals("true")) {
                launchAudioIntent();
            } else {
                Intent intent = new Intent(this, PrimeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                Toasty.error(UploadSoundActivity.this, R.string.prime_alert, Toasty.LENGTH_SHORT).show();
            }

        });*/

        uploadButton.setOnClickListener(v -> {
            if (SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false").equals("false") && !AdminData.uploadSound) {
                launchAudioIntent();
            }
            else {
                Intent intent = new Intent(this, PrimeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                Toasty.error(UploadSoundActivity.this, R.string.prime_alert, Toasty.LENGTH_SHORT).show();
            }

        });

        btnBack.setOnClickListener(v -> backPressed());

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.removeCallbacks(inputFinishChecker);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                lastEdited = System.currentTimeMillis();
                handler.postDelayed(inputFinishChecker, Constants.TYPING_DELAY);
            }
        });

        viewPager.setCurrentItem(0);
        btnDiscover.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnFav.setTextColor(getResources().getColor(R.color.bottomsheet_viewline));
    }

    private void launchAudioIntent() {
        if (NetworkReceiver.isConnected()) {
            audioSelectLauncher.launch(new Intent(UploadSoundActivity.this, ActivityEditor.class));
        } else {
            Toasty.error(this, getResources().getString(R.string.no_internet_connection), Toasty.LENGTH_SHORT).show();
        }
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

    private void initActivityResultLauncher() {
        audioSelectLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        Intent data = result.getData();
                        showProgressLoading();
//                    outputPath = StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_AUDIO_TRIM + Utility.AUDIO_FORMAT);
                        Timber.i("song title: %s", data.getStringExtra(Constants.TAG_SONG_TITLE));
                        Timber.i("song duration: %s", data.getStringExtra(Constants.TAG_SONG_DURATION));
                        Timber.i("TAG_SONG_ALBUM: %s", data.getStringExtra(Constants.TAG_SONG_ALBUM));

                        if (data.getStringExtra(Constants.TAG_SONG_ALBUM) != null)
                            videoThumbnail = getAlbumImage(data.getStringExtra(Constants.TAG_SONG_ALBUM));
                        String duration = data.getStringExtra(Constants.TAG_SONG_DURATION);
                        String outputPath = data.getStringExtra(Constants.TAG_SOUND_URL);
                        hideProgressLoading();
                        Intent intent = new Intent();
                        intent.putExtra(Constants.TAG_SOUND_URL, outputPath);
                        intent.putExtra(Constants.TAG_SOUND_DURATION, duration);
                        //intent.putExtra(Constants.TAG_SONG_ALBUM, videoThumbnail);
                        intent.putExtra(Constants.TAG_SONG_TITLE, data.getStringExtra(Constants.TAG_SONG_TITLE));
                        Log.d("TAG", "upload_songcheck: "+new Gson().toJson(intent));
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                /*
                showProgressLoading();
                *//*val warningSnackBar = Snacky.builder()
                        .setActivty(this)
                        .setBackgroundColor(ContextCompat.getColor(this, R.color.editor_toast_color))
                        .setText(this.getString(R.string.Edit_Done_Toast))
                        .setDuration(Snacky.LENGTH_LONG);

                warningSnackBar.success().show();*//*

                if (data != null) {
//                    outputPath = StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_AUDIO_TRIM + Utility.AUDIO_FORMAT);
                    Timber.i("song title %s", data.getStringExtra(Constants.TAG_SONG_TITLE));
                    Timber.i("song duration %s", data.getStringExtra(Constants.TAG_SONG_DURATION));
                    Timber.i("TAG_SONG_ALBUM %s", data.getStringExtra(Constants.TAG_SONG_ALBUM));

                    if (data.getStringExtra(Constants.TAG_SONG_ALBUM) != null)
                        videoThumnail = getAlbumImage(data.getStringExtra(Constants.TAG_SONG_ALBUM));

                    outputPath = StorageUtils.getInstance(this).getTempFile(this, Constants.TAG_AUDIO_TRIM + Utility.AUDIO_FORMAT);

                    ProgressRequestBody.UploadCallbacks uploadCallbacks =
                            new ProgressRequestBody.UploadCallbacks() {
                                @Override
                                public void onProgressUpdate(int percentage) {

                                }

                                @Override
                                public void onError(String message) {
                                    Timber.d("onError: %s", message);
                                }

                                @Override
                                public void onFinish() {


                                }
                            };


                    Random r = new Random();


                    ProgressRequestBody fileBody = new ProgressRequestBody(outputPath, "audio.mp3",
                            uploadCallbacks);

                    MultipartBody.Part filePart =
                            MultipartBody.Part.createFormData("sound_data", data.getStringExtra(Constants.TAG_SONG_TITLE) + ".mp3", fileBody);

                    RequestBody userId = RequestBody.create(MediaType.parse("text/plain"),
                            GetSet.getUserId());
                    RequestBody token = RequestBody.create(MediaType.parse("text/plain"),
                            GetSet.getAuthToken());

                    RequestBody title = RequestBody.create(MediaType.parse("text/plain"),
                            data.getStringExtra(Constants.TAG_SONG_TITLE) + +Math.abs(r.nextInt()));

                    RequestBody duration = RequestBody.create(MediaType.parse("text/plain"),
                            data.getStringExtra(Constants.TAG_SONG_DURATION));


                    Call<Map<String, String>> call = apiInterface.uploadAudio(filePart, userId, token, title, duration);

                    call.enqueue(new Callback<Map<String, String>>() {
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {

                            Timber.d("onResponse:status  %s", response.body());

                            Map<String, String> getRes = response.body();

                            if (getRes.get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {

                                //  App.makeToast("Songs uploaded");

                                if (videoThumnail != null) {
                                    pdLoading.setMessage(getResources().getString(R.string.almost_done));

                                    uploadImage(getRes.get("sound_id"), videoThumnail, data.getStringExtra(Constants.TAG_SONG_DURATION));
                                } else {
                                    pdLoading.dismiss();
                                    Intent intent = new Intent();
                                    intent.putExtra(Constants.TAG_SOUND_ID, getRes.get("sound_id"));
                                    intent.putExtra(Constants.TAG_SOUND_DURATION, data.getStringExtra(Constants.TAG_SONG_DURATION));
                                    setResult(Constants.CAMERA_SOUND, intent);
                                    finish();

                                }

                            *//*if (!getTitle.equals("notitle"))
                                uploadImage(getRes.get("sound_id"));
                            else {

                                Intent intent = new Intent();
                                intent.putExtra(Constants.TAG_SOUND_ID, getRes.get("sound_id"));
                                setResult(Constants.TRIMMER_REQUEST_CODE);
                                finish();
                            }*//*


                            } else {
                                //  mProgressDialog.dismiss();
                                App.makeToast("Songs already uploaded");
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {

                            Timber.d("onResponse: %s", t.getMessage());

                        }
                    });
                }

            */
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    //  Toast.makeText(UploadSoundActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        backPressed();
    }

    public void backPressed() {
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_down);
        setResult(RESULT_CANCELED);
        finish();
    }


    @Override
    public void onNetworkChanged(boolean isConnected) {

        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
        Timber.i("onNetworkChanged %s", isConnected);

        compositeOnNetworkChangedListener.onNetworkChanged(isConnected);

    }


    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DiscoverSoundFragment(getSoundId), getString(R.string.broadcast));
        adapter.addFragment(new DiscoverFavSoundFragment(getSoundId), getString(R.string.people));

//        viewPager.setPageTransformer(true, new FlipHorizontalPageTransformer());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 0:
                    default:
                        uploadButton.setVisibility(View.VISIBLE);
                        btnDiscover.setTextColor(getResources().getColor(R.color.colorPrimary));
                        btnFav.setTextColor(getResources().getColor(R.color.bottomsheet_viewline));
                        edtSearch.setHint(getString(R.string.search_sounds));
                        enableSliding(true);
                        break;
                    case 1:
                        uploadButton.setVisibility(View.VISIBLE);
                        btnDiscover.setTextColor(getResources().getColor(R.color.bottomsheet_viewline));
                        btnFav.setTextColor(getResources().getColor(R.color.colorPrimary));
                        edtSearch.setHint(getString(R.string.search_fav));
                        enableSliding(false);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void updateFragments() {
        adapter.updateFragments();
    }

    public void enableSliding(boolean enable) {
        if (enable)
            slidrInterface.unlock();
        else
            slidrInterface.lock();
    }

    public void setAudioData(Bundle bundle) {
        Intent intent = new Intent();
        intent.putExtra(Constants.TAG_SOUND_ID, bundle.getString(Constants.TAG_SOUND_ID));
        intent.putExtra(Constants.TAG_SOUND_URL, bundle.getString(Constants.TAG_SOUND_URL));
        intent.putExtra(Constants.TAG_SOUND_DURATION, bundle.getString(Constants.TAG_SOUND_DURATION));
        setResult(RESULT_OK, intent);
        finish();
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private Observable mObservers = new FragmentObserver();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragmentList.get(position) instanceof Observer)
                mObservers.addObserver((Observer) mFragmentList.get(position));
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public void updateFragments() {
            mObservers.notifyObservers();
        }
    }


    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String getDots(int count, String outDots) {
        if (count <= 0) return outDots;
        return getDots(--count, outDots) + ".";
    }

    private void showProgressLoading() {
        pdLoading = new ProgressDialog(UploadSoundActivity.this, R.style.CameraAlertDialog);
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
        pdLoading.show();
    }

    private void hideProgressLoading() {
        if (pdLoading != null && pdLoading.isShowing()) {
            pdLoading.dismiss();
        }
    }

    public void uploadImage(String getSoundId, Bitmap videoThumnail, String getSoundDuration) {


        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        videoThumnail.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] imageBytes = stream.toByteArray();

        RequestBody requestFile = RequestBody.create(imageBytes, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData(Constants.TAG_COVER_IMAGE, "cover_image" + ".jpeg", requestFile);
        RequestBody publisherId = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
        RequestBody soundId = RequestBody.create(getSoundId, MediaType.parse("multipart/form-data"));

        Call<Map<String, String>> call3 = apiInterface.uploadAlbumImage(body, publisherId, soundId);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                Timber.d("onResponse: %s", response.body());

                pdLoading.dismiss();
                Intent intent = new Intent();
                intent.putExtra(Constants.TAG_SOUND_ID, getSoundId);
                intent.putExtra(Constants.TAG_SOUND_DURATION, getSoundDuration);
                setResult(Constants.CAMERA_SOUND, intent);
                finish();

            }

            @Override
            public void onFailure(@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                Timber.d("onResponse: %s", t.getMessage());
                call.cancel();
            }
        });


    }


    private Bitmap getAlbumImage(String path) {
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, Uri.parse(path));
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) return BitmapFactory.decodeByteArray(data, 0, data.length);
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!NetworkReceiver.isConnected()) {
            AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
        }
    }
}