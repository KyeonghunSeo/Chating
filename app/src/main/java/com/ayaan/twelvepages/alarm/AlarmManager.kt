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
    val offsets = arrayOf(
            0L,
            1000L * 60 * 60 * 9,
            1000L * 60 * 60 * 12,
            1000L * 60 * 60 * 18,
            -1000L * 60 * 30,
            -1000L * 60 * 60,
            -1000L * 60 * 60 * 24,
            -1000L * 60 * 60 * 24 * 7)
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

    fun getOffsetText(offset: Long) : String? {
        val index = offsets.indexOf(offset)
        return if(index >= 0) alarmOffsetStrings[index]
        else null
    }

    fun getAlarmNotiText(record: Record) : String {
        val alarm = record.alarms[0]!!
        val timeStr = if(record.isSetTime()) AppDateFormat.dateTime.format(Date(record.dtStart))
        else AppDateFormat.mdeDate.format(Date(record.dtStart))
        val diffStr = getDiffStr(alarm.dtAlarm, record.dtStart)
        return "$timeStr ($diffStr)"
    }

    fun getDiffStr(dtAlarm: Long, dtStart: Long) : String {
        val diff = dtAlarm - dtStart
        return when  {
            diff == 0L -> App.context.getString(R.string.now)
            diff > 0 -> {
                tempCal.timeInMillis = dtAlarm
                val diffToday = Math.abs(getDiffToday(tempCal))
                when (diffToday) {
                    0 -> App.context.getString(R.string.today)
                    1 -> App.context.getString(R.string.yesterday)
                    else -> String.format(App.context.getString(R.string.date_before), diffToday)
                }
            }
            else -> {
                val diffMin = Math.abs(diff / MIN_MILL)
                when {
                    diffMin < 60 -> String.format(App.context.getString(R.string.min_after), Math.abs(diffMin))
                    diffMin < 60 * 24 -> {
                        val diffHour = Math.abs(diff / HOUR_MILL)
                        val diffMinHour = diffMin % 60
                        when (diffMinHour) {
                            0L -> String.format(App.context.getString(R.string.hour_after), diffHour)
                            else -> String.format(App.context.getString(R.string.hour_min_after), diffHour, diffMinHour)
                        }
                    }
                    else -> {
                        val diffDate = Math.abs(diff / DAY_MILL)
                        when (diffDate) {
                            1L -> App.context.getString(R.string.tomorrow)
                            else ->String.format(App.context.getString(R.string.date_after), diffDate)
                        }
                    }
                }
            }
        }
    }

}