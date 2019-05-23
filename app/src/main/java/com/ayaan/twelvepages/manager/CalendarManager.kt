package com.ayaan.twelvepages.manager

import com.ayaan.twelvepages.AppTheme
import com.pixplicity.easyprefs.library.Prefs

object CalendarManager {
    var backgroundColor: Int = 0
    var dateColor: Int = 0
    var sundayColor: Int = 0
    var saturdayColor: Int = 0
    var selectedDateColor: Int = 0

    init {
        backgroundColor = AppTheme.backgroundColor
        dateColor = AppTheme.primaryColor
        sundayColor = Prefs.getInt("sundayColor", AppTheme.redColor)
        saturdayColor = Prefs.getInt("saturdayColor", AppTheme.primaryColor)
        selectedDateColor = AppTheme.primaryColor
    }
}