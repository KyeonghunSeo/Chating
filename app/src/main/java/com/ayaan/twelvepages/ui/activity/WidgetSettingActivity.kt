package com.ayaan.twelvepages.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.ui.view.InkView
import com.ayaan.twelvepages.widget.MonthlyCalendarWidget
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_widget_setting.*

class WidgetSettingActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_setting)
        initTheme(rootLy)

        val progress = Prefs.getInt("monthlyWidgetTransparency", 100)
        transparencySeekBar.progress = progress
        transparencyText.text = progress.toString()
        transparencySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                transparencyText.text = seekBar.progress.toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
        })
    }

    override fun onStop() {
        super.onStop()
        Prefs.putInt("monthlyWidgetTransparency", transparencySeekBar.progress)
        val intent = Intent(this, MonthlyCalendarWidget::class.java)
        intent.action = "android.appwidget.action.APPWIDGET_UPDATE"
        sendBroadcast(intent)
    }
}