package com.hellowo.chating

import android.app.Application
import io.realm.Realm
import io.realm.SyncConfiguration

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AppRes.init(this)
        Realm.init(this)
    }
}