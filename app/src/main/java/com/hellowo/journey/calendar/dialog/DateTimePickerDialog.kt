package com.hellowo.journey.calendar.dialog

import android.animation.AnimatorSet
import android.animation.LayoutTransition.CHANGING
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hellowo.journey.*
import com.hellowo.journey.calendar.model.CalendarSkin
import com.hellowo.journey.calendar.model.TimeObject
import kotlinx.android.synthetic.main.dialog_date_time_picker.*
import java.util.*

@SuppressLint("ValidFragment")
class DateTimePickerDialog(private val activity: Activity, private val timeObject: TimeObject,
                           private val onConfirmed: (Calendar, Calendar, Boolean) -> Unit) : BottomSheetDialogFragment() {

    val maxCellNum = 42
    val dateTextSize = 16f
    val columns = 7
    val calendarLy = LinearLayout(activity)
    val weekLys = Array(6) { _ -> FrameLayout(activity) }
    val dateLys = Array(maxCellNum) { _ -> FrameLayout(activity) }
    val dateTexts = Array(maxCellNum) { _ -> DateTextView(activity) }

    private val todayCal: Calendar = Calendar.getInstance()
    private val tempCal: Calendar = Calendar.getInstance()
    private val monthCal: Calendar = Calendar.getInstance()
    private val startCal: Calendar = Calendar.getInstance()
    private val endCal: Calendar = Calendar.getInstance()
    private var selectedCellNum = -1

    val cellTimeMills = LongArray(maxCellNum) { _ -> Long.MIN_VALUE}
    var calendarStartTime = Long.MAX_VALUE
    var calendarEndTime = Long.MAX_VALUE
    var startCellNum = 0
    var endCellNum = 0
    val calendarHeight = dpToPx(250)
    var minWidth = 0f
    var minHeight = 0f
    var rows = 0

    var startEndMode = 0
    var timeMode = if(timeObject.allday) 0 else 1

    init {
        startCal.timeInMillis = timeObject.dtStart
        endCal.timeInMillis = timeObject.dtEnd
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = View.inflate(context, R.layout.dialog_date_time_picker, null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog.setOnShowListener {
            (rootLy.parent as View).setBackgroundColor(Color.TRANSPARENT)
            val behavior = ((rootLy.parent as View).layoutParams as CoordinatorLayout.LayoutParams).behavior as BottomSheetBehavior<*>?
            behavior?.let {
                it.state = BottomSheetBehavior.STATE_EXPANDED
            }
            setLayout()
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        onConfirmed.invoke(startCal, endCal, !timeSwitch.isChecked)
    }

    private fun setLayout() {
        makeCalendar()

        startTab.setOnClickListener {
            startEndMode = 0
            setModeLy()
        }

        endTab.setOnClickListener {
            startEndMode = 1
            setModeLy()
        }

        calBtn.setOnClickListener {
            timeMode = 0
            setModeLy()
        }

        timeBtn.setOnClickListener {
            timeMode = 1
            setModeLy()
        }

        rootLy.setOnClickListener {}

        timeSwitch.isChecked = !timeObject.allday
        timeSwitch.setOnCheckedChangeListener { compoundButton, checked ->
            timeMode = if(checked) 1 else 0
            setModeLy()
        }

        setModeLy()
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
                dateLy.setBackgroundResource(AppRes.selectableItemBackground)

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

    private fun setTimeView(cal: Calendar) {
        timeWheel.setOnTimeChangedListener(null)
        timeWheel.currentHour = cal.get(Calendar.HOUR_OF_DAY)
        timeWheel.currentMinute = cal.get(Calendar.MINUTE)
        timeWheel.setOnTimeChangedListener { timePicker, h, m ->
            if(startEndMode == 0) {
                startCal.set(Calendar.HOUR_OF_DAY, h)
                startCal.set(Calendar.MINUTE, m)
                if(startCal > endCal) {
                    endCal.timeInMillis = startCal.timeInMillis
                }
            }else {
                endCal.set(Calendar.HOUR_OF_DAY, h)
                endCal.set(Calendar.MINUTE, m)
                if(startCal > endCal) {
                    startCal.timeInMillis = endCal.timeInMillis
                }
            }
            setDateText()
        }
    }

    private fun setDateText() {
        val dtStart = startCal.timeInMillis
        val dtEnd = endCal.timeInMillis
        if(!timeSwitch.isChecked) {
            startTimeTText.text = "${AppRes.date.format(dtStart)} ${AppRes.dow.format(dtStart)}"
            startTimeYMDText.text =  "${AppRes.ymDate.format(dtStart)}"
            endTimeTText.text = "${AppRes.date.format(dtEnd)} ${AppRes.dow.format(dtEnd)}"
            endTimeYMDText.text =  "${AppRes.ymDate.format(dtEnd)}"
        }else {
            startTimeTText.text = AppRes.time.format(dtStart)
            startTimeYMDText.text = AppRes.ymdDate.format(dtStart)
            endTimeTText.text = AppRes.time.format(dtEnd)
            endTimeYMDText.text = AppRes.ymdDate.format(dtEnd)
        }

        if(startEndMode == 0) {
            startText.setTextColor(CalendarSkin.selectedDateColor)
            startTimeTText.setTextColor(CalendarSkin.selectedDateColor)
            startTimeYMDText.setTextColor(CalendarSkin.selectedDateColor)

            endText.setTextColor(CalendarSkin.selectedBackgroundColor)
            endTimeTText.setTextColor(CalendarSkin.selectedBackgroundColor)
            endTimeYMDText.setTextColor(CalendarSkin.selectedBackgroundColor)
        }else {
            startText.setTextColor(CalendarSkin.selectedBackgroundColor)
            startTimeTText.setTextColor(CalendarSkin.selectedBackgroundColor)
            startTimeYMDText.setTextColor(CalendarSkin.selectedBackgroundColor)

            endText.setTextColor(CalendarSkin.selectedDateColor)
            endTimeTText.setTextColor(CalendarSkin.selectedDateColor)
            endTimeYMDText.setTextColor(CalendarSkin.selectedDateColor)
        }
    }

    private fun setModeLy() {
        if(timeSwitch.isChecked) {
            timeBtn.visibility = View.VISIBLE
        }else {
            timeBtn.visibility = View.GONE
        }

        if(timeMode == 0) {
            timeLy.visibility = View.GONE
            containter.visibility = View.VISIBLE
            dayOfWeekView.visibility = View.VISIBLE
            calendarText.visibility = View.VISIBLE
            calBtn.alpha = 1f
            timeBtn.alpha = 0.3f
            if(startEndMode == 0) {
                drawCalendar(startCal.timeInMillis)
            }else {
                drawCalendar(endCal.timeInMillis)
            }
        }else {
            timeLy.visibility = View.VISIBLE
            containter.visibility = View.GONE
            dayOfWeekView.visibility = View.GONE
            calendarText.visibility = View.GONE
            calBtn.alpha = 0.3f
            timeBtn.alpha = 1f
            if(startEndMode == 0) {
                setTimeView(startCal)
            }else {
                setTimeView(endCal)
            }
        }

        setDateText()
    }

    var startTextCellnum = -1
    var endTextCellnum = -1

    private fun drawCalendar(time: Long) {
        todayCal.timeInMillis = System.currentTimeMillis()
        tempCal.timeInMillis = time
        monthCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), todayCal.get(Calendar.DATE))
        setCalendarTime0(tempCal)
        tempCal.set(Calendar.DATE, 1)

        startCellNum = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        endCellNum = startCellNum + tempCal.getActualMaximum(Calendar.DATE) - 1
        rows = (endCellNum + 1) / 7 + if ((endCellNum + 1) % 7 > 0) 1 else 0
        minWidth = containter.width / columns.toFloat()
        minHeight = calendarHeight.toFloat() / rows
        calendarLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, calendarHeight)
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
            weekLy.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, minHeight.toInt())
            if(i < rows) {
                weekLy.visibility = View.VISIBLE
                for (j in 0..6){
                    val cellNum = i*7 + j
                    cellTimeMills[cellNum] = tempCal.timeInMillis
                    val dateLy = dateLys[cellNum]
                    dateLy.layoutParams = FrameLayout.LayoutParams(minWidth.toInt(), MATCH_PARENT)
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

        calendarText.text = AppRes.ymDate.format(monthCal.time)
        setDateText()
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
            dateText.setTypeface(null, Typeface.BOLD)
            dateText.setTextColor(CalendarSkin.dateColor)
        }else {
            dateText.setTypeface(null, Typeface.NORMAL)
            if(cellNum % columns == 0) {
                dateText.setTextColor(CalendarSkin.holiDateColor)
            }else {
                dateText.setTextColor(CalendarSkin.dateColor)
            }
        }
    }

    fun setDefaultDateTextSkin(textView: TextView) {
        val dateTextLayoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, dateTextSize)
        //textView.setTypeface(null, Typeface.BOLD)
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
        val size = dpToPx(40)
        var mode = 0
        val color = timeObject.color
        val lightColor = timeObject.color

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
