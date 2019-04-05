package com.hellowo.journey.manager

import com.hellowo.journey.AppStatus
import com.hellowo.journey.R
import java.util.*

object HolidayManager {
    private val solarHolidays = HashMap<String, String>()
    private val lunarHolidays = HashMap<String, String>()

    init {
        init()
    }

    fun init() {
        when(AppStatus.holidayDisplay) {
            2 -> setHolidaysKR()
            else -> setHolidaysKR()
        }
    }

    private fun setHolidaysKR() {
        solarHolidays.clear()
        lunarHolidays.clear()
        solarHolidays["0101"] = "새해첫날"
        solarHolidays["0301"] = "3.1절"
        solarHolidays["0505"] = "어린이날"
        solarHolidays["0606"] = "현충일"
        solarHolidays["0815"] = "광복절"
        solarHolidays["1003"] = "개천절"
        solarHolidays["1009"] = "한글날"
        solarHolidays["1225"] = "크리스마스"
        lunarHolidays["1230"] = "설 공휴일"
        lunarHolidays["0101"] = "설날"
        lunarHolidays["0102"] = "설 공휴일"
        lunarHolidays["0408"] = "석가탄신일"
        lunarHolidays["0814"] = "추석 공휴일"
        lunarHolidays["0815"] = "추석"
        lunarHolidays["0816"] = "추석 공휴일"
    }

    fun getHoliday(solarKey: String, lunarKey: String): String? {
        return if(AppStatus.holidayDisplay == 0) null
        else when {
            solarHolidays.containsKey(solarKey) -> solarHolidays[solarKey]
            lunarHolidays.containsKey(lunarKey) -> lunarHolidays[lunarKey]
            else -> null
        }
    }
}