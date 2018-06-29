package com.hellowo.chating.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
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
