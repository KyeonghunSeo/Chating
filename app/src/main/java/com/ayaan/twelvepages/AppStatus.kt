package com.ayaan.twelvepages

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
    var weekLine = 0f
    var isWeekNumDisplay = true
    var checkedRecordDisplay = 0
    var rememberPhoto = false

    var screenWidth = 0
    var screenHeight = 0

    fun init(context: Context) {
        startDayOfWeek = Prefs.getInt("startDayOfWeek", Calendar.SUNDAY)
        isDowDisplay = Prefs.getBoolean("isDowDisplay", true)
        holidayDisplay = Prefs.getInt("holidayDisplay", 1)
        isLunarDisplay = Prefs.getBoolean("isLunarDisplay", false)
        outsideMonthAlpha = Prefs.getFloat("outsideMonthAlpha", 0f)
        calTextSize = Prefs.getInt("calTextSize", 0)
        weekLine = Prefs.getFloat("weekLine", 0f)
        isWeekNumDisplay = Prefs.getBoolean("isWeekNumDisplay", true)
        checkedRecordDisplay = Prefs.getInt("checkedRecordDisplay", 0)
        rememberPhoto = Prefs.getBoolean("rememberPhoto", false)

        val screenSize = getScreenSize(context)
        screenWidth = screenSize[0]
        screenHeight = screenSize[1]
    }
}