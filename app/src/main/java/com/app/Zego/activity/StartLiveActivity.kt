package com.app.Zego.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.app.Zego.ApiBase.ZegoApiClient
import com.app.Zego.BottomSheet.AddCategoryBottomSheet
import com.app.Zego.BottomSheet.LivePrivacyModeBtmSheet
import com.app.Zego.BottomSheet.RoomLayoutBottomSheets
import com.app.Zego.BottomSheet.SettingsBottomSheet
import com.app.Zego.Model.CreateStreamModel
import com.app.Zego.Model.TokenResponse
import com.app.Zego.Pusher.PusherRepository
import com.app.Zego.Pusher.PusherViewModel
import com.app.Zego.Pusher.PusherViewModelFactory
import com.app.Zego.internal.ZEGOLiveStreamingManager
import com.app.Zego.internal.sdk.ZEGOSDKManager
import com.app.Zego.internal.sdk.basic.ZEGOSDKCallBack
import com.app.addon.livestream.external.utils.ApiInterface

import com.app.addon.livestream.external.utils.Constants
import com.app.addon.livestream.external.utils.StreamConstants
import com.app.alhawanajd.App
import com.app.alhawanajd.BaseActivity
import com.app.alhawanajd.R
import com.app.alhawanajd.databinding.ActivityStartLiveBinding
import com.app.helper.NetworkReceiver
import com.app.model.GetSet
import com.app.model.ProfileRequest
import com.app.model.ProfileResponse
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.constants.ZegoVideoCodecID
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset
import im.zego.zegoexpress.entity.ZegoVideoConfig
import im.zego.zegoexpress.constants.ZegoVideoMirrorMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable

class StartLiveActivity : BaseActivity(), View.OnClickListener {

    private lateinit var binding: ActivityStartLiveBinding
    private lateinit var context: Context
    private lateinit var pref: SharedPreferences

     private var bottomCameraDialog: BottomSheetDialog? = null

    private var title: String? = null
    private var streamName: String? = null
    private var streamToken: String? = null
    private var inviteLink: String? = null
    private var type: String = "single"
    var enableCamera: Boolean = true
    var enableMic: Boolean = true
    var frontFacing: Boolean = true
    var apiInterface: ApiInterface? = null
    var selectedMutual: List<ProfileResponse> = ArrayList()
    var selectedMutualId: java.util.ArrayList<String> = ArrayList()
    var isMirror: Boolean = true

    private val imagePickerLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            Glide.with(this)
                .load(uri)
                .error(R.drawable.addtitle_img)
                .placeholder(R.drawable.addtitle_img)
                .into(binding.includePublish.profileImage)
        }
    }

    companion object {
        private const val TAG = "StartLiveActivity"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartLiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this

        // Transparent StatusBar (Fullscreen Look)
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        intent?.let {
            title = it.getStringExtra(StreamConstants.TAG_TITLE)
            streamName = it.getStringExtra(StreamConstants.TAG_STREAM_NAME)
            streamToken = it.getStringExtra(StreamConstants.TAG_STREAM_TOKEN)
        }

        pref = getSharedPreferences(Constants.SHARED_PREFERENCE, MODE_PRIVATE)
        apiInterface = ZegoApiClient.getClient(context).create(ApiInterface::class.java)
        Log.e(TAG,"userId :" + GetSet.getUserId() + "  name : "+GetSet.getName())
        //for zego
        signInZEGOSDK(GetSet.getUserId(), GetSet.getName()) { errorCode, msg -> }

        initViews()
        initCamera()

    }

    override fun onNetworkChanged(isConnected: Boolean) {
        Log.e(TAG,"Netwrok");
    }


    private fun initCamera() {
        // Create video config with a preset (1080p recommended)
        val videoConfig = ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_1080P)
        // Override defaults if needed
        videoConfig.setCaptureResolution(720, 1280)  // capture resolution
        videoConfig.setEncodeResolution(720, 1280)   // encoded resolution
        videoConfig.setVideoFPS(30)                   // 30 FPS for smoothness
        videoConfig.setVideoBitrate(3000)             // bitrate in kbps (3 Mbps good for 1080p)
        videoConfig.setCodecID(ZegoVideoCodecID.H265) // or H265 if supported

        // Apply video config before publishing
        ZegoExpressEngine.getEngine().setVideoConfig(videoConfig)
        Log.e(TAG,"token :" + GetSet.getStreamToken())

        if (GetSet.getStreamToken().isNullOrEmpty()) {
            getStreamToken()
        }
    }

    private fun getStreamToken() {
        val call: Call<TokenResponse>? = apiInterface?.getStreamToken(
            GetSet.getUserId()
        )
        call?.enqueue(object : Callback<TokenResponse?> {
            override fun onResponse(
                call: Call<TokenResponse?>,
                response: Response<TokenResponse?>
            ) {
                val tokenResponse = response.body()
                Log.e(TAG, "streamToken : " + Gson().toJson(response.body()))
                if (tokenResponse != null) {
                    GetSet.setStreamToken(tokenResponse.stream_token)
                }
            }

            override fun onFailure(call: Call<TokenResponse?>, t: Throwable) {
            }
        })
    }


    private fun initViews() {
        pauseExternalAudio(this)
        ZEGOSDKManager.getInstance().expressService.openCamera(true)
        ZEGOSDKManager.getInstance().expressService.openMicrophone(true)
        ZegoExpressEngine.getEngine().useFrontCamera(true)

        ZEGOSDKManager.getInstance()
        binding.hostView.startPreviewOnly()

        val radius = 24f
        val decorView = window.decorView
        val target = binding.blurTarget
        val windowBackground = decorView.background

        binding.includePublish.topBlurView.setupWith(target)
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(radius)

        binding.includePublish.categoryLayout.setupWith(target)
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(radius)

        binding.includePublish.btnGoLive.setOnClickListener(this)
        binding.includePublish.imgCamerachange.setOnClickListener(this)
        binding.includePublish.llMirror.setOnClickListener(this)
        binding.includePublish.btnCancel.setOnClickListener(this)
        binding.includePublish.llLayoutOption.setOnClickListener(this)
        binding.includePublish.imgSelectstreamtype.setOnClickListener(this)
        binding.includePublish.llSettings.setOnClickListener(this)
        binding.includePublish.rlProfileImage.setOnClickListener(this)
        binding.includePublish.llMic.setOnClickListener(this)
        binding.includePublish.llTabVideo.setOnClickListener(this)
        binding.includePublish.llTabLive.setOnClickListener(this)
        binding.includePublish.llTabAudio.setOnClickListener(this)
        binding.includePublish.llTabShare.setOnClickListener(this)


        // Camera permission check
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
          //  startCamera(if (isFrontCamera) "front" else "back")
        }

        // Title input limit
        binding.includePublish.edtTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                if (editable?.length ?: 0 >= 80) {
                    Toast.makeText(context, getString(R.string.reachlimt), Toast.LENGTH_SHORT).show()
                }
            }
        })

        Glide.with(this)
            .load(GetSet.getUserImage())
            .error(R.drawable.addtitle_img)
            .placeholder(R.drawable.addtitle_img)
            .into(binding.includePublish.profileImage)

    }


    override fun onClick(v: View?) {
        when (v?.id) {
            binding.includePublish.imgCamerachange.id  -> {
                /*
                frontFacing = !frontFacing
                ZegoExpressEngine.getEngine().useFrontCamera(frontFacing)
                isMirror = frontFacing
                ZegoExpressEngine.getEngine().setVideoMirrorMode(
                    if (isMirror) ZegoVideoMirrorMode.BOTH_MIRROR else ZegoVideoMirrorMode.NO_MIRROR
                )
                */
                enableCamera = !enableCamera
                ZEGOSDKManager.getInstance().expressService.openCamera(enableCamera)
                if (enableCamera) {
                    binding.includePublish.imgCameraIcon.setImageResource(R.drawable.ic_camera_on)
                } else {
                    binding.includePublish.imgCameraIcon.setImageResource(R.drawable.ic_camera_off)
                }
            }
            binding.includePublish.llMirror.id -> {
                isMirror = !isMirror
                ZegoExpressEngine.getEngine().setVideoMirrorMode(
                    if (isMirror) ZegoVideoMirrorMode.BOTH_MIRROR else ZegoVideoMirrorMode.NO_MIRROR
                )
            }
            binding.includePublish.btnCancel.id -> {
                finish()
            }
            binding.includePublish.llLayoutOption.id -> {
                RoomLayoutBottomSheets.getInstance(this, supportFragmentManager) { selectedOption ->
                    // Handle option selection if needed
                }
            }
            binding.includePublish.imgSelectstreamtype.id -> {
                AddCategoryBottomSheet.getInstance(this, supportFragmentManager) { selectedCategory ->
                    binding.includePublish.tvCategory.text = selectedCategory.name
                }
            }
            binding.includePublish.llSettings.id -> {
                SettingsBottomSheet.getInstance(this, supportFragmentManager) {
                    // Handle done clicked if needed
                }
            }
            binding.includePublish.llMic.id -> {
                enableMic = !enableMic
                ZEGOSDKManager.getInstance().expressService.openMicrophone(enableMic)
                if (enableMic) {
                    binding.includePublish.imgMicIcon.setImageResource(R.drawable.ic_mic)
                } else {
                    binding.includePublish.imgMicIcon.setImageResource(R.drawable.ic_mic_off)
                }
            }
            binding.includePublish.rlProfileImage.id -> {
                imagePickerLauncher.launch("image/*")
            }
            binding.includePublish.llTabVideo.id -> {
                val intent = Intent(this, com.app.camera.CameraLiveChooserActivity::class.java)
                startActivity(intent)
                finish()
            }

            binding.includePublish.btnGoLive.id  -> {
                if (TextUtils.isEmpty(binding.includePublish.edtTitle.text.toString().trim())) {
                    Toast.makeText(this, getString(R.string.txt_addtitle), Toast.LENGTH_SHORT).show()
                } else if (!NetworkReceiver.isConnected()) {
                    Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
                } else {
                    hideSoftKeyboard(this)
                    Log.e(TAG, "CameraType: ${if (enableCamera) "front" else "back"}")
                    Log.e(TAG, "Type: $type")
                    pauseExternalAudio(this@StartLiveActivity)
                    binding.includePublish.btnGoLive.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    getProfile()
                   // openLiveModeDialog()

                }
            }
        }
    }


    private fun openBottomSheetLiveSelection() {
        if (bottomCameraDialog == null) {
            val bottomSheet = layoutInflater.inflate(R.layout.bottomsheet_live_selection, null)
            bottomCameraDialog = BottomSheetDialog(this, R.style.VideoCommentNewDialog).apply {
                setContentView(bottomSheet)
            }
        }
        bottomCameraDialog?.show()
    }

    private fun pauseExternalAudio(context: Context) {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (manager.isMusicActive) {
            Constants.isExternalPlay = true
            val i = Intent("com.android.music.musicservicecommand")
            i.putExtra("command", "pause")
            context.sendBroadcast(i)
        }
    }

    private fun hideSoftKeyboard(activity: Activity) {
        try {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity.currentFocus
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun signInZEGOSDK(userID: String, userName: String, callback: ZEGOSDKCallBack) {
        if(userID.isNotBlank()){
            ZEGOSDKManager.getInstance().connectUser(userID, userName, callback)
        }
    }

    private fun openLiveModeDialog() {
        LivePrivacyModeBtmSheet.getInstance(
            this,
            supportFragmentManager,
            onBtnClicked = { mode ->
                if (mode == "private") {
                    /*Intent(this, MutualListActivity::class.java)
                        .apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            putExtra("from", "publish")
                            putExtra(Constants.TAG_INTENT_DATA, selectedMutual as Serializable)
                            putExtra("mutualfollowers", selectedMutual.size)
                            startActivityForResult(this, 500)
                        }*/
                } else {
                    getStreamName(mode)
                }
            }
        )
    }

    private fun getStreamName(mode: String) {
        if (NetworkReceiver.isConnected()) {
            binding.includePublish.btnGoLive.isEnabled = false
            showProgress()
            ZEGOLiveStreamingManager.getInstance().init()
            Log.e(TAG, "CreateStream:: request" + GetSet.getUserId())

            apiInterface?.getLiveStreamName(
                GetSet.getUserId(),
                "single"
            )?.enqueue(object : Callback<CreateStreamModel> {
                override fun onResponse(
                    call: Call<CreateStreamModel>,
                    response: Response<CreateStreamModel>
                ) {
                    if (response.isSuccessful) {
                        Log.e(TAG, "onResponse: CreateStream::" + Gson().toJson(response.body()))
                        if (response.body()?.status.toString() == Constants.TAG_TRUE) {
                            title = binding.includePublish.edtTitle.text.toString().trim()
                                .takeIf { it.isNotEmpty() } ?: "Chobi Live"
                            streamName = response.body()?.streamName.toString()
                            streamToken = response.body()?.stream_token.toString()
                            inviteLink = response.body()?.shareStreamLink.toString()
                            PrepareToLive(mode)
                        } else {
                            Toast.makeText(
                                this@StartLiveActivity,
                                response.body()?.message.toString() + "",
                                Toast.LENGTH_SHORT
                            ).show()
                            hideProgress()
                        }
                    } else {
                        binding.includePublish.btnGoLive.isEnabled = true
                        hideProgress()
                    }
                }

                override fun onFailure(call: Call<CreateStreamModel>, t: Throwable) {
                    Log.e(TAG, "onFailure: CreateStream:", t)
                    hideProgress()
                }

            })
        }
    }


    fun getProfile() {
        if (NetworkReceiver.isConnected()) {
            GetSet.getUserId()?.let { userId ->

                val request = ProfileRequest().apply {
                    this.userId = userId
                    this.profileId = userId
                }

                val call = apiInterface!!.getProfile(request)

                call.enqueue(object : Callback<ProfileResponse> {

                    override fun onResponse(
                        call: Call<ProfileResponse>,
                        response: Response<ProfileResponse>
                    ) {

                        if (response.isSuccessful && response.body() != null) {

                            Log.e("Profile Response", Gson().toJson(response.body()))
                            val profile = response.body()
                            try {
                                profile?.let {
                                    if (it.status != null && it.status == Constants.TAG_TRUE) {
                                        binding.progressBar.visibility = View.GONE
//                                         if(it.followers>=1000) {
                                            getStreamName("public")
                                        /*}else{
                                             App.makeToast(this@StartLiveActivity.getString(R.string.followers_alert))
                                         }*/
                                    }else{
                                        binding.progressBar.visibility = View.GONE
                                    }
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                        t.printStackTrace()
                        Log.e(TAG, "onFailure: :::::::profile::::${t.message}")
                        binding.progressBar.visibility = View.GONE
                    }
                })
            }
        }
    }

    fun showProgress() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun hideProgress() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun PrepareToLive(mode: String) {
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            ZegoExpressEngine.getEngine().stopPreview()
            binding.includePublish.edtTitle.text.clear()
            binding.includePublish.btnGoLive.isEnabled = true

            signInZEGOSDK(
                GetSet.getUserId(),
                GetSet.getName()
            ) { errorCode, msg ->
                Log.e(TAG, "PrepareToLive: :::::Zego login errorcode:$errorCode , message: $msg")
                if (errorCode == 0 || errorCode == 6000111) {
                    Intent(this@StartLiveActivity, PkLiveStreamingActivity::class.java).apply {
                        putExtra("from", "host")
                        putExtra("type", "single")
                        putExtra("cameraStatus", frontFacing)
                        putExtra("publisherName", GetSet.getName())
                        putExtra(StreamConstants.TAG_TITLE, title)
                        putExtra(StreamConstants.TAG_STREAM_NAME, streamName)
                        putExtra(StreamConstants.TAG_INVITE_LINK, inviteLink)
                        putExtra(StreamConstants.TAG_STREAM_TOKEN, streamToken)
                        putExtra("host", true)
                        putExtra("liveID", streamName)
                        putExtra("hostId", GetSet.getUserId())
                        putExtra("mode", mode)
                        putExtra("enableCamera", enableCamera)
                        putExtra("enableMic", enableMic) //here passes static data
                        if (mode == "private") {
                            putStringArrayListExtra(
                                StreamConstants.TAG_GROUP_MEMBERS_ID,
                                selectedMutualId
                            )
                            putExtra(
                                StreamConstants.TAG_GROUP_MEMBERS,
                                selectedMutual as Serializable
                            )
                        }
                        hideProgress()
                        startActivity(this)
                        finishAffinity() // Closes all activities in the current task
                    }
                }
            }

        }, 1000)
    }

}
