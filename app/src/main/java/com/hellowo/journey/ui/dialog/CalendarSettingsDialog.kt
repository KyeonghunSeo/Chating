package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.hellowo.journey.*
import com.hellowo.journey.manager.CalendarManager
import com.hellowo.journey.manager.HolidayManager
import com.hellowo.journey.ui.activity.MainActivity
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.dialog_calendar_settings.*


class CalendarSettingsDialog(private val activity: Activity) : Dialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        window.setGravity(Gravity.BOTTOM or Gravity.LEFT)
        setContentView(R.layout.dialog_calendar_settings)
        setGlobalTheme(rootLy)
        setLayout()
        setOnShowListener {}
    }

    private fun setLayout() {
        rootLy.layoutParams.width = WRAP_CONTENT
        rootLy.requestLayout()
        setStartDow()
        setDowDisplay()
        setWeekendDisplay()
        setHoliDisplay()
        setLunarDisplay()
        setOutsideMonth()
        setCalTextSize()
    }



    private fun setStartDow() {
        val dowList = context.resources.getStringArray(R.array.day_of_weeks).toList()
        startdowText.text = dowList[AppStatus.startDayOfWeek - 1]
        startdowBtn.setOnClickListener {
            showDialog(CustomListDialog(activity,
                    context.getString(R.string.startdow),
                    context.getString(R.string.startdow_sub),
                    null,
                    false,
                    dowList) { index ->
                AppStatus.startDayOfWeek = index + 1
                Prefs.putInt("startDayOfWeek", AppStatus.startDayOfWeek)
                setStartDow()
                MainActivity.getCalendarPagerView()?.redraw()
            }, true, true, true, false)
        }
    }

    private fun setDowDisplay() {
        if(AppStatus.isDowDisplay) {
            dowDisplayText.text = context.getString(R.string.visible)
            dowDisplayText.setTextColor(AppTheme.primaryText)
        }else {
            dowDisplayText.text = context.getString(R.string.unvisible)
            dowDisplayText.setTextColor(AppTheme.disableText)
        }
        dowDisplayBtn.setOnClickListener {
            AppStatus.isDowDisplay = !AppStatus.isDowDisplay
            Prefs.putBoolean("isDowDisplay", AppStatus.isDowDisplay)
            setDowDisplay()
            MainActivity.getCalendarPagerView()?.redraw()
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
                    CalendarManager.saturdayColor = CalendarManager.dateColor
                    Prefs.putInt("saturdayColor", CalendarManager.saturdayColor)
                }
                CalendarManager.sundayColor == AppTheme.redColor && CalendarManager.saturdayColor == CalendarManager.dateColor -> {
                    CalendarManager.sundayColor = CalendarManager.dateColor
                    Prefs.putInt("sundayColor", CalendarManager.sundayColor)
                }
                CalendarManager.sundayColor == CalendarManager.dateColor && CalendarManager.saturdayColor == CalendarManager.dateColor -> {
                    CalendarManager.sundayColor = AppTheme.redColor
                    CalendarManager.saturdayColor = AppTheme.blueColor
                    Prefs.putInt("sundayColor", CalendarManager.sundayColor)
                    Prefs.putInt("saturdayColor", CalendarManager.saturdayColor)
                }
            }
            setWeekendDisplay()
            MainActivity.getCalendarPagerView()?.redraw()
        }
    }

    private fun setHoliDisplay() {
        val holiList = context.resources.getStringArray(R.array.holidays).toList()
        holiDisplayText.text = holiList[AppStatus.holidayDisplay]
        if(AppStatus.holidayDisplay == 0) {
            holiDisplayText.setTextColor(AppTheme.disableText)
        }else {
            holiDisplayText.setTextColor(AppTheme.primaryText)
        }
        holiDisplayBtn.setOnClickListener {
            showDialog(CustomListDialog(activity,
                    context.getString(R.string.holi_display),
                    context.getString(R.string.holi_display_sub),
                    null,
                    false,
                    holiList) { index ->
                AppStatus.holidayDisplay = index
                Prefs.putInt("holidayDisplay", AppStatus.holidayDisplay)
                HolidayManager.init()
                setHoliDisplay()
                MainActivity.getCalendarPagerView()?.redraw()
            }, true, true, true, false)
        }
    }

    private fun setLunarDisplay() {
        if(AppStatus.isLunarDisplay) {
            lunarDisplayText.text = context.getString(R.string.visible)
            lunarDisplayText.setTextColor(AppTheme.primaryText)
        }else {
            lunarDisplayText.text = context.getString(R.string.unvisible)
            lunarDisplayText.setTextColor(AppTheme.disableText)
        }
        lunarDisplayBtn.setOnClickListener {
            AppStatus.isLunarDisplay = !AppStatus.isLunarDisplay
            Prefs.putBoolean("isLunarDisplay", AppStatus.isLunarDisplay)
            setLunarDisplay()
            MainActivity.getCalendarPagerView()?.redraw()
        }
    }

    private fun setOutsideMonth() {
        when(AppStatus.outsideMonthAlpha) {
            0f -> {
                outsideMonthText.text = context.getString(R.string.unvisible)
                outsideMonthText.setTextColor(AppTheme.disableText)
            }
            0.3f -> {
                outsideMonthText.text = context.getString(R.string.visible)
                outsideMonthText.setTextColor(AppTheme.primaryText)
            }
        }

        outsideMonthBtn.setOnClickListener {
            when(AppStatus.outsideMonthAlpha) {
                0f -> AppStatus.outsideMonthAlpha = 0.3f
                0.3f -> AppStatus.outsideMonthAlpha = 0f
            }
            Prefs.putFloat("outsideMonthAlpha", AppStatus.outsideMonthAlpha)
            setOutsideMonth()
            MainActivity.getCalendarPagerView()?.redraw()
        }
    }

    private fun setCalTextSize() {
        when(AppStatus.calTextSize) {
            -1 -> calTextSizeText.text = context.getString(R.string.small)
            0 -> calTextSizeText.text = context.getString(R.string.normal)
            1 -> calTextSizeText.text = context.getString(R.string.big)
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
            MainActivity.getCalendarPagerView()?.redraw()
        }
    }

}
