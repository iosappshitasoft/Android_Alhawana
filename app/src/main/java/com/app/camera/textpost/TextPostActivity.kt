package com.app.camera.textpost


import android.app.ProgressDialog
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.*
import android.view.Gravity
import android.webkit.MimeTypeMap
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.app.alhawanajd.BaseActivity
import com.app.alhawanajd.MainActivity
import com.app.alhawanajd.R
import com.app.alhawanajd.Video.PostVideoActivity
import com.app.alhawanajd.Video.UploadSoundActivity
import com.app.camera.CameraLiveChooserActivity
import com.app.camera.audioimage.SimpleWaveformView
import com.app.utils.Constants
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.RangeSlider
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.random.Random
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.Locale
import kotlin.collections.get
import kotlin.math.min
import kotlin.math.sin

class TextPostActivity : BaseActivity() {

    private lateinit var btnClose: ImageButton
    private lateinit var editCard: MaterialCardView
    private lateinit var editText: EditText
    private lateinit var btnMusic: LinearLayout
    private lateinit var btnBg: LinearLayout
    private lateinit var btnColor: LinearLayout
    private lateinit var btnSize: LinearLayout
    private lateinit var btnFont: LinearLayout
    private lateinit var btnNext: Button
    private lateinit var topAudioPill: LinearLayout
    private lateinit var txtTopAudio: TextView
    private lateinit var textMusic: TextView
    private lateinit var btnChangeAudio: ImageButton

    private var soundId: String? = null
    private var soundUrl: String? = null
    private lateinit var selectSoundLauncher: ActivityResultLauncher<Intent>
    private var mixProgressDialog: ProgressDialog? = null

    private var currentFontIndex = 0
    private val fonts: List<Typeface?> by lazy {
        listOf(Typeface.DEFAULT, Typeface.SANS_SERIF, Typeface.SERIF, Typeface.MONOSPACE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_post)

        btnClose = findViewById(R.id.btnClose)
        editCard = findViewById(R.id.editCard)
        editText = findViewById(R.id.editText)
        btnMusic = findViewById(R.id.btnMusic)
        btnBg = findViewById(R.id.btnBg)
        btnColor = findViewById(R.id.btnColor)
        btnSize = findViewById(R.id.btnSize)
        btnFont = findViewById(R.id.btnFont)
        btnNext = findViewById(R.id.btnNext)
        topAudioPill = findViewById(R.id.topAudioPill)
        txtTopAudio = findViewById(R.id.txtTopAudio)
        textMusic = findViewById(R.id.textMusic)
        btnChangeAudio = findViewById(R.id.btnChangeAudio)
        btnClose.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@TextPostActivity, MainActivity::class.java)
                startActivity(intent)
            }
        })

        selectSoundLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val id = result.data!!.getStringExtra("sound_id")
                    val url = result.data!!.getStringExtra("sound_url")
                    // Handle selected sound if needed
                    soundId = id
                    soundUrl = url
                    textMusic.text = getString(R.string.music) + " ✓"
                    url?.let {
                        // update top pill immediately with filename and placeholder art
                        val titleGuess = try { Uri.parse(it).lastPathSegment } catch (_: Exception) { null }
                        updateTopAudioPill(titleGuess ?: getString(R.string.selected_audio))
                        showTrimAudioSheet(it)
                    }
                }
            }


        btnMusic.setOnClickListener {
            val intent = Intent(applicationContext, UploadSoundActivity::class.java).apply {
                putExtra(Constants.TAG_SOUND_ID, soundId)
            }
            selectSoundLauncher.launch(intent)
        }
        topAudioPill.setOnClickListener {
            val intent = Intent(applicationContext, UploadSoundActivity::class.java).apply {
                putExtra(Constants.TAG_SOUND_ID, soundId)
            }
            selectSoundLauncher.launch(intent)
        }
        btnChangeAudio.setOnClickListener {
            val intent = Intent(applicationContext, UploadSoundActivity::class.java).apply {
                putExtra(Constants.TAG_SOUND_ID, soundId)
            }
            selectSoundLauncher.launch(intent)
        }

        btnBg.setOnClickListener { showBgColorPickerSheet() }
        btnColor.setOnClickListener { showTextColorPickerSheet() }

        btnSize.setOnClickListener { showSizePicker() }

        btnFont.setOnClickListener { showFontPickerSheet() }

        btnNext.setOnClickListener {
            val exportedImageUri = exportCardToImage() ?: run {
                Toast.makeText(this, getString(R.string.unable_to_export), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (soundUrl.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.please_select_audio_first), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnNext.isEnabled = false
            showMixProgress(true, getString(R.string.preparing))

            val imageFile = try {
                copyUriToCache(exportedImageUri)
            } catch (_: Exception) {
                null
            } ?: run {
                showMixProgress(false, "")
                btnNext.isEnabled = true
                Toast.makeText(this, getString(R.string.failed_to_access_image), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val audioFile = when {
                soundUrl!!.startsWith("http", true) -> copyUrlToCache(soundUrl!!)
                soundUrl!!.startsWith("content://") || soundUrl!!.startsWith("file://") -> {
                    try { copyUriToCache(Uri.parse(soundUrl)) } catch (_: Exception) { null }
                }
                else -> File(soundUrl!!).takeIf { it.exists() }
            } ?: run {
                showMixProgress(false, "")
                btnNext.isEnabled = true
                Toast.makeText(this, getString(R.string.failed_to_access_audio), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val outFile = File(cacheDir, "textpost_mix_${System.currentTimeMillis()}.mp4")
            if (outFile.exists()) outFile.delete()

            // Get dimensions from the exported image (most accurate)
            var imageWidth: Int
            var imageHeight: Int
            try {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(imageFile.absolutePath, options)
                imageWidth = options.outWidth
                imageHeight = options.outHeight
                Log.d("TextPost", "Image dimensions from file: ${imageWidth}x${imageHeight}")
            } catch (e: Exception) {
                Log.e("TextPost", "Failed to get image dimensions: ${e.message}")
                // Fallback: use card dimensions
                val cardWidth = editCard.width.takeIf { it > 0 } ?: editCard.measuredWidth
                val cardHeight = editCard.height.takeIf { it > 0 } ?: editCard.measuredHeight
                if (cardWidth > 0 && cardHeight > 0) {
                    imageWidth = cardWidth
                    imageHeight = cardHeight
                } else {
                    // Final fallback
                    imageWidth = 1080
                    imageHeight = 1920
                }
            }

            // Ensure even dimensions for video codec (required by most codecs)
            val videoWidth = if (imageWidth % 2 == 0) imageWidth else imageWidth + 1
            val videoHeight = if (imageHeight % 2 == 0) imageHeight else imageHeight + 1

            Log.d("TextPost", "Video output dimensions: ${videoWidth}x${videoHeight}")

            // Use scale filter to match exact dimensions
            // If dimensions are already even, scale will be 1:1 (no change)
            // If odd, we'll scale to even dimensions (minimal change, preserves aspect ratio)
            val scaleFilter = if (imageWidth == videoWidth && imageHeight == videoHeight) {
                // Already even dimensions, no scaling needed
                "setsar=1,format=yuv420p"
            } else {
                // Scale to even dimensions, preserving aspect ratio
                "scale=${videoWidth}:${videoHeight}:force_original_aspect_ratio=decrease,pad=${videoWidth}:${videoHeight}:(ow-iw)/2:(oh-ih)/2:color=white,setsar=1,format=yuv420p"
            }
            
            val cmd = arrayOf(
                "-y",
                "-loop", "1",
                "-i", "\"${imageFile.absolutePath}\"",
                "-i", "\"${audioFile.absolutePath}\"",
                "-c:v", "mpeg4",
                "-tune", "stillimage",
                "-preset", "ultrafast",
                "-b:v", "1500k",
                "-vf", scaleFilter,
                "-c:a", "aac",
                "-b:a", "192k",
                "-shortest",
                "\"${outFile.absolutePath}\""
            ).joinToString(" ")

            FFmpegKit.executeAsync(cmd) { session ->
                val ok = ReturnCode.isSuccess(session.returnCode)
                runOnUiThread {
                    showMixProgress(false, "")
                    btnNext.isEnabled = true
                    if (ok) {
                        val intent = Intent(this, PostVideoActivity::class.java)
                        intent.putExtra(Constants.TAG_VIDEO_PATH, outFile.absolutePath)
                        intent.putExtra(Constants.TAG_SOUND_ID, soundId)
                        intent.putExtra(Constants.TAG_SOUND_URL, soundUrl)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        intent.putExtra(Constants.TAG_TYPE, "gallery")
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, getString(R.string.failed_to_prepare_preview), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onNetworkChanged(isConnected: Boolean) {

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


        val inputFile: File? = when {
            source.startsWith("http") -> copyUrlToCache(source)
            source.startsWith("content://") || source.startsWith("file://") -> {
                try { copyUriToCache(Uri.parse(source)) } catch (_: Exception) { null }
            }
            else -> File(source).takeIf { it.exists() }
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
                Log.e("TAG", "preparePlayer error: ${e.message}", e)
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
                try {
                    view.findViewById<TextView>(R.id.volumeValue)?.text = (progress / 10f).toInt().toString()
                } catch (_: Exception) {}
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
                                    val simulatedAmplitude = (0.3f + (sin(currentTime) * 0.3f + Random.nextFloat() * 0.2f)).coerceIn(0.1, 1.0)
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
            try { mediaPlayer?.release() } catch (_: Exception) {}
            mediaPlayer = null
            soundId = null
            soundUrl = null
            textMusic.text = getString(R.string.music)
            try { topAudioPill.visibility = View.GONE } catch (_: Exception) {}
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
            try { mediaPlayer?.pause() } catch (_: Exception) {}

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
                        // point soundUrl to trimmed local file so mixing uses it
                        soundUrl = Uri.fromFile(out).toString()
                        try { updateTopAudioPill(out.nameWithoutExtension) } catch (_: Exception) {}
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
            try { mediaPlayer?.release() } catch (_: Exception) {}
            mediaPlayer = null
        }

        dialog.show()
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


    private fun copyUriToCache(uri: Uri): File? {
        return try {
            val baseName = queryName(uri)?.takeIf { it.isNotBlank() } ?: "audio_${System.currentTimeMillis()}"
            val mime = contentResolver.getType(uri)
            val extFromMime = mime?.let { MimeTypeMap.getSingleton().getExtensionFromMimeType(it) }
            val fileName = when {
                !extFromMime.isNullOrBlank() && !baseName.endsWith(".${extFromMime}") -> "$baseName.$extFromMime"
                else -> baseName
            }
            val outFile = java.io.File(cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(outFile).use { output ->
                    input.copyTo(output, 8 * 1024)
                }
            } ?: return null
            outFile
        } catch (_: Exception) { null }
    }

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

    private fun updateTopAudioPill(title: String) {
        try {
            txtTopAudio.text = title
            topAudioPill.visibility = View.VISIBLE
        } catch (_: Exception) {}
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



    // removed legacy alert-dialog color picker in favor of bottom-sheet pickers

    private fun showSizePicker() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
        }
        val seek = SeekBar(this).apply {
            max = 50
            progress = editText.textSize.toInt() / 2 // rough mapping
        }
        val hint = TextView(this).apply {
            text = getString(R.string.size_sp, seek.progress)
        }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                hint.text = getString(R.string.size_sp, progress)
                editText.textSize = progress.toFloat()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        layout.addView(hint)
        layout.addView(seek)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.text_size))
            .setView(layout)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun showTextColorPickerSheet() {
        val dialog = BottomSheetDialog(this, R.style.DialogKeyBoardStyle)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 24)
            background = GradientDrawable().apply {
                cornerRadius = 24f
                setColor(0xFF101010.toInt())
            }
        }
        val heading = TextView(this).apply {
            text = getString(R.string.text_color)
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 16f
            setPadding(8, 8, 8, 16)
        }
        val scroll = HorizontalScrollView(this)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val colors = intArrayOf(
            0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0xFFFF3366.toInt(), 0xFFFF1A1A.toInt(),
            0xFFFFEB3B.toInt(), 0xFF00E5FF.toInt(), 0xFF00E676.toInt(), 0xFF9C27B0.toInt(),
            0xFF3F51B5.toInt(), 0xFFFF9800.toInt()
        )
        colors.forEach { color ->
            val outer = FrameLayout(this).apply {
                val lp = LinearLayout.LayoutParams(resources.displayMetrics.density.times(46).toInt(), resources.displayMetrics.density.times(46).toInt())
                lp.setMargins(12, 8, 12, 8)
                layoutParams = lp
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(0x00000000)
                    setStroke(3, 0xFFFFFFFF.toInt())
                }
                setOnClickListener {
                    editText.setTextColor(color)
                    dialog.dismiss()
                }
            }
            val circle = View(this).apply {
                val size = resources.displayMetrics.density.times(38).toInt()
                val lp = FrameLayout.LayoutParams(size, size)
                lp.gravity = Gravity.CENTER
                layoutParams = lp
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                }
            }
            outer.addView(circle)
            row.addView(outer)
        }
        scroll.addView(row)
        root.addView(heading)
        root.addView(scroll)
        dialog.setContentView(root)
        dialog.show()
    }

    private fun showBgColorPickerSheet() {
        val dialog = BottomSheetDialog(this, R.style.DialogKeyBoardStyle)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 24)
            background = GradientDrawable().apply {
                cornerRadius = 24f
                setColor(0xFF101010.toInt())
            }
        }
        val heading = TextView(this).apply {
            text = getString(R.string.background)
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 16f
            setPadding(8, 8, 8, 16)
        }
        val scroll = HorizontalScrollView(this)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val colors = intArrayOf(
            0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0xFF3D38F5.toInt(), 0xFF8E24AA.toInt(),
            0xFFFF1744.toInt(), 0xFFFFC107.toInt(), 0xFF00BCD4.toInt(), 0xFF4CAF50.toInt(),
            0xFF1A1A1A.toInt(), 0xFF121212.toInt()
        )
        colors.forEach { color ->
            val outer = FrameLayout(this).apply {
                val lp = LinearLayout.LayoutParams(resources.displayMetrics.density.times(46).toInt(), resources.displayMetrics.density.times(46).toInt())
                lp.setMargins(12, 8, 12, 8)
                layoutParams = lp
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(0x00000000)
                    setStroke(3, 0xFFFFFFFF.toInt())
                }
                setOnClickListener {
                    editCard.setCardBackgroundColor(color)
                    dialog.dismiss()
                }
            }
            val circle = View(this).apply {
                val size = resources.displayMetrics.density.times(38).toInt()
                val lp = FrameLayout.LayoutParams(size, size)
                lp.gravity = Gravity.CENTER
                layoutParams = lp
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                }
            }
            outer.addView(circle)
            row.addView(outer)
        }
        scroll.addView(row)
        root.addView(heading)
        root.addView(scroll)
        dialog.setContentView(root)
        dialog.show()
    }

    private fun showFontPickerSheet() {
        fun dp(dp: Int): Int =
            (dp * resources.displayMetrics.density).toInt()

        val dialog = BottomSheetDialog(this, R.style.DialogKeyBoardStyle)

        // Root container (no opaque background so sheet looks clean)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(16), dp(24), dp(24))
            // Remove or keep rounded background — here we keep a slight rounded transparent bg for shape.
            background = GradientDrawable().apply {
                cornerRadius = dp(12).toFloat()
                // Transparent background (change to 0xFF101010.toInt() if you want a dark sheet)
                setColor(0x00000000)
            }
        }

        // Heading centered
        val heading = TextView(this).apply {
            text = getString(R.string.font_weight)
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 16f
            setPadding(dp(8), dp(8), dp(8), dp(16))
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Pill: full-width, chips centered inside
        val pill = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))

            // pill background (change to transparent if you don't want a pill bg)
            background = GradientDrawable().apply {
                cornerRadius = dp(24).toFloat()
                setColor(0xFF222222.toInt()) // keep pill bg; set to 0x00000000 for transparent
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // <- MATCH_PARENT width
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // small top/bottom margin so pill separates from heading
                topMargin = dp(4)
                bottomMargin = dp(4)
            }
        }

        // helper to create chips
        fun makeChip(text: String, onClick: () -> Unit): TextView {
            return TextView(this).apply {
                this.text = text
                setPadding(dp(28), dp(12), dp(28), dp(12))
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 14f
                // default transparent background
                background = GradientDrawable().apply {
                    cornerRadius = dp(24).toFloat()
                    setColor(0x00000000)
                }
                // chip click: clears previous highlight and highlights this one
                setOnClickListener {
                    for (i in 0 until pill.childCount) {
                        val child = pill.getChildAt(i)
                        if (child is TextView) {
                            child.background = GradientDrawable().apply {
                                cornerRadius = dp(24).toFloat()
                                setColor(0x00000000)
                            }
                        }
                    }
                    background = GradientDrawable().apply {
                        cornerRadius = dp(24).toFloat()
                        setColor(0xFFFF3366.toInt()) // selected color
                    }
                    onClick()
                }
            }
        }

        // Add chips + small spacer between them
        pill.addView(makeChip(getString(R.string.regular)) { editText.setTypeface(Typeface.DEFAULT, Typeface.NORMAL) })
        pill.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(12), 1)
        })
        pill.addView(makeChip(getString(R.string.bold)) { editText.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD) })
        pill.addView(Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(12), 1)
        })
        pill.addView(makeChip(getString(R.string.light)) { editText.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL) })

        // assemble
        root.addView(heading)
        root.addView(pill)

        dialog.setContentView(root)
        dialog.show()
    }

    // no-op placeholder removed (conflicting overload)

    private fun exportCardToImage(): Uri? {
        return try {
            // Force measure and layout to ensure card has proper dimensions
            // Use actual screen dimensions minus margins (16dp on each side = 32 total)
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            val margin = (16 * resources.displayMetrics.density).toInt() // 16dp in pixels
            val cardWidthSpec = View.MeasureSpec.makeMeasureSpec(screenWidth - (margin * 2), View.MeasureSpec.EXACTLY)
            val cardHeightSpec = View.MeasureSpec.makeMeasureSpec(screenHeight, View.MeasureSpec.AT_MOST)
            
            // Measure the card - this will also measure its children
            editCard.measure(cardWidthSpec, cardHeightSpec)
            
            // Layout the card - this will layout its children too
            editCard.layout(0, 0, editCard.measuredWidth, editCard.measuredHeight)
            
            // Get final dimensions - use measured dimensions as they're most accurate
            val w = editCard.measuredWidth
            val h = editCard.measuredHeight
            
            if (w <= 0 || h <= 0) {
                Log.e("TextPost", "Invalid card dimensions: $w x $h")
                return null
            }
            
            // Center text temporarily for export
            val oldGravity = editText.gravity
            val oldTextAlignment = editText.textAlignment
            val oldCursorVisible = editText.isCursorVisible
            try {
                editText.gravity = Gravity.CENTER
                editText.textAlignment = View.TEXT_ALIGNMENT_CENTER
                editText.isCursorVisible = false
            } catch (_: Exception) { }
            
            // Create bitmap with exact card dimensions
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.WHITE) // White background
            editCard.draw(canvas)
            
            // restore state
            try {
                editText.gravity = oldGravity
                editText.textAlignment = oldTextAlignment
                editText.isCursorVisible = oldCursorVisible
            } catch (_: Exception) { }
            
            val outFile = File(cacheDir, "text_post_${System.currentTimeMillis()}.png")
            FileOutputStream(outFile).use { fos ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            Log.d("TextPost", "Exported image: ${outFile.absolutePath}, size: ${w}x${h}")
            Uri.fromFile(outFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
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
            Log.e("TAG", "copyUrlToCache error: ${e.message}", e)
            null
        }
    }
}


