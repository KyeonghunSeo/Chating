package com.ayaan.twelvepages.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.*
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.CalendarManager
import com.ayaan.twelvepages.model.KoreanLunarCalendar
import com.ayaan.twelvepages.model.Record
import kotlinx.android.synthetic.main.dialog_base.*
import kotlinx.android.synthetic.main.view_day_of_week.*
import kotlinx.android.synthetic.main.container_scheduling_dlg.*
import java.util.*

@SuppressLint("ValidFragment")
class SchedulingDialog(activity: Activity, record: Record, private val pickerMode: Int,
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
        setLayout(R.layout.container_scheduling_dlg, dpToPx(340))
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
            textView.text = AppDateFormat.dows[(index + AppStatus.startDayOfWeek - 1) % 7]
            textView.setTextColor(if(index == sundayPos) CalendarManager.sundayColor else CalendarManager.dateColor)
        }
    }

    private fun setCalendarPicker() {
        calendarPicker.mode = pickerMode
        calendarPicker.setStartEndCalendar(startCal, endCal)
        calendarPicker.setColor(color)
        calendarPicker.onTargetedDate = { time ->
            monthYearText.text = AppDateFormat.ym.format(Date(time))
        }
        calendarPicker.onSelectedDate = {
            setDateText()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDateText() {
        titleText.text = makeSheduleText(startCal.timeInMillis, endCal.timeInMillis,
                false, true, false, true)
        if(isSameDay(startCal, endCal)) {
            if(AppStatus.isLunarDisplay) {
                val lunarCalendar = KoreanLunarCalendar.getInstance()
                lunarCalendar.setSolarDate(startCal.get(Calendar.YEAR),
                        startCal.get(Calendar.MONTH) + 1, startCal.get(Calendar.DATE))
                lunarText.text = lunarCalendar.lunarFormat
            }
        }else {
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
