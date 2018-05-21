package com.hellowo.colosseum.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.CardView
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.hellowo.chating.R

@SuppressLint("ValidFragment")
class EnterCommentDialog(private val dialogInterface: (String) -> Unit) : BottomSheetDialog() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.dialog_input_text, null)
        dialog.setContentView(contentView)

        val layoutParams = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        sheetBehavior = layoutParams.behavior as BottomSheetBehavior<*>?
        if (sheetBehavior != null) {
            sheetBehavior?.setBottomSheetCallback(mBottomSheetBehaviorCallback)

            val messageInput = contentView.findViewById<EditText>(R.id.messageInput)
            messageInput.setHint(R.string.enter_comment)

            val sendBtn = contentView.findViewById<CardView>(R.id.sendBtn)
            sendBtn.setOnClickListener {
                if (!TextUtils.isEmpty(messageInput.text)) {
                    dialogInterface.invoke(messageInput.text.toString())
                    dismiss()
                }
            }

            messageInput.postDelayed({
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(messageInput,
                        InputMethodManager.SHOW_IMPLICIT)
            }, 0)
        }
    }
}
