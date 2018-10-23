package com.hellowo.journey.calendar.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.hellowo.journey.R
import com.hellowo.journey.dpToPx
import com.hellowo.journey.startDialogShowAnimation
import com.hellowo.journey.startFromBottomSlideAppearAnimation
import kotlinx.android.synthetic.main.dialog_color_picker.*


class ColorPickerDialog(private val activity: Activity, private val color: Int,
                        private val onConfirmed: (Int, Int) -> Unit) : Dialog(activity) {

    init {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_color_picker)
        setLayout()
        setOnShowListener {
            startDialogShowAnimation(contentLy, dpToPx(10).toFloat())
        }
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()
        colorPicker.onSelceted = onConfirmed
        colorPicker.setDialog = this
    }

}
