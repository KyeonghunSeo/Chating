package com.ayaan.twelvepages

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import com.facebook.drawee.backends.pipeline.Fresco
import com.ayaan.twelvepages.alarm.AlarmManager
import com.pixplicity.easyprefs.library.Prefs
import es.dmoral.toasty.Toasty
import io.realm.Realm

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
        Fresco.initialize(this)
        Toasty.Config.getInstance()
                .tintIcon(true)
                .setToastTypeface(AppTheme.regularFont)
                .setTextSize(14)
                .allowQueue(false)
                .apply()
    }
}