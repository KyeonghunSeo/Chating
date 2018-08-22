package com.hellowo.chating.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hellowo.chating.USER_URL
import com.hellowo.chating.calendar.model.TimeObject
import io.realm.*

class MainViewModel : ViewModel() {
    val realm = Realm.getDefaultInstance()
    val loading = MutableLiveData<Boolean>()
    val targetTimeObject = MutableLiveData<TimeObject?>()

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