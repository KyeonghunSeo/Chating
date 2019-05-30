package com.ayaan.twelvepages.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.USER_URL
import com.ayaan.twelvepages.l
import com.ayaan.twelvepages.model.Record
import io.realm.Realm
import io.realm.SyncUser
import java.lang.Exception

class RecordAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        l("[알람 리시브]")
        try{
            /*
            val config = SyncUser.current()
                    .createConfiguration(USER_URL)
                    .fullSynchronization()
                    .build()
            Realm.getInstanceAsync(config, object : Realm.Callback() {
                override fun onSuccess(realm: Realm) {
                    checkVaildAlarm(context, realm, intent.getStringExtra("recordId"))
                }
            })
            */
            checkVaildAlarm(context, Realm.getDefaultInstance(), intent.getStringExtra("recordId"))
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun checkVaildAlarm(context: Context, realm: Realm, recordId: String) {
        realm.where(Record::class.java)
                .equalTo("id", recordId)
                .findFirst()?.let {
                    val timeObject = realm.copyFromRealm(it)
                    realm.where(RegistedAlarm::class.java)
                            .equalTo("recordId", timeObject.id).findFirst()?.let { registedAlarm ->
                                showNotification(context, timeObject)
                                AlarmManager.unRegistTimeObjectAlarm(registedAlarm.requestCode)
                                AlarmManager.registTimeObjectAlarm(timeObject, registedAlarm)
                            }}
        realm.close()
    }

    private fun showNotification(context: Context, record: Record) {
        val intent = Intent(context, NotiService::class.java)
        intent.putExtra("action", 2)
        intent.putExtra("recordId", record.id)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val mCompatBuilder = NotificationCompat.Builder(context, context.getString(R.string.notification_default_channel))
        mCompatBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
        mCompatBuilder.setTicker("ticker")
        mCompatBuilder.color = AppTheme.primaryColor
        mCompatBuilder.setWhen(System.currentTimeMillis())
        mCompatBuilder.setContentTitle(record.title)
        mCompatBuilder.setContentText("contents")
        mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
        mCompatBuilder.setContentIntent(pendingIntent)
        mCompatBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // public
        mCompatBuilder.priority = Notification.PRIORITY_MAX // max
        mCompatBuilder.setAutoCancel(true)
        nm.notify(System.currentTimeMillis().toInt(), mCompatBuilder.build())
    }

}