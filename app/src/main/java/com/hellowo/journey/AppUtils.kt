package com.hellowo.journey

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.Transition
import com.hellowo.journey.ui.view.CalendarView
import java.io.ByteArrayOutputStream
import java.util.*

private val tempCal = Calendar.getInstance()

fun l(s: String){
    Log.d("aaa", s)
}

fun dpToPx(dps: Int): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps.toFloat(), Resources.getSystem().displayMetrics).toInt()
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isToday(cal: Calendar): Boolean {
    tempCal.timeInMillis = System.currentTimeMillis()
    return isSameDay(cal, tempCal)
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

fun getDiffToday(cal: Calendar): Int {
    tempCal.timeInMillis = System.currentTimeMillis()
    return getDiffDate(tempCal, cal)
}

fun getDiffDate(c1: Calendar, c2: Calendar): Int {
    return if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) {
        c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR)
    } else {
        (c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR)) * 365 + (c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR))
    }
}

fun copyYearMonthDate(toCal: Calendar, fromCal: Calendar) {
    toCal.set(
            fromCal.get(Calendar.YEAR),
            fromCal.get(Calendar.MONTH),
            fromCal.get(Calendar.DATE)
    )
    //UPDATE BY CALLING getTimeInMillis() or any of the previously mentioned functions
    toCal.timeInMillis
}

fun copyHourMinSecMill(toCal: Calendar, fromCal: Calendar) {
    toCal.set(Calendar.HOUR_OF_DAY, fromCal.get(Calendar.HOUR_OF_DAY))
    toCal.set(Calendar.MINUTE, fromCal.get(Calendar.MINUTE))
    toCal.set(Calendar.SECOND, fromCal.get(Calendar.SECOND))
    toCal.set(Calendar.MILLISECOND, fromCal.get(Calendar.MILLISECOND))
    //UPDATE BY CALLING getTimeInMillis() or any of the previously mentioned functions
    toCal.timeInMillis
}

fun setTimeNearOClock(cal :Calendar) {
    tempCal.timeInMillis = System.currentTimeMillis()
    copyHourMinSecMill(cal, tempCal)
    if (cal.get(Calendar.HOUR_OF_DAY) < 23) {
        cal.add(Calendar.HOUR_OF_DAY, 1)
    }
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
}

fun setTime1HourInterval(startCal: Calendar, endCal: Calendar) {
    val hour = startCal.get(Calendar.HOUR_OF_DAY)
    copyHourMinSecMill(endCal, startCal)
    if (hour < 23) {
        endCal.add(Calendar.HOUR_OF_DAY, 1)
    } else {
        setCalendarTime23(endCal)
    }
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
    (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?)?.vibrate(15)
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
    animSet.start()
}

fun startFromBottomSlideAppearAnimation(view: View, offset: Float) {
    val animSet = AnimatorSet()
    animSet.playTogether(
            ObjectAnimator.ofFloat(view, "translationY", offset, 0f),
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
    )
    animSet.interpolator = FastOutSlowInInterpolator()
    animSet.duration = CalendarView.animDur
    animSet.start()
}

fun startDialogShowAnimation(view: View) {
    val animSet = AnimatorSet()
    animSet.playTogether(
            ObjectAnimator.ofFloat(view, "translationY", dpToPx(20).toFloat(), 0f),
            ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
    )
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

/**
 * 다이얼로그 보여주기
 * @param dialog 다이어로그
 * @param is_cancelable 백키로 종료하기
 * @param is_dim 배경 어둡게 하지 않기
 * @param is_backgroun_transparent 배경 투명하게 하기
 * @param is_touchable_outside 외부 터치 가능하게 하기
 */
fun showDialog(dialog: Dialog, is_cancelable: Boolean, is_dim: Boolean, is_backgroun_transparent: Boolean, is_touchable_outside: Boolean) {
    try {
        dialog.setCancelable(is_cancelable) // 백키로 종료하기
        dialog.setOnKeyListener { dialogInterface, i, keyEvent ->
            !is_cancelable && keyEvent.action == KeyEvent.KEYCODE_BACK
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 숨기기
        if (!is_dim) { // 배경 어둡게 하지 않기
            dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        if (is_backgroun_transparent) { // 배경 투명하게 하기
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        if (is_touchable_outside) { // 외부 터치 가능하게 하기
            dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        }
        dialog.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

/* 코드
class MyAsyncTask() : AsyncTask<String, String, String?>() {
    override fun doInBackground(vararg args: String): String? {
        return null
    }

    override fun onProgressUpdate(vararg text: String) {
    }

    override fun onPostExecute(result: String?) {
    }
}

 */