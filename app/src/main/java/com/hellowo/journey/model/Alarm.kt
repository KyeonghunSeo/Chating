package com.hellowo.journey.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Alarm(@PrimaryKey var id: String? = null,
                 var dtAlarm: Long = Long.MIN_VALUE,
                 var offset: Long = 0,
                 var action: Int = 0): RealmObject() {
    override fun toString(): String {
        return "Alarm(id=$id, dtAlarm=$dtAlarm, offset=$offset, action=$action)"
    }
}