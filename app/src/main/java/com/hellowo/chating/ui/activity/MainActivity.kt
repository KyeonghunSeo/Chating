package com.hellowo.chating.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.hellowo.chating.R
import com.hellowo.chating.TIME
import com.hellowo.chating.calendar.TimeObjectManager
import com.hellowo.chating.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    @SuppressLint("SimpleDateFormat")
    private val yearDf = SimpleDateFormat("yyyy")
    private val monthDf = SimpleDateFormat("MMMM", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        TimeObjectManager.init()
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        calendarView.onDrawed = { cal ->
            val date = cal.time
            yearText.text = yearDf.format(date)
            monthText.text = monthDf.format(date)
        }
        timeObjectView.setCalendarView(calendarView)

        insertBtn.setOnClickListener {
            timeObjectView.onClick()/*
            val intent = Intent(this@MainActivity, TimeObjectActivity::class.java)
            intent.putExtra(TIME, calendarView.getSelectedCalendar().timeInMillis)
            startActivity(intent)*/
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TimeObjectManager.clear()
    }
}