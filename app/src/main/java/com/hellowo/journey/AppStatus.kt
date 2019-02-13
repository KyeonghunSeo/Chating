package com.hellowo.journey

import android.content.Context
import com.pixplicity.easyprefs.library.Prefs
import java.util.*

object AppStatus {
    var startDayOfWeek = Calendar.SUNDAY
    var saturdayColor = 0
    var sundayColor = 1

    fun init(context: Context) {
        startDayOfWeek = Prefs.getInt("startDayOfWeek", Calendar.SUNDAY)
    }
}