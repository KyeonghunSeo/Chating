package com.hellowo.chating

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import com.hellowo.chating.alarm.AlarmManager
import com.pixplicity.easyprefs.library.Prefs
import io.realm.Realm
import io.realm.SyncConfiguration

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()
        AppRes.init(this)
        Realm.init(this)
        AlarmManager.init(this)
    }
}