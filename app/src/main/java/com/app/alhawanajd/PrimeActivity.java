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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import com.app.alhawanajd.databinding.ActivityPrimeBinding;
import com.app.alhawanajd.databinding.ItemPrimeBinding;
import com.app.alhawanajd.databinding.ItemPrimePackTwoBinding;
import com.app.alhawanajd.databinding.PlaceholderItemPrimeBinding;
import com.app.model.PackageModel;
import com.bumptech.glide.Glide;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.app.alhawanajd.R;
import com.app.external.LoopViewPager;
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
import com.app.view.CheckableCardView;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

@SuppressLint("NonConstantResourceId")
public class PrimeActivity extends BaseFragmentActivity implements PurchasesUpdatedListener {

    private static final String TAG = PrimeActivity.class.getSimpleName();
    int displayHeight, displayWidth;
    private ActivityPrimeBinding binding;

    private PrimeAdapter primeAdapter;
    private SlidrInterface sliderInterface;
    // create new Person
    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;
    private String productId = "", purchasePrice = "", purchaseCurrency = "", transactionId = "";
    // create new Person
    private BillingClient billingClient;
    ApiInterface apiInterface;
    int resultCode = Activity.RESULT_CANCELED;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    PackageAdapter packageAdapter;
    int selectedPosition = -1;
    List<MembershipPackages> productList = new ArrayList<>();
    NewPackageAdapter newPackageAdapter;
    List<PackageModel> productListNew = new ArrayList<>();

    int slidelist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        binding = ActivityPrimeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        billingClient = BillingClient.newBuilder(PrimeActivity.this).enablePendingPurchases().setListener(this).build();
        initView();


       // commented on jan8 getPrimeDetails();

    }

    private void initView() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayHeight = displayMetrics.heightPixels;
        displayWidth = displayMetrics.widthPixels;


        LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearParams.width = displayWidth;
        linearParams.height = (int) (displayHeight * 0.25);
        binding.looperViewpager.setClipToPadding(true);
        binding.looperViewpager.setPadding(40, 40, 40, 0);
        binding.looperViewpager.setLayoutParams(linearParams);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .build();
        sliderInterface = Slidr.attach(this, config);

        productListNew.add(new PackageModel("Annual", "$12.30/month"));
        productListNew.add(new PackageModel("3 Month", "$3.30/month"));
        productListNew.add(new PackageModel("6 Month", "$6.30/month"));
        productListNew.add(new PackageModel("9 Month", "$9.30/month"));

        newPackageAdapter = new NewPackageAdapter(PrimeActivity.this, productListNew);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(newPackageAdapter);
        binding.recyclerView.setHasFixedSize(true);

        if (LocaleManager.isRTL()) {
            binding.btnBack.setRotation(180);
        } else {
            binding.btnBack.setRotation(0);
        }

        binding.btnBack.setVisibility(View.VISIBLE);
     //   txtSubTitle.setVisibility(View.GONE);
        binding.txtTitle.setTextColor(getResources().getColor(R.color.colorWhite));
        binding.txtTitle.setText(getString(R.string.prime));

        /*Init Prime View*/
        /*txtPrimeTitle.setText(AppUtils.getPrimeTitle(getApplicationContext()));
        txtPrice.setText(AppUtils.getPrimeContent(getApplicationContext()));
        premiumLay.setVisibility(GetSet.getPremiumMember().equals(Constants.TAG_TRUE) ? View.VISIBLE : View.GONE);
        btnSubscribe.setVisibility(!GetSet.getPremiumMember().equals(Constants.TAG_TRUE) ? View.VISIBLE : View.GONE);*/

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (packageAdapter != null && packageAdapter.getItemCount() > 0) {
                    productId = packageAdapter.getPackageList().get(selectedPosition).getSubsProductId();
                }
                initBilling(true);

            }
        });

        binding.looperViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position > 0) {
                    sliderInterface.lock();
                } else {

                    sliderInterface.unlock();
                }

                if(slidelist==1){
                    sliderInterface.unlock();
                    (binding.looperViewpager).lock();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dx > 0) {
                    sliderInterface.lock();
                } else sliderInterface.unlock();
            }
        });
       /* commented on jan8 initPackageAdapter();
        initBilling(false);
        loadAd();*/
    }

    private void loadAd() {
//        BannerAdUtils.loadBannerAd(this, (FrameLayout) findViewById(R.id.adLay));
    }


    private void getPrimeDetails() {
        List<PrimeBenefit> primeList = new ArrayList<>();
        if (AdminData.primeDetails.getPrimeBenefits() != null && AdminData.primeDetails.getPrimeBenefits().size() > 0) {
            primeList.addAll(AdminData.primeDetails.getPrimeBenefits());
        }

        slidelist= primeList.size();
        if (primeList.size() == 0) {
            binding.pagerIndicator.setVisibility(View.GONE);
            primeList = getPlaceHolderList();
        } else {
            //pagerIndicator.setVisibility(View.VISIBLE);
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
        AppUtils.showSnack(this, findViewById(R.id.parentLay), isConnected);
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
                    // The billing client is ready. You can query purchases here.
                    List<QueryProductDetailsParams.Product> tempList = new ArrayList<>();
                    for (MembershipPackages membershipPackages : AdminData.membershipList) {
                        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(membershipPackages.getSubsProductId())
                                .setProductType(BillingClient.ProductType.INAPP).build();
                        tempList.add(product);
                    }
                    QueryProductDetailsParams queryProductDetailsParams = null;
                    if (tempList.size() > 0) {
                        queryProductDetailsParams = QueryProductDetailsParams.newBuilder().setProductList(tempList).build();
                        billingClient.queryProductDetailsAsync(queryProductDetailsParams,
                                new ProductDetailsResponseListener() {
                                    @Override
                                    public void onProductDetailsResponse(BillingResult billingResult, List<ProductDetails> productDetailsList) {
                                        // Process the result.
                                        // Timber.tag(TAG).i("skuDetailsList: %s", new Gson().toJson(productDetailsList));
                                        if (openDialog) {
                                            for (ProductDetails productDetails : productDetailsList) {
                                                String sku = productDetails.getProductId();
                                                String price = productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
                                                String currency = productDetails.getOneTimePurchaseOfferDetails().getPriceCurrencyCode();
                                                /*if (purchaseSku.equals(sku) && BuildConfig.DEBUG) {*/
                                                if (productId.equals(sku)) {
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
                                                    billingClient.launchBillingFlow(PrimeActivity.this, flowParams);
                                                }

                                            }
                                        } else {
                                            for (ProductDetails productDetails : productDetailsList) {
                                                purchaseCurrency = productDetails.getOneTimePurchaseOfferDetails().getPriceCurrencyCode();
                                                for (MembershipPackages membershipPackages : AdminData.membershipList) {
                                                    if (productDetails.getProductId().equals(membershipPackages.getSubsProductId())) {
                                                        membershipPackages.setSubsPrice(productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice());
                                                        productList.add(membershipPackages);
                                                        break;
                                                    }
                                                }
                                            }
                                            mainHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    packageAdapter.notifyDataSetChanged();
                                                }
                                            });

                                            if (productList.size() > 0) {
                                                binding.btnSubscribe.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }
                                });
                    } else {
                        if (openDialog) {
                            App.makeToast(getString(R.string.package_empty_description));
                        }
                    }
                } else {
                    Timber.i("onSkuDetailsResponse %s", "else calling");
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Timber.i("onSkuDetailsResponse %s", "onBillingServiceDisconnected");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    private void initPackageAdapter() {
        Timber.d("initPackageAdapter: %s", new Gson().toJson(productList));
        packageAdapter = new PackageAdapter(PrimeActivity.this, productList);
        binding.recyclerView.setAdapter(packageAdapter);
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                packageAdapter.notifyDataSetChanged();
            }
        });

        if (productList.size() > 0) {
            binding.btnSubscribe.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                if (productId.equals(purchase.getProducts().get(0))) {
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
                            if (productId.equals(purchase.getProducts().get(0))) {
                                handlePurchase(purchase.getPurchaseToken());
                                break;
                            }
                        }
                    }
                }
            });
        } else {
            // Handle any other error codes.
            Timber.e("onPurchasesUpdated: %s", billingResult.getResponseCode());
        }
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
                    SubscriptionResponse subscription = response.body();
                    if (response.body().getStatus().equals(Constants.TAG_TRUE)) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                App.makeToast(getString(R.string.subscribed_successfully));
                            }
                        });

                        GetSet.setPremiumMember(subscription.getPremiumMember());
                        GetSet.setPremiumExpiry(subscription.getPremiumExpiryDate());
                        GetSet.setGems(subscription.getAvailableGems() != null ? Float.parseFloat(subscription.getAvailableGems()) : 0);
                        GetSet.setOncePurchased(true);
                        SharedPref.putString(SharedPref.IS_PREMIUM_MEMBER, GetSet.getPremiumMember());
                        SharedPref.putString(SharedPref.PREMIUM_EXPIRY, GetSet.getPremiumExpiry());
                        SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems());
                        SharedPref.putBoolean(SharedPref.ONCE_PAID, GetSet.isOncePurchased());
                        billingClient.endConnection();
                        resultCode = Activity.RESULT_OK;
                        setResult(resultCode);
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<SubscriptionResponse> call, Throwable t) {

                }
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
        private List<PrimeBenefit> primeList = new ArrayList<>();
        private final LayoutInflater inflater;

        public PrimeAdapter(Context context, List<PrimeBenefit> primeList) {
            this.context = context;
            this.primeList = primeList;
            // It's more efficient to get the inflater once.
            this.inflater = LayoutInflater.from(context);
        }
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View itemView;
            // Use View Binding to inflate the correct layout
            if (primeList.get(position) == null) {
                // Inflate placeholder layout
                PlaceholderItemPrimeBinding placeholderBinding = PlaceholderItemPrimeBinding.inflate(inflater, container, false);
                container.addView(placeholderBinding.getRoot());
                return placeholderBinding.getRoot();
            } else {
                // Inflate the main item layout
                ItemPrimeBinding binding = ItemPrimeBinding.inflate(inflater, container, false);

                // The ButterKnife.bind() call is removed.

                final PrimeBenefit prime = primeList.get(position);
                Timber.d("instantiateItem: %s", new Gson().toJson(prime));

                // Access views through the binding object
                binding.tvTitle.setText(prime.getTitle());
                binding.tvDescription.setText(prime.getDescription());
                binding.tvDescription.setMovementMethod(new ScrollingMovementMethod());

                Glide.with(context)
                        .load(Constants.PRIME_IMAGE_URL + prime.getImage())
                        .centerInside()
                        .placeholder(R.drawable.gift_primary)
                        .into(binding.sliderImage);

                // Add the inflated view's root to the container
                container.addView(binding.getRoot());
                return binding.getRoot();
            }
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((ViewGroup) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public int getCount() {
            return primeList.size();
        }

    }

    public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.MyViewHolder> {
        private final Context context;
        private final List<MembershipPackages> packageList;

        public PackageAdapter(Context context, List<MembershipPackages> packageList) {
            this.context = context;
            this.packageList = packageList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the layout using View Binding
            ItemPrimePackTwoBinding binding = ItemPrimePackTwoBinding.inflate(LayoutInflater.from(context), parent, false);
            return new MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            MembershipPackages pack = packageList.get(position);

            // Use the binding object from the holder to access views
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
        }

        @Override
        public int getItemCount() {
            return packageList.size();
        }

        public List<MembershipPackages> getPackageList() {
            return packageList;
        }

        // This is the updated MyViewHolder for PackageAdapter
        public class MyViewHolder extends RecyclerView.ViewHolder {
            // Hold the binding object
            private final ItemPrimePackTwoBinding binding;

            public MyViewHolder(ItemPrimePackTwoBinding binding) {
                super(binding.getRoot());
                this.binding = binding; // Store the binding

                // Set layout params programmatically
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) binding.itemLay.getLayoutParams();
                params.width = (int) (displayWidth * 0.35);
                binding.itemLay.setLayoutParams(params);

                // Set click listener
                binding.itemLay.setOnClickListener(v -> {
                    selectedPosition = getAbsoluteAdapterPosition();
                    notifyDataSetChanged(); // Use adapter's notifyDataSetChanged()
                });
            }

            public void animateDecor() {
                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, binding.viewDecor.getMeasuredWidth());
                valueAnimator.setDuration(500L);
                valueAnimator.setInterpolator(new DecelerateInterpolator());
                valueAnimator.addUpdateListener(anim ->
                {
                    ViewGroup.LayoutParams lp = binding.viewDecor.getLayoutParams();
                    lp.width = (int) anim.getAnimatedValue();
                    binding.viewDecor.setLayoutParams(lp);
                    Timber.d("Animated: val %d", (int) anim.getAnimatedValue());
                });
                valueAnimator.start();
            }
        }
    }

    public class NewPackageAdapter extends RecyclerView.Adapter<NewPackageAdapter.MyViewHolder> {
        private final Context context;
        private final List<PackageModel> packageList;

        public NewPackageAdapter(Context context, List<PackageModel> packageList) {
            this.context = context;
            this.packageList = packageList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate the layout using View Binding
            ItemPrimePackTwoBinding binding = ItemPrimePackTwoBinding.inflate(LayoutInflater.from(context), parent, false);
            return new MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            PackageModel pack = packageList.get(position);

            // Use the binding object from the holder to access views
            holder.binding.txtPrimeTitle.setText(pack.getName());
            holder.binding.txtPrimePrice.setText(pack.getPrice());

            // Divider logic
            if (position == packageList.size() - 1) {
                holder.binding.viewDecor.setVisibility(View.GONE);
            } else {
                holder.binding.viewDecor.setVisibility(View.VISIBLE);
            }

            // RadioButton selection logic
            holder.binding.radioButtonPackage.setChecked(selectedPosition == position);

            // Click listeners for selection
            holder.itemView.setOnClickListener(v -> {
                selectedPosition = holder.getAbsoluteAdapterPosition();
                notifyDataSetChanged();
            });

            holder.binding.radioButtonPackage.setOnClickListener(v -> {
                selectedPosition = holder.getAbsoluteAdapterPosition();
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return packageList.size();
        }

        public List<PackageModel> getPackageList() {
            return packageList;
        }

        // This is the updated MyViewHolder for NewPackageAdapter
        public class MyViewHolder extends RecyclerView.ViewHolder {
            // Hold the binding object
            private final ItemPrimePackTwoBinding binding;

            public MyViewHolder(ItemPrimePackTwoBinding binding) {
                super(binding.getRoot());
                this.binding = binding; // Store the binding
                // No more ButterKnife.bind() needed
            }
        }
    }

}



