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
        NOTE(R.string.note, R.drawable.ic_baseline_description_24px),
        EVENT(R.string.event, R.drawable.ic_baseline_calendar_today_24px),
        TASK(R.string.task, R.drawable.ic_baseline_done_24px),
        STAMP(R.string.stamp, R.drawable.ic_outline_class),
        MONEY(R.string.money, R.drawable.ic_outline_monetization_on),
        DECORATION(R.string.decoration, R.drawable.ic_baseline_favorite_24px)
    }

    enum class Style {
        DEFAULT, SHORT, LONG, INDICATOR
    }

    enum class Formula {
        BACKGROUND, TOPSTACK, LINEAR, BOTTOMSTACK, OVERLAY
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
        Type.NOTE -> Formula.LINEAR
        Type.EVENT -> {
            when(Style.values()[style]) {
                Style.SHORT -> Formula.LINEAR
                else -> Formula.TOPSTACK
            }
        }
        Type.TASK -> {
            when(Style.values()[style]) {
                Style.LONG -> Formula.TOPSTACK
                else -> Formula.LINEAR
            }
        }
        Type.MONEY -> {
            when(Style.values()[style]) {
                Style.SHORT -> Formula.TOPSTACK
                Style.LONG -> Formula.TOPSTACK
                else -> Formula.LINEAR
            }
        }
        Type.STAMP -> Formula.TOPSTACK
        Type.DECORATION -> Formula.TOPSTACK
        else -> Formula.TOPSTACK
    }

    override fun toString(): String {
        return "TimeObject(title=$title,type=$type, style=$style, color=$color, location=$location, description=$description, allday=$allday, dtStart=${DateFormat.getDateTimeInstance().format(Date(dtStart))}, dtEnd=${DateFormat.getDateTimeInstance().format(Date(dtEnd))}, dtUpdated=$dtUpdated, timeZone=$timeZone)"
    }

    
}