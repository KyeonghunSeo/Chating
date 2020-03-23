package com.ayaan.twelvepages.ui.sheet

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.CalendarManager
import com.ayaan.twelvepages.manager.DateInfoManager
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.dialog.BottomSheetDialog
import com.ayaan.twelvepages.ui.dialog.CustomListDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.sheet_dayview_settings.view.*


class DayViewSettingsSheet(private val activity: Activity) : BottomSheetDialog() {
    
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.sheet_dayview_settings)
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
        setShowUpdateTime()
        setDisplayDayViewWeekNum()
        setCheckedRecordDisplay()
        setRememberPhoto()
        setRememberBeforeYear()
        setDisplayRecordDivider()
    }

    private fun setShowUpdateTime() {
        if(AppStatus.isDisplayUpdateTime) {
            root.updatedTimeText.text = str(R.string.visible)
            root.updatedTimeText.setTextColor(AppTheme.secondaryText)
        }else {
            root.updatedTimeText.text = str(R.string.unvisible)
            root.updatedTimeText.setTextColor(AppTheme.disableText)
        }
        root.updatedTimeBtn.setOnClickListener {
            AppStatus.isDisplayUpdateTime = !AppStatus.isDisplayUpdateTime
            Prefs.putBoolean("isDisplayUpdateTime", AppStatus.isDisplayUpdateTime)
            setShowUpdateTime()
            MainActivity.getDayPager()?.redraw()
        }
    }

    private fun setDisplayDayViewWeekNum() {
        if(AppStatus.isDisplayDayViewWeekNum) {
            root.weekNumDisplayText.text = str(R.string.visible)
            root.weekNumDisplayText.setTextColor(AppTheme.secondaryText)
        }else {
            root.weekNumDisplayText.text = str(R.string.unvisible)
            root.weekNumDisplayText.setTextColor(AppTheme.disableText)
        }
        root.weekNumDisplayBtn.setOnClickListener {
            AppStatus.isDisplayDayViewWeekNum = !AppStatus.isDisplayDayViewWeekNum
            Prefs.putBoolean("isDisplayDayViewWeekNum", AppStatus.isDisplayDayViewWeekNum)
            setDisplayDayViewWeekNum()
            MainActivity.getDayPager()?.redraw()
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
            MainActivity.getDayPager()?.redraw()
        }
    }

    private fun setRememberPhoto() {
        if(AppStatus.rememberPhoto == NONE || AppStatus.rememberPhoto == NO) {
            root.rememberPhotoText.text = str(R.string.unuse)
            root.rememberPhotoText.setTextColor(AppTheme.disableText)
        }else {
            root.rememberPhotoText.text = str(R.string.use)
            root.rememberPhotoText.setTextColor(AppTheme.secondaryText)
        }
        root.rememberPhotoBtn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.instance!!, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), RC_IMAGE_ATTACHMENT)
            } else {
                setRememberPhotoData()
            }
        }
    }

    private fun setRememberPhotoData() {
        AppStatus.rememberPhoto = if(AppStatus.rememberPhoto == NONE || AppStatus.rememberPhoto == NO) YES else NO
        Prefs.putInt("rememberPhoto", AppStatus.rememberPhoto)
        setRememberPhoto()
        MainActivity.getDayPager()?.redraw()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            RC_IMAGE_ATTACHMENT -> {
                permissions.indices
                        .filter { permissions[it] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .forEach { _ -> setRememberPhotoData() }
                return
            }
        }
    }

    private fun setRememberBeforeYear() {
        if(AppStatus.rememberBeforeYear == NONE || AppStatus.rememberBeforeYear == NO) {
            root.rememberBeforeYearText.text = str(R.string.unuse)
            root.rememberBeforeYearText.setTextColor(AppTheme.disableText)
        }else {
            root.rememberBeforeYearText.text = str(R.string.use)
            root.rememberBeforeYearText.setTextColor(AppTheme.secondaryText)
        }
        root.rememberBeforeYearBtn.setOnClickListener {
            AppStatus.rememberBeforeYear = if(AppStatus.rememberBeforeYear == NONE || AppStatus.rememberBeforeYear == NO) YES else NO
            Prefs.putInt("rememberBeforeYear", AppStatus.rememberBeforeYear)
            setRememberBeforeYear()
            MainActivity.getDayPager()?.redraw()
        }
    }

    private fun setDisplayRecordDivider() {
        when(AppStatus.displayRecordDivider) {
            0 -> {
                root.recordDividerText.text = str(R.string.unvisible)
                root.recordDividerText.setTextColor(AppTheme.disableText)
            }
            else -> {
                root.recordDividerText.text = str(R.string.visible)
                root.recordDividerText.setTextColor(AppTheme.secondaryText)
            }
        }
        root.recordDividerBtn.setOnClickListener {
            when(AppStatus.displayRecordDivider) {
                0 -> AppStatus.displayRecordDivider = 1
                else -> AppStatus.displayRecordDivider = 0
            }
            Prefs.putInt("displayRecordDivider", AppStatus.displayRecordDivider)
            setDisplayRecordDivider()
            MainActivity.getDayPager()?.redraw()
        }
    }

}
