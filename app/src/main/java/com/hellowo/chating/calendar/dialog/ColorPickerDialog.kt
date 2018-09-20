package com.hellowo.chating.calendar.dialog

import android.animation.LayoutTransition.CHANGING
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.hellowo.chating.R
import com.hellowo.chating.dpToPx
import com.hellowo.chating.startFromBottomSlideAppearAnimation
import kotlinx.android.synthetic.main.dialog_color_picker.*


class ColorPickerDialog(private val activity: Activity, private val color: Int,
                        private val onConfirmed: (Int, Int) -> Unit) : Dialog(activity) {

    init {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_color_picker)
        setLayout()
        setOnShowListener {
            startFromBottomSlideAppearAnimation(contentLy, dpToPx(10).toFloat())
        }
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()
        colorPicker.onSelceted = onConfirmed
        colorPicker.setDialog = this
    }

}
