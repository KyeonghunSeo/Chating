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
import io.realm.*

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
        initRealm()
        Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(packageName)
                .setUseDefaultSharedPreference(true)
                .build()
        AppDateFormat.init(this)
        AppStatus.init(this)
        AppTheme.init(this)
        AlarmManager.init(this)
        Fresco.initialize(this)
        Toasty.Config.getInstance()
                .tintIcon(true)
                .setToastTypeface(AppTheme.regularFont)
                .setTextSize(14)
                .allowQueue(false)
                .apply()
    }

    private fun initRealm() {
        Realm.init(this)
        val config = RealmConfiguration.Builder()
                .schemaVersion(2)
                .migration { realm, oldVersion, newVersion ->
                    val schema = realm.schema
                    var version = oldVersion
                    if (version == 1L) {
                        schema.get("Template")?.let {
                            it.addField("recordMemo", String::class.java)
                                    .addField("recordMemoSelection", Int::class.java)
                        }
                        version++
                    }
                }.build()
        Realm.setDefaultConfiguration(config)
    }
}