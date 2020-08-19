package com.ayaan.twelvepages.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.SeekBar
import com.ayaan.twelvepages.AppStatus
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.str
import com.ayaan.twelvepages.widget.MonthlyCalendarWidget
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_widget_setting.*
import kotlinx.android.synthetic.main.dialog_calendar_settings.view.*

class WidgetSettingActivity : BaseActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_setting)
        initTheme(rootLy)
        backBtn.setOnClickListener { finish() }
        val progress = Prefs.getInt("monthlyWidgetTransparency", 100)
        transparencySeekBar.progress = progress
        transparencyText.text = "${100 - progress}%"
        transparencySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                transparencyText.text = "${100 - seekBar.progress}%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
        })

        initTextColor()
        initTextSize()
    }

    private fun initTextColor() {
        val textColor = Prefs.getInt("monthlyWidgetTextColor", AppTheme.secondaryText)
        textColorImg.setCardBackgroundColor(textColor)
        textColorBtn.setOnClickListener {
            if(textColor == AppTheme.secondaryText) {
                Prefs.putInt("monthlyWidgetTextColor", Color.WHITE)
            }else {
                Prefs.putInt("monthlyWidgetTextColor", AppTheme.secondaryText)
            }
            initTextColor()
        }
    }

    private fun initTextSize() {
        val textSize = Prefs.getFloat("monthlyWidgetTextSize", 8f)
        when(textSize) {
            7f -> textSizeText.text = str(R.string.small)
            8f -> textSizeText.text = str(R.string.normal)
            9f -> textSizeText.text = str(R.string.big)
            10f -> textSizeText.text = str(R.string.very_big)
        }
        textSizeBtn.setOnClickListener {
            val newSize = when(textSize) {
                7f -> 8f
                8f -> 9f
                9f -> 10f
                10f -> 7f
                else -> 8f
            }
            Prefs.putFloat("monthlyWidgetTextSize", newSize)
            initTextSize()
        }
    }

    override fun onStop() {
        super.onStop()
        Prefs.putInt("monthlyWidgetTransparency", transparencySeekBar.progress)
        val intent = Intent(this, MonthlyCalendarWidget::class.java)
        intent.action = "android.appwidget.action.APPWIDGET_UPDATE"
        sendBroadcast(intent)
    }
}