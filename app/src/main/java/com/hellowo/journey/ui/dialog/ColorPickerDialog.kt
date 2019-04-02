package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import kotlinx.android.synthetic.main.dialog_color_picker.*


class ColorPickerDialog(activity: Activity, private val color: Int,
                        private val onResult: (Int, Int) -> Unit) : Dialog(activity) {

    init {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_color_picker)
        setLayout()
        setOnShowListener {
            window.setLayout(MATCH_PARENT, WRAP_CONTENT)
        }
    }

    private fun setLayout() {
        rootLy.setBackgroundColor(Color.WHITE)
        colorPicker.onSelceted = { colorKey, color ->
            onResult.invoke(colorKey, color)
            dismiss()
        }
    }

}
