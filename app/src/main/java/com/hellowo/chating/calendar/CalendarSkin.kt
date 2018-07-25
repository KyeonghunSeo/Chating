package com.hellowo.chating.calendar

import com.hellowo.chating.R

object CalendarSkin {
    var backgroundColor: Int = 0
    var dateColor: Int = 0
    var holiDateColor: Int = 0
    var selectedDateColor: Int = 0
    var greyColor: Int = 0

    fun init(calendarView: CalendarView) {
        val resource = calendarView.context.resources
        backgroundColor = resource.getColor(R.color.white)
        dateColor = resource.getColor(R.color.secondaryText)
        holiDateColor = resource.getColor(R.color.red)
        selectedDateColor = resource.getColor(R.color.primaryText)
        greyColor = resource.getColor(R.color.grey)
    }

}