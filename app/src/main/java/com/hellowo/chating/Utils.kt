package com.hellowo.chating

import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import java.util.*

fun l(s: String){
    Log.d("aaa", s)
}

fun dpToPx(dps: Int): Int {
    return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dps.toFloat(), Resources.getSystem().displayMetrics).toInt()
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}