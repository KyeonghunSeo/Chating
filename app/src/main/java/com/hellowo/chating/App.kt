package com.hellowo.chating

import android.app.Application
import io.realm.Realm
import io.realm.SyncConfiguration

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        Realm.setDefaultConfiguration(SyncConfiguration.automatic())
    }
}