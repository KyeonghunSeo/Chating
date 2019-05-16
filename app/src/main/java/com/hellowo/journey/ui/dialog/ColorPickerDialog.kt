package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.hellowo.journey.R
import kotlinx.android.synthetic.main.dialog_color_picker.*


class ColorPickerDialog(activity: Activity, private val color: Int,
                        private val onResult: (Int) -> Unit) : Dialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_color_picker)
        setLayout()
        setOnShowListener {}
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()
        rootLy.setBackgroundColor(Color.WHITE)
        colorPicker.onSelceted = { colorKey ->
            onResult.invoke(colorKey)
            dismiss()
        }
    }

}
