package com.hellowo.journey.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.TypedValue
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.hellowo.journey.*
import com.hellowo.journey.manager.CalendarManager
import com.hellowo.journey.manager.HolidayManager
import com.hellowo.journey.manager.OsCalendarManager
import com.hellowo.journey.ui.dialog.CustomDialog
import com.hellowo.journey.ui.dialog.CustomListDialog
import com.hellowo.journey.ui.dialog.OsCalendarDialog
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

        setStartDow()
        setWeekendDisplay()
        setHoliDisplay()
        setLunarDisplay()
        setOutsideMonth()
        setCalTextSize()

        setConnectOsCalendar()

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

    private fun setStartDow() {
        val dowList = resources.getStringArray(R.array.day_of_weeks).toList()
        startdowText.text = dowList[AppStatus.startDayOfWeek - 1]
        startdowBtn.setOnClickListener {
            showDialog(CustomListDialog(this@SettingsActivity,
                    getString(R.string.startdow),
                    getString(R.string.startdow_sub),
                    null,
                    false,
                    dowList) { index ->
                AppStatus.startDayOfWeek = index + 1
                Prefs.putInt("startDayOfWeek", AppStatus.startDayOfWeek)
                setStartDow()
            }, true, true, true, false)
        }
    }

    private fun setWeekendDisplay() {
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
            setWeekendDisplay()
        }
    }

    private fun setHoliDisplay() {
        val holiList = resources.getStringArray(R.array.holidays).toList()
        holiDisplayText.text = holiList[AppStatus.holidayDisplay]
        if(AppStatus.holidayDisplay == 0) {
            holiDisplayText.setTextColor(AppTheme.disableText)
        }else {
            holiDisplayText.setTextColor(AppTheme.primaryText)
        }
        holiDisplayBtn.setOnClickListener {
            showDialog(CustomListDialog(this@SettingsActivity,
                    getString(R.string.holi_display),
                    getString(R.string.holi_display_sub),
                    null,
                    false,
                    holiList) { index ->
                AppStatus.holidayDisplay = index
                Prefs.putInt("holidayDisplay", AppStatus.holidayDisplay)
                HolidayManager.init()
                setHoliDisplay()
            }, true, true, true, false)
        }
    }

    private fun setLunarDisplay() {
        if(AppStatus.isLunarDisplay) {
            lunarDisplayText.text = getString(R.string.visible)
            lunarDisplayText.setTextColor(AppTheme.primaryText)
        }else {
            lunarDisplayText.text = getString(R.string.unvisible)
            lunarDisplayText.setTextColor(AppTheme.disableText)
        }
        lunarDisplayBtn.setOnClickListener {
            AppStatus.isLunarDisplay = !AppStatus.isLunarDisplay
            Prefs.putBoolean("isLunarDisplay", AppStatus.isLunarDisplay)
            setLunarDisplay()
        }
    }

    private fun setOutsideMonth() {
        when(AppStatus.outsideMonthAlpha) {
            0f -> {
                outsideMonthText.text = getString(R.string.unvisible)
                outsideMonthText.setTextColor(AppTheme.disableText)
            }
            0.3f -> {
                outsideMonthText.text = getString(R.string.blur)
                outsideMonthText.setTextColor(AppTheme.secondaryText)
            }
            1f -> {
                outsideMonthText.text = getString(R.string.visible)
                outsideMonthText.setTextColor(AppTheme.primaryText)
            }
        }

        outsideMonthBtn.setOnClickListener {
            when(AppStatus.outsideMonthAlpha) {
                0f -> AppStatus.outsideMonthAlpha = 0.3f
                0.3f -> AppStatus.outsideMonthAlpha = 1f
                1f -> AppStatus.outsideMonthAlpha = 0f
            }
            Prefs.putFloat("outsideMonthAlpha", AppStatus.outsideMonthAlpha)
            setOutsideMonth()
        }
    }

    private fun setCalTextSize() {
        when(AppStatus.calTextSize) {
            -1 -> calTextSizeText.text = getString(R.string.small)
            0 -> calTextSizeText.text = getString(R.string.normal)
            1 -> calTextSizeText.text = getString(R.string.big)
        }
        //calTextSizeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (8 + AppStatus.calTextSize).toFloat())
        calTextSizeBtn.setOnClickListener {
            when(AppStatus.calTextSize) {
                -1 -> AppStatus.calTextSize = 0
                0 -> AppStatus.calTextSize = 1
                1 -> AppStatus.calTextSize = -1
            }
            Prefs.putInt("calTextSize", AppStatus.calTextSize)
            setCalTextSize()
        }
    }

    private fun setConnectOsCalendar() {
        osCalendarBtn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                            this@SettingsActivity, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this@SettingsActivity, arrayOf(Manifest.permission.READ_CALENDAR), RC_PERMISSIONS)
            } else {
                showDialog(OsCalendarDialog(this@SettingsActivity),
                        true, true, true, false)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RC_PERMISSIONS -> {
                permissions.indices
                        .filter { permissions[it] == Manifest.permission.READ_CALENDAR
                                && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .forEach { _ -> showDialog(OsCalendarDialog(this@SettingsActivity),
                                true, true, true, false) }
                return
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if(FirebaseAuth.getInstance().currentUser != null) {
            MainActivity.getCalendarPagerView()?.redraw()
        }else {
            MainActivity.instance?.finish()
        }
    }
}