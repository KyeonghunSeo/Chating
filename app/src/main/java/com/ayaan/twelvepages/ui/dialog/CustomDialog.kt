package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.TextView
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.setGlobalTheme
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.container_custom_dlg.*


class CustomDialog(activity: Activity, private val title: String, private val sub: String?,
                   private val options: Array<String>?, private val icon: Int = Int.MIN_VALUE,
                   private val onResult: (Boolean, Int, String?) -> Unit) : BaseDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_custom_dlg, dpToPx(300))
        titleText.text = title
        if(icon != Int.MIN_VALUE) titleIcon.setImageResource(icon)
        else titleIcon.visibility = View.GONE

        if(!sub.isNullOrEmpty()) {
            subText.visibility = View.VISIBLE
            subText.text = sub
        }else {
            subText.visibility = View.GONE
        }

        if(options != null) {
            optionLy.visibility = View.VISIBLE
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
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            onResult.invoke(true, 0, input.text.toString())
            dismiss()
        }

        cancelBtn.setOnClickListener {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            onResult.invoke(false, 0, null)
            dismiss()
        }

        setOnCancelListener {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
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
            if (actionId == IME_ACTION_DONE) {
                confirmBtn.callOnClick()
            }
            return@setOnEditorActionListener false
        }
    }

}
