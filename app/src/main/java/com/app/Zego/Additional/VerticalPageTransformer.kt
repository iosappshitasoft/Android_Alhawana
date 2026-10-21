package com.app.Zego.Additional

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.apply

private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f

class VerticalSlidePageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(view: View, position: Float) {
        view.apply {
            val pageWidth = width
            val pageHeight = height

            when {
                position < -1 -> { // Off-screen to the top
                    alpha = 0f
                }

                position <= 1 -> { // Visible pages [-1,1]
                    val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
                    val vertMargin = pageHeight * (1 - scaleFactor) / 2
                    val horzMargin = pageWidth * (1 - scaleFactor) / 2

                    // Instead of horizontal translation, use vertical translation
                    translationY = if (position < 0) {
                        vertMargin - horzMargin / 2
                    } else {
                        vertMargin + horzMargin / 2
                    }

                    // Scale page
                    scaleX = scaleFactor
                    scaleY = scaleFactor

                    // Fade page
                    alpha =
                        MIN_ALPHA + ((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)
                }

                else -> { // Off-screen to the bottom
                    alpha = 0f
                }
            }
        }
    }
}

