package com.ayaan.twelvepages.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.model.KoreanLunarCalendar
import com.ayaan.twelvepages.model.Record
import kotlinx.android.synthetic.main.dialog_start_end_picker.*
import java.util.*



@SuppressLint("ValidFragment")
class StartEndPickerDialog(activity: Activity, private val record: Record,
                           private val onConfirmed: (Calendar, Calendar, Boolean) -> Unit) : Dialog(activity) {
    private val startCal = Calendar.getInstance()
    private val endCal = Calendar.getInstance()
    private var startEndMode = 0
    private var timeMode = if(record.isSetTime()) 1 else 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_start_end_picker)
        setGlobalTheme(rootLy)
        startCal.timeInMillis = record.dtStart
        endCal.timeInMillis = record.dtEnd
        setLayout()
        setOnShowListener {
            startDialogShowAnimation(contentLy)
        }
    }

    private fun setLayout() {
        startTab.setOnClickListener {
            startEndMode = 0
            setModeLy()
        }

        endTab.setOnClickListener {
            startEndMode = 1
            setModeLy()
        }

        confirmBtn.setOnClickListener {
            onConfirmed.invoke(startCal, endCal, timeMode == 1)
            dismiss()
        }

        cancelBtn.setOnClickListener { dismiss() }

        setTimeBtn.setOnClickListener {
            timeMode = if(timeMode == 0) {
                setTimeNearOClock(startCal)
                setTime1HourInterval(startCal, endCal)
                1
            }else {
                setCalendarTime0(startCal)
                setCalendarTime23(endCal)
                0
            }
            setModeLy()
        }

        setModeLy()
    }

    private fun setModeLy() {
        if(startEndMode == 0) {
            setDateView(startCal)
            setTimeView(startCal)
        }else {
            setDateView(endCal)
            setTimeView(endCal)
        }

        if(timeMode == 0) {
            timeWheel.visibility = View.GONE
            setTimeBtn.text = context.getString(R.string.set_time)
        }else {
            timeWheel.visibility = View.VISIBLE
            setTimeBtn.text = context.getString(R.string.clear_time)

        }
        setDateText()
    }

    private fun setDateView(cal: Calendar) {
        calendarView.date = cal.timeInMillis
        calendarView.firstDayOfWeek = AppStatus.startDayOfWeek
        calendarView.setOnDateChangeListener { view, year, month, date ->
            if(startEndMode == 0) {
                startCal.set(Calendar.YEAR, year)
                startCal.set(Calendar.MONTH, month)
                startCal.set(Calendar.DATE, date)
                if(startCal > endCal) {
                    endCal.timeInMillis = startCal.timeInMillis
                }
            }else {
                endCal.set(Calendar.YEAR, year)
                endCal.set(Calendar.MONTH, month)
                endCal.set(Calendar.DATE, date)
                if(startCal > endCal) {
                    startCal.timeInMillis = endCal.timeInMillis
                }
            }
            setDateText()
        }
    }

    private fun setTimeView(cal: Calendar) {
        timeWheel.setOnTimeChangedListener(null)
        timeWheel.currentHour = cal.get(Calendar.HOUR_OF_DAY)
        timeWheel.currentMinute = cal.get(Calendar.MINUTE)
        timeWheel.setOnTimeChangedListener { timePicker, h, m ->
            if(startEndMode == 0) {
                startCal.set(Calendar.HOUR_OF_DAY, h)
                startCal.set(Calendar.MINUTE, m)
                if(startCal > endCal) {
                    endCal.timeInMillis = startCal.timeInMillis
                }
            }else {
                endCal.set(Calendar.HOUR_OF_DAY, h)
                endCal.set(Calendar.MINUTE, m)
                if(startCal > endCal) {
                    startCal.timeInMillis = endCal.timeInMillis
                }
            }
            setDateText()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDateText() {
        val dtStart = startCal.timeInMillis
        val dtEnd = endCal.timeInMillis
        if(timeMode == 0) {
            startTimeTText.text = "${AppDateFormat.date.format(dtStart)} ${AppDateFormat.dow.format(dtStart)}"
            startTimeYMDText.text = AppDateFormat.ymDate.format(dtStart)
            endTimeTText.text = "${AppDateFormat.date.format(dtEnd)} ${AppDateFormat.dow.format(dtEnd)}"
            endTimeYMDText.text = AppDateFormat.ymDate.format(dtEnd)
        }else {
            startTimeTText.text = AppDateFormat.time.format(dtStart)
            startTimeYMDText.text = AppDateFormat.ymdDate.format(dtStart)
            endTimeTText.text = AppDateFormat.time.format(dtEnd)
            endTimeYMDText.text = AppDateFormat.ymdDate.format(dtEnd)
        }

        if(startEndMode == 0) {
            startText.setBackgroundColor(AppTheme.primaryColor)
            startText.setTextColor(Color.WHITE)
            startTimeTText.setTextColor(AppTheme.primaryColor)
            startTimeYMDText.setTextColor(AppTheme.primaryColor)

            endText.setBackgroundColor(Color.TRANSPARENT)
            endText.setTextColor(AppTheme.secondaryText)
            endTimeTText.setTextColor(AppTheme.disableText)
            endTimeYMDText.setTextColor(AppTheme.disableText)
        }else {
            startText.setBackgroundColor(Color.TRANSPARENT)
            startText.setTextColor(AppTheme.secondaryText)
            startTimeTText.setTextColor(AppTheme.disableText)
            startTimeYMDText.setTextColor(AppTheme.disableText)

            endText.setBackgroundColor(AppTheme.primaryColor)
            endText.setTextColor(Color.WHITE)
            endTimeTText.setTextColor(AppTheme.primaryColor)
            endTimeYMDText.setTextColor(AppTheme.primaryColor)
        }

        if(AppStatus.isLunarDisplay) {
            val lunarCalendar = KoreanLunarCalendar.getInstance()
            if(timeMode == 0) {
                lunarCalendar.setSolarDate(startCal.get(Calendar.YEAR),
                        startCal.get(Calendar.MONTH) + 1, startCal.get(Calendar.DATE))
            }else {
                lunarCalendar.setSolarDate(endCal.get(Calendar.YEAR),
                        endCal.get(Calendar.MONTH) + 1, endCal.get(Calendar.DATE))
            }
            lunarText.text = lunarCalendar.lunarFormat
        }else {
            lunarText.text = ""
        }

        durationText.text = getDurationText(startCal.timeInMillis, endCal.timeInMillis, timeMode == 0)
    }

}
