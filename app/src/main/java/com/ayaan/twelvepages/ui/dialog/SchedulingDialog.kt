package com.ayaan.twelvepages.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.*
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.CalendarManager
import com.ayaan.twelvepages.model.KoreanLunarCalendar
import com.ayaan.twelvepages.model.Record
import kotlinx.android.synthetic.main.activity_record.*
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.view_day_of_week.*
import kotlinx.android.synthetic.main.containter_scheduling_dlg.*
import java.util.*

@SuppressLint("ValidFragment")
class SchedulingDialog(activity: Activity, record: Record,
                       private val onConfirmed: (Calendar, Calendar) -> Unit) : BaseDialog(activity) {
    private val startCal = Calendar.getInstance()
    private val endCal = Calendar.getInstance()
    private val color: Int

    init {
        startCal.timeInMillis = record.dtStart
        endCal.timeInMillis = record.dtEnd
        color = record.getColor()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.containter_scheduling_dlg, dpToPx(350))
        titleIcon.visibility = View.GONE
        setDowTexts()
        setCalendarPicker()
        confirmBtn.setOnClickListener {
            onConfirmed.invoke(startCal, endCal)
            dismiss()
        }
        leftBtn.setOnClickListener {
            calendarPicker.moveMonth(-1)
        }
        rightBtn.setOnClickListener {
            calendarPicker.moveMonth(1)
        }
        cancelBtn.setOnClickListener { dismiss() }
        setOnShowListener {
            calendarPicker.selectDate(startCal.timeInMillis)
            setDateText()
        }
    }

    private fun setDowTexts() {
        val sundayPos = if(AppStatus.startDayOfWeek == Calendar.SUNDAY) {
            0
        } else {
            8 - AppStatus.startDayOfWeek
        }
        val dowTexts = arrayOf(dowText0, dowText1, dowText2, dowText3, dowText4, dowText5, dowText6)
        dowTexts.forEachIndexed { index, textView ->
            textView.text = AppDateFormat.dowString[(index + AppStatus.startDayOfWeek - 1) % 7]
            textView.setTextColor(if(index == sundayPos) CalendarManager.sundayColor else CalendarManager.dateColor)
        }
    }

    private fun setCalendarPicker() {
        calendarPicker.setStartEndCalendar(startCal, endCal)
        calendarPicker.setColor(color)
        calendarPicker.onTargetedDate = { time ->
            monthYearText.text = AppDateFormat.ymDate.format(Date(time))
        }
        calendarPicker.onSelectedDate = {
            setDateText()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDateText() {
        if(isSameDay(startCal, endCal)) {
            titleText.text = AppDateFormat.mdeDate.format(startCal.time) +
                    "\n" + str(R.string.one_day)
            if(AppStatus.isLunarDisplay) {
                val lunarCalendar = KoreanLunarCalendar.getInstance()
                lunarCalendar.setSolarDate(startCal.get(Calendar.YEAR),
                        startCal.get(Calendar.MONTH) + 1, startCal.get(Calendar.DATE))
                lunarText.text = lunarCalendar.lunarFormat
            }
        }else {
            titleText.text = String.format(str(R.string.from), AppDateFormat.mdeDate.format(startCal.time)) +
                    "\n" + String.format(str(R.string.to), AppDateFormat.mdeDate.format(endCal.time)) +
                    ", " + getDurationText(startCal.timeInMillis, endCal.timeInMillis, true)
            if(AppStatus.isLunarDisplay) {
                val lunarCalendar = KoreanLunarCalendar.getInstance()
                lunarCalendar.setSolarDate(startCal.get(Calendar.YEAR),
                        startCal.get(Calendar.MONTH) + 1, startCal.get(Calendar.DATE))
                val sLunar = lunarCalendar.lunarFormat
                lunarCalendar.setSolarDate(endCal.get(Calendar.YEAR),
                        endCal.get(Calendar.MONTH) + 1, endCal.get(Calendar.DATE))
                val eLunar = lunarCalendar.lunarFormat

                lunarText.text = "$sLunar - $eLunar"
            }
        }

        if(AppStatus.isLunarDisplay){
            lunarText.visibility = View.VISIBLE
        }else {
            lunarText.visibility = View.GONE
        }
    }

}
