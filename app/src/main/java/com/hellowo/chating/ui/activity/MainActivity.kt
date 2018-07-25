package com.hellowo.chating.ui.activity

import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import com.hellowo.chating.*
import com.hellowo.chating.calendar.CalendarSkin
import com.hellowo.chating.calendar.TimeObjectManager
import com.hellowo.chating.calendar.ViewMode
import com.hellowo.chating.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_day_of_week.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    @SuppressLint("SimpleDateFormat")
    private val yearDf = SimpleDateFormat("yyyy")
    private val monthDf = SimpleDateFormat("MMMM", Locale.US)
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
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        dateLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        calendarView.onDrawed = { cal -> setDateText(cal.time) }
        calendarView.onSelected = { time, cellNum ->
            setDayOfWeekLy(cellNum)
        }
        editorView.setCalendarView(calendarView)

        insertBtn.setOnClickListener {
            if(editorView.viewMode == ViewMode.CLOSED) {
                editorView.show()
                calendarView.startEditMode()
            }else {
                editorView.confirm()
            }
        }

        keepView.mainActivity = this
        keepView.setOnClickListener {
            keepView.show()
        }

        briefingView.setOnClickListener {
            briefingView.show()
        }
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
            editorView.viewMode == ViewMode.OPENED -> editorView.hide()
            keepView.viewMode == ViewMode.OPENED -> keepView.hide()
            briefingView.viewMode == ViewMode.OPENED -> briefingView.hide()
            else -> super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TimeObjectManager.clear()
    }
}