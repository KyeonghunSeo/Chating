package com.ayaan.twelvepages.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.ayaan.twelvepages.AppStatus
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.str
import com.ayaan.twelvepages.widget.MonthlyCalendarWidget
import com.ayaan.twelvepages.widget.WeeklyCalendarWidget
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_widget_setting.*

class WidgetSettingActivity : BaseActivity() {
    lateinit var widgetName: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_setting)
        initTheme(rootLy)
        backBtn.setOnClickListener { finish() }
        widgetName = intent?.data?.toString() ?: "monthlyWidget"
        initTransparency()
        initTextColor()
        initFont()
        initDateTextSize()
        initTextSize()
        initWeekLine()
        initShowNextWeek()
    }

    private fun initTransparency() {
        val progress = Prefs.getInt("${widgetName}Transparency", 100)
        transparencySeekBar.progress = progress
        transparencyText.text = "${100 - progress}%"
        transparencySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                transparencyText.text = "${100 - seekBar.progress}%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
        })
    }

    private fun initTextColor() {
        val textColor = Prefs.getInt("${widgetName}TextColor", AppTheme.secondaryText)
        textColorImg.setCardBackgroundColor(textColor)
        textColorBtn.setOnClickListener {
            if(textColor == AppTheme.secondaryText) {
                Prefs.putInt("${widgetName}TextColor", Color.WHITE)
            }else {
                Prefs.putInt("${widgetName}TextColor", AppTheme.secondaryText)
            }
            initTextColor()
        }
    }

    private fun initFont() {
        val font = Prefs.getInt("${widgetName}Font", 0)
        when(font) {
            1 -> fontText.text = str(R.string.moon_record_font)
            else -> fontText.text = str(R.string.os_font)
        }
        fontBtn.setOnClickListener {
            if(font == 0) {
                Prefs.putInt("${widgetName}Font", 1)
            }else {
                Prefs.putInt("${widgetName}Font", 0)
            }
            initFont()
        }
    }

    private fun initDateTextSize() {
        val textSize = Prefs.getFloat("${widgetName}DateTextSize", 12f)
        when(textSize) {
            12f -> dateTextSizeText.text = str(R.string.small)
            13f -> dateTextSizeText.text = str(R.string.normal)
            else -> dateTextSizeText.text = str(R.string.big)
        }
        dateTextSizeBtn.setOnClickListener {
            val newSize = when(textSize) {
                12f -> 13f
                13f -> 14f
                else -> 12f
            }
            Prefs.putFloat("${widgetName}DateTextSize", newSize)
            initDateTextSize()
        }
    }

    private fun initTextSize() {
        val textSize = Prefs.getFloat("${widgetName}TextSize", 8f)
        when(textSize) {
            7f -> textSizeText.text = str(R.string.small)
            8f -> textSizeText.text = str(R.string.normal)
            9f -> textSizeText.text = str(R.string.big)
            10f -> textSizeText.text = str(R.string.very_big)
            11f, 12f -> textSizeText.text = str(R.string.super_big)
            else -> textSizeText.text = str(R.string.normal)
        }
        textSizeBtn.setOnClickListener {
            val newSize = when(textSize) {
                7f -> 8f
                8f -> 9f
                9f -> 10f
                10f -> 7f
                else -> 7f
            }
            Prefs.putFloat("${widgetName}TextSize", newSize)
            initTextSize()
        }
    }

    private fun initWeekLine() {
        val lineVisibility = Prefs.getInt("${widgetName}WeekLine", View.VISIBLE)
        when(lineVisibility) {
            View.VISIBLE -> weekLineText.text = str(R.string.show)
            else -> weekLineText.text = str(R.string.hide)
        }
        weekLineBtn.setOnClickListener {
            val newVisibility = when(lineVisibility) {
                View.VISIBLE -> View.GONE
                else -> View.VISIBLE
            }
            Prefs.putInt("${widgetName}WeekLine", newVisibility)
            initWeekLine()
        }
    }

    private fun initShowNextWeek() {
        if(widgetName == "weeklyWidget") {
            nextWeekBtn.visibility = View.VISIBLE
        }else {
            nextWeekBtn.visibility = View.GONE
        }
        val showNextWeek = Prefs.getInt("${widgetName}ShowNextWeek", 0)
        when(showNextWeek) {
            1 -> nextWeekText.text = str(R.string.show)
            else -> nextWeekText.text = str(R.string.hide)
        }
        nextWeekBtn.setOnClickListener {
            if(showNextWeek == 0) {
                Prefs.putInt("${widgetName}ShowNextWeek", 1)
            }else {
                Prefs.putInt("${widgetName}ShowNextWeek", 0)
            }
            initShowNextWeek()
        }
    }

    override fun onStop() {
        super.onStop()
        Prefs.putInt("${widgetName}Transparency", transparencySeekBar.progress)
        sendBroadcast(Intent(this, MonthlyCalendarWidget::class.java).apply {
            action = "android.appwidget.action.APPWIDGET_UPDATE"
        })
        sendBroadcast(Intent(this, WeeklyCalendarWidget::class.java).apply {
            action = "android.appwidget.action.APPWIDGET_UPDATE"
        })
    }
}