package com.ayaan.twelvepages.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.ayaan.twelvepages.R

import androidx.core.app.NotificationCompat
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.l

class BriefingAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        l("[브리핑 알람 리시브]")
        AlarmManager.setDailyRecordAlarm()
        showNotification(context)
    }

    private fun showNotification(context: Context) {
        val intent = Intent(context, NotiService::class.java)
        intent.putExtra("action", 1)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val mCompatBuilder = NotificationCompat.Builder(context, context.getString(R.string.notification_default_channel))
        mCompatBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
        mCompatBuilder.setTicker("오늘의 알림 리셋")
        mCompatBuilder.color = AppTheme.primary
        mCompatBuilder.setWhen(System.currentTimeMillis())
        mCompatBuilder.setContentTitle("오늘의 알림 리셋")
        mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
        mCompatBuilder.setContentIntent(pendingIntent)
        mCompatBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // public
        mCompatBuilder.setAutoCancel(true)
        nm.notify(System.currentTimeMillis().toInt(), mCompatBuilder.build())
    }

}