package com.hellowo.chating.calendar

import android.graphics.Color
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class TimeObject(@PrimaryKey var id: String? = null,
                      var type: Int = 0,
                      var title: String? = null,
                      var color: Int = Color.BLACK,
                      var location: String? = null,
                      var description: String? = null,
                      var dtStart: Long = Long.MIN_VALUE,
                      var dtEnd: Long = Long.MIN_VALUE,
                      var dtUpdated: Long = Long.MIN_VALUE,
                      var timeZone: String? = null): RealmObject()