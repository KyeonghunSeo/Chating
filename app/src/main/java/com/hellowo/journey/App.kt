package com.hellowo.journey

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import com.hellowo.journey.alarm.AlarmManager
import com.pixplicity.easyprefs.library.Prefs
import io.realm.Realm
import io.realm.SyncUser

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var resource: Resources
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        resource = resources
        Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()
        AppDateFormat.init(this)
        AppStatus.init(this)
        AppTheme.init(this)
        Realm.init(this)
        AlarmManager.init(this)
    }
}