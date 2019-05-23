package com.ayaan.twelvepages.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.content.Intent
import com.ayaan.twelvepages.ID
import com.ayaan.twelvepages.TIME
import com.ayaan.twelvepages.model.Record
import io.realm.Realm
import java.util.*

class TimeObjectViewModel : ViewModel() {
    val loadingLiveData = MutableLiveData<Boolean>()
    val editingTimeObjectLiveData = MutableLiveData<Record>()

    init {
    }

    fun init(intent: Intent?) {
        intent?.let {
            it.getStringExtra(ID)?.let {

            }

            if(editingTimeObjectLiveData.value == null) {
                editingTimeObjectLiveData.value = Record()
            }

            if(it.hasExtra(TIME)) {
                val time = it.getLongExtra(TIME, 0)
                editingTimeObjectLiveData.value?.let {
                    it.dtStart = time
                    it.dtEnd = time
                    it.timeZone = TimeZone.getDefault().id
                    editingTimeObjectLiveData.value = it
                }
            }
        }
    }

    fun setTitle(title: String) {
        editingTimeObjectLiveData.value?.title = title
    }

    fun save() {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransactionAsync{ realm ->
                editingTimeObjectLiveData.value?.let { timeObject ->
                    if(timeObject.id == null) {
                        timeObject.id = UUID.randomUUID().toString()
                        timeObject.dtUpdated = System.currentTimeMillis()
                        realm.insert(timeObject)
                    }
                }
            }
        }
    }
}