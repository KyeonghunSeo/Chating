package com.hellowo.journey.model

import android.graphics.Color
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.getCalendarTime0
import com.hellowo.journey.getDiffDate
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import com.hellowo.journey.model.TimeObject.Style.*
import io.realm.annotations.Ignore
import java.lang.Exception
import java.util.*


open class TimeObject(@PrimaryKey var id: String? = null,
                      var type: Int = 0,
                      var style: Int = 0,
                      var title: String? = null,
                      var colorKey: Int = -1,
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

    @Ignore var repeatKey: String? = null

    enum class Style {
        DEFAULT, RECT_STROKE, RECT_FILL, ROUND_STROKE, ROUND_FILL, CANDY, HATCHED, TOP_LINE, BOTTOM_LINE
    }

    enum class Formula {
        BACKGROUND, TOP_STACK, TOP_FLOW, TOP_LINEAR, BOTTOM_LINEAR, BOTTOM_STACK, OVERLAY
    }

    fun getFormula(): Formula {
        return if(inCalendar) {
            Formula.TOP_STACK
        }else {
            Formula.TOP_LINEAR
        }
    }

    fun getDuration() = dtEnd - dtStart

    fun setDate(time: Long) {
        setDateTime(allday, time, time + getDuration())
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

    fun getColor() : Int = AppTheme.getColor(colorKey)

    fun copy(data: TimeObject) {
        id = data.id
        type = data.type
        style = data.style
        title = data.title
        colorKey = data.colorKey
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
            tags.add(Tag(it.id))
        }
        alarms.clear()
        data.alarms.forEach {
            alarms.add(Alarm(it.id, it.dtAlarm, it.offset, it.action))
        }
        links.clear()
        data.links.forEach {
            links.add(Link(it.id, it.type, it.title, it.properties))
        }
        latitude = data.latitude
        longitude = data.longitude
        inCalendar = data.inCalendar
        ordering = data.ordering
        folder = data.folder
        repeatKey = data.repeatKey
    }

    fun clearRepeat() {
        repeat = null
        dtUntil = Long.MIN_VALUE
        exDates.clear()
    }

    override fun toString(): String {
        return "TimeObject(id=$id, type=$type, style=$style, title=$title, colorKey=$colorKey, location=$location, description=$description, repeat=$repeat, count=$count, dtUntil=$dtUntil, allday=$allday, dtStart=$dtStart, dtEnd=$dtEnd, dtDone=$dtDone, dtCreated=$dtCreated, dtUpdated=$dtUpdated, timeZone=$timeZone, exDates=${exDates.joinToString(",")}, tags=${tags.joinToString(",")}, alarms=${alarms.joinToString(",")}, links=${links.joinToString(",")}, latitude=$latitude, longitude=$longitude, inCalendar=$inCalendar)"
    }

    override fun equals(other: Any?): Boolean { // 리스트 업데이트 비교시 사용
        if (other is TimeObject) {
            return id == other.id
                    && type == other.type
                    && style == other.style
                    && title == other.title
                    && colorKey == other.colorKey
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
                    && folder == other.folder
        }
        return false
    }

    fun isDone(): Boolean = dtDone != Long.MIN_VALUE

    fun done() {
        dtDone = System.currentTimeMillis()
    }

    fun undone() {
        dtDone = Long.MIN_VALUE
    }

    fun isSetDday(): Boolean = links.any { it.type == Link.Type.DDAY.ordinal }

    fun clearDday() {
        links.first{ it.type == Link.Type.DDAY.ordinal }?.let { links.remove(it) }
    }

    fun setDday() {
        if(!isSetDday()) {
            links.add(Link(type = Link.Type.DDAY.ordinal))
        }
    }

    fun getDdayText(time: Long): String {
        val str = StringBuilder("D")
        val diffDate = getDiffDate(dtStart, time)
        when {
            diffDate > 0 -> str.append("+$diffDate")
            diffDate < 0 -> str.append(diffDate.toString())
            else -> str.append("-day")
        }
        return str.toString()
    }

    fun isScheduled() = type and 0b1 == 0b1

    fun setSchedule() {
        type = type or 0b1
    }

    fun clearSchdule() {
        type = type and 0b1.inv()
        setDateTime(allday, dtStart, dtStart)
    }

    fun isSetCheckBox() = type and 0b10 == 0b10

    fun setCheckBox() {
        type = type or 0b10
    }

    fun clearCheckBox() {
        type = type and 0b10.inv()
        undone()
    }

    fun isSetCheckList(): Boolean = links.any { it.type == Link.Type.CHECKLIST.ordinal }

    fun clearCheckList() {
        links.first{ it.type == Link.Type.CHECKLIST.ordinal }?.let { links.remove(it) }
    }

    fun setCheckList() {
        if(!isSetCheckList()) {
            links.add(Link(type = Link.Type.CHECKLIST.ordinal))
        }
    }

    fun isSetDeadLine(): Boolean = links.any { it.type == Link.Type.DEADLINE.ordinal }

    fun clearDeadLine() {
        links.first{ it.type == Link.Type.DEADLINE.ordinal }?.let { links.remove(it) }
    }

    fun setDeadLine() {
        if(!isSetDeadLine()) {
            links.add(Link(type = Link.Type.DEADLINE.ordinal))
        }
    }

    fun isSetPercentage(): Boolean = links.any { it.type == Link.Type.PERCENTAGE.ordinal }

    fun clearPercentage() {
        links.first{ it.type == Link.Type.PERCENTAGE.ordinal }?.let { links.remove(it) }
    }

    fun setPercentage() {
        if(!isSetPercentage()) {
            links.add(Link(type = Link.Type.PERCENTAGE.ordinal))
        }
    }

    fun isRepeat(): Boolean = !repeat.isNullOrEmpty()

    fun isLunarRepeat(): Boolean = repeat?.contains("lunar") == true

    fun isSetLink(): Boolean = links.any { it.type == Link.Type.IMAGE.ordinal }
}