package com.app.alhawanajd;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.alhawanajd.databinding.ActivityBlockedUsersBinding;
import com.app.alhawanajd.databinding.ItemBlockedUserBinding;
import com.app.alhawanajd.databinding.ItemLoadingBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.app.db.DBHelper;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.model.GetSet;
import com.app.model.ProfileResponse;
import com.app.model.SearchUserResponse;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class BlockedUsersActivity extends BaseFragmentActivity {

    private static final String TAG = "BlockedUsersActivity";

    // 1. Declare the binding object for the activity
    private ActivityBlockedUsersBinding binding;

    private AppUtils appUtils;
    private DBHelper dbHelper;
    private ApiInterface apiInterface;
    public List<ProfileResponse> usersList = new ArrayList<>();
    public BlockedAdapter blockedAdapter;
    private LinearLayoutManager mLayoutManager;
    private final int visibleThreshold = 10;
    private int visibleItemCount;
    private int totalItemCount;
    private int firstVisibleItem;
    private int previousTotal;
    private boolean isLoading = true;
    int currentPage = 0, limit = 20;
    public static boolean hasBlockChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 2. Inflate the layout using View Binding and set the content view
        binding = ActivityBlockedUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // 3. ButterKnife.bind(this) is removed.

        appUtils = new AppUtils(this);
        dbHelper = DBHelper.getInstance(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initView();
    }

    private void initView() {
        // 4. Access views through the binding object
        binding.includeHeader.btnBack.setVisibility(View.VISIBLE);
        if (LocaleManager.isRTL()) {
            binding.includeHeader.btnBack.setScaleX(-1);
        } else {
            binding.includeHeader.btnBack.setScaleX(1);
        }
        binding.includeHeader.txtTitle.setVisibility(View.VISIBLE);
        binding.includeHeader.txtSubTitle.setVisibility(View.GONE);
        binding.nullLay.nullImage.setVisibility(View.GONE);
        binding.nullLay.nullText.setText(R.string.no_user_you_blocked_yet);
        binding.includeHeader.txtTitle.setText(R.string.blocked_list);

        binding.swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorPrimary));

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();

        Slidr.attach(this, config);

        mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        blockedAdapter = new BlockedAdapter(this, usersList);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setAdapter(blockedAdapter);
        blockedAdapter.notifyDataSetChanged();

        // 5. Setup click listener for the back button, replacing @OnClick
        binding.includeHeader.btnBack.setOnClickListener(v -> onBackPressed());

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 0;
            getBlockedList(currentPage);
        });

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull final RecyclerView rv, final int dx, final int dy) {
                visibleItemCount = binding.recyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
                if (dy > 0) {//check for scroll down
                    if (isLoading) {
                        if (totalItemCount > previousTotal) {
                            isLoading = false;
                            previousTotal = totalItemCount;
                            currentPage++;
                        }
                    }

                    if (!isLoading && (totalItemCount - visibleItemCount)
                            <= (firstVisibleItem + visibleThreshold)) {
                        // End has been reached
                        getBlockedList(currentPage);
                        isLoading = true;
                    }
                }
            }
        });

        /*To load first ten items*/
        swipeRefresh(true);
        getBlockedList(currentPage = 0);
    }

    private void getBlockedList(int offset) {
        if (NetworkReceiver.isConnected()) {
            binding.nullLay.getRoot().setVisibility(View.GONE);
            if (!binding.swipeRefreshLayout.isRefreshing()) {
                blockedAdapter.showLoading(true);
            }
            Call<SearchUserResponse> call = apiInterface.getBlockedList(GetSet.getUserId(), offset * 20, limit);
            call.enqueue(new Callback<SearchUserResponse>() {
                @Override
                public void onResponse(@NonNull Call<SearchUserResponse> call, @NonNull Response<SearchUserResponse> response) {
                    if (binding.swipeRefreshLayout.isRefreshing()) {
                        usersList.clear();
                    }
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            binding.nullLay.getRoot().setVisibility(View.GONE);
                            usersList.addAll(response.body().getUserList());
                        }
                    }
                    binding.nullLay.getRoot().setVisibility(usersList.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.recyclerView.setVisibility(usersList.isEmpty() ? View.GONE : View.VISIBLE);

                    if (binding.swipeRefreshLayout.isRefreshing()) {
                        blockedAdapter.showLoading(false);
                        swipeRefresh(false);
                        blockedAdapter.notifyDataSetChanged();
                        isLoading = true;
                    } else {
                        blockedAdapter.showLoading(false);
                        blockedAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<SearchUserResponse> call, @NonNull Throwable t) {
                    call.cancel();
                    t.printStackTrace();
                    if (currentPage != 0)
                        currentPage--;
                }
            });
        } else {
            if (currentPage != 0)
                currentPage--;
            if (!binding.swipeRefreshLayout.isRefreshing()) {
                blockedAdapter.showLoading(false);
            } else {
                if (usersList.isEmpty()) {
                    binding.nullLay.nullText.setVisibility(View.VISIBLE);
                    binding.nullLay.nullText.setText(getString(R.string.no_internet_connection));
                }
                swipeRefresh(false);
            }
        }
    }

    private void swipeRefresh(final boolean refresh) {
        binding.swipeRefreshLayout.setRefreshing(refresh);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasBlockChanges) {
            hasBlockChanges = false;
            swipeRefresh(true);
            getBlockedList(currentPage = 0);
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, binding.parentLay, isConnected);
    }

    // 6. The @OnClick method is removed. Its logic is now in a direct listener in initView().
    /*
    @OnClick(R.id.btnBack)
    public void onViewClicked() {
        onBackPressed();
    }
    */

    public class BlockedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;

        private final List<ProfileResponse> itemList;
        private boolean showLoading = false;

        public BlockedAdapter(Context context, List<ProfileResponse> followersList) {
            this.context = context;
            this.itemList = followersList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            if (viewType == VIEW_TYPE_ITEM) {
                // 7. Use View Binding to inflate the item layout
                ItemBlockedUserBinding itemBinding = ItemBlockedUserBinding.inflate(inflater, parent, false);
                return new MyViewHolder(itemBinding);
            } else { // VIEW_TYPE_FOOTER
                ItemLoadingBinding loadingBinding = ItemLoadingBinding.inflate(inflater, parent, false);
                return new FooterViewHolder(loadingBinding);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int pos) {
            if (holder instanceof MyViewHolder) {
                MyViewHolder myViewHolder = (MyViewHolder) holder;
                final ProfileResponse profile = itemList.get(holder.getAbsoluteAdapterPosition());
                Timber.d("onBindViewHolder: profile=> %s", new Gson().toJson(profile));

                myViewHolder.binding.txtName.setText(profile.getName());
                myViewHolder.binding.txtLocation.setText(profile.getUserName());
                myViewHolder.binding.premiumImage.setVisibility(profile.getPremiumMember().equals(Constants.TAG_TRUE) ?
                        View.VISIBLE : View.GONE);
                Glide.with(context)
                        .load(profile.getUserImage())
                        .apply(new RequestOptions().error(R.drawable.alhawana).placeholder(R.drawable.alhawana)
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(myViewHolder.binding.userImage);

            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                footerHolder.binding.progressBar.setIndeterminate(true);
                footerHolder.binding.progressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            int itemCount = itemList.size();
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

        // 8. The ViewHolder now holds a binding object instead of individual views.
        public class MyViewHolder extends RecyclerView.ViewHolder {
            private final ItemBlockedUserBinding binding;

            public MyViewHolder(ItemBlockedUserBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                // ButterKnife.bind() is removed.

                // 9. Set click listener in the ViewHolder's constructor
                itemView.setOnClickListener(v -> {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Intent intent = new Intent(context, OthersProfileActivity.class);
                        intent.putExtra(Constants.TAG_USER_ID, itemList.get(position).getUserId());
                        intent.putExtra(Constants.TAG_FROM, "block");
                        startActivity(intent);
                    }
                });

                binding.btnUnBlock.setOnClickListener(v -> {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        unBlockUser(itemList.get(position).getUserId(), position);
                    }
                });
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {
            private final ItemLoadingBinding binding;

            public FooterViewHolder(ItemLoadingBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                // ButterKnife.bind() is removed.
            }
        }
    }

    private void unBlockUser(String userId, int position) {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> requestMap = new HashMap<>();
            requestMap.put(Constants.TAG_USER_ID, GetSet.getUserId());
            requestMap.put(Constants.TAG_BLOCK_USER_ID, userId);
            Call<HashMap<String, String>> call = apiInterface.blockUser(requestMap);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(@NonNull Call<HashMap<String, String>> call, @NonNull Response<HashMap<String, String>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body().get(Constants.TAG_STATUS).equals(Constants.TAG_TRUE)) {
                            // User unblocked
                            usersList.remove(position);
                            blockedAdapter.notifyItemRemoved(position);
                            hasBlockChanges = true;
                            // Update UI if list becomes empty
                            if (usersList.isEmpty()) {
                                binding.nullLay.getRoot().setVisibility(View.VISIBLE);
                                binding.recyclerView.setVisibility(View.GONE);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<HashMap<String, String>> call, @NonNull Throwable t) {
                    // Handle failure
                }
            });
        }
    }
}
