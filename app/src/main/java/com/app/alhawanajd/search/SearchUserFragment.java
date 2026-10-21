package com.app.alhawanajd.search;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.alhawanajd.App;
import com.app.alhawanajd.profile.MyProfileActivity;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.alhawanajd.search.adapter.SearchUserAdapter;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.AdapterItemClickListener;
import com.app.model.FollowRequest;
import com.app.model.GetSet;
import com.app.model.ProfileResponse;
import com.app.model.UserList;
import com.app.utils.Constants;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by hitasoft on 26/2/18.
 */

public class SearchUserFragment extends Fragment implements Observer, AdapterItemClickListener {

    private static final String TAG = SearchUserFragment.class.getSimpleName();
    private Context mContext;
    private ApiInterface apiInterface;
    private RecyclerView recyclerView;
    private RelativeLayout nullLay;
    private ImageView nullImage;
    private TextView nullText;
    private List<UserList.Result> usersList = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private SearchUserAdapter itemAdapter;
    private LinearLayoutManager itemManager;
    private int limit = Constants.MAX_LIMIT, offset = 0;
    private boolean isLoading, isScroll = false;
    private int lastVisibleItem, totalItemCount;
    private int visibleThreshold = 1;
    private int selectedPosition = -1;
    private ActivityResultLauncher<Intent> profileResultLauncher;

    public SearchUserFragment() {
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
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        visibleThreshold = Constants.MAX_LIMIT;
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        findViews(view);
        initResultLauncher();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void findViews(View rootView) {
        recyclerView = rootView.findViewById(R.id.recyclerView);
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        nullLay = rootView.findViewById(R.id.nullLay);
        nullImage = rootView.findViewById(R.id.nullImage);
        nullText = rootView.findViewById(R.id.nullText);
    }

    private void initResultLauncher() {
        profileResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
//                Timber.tag(TAG).d("profileResultLauncher: %s", result.getResultCode());
                if (result.getResultCode() == RESULT_OK) {
                    ProfileResponse profile = null;
                    if (selectedPosition != -1) {
                        String jsonData = result.getData().getStringExtra(Constants.TAG_INTENT_DATA);
                        profile = new Gson().fromJson(jsonData, ProfileResponse.class);
                        /*UserList.Result user = new UserList.Result();
                        user.setUserId(profile.getUserId());
                        user.setUserImage(profile.getFollow());
                        user.setUserName(profile.getUserName());
                        user.setName(profile.getName());
                        user.setPrimeMember(Boolean.parseBoolean(profile.getPremiumMember()));
                        user.setFollow(Boolean.parseBoolean(profile.getFollow()));
                        user.setFollowers(profile.getFollowers());
                        user.setFollowings(profile.getFollowings());*/
                        usersList.get(selectedPosition).setFollow(Boolean.parseBoolean(profile.getFollow()));
                        itemAdapter.notifyItemChanged(selectedPosition);
                    }
                }
                selectedPosition = -1;
            }
        });
    }

    private void initView() {
        recyclerView.setHasFixedSize(true);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));

        itemManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(itemManager);

        itemAdapter = new SearchUserAdapter(getActivity(), usersList, this);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setHasFixedSize(true);
        float horizontalMargin = getResources().getDimension(R.dimen.activity_horizontal_margin);
        float verticalMargin = getResources().getDimension(R.dimen.activity_vertical_margin);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildViewHolder(view).getAbsoluteAdapterPosition();
                int itemCount = state.getItemCount();
                outRect.top = (int) verticalMargin;
                if (position == (itemCount - 1)) {
                    outRect.bottom = (int) verticalMargin;
                } else {
                    outRect.bottom = 0;
                }
                outRect.left = (int) horizontalMargin;
                outRect.right = (int) horizontalMargin;
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(() -> swipeRefresh(true));

        setScrollMethod();

        usersList.clear();
        swipeRefresh(true);
    }

    @Override
    public void update(Observable observable, Object o) {
        usersList.clear();
        swipeRefresh(true);
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
                    usersList.add(null);
                    itemAdapter.notifyItemInserted(usersList.size() - 1);
                    offset++;
                    isScroll = true;
                    getUsersList();
                    isLoading = true;
                }
            }
        });
    }

    private void swipeRefresh(final boolean refresh) {
        mSwipeRefreshLayout.setRefreshing(refresh);
        if (refresh) {
            selectedPosition = -1;
            offset = 0;
            totalItemCount = 0;
            lastVisibleItem = 0;
            getUsersList();
        }
    }


    private void getUsersList() {
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
            map.put(Constants.TAG_OFFSET, String.valueOf(limit * offset));
            map.put(Constants.TAG_TYPE, Constants.TAG_SEARCH_USER);
            if (!TextUtils.isEmpty(SearchActivity.searchQuery)) {
                map.put(Constants.TAG_TEXT, SearchActivity.searchQuery);
            }

            Timber.tag(TAG).d("getUsersList: %s", App.getGsonPrettyInstance().toJson(map));
            Call<UserList> call = apiInterface.getSearchUser(map);
            call.enqueue(new Callback<UserList>() {
                @Override
                public void onResponse(@NotNull Call<UserList> call, @NotNull Response<UserList> response) {
                    swipeRefresh(false);
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            if (isScroll) {
                                removeLoadMore();
                            }
                            if (offset == 0) {
                                usersList.clear();
                            }
                            List<UserList.Result> resultList = response.body().getResult();
                            usersList.addAll(resultList);
                            recyclerView.setVisibility(View.VISIBLE);
                            if (resultList.size() > 0 && resultList.size() >= Constants.MAX_LIMIT) {
                                isLoading = false;
                                itemAdapter.notifyDataSetChanged();
                            } else
                                isLoading = true;
                            setNullLay();
                        } else {
                            //Remove bottom loading item
                            if (isScroll) {
                                removeLoadMore();
                            }
                            isLoading = true;
                            setNullLay();
                        }
                    }
                    if (mSwipeRefreshLayout.isRefreshing()) {
                        swipeRefresh(false);
                        isLoading = true;
                    }
                    itemAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(@NotNull Call<UserList> call, @NotNull Throwable t) {
                    t.printStackTrace();
                    swipeRefresh(false);
                    if (offset != 0)
                        offset--;
                    removeLoadMore();
                    call.cancel();
                }
            });
        } else {
            //AppUtils.showSnack(this, findViewById(R.id.parentLay), false);
            swipeRefresh(false);
            nullImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getResources().getText(R.string.no_internet_connection));
        }
    }

    private void setNullLay() {
        if (usersList.size() == 0) {
            nullImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.no_follow));
            nullText.setText(mContext.getString(R.string.no_user));
            nullLay.setVisibility(View.VISIBLE);
        } else {
            nullLay.setVisibility(View.GONE);
        }
    }

    private void removeLoadMore() {
        if (usersList.size() > 0 && usersList.get(usersList.size() - 1) == null) {
            usersList.remove(usersList.size() - 1);
            itemAdapter.notifyItemRemoved(usersList.size());
        }
    }

    @Override
    public void onItemClicked(Object o, int position, String type) {
        if (type.equals(Constants.TAG_FOLLOW)) {
            UserList.Result finalItem = (UserList.Result) o;
            followUnFollowUser(finalItem.getUserId(), finalItem.isFollow());
        } else if (type.equals(Constants.TAG_USERS)) {
            UserList.Result finalItem = (UserList.Result) o;
            if (finalItem.getUserId().equals(GetSet.getUserId())) {
                Intent profile = new Intent(mContext, MyProfileActivity.class);
                profile.putExtra(Constants.TAG_EDIT, true);
                profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityForResult(profile, Constants.PROFILE_OWN_SEARCH_CODE);
            } else {
                selectedPosition = position;
                Intent profile = new Intent(mContext, OthersProfileActivity.class);
                profile.putExtra(Constants.TAG_USER_ID, finalItem.getUserId());
                profile.putExtra(Constants.TAG_INTENT_DATA, new Gson().toJson(finalItem));
                profile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                profileResultLauncher.launch(profile);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // requestCode which we have sent that
        if (resultCode == RESULT_OK && requestCode == Constants.PROFILE_OWN_SEARCH_CODE) {
            swipeRefresh(true);
        }
    }

    private void followUnFollowUser(String otherUserId, boolean follow) {
        if (NetworkReceiver.isConnected()) {
            FollowRequest followRequest = new FollowRequest();
            if (otherUserId != null) followRequest.setUserId(otherUserId);
            followRequest.setFollowerId(GetSet.getUserId());
            if (follow)
                followRequest.setType(Constants.TAG_FOLLOW_USER);
            else
                followRequest.setType(Constants.TAG_UNFOLLOW_USER);

            Call<Map<String, String>> call = apiInterface.followUser(followRequest);
            call.enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(@NotNull Call<Map<String, String>> call, @NotNull Response<Map<String, String>> response) {

                }

                @Override
                public void onFailure
                        (@NotNull Call<Map<String, String>> call, @NotNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }
}


