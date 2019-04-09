package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.hellowo.journey.R
import com.hellowo.journey.setGlobalTheme
import com.hellowo.journey.startDialogShowAnimation
import kotlinx.android.synthetic.main.dialog_time_picker.*
import java.util.*


class TimePickerDialog(activity: Activity, time: Long,
                       private val onResult: (Long) -> Unit) : Dialog(activity) {
    private val cal = Calendar.getInstance()

    init {
        cal.timeInMillis = time
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_time_picker)
        setLayout()
        setOnShowListener {
            startDialogShowAnimation(contentLy)
        }
    }

    private fun setLayout() {
        setGlobalTheme(rootLy)
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()

        timePicker.currentHour = cal.get(Calendar.HOUR_OF_DAY)
        timePicker.currentMinute = cal.get(Calendar.MINUTE)

        cancelBtn.setOnClickListener { dismiss() }
        confirmBtn.setOnClickListener {
            cal.set(Calendar.HOUR_OF_DAY, timePicker.currentHour)
            cal.set(Calendar.MINUTE, timePicker.currentMinute)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            onResult.invoke(cal.timeInMillis)
            dismiss()
        }
    }

}
