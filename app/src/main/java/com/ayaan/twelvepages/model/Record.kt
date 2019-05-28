package com.ayaan.twelvepages.model

import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Ignore
import java.util.*


open class Record(@PrimaryKey var id: String? = null,
                  var type: Int = 0,
                  var style: Int = RecordCalendarAdapter.Formula.STACK.ordinal,
                  var title: String? = null,
                  var colorKey: Int = 0,
                  var location: String? = null,
                  var description: String? = null,
                  var repeat: String? = null,
                  var dtUntil: Long = Long.MIN_VALUE,
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
                  var ordering: Int = Int.MIN_VALUE,
                  var folder: Folder? = null): RealmObject() {

    companion object {
        const val scheduleFlag = 0b1
        const val timeFlag = 0b10
        const val checkBoxFlag = 0b100
    }

    @Ignore var repeatKey: String? = null

    fun setFormula(formula: Int) {
        style = formula
    }

    fun getFormula() = style % 100

    fun setStyleShape(shape: Int) {
        style = style % 100 + shape * 100
    }

    fun getDuration() = dtEnd - dtStart

    fun setDate(time: Long) {
        setDateTime(isSetTime(), time, time + getDuration())
    }

    fun setDateTime(isSetTime: Boolean, s: Calendar, e: Calendar) {
        if(isSetTime) {
            setDateTime(isSetTime, s.timeInMillis, e.timeInMillis)
        }else {
            setDateTime(isSetTime, getCalendarTime0(s), getCalendarTime0(e))
        }
    }

    fun setDateTime(isSetTime: Boolean, s: Long, e: Long) {
        if(isSetTime) {
            setTime()
        }else {
            clearTime()
        }
        dtStart = s
        dtEnd = e
        alarms.forEach {
            it.dtAlarm = dtStart + it.offset
        }
    }

    fun makeCopyObject(): Record{
        val o = Record()
        o.copy(this)
        return o
    }

    fun getColor() : Int = AppTheme.getColor(colorKey)

    fun copy(data: Record) {
        id = data.id
        type = data.type
        style = data.style
        title = data.title
        colorKey = data.colorKey
        location = data.location
        description = data.description
        repeat = data.repeat
        dtUntil = data.dtUntil
        dtStart = data.dtStart
        dtEnd = data.dtEnd
        dtDone = data.dtDone
        dtCreated = data.dtCreated
        dtUpdated = data.dtUpdated
        timeZone = data.timeZone
        exDates.clear()
        exDates.addAll(data.exDates)
        tags.clear()
        data.tags.forEach { tags.add(Tag(it)) }
        alarms.clear()
        data.alarms.forEach { alarms.add(Alarm(it.id, it.dtAlarm, it.offset, it.action)) }
        links.clear()
        data.links.forEach { links.add(Link(it.id, it.type, it.title, it.properties)) }
        latitude = data.latitude
        longitude = data.longitude
        ordering = data.ordering
        data.folder?.let { folder = Folder(it) }
        repeatKey = data.repeatKey
    }

    fun clearRepeat() {
        repeat = null
        dtUntil = Long.MIN_VALUE
        exDates.clear()
    }

    override fun toString(): String {
        return "Record(id=$id, type=$type, style=$style, title=$title, colorKey=$colorKey, location=$location, " +
                "description=$description, repeat=$repeat, dtUntil=$dtUntil, dtStart=$dtStart, dtEnd=$dtEnd, " +
                "dtDone=$dtDone, dtCreated=$dtCreated, dtUpdated=$dtUpdated, timeZone=$timeZone, " +
                "exDates=${exDates.joinToString(",")}, tags=${tags.joinToString(",")}, " +
                "alarms=${alarms.joinToString(",")}, links=${links.joinToString(",")}, " +
                "latitude=$latitude, longitude=$longitude, folder=${folder.toString()})"
    }

    override fun equals(other: Any?): Boolean { // 리스트 업데이트 비교시 사용
        if (other is Record) {
            return id == other.id
                    && type == other.type
                    && style == other.style
                    && title == other.title
                    && colorKey == other.colorKey
                    && location == other.location
                    && description == other.description
                    && repeat == other.repeat
                    && dtUntil == other.dtUntil
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

    fun isScheduled() = type and scheduleFlag == scheduleFlag
    fun setSchedule() { type = type or scheduleFlag }
    fun clearSchdule() {
        type = type and scheduleFlag.inv()
        setDateTime(isSetTime(), dtStart, dtStart)
    }

    fun isSetTime() = type and timeFlag == timeFlag
    fun setTime() { type = type or timeFlag }
    fun clearTime() { type = type and timeFlag.inv() }

    fun isSetCheckBox() = type and checkBoxFlag == checkBoxFlag
    fun setCheckBox() { type = type or checkBoxFlag }
    fun clearCheckBox() {
        type = type and checkBoxFlag.inv()
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

    fun getTitleInCalendar() = if(!title.isNullOrBlank())
        title?.replace(System.getProperty("line.separator"), " ")
    else App.context.getString(R.string.untitle)
}