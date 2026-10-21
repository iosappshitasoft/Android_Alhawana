package com.app.alhawanajd;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.app.alhawanajd.databinding.ActivityGemsStoreBinding;
import com.app.alhawanajd.databinding.ItemGemsStoreBinding;
import com.app.alhawanajd.databinding.ItemLoadingBinding;
import com.app.alhawanajd.databinding.LayoutPrimeBannerBinding;
import com.app.alhawanajd.databinding.LayoutRenewalBannerBinding;
import com.app.alhawanajd.databinding.PlaceholderGemsStoreBinding;
import com.app.db.DBHelper;
import com.app.model.ProfileRequest;
import com.app.model.ProfileResponse;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
//import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.helper.callback.OnOkCancelClickListener;
import com.app.model.GemsPurchaseRequest;
import com.app.model.GemsPurchaseResponse;
import com.app.model.GemsStoreResponse;
import com.app.model.GetSet;
import com.app.utils.AppUtils;
import com.app.utils.Constants;
import com.app.utils.SharedPref;
import com.app.utils.api.ApiClient;
import com.app.utils.api.ApiInterface;
import com.makeramen.roundedimageview.RoundedImageView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class GemsStoreActivity extends BaseFragmentActivity implements PurchasesUpdatedListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = GemsStoreActivity.class.getSimpleName();
    private ActivityGemsStoreBinding binding;

    GemsAdapter gemsAdapter;
    List<GemsStoreResponse.GemsList> gemsList = new ArrayList<>();
    private int currentPage = 0, limit = 100;
    private GridLayoutManager mLayoutManager;
    private boolean isLoading = true, hasLoadMore = true, isSwipeEnabled = true;
    ApiInterface apiInterface;
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_FOOTER = 1;
    private final int VIEW_TYPE_BANNER = 2;
    private final int VIEW_TYPE_PLACEHOLDER = 3;
    private BillingClient billingClient;
    private String purchaseSku = "", purchasePrice = "", purchaseCurrency = "";
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.6f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;
    DialogCreditGems alertDialog;
    private AppUtils appUtils;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        binding = ActivityGemsStoreBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        billingClient = BillingClient.newBuilder(GemsStoreActivity.this).enablePendingPurchases().setListener(this).build();
        appUtils = new AppUtils(this);
        initView();
        getFreeGems(currentPage);
    }

    private void initView() {
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        Slidr.attach(this, config);
      /*  startAlphaAnimation(txtGems, 0, View.INVISIBLE);*/
      /*  startAlphaAnimation(txtGems, 0, View.INVISIBLE);*/
        gemsList = getGemsPlaceHolder();
        gemsAdapter = new GemsAdapter(GemsStoreActivity.this, gemsList);
        mLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setAdapter(gemsAdapter);
        gemsAdapter.notifyDataSetChanged();

      /* commented for new design mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (gemsAdapter.isPositionFooter(position)) {
                    return mLayoutManager.getSpanCount();
                } else if (gemsAdapter.getItemViewType(position) == VIEW_TYPE_BANNER) {
                    return mLayoutManager.getSpanCount();
                } else {
                    return 1;
                }
            }
        });*/

        binding.swipeRefreshLayout.setOnRefreshListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (scrollY > 0 && v.getChildAt(v.getChildCount() - 1) != null) {
                        if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                                scrollY > oldScrollY) {
                            //code to fetch more data for endless scrolling
                            if (isLoading && hasLoadMore) {
                                isLoading = false;
                                currentPage++;
                            }
                            if (hasLoadMore) {
                                // End has been reached
                                getFreeGems(currentPage);
                                isLoading = true;
                            }
                        }
                    }
                }
            });
        } else {
            binding.nestedScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    View view = binding.nestedScrollView.getChildAt(binding.nestedScrollView.getChildCount() - 1);
                    int diff = (view.getBottom() - (binding.nestedScrollView.getHeight() + binding.nestedScrollView
                            .getScrollY()));

                    if (binding.nestedScrollView.getScrollY() > 0 && diff == 0) {
                        if (isLoading && hasLoadMore) {
                            isLoading = false;
                            currentPage++;
                        }
                        if (hasLoadMore) {
                            // End has been reached
                            getFreeGems(currentPage);
                            isLoading = true;
                        }
                    }
                }
            });
        }

        binding.btnFreeGems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent freeGems = new Intent(getApplicationContext(), EarnGemsActivity.class);
                freeGems.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(freeGems);
            }
        });

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
/*

        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int maxScroll = appBarLayout.getTotalScrollRange();
                float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;

                handleAlphaOnTitle(percentage);
                handleToolbarTitleVisibility(percentage);
            }
        });

*/

        if (LocaleManager.isRTL()) {
            binding.btnBack.setRotation(180);
        } else {
            binding.btnBack.setRotation(0);
        }
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if (!mIsTheTitleVisible) {
                startAlphaAnimation(binding.txtGems, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(binding.txtGems, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (mIsTheTitleContainerVisible) {
                startAlphaAnimation(binding.headerLay, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(binding.headerLay, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    private List<GemsStoreResponse.GemsList> getGemsPlaceHolder() {
        List<GemsStoreResponse.GemsList> gemsLists = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            gemsLists.add(null);
        }
        return gemsLists;
    }

    private void getFreeGems(int offset) {
        if (NetworkReceiver.isConnected()) {
            binding.nullLay1.nullLay.setVisibility(View.GONE);
            if (!binding.swipeRefreshLayout.isRefreshing() && offset != 0) {
                gemsAdapter.showLoading(true);
            }
            Log.d(TAG, "getFreeGems: Params" + GetSet.getUserId() + "," + Constants.TAG_ANDROID + "," + offset * limit + "," + limit);
            Call<GemsStoreResponse> call = apiInterface.getGemsList(GetSet.getToken(),GetSet.getUserId(), Constants.TAG_ANDROID, offset * limit, limit);
            call.enqueue(new Callback<GemsStoreResponse>() {
                @Override
                public void onResponse(Call<GemsStoreResponse> call, Response<GemsStoreResponse> response) {
                    if (offset == 0) {
                        gemsList.clear();
                    }
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        binding.nullLay1.nullLay.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        if (offset == 0 && response.body().getGemsList().size() < 10) {
                            gemsList.addAll(response.body().getGemsList());
                            initBilling(false);
                            /*Add banner*/
                            /*GemsStoreResponse.GemsList bannerItem = new GemsStoreResponse().new GemsList();
                            bannerItem.setViewType(VIEW_TYPE_BANNER);
                            gemsList.add(bannerItem);*/
                        } else {
                            int gemsListSize = response.body().getGemsList().size();
                            for (int i = 0; i < gemsListSize; i++) {
                                gemsList.add(response.body().getGemsList().get(i));
                                if ((i + 1) % 10 == 0) {
                                    /*Add banner*/
                                    /*GemsStoreResponse.GemsList bannerItem = new GemsStoreResponse().new GemsList();
                                    bannerItem.setViewType(VIEW_TYPE_BANNER);
                                    gemsList.add(bannerItem);*/
                                }
                            }
                            initBilling(false);
                        }
                    } else {
                        swipeRefresh(false);
                        hasLoadMore = false;
                        if (gemsList.size() == 0) {
                            binding.recyclerView.setVisibility(View.GONE);
                            binding.nullLay1.nullLay.setVisibility(View.VISIBLE);
                            binding.nullLay1.nullText.setVisibility(View.VISIBLE);
                            binding.nullLay1.nullImage.setImageDrawable(ContextCompat.getDrawable(GemsStoreActivity.this, R.drawable.coinprimarycolor));
                            binding.nullLay1.nullText.setTextColor(ContextCompat.getColor(GemsStoreActivity.this, R.color.colorWhite));
                            binding.nullLay1.nullText.setText(getString(R.string.no_gems_list_found));
                        }
                    }

                }

                @Override
                public void onFailure(Call<GemsStoreResponse> call, Throwable t) {
                    call.cancel();
                    if (currentPage != 0)
                        currentPage--;
                }
            });
        } else {
            if (currentPage != 0)
                currentPage--;
            if (!binding.swipeRefreshLayout.isRefreshing()) {
                gemsAdapter.showLoading(false);
            } else {
                if (gemsList.size() == 0) {
                    binding.nullLay1.nullText.setVisibility(View.GONE);
                    binding.nullLay1.nullText.setText(getString(R.string.no_internet_connection));
                }
                swipeRefresh(false);
            }
        }
    }

    private void swipeRefresh(final boolean refresh) {
        binding.swipeRefreshLayout.setRefreshing(refresh);
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), isConnected);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    @Override
    public void onRefresh() {
        currentPage = 0;
        hasLoadMore = true;
        getFreeGems(currentPage);
    }

    void handlePurchase(String purchaseToken) {
        // Purchase retrieved from BillingClient#queryPurchases or your PurchasesUpdatedListener.
        // Verify the purchase.
        // Ensure entitlement was not already granted for this purchaseToken.
        // Grant entitlement to the user.

        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchaseToken)
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                }
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                if (purchaseSku.equals(purchase.getProducts().get(0))) {
                    //Consume Item to purchase again.
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        payInApp(purchase);
                        handlePurchase(purchase.getPurchaseToken());
                    }
                    break;
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
            App.makeToast(getString(R.string.purchase_cancelled));
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            // Handle an error caused by if item already owned.
            billingClient.queryPurchaseHistoryAsync(QueryPurchaseHistoryParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build(), new PurchaseHistoryResponseListener() {
                @Override
                public void onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> purchasesList) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        for (PurchaseHistoryRecord purchase : purchasesList) {
                            if (purchaseSku.equals(purchase.getProducts().get(0))) {
                                handlePurchase(purchase.getPurchaseToken());
                                break;
                            }
                        }
                    }
                }
            });
        } else {
            // Handle any other error codes.
//            App.makeToast("" + responseCode);
        }

    }

 /*   public class GemsAdapter extends RecyclerView.Adapter {

        private final Context context;
        private List<GemsStoreResponse.GemsList> gemsList = new ArrayList<>();
        private boolean showLoading = false;
        private RecyclerView.ViewHolder viewHolder;
        Typeface typefaceLight = null, typefaceRegular;

        public GemsAdapter(Context context, List<GemsStoreResponse.GemsList> gemsList) {
            this.context = context;
            this.gemsList = gemsList;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                typefaceLight = getResources().getFont(R.font.font_light);
                typefaceRegular = getResources().getFont(R.font.font_regular);
            } else {
                typefaceLight = ResourcesCompat.getFont(context, R.font.font_light);
                typefaceRegular = ResourcesCompat.getFont(context, R.font.font_regular);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.item_gems_store, parent, false);
                viewHolder = new MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_PLACEHOLDER) {
                View itemView = LayoutInflater.from(context)
                        .inflate(R.layout.placeholder_gems_store, parent, false);
                viewHolder = new PlaceHolder(itemView);
            } *//*else if (viewType == VIEW_TYPE_BANNER) {
                View v;
                if (GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    v = LayoutInflater.from(context).inflate(R.layout.layout_prime_banner, parent, false);
                    viewHolder = new PrimeViewHolder(v);
                } else if (GetSet.isOncePurchased()) {
                    v = LayoutInflater.from(context).inflate(R.layout.layout_renewal_banner, parent, false);
                    viewHolder = new RenewalViewHolder(v);
                } else {
                    v = LayoutInflater.from(context).inflate(R.layout.layout_prime_banner, parent, false);
                    viewHolder = new PrimeViewHolder(v);
                }
            } *//*else if (viewType == VIEW_TYPE_FOOTER) {
                View v = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
                viewHolder = new FooterViewHolder(v);
            }else *//* viewType == VIEW_TYPE_BANNER *//* {
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_gems_store, parent, false);
                viewHolder = new MyViewHolder(itemView);
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                final GemsStoreResponse.GemsList gems = gemsList.get(position);
                ((MyViewHolder) holder).txtGemsCount.setText("" + Math.round(gems.getGemCount()));
                ((MyViewHolder) holder).txtPrice.setText(gems.getGemPrice());
                Glide.with(context)
                        *//*.load(Constants.GEMS_IMAGE_URL + gems.getGemIcon())*//*
                        .load(gems.getGemIcon())
                        .apply(new RequestOptions().placeholder(R.drawable.diamond).error(R.drawable.diamond).dontAnimate().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(((MyViewHolder) holder).giftImage);
            } else if (holder instanceof PlaceHolder) {

            } else if (holder instanceof PrimeViewHolder) {
                if (GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    ((PrimeViewHolder) holder).btnSubscribe.setVisibility(View.GONE);
                    ((PrimeViewHolder) holder).premiumLay.setVisibility(View.VISIBLE);
                } else {
                    ((PrimeViewHolder) holder).premiumLay.setVisibility(View.GONE);
                    ((PrimeViewHolder) holder).btnSubscribe.setVisibility(View.VISIBLE);
                }
                ((PrimeViewHolder) holder).txtPrimeTitle.setText(AppUtils.getPrimeTitle(context));
                ((PrimeViewHolder) holder).txtPrice.setText(AppUtils.getPrimeContent(context));
            } else if (holder instanceof RenewalViewHolder) {
                RenewalViewHolder renewalViewHolder = (RenewalViewHolder) holder;
                ((RenewalViewHolder) holder).renewalLay.setVisibility(View.VISIBLE);

            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerHolder = (FooterViewHolder) holder;
                footerHolder.progressBar.setIndeterminate(true);
                footerHolder.progressBar.setVisibility(View.VISIBLE);
            }
        }

       *//* @Override
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            else if (gemsList == null || gemsList.size() == 0 ||
                    position > gemsList.size() || gemsList.get(position) == null)
                return VIEW_TYPE_PLACEHOLDER;
            else if (gemsList.get(position).getViewType() == VIEW_TYPE_BANNER) {
                return VIEW_TYPE_BANNER;
            } else
                return VIEW_TYPE_ITEM;
        }*//*

        @Override
        public int getItemViewType(int position) {
            if (showLoading && isPositionFooter(position))
                return VIEW_TYPE_FOOTER;
            else if (gemsList == null || gemsList.size() == 0 ||
                    position > gemsList.size() || gemsList.get(position) == null)
                return VIEW_TYPE_PLACEHOLDER;
            else
                return VIEW_TYPE_ITEM; // always treat as normal item
        }


        @Override
        public int getItemCount() {
            int itemCount = gemsList.size();
            if (showLoading)
                itemCount++;
            return itemCount;
        }

        public boolean isPositionFooter(int position) {
            return position == getItemCount() - 1 && showLoading;
        }

        public void showLoading(boolean value) {
            showLoading = value;
            notifyDataSetChanged();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.giftImage)
            ImageView giftImage;
            @BindView(R.id.txtGemsCount)
            TextView txtGemsCount;
            @BindView(R.id.txtPrice)
            TextView txtPrice;
            @BindView(R.id.itemLay)
            CardView itemLay;

            public MyViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
                txtGemsCount.setTypeface(typefaceRegular);
                txtPrice.setTypeface(typefaceRegular);
            }

            @OnClick(R.id.itemLay)
            public void onViewClicked() {
                App.preventMultipleClick(itemView);
                purchaseSku = gemsList.get(getAdapterPosition()).getGemTitle();
                initBilling(true);
            }
        }

        public class PlaceHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.shimmerLayout)
            ShimmerFrameLayout shimmerLayout;

            public PlaceHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.progressBar)
            ProgressBar progressBar;

            public FooterViewHolder(View parent) {
                super(parent);
                ButterKnife.bind(this, parent);
            }
        }

        public class PrimeViewHolder extends RecyclerView.ViewHolder {

            *//*Subscribe Lay*//*
            @BindView(R.id.txtPrice)
            TextView txtPrice;
            @BindView(R.id.btnSubscribe)
            Button btnSubscribe;
            @BindView(R.id.subscribeLay)
            MaterialCardView subscribeLay;
            @BindView(R.id.txtPrimeTitle)
            TextView txtPrimeTitle;
            @BindView(R.id.premiumLay)
            RelativeLayout premiumLay;
            @BindView(R.id.primeBgLay)
            RelativeLayout primeBgLay;


            public PrimeViewHolder(View parent) {
                super(parent);
                ButterKnife.bind(this, parent);
               *//* RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(5, 10, 5, 30);*//*
                //subscribeLay.setLayoutParams(params);
                txtPrimeTitle.setTypeface(typefaceLight);
                txtPrice.setTypeface(typefaceLight);
                btnSubscribe.setTypeface(typefaceLight);
            }

            @OnClick({R.id.btnSubscribe, R.id.subscribeLay})
            public void onViewClicked(View view) {
                switch (view.getId()) {
                    case R.id.btnSubscribe:
                    case R.id.subscribeLay:
                        if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                            App.preventMultipleClick(btnSubscribe);
                            App.preventMultipleClick(subscribeLay);
                            startActivity(new Intent(getApplicationContext(), PrimeActivity.class));
                        }
                        break;
                }
            }
        }

        public class RenewalViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.btnRenewal)
            Button btnRenewal;
            @BindView(R.id.renewalBgLay)
            RelativeLayout renewalBgLay;
            @BindView(R.id.renewalLay)
            RelativeLayout renewalLay;
            @BindView(R.id.txtRenewalTitle)
            TextView txtRenewalTitle;
            @BindView(R.id.txtRenewalDes)
            TextView txtRenewalDes;

            public RenewalViewHolder(View parent) {
                super(parent);
                ButterKnife.bind(this, parent);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(5, 10, 5, 30);
                renewalLay.setLayoutParams(params);
                txtRenewalTitle.setTypeface(typefaceLight);
                txtRenewalDes.setTypeface(typefaceLight);
                btnRenewal.setTypeface(typefaceLight);
            }

            @OnClick({R.id.btnRenewal, R.id.renewalLay})
            public void onViewClicked(View view) {
                switch (view.getId()) {
                    case R.id.renewalLay:
                    case R.id.btnRenewal:
                        if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                            App.preventMultipleClick(btnRenewal);
                            App.preventMultipleClick(renewalLay);
                            startActivity(new Intent(getApplicationContext(), PrimeActivity.class));
                        }
                        break;
                }
            }
        }
    }
*/
    public class GemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final Context context;
        private final List<GemsStoreResponse.GemsList> gemsList;
        private boolean showLoading = false;
        private Typeface typefaceLight = null, typefaceRegular;

        public GemsAdapter(Context context, List<GemsStoreResponse.GemsList> gemsList) {
            this.context = context;
            this.gemsList = gemsList;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                typefaceLight = getResources().getFont(R.font.font_light);
                typefaceRegular = getResources().getFont(R.font.font_regular);
            } else {
                typefaceLight = ResourcesCompat.getFont(context, R.font.font_light);
                typefaceRegular = ResourcesCompat.getFont(context, R.font.font_regular);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            // 1. Inflate layouts using their respective View Binding classes.
            switch (viewType) {
                case VIEW_TYPE_ITEM:
                    ItemGemsStoreBinding itemBinding = ItemGemsStoreBinding.inflate(inflater, parent, false);
                    return new MyViewHolder(itemBinding);

                case VIEW_TYPE_FOOTER:
                    ItemLoadingBinding loadingBinding = ItemLoadingBinding.inflate(inflater, parent, false);
                    return new FooterViewHolder(loadingBinding);
/*

                case VIEW_TYPE_PRIME: // Used for Prime Banner
                    ItemPrimeBannerBinding primeBinding = ItemPrimeBannerBinding.inflate(inflater, parent, false);
                    return new PrimeViewHolder(primeBinding);

                case VIEW_TYPE_RENEWAL: // Used for Renewal Banner
                    ItemRenewalBannerBinding renewalBinding = ItemRenewalBannerBinding.inflate(inflater, parent, false);
                    return new RenewalViewHolder(renewalBinding);
*/

                case VIEW_TYPE_PLACEHOLDER:
                default:
                    PlaceholderGemsStoreBinding placeholderBinding = PlaceholderGemsStoreBinding.inflate(inflater, parent, false);
                    return new PlaceHolder(placeholderBinding);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            // 2. Access views through the holder's binding object.
            if (holder instanceof MyViewHolder) {
                final GemsStoreResponse.GemsList gems = gemsList.get(position);
                MyViewHolder myViewHolder = (MyViewHolder) holder;
                myViewHolder.binding.txtGemsCount.setText(String.valueOf(Math.round(gems.getGemCount())));
                myViewHolder.binding.txtPrice.setText(gems.getGemPrice());
                Glide.with(context)
                        .load(gems.getGemIcon())
                        .apply(new RequestOptions().placeholder(R.drawable.diamond).error(R.drawable.diamond).dontAnimate().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(myViewHolder.binding.giftImage);

            } else if (holder instanceof PrimeViewHolder) {
                PrimeViewHolder primeViewHolder = (PrimeViewHolder) holder;
                if (GetSet.getPremiumMember() != null && GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    primeViewHolder.binding.btnSubscribe.setVisibility(View.GONE);
                    // Assuming you have a 'premiumLay' in this layout
                    // primeViewHolder.binding.premiumLay.setVisibility(View.VISIBLE);
                } else {
                    // primeViewHolder.binding.premiumLay.setVisibility(View.GONE);
                    primeViewHolder.binding.btnSubscribe.setVisibility(View.VISIBLE);
                }
                primeViewHolder.binding.txtPrimeTitle.setText(AppUtils.getPrimeTitle(context));
                primeViewHolder.binding.txtPrice.setText(AppUtils.getPrimeContent(context));

            } else if (holder instanceof RenewalViewHolder) {
                RenewalViewHolder renewalViewHolder = (RenewalViewHolder) holder;
                renewalViewHolder.binding.renewalLay.setVisibility(View.VISIBLE);

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
            else if (gemsList == null || gemsList.size() == 0 ||
                    position > gemsList.size() || gemsList.get(position) == null)
                return VIEW_TYPE_PLACEHOLDER;
            else
                return VIEW_TYPE_ITEM; // always treat as normal item
        }

        @Override
        public int getItemCount() {
            int itemCount = gemsList.size();
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

        // 3. All ViewHolder classes are updated to hold a binding object.

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final ItemGemsStoreBinding binding;

            public MyViewHolder(ItemGemsStoreBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                binding.txtGemsCount.setTypeface(typefaceRegular);
                binding.txtPrice.setTypeface(typefaceRegular);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                App.preventMultipleClick(itemView);
                int position = getAdapterPosition();
                if(position != RecyclerView.NO_POSITION) {
                    purchaseSku = gemsList.get(position).getGemTitle();
                    initBilling(true);
                }
            }
        }

        public class PlaceHolder extends RecyclerView.ViewHolder {
            private final PlaceholderGemsStoreBinding binding;
            public PlaceHolder(PlaceholderGemsStoreBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

        public class FooterViewHolder extends RecyclerView.ViewHolder {
            private final ItemLoadingBinding binding;
            public FooterViewHolder(ItemLoadingBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

        public class PrimeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final LayoutPrimeBannerBinding binding;
            public PrimeViewHolder(LayoutPrimeBannerBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                binding.txtPrimeTitle.setTypeface(typefaceLight);
                binding.txtPrice.setTypeface(typefaceLight);
                binding.btnSubscribe.setTypeface(typefaceLight);

                binding.btnSubscribe.setOnClickListener(this);
                // Set listener on the whole layout if needed
                // binding.subscribeLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    App.preventMultipleClick(v);
                    startActivity(new Intent(getApplicationContext(), PrimeActivity.class));
                }
            }
        }

        public class RenewalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private final LayoutRenewalBannerBinding binding;
            public RenewalViewHolder(LayoutRenewalBannerBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                binding.txtRenewalTitle.setTypeface(typefaceLight);
                binding.txtRenewalDes.setTypeface(typefaceLight);
                binding.btnRenewal.setTypeface(typefaceLight);

                binding.btnRenewal.setOnClickListener(this);
                binding.renewalLay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (GetSet.getPremiumMember() != null && !GetSet.getPremiumMember().equals(Constants.TAG_TRUE)) {
                    App.preventMultipleClick(v);
                    startActivity(new Intent(getApplicationContext(), PrimeActivity.class));
                }
            }
        }
    }

    private void initBilling(boolean showAlert) {
        billingClient.startConnection(new BillingClientStateListener() {

            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                int billingResponseCode = billingResult.getResponseCode();
                if (billingResponseCode == BillingClient.BillingResponseCode.OK) {
                    // The billing client is ready. You can query purchases here.
                    List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
                    for (GemsStoreResponse.GemsList gems : gemsList) {
                        if (gems.getGemTitle() != null) {
                            QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder().setProductId(gems.getGemTitle())
                                    .setProductType(BillingClient.ProductType.INAPP).build();
                            productList.add(product);
                        }
                    }
                    QueryProductDetailsParams queryProductDetailsParams =
                            QueryProductDetailsParams.newBuilder().setProductList(productList).build();

                    billingClient.queryProductDetailsAsync(queryProductDetailsParams,
                            new ProductDetailsResponseListener() {
                                @Override
                                public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {

                                    // Process the result.
                                    Timber.tag(TAG).i("skuDetailsList: %s", new Gson().toJson(productDetailsList));
//                                    Timber.i("skuDetailsList getDebugMessage %s", billingResult.getDebugMessage());
//                                    Timber.i("skuDetailsList getResponseCode %s", billingResult.getResponseCode());

                                    if (showAlert) {
                                        for (ProductDetails productDetails : productDetailsList) {
                                            String sku = productDetails.getProductId();
                                            String price = productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
                                            String currency = productDetails.getOneTimePurchaseOfferDetails().getPriceCurrencyCode();
                                            if (purchaseSku.equals(sku)) {
                                                purchasePrice = price;
                                                purchaseCurrency = currency;
                                                // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
                                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                                        .setProductDetailsParamsList(ImmutableList.of(
                                                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                                                        .setProductDetails(productDetails)
                                                                        .build()
                                                        ))
                                                        .build();
                                                BillingResult result = billingClient.launchBillingFlow(GemsStoreActivity.this, flowParams);
                                            }
                                        }
                                    } else {
                                        Timber.i("skuDetailsList %s", "else enter");
                                        for (ProductDetails productDetails : productDetailsList) {
                                            purchaseCurrency = productDetails.getOneTimePurchaseOfferDetails().getPriceCurrencyCode();
                                            for (GemsStoreResponse.GemsList list : gemsList) {
                                                if (list.getViewType() == VIEW_TYPE_ITEM && list.getGemTitle().equals(productDetails.getProductId())) {
                                                    list.setGemPrice(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());
                                                    break;
                                                }
                                            }
                                        }
                                        mainHandler.post(new Runnable() {
                                            public void run() {
                                                if (binding.swipeRefreshLayout.isRefreshing()) {
                                                    gemsAdapter.showLoading(false);
                                                    swipeRefresh(false);
                                                    gemsAdapter.notifyDataSetChanged();
                                                    isLoading = true;
                                                } else {
                                                    gemsAdapter.showLoading(false);
                                                    gemsAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }


                                }

                            });
                }

            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }


    private void payInApp(Purchase purchase) {
        if (NetworkReceiver.isConnected()) {
            GemsPurchaseRequest request = new GemsPurchaseRequest();
            request.setUserId(GetSet.getUserId());
            request.setGemId(purchaseSku);
            request.setPaidAmount(purchaseCurrency + " " + purchasePrice);
            request.setTransactionId(purchase.getOrderId());
            Call<GemsPurchaseResponse> call = apiInterface.buyGems(request);
            call.enqueue(new Callback<GemsPurchaseResponse>() {
                @Override
                public void onResponse(@NonNull Call<GemsPurchaseResponse> call, @NonNull Response<GemsPurchaseResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        GemsPurchaseResponse purchaseResponse = response.body();
                        if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                            GetSet.setGems(Float.valueOf(purchaseResponse.getAvailableGems()));
                            SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                            binding.txtAvailableGems.setText(GetSet.getGems() + " " + getString(R.string.coins));
                            showAlertDialog(getString(R.string.gems_purchase_response));
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GemsPurchaseResponse> call, @NonNull Throwable t) {

                }
            });
        }
    }

    private void showAlertDialog(String message) {
        if (alertDialog != null && alertDialog.isAdded()) {
            return;
        }
        alertDialog = new DialogCreditGems();
        alertDialog.setContext(GemsStoreActivity.this);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(message);
        alertDialog.setCallBack(new OnOkCancelClickListener() {
            @Override
            public void onOkClicked(Object o) {
                alertDialog.dismissAllowingStateLoss();
            }

            @Override
            public void onCancelClicked(Object o) {
                alertDialog.dismissAllowingStateLoss();
            }
        });
        alertDialog.show(getSupportFragmentManager(), TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getProfile();
        AppUtils.showSnack(getApplicationContext(), findViewById(R.id.parentLay), NetworkReceiver.isConnected());
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    private void getProfile() {
        if (GetSet.getUserId() != null && apiInterface != null) {
            ProfileRequest request = new ProfileRequest();
            request.setUserId(GetSet.getUserId());
            request.setProfileId(GetSet.getUserId());

//            Timber.d("getProfile: %s", new Gson().toJson(request));

            Call<ProfileResponse> call = apiInterface.getProfile(request);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(@NotNull Call<ProfileResponse> call, @NotNull Response<ProfileResponse> response) {
                    if (response.isSuccessful() && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        ProfileResponse profile = response.body();
                        Log.d("getProfile",new Gson().toJson(profile));
                         GetSet.setGifts(profile.getAvailableGifts() != null ? Long.parseLong(String.valueOf(profile.getAvailableGifts())) : 0L);
                         GetSet.setGems(profile.getAvailableGems() != null ? (profile.getAvailableGems()) : 0F);
                         SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts());
                         SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                        // Long gems = GetSet.getGems().longValue();
                         binding.txtAvailableGems.setText(profile.getAvailableGems() +" " + getString(R.string.coins));
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
