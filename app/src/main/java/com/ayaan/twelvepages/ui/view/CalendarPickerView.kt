package com.ayaan.twelvepages.ui.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.CalendarManager
import kotlinx.android.synthetic.main.view_calendar_picker.view.*
import java.util.*

class CalendarPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    val maxCellNum = 42
    val dateTextSize = 16f
    val columns = 7
    val calendarLy = LinearLayout(context)
    val weekLys = Array(6) { _ -> FrameLayout(context) }
    val dateLys = Array(maxCellNum) { _ -> FrameLayout(context) }
    val dateTexts = Array(maxCellNum) { _ -> DateTextView(context) }

    private val todayCal: Calendar = Calendar.getInstance()
    private val tempCal: Calendar = Calendar.getInstance()
    private val monthCal: Calendar = Calendar.getInstance()
    val startCal: Calendar = Calendar.getInstance()
    val endCal: Calendar = Calendar.getInstance()
    private var selectedCellNum = -1

    val cellTimeMills = LongArray(maxCellNum) { _ -> Long.MIN_VALUE}
    var calendarStartTime = Long.MAX_VALUE
    var calendarEndTime = Long.MAX_VALUE
    var startCellNum = 0
    var endCellNum = 0
    var minWidth = 0f
    var minHeight = 0f
    var rows = 0

    var startEndMode = 0

    var startTextCellnum = -1
    var endTextCellnum = -1

    var color = Color.BLACK
    var lightColor = Color.BLACK

    var onDrawed: ((Unit) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_calendar_picker, this, true)
        makeCalendar()
    }

    private fun makeCalendar() {
        containter.addView(calendarLy)
        calendarLy.orientation = LinearLayout.VERTICAL
        calendarLy.clipChildren = false

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.clipChildren = false
            for (j in 0..6){
                val cellNum = i*7 + j
                val dateLy = dateLys[cellNum]
                dateLy.clipChildren = false

                val dateText = dateTexts[cellNum]
                setDefaultDateTextSkin(dateText)

                dateLy.addView(dateText)
                weekLy.addView(dateLy)
            }
            calendarLy.addView(weekLy)
        }

        val gestureDetector = GestureDetector(context, SwipeGestureDetector())
        containter.onTouched = { ev ->
            !gestureDetector.onTouchEvent(ev)
        }
    }

    fun drawCalendar(time: Long) {
        todayCal.timeInMillis = System.currentTimeMillis()
        tempCal.timeInMillis = time
        monthCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), todayCal.get(Calendar.DATE))
        setCalendarTime0(tempCal)
        tempCal.set(Calendar.DATE, 1)

        startCellNum = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        endCellNum = startCellNum + tempCal.getActualMaximum(Calendar.DATE) - 1
        rows = (endCellNum + 1) / 7 + if ((endCellNum + 1) % 7 > 0) 1 else 0
        minWidth = width / columns.toFloat()
        minHeight = containter.height.toFloat() / rows
        calendarLy.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, containter.height)
        tempCal.add(Calendar.DATE, -startCellNum)

        startTextCellnum = if(getDiffDate(startCal, tempCal) > 0) {
            -1
        }else {
            maxCellNum
        }
        endTextCellnum = if(getDiffDate(endCal, tempCal) > 0) {
            -1
        }else {
            maxCellNum
        }

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, minHeight.toInt())
            if(i < rows) {
                weekLy.visibility = View.VISIBLE
                for (j in 0..6){
                    val cellNum = i*7 + j
                    cellTimeMills[cellNum] = tempCal.timeInMillis
                    val dateLy = dateLys[cellNum]
                    dateLy.layoutParams = FrameLayout.LayoutParams(minWidth.toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
                    dateLy.translationX = cellNum % columns * minWidth
                    dateLy.setOnClickListener { onDateClick(cellNum) }
                    val dateText = dateTexts[cellNum]
                    dateText.text = tempCal.get(Calendar.DATE).toString()

                    if(startTextCellnum == maxCellNum && isSameDay(tempCal, startCal)) startTextCellnum = cellNum
                    if(endTextCellnum == maxCellNum && isSameDay(tempCal, endCal)) endTextCellnum = cellNum

                    if(cellNum == 0) {
                        calendarStartTime = tempCal.timeInMillis
                    }else if(cellNum == rows * columns - 1) {
                        setCalendarTime23(tempCal)
                        calendarEndTime = tempCal.timeInMillis
                    }
                    tempCal.add(Calendar.DATE, 1)
                }
            }else {
                weekLy.visibility = View.GONE
            }
        }

        for(i in 0..5) {
            if(i < rows) {
                for (j in 0..6){
                    val cellNum = i*7 + j
                    val dateText = dateTexts[cellNum]
                    if(cellNum in startTextCellnum..endTextCellnum) {
                        dateText.setTextColor(Color.WHITE)
                        when {
                            startTextCellnum == endTextCellnum -> dateText.mode = 1
                            cellNum == startTextCellnum -> dateText.mode = 2
                            cellNum == endTextCellnum -> dateText.mode = 3
                            else -> dateText.mode = 4
                        }
                        dateText.alpha = 1f
                    }else {
                        dateText.mode = 0
                        setDateTextColor(cellNum)
                        dateText.alpha = if(cellNum in startCellNum..endCellNum) 1f else 0.3f
                    }
                    dateText.invalidate()
                }
            }
        }

        calendarText.text = AppDateFormat.ymDate.format(monthCal.time)
        onDrawed?.invoke(Unit)
    }

    private fun onDateClick(cellNum: Int) {
        tempCal.timeInMillis = cellTimeMills[cellNum]
        if(startEndMode == 0) {
            copyYearMonthDate(startCal, tempCal)
            if(startCal > endCal) {
                endCal.timeInMillis = startCal.timeInMillis
            }
        }else {
            copyYearMonthDate(endCal, tempCal)
            if(startCal > endCal) {
                startCal.timeInMillis = endCal.timeInMillis
            }
        }
        drawCalendar(monthCal.timeInMillis)
    }

    private fun setDateTextColor(cellNum: Int) {
        val dateText = dateTexts[cellNum]
        if(cellNum == selectedCellNum) {
            dateText.setTextColor(CalendarManager.dateColor)
        }else {
            if(cellNum % columns == 0) {
                dateText.setTextColor(CalendarManager.sundayColor)
            }else {
                dateText.setTextColor(CalendarManager.dateColor)
            }
        }
    }

    private fun setDefaultDateTextSkin(textView: TextView) {
        val dateTextLayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, dateTextSize)
        textView.typeface = AppTheme.regularFont
        textView.gravity = Gravity.CENTER
        textView.layoutParams = dateTextLayoutParams
    }

    inner class SwipeGestureDetector : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100
        private val offset = dpToPx(50).toFloat()
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            var result = false
            try {
                val diffY = e2!!.y - e1!!.y
                val diffX = e2.x - e1.x
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        val animSet = AnimatorSet()
                        if (diffX > 0) {
                            monthCal.add(Calendar.MONTH, -1)
                            animSet.playTogether(ObjectAnimator.ofFloat(calendarLy, "translationX", -offset, 0f),
                                    ObjectAnimator.ofFloat(calendarLy, "alpha", 0.5f, 1f))
                        } else {
                            monthCal.add(Calendar.MONTH, 1)
                            animSet.playTogether(ObjectAnimator.ofFloat(calendarLy, "translationX", offset, 0f),
                                    ObjectAnimator.ofFloat(calendarLy, "alpha", 0.5f, 1f))
                        }
                        drawCalendar(monthCal.timeInMillis)
                        animSet.interpolator = FastOutSlowInInterpolator()
                        animSet.duration = 250
                        animSet.start()
                        result = false
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }
    }

    inner class DateTextView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
        : TextView(context, attrs, defStyleAttr) {
        val size = dpToPx(36)
        var mode = 0

        override fun onDraw(canvas: Canvas?) {
            val paint = paint
            paint.style = Paint.Style.FILL
            paint.color = color
            paint.isAntiAlias = true
            when(mode){
                1 -> {
                    canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (size / 2).toFloat(), paint)
                }
                2 -> {
                    paint.color = lightColor
                    canvas?.drawRect((width / 2).toFloat(), (height / 2).toFloat() - (size / 2).toFloat(),
                            width.toFloat() + 1, (height / 2).toFloat() + (size / 2).toFloat(), paint)
                    paint.color = color
                    canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (size / 2).toFloat(), paint)
                }
                3 -> {
                    paint.color = lightColor
                    canvas?.drawRect(0f, (height / 2).toFloat() - (size / 2).toFloat(),
                            (width / 2).toFloat(), (height / 2).toFloat() + (size / 2).toFloat(), paint)
                    paint.color = color
                    canvas?.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), (size / 2).toFloat(), paint)
                }
                4 -> {
                    paint.color = lightColor
                    canvas?.drawRect(0f, (height / 2).toFloat() - (size / 2).toFloat(),
                            width.toFloat() + 1, (height / 2).toFloat() + (size / 2).toFloat(), paint)
                }
            }
            super.onDraw(canvas)
        }
    }
}