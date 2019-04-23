package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.hellowo.journey.R
import kotlinx.android.synthetic.main.dialog_style_picker.*

class StylePickerDialog(activity: Activity, private val colorKey: Int, private val type: Int,
                        private val title: String, private val onResult: (Int) -> Unit) : Dialog(activity) {

    init {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        window.attributes.gravity = Gravity.BOTTOM
        setContentView(R.layout.dialog_style_picker)
        setLayout()
        setOnShowListener {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun setLayout() {
        rootLy.setBackgroundColor(Color.WHITE)
        stylePicker.type = type
        stylePicker.colorKey = colorKey
        if(title.isNotEmpty()) stylePicker.title = title
        stylePicker.onSelected = { style ->
            onResult.invoke(style)
            dismiss()
        }
        stylePicker.adapter?.notifyDataSetChanged()
    }

}
