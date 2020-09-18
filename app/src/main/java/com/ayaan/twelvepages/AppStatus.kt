package com.ayaan.twelvepages

import android.content.Context
import com.pixplicity.easyprefs.library.Prefs
import java.util.*

object AppStatus {
    var permissionStorage = false
    var statusBarHeight = 0
    var startDayOfWeek = Calendar.SUNDAY
    var isDowDisplay = true
    var holidayDisplay = 0
    var isLunarDisplay = true
    var outsideMonthAlpha = 0f
    var calTextSize = 0
    var weekLine = 0f
    var dayDivider = 0f
    var isWeekNumDisplay = false
    var checkedRecordDisplay = 0
    var rememberPhoto = 0
    var rememberBeforeMonth = 0
    var rememberBeforeYear = 0
    var isDisplayUpdateTime = false
    var isDisplayRecordViewStyle = false
    var displayRecordDivider = 0
    var isDisplayDayViewWeekNum = false
    var premiumTime = Long.MIN_VALUE
    var calRecordFontWidth = 0
    var calFont = 0
    var recordListFont = 0
    var isDayViewAnimation = true
    var lastPremiumCheckTime = Long.MIN_VALUE

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
        dayDivider = Prefs.getFloat("dayDivider", 0f)
        isWeekNumDisplay = Prefs.getBoolean("isWeekNumDisplay", false)
        checkedRecordDisplay = Prefs.getInt("checkedRecordDisplay", 0)
        rememberPhoto = Prefs.getInt("rememberPhoto", 0)
        rememberBeforeMonth = Prefs.getInt("rememberBeforeMonth", 0)
        rememberBeforeYear = Prefs.getInt("rememberBeforeYear", 0)
        isDisplayUpdateTime = Prefs.getBoolean("isDisplayUpdateTime", false)
        isDisplayRecordViewStyle = Prefs.getBoolean("isDisplayRecordViewStyle", false)
        displayRecordDivider = Prefs.getInt("displayRecordDivider", 0)
        isDisplayDayViewWeekNum = Prefs.getBoolean("isDisplayDayViewWeekNum", true)
        premiumTime = Prefs.getLong("premiumTime", Long.MIN_VALUE)
        calRecordFontWidth = Prefs.getInt("calRecordFontWidth", 0)
        calFont = Prefs.getInt("calFont", 0)
        recordListFont = Prefs.getInt("recordListFont", 0)
        isDayViewAnimation = Prefs.getBoolean("isDayViewAnimation", true)
        lastPremiumCheckTime = Prefs.getLong("lastPremiumCheckTime", Long.MIN_VALUE)

        val screenSize = getScreenSize(context)
        screenWidth = screenSize[0]
        screenHeight = screenSize[1]
        val resourceId: Int = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)
        }
    }

    fun isPremium() = premiumTime > System.currentTimeMillis()
}