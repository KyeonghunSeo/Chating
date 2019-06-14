package com.ayaan.twelvepages.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.alarm.AlarmManager
import com.ayaan.twelvepages.manager.OsCalendarManager
import com.ayaan.twelvepages.model.Record
import com.google.firebase.auth.FirebaseAuth
import com.ayaan.twelvepages.ui.dialog.CustomDialog
import com.ayaan.twelvepages.ui.dialog.OsCalendarDialog
import com.ayaan.twelvepages.ui.dialog.RecordViewStyleDialog
import com.ayaan.twelvepages.ui.dialog.TimePickerDialog
import com.ayaan.twelvepages.ui.view.RecordView
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initTheme(rootLy)
        initLayout()
    }

    private fun initLayout() {
        backBtn.setOnClickListener { onBackPressed() }

        var lastY = 0
        mainScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, y: Int, _: Int, _: Int ->
            /*
            if(y > lastY) {
                headerLy.translationY = -Math.min(y, headerLy.height).toFloat()
            }else {
                headerLy.translationY = Math.min(0f, headerLy.translationY + (y - lastY))
            }
            lastY = y*/
        }

        setDefaultAlarmTime()
        setConnectOsCalendar()

        emailText.text = FirebaseAuth.getInstance().currentUser?.email
        logoutBtn.setOnClickListener {
            showDialog(CustomDialog(this@SettingsActivity, getString(R.string.logout),
                    getString(R.string.ask_logout), null) { result, _, _ ->
                if(result) {
                    FirebaseAuth.getInstance().signOut()
                    finish()
                }
            }, true, true, true, false)
        }

        calendarSettingBtn.setOnClickListener {
            MainActivity.instance?.let {
                setResult(RESULT_CALENDAR_SETTING)
                finish()
            }
        }
    }

    private fun setDefaultAlarmTime() {
        setCalendarTime0(tempCal)
        val time0 = tempCal.timeInMillis
        morningAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[0]))
        afternoonAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[1]))
        eveningAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[2]))
        nightAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[3]))
        morningAlarmTimeBtn.setOnClickListener {
            showDialog(TimePickerDialog(this, time0 + AlarmManager.defaultAlarmTime[0]) { time ->
                tempCal.timeInMillis = time
                AlarmManager.defaultAlarmTime[0] = tempCal.get(Calendar.HOUR_OF_DAY) * HOUR_MILL + tempCal.get(Calendar.MINUTE) * MIN_MILL
                Prefs.putLong("defaultAlarmTime0", AlarmManager.defaultAlarmTime[0])
                morningAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[0]))
            }, true, true, true, false)
        }
        afternoonAlarmTimeBtn.setOnClickListener {
            showDialog(TimePickerDialog(this, time0 + AlarmManager.defaultAlarmTime[1]) { time ->
                tempCal.timeInMillis = time
                AlarmManager.defaultAlarmTime[1] = tempCal.get(Calendar.HOUR_OF_DAY) * HOUR_MILL + tempCal.get(Calendar.MINUTE) * MIN_MILL
                Prefs.putLong("defaultAlarmTime1", AlarmManager.defaultAlarmTime[1])
                afternoonAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[1]))
            }, true, true, true, false)
        }
        eveningAlarmTimeBtn.setOnClickListener {
            showDialog(TimePickerDialog(this, time0 + AlarmManager.defaultAlarmTime[2]) { time ->
                tempCal.timeInMillis = time
                AlarmManager.defaultAlarmTime[2] = tempCal.get(Calendar.HOUR_OF_DAY) * HOUR_MILL + tempCal.get(Calendar.MINUTE) * MIN_MILL
                Prefs.putLong("defaultAlarmTime2", AlarmManager.defaultAlarmTime[2])
                eveningAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[2]))
            }, true, true, true, false)
        }
        nightAlarmTimeBtn.setOnClickListener {
            showDialog(TimePickerDialog(this, time0 + AlarmManager.defaultAlarmTime[3]) { time ->
                tempCal.timeInMillis = time
                AlarmManager.defaultAlarmTime[3] = tempCal.get(Calendar.HOUR_OF_DAY) * HOUR_MILL + tempCal.get(Calendar.MINUTE) * MIN_MILL
                Prefs.putLong("defaultAlarmTime3", AlarmManager.defaultAlarmTime[3])
                nightAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[3]))
            }, true, true, true, false)
        }
    }

    private fun setConnectOsCalendar() {
        val size = OsCalendarManager.getConnectedCalendarIdsSet().size
        osCalendarText.text = if(size == 0) {
            osCalendarText.setTextColor(AppTheme.disableText)
            str(R.string.no_reference)
        } else {
            osCalendarText.setTextColor(AppTheme.primaryText)
            str(R.string.reference)
        }
        osCalendarBtn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALENDAR), RC_PERMISSIONS)
            } else {
                showDialog(OsCalendarDialog(this) { result -> if(result) setConnectOsCalendar()
                }, true, true, true, false)
            }
        }

        osCalendarStyleText.text = RecordView.getStyleText(OsCalendarManager.style)
        osCalendarStyleBtn.setOnClickListener {
            val record = Record(
                    id = "osInstance::",
                    style = OsCalendarManager.style,
                    colorKey = 0)
            showDialog(RecordViewStyleDialog(this, record, null) { style, colorKey ->
                OsCalendarManager.saveStyle(style)
                setConnectOsCalendar()
            }, true, true, true, false)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            RC_PERMISSIONS -> {
                permissions.indices
                        .filter { permissions[it] == Manifest.permission.READ_CALENDAR
                                && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .forEach { _ -> showDialog(OsCalendarDialog(this) { result -> if(result) setConnectOsCalendar()
                        }, true, true, true, false) }
                return
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if(FirebaseAuth.getInstance().currentUser != null) {
            MainActivity.getCalendarPager()?.redraw()
        }else {
            MainActivity.instance?.finish()
        }
    }
}