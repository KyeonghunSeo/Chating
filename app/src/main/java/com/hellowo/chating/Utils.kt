package com.hellowo.chating

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.Transition
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

fun setCalendarTime0 (cal: Calendar) {
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
}

fun setCalendarTime23 (cal: Calendar) {
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
}

fun makeFromBottomSlideTransition() : Transition {
    val transition = Slide()
    transition.slideEdge = Gravity.BOTTOM
    transition.duration = 250
    transition.interpolator = FastOutSlowInInterpolator()
    return transition
}

fun makeFadeInTransition() : Transition {
    val transition = Fade()
    transition.mode = Fade.MODE_IN
    transition.duration = 250
    transition.interpolator = FastOutSlowInInterpolator()
    return transition
}

fun makeChangeBounceTransition() : Transition {
    val transition = androidx.transition.ChangeBounds()
    transition.duration = 250
    transition.interpolator = FastOutSlowInInterpolator()
    return transition
}

fun makeViewToBitmap(view: View) : Bitmap {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    view.draw(canvas)
    return bitmap
}
