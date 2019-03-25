package com.hellowo.journey.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hellowo.journey.*
import com.hellowo.journey.manager.CalendarManager
import com.hellowo.journey.model.TimeObject
import kotlinx.android.synthetic.main.dialog_start_end_picker.*
import java.util.*

@SuppressLint("ValidFragment")
class StartEndPickerDialog(activity: Activity, private val timeObject: TimeObject,
                           private val onConfirmed: (Calendar, Calendar, Boolean) -> Unit) : Dialog(activity) {

    var timeMode = if(timeObject.allday) 0 else 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_start_end_picker)
        setGlobalTheme(rootLy)
        setLayout()
        setOnShowListener {
            startDialogShowAnimation(contentLy)
        }
    }

    private fun setLayout() {
        calendarView.startCal.timeInMillis = timeObject.dtStart
        calendarView.endCal.timeInMillis = timeObject.dtEnd
        calendarView.onDrawed = { setDateText() }

        startTab.setOnClickListener {
            calendarView.startEndMode = 0
            setModeLy()
        }

        endTab.setOnClickListener {
            calendarView.startEndMode = 1
            setModeLy()
        }

        calBtn.setOnClickListener {
            timeMode = 0
            setModeLy()
        }

        timeBtn.setOnClickListener {
            timeMode = 1
            setModeLy()
        }

        confirmBtn.setOnClickListener {
            onConfirmed.invoke(calendarView.startCal, calendarView.endCal, !timeSwitch.isChecked)
            dismiss()
        }

        cancelBtn.setOnClickListener { dismiss() }

        timeSwitch.isChecked = !timeObject.allday
        timeSwitch.setOnCheckedChangeListener { compoundButton, checked ->
            timeMode = if(checked) {
                setTimeNearOClock(calendarView.startCal)
                setTime1HourInterval(calendarView.startCal, calendarView.endCal)
                1
            }else {
                setCalendarTime0(calendarView.startCal)
                setCalendarTime23(calendarView.endCal)
                0
            }
            setModeLy()
        }

        setModeLy()
    }

    private fun setTimeView(cal: Calendar) {
        timeWheel.setOnTimeChangedListener(null)
        timeWheel.currentHour = cal.get(Calendar.HOUR_OF_DAY)
        timeWheel.currentMinute = cal.get(Calendar.MINUTE)
        timeWheel.setOnTimeChangedListener { timePicker, h, m ->
            if(calendarView.startEndMode == 0) {
                calendarView.startCal.set(Calendar.HOUR_OF_DAY, h)
                calendarView.startCal.set(Calendar.MINUTE, m)
                if(calendarView.startCal > calendarView.endCal) {
                    calendarView.endCal.timeInMillis = calendarView.startCal.timeInMillis
                }
            }else {
                calendarView.endCal.set(Calendar.HOUR_OF_DAY, h)
                calendarView.endCal.set(Calendar.MINUTE, m)
                if(calendarView.startCal > calendarView.endCal) {
                    calendarView.startCal.timeInMillis = calendarView.endCal.timeInMillis
                }
            }
            setDateText()
        }
    }

    private fun setDateText() {
        val dtStart = calendarView.startCal.timeInMillis
        val dtEnd = calendarView.endCal.timeInMillis
        if(!timeSwitch.isChecked) {
            startTimeTText.text = "${AppDateFormat.date.format(dtStart)} ${AppDateFormat.dow.format(dtStart)}"
            startTimeYMDText.text =  "${AppDateFormat.ymDate.format(dtStart)}"
            endTimeTText.text = "${AppDateFormat.date.format(dtEnd)} ${AppDateFormat.dow.format(dtEnd)}"
            endTimeYMDText.text =  "${AppDateFormat.ymDate.format(dtEnd)}"
        }else {
            startTimeTText.text = AppDateFormat.time.format(dtStart)
            startTimeYMDText.text = AppDateFormat.ymdDate.format(dtStart)
            endTimeTText.text = AppDateFormat.time.format(dtEnd)
            endTimeYMDText.text = AppDateFormat.ymdDate.format(dtEnd)
        }

        if(calendarView.startEndMode == 0) {
            startText.setTextColor(CalendarManager.selectedDateColor)
            startTimeTText.setTextColor(CalendarManager.selectedDateColor)
            startTimeYMDText.setTextColor(CalendarManager.selectedDateColor)

            endText.setTextColor(CalendarManager.selectedBackgroundColor)
            endTimeTText.setTextColor(CalendarManager.selectedBackgroundColor)
            endTimeYMDText.setTextColor(CalendarManager.selectedBackgroundColor)
        }else {
            startText.setTextColor(CalendarManager.selectedBackgroundColor)
            startTimeTText.setTextColor(CalendarManager.selectedBackgroundColor)
            startTimeYMDText.setTextColor(CalendarManager.selectedBackgroundColor)

            endText.setTextColor(CalendarManager.selectedDateColor)
            endTimeTText.setTextColor(CalendarManager.selectedDateColor)
            endTimeYMDText.setTextColor(CalendarManager.selectedDateColor)
        }
    }

    private fun setModeLy() {
        if(timeSwitch.isChecked) {
            timeBtn.visibility = View.VISIBLE
        }else {
            timeBtn.visibility = View.GONE
        }

        if(timeMode == 0) {
            timeLy.visibility = View.GONE
            calendarView.visibility = View.VISIBLE
            calBtn.alpha = 1f
            timeBtn.alpha = 0.3f
            if(calendarView.startEndMode == 0) {
                calendarView.drawCalendar(calendarView.startCal.timeInMillis)
            }else {
                calendarView.drawCalendar(calendarView.endCal.timeInMillis)
            }
        }else {
            timeLy.visibility = View.VISIBLE
            calendarView.visibility = View.GONE
            calBtn.alpha = 0.3f
            timeBtn.alpha = 1f
            if(calendarView.startEndMode == 0) {
                setTimeView(calendarView.startCal)
            }else {
                setTimeView(calendarView.endCal)
            }
        }
        setDateText()
    }

}
