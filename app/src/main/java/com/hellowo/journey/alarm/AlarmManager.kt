package com.hellowo.journey.alarm

import android.annotation.SuppressLint
import android.app.*
import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.content.Context
import android.content.Intent
import android.os.Build
import com.hellowo.journey.*
import com.hellowo.journey.calendar.model.Alarm
import com.hellowo.journey.calendar.model.TimeObject
import com.pixplicity.easyprefs.library.Prefs
import java.util.*

object AlarmManager {
    private lateinit var manager: android.app.AlarmManager
    var eventAlarm = Long.MIN_VALUE
    var todoAlarm = Long.MIN_VALUE
    var socialAlarm = Long.MIN_VALUE
    var briefingAlarm = Long.MIN_VALUE

    fun init(context: Context) {
        if(!Prefs.getBoolean("createNotificationChannel", false)) {
            createNotificationChannel(context)
            Prefs.putBoolean("createNotificationChannel", true)
        }
        eventAlarm = Prefs.getLong("eventAlarm", Long.MIN_VALUE)
        todoAlarm = Prefs.getLong("todoAlarm", Long.MIN_VALUE)
        socialAlarm = Prefs.getLong("socialAlarm", Long.MIN_VALUE)
        briefingAlarm = Prefs.getLong("briefingAlarm", Long.MIN_VALUE)
        manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        //registBriefingAlarm()
    }

    private fun createNotificationChannel(context: Context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(context.getString(R.string.notification_default_channel),
                        context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = AppRes.primaryColor
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
                manager.setRepeating(RTC_WAKEUP, System.currentTimeMillis() + 5000, MIN_MILL, pIntent)
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

    fun registTimeObjectAlarm(timeObject: TimeObject) {
        val intent = Intent(App.context, TimeObjectAlarmReceiver::class.java)
        intent.putExtra("timeObjectId", timeObject.id)
        timeObject.alarms.sortBy { it.dtAlarm }
        timeObject.alarms.asSequence().filter { it.dtAlarm >= System.currentTimeMillis() }.sortedBy { it.dtAlarm }.firstOrNull()?.let {
            val alarmRequestCode = Prefs.getInt("alarmRequestCode", 0) + 1
            Prefs.putInt("alarmRequestCode", alarmRequestCode)
            intent.putExtra("alarmRequestCode", alarmRequestCode)
            val pIntent = PendingIntent.getBroadcast(App.context, alarmRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            registAlarm(it, pIntent)
        }
    }

    fun unRegistTimeObjectAlarm(requestCode: Int) {
        val intent = Intent(App.context, TimeObjectAlarmReceiver::class.java)
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
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> manager.setExact(AlarmManager.RTC_WAKEUP, alarm.dtAlarm, pendingIntent)
                    else -> manager.set(AlarmManager.RTC_WAKEUP, alarm.dtAlarm, pendingIntent)
                }
                l("새 알람 등록 : ${AppRes.ymdDate.format(Date(alarm.dtAlarm))} ${AppRes.time.format(Date(alarm.dtAlarm))}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}