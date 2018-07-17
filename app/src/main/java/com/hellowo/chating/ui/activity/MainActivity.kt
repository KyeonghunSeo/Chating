package com.hellowo.chating.ui.activity

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.hellowo.chating.R
import com.hellowo.chating.TIME
import com.hellowo.chating.calendar.TimeObjectManager
import com.hellowo.chating.l
import com.hellowo.chating.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
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
        timeObjectView.setCalendarView(calendarView)

        insertBtn.setOnClickListener {
            if(timeObjectView.viewMode == 0) {
                timeObjectView.show()
                calendarView.startEditMode()
            }else {
                timeObjectView.confirm()
            }
        }
    }

    fun setDateText(date: Date) {
        yearText.visibility = View.VISIBLE
        yearText.text = yearDf.format(date)
        monthText.text = monthDf.format(date)
        dateTextHandler.removeMessages(0)
        dateTextHandler.sendEmptyMessageDelayed(0, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        TimeObjectManager.clear()
    }
}