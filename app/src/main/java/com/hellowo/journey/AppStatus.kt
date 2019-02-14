package com.hellowo.journey

import android.content.Context
import com.pixplicity.easyprefs.library.Prefs
import java.util.*

object AppStatus {
    var startDayOfWeek = Calendar.SUNDAY
    var isLunarDisplay = true

    fun init(context: Context) {
        startDayOfWeek = Prefs.getInt("startDayOfWeek", Calendar.SUNDAY)
        isLunarDisplay = Prefs.getBoolean("isLunarDisplay", true)
    }
}