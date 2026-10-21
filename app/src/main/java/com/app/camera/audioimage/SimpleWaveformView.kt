package com.app.camera.audioimage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class SimpleWaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF3366.toInt() // Pink color
        style = Paint.Style.FILL
    }

    private var waveformData: FloatArray = FloatArray(0)
    private var barWidth = 4f
    private var barGap = 2f
    private var maxBarHeight = 0f

    fun setWaveformData(data: FloatArray) {
        waveformData = data
        invalidate()
    }

    fun updateAmplitude(amplitude: Double) {
        // Shift array left and add new amplitude at the end
        if (waveformData.isNotEmpty()) {
            System.arraycopy(waveformData, 1, waveformData, 0, waveformData.size - 1)
            waveformData[waveformData.size - 1] = amplitude.toFloat()
            invalidate()
        }
    }

    fun initializeWaveform(barCount: Int) {
        waveformData = FloatArray(barCount) { 0.3f } // Start with small bars
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (waveformData.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2f
        
        maxBarHeight = height * 0.7f // Max bar height is 70% of view height

        val totalBarWidth = barWidth + barGap
        val availableWidth = width - paddingStart - paddingEnd
        val maxBars = (availableWidth / totalBarWidth).toInt()
        val barsToDraw = min(waveformData.size, maxBars)

        var x = paddingStart.toFloat()
        
        for (i in 0 until barsToDraw) {
            val amplitude = waveformData[i].coerceIn(0.1f, 1.0f)
            val barHeight = amplitude * maxBarHeight
            
            val top = centerY - barHeight / 2f
            val bottom = centerY + barHeight / 2f
            
            canvas.drawRoundRect(
                x,
                top,
                x + barWidth,
                bottom,
                2f,
                2f,
                paint
            )
            
            x += totalBarWidth
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // Ensure we have a reasonable height
        val minHeight = (40 * resources.displayMetrics.density).toInt()
        val measuredHeight = measuredHeight
        if (measuredHeight < minHeight) {
            setMeasuredDimension(measuredWidth, minHeight)
        }
    }
}

