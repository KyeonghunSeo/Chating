package com.hellowo.journey.manager

import com.hellowo.journey.R
import java.util.*

object HolidayManager {
    private val solarHolidays = HashMap<String, String>()
    private val lunarHolidays = HashMap<String, String>()

    init {
        setHolidaysKR()
    }

    private fun setHolidaysKR() {
        solarHolidays.clear()
        lunarHolidays.clear()
    }

    private fun getHoliday(cal: Calendar): String? {

        return null
    }
}