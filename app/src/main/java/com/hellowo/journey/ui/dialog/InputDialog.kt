package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import com.hellowo.journey.R
import com.hellowo.journey.setGlobalTheme
import kotlinx.android.synthetic.main.dialog_input.*


class InputDialog(activity: Activity, private val title: String, private val sub: String?, private val hint: String?,
                  private val text: String, private val onResult: (Boolean, String) -> Unit) : Dialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        setContentView(R.layout.dialog_input)
        setGlobalTheme(rootLy)
        setLayout()
        setOnShowListener {}
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()

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
