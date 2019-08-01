package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.os.Bundle
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import kotlinx.android.synthetic.main.container_time_picker_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*
import java.util.*


class TimePickerDialog(activity: Activity, time: Long, private val onResult: (Long) -> Unit) : BaseDialog(activity) {
    private val cal = Calendar.getInstance()

    init {
        cal.timeInMillis = time
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_time_picker_dlg, dpToPx(300))
        setLayout()
    }

    private fun setLayout() {
        hideHeader()
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
