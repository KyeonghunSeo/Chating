package com.ayaan.twelvepages.manager

import android.graphics.Typeface
import com.ayaan.twelvepages.AppTheme
import com.pixplicity.easyprefs.library.Prefs

object CalendarManager {
    var backgroundColor: Int = 0
    var dateColor: Int = 0
    var sundayColor: Int = 0
    var saturdayColor: Int = 0
    var selectedDateColor: Int = 0
    var dateFont: Typeface

    init {
        backgroundColor = AppTheme.background
        dateColor = AppTheme.secondaryText
        sundayColor = Prefs.getInt("sundayColor", AppTheme.red)
        saturdayColor = Prefs.getInt("saturdayColor", dateColor)
        selectedDateColor = AppTheme.secondaryText
        dateFont = AppTheme.regularFont
    }
}