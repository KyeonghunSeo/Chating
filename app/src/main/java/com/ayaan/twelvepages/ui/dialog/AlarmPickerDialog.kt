package com.ayaan.twelvepages.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.model.Alarm
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.setGlobalTheme
import com.ayaan.twelvepages.ui.view.AlarmPickerView
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.container_alarm_picker.*


@SuppressLint("ValidFragment")
class AlarmPickerDialog(activity: Activity, private val record: Record, private val alarm: Alarm,
                        private val onResult: (Boolean, Long) -> Unit) : BaseDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout()
        setGlobalTheme(rootLy)
    }

    private fun setLayout() {
        titleText.text = context.getString(R.string.set_alarm)
        hideBottomBtnsLy()

        container.layoutParams.width = dpToPx(330)
        container.requestLayout()
        LayoutInflater.from(context).inflate(R.layout.container_alarm_picker, container, true)

        alarmPicker.onSelected = { offset ->
            onResult.invoke(true, offset)
            dismiss()
        }
    }
}
