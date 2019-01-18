package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.TextView
import com.hellowo.journey.R
import com.hellowo.journey.setGlobalTheme
import kotlinx.android.synthetic.main.dialog_custom.*


class CustomDialog(activity: Activity, private val title: String, private val sub: String?,
                   private val options: Array<String>?, private val onResult: (Boolean, Int, String?) -> Unit) : Dialog(activity) {

    init {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_custom)
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

        if(options != null) {
            optionLy.visibility = View.VISIBLE
            bottomDivider.visibility = View.GONE
            confirmBtn.visibility = View.GONE
            (0 until optionLy.childCount).forEach { index ->
                if(index < options.size) {
                    (optionLy.getChildAt(index) as TextView).let { textview ->
                        textview.visibility = View.VISIBLE
                        textview.text = options[index]
                        textview.setOnClickListener {
                            onResult.invoke(true, index, null)
                            dismiss()
                        }
                    }
                }else {
                    (optionLy.getChildAt(index) as TextView).visibility = View.GONE
                }
            }
        }else {
            optionLy.visibility = View.GONE
        }

        confirmBtn.setOnClickListener {
            onResult.invoke(true, 0, input.text.toString())
            dismiss()
        }

        cancelBtn.setOnClickListener {
            onResult.invoke(false, 0, null)
            dismiss()
        }
    }

    fun showInput(hint: String, text: String) {
        inputLy.visibility = View.VISIBLE
        input.hint = hint
        input.setText(text)
        input.setSelection(text.length)
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) { confirmBtn.callOnClick() }
            return@setOnEditorActionListener false
        }
    }

}
