package com.hellowo.chating.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri

import com.hellowo.chating.AppRes
import com.hellowo.chating.R
import com.hellowo.chating.calendar.model.Alarm

import java.util.Calendar

import androidx.core.app.NotificationCompat

class BriefingAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context)
    }

    private fun showNotification(context: Context) {
        val intent = Intent(context, NotiService::class.java)
        intent.putExtra("action", 1)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val mCompatBuilder = NotificationCompat.Builder(context, context.getString(R.string.notification_default_channel))
        mCompatBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
        mCompatBuilder.setTicker("ticker")
        mCompatBuilder.color = AppRes.primaryColor
        mCompatBuilder.setWhen(System.currentTimeMillis())
        mCompatBuilder.setContentTitle("title")
        mCompatBuilder.setContentText("contents")
        mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
        mCompatBuilder.setContentIntent(pendingIntent)
        mCompatBuilder.setVisibility(Notification.VISIBILITY_PUBLIC) // public
        mCompatBuilder.priority = Notification.PRIORITY_MAX // max
        mCompatBuilder.setAutoCancel(true)
        nm.notify(System.currentTimeMillis().toInt(), mCompatBuilder.build())
    }

}