package com.hellowo.chating.calendar.model

import android.graphics.Color
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class Alarm(@PrimaryKey var id: String? = null,
                 var dtAlarm: Long = Long.MIN_VALUE,
                 var action: Int = 0): RealmObject()