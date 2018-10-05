package com.hellowo.journey.calendar.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Alarm(@PrimaryKey var id: String? = null,
                 var dtAlarm: Long = Long.MIN_VALUE,
                 var action: Int = 0): RealmObject()