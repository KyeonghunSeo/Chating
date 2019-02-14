package com.hellowo.journey.ui.activity

import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.hellowo.journey.AppStatus
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.manager.CalendarManager
import com.hellowo.journey.showDialog
import com.hellowo.journey.ui.dialog.CustomDialog
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

        weekDisplaySunText.setTextColor(CalendarManager.sundayColor)
        weekDisplaySatText.setTextColor(CalendarManager.saturdayColor)
        weekDisplayBtn.setOnClickListener {
            when {
                CalendarManager.sundayColor == AppTheme.redColor && CalendarManager.saturdayColor == AppTheme.blueColor -> {
                    CalendarManager.saturdayColor = AppTheme.redColor
                    Prefs.putInt("saturdayColor", CalendarManager.saturdayColor)
                }
                CalendarManager.sundayColor == AppTheme.redColor && CalendarManager.saturdayColor == AppTheme.redColor -> {
                    CalendarManager.saturdayColor = AppTheme.primaryText
                    Prefs.putInt("saturdayColor", CalendarManager.saturdayColor)
                }
                CalendarManager.sundayColor == AppTheme.redColor && CalendarManager.saturdayColor == AppTheme.primaryText -> {
                    CalendarManager.sundayColor = AppTheme.primaryText
                    Prefs.putInt("sundayColor", CalendarManager.sundayColor)
                }
                CalendarManager.sundayColor == AppTheme.primaryText && CalendarManager.saturdayColor == AppTheme.primaryText -> {
                    CalendarManager.sundayColor = AppTheme.redColor
                    CalendarManager.saturdayColor = AppTheme.blueColor
                    Prefs.putInt("sundayColor", CalendarManager.sundayColor)
                    Prefs.putInt("saturdayColor", CalendarManager.saturdayColor)
                }
            }
            weekDisplaySunText.setTextColor(CalendarManager.sundayColor)
            weekDisplaySatText.setTextColor(CalendarManager.saturdayColor)
        }

        lunarSwitch.isChecked = AppStatus.isLunarDisplay
        lunarSwitch.setOnCheckedChangeListener { compoundButton, checked ->
            AppStatus.isLunarDisplay = checked
            Prefs.putBoolean("isLunarDisplay", AppStatus.isLunarDisplay)
        }



        logoutBtn.setOnClickListener {
            showDialog(CustomDialog(this@SettingsActivity, getString(R.string.logout),
                    getString(R.string.ask_logout), null) { result, _, _ ->
                if(result) {
                    FirebaseAuth.getInstance().signOut()
                    finish()
                }
            }, true, true, true, false)
        }
    }

    override fun onStop() {
        super.onStop()
        if(FirebaseAuth.getInstance().currentUser != null) {
            MainActivity.instance?.getCalendarView()?.reDrawCalendar()
        }else {
            MainActivity.instance?.finish()
        }
    }
}