package com.hellowo.chating.ui.activity

import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionManager
import com.hellowo.chating.*
import com.hellowo.chating.calendar.CalendarSkin
import com.hellowo.chating.calendar.TimeObjectManager
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
            val col = cellNum % 7
            for (i in 0..6) {
                (dayOfWeekLy.getChildAt(i) as LinearLayout).getChildAt(1)
                        .setBackgroundColor(if(i == col) CalendarSkin.selectedDateColor else CalendarSkin.dateColor)
            }
        }
        timeObjectView.setCalendarView(calendarView)

        insertBtn.setOnClickListener {
            if(timeObjectView.viewMode == 0) {
                timeObjectView.show()
                calendarView.startEditMode()
            }else {
                timeObjectView.confirm()
            }
        }

        keepBtn.setOnClickListener {
            /*
            val animSet = AnimatorSet()
            animSet.playTogether(ObjectAnimator.ofFloat(keepBtn, "translationZ", 0f, dpToPx(15).toFloat()).setDuration(ANIM_DUR),
                    ObjectAnimator.ofFloat(keepBtn, "radius", dpToPx(15).toFloat(), dpToPx(25).toFloat()).setDuration(ANIM_DUR))
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()*/

            TransitionManager.beginDelayedTransition(rootLy, makeChangeBounceTransition())
            keepBtn.layoutParams = FrameLayout.LayoutParams(dpToPx(200), dpToPx(200)).apply {
                gravity = Gravity.BOTTOM
                setMargins(dpToPx(20), 0, 0, dpToPx(20))
            }
            keepBtn.elevation = dpToPx(15).toFloat()
        }
    }

    fun setDateText(date: Date) {
        yearText.visibility = View.VISIBLE
        yearText.text = yearDf.format(date)
        monthText.text = monthDf.format(date)
        dateTextHandler.removeMessages(0)
        dateTextHandler.sendEmptyMessageDelayed(0, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        TimeObjectManager.clear()
    }
}