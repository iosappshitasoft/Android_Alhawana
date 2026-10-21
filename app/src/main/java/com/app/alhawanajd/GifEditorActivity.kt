package com.app.alhawanajd


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import com.app.alhawanajd.databinding.ActivityGifEditorBinding
import com.app.external.video_trimmer.utils.TrimmerUtils
import com.arthenica.ffmpegkit.FFmpegKit
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener
import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarFinalValueListener
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import java.io.File

class GifEditorActivity : BaseActivity() {

    private lateinit var binding: ActivityGifEditorBinding
    private var exoPlayer: ExoPlayer? = null

    private var videoPath: String = ""
    private var totalDuration = 0L

    private var trimStart = 0L      // selected start position
    private val trimDuration = 5000L // FIXED 5 seconds
    private var isProgrammaticChange = false

    private var trimType= 1

    private var lastMinValue: Long = 0
    private var lastMaxValue: Long = 0

    private var hidePlayerSeek = false   // ← or from your logic
    // ← set based on your use case

    private var fixedGap = 5000          // trim window width
    private var minGap = 3000            // minimum trim for type 2
    private var maxToGap = 10000         // max position for type 3
    private var minFromGap = 2000

    private val uiHandler = Handler(Looper.getMainLooper())

    private var isUserDraggingController = false
    private var isUserDraggingRange = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGifEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoPath = intent.getStringExtra("VIDEO_PATH") ?: ""
        if (videoPath.isEmpty()) {
            Toast.makeText(this, "Invalid video!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        hideUnusedTrimUI()
        setupPlayer()
//        setupTrimSeekbar()
        setupGenerateButton()


    }

    // ---------------- HIDE ALL OLD TRIM UI ---------------- //
    private fun hideUnusedTrimUI() {
        binding.videoController.rangeSeekBar.visibility = View.VISIBLE
        binding.videoController.seekbarController.visibility = View.VISIBLE
        binding.videoController.txtStartDuration.visibility = View.VISIBLE
        binding.videoController.txtEndDuration.visibility = View.VISIBLE
        binding.videoController.txtTrimDuration.visibility = View.VISIBLE
        binding.progressCircular.visibility = View.GONE
        binding.imagePlayPause.visibility = View.GONE
    }

    private fun setupTrimSeekbar() {

        Log.d("TAG", "setupTrimSeekbar111: "+totalDuration.toFloat())
        
        // Prevent change listener from interfering during setup
        isProgrammaticChange = true
        
        binding.videoController.seekbarController.setMaxValue(totalDuration.toFloat()).apply()
        binding.videoController.rangeSeekBar.setMaxValue(totalDuration.toFloat()).apply()
        binding.videoController.rangeSeekBar.setMaxStartValue(totalDuration.toFloat()).apply()
        
        // Initialize initial range values based on trim type
        if (trimType == 1) {
            // For fixed gap, set initial min position first, then fix gap
            // The library will automatically maintain the gap when user drags
            binding.videoController.rangeSeekBar
                .setMinStartValue(0f)
                .setFixGap(fixedGap.toFloat())
                .apply()
            // Initialize our tracking variables - they'll be updated by the change listener callback
            lastMinValue = 0L
            lastMaxValue = fixedGap.toLong().coerceAtMost(totalDuration)
            trimStart = 0L
        } else if (trimType == 2) {
            binding.videoController.rangeSeekBar.setMaxStartValue(minGap.toFloat())
            binding.videoController.rangeSeekBar.setGap(minGap.toFloat()).apply()
            lastMinValue = 0L
            lastMaxValue = totalDuration
        } else if (trimType == 3) {
            binding.videoController.rangeSeekBar.setMaxStartValue(maxToGap.toFloat())
            binding.videoController.rangeSeekBar.setGap(minFromGap.toFloat()).apply()
            lastMinValue = 0L
            lastMaxValue = maxToGap.toLong().coerceAtMost(totalDuration)
        } else {
            binding.videoController.rangeSeekBar.setGap(2f).apply()
            lastMinValue = 0L
            lastMaxValue = totalDuration
        }
        
        // After setup, get actual values from the range seekbar (it may have adjusted them)
        // This ensures we're in sync with what the library actually set
        val actualMin = binding.videoController.rangeSeekBar.getSelectedMinValue()?.toLong() ?: lastMinValue
        val actualMax = binding.videoController.rangeSeekBar.getSelectedMaxValue()?.toLong() ?: lastMaxValue
        lastMinValue = actualMin
        lastMaxValue = actualMax
        trimStart = actualMin
        
        // Allow change listener to work normally after setup
        isProgrammaticChange = false
        
        // Initialize controller seekbar with initial position
        if (!hidePlayerSeek) {
            binding.videoController.seekbarController
                .setMinStartValue(actualMin.toFloat()) // Start at actual position
                ?.apply()
            binding.videoController.seekbarController.visibility = View.VISIBLE
        } else {
            binding.videoController.seekbarController.setVisibility(View.GONE)
        }
        
        // Update initial text fields with actual values
        binding.videoController.txtStartDuration.setText(TrimmerUtils.formatSeconds(actualMin))
        binding.videoController.txtEndDuration.setText(TrimmerUtils.formatSeconds(actualMax))
        binding.videoController.txtTrimDuration.setText(TrimmerUtils.formatSeconds(actualMax - actualMin))

        binding.videoController.rangeSeekBar.setOnRangeSeekbarFinalValueListener(OnRangeSeekbarFinalValueListener { minValue: Number?, maxValue: Number? ->
            // Don't process if this is from a programmatic update
            if (isProgrammaticChange) return@OnRangeSeekbarFinalValueListener
            
            isUserDraggingRange = false

            // Update trimStart when user finishes dragging
            val minVal = minValue?.toLong() ?: 0L
            val maxVal = maxValue?.toLong() ?: 0L
            
            // IMPORTANT: Update our tracking variables to match what the user selected
            // Do NOT programmatically update the range seekbar position - it's already correct!
            trimStart = minVal
            lastMinValue = minVal
            lastMaxValue = maxVal
            
            // Show controller after dragging ends (it was hidden during drag)
            if (!hidePlayerSeek) {
                binding.videoController.seekbarController.setVisibility(View.VISIBLE)
                // Update controller position to current video position
                val currentPos = exoPlayer?.currentPosition ?: minVal
                val constrainedPos = currentPos.coerceIn(minVal, maxVal)
                binding.videoController.seekbarController
                    .setMinStartValue(constrainedPos.toFloat())
                    ?.apply()
            }
        })

        binding.videoController.rangeSeekBar.setOnRangeSeekbarChangeListener(OnRangeSeekbarChangeListener { minValue: Number?, maxValue: Number? ->
            // Don't process if this is from a programmatic update
            if (isProgrammaticChange) return@OnRangeSeekbarChangeListener

            isUserDraggingRange = true
            val minVal = (minValue?.toLong() ?: 0L).coerceAtLeast(0L)
            val maxVal = (maxValue?.toLong() ?: 0L).coerceAtMost(totalDuration)
            
            // Store previous values to check if we need to seek
            val prevMinValue = lastMinValue
            
            // Update trimStart immediately when range changes
            trimStart = minVal
            lastMinValue = minVal
            lastMaxValue = maxVal
            
            // Only seek if the min value actually changed (user dragged left thumb)
            if (prevMinValue != minVal) {
                seekTo(minVal)
            }
            
            // Hide controller during dragging (as per Java implementation)
            if (!hidePlayerSeek) {
                binding.videoController.seekbarController.setVisibility(View.GONE)
            }
            
            // Update text fields
            binding.videoController.txtStartDuration.setText(TrimmerUtils.formatSeconds(minVal))
            binding.videoController.txtEndDuration.setText(TrimmerUtils.formatSeconds(maxVal))
            binding.videoController.txtTrimDuration.setText(TrimmerUtils.formatSeconds(maxVal - minVal))
            if (trimType == 3) setDoneColor(minVal, maxVal)
        })
        
        binding.videoController.seekbarController.setOnSeekbarFinalValueListener(OnSeekbarFinalValueListener { value: Number? ->
            isUserDraggingController = false
            val value1 = value as Long
            val constrainedValue = value1.coerceIn(lastMinValue, lastMaxValue)
            seekTo(constrainedValue)
        })
        
        // Track when user starts dragging controller
        binding.videoController.seekbarController.setOnTouchListener { _, _ ->
            isUserDraggingController = true
            false // Let the seekbar handle the touch
        }


    }
    private fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }
    private fun setDoneColor(start: Long, end: Long) {
        // your UI logic here
    }

    // ---------------- PLAYER SETUP ------------------------ //
    private fun setupPlayer() {

        exoPlayer = ExoPlayer.Builder(this).build()
        binding.playerViewLib.player = exoPlayer
        binding.playerViewLib.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)

        val item = MediaItem.fromUri(videoPath)
        exoPlayer?.setMediaItem(item)

        exoPlayer?.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(state: Int) {

                if (state == Player.STATE_READY) {

                    totalDuration = exoPlayer?.duration ?: 0L
                    Log.d("TAG", "onPlaybackStateChanged: "+totalDuration)
                    setupTrimSeekbar()
                    startSeekbarAutoUpdate()

                    // If video < 5 sec, auto adjust
                    if (totalDuration < trimDuration) {
                        trimStart = 0
                    }

                    binding.progressCircular.visibility = View.GONE
                }
            }
        })

        exoPlayer?.prepare()
        exoPlayer?.playWhenReady = true
    }

    private fun startSeekbarAutoUpdate() {

        uiHandler.post(object : Runnable {
            override fun run() {
                val player = exoPlayer ?: return
                val pos = player.currentPosition

                // Use lastMinValue and lastMaxValue for the selected range
                val startPos = lastMinValue
                val endPos = lastMaxValue

                // Constrain player position within selected range and loop
                if (pos >= endPos || pos < startPos) {
                    exoPlayer?.seekTo(startPos)
                }

                // Update controller position only when not dragging
                // Constrain controller position within selected range
                if (!isUserDraggingController && !isUserDraggingRange && !hidePlayerSeek) {
                    val constrainedPos = pos.coerceIn(startPos, endPos)
                    binding.videoController.seekbarController
                        .setMinStartValue(constrainedPos.toFloat())
                        ?.apply()
                    
                    // Ensure controller is visible
                    binding.videoController.seekbarController.visibility = View.VISIBLE
                }

                uiHandler.postDelayed(this, 40)
            }
        })
    }



    // ---------------- GENERATE GIF ------------------------ //
    private fun setupGenerateButton() {
        binding.generateLay.setOnClickListener { generateGif() }
    }

    private fun generateGif() {

        val output = File(getExternalFilesDir(null),
            "GIF_${System.currentTimeMillis()}.gif"
        )

        val startSeconds = trimStart / 1000f

        val cmd = "-ss $startSeconds -t 5 -i \"$videoPath\" " +
                "-vf fps=10,scale=480:-1:flags=lanczos \"${output.absolutePath}\""

        Toast.makeText(this, "Generating GIF…", Toast.LENGTH_SHORT).show()

        FFmpegKit.executeAsync(cmd) { session ->

            val result = session.returnCode

            runOnUiThread {
                if (result.isValueSuccess) {
                    Toast.makeText(
                        this,
                        "GIF Saved:\n${output.absolutePath}",
                        Toast.LENGTH_LONG
                    ).show()

                } else {
                    Toast.makeText(this,
                        "GIF failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        exoPlayer?.release()
        super.onDestroy()
    }

    override fun onNetworkChanged(isConnected: Boolean) {}
}
