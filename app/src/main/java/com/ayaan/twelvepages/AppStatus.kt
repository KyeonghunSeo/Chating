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
    var rememberPhoto = 0
    var rememberBeforeYear = 0
    var isDisplayUpdateTime = true
    var displayRecordDivider = 1
    var isDisplayDayViewWeekNum = false
    var isPremium = false
    var calRecordFontWidth = 0

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
        rememberPhoto = Prefs.getInt("rememberPhoto", 0)
        rememberBeforeYear = Prefs.getInt("rememberBeforeYear", 0)
        isDisplayUpdateTime = Prefs.getBoolean("isDisplayUpdateTime", true)
        displayRecordDivider = Prefs.getInt("displayRecordDivider", 1)
        isDisplayDayViewWeekNum = Prefs.getBoolean("isDisplayDayViewWeekNum", true)
        isPremium = Prefs.getBoolean("isPremium", false)
        calRecordFontWidth = Prefs.getInt("calRecordFontWidth", 0)

        val screenSize = getScreenSize(context)
        screenWidth = screenSize[0]
        screenHeight = screenSize[1]
    }
}