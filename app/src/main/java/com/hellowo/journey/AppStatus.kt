package com.hellowo.journey

import android.content.Context
import com.pixplicity.easyprefs.library.Prefs
import java.util.*

object AppStatus {
    var startDayOfWeek = Calendar.SUNDAY
    var holidayDisplay = 0
    var isLunarDisplay = true
    var calTextSize = 0

    fun init(context: Context) {
        startDayOfWeek = Prefs.getInt("startDayOfWeek", Calendar.SUNDAY)
        isLunarDisplay = Prefs.getBoolean("isLunarDisplay", true)
        holidayDisplay = Prefs.getInt("holidayDisplay", 0)
        calTextSize = Prefs.getInt("calTextSize", 0)
    }
}