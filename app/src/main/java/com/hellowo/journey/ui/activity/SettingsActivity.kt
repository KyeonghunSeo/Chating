package com.hellowo.journey.ui.activity

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import com.google.firebase.auth.FirebaseAuth
import com.hellowo.journey.R
import com.hellowo.journey.RC_PERMISSIONS
import com.hellowo.journey.RESULT_CALENDAR_SETTING
import com.hellowo.journey.showDialog
import com.hellowo.journey.ui.dialog.CalendarSettingsDialog
import com.hellowo.journey.ui.dialog.CustomDialog
import com.hellowo.journey.ui.dialog.OsCalendarDialog
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
        mainScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if(scrollY > 0) topShadow.visibility = View.VISIBLE
            else topShadow.visibility = View.GONE
        }

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

        calendarSettingBtn.setOnClickListener {
            MainActivity.instance?.let {
                setResult(RESULT_CALENDAR_SETTING)
                finish()
            }
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