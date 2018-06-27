package com.hellowo.chating.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Intent
import com.hellowo.chating.ID
import com.hellowo.chating.TIME
import com.hellowo.chating.calendar.TimeObject
import java.util.*

class TimeObjectViewModel : ViewModel() {
    val loadingLiveData = MutableLiveData<Boolean>()
    val editingTimeObjectLiveData = MutableLiveData<TimeObject>()
    var originalTimeObject: TimeObject? = null

    init {
    }

    fun init(intent: Intent?) {
        intent?.let {
            it.getStringExtra(ID)?.let {

            }

            if(originalTimeObject == null) {
                editingTimeObjectLiveData.value = TimeObject()
            }else {

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

    fun save() {

    }
}