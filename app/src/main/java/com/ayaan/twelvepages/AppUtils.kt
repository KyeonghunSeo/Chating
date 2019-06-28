package com.ayaan.twelvepages

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Vibrator
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.ChangeBounds
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.Transition
import com.ayaan.twelvepages.model.Photo
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.view.CalendarView
import com.ayaan.twelvepages.ui.view.base.Line
import es.dmoral.toasty.Toasty
import java.io.ByteArrayOutputStream
import java.util.*

val tempCal = Calendar.getInstance()
private val tempCal2 = Calendar.getInstance()

fun l(s: String){
    Log.e("aaa", s)
}

fun dpToPx(dps: Int): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps.toFloat(), Resources.getSystem().displayMetrics).toInt()
}

fun dpToPx(dps: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, Resources.getSystem().displayMetrics)
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isToday(cal: Calendar): Boolean {
    tempCal.timeInMillis = System.currentTimeMillis()
    return isSameDay(cal, tempCal)
}

fun getNewCalendar() = Calendar.getInstance()

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

fun getCalendarTime0 (time: Long) : Long {
    tempCal.timeInMillis = time
    tempCal.set(Calendar.HOUR_OF_DAY, 0)
    tempCal.set(Calendar.MINUTE, 0)
    tempCal.set(Calendar.SECOND, 0)
    tempCal.set(Calendar.MILLISECOND, 0)
    return tempCal.timeInMillis
}

fun getCalendarTime23 (time: Long) : Long  {
    tempCal.timeInMillis = time
    tempCal.set(Calendar.HOUR_OF_DAY, 23)
    tempCal.set(Calendar.MINUTE, 59)
    tempCal.set(Calendar.SECOND, 59)
    tempCal.set(Calendar.MILLISECOND, 999)
    return tempCal.timeInMillis
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

fun getDiffDate(t1: Long, t2: Long): Int {
    tempCal.timeInMillis = t1
    tempCal2.timeInMillis = t2
    return getDiffDate(tempCal, tempCal2)
}

fun getDiffDate(c1: Calendar, c2: Calendar): Int {
    return if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)) {
        c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR)
    } else {
        (c2.get(Calendar.YEAR) - c1.get(Calendar.YEAR)) * 365 + (c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR))
    }
}

fun getDiffMonth(t1: Long, t2: Long): Int {
    tempCal.timeInMillis = t1
    tempCal2.timeInMillis = t2
    return tempCal2.get(Calendar.MONTH) - tempCal.get(Calendar.MONTH)
}

fun getDiffYear(t1: Long, t2: Long): Int {
    tempCal.timeInMillis = t1
    tempCal2.timeInMillis = t2
    return tempCal2.get(Calendar.YEAR) - tempCal.get(Calendar.YEAR)
}

fun getTodayStartCal() : Long {
    tempCal.timeInMillis = System.currentTimeMillis()
    getCalendarTime0(tempCal)
    return tempCal.timeInMillis
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

fun getTimeOnDate(date: Long, time: Long) : Long {
    tempCal.timeInMillis = date
    tempCal2.timeInMillis = time
    copyHourMinSecMill(tempCal, tempCal2)
    return tempCal.timeInMillis
}

fun getDurationText(dtStart: Long, dtEnd: Long, allday: Boolean) : String {
    val diff = dtEnd - dtStart
    if (allday) {
        val diffDate = diff / DAY_MILL
        return when (diffDate) {
            0L -> App.context.getString(R.string.one_day)
            else -> String.format(App.context.getString(R.string.duration_day), diffDate + 1)
        }
    }else {
        val diffMin = diff / MIN_MILL
        return when  {
            diffMin == 0L -> "--"
            diffMin < 60 -> String.format(App.context.getString(R.string.duration_min), diffMin)
            else -> {
                val diffHour = diff / HOUR_MILL
                val diffMinHour = diffMin % 60
                when (diffMinHour) {
                    0L -> String.format(App.context.getString(R.string.duration_hour), diffHour)
                    else -> String.format(App.context.getString(R.string.duration_min_hour), diffHour, diffMinHour)
                }
            }
        }
    }
}

fun makeFromBottomSlideTransition() : Transition {
    val transition = Slide()
    transition.slideEdge = Gravity.BOTTOM
    transition.duration = ANIM_DUR
    transition.interpolator = FastOutSlowInInterpolator()
    return transition
}

@SuppressLint("RtlHardcoded")
fun makeFromLeftSlideTransition() : Transition {
    val transition = Slide()
    transition.slideEdge = Gravity.LEFT
    transition.duration = ANIM_DUR
    transition.interpolator = FastOutSlowInInterpolator()
    return transition
}

@SuppressLint("RtlHardcoded")
fun makeFromRightSlideTransition() : Transition {
    val transition = Slide()
    transition.slideEdge = Gravity.RIGHT
    transition.duration = ANIM_DUR
    transition.interpolator = FastOutSlowInInterpolator()
    return transition
}

fun makeFromTopSlideTransition() : Transition {
    val transition = Slide()
    transition.slideEdge = Gravity.TOP
    transition.duration = ANIM_DUR
    transition.interpolator = FastOutSlowInInterpolator()
    return transition
}

fun makeFadeTransition() : Transition {
    val transition = Fade()
    transition.duration = ANIM_DUR
    transition.interpolator = FastOutSlowInInterpolator()
    return transition
}

fun makeChangeBounceTransition() : Transition {
    val transition = ChangeBounds()
    transition.duration = ANIM_DUR
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
        animSet.playTogether(ObjectAnimator.ofFloat(view, "translationX", -view.width.toFloat(), 0f))
    }else {
        animSet.playTogether(ObjectAnimator.ofFloat(view, "translationX", view.width.toFloat(), 0f))
    }
    animSet.playTogether(ObjectAnimator.ofFloat(view, "alpha", 1f, 1f))
    listener?.let { animSet.addListener(it) }
    animSet.duration = 250
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
            ObjectAnimator.ofFloat(view, "translationY", dpToPx(15f), 0f),
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

fun hideKeyPad(input: EditText) {
    input.post{ (input.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(input.windowToken, 0) }
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

fun callAfterViewDrawed(view: View, callback: Runnable) {
    view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            callback.run()
        }
    })
}

fun showDialog(dialog: Dialog, cancelable: Boolean, dim: Boolean, blankBackground: Boolean, touchOutside: Boolean) {
    try {
        dialog.setCancelable(cancelable) // 백키로 종료하기
        dialog.setOnKeyListener { dialogInterface, i, keyEvent ->
            !cancelable && keyEvent.action == KeyEvent.KEYCODE_BACK
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 타이틀 숨기기
        if (!dim) { // 배경 어둡게 하지 않기
            dialog.window!!.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
        if (blankBackground) { // 배경 투명하게 하기
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        if (touchOutside) { // 외부 터치 가능하게 하기
            dialog.window!!.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        }
        dialog.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }

}

fun setTextBoldUnderBar(originText: String, textView: TextView, color: Int, texts: Array<String>, clickableSpanTerms: Array<ClickableSpan>?) {
    val ss = SpannableString(originText)
    texts.forEach {
        //ss.setSpan(clickableSpanTerms, text.length - 7, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val index = originText.indexOf(it)
        ss.setSpan(StyleSpan(Typeface.BOLD), index, it.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(ForegroundColorSpan(color), index, it.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    textView.text = ss
}

fun setGlobalTheme(view: View?) {
    if (view != null) {
        if (view is ViewGroup) {
            val vg = view as ViewGroup?
            val vgCnt = vg!!.childCount
            for (i in 0 until vgCnt) {
                val v = vg.getChildAt(i)
                when (v) {
                    is TextView -> {
                        when(v.typeface) {
                            AppTheme.boldFont -> v.setTypeface(AppTheme.boldFont, Typeface.BOLD)
                            AppTheme.thinFont -> v.typeface = AppTheme.thinFont
                            else -> v.typeface = AppTheme.regularFont
                        }
                    }
                    is EditText -> {
                        when(v.typeface) {
                            AppTheme.boldFont -> v.setTypeface(AppTheme.boldFont, Typeface.BOLD)
                            AppTheme.thinFont -> v.typeface = AppTheme.thinFont
                            else -> v.typeface = AppTheme.regularFont
                        }
                    }
                    is Button -> {
                        when(v.typeface) {
                            AppTheme.boldFont -> v.setTypeface(AppTheme.boldFont, Typeface.BOLD)
                            AppTheme.thinFont -> v.typeface = AppTheme.thinFont
                            else -> v.typeface = AppTheme.regularFont
                        }
                    }
                    is Line -> {
                        when(v.colorFlag) {
                            1 -> v.setBackgroundColor(AppTheme.secondaryText)
                            2 -> v.setBackgroundColor(AppTheme.disableText)
                            3 -> v.setBackgroundColor(AppTheme.line)
                            4 -> v.setBackgroundColor(AppTheme.lightLine)
                            else -> v.setBackgroundColor(AppTheme.primaryText)
                        }
                    }
                }
                setGlobalTheme(v)
            }
        }
    }
}

fun toast(strId: Int) {
    Toasty.custom(App.context, strId, R.drawable.info, R.color.dim, Toast.LENGTH_SHORT, false, true).show()
}

fun toast(strId: Int, imgId: Int) {
    Toasty.custom(App.context, strId, imgId, R.color.dim, Toast.LENGTH_SHORT, true, true).show()
}

fun getScreenSize(context: Context) : IntArray {
    val size = IntArray(2)
    context.resources.displayMetrics?.let {
        size[0] = it.widthPixels
        size[1] = it.heightPixels
    }
    return size
}

fun getRandomText(arrayId: Int): String {
    App.resource.getStringArray(arrayId).let {
        return it[Random().nextInt(it.size)]
    }
}

fun checkView(view: View) {
    view.setBackgroundColor(AppTheme.primary)
    view.alpha = 1f
    view.findViewWithTag<ImageView>("icon")?.setColorFilter(AppTheme.background)
    view.findViewWithTag<TextView>("text")?.let {
        it.setTextColor(AppTheme.background)
        it.typeface = AppTheme.boldFont
    }
}

fun uncheckView(view: View) {
    view.setBackgroundColor(AppTheme.disableText)
    view.alpha = 0.4f
    view.findViewWithTag<ImageView>("icon")?.setColorFilter(AppTheme.primary)
    view.findViewWithTag<TextView>("text")?.let {
        it.setTextColor(AppTheme.primary)
        it.typeface = AppTheme.regularFont
    }
}

fun str(stringId: Int): String = App.context.getString(stringId)

fun setImageViewGrayFilter(v: ImageView) {
    val matrix = ColorMatrix()
    matrix.setSaturation(0f) //0 means grayscale
    val cf = ColorMatrixColorFilter(matrix)
    v.colorFilter = cf
    v.alpha = 1f
}

fun removeImageViewFilter(v: ImageView) {
    v.colorFilter = null
    v.alpha = 1f
}

fun makeSheduleText(dtStart: Long, dtEnd: Long, showOneday: Boolean) : String {
    tempCal.timeInMillis = dtStart
    tempCal2.timeInMillis = dtEnd
    return if(isSameDay(tempCal, tempCal2)) {
        AppDateFormat.mde.format(tempCal.time) + if(showOneday) "\n${str(R.string.one_day)}" else ""
    }else {
        String.format(str(R.string.from), AppDateFormat.mde.format(tempCal.time)) +
                "\n" + String.format(str(R.string.to), AppDateFormat.mde.format(tempCal2.time)) +
                ", " + getDurationText(tempCal.timeInMillis, tempCal2.timeInMillis, true)
    }
}

fun makeTimeText(dtStart: Long, dtEnd: Long) : String {
    tempCal.timeInMillis = dtStart
    tempCal2.timeInMillis = dtEnd
    return if(dtStart == dtEnd) {
        AppDateFormat.time.format(tempCal.time)
    }else {
        String.format(str(R.string.from), AppDateFormat.time.format(tempCal.time)) +
                "\n" + String.format(str(R.string.to), AppDateFormat.time.format(tempCal2.time))
    }
}

fun makeShareContentsByRecord(record: Record) : String {
    val result = StringBuilder()
    record.title?.let { result.append("$it\n") }
    if(record.dtStart != Long.MIN_VALUE) {
        result.append("[${str(R.string.date_time)}]\n${makeSheduleText(record.dtStart, record.dtEnd, false)}\n")
        if(record.isSetTime) {
            result.append("[${str(R.string.time)}]\n${makeTimeText(record.dtStart, record.dtEnd)}\n")
        }
    }
    if(!record.location.isNullOrEmpty()) {
        result.append("[${str(R.string.location)}]\n${record.location}\n")
    }
    if(!record.description.isNullOrEmpty()) {
        result.append("[${str(R.string.memo)}]\n${record.description}\n")
    }
    return result.toString().trim()
}

fun getPhotosByDate(context: Context, calendar: Calendar): ArrayList<Photo> {
    val selection = "(${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}" +
            " OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO})" +
            " AND (${MediaStore.MediaColumns.DATE_ADDED} >= ? AND ${MediaStore.MediaColumns.DATE_ADDED} < ?)"

    val queryUri = MediaStore.Files.getContentUri("external")
    val listOfAllImages = ArrayList<Photo>()
    val cursor: Cursor?

    val projection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Files.FileColumns.MEDIA_TYPE, MediaStore.Video.VideoColumns.DURATION)

    cursor = context.contentResolver.query(queryUri, projection, selection,
            arrayOf((calendar.timeInMillis / 1000).toString(), ((calendar.timeInMillis + DAY_MILL) / 1000).toString()),
            MediaStore.MediaColumns.DATE_MODIFIED + " desc")

    while (cursor!!.moveToNext()) {
        listOfAllImages.add(Photo(cursor.getInt(1) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO, cursor.getString(0), cursor.getLong(2)))
    }
    return listOfAllImages
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

public Bitmap buildUpdate(String time) // 위젯에서 폰트 적용
{
    Bitmap myBitmap = Bitmap.createBitmap(160, 84, Bitmap.Config.ARGB_4444);
    Canvas myCanvas = new Canvas(myBitmap);
    Paint paint = new Paint();
    Typeface clock = Typeface.createFromAsset(this.getAssets(),"Clockopia.ttf");
    paint.setAntiAlias(true);
    paint.setSubpixelText(true);
    paint.setTypeface(clock);
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.WHITE);
    paint.setTextSize(65);
    paint.setTextAlign(Align.CENTER);
    myCanvas.drawText(time, 80, 60, paint);
    return myBitmap;
}

 */