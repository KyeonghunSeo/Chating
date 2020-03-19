package com.ayaan.twelvepages.model

import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.alarm.AlarmManager
import com.ayaan.twelvepages.ui.view.RecordView
import io.realm.RealmList
import io.realm.RealmObject
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter.Formula.SINGLE_TEXT
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.StickerManager
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Ignore
import java.util.*


open class Record(@PrimaryKey var id: String? = null,
                  var type: Int = 0,
                  var style: Int = SINGLE_TEXT.shapes[Random().nextInt(SINGLE_TEXT.shapes.size)].ordinal * 100 + SINGLE_TEXT.ordinal,
                  var symbol: String? = null,
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
        alarms.forEach { it.set(it.dayOffset, it.time, dtStart, 0) }
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
        symbol = data.symbol
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
        return "Record(id=$id, type=$type, style=$style, symbol=$symbol, title=$title, isSetTime=$isSetTime, isSetCheckBox=$isSetCheckBox, " +
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
                    && symbol == other.symbol
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
    fun getDueText(time: Long): String {
        if(isDone()) {
            return String.format(str(R.string.done_when), "${AppDateFormat.mde.format(Date(dtDone))} ${AppDateFormat.time.format(Date(dtDone))}")
        }else {
            val diffDate = getDiffDate(dtStart, time)
            return if(isSetTime && diffDate == 0) {
                val diff = dtStart - time
                val diffMin = Math.abs(diff / MIN_MILL)
                val diffHour = Math.abs(diff / HOUR_MILL)
                val diffMinHour = diffMin % 60
                if(diff < 0) {
                    if(diffMin < 60) {
                        String.format(str(R.string.overdue), String.format(str(R.string.some_min), diffMin.toString()))
                    }else {
                        if(diffMinHour == 0L) {
                            String.format(str(R.string.overdue), String.format(str(R.string.some_hour), diffHour.toString()))
                        }else {
                            String.format(str(R.string.overdue), "${String.format(str(R.string.some_hour), diffHour.toString())} " +
                                    String.format(str(R.string.some_min), diffMinHour.toString()))
                        }
                    }
                }else {
                    if(diffMin < 60) {
                        String.format(str(R.string.due), String.format(str(R.string.some_min), diffMin.toString()))
                    }else {
                        if(diffMinHour == 0L) {
                            String.format(str(R.string.due), String.format(str(R.string.some_hour), diffHour.toString()))
                        }else {
                            String.format(str(R.string.due), "${String.format(str(R.string.some_hour), diffHour.toString())} " +
                                    String.format(str(R.string.some_min), diffMinHour.toString()))
                        }
                    }
                }
            }else {
                when {
                    diffDate > 0 -> String.format(str(R.string.overdue), String.format(str(R.string.some_date), diffDate.toString()))
                    diffDate < 0 -> String.format(str(R.string.due), String.format(str(R.string.some_date), diffDate.toString()))
                    else -> str(R.string.today)
                }
            }
        }
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
    fun clearSticker() { links.firstOrNull{ it.type == Link.Type.STICKER.ordinal }?.let { links.remove(it) } }
    fun setSticker(sticker: StickerManager.Sticker) {
        if(isSetSticker()) {
            links.firstOrNull{ it.type == Link.Type.STICKER.ordinal }?.let {
                it.intParam0 = StickerManager.getStickerKey(sticker) }
        }else {
            links.add(Link(UUID.randomUUID().toString(), Link.Type.STICKER.ordinal,
                    intParam0 = StickerManager.getStickerKey(sticker)))
        }
    }
    fun getSticker(): StickerManager.Sticker? {
        links.firstOrNull{ it.type == Link.Type.STICKER.ordinal }?.let {
            return StickerManager.getSticker(it.intParam0)
        }
        return null
    }

    fun isSetCheckList(): Boolean = links.any { it.type == Link.Type.CHECKLIST.ordinal }
    fun clearCheckList() { links.firstOrNull{ it.type == Link.Type.CHECKLIST.ordinal }?.let { links.remove(it) } }
    fun setCheckList() {
        if(!isSetCheckList()) {
            links.add(Link(type = Link.Type.CHECKLIST.ordinal))
        }
    }
    fun getCheckList() = links.firstOrNull{ it.type == Link.Type.CHECKLIST.ordinal }

    fun isSetPercentage(): Boolean = links.any { it.type == Link.Type.PERCENTAGE.ordinal }
    fun clearPercentage() { links.firstOrNull{ it.type == Link.Type.PERCENTAGE.ordinal }?.let { links.remove(it) } }
    fun setPercentage() {
        if(!isSetPercentage()) {
            links.add(Link(type = Link.Type.PERCENTAGE.ordinal))
        }
    }

    fun isRepeat(): Boolean = !repeat.isNullOrEmpty()

    fun isLunarRepeat(): Boolean = repeat?.contains("lunar") == true

    fun isSetPhoto(): Boolean = links.any { it.type == Link.Type.IMAGE.ordinal }
    fun isSetLink(): Boolean = links.any { it.type == Link.Type.WEB.ordinal }

    fun getTitleInCalendar() = if(!title.isNullOrBlank())
        title?.replace(System.getProperty("line.separator") ?: "\n", " ") + if(!description.isNullOrBlank()) {
            "\n${description?.replace(System.getProperty("line.separator") ?: "\n", " ")}"
        }else ""
    else if(!description.isNullOrBlank())
        description?.replace(System.getProperty("line.separator") ?: "\n", " ")
    else
        ""

    fun getShortTilte(): String {
        getTitleInCalendar()?.let {
            return it.take(8) + if(it.length > 8) ".." else ""
        }
        return ""
    }

    fun setAlarm(dayOffset: Int, time: Long) {
        alarms.clear()
        alarms.add(Alarm(UUID.randomUUID().toString()).apply { set(dayOffset, time, dtStart, 0) })
    }
    fun getAlarm(): Alarm {
        if(alarms.isEmpty()) {
            setAlarm(0, 0)
        }
        return alarms[0]!!
    }
    fun getAlarmTimeOffset() : Long {
        return if(alarms.isEmpty()) {
            0
        }else {
            getAlarm().dtAlarm - dtStart
        }
    }
    fun removeAlarm() { alarms.clear() }
    fun isSetAlarm() = alarms.isNotEmpty()
    fun getAlarmText(): String = if(isSetAlarm()) AlarmManager.getAlarmText(this)
    else str(R.string.no_alarm)

    fun isDeleted() = dtCreated == -1L
    fun isScheduled() = dtStart != Long.MIN_VALUE
    fun isOsInstance() = id?.startsWith("osInstance::") == true
    fun getOsEventId() = id?.substring("osInstance::".length, id!!.length)?.toLong() ?: -1
    fun isSetLocation() = location != null
    fun isSetMainText() = description != null
    fun isSetTitle() = title != null
    fun isSetSymbol() = symbol != null

}