package com.hellowo.chating.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Log
import com.hellowo.chating.USER_URL
import com.hellowo.chating.model.ChatRoom
import io.realm.*
import java.util.*

class MainViewModel : ViewModel() {
    val realm = Realm.getDefaultInstance()
    var loading = MutableLiveData<Boolean>()

    init {
        SyncUser.current()?.let {
            val config = SyncConfiguration.Builder(it, USER_URL).partialRealm().build()
            Realm.setDefaultConfiguration(config)
        }
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }
}