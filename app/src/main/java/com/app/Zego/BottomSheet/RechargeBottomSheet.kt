package com.app.Zego.BottomSheet

import android.content.Context
import android.icu.util.TimeZone
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.app.Zego.Adapter.GemsAdapter
import com.app.addon.livestream.external.helper.NetworkReceiver
import com.app.addon.livestream.external.utils.ApiClient
import com.app.addon.livestream.external.utils.ApiInterface
import com.app.addon.livestream.external.utils.Constants
import com.app.alhawanajd.App
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.RechargeDialogBinding
import com.app.model.GemsPurchaseRequest
import com.app.model.GemsPurchaseResponse
import com.app.model.GemsStoreResponse
import com.app.model.GetSet
import com.app.model.ProfileRequest
import com.app.model.ProfileResponse
import com.app.utils.SharedPref
import com.app.utils.api.CommonApi
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class RechargeBottomSheet : BottomSheetDialogFragment(), PurchasesUpdatedListener {

    override fun getTheme(): Int = R.style.SimpleBottomDialog1

    private lateinit var binding: RechargeDialogBinding
    private lateinit var gemsAdapter: GemsAdapter
    private val gemsList = ArrayList<GemsStoreResponse.GemsList>()
    private var currentPage = 0
    private val limit = 10
    var apiInterface: ApiInterface? = null
    private lateinit var billingClient: BillingClient
    private var purchaseSku: String = ""
    private var purchasePrice: String = ""
    private var purchaseCurrency: String = ""
    private val VIEW_TYPE_ITEM: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RechargeDialogBinding.inflate(inflater, container, false)
        apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        billingClient = BillingClient.newBuilder(requireContext())
            .enablePendingPurchases()
            .setListener(this)  // because your Fragment implements PurchasesUpdatedListener
            .build()
        setupRecycler()
        getProfile(requireContext(), GetSet.getUserId())
        loadGems(currentPage)

        return binding.root
    }

    private fun setupRecycler() {
        gemsAdapter = GemsAdapter(requireContext(), gemsList){ gem ->
            // Called when an item is clicked
            onGemSelected(gem)
        }
        binding.gemRV.apply {
         /*   layoutManager = LinearLayoutManager(requireContext())
            adapter = gemsAdapter*/
            layoutManager = GridLayoutManager(requireContext(), 2) // ✅ 2 columns
            adapter = gemsAdapter
        }
    }

    private fun onGemSelected(gem: GemsStoreResponse.GemsList) {
        val purchaseSku = gem.gemTitle
        initBilling(true, purchaseSku)
    }

    private fun initBilling(showAlert: Boolean, purchaseSku: String) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val productList = gemsList.mapNotNull { gems ->
                        gems.gemTitle?.let {
                            QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(it)
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build()
                        }
                    }

                    val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
                        .setProductList(productList)
                        .build()

                    billingClient.queryProductDetailsAsync(queryProductDetailsParams) { result, productDetailsList ->
                        Timber.tag("Rechargebottomsheet").i("skuDetailsList: %s", Gson().toJson(productDetailsList))

                        if (showAlert) {
                            productDetailsList.forEach { productDetails ->
                                val sku = productDetails.productId
                                val price = productDetails.oneTimePurchaseOfferDetails?.formattedPrice
                                val currency = productDetails.oneTimePurchaseOfferDetails?.priceCurrencyCode

                                if (purchaseSku == sku) {
                                    purchasePrice = price ?: ""
                                    purchaseCurrency = currency ?: ""

                                    val flowParams = BillingFlowParams.newBuilder()
                                        .setProductDetailsParamsList(
                                            listOf(
                                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                                    .setProductDetails(productDetails)
                                                    .build()
                                            )
                                        )
                                        .build()

                                    billingClient.launchBillingFlow(requireActivity(), flowParams)
                                }
                            }
                        } else {
                            productDetailsList.forEach { productDetails ->
                                val currency = productDetails.oneTimePurchaseOfferDetails?.priceCurrencyCode
                                purchaseCurrency = currency ?: ""

                                gemsList.firstOrNull { list ->
                                    list.viewType == VIEW_TYPE_ITEM &&
                                            list.gemTitle == productDetails.productId
                                }?.gemPrice = productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                            }
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Handle reconnect logic here if needed
            }
        })
    }

    private fun loadGems(offset: Int) {
        if (!NetworkReceiver.isConnected()) return


        val call = apiInterface?.getGemsList(
            GetSet.getAuthToken(),
            GetSet.getUserId(),
            Constants.TAG_ANDROID,
            offset * limit,
            limit
        )
        Log.e("Gemlist", "onRequest: $limit ," + offset  );

        call?.enqueue(object : Callback<GemsStoreResponse> {
            override fun onResponse(
                call: Call<GemsStoreResponse>,
                response: Response<GemsStoreResponse>
            ) {
                Log.e("Gemlist", "onResponse: " + Gson().toJson(response.body()));
                if (offset == 0) gemsList.clear()
                response.body()?.let { res ->
                    if (res.status == Constants.TAG_TRUE) {
                        binding.gemRV.visibility = View.VISIBLE
                        gemsList.addAll(res.gemsList)
                        gemsAdapter.notifyDataSetChanged()
                    }else{
                        Toast.makeText(requireContext(), res.message, Toast.LENGTH_SHORT).show()
                        binding.gemRV.visibility = View.GONE
                        dismiss()
                    }
                }
            }

            override fun onFailure(call: Call<GemsStoreResponse>, t: Throwable) {
                call.cancel()
            }
        })
    }

    fun getProfile(context: Context, profileId: String) {
        val request = ProfileRequest().apply {
            userId = GetSet.getUserId()
            this.profileId = profileId
        }

        Timber.d("getProfile: %s", Gson().toJson(request))

        val apiInterface = ApiClient.getClient().create(ApiInterface::class.java)
        val call = apiInterface.getProfile(request)

        call.enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(
                call: Call<ProfileResponse>,
                response: Response<ProfileResponse>
            ) {
                try {
                    val body = response.body()
                    if (response.isSuccessful && body != null && body.status == Constants.TAG_TRUE) {

                        Timber.d("getProfile: %s", Gson().toJson(body))
                        binding.gemBalTV.text =
                            "${GetSet.getGems()} ${getString(com.app.alhawanajd.R.string.coins_txt)}"

                        GetSet.setUserId(body.userId)
                        GetSet.setName(body.name)
                        GetSet.setUserName(body.userName)
                        GetSet.setGifts(body.availableGifts?.toLong() ?: 0L)
                        GetSet.setGems(body.availableGems ?: 0f)
                        GetSet.setVideos(body.videosCount?.toLong() ?: 0L)
                        GetSet.setGiftEarnings(body.giftEarnings?.toString())
                        GetSet.setPaypal_id(body.paypalId)

                        SharedPref.putLong(SharedPref.GIFTS, GetSet.getGifts())
                        SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems())
                        SharedPref.putLong(SharedPref.VIDEOS, GetSet.getVideos())
                        SharedPref.putString(SharedPref.GIFT_EARNINGS, GetSet.getGiftEarnings())
                        SharedPref.putString(SharedPref.PAYPAL_ID, GetSet.getPaypal_id())
                    }

                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                Log.e("Profile", "getProfile: ${t.message}")
                call.cancel()
            }
        })
    }



    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchaseSku == purchase.products[0]) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            payInApp(purchase)
                            handlePurchase(purchase.purchaseToken)
                        }
                        return  // stop after handling
                    }
                }
            }

            BillingClient.BillingResponseCode.USER_CANCELED -> {
                App.makeToast(getString(R.string.purchase_cancelled))
            }

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                val params = QueryPurchaseHistoryParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()

                billingClient.queryPurchaseHistoryAsync(params) { result, purchasesList ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        purchasesList?.forEach { purchase ->
                            if (purchaseSku == purchase.products[0]) {
                                handlePurchase(purchase.purchaseToken)
                                return@forEach
                            }
                        }
                    }
                }
            }

            else -> {
                // Handle other cases if needed
            }
        }
    }

    private fun handlePurchase(purchaseToken: String) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        val listener = ConsumeResponseListener { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Item consumed successfully
            }
        }

        billingClient.consumeAsync(consumeParams, listener)
    }

    private fun payInApp(purchase: Purchase) {
        if (NetworkReceiver.isConnected()) {
            val request = GemsPurchaseRequest().apply {
                userId = GetSet.getUserId()
                gemId = purchaseSku
                paidAmount = purchasePrice  // or "$purchaseCurrency$purchasePrice" if needed
                transactionId = purchase.orderId
                purchase_type = "inapp"
                sdk_token = ""
                purchaseToken = purchase.purchaseToken
                try {
                    time_zone = TimeZone.getDefault().id
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val call = apiInterface?.androidverifyReceipt(request)
            call?.enqueue(object : Callback<GemsPurchaseResponse> {
                override fun onResponse(
                    call: Call<GemsPurchaseResponse>,
                    response: Response<GemsPurchaseResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val purchaseResponse = response.body()!!
                        if (purchaseResponse.status == Constants.TAG_TRUE) {
                            GetSet.setGems(purchaseResponse.availableGems.toFloat())
                            SharedPref.putFloat(SharedPref.GEMS, GetSet.getGems())
                            /*binding.gemBalTV.text =
                                "${GetSet.getGems()} ${getString(R.string.coins)}"*/
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.coins_updated),
                                Toast.LENGTH_SHORT
                            ).show()
                            getProfile(requireContext(), GetSet.getUserId())
                        }
                    }
                }

                override fun onFailure(call: Call<GemsPurchaseResponse>, t: Throwable) {
                    // Handle failure if needed
                }
            })
        }
    }

}
