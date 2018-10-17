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

    var timeMode = if(timeObject.allday) 0 else 1

    init {}

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
        onConfirmed.invoke(calendarView.startCal, calendarView.endCal, !timeSwitch.isChecked)
    }

    private fun setLayout() {
        calendarView.startCal.timeInMillis = timeObject.dtStart
        calendarView.endCal.timeInMillis = timeObject.dtEnd
        calendarView.onDrawed = { setDateText() }

        startTab.setOnClickListener {
            calendarView.startEndMode = 0
            setModeLy()
        }

        endTab.setOnClickListener {
            calendarView.startEndMode = 1
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

    private fun setTimeView(cal: Calendar) {
        timeWheel.setOnTimeChangedListener(null)
        timeWheel.currentHour = cal.get(Calendar.HOUR_OF_DAY)
        timeWheel.currentMinute = cal.get(Calendar.MINUTE)
        timeWheel.setOnTimeChangedListener { timePicker, h, m ->
            if(calendarView.startEndMode == 0) {
                calendarView.startCal.set(Calendar.HOUR_OF_DAY, h)
                calendarView.startCal.set(Calendar.MINUTE, m)
                if(calendarView.startCal > calendarView.endCal) {
                    calendarView.endCal.timeInMillis = calendarView.startCal.timeInMillis
                }
            }else {
                calendarView.endCal.set(Calendar.HOUR_OF_DAY, h)
                calendarView.endCal.set(Calendar.MINUTE, m)
                if(calendarView.startCal > calendarView.endCal) {
                    calendarView.startCal.timeInMillis = calendarView.endCal.timeInMillis
                }
            }
            setDateText()
        }
    }

    private fun setDateText() {
        val dtStart = calendarView.startCal.timeInMillis
        val dtEnd = calendarView.endCal.timeInMillis
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

        if(calendarView.startEndMode == 0) {
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
            calendarView.visibility = View.VISIBLE
            calBtn.alpha = 1f
            timeBtn.alpha = 0.3f
            if(calendarView.startEndMode == 0) {
                calendarView.drawCalendar(calendarView.startCal.timeInMillis)
            }else {
                calendarView.drawCalendar(calendarView.endCal.timeInMillis)
            }
        }else {
            timeLy.visibility = View.VISIBLE
            calendarView.visibility = View.GONE
            calBtn.alpha = 0.3f
            timeBtn.alpha = 1f
            if(calendarView.startEndMode == 0) {
                setTimeView(calendarView.startCal)
            }else {
                setTimeView(calendarView.endCal)
            }
        }
        setDateText()
    }

}
