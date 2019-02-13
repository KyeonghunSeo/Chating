package com.hellowo.journey.ui.activity

import android.os.Bundle
import com.hellowo.journey.AppStatus
import com.hellowo.journey.R
import com.hellowo.journey.showDialog
import com.hellowo.journey.ui.dialog.CustomListDialog
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initTheme(rootLy)
        initLayout()
    }

    private fun initLayout() {
        backBtn.setOnClickListener { onBackPressed() }

        val dowList = resources.getStringArray(R.array.day_of_weeks).toList()
        startdowText.text = dowList[AppStatus.startDayOfWeek - 1]
        startdowBtn.setOnClickListener {
            showDialog(CustomListDialog(this@SettingsActivity,
                    getString(R.string.startdow),
                    getString(R.string.startdow_sub),
                    null,
                    false,
                    resources.getStringArray(R.array.day_of_weeks).toList()) { index ->
                AppStatus.startDayOfWeek = index + 1
                Prefs.putInt("startDayOfWeek", AppStatus.startDayOfWeek)
                startdowText.text = dowList[AppStatus.startDayOfWeek - 1]
            }, true, true, true, false)
        }
    }

    override fun onStop() {
        super.onStop()
        MainActivity.instance?.getCalendarView()?.reDrawCalendar()
    }
}