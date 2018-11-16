package com.hellowo.journey.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.model.TimeObject
import io.realm.Realm
import java.lang.Exception

class TimeObjectAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        try{
            val realm = Realm.getDefaultInstance()
            realm.where(TimeObject::class.java)
                    .equalTo("id", intent.getStringExtra("timeObjectId"))
                    .findFirst()?.let {
                        val timeObject = realm.copyFromRealm(it)
                        realm.where(RegistedAlarm::class.java)
                                .equalTo("timeObjectId", timeObject.id).findFirst()?.let { registedAlarm ->
                                    showNotification(context, timeObject)
                                    AlarmManager.unRegistTimeObjectAlarm(registedAlarm.requestCode)
                                    AlarmManager.registTimeObjectAlarm(timeObject, registedAlarm)
                                }
                    }
            realm.close()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun showNotification(context: Context, timeObject: TimeObject) {
        val intent = Intent(context, NotiService::class.java)
        intent.putExtra("action", 2)
        intent.putExtra("timeObjectId", timeObject.id)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val mCompatBuilder = NotificationCompat.Builder(context, context.getString(R.string.notification_default_channel))
        mCompatBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
        mCompatBuilder.setTicker("ticker")
        mCompatBuilder.color = AppTheme.primaryColor
        mCompatBuilder.setWhen(System.currentTimeMillis())
        mCompatBuilder.setContentTitle(timeObject.title)
        mCompatBuilder.setContentText("contents")
        mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
        mCompatBuilder.setContentIntent(pendingIntent)
        mCompatBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // public
        mCompatBuilder.priority = Notification.PRIORITY_MAX // max
        mCompatBuilder.setAutoCancel(true)
        nm.notify(System.currentTimeMillis().toInt(), mCompatBuilder.build())
    }

}