package com.ayaan.twelvepages.ui.sheet

import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.view.View
import android.view.WindowManager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.CalendarManager
import com.ayaan.twelvepages.manager.DateInfoManager
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.dialog.BottomSheetDialog
import com.ayaan.twelvepages.ui.dialog.CustomListDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.dialog_calendar_settings.view.*


class CalendarSettingsSheet(private val activity: Activity) : BottomSheetDialog() {
    
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.dialog_calendar_settings)
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        dialog.window?.navigationBarColor = AppTheme.backgroundDark
        val flags =  dialog.window?.peekDecorView()?.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.window?.peekDecorView()?.systemUiVisibility = flags!! or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        sheetBehavior.peekHeight = dpToPx(200)
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        setLayout()
        dialog.setOnShowListener {}
    }

    private fun setLayout() {
        setStartDow()
        setWeekNumDisplay()
        setDowDisplay()
        setWeekendDisplay()
        setHoliDisplay()
        setLunarDisplay()
        setOutsideMonth()
        setCalTextSize()
        setCalFontWidth()
        setWeekLine()
        setCheckedRecordDisplay()
    }

    private fun setStartDow() {
        val dowList = resources.getStringArray(R.array.day_of_weeks).toList()
        root.startdowText.text = dowList[AppStatus.startDayOfWeek - 1]
        root.startdowBtn.setOnClickListener {
            showDialog(CustomListDialog(activity,
                    str(R.string.startdow),
                    str(R.string.startdow_sub),
                    null,
                    false,
                    dowList) { index ->
                AppStatus.startDayOfWeek = index + 1
                Prefs.putInt("startDayOfWeek", AppStatus.startDayOfWeek)
                setStartDow()
                MainActivity.getCalendarPager()?.redrawAndSelect()
            }, true, true, true, false)
        }
    }

    private fun setDowDisplay() {
        if(AppStatus.isDowDisplay) {
            root.dowDisplayText.text = str(R.string.visible)
            root.dowDisplayText.setTextColor(AppTheme.primaryText)
        }else {
            root.dowDisplayText.text = str(R.string.unvisible)
            root.dowDisplayText.setTextColor(AppTheme.disableText)
        }
        root.dowDisplayBtn.setOnClickListener {
            AppStatus.isDowDisplay = !AppStatus.isDowDisplay
            Prefs.putBoolean("isDowDisplay", AppStatus.isDowDisplay)
            setDowDisplay()
            MainActivity.getCalendarPager()?.redrawAndSelect()
        }
    }

    private fun setWeekendDisplay() {
        root.weekDisplaySunText.setTextColor(CalendarManager.sundayColor)
        root.weekDisplaySatText.setTextColor(CalendarManager.saturdayColor)
        root.weekDisplayBtn.setOnClickListener {
            when {
                CalendarManager.sundayColor == AppTheme.red && CalendarManager.saturdayColor == AppTheme.blue -> {
                    CalendarManager.saturdayColor = AppTheme.red
                    Prefs.putInt("saturdayColor", CalendarManager.saturdayColor)
                }
                CalendarManager.sundayColor == AppTheme.red && CalendarManager.saturdayColor == AppTheme.red -> {
                    CalendarManager.saturdayColor = CalendarManager.dateColor
                    Prefs.putInt("saturdayColor", CalendarManager.saturdayColor)
                }
                CalendarManager.sundayColor == AppTheme.red && CalendarManager.saturdayColor == CalendarManager.dateColor -> {
                    CalendarManager.sundayColor = CalendarManager.dateColor
                    Prefs.putInt("sundayColor", CalendarManager.sundayColor)
                }
                CalendarManager.sundayColor == CalendarManager.dateColor && CalendarManager.saturdayColor == CalendarManager.dateColor -> {
                    CalendarManager.sundayColor = AppTheme.red
                    CalendarManager.saturdayColor = AppTheme.blue
                    Prefs.putInt("sundayColor", CalendarManager.sundayColor)
                    Prefs.putInt("saturdayColor", CalendarManager.saturdayColor)
                }
                else -> {
                    CalendarManager.saturdayColor = CalendarManager.dateColor
                    Prefs.putInt("saturdayColor", CalendarManager.saturdayColor)
                    CalendarManager.sundayColor = CalendarManager.dateColor
                    Prefs.putInt("sundayColor", CalendarManager.sundayColor)
                }
            }
            setWeekendDisplay()
            MainActivity.getCalendarPager()?.redrawAndSelect()
        }
    }

    private fun setHoliDisplay() {
        val holiList = resources.getStringArray(R.array.holidays).toList()
        root.holiDisplayText.text = holiList[AppStatus.holidayDisplay]
        if(AppStatus.holidayDisplay == 0) {
            root.holiDisplayText.setTextColor(AppTheme.disableText)
        }else {
            root.holiDisplayText.setTextColor(AppTheme.primaryText)
        }
        root.holiDisplayBtn.setOnClickListener {
            showDialog(CustomListDialog(activity,
                    str(R.string.holi_display),
                    str(R.string.holi_display_sub),
                    null,
                    false,
                    holiList) { index ->
                AppStatus.holidayDisplay = index
                Prefs.putInt("holidayDisplay", AppStatus.holidayDisplay)
                DateInfoManager.init()
                setHoliDisplay()
                MainActivity.getCalendarPager()?.redrawAndSelect()
            }, true, true, true, false)
        }
    }

    private fun setLunarDisplay() {
        if(AppStatus.isLunarDisplay) {
            root.lunarDisplayText.text = str(R.string.visible)
            root.lunarDisplayText.setTextColor(AppTheme.primaryText)
        }else {
            root.lunarDisplayText.text = str(R.string.unvisible)
            root.lunarDisplayText.setTextColor(AppTheme.disableText)
        }
        root.lunarDisplayBtn.setOnClickListener {
            AppStatus.isLunarDisplay = !AppStatus.isLunarDisplay
            Prefs.putBoolean("isLunarDisplay", AppStatus.isLunarDisplay)
            setLunarDisplay()
            MainActivity.getCalendarPager()?.redrawAndSelect()
        }
    }

    private fun setOutsideMonth() {
        when(AppStatus.outsideMonthAlpha) {
            0f -> {
                root.outsideMonthText.text = str(R.string.unvisible)
                root.outsideMonthText.setTextColor(AppTheme.disableText)
            }
            0.3f -> {
                root.outsideMonthText.text = str(R.string.visible)
                root.outsideMonthText.setTextColor(AppTheme.primaryText)
            }
        }

        root.outsideMonthBtn.setOnClickListener {
            when(AppStatus.outsideMonthAlpha) {
                0f -> AppStatus.outsideMonthAlpha = 0.3f
                0.3f -> AppStatus.outsideMonthAlpha = 0f
            }
            Prefs.putFloat("outsideMonthAlpha", AppStatus.outsideMonthAlpha)
            setOutsideMonth()
            MainActivity.getCalendarPager()?.redrawAndSelect()
        }
    }

    private fun setCalTextSize() {
        when(AppStatus.calTextSize) {
            -2 -> root.calTextSizeText.text = str(R.string.very_small)
            -1 -> root.calTextSizeText.text = str(R.string.small)
            0 -> root.calTextSizeText.text = str(R.string.normal)
            1 -> root.calTextSizeText.text = str(R.string.big)
            2 -> root.calTextSizeText.text = str(R.string.very_big)
        }
        //calTextSizeText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (8 + AppStatus.calTextSize).toFloat())
        root.calTextSizeBtn.setOnClickListener {
            when(AppStatus.calTextSize) {
                -2 -> AppStatus.calTextSize = -1
                -1 -> AppStatus.calTextSize = 0
                0 -> AppStatus.calTextSize = 1
                1 -> AppStatus.calTextSize = 2
                2 -> AppStatus.calTextSize = -2
            }
            Prefs.putInt("calTextSize", AppStatus.calTextSize)
            setCalTextSize()
            MainActivity.getCalendarPager()?.redrawAndSelect()
        }
    }

    private fun setCalFontWidth() {
        when(AppStatus.calRecordFontWidth) {
            0 -> root.calFontWidthText.text = str(R.string.normal)
            1 -> root.calFontWidthText.text = str(R.string.bold)
        }
        root.calFontWidthBtn.setOnClickListener {
            when(AppStatus.calRecordFontWidth) {
                0 -> AppStatus.calRecordFontWidth = 1
                1 -> AppStatus.calRecordFontWidth = 0
            }
            Prefs.putInt("calRecordFontWidth", AppStatus.calRecordFontWidth)
            setCalFontWidth()
            MainActivity.getCalendarPager()?.redrawAndSelect()
        }
    }

    private fun setWeekLine() {
        when(AppStatus.weekLine) {
            0f -> root.weekLineText.text = str(R.string.unvisible)
            0.1f -> root.weekLineText.text = str(R.string.thin)
            else -> root.weekLineText.text = str(R.string.bold)
        }
        root.weekLineBtn.setOnClickListener {
            when(AppStatus.weekLine) {
                0f -> AppStatus.weekLine = 0.1f
                0.1f -> AppStatus.weekLine = 0.5f
                else -> AppStatus.weekLine = 0f
            }
            Prefs.putFloat("weekLine", AppStatus.weekLine)
            setWeekLine()
            MainActivity.getCalendarPager()?.redrawAndSelect()
        }
    }

    private fun setWeekNumDisplay() {
        if(AppStatus.isWeekNumDisplay) {
            root.weekNumDisplayText.text = str(R.string.visible)
            root.weekNumDisplayText.setTextColor(AppTheme.primaryText)
        }else {
            root.weekNumDisplayText.text = str(R.string.unvisible)
            root.weekNumDisplayText.setTextColor(AppTheme.disableText)
        }
        root.weekNumDisplayBtn.setOnClickListener {
            AppStatus.isWeekNumDisplay = !AppStatus.isWeekNumDisplay
            Prefs.putBoolean("isWeekNumDisplay", AppStatus.isWeekNumDisplay)
            setWeekNumDisplay()
            MainActivity.getCalendarPager()?.redrawAndSelect()
        }
    }

    private fun setCheckedRecordDisplay() {
        when(AppStatus.checkedRecordDisplay) {
            0 -> root.checkedRecordDisplayText.text = str(R.string.check_option_0)
            1 -> root.checkedRecordDisplayText.text = str(R.string.check_option_1)
            2 -> root.checkedRecordDisplayText.text = str(R.string.check_option_2)
            3 -> root.checkedRecordDisplayText.text = str(R.string.check_option_3)
        }
        root.checkedRecordDisplayBtn.setOnClickListener {
            when(AppStatus.checkedRecordDisplay) {
                0 -> AppStatus.checkedRecordDisplay = 1
                1 -> AppStatus.checkedRecordDisplay = 2
                2 -> AppStatus.checkedRecordDisplay = 3
                3 -> AppStatus.checkedRecordDisplay = 0
            }
            Prefs.putInt("checkedRecordDisplay", AppStatus.checkedRecordDisplay)
            setCheckedRecordDisplay()
            MainActivity.getCalendarPager()?.redrawAndSelect()
        }
    }

}
