package com.hellowo.chating.calendar.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.cardview.widget.CardView
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.hellowo.chating.R
import com.hellowo.chating.ui.dialog.BottomSheetDialog



@SuppressLint("ValidFragment")
class TypePickerDialog(private val dialogInterface: (String) -> Unit) : BottomSheetDialog() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.dialog_type_picker, null)
        dialog.setContentView(contentView)

        val layoutParams = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        sheetBehavior = layoutParams.behavior as BottomSheetBehavior<*>?
        if (sheetBehavior != null) {
            sheetBehavior?.setBottomSheetCallback(mBottomSheetBehaviorCallback)
            dialog.setOnShowListener {
                sheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
            }
            (contentView.parent as View).setBackgroundColor(Color.TRANSPARENT)
            //val messageInput = contentView.findViewById<EditText>(R.id.messageInput)
        }
    }
}
