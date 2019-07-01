package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.os.Bundle
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.alarm.AlarmManager
import kotlinx.android.synthetic.main.container_alarm_picker.*
import kotlinx.android.synthetic.main.dialog_base.*
import java.util.*

class AlarmPickerDialog(private val activity: Activity, private var dayOffset: Int, private var time: Long,
                        private val dtStart: Long = Long.MIN_VALUE, private val onResult: (Boolean, Int, Long) -> Unit) : BaseDialog(activity) {
    private var isTemplate = dtStart == Long.MIN_VALUE

    init {
        if (dayOffset == Int.MIN_VALUE) dayOffset = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_alarm_picker, dpToPx(325))
        titleText.text = context.getString(R.string.set_alarm)
        titleIcon.setImageResource(R.drawable.alarm)

        confirmBtn.setOnClickListener {
            onResult.invoke(true, dayOffset, time)
            dismiss()
        }

        cancelBtn.text = context.getString(R.string.delete_alarm)
        cancelBtn.setTextColor(AppTheme.red)
        cancelBtn.setOnClickListener {
            onResult.invoke(false, dayOffset, time)
            dismiss()
        }

        dateBtn.setOnClickListener {
            showDialog(PopupOptionDialog(activity,
                    arrayOf(PopupOptionDialog.Item(str(R.string.alarm_at_time), subText = getDateText(0), isActive = dayOffset != 0),
                            PopupOptionDialog.Item(str(R.string.alarm_at_b1d), subText = getDateText(-1), isActive = dayOffset != -1),
                            PopupOptionDialog.Item(str(R.string.alarm_at_b1w), subText = getDateText(-7), isActive = dayOffset != -7),
                            PopupOptionDialog.Item(str(R.string.set_date), isActive = !isTemplate)), dateBtn, true) { index ->
                when(index) {
                    0 -> dayOffset = 0
                    1 -> dayOffset = -1
                    2 -> dayOffset = -7
                    3 -> {
                        val dialog = DatePickerDialog(activity, dtStart + DAY_MILL * dayOffset) { time ->
                            dayOffset = getDiffDate(dtStart, time)
                            setViews()
                        }
                        showDialog(dialog, true, true, true, false)
                        dialog.setMinDate(System.currentTimeMillis())
                    }
                }
                setViews()
            }, true, false, true, false)
        }

        timeBtn.setOnClickListener {
            showDialog(PopupOptionDialog(activity,
                    arrayOf(PopupOptionDialog.Item(str(R.string.morningAlarmTime), subText = getTimeText(AlarmManager.defaultAlarmTime[0])),
                            PopupOptionDialog.Item(str(R.string.afternoonAlarmTime), subText = getTimeText(AlarmManager.defaultAlarmTime[1])),
                            PopupOptionDialog.Item(str(R.string.eveningAlarmTime), subText = getTimeText(AlarmManager.defaultAlarmTime[2])),
                            PopupOptionDialog.Item(str(R.string.nightAlarmTime), subText = getTimeText(AlarmManager.defaultAlarmTime[3])),
                            PopupOptionDialog.Item(str(R.string.set_time))),
                    timeBtn, true) { index ->
                when(index) {
                    in 0..3 -> time = AlarmManager.defaultAlarmTime[index]
                    4 -> {
                        showDialog(TimePickerDialog(activity, getTodayStartTime() + time) { t ->
                            time = getOnlyTime(t)
                            setViews()
                        }, true, true, true, false)
                    }
                }
                setViews()
            }, true, false, true, false)

        }

        setViews()
    }

    private fun setViews() {
        dateOffsetText.text = when(dayOffset) {
            0 -> str(R.string.alarm_at_time)
            -1 -> str(R.string.alarm_at_b1d)
            -7 -> str(R.string.alarm_at_b1w)
            else -> {
                if(dayOffset > 0) {
                    String.format(App.resource.getString(R.string.date_after), Math.abs(dayOffset))
                }else {
                    String.format(App.resource.getString(R.string.date_before), Math.abs(dayOffset))
                }
            }
        }
        dateText.text = getDateText(dayOffset)

        timeOffsetText.text = when(time) {
            AlarmManager.defaultAlarmTime[0] -> str(R.string.morningAlarmTime)
            AlarmManager.defaultAlarmTime[1] -> str(R.string.afternoonAlarmTime)
            AlarmManager.defaultAlarmTime[2] -> str(R.string.eveningAlarmTime)
            AlarmManager.defaultAlarmTime[3] -> str(R.string.nightAlarmTime)
            else -> str(R.string.custom_time)
        }
        timeText.text = getTimeText(time)
    }

    private fun getDateText(offset: Int): String {
        return if(isTemplate) {
            ""
        }else {
            AppDateFormat.mdeShort.format(Date(dtStart + DAY_MILL * offset))
        }
    }

    private fun getTimeText(t: Long): String {
        return AppDateFormat.time.format(Date(getTodayStartTime() + t))
    }
}
