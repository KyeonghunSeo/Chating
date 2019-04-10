package com.hellowo.journey

import android.content.Context
import com.pixplicity.easyprefs.library.Prefs
import java.util.*

object AppStatus {
    var statusBarHeight = 0

    var startDayOfWeek = Calendar.SUNDAY
    var isDowDisplay = true
    var holidayDisplay = 0
    var isLunarDisplay = true
    var outsideMonthAlpha = 0f
    var calTextSize = 0
    var weekLine = 0

    fun init(context: Context) {
        startDayOfWeek = Prefs.getInt("startDayOfWeek", Calendar.SUNDAY)
        isDowDisplay = Prefs.getBoolean("isDowDisplay", true)
        holidayDisplay = Prefs.getInt("holidayDisplay", 0)
        isLunarDisplay = Prefs.getBoolean("isLunarDisplay", true)
        outsideMonthAlpha = Prefs.getFloat("outsideMonthAlpha", 0f)
        calTextSize = Prefs.getInt("calTextSize", 0)
        weekLine = Prefs.getInt("weekLine", 0)
    }
}