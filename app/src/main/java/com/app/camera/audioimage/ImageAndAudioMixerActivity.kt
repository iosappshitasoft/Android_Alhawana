package com.app.camera.audioimage

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.app.ProgressDialog
import android.media.MediaPlayer
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.content.FileProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.slider.RangeSlider
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.app.alhawanajd.App
import com.app.alhawanajd.BaseActivity
import com.app.alhawanajd.MainActivity
import com.app.alhawanajd.R
import com.app.alhawanajd.Video.PostVideoActivity
import com.app.alhawanajd.Video.UploadSoundActivity
import com.app.camera.CameraLiveChooserActivity
import com.app.utils.Constants
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.random.Random
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.Locale
import kotlin.collections.get
import kotlin.math.min
import kotlin.math.sin


class ImageAndAudioMixerActivity : BaseActivity() {

    private var imageUri: Uri? = null
    private var audioUri: Uri? = null
    private var cameraImageUri: Uri? = null
    private var cameraVideoUri: Uri? = null

    private lateinit var previewImage: ImageView
    private var isVideoSelected = false
    private lateinit var rvThumbs: RecyclerView
    private lateinit var thumbsAdapter: SelectedImagesAdapter
    private lateinit var btnMusic: LinearLayout
    private lateinit var btnOverlay: LinearLayout
    private lateinit var btnCamera: LinearLayout
    private lateinit var btnNext: Button
    private lateinit var topAddAudioPill: LinearLayout
    private lateinit var btnAddAudio: ImageView
    private lateinit var btnAdd: FloatingActionButton
    private lateinit var txtCounter: TextView
    private lateinit var btnClose: ImageButton
    private val selectedMedia: MutableList<Uri> = mutableListOf()
    private lateinit var selectSoundLauncher: ActivityResultLauncher<Intent>
    private var soundId: String? = null
    private lateinit var imgAudioArt: ImageView
    private lateinit var txtAddAudio: TextView
    private lateinit var txtAudioSubtitle: TextView
    private var mixProgressDialog: ProgressDialog? = null
    private var generatedVideoFile: File? = null

    companion object {
        private const val TAG = "ImageAudioMixer"
        private const val MAX_VIDEO_SIZE_BYTES = 10 * 1024 * 1024L // 50 MB
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_audio_mixer)
        previewImage = findViewById(R.id.previewImage)
        rvThumbs = findViewById(R.id.rvThumbs)
        btnMusic = findViewById(R.id.btnMusic)
        btnOverlay = findViewById(R.id.btnOverlay)
        btnCamera = findViewById(R.id.btnCamera)
        btnNext = findViewById(R.id.btnNext)
        topAddAudioPill = findViewById(R.id.topAddAudioPill)
        btnAddAudio = findViewById(R.id.btnAddAudio)
        btnAdd = findViewById(R.id.btnAdd)
        txtCounter = findViewById(R.id.txtCounter)
        btnClose = findViewById(R.id.btnClose)
        imgAudioArt = findViewById(R.id.imgAudioArt)
        txtAddAudio = findViewById(R.id.txtAddAudio)
        txtAudioSubtitle = findViewById(R.id.txtAudioSubtitle)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ImageAndAudioMixerActivity, MainActivity::class.java)
                startActivity(intent)
            }
        })


        // Thumbnails Recycler
        rvThumbs.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        thumbsAdapter = SelectedImagesAdapter(
            images = selectedMedia,
            onClick = { _, uri ->
                imageUri = uri
                setPreviewImage(uri)
            },
            onRemove = { position, _ ->
                if (position in selectedMedia.indices) {
                    selectedMedia.removeAt(position)
                    thumbsAdapter.notifyItemRemoved(position)
                    thumbsAdapter.notifyItemRangeChanged(position, selectedMedia.size - position)
                    updateCounter()
                    imageUri = selectedMedia.lastOrNull()
                    if (imageUri != null) {
                        setPreviewImage(imageUri!!)
                    } else {
                        previewImage.setImageDrawable(null)
                    }
                }
            }
        )
        rvThumbs.adapter = thumbsAdapter

        // Enable drag & drop reordering for selected media thumbnails
        val dragCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                if (from in selectedMedia.indices && to in selectedMedia.indices && from != to) {
                    val moved = selectedMedia.removeAt(from)
                    selectedMedia.add(to, moved)
                    thumbsAdapter.notifyItemMoved(from, to)
                    // Update badges for visible range
                    val start = minOf(from, to)
                    val end = maxOf(from, to)
                    thumbsAdapter.notifyItemRangeChanged(start, end - start + 1)
                    // Keep preview pointing to currently selected last item if any
                    imageUri = selectedMedia.lastOrNull()
                    imageUri?.let { setPreviewImage(it) }
                    updateCounter()
                }
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // no swipe actions
            }

            override fun isLongPressDragEnabled(): Boolean = true
        }
        ItemTouchHelper(dragCallback).attachToRecyclerView(rvThumbs)

        // Request legacy storage permission only for API <= 28 (if you plan to write to external storage directly)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                101
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                101
            )
        }

        // If a preselected image is provided from another screen, use it and skip auto picker
        intent.getStringExtra("preselect_image_uri")?.let { uriStr ->
            try {
                val u = Uri.parse(uriStr)
                imageUri = u
                setPreviewImage(u)
                addMediaToList(u)
                updateCounter()
            } catch (_: Exception) {
                // fallback to picker if parse fails
                pickMultipleImages.launch("image/*")
            }
        } ?: run {
            pickMultipleImages.launch("image/*")
        }

        btnMusic.setOnClickListener {
            App.preventMultipleClick(btnMusic)
            val intent = Intent(applicationContext, UploadSoundActivity::class.java).apply {
                putExtra(Constants.TAG_SOUND_ID, soundId)
            }
            selectSoundLauncher.launch(intent)
        }

        selectSoundLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val id = result.data!!.getStringExtra("sound_id")
                    val url = result.data!!.getStringExtra("sound_url")
                    // Handle selected sound if needed
                    Log.e(TAG, "onCreate: " + url)
                    soundId = id
                    url?.let {
                        // update top pill immediately with filename and placeholder art
                        val titleGuess = try { Uri.parse(it).lastPathSegment } catch (_: Exception) { null }
                        updateTopAudioPill(titleGuess ?: "Selected audio", null)
                        showTrimAudioSheet(it)
                    }
                }
            }

        topAddAudioPill.setOnClickListener {
            App.preventMultipleClick(btnMusic)
            val intent = Intent(applicationContext, UploadSoundActivity::class.java).apply {
                putExtra(Constants.TAG_SOUND_ID, soundId)
            }
            selectSoundLauncher.launch(intent)
        }
        btnAddAudio.setOnClickListener {
            App.preventMultipleClick(btnMusic)
            val intent = Intent(applicationContext, UploadSoundActivity::class.java).apply {
                putExtra(Constants.TAG_SOUND_ID, soundId)
            }
            selectSoundLauncher.launch(intent)
        }
        btnOverlay.setOnClickListener { pickMultipleImages.launch("image/*") }
        btnCamera.setOnClickListener {
            launchCamera()
        }
        btnAdd.setOnClickListener { pickMultipleImages.launch("image/*") }
        // Replace simple add with a chooser sheet (Gallery / Camera)
        btnAdd.setOnClickListener { showAddImageSheet() }
        btnNext.setOnClickListener {
            if (imageUri != null && audioUri != null) {
                // If a single video is selected (not mixed with images), use it directly
                if (isVideoSelected && selectedMedia.size == 1) {
                    val videoFile = copyUriToCache(imageUri!!)
                    if (videoFile != null) {
                        val intent = Intent(this, PostVideoActivity::class.java)
                        intent.putExtra(Constants.TAG_VIDEO_PATH, videoFile.absolutePath)
                        intent.putExtra(Constants.TAG_SOUND_ID, soundId)
                        intent.putExtra(Constants.TAG_SOUND_URL, audioUri.toString())
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        intent.putExtra(Constants.TAG_TYPE, "gallery")
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, getString(R.string.failed_to_access_video), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Create mixed video in cache and go to Post screen
                    btnNext.isEnabled = false
                    buildMixedVideoInCache(true) { file ->
                        btnNext.isEnabled = true
                        if (file != null) {
                            val intent = Intent(this, PostVideoActivity::class.java)
                            intent.putExtra(Constants.TAG_VIDEO_PATH, file.absolutePath)
                            intent.putExtra(Constants.TAG_SOUND_ID, soundId)
                            intent.putExtra(Constants.TAG_SOUND_URL, audioUri.toString())
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            intent.putExtra(Constants.TAG_TYPE, "gallery")
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, getString(R.string.failed_to_prepare_preview), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.please_pick_image_video_audio),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        // Preview click -> play video if video selected, otherwise generate mixed video
        previewImage.setOnClickListener {
            // If a video is selected, play it directly
            if (isVideoSelected && imageUri != null) {
                playVideoUri(imageUri!!)
            } else if (imageUri != null && audioUri != null) {
                // For images, generate mixed video
                buildMixedVideoInCache(true) { file ->
                    if (file != null) {
                        generatedVideoFile = file
                        // Extract and show thumbnail
                        extractVideoThumbnail(file) { thumbnailFile ->
                            runOnUiThread {
                                thumbnailFile?.let {
                                    previewImage.setImageURI(Uri.fromFile(it))
                                } ?: run {
                                    // Fallback: show first image if thumbnail extraction fails
                                    imageUri?.let { setPreviewImage(it) }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.failed_to_prepare_preview), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        btnClose.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        // Show thumbnail if video was generated
        generatedVideoFile?.let { videoFile ->
            if (videoFile.exists()) {
                extractVideoThumbnail(videoFile) { thumbnailFile ->
                    runOnUiThread {
                        thumbnailFile?.let {
                            previewImage.setImageURI(Uri.fromFile(it))
                        }
                    }
                }
            }
        }
    }

    override fun onNetworkChanged(isConnected: Boolean) {
    }

    // --- Pickers ---
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
                setPreviewImage(it)
                addMediaToList(it)
                updateCounter()
            Toast.makeText(this, getString(R.string.image_selected), Toast.LENGTH_SHORT).show()
        }
    }

    private val pickMultipleImages =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            if (!uris.isNullOrEmpty()) {
                uris.forEach { u -> addMediaToList(u) }
                imageUri = uris.last()
                setPreviewImage(imageUri!!)
                updateCounter()
                Toast.makeText(this, getString(R.string.images_selected, uris.size), Toast.LENGTH_SHORT).show()
            }
        }

    private val pickMultipleVideos =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            if (!uris.isNullOrEmpty()) {
                val validVideos = mutableListOf<Uri>()
                val rejectedVideos = mutableListOf<String>()
                
                uris.forEach { u ->
                    val size = getFileSize(u)
                    if (size > MAX_VIDEO_SIZE_BYTES) {
                        rejectedVideos.add(formatFileSize(size))
                    } else {
                        validVideos.add(u)
                    }
                }
                
                if (rejectedVideos.isNotEmpty()) {
                    Toast.makeText(
                        this,
                        getString(R.string.videos_exceed_limit, rejectedVideos.size),
                        Toast.LENGTH_LONG
                    ).show()
                }
                
                if (validVideos.isNotEmpty()) {
                    validVideos.forEach { u -> addMediaToList(u) }
                    imageUri = validVideos.last()
                    setPreviewImage(imageUri!!)
                    updateCounter()
                    Toast.makeText(this, getString(R.string.videos_selected, validVideos.size), Toast.LENGTH_SHORT).show()
                } else if (rejectedVideos.isEmpty()) {
                    Toast.makeText(this, getString(R.string.no_videos_selected), Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val pickAudio =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            audioUri = it
                // update pill from metadata if available
                try {
                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(this, it)
                    val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                        ?: queryName(it) ?: getString(R.string.selected_audio)
                    val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
                    val art = mmr.embeddedPicture
                    updateTopAudioPill(if (artist.isNotEmpty()) title else title, art)
                    mmr.release()
                } catch (_: Exception) {
                    updateTopAudioPill(queryName(it) ?: getString(R.string.selected_audio), null)
                }
            Toast.makeText(this, getString(R.string.audio_selected), Toast.LENGTH_SHORT).show()
                // Open trim popup for gallery-selected audio as well
                showTrimAudioSheet(it.toString())
            }
        }

        private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success && cameraImageUri != null) {
                imageUri = cameraImageUri
                setPreviewImage(cameraImageUri!!)
                addImageToList(cameraImageUri!!)
                updateCounter()
                Toast.makeText(this, getString(R.string.photo_captured), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.camera_canceled), Toast.LENGTH_SHORT).show()
            }
        }

    private fun showAddImageSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_image, null)
        val btnGallery = view.findViewById<TextView>(R.id.actionGallery)
        val btnCameraAction = view.findViewById<TextView>(R.id.actionCamera)
        val btnCameraVideo = view.findViewById<TextView>(R.id.actionCameraVideo)
        val btnVideo = view.findViewById<TextView>(R.id.actionVideo)
        btnGallery.setOnClickListener {
            dialog.dismiss()
            pickMultipleImages.launch("image/*")
        }
        btnCameraAction.setOnClickListener {
            dialog.dismiss()
            launchCamera()
        }
        btnCameraVideo.setOnClickListener {
            dialog.dismiss()
            launchCameraVideo()
        }
        btnVideo.setOnClickListener {
            dialog.dismiss()
            pickMultipleVideos.launch("video/*")
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun launchCamera() {
        try {
            val imageFile = createTempImageFile()
            cameraImageUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                imageFile
            )
            takePictureLauncher.launch(cameraImageUri!!)
        } catch (e: Exception) {
            Log.e(TAG, "launchCamera error: ${e.message}", e)
            Toast.makeText(this, getString(R.string.unable_to_open_camera), Toast.LENGTH_SHORT).show()
        }
    }

    private fun createTempImageFile(): File {
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
        return File(dir, "IMG_${System.currentTimeMillis()}.jpg")
    }

    private fun createTempVideoFile(): File {
        val dir = getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: filesDir
        return File(dir, "VID_${System.currentTimeMillis()}.mp4")
    }

    private val captureVideoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val returned = result.data?.data
                val uri = returned ?: cameraVideoUri
                if (uri != null) {
                    val size = getFileSize(uri)
                    if (size > MAX_VIDEO_SIZE_BYTES) {
                        Toast.makeText(
                            this,
                            getString(R.string.video_size_exceeds_limit, formatFileSize(size)),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        addMediaToList(uri)
                        imageUri = uri
                        setPreviewImage(uri)
                        updateCounter()
                        Toast.makeText(this, getString(R.string.video_captured), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.capture_failed), Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.camera_canceled), Toast.LENGTH_SHORT).show()
            }
        }

    private fun launchCameraVideo() {
        try {
            val videoFile = createTempVideoFile()
            cameraVideoUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                videoFile
            )
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, cameraVideoUri)
                putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            }
            captureVideoLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "launchCameraVideo error: ${e.message}", e)
            Toast.makeText(this, getString(R.string.unable_to_open_camera), Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildMixedVideoInCache(showProgress: Boolean = false, onDone: (File?) -> Unit) {
        if (showProgress) showMixProgress(true, getString(R.string.preparing_preview))
        val aud = audioUri ?: return onDone(null)
        val audioFile = copyUriToCache(aud) ?: return onDone(null)
        val outFile = File(cacheDir, "mixed_preview_${System.currentTimeMillis()}.mp4")
        if (outFile.exists()) outFile.delete()

        // If multiple images selected, build a slideshow; otherwise use the single image/video
        val imagesToUse: List<Uri> = if (selectedMedia.isNotEmpty()) selectedMedia.toList() else listOfNotNull(imageUri)
        if (imagesToUse.isEmpty()) return onDone(null)

        if (imagesToUse.size == 1) {
            val mediaFile = copyUriToCache(imagesToUse.first()) ?: return onDone(null)
            val mime = contentResolver.getType(imagesToUse.first()) ?: ""
            
            // If it's a video, mix video with audio; otherwise treat as image
            val cmd = if (mime.startsWith("video")) {
                // Mix video with audio - replace audio track
                arrayOf(
                    "-y",
                    "-i", "\"${mediaFile.absolutePath}\"",
                    "-i", "\"${audioFile.absolutePath}\"",
                    "-c:v", "copy", // Copy video stream without re-encoding
                    "-c:a", "aac",
                    "-b:a", "192k",
                    "-map", "0:v:0", // Use video from first input
                    "-map", "1:a:0", // Use audio from second input
                    "-shortest", // End when shortest stream ends
                    "\"${outFile.absolutePath}\""
                ).joinToString(" ")
            } else {
                // Use decrease with padding to preserve full image (no cropping)
                arrayOf(
                    "-y",
                    "-loop", "1",
                    "-i", "\"${mediaFile.absolutePath}\"",
                    "-i", "\"${audioFile.absolutePath}\"",
                    "-c:v", "mpeg4",
                    "-tune", "stillimage",
                    "-preset", "ultrafast",
                    "-b:v", "1500k",
                    "-vf", "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2:color=black,setsar=1,format=yuv420p",
                    "-c:a", "aac",
                    "-b:a", "192k",
                    "-shortest",
                    "\"${outFile.absolutePath}\""
                ).joinToString(" ")
            }
            FFmpegKit.executeAsync(cmd) { session ->
                val ok = ReturnCode.isSuccess(session.returnCode)
                if (ok && outFile.exists()) {
                    generatedVideoFile = outFile
                    // Extract thumbnail from video and update preview
                    extractVideoThumbnail(outFile) { thumbnailFile ->
                        runOnUiThread {
                            thumbnailFile?.let {
                                previewImage.setImageURI(Uri.fromFile(it))
                            }
                        }
                    }
                }
                runOnUiThread { onDone(if (ok) outFile else null) }
                if (showProgress) runOnUiThread { showMixProgress(false, "") }
            }
            return
        }

        // Build slideshow using filter_complex concat
        val imageFiles = mutableListOf<File>()
        val videoFiles = mutableListOf<File>()
        imagesToUse.forEach { u ->
            try {
                val mime = contentResolver.getType(u) ?: ""
                val f = copyUriToCache(u)
                if (f != null) {
                    if (mime.startsWith("video")) videoFiles.add(f) else imageFiles.add(f)
                }
            } catch (_: Exception) {
                val f = copyUriToCache(u)
                if (f != null) imageFiles.add(f)
            }
        }
        if (imageFiles.isEmpty() && videoFiles.isEmpty()) return onDone(null)
        
        // Get audio duration and calculate perImageSeconds dynamically
        val audioDurationSeconds = try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(audioFile.absolutePath)
            val durationMs = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            mmr.release()
            (durationMs / 1000.0).coerceAtLeast(0.1)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting audio duration: ${e.message}", e)
            15.0 // Fallback to 15 seconds
        }
        
        // Calculate perImageSeconds based on audio duration and number of images
        // This ensures all images are used within the audio duration
        val totalImages = imageFiles.size
        val perImageSeconds = if (totalImages > 0) {
            (audioDurationSeconds / totalImages).coerceAtLeast(0.1).coerceAtMost(2.0) // Min 0.1s, Max 2s per image
        } else {
            0.5 // Fallback
        }
        
        Log.d(TAG, "Audio duration: ${audioDurationSeconds}s, Images: $totalImages, Per image: ${perImageSeconds}s")

        val inputs = StringBuilder()
        // order: all images (looped) then all videos, then audio
        imageFiles.forEach { file -> inputs.append("-loop 1 -t $perImageSeconds -i \"${file.absolutePath}\" ") }
        videoFiles.forEach { file -> inputs.append("-i \"${file.absolutePath}\" ") }
        inputs.append("-i \"${audioFile.absolutePath}\" ")

        // Scale to fit within frame (1080x1920 portrait) with padding to preserve full image (no cropping)
        val scalePad = "scale=1080:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:(oh-ih)/2:color=black,setsar=1"
        val totalMedia = imageFiles.size + videoFiles.size
        val mapScales = (0 until totalMedia).joinToString(";") { idx -> "[$idx:v]$scalePad[v$idx]" }
        val concatInputs = (0 until totalMedia).joinToString("") { idx -> "[v$idx]" }
        val filter = "$mapScales;$concatInputs concat=n=$totalMedia:v=1:a=0[vout]"

        val mediaCount = totalMedia
        val cmd = StringBuilder()
            .append("-y ")
            .append(inputs)
            .append("-filter_complex \"$filter\" ")
            .append("-map \"[vout]\" ")
            .append("-map ${mediaCount}:a:0 ")
            .append("-c:v mpeg4 -r 30 -c:a aac -b:a 192k -shortest ")
            .append("\"${outFile.absolutePath}\"")
            .toString()

        FFmpegKit.executeAsync(cmd) { session ->
            val ok = ReturnCode.isSuccess(session.returnCode)
            if (ok && outFile.exists()) {
                generatedVideoFile = outFile
                // Extract thumbnail from video and update preview
                extractVideoThumbnail(outFile) { thumbnailFile ->
                    runOnUiThread {
                        thumbnailFile?.let {
                            previewImage.setImageURI(Uri.fromFile(it))
                        }
                    }
                }
            }
            runOnUiThread { onDone(if (ok) outFile else null) }
            if (showProgress) runOnUiThread { showMixProgress(false, "") }
        }
    }

    private fun setPreviewImage(uri: Uri) {
        val mime = contentResolver.getType(uri) ?: ""
        if (mime.startsWith("video")) {
            isVideoSelected = true
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this, uri)
                val bmp = retriever.frameAtTime
                if (bmp != null) {
                    previewImage.setImageBitmap(bmp)
                } else {
                    previewImage.setImageResource(R.drawable.video_placeholder)
                }
                retriever.release()
            } catch (e: Exception) {
                previewImage.setImageResource(R.drawable.video_placeholder)
            }
        } else {
            isVideoSelected = false
            previewImage.setImageURI(uri)
        }
    }

    private fun extractVideoThumbnail(videoFile: File, onDone: (File?) -> Unit) {
        val thumbnailFile = File(cacheDir, "video_thumb_${System.currentTimeMillis()}.jpg")
        val cmd = arrayOf(
            "-y",
            "-i", "\"${videoFile.absolutePath}\"",
            "-ss", "00:00:00.500", // Extract frame at 0.5 seconds
            "-vframes", "1",
            "-vf", "scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920",
            "\"${thumbnailFile.absolutePath}\""
        ).joinToString(" ")
        
        FFmpegKit.executeAsync(cmd) { session ->
            val ok = ReturnCode.isSuccess(session.returnCode)
            runOnUiThread { onDone(if (ok && thumbnailFile.exists()) thumbnailFile else null) }
        }
    }

    private fun showMixProgress(show: Boolean, message: String) {
        if (show) {
            if (mixProgressDialog == null) {
                mixProgressDialog = ProgressDialog(this).apply {
                    setCancelable(false)
                    isIndeterminate = true
                }
            }
            mixProgressDialog?.setMessage(message)
            if (mixProgressDialog?.isShowing != true) {
                mixProgressDialog?.show()
            }
        } else {
            mixProgressDialog?.dismiss()
        }
    }
    private fun copyUrlToCache(url: String): File? {
        return try {
            val out = File(cacheDir, "audio_${System.currentTimeMillis()}.m4a")
            URL(url).openStream().use { input ->
                FileOutputStream(out).use { output ->
                    input.copyTo(output)
                }
            }
            out
        } catch (e: Exception) {
            Log.e(TAG, "copyUrlToCache error: ${e.message}", e)
            null
        }
    }

    private fun showTrimAudioSheet(source: String) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_trim_audio, null)
        dialog.setContentView(view)

        val btnDelete = view.findViewById<ImageButton>(R.id.btnDeleteAudio)
        val btnDone = view.findViewById<TextView>(R.id.btnDone)
        val imgArt = view.findViewById<ImageView>(R.id.imgArt)
        val txtTitle = view.findViewById<TextView>(R.id.txtTitle)
        val volumeSeek = view.findViewById<SeekBar>(R.id.volumeSeek)
        val trimSlider = view.findViewById<RangeSlider>(R.id.trimSlider)
        val btnPlay = view.findViewById<ImageButton>(R.id.btnPlay)
        val trimContainer = view.findViewById<FrameLayout>(R.id.trimContainer)
        val handleLeft = view.findViewById<ImageView>(R.id.handleLeft)
        val handleRight = view.findViewById<ImageView>(R.id.handleRight)
        val waveformView = view.findViewById<SimpleWaveformView>(R.id.waveformView)
        val txtStartTime = view.findViewById<TextView>(R.id.txtStartTime)
        val txtEndTime = view.findViewById<TextView>(R.id.txtEndTime)
        val txtDuration = view.findViewById<TextView>(R.id.txtDuration)


        // load file locally
        val inputFile: File? = if (source.startsWith("http")) {
            copyUrlToCache(source)
        } else if (source.startsWith("content://") || source.startsWith("file://")) {
            try {
                val uri = Uri.parse(source)
                copyUriToCache(uri)
            } catch (e: Exception) {
                null
            }
        } else {
            val f = File(source)
            if (f.exists()) f else null
        }
        if (inputFile == null) {
            Toast.makeText(this, getString(R.string.unable_to_open_audio), Toast.LENGTH_SHORT).show()
            return
        }

        // title + embedded art (if available)
        txtTitle.text = inputFile.nameWithoutExtension
        try {
            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(inputFile.absolutePath)
            val art = mmr.embeddedPicture
            if (art != null) {
                try {
                    val bmp = BitmapFactory.decodeByteArray(art, 0, art.size)
                    if (bmp != null) {
                        imgArt.setImageBitmap(bmp)
                    } else {
                        imgArt.setImageResource(R.drawable.ic_music_note_white)
                    }
                } catch (_: Exception) {
                    imgArt.setImageResource(R.drawable.ic_music_note_white)
                }
            } else {
                imgArt.setImageResource(R.drawable.ic_music_note_white)
            }
            mmr.release()
        } catch (_: Exception) {
            imgArt.setImageResource(R.drawable.ic_music_note_white)
        }

        // MediaPlayer for preview
        var mediaPlayer: MediaPlayer? = null
        mediaPlayer?.setOnCompletionListener {
            btnPlay.setImageResource(android.R.drawable.ic_media_play)
        }

        var startSec = 0f
        var endSec = 0f
        var isPlaying = false
        var isFinished = false
        
        // Format seconds to MM:SS or M:SS - always use English numbers
        fun formatTime(seconds: Float): String {
            val totalSeconds = seconds.toInt()
            val minutes = totalSeconds / 60
            val secs = totalSeconds % 60
            return String.format(Locale.US, "%d:%02d", minutes, secs)
        }
        
        // Update time display - ensure Western numbers
        fun updateTimeDisplay() {
            val startTimeStr = formatTime(startSec)
            val endTimeStr = formatTime(endSec)
            val durationStr = formatTime(endSec - startSec)
            
            txtStartTime?.apply {
                text = startTimeStr
                // Force Western locale for text rendering
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    textLocale = java.util.Locale.US
                }
            }
            txtEndTime?.apply {
                text = endTimeStr
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    textLocale = java.util.Locale.US
                }
            }
            txtDuration?.apply {
                text = durationStr
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    textLocale = java.util.Locale.US
                }
            }
        }
        
        fun preparePlayer() {
            try {
                mediaPlayer?.release()

                mediaPlayer = MediaPlayer().apply {

                    setDataSource(inputFile.absolutePath)

                    setOnPreparedListener { mp ->
                        val durationMs = mp.duration.coerceAtLeast(1)
                        val durationSec = durationMs / 1000f

                        trimSlider.valueFrom = 0f
                        trimSlider.valueTo = durationSec
                        trimSlider.stepSize = 0f
                        
                        // Set label formatter to use Western numbers
                        trimSlider.setLabelFormatter { value ->
                            val totalSeconds = value.toInt()
                            val minutes = totalSeconds / 60
                            val secs = totalSeconds % 60
                            String.format(Locale.US, "%d:%02d", minutes, secs)
                        }

                        val defaultEnd = min(15f, durationSec)
                        trimSlider.values = listOf(0f, defaultEnd)

                        startSec = 0f
                        endSec = defaultEnd

                        trimContainer.post {
                            updateHandlePositions(trimContainer, handleLeft, handleRight, trimSlider)
                            updateTimeDisplay()
                        }
                    }

                    // ✅ FIXED: set listener HERE (not when mediaPlayer = null)
                    setOnCompletionListener {
                        isPlaying = false
                        isFinished = true
                        btnPlay.setImageResource(android.R.drawable.ic_media_play)
                    }

                    prepareAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "preparePlayer error: ${e.message}", e)
                Toast.makeText(this, getString(R.string.preview_failed), Toast.LENGTH_SHORT).show()
            }
        }

        preparePlayer()

        volumeSeek.max = 100
        volumeSeek.progress = 50
        volumeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val vol = progress / 100f
                mediaPlayer?.setVolume(vol, vol)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        trimSlider.addOnChangeListener { _, _, _ ->
            val values = trimSlider.values
            if (values.size >= 2) {
                startSec = values[0]
                endSec = values[1]
                updateHandlePositions(trimContainer, handleLeft, handleRight, trimSlider)
                updateTimeDisplay()
            }
        }


        var pollingRunnable: Runnable? = null
        btnPlay.setOnClickListener {
            val mp = mediaPlayer ?: return@setOnClickListener
            // If music finished (showing play icon), reset to beginning and play
            if (isFinished) {
                try {
                    mp.seekTo((startSec * 1000).toInt())
                    isFinished = false
                    btnPlay.setImageResource(android.R.drawable.ic_media_play)
                } catch (_: Exception) {
                }
                return@setOnClickListener
            }
            if (!isPlaying) {
                try {
                    mp.seekTo((startSec * 1000).toInt())
                    mp.start()
                    isPlaying = true
                    btnPlay.setImageResource(android.R.drawable.ic_media_pause)
                    // Show waveform and initialize it
                    waveformView.visibility = View.VISIBLE
                    trimSlider.visibility = View.GONE
                    val barCount = (trimContainer.width / 6).coerceAtLeast(50).coerceAtMost(200)
                    waveformView.initializeWaveform(barCount)
                    // simple polling to stop at end and update waveform
                    pollingRunnable = object : Runnable {
                        override fun run() {
                            if (!isPlaying || mediaPlayer == null) return
                            try {
                                if (mediaPlayer!!.isPlaying && mediaPlayer!!.currentPosition >= (endSec * 1000).toInt()) {
                                    mediaPlayer!!.pause()
                                    isPlaying = false
                                    isFinished = true
                                    btnPlay.setImageResource(android.R.drawable.ic_media_play)
                                    waveformView.visibility = View.GONE
                                    trimSlider.visibility = View.VISIBLE
                                } else if (isPlaying && mediaPlayer != null) {
                                    // Update waveform with simulated amplitude
                                    // MediaPlayer doesn't provide real-time amplitude, so we simulate it
                                    val currentTime = System.currentTimeMillis() / 100.0
                                    val simulatedAmplitude = (0.3f + (sin(currentTime) * 0.3f + Random.nextFloat() * 0.2f)).coerceIn(
                                        0.1, 1.0)
                                    waveformView.updateAmplitude(simulatedAmplitude)
                                    btnPlay.postDelayed(this, 50)
                                }
                            } catch (e: IllegalStateException) {
                                // MediaPlayer was released or in invalid state
                                isPlaying = false
                                isFinished = true
                                btnPlay.setImageResource(android.R.drawable.ic_media_play)
                                waveformView.visibility = View.GONE
                                trimSlider.visibility = View.VISIBLE
                            } catch (e: Exception) {
                                // Other exceptions, stop polling
                                isPlaying = false
                                isFinished = true
                                btnPlay.setImageResource(android.R.drawable.ic_media_play)
                                waveformView.visibility = View.GONE
                                trimSlider.visibility = View.VISIBLE
                            }
                        }
                    }
                    btnPlay.postDelayed(pollingRunnable!!, 50)
                } catch (_: Exception) {
                }
            } else {
                mp.pause()
                isPlaying = false
                isFinished = false
                pollingRunnable?.let { btnPlay.removeCallbacks(it) }
                btnPlay.setImageResource(android.R.drawable.ic_media_play)
                waveformView.visibility = View.GONE
                trimSlider.visibility = View.VISIBLE
            }
        }

        btnDelete.setOnClickListener {
            isPlaying = false
            isFinished = false
            pollingRunnable?.let { btnPlay.removeCallbacks(it) }
            pollingRunnable = null
            waveformView.visibility = View.GONE
            trimSlider.visibility = View.VISIBLE
            try {
                mediaPlayer?.stop()
            } catch (_: Exception) {}
            try {
                mediaPlayer?.release()
            } catch (_: Exception) {}
            mediaPlayer = null
            audioUri = null
            dialog.dismiss()
            Toast.makeText(this, getString(R.string.audio_removed), Toast.LENGTH_SHORT).show()
        }

        btnDone.setOnClickListener {
            // show inline progress and disable actions
            btnDone.isEnabled = false
            btnPlay.isEnabled = false
            btnDelete.isEnabled = false
            view.findViewById<ProgressBar>(R.id.progressTrim)?.visibility = View.VISIBLE
            btnDone.text = getString(R.string.trimming)
            mediaPlayer?.pause()
            val out = File(cacheDir, "trim_${System.currentTimeMillis()}.m4a")
            val cmd = arrayOf(
                "-y",
                "-ss", startSec.toString(),
                "-to", endSec.toString(),
                "-i", inputFile.absolutePath,
                "-c:a", "aac",
                "-b:a", "192k",
                out.absolutePath
            ).joinToString(" ")

            Toast.makeText(this, getString(R.string.trimming), Toast.LENGTH_SHORT).show()
            FFmpegKit.executeAsync(cmd) { session ->
                val ok = ReturnCode.isSuccess(session.returnCode)
                runOnUiThread {
                    // hide progress and re-enable
                    btnDone.isEnabled = true
                    btnPlay.isEnabled = true
                    btnDelete.isEnabled = true
                    view.findViewById<ProgressBar>(R.id.progressTrim)?.visibility = View.GONE
                    btnDone.text = getString(R.string.done)
                    if (ok) {
                        audioUri = Uri.fromFile(out)
                        // refresh pill from trimmed file
                        try {
                            val mmr = MediaMetadataRetriever()
                            mmr.setDataSource(out.absolutePath)
                            val title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                                ?: out.nameWithoutExtension
                            val art = mmr.embeddedPicture
                            updateTopAudioPill(title, art)
                            mmr.release()
                        } catch (_: Exception) {
                            updateTopAudioPill(out.nameWithoutExtension, null)
                        }
                        Toast.makeText(this, getString(R.string.audio_trimmed), Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, getString(R.string.trim_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.setOnDismissListener {
            isPlaying = false
            isFinished = false
            pollingRunnable?.let { btnPlay.removeCallbacks(it) }
            pollingRunnable = null
            waveformView.visibility = View.GONE
            trimSlider.visibility = View.VISIBLE
            try {
                mediaPlayer?.stop()
            } catch (_: Exception) {}
            try {
                mediaPlayer?.release()
            } catch (_: Exception) {
            }
            mediaPlayer = null
        }

        dialog.show()
    }

    private fun updateHandlePositions(
        container: View,
        left: View,
        right: View,
        slider: RangeSlider
    ) {
        val values = slider.values
        if (values.size < 2) return
        val vf = slider.valueFrom
        val vt = slider.valueTo
        if (vt <= vf) return

        val width = container.width.takeIf { it > 0 } ?: return
        val leftW = left.width.takeIf { it > 0 } ?: 0
        val rightW = right.width.takeIf { it > 0 } ?: 0
        val padding = container.paddingLeft + container.paddingRight

        val leftFrac = ((values[0] - vf) / (vt - vf)).coerceIn(0f, 1f)
        val rightFrac = ((values[1] - vf) / (vt - vf)).coerceIn(0f, 1f)

        // Calculate positions accounting for padding and handle widths
        val availableWidth = width - padding - leftW
        val leftPos = padding + (leftFrac * availableWidth)
        val rightPos = padding + (rightFrac * availableWidth) - rightW

        left.x = leftPos
        right.x = rightPos
    }
    private fun addImageToList(uri: Uri) {
        if (selectedMedia.size >= 35) {
            Toast.makeText(this, getString(R.string.max_images_limit), Toast.LENGTH_SHORT).show()
            return
        }
        selectedMedia.add(uri)
        thumbsAdapter.notifyItemInserted(selectedMedia.lastIndex)
        rvThumbs.smoothScrollToPosition(selectedMedia.lastIndex)
    }

    // Backward-compatible alias now that we support both images and videos
    private fun addMediaToList(uri: Uri) {
        addImageToList(uri)
    }

    private fun updateCounter() {
        txtCounter.text = "${selectedMedia.size}/35"
    }

    private fun getFileSize(uri: Uri): Long {
        return try {
            contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
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



    private fun updateTopAudioPill(title: String, artBytes: ByteArray?) {
        txtAddAudio.text = title
        txtAudioSubtitle.text = getString(R.string.selected_audio)
        if (artBytes != null) {
            try {
                val bmp = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
                if (bmp != null) {
                    imgAudioArt.setImageBitmap(bmp)
                    return
                }
            } catch (_: Exception) {}
        }
        imgAudioArt.setImageResource(R.drawable.ic_music_note_white)
    }

    // --- Combine & Save & Play ---
    private fun combineImageAndAudioAndSave() {
        // copy both to cache so FFmpeg can read them as regular files
        val imageFile = copyUriToCache(imageUri!!) ?: run {
            Toast.makeText(this, getString(R.string.failed_to_access_image), Toast.LENGTH_SHORT).show()
            return
        }
        val audioFile = copyUriToCache(audioUri!!) ?: run {
            Toast.makeText(this, getString(R.string.failed_to_access_audio), Toast.LENGTH_SHORT).show()
            return
        }

        // temporary output in cache
        val tmpOutput = File(cacheDir, "tmp_output_video.mp4")
        if (tmpOutput.exists()) tmpOutput.delete()

        // --- Combine image and audio using FFmpeg ---
        val cmd = arrayOf(
            "-y",
            "-loop", "1",
            "-i", imageFile.absolutePath,
            "-i", audioFile.absolutePath,
            "-c:v", "mpeg4", // ✅ use MPEG4 instead of libx264
            "-tune", "stillimage",
            "-preset", "ultrafast",
            "-b:v", "1500k",
            "-vf", "scale=1280:720,format=yuv420p",
            "-c:a", "aac",
            "-b:a", "192k",
            "-shortest",
            tmpOutput.absolutePath
        ).joinToString(" ")

        Toast.makeText(this, getString(R.string.processing_may_take_time), Toast.LENGTH_SHORT).show()

        FFmpegKit.executeAsync(cmd) { session ->
            val returnCode = session.returnCode
            val logs = session.allLogsAsString ?: ""
            if (ReturnCode.isSuccess(returnCode)) {
                // success -> save to Downloads and play
                runOnUiThread {
                    try {
                        val savedUri = saveVideoToDownloads(
                            tmpOutput,
                            "video_from_image_${System.currentTimeMillis()}.mp4"
                        )
                        if (savedUri != null) {
                            Toast.makeText(this, getString(R.string.saved_to_downloads), Toast.LENGTH_SHORT).show()
                            playVideoUri(savedUri)
                        } else {
                            // fallback: scan temp file and play directly
                            MediaScannerConnection.scanFile(
                                this,
                                arrayOf(tmpOutput.absolutePath),
                                arrayOf("video/mp4"),
                                null
                            )
                            playVideoUri(Uri.fromFile(tmpOutput))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Save/play failed: ${e.message}", e)
                        Toast.makeText(this, getString(R.string.save_failed, e.message ?: ""), Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Log.e(TAG, "FFmpeg failed: $logs")
                runOnUiThread {
                    Toast.makeText(this, getString(R.string.failed_to_create_video), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Copy content URI to cache and return File
    private fun copyUriToCache(uri: Uri): File? {
        return try {
            val name = queryName(uri) ?: "tempfile"
            val outFile = File(cacheDir, name)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output)
                }
            }
            outFile
        } catch (e: Exception) {
            Log.e(TAG, "copyUriToCache error: ${e.message}", e)
            null
        }
    }

    // get display name for content Uri
    private fun queryName(uri: Uri): String? {
        var result: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                result = cursor.getString(nameIndex)
            }
        }
        return result
    }

    // Save mp4 file into Downloads using MediaStore on Android Q+ or legacy file copy
    private fun saveVideoToDownloads(sourceFile: File, displayName: String): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS + File.separator + "MyVideos"
                    )
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val resolver = contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: resolver.insert(
                        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                        values
                    )

                uri?.let {
                    resolver.openOutputStream(it)?.use { out ->
                        sourceFile.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    values.clear()
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
                uri
            } else {
                // Legacy path: write directly to Environment.getExternalStoragePublicDirectory
                val downloads =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val dir = File(downloads, "MyVideos")
                if (!dir.exists()) dir.mkdirs()
                val dest = File(dir, displayName)
                sourceFile.copyTo(dest, overwrite = true)
                // make available to media scanner
                MediaScannerConnection.scanFile(
                    this,
                    arrayOf(dest.absolutePath),
                    arrayOf("video/mp4"),
                    null
                )
                Uri.fromFile(dest)
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveVideoToDownloads failed: ${e.message}", e)
            null
        }
    }

    // Play using external video player (Intent)
    private fun playVideoUri(uri: Uri) {
        try {
            val playIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/*")
            }
            startActivity(playIntent)
        } catch (e: Exception) {
            Log.e(TAG, "playVideoUri error: ${e.message}", e)
            Toast.makeText(this, getString(R.string.no_app_to_play_video), Toast.LENGTH_SHORT).show()
        }
    }
}
