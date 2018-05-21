package com.hellowo.colosseum.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.view.View

open class BottomSheetDialog : BottomSheetDialogFragment() {
    internal var sheetBehavior: BottomSheetBehavior<*>? = null

    internal var mBottomSheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }
        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
    }
}
