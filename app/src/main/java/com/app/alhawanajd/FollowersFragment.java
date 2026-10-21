package com.app.alhawanajd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.app.helper.BlurUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.card.MaterialCardView;
import com.app.alhawanajd.R;
import com.app.alhawanajd.profile.MyProfileActivity;
import com.app.alhawanajd.profile.OthersProfileActivity;
import com.app.helper.NetworkReceiver;
import com.app.model.FollowersResponse;
import com.app.model.GetSet;
import com.app.utils.Constants;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowersFragment extends Fragment {

    private static final String TAG = FollowersFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView txtError;

    private Context context;
    private ApiInterface apiInterface;
    int currentPage = 0, limit = 20;
    private final List<FollowersResponse.FollowersList> followersList = new ArrayList<>();
    private FollowersAdapter adapter;
    private final int visibleThreshold = 10;
    private int visibleItemCount;
    private int totalItemCount;
    private int firstVisibleItem;
    private int previousTotal;
    private GridLayoutManager mLayoutManager;
    String partnerId;
    private boolean isLoading = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_followers, container, false);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        partnerId = getArguments().getString(Constants.TAG_PARTNER_ID);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        initView();
    }

    private void findViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        txtError = view.findViewById(R.id.txtError);
    }

    private void initView() {

        swipeRefreshLayout.setBackgroundColor(getResources().getColor(R.color.colorBlack));
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        adapter = new FollowersAdapter(getActivity(), followersList);
        mLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isPositionFooter(position) ? mLayoutManager.getSpanCount() : 1;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = 0;
                getFollowersList(currentPage);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(final RecyclerView rv, final int dx, final int dy) {
                visibleItemCount = recyclerView.getChildCount();
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
                        getFollowersList(currentPage);
                        isLoading = true;
                    }
                }
            }
        });

        /*To load first ten items*/
        swipeRefresh(true);
        getFollowersList(currentPage = 0);
    }

    private void getFollowersList(int offset) {
        if (NetworkReceiver.isConnected()) {
            txtError.setVisibility(View.GONE);
            if (!swipeRefreshLayout.isRefreshing()) {
                adapter.showLoading(true);
            }
            Call<FollowersResponse> call;
            call = apiInterface.getFollowers(partnerId, offset * 20, limit);
            call.enqueue(new Callback<FollowersResponse>() {
                @Override
                public void onResponse(@NotNull Call<FollowersResponse> call, @NotNull Response<FollowersResponse> response) {
                    if (swipeRefreshLayout.isRefreshing()) {
                        followersList.clear();
                    }
                    if (response.isSuccessful()) {
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            txtError.setVisibility(View.GONE);
//                            Timber.d("onResponse: %s", App.getGsonPrettyInstance().toJson(response.body().getFollowersList()));

                            followersList.addAll(response.body().getFollowersList());
                        }
                    }
                    txtError.setVisibility(followersList.size() == 0 ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(followersList.size() > 0 ? View.VISIBLE : View.GONE);
                    txtError.setText(context.getString(R.string.no_followers_yet));
                    if (swipeRefreshLayout.isRefreshing()) {
                        adapter.showLoading(false);
                        swipeRefresh(false);
                        adapter.notifyDataSetChanged();
                        isLoading = true;
                    } else {
                        adapter.showLoading(false);
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Call<FollowersResponse> call, Throwable t) {
                    call.cancel();
                    if (currentPage != 0)
                        currentPage--;
                }
            });
        } else {
            if (currentPage != 0)
                currentPage--;
            if (!swipeRefreshLayout.isRefreshing()) {
                adapter.showLoading(false);
            } else {
                if (followersList.size() == 0) {
                    txtError.setVisibility(View.VISIBLE);
                    txtError.setText(context.getString(R.string.no_internet_connection));
                }
                swipeRefresh(false);
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FollowersActivity.hasInterestChange) {
            FollowersActivity.hasInterestChange = false;
            swipeRefresh(true);
            getFollowersList(currentPage = 0);
        }
    }

    private void swipeRefresh(final boolean refresh) {
        swipeRefreshLayout.setRefreshing(refresh);
    }

    public class FollowersAdapter extends RecyclerView.Adapter {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_FOOTER = 1;
        private final Context context;

        private List<FollowersResponse.FollowersList> itemList = new ArrayList<>();
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;

        public FollowersAdapter(Context context, List<FollowersResponse.FollowersList> followersList) {
            this.context = context;
            this.itemList = followersList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_followers, parent, false);
                viewHolder = new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }

            return viewHolder;
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(RecyclerView.@NotNull ViewHolder holder, int pos) {
            if (holder instanceof MyViewHolder) {
                int position = holder.getAbsoluteAdapterPosition();
                final FollowersResponse.FollowersList follower = itemList.get(position);

                ((MyViewHolder) holder).txtName.setText(follower.getName());
                ((MyViewHolder) holder).userName.setText(follower.getName());

                if (follower.getGender().equals(Constants.TAG_MALE)) {
                    ((MyViewHolder) holder).genderImage.setImageDrawable(context.getDrawable(R.drawable.men));
                } else {
                    ((MyViewHolder) holder).genderImage.setImageDrawable(context.getDrawable(R.drawable.women));
                }
                ((MyViewHolder) holder).premiumImage.setVisibility(follower.getPremiumMember().equals(Constants.TAG_TRUE) ?
                        View.VISIBLE : View.GONE);
              //due to new design jan7, me onlt hided this line  ((MyViewHolder) holder).userLay.setVisibility(View.VISIBLE);
                ((MyViewHolder) holder).lockedLay.setVisibility(View.GONE);

                Glide.with(context)
                        .load(follower.getUserImage())
                        .apply(new RequestOptions().error(R.drawable.default_profile_image).placeholder(R.drawable.default_profile_image))
                        .into(((MyViewHolder) holder).userImage);


                // Capture the background of userLay
               /* Glide.with(context)
                        .asBitmap() // Load the image as a Bitmap
                        .load(follower.getUserImage()) // Background image URL
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                ((MyViewHolder) holder).itemLay.post(() -> {
                                    // Get the position of userLay relative to ivThumb
                                    int[] userLayLocation = new int[2];
                                    ((MyViewHolder) holder).userLayNew.getLocationOnScreen(userLayLocation);

                                    int[] ivThumbLocation = new int[2];
                                    ((MyViewHolder) holder).userImage.getLocationOnScreen(ivThumbLocation);

                                    int left = userLayLocation[0] - ivThumbLocation[0];
                                    int top = userLayLocation[1] - ivThumbLocation[1];
                                    int width =  ((MyViewHolder) holder).userLayNew.getWidth();
                                    int height =  ((MyViewHolder) holder).userLayNew.getHeight();

                                    // Safeguard against invalid dimensions
                                    left = Math.max(0, left);
                                    top = Math.max(0, top);

                                    if (left + width > resource.getWidth()) {
                                        width = resource.getWidth() - left;
                                    }

                                    if (top + height > resource.getHeight()) {
                                        height = resource.getHeight() - top;
                                    }

                                    // Ensure width and height are valid
                                    if (width > 0 && height > 0) {
                                        // Crop the specific portion of the image for userLay
                                        Bitmap croppedBitmap = Bitmap.createBitmap(resource, left, top, width, height);

                                        // Apply blur to the cropped portion
                                        Bitmap blurredBitmap = BlurUtil.blur(context, croppedBitmap, 15f);

                                        // Create a rounded drawable from the blurred bitmap
                                        RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), blurredBitmap);
                                        roundedDrawable.setCornerRadius(20f); // Set the desired corner radius
                                        roundedDrawable.setAntiAlias(true);

                                        // Set the rounded drawable as the background of userLay
                                        ((MyViewHolder) holder).userLayNew.setBackground(roundedDrawable);
                                    } else {
                                        // Handle the case where width or height is invalid (e.g., fallback or log)
                                        Log.e(TAG, "Invalid crop dimensions: width=" + width + ", height=" + height);
                                    }
                                });
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                // Clear resources if needed
                            }
                        });*/

                ((MyViewHolder) holder).itemLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    /*FollowersResponse.FollowersList item = itemList.get(position);
                    Intent intent = new Intent(getActivity(), OthersProfileActivity.class);
                    intent.putExtra(Constants.TAG_PARTNER_ID, item.getUserId());
                    intent.putExtra(Constants.TAG_PARTNER_NAME, item.getName());
                    intent.putExtra(Constants.TAG_AGE, "" + item.getAge());
                    intent.putExtra(Constants.TAG_PARTNER_IMAGE, item.getUserImage());
                    intent.putExtra(Constants.TAG_GENDER, item.getGender());
                    intent.putExtra(Constants.TAG_BLOCKED_BY_ME, "");
                    intent.putExtra(Constants.TAG_LOCATION, item.getLocation());
                    intent.putExtra(Constants.TAG_PRIVACY_AGE, item.getPrivacyAge());
                    intent.putExtra(Constants.TAG_PRIVACY_CONTACT_ME, item.getPrivacyContactMe());
                    intent.putExtra(Constants.TAG_FOLLOWERS, "-");
                    intent.putExtra(Constants.TAG_FOLLOWINGS, "-");
                    intent.putExtra(Constants.TAG_PREMIUM_MEBER, item.getPremiumMember());
                    intent.putExtra(Constants.TAG_FROM, Constants.TAG_INTEREST);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);*/

                        if (itemList.get(position).getUserId().equals(GetSet.getUserId())) {
                            Intent profile = new Intent(getActivity(), MyProfileActivity.class);
                            profile.putExtra(Constants.TAG_FROM, Constants.TAG_OTHER_PROFILE_FANS_FOLLOWERS);
                            startActivity(profile);
                        } else {
                            Intent profile = new Intent(getActivity(), OthersProfileActivity.class);
                            profile.putExtra(Constants.TAG_USER_ID, itemList.get(position).getUserId());
                            profile.putExtra(Constants.TAG_USER_IMAGE, itemList.get(position).getUserImage());
                            profile.putExtra(Constants.TAG_FROM, Constants.TAG_OTHER_PROFILE_FANS_FOLLOWERS);
                            startActivity(profile);
                        }
                    }
                });

            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(View.VISIBLE);
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

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView userImage;
            ImageView premiumImage;
            TextView txtName, userName;
            TextView txtAge;
            ImageView genderImage;
            LinearLayout userLay,userLayNew;
            MaterialCardView itemLay;
            LottieAnimationView iconLock;
            FrameLayout lockedLay;

            public MyViewHolder(View view) {
                super(view);
                itemLay = view.findViewById(R.id.itemLay);
                userImage = view.findViewById(R.id.userImage);
                userLay = view.findViewById(R.id.userLay);
                userLayNew = view.findViewById(R.id.userLayNew);
                premiumImage = view.findViewById(R.id.premiumImage);
                txtName = view.findViewById(R.id.txtName);
                txtAge = view.findViewById(R.id.txtAge);
                userName = view.findViewById(R.id.userName);
                genderImage = view.findViewById(R.id.genderImage);
                lockedLay = view.findViewById(R.id.lockedLay);
                iconLock = view.findViewById(R.id.iconLock);

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                txtName.setMaxWidth((int) (displayMetrics.widthPixels * 0.20));
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

    private void goToPrime() {
        Intent prime = new Intent(context, PrimeActivity.class);
        prime.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(prime, Constants.PRIME_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.PRIME_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            swipeRefresh(true);
            getFollowersList(currentPage = 0);
        }
    }

}



