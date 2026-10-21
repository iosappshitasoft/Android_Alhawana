package com.app.alhawanajd.Video;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.alhawanajd.R;
import com.app.external.MediaPlayerUtils;
import com.app.alhawanajd.App;
import com.app.alhawanajd.FavMusicAdapter;
import com.app.alhawanajd.Utility;
import com.app.helper.NetworkReceiver;
import com.app.helper.StorageUtils;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.helper.callback.OnNetworkChangedListener;
import com.app.model.DiscoverSoundResponse;
import com.app.model.GetSet;
import com.app.model.MusicListResponse;
import com.app.model.Request.DiscoverSoundRequest;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.AppUtils;
import com.app.utils.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class DiscoverSoundFragment extends Fragment implements Observer, OnNetworkChangedListener, AdapterItemClickListener {

    private static final String TAG = DiscoverSoundFragment.class.getSimpleName();
    private Context mContext;
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private final ArrayList<MusicListResponse.Result> soundList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private FavMusicAdapter soundAdapter;
    private LinearLayoutManager itemManager;
    private int limit = Constants.MAX_LIMIT, offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;
    AppUtils appUtils;
    File saveaudiopath;
    AsyncTask<String, Void, String> songDownload;

    Boolean onNetworkConnected = true;
    ProgressDialog pdLoading;

    @Override
    public void onNetworkChanged(Boolean connect) {

        Timber.i("DiscoverSoundFragment connect %s", connect);

        onNetworkConnected = connect;
        if (!connect) {
            if (songDownload != null) {
                getSoundId = "";
                songDownload.cancel(false);
                Toasty.error(mContext, R.string.internet_disturb, Toasty.LENGTH_SHORT).show();
                pdLoading.dismiss();
            }
        } else {
            if (nullLay != null && nullLay.getVisibility() == View.VISIBLE)
                swipeRefresh(true);
        }

    }

    private String getSoundId = "";
    private int selectedPosition = -1, lastItemSelectedPos = -1;// no selection by default

    public DiscoverSoundFragment(String SoundId) {
        Timber.d("DiscoverSoundFragment: %s", SoundId);
        getSoundId = SoundId;
        Log.e("check","soundId"+SoundId);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover_sound, container, false);
        appUtils = new AppUtils(getActivity());
        initView(view);
        return view;
    }

    private void initView(View rootView) {
        nullLay = rootView.findViewById(R.id.nullLay);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);
        pdLoading = new ProgressDialog(getContext(), R.style.CameraAlertDialog);

        recyclerView.setHasFixedSize(true);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        visibleThreshold = Constants.MAX_LIMIT;

        itemManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        soundAdapter = new FavMusicAdapter(getActivity(), soundList, this,getSoundId);
        recyclerView.setAdapter(soundAdapter);
        soundAdapter.setBookMarkVisible(View.INVISIBLE);
        soundAdapter.setNextVisible(View.GONE);

        ((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);

        mSwipeRefreshLayout.setOnRefreshListener(() -> swipeRefresh(true));
        saveaudiopath = StorageUtils.getInstance(getContext()).getTempFile(getContext(), Constants.TAG_AUDIO_TRIM + Utility.AUDIO_FORMAT);

        setScrollMethod();
        soundList.clear();
        swipeRefresh(true);
    }

    public void getSound() {
        if (NetworkReceiver.isConnected()) {
            DiscoverSoundRequest request = new DiscoverSoundRequest();
            request.setUserId(GetSet.getUserId());
            request.setType(Constants.TAG_DISCOVER);
            request.setLimit("" + limit);
            request.setOffset("" + (limit * offset));
            if (!TextUtils.isEmpty(UploadSoundActivity.searchQuery)) {
                request.setSearchKey(UploadSoundActivity.searchQuery);
                MediaPlayerUtils.releaseMediaPlayer();
                selectedPosition = -1;
                lastItemSelectedPos = -1;
            }

            Timber.d("getSound: %s", App.getGsonPrettyInstance().toJson(request));
            Call<DiscoverSoundResponse> call3 = apiInterface.getSound(request);
            call3.enqueue(new Callback<DiscoverSoundResponse>() {
                @Override
                public void onResponse(Call<DiscoverSoundResponse> call, Response<DiscoverSoundResponse> response) {
                    swipeRefresh(false);
                    if (response.isSuccessful()) {
                        if (offset == 0) {
                            soundList.clear();
                            soundAdapter.notifyDataSetChanged();
                        }
                        if (response.body().getStatus().equals("true")) {
                            if (isScroll) {
                                removeLoadMore();
                            }
                            List<MusicListResponse.Result> resultList = response.body().getSounds();
                            soundList.addAll(resultList);
                            recyclerView.setVisibility(View.VISIBLE);
                            if (resultList.size() > 0) {
                                isLoading = false;
                                soundAdapter.notifyDataSetChanged();
                            } else
                                isLoading = true;
                        } else {
                            //Remove bottom loading item
                            if (isScroll) {
                                removeLoadMore();
                            }
                            isLoading = true;
                        }
                    }
                    setNullLay();
                }

                @Override
                public void onFailure(Call<DiscoverSoundResponse> call, Throwable t) {
                    t.printStackTrace();
                    swipeRefresh(false);
                    if (offset != 0)
                        offset--;
                    removeLoadMore();
                    call.cancel();
                }
            });
        } else {
            swipeRefresh(false);
            nullLay.setVisibility(View.VISIBLE);
            nullImage.setVisibility(View.GONE);
            nullText.setText(getResources().getText(R.string.no_internet_connection));
        }
    }

    @Override
    public void onItemClicked(Object o, int position, String type) {
        if (type.equals(Constants.TAG_VIEW)) {
            MusicListResponse.Result item = (MusicListResponse.Result) o;
            getSoundId = item.getSoundId();
            songDownload = new SongDownload().execute(item.getSoundUrl(), String.valueOf(saveaudiopath), item.getSoundId(), item.getDuration());
        }
    }

    private void setNullLay() {
        if (soundList.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_videos));
            nullText.setText(mContext.getString(R.string.no_videos_found));
            nullLay.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            nullLay.setVisibility(View.GONE);
        }
    }

    private void removeLoadMore() {
        if (soundList.size() > 0 && soundList.get(soundList.size() - 1) == null) {
            soundList.remove(soundList.size() - 1);
            soundAdapter.notifyItemRemoved(soundList.size());
        }
    }

    @Override
    public void update(Observable o, Object arg) {
//        soundList.clear();
//        soundAdapter.notifyDataSetChanged();
        swipeRefresh(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof UploadSoundActivity) {
            ((UploadSoundActivity) context).compositeOnNetworkChangedListener.register(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (getContext() instanceof UploadSoundActivity) {
            ((UploadSoundActivity) getContext()).compositeOnNetworkChangedListener.unregister(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MediaPlayerUtils.pauseMediaPlayer();
        soundAdapter.notifyDataSetChanged();
    }

    private void setScrollMethod() {
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (dy >= 0 && !isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    soundList.add(null);
                    soundAdapter.notifyItemInserted(soundList.size() - 1);
                    offset++;
                    isScroll = true;
                    getSound();
                    isLoading = true;
                }
            }
        });
    }

    private void swipeRefresh(final boolean refresh) {
        mSwipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            offset = 0;
            totalItemCount = 0;
            lastVisibleItem = 0;
            getSound();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SongDownload extends AsyncTask<String, Void, String> {
        String outputPath = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setCancelable(false);
            ValueAnimator processingAnimator = ValueAnimator.ofInt(0, 4);
            processingAnimator.setRepeatCount(Animation.INFINITE);
            processingAnimator.setDuration(1000L);
            processingAnimator.addUpdateListener(animation -> {
                // noinspection SetTextI18n
                pdLoading.setMessage(getString(R.string.msg_please_wait) + getDots((Integer) processingAnimator.getAnimatedValue(), ""));
            });
            processingAnimator.start();
            pdLoading.setMessage(getResources().getString(R.string.processing));
            pdLoading.setOnShowListener(dialog -> processingAnimator.start());
            pdLoading.setOnDismissListener(dialog -> processingAnimator.cancel());
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL u = new URL(strings[0]);
                URLConnection conn = u.openConnection();
                outputPath = strings[1];

                int contentLength = conn.getContentLength();

                DataInputStream stream = new DataInputStream(u.openStream());

                if (contentLength <= 0) throw new IOException("Nothing received: " + contentLength);

                byte[] buffer = new byte[contentLength];
                stream.readFully(buffer);
                stream.close();

                DataOutputStream fos = new DataOutputStream(new FileOutputStream(strings[1]));
                fos.write(buffer);
                fos.flush();
                fos.close();


                if (isCancelled()) return null;


            } catch (FileNotFoundException e) {
                Timber.d("FileNotFoundException: %s", e.getMessage());
                return null;
            } catch (IOException e) {
                Timber.d("IOException: %s", e.getMessage());
                return null;
            }
            return strings[3];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                Timber.tag(TAG).d("onPostExecute: outputPath %s", outputPath);
                Toasty.success(requireContext(), getResources().getString(R.string.selected_song_sucess), Toasty.LENGTH_SHORT).show();
                pdLoading.dismiss();
                if (getActivity() != null && getActivity() instanceof UploadSoundActivity) {
                    UploadSoundActivity activity = (UploadSoundActivity) getActivity();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.TAG_SOUND_ID, getSoundId);
                    bundle.putString(Constants.TAG_SOUND_DURATION, result);
                    bundle.putString(Constants.TAG_SOUND_URL, outputPath);
                    activity.setAudioData(bundle);
                }
            }
        }
    }

    public static String getDots(int count, String outDots) {
        if (count <= 0) return outDots;
        return getDots(--count, outDots) + ".";
    }


}