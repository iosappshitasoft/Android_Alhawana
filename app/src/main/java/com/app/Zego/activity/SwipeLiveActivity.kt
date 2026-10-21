package com.app.Zego.pkbattle.activity

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.app.Zego.Adapter.SwipeLiveAdapter
import com.app.Zego.Additional.VerticalSlidePageTransformer
import com.app.Zego.ApiBase.ZegoApiClient
import com.app.Zego.CallBacks.JoinCoHost
import com.app.Zego.CallBacks.RemoveEndList
import com.app.Zego.Fragment.StreamListFragment
import com.app.Zego.Model.LiveStreamRequest1
import com.app.Zego.internal.ZEGOLiveStreamingManager
import com.app.addon.livestream.external.helper.NetworkReceiver
import com.app.addon.livestream.external.model.LiveStreamRequest
import com.app.addon.livestream.external.model.LiveStreamResponse
import com.app.addon.livestream.external.model.StreamDetails
import com.app.addon.livestream.external.utils.ApiInterface
import com.app.addon.livestream.external.utils.StreamConstants
import com.app.alhawanajd.App
import com.app.alhawanajd.BaseActivity
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.ActivitySwipeLiveBinding
import com.app.external.MediaPlayerUtils
import com.app.model.GetSet
import com.app.utils.AppUtils
import com.app.utils.Constants
import com.app.utils.SharedPref
import com.app.utils.api.CommonApi
import com.bumptech.glide.Glide
import com.cashgate.chobi.Zego.callbacks.FragmentReloadCallback

import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.sortBy
import kotlin.coroutines.Continuation

import kotlin.text.compareTo
import kotlin.text.replace
import kotlin.times
import kotlin.toString

class SwipeLiveActivity : BaseActivity(), JoinCoHost, RemoveEndList {

    val bind: ActivitySwipeLiveBinding by lazy { ActivitySwipeLiveBinding.inflate(layoutInflater) }
    var apiInterface: ApiInterface? = null
    var payload = false
    private var lastPosition = 0
    companion object {
        val streamLists = ArrayList<StreamDetails>()
        var drawerLayout: DrawerLayout? = null
        var hostId = ""
    }

    private var appUtils: AppUtils? = null
    private val fragmentList = mutableListOf<Fragment>()
    private lateinit var adapter: SwipeLiveAdapter
    private var isLoading = false
    private var currentPage = 0
    private val maxLimit = StreamConstants.MAX_LIMIT
    private lateinit var fragmentReloadCallback: FragmentReloadCallback
    var liveID = ""
    var hostId = ""
    var hostImage = ""


    // First, reorder the list to put the matching stream at position 0
    val reorderedList = mutableListOf<StreamDetails>()

    override fun onResume() {
        super.onResume()
        App.setCurrentActivity(this)
      //  App.showFloatingIcon(getApplicationContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bind.root)
        apiInterface = ZegoApiClient.getClient(this).create(ApiInterface::class.java)
        bind.progressLay.visibility = View.VISIBLE
        adapter = SwipeLiveAdapter(this@SwipeLiveActivity, bind.viewPager)
        bind.viewPager.adapter = adapter
        bind.viewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        bind.viewPager.setPageTransformer(VerticalSlidePageTransformer())
        appUtils = AppUtils(this)
        try {
            appUtils?.pauseExternalAudio()
            if (MediaPlayerUtils.isPlaying()) {
                MediaPlayerUtils.pauseMediaPlayer()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val slideFragment = StreamListFragment()
        fragmentReloadCallback = slideFragment

        supportFragmentManager.beginTransaction()
            .replace(R.id.right_drawer, slideFragment)
            .commit()

        drawerLayout = bind.drawerMain


        liveID = intent.getStringExtra(StreamConstants.TAG_STREAM_NAME).toString()
        hostId = intent.getStringExtra("hostId").toString()
        hostImage = intent.getStringExtra("hostImage").toString()

        if(hostImage.isNotEmpty()){
            Glide.with(applicationContext)
                .load(hostImage)
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_launcher)
                .into(bind.streamHostImage)
        }


        bind.btnBack.setOnClickListener { finish() }
        bind.btnBack.visibility = View.VISIBLE

        val customHelper = CustomViewPagerHelper(bind.viewPager)
        customHelper.enforceScrollDelay()



        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (closeRightSlider()) {
                    return
                }
                finish()
            }
        })


        bind.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //newly added
                if (position != lastPosition) {
                    val previousFragment = adapter.getFragmentAt(lastPosition)
                    previousFragment?.unSubscribeStream(GetSet.getUserId())
                }
                lastPosition = position


                // Load more data if reached end
                if (position in reorderedList.indices) {
                    if (position - 1 < reorderedList.size) {
                        Glide.with(applicationContext)
                            .load(reorderedList[position].publisherImage)
                            .placeholder(R.drawable.ic_launcher)
                            .error(R.drawable.ic_launcher)
                            .into(bind.streamHostImage)
                    } else {
                        Log.e("GlideLoad", "Invalid position: $position for list size: ${reorderedList.size}")
                    }

                } else {
                    Log.e("GlideError", "Invalid position: $position for list size: ${reorderedList.size}")
                }
                if (!isLoading && position == fragmentList.size - 1) {
                    currentPage++
                    getLiveStreams(currentPage)
                }

                if (ZEGOLiveStreamingManager.getInstance()
                        .isHost(GetSet.getUserId()) || ZEGOLiveStreamingManager.getInstance()
                        .isCoHost(GetSet.getUserId())
                ) {
                    bind.viewPager.isUserInputEnabled = false
                    bind.drawerMain.closeDrawer(GravityCompat.END) // to close the right drawer
                    bind.drawerMain.setDrawerLockMode(
                        DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                        GravityCompat.END
                    );
                } else {
                    bind.viewPager.isUserInputEnabled = true
                }
            }


            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (ZEGOLiveStreamingManager.getInstance().isCoHost(GetSet.getUserId())) {
                    Log.e("Hiiiiiiii", "check onPageScrolled: :::::1")
                }

                when (state) {
                    ViewPager2.SCROLL_STATE_DRAGGING -> {
                        if (ZEGOLiveStreamingManager.getInstance()
                                .isHost(GetSet.getUserId()) || ZEGOLiveStreamingManager.getInstance()
                                .isCoHost(GetSet.getUserId())
                        ) {
                            bind.viewPager.isUserInputEnabled = false
                            bind.drawerMain.closeDrawer(GravityCompat.END) // to close the right drawer
                        } else {
                            bind.viewPager.isUserInputEnabled = true
                        }
                    }

                    ViewPager2.SCROLL_STATE_IDLE -> {
                        lifecycleScope.launch {
                            delay(2000)
                            bind.progressLay.visibility = View.GONE
                            bind.viewPager.visibility = View.VISIBLE
                        }
                    }

                    ViewPager2.SCROLL_STATE_SETTLING -> {
                        lifecycleScope.launch {
                            bind.viewPager.visibility = View.GONE
                            bind.progressLay.visibility = View.VISIBLE
                            if (currentPage != 0 && streamLists.size != currentPage)
                                payload = false
                        }
                    }
                }
            }
        })

        getLiveStreams(currentPage)

    }

    override fun onNetworkChanged(isConnected: Boolean) {

    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val edgeSize = 50 * resources.displayMetrics.density // px
            if (event.x < edgeSize) {
                Log.d("Gesture", "User started touch near left edge (maybe back gesture)")
                //bind.drawerMain.openDrawer(GravityCompat.START)
            } else if (event.x > (resources.displayMetrics.widthPixels - edgeSize)) {
                Log.d("Gesture", "User started touch near right edge")
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun getLiveStreams(offset: Int) {
        if (isLoading) return // prevent multiple calls
        isLoading = true
        if (NetworkReceiver.isConnected()) {
            val request = LiveStreamRequest().apply {
                userId = SharedPref.getString(SharedPref.USER_ID, "")
                profileId = SharedPref.getString(SharedPref.USER_ID, "")
                sortBy = StreamConstants.TAG_RECENT
                limit = maxLimit.toString()
                this.offset = (maxLimit * offset).toString()
                type = StreamConstants.TAG_LIVE
            }
            Log.e("SwipeLiveActivity", "getLiveStreamsParams: ${Gson().toJson(request)}")

            val call = apiInterface?.getCurrentStreams(request)
            call?.enqueue(object : Callback<LiveStreamResponse> {
                override fun onResponse(
                    call: Call<LiveStreamResponse>,
                    response: Response<LiveStreamResponse>
                ) {
                    isLoading = false
                    Log.e("SwipeLiveActivity" , "Response succ: " + Gson().toJson(response.body()))
                    if (response.isSuccessful && response.body()?.status == Constants.TAG_TRUE) {
                        val data = response.body()
                        if(offset == 0) {
                            reorderedList.clear()
                            Log.e("SwipeLiveActivity", "=========: ${offset}")
                        }
                        data?.result?.let {
                            if (it.isNotEmpty()) {
                                streamLists.addAll(it)
                                val startIndex = fragmentList.size
                                Log.e("SwipeLiveActivity liveID", "onResponse: " + liveID)
                                Log.e("SwipeLiveActivity hostid", "onResponse: " + hostId)
                                if (liveID.isNotEmpty() && hostId.isNotEmpty()) {
                                    val matchedIndex = it.indexOfFirst { stream ->
                                        stream.name == liveID && stream.publisherId == hostId /*&& stream.streamblock == "false" && stream.streamkickout == "false"*/
                                    }

                                    it.forEachIndexed { index, stream ->
                                        if(matchedIndex <= index/* && stream.streamblock == "false" && stream.streamkickout == "false"*/){
                                            reorderedList.add(stream)
                                        }
                                    }
                                }else {
                                    // Add the rest, excluding the matched one
                                    for (stream in it) {
                                        if (!(stream.name == liveID && stream.publisherId == hostId) /*&&
                                            stream.streamblock == "false" &&
                                            stream.streamkickout == "false"*/
                                        ) {
                                            Log.e("SwipeLiveActivity", "getLiveStreamsresponse else: ${Gson().toJson(stream)}")
                                            reorderedList.add(stream)
                                        }
                                    }
                                }

                                adapter.updateLiveStream(reorderedList)
                                adapter.notifyItemRangeInserted(startIndex, it.size)
                                hideEmptyState()
                            } else {
                                // No more data, you may disable pagination here
                                if (offset == 0) {
                                    showEmptyState(data.message ?: getString(R.string.no_live_streams))
                                }
                            }
                        } ?: run {
                            if (offset == 0) {
                                showEmptyState(data?.message ?: getString(R.string.no_live_streams))
                            }
                        }
                    }
                    else if(response.isSuccessful && response.body()?.status == Constants.TAG_FALSE){
                        // Handle API error
                        reorderedList.clear()
                        streamLists.clear()

                        // 2. Update the adapter to remove static items
                        adapter.updateLiveStream(reorderedList)
                        adapter.notifyDataSetChanged()

                        if (offset == 0) {
                            showEmptyState(response.body()?.message ?: getString(R.string.no_live_streams))
                        }
                    } else if (offset == 0) {
                        showEmptyState(getString(R.string.unable_to_reach_server))
                    }
                }

                override fun onFailure(call: Call<LiveStreamResponse>, t: Throwable) {
                    isLoading = false
                    t.printStackTrace()
                    Log.e("SwipeLiveActivity", "getLiveStreamsfailure: ${t.message}")
                    if (offset == 0) {
                        showEmptyState(getString(R.string.unable_to_reach_server))
                    }
                }
            })
        } else {
            // Handle no network case
            isLoading = false
            if (offset == 0) {
                showEmptyState(getString(R.string.no_internet_connection))
            }
        }
    }

    private fun showEmptyState(message: String) {
        bind.progressLay.visibility = View.GONE
        bind.viewPager.visibility = View.GONE
        bind.emptyLay.visibility = View.VISIBLE
        bind.btnBack.visibility = View.VISIBLE
        bind.emptyText.text = message
    }

    private fun hideEmptyState() {
        bind.emptyLay.visibility = View.GONE
        bind.btnBack.visibility = View.GONE
        bind.viewPager.visibility = View.VISIBLE
    }


    class CustomViewPagerHelper(private val viewPager2: ViewPager2) {
        private val minScrollDelay = 1000L // Minimum delay in milliseconds
        private var lastScrollTime = 0L
        fun enforceScrollDelay() {
            val recyclerView = viewPager2.getChildAt(0) as RecyclerView
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastScrollTime < minScrollDelay) {
                            recyclerView.stopScroll() // Prevents the scroll
                        } else {
                            lastScrollTime = currentTime
                        }
                    }
                }
            })
        }
    }


    fun isGestureNavigationEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val navMode = Settings.Secure.getInt(
                    context.contentResolver, "navigation_mode"
                )
                navMode == 2
            } catch (e: Settings.SettingNotFoundException) {
                false
            }
        } else false
    }

    override fun onDataReceived(success: Boolean) {
        if (success) {
            bind.drawerMain.setDrawerLockMode(
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                GravityCompat.END
            )
            bind.viewPager.isUserInputEnabled = false
            // bind.drawerMain.openDrawer(GravityCompat.END)
        } else {
            bind.drawerMain.setDrawerLockMode(
                DrawerLayout.LOCK_MODE_UNLOCKED,
                GravityCompat.END
            )
            bind.viewPager.isUserInputEnabled = true
        }
    }


    override fun onDataReceived(user_id: String) {
        fragmentReloadCallback.onDataReceived()
        bind.drawerMain.openDrawer(GravityCompat.END)
        bind.drawerMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN)
    }

    fun closeRightSlider(): Boolean {
        if (bind.drawerMain.isDrawerOpen(GravityCompat.END)) {
            bind.drawerMain.setDrawerLockMode(
                DrawerLayout.LOCK_MODE_UNLOCKED,
                GravityCompat.END
            )
            bind.drawerMain.closeDrawer(GravityCompat.END)
            return true
        }
        return false
    }
}
