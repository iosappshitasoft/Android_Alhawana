package com.app.camera


import android.Manifest.permission.*
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.*
import android.provider.MediaStore
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.alhawanajd.App
import com.app.alhawanajd.MainActivity
import com.app.alhawanajd.PrimeActivity
import com.app.alhawanajd.R
import com.app.alhawanajd.Utility
import com.app.alhawanajd.Video.FilterType
import com.app.alhawanajd.Video.ImageFilterType
import com.app.alhawanajd.Video.PostVideoActivity
import com.app.alhawanajd.Video.UploadSoundActivity
import com.app.alhawanajd.databinding.ActivityCameraNewBinding
import com.app.external.video_trimmer.utils.TrimType
import com.app.external.video_trimmer.utils.TrimVideo
import com.app.helper.CountDownAnimation
import com.app.helper.StorageUtils
import com.app.model.GetSet
import com.app.utils.AdminData
import com.app.utils.Constants
import com.app.utils.SharedPref
import com.daasuu.gpuv.camerarecorder.*
import com.daasuu.gpuv.egl.filter.GlBrightnessFilter
import com.daasuu.gpuv.egl.filter.GlFilter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import es.dmoral.toasty.Toasty
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import timber.log.Timber
import java.io.File


class CameraFragmentNew : Fragment(), CountDownAnimation.CountDownListener {

    private lateinit var binding: ActivityCameraNewBinding
    private lateinit var mContext: Context

    private var toggleClick = false
    private var isSelectedPosition = -1
    private var lensFacing = LensFacing.BACK
    private var isFlash = false
    private var isRecord = false
    private lateinit var inputVideoFile: File
    private lateinit var imageView2: GPUImageView
    private lateinit var pdLoading: ProgressDialog
    private lateinit var countDownAnimation: CountDownAnimation
    private var isCountDownRunning = false
    private var gpuCameraRecorder: GPUCameraRecorder? = null
    private var sampleGLView: GLSurfaceView? = null

    private lateinit var trimResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var selectSoundLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var postResultLauncher: ActivityResultLauncher<Intent>

    private var i = 0
    private var countDownTimer: CountDownTimer? = null
    private lateinit var getRecordTime: String // Example: "15" (in seconds)
    private lateinit var progressBar: ProgressBar
    private lateinit var frameLayout: FrameLayout
    private lateinit var soundLay: LinearLayout
    private lateinit var galleryLay: LinearLayout
    private lateinit var recordLayout: RelativeLayout
    private lateinit var cameraOptions: LinearLayout
    private lateinit var filterRecLay: RelativeLayout
    private lateinit var beforeVideoRecordingLayout: LinearLayout
    private lateinit var videoRecordingLayout: RelativeLayout
    private lateinit var textView: TextView
    private lateinit var filterRecyclerview: RecyclerView
    private lateinit var cameraPageCloseButton: ShapeableImageView
    private lateinit var tickRecord: ShapeableImageView
    private lateinit var stopRecordIcon: ShapeableImageView
    private lateinit var flashIcon: ShapeableImageView
    private lateinit var setfilter: GlFilter

    private var soundId: String? = null
    private var soundUrl: String? = null
    private var video_id: String? = null
    private var trimmedVideo: String? = null
    private var gethashTag: String? = null
    private var isCamera = false
    private var reShootDialog: BottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = requireContext()

        trimResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
           /* if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val path = result.data!!.getStringExtra(TrimVideo.TRIMMED_VIDEO_PATH)
                if (!path.isNullOrEmpty()) {
                    val intent = Intent(mContext, PostVideoActivity::class.java)
                    intent.putExtra("videoPath", path)
                    startActivity(intent)
                }
            }*/

            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                result.data?.let { data ->
                    if (isCamera) {
                        soundId = data.getStringExtra(Constants.TAG_SOUND_ID)
                        soundUrl = data.getStringExtra(Constants.TAG_SOUND_URL)
                        trimmedVideo = data.getStringExtra(TrimVideo.TRIMMED_VIDEO_PATH)
                        gotoPostVideoPage("camera")
                    } else {
                        soundId = data.getStringExtra(Constants.TAG_SOUND_ID)
                        soundUrl = data.getStringExtra(Constants.TAG_SOUND_URL)
                        trimmedVideo = data.getStringExtra(TrimVideo.TRIMMED_VIDEO_PATH)
                        gotoPostVideoPage("gallery")
                    }
                }
            }
        }

        selectSoundLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val id = result.data!!.getStringExtra("sound_id")
                val url = result.data!!.getStringExtra("sound_url")
                // Handle selected sound if needed
            }
        }

        galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                isCamera = false
                val selectedVideoUri = result.data!!.data
                selectedVideoUri?.let { uri ->
                    // Check file size and type
                    val mimeType = requireContext().contentResolver.getType(uri)
                    if (mimeType == null || !mimeType.startsWith("video")) {
                        Toast.makeText(requireContext(), "Please select a video file only.", Toast.LENGTH_SHORT).show()
                        return@let
                    }

                    val fileSize = getFileSize(uri)
                    if (fileSize > MAX_VIDEO_SIZE_BYTES) {
                        Toast.makeText(
                            requireContext(),
                            "Video size (${formatFileSize(fileSize)}) exceeds 50 MB limit",
                            Toast.LENGTH_LONG
                        ).show()
                        return@let
                    }
                    
                    TrimVideo.activity(uri.toString())
                        .setTrimType(TrimType.MIN_MAX_DURATION)
                        .setAccurateCut(true)
                        .setCompressOption(null)
                        .setSoundId(null)
                        .setSoundUrl(null)
                        .setMediaFrom("gallery")
                        .setMinToMax(5L, AdminData.max_video_duration.toLong())
                        .start(requireActivity(), trimResultLauncher)
                }
            }
        }

        postResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Video posted successfully — do nothing or refresh UI
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                result.data?.let { data ->
                    if (data.getBooleanExtra("reshoot", false)) {
                        data.getStringExtra(Constants.TAG_SOUND_ID)?.let {
                            soundId = it
                        }
                        data.getStringExtra(Constants.TAG_SOUND_URL)?.let {
                            soundUrl = it
                        }
                        Timber.d("postResultLauncher: $soundId, $soundUrl")
                        reShoot()
                    } else {
                        requireActivity().finish()
                    }
                }
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ActivityCameraNewBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    private fun initViews() {
        pdLoading = ProgressDialog(mContext, R.style.CameraAlertDialog)
        inputVideoFile = File(StorageUtils.getInstance(mContext).getExtCacheDir(), Constants.TAG_VIDEO_TRIM + ".mp4")
        imageView2 = GPUImageView(mContext as Activity?)
        frameLayout = binding.wrapView
        soundLay = binding.soundLay
        galleryLay = binding.gallaryLay
        recordLayout = binding.recordLayout
        cameraOptions = binding.cameraOptions
        filterRecLay = binding.filterRecLay
        beforeVideoRecordingLayout = binding.BeforeVideoRecordingLayout
        videoRecordingLayout = binding.videoRecordingLayout
        progressBar = binding.progressBar
        textView = binding.textView
        filterRecyclerview = binding.filterRecyclerview


        filterRecyclerview.setLayoutManager(
            LinearLayoutManager(
                requireActivity(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        val filterAdapter = FilterAdapter(requireActivity())
        filterRecyclerview.setAdapter(filterAdapter)
        cameraPageCloseButton = binding.cameraPageCloseButton
        tickRecord = binding.tickRecord
        stopRecordIcon = binding.stopRecordIcon
        flashIcon = binding.flashIcon
        frameLayout = binding.wrapView
        initCountDownAnimation()
        setfilter = GlBrightnessFilter().apply { setBrightness(0.1f) }
        // Stop recording
        stopRecordIcon.setOnClickListener {
            App.preventMultipleClick(stopRecordIcon)
            stopRecording()
        }

//        binding.imageLay.setOnClickListener {
//            App.preventMultipleClick(tickRecord)
////            val intent = Intent(requireContext(), ImageAndAudioMixerActivity::class.java)
////            startActivity(intent)
//
//        }

       // Tick to confirm video
        tickRecord.setOnClickListener {
            App.preventMultipleClick(tickRecord)
            stopRecording()
        }

        // Sound picker
        soundLay.setOnClickListener {
            App.preventMultipleClick(soundLay)
            val intent = Intent(requireContext(), UploadSoundActivity::class.java).apply {
                putExtra(Constants.TAG_SOUND_ID, soundId)
            }
            selectSoundLauncher.launch(intent)
        }

       // Gallery picker (with premium check)
        galleryLay.setOnClickListener {
            App.preventMultipleClick(galleryLay)
            if (SharedPref.getString(SharedPref.IS_PREMIUM_MEMBER, "false") == "false" && !AdminData.galleryUpload) {
                launchGallery()
            } else {
                App.makeToast(requireContext().getString(R.string.prime_alert))
                val intent = Intent(requireContext(), PrimeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
                startActivity(intent)
            }
        }

        // Flash toggle
        binding.flashLay.setOnClickListener {
            gpuCameraRecorder?.takeIf { it.isFlashSupport }?.let {
                it.switchFlashMode()
                it.changeAutoFocus()
                if (!isFlash) {
                    isFlash = true
                    flashIcon.setImageResource(R.drawable.flashon)
                } else {
                    isFlash = false
                    flashIcon.setImageResource(R.drawable.flashoff)
                }
            } ?: Toasty.info(requireContext(), R.string.flash_not_support, Toasty.LENGTH_SHORT).show()
        }

       // Timer / countdown
        binding.timerLay.setOnClickListener {
            App.preventMultipleClick(binding.timerLay)
            if (!isCountDownRunning) {
                cameraOptions.visibility = View.GONE
                recordLayout.visibility = View.GONE
                startCountDownAnimation()
            }
        }

      // Filter open
        binding.filterLay.setOnClickListener {
            App.preventMultipleClick(binding.filterLay)
            cameraOptions.visibility = View.GONE
            recordLayout.visibility = View.GONE
            filterRecLay.visibility = View.VISIBLE
        }

      // Filter close
        binding.filterClose.setOnClickListener {
            filterRecLay.visibility = View.GONE
            cameraOptions.visibility = View.VISIBLE
            recordLayout.visibility = View.VISIBLE
        }

       // Flip / switch camera
        binding.flipLay.setOnClickListener {
            releaseCamera()
            lensFacing = if (lensFacing == LensFacing.BACK) LensFacing.FRONT else LensFacing.BACK
            setfilter = FilterType.gpuVideoView(0, requireContext())
            toggleClick = true
        }

       // Record button
        binding.recordIcon.setOnClickListener {
            App.preventMultipleClick(binding.recordIcon)
            stopRecordIcon.isEnabled = false
            tickRecord.isEnabled = false
            stopRecordIcon.alpha = 0.3f
            tickRecord.alpha = 0.3f
            recordVideo()
        }

       // Close button (back pressed)
        cameraPageCloseButton.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(this)
            }
        }
    }


    private fun startCountDownAnimation() {
        val scaleAnimation = ScaleAnimation(
            1.0f, 0.0f,
            1.0f, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )

        val alphaAnimation = AlphaAnimation(1.0f, 0.0f)

        val animationSet = AnimationSet(false).apply {
            addAnimation(scaleAnimation)
            addAnimation(alphaAnimation)
        }

        countDownAnimation.setAnimation(animationSet)
        countDownAnimation.setStartCount(5)
        countDownAnimation.start()
        isCountDownRunning = true
    }

    private fun gotoPostVideoPage(uploadVideoType: String) {
        val intent = Intent(requireContext(), PostVideoActivity::class.java)
        soundId?.let { intent.putExtra(Constants.TAG_SOUND_ID, it) }
        soundUrl?.let { intent.putExtra(Constants.TAG_SOUND_URL, it) }
        video_id?.let { intent.putExtra(Constants.TAG_VIDEO_ID, it) }
        trimmedVideo?.let { intent.putExtra(Constants.TAG_VIDEO_PATH, it) }
        intent.putExtra(Constants.TAG_SELECT_HASH_TAG, gethashTag)
        intent.putExtra(Constants.TAG_TYPE, uploadVideoType)
        postResultLauncher.launch(intent)
        requireActivity().overridePendingTransition(R.anim.anim_rtl_right_in, R.anim.anim_rtl_right_out)
    }


    private fun initCameraView() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(CAMERA, RECORD_AUDIO)
        } else {
            arrayOf(CAMERA, WRITE_EXTERNAL_STORAGE, RECORD_AUDIO)
        }

        if (permissions.any { ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED }) {
            Timber.w("Missing permissions for initCameraView")
            return
        }

        binding.wrapView.removeAllViews()
        sampleGLView = GLSurfaceView(requireContext())
        binding.wrapView.addView(sampleGLView)
        gpuCameraRecorder = GPUCameraRecorderBuilder(requireContext() as Activity?, sampleGLView)
            .cameraRecordListener(object : CameraRecordListener {
                override fun onGetFlashSupport(flashSupport: Boolean) {}
                override fun onRecordComplete() {}
                override fun onRecordStart() { timerStart() }
                override fun onError(exception: java.lang.Exception?) {}
                override fun onCameraThreadFinish() {
                    if (toggleClick) {
                        activity?.runOnUiThread { initCameraView() }
                    }
                    toggleClick = false
                }
                override fun onVideoFileReady() {

                    activity?.runOnUiThread {
                        binding.videoRecordingLayout.visibility = View.GONE
                        binding.recordLayout.visibility = View.VISIBLE
                        binding.BeforeVideoRecordingLayout.visibility = View.VISIBLE
                        binding.cameraOptions.visibility = View.VISIBLE
                        binding.soundLay.visibility = View.GONE
                        binding.gallaryLay.visibility = View.VISIBLE
                        binding.progressBar.progress = 0

                        if (isAdded) {
                            if (reShootDialog?.isShowing == true) {
                                timerCancel()
                                reShootDialog?.dismiss()
                            } else {
                                isCamera = true
                                TrimVideo.activity(inputVideoFile.absolutePath)
                                    .setTrimType(TrimType.MIN_MAX_DURATION)
                                    .setAccurateCut(true)
                                    .setCompressOption(null)
                                    .setSoundId(soundId)
                                    .setSoundUrl(soundUrl)
                                    .setMediaFrom("camera")
                                    .setMinToMax(5L, AdminData.max_video_duration.toLong())
                                    .start(this@CameraFragmentNew, trimResultLauncher)
                            }
                        }
                    }
                }
            })
            .lensFacing(lensFacing)
            .mute(false)
            .filter(GlBrightnessFilter().apply { setBrightness(0.1f) })
            .build()
    }

    private fun initCountDownAnimation() {
        countDownAnimation = CountDownAnimation(binding.textView, 5)
        countDownAnimation.setCountDownListener(this)
    }

    private fun timerStart() {
        i = 0
        val conSec = getRecordTime.toLong() * 1000
        progressBar.max = getRecordTime.toInt()
        Timber.d("timerStart: getRec max %s", getRecordTime.toInt())

        countDownTimer = object : CountDownTimer(conSec, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                i++
                val progress = (i * getRecordTime.toInt() / (conSec / 1000)).toInt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress(progress, true)
                } else {
                    progressBar.progress = progress
                }

                if (i == 3) {
                    view?.findViewById<View>(R.id.stopRecordIcon)?.apply {
                        alpha = 1f
                        isEnabled = true
                    }
                    view?.findViewById<View>(R.id.tickRecord)?.apply {
                        alpha = 1f
                        isEnabled = true
                    }
                }
            }

            override fun onFinish() {
                stopRecording()
            }
        }.start()
    }

    private fun timerCancel() {
        countDownTimer?.cancel()
        progressBar.progress = 0
        progressBar.invalidate()
    }

    private fun releaseCamera() {
        try {
            sampleGLView?.onPause()

            gpuCameraRecorder?.let {
                it.stop()
                it.release()
                gpuCameraRecorder = null
            }

            val wrapView = view?.findViewById<FrameLayout>(R.id.wrap_view)
            sampleGLView?.let {
                wrapView?.removeView(it)
                sampleGLView = null
            }
        } catch (e: Exception) {
            Timber.i("exception %s", e.message)
        }
    }

    private fun stopRecording() {
        gpuCameraRecorder?.stop()
        isRecord = false
    }

    override fun onCountDownEnd(animation: CountDownAnimation?) {
        isCountDownRunning = false
        if (!isRecord) {
            gpuCameraRecorder?.start(inputVideoFile.absolutePath)
            isRecord = true
        }
    }

    private inner class FilterAdapter(private val context: Context) :
        RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

        init {
            if (isSelectedPosition == -1) isSelectedPosition = 0
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_filter, parent, false)
            return FilterViewHolder(view)
        }

        override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
            holder.filterTitle.text = ImageFilterType.gpuImageView()[position].name
            holder.setGpuImage(holder.bmp, ImageFilterType.gpuImageView(position, context))

            holder.itemView.setOnClickListener {
                isSelectedPosition = position

                if (lensFacing == LensFacing.FRONT && position == 0) {
                    val brightnessFilter = GlBrightnessFilter().apply { setBrightness(0.1f) }
                    gpuCameraRecorder?.setFilter(brightnessFilter)
                } else {
                    gpuCameraRecorder?.setFilter(FilterType.gpuVideoView(position, requireContext()))
                }

                notifyDataSetChanged()
            }

            holder.imageView.strokeWidth = if (isSelectedPosition == position) 2.5f else 0f
        }

        override fun getItemCount(): Int = ImageFilterType.gpuImageView().size

        inner class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val filterTitle: TextView = itemView.findViewById(R.id.filterTitle)
            val imageView: ShapeableImageView = itemView.findViewById(R.id.imageView)
            val bmp: Bitmap = drawableToBitmap(
                ResourcesCompat.getDrawable(resources, R.drawable.filter_sample_image, requireContext().theme)!!
            )

            private var gpuImage: GPUImage? = null

            fun setGpuImage(bitmap: Bitmap, filter: GPUImageFilter) {
                if (gpuImage == null) {
                    gpuImage = GPUImage(requireContext())
                }
                gpuImage?.setFilter(filter)
                imageView.setImageBitmap(gpuImage?.getBitmapWithFilterApplied(bitmap))
            }
        }
    }


    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            drawable.bitmap?.let {
                return it
            }
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun checkRecordPermissions() {
        val permissions = arrayOf(
            CAMERA,
            WRITE_EXTERNAL_STORAGE,
            RECORD_AUDIO
        )

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            requestPermissions(permissions, Constants.CAMERA_REQUEST_CODE)
        } else {
            initCameraView()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.CAMERA_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initCameraView()
            } else {
                Toast.makeText(requireContext(), "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDots(count: Int, outDots: String = ""): String {
        if (count <= 0) return outDots
        return getDots(count - 1, outDots) + "."
    }

    private fun backPressed() {
        if (!isRecord) {
            requireActivity().overridePendingTransition(R.anim.anim_stay, R.anim.anim_slide_down)
            requireActivity().finish()
        } else {
            openReshootDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        App.setCurrentActivity(requireActivity()) // or pass the fragment if needed

        requireActivity().overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_stay)

       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            initCameraView()
        } else {
            checkRecordPermissions()
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkRecordPermissions13()
        } else {
            checkRecordPermissions()
        }
        
    }

    private fun checkRecordPermissions13() {
        val permissions = mutableListOf(
            CAMERA,
            RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(READ_MEDIA_VIDEO)
            permissions.add(READ_MEDIA_IMAGES)
            permissions.add(READ_MEDIA_AUDIO)
            permissions.add(POST_NOTIFICATIONS)
        } else {
            permissions.add(WRITE_EXTERNAL_STORAGE)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allGranted) {
            requestPermissions(permissions.toTypedArray(), Constants.CAMERA_REQUEST_CODE)
        } else {
            initCameraView()
        }
    }


    override fun onPause() {
        super.onPause()
        releaseCamera()
        gpuCameraRecorder?.let {
            it.stop()
            it.release()
            gpuCameraRecorder = null
        }
    }

    override fun onStop() {
        super.onStop()
        timerCancel()
        sampleGLView?.let {
            frameLayout.removeView(it)
            sampleGLView = null
        }
    }


    private fun recordVideo() {
        gpuCameraRecorder?.let {
            getRecordTime = GetSet.getMax_video_duration().toString()
            inputVideoFile = File(
                StorageUtils.getInstance(requireContext()).extCacheDir,
                Constants.TAG_VIDEO_TRIM + Utility.VIDEO_FORMAT
            )
            it.start(inputVideoFile.absolutePath)
            cameraOptions.visibility = View.GONE
            soundLay.visibility = View.GONE
            galleryLay.visibility = View.GONE
            isRecord = true
            recordLayout.visibility = View.VISIBLE
            beforeVideoRecordingLayout.visibility = View.GONE
            videoRecordingLayout.visibility = View.VISIBLE
        }
    }

    private fun launchGallery() {
        if (!isRecord) {
            val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val intent = Intent(Intent.ACTION_PICK, collection).apply {
                type = "video/*"
            }

            galleryResultLauncher.launch(Intent.createChooser(intent, "Select Video"))
        }
    }

    companion object {
        private const val MAX_VIDEO_SIZE_BYTES = 50 * 1024 * 1024L // 50 MB
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            requireContext().contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.statSize
            } ?: 0L
        } catch (e: Exception) {
            // Fallback: try to get size from file path
            try {
                uri.path?.let { path ->
                    File(path).takeIf { it.exists() }?.length() ?: 0L
                } ?: 0L
            } catch (_: Exception) {
                0L
            }
        }
    }

    private fun formatFileSize(bytes: Long): String {
        val mb = bytes / (1024.0 * 1024.0)
        return String.format("%.2f MB", mb)
    }

    private fun reShoot() {
        try {
            progressBar.setProgress(0)
            videoRecordingLayout.setVisibility(View.GONE)
            recordLayout.setVisibility(View.VISIBLE)
            beforeVideoRecordingLayout.setVisibility(View.VISIBLE)
            cameraOptions.setVisibility(View.VISIBLE)
            soundLay.setVisibility(View.GONE)
            galleryLay.setVisibility(View.VISIBLE)
        } catch (e: java.lang.Exception) {
            Timber.i("view exception %s", e.message)
        }
    }


    private fun openReshootDialog() {
        val reShootSheetView = layoutInflater.inflate(R.layout.bottom_sheet_reshoot, null)
        reShootDialog = BottomSheetDialog(requireContext(), R.style.reshootBottomDialog).apply {
            setContentView(reShootSheetView)
            behavior.isHideable = false
        }

        val titleTextView = reShootSheetView.findViewById<TextView>(R.id.title)
        titleTextView.setText(R.string.alert_close_this_video)

        reShootSheetView.findViewById<View>(R.id.reshoot).setOnClickListener {
            if (reShootDialog?.isShowing == true) {
                gpuCameraRecorder?.stop()
                isRecord = false
                timerCancel()
                reShootDialog?.dismiss()
            }
        }

        reShootSheetView.findViewById<View>(R.id.txtcancel).setOnClickListener {
            reShootDialog?.dismiss()
        }

        reShootSheetView.findViewById<View>(R.id.exit).setOnClickListener {
            requireActivity().finish()
        }

        reShootDialog?.show()
    }
}
