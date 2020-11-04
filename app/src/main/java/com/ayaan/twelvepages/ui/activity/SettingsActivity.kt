package com.ayaan.twelvepages.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.DateTimePatternGenerator
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.text.format.DateFormat
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.alarm.AlarmManager
import com.ayaan.twelvepages.manager.OsCalendarManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.dialog.CustomDialog
import com.ayaan.twelvepages.ui.dialog.OsCalendarDialog
import com.ayaan.twelvepages.ui.dialog.RecordViewStyleDialog
import com.ayaan.twelvepages.ui.dialog.TimePickerDialog
import com.ayaan.twelvepages.ui.view.RecordView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.pixplicity.easyprefs.library.Prefs
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.*
import java.time.format.DateTimeFormatter
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
        mainScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, y: Int, _: Int, _: Int ->
            if(y > 0) topShadow.visibility = View.VISIBLE
            else topShadow.visibility = View.GONE
        }
        setDayViewAniamtion()
        setDefaultAlarmTime()
        setConnectOsCalendar()
        setExport()
        setBackup()

        emailText.text = "(${FirebaseAuth.getInstance().currentUser?.email})"
        premiumBtn.setOnClickListener { startActivity(Intent(this, PremiumActivity::class.java)) }
        supportBtn.setOnClickListener { startActivity(Intent(this, AboutUsActivity::class.java)) }
        logoutBtn.setOnClickListener {
            showDialog(CustomDialog(this@SettingsActivity, getString(R.string.logout),
                    getString(R.string.ask_logout), null) { result, _, _ ->
                if(result) {
                    backupDB(this, Runnable{
                        FirebaseAuth.getInstance().signOut()
                        setResult(RC_LOGOUT)
                        finish()
                    })
                }
            }, true, true, true, false)
        }

        calendarSettingBtn.setOnClickListener {
            MainActivity.instance?.let {
                setResult(RESULT_CALENDAR_SETTING)
                finish()
            }
        }

        dayviewSettingBtn.setOnClickListener {
            MainActivity.instance?.let {
                setResult(RESULT_DAYVIEW_SETTING)
                finish()
            }
        }

        instaBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://www.instagram.com/moon_records_diary")
            startActivity(intent)
        }

        shareBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.putExtra(Intent.EXTRA_TEXT, str(R.string.app_share_text))
            shareIntent.type = "text/plain"
            val chooser = Intent.createChooser(shareIntent, str(R.string.app_name))
            startActivityForResult(chooser, RC_APP_SHARE)
        }
    }

    private fun setDayViewAniamtion() {
        if(AppStatus.isDayViewAnimation) {
            dayViewAnimationText.setTextColor(AppTheme.secondaryText)
            dayViewAnimationText.text = getString(R.string.show)
        } else {
            dayViewAnimationText.setTextColor(AppTheme.disableText)
            dayViewAnimationText.text = getString(R.string.hide)
        }
        dayViewAnimationBtn.setOnClickListener {
            AppStatus.isDayViewAnimation = !AppStatus.isDayViewAnimation
            Prefs.putBoolean("isDayViewAnimation", AppStatus.isDayViewAnimation)
            setDayViewAniamtion()
        }
    }


    private fun setExport() {
        exportBtn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), RC_EXPORT_PERMISSION)
            } else {
                export()
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private fun export() {
        object : AsyncTask<String, String, String?>() {
            override fun doInBackground(vararg args: String): String? {
                val saveFile = File(Environment.getExternalStorageDirectory().absolutePath + "/${str(R.string.app_name)}")
                if (!saveFile.exists()) {
                    saveFile.mkdir()
                }
                try {
                    val buf = BufferedWriter(FileWriter(saveFile.path + "/EXPORT_${AppDateFormat.ymdtkey.format(Date())}.txt", true))
                    val realm = Realm.getDefaultInstance()
                    realm.where(Record::class.java).sort("dtStart").findAll()?.forEach { record ->
                        record?.let {
                            buf.append(makeTextContentsByRecord(it)) // 날짜 쓰기
                            buf.newLine() // 개행
                            buf.newLine() // 개행
                        }
                    }
                    realm.close()
                    buf.close()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return null
            }

            override fun onPreExecute() {
                showProgressDialog(null)
            }
            override fun onProgressUpdate(vararg text: String) {}
            override fun onPostExecute(result: String?) {
                hideProgressDialog()
                toast(R.string.export_done)
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun setBackup() {
        setBackupTimeText()
        backupBtn.setOnClickListener {
            val lastBackupTime = Prefs.getLong("last_backup_time", 0L)
            if(AppStatus.isPremium() || lastBackupTime < System.currentTimeMillis() - MIN_MILL) {
                backupDB(this, Runnable{
                    setBackupTimeText()
                    toast(R.string.success_backup, R.drawable.done)
                })
            }else {
                toast(R.string.try_again_after_min, R.drawable.info)
            }
        }

        leaveBtn.setOnClickListener {
            showDialog(CustomDialog(this@SettingsActivity, getString(R.string.leave),
                    getString(R.string.ask_leave), null) { result, _, _ ->
                if(result) {
                    Realm.getDefaultInstance().use {
                        it.executeTransaction {
                            it.deleteAll()
                        }
                    }

                    backupDB(this, Runnable{
                        FirebaseAuth.getInstance().signOut()
                        setResult(RC_LOGOUT)
                        finish()
                    })
                }
            }, true, true, true, false)
        }
    }

    private fun sync() {
        MainActivity.instance?.let { activity ->
            activity.showProgressDialog()
            val mAuth = FirebaseAuth.getInstance()
            val user = mAuth.currentUser
            val ref = FirebaseStorage.getInstance().reference
                    .child("${user?.uid}/db")
//        val realm = Realm.getDefaultInstance()
            ref.metadata.addOnSuccessListener {
                activity.hideProgressDialog()
                val dialog = CustomDialog(activity, activity.getString(R.string.sync),
                        AppDateFormat.ymdkey.format(Date(it.updatedTimeMillis)), null,
                        R.drawable.download_cloud) { result, _, _ ->
                    if(result) {

                    }
                }
                showDialog(dialog, true, true, true, false)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                toast(R.string.no_cloud_data)
            }
//        ref.getFile(File(realm.path)).addOnSuccessListener {
//            realm.close()
//        }.addOnFailureListener {
//            MainActivity.instance?.hideProgressDialog()
//            realm.close()
//        }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setBackupTimeText() {
        val lastBackupTime = Prefs.getLong("last_backup_time", 0L)
        backupText.text = AppDateFormat.ymd.format(Date(lastBackupTime)) + " " + AppDateFormat.time.format(Date(lastBackupTime))
    }

    private fun setDefaultAlarmTime() {
        val time0 = getTodayStartTime()
        morningAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[0]))
        afternoonAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[1]))
        eveningAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[2]))
        nightAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[3]))
        morningAlarmTimeBtn.setOnClickListener {
            showDialog(TimePickerDialog(this, time0 + AlarmManager.defaultAlarmTime[0]) { time ->
                AlarmManager.defaultAlarmTime[0] = getOnlyTime(time)
                Prefs.putLong("defaultAlarmTime0", AlarmManager.defaultAlarmTime[0])
                morningAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[0]))
            }, true, true, true, false)
        }
        afternoonAlarmTimeBtn.setOnClickListener {
            showDialog(TimePickerDialog(this, time0 + AlarmManager.defaultAlarmTime[1]) { time ->
                AlarmManager.defaultAlarmTime[1] = getOnlyTime(time)
                Prefs.putLong("defaultAlarmTime1", AlarmManager.defaultAlarmTime[1])
                afternoonAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[1]))
            }, true, true, true, false)
        }
        eveningAlarmTimeBtn.setOnClickListener {
            showDialog(TimePickerDialog(this, time0 + AlarmManager.defaultAlarmTime[2]) { time ->
                tempCal.timeInMillis = time
                AlarmManager.defaultAlarmTime[2] = getOnlyTime(time)
                Prefs.putLong("defaultAlarmTime2", AlarmManager.defaultAlarmTime[2])
                eveningAlarmTimeText.text = AppDateFormat.time.format(Date(time0 + AlarmManager.defaultAlarmTime[2]))
            }, true, true, true, false)
        }
        nightAlarmTimeBtn.setOnClickListener {
            showDialog(TimePickerDialog(this, time0 + AlarmManager.defaultAlarmTime[3]) { time ->
                AlarmManager.defaultAlarmTime[3] = getOnlyTime(time)
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
            osCalendarText.setTextColor(AppTheme.secondaryText)
            String.format(str(R.string.referencing), size)
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
            showDialog(RecordViewStyleDialog(this, record, null) { style, colorKey, symbol ->
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
            RC_EXPORT_PERMISSION -> {
                permissions.indices
                        .filter { permissions[it] == Manifest.permission.WRITE_EXTERNAL_STORAGE
                                && grantResults[it] == PackageManager.PERMISSION_GRANTED }
                        .forEach { _ -> export() }
                return
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if(FirebaseAuth.getInstance().currentUser != null) {
            MainActivity.getCalendarPager()?.redrawAndSelect()
        }else {
            MainActivity.instance?.finish()
        }
    }
}