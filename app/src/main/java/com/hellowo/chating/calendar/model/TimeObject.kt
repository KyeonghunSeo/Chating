package com.hellowo.chating.calendar.model

import android.graphics.Color
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.text.DateFormat
import java.util.*

open class TimeObject(@PrimaryKey var id: String? = null,
                      var type: Int = 0,
                      var style: Int = 0,
                      var title: String? = null,
                      var color: Int = Color.TRANSPARENT,
                      var location: String? = null,
                      var description: String? = null,
                      var allday: Boolean = false,
                      var dtStart: Long = Long.MIN_VALUE,
                      var dtEnd: Long = Long.MIN_VALUE,
                      var dtUpdated: Long = Long.MIN_VALUE,
                      var timeZone: String? = null): RealmObject() {

    enum class Type {
        EVENT, TODO, MEMO, STAMP, IMAGE, EFFACT
    }

    enum class Style {
        DEFAULT, SHORT, LONG, INDICATOR
    }

    enum class Formula {
        BACKGROUND, FILL, BOTTOM, OVERLAY
    }

    enum class ViewLevel(val priority: Int) {
        IMPORTANT(0), NORMAL(1), PROJECT(2), STAMP(3), JOURNAL(4), ROUGH(5), BACKGROUND(-1)
    }

    fun getViewLevelPriority(): Int = when(Type.values()[type]) {
        Type.EVENT -> {
            when(Style.values()[style]) {
                Style.LONG -> ViewLevel.ROUGH.priority
                else -> ViewLevel.NORMAL.priority
            }
        }
        Type.TODO -> {
            when(Style.values()[style]) {
                Style.LONG -> ViewLevel.PROJECT.priority
                else -> ViewLevel.NORMAL.priority
            }
        }
        Type.MEMO -> {
            when(Style.values()[style]) {
                Style.SHORT -> ViewLevel.JOURNAL.priority
                Style.LONG -> ViewLevel.JOURNAL.priority
                else -> ViewLevel.NORMAL.priority
            }
        }
        Type.STAMP -> ViewLevel.STAMP.priority
        Type.IMAGE -> ViewLevel.JOURNAL.priority
        else -> ViewLevel.NORMAL.priority
    }

    fun getFormula(): Formula = when(Type.values()[type]) {
        Type.EVENT -> {
            when(Style.values()[style]) {
                Style.SHORT -> Formula.BOTTOM
                else -> Formula.FILL
            }
        }
        Type.TODO -> {
            when(Style.values()[style]) {
                Style.LONG -> Formula.FILL
                else -> Formula.BOTTOM
            }
        }
        Type.MEMO -> {
            when(Style.values()[style]) {
                Style.SHORT -> Formula.FILL
                Style.LONG -> Formula.FILL
                else -> Formula.BOTTOM
            }
        }
        Type.STAMP -> Formula.FILL
        Type.IMAGE -> Formula.FILL
        else -> Formula.FILL
    }

    override fun toString(): String {
        return "TimeObject(title=$title,type=$type, style=$style, color=$color, location=$location, description=$description, allday=$allday, dtStart=${DateFormat.getDateTimeInstance().format(Date(dtStart))}, dtEnd=${DateFormat.getDateTimeInstance().format(Date(dtEnd))}, dtUpdated=$dtUpdated, timeZone=$timeZone)"
    }
}