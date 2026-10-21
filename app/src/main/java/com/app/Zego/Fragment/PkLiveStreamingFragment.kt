package com.app.Zego.Fragment

import android.view.WindowManager
import androidx.core.view.WindowCompat

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.text.TextUtils
import android.util.Log
import android.app.Dialog
import android.view.Window
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.app.Zego.Adapter.CommentsAdapter
import com.app.Zego.Adapter.RoomRequestAdapter
import com.app.Zego.Adapter.ViewerListAdapter
import com.app.Zego.Additional.CustomLayoutManager
import com.app.Zego.Additional.OverlapDecoration
import com.app.Zego.ApiBase.ZegoApiClient
import com.app.Zego.BottomSheet.CommentBottomSheet
import com.app.Zego.BottomSheet.LiveViewersListBtmsheet
import com.app.Zego.BottomSheet.SendGiftBottomSheet
import com.app.Zego.CallBacks.JoinCoHost
import com.app.Zego.CallBacks.OnCommentClickListener
import com.app.Zego.CallBacks.RemoveEndList
import com.app.Zego.CallBacks.onCoHostClicked
import com.app.Zego.Model.GiftList
import com.app.Zego.Model.LeaderBoardUser
import com.app.Zego.Pusher.PusherRepository
import com.app.Zego.Pusher.PusherViewModel
import com.app.Zego.Pusher.PusherViewModelFactory
import com.app.Zego.activity.PkLiveStreamingActivity
import com.app.Zego.internal.ZEGOLiveStreamingManager
import com.app.Zego.internal.business.RoomRequestExtendedData
import com.app.Zego.internal.business.RoomRequestType
import com.app.Zego.internal.business.pk.PKUser
import com.app.Zego.internal.sdk.ZEGOSDKManager
import com.app.Zego.internal.sdk.basic.ZEGOSDKCallBack
import com.app.Zego.internal.sdk.basic.ZEGOSDKUser
import com.app.Zego.internal.sdk.express.IExpressEngineEventHandler
import com.app.Zego.internal.sdk.zim.IZIMEventHandler
import com.app.Zego.internal.sdk.zim.RoomRequestCallback
import com.app.Zego.pkbattle.activity.SwipeLiveActivity
import com.app.addon.livestream.external.helper.LocaleManager
import com.app.addon.livestream.external.helper.NetworkReceiver
import com.app.addon.livestream.external.helper.SocketIO
import com.app.addon.livestream.external.model.ProfileResponse
import com.app.addon.livestream.external.model.StreamDetails
import com.app.addon.livestream.external.utils.ApiInterface
import com.app.addon.livestream.external.utils.StreamConstants
import com.app.alhawanajd.App
import com.app.alhawanajd.MainActivity
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.BottomSheetPkMenuBinding
import com.app.alhawanajd.databinding.PkBottomMenuBarContent1Binding
import com.app.alhawanajd.databinding.PkTestActivityLiveStreamingBinding
import com.app.alhawanajd.home.HomeBottomDialog
import com.app.db.DBHelper
import com.app.helper.AppWebSocket
import com.app.helper.MyFirebaseMessagingService
import com.app.model.ChatResponse
import com.app.model.CommonResponse
import com.app.model.GetSet
import com.app.model.Gift
import com.app.model.ProfileRequest
import com.app.model.StartStreamResponse
import com.app.utils.AdminData
import com.app.utils.AppUtils
import com.app.utils.Constants
import com.app.utils.DevicePref
import com.app.utils.SharedPref
import com.app.utils.Util
import com.app.utils.api.ApiClient
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.pusher.client.connection.ConnectionState
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.ZegoMediaPlayer
import im.zego.zegoexpress.callback.IZegoMediaPlayerEventHandler
import im.zego.zegoexpress.callback.IZegoMediaPlayerLoadResourceCallback
import im.zego.zegoexpress.constants.ZegoAlphaLayoutType
import im.zego.zegoexpress.constants.ZegoMediaPlayerState
import im.zego.zegoexpress.constants.ZegoMultimediaLoadType
import im.zego.zegoexpress.constants.ZegoPublisherState
import im.zego.zegoexpress.constants.ZegoRoomState
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason
import im.zego.zegoexpress.constants.ZegoScenario
import im.zego.zegoexpress.constants.ZegoUpdateType
import im.zego.zegoexpress.constants.ZegoVideoMirrorMode
import im.zego.zegoexpress.entity.ZegoCanvas
import im.zego.zegoexpress.entity.ZegoMediaPlayerResource
import im.zego.zegoexpress.entity.ZegoStream
import im.zego.zegoexpress.entity.ZegoUser
import im.zego.zim.ZIM
import im.zego.zim.enums.ZIMConnectionEvent
import im.zego.zim.enums.ZIMConnectionState
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.Locale
import kotlin.random.Random

class PkLiveStreamingFragment : Fragment(),
    App.AppBackgroundListener,
    SensorEventListener,
    onCoHostClicked,
    SocketIO.SocketEvents,
    View.OnClickListener,
    PurchasesUpdatedListener,
    RoomRequestAdapter.RoomRequestClick,
    OnCommentClickListener {
    // ===== ViewBinding ===F==
    private var _binding: PkTestActivityLiveStreamingBinding? = null
    private val binding get() = _binding!!
    private var TAG_Gifts: String? = "12345 Pklivestreamingactivity gifts"

    // ===== Dependencies =====
    private lateinit var apiInterface: ApiInterface
    private lateinit var apiInterfaceForProfile: ApiInterface
    private lateinit var dbHelper: DBHelper
    private lateinit var viewModel: PusherViewModel

    // ===== Context & System Services =====
    private lateinit var appContext: Context
   /* private lateinit var appContext: Context
    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
*/    private var isScreenOn: Boolean = false

    // ===== State flags =====
    var isUserJoined: Boolean = false
    var isHost: Boolean = false
    var giftEmpty: Boolean = false
    var giftShow: Boolean = true
    var isEnableCamera: Boolean = true
    var isEnableMic: Boolean = true
    var micStatus: Boolean = true
    var lensFacing: Boolean = true
    val guestRequestedUsers = mutableListOf<String>()

    // ===== User & Media =====
    var userInfo: ZEGOSDKUser? = null
    private var giftMediaPlayer: ZegoMediaPlayer? = null
    private var giftSurfaceView: SurfaceView? = null

    // ===== BottomSheet Menus =====
    private lateinit var menuBinding: BottomSheetPkMenuBinding
    private lateinit var bottomSheetMenuDialog: BottomSheetDialog
    private lateinit var bottomMenuBinding: PkBottomMenuBarContent1Binding
    private var liveEndDialog: AlertDialog? = null

    // ===== Timestamps =====
    private val DEBOUNCE_INTERVAL = 1000L // 1 second
    private var lastUserJoinTime: Long = 0
    private var lastUserQuite: Long = 0
    private var lastUserUpdateTime: Long = 0

    val giftListItems = ArrayList<GiftList>()
    val giftLists = ArrayList<GiftList>()
    val tempViewList = ArrayList<StreamDetails.LiveViewers>()
    private lateinit var viewerListAdapter: ViewerListAdapter
    private var roomRequestDialog: BottomSheetDialog? = null
    private lateinit var roomRequestRV: RecyclerView
    private lateinit var roomRequestAdapter: RoomRequestAdapter
    private lateinit var roomRequestLayoutManager: CustomLayoutManager

    private val roomRequestList: MutableList<MutableMap<String, String>> = mutableListOf()
    private val animation_duration: Double = 5.0
    var finalGiftPosition = -1
    private var selected_userId_forGift: String = ""
    private var selected_userName_forGift: String = ""
    private var mainStreamName: String = ""
    private var hostId: String? = null
    private var publisherName: String? = null
    private var title: String? = null
    private var live_type: String? = null
    private var last_gifted_date: String? = null
    private var profile_verified: String? = null
    private var streamName: String? = null
    private var type: String? = null
    private var inviteApiLink: String? = null
    private var inviteSocketLink: String? = null
    private var streamNameNew: String? = null
    private var hostImage: String? = null
    private var follow_sts: String? = null
    private var watchCount: String? = null
    private var max_cohost_allowed: Int? = 8;
    private var targetStreamId: String? = null
    val zimstate: ZIMConnectionState = ZIMConnectionState.CONNECTED

    private var content_new: String? = null
    private var TAG_PUSHER: String? = "12345 PkLivestreamingFragment"
    private var TAG_PUSHER_RESPONSE: String? = "12345 PkLivestreamingFragment"
    private var TAG_Updatestream: String? = "12345 PkLivestreamingFragment"
    private var TAG_ZEGO_LOGIN = "12345 PkLivestreamingFragment"
    private var TAG_WEBVIEW = "12345 PkLivestreamingFragment"
    private val cohostUniqueSet: MutableSet<ZEGOSDKUser> = HashSet()
    private val cohostList: MutableList<ZEGOSDKUser> = ArrayList()
    private var selectedIds: MutableList<String> = ArrayList()
    private var selectedMutual: MutableList<ProfileResponse> = ArrayList()
    private var mode: String? = null
    private var liveID: String? = null
    private var totalLikeCount: Int? = 0
    private var taskID: String? = null
    private var user_type: String? = null
    private var userLikeColor: String? = null
    private var multiliveApiCalled: Boolean = false

    var joinCoHost: JoinCoHost? = null
    var removeEndList: RemoveEndList? = null

    private lateinit var commentsAdapter: CommentsAdapter
    private val commentList = ArrayList<HashMap<String, String>>()
    private lateinit var commentLayoutManager: CustomLayoutManager


    var progress_value: Int = 1
    var availableGems: Int = 0
    var previousItemCount: Int = 0
  //  var isWelcomeMessageAdded: Boolean = false
    var isCommentsScrolled: Boolean = false
    var isHostChatDisabled: Boolean = false

    // ===== Companion Object =====
    companion object {
        private const val TAG_CPU_WAKELOCK = "MyApp::MyWakelockTag"

        @JvmField
        var gemList: ArrayList<StreamDetails.StreamCohost> = ArrayList()

        fun newInstance(args: Bundle? = null): PkLiveStreamingFragment {
            val fragment = PkLiveStreamingFragment()
            args?.let { fragment.arguments = it }
            return fragment
        }
    }

    // ===== Lifecycle =====
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PkTestActivityLiveStreamingBinding.inflate(inflater, container, false)
        appContext = requireContext()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFullScreenMode()

        // register app background listener
        App.AppLifecycleObserver.setAppBackgroundListener(this)

        // update locale
        LocaleManager.updateResources(
            requireContext(),
            LocaleManager.getLanguageCode(requireContext())
        )

      //  initPowerOptions()
      //  turnOnScreen()

        // init APIs & DB
        apiInterface = ZegoApiClient.getClient(requireContext()).create(ApiInterface::class.java)
        apiInterfaceForProfile = ApiClient.getClient().create(ApiInterface::class.java)
        dbHelper = DBHelper.getInstance(requireContext())

        // bottom menu
        bottomMenuBinding = PkBottomMenuBarContent1Binding.bind(binding.liveBottomMenuBar)
        binding.blurTarget.post { setupBottomBarBlur() }
        menuBinding = BottomSheetPkMenuBinding.inflate(layoutInflater)

        commentsAdapter = CommentsAdapter(requireContext(), commentList, this)

        commentLayoutManager = CustomLayoutManager(requireContext())
        commentLayoutManager.setScrollEnabled(true)

        binding.liveMessageView.layoutManager = commentLayoutManager
        binding.liveMessageView.adapter = commentsAdapter
        addDefaultSystemMessages()
        commentsAdapter.notifyDataSetChanged()

        // arguments instead of Intent
        getIntentData()
        setRandomColor()
        showMenu()
        initPusher()
        callZegoFunction()
        setCurrentUserInfo()
        initializeWebView()
        setCommentsView()
        setOnclickMethods()
        initViewerList()
        streamDetailsApi()
        streamDetailsInfo()


    }

    fun setRandomColor(){
        val i1 = Random.nextInt(StreamConstants.LIKE_COLOR.size)
        userLikeColor = StreamConstants.LIKE_COLOR[i1].trim()
    }

    fun initViewerList(){
        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, true)
        binding.rvViewers.layoutManager = layoutManager
        viewerListAdapter = ViewerListAdapter(requireContext(), tempViewList, "mls")
        binding.rvViewers.adapter = viewerListAdapter
    }

    private fun addDefaultSystemMessages() {
        // Welcome message
        val welcomeMap = HashMap<String, String>()
        welcomeMap[Constants.TAG_TYPE] = "system_message"
        welcomeMap["system_icon"] = CommentsAdapter.SYSTEM_ICON_WELCOME
        welcomeMap["title"] = getString(R.string.system_welcome_title)
        welcomeMap[Constants.TAG_MESSAGE] = getString(R.string.system_welcome_message)
        commentList.add(welcomeMap)

        // Gift Sounds message
        val giftMap = HashMap<String, String>()
        giftMap[Constants.TAG_TYPE] = "system_message"
        giftMap["system_icon"] = CommentsAdapter.SYSTEM_ICON_GIFT_SOUND
        giftMap["title"] = ""
        giftMap[Constants.TAG_MESSAGE] = getString(R.string.system_gift_sound_on)
        commentList.add(giftMap)
    }

    private fun setupBottomBarBlur() {
        val radius = 24f
        val decorView = requireActivity().window.decorView
        val target = binding.blurTarget
        val windowBackground = decorView.background

        if (!isHost) {
            bottomMenuBinding.messageBlurView.apply {
                outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
                clipToOutline = true
            }
            bottomMenuBinding.messageBlurView.setupWith(target)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius)
        }

        binding.blurCard.setupWith(target)
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(radius)

        binding.layView.setupWith(target)
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(radius)

        binding.blurClose.setupWith(target)
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(radius)

        binding.blurEntertainment.setupWith(target)
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(radius)

        binding.ranking.setupWith(target)
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(radius)
    }

    fun setOnclickMethods() {

        bottomMenuBinding.roomRequestLay.setOnClickListener { v->
            App.preventMultipleClick(v)
            initRoomRequestDialog(roomRequestList)
        }

        bottomMenuBinding.shareLay.setOnClickListener { v->
            App.preventMultipleClick(v)
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/plain")
            val msg = "${GetSet.getName()} ${getString(R.string.stream_share_description)} $inviteApiLink"
            intent.putExtra(Intent.EXTRA_TEXT, msg)
            requireContext().startActivity(
                Intent.createChooser(
                    intent,
                    requireContext().getString(R.string.share_video)
                )
            )
        }
        bottomMenuBinding.roomRequestTV.setOnClickListener { v ->
            App.preventMultipleClick(v)
            initRoomRequestDialog(roomRequestList)
        }

        bottomMenuBinding.liveMenu.setOnClickListener {

            // remove from parent if already attached
            (menuBinding.root.parent as? ViewGroup)?.removeView(menuBinding.root)

            bottomSheetMenuDialog = BottomSheetDialog(requireContext())
            bottomSheetMenuDialog?.setContentView(menuBinding.root)
            bottomSheetMenuDialog?.show()
        }
        bottomMenuBinding.handRaiseLay.visibility = View.VISIBLE
        bottomMenuBinding.handRaise.visibility = View.VISIBLE

        bottomMenuBinding.handRaiseLay.setOnClickListener {
            try {
                App.preventMultipleClick(it)
                if (gemList != null && gemList.size < 8) {
                    if (!checkUserInRequestList(GetSet.getUserId())) {
                        if (!ZEGOLiveStreamingManager.getInstance().isCoHost(GetSet.getUserId())) {
                            val hostUser = ZEGOLiveStreamingManager.getInstance().hostUser
                            if (hostUser != null) {
                                val jsonObject = JSONObject()
                                try {
                                    jsonObject.put("room_request_type", RoomRequestType.REQUEST_COHOST)
                                } catch (_: JSONException) {
                                }

                                ZEGOSDKManager.getInstance().zimService.sendRoomRequest(
                                    hostUser.userID,
                                    jsonObject.toString(),
                                    object : RoomRequestCallback {
                                        override fun onRoomRequestSend(errorCode: Int, requestID: String) {
                                            if (errorCode == 0) {
                                                sendRequestAsCohost("viewer", GetSet.getName(), GetSet.getUserId(), "")
                                            } else {
                                                Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            } else {
                                sendRequestAsCohost("viewer", GetSet.getName(), GetSet.getUserId(), "")
                            }
                        } /*else if (ZEGOLiveStreamingManager.getInstance().isHost(GetSet.getUserId())) {
                            // Do nothing
                        } else {
                            mlsTxt.text = getString(R.string.txt_multilive)
                        }*/
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.already_request_received_from_this_host), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.txt_roomfulltext), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        bottomMenuBinding.handRaise.setOnClickListener {
            try {
                App.preventMultipleClick(it)
                if (gemList != null && gemList.size < 8) {
                    if (!checkUserInRequestList(GetSet.getUserId())) {
                        if (!ZEGOLiveStreamingManager.getInstance().isCoHost(GetSet.getUserId())) {
                            val hostUser = ZEGOLiveStreamingManager.getInstance().hostUser
                            if (hostUser != null) {
                                val jsonObject = JSONObject()
                                try {
                                    jsonObject.put("room_request_type", RoomRequestType.REQUEST_COHOST)
                                } catch (_: JSONException) {
                                }

                                ZEGOSDKManager.getInstance().zimService.sendRoomRequest(
                                    hostUser.userID,
                                    jsonObject.toString(),
                                    object : RoomRequestCallback {
                                        override fun onRoomRequestSend(errorCode: Int, requestID: String) {
                                            if (errorCode == 0) {
                                                sendRequestAsCohost("viewer", GetSet.getName(), GetSet.getUserId(), "")
                                            } else {
                                                Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            } else {
                                sendRequestAsCohost("viewer", GetSet.getName(), GetSet.getUserId(), "")
                            }
                        } /*else if (ZEGOLiveStreamingManager.getInstance().isHost(GetSet.getUserId())) {
                            // Do nothing
                        } else {
                            mlsTxt.text = getString(R.string.txt_multilive)
                        }*/
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.already_request_received_from_this_host), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Room is already full!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        bottomMenuBinding.viewerGuestLay.setOnClickListener {
            try {
                App.preventMultipleClick(it)
                if (gemList != null && gemList.size < 8) {
                    if (!checkUserInRequestList(GetSet.getUserId())) {
                        if (!ZEGOLiveStreamingManager.getInstance().isCoHost(GetSet.getUserId())) {
                            val hostUser = ZEGOLiveStreamingManager.getInstance().hostUser
                            if (hostUser != null) {
                                val jsonObject = JSONObject()
                                try {
                                    jsonObject.put("room_request_type", RoomRequestType.REQUEST_COHOST)
                                } catch (_: JSONException) {
                                }

                                ZEGOSDKManager.getInstance().zimService.sendRoomRequest(
                                    hostUser.userID,
                                    jsonObject.toString(),
                                    object : RoomRequestCallback {
                                        override fun onRoomRequestSend(errorCode: Int, requestID: String) {
                                            if (errorCode == 0) {
                                                sendRequestAsCohost("viewer", GetSet.getName(), GetSet.getUserId(), "")
                                            } else {
                                                Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            } else {
                                sendRequestAsCohost("viewer", GetSet.getName(), GetSet.getUserId(), "")
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.already_request_received_from_this_host), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Room is already full!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        bottomMenuBinding.btnGift.setOnClickListener {
            val gifts = AdminData.livestream_gifts
            Log.e(TAG_PUSHER,"giftsie : " + gifts.size)
            SendGiftBottomSheet(gifts) { selectedGift ->
                    finalGiftPosition = 0
                    selected_userId_forGift = hostId.toString()
                    selected_userName_forGift = publisherName.toString()
                    Log.d("Pusher Gift1", "Selected gift: ${selectedGift.giftTitle}, gems=${selectedGift.giftGems}")
                    sendGiftPusher(selectedGift, selectedGift.giftGems, 1, liveID.toString(), hostId.toString(), selected_userId_forGift, selected_userName_forGift, selectedGift.iconType) 
                }.show(parentFragmentManager, "SendGiftBottomSheet")
        }

        bottomMenuBinding.interactionLay.setOnClickListener {
            val interactionsSheet = com.app.Zego.BottomSheet.InteractionsBottomSheet()
            interactionsSheet.onPollClicked = {
                val pollSheet = com.app.Zego.BottomSheet.PollBottomSheet()
                pollSheet.show(parentFragmentManager, "PollBottomSheet")
            }
            interactionsSheet.onGiftsClicked = {
                val gifts = AdminData.livestream_gifts
                Log.e(TAG_PUSHER,"giftsie : " + gifts.size)
                SendGiftBottomSheet(gifts) { selectedGift ->
                    finalGiftPosition = 0
                    selected_userId_forGift = hostId.toString()
                    selected_userName_forGift = publisherName.toString()
                    Log.d("Pusher Gift1", "Selected gift: ${selectedGift.giftTitle}, gems=${selectedGift.giftGems}")
                    sendGiftPusher(selectedGift, selectedGift.giftGems, 1, liveID.toString(), hostId.toString(), selected_userId_forGift, selected_userName_forGift, selectedGift.iconType)
                }.show(parentFragmentManager, "SendGiftBottomSheet")
            }
            interactionsSheet.show(parentFragmentManager, "InteractionsBottomSheet")
        }

        binding.imgClose.setOnClickListener {
            if (isHost) {
                closeConfirmDialog(getString(R.string.stop_publish_description))
            } else {
                if (user_type.equals("viewer", ignoreCase = true)) {
                    closeConfirmDialog(getString(R.string.really_want_exit))
                } else {
                    closeConfirmDialog(getString(R.string.really_want_exit))
                }
            }
        }

        bottomMenuBinding.messageLay.setOnClickListener {
            CommentBottomSheet(
                requireContext(),
                isHostChatDisabled
            ) { message ->
                sendMessage(message)
            }.show()
        }

        binding.textViewViewerscount.setOnClickListener {
            App.preventMultipleClick(it)
            getStreamViewersInfo("viewers")
        }

        binding.layView.setOnClickListener {
            App.preventMultipleClick(it)
            getStreamViewersInfo("viewers")
        }
    }

    private fun getStreamViewersInfo(from: String) {
        LiveViewersListBtmsheet.getInstance(
            from,
            liveID.toString(),
            guestRequestedUsers,
            { viewer ->
                getProfile(viewer.userId, "profile")
                null
            },
            { viewer ->
                if (!checkUserInRequestList(viewer.userId)) {
                    var mRoomRequestID: String? = null
                    val localUser = ZEGOSDKManager.getInstance().expressService.currentUser
                    val hostUser = ZEGOLiveStreamingManager.getInstance().hostUser

                    // Null check for hostUser
                    if (hostUser == null) {
                        Log.e(TAG_PUSHER, "localUser or hostUser is null")
                        return@getInstance
                    }

                    val roomRequest =
                        ZEGOSDKManager.getInstance().zimService.getRoomRequestByRequestID(mRoomRequestID)

                    if (roomRequest == null) {
                        val jsonObject = JSONObject()
                        try {
                            jsonObject.put("room_request_type", RoomRequestType.INVITE_COHOST)
                        } catch (_: JSONException) {
                        }

                        ZEGOSDKManager.getInstance().zimService.sendRoomRequest(
                            hostUser.userID,
                            jsonObject.toString(),
                            object : RoomRequestCallback {
                                override fun onRoomRequestSend(errorCode: Int, requestID: String) {
                                    Log.d(TAG_PUSHER, "onRoomRequestSend")
                                    if (errorCode == 0) {
                                        Toast.makeText(
                                            requireContext(),
                                            getString(R.string.request_send_successfully),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        mRoomRequestID = requestID
                                        Log.e(
                                            TAG_PUSHER,
                                            "onRoomRequestSend: ::::::::::$errorCode:::$requestID"
                                        )
                                        sendRequestAsCohost(
                                            "host",
                                            viewer.userName,
                                            viewer.userId,
                                            "0"
                                        )
                                    }
                                }
                            }
                        )
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.already_request_received_from_this_guest),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                null
            },
            {
                //isViewTapped = false
                //openGiftDialog("", "")
                null
            }
        ).show(parentFragmentManager, "LiveViewersList")
    }

    fun sendGiftPusher(
        gift: Gift,
        giftPrice: Int,
        touchCount: Int,
        liveID: String,
        hostId: String,
        selectedUserId: String,
        selectedUserName: String,
        iconType: String
    ) {
        var isViewerGift = false
        val totalPrice = giftPrice * touchCount
        val channelName = "private-$liveID"

        val eventData = JsonObject().apply {
            addProperty("socket_id", viewModel.getSocketId())
            addProperty(Constants.TAG_GIFT_FROM, GetSet.getUserId())
            addProperty(Constants.TAG_GIFT_TO, selectedUserId)
            addProperty(Constants.TAG_GEMS_COUNT, totalPrice)
            addProperty(Constants.TAG_GIFT_ID, gift.giftId)
            addProperty("gift_count", touchCount)
            addProperty("gems_earnings", totalPrice)
            addProperty(Constants.TAG_GIFT_TITLE, gift.giftTitle)
            addProperty(Constants.TAG_GIFT_ICON, gift.giftIcon)
            addProperty("pk_gift", "0") //take a note for single and multi 0, for pk 1
            addProperty("verified_user", /*GetSet.getVerified_user()*/ "0")
            addProperty("text_color", /*GetSet.getTextcolor()*/"0")
            addProperty("user_level", /*if (!GetSet.isHide_my_level()) GetSet.getLevel().toString() else*/"-1")
            addProperty("last_gifted_date", /*GetSet.getLast_gifted_date()*/"")
            addProperty("live_type", /*ZEGOLiveStreamingManager.getInstance().getPKBattleInfo()?.let { getPKLiveType() } ?:*/"single")
            addProperty("icon_type", gift.iconType)
            addProperty(Constants.TAG_TYPE, Constants.TAG_GIFT)
            addProperty(Constants.TAG_USER_ID, GetSet.getUserId())
            addProperty(Constants.TAG_USER_NAME, GetSet.getName())
            addProperty(Constants.TAG_USER_IMAGE, GetSet.getUserImage())
            addProperty("host_id", hostId)
            addProperty("stream_name", liveID)
            addProperty("channel_name", channelName)
            addProperty("receiver_name", selectedUserName)
            if (hostId.equals(selectedUserId, true)) {
                addProperty("gift_to_user_type", "host")
            } else if (ZEGOLiveStreamingManager.getInstance().isCoHost(selectedUserId)) {
                addProperty("gift_to_user_type", "cohost")
            } else {
                isViewerGift = true
                addProperty("gift_to_user_type", "viewer")
            }
            when (iconType) {
                "svga" -> {
                    addProperty("gft_html_icon", "")
                    addProperty("gft_html_wav", "")
                    addProperty("svgatype_url", gift.gftSvgaIcon)
                }

                "html" -> {
                    addProperty("gft_html_icon", gift.gftHtmlIcon)
                    addProperty("gft_html_wav", gift.gft_html_wav)
                    addProperty("svgatype_url", "")
                }

                else -> {
                    addProperty("gft_mp4_url", gift.gft_mp4_url)
                    addProperty("giftdirection", gift.gft_direction)
                    addProperty("svgatype_url", gift.iconType)
                }
            }
        }
        Log.e("Pusher success gift req", Gson().toJson(eventData))

        viewModel.getPusherService().sendGiftPusher(GetSet.getAuthToken(), eventData)
            .enqueue(object : Callback<JsonObject>{
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                    Log.e("Pusher success gift", Gson().toJson(response.body()))
                    if (response.code() == 200) {
                        try {
                            val jsonData = JSONObject(response.body().toString())
                            val status = jsonData.optString(Constants.TAG_STATUS)
                            val outOfBalance = jsonData.optString(Constants.TAG_OUT_OF_BALANCE_STATUS)

                            (context as? Activity)?.runOnUiThread {
                                Log.e(
                                    "Pusher success gift 1",
                                    Gson().toJson(jsonData) + "---" + jsonData.optString(Constants.TAG_STATUS)
                                )

                                when {
                                    status.equals(
                                        Constants.TAG_TRUE,
                                        ignoreCase = true
                                    ) && finalGiftPosition != -1 -> {
                                        val totalGems = jsonData.optString(Constants.TAG_TOTAL_GEMS)
                                        if (totalGems.equals(
                                                "0",
                                                ignoreCase = true
                                            ) || totalGems.isBlank()
                                        ) {
                                            GetSet.setGems(0f)
                                        } else {
                                            GetSet.setGems(totalGems.toFloat())
                                        }

                                        /*GetSet.level = jsonData.optInt("level")
                                        level_limit = jsonData.optInt("level_limit")*/
                                    }

                                    status.equals(Constants.TAG_FALSE, ignoreCase = true) -> {
                                        if (outOfBalance.equals(
                                                Constants.TAG_TRUE,
                                                ignoreCase = true
                                            )
                                        ) {
                                            /*val count = clickCount - 1
                                            relative_lottie_view.visibility = View.GONE
                                            lottieCountDown.visibility = View.GONE*/
                                            //openRechargeDialog()
                                            Toast.makeText(
                                                context,
                                                getString(R.string.txt_nocoinsforsendgift),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    else -> {
                                        /* relative_lottie_view.visibility = View.GONE
                                         lottieCountDown.visibility = View.GONE*/
                                        Toast.makeText(
                                            context,
                                            requireContext().getString(R.string.something_went_wrong),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Log.e("Pusher success gift", t.localizedMessage.toString())

                }
            })

    }

    
    fun setCommentsView() {
        binding.liveCohostView.setOnCoHostClickListener(this)

        binding.liveMessageView.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            hideKeyboard()
            false
        }

        binding.liveMessageView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (dy < 0) {
                    // Scrolling up
                    if (previousItemCount == 0) {
                        previousItemCount = totalItemCount
                    }
                    isCommentsScrolled = true
                } else if (lastVisibleItem == totalItemCount - 1) {
                    isCommentsScrolled = false
                    binding.bubbleView.visibility = View.GONE
                    previousItemCount = 0
                }
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackPress()
                }
            }
        )
    }

    private fun handleBackPress() {
        val activity = requireActivity()
        if (activity is SwipeLiveActivity && activity.closeRightSlider()) {
            return
        }
        if (isHost) {
            closeConfirmDialog(getString(R.string.stop_publish_description))
        } else {
            if (ZEGOLiveStreamingManager.getInstance().isAudience(GetSet.getUserId())) {
                closeConfirmDialog(getString(R.string.really_want_exit))
            } else {
                closeConfirmDialog(getString(R.string.really_want_exit))
            }
        }
    }

    private fun closeConfirmDialog(message: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_end_live)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val tvTitle = dialog.findViewById<TextView>(R.id.tv_title)
        val tvMessage = dialog.findViewById<TextView>(R.id.tv_message)
        val btnCancel = dialog.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_cancel)
        val btnEndLive = dialog.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_end_live)

        if (isHost) {
            tvTitle.text = getString(R.string.end_live_title)
            tvTitle.visibility = View.VISIBLE
            tvMessage.text = getString(R.string.end_live_message)
            tvMessage.visibility = View.VISIBLE
            btnEndLive.text = getString(R.string.end_live_action)
        } else {
            tvTitle.text = getString(R.string.disconnect_confirm_title)
            tvTitle.visibility = View.VISIBLE
            tvMessage.visibility = View.GONE
            btnEndLive.text = getString(R.string.disconnect_action)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnEndLive.setOnClickListener {
            dialog.dismiss()
            if (isHost) {
                Constants.TAG_STREAM_HOST = false
                guestRequestedUsers.clear()
                if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo != null) {
                    // Handle PK battle case if needed
                } else {
                    stopStream("close dialog")
                    liveStopStream()
                    unSubscribeStream(GetSet.getUserId())
                }
            } else {
                Log.e(TAG_PUSHER, "closeConfirmDialog: ::::::::::::::")
                if (ZEGOLiveStreamingManager.getInstance().isCoHost(GetSet.getUserId())) {
                    Log.e(TAG_PUSHER, "closeConfirmDialog: :::::::::::::: cohost")
                    unSubscribeStream(GetSet.getUserId())
                    micStatus = false

                    Handler(Looper.getMainLooper()).postDelayed({
                        ZEGOSDKManager.getInstance().expressService.stopPreview()
                        ZEGOSDKManager.getInstance().expressService.stopPublishingStream()
                    }, 100)


                    user_type = "viewer"
                    subscribeStream("single", "closedialog")
                    // streamDetailsApi()
                    getStreamInfo(liveID.toString(), "cohost exist")
                    binding.liveCohostView.updateList()
                    bottomMenuBinding.handRaiseLay.visibility = View.VISIBLE
                    bottomMenuBinding.handRaise.visibility = View.VISIBLE
                } else {
                    Log.e(TAG_PUSHER, "closeConfirmDialog: :::::::::::::: viewer")

                    unSubscribeStream(GetSet.getUserId())
                    ZEGOSDKManager.getInstance().logoutRoom(null)
                    val newIntent = Intent(requireContext().applicationContext, MainActivity::class.java)
                    requireActivity().startActivity(newIntent)

                    /* a_viewer_list.clear()
                    b_viewer_list.clear() */
                }
            }
        }

        dialog.show()
    }


    fun getStreamInfo(
        liveID: String,
        from: String
    ) {
        val socketId = viewModel.getSocketId() ?: return
        val jsonObject = JsonObject().apply {
            addProperty("socket_id", socketId)
            addProperty("channel_name", "private-$liveID")
            addProperty(Constants.TAG_USER_ID, GetSet.getUserId())
            addProperty(Constants.TAG_USER_NAME, GetSet.getName())
            addProperty(Constants.TAG_USER_IMAGE, GetSet.getUserImage())
            addProperty(StreamConstants.TAG_STREAM_NAME, liveID)
            addProperty("from", if (from == "initial") "" else from)
        }

        Log.e(TAG_PUSHER, "pusher streaminfo param: $jsonObject")

        viewModel.getPusherService()
            .getStreamInfo(GetSet.getAuthToken(), jsonObject)
            .enqueue(object : Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        Log.e(TAG_PUSHER_RESPONSE, "pusher getstreaminfo onResponse: ${response.body()}")
                    }
                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    Log.e(TAG_PUSHER_RESPONSE, "pusher getstreaminfo failure: ${t.message}")

                }
            })
    }

    fun subscribeStream(from: String, s: String) {
        val socketId = viewModel.getSocketId() ?: return

        val jsonObject = JsonObject().apply {
            addProperty("socket_id", socketId)
            addProperty("channel_name", "private-$liveID")
            // addProperty("text_color", GetSet.getTextcolor())
        }

        when (from) {
            "single" -> {
                jsonObject.addProperty(Constants.USER_TYPE, "viewer")
                jsonObject.addProperty(StreamConstants.TAG_STREAM_NAME, liveID)
            }
            "cohost" -> {
                jsonObject.addProperty(Constants.USER_TYPE, "cohost")
                jsonObject.addProperty(StreamConstants.TAG_STREAM_NAME, liveID)
            }
            else -> {
                jsonObject.addProperty(Constants.USER_TYPE, "guest")
                jsonObject.addProperty(StreamConstants.TAG_STREAM_NAME, /*otherLiveID*/"")
            }
        }

        jsonObject.addProperty("opponent_channel_name",/* if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo != null) "private-$otherLiveID" else*/ "")
        jsonObject.addProperty("live_type",/*ZEGOLiveStreamingManager.getInstance().pkBattleInfo?.let { getPKLiveType() } ?:*/ "")
        /* if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo?.pkAcceptedUsersList?.size == 3) {  jsonObject.addProperty("host_count", "3") }*/
        jsonObject.addProperty("opponent_stream_name",/*if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo != null) otherLiveID else*/ "")
        jsonObject.addProperty(Constants.TAG_USER_ID, GetSet.getUserId())
        jsonObject.addProperty("publisher_name", publisherName)
        jsonObject.addProperty(Constants.TAG_USER_NAME, GetSet.getName())
        jsonObject.addProperty(Constants.TAG_USER_IMAGE, GetSet.getUserImage()/*.replace(AdminData.mediaURL, "")*/)

        Log.e(TAG_PUSHER, "pusher subscribeStream: params: from::$s::: $jsonObject")

        viewModel.getPusherService()
            .subscribeStream(GetSet.getAuthToken(), jsonObject)
            .enqueue(object : Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    //{success=200}
                    Log.e(TAG_PUSHER_RESPONSE, "pusher subscribe onResponse: ${response.body()}")
                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    Log.e(TAG_PUSHER_RESPONSE, "pusher subscribe failure: $t")
                }
            })
    }

    fun sendUserJoined() {
        if (isUserJoined) return
        val socketId = viewModel.getSocketId() ?: return
        val jsonObject = JsonObject().apply {
            addProperty("socket_id", socketId)
            addProperty("channel_name", "private-$liveID")
            addProperty(Constants.TAG_TYPE, StreamConstants.TAG_STREAM_JOINED)
            addProperty(Constants.TAG_USER_ID, GetSet.getUserId())
            addProperty(Constants.TAG_USER_NAME, GetSet.getName())
            addProperty(Constants.TAG_USER_IMAGE, GetSet.getUserImage())
            addProperty(StreamConstants.TAG_STREAM_NAME, liveID)
            addProperty(StreamConstants.TAG_TIME, "")
           /* addProperty("verified_user", GetSet.getVerified_user())
            addProperty("text_color", GetSet.getTextcolor())
            if (!GetSet.isHide_my_level()) {
                addProperty("user_level", GetSet.getLevel().toString())
            } else {
                addProperty("user_level", "-1")
            }
            addProperty("last_gifted_date", GetSet.getLast_gifted_date())*/
        }

        Log.e(TAG_PUSHER, "pusher sendUserJoined params: $jsonObject")

        viewModel.getPusherService()
            .sendMessagePusher(GetSet.getAuthToken(),jsonObject)
            .enqueue(object : Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    Log.e(TAG_PUSHER_RESPONSE, "pusher sendUserJoined onResponse: ${response.body()}")
                    isUserJoined = true
                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    Log.e(TAG_PUSHER_RESPONSE, "pusher sendUserJoined failure: $t")
                }
            })
    }

    fun stopStream(from: String) {
        Log.e(TAG_PUSHER, "stopStream: ::::::::$from")

        val map = HashMap<String, String>().apply {
            put(StreamConstants.TAG_PUBLISHER_ID, GetSet.getUserId())
            put("TaskId", "")
            put(StreamConstants.TAG_NAME, liveID.toString())
        }

        val call3 = apiInterface.stopStreamNew(map)
        call3.enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {
                ZEGOSDKManager.getInstance().logoutRoom(null)
                Log.e(TAG_PUSHER, "stopstream : ${Gson().toJson(response.body())}")
                /*val intent = Intent(requireContext(), LiveEndActivity::class.java).apply {
                    putExtra("stream_name", streamNameNew)
                }
                startActivity(intent)*/
                 requireActivity().getSupportFragmentManager().popBackStack();
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                ZEGOSDKManager.getInstance().logoutRoom(null)
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        })
    }

    private fun addComment(map: HashMap<String, String>) {
       // isWelcomeMessageAdded = true
        commentList.add(map)
        Log.d(TAG_PUSHER, "add comments size-${commentList.size}")

        if (isCommentsScrolled) {
            showNewItemsCount()
        } else {
            commentsAdapter.notifyItemInserted(commentList.size - 1)
            binding.liveMessageView.scrollToPosition(commentList.size - 1)
        }

    }

    private fun showNewItemsCount() {
        val currentItemCount = commentList.size
        val newItemsCount = currentItemCount - previousItemCount

        if (newItemsCount > 0) {
            binding.bubbleView.text = getString(
                R.string.d_new_comments,
                Util.formatArToNumber(newItemsCount.toString())
            )
            binding.bubbleView.visibility = View.VISIBLE
        } else {
            binding.bubbleView.visibility = View.GONE
        }

        commentsAdapter.notifyItemInserted(commentList.size - 1)

        binding.bubbleView.setOnClickListener {
            binding.liveMessageView.smoothScrollToPosition(commentList.size - 1)
            isCommentsScrolled = false
            previousItemCount = 0
            binding.bubbleView.visibility = View.GONE
        }
    }

    fun sendLike() {
        val socketId = viewModel.getSocketId() ?: ""
        val eventData = JsonObject().apply {
            addProperty("socket_id", socketId)
            addProperty(Constants.TAG_TYPE, StreamConstants.TAG_LIKE)
            addProperty("channel_name", "private-$liveID")
            addProperty("like_to", hostId)
            addProperty(StreamConstants.TAG_STREAM_NAME, liveID)
            addProperty("like_count", progress_value.toString())
            addProperty("user_id", GetSet.getUserId())
        }

        Log.e(TAG_PUSHER, "Pusher sendLike params: $eventData")

        viewModel.getPusherService()
            .sendLike(GetSet.getAuthToken(), eventData)   // 👈 same pattern as liveStopStream
            .enqueue(object : Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    Log.e(TAG_PUSHER_RESPONSE, "pusher sendLike success: ${Gson().toJson(response.body())}")
                    progress_value = 1
                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    Log.e(TAG_PUSHER_RESPONSE, "pusher sendLike failure: ", t)
                    progress_value = 1
                }
            })
    }


    fun sendMessage(message: String){
        val socketId = viewModel.getSocketId() ?: ""
        val eventData = JsonObject().apply {
            addProperty("message", message)
            addProperty("socket_id", socketId )
            addProperty("channel_name", "private-$liveID")
            addProperty(Constants.TAG_USER_ID, GetSet.getUserId())
            addProperty(Constants.TAG_USER_NAME, GetSet.getName())
            addProperty(Constants.TAG_USER_IMAGE, GetSet.getUserImage())
            addProperty(StreamConstants.TAG_STREAM_NAME, liveID)
            addProperty("host_id", hostId)
            addProperty(Constants.TAG_TYPE, Constants.TAG_TEXT)
        }
        Log.d(TAG_PUSHER, "Message sent request: ${eventData}")

        viewModel.getPusherService()
            .sendMessagePusher(GetSet.getAuthToken() ,eventData)
            .enqueue(object : Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    Log.d(TAG_PUSHER, "Message sent response: ${response.body()}")
                    if (response.isSuccessful && response.body() != null) {
                        val responseMap: HashMap<String, String> = response.body()!!
                        /*
                         val status = responseMap["status"]
                         val message = responseMap["message"]

                        if (status.equals("true", ignoreCase = true)) {
                            Log.d(TAG_PUSHER_RESPONSE, "Comment allowed")
                            addComment(responseMap)
                        } else {
                            Log.w(TAG_PUSHER_RESPONSE, "Comment blocked: $message")
                        }*/

                        val normalizedMap = HashMap<String, String>().apply {
                            put("message", message.toString())
                            put("socket_id", socketId )
                            put("channel_name", "private-$liveID")
                            put(Constants.TAG_USER_ID, GetSet.getUserId())
                            put(Constants.TAG_USER_NAME, GetSet.getName())
                            put(Constants.TAG_USER_IMAGE, GetSet.getUserImage())
                            put(StreamConstants.TAG_STREAM_NAME, liveID.toString())
                            put("host_id", hostId.toString())
                            put(Constants.TAG_TYPE, Constants.TAG_TEXT)
                        }

                        addComment(normalizedMap)
                    } else {
                        Log.e(TAG_PUSHER_RESPONSE, "pusher sendChat: response unsuccessful or empty")
                    }

                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    Log.e("PkLiveStreamingActivity", "Failed to send message", t)
                }
            })
    }

    fun liveStopStream() {
        val socketId = viewModel.getSocketId() ?: ""
        val eventData = JsonObject().apply {
            addProperty("socket_id", socketId)
            addProperty("channel_name", "private-$liveID")
            addProperty(Constants.TAG_TYPE, "livestopstream")
            addProperty(Constants.TAG_USER_ID, GetSet.getUserId())
            addProperty(Constants.TAG_USER_NAME, GetSet.getName())
            addProperty(Constants.TAG_USER_IMAGE, GetSet.getUserImage())
            addProperty(StreamConstants.TAG_STREAM_NAME, liveID)
            addProperty("host_id", hostId)
        }

        Log.e(TAG_PUSHER, "pusher live stop params: $eventData")

        viewModel.getPusherService()
            .sendMessagePusher(GetSet.getAuthToken(), eventData)
            .enqueue(object : Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    Log.e(TAG_PUSHER_RESPONSE, "pusher live stop onResponse: ${response.body()}")
                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    Log.e(TAG_PUSHER_RESPONSE, "pusher live stop failure: ", t)
                }
            })
    }

    fun unSubscribeStream(userId: String) {
        val socketId = viewModel.getSocketId() ?: ""

        val eventData = JsonObject().apply {
            addProperty("socket_id", socketId)
            addProperty("channel_name", "private-$liveID")
            addProperty("opponent_channel_name", "")
            addProperty(Constants.TAG_USER_ID, userId)
            addProperty(StreamConstants.TAG_STREAM_NAME, liveID)
            //for pk, we need to enable this line and also check opponent channel name
            // addProperty("live_type", if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo != null) getPKLiveType() else "")
            addProperty("live_type",  "")
            addProperty("opponent_stream_name", /*if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo != null) otherLiveID else*/ "")
        }

        Log.e(TAG_PUSHER, "pusher unSubscribeStream: params $eventData")

        viewModel.getPusherService()
            .unsubscribeStream(GetSet.getAuthToken(), eventData)
            .enqueue(object : Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    Log.e(TAG_PUSHER_RESPONSE, "pusher unsubscribe onResponse: ${response.body()}")
                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    Log.e(TAG_PUSHER_RESPONSE, "pusher unsubscribe failure", t)
                }
            })
    }

    private fun hideKeyboard() {
        requireActivity().currentFocus?.let { view ->
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }


    fun initializeWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.clearCache(true)

        binding.webView.settings.apply {
            javaScriptEnabled = true
            builtInZoomControls = false
            setSupportZoom(false)
        }

        binding.webView.setBackgroundColor(Color.TRANSPARENT)

        // Define touch listener
        val likeTouchListener = View.OnTouchListener { _, event ->
            Log.e(TAG_WEBVIEW, "onTouch: :::::::::::::::")

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.heartLay.addHeart(Color.parseColor(userLikeColor))
                    // touchY = event.y
                    // touchX = event.x
                    // startProgressUpdates(v)
                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    // nothing in your original code
                }
            }
            false
        }

        // Attach listener
        binding.webView.setOnTouchListener(likeTouchListener)
    }

    fun initPusher() {
        val repository = PusherRepository(
            authTokenProvider = { GetSet.getAuthToken() },
            userIdProvider = { GetSet.getUserId() }
        )

        val factory = PusherViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[PusherViewModel::class.java]

        // Initialize Pusher
        viewModel.initPusher(liveID)

        // Observe connection state
        initPusherObservers()
    }

    fun initPusherObservers() {

        viewModel.connectionState.observe(viewLifecycleOwner) { change ->
            if (change.currentState == ConnectionState.CONNECTED) {
                Log.d(TAG_PUSHER, "Subscribed to private-$liveID")
                 getStreamDetails(liveID.toString(), "", "", "", "", "")

                if (!isHost) {
                       subscribeStream("single", "pusheronConnect")
                       sendUserJoined()
                }

               /* if (!isWelcomeMessageAdded) {
                    val map = HashMap<String, String>().apply {
                        put(Constants.TAG_USER_ID, GetSet.getUserId())
                        put(Constants.TAG_USER_NAME, GetSet.getName())
                        put(Constants.TAG_USER_IMAGE, GetSet.getUserImage())
                        put(Constants.TAG_MESSAGE, getString(R.string.live_welcome_msg))
                        put(Constants.TAG_TYPE, "welcome")
                        put(Constants.TAG_VISIBILITY, Constants.TAG_TRUE)
                    }
                    Log.d(TAG_PUSHER, "add comments-${Gson().toJson(map)}")
                    addComment(map)
                    isWelcomeMessageAdded = true
                }*/
            }
        }

        viewModel.pusherError.observe(viewLifecycleOwner) { error ->
            Log.e(TAG_PUSHER, error)
        }

        viewModel.msgReceived.observe(viewLifecycleOwner) { message ->
            Log.e(TAG_PUSHER, "msgReceived")
            Log.e(TAG_PUSHER, "msgReceived : $message")
            
            try {
                val data = JSONObject(message.toString())
                val userId = data.optString("user_id")
                val type = data.optString("type")
                val streamName = data.optString(Constants.TAG_STREAM_NAME)

                requireActivity().runOnUiThread {
                    if (streamName.equals(liveID, ignoreCase = true)) {
                        when (type) {
                            StreamConstants.TAG_HAND_RAISE -> {
                                if (data.optString(Constants.USER_TYPE) == "host") {
                                    if (data.optString(Constants.TAG_USER_ID) == GetSet.getUserId()) {
                                        val map = hashMapOf<String, String>(
                                            Constants.TAG_USER_ID to data.optString(Constants.TAG_USER_ID),
                                            Constants.TAG_USER_NAME to data.optString("user_name"),
                                            Constants.TAG_USER_IMAGE to data.optString(Constants.TAG_USER_IMAGE),
                                            Constants.TAG_TYPE to data.optString(Constants.TAG_TYPE),
                                            Constants.USER_TYPE to data.optString(Constants.USER_TYPE)
                                        )
                                        addRoomRequestComment(map)
                                    }
                                } else {
                                    for (i in roomRequestList.indices) {
                                        if (roomRequestList[i]["type"] == "handraise" ||
                                            roomRequestList[i]["type"] == "handraise_cancel" ||
                                            roomRequestList[i]["type"] == "handraise_accepted"
                                        ) {
                                            val checkMap = roomRequestList[i]
                                            if (checkMap["user_id"] == data.optString(Constants.TAG_USER_ID)) {
                                                roomRequestList.remove(checkMap)
                                                break
                                            }
                                        }
                                    }

                                    if (hostId == GetSet.getUserId()) {
                                        Log.e(TAG_PUSHER, "run: ::::::::4")
                                        val map = hashMapOf<String, String>(
                                            Constants.TAG_USER_ID to data.optString(Constants.TAG_USER_ID),
                                            Constants.TAG_USER_NAME to data.optString(Constants.TAG_USER_NAME),
                                            Constants.TAG_USER_IMAGE to data.optString(Constants.TAG_USER_IMAGE),
                                            Constants.TAG_TYPE to data.optString(Constants.TAG_TYPE),
                                            Constants.USER_TYPE to data.optString(Constants.USER_TYPE)
                                        )
                                        addRoomRequestComment(map)
                                    } else {
                                        // do nothing
                                    }
                                }

                            }
                            StreamConstants.TAG_HAND_RAISE_REJECT ->{
                                if (isHost && guestRequestedUsers.isNotEmpty()) {
                                    guestRequestedUsers.remove(data.optString(Constants.TAG_USER_ID))
                                }

                                if (data.optString(Constants.TAG_USER_ID) == GetSet.getUserId()) {
                                    //handView.visibility = View.VISIBLE
                                    bottomMenuBinding.handRaiseLay.visibility = View.VISIBLE
                                     bottomMenuBinding.handRaise.visibility = View.VISIBLE
                                    //take a note on viewer side...now it is host side
                                }

                               // Toast.makeText(context, "Host Rejected your request", Toast.LENGTH_SHORT).show()

                                val map = HashMap<String, String>().apply {
                                    put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID))
                                    put(Constants.TAG_USER_NAME,data.optString("user_name"))
                                    put(Constants.TAG_USER_IMAGE, if (data.optString(Constants.USER_TYPE).equals("viewer", ignoreCase = true)) { data.optString("to_user_image") } else { data.optString(Constants.TAG_USER_IMAGE) })
                                    put(Constants.TAG_TYPE, data.optString(Constants.TAG_TYPE))
                                    put(Constants.USER_TYPE, data.optString(Constants.USER_TYPE))
                                    put("user_level", data.optString("user_level"))
                                    put("host_name", data.optString("host_name"))
                                }
                                if (map["user_type"] == "host") {
                                    if (com.app.helper.LocaleManager.isRTL()) {
                                        content_new = "${map["host_name"]} رفض طلبك  "
                                    } else {
                                        content_new =
                                            "${map["host_name"]} ${context?.getString(R.string.text_rejected_your_request)}"
                                    }
                                } else {
                                    if (com.app.helper.LocaleManager.isRTL()) {
                                        content_new = "${map["user_name"]} رفض طلبك "
                                    } else {
                                        content_new =
                                            "${map["user_name"]} ${context?.getString(R.string.text_rejected_your_request)}"
                                    }
                                }

                                Toast.makeText(context, content_new, Toast.LENGTH_SHORT).show()
                                getStreamInfo(liveID.toString(), "TAG_HAND_RAISE_REJECT1")

                            }
                            StreamConstants.TAG_HAND_RAISE_ACCEPT ->{

                                if (isHost && guestRequestedUsers.isNotEmpty()) {
                                    guestRequestedUsers.remove(data.optString(Constants.TAG_USER_ID))
                                }

                                if (checkUserOnline(userId)) {
                                    if (userId == GetSet.getUserId()) {
                                        user_type = "cohost"
                                        unSubscribeStream(GetSet.getUserId())

                                        Handler(Looper.getMainLooper()).postDelayed({
                                            subscribeStream("cohost", "TAG_HAND_RAISE_ACCEPT")
                                        }, 1000)

                                        val userName = if (data.optString(Constants.USER_TYPE).equals("viewer", ignoreCase = true)) {
                                            publisherName
                                        } else {
                                            data.optString(Constants.TAG_USER_NAME)
                                        }
                                        val map = HashMap<String, String>().apply {
                                            put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID))
                                            put(Constants.TAG_USER_NAME,userName.toString())
                                            put(Constants.TAG_USER_IMAGE, if (data.optString(Constants.USER_TYPE).equals("viewer", ignoreCase = true)) { data.optString("to_user_image") } else { data.optString(Constants.TAG_USER_IMAGE) })
                                            put(Constants.TAG_TYPE, data.optString(Constants.TAG_TYPE))
                                            put(Constants.USER_TYPE, data.optString(Constants.USER_TYPE))
                                            put("user_level", data.optString("user_level"))
                                            put("host_name", data.optString("host_name"))
                                        }

                                        Log.d(TAG_PUSHER, "add comments-${Gson().toJson(map)}")
                                        //addComment(map)

                                        if (map["user_type"] == "host") {
                                            if (com.app.helper.LocaleManager.isRTL()) {
                                                content_new = "${map["host_name"]}  قبل طلبك  "
                                            } else {
                                                content_new =
                                                    "${map["host_name"]} ${context?.getString(R.string.text_accepted_your_request)}"
                                            }
                                        } else {
                                            if (com.app.helper.LocaleManager.isRTL()) {
                                                content_new = "${map["user_name"]}  قبل طلبك  "
                                            } else {
                                                content_new =
                                                    "${map["user_name"]} ${context?.getString(R.string.text_accepted_your_request)}"
                                            }
                                        }

                                        Toast.makeText(context, content_new, Toast.LENGTH_SHORT).show()

                                        val expressService = ZEGOSDKManager.getInstance().expressService
                                        val currentUser: ZEGOSDKUser = expressService.currentUser

                                        if (ZEGOLiveStreamingManager.getInstance().isAudience(GetSet.getUserId())) {
                                            expressService.openCamera(true)
                                            expressService.openMicrophone(true)
                                            menuBinding.switchVideo.isChecked = true
                                            menuBinding.switchAudio.isChecked = true
                                            menuBinding.switchFlipCamera.isChecked = false

                                            Handler(Looper.getMainLooper()).postDelayed({
                                                ZEGOLiveStreamingManager.getInstance().startPublishingStream()
                                                // showBeauty()
                                            }, 500)
                                        }

                                        sendNewCohostJoin()

                                    } else if (data.optString("host_id") == GetSet.getUserId()) {
                                        val map = hashMapOf(
                                            Constants.TAG_USER_ID to if (data.optString(Constants.USER_TYPE)
                                                    .equals("viewer", ignoreCase = true)
                                            ) data.optString("to_user_id") else data.optString(Constants.TAG_USER_ID),
                                            Constants.TAG_USER_NAME to if (data.optString(Constants.USER_TYPE)
                                                    .equals("viewer", ignoreCase = true)
                                            ) data.optString("to_user_name") else data.optString(Constants.TAG_USER_NAME),
                                            Constants.TAG_USER_IMAGE to if (data.optString(Constants.USER_TYPE)
                                                    .equals("viewer", ignoreCase = true)
                                            ) data.optString("to_user_image") else data.optString(Constants.TAG_USER_IMAGE),
                                            Constants.TAG_TYPE to data.optString(Constants.TAG_TYPE),
                                            Constants.USER_TYPE to data.optString(Constants.USER_TYPE)
                                        )
                                        // addComment(map)
                                    }

                                    Handler(Looper.getMainLooper()).postDelayed({
                                        getStreamInfo(liveID.toString(), "newcohostjoin")
                                        Log.d("ON_New_User_Join", "User type : $user_type")
                                        Log.d("ON_New_User_Join", "Data Recieved : $data")

                                        if (user_type == "cohost") {
                                            val userId = data.optString(Constants.TAG_USER_ID)
                                            if (userId == GetSet.getUserId()) {
                                                disableVideoAndAudio()
                                            }
                                        }
                                    }, 1000)
                                }

                            }
                            "nomsg" -> {
                                // getStreamInfo(liveID, "nomsg")
                            }

                            "updateGunView" -> {
                                //
                            }

                            "remainingGifts" -> {
                                if (GetSet.getUserId() == data.optString(Constants.TAG_USER_ID)) {
                                    val gift = data.optString("gifts")
                                    try {
                                        val jsonArrayGift = JSONArray(gift)
                                        for (i in 0 until jsonArrayGift.length()) {
                                            val jsonObject = jsonArrayGift.getJSONObject(i)
                                            val giftList = GiftList(
                                                jsonObject.getString("type"),
                                                jsonObject.optString("video_path"),
                                                jsonObject.optString("audio_path"),
                                                jsonObject.optString("svga_file"),
                                                jsonObject.optString("loop_count"),
                                                jsonObject.optDouble("animation_duration")
                                            )
                                            giftListItems.add(giftList)
                                            if (giftListItems.size == 1) {
                                                showGifts()
                                            }
                                        }
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                }
                            }

                            "cohostblocked" -> {
                                  if (ZEGOLiveStreamingManager.getInstance().isCoHost(GetSet.getUserId())) {
                                      if (data.optString(Constants.TAG_USER_ID) == GetSet.getUserId()) {
                                          unSubscribeStream(GetSet.getUserId())
                                          micStatus = false
                                          ZEGOSDKManager.getInstance().expressService.stopPreview()
                                          ZEGOSDKManager.getInstance().expressService.stopPublishingStream()
                                          user_type = "viewer"
  
                                         /* bottomDialogForGift?.let {
                                              if (it.isShowing) it.dismiss()
                                          }
                                          giftDialogForMulti?.let {
                                              if (it.isVisible) it.dismiss()
                                          }*/
  
                                   /*       Handler().postDelayed({
                                              bottomMenuBinding.handRaiseLay.visibility = View.VISIBLE
                                              bottomMenuBinding.handRaise.visibility = View.VISIBLE
                                              subscribeStream("single", "cohostblocked")
                                          }, 300)*/

                                          Handler(Looper.getMainLooper()).postDelayed({
                                              bottomMenuBinding.handRaiseLay.visibility = View.VISIBLE
                                              bottomMenuBinding.handRaise.visibility = View.VISIBLE
                                              subscribeStream("single", "cohostblocked")
                                          }, 300)


                                          // streamDetailsApi()
                                          //newly added on sep22
                                          bottomMenuBinding.handRaise.visibility = View.VISIBLE
                                          bottomMenuBinding.handRaiseLay.visibility = View.VISIBLE


                                          getStreamInfo(liveID.toString(), "cohostblocked")
                                          //Newly added ons ep22
                                          joinCoHost?.onDataReceived(false)
                                          Toast.makeText(
                                              context,
                                              getString(R.string.you_re_kicked_out_from_stream_by_stream_owner),
                                              Toast.LENGTH_SHORT
                                          ).show()
                                      }
                                  }
                            }

                            Constants.TAG_GIFT -> {
                                handleGiftMessage(data, streamName)
                            }

                            StreamConstants.TAG_STREAM_JOINED -> {
                                val map = hashMapOf(
                                    Constants.TAG_USER_ID to data.optString(Constants.TAG_USER_ID),
                                    Constants.TAG_USER_NAME to data.optString(Constants.TAG_USER_NAME),
                                    Constants.TAG_USER_IMAGE to data.optString(Constants.TAG_USER_IMAGE),
                                    Constants.TAG_MESSAGE to data.optString(Constants.TAG_MESSAGE),
                                    Constants.TAG_TYPE to data.optString(Constants.TAG_TYPE),
                                    "verified_user" to data.optString("verified_user"),
                                    "user_level" to data.optString("user_level"),
                                    "last_gifted_date" to data.optString("last_gifted_date"),
                                    "text_color" to data.optString("text_color")
                                )

                                addComment(map)
                            }

                            Constants.TAG_TEXT -> {

                                val map = hashMapOf(
                                    Constants.TAG_USER_ID to data.optString(Constants.TAG_USER_ID),
                                    Constants.TAG_USER_NAME to data.optString(Constants.TAG_USER_NAME),
                                    Constants.TAG_USER_IMAGE to data.optString(Constants.TAG_USER_IMAGE),
                                    Constants.TAG_MESSAGE to data.optString(Constants.TAG_MESSAGE),
                                    Constants.TAG_TYPE to Constants.TAG_TEXT
                                )

                                Log.d(TAG_PUSHER, "runnnn: ${data.optString("text_color")}")

                                if (!data.optString(Constants.TAG_USER_ID).equals(GetSet.getUserId(), ignoreCase = true)) {
                                    addComment(map)
                                }
                            }

                            StreamConstants.TAG_LIVE_STOP_STREAM -> {
                                var hostId: String? = null
                                try {
                                    hostId = data.getString("host_id")
                                    Log.e(TAG_PUSHER, "onReceiveStreamRemove: $hostId")

                                    if (SwipeLiveActivity.Companion.hostId == hostId) {
                                        Log.e(TAG_PUSHER, "onReceiveStreamRemove: $hostId")
                                        showLiveEndDialog()
                                    }
                                } catch (e: JSONException) {
                                    throw RuntimeException(e)
                                }

                            }

                        }
                    }
                    else {
                        //Take a note for battleld
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        viewModel.viewersinfo.observe(viewLifecycleOwner) { message ->
            Log.e(TAG_PUSHER, "viewersinfo : $message")

            try {
                val data = JSONObject(message.toString())
                val gson = Gson()
                val tempDetails = gson.fromJson(data.toString(), StreamDetails::class.java)
                targetStreamId = tempDetails.streamName
                Log.e(TAG_PUSHER, ":::::::::_viewersinfo::: ${Gson().toJson(tempDetails)}")
                if (targetStreamId != null && targetStreamId == liveID) {
                    binding.textViewViewerscount.text = tempDetails.watchCount?.let {
                        Util.formatNumberToAbbreviation(it.toLong())
                    } ?: "0"
                }

                // update joined layout
                /*try {
                    val watchCount = tempDetails.watchCount?.toLong() ?: 0
                    binding.itemLayJoined.visibility = if (watchCount > 0) View.VISIBLE else View.GONE
                } catch (e: Exception) {
                    binding.itemLayJoined.visibility = View.GONE
                }*/

                // viewers list update
                if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo == null) {
                    watchCount = tempViewList.size.toString()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            // remove viewers not in new data
                            tempViewList.removeIf { existingViewer ->
                                tempDetails.liveViewers.none { it.userId == existingViewer.userId }
                            }

                            // add new viewers
                            tempDetails.liveViewers
                                .filter { newViewer ->
                                    tempViewList.none { it.userId == newViewer.userId }
                                }
                                .forEach { newViewer ->
                                    tempViewList.add(newViewer)
                                }

                            // disable change animations
                            binding.rvViewers.itemAnimator = object : DefaultItemAnimator() {
                                override fun getChangeDuration(): Long = 0
                            }

                            viewerListAdapter.notifyDataSetChanged()

                        } catch (error: Exception) {
                            Log.e(TAG_PUSHER, "exception_error: ${error.message}")
                        }
                    }

                    // host guest button visibility
                    /*guestButton.visibility = if (isHost && tempViewList.isNotEmpty()) {
                        if (isEnableMultiLive) View.GONE else View.VISIBLE
                    } else View.GONE*/

                    if (isHost && tempViewList.isEmpty() /*&& roomRequestLay.visibility == View.VISIBLE*/) {
                       // roomRequestLay.visibility = View.GONE
                        roomRequestList.clear()
                    }

                    if (tempViewList.isNotEmpty()) {
                        binding.rvViewers.visibility = View.VISIBLE
                       // binding.header.hostFollow.visibility = View.GONE
                    } else {
                        binding.rvViewers.visibility = View.INVISIBLE
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        viewModel.likeReceived.observe(viewLifecycleOwner) { message ->
            Log.e(TAG_PUSHER, "likeReceived : $message")

            try {
                val jsonData = JSONObject(message.toString())

                val userId = jsonData.optString("user_id")
                val streamName = jsonData.optString("stream_name")
                val totalLikeCountStr = jsonData.optString("total_like_count")

                if (streamName.equals(liveID, ignoreCase = true)) {
                    //this concept is for more than 100 likes, then view count will changed
                    /* if (likeUpdate) {
                         totalLikeCount = totalLikeCountStr.toIntOrNull() ?: 0
                     }*/
                    totalLikeCount = totalLikeCountStr.toIntOrNull() ?: 0
                    binding.textViewLikecount.text = totalLikeCount.toString()

                    // If like is from other users
                    if (!userId.equals(GetSet.getUserId(), ignoreCase = true)) {
                        try {
                            if (jsonData.getString(StreamConstants.TAG_STREAM_NAME) == liveID) {
                                binding.heartLay.addHeart(Color.parseColor("#05AC90"))
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: JSONException) {
                Log.e(TAG_PUSHER, "Error parsing JSON: ${e.message}")
            }
        }

        viewModel.streamInfo.observe(viewLifecycleOwner) { message ->
            Log.e(TAG_PUSHER, "streamInfo viewer: $message")
            try {
                val jsonData = JSONObject(message.toString())
                if (jsonData.optString("status") == "false") {
                    return@observe
                }

                val gson = Gson()
                val tempDetails = gson.fromJson(jsonData.toString(), StreamDetails::class.java)

                if (tempDetails.name.equals(liveID, ignoreCase = true)) {
                    targetStreamId = tempDetails.name
                    gemList.clear()
                    gemList.addAll(tempDetails.streamCohostList)
                    updateView(cohostList)
                    binding.liveCohostView.updateGemList(gemList)
                    updateGemView()
                }
            } catch (e: JSONException) {
                Log.e(TAG_PUSHER, "Error parsing JSON: ${e.message}")
            }
        }

        viewModel.updatedgem.observe(viewLifecycleOwner) { message ->
            Log.e(TAG_PUSHER, "UpdatedGem : $message")
            try {
                val jsonData = JSONObject(message.toString())
                val userId = jsonData.optString("user_id")
                val earnedGem = jsonData.optInt("earned_gem")

                for (i in gemList.indices) {
                    if (gemList[i].userId.equals(userId, ignoreCase = true)) {
                        gemList[i].gemEarned = earnedGem
                        Log.e(TAG_PUSHER, "updateGemValue: ::::::::$i:::::$earnedGem")
                        binding.liveCohostView.updateGemList(gemList)
                        break
                    }
                }

                if (userId.equals(hostId, ignoreCase = true)) {
                    binding.tvDiamond.text = Util.formatNumberToAbbreviation(earnedGem.toLong())
                }

            } catch (e: JSONException) {
                Log.e(TAG_PUSHER, "Error parsing JSON: ${e.message}")
            }
        }

    }

    private fun handleGiftMessage(data: JSONObject, streamName: String) {
        val map = hashMapOf<String, String>().apply {
            put(Constants.TAG_USER_IMAGE, data.optString(Constants.TAG_USER_IMAGE))
            put(Constants.TAG_USER_ID, data.optString(Constants.TAG_USER_ID))
            put(Constants.TAG_USER_NAME, data.optString(Constants.TAG_USER_NAME))
            put(StreamConstants.TAG_STREAM_NAME, streamName)
            put(Constants.TAG_GIFT_TITLE, data.optString(Constants.TAG_GIFT_TITLE))
            put(Constants.TAG_GIFT_ICON, data.optString(Constants.TAG_GIFT_ICON))
            put(Constants.TAG_TYPE, data.optString(Constants.TAG_TYPE))
            put("selected_user_name", data.optString("receiver_name"))
            put(Constants.TAG_GIFT_TO, data.optString(Constants.TAG_GIFT_TO))
            put("gift_totalearning", data.optString("gift_totalearning"))
            put("gift_count", data.optString("gift_count"))
            put("verified_user", data.optString("verified_user"))
            put("user_level", data.optString("user_level"))
            put("text_color", data.optString("text_color"))
            put("last_gifted_date", data.optString("last_gifted_date"))
        }
        addComment(map)

        val iconType = data.optString("icon_type")
        val tcount = data.optInt("gift_count", 0)

        when (iconType) {
            "svga" -> {

            }

            "html" -> {

            }

            "mp4" -> {
                App.hideKeyboardActivity(requireActivity())
                repeat(tcount) {
                    val giftList = GiftList(
                        iconType,
                        data.optString("gft_mp4_url").replace("\"", ""),
                        data.optString("giftdirection"),
                        "",
                        "$tcount",
                        animation_duration
                    )
                    giftListItems.add(giftList)
                }
                if (giftListItems.isNotEmpty()) showGifts()
            }
        }
       
    }

    private fun showGifts() {
        if (giftListItems.isNullOrEmpty()) return

        Log.e(TAG_Gifts, "showGiftTest : giftListItems size ${Gson().toJson(giftListItems)}")

        when (giftListItems[0].type) {
            "html" -> {}

            "mp4" -> {
                loadMp4Gifts(0, giftListItems[0])
            }

            else -> {

            }
        }
    }

    private fun loadMp4Gifts(position: Int, giftList: GiftList) {
        if (giftListItems.isNullOrEmpty()) return

        binding.giftPlayerLay.visibility = View.VISIBLE

        val resource = ZegoMediaPlayerResource().apply {
            loadType = ZegoMultimediaLoadType.FILE_PATH

            val fileName = giftList.videoPath.substringAfterLast("/")
            val path = getStorageDirectory(requireContext()) + "/Alhawanajd/Gift/Animated/$fileName"
            val file = File(path)

            filePath = if (file.exists()) {
                Log.e(TAG_Gifts, "LoadMp4Gifts: file exists ${file.absolutePath}")
                file.absolutePath
            } else {
                Log.e(TAG_Gifts, "LoadMp4Gifts: File not exists ${giftList.videoPath}")
                giftList.videoPath
            }

            alphaLayout = when (giftList.audioPath) {
                "left" -> ZegoAlphaLayoutType.LEFT
                "right" -> ZegoAlphaLayoutType.RIGHT
                "none" -> ZegoAlphaLayoutType.NONE
                else -> ZegoAlphaLayoutType.NONE
            }
        }

        ZEGOSDKManager.getInstance().expressService.loadResourceFile(resource,
            object : IZegoMediaPlayerLoadResourceCallback {
                override fun onLoadResourceCallback(errorCode: Int) {
                    if (errorCode == 0 && giftSurfaceView?.parent == null) {
                        binding.giftPlayerLay.addView(giftSurfaceView)
                        giftMediaPlayer?.start()
                    }
                }
            })

        ZEGOSDKManager.getInstance().expressService.setMediaPlayerEventHandler(
            object : IZegoMediaPlayerEventHandler() {
                override fun onMediaPlayerStateUpdate(
                    mediaPlayer: ZegoMediaPlayer,
                    state: ZegoMediaPlayerState,
                    errorCode: Int
                ) {
                    super.onMediaPlayerStateUpdate(mediaPlayer, state, errorCode)
                    if (state == ZegoMediaPlayerState.PLAY_ENDED) {
                        binding.giftPlayerLay.removeView(giftSurfaceView)
                        binding.giftPlayerLay.visibility = View.GONE

                        if (giftListItems.isNotEmpty()) {
                            giftListItems.removeAt(0)
                            if (giftListItems.isNotEmpty()) {
                                showGifts()
                            }
                        }
                    }
                }
            })
    }

    fun getStorageDirectory(context: Context): String {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                ContextCompat.getExternalFilesDirs(context, null)[0].path
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                ContextCompat.getExternalFilesDirs(context, null)[0].path
            }
            else -> {
                Environment.getExternalStorageDirectory().path
            }
        }
    }

    fun callZegoFunction() {
        ZEGOLiveStreamingManager.getInstance().leave()
        ZEGOLiveStreamingManager.getInstance().addListenersForUserJoinRoom()
        listenSDKEvent()
        loginRoom("onCreate")
    }

    fun listenSDKEvent() {
        ZEGOSDKManager.getInstance().expressService.addEventHandler(object : IExpressEngineEventHandler() {

            override fun onRoomStateUpdate(
                roomID: String,
                state: ZegoRoomState,
                errorCode: Int,
                extendedData: JSONObject?
            ) {
                super.onRoomStateUpdate(roomID, state, errorCode, extendedData)
                Log.e(TAG_Updatestream, "onRoomStateUpdate ::: ${Gson().toJson(state)} ${Gson().toJson(extendedData)}")

                when (state) {
                    ZegoRoomState.DISCONNECTED -> {
                        Log.d("EngineEventHandler", "DISCONNECTED...$errorCode")
                    }
                    ZegoRoomState.CONNECTING -> {
                        Log.d("EngineEventHandler", "CONNECTING...")
                    }
                    ZegoRoomState.CONNECTED -> {
                        Log.d("EngineEventHandler", "CONNECTED...")
                    }
                    else -> {}
                }
            }

            override fun onRoomStreamUpdate(
                roomID: String,
                updateType: ZegoUpdateType,
                streamList: ArrayList<ZegoStream>,
                extendedData: JSONObject?
            ) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData)
                Log.e(TAG_Updatestream, "onRoomStreamUpdate ::: ${Gson().toJson(streamList)} ${Gson().toJson(extendedData)}")
            }

            override fun onRemoteSoundLevelUpdate(soundLevels: HashMap<String, Float>) {
                super.onRemoteSoundLevelUpdate(soundLevels)
                /* if (pkViewList == null || pkViewList.isEmpty()) return

                 for ((streamId, soundLevel) in soundLevels) {
                     for (i in pkViewList.indices) {
                         val user = pkViewList[i]
                         val roomID = user.name ?: ""
                         val userID = user.publisherId ?: ""
                         val fullStreamID = "${roomID}_${userID}_main_host"

                         if (streamId == fullStreamID) {
                              updateSpeechView(i, soundLevel)
                         }
                     }
                 }*/
            }

            override fun onCapturedSoundLevelUpdate(soundLevel: Float) {
                super.onCapturedSoundLevelUpdate(soundLevel)
                /* if (isHost && soundLevel > 0.5f && ZEGOLiveStreamingManager.getInstance().pkBattleInfo != null) {
                    updateSpeechView(0, soundLevel) // index 0 is self-host
                }*/
            }

            override fun onMixerSoundLevelUpdate(soundLevels: HashMap<Int, Float>) {
                super.onMixerSoundLevelUpdate(soundLevels)
                /* if (!isHost) {
                     if (pkViewList == null || pkViewList.isEmpty()) return

                     for ((streamIdInt, soundLevel) in soundLevels) {
                         val streamId = streamIdInt.toString()

                         for (i in pkViewList.indices) {
                             val user = pkViewList[i]
                             val roomID = user.name ?: ""
                             val userID = user.publisherId ?: ""
                             val fullStreamID = "${roomID}_${userID}_main_host"

                             if (streamId == fullStreamID) {
                                 updateSpeechView(i, soundLevel)
                             }
                         }
                     }
                 }*/
            }

            override fun onPublisherStateUpdate(streamID: String, state: ZegoPublisherState, errorCode: Int, extendedData: JSONObject?) {
                Log.e(TAG_Updatestream, "onPublisherStateUpdate: EngineEventHandler")

                if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo == null) {
                    if (isHost) {
                        return
                    }
                    super.onPublisherStateUpdate(streamID, state, errorCode, extendedData)
                    val currentUser = ZEGOSDKManager.getInstance().expressService.currentUser
                    Log.d(TAG_Updatestream, "onStreamPublished() called with: streamID = [$streamID], state = [$state], errorCode = [$errorCode]")

                    if (state == ZegoPublisherState.PUBLISHING) {
                        if (ZEGOLiveStreamingManager.getInstance().isCoHost(currentUser.userID)) {
                            bottomMenuBinding.handRaise.visibility = View.GONE
                            bottomMenuBinding.handRaiseLay.visibility = View.GONE
                            multiliveApiCalled = false
                            binding.liveCohostView.addUser(currentUser,
                                PkLiveStreamingActivity.Companion.gemList
                            )

                            //bottomDialogForGift?.takeIf { it.isShowing }?.dismiss()
                            //giftDialogForMulti?.takeIf { it.isVisible }?.dismiss()

                            if (cohostUniqueSet.add(currentUser)) {
                                cohostList.add(currentUser)
                            }
                            updateView(cohostList)

                            if (currentUser.userID == GetSet.getUserId()) {
                                updateBottomBarForCohost(true)
                                if (!bottomSheetMenuDialog.isShowing) {
                                    bottomSheetMenuDialog.show()
                                }
                            }

                        } else if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost) {

                            if (cohostUniqueSet.add(currentUser)) {
                                cohostList.add(currentUser)
                            }
                            updateView(cohostList)
                        }

                    } else if (state == ZegoPublisherState.NO_PUBLISH) {
                        if (streamID.endsWith("_host")) {
                            // binding.mainHostVideoIcon.letter = ""
                        } else {
                            binding.liveCohostView.removeUser(currentUser)

                            //   bottomDialogForGift?.takeIf { it.isShowing }?.dismiss()
                            //   giftDialogForMulti?.takeIf { it.isVisible }?.dismiss()

                            if (cohostUniqueSet.remove(currentUser)) {
                                cohostList.remove(currentUser)
                            }
                            updateView(cohostList)

                            if (currentUser.userID == GetSet.getUserId()) {
                                updateBottomBarForCohost(false)
                            }
                        }
                    }

                    checkBottomsButtons(liveID.toString())

                }
            }

            override fun onCameraOpen(userID: String, open: Boolean) {
                Log.e(TAG_Updatestream, "onCameraOpen: :::::userID::: $userID" )
                if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo != null) {
                 //   onPKUserCameraUpdate(userID, open)
                } else {
                    binding.liveCohostView.updateCamera(userID,open)
                    if (!ZEGOLiveStreamingManager.getInstance().isHost(userID)) {
                        binding.liveCohostView.updateList()
                    }

                }
            }

            override fun onMicrophoneOpen(userID: String, open: Boolean) {
                Log.e(TAG_Updatestream, "onMicrophoneOpen: :::::userID::: $userID")
                if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo == null) {
                    binding.liveCohostView.updateMicrophone(userID, open)
                    if (!ZEGOLiveStreamingManager.getInstance().isHost(userID)) {
                        binding.liveCohostView.updateList()
                    }
                }
            }

            override fun onReceiveStreamAdd(userList: List<ZEGOSDKUser>) {
                Log.e(TAG_Updatestream, "EngineEventHandler onReceiveStreamAdd() called with: userList = [$userList]:::::::::${Gson().toJson(
                    PkLiveStreamingActivity.Companion.gemList
                )}")
                Log.e(TAG_Updatestream, "onReceiveStreamAdd: :::::hostlist size:::${userList.size}")
                val coHostUserList = mutableListOf<ZEGOSDKUser>()
                if (cohostUniqueSet.addAll(userList)) {
                    cohostList.addAll(userList)
                }

                for (zegosdkUser in userList) {
                    if (ZEGOLiveStreamingManager.getInstance().isHost(zegosdkUser.userID)) {
                        binding.mainHostVideoIcon.setLetter(zegosdkUser.userName)
                        /* if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo == null) {
                             startPk.visibility = View.GONE
                         }*/
                    }
                }

                if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo == null) {
                    Log.e(TAG_Updatestream, "onReceiveStreamAdd: :::::::::3    ${coHostUserList.size}")
                    binding.liveCohostView.addUser(userList,
                        PkLiveStreamingActivity.Companion.gemList
                    )
                    //  bottomDialogForGift?.takeIf { it.isShowing }?.dismiss()
                    //  giftDialogForMulti?.takeIf { it.isVisible }?.dismiss()
                    Log.e(TAG_Updatestream, "onReceiveStreamAdd: :::::::::3    ${PkLiveStreamingActivity.Companion.gemList.size} test")
                }

                updateView(userList)
            }

            override fun onReceiveStreamRemove(userList: List<ZEGOSDKUser>) {
                Log.e(TAG_Updatestream, "EngineEventHandler onReceiveStreamRemove: ::::::hostList size:::${userList.size}")

                if (cohostUniqueSet.removeAll(userList)) {
                    cohostList.removeAll(userList)
                }

                updateView(userList)

                val coHostUserList = mutableListOf<ZEGOSDKUser>()
                for (user in userList) {
                    coHostUserList.add(user)
                }
                binding.liveCohostView.removeUser(coHostUserList)

                //  bottomDialogForGift?.takeIf { it.isShowing }?.dismiss()
                //  giftDialogForMulti?.takeIf { it.isVisible }?.dismiss()

                if (isHost && cohostList.isEmpty()) {
                    updateStreamDynamic("onReceiveStreamRemove", GetSet.getUserId(), "single", "", "", "", null)
                }

                getStreamInfo(liveID.toString(), "onReceiveStreamRemove")

                for (user in userList) {
                    if (user.userID == hostId) {
                        // showLiveEndDialog()
                        break
                    }
                }
            }

            override fun onRoomUserUpdate(
                roomID: String,
                updateType: ZegoUpdateType,
                userList: ArrayList<ZegoUser>
            ) {
                super.onRoomUserUpdate(roomID, updateType, userList)
                Log.e(TAG_Updatestream, "onRoomUserUpdate: EngineEventHandler $roomID::::$updateType:::${Gson().toJson(userList)}")
            }

            override fun onUserEnter(userList: List<ZEGOSDKUser>) {
                Log.e(TAG_Updatestream, "EngineEventHandler onUserEnter: :::::::hostList size:::${userList.size}")
            }

            override fun onUserLeft(userList: List<ZEGOSDKUser>) {
                Log.e(TAG_Updatestream, "EngineEventHandler onUserLeft: :::::::hostList size:::${userList.size}")

                //For pk
                /*   if (!ZEGOLiveStreamingManager.getInstance().isCurrentUserHost) {
                       val pkBattleInfo = ZEGOLiveStreamingManager.getInstance().pkBattleInfo
                       try {
                           if (pkBattleInfo != null) {
                               for (zegosdkUser in userList) {
                                   for (pkUser in pkBattleInfo.pkUserList) {
                                       if (pkUser.userID.contains(zegosdkUser.userID)) {
                                           streamersLeft()
                                       }
                                   }
                               }
                           }
                       } catch (e: Exception) {
                           e.printStackTrace()
                       }
                   }
         */
                //Take a note for this
                if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost) {

                    for (user in userList) {
                        if (isHost && guestRequestedUsers.isNotEmpty()) {
                            guestRequestedUsers.remove(user.userID)
                            break
                        }
                    }

                    val iterator = commentList.iterator()
                    var index = 0
                    while (iterator.hasNext()) {
                        val checkMap = iterator.next()
                        if (checkMap["type"] == "handraise" ||
                            checkMap["type"] == "handraise_cancel" ||
                            checkMap["type"] == "handraise_accepted"
                        ) {
                            for (user in userList) {
                                if (checkMap["user_id"] == user.userID) {
                                    iterator.remove()
                                    commentsAdapter.notifyItemRemoved(index)
                                    break
                                }
                            }
                        }
                        index++
                    }
                }
                Log.e(TAG_Updatestream, "onUserLeft: :::::::::${Gson().toJson(userList)}")

            }

            override fun onRoomStateChanged(
                roomID: String,
                reason: ZegoRoomStateChangedReason,
                errorCode: Int,
                extendedData: JSONObject?
            ) {
                Log.e(TAG_Updatestream, "onRoomStateChanged: host EngineEventHandler${Gson().toJson(reason)} $errorCode test")
                Log.e(TAG_Updatestream, "onRoomStateChanged: host EngineEventHandler${Gson().toJson(extendedData)} $errorCode test")
                //need to add code here, if same user logged another mobile and make live means need to show an alert
            }

        })

        ZEGOSDKManager.getInstance().zimService.addEventHandler(object : IZIMEventHandler() {

            override fun onOutgoingUserRequestRejected(requestID: String, invitee: String, extendedData: String) {
                super.onOutgoingUserRequestRejected(requestID, invitee, extendedData)
                Log.e(TAG_Updatestream, "ZEGO onOutgoingUserRequestRejected: ::::::::::::::$requestID ::: $invitee ::: $extendedData")
            }

            override fun onOutgoingUserRequestTimeout(requestID: String) {
                Log.d(TAG_Updatestream, "onOutgoingUserRequestTimeout")
                val request = ZEGOSDKManager.getInstance().zimService.getRoomRequestByRequestID(requestID)
                //Take a note, need to implement this for pk in future
                /*  if (hostList_updated != null && hostList_updated.isNotEmpty()) {
                    hostList_updated.removeAt(0)
                }*/
                //Log.e(TAG_Updatestream, "hello world onOutgoingUserRequestTimeout: ${Gson().toJson(hostList_updated)} ::: $requestID")
            }

            override fun onOutgoingRoomRequestAccepted(requestID: String, extendedData: String) {
                Log.e(TAG_Updatestream, "onOutgoingRoomRequestAccepted")

                val data = RoomRequestExtendedData.parse(extendedData)
                if (data != null && data.roomRequestType == RoomRequestType.REQUEST_COHOST) {
                    val expressService = ZEGOSDKManager.getInstance().expressService
                    val currentUser = expressService.currentUser
                    if (ZEGOLiveStreamingManager.getInstance().isAudience(currentUser.userID)) {
                        ZEGOLiveStreamingManager.getInstance().startCoHost()
                    }
                }

                val expressService = ZEGOSDKManager.getInstance().expressService
                val currentUser = expressService.currentUser
                //need to ask permission here
            }

            override fun onInComingRoomRequestReceived(requestID: String, extendedData: String) {
                Log.e(TAG_Updatestream, "ZEGO onInComingRoomRequestReceived ::::::::::::")
            }

            override fun onInComingRoomRequestCancelled(requestID: String, extendedData: String) {
                Log.e(TAG_Updatestream, "ZEGO onInComingRoomRequestCancelled ::::::::::::")
            }

            override fun onAcceptIncomingRoomRequest(errorCode: Int, requestID: String, extendedData: String) {
                Log.e(TAG_Updatestream, "ZEGO onAcceptIncomingRoomRequest ::::::::::::")
            }

            override fun onRejectIncomingRoomRequest(errorCode: Int, requestID: String, extendedData: String) {
                Log.e(TAG_Updatestream, "ZEGO onRejectIncomingRoomRequest ::::::::::::")
            }

            override fun onConnectionStateChanged(
                zim: ZIM,
                state: ZIMConnectionState,
                event: ZIMConnectionEvent,
                extendedData: JSONObject?
            ) {
                Log.e(TAG_Updatestream, "onConnectionStateChanged: EngineEventHandler")
                if (!isAdded || activity == null) {
                    return
                }
                zimstate == state

                //need to add code for same user logged on another device
            }
        })

    }

    fun setCurrentUserInfo() {
        // initialize here
        userInfo = ZEGOSDKManager.getInstance().expressService.currentUser
        if (userInfo == null) {
            signInZEGOSDK(GetSet.getUserId(), GetSet.getName()) { errorCode, _ ->
                if (errorCode == 0 || errorCode == 6000111) {
                    userInfo = ZEGOSDKManager.getInstance().expressService.currentUser
                }
            }
        }

        if (isHost) {
            SharedPref.putString("lastStreamId", liveID)
            user_type = "host"

            if (userInfo != null) {
                userInfo?.setCameraOpen(isEnableCamera)
                userInfo?.setMicrophoneOpen(isEnableMic)
            } else {
                userInfo = ZEGOSDKManager.getInstance().expressService.currentUser
                if (userInfo != null) {
                    ZEGOSDKManager.getInstance().expressService.openCamera(isEnableCamera)
                    ZEGOSDKManager.getInstance().expressService.openMicrophone(isEnableMic)
                } else {
                    Log.e("PkLiveStreamingFragment", "Error: Current user info is null")
                }
            }

            ZEGOLiveStreamingManager.getInstance().setHostUser(userInfo)

            // if you want beauty filter enable this and add code
            // showBeauty()

            menuBinding.switchVideo.isChecked = !isEnableCamera
            menuBinding.switchAudio.isChecked = !isEnableMic
            if (lensFacing) {
                ZegoExpressEngine.getEngine().setVideoMirrorMode(ZegoVideoMirrorMode.BOTH_MIRROR)
                menuBinding.switchFlipCamera.isChecked = true
            } else {
                ZegoExpressEngine.getEngine().setVideoMirrorMode(ZegoVideoMirrorMode.NO_MIRROR)
                menuBinding.switchFlipCamera.isChecked = false
            }

            ZEGOSDKManager.getInstance().expressService.openCamera(isEnableCamera)
            micStatus = isEnableMic
            ZEGOSDKManager.getInstance().expressService.openMicrophone(isEnableMic)
            binding.liveCohostView.addUser(
                ZEGOSDKManager.getInstance().expressService.currentUser,
                gemList
            )

            binding.liveCohostView.updateList()
            bottomMenuBinding.cohostButtonLay.visibility = View.VISIBLE
            bottomMenuBinding.cohostButton.visibility = View.VISIBLE
            bottomMenuBinding.handRaise.visibility = View.GONE
            bottomMenuBinding.handRaiseLay.visibility = View.GONE
            binding.hostFollow.visibility = View.GONE
        } else {
            user_type = "viewer"

            // get arguments instead of intent
            streamNameNew = arguments?.getString(StreamConstants.TAG_STREAM_NAME)
            bottomMenuBinding.btnGift.visibility = View.VISIBLE
            bottomMenuBinding.cohostButtonLay.visibility = View.GONE
            bottomMenuBinding.cohostButton.visibility = View.GONE
            bottomMenuBinding.handRaiseLay.visibility = View.VISIBLE
            bottomMenuBinding.handRaise.visibility = View.VISIBLE
            updateBottomBarForCohost()

            // If you want follow function for host means need to enable and check
            if (follow_sts != null && follow_sts.equals("false", ignoreCase = true)) {
                binding.hostFollow.visibility = View.VISIBLE
            } else if (follow_sts == "0") {
                binding.hostFollow.visibility = View.VISIBLE
            } else {
                binding.hostFollow.visibility = View.GONE
            }
        }

        binding.liveCohostView.visibility = View.VISIBLE

        Glide.with(requireContext())
            .load(hostImage)
            .placeholder(R.drawable.alhawana)
            .error(R.drawable.alhawana)
            .into(binding.userProfileImage)
    }

    private fun loginRoom(from: String) {
        ZEGOSDKManager.getInstance().loginRoom(
            liveID,
            ZegoScenario.BROADCAST,
            object : ZEGOSDKCallBack {
                override fun onResult(errorCode: Int, message: String?) {
                    Log.e(TAG_ZEGO_LOGIN, "onRoomLoginResult: :::::$errorCode::::${message.orEmpty()}")

                    if (errorCode == 0 || errorCode == 6000111) {
                        Log.e(TAG_ZEGO_LOGIN, "onRoomLoginResult: :::::1")
                        if (isHost) {
                            val currentUser = ZEGOSDKManager.getInstance().expressService.currentUser
                            ZEGOLiveStreamingManager.getInstance().setHostUser(currentUser)
                        }
                        onJoinRoomSuccess(from)
                    } else if (errorCode == 6000323 || errorCode == 6000320) {
                        ZEGOSDKManager.getInstance().logoutRoom(null)
                        onJoinRoomFailed()
                    } else {
                        Log.e(TAG_ZEGO_LOGIN, "onRoomLoginResult: :::::2")
                        ZEGOSDKManager.getInstance().logoutRoom(null)
                        ZEGOLiveStreamingManager.getInstance().removeRoomData()
                        ZEGOLiveStreamingManager.getInstance().removeRoomListeners()
                        ZEGOSDKManager.getInstance().disconnectUser()
                        requireActivity().finish() // Fragment-safe finish
                    }
                }
            }
        )
    }

    private fun onJoinRoomSuccess(from: String) {
        if (isHost) {
            if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost) {
                ZEGOSDKManager.getInstance().expressService.openCamera(isEnableCamera)
                ZEGOSDKManager.getInstance().expressService.openMicrophone(isEnableMic)
                ZEGOLiveStreamingManager.getInstance().startPublishingStream()
                // beauty filters hook here if needed
            }
        }

        if (isHost) {
            Log.e(TAG_ZEGO_LOGIN, "Invitelink : $inviteApiLink")
            val shortLinkTask = AppUtils.getStreamShareLink(requireContext(), inviteApiLink)
            shortLinkTask
                .addOnCompleteListener(requireActivity()) { task ->
                    Log.e(TAG_ZEGO_LOGIN, "publishTask response ${task.isSuccessful} streamlink $inviteApiLink")
                    if (task.isSuccessful) {
                        val shortLink = task.result?.shortLink
                        inviteApiLink = "$shortLink"
                        inviteSocketLink =
                            "${GetSet.getName()} ${getString(R.string.stream_share_description)} $shortLink"
                        startStream(inviteSocketLink.toString())
                        for (profileResponse in selectedMutual) {
                            Log.e(TAG_ZEGO_LOGIN, "onComplete: :::::::sendChat::::${Gson().toJson(profileResponse)}")
                            sendChat(inviteSocketLink.toString(), Constants.TAG_LIVE, profileResponse)
                        }
                    } else {
                        startStream(inviteSocketLink.toString())
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }

        binding.liveBottomMenuBar.visibility = View.VISIBLE
        checkBottomsButtons(liveID.toString())
        ZEGOSDKManager.getInstance().expressService.startSoundLevelMonitor()
        initGiftPlayer()
        binding.blurTarget.post { setupBottomBarBlur() }
    }

    fun initGiftPlayer(){

        giftMediaPlayer = ZEGOSDKManager.getInstance().expressService.mediaPlayer
        giftSurfaceView = SurfaceView(requireContext()).apply {
            holder.setFormat(PixelFormat.TRANSLUCENT)
            setZOrderOnTop(true)
        }
        val canvas = ZegoCanvas(giftSurfaceView).apply {
            alphaBlend = true
        }
        giftMediaPlayer?.setPlayerCanvas(canvas)

    }

    fun startStream(inviteSocketLink: String) {

        val map = HashMap<String, String>().apply {
            put(StreamConstants.TAG_PUBLISHER_ID, GetSet.getUserId())
            put(StreamConstants.TAG_TITLE, title ?: "")
            put(StreamConstants.TAG_NAME, streamName ?: "")
            put(Constants.TAG_TYPE, type ?: "")
            put(StreamConstants.TAG_STREAM_TOKEN, GetSet.getStreamToken())
            put(StreamConstants.TAG_INVITE_LINK, inviteApiLink ?: "")
            put(StreamConstants.TAG_RECORDING, false.toString())
            put("mode", mode ?: "")
            if (selectedIds.isNotEmpty()) {
                put(StreamConstants.TAG_GROUP_MEMBERS, TextUtils.join(",", selectedIds))
            }
        }

        Log.e(TAG_ZEGO_LOGIN, "startStream request: ::::::$map")

        val call = apiInterface.startStream(map)
        call.enqueue(object : Callback<StartStreamResponse> {
            override fun onResponse(
                call: Call<StartStreamResponse>,
                response: Response<StartStreamResponse>
            ) {
                Log.e(TAG_ZEGO_LOGIN, "startStream onResponse::${Gson().toJson(response.body())}")
                if (response.isSuccessful && response.body() != null && response.body()?.status != null) {
                    Constants.TAG_STREAM_HOST = true
                    if (response.body()?.status == "true") {
                        taskID = response.body()?.taskId
                        // Use Fragment-safe context
                        MyFirebaseMessagingService.clearAllNotifications(requireContext())
                    }
                }
            }

            override fun onFailure(call: Call<StartStreamResponse>, t: Throwable) {
                Log.e(TAG_ZEGO_LOGIN, "onFailure: :::startStream::", t)
                call.cancel()
            }
        })
    }

    private fun onJoinRoomFailed() {
        signInZEGOSDK(GetSet.getUserId(), GetSet.getName()) { errorCode, _ ->
            if (errorCode == 0 || errorCode == 6000111) {
                loginRoom("failed")
            }
        }
    }

    private fun signInZEGOSDK(userID: String?, userName: String?, callback: ZEGOSDKCallBack) {
        if (!userID.isNullOrBlank()) {
            ZEGOSDKManager.getInstance().connectUser(userID, userName, callback)
        }
    }

    // Normal AppWebSocket Functionality
// This function is for sending a message with the live link to selected mutual friends
    private fun sendChat(shareMessage: String, streamType: String, profileResponse: ProfileResponse) {
        try {
            val utcTime = AppUtils.getCurrentUTCTime(requireContext())

            val chatResponse = ChatResponse().apply {
                chatId = GetSet.getUserId() + profileResponse.userId
                type = Constants.TAG_SEND_CHAT
                userId = GetSet.getUserId()
                userName = GetSet.getName()
                userImage = GetSet.getUserImage()
                receiverId = profileResponse.userId
                messageType = streamType
                messageEnd = Constants.TAG_SEND
                message = shareMessage
                messageId = GetSet.getUserId() + (System.currentTimeMillis() / 1000)
                chatTime = utcTime
                receivedTime = AppUtils.getCurrentUTCTime(App.getInstance())
                chatType = Constants.TAG_USER_CHAT
                deliveryStatus = Constants.TAG_SEND
                thumbnail = ""
                progress = ""
                userId = profileResponse.userId
                userName = profileResponse.name
                userImage = profileResponse.userImage
                message = shareMessage
            }

            dbHelper.addMessage(chatResponse)
            dbHelper.addRecentMessage(chatResponse)

            val json = JSONObject().apply {
                put("type", chatResponse.type)
                put(Constants.TAG_RECEIVER_ID, chatResponse.receiverId)
                put("user_id", chatResponse.userId)
                put(Constants.TAG_USER_NAME, chatResponse.userName)
                put(Constants.TAG_USER_IMAGE, chatResponse.userImage)
                put(Constants.TAG_CHAT_ID, chatResponse.chatId)
                put(Constants.TAG_CHAT_TYPE, chatResponse.chatType)
                put(Constants.TAG_MSG_TYPE, chatResponse.messageType)
                put(Constants.TAG_MESSAGE_END, chatResponse.messageEnd)
                put("message", AppUtils.encryptMessage(chatResponse.message))
                put(Constants.TAG_MSG_ID, chatResponse.messageId)
                put(Constants.TAG_CHAT_TIME, chatResponse.chatTime)
                put(Constants.TAG_THUMBNAIL, chatResponse.thumbnail)
            }

            Log.e(TAG_PUSHER_RESPONSE, "sendChat:  $shareMessage   $json")
            AppWebSocket.getInstance(requireContext()).send(json.toString())

        } catch (e: JSONException) {
            Log.e(TAG_PUSHER_RESPONSE, "sendChat: ${e.message}")
        }
    }

    fun showMenu() {
        menuBinding = BottomSheetPkMenuBinding.inflate(layoutInflater)

        bottomSheetMenuDialog = BottomSheetDialog(requireContext(), R.style.Bottom_WhiteDialogNew).apply {
            setContentView(menuBinding.root)
        }

        menuBinding.switchVideo.setOnCheckedChangeListener { _, isChecked ->
            showSwitchProgressBar(menuBinding.switchVideo, menuBinding.progressVideo)
            val cameraOn = !isChecked
            ZEGOSDKManager.getInstance().expressService.openCamera(cameraOn)
            menuBinding.bottomCamera.setImageResource(
                if (isChecked) R.drawable.pkimg_videooff else R.drawable.pkimg_cameraon
            )
            isEnableCamera = !isChecked
        }

        menuBinding.switchAudio.setOnCheckedChangeListener { _, isChecked ->
            showSwitchProgressBar(menuBinding.switchAudio, menuBinding.progressAudio)

            ZEGOSDKManager.getInstance().expressService.openMicrophone(!isChecked)
            ZEGOSDKManager.getInstance().expressService.setAudioRouteToSpeaker(true)

            menuBinding.bottomMic.setImageResource(
                if (!isChecked) R.drawable.pkimg_mic_on else R.drawable.pkimg_micoff
            )

            isEnableMic = !isChecked
            micStatus = !isChecked
        }

        menuBinding.switchFlipCamera.setOnClickListener {
            showSwitchProgressBar(menuBinding.switchFlipCamera, menuBinding.progressFlipCamera)

            val useFrontCamera = menuBinding.switchFlipCamera.isChecked
            ZEGOSDKManager.getInstance().expressService.useFrontCamera(useFrontCamera)

            if (useFrontCamera) {
                // FRONT camera - mirror both preview and publish
                ZegoExpressEngine.getEngine().setVideoMirrorMode(ZegoVideoMirrorMode.BOTH_MIRROR)
            } else {
                // BACK camera - no mirror
                ZegoExpressEngine.getEngine().setVideoMirrorMode(ZegoVideoMirrorMode.NO_MIRROR)
            }
        }
    }

    private fun updateBottomBarForCohost(forcedCohostState: Boolean? = null) {
        val isCohost = forcedCohostState ?: ZEGOLiveStreamingManager.getInstance().isCoHost(GetSet.getUserId())
        if (isCohost) {
            bottomMenuBinding.viewerGuestLay.setImageResource(R.drawable.ic_settings)
            bottomMenuBinding.viewerGuestLay.setOnClickListener { v ->
                App.preventMultipleClick(v)
                if (!bottomSheetMenuDialog.isShowing) {
                    bottomSheetMenuDialog.show()
                }
            }
        } else {
            bottomMenuBinding.viewerGuestLay.setImageResource(R.drawable.ic_viewer_gusts)
            bottomMenuBinding.viewerGuestLay.setOnClickListener { v ->
                try {
                    App.preventMultipleClick(v)
                    if (gemList != null && gemList.size < 8) {
                        if (!checkUserInRequestList(GetSet.getUserId())) {
                            if (!ZEGOLiveStreamingManager.getInstance().isCoHost(GetSet.getUserId())) {
                                val hostUser = ZEGOLiveStreamingManager.getInstance().hostUser
                                if (hostUser != null) {
                                    val jsonObject = JSONObject()
                                    try {
                                        jsonObject.put("room_request_type", RoomRequestType.REQUEST_COHOST)
                                    } catch (_: JSONException) {
                                    }

                                    ZEGOSDKManager.getInstance().zimService.sendRoomRequest(
                                        hostUser.userID,
                                        jsonObject.toString(),
                                        object : RoomRequestCallback {
                                            override fun onRoomRequestSend(errorCode: Int, requestID: String) {
                                                if (errorCode == 0) {
                                                    sendRequestAsCohost("viewer", GetSet.getName(), GetSet.getUserId(), "")
                                                } else {
                                                    Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    sendRequestAsCohost("viewer", GetSet.getName(), GetSet.getUserId(), "")
                                }
                            }
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.already_request_received_from_this_host), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Room is already full!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    //below function for changing the bottom bar for single, multi or pk
    fun checkBottomsButtons(streamName: String) {
        try {
            val localUser = ZEGOSDKManager.getInstance().expressService.currentUser

            //this commented portion for pk function
            /* if ((localUser != null && ZEGOLiveStreamingManager.getInstance().isAudience(localUser.userID))
                 || ZEGOLiveStreamingManager.getInstance().isCoHost(localUser?.userID ?: "")
             ) {
                 pkButton.visibility = View.GONE
             } else {
                 pkButton.visibility = View.GONE
             }
 */
            if (localUser != null && ZEGOLiveStreamingManager.getInstance().isAudience(localUser.userID)) {
                menuBinding.switchAudio.visibility = View.GONE
                menuBinding.switchFlipCamera.visibility = View.GONE
                menuBinding.switchVideo.visibility = View.GONE
                bottomMenuBinding.liveMenu.visibility = View.GONE
            } else {
                menuBinding.switchAudio.visibility = View.VISIBLE
                menuBinding.switchFlipCamera.visibility = View.VISIBLE
                menuBinding.switchVideo.visibility = View.VISIBLE
                bottomMenuBinding.liveMenu.visibility = View.VISIBLE
            }

            //Pk function, currently I am hided here
            //updatePKButton()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showSwitchProgressBar(switchCompat: Switch, bar: ProgressBar) {
        try {
            bar.visibility = View.VISIBLE
            switchCompat.visibility = View.GONE

            Handler(Looper.getMainLooper()).postDelayed({
                bar.visibility = View.GONE
                switchCompat.visibility = View.VISIBLE
            }, 1200)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


 /*   private fun setupFullScreenMode() {
        val window = requireActivity().window

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Disable fitting system windows so content draws edge-to-edge
            window.setDecorFitsSystemWindows(false)

            // Transparent bars
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT

            // Allow bars to be shown transiently with swipe
            window.insetsController?.setSystemBarsBehavior(
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            )

            // Handle gesture navigation bottom inset
            binding.root.setOnApplyWindowInsetsListener { v, insets ->
                val systemBars = insets.getInsets(WindowInsets.Type.systemBars())
                v.setPadding(0, 0, 0, systemBars.bottom) // only bottom padding
                WindowInsets.CONSUMED
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

            @Suppress("DEPRECATION")
            window.statusBarColor = Color.TRANSPARENT
        }
    }
*/
 private fun setupFullScreenMode() {
     val window = requireActivity().window

     // This one line handles making the app fullscreen for all API levels correctly.
     WindowCompat.setDecorFitsSystemWindows(window, false)

     // Your existing code to handle gesture navigation padding is correct and should be kept.
     /*binding.root.setOnApplyWindowInsetsListener { v, insets ->
         val systemBars = insets.getInsets(WindowInsets.Type.systemBars())
         v.setPadding(0, 0, 0, systemBars.bottom) // only bottom padding
         WindowInsets.CONSUMED
     }*/

     ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
         val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
         v.setPadding(0, 0, 0, systemBars.bottom)
         WindowInsetsCompat.CONSUMED
     }
 }

    /* private fun initPowerOptions() {
         powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
         wakeLock = powerManager?.newWakeLock(
             PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
             TAG_CPU_WAKELOCK
         )
         wakeLock?.acquire(600_000L) // acquire for 10 minutes
         isScreenOn = powerManager?.isInteractive == true
     }
 */
   /* fun turnOnScreen() {
        releaseWakeLock()
        if (!isScreenOn) {
            if (wakeLock?.isHeld == false) {
                wakeLock?.acquire()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                requireActivity().setShowWhenLocked(true)
                requireActivity().setTurnScreenOn(true)
            }
            isScreenOn = true
        }
    }

    fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
    }*/

    private fun getIntentData() {
        isHost = arguments?.getBoolean("host", true) ?: true
        liveID = arguments?.getString("liveID")
        hostId = arguments?.getString("hostId")
        publisherName = arguments?.getString("publisherName")
        Log.e(TAG_PUSHER ,"Publisher Name : " + publisherName +" test");
        hostImage = arguments?.getString("hostImage")
        follow_sts = arguments?.getString("follow_sts")
        title = arguments?.getString(StreamConstants.TAG_TITLE)
        live_type = arguments?.getString("live_type")
        profile_verified = arguments?.getString("profile_verified")
        last_gifted_date = arguments?.getString("last_gifted_date")
        inviteApiLink = arguments?.getString(StreamConstants.TAG_INVITE_LINK)
        Log.e(TAG_PUSHER, "getIntentData: $inviteApiLink")
       // inviteApiLink = arguments?.getString(StreamConstants.TAG_NEW_SHARE_LINK)
        streamNameNew = arguments?.getString(StreamConstants.TAG_STREAM_NAME)
        streamName = arguments?.getString(StreamConstants.TAG_STREAM_NAME)

        if (isHost) {
            isEnableMic = arguments?.getBoolean("enableMic", true) ?: true
            isEnableCamera = arguments?.getBoolean("enableCamera", true) ?: true

            menuBinding.switchVideo.isChecked = isEnableCamera
            menuBinding.switchAudio.isChecked = isEnableMic

            lensFacing = arguments?.getBoolean("cameraStatus", true) ?: true
            mode = arguments?.getString("mode")

            if (arguments?.containsKey(StreamConstants.TAG_GROUP_MEMBERS) == true) {
                @Suppress("UNCHECKED_CAST")
                selectedMutual = arguments?.getSerializable(StreamConstants.TAG_GROUP_MEMBERS)
                        as? MutableList<ProfileResponse> ?: mutableListOf()
                selectedIds = arguments?.getStringArrayList(StreamConstants.TAG_GROUP_MEMBERS_ID) ?: arrayListOf()
            }
        }

        arguments?.getString("main_stream_name")?.let {
            mainStreamName = it
        }

        binding.txtViewHeadingNew.text = publisherName
    }

    override fun onResume() {
        super.onResume()
        // Keep the screen on while this fragment is visible
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Move onResume logic from Activity here
    }

    override fun onPause() {
        super.onPause()
        // Allow the screen to turn off when the fragment is not visible
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Move onPause logic here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cleanup resources, unregister listeners, etc.
    }

    override fun onAppBackgrounded() {
        TODO("Not yet implemented")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        TODO("Not yet implemented")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    override fun onUserTapped(x: Float, y: Float) {
        TODO("Not yet implemented")
    }

    override fun onCoHostClick(
        userId: String?,
        type: String?,
        userName: String?,
        v: View?
    ) {
        when (type) {
            "profile" -> {
                userId?.let {
                    getProfile(it, type)
                }
            }

            "kickout" -> {
                userId?.let {
                    //consider for cohost left
                     kickoutCoHostPusher(it)

                   /* bottomDialogForGift?.let { dialog ->
                        if (dialog.isShowing) dialog.dismiss()
                    }

                    giftDialogForMulti?.let { dialog ->
                        if (dialog.isVisible) dialog.dismiss()
                    }*/

                    Toast.makeText(
                        requireContext(),
                        "$userName ${getString(R.string.kicked_out_from_stream)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            "gift" -> {
                if (userId != null && GetSet.getUserId() != userId) {
                    Handler(Looper.getMainLooper()).postDelayed({
                       // isViewTapped = false
                        selected_userId_forGift = userId
                        selected_userName_forGift = userName.toString()

                        val gifts = AdminData.livestream_gifts
                        SendGiftBottomSheet(gifts) { selectedGift ->
                            finalGiftPosition = 0
                            Log.d("Pusher Gift1", "Selected gift: ${selectedGift.giftTitle}, gems=${selectedGift.giftGems}")
                            sendGiftPusher(selectedGift, selectedGift.giftGems, 1, liveID.toString(), hostId.toString(), selected_userId_forGift, selected_userName_forGift, selectedGift.iconType)
                        }.show(parentFragmentManager, "SendGiftBottomSheet")
                    }, 500)

                }
            }

            "heart" -> {
                Log.e(TAG_Gifts, "Heart clicked")
               /* likepBar.visibility = View.VISIBLE
                pBarLottie.visibility = View.VISIBLE
                linear_seat_host_bottom_diamond.visibility = View.GONE

                if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo != null) {
                    pkHeartLay.addHeart(Color.parseColor(userLikeColor))
                } else {
                    heartLay.addHeart(Color.parseColor(userLikeColor))
                }*/
                binding.heartLay.addHeart(Color.parseColor(userLikeColor))
                v?.let { sendLike() }
            }
        }
    }


    override fun onFirstItemSizeCalculated(
        width: Int,
        height: Int,
        viewtype: Int
    ) {
        Log.d("PkLiveStreaming", "CoHost clicked: onFirstItemSizeCalculated")

    }

    override fun onZoneTimeClicked(str: Map<String?, String?>?) {
        Log.d("PkLiveStreaming", "CoHost clicked: onFirstItemSizeCalculated")

    }

    override fun onLeaderBoardClick(
        stream: LeaderBoardUser.Stream?,
        type: String?,
        userId: String?
    ) {
        Log.d("PkLiveStreaming", "CoHost clicked: onFirstItemSizeCalculated")

    }

    override fun onSendRequest(
        from: String?,
        name: String?,
        userId: String?,
        position: String?
    ) {
        Log.d("PkLiveStreaming", "CoHost clicked: onFirstItemSizeCalculated")

    }

    override fun onClick(v: View?) {
        Log.d("PkLiveStreaming", "CoHost clicked: onFirstItemSizeCalculated")

    }

    override fun onPurchasesUpdated(
        p0: BillingResult,
        p1: List<Purchase?>?
    ) {
        Log.d("PkLiveStreaming", "CoHost clicked: onFirstItemSizeCalculated")

    }

    /*override fun onRequestClick(
        map: Map<String?, String?>?,
        status: String?,
        pos: Int
    ) {
        Log.d("PkLiveStreaming", "CoHost clicked: onFirstItemSizeCalculated")

    }*/

    override fun onRequestClick(map: Map<String, String>, status: String, position: Int) {
        Log.d(TAG_PUSHER, "onRequestClick: $status")

        if (status == "reject") {
            if (checkUserOnline(map["user_id"].toString())) {
                sendConfirmationToCohost(map["user_id"] ?: "", map[Constants.TAG_USER_NAME] ?: "", "reject", map[Constants.USER_TYPE] ?: "", position.toString())
                roomRequestList.remove(map)
            } else {
                roomRequestList.remove(map)
                Toast.makeText(requireContext(), getString(R.string.txt_userwentoffline), Toast.LENGTH_SHORT).show()
            }
        } else {
            if (gemList != null && gemList.size < 8) {
                if (checkUserOnline(map["user_id"].toString())) {
                    sendConfirmationToCohost(map["user_id"] ?: "", map[Constants.TAG_USER_NAME] ?: "", "accept", map[Constants.USER_TYPE] ?: "", position.toString())
                    Log.e(TAG_Updatestream, "multiliveApiCalled : $multiliveApiCalled")
                    updateStreamDynamic("onRequestClick", hostId.toString(), "multiple", "", "", "", null)
                    roomRequestList.remove(map)
                } else {
                    roomRequestList.remove(map)
                    Toast.makeText(requireContext(), getString(R.string.txt_userwentoffline), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), getString(R.string.txt_roomfulltext), Toast.LENGTH_SHORT).show()
            }
        }

        // If dialog is showing and no requests left, dismiss
        if (roomRequestDialog != null && roomRequestDialog!!.isShowing && roomRequestList.isEmpty()) {
            roomRequestDialog!!.dismiss()
            bottomMenuBinding.roomRequestLay.visibility = View.GONE
        }
    }

    fun sendConfirmationToCohost(
        userId: String,
        userName: String,
        status: String,
        userType: String,
        position: String
    ) {
        val socketId = viewModel.getSocketId() ?: ""
        val jsonObject = JsonObject().apply {
            addProperty("socket_id", socketId)
            addProperty("channel_name", "private-$liveID")
            addProperty(Constants.TAG_TYPE, if (status == "accept") Constants.TAG_HAND_RAISE_ACCEPT else Constants.TAG_HAND_RAISE_REJECT)
            addProperty(Constants.TAG_USER_ID, userId)
            addProperty(Constants.TAG_HOST_ID, hostId)
            addProperty(Constants.TAG_USER_NAME, GetSet.getName())
            addProperty(StreamConstants.TAG_STREAM_NAME, liveID)
            addProperty(Constants.USER_TYPE, userType)
            addProperty("pos", position)
            addProperty("from_user_id", GetSet.getUserId())

            if (userType != "viewer") {
                addProperty("to_user_id", hostId)
                addProperty("to_user_name", publisherName)
                addProperty("to_user_image", GetSet.getUserImage())
            } else {
                addProperty("to_user_id", userId)
                addProperty("to_user_name", userName)
                addProperty("to_user_image", if (isHost) GetSet.getUserImage() else hostImage)
            }
        }

        Log.e(TAG_PUSHER, "pusher sendConfirmationToCohost params: $jsonObject")

        viewModel.getPusherService()
            .sendMessagePusher(GetSet.getAuthToken(),jsonObject).enqueue(object : Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    Log.e(TAG_PUSHER_RESPONSE, "pusher sendConfirmationToCohost onResponse: ${response.body()}")

                    if (userType == "host" && status == "accept") {
                        handleHostAccept(userId)
                    } else {
                        handleOtherUserResponse(userId, status)
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        getStreamInfo(liveID.toString(), "sendConfirmationToCohost")
                    }, 500)

                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    Log.e(TAG_PUSHER_RESPONSE, "pusher sendConfirmationToCohost failure: $t")
                }
            })
    }

    private fun handleHostAccept(userId: String) {
        Log.e(TAG_PUSHER, "sendConfirmationToCohost: ::::::::1")
        user_type = "cohost"

        if (ZEGOLiveStreamingManager.getInstance().isAudience(GetSet.getUserId())) {
            ZEGOSDKManager.getInstance().expressService.openCamera(true)
            micStatus = true
            ZEGOSDKManager.getInstance().expressService.openMicrophone(true)
            menuBinding.switchVideo.isChecked = false
            menuBinding.switchAudio.isChecked = false
            menuBinding.switchFlipCamera.isChecked = true
            ZEGOLiveStreamingManager.getInstance().startPublishingStream()
            // showBeauty()
        }
        unSubscribeStream(GetSet.getUserId())
        subscribeStream("cohost", "sendConfirmationToCohost")
        bottomMenuBinding.cohostButtonLay.visibility = View.GONE
        bottomMenuBinding.cohostButton.visibility = View.GONE
        bottomMenuBinding.handRaise.visibility = View.GONE
        bottomMenuBinding.handRaiseLay.visibility = View.GONE
        sendNewCohostJoin()
    }

    private fun handleOtherUserResponse(userId: String, status: String) {
        Log.e(TAG_PUSHER, "sendConfirmationToCohost: ::::::::2")
        val cloudUser = ZEGOSDKManager.getInstance().expressService.getUser(userId)
        cloudUser?.userID?.let { uid ->
            val protocol = ZEGOSDKManager.getInstance().zimService.getRoomRequestBySenderID(uid)
            if (protocol != null && ZEGOSDKManager.getInstance().zimService.myReceivedRoomRequests.isNotEmpty()) {
                if (status == "accept") {
                    ZEGOSDKManager.getInstance().zimService.acceptRoomRequest(protocol.requestID) { errorCode, _ ->
                        Log.e(TAG_PUSHER_RESPONSE, "onRoomRequestSend: :::::::::$errorCode")
                    }
                } else {
                    ZEGOSDKManager.getInstance().zimService.rejectRoomRequest(protocol.requestID) { _, _ -> }
                }
            }
        }
    }

    private fun updateView(userList: List<ZEGOSDKUser>) {
        Log.e(TAG_ZEGO_LOGIN, "updateView: :::::::: ${cohostList.size}")

        if (ZEGOLiveStreamingManager.getInstance().pkBattleInfo == null) {
            val condition = if (isHost) cohostList.size + 1 else cohostList.size
            if (cohostList != null && condition <= 1) {
                binding.topView.visibility = View.GONE
                binding.bottomView.visibility = View.GONE
            } else {
                binding.topView.visibility = View.VISIBLE
                binding.bottomView.visibility = View.VISIBLE
            }
        } else {
            binding.topView.visibility = View.VISIBLE
            binding.bottomView.visibility = View.VISIBLE
        }
    }

    override fun onProfileClick(userId: String) {
        getProfile(userId, "profile")
    }

    private fun getProfile(userId: String?, from: String) {
        if (userId != null && apiInterfaceForProfile != null) {
            val request = ProfileRequest().apply {
                this.userId = GetSet.getUserId()
                this.profileId = userId
                /*streamName = streamNameNew*/
            }

            Log.e(TAG_PUSHER, "streamName_streamName : $streamNameNew")

            val call = apiInterface!!.getProfile(request)
            call.enqueue(object : Callback<com.app.model.ProfileResponse> {
                override fun onResponse(
                    call: Call<com.app.model.ProfileResponse>,
                    response: Response<com.app.model.ProfileResponse>
                ) {
                    try {
                        Log.e(TAG_PUSHER, "Profileresponse : " + Gson().toJson(response.body()))
                        val profile = response.body()

                        if (response.isSuccessful &&
                            profile?.status != null &&
                            profile.status == Constants.TAG_TRUE
                        ) {
                            when (from) {
                                "profile", "profile1", "leader" -> {
                                    // newly added by vishnu
                                    /* BlockUserId = profile.userId
                                     commentBlock = profile.commentBlock
                                     streamBlock = profile.streamBlock*/
                                    // initProfileDialog(profile, from)
                                }

                                "profileblock" -> {
                                    // do nothing, just placeholder
                                }

                                "gems" -> {
                                    availableGems = profile.availableGems.toInt()
                                    Log.e(TAG_PUSHER, "available_gems : $availableGems")
                                }

                                "recharge" -> {
                                    // confirmDialog(getString(R.string.please_validate_profile), profile)
                                }

                                "getlimit" -> {

                                }

                                "host_profile_image" -> {
                                    Log.e(TAG_PUSHER, "initial : ${profile.follow}")
                                    /* followUser(
                                         profile.follow.toString(),
                                         profile.userId,
                                         profile.name,
                                         true
                                     )*/
                                }

                                else -> {

                                }
                            }
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                        App.makeToast(getString(R.string.txt_null))
                    }
                }

                override fun onFailure(call: Call<com.app.model.ProfileResponse>, t: Throwable) {
                    call.cancel()
                }
            })
        }
    }

    private fun updateStreamDynamic(
        from: String,
        userId: String,
        type: String,
        oppHostId: String,
        oppStream: String,
        battleId: String,
        pkUserList: ArrayList<PKUser>?
    ) {
        var listOppHost: String? = null
        /* if (pkUserList != null) {
             listOppHost = createPKTeamListForUpdateStream(pkUserList)
             Log.d("ConvertedListOfPkUsers", "asdasdad$listOppHost")
         }*/

        var userType = ""
        var streamName = ""

        /* if (type != "single" && type != "multiple") {
             userType = if (isHost) "host" else "viewer"
             streamName = if (mainStreamName.isNullOrEmpty()) liveID else mainStreamName
         }
 */
        Log.e(
            TAG_Updatestream, "updateStream: params:::from:$from\n" +
                    "userid:$userId\n" +
                    "type:$type\n" +
                    "stream_id:$liveID\n" +
                    "opp_host_streamId:$oppStream\n" +
                    "opp_host_id:$oppHostId\n" +
                    "battle_id:$battleId\n" +
                    /*  "pk_id:$pk_id\n" +*/
                    "list_opp_host:$listOppHost\n" +
                    "main_stream_name:$streamName\n" +
                    "user_type:$userType\n" +
                    "user_name:${GetSet.getName()}"
        )

        val call = apiInterface.updateStreamDynamic(
            userId,
            type,
            liveID,
            /*oppStream*/"",
            /*oppHostId*/"",
            /*battleId*/"",
            /*pk_id*/ "",
            /*listOppHost*/"",
            streamName,
            userType,
            GetSet.getName()
        )

        call.enqueue(object : Callback<CommonResponse> {
            override fun onResponse(
                call: Call<CommonResponse>,
                response: Response<CommonResponse>
            ) {
                Log.e(TAG_Updatestream, "updatestreamresponse : ${Gson().toJson(response.body())}")
                if (type == "single") {
                    getStreamInfo(liveID.toString(), "updatetstream")
                }
            }

            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                Log.e(TAG_Updatestream, "updateStreamDynamic failed: ${t.message}")
            }
        })
    }

    fun getStreamDetails(
        liveID: String,
        from: String,
        requestID: String,
        inviter: String,
        inviterName: String,
        hostName: String
    ) {
        Log.e(TAG_PUSHER, "liveId$liveID from$from requestID$requestID inviter$inviter inviterName$inviterName host_name$hostName")

        val call3 = apiInterface.getStreamDetailsNew(GetSet.getUserId(), liveID)
        call3.enqueue(object : Callback<StreamDetails> {
            override fun onResponse(call: Call<StreamDetails>, response: Response<StreamDetails>) {
                if (!isAdded) return  // Exit early if fragment not attached

                Log.e(TAG_PUSHER_RESPONSE,  "started resposne : " + Gson().toJson(response.body()))

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (body.status == Constants.TAG_TRUE) {
                        val streamDetails = body

                       /* if (from != "pkRequest" && from != "pkRequestCheck") {
                            mainStreamName = streamDetails.mainStreamName
                            Log.d(TAG_Updatestream, "MAIN STREAM NAME 1: $mainStreamName")
                        }*/

                       /* if (!streamDetails.hourly_ranking.isNullOrEmpty()) {
                            textLevel_Host.text =
                                getString(R.string.hourly_ranking) + ":" + streamDetails.hourly_ranking
                        }*/

                        when (from) {
                            "pkRequest" -> {

                            }

                            "pkRequestCheck" -> {

                            }

                            "switch" -> {

                            }

                            else -> {
                               // inviteApiLink = streamDetails.inviteLink
                               // inviteApiLink = streamDetails.shareStreamLink
                                Log.e(TAG_PUSHER, "getIntentData1: $inviteApiLink")
                                if (isAdded && activity != null) {
                                    inviteSocketLink =
                                        GetSet.getName() + " " + getString(R.string.stream_share_description) + " " + inviteApiLink
                                    Log.e(TAG_PUSHER, "onResponse: ::::::$inviteSocketLink")
                                }

                                if (!isHost) {
                                   /* if (!"1on1".equals(stream_type, ignoreCase = true)) {
                                        streamDetailsApi(liveID)
                                    } else {*/
                                        //getStreamInfo(liveID, "initial")
                                    getStreamInfo(liveID,"started")
                                    // }
                                }
                            }
                        }

                        if (Constants.TAG_LIVE != body.type) {
                            showLiveEndDialog()
                        }
                    } else {
                      //  stream_type = ""
                    }
                }
            }

            override fun onFailure(call: Call<StreamDetails>, t: Throwable) {
                call.cancel()
                // Optional: handle failure
            }
        })
    }

    private fun showLiveEndDialog() {
        if (!isAdded) return  // Ensure Fragment is attached

        requireActivity().runOnUiThread {
            // Update UI safely
            Glide.with(requireContext())
                .load(hostImage)
                .error(R.drawable.alhawana)
                .into(binding.endingLayout.userProfileImageNew)

            val count = watchCount ?: "0"
            binding.endingLayout.txtViewCountTwo.text = count
            binding.endingLayout.txtName.text = publisherName

            //rootView.visibility = View.GONE
            binding.endingLayout.root.visibility = View.VISIBLE

            // Close button
            binding.endingLayout.imgClose.setOnClickListener {
               // requireActivity().finish()
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }

            // Browse button
            binding.endingLayout.browse.setOnClickListener {
                removeEndList?.onDataReceived("")
            }
        }

        // Cleanup after end
        unSubscribeStream(GetSet.getUserId())
        ZEGOSDKManager.getInstance().logoutRoom(null)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is JoinCoHost) {
            joinCoHost = context
        } else {
            Log.e("JoinCoHost", "Host activity does not implement JoinCoHost")
            joinCoHost = null
        }

        if (context is RemoveEndList) {
            removeEndList = context
        } else {
            Log.e("RemoveEndList", "Host activity does not implement RemoveEndList")
            removeEndList = null
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.e(TAG_PUSHER, "onDetach")

        //kindly take a look for below two line
       //joinCoHost = null
       //removeEndList = null
        // previously working fine, but when
        // app goes to background, unsubscribe is called...again enters app means viewers count is 0
        if (requireActivity().isFinishing || isRemoving) {
            if (App.onAppForegrounded) {
                Log.e(TAG_PUSHER, "App not in background, safe to unsubscribe")
                unSubscribeStream(GetSet.getUserId())
            } else {
                Log.e(TAG_PUSHER, "App is in background — skipping unsubscribe")
            }
        }
    }


    private fun updateGemView() {
        //if (!isBattleStarted) {
        Log.e("size:", gemList.size.toString())
        if (gemList.isEmpty()) {
            binding.tvDiamond.text = "0"
        } else {
            val value = gemList[0].gemEarned
            binding.tvDiamond.text = Util.formatNumberToAbbreviation(value.toLong())
        }
        //}
    }

    private fun checkUserInRequestList(userId: String?): Boolean {
        if (roomRequestList.isNotEmpty() && userId != null) {
                for (map in roomRequestList) {
                if (map["user_id"].equals(userId, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    private fun sendRequestAsCohost(from: String, userName: String, userId: String, pos: String) {
        Log.e(TAG_PUSHER, "sendRequestAsCohost: :::::::$from:::$userName:::$userId:::$pos")

        val jsonObject = JsonObject().apply {
            addProperty("socket_id", viewModel.getSocketId())
            addProperty("channel_name", "private-$liveID")
            /*    addProperty("opponent_channel_name", "private-$otherLiveID")
             */   addProperty(Constants.TAG_TYPE, Constants.TAG_HAND_RAISE)
            addProperty(Constants.USER_TYPE, from) // 1) host // 2) viewer
            addProperty(Constants.TAG_USER_ID, userId) // 1) selected user id
            addProperty(Constants.TAG_HOST_ID, hostId)
            addProperty("host_name", publisherName)
            addProperty(Constants.TAG_USER_NAME, userName)
            addProperty(StreamConstants.TAG_STREAM_NAME, liveID)
            addProperty(Constants.TAG_USER_IMAGE, GetSet.getUserImage())
            addProperty("pos", pos)

            if (from == "viewer") {
                addProperty("to_user_id", hostId)
                addProperty("to_user_name", publisherName)
                addProperty("to_user_image", hostImage)
                addProperty("from_user_id", GetSet.getUserId())
                addProperty("from_user_name", GetSet.getName())
                addProperty("from_user_image", GetSet.getUserImage())
            } else {
                addProperty("to_user_id", userId)
                addProperty("to_user_name", userName)
                addProperty("to_user_image", GetSet.getUserImage())
                addProperty("from_user_id", GetSet.getUserId())
                addProperty("from_user_name", GetSet.getName())
                addProperty("from_user_image", GetSet.getUserImage())
            }
        }

        Log.e(TAG_PUSHER, "pusher sendRequestAsCohost params: $jsonObject")

        viewModel.getPusherService()
            .sendMessagePusher(GetSet.getAuthToken(), jsonObject)
            .enqueue(object : Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    Log.d(TAG_PUSHER, "pusher sendRequestAsCohost onResponse:  ${response.body()}")
                    if (response.isSuccessful && response.body() != null) {
                        guestRequestedUsers.add(userId)
                        bottomMenuBinding.handRaise.visibility = View.GONE
                        bottomMenuBinding.handRaiseLay.visibility = View.GONE
                        //take a note for cohost button shown or not
                    } else {
                        Log.e(TAG_PUSHER_RESPONSE, "pusher sendChat: response unsuccessful or empty")
                    }

                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    Log.e("PkLiveStreamingActivity", "Failed to send message", t)
                }
            })

    }

    private fun checkUserOnline(userId: String): Boolean {
        var isOnline = false
        Log.e(TAG_Updatestream, "$zimstate : zimstateTEST")

        if (zimstate == ZIMConnectionState.CONNECTED) {
            for (viewer in tempViewList) {
                if (viewer.userId == userId) {
                    isOnline = true
                    break
                }
            }
        }

        return isOnline
    }

    fun sendNewCohostJoin() {
        val socketId = viewModel.getSocketId() ?: ""

        val eventData = JsonObject().apply {
            addProperty("socket_id", socketId)
            addProperty("channel_name", "private-$liveID")
            addProperty(Constants.TAG_TYPE, "newcohostjoin")
            addProperty(Constants.TAG_USER_ID, GetSet.getUserId())
            addProperty(Constants.TAG_USER_NAME, GetSet.getName())
            addProperty(Constants.TAG_USER_IMAGE, GetSet.getUserImage())
            addProperty(StreamConstants.TAG_STREAM_NAME, liveID)
            addProperty("host_id", hostId)
        }

        Log.d(TAG_PUSHER, "sendNewCohostJoin req: $eventData")

        viewModel.getPusherService()
            .sendMessagePusher(GetSet.getAuthToken(), eventData)
            .enqueue(object : Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    Log.d(TAG_PUSHER, "sendNewCohostJoin res: ${response.body()}")

                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    Log.e(TAG_PUSHER_RESPONSE, "Failed to send new cohost join", t)
                }
            })
    }

    private fun addRoomRequestComment(map: HashMap<String, String>) {
        val wasEmpty = roomRequestList.isEmpty()
        roomRequestList.add(map)

        if (roomRequestList.isNotEmpty() &&
            (isHost || GetSet.getUserId().equals(map[Constants.TAG_USER_ID], ignoreCase = true)) /*&&
          !isBattleStarted*/
        ) {
            bottomMenuBinding.roomRequestLay.visibility = View.VISIBLE
            if (wasEmpty) {
                initRoomRequestDialog(roomRequestList)
            }
        } else {
            bottomMenuBinding.roomRequestLay.visibility = View.GONE
        }
    }

    private fun disableVideoAndAudio() {
        if (isAdded && !isDetached) {
            menuBinding.switchFlipCamera.isChecked = true
            menuBinding.switchAudio.isChecked = true
            menuBinding.switchVideo.isChecked = true

            ZEGOSDKManager.getInstance().expressService.openMicrophone(false)
            ZEGOSDKManager.getInstance().expressService.openCamera(false)

            Handler(Looper.getMainLooper()).postDelayed({
                if (!bottomSheetMenuDialog.isShowing) {
                    bottomSheetMenuDialog.show()
                    if (!isHost) {
                        //menuBinding.laySwitchMultilive.visibility = View.GONE
                        //menuBinding.laySwitchPklive.visibility = View.GONE
                        //menuBinding.layChatLive.visibility = View.GONE
                    }
                }
            }, 1500)
        }
    }

    fun kickoutCoHostPusher(userId: String) {
        val socketId = viewModel.getSocketId() ?: ""
        val eventData = JsonObject().apply {
            addProperty("socket_id", socketId)
            addProperty("channel_name", "private-$liveID")
           /* addProperty("opponent_channel_name", "private-$otherLiveID")
           */ addProperty(Constants.TAG_TYPE, "cohostblocked")
            addProperty(Constants.TAG_USER_ID, userId)
            addProperty(StreamConstants.TAG_STREAM_NAME, liveID)
        }

        Log.d(TAG_PUSHER, "kickoutCoHostPusher req: $eventData")

        viewModel.getPusherService()
            .sendMessagePusher(GetSet.getAuthToken(), eventData)
            .enqueue(object : Callback<HashMap<String, String>> {
                override fun onResponse(
                    call: Call<HashMap<String, String>>,
                    response: Response<HashMap<String, String>>
                ) {
                    Log.d(TAG_PUSHER, "kickoutCoHostPusher res: ${response.body()}")
                }

                override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                    Log.e(TAG_PUSHER, "kickoutCoHostPusher failure", t)
                }
            })
    }


    private fun initRoomRequestDialog(roomRequestList: List<Map<String, String>>) {

        if (roomRequestDialog != null) {
            roomRequestDialog = null
        }

        val sheetView = layoutInflater.inflate(R.layout.btmsht_roomrequest, null)
        roomRequestDialog = BottomSheetDialog(requireContext(), R.style.SimpleBottomDialog1).apply {
            setContentView(sheetView)
        }

        val mBehavior = BottomSheetBehavior.from(sheetView.parent as View)
        val maxHeight =
            (DevicePref.getInstance(requireContext()).getInt(DevicePref.SCREEN_HEIGHT, 0) * 80) / 100
        mBehavior.peekHeight = maxHeight
        sheetView.requestLayout()

        roomRequestRV = sheetView.findViewById(R.id.roomRequestRV)

        roomRequestDialog?.setOnShowListener { dialogInterface ->
            val dialog = dialogInterface as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                    isHideable = true
                }
            }
        }

        roomRequestDialog?.setOnDismissListener { dialogInterface ->
            val dialog = dialogInterface as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_COLLAPSED
                    skipCollapsed = true
                    isHideable = true
                }
            }
        }

        roomRequestAdapter = RoomRequestAdapter(requireContext(), roomRequestList, this, publisherName)
        roomRequestLayoutManager = CustomLayoutManager(requireContext()).apply {
            setScrollEnabled(true)
        }

        roomRequestRV.layoutManager = roomRequestLayoutManager
        roomRequestRV.adapter = roomRequestAdapter
        roomRequestAdapter.notifyDataSetChanged()

        roomRequestDialog?.show()
    }


    private fun streamDetailsApi() {
     /*   if (NetworkReceiver.isConnected()) {
            val call = apiInterface.getStreamDetails(
                GetSet.getAuthToken(),
                GetSet.getUserId(),
                liveID.toString()
            )

            Log.e(TAG_PUSHER, "streamDetailsApi: :::::::::::$liveID::::")

            call.enqueue(object : Callback<StreamDetails> {
                override fun onResponse(
                    call: Call<StreamDetails>,
                    response: Response<StreamDetails>
                ) {
                    Log.e(TAG_PUSHER, "onResponse: ::::::::streamInfo::: --1${Gson().toJson(response.body())}")
                    if (response.isSuccessful && response.body() != null) {
                        val tempDetails = response.body()
                        Log.e(TAG_PUSHER, "onResponse: ::::::::api::: --1${Gson().toJson(response.body())}")

                        tempDetails?.name?.let { name ->

                        }
                    }
                }

                override fun onFailure(call: Call<StreamDetails>, t: Throwable) {
                    Log.e(TAG_PUSHER, "onResponse: ::::::::streamInfo:::")
                    t.printStackTrace()
                }
            })
        }*/
    }

    private fun streamDetailsInfo() {
        if (NetworkReceiver.isConnected()) {
            val call = apiInterface.getStreamInfo(
                GetSet.getUserId(), liveID,
                user_type,
                "", liveID.toString())

            Log.e(TAG_PUSHER, "streamDetailsApi: :::::::::::$liveID::::")

            call.enqueue(object : Callback<StreamDetails> {
                override fun onResponse(
                    call: Call<StreamDetails>,
                    response: Response<StreamDetails>
                ) {
                    Log.e(TAG_PUSHER, "onResponse: ::::::::streamInfo::: --1${Gson().toJson(response.body())}")
                    val body = response.body()
                    Log.e(TAG_PUSHER, "onResponse: streamInfo = ${Gson().toJson(body)}")

                    if (response.isSuccessful && body != null) {
                        Log.e(TAG_PUSHER, "onResponse: api = ${Gson().toJson(body)}")

                        val cohostList = body.streamCohostList
                        if (!cohostList.isNullOrEmpty()) {
                            val firstItem = cohostList[0]
                            val value = firstItem?.gemEarned ?: 0L
                            Log.e(TAG_PUSHER, "First gem_earned = $value")
                            binding.tvDiamond.text = Util.formatNumberToAbbreviation(value.toLong())
                        }
                    }
                }

                override fun onFailure(call: Call<StreamDetails>, t: Throwable) {
                    Log.e(TAG_PUSHER, "onResponse: ::::::::streamInfo:::")
                    t.printStackTrace()
                }
            })
        }
    }

}