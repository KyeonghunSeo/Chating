package com.hellowo.chating

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Vibrator
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.Transition
import com.hellowo.chating.calendar.view.CalendarView
import java.text.SimpleDateFormat
import java.util.*

private val tempCal = Calendar.getInstance()

@SuppressLint("SimpleDateFormat")
val yearDf = SimpleDateFormat("yyyy")
val monthDf = SimpleDateFormat("MMMM", Locale.US)

fun l(s: String){
    Log.d("aaa", s)
}

fun dpToPx(dps: Int): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps.toFloat(), Resources.getSystem().displayMetrics).toInt()
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

fun getCalendarTime0 (cal: Calendar) : Long {
    tempCal.timeInMillis = cal.timeInMillis
    tempCal.set(Calendar.HOUR_OF_DAY, 0)
    tempCal.set(Calendar.MINUTE, 0)
    tempCal.set(Calendar.SECOND, 0)
    tempCal.set(Calendar.MILLISECOND, 0)
    return tempCal.timeInMillis
}

fun getCalendarTime23 (cal: Calendar) : Long  {
    tempCal.timeInMillis = cal.timeInMillis
    tempCal.set(Calendar.HOUR_OF_DAY, 23)
    tempCal.set(Calendar.MINUTE, 59)
    tempCal.set(Calendar.SECOND, 59)
    tempCal.set(Calendar.MILLISECOND, 999)
    return tempCal.timeInMillis
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

fun vibrate(context: Context) {
    (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?)?.vibrate(10)
}

fun startPagingEffectAnimation(direction: Int, view: View, listener: Animator.AnimatorListener?) {
    val animSet = AnimatorSet()
    if(direction < 0) {
        animSet.playTogether(ObjectAnimator.ofFloat(view, "translationX", -dpToPx(50).toFloat(), 0f))
    }else {
        animSet.playTogether(ObjectAnimator.ofFloat(view, "translationX", dpToPx(50).toFloat(), 0f))
    }
    animSet.playTogether(ObjectAnimator.ofFloat(view, "alpha", 0f, 1f))
    listener?.let { animSet.addListener(it) }
    animSet.interpolator = FastOutSlowInInterpolator()
    animSet.duration = CalendarView.animDur
    animSet.start()
}