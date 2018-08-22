package com.hellowo.chating.ui.activity

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.hellowo.chating.*
import com.hellowo.chating.calendar.model.CalendarSkin
import com.hellowo.chating.calendar.TimeObjectManager
import com.hellowo.chating.calendar.ViewMode
import com.hellowo.chating.ui.view.SwipeScrollView.Companion.SWIPE_LEFT
import com.hellowo.chating.ui.view.SwipeScrollView.Companion.SWIPE_RIGHT
import com.hellowo.chating.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_day_of_week.*
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        var instance: MainActivity? = null
    }

    lateinit var viewModel: MainViewModel
    private val dateTextHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            yearText.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        TimeObjectManager.init()
        instance = this
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        initLayout()
        initCalendarView()
        initDayView()
        initKeepView()
        initBriefingView()
        initBtns()
        initObserver()
    }

    private fun initLayout() {
        dateLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    private fun initCalendarView() {
        calendarView.onDrawed = { cal -> setDateText(cal.time) }
        calendarView.onSelected = { time, cellNum, showDayView ->
            if(showDayView && dayView.viewMode == ViewMode.CLOSED) {
                dayView.show()
            }else {
                setDayOfWeekLy(cellNum)
            }
        }
        calendarView.setOnSwiped { state ->
            if(dayView.viewMode == ViewMode.OPENED) {
                when(state) {
                    SWIPE_LEFT -> {
                        calendarView.moveDate(-1)
                        dayView.notifyDateChanged(-1)
                    }
                    SWIPE_RIGHT -> {
                        calendarView.moveDate(1)
                        dayView.notifyDateChanged(1)
                    }
                }
            }else {
                when(state) {
                    SWIPE_LEFT -> calendarView.moveMonth(-1)
                    SWIPE_RIGHT -> calendarView.moveMonth(1)
                }
            }
        }
        detailView.setCalendarView(calendarView)

    }

    private fun initDayView() {
        dayView.setCalendarView(calendarView)
    }

    private fun initKeepView() {
        keepView.mainActivity = this
        keepView.setOnClickListener {
            if(keepView.viewMode == ViewMode.CLOSED) { keepView.show() }
        }
    }

    private fun initBriefingView() {
        briefingView.setOnClickListener {
            if(briefingView.viewMode == ViewMode.CLOSED) { briefingView.show() }
        }
    }

    private fun initBtns() {
        insertBtn.setOnClickListener {
            if(detailView.viewMode == ViewMode.CLOSED) {
                viewModel.targetTimeObject.value = TimeObjectManager.makeNewTimeObject(
                        getCalendarTime0(calendarView.selectedCal), getCalendarTime23(calendarView.selectedCal))
            }else {
                detailView.confirm()
            }
        }
    }

    private fun initObserver() {
        viewModel.targetTimeObject.observe(this, androidx.lifecycle.Observer { timeObject ->
            if(detailView.viewMode == ViewMode.CLOSED && timeObject != null) {
                detailView.show(timeObject)
            }
        })
    }

    private fun setDayOfWeekLy(cellNum: Int) {
        val col = cellNum % 7
        for (i in 0..6) {
            val textView = ((dayOfWeekLy.getChildAt(i) as LinearLayout).getChildAt(0) as TextView)
            val bar = (dayOfWeekLy.getChildAt(i) as LinearLayout).getChildAt(1)
            if(i == col) {
                textView.setTextColor(CalendarSkin.selectedDateColor)
                textView.setTypeface(null, Typeface.BOLD)
                bar.setBackgroundColor(CalendarSkin.selectedDateColor)
                bar.scaleY = 2f
            }else {
                textView.setTextColor(CalendarSkin.dateColor)
                textView.setTypeface(null, Typeface.NORMAL)
                bar.setBackgroundColor(CalendarSkin.dateColor)
                bar.scaleY = 1f
            }
        }
    }

    fun setDateText(date: Date) {
        yearText.visibility = View.VISIBLE
        yearText.text = yearDf.format(date)
        monthText.text = monthDf.format(date)
        dateTextHandler.removeMessages(0)
        dateTextHandler.sendEmptyMessageDelayed(0, 2000)
    }

    override fun onBackPressed() {
        when{
            detailView.viewMode == ViewMode.OPENED -> detailView.hide()
            keepView.viewMode == ViewMode.OPENED -> keepView.hide()
            briefingView.viewMode == ViewMode.OPENED -> briefingView.hide()
            dayView.viewMode == ViewMode.OPENED -> dayView.hide()
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TimeObjectManager.clear()
    }
}