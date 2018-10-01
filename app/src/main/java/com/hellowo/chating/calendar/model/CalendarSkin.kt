package com.hellowo.chating.calendar.model

import android.graphics.Typeface
import android.os.Build
import com.hellowo.chating.AppRes
import com.hellowo.chating.R
import com.hellowo.chating.calendar.view.CalendarView

object CalendarSkin {
    var backgroundColor: Int = 0
    var dateColor: Int = 0
    var holiDateColor: Int = 0
    var todayDateColor: Int = 0
    var selectedDateColor: Int = 0
    var selectedBackgroundColor: Int = 0
    var greyColor: Int = 0
    var dateFont = AppRes.digitFont
    var noteFont = AppRes.boldFont
    var selectFont = AppRes.digitBoldFont

    fun init(calendarView: CalendarView) {
        val resource = calendarView.context.resources
        backgroundColor = resource.getColor(R.color.calendarBackground)
        dateColor = resource.getColor(R.color.primaryText)
        holiDateColor = resource.getColor(R.color.red)
        todayDateColor = resource.getColor(R.color.iconTint)
        selectedDateColor = resource.getColor(R.color.primaryText)
        selectedBackgroundColor = resource.getColor(R.color.grey)
        greyColor = resource.getColor(R.color.grey)
    }
}