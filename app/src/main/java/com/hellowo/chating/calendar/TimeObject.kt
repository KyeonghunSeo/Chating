package com.hellowo.chating.calendar

import android.graphics.Color
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.text.DateFormat

open class TimeObject(@PrimaryKey var id: String? = null,
                      var type: Int = 0,
                      var title: String? = null,
                      var color: Int = Color.BLACK,
                      var location: String? = null,
                      var description: String? = null,
                      var dtStart: Long = Long.MIN_VALUE,
                      var dtEnd: Long = Long.MIN_VALUE,
                      var dtUpdated: Long = Long.MIN_VALUE,
                      var timeZone: String? = null): RealmObject() {
    override fun toString(): String {
        return "TimeObject(id=$id, type=$type, title=$title, color=$color, location=$location, description=$description, " +
                "dtStart=${DateFormat.getDateTimeInstance().format(dtStart)}, dtEnd=${DateFormat.getDateTimeInstance().format(dtEnd)}, " +
                "dtUpdated=${DateFormat.getDateTimeInstance().format(dtUpdated)}, timeZone=$timeZone)"
    }
}