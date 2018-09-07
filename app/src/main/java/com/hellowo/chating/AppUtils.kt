package com.hellowo.chating

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.Transition
import com.hellowo.chating.calendar.view.CalendarView
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

private val tempCal = Calendar.getInstance()

@SuppressLint("SimpleDateFormat")
val yearDf = SimpleDateFormat("yyyy")
val monthDf = SimpleDateFormat("MMMM")
val fullDowDf = SimpleDateFormat("EEEE")
val simpleYMDf = SimpleDateFormat("M. YYYY")

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
    (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?)?.vibrate(30)
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

fun bitmapToByteArray(bitmap: Bitmap) : ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
}

fun byteArrayToBitmap(bytes: ByteArray) : Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

fun showKeyPad(input: EditText) {
    input.requestFocus()
    input.post { (input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(input, 0) }
}

fun hideKeyPad(windowToken: IBinder, input: EditText) {
    input.post{ (input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0) }
}

fun loadBitmapFromView(v: View): Bitmap {
    val specWidth = View.MeasureSpec.makeMeasureSpec(900 /* any */, View.MeasureSpec.EXACTLY)
    v.measure(specWidth, specWidth)
    val questionWidth = v.measuredWidth

    val b = Bitmap.createBitmap(questionWidth, questionWidth, Bitmap.Config.ARGB_8888)
    val c = Canvas(b)
    c.drawColor(Color.WHITE)
    v.layout(v.left, v.top, v.right, v.bottom)
    v.draw(c)
    return b
}

fun ClosedRange<Int>.random() =
        Random().nextInt((endInclusive + 1) - start) +  start

fun statusBarWhite(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val window = activity.window
        var flags = window.peekDecorView().systemUiVisibility
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.peekDecorView().systemUiVisibility = flags
        window.statusBarColor = ContextCompat.getColor(activity, R.color.white)
    }
}

fun statusBarBlackAlpah(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val window = activity.window
        var flags = window.peekDecorView().systemUiVisibility
        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        window.peekDecorView().systemUiVisibility = flags
        window.statusBarColor = ContextCompat.getColor(activity, R.color.transitionDim)
    }
}

fun callAfterViewDrawed(view: View, callback: Runnable) {
    view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            callback.run()
        }
    })
}