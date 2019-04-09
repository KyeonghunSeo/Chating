package com.hellowo.journey.manager

import com.hellowo.journey.AppStatus
import com.hellowo.journey.R
import java.util.*

object HolidayManager {
    private val solarHolidays = HashMap<String, Holiday>()
    private val lunarHolidays = HashMap<String, Holiday>()

    init {
        init()
    }

    class Holiday(val title: String, val isHoli: Boolean)

    fun init() {
        when(AppStatus.holidayDisplay) {
            2 -> setHolidaysKR()
            else -> setHolidaysKR()
        }
    }

    private fun setHolidaysKR() {
        solarHolidays.clear()
        lunarHolidays.clear()
        solarHolidays["0101"] = Holiday("새해첫날", true)
        solarHolidays["0301"] = Holiday("3.1절", true)
        solarHolidays["0505"] = Holiday("어린이날", true)
        solarHolidays["0606"] = Holiday("현충일", true)
        solarHolidays["0815"] = Holiday("광복절", true)
        solarHolidays["1003"] = Holiday("개천절", true)
        solarHolidays["1009"] = Holiday("한글날", true)
        solarHolidays["1225"] = Holiday("크리스마스", true)

        solarHolidays["0106"] = Holiday("소한", false)
        solarHolidays["0120"] = Holiday("대한", false)
        solarHolidays["0214"] = Holiday("발런타인데이", false)
        solarHolidays["0219"] = Holiday("우수", false)
        solarHolidays["0314"] = Holiday("화이트데이", false)
        solarHolidays["0321"] = Holiday("춘분", false)
        solarHolidays["0403"] = Holiday("4.3희생자 추념일", false)
        solarHolidays["0405"] = Holiday("식목일", false)
        solarHolidays["0406"] = Holiday("한식", false)
        solarHolidays["0420"] = Holiday("곡우", false)
        solarHolidays["0501"] = Holiday("근로자의날", false)
        solarHolidays["0508"] = Holiday("어버이날", false)
        solarHolidays["0510"] = Holiday("유권자의날", false)
        solarHolidays["0501"] = Holiday("근로자의날", false)
        solarHolidays["0508"] = Holiday("어버이날", false)
        solarHolidays["0510"] = Holiday("유권자의날", false)
        solarHolidays["0515"] = Holiday("스승의날", false)
        solarHolidays["0521"] = Holiday("소만", false)
        solarHolidays["0607"] = Holiday("단오", false)
        solarHolidays["0622"] = Holiday("하지", false)
        solarHolidays["0625"] = Holiday("6.25전쟁 추념일", false)
        solarHolidays["0707"] = Holiday("소서", false)
        solarHolidays["0717"] = Holiday("제헌절", false)
        solarHolidays["0723"] = Holiday("대서", false)
        solarHolidays["0808"] = Holiday("입추", false)
        solarHolidays["0823"] = Holiday("처서", false)
        solarHolidays["0908"] = Holiday("백로", false)
        solarHolidays["0923"] = Holiday("추분", false)
        solarHolidays["1008"] = Holiday("한로", false)
        solarHolidays["1024"] = Holiday("상강", false)
        solarHolidays["1108"] = Holiday("입동", false)
        solarHolidays["1122"] = Holiday("소설", false)
        solarHolidays["1207"] = Holiday("대설", false)
        solarHolidays["1222"] = Holiday("동지", false)

        lunarHolidays["1230"] = Holiday("설 연휴", true)
        lunarHolidays["0101"] = Holiday("설날", true)
        lunarHolidays["0102"] = Holiday("설 연휴", true)
        lunarHolidays["0408"] = Holiday("부처님오신날", true)
        lunarHolidays["0814"] = Holiday("추석 연휴", true)
        lunarHolidays["0815"] = Holiday("추석", true)
        lunarHolidays["0816"] = Holiday("추석 연휴", true)
    }

    fun getHoliday(solarKey: String, lunarKey: String): Holiday? {
        return if(AppStatus.holidayDisplay == 0) null
        else when {
            solarHolidays.containsKey(solarKey) -> solarHolidays[solarKey]
            lunarHolidays.containsKey(lunarKey) -> lunarHolidays[lunarKey]
            else -> null
        }
    }
}