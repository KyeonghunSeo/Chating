package com.hellowo.chating.alarm

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.hellowo.chating.l
import com.hellowo.chating.ui.activity.MainActivity

class NotiService : Service() {
    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getIntExtra("action", 0) ?: 0
        if(MainActivity.isShowing) {
            MainActivity.instance?.playAction(action)
        }else {
            appOpen(action)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun appOpen(action: Int) {
        val manager = packageManager
        val intent = manager.getLaunchIntentForPackage(packageName)
        intent.putExtra("action", action)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        startActivity(intent)
    }
}