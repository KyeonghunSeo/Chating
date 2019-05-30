package com.ayaan.twelvepages.alarm

import android.app.*
import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.model.Alarm
import com.ayaan.twelvepages.model.Record
import com.pixplicity.easyprefs.library.Prefs
import java.util.*

object AlarmManager {
    private lateinit var manager: android.app.AlarmManager
    private lateinit var alarmOffsetStrings: Array<String>
    var eventAlarm = Long.MIN_VALUE
    var todoAlarm = Long.MIN_VALUE
    var socialAlarm = Long.MIN_VALUE
    var briefingAlarm = Long.MIN_VALUE

    fun init(context: Context) {
        alarmOffsetStrings = context.resources.getStringArray(R.array.alarms)
        if(!Prefs.getBoolean("createNotificationChannel", false)) {
            createNotificationChannel(context)
            Prefs.putBoolean("createNotificationChannel", true)
        }
        eventAlarm = Prefs.getLong("eventAlarm", Long.MIN_VALUE)
        todoAlarm = Prefs.getLong("todoAlarm", Long.MIN_VALUE)
        socialAlarm = Prefs.getLong("socialAlarm", Long.MIN_VALUE)
        briefingAlarm = Prefs.getLong("briefingAlarm", Long.MIN_VALUE)
        manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        registBriefingAlarm()
    }

    private fun createNotificationChannel(context: Context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(context.getString(R.string.notification_default_channel),
                        context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = AppTheme.primaryColor
                notificationChannel.setShowBadge(true)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(notificationChannel)
            }
        } catch (ignored: Exception) {}
    }

    private fun registBriefingAlarm() {
        if(briefingAlarm == Long.MIN_VALUE) {
            val intent = Intent(App.context, BriefingAlarmReceiver::class.java)
            val sender = PendingIntent.getBroadcast(App.context, 0, intent, PendingIntent.FLAG_NO_CREATE)
            if(sender == null) {
                val pIntent = PendingIntent.getBroadcast(App.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                manager.setRepeating(RTC_WAKEUP, System.currentTimeMillis() + 5000, HOUR_MILL, pIntent)
            }
        }
    }

    fun unRegistBrifingAlarm() {
        val intent = Intent(App.context, BriefingAlarmReceiver::class.java)
        val sender = PendingIntent.getBroadcast(App.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        if(sender != null) {
            manager.cancel(sender)
            sender.cancel()
        }
    }

    fun registTimeObjectAlarm(record: Record, registedAlarm: RegistedAlarm) {
        val intent = Intent(App.context, RecordAlarmReceiver::class.java)
        intent.putExtra("recordId", record.id)
        record.alarms
                .asSequence()
                .filter { it.dtAlarm >= System.currentTimeMillis() }
                .firstOrNull()?.let { alarm ->
                    val pIntent = PendingIntent.getBroadcast(App.context, registedAlarm.requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    registAlarm(alarm, pIntent)
                }
    }

    fun unRegistTimeObjectAlarm(requestCode: Int) {
        val intent = Intent(App.context, RecordAlarmReceiver::class.java)
        val sender = PendingIntent.getBroadcast(App.context, requestCode, intent, PendingIntent.FLAG_NO_CREATE)
        if(sender != null) {
            manager.cancel(sender)
            sender.cancel()
            l("알람 삭제 : $requestCode")
        }
    }

    private fun registAlarm(alarm: Alarm, pendingIntent: PendingIntent) {
        try {
            if (alarm.dtAlarm >= System.currentTimeMillis()) {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarm.dtAlarm, pendingIntent)
                    else -> manager.set(AlarmManager.RTC_WAKEUP, alarm.dtAlarm, pendingIntent)
                }
                l("알람 등록 : ${AppDateFormat.ymdDate.format(Date(alarm.dtAlarm))} ${AppDateFormat.time.format(Date(alarm.dtAlarm))}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTimeObjectAlarmText(offset: Long) : String? = when(offset) {
        0L -> alarmOffsetStrings[0]
        1000L * 60 * 60 * 9 -> alarmOffsetStrings[1]
        1000L * 60 * 60 * 12 -> alarmOffsetStrings[2]
        1000L * 60 * 60 * 18 -> alarmOffsetStrings[3]
        -1000L * 60 * 30 -> alarmOffsetStrings[4]
        -1000L * 60 * 60 -> alarmOffsetStrings[5]
        -1000L * 60 * 60 * 24 -> alarmOffsetStrings[6]
        -1000L * 60 * 60 * 24 * 7 -> alarmOffsetStrings[7]
        else -> null
    }

}