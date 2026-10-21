package com.app.external

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class CropOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paintFrame = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f * resources.displayMetrics.density
        color = Color.WHITE
        isAntiAlias = true
    }
    private val paintDim = Paint().apply {
        color = Color.parseColor("#99000000")
    }
    private val handleRadius = 14f * resources.displayMetrics.density

    // current crop rect in view coords
    private val rectF = RectF()
    // for moving/resizing
    private var activePointer = -1
    private var lastX = 0f
    private var lastY = 0f
    private var mode: Mode = Mode.NONE

    // fraction-based defaults (center square)
    init {
        post {
            val w = width.toFloat()
            val h = height.toFloat()
            val size = min(w, h) * 0.7f
            val left = (w - size) / 2f
            val top = (h - size) / 2f
            rectF.set(left, top, left + size, top + size)
            invalidate()
            onCropChanged?.invoke(getRect())
        }
    }

    enum class Mode { NONE, MOVE, RESIZE_LT, RESIZE_RT, RESIZE_LB, RESIZE_RB }

    // callback in integer view pixels
    var onCropChanged: ((android.graphics.Rect) -> Unit)? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // dim outside
        canvas.drawRect(0f, 0f, width.toFloat(), rectF.top, paintDim)
        canvas.drawRect(0f, rectF.bottom, width.toFloat(), height.toFloat(), paintDim)
        canvas.drawRect(0f, rectF.top, rectF.left, rectF.bottom, paintDim)
        canvas.drawRect(rectF.right, rectF.top, width.toFloat(), rectF.bottom, paintDim)

        // frame
        canvas.drawRect(rectF, paintFrame)

        // handles (4 corners)
        canvas.drawCircle(rectF.left, rectF.top, handleRadius, paintFrame)
        canvas.drawCircle(rectF.right, rectF.top, handleRadius, paintFrame)
        canvas.drawCircle(rectF.left, rectF.bottom, handleRadius, paintFrame)
        canvas.drawCircle(rectF.right, rectF.bottom, handleRadius, paintFrame)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activePointer = event.getPointerId(0)
                lastX = x; lastY = y
                mode = detectMode(x, y)
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastX
                val dy = y - lastY
                when (mode) {
                    Mode.MOVE -> moveRect(dx, dy)
                    Mode.RESIZE_LT -> resizeLeftTop(dx, dy)
                    Mode.RESIZE_RT -> resizeRightTop(dx, dy)
                    Mode.RESIZE_LB -> resizeLeftBottom(dx, dy)
                    Mode.RESIZE_RB -> resizeRightBottom(dx, dy)
                    else -> {}
                }
                lastX = x; lastY = y
                invalidate()
                onCropChanged?.invoke(getRect())
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointer = -1
                mode = Mode.NONE
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    private fun detectMode(x: Float, y: Float): Mode {
        val lt = PointF(rectF.left, rectF.top)
        val rt = PointF(rectF.right, rectF.top)
        val lb = PointF(rectF.left, rectF.bottom)
        val rb = PointF(rectF.right, rectF.bottom)
        if (distance(x, y, lt.x, lt.y) <= handleRadius * 1.6f) return Mode.RESIZE_LT
        if (distance(x, y, rt.x, rt.y) <= handleRadius * 1.6f) return Mode.RESIZE_RT
        if (distance(x, y, lb.x, lb.y) <= handleRadius * 1.6f) return Mode.RESIZE_LB
        if (distance(x, y, rb.x, rb.y) <= handleRadius * 1.6f) return Mode.RESIZE_RB
        if (rectF.contains(x, y)) return Mode.MOVE
        return Mode.NONE
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float) =
        kotlin.math.hypot((x1 - x2).toDouble(), (y1 - y2).toDouble()).toFloat()

    private fun moveRect(dx: Float, dy: Float) {
        var left = rectF.left + dx
        var top = rectF.top + dy
        var right = rectF.right + dx
        var bottom = rectF.bottom + dy

        // clamp to view
        val w = width.toFloat()
        val h = height.toFloat()
        val rw = rectF.width()
        val rh = rectF.height()

        left = max(0f, min(left, w - rw))
        top = max(0f, min(top, h - rh))
        right = left + rw
        bottom = top + rh
        rectF.set(left, top, right, bottom)
    }

    private fun resizeLeftTop(dx: Float, dy: Float) {
        val newLeft = max(0f, min(rectF.left + dx, rectF.right - 60f))
        val newTop = max(0f, min(rectF.top + dy, rectF.bottom - 60f))
        rectF.left = newLeft
        rectF.top = newTop
    }

    private fun resizeRightTop(dx: Float, dy: Float) {
        val newRight = min(width.toFloat(), max(rectF.right + dx, rectF.left + 60f))
        val newTop = max(0f, min(rectF.top + dy, rectF.bottom - 60f))
        rectF.right = newRight
        rectF.top = newTop
    }

    private fun resizeLeftBottom(dx: Float, dy: Float) {
        val newLeft = max(0f, min(rectF.left + dx, rectF.right - 60f))
        val newBottom = min(height.toFloat(), max(rectF.bottom + dy, rectF.top + 60f))
        rectF.left = newLeft
        rectF.bottom = newBottom
    }

    private fun resizeRightBottom(dx: Float, dy: Float) {
        val newRight = min(width.toFloat(), max(rectF.right + dx, rectF.left + 60f))
        val newBottom = min(height.toFloat(), max(rectF.bottom + dy, rectF.top + 60f))
        rectF.right = newRight
        rectF.bottom = newBottom
    }

    fun resetToCenterSquare() {
        post {
            val w = width.toFloat()
            val h = height.toFloat()
            val size = min(w, h) * 0.7f
            val left = (w - size) / 2f
            val top = (h - size) / 2f
            rectF.set(left, top, left + size, top + size)
            invalidate()
            onCropChanged?.invoke(getRect())
        }
    }

    fun getRect(): android.graphics.Rect {
        return android.graphics.Rect(
            rectF.left.toInt(),
            rectF.top.toInt(),
            rectF.right.toInt(),
            rectF.bottom.toInt()
        )
    }
}
