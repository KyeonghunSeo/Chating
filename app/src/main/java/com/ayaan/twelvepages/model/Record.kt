package com.ayaan.twelvepages.model

import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.alarm.AlarmManager
import com.ayaan.twelvepages.ui.view.RecordView
import io.realm.RealmList
import io.realm.RealmObject
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter.Formula.STACK
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.StickerManager
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Ignore
import java.util.*


open class Record(@PrimaryKey var id: String? = null,
                  var type: Int = 0,
                  var style: Int = STACK.shapes[Random().nextInt(STACK.shapes.size)].ordinal * 100 + STACK.ordinal,
                  var title: String? = null,
                  var isSetTime: Boolean = false,
                  var isSetCheckBox: Boolean = false,
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

    @Ignore var repeatKey: String? = null

    fun setFormula(formula: RecordCalendarAdapter.Formula) {
        style = getTextColred() + formula.shapes[0].ordinal * 100 + formula.ordinal
    }

    fun getFormula() = RecordCalendarAdapter.Formula.styleToFormula(style)

    fun setShape(shape: RecordView.Shape) {
        style = getTextColred() + shape.ordinal * 100 + getFormula().ordinal
    }

    fun getShape() = RecordView.Shape.styleToShape(style)

    fun isTextColored() = getTextColred() > 10000

    fun setTextColored(colored: Boolean) {
        style = if(colored) 10000 else 0 + getShape().ordinal * 100 + getFormula().ordinal
    }

    fun getTextColred() = style / 10000 * 10000

    fun getDuration() = dtEnd - dtStart

    fun setDate(time: Long) {
        setDateTime(isSetTime, time, time + getDuration())
    }

    fun setDateTime(s: Calendar, e: Calendar) {
        setDateTime(isSetTime, s, e)
    }

    fun setDateTime(isSetTime: Boolean, s: Calendar, e: Calendar) {
        if(isSetTime) {
            setDateTime(isSetTime, s.timeInMillis, e.timeInMillis)
        }else {
            setDateTime(isSetTime, getCalendarTime0(s), getCalendarTime23(e))
        }
    }

    fun setDateTime(t: Boolean, s: Long, e: Long) {
        isSetTime = t
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

    fun getColor() : Int = ColorManager.getColor(colorKey)

    fun copy(data: Record) {
        id = data.id
        type = data.type
        style = data.style
        title = data.title
        isSetTime = data.isSetTime
        isSetCheckBox = data.isSetCheckBox
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
        data.alarms.forEach { alarms.add(Alarm(it)) }
        links.clear()
        data.links.forEach { links.add(Link(it)) }
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
        return "Record(id=$id, type=$type, style=$style, title=$title, isSetTime=$isSetTime, isSetCheckBox=$isSetCheckBox, " +
                "colorKey=$colorKey, location=$location, description=$description, repeat=$repeat, " +
                "dtUntil=$dtUntil, dtStart=$dtStart, dtEnd=$dtEnd, " +
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
                    && isSetTime == other.isSetTime
                    && isSetCheckBox == other.isSetCheckBox
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

    fun isSetCountdown(): Boolean = links.any { it.type == Link.Type.COUNTDOWN.ordinal }
    fun clearCountdown() { links.first{ it.type == Link.Type.COUNTDOWN.ordinal }?.let { links.remove(it) } }
    fun setCountdown() {
        if(!isSetCountdown()) {
            links.add(Link(type = Link.Type.COUNTDOWN.ordinal))
        }
    }
    fun getCountdownText(time: Long): String {
        val str = StringBuilder("D")
        val diffDate = getDiffDate(dtStart, time)
        when {
            diffDate > 0 -> str.append("+$diffDate")
            diffDate < 0 -> str.append(diffDate.toString())
            else -> str.append("-day")
        }
        return str.toString()
    }

    fun isSetSticker(): Boolean = links.any { it.type == Link.Type.STICKER.ordinal }
    fun clearSticker() { links.first{ it.type == Link.Type.STICKER.ordinal }?.let { links.remove(it) } }
    fun setSticker() {
        if(!isSetSticker()) {
            links.add(Link(type = Link.Type.STICKER.ordinal))
        }
    }
    fun getSticker(): StickerManager.Sticker? {
        links.first{ it.type == Link.Type.STICKER.ordinal }?.let {
            return StickerManager.getSticker(it.intParam0)
        }
        return null
    }

    fun isSetCheckList(): Boolean = links.any { it.type == Link.Type.CHECKLIST.ordinal }
    fun clearCheckList() { links.first{ it.type == Link.Type.CHECKLIST.ordinal }?.let { links.remove(it) } }
    fun setCheckList() {
        if(!isSetCheckList()) {
            links.add(Link(type = Link.Type.CHECKLIST.ordinal))
        }
    }

    fun isSetPercentage(): Boolean = links.any { it.type == Link.Type.PERCENTAGE.ordinal }
    fun clearPercentage() { links.first{ it.type == Link.Type.PERCENTAGE.ordinal }?.let { links.remove(it) } }
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
    else App.context.getString(R.string.empty)

    fun setAlarm(offset: Long, dtAlarm: Long) {
        if(alarms.isEmpty()) {
            alarms.add(Alarm(UUID.randomUUID().toString()).apply {
                this.offset = offset
                if (offset != Long.MIN_VALUE) {
                    this.dtAlarm = dtStart + offset
                } else {
                    this.dtAlarm = dtAlarm
                }
            })
        }else {
            alarms.first()?.let {
                it.offset = offset
                if (offset != Long.MIN_VALUE) {
                    it.dtAlarm = dtStart + offset
                } else {
                    it.dtAlarm = dtAlarm
                }
            }
        }
    }
    fun getAlarmOffset(): Long {
        return if(alarms.isEmpty()) 0
        else alarms.first()!!.offset
    }
    fun getDtAlarm(): Long {
        return if(alarms.isEmpty()) Long.MIN_VALUE
        else alarms.first()!!.dtAlarm
    }
    fun removeAlarm() { alarms.clear() }
    fun isSetAlarm() = alarms.isNotEmpty()
    fun getAlarmText(): String = alarms.joinToString(", ") {
        AlarmManager.getOffsetText(it.offset) ?: AppDateFormat.dateTime.format(it.dtAlarm)
    }

    fun isDeleted() = dtCreated == -1L
    fun isScheduled() = dtStart != Long.MIN_VALUE
}