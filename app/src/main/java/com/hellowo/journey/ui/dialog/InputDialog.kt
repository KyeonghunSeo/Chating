package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import com.hellowo.journey.R
import com.hellowo.journey.setGlobalTheme
import com.hellowo.journey.showKeyPad
import kotlinx.android.synthetic.main.dialog_input.*


class InputDialog(activity: Activity, private val title: String, private val sub: String?, private val hint: String?,
                  private val text: String, private val isSingleLine: Boolean, private val onResult: (Boolean, String) -> Unit)
    : BaseDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_input)
        setLayout()
        setOnShowListener {
            showKeyPad(input)
        }
    }

    private fun setLayout() {
        setGlobalTheme(rootLy)
        titleText.text = title

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
