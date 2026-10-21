package com.app.Zego.BottomSheet

import android.graphics.Color
import android.view.View
import android.view.WindowManager
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.app.alhawanajd.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

object BottomSheetSystemBarsHelper {

    const val SHEET_BG_COLOR = "#050505"

    fun applyTheme(): Int = R.style.ZegoDarkBottomSheetDialog

    fun configure(
        dialog: BottomSheetDialog,
        maxHeightFraction: Float,
        peekHeightFraction: Float = maxHeightFraction,
        skipCollapsed: Boolean = true
    ) {
        dialog.window?.apply {
            WindowCompat.setDecorFitsSystemWindows(this, false)
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            navigationBarColor = Color.parseColor(SHEET_BG_COLOR)
            statusBarColor = Color.TRANSPARENT
        }

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            ) ?: return@setOnShowListener

            applyRoundedBackground(bottomSheet)

            val behavior = BottomSheetBehavior.from(bottomSheet)
            val displayMetrics = bottomSheet.context.resources.displayMetrics
            val maxHeight = (displayMetrics.heightPixels * maxHeightFraction).toInt()
            val peekHeightValue = (displayMetrics.heightPixels * peekHeightFraction).toInt()

            bottomSheet.layoutParams.height = maxHeight
            behavior.maxHeight = maxHeight
            behavior.peekHeight = peekHeightValue
            behavior.state = if (peekHeightFraction < maxHeightFraction) BottomSheetBehavior.STATE_COLLAPSED else BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = skipCollapsed

            ViewCompat.setOnApplyWindowInsetsListener(bottomSheet) { view, insets ->
                val navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                view.updatePadding(bottom = navBarInsets.bottom)
                insets
            }
            ViewCompat.requestApplyInsets(bottomSheet)
        }
    }

    fun applyRoundedBackground(view: View) {
        view.background = AppCompatResources.getDrawable(view.context, R.drawable.bg_bottom_sheet_top_40)
        view.clipToOutline = true
    }

    fun applyParentBackground(view: View) {
        (view.parent as? View)?.let { applyRoundedBackground(it) }
    }

    fun applyNavigationBarColor(dialog: BottomSheetDialog?) {
        dialog?.window?.navigationBarColor = Color.parseColor(SHEET_BG_COLOR)
    }
}
