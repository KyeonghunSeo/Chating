package com.ayaan.twelvepages.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.Alarm
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.setGlobalTheme
import kotlinx.android.synthetic.main.dialog_alarm_picker.*


@SuppressLint("ValidFragment")
class AlarmPickerDialog(activity: Activity, private val record: Record, private val alarm: Alarm,
                        private val onResult: (Boolean, Long) -> Unit) : Dialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_alarm_picker)
        setLayout()
    }

    private fun setLayout() {
        setGlobalTheme(rootLy)
        alarmPicker.onSelected = { offset ->
            onResult.invoke(true, offset)
            dismiss()
        }
        deleteBtn.setOnClickListener {
            onResult.invoke(false, 0)
            dismiss()
        }
    }
}
