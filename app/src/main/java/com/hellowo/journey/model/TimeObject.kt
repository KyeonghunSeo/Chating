package com.hellowo.journey.model

import android.graphics.Color
import com.hellowo.journey.R
import com.hellowo.journey.getCalendarTime0
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*


open class TimeObject(@PrimaryKey var id: String? = null,
                      var type: Int = 0,
                      var style: Int = 0,
                      var title: String? = null,
                      var color: Int = Color.BLACK,
                      var fontColor: Int = Color.WHITE,
                      var location: String? = null,
                      var description: String? = null,
                      var repeat: String? = null,
                      var count: Int = 0,
                      var dtUntil: Long = Long.MIN_VALUE,
                      var allday: Boolean = true,
                      var dtStart: Long = Long.MIN_VALUE,
                      var dtEnd: Long = Long.MIN_VALUE,
                      var dtDone: Long = Long.MIN_VALUE,
                      var dtCreated: Long = Long.MIN_VALUE,
                      var dtUpdated: Long = Long.MIN_VALUE,
                      var timeZone: String? = null,
                      var exDates: RealmList<String> = RealmList(),
                      var tags: RealmList<Tag> = RealmList(),
                      var alarms: RealmList<Alarm> = RealmList(),
                      var links: RealmList<Link> = RealmList(),
                      var latitude: Double = Double.MIN_VALUE,
                      var longitude: Double = Double.MIN_VALUE,
                      var inCalendar: Boolean = true,
                      var ordering: Int = Int.MIN_VALUE,
                      var folder: Folder? = null): RealmObject() {

    enum class Type(val titleId: Int, val iconId: Int, val enableLongTerm: Boolean) {
        EVENT(R.string.event, R.drawable.ic_baseline_calendar_today_24px, true),
        TASK(R.string.task, R.drawable.ic_baseline_done_24px, false),
        NOTE(R.string.note, R.drawable.ic_baseline_description_24px, false),
        STAMP(R.string.stamp, R.drawable.ic_outline_class, false),
        TERM(R.string.term, R.drawable.ic_outline_class, true),
        MONEY(R.string.money, R.drawable.ic_outline_monetization_on, false),
        DRAWING(R.string.drawing, R.drawable.ic_baseline_favorite_24px, false)
    }

    enum class Style {
        DEFAULT, SHORT, LONG, INDICATOR
    }

    enum class Formula {
        BACKGROUND, TOP_STACK, TOP_FLOW, TOP_LINEAR, MID_FLOW, BOTTOM_LINEAR, BOTTOM_STACK, OVERLAY
    }

    fun getFormula(): Formula = when(Type.values()[type]) {
        Type.EVENT -> {
            when(Style.values()[style]) {
                Style.SHORT -> Formula.TOP_LINEAR
                else -> Formula.TOP_STACK
            }
        }
        Type.TASK -> {
            when(Style.values()[style]) {
                Style.LONG -> Formula.TOP_STACK
                else -> Formula.TOP_LINEAR
            }
        }
        Type.NOTE -> Formula.TOP_LINEAR
        Type.STAMP -> Formula.TOP_FLOW
        Type.MONEY -> Formula.MID_FLOW
        Type.TERM -> Formula.BOTTOM_STACK
        else -> Formula.TOP_STACK
    }

    fun setDateTime(a: Boolean, s: Calendar, e: Calendar) {
        if(a) {
            setDateTime(a, getCalendarTime0(s), getCalendarTime0(e))
        }else {
            setDateTime(a, s.timeInMillis, e.timeInMillis)
        }
    }

    fun setDateTime(a: Boolean, s: Long, e: Long) {
        allday = a
        dtStart = s
        dtEnd = e
        alarms.forEach {
            it.dtAlarm = dtStart + it.offset
        }
    }

    fun makeCopyObject(): TimeObject{
        val o = TimeObject()
        o.copy(this)
        return o
    }

    fun copy(data: TimeObject) {
        id = data.id
        type = data.type
        style = data.style
        title = data.title
        color = data.color
        fontColor = data.fontColor
        location = data.location
        description = data.description
        repeat = data.repeat
        count = data.count
        dtUntil = data.dtUntil
        allday = data.allday
        dtStart = data.dtStart
        dtEnd = data.dtEnd
        dtDone = data.dtDone
        dtCreated = data.dtCreated
        dtUpdated = data.dtUpdated
        timeZone = data.timeZone
        exDates.clear()
        exDates.addAll(data.exDates)
        tags.clear()
        data.tags.forEach {

        }
        alarms.clear()
        data.alarms.forEach {
            alarms.add(Alarm(it.id, it.dtAlarm, it.offset, it.action))
        }
        links.clear()
        data.links.forEach {

        }
        latitude = data.latitude
        longitude = data.longitude
        inCalendar = data.inCalendar
        ordering = data.ordering
        folder = data.folder
    }

    fun clearRepeat() {
        repeat = null
        dtUntil = Long.MIN_VALUE
        exDates.clear()
    }

    override fun toString(): String {
        return "TimeObject(id=$id, type=$type, style=$style, title=$title, color=$color, fontColor=$fontColor, location=$location, description=$description, repeat=$repeat, count=$count, dtUntil=$dtUntil, allday=$allday, dtStart=$dtStart, dtEnd=$dtEnd, dtDone=$dtDone, dtCreated=$dtCreated, dtUpdated=$dtUpdated, timeZone=$timeZone, exDates=${exDates.joinToString(",")}, tags=${tags.joinToString(",")}, alarms=${alarms.joinToString(",")}, links=${links.joinToString(",")}, latitude=$latitude, longitude=$longitude, inCalendar=$inCalendar)"
    }

    override fun equals(other: Any?): Boolean { // 리스트 업데이트 비교시 사용
        if (other is TimeObject) {
            return id == other.id
                    && type == other.type
                    && style == other.style
                    && title == other.title
                    && color == other.color
                    && fontColor == other.fontColor
                    && location == other.location
                    && description == other.description
                    && repeat == other.repeat
                    && count == other.count
                    && dtUntil == other.dtUntil
                    && allday == other.allday
                    && dtStart == other.dtStart
                    && dtEnd == other.dtEnd
                    && dtDone == other.dtDone
                    && timeZone == other.timeZone
                    && exDates.joinToString(",") == other.exDates.joinToString(",")
                    && tags.joinToString(",") == other.tags.joinToString(",")
                    && alarms.joinToString(",") == other.alarms.joinToString(",")
                    && links.joinToString(",") == other.links.joinToString(",")
                    && latitude == other.latitude
                    && longitude == other.longitude
                    && inCalendar == other.inCalendar
        }
        return false
    }

    fun isDone(): Boolean = dtDone != Long.MIN_VALUE


}