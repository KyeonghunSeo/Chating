package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.showKeyPad
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.container_input_dlg.*


class InputDialog(activity: Activity, private val icon: Int = Int.MIN_VALUE, private val title: String,
                  private val sub: String?, private val hint: String?, private val text: String,
                  private val isSingleLine: Boolean, private val onResult: (Boolean, String) -> Unit)
    : BaseDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_input_dlg, dpToPx(300))
        setLayout()
        setOnShowListener { showKeyPad(input) }
    }

    private fun setLayout() {
        titleText.text = title

        if(icon != Int.MIN_VALUE) titleIcon.setImageResource(icon)
        else titleIcon.visibility = View.GONE

        if(!sub.isNullOrEmpty()) {
            subText.visibility = View.VISIBLE
            subText.text = sub
        }else {
            subText.visibility = View.GONE
        }

        if(!hint.isNullOrEmpty()) {
            input.hint = hint
        }

        if(isSingleLine) {
            input.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == IME_ACTION_DONE) {
                    confirmBtn.callOnClick()
                }
                return@setOnEditorActionListener false
            }
        }else {
            input.setSingleLine(false)
        }

        input.setText(text)
        input.setSelection(text.length)

        confirmBtn.setOnClickListener {
            onResult.invoke(true, input.text.toString())
            dismiss()
        }

        cancelBtn.setOnClickListener {
            onResult.invoke(false, input.text.toString())
            dismiss()
        }
    }

}
