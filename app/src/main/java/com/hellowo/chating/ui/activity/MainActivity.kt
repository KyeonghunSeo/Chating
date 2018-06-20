package com.hellowo.chating.ui.activity

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.hellowo.chating.R
import com.hellowo.chating.l
import com.hellowo.chating.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import me.everything.android.ui.overscroll.IOverScrollState.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    @SuppressLint("SimpleDateFormat")
    private val yearDf = SimpleDateFormat("yyyy")
    private val monthDf = SimpleDateFormat("MMMM", Locale.US)
    var scrollOffset = 0f
    var scrollState = STATE_IDLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        val decor = OverScrollDecoratorHelper.setUpOverScroll(calendarView)
        decor.setOverScrollStateListener{ decor, oldState, newState ->
            scrollState = newState
            when (newState) {
                STATE_IDLE -> { l("STATE_IDLE") }
                STATE_DRAG_START_SIDE -> { }
                STATE_DRAG_END_SIDE -> { }
                STATE_BOUNCE_BACK -> {
                    if(scrollOffset > 100f) {
                        calendarView.moveMonth(-1)
                    }else if(scrollOffset < -100f){
                        calendarView.moveMonth(1)
                    }
                }
            }
        }

        decor.setOverScrollUpdateListener { decor, state, offset -> scrollOffset = offset }

        calendarView.onDrawed = { cal ->
            val date = cal.time
            yearText.text = yearDf.format(date)
            monthText.text = monthDf.format(date)
        }
    }
}