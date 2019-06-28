package com.ayaan.twelvepages.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.alarm.AlarmManager
import kotlinx.android.synthetic.main.container_alarm_picker.*
import kotlinx.android.synthetic.main.dialog_base.*
import java.util.*

class AlarmPickerDialog(private val activity: Activity, private var offset: Long, private var dtAlarm: Long,
                        private val dtStart: Long = Long.MIN_VALUE, private val onResult: (Boolean, Long, Long) -> Unit) : BaseDialog(activity) {
    private val offsets = AlarmManager.offsets
    private var isTemplate = false

    init {
        if(dtAlarm == Long.MIN_VALUE) {
        }else if(dtAlarm == Long.MAX_VALUE) {
            isTemplate = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_alarm_picker, dpToPx(325))
        titleText.text = context.getString(R.string.set_alarm)
        titleIcon.setImageResource(R.drawable.alarm)

        confirmBtn.setOnClickListener {
            onResult.invoke(true, offset, dtAlarm)
            dismiss()
        }

        cancelBtn.text = context.getString(R.string.delete_alarm)
        cancelBtn.setTextColor(AppTheme.red)
        cancelBtn.setOnClickListener {
            onResult.invoke(false, offset, dtAlarm)
            dismiss()
        }

        dateBtn.setOnClickListener {
            showDialog(PopupOptionDialog(activity,
                    arrayOf(PopupOptionDialog.Item(str(R.string.alarm_at_time)),
                            PopupOptionDialog.Item(str(R.string.alarm_at_b1d)),
                            PopupOptionDialog.Item(str(R.string.alarm_at_b1w)),
                            PopupOptionDialog.Item(str(R.string.set_date))),
                    dateBtn, true) { index ->
                when(index) {
                    0 -> {

                    }
                    1 -> {

                    }
                    3 -> {
                        showDialog(DatePickerDialog(activity, dtAlarm) {

                        }, true, true, true, false)
                    }
                }
            }, true, false, true, false)


            /*
            showDialog(TimePickerDialog(activity, dtAlarm) { time ->
                offset = Long.MIN_VALUE
                dtAlarm = time
                setBtns(btns)
            }, true, true, true, false)
            */
        }

        timeBtn.setOnClickListener {
            showDialog(PopupOptionDialog(activity,
                    arrayOf(PopupOptionDialog.Item(str(R.string.morningAlarmTime)),
                            PopupOptionDialog.Item(str(R.string.afternoonAlarmTime)),
                            PopupOptionDialog.Item(str(R.string.eveningAlarmTime)),
                            PopupOptionDialog.Item(str(R.string.nightAlarmTime)),
                            PopupOptionDialog.Item(str(R.string.set_time))),
                    timeBtn, true) { index ->
                when(index) {
                    0 -> {

                    }
                    1 -> {

                    }
                    4 -> {
                        showDialog(TimePickerDialog(activity, dtAlarm) { time ->

                        }, true, true, true, false)
                    }
                }
            }, true, false, true, false)

        }
    }
}
