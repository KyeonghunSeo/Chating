package com.hellowo.chating.calendar.model

import android.graphics.Color
import com.hellowo.chating.R
import io.realm.RealmList
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
                      var repeatId: String? = null,
                      var repeat: String? = null,
                      var count: Int = 0,
                      var dtUntil: Long = Long.MIN_VALUE,
                      var allday: Boolean = false,
                      var dtStart: Long = Long.MIN_VALUE,
                      var dtEnd: Long = Long.MIN_VALUE,
                      var dtCreated: Long = Long.MIN_VALUE,
                      var dtUpdated: Long = Long.MIN_VALUE,
                      var timeZone: String? = null,
                      var tags: RealmList<Tag>? = null,
                      var alarms: RealmList<Alarm>? = null,
                      var links: RealmList<Link>? = null): RealmObject() {

    enum class Type(val titleId: Int, val iconId: Int) {
        NOTE(R.string.note, R.drawable.ic_outline_note),
        EVENT(R.string.event, R.drawable.ic_outline_event),
        TASK(R.string.task, R.drawable.ic_outline_done_outline),
        STAMP(R.string.stamp, R.drawable.ic_outline_class),
        MONEY(R.string.money, R.drawable.ic_outline_monetization_on),
        DECORATION(R.string.decoration, R.drawable.ic_outline_star_border)
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
        Type.TASK -> {
            when(Style.values()[style]) {
                Style.LONG -> ViewLevel.PROJECT.priority
                else -> ViewLevel.NORMAL.priority
            }
        }
        Type.MONEY -> {
            when(Style.values()[style]) {
                Style.SHORT -> ViewLevel.JOURNAL.priority
                Style.LONG -> ViewLevel.JOURNAL.priority
                else -> ViewLevel.NORMAL.priority
            }
        }
        Type.STAMP -> ViewLevel.STAMP.priority
        Type.DECORATION -> ViewLevel.JOURNAL.priority
        else -> ViewLevel.NORMAL.priority
    }

    fun getFormula(): Formula = when(Type.values()[type]) {
        Type.EVENT -> {
            when(Style.values()[style]) {
                Style.SHORT -> Formula.BOTTOM
                else -> Formula.FILL
            }
        }
        Type.TASK -> {
            when(Style.values()[style]) {
                Style.LONG -> Formula.FILL
                else -> Formula.BOTTOM
            }
        }
        Type.MONEY -> {
            when(Style.values()[style]) {
                Style.SHORT -> Formula.FILL
                Style.LONG -> Formula.FILL
                else -> Formula.BOTTOM
            }
        }
        Type.STAMP -> Formula.FILL
        Type.DECORATION -> Formula.FILL
        else -> Formula.FILL
    }

    override fun toString(): String {
        return "TimeObject(title=$title,type=$type, style=$style, color=$color, location=$location, description=$description, allday=$allday, dtStart=${DateFormat.getDateTimeInstance().format(Date(dtStart))}, dtEnd=${DateFormat.getDateTimeInstance().format(Date(dtEnd))}, dtUpdated=$dtUpdated, timeZone=$timeZone)"
    }
}