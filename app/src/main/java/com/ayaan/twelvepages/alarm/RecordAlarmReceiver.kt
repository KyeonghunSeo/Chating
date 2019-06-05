package com.ayaan.twelvepages.alarm

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.l
import com.ayaan.twelvepages.model.Record
import io.realm.Realm
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
            checkVaildAlarm(context, Realm.getDefaultInstance(),
                    intent.getStringExtra("recordId"), intent.getLongExtra("dtStart", Long.MIN_VALUE))
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun checkVaildAlarm(context: Context, realm: Realm, recordId: String, dtStart: Long) {
        realm.where(Record::class.java)
                .equalTo("id", recordId)
                .findFirst()?.let {
                    val record = realm.copyFromRealm(it)
                    if (!record.isDeleted()) {
                        if(dtStart != Long.MIN_VALUE) {
                            val duration = record.getDuration()
                            record.setDateTime(record.isSetTime(), dtStart, dtStart + duration)
                        }
                        showNotification(context, record)
                        AlarmManager.registRecordAlarm(realm, record)
                    }
                }
        realm.close()
    }

    private fun showNotification(context: Context, record: Record) {
        val intent = Intent(context, NotiService::class.java)
        intent.putExtra("action", 2)
        intent.putExtra("recordId", record.id)
        intent.putExtra("dtStart", record.dtStart)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val mCompatBuilder = NotificationCompat.Builder(context, context.getString(R.string.notification_default_channel))
        mCompatBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
        mCompatBuilder.setTicker(record.getTitleInCalendar())
        mCompatBuilder.color = record.getColor()
        mCompatBuilder.setWhen(System.currentTimeMillis())
        mCompatBuilder.setContentTitle(record.getTitleInCalendar())
        val contents = StringBuilder()
        if(record.dtStart != Long.MIN_VALUE && record.alarms.isNotEmpty()) {
            contents.append(AlarmManager.getAlarmNotiText(record))
        }
        if(!record.location.isNullOrEmpty()) {
            if(contents.isNotEmpty()) contents.append("\n")
            contents.append(record.location)
        }
        if(!record.description.isNullOrEmpty()) {
            if(contents.isNotEmpty()) contents.append("\n")
            contents.append(record.description)
        }
        if(contents.isNotEmpty()) {
            val lines = contents.lines()
            if (lines.size == 1) {
                mCompatBuilder.setContentText(contents.toString())
            }else {
                mCompatBuilder.setContentText(lines[0])
                mCompatBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(contents.toString()))
            }
        }
        mCompatBuilder.setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
        mCompatBuilder.setContentIntent(pendingIntent)
        mCompatBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // public
        mCompatBuilder.setAutoCancel(true)
        nm.notify(System.currentTimeMillis().toInt(), mCompatBuilder.build())
    }

}