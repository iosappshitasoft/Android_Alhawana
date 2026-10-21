package com.app.alhawanajd;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

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
import com.app.alhawanajd.databinding.ActivityPrimeOldBinding; // 1. Import Activity binding
import com.app.alhawanajd.databinding.ItemPrimeBinding;         // 1. Import Item binding
import com.app.alhawanajd.databinding.ItemPrimePackBinding;     // 1. Import Item binding
import com.app.alhawanajd.databinding.PlaceholderItemPrimeBinding; // 1. Import Item binding
import com.app.model.PackageModel;
import com.bumptech.glide.Glide;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.app.helper.LocaleManager;
import com.app.helper.NetworkReceiver;
import com.app.model.GetSet;
import com.app.model.MembershipPackages;
import com.app.model.PrimeBenefit;
import com.app.model.SubscriptionRequest;
import com.app.model.SubscriptionResponse;
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

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

// No more ButterKnife imports

@SuppressLint("NonConstantResourceId")
public class PrimeActivityOld extends BaseFragmentActivity implements PurchasesUpdatedListener {

    private static final String TAG = PrimeActivityOld.class.getSimpleName();
    int displayHeight, displayWidth;

    // 2. Declare the binding object
    private ActivityPrimeOldBinding binding;

    private PrimeAdapter primeAdapter;
    private SlidrInterface sliderInterface;
    static final int RC_REQUEST = 10001;
    private String productId = "", purchasePrice = "", purchaseCurrency = "", transactionId = "";
    private BillingClient billingClient;
    ApiInterface apiInterface;
    int resultCode = RESULT_CANCELED;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    PackageAdapter packageAdapter;
    int selectedPosition = 0;
    List<MembershipPackages> productList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        // 3. Inflate the layout using View Binding
        binding = ActivityPrimeOldBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // No more ButterKnife.bind(this)

        billingClient = BillingClient.newBuilder(PrimeActivityOld.this).enablePendingPurchases().setListener(this).build();
        initView();
        getPrimeDetails();
    }

    private void initView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;

        // 4. Access all views through the binding object
        binding.looperViewpager.setClipToPadding(true);
        binding.looperViewpager.setPadding(40, 40, 40, 0);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        sliderInterface = Slidr.attach(this, config);

        binding.toolbar.txtSubTitle.setVisibility(View.VISIBLE);

        if (LocaleManager.isRTL()) {
            binding.toolbar.btnBack.setRotation(180);
        } else {
            binding.toolbar.btnBack.setRotation(0);
        }

        binding.toolbar.btnBack.setVisibility(View.VISIBLE);
        binding.toolbar.txtSubTitle.setVisibility(View.GONE);
        binding.toolbar.txtTitle.setTextColor(getResources().getColor(R.color.colorWhite));
        binding.toolbar.txtTitle.setText(getString(R.string.prime));

        // 5. Set up click listeners manually
        binding.toolbar.btnBack.setOnClickListener(v -> onBackPressed());
        binding.btnSubscribe.setOnClickListener(v -> {
            if (packageAdapter != null && packageAdapter.getItemCount() > 0) {
                productId = packageAdapter.getPackageList().get(selectedPosition).getSubsProductId();
            }
            initBilling(true);
        });

        binding.looperViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                if (position > 0) {
                    sliderInterface.lock();
                } else {
                    sliderInterface.unlock();
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx > 0) {
                    sliderInterface.lock();
                } else sliderInterface.unlock();
            }
        });

        initPackageAdapter();
        initBilling(false);
    }

    private void getPrimeDetails() {
        List<PrimeBenefit> primeList = new ArrayList<>();
        if (AdminData.primeDetails.getPrimeBenefits() != null && AdminData.primeDetails.getPrimeBenefits().size() > 0) {
            primeList.addAll(AdminData.primeDetails.getPrimeBenefits());
        }

        if (primeList.size() == 0) {
            binding.pagerIndicator.setVisibility(View.GONE);
            primeList = getPlaceHolderList();
        } else {
            binding.pagerIndicator.setVisibility(View.VISIBLE);
        }

        primeAdapter = new PrimeAdapter(getApplicationContext(), primeList);
        binding.looperViewpager.setAdapter(primeAdapter);
        primeAdapter.notifyDataSetChanged();
        binding.pagerIndicator.setViewPager(binding.looperViewpager);
    }

    private List<PrimeBenefit> getPlaceHolderList() {
        List<PrimeBenefit> primeList = new ArrayList<>();
        primeList.add(null);
        return primeList;
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        AppUtils.showSnack(this, binding.getRoot(), isConnected);
    }

    @Override
    public void onBackPressed() {
        setResult(resultCode);
        finish();
        overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_right_out);
    }

    private void initBilling(boolean openDialog) {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    List<QueryProductDetailsParams.Product> tempList = new ArrayList<>();
                    for (MembershipPackages membershipPackages : AdminData.membershipList) {
                        tempList.add(QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(membershipPackages.getSubsProductId())
                                .setProductType(BillingClient.ProductType.INAPP).build());
                    }

                    if (tempList.size() > 0) {
                        QueryProductDetailsParams queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(tempList).build();
                        billingClient.queryProductDetailsAsync(queryProductDetailsParams,
                                (billingResult1, productDetailsList) -> {
                                    if (openDialog) {
                                        for (ProductDetails productDetails : productDetailsList) {
                                            if (productId.equals(productDetails.getProductId())) {
                                                purchasePrice = productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
                                                purchaseCurrency = productDetails.getOneTimePurchaseOfferDetails().getPriceCurrencyCode();
                                                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                                        .setProductDetailsParamsList(ImmutableList.of(
                                                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                                                        .setProductDetails(productDetails)
                                                                        .build()
                                                        ))
                                                        .build();
                                                billingClient.launchBillingFlow(PrimeActivityOld.this, flowParams);
                                                break;
                                            }
                                        }
                                    } else {
                                        for (ProductDetails productDetails : productDetailsList) {
                                            for (MembershipPackages membershipPackages : AdminData.membershipList) {
                                                if (productDetails.getProductId().equals(membershipPackages.getSubsProductId())) {
                                                    membershipPackages.setSubsPrice(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());
                                                    //membershipPackages.setSubsPriceValue(productDetails.getOneTimePurchaseOfferDetails().getPriceAmountMicros() / 1000000.0);
                                                    productList.add(membershipPackages);
                                                    break;
                                                }
                                            }
                                        }
                                        mainHandler.post(() -> {
                                            packageAdapter.setPackageList(productList);
                                            packageAdapter.notifyDataSetChanged();
                                            if (productList.size() > 0) {
                                                binding.btnSubscribe.setVisibility(View.VISIBLE);
                                            }
                                        });
                                    }
                                });
                    } else {
                        if (openDialog) {
                            App.makeToast(getString(R.string.package_empty_description));
                        }
                    }
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                initBilling(false);
            }
        });
    }

    private void initPackageAdapter() {
        packageAdapter = new PackageAdapter(PrimeActivityOld.this, new ArrayList<>());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setAdapter(packageAdapter);
        binding.recyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                if (productId.equals(purchase.getProducts().get(0))) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        payInApp(purchase);
                        handlePurchase(purchase.getPurchaseToken());
                    }
                    break;
                }
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            App.makeToast(getString(R.string.purchase_cancelled));
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            billingClient.queryPurchaseHistoryAsync(QueryPurchaseHistoryParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP).build(), (billingResult1, purchasesList) -> {
                if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    for (PurchaseHistoryRecord purchase : purchasesList) {
                        if (productId.equals(purchase.getProducts().get(0))) {
                            handlePurchase(purchase.getPurchaseToken());
                            break;
                        }
                    }
                }
            });
        } else {
            Timber.e("onPurchasesUpdated: %s", billingResult.getResponseCode());
        }
    }

    void handlePurchase(String purchaseToken) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchaseToken).build();
        billingClient.consumeAsync(consumeParams, (billingResult, token) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                // Handle success
            }
        });
    }

    private void payInApp(Purchase purchase) {
        if (NetworkReceiver.isConnected()) {
            SubscriptionRequest request = new SubscriptionRequest();
            request.setUserId(GetSet.getUserId());
            request.setMembershipId(productId);
            request.setPaidAmount(purchaseCurrency + " " + purchasePrice);
            request.setTransactionId(purchase.getOrderId());
            Call<SubscriptionResponse> call = apiInterface.paySubscription(request);
            call.enqueue(new Callback<SubscriptionResponse>() {
                @Override
                public void onResponse(Call<SubscriptionResponse> call, Response<SubscriptionResponse> response) {
                    if (response.body() != null && response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        SubscriptionResponse subscription = response.body();
                        mainHandler.post(() -> App.makeToast(getString(R.string.subscribed_successfully)));

                        GetSet.setPremiumMember(subscription.getPremiumMember());
                        GetSet.setPremiumExpiry(subscription.getPremiumExpiryDate());
                        GetSet.setGems(subscription.getAvailableGems() != null ? Float.parseFloat(subscription.getAvailableGems()) : 0);
                        GetSet.setOncePurchased(true);
                        SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                        SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                        SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                        SharedPref.putBoolean(SharedPref.ONCE_PAID, GetSet.isOncePurchased());
                        billingClient.endConnection();
                        resultCode = RESULT_OK;
                        setResult(resultCode);
                        finish();
                    }
                }
                @Override
                public void onFailure(Call<SubscriptionResponse> call, Throwable t) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkReceiver();
        super.onDestroy();
    }

    public class PrimeAdapter extends PagerAdapter {
        private final Context context;
        private List<PrimeBenefit> primeList;

        public PrimeAdapter(Context context, List<PrimeBenefit> primeList) {
            this.context = context;
            this.primeList = primeList;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(context);
            if (primeList.get(position) == null) {
                PlaceholderItemPrimeBinding placeholderBinding = PlaceholderItemPrimeBinding.inflate(inflater, container, false);
                container.addView(placeholderBinding.getRoot());
                return placeholderBinding.getRoot();
            } else {
                ItemPrimeBinding itemBinding = ItemPrimeBinding.inflate(inflater, container, false);
                final PrimeBenefit prime = primeList.get(position);

                itemBinding.tvTitle.setText(prime.getTitle());
                itemBinding.tvDescription.setText(prime.getDescription());
                itemBinding.tvDescription.setMovementMethod(new ScrollingMovementMethod());
                Glide.with(context)
                        .load(Constants.PRIME_IMAGE_URL + prime.getImage())
                        .centerInside()
                        .placeholder(R.drawable.gift_primary)
                        .into(itemBinding.sliderImage);

                container.addView(itemBinding.getRoot());
                return itemBinding.getRoot();
            }
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return primeList.size();
        }
    }

    public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.MyViewHolder> {
        private final Context context;
        private List<MembershipPackages> packageList;

        public PackageAdapter(Context context, List<MembershipPackages> packageList) {
            this.context = context;
            this.packageList = packageList;
        }

        public void setPackageList(List<MembershipPackages> packageList) {
            this.packageList = packageList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemPrimePackBinding itemBinding = ItemPrimePackBinding.inflate(LayoutInflater.from(context), parent, false);
            return new MyViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            MembershipPackages pack = packageList.get(position);
            holder.binding.itemLay.setChecked(position == selectedPosition);
            int textColor;
            if (position == selectedPosition) {
                textColor = ResourcesCompat.getColor(getResources(), R.color.white, null);
                holder.binding.viewDecor.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.primary_decor, null));
                holder.animateDecor();
            } else {
                textColor = ResourcesCompat.getColor(getResources(), R.color.colorPrimaryText, null);
                holder.binding.viewDecor.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.grey_decor, null));
            }
            holder.binding.txtPrimeTitle.setTextColor(textColor);
            holder.binding.txtPrimePrice.setTextColor(textColor);

            holder.binding.txtPrimePrice.setText(pack.getSubsPrice());
            holder.binding.txtPrimeTitle.setText(pack.getSubsTitle());
            holder.binding.txtPrimeValidity.setText(AppUtils.getPackageTitle(pack.getSubsValidity(), context));
        }

        @Override
        public int getItemCount() {
            return packageList.size();
        }

        public List<MembershipPackages> getPackageList() {
            return packageList;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private final ItemPrimePackBinding binding;

            public MyViewHolder(ItemPrimePackBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) binding.itemLay.getLayoutParams();
                params.width = (int) (displayWidth * 0.35);
                binding.itemLay.setLayoutParams(params);

                binding.itemLay.setOnClickListener(v -> {
                    int oldPosition = selectedPosition;
                    selectedPosition = getAbsoluteAdapterPosition();
                    notifyItemChanged(oldPosition);
                    notifyItemChanged(selectedPosition);
                });
            }

            public void animateDecor() {
                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, binding.viewDecor.getMeasuredWidth());
                valueAnimator.setDuration(500L);
                valueAnimator.setInterpolator(new DecelerateInterpolator());
                valueAnimator.addUpdateListener(anim -> {
                    ViewGroup.LayoutParams lp = binding.viewDecor.getLayoutParams();
                    lp.width = (int) anim.getAnimatedValue();
                    binding.viewDecor.setLayoutParams(lp);
                });
                valueAnimator.start();
            }
        }
    }
}
