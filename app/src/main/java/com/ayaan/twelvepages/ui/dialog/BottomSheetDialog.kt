package com.ayaan.twelvepages.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.setGlobalTheme
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

open class BottomSheetDialog : BottomSheetDialogFragment() {
    lateinit var root : View
    lateinit var sheetBehavior: BottomSheetBehavior<*>
    private var mBottomSheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }
        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    override fun getTheme() = R.style.Widget_AppTheme_BottomSheet

    @SuppressLint("RestrictedApi")
    fun setupDialog(dialog: Dialog, style: Int, layoutId: Int) {
        super.setupDialog(dialog, style)
        View.inflate(context, layoutId, null)?.let {
            dialog.setContentView(it)
            setGlobalTheme(it)
            (it.parent as View).setBackgroundColor(Color.TRANSPARENT)
            (it.parent as View).fitsSystemWindows = false
            val layoutParams = (it.parent as View).layoutParams as CoordinatorLayout.LayoutParams
            (layoutParams.behavior as BottomSheetBehavior<*>?)?.let { behavior ->
                sheetBehavior = behavior
                behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
            }
            root = it
        }
    }
}
