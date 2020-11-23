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
                .setTextSize(12)
                .allowQueue(false)
                .apply()
    }

    fun initRealm() {
        Realm.init(this)
        val config = RealmConfiguration.Builder()
                .schemaVersion(3)
                .migration { realm, oldVersion, newVersion ->
                    val schema = realm.schema
                    var version = oldVersion
                    if (version == 1L) {
                        schema.get("Template")
                                ?.addField("recordMemo", String::class.java)
                                ?.addField("recordMemoSelection", Int::class.java)
                        version++
                    }

                    if (version == 2L) {
                        schema.create("SearchFilter")
                                ?.addField("id", String::class.java, FieldAttribute.PRIMARY_KEY)
                                ?.addField("filter", String::class.java)
                                ?.addField("order", Int::class.java)
                        version++
                    }
                }.build()
        Realm.setDefaultConfiguration(config)
    }
}