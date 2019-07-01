package com.ayaan.twelvepages.model

import com.ayaan.twelvepages.DAY_MILL
import com.ayaan.twelvepages.getCalendarTime0
import com.ayaan.twelvepages.tempCal
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Alarm(@PrimaryKey var id: String? = null,
                 var dtAlarm: Long = Long.MIN_VALUE,
                 var dayOffset: Int = 0,
                 var time: Long = 0,
                 var action: Int = 0): RealmObject() {
    constructor(alarm: Alarm) : this(alarm.id, alarm.dtAlarm, alarm.dayOffset, alarm.time, alarm.action)

    fun set(dayOffset: Int, time: Long, dtStart: Long, action: Int){
        this.dayOffset = dayOffset
        this.time = time
        this.action = action
        dtAlarm = getCalendarTime0(dtStart) + dayOffset * DAY_MILL + time
    }

    override fun toString(): String {
        return "Alarm(id=$id, dtAlarm=$dtAlarm, dayOffset=$dayOffset, time=$time, action=$action)"
    }
}