package com.ayaan.twelvepages.alarm

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import com.ayaan.twelvepages.ui.activity.MainActivity

class NotiService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val action = intent.getIntExtra("action", 0)
            val bundle = Bundle()

            when(action) {
                2 -> bundle.putString("timeObjectId", intent.getStringExtra("timeObjectId"))
            }

            if(MainActivity.isShowing) {
                MainActivity.instance?.playAction(action, bundle)
            }else {
                appOpen(action, bundle)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun appOpen(action: Int, bundle: Bundle) {
        val manager = packageManager
        val intent = manager.getLaunchIntentForPackage(packageName)
        intent.putExtra("action", action)
        intent.putExtra("bundle", bundle)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        startActivity(intent)
    }
}