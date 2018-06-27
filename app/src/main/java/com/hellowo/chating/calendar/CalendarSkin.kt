package com.hellowo.chating.calendar

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import com.hellowo.chating.R

object CalendarSkin {
    var dateColor: Int = 0
    var holiDateColor: Int = 0
    var selectedDateColor: Int = 0

    fun init(context: Context) {
        val resource = context.resources
        dateColor = resource.getColor(R.color.primaryText)
        holiDateColor = resource.getColor(R.color.red)
        selectedDateColor = resource.getColor(R.color.blue)
    }

    fun setDefaultDateTextSkin(textView: TextView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, CalendarView.dateTextSize)
        //textView.setTypeface(null, Typeface.BOLD)
        textView.gravity = Gravity.CENTER
        textView.layoutParams = CalendarView.dateTextLayoutParams
    }
}