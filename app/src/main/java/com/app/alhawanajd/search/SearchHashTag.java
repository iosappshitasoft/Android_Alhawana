package com.app.alhawanajd.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.app.alhawanajd.R;
import com.app.addon.livestream.external.helper.LocaleManager;
import com.app.external.avloading.AVLoadingIndicatorView;
import com.app.alhawanajd.App;
import com.app.alhawanajd.VideoListActivity;
import com.app.helper.NetworkReceiver;
import com.app.model.GetSet;
import com.app.model.SearchHashTagRes;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.app.utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by hitasoft on 26/2/18.
 */

public class SearchHashTag extends Fragment implements Observer {

    private static final String TAG = SearchHashTag.class.getSimpleName();
    private Context mContext;
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private ArrayList<SearchHashTagRes.Result> hashTagList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private RecyclerViewAdapter itemAdapter;
    private int limit = 10, offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;

    public SearchHashTag() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hashtag, container, false);
        initView(view);
        return view;
    }

    private void initView(View rootView) {
        nullLay = rootView.findViewById(R.id.nullLay);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        nullImage.setImageResource(R.drawable.no_hastage);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        recyclerView.setHasFixedSize(true);
        visibleThreshold = Constants.MAX_LIMIT;

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

        itemAdapter = new RecyclerViewAdapter(getActivity(), hashTagList);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setHasFixedSize(true);

        hashTagList.clear();
        isLoading = true;
        swipeRefresh(true);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh(true);
            }
        });

        setScrollMethod();

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
                if (dy >= 0 && !mSwipeRefreshLayout.isRefreshing() && !isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    hashTagList.add(null);
                    itemAdapter.notifyItemInserted(hashTagList.size() - 1);
                    offset++;
                    isScroll = true;
                    getHashTag();
                    isLoading = true;
                }
            }
        });
    }

    @Override
    public void update(Observable observable, Object o) {
        hashTagList.clear();
        swipeRefresh(true);
    }

    private void swipeRefresh(final boolean refresh) {
        mSwipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            offset = 0;
            totalItemCount = 0;
            lastVisibleItem = 0;
            getHashTag();
        }
    }

    public void getHashTag() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<>();
            if (GetSet.getUserId() == null){
                map.put(Constants.TAG_USER_ID, "");
            }else {
                map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            }

            if (GetSet.getAuthToken() == null){
                map.put(Constants.TAG_TOKEN, "");
            }else {
                map.put(Constants.TAG_TOKEN, GetSet.getAuthToken());
            }

            map.put(Constants.TAG_LIMIT, "" + limit);
            map.put(Constants.TAG_OFFSET, String.valueOf(offset * limit));
            map.put(Constants.TAG_TYPE, Constants.TAG_SEARCH_HASH_TAG);
            if (!TextUtils.isEmpty(SearchActivity.searchQuery)) {
                map.put(Constants.TAG_TEXT, SearchActivity.searchQuery);
            }
            Timber.d("getHashTag params: %s", App.getGsonPrettyInstance().toJson(map));

            if (offset > 0) {
                enableSwipeRefresh(false);
            }
            Call<SearchHashTagRes> call3 = apiInterface.getSearchHash(map);
            call3.enqueue(new Callback<SearchHashTagRes>() {
                @Override
                public void onResponse(@NotNull Call<SearchHashTagRes> call, @NotNull Response<SearchHashTagRes> response) {
                    swipeRefresh(false);
                    if (response.isSuccessful()) {
                        if (offset == 0) {
                            hashTagList.clear();
                            itemAdapter.notifyDataSetChanged();
                        }
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            if (isScroll) {
                                removeLoadMore();
                            }
//                        Timber.d("getHashTag: %s", App.getGsonPrettyInstance().toJson(response.body().getResult()));
                            List<SearchHashTagRes.Result> resultList = response.body().getResult();
                            hashTagList.addAll(resultList);
                            if (resultList.size() > 0) {
                                recyclerView.setVisibility(View.VISIBLE);
                                isLoading = false;
                                itemAdapter.notifyDataSetChanged();
                            } else
                                isLoading = true;
                        } else {
                            //Remove bottom loading item
                            if (isScroll) {
                                removeLoadMore();
                            }
                            isLoading = true;
                        }
                    } else {
                        isLoading = true;
                    }
                    enableSwipeRefresh(true);
                    setNullLay();
                }

                @Override
                public void onFailure(Call<SearchHashTagRes> call, Throwable t) {
                    t.printStackTrace();
                    enableSwipeRefresh(true);
                    swipeRefresh(false);
                    if (offset != 0)
                        offset--;
                    removeLoadMore();
                    call.cancel();
                }
            });
        } else {
            swipeRefresh(false);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            nullImage.setVisibility(View.GONE);
            nullText.setText(getResources().getText(R.string.no_internet_connection));
        }
    }

    private void enableSwipeRefresh(boolean enable) {
        mSwipeRefreshLayout.setEnabled(enable);
    }

    private void setNullLay() {
        if (hashTagList.size() == 0) {
            nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_hastage));
            nullText.setText(mContext.getString(R.string.no_hashtags));
            nullLay.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            nullLay.setVisibility(View.GONE);
        }
    }

    private void removeLoadMore() {
        if (hashTagList.size() > 0 && hashTagList.get(hashTagList.size() - 1) == null) {
            hashTagList.remove(hashTagList.size() - 1);
            itemAdapter.notifyItemRemoved(hashTagList.size());
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        ArrayList<SearchHashTagRes.Result> hashTagList;
        private boolean showLoading = false;
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<SearchHashTagRes.Result> hashTagList) {
            this.hashTagList = hashTagList;
            this.context = context;
        }

        @Override
        public RecyclerView.@NotNull ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hashtag, parent, false);
                return new MyViewHolder(view);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.livza_progress, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.@NotNull ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final SearchHashTagRes.Result tempMap = hashTagList.get(position);
                MyViewHolder viewHolder = (MyViewHolder) holder;
//                viewHolder.title.setText(String.format("#%s", tempMap.getHashtagName()));
                viewHolder.title.setText(tempMap.getHashtagName());
                viewHolder.count.setText(tempMap.getUsedCount());
                if (tempMap.getTrend())
                    viewHolder.trendingHashTag.setVisibility(View.VISIBLE);
                else viewHolder.trendingHashTag.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return hashTagList.get(position) == null ? VIEW_TYPE_FOOTER : VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            return hashTagList == null ? 0 : hashTagList.size();
        }


        public boolean isFooter(int position) {
            return hashTagList.get(position) == null;
        }


        public class LoadingViewHolder extends RecyclerView.ViewHolder {
            public AVLoadingIndicatorView progressBar;
            FrameLayout progressLay;

            public LoadingViewHolder(View view) {
                super(view);
                progressBar = view.findViewById(R.id.progressBarNew);
                progressLay = view.findViewById(R.id.progressLay);
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            MaterialTextView title, trendingHashTag, count;
            MaterialCardView cardMainLay;
            ImageView btnNext;

            public MyViewHolder(View view) {
                super(view);

                title = view.findViewById(R.id.title);
                trendingHashTag = view.findViewById(R.id.trendingHashTag);
                count = view.findViewById(R.id.count);
                cardMainLay = view.findViewById(R.id.cardMainLay);
                btnNext = view.findViewById(R.id.btnNext);

                if (LocaleManager.isRTL()) {
                    btnNext.setRotation(180);
                } else {
                    btnNext.setRotation(0);
                }

                btnNext.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cardMainLay.performClick();
                    }
                });

                cardMainLay.setOnClickListener(v -> {
                    Intent intent = new Intent(context, VideoListActivity.class);
                    intent.putExtra(Constants.TAG_TITLE, hashTagList.get(getAbsoluteAdapterPosition()).getHashtagName());
                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_RELATED_HASHTAG);
                    intent.putExtra(Constants.TAG_TYPE, Constants.TAG_RELATED);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                });
            }
        }
    }
}


