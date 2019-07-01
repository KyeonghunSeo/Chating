package com.ayaan.twelvepages.model

import com.ayaan.twelvepages.App
import com.ayaan.twelvepages.AppDateFormat
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.alarm.AlarmManager
import com.ayaan.twelvepages.tempCal
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter.Formula.STACK
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Template(@PrimaryKey var id: String? = null,
                    var title: String? = null,
                    var type: Int = 0,
                    var colorKey: Int = 0,
                    var style: Int = STACK.shapes[0].ordinal * 100 + STACK.ordinal,
                    var recordTitle: String? = null,
                    var recordTitleSelection: Int = 0,
                    var alarmDayOffset: Int = Int.MIN_VALUE,
                    var alarmTime: Long = AlarmManager.defaultAlarmTime[0],
                    var folder: Folder? = null,
                    var tags: RealmList<Tag> = RealmList(),
                    var order: Int = 0): RealmObject() {

    companion object {
        const val scheduleFlag = 0b1
        const val timeFlag = 0b10
        const val checkBoxFlag = 0b100
        const val memoFlag = 0b1000
    }

    override fun equals(other: Any?): Boolean {
        other as Template

        if (id != other.id) return false
        if (title != other.title) return false
        if (type != other.type) return false
        if (colorKey != other.colorKey) return false
        if (style != other.style) return false
        if (recordTitle != other.recordTitle) return false
        if (recordTitleSelection != other.recordTitleSelection) return false
        if (alarmDayOffset != other.alarmDayOffset) return false
        if (alarmTime != other.alarmTime) return false
        if (folder != other.folder) return false
        if (tags.joinToString(",") != other.tags.joinToString(",")) return false

        return true
    }

    fun copy(data: Template) {
        id = data.id
        title = data.title
        type = data.type
        colorKey = data.colorKey
        style = data.style
        recordTitle = data.recordTitle
        recordTitleSelection = data.recordTitleSelection
        alarmDayOffset = data.alarmDayOffset
        alarmTime = data.alarmTime
        tags.clear()
        data.tags.forEach { tags.add(Tag(it)) }
        data.folder?.let { folder = Folder(it) }
        order = data.order
    }

    fun isScheduled() = type and scheduleFlag == scheduleFlag
    fun setSchedule() { type = type or scheduleFlag }
    fun clearSchdule() { type = type and scheduleFlag.inv() }

    fun isSetTime() = type and timeFlag == timeFlag
    fun setTime() { type = type or timeFlag }
    fun clearTime() { type = type and timeFlag.inv() }

    fun isSetCheckBox() = type and checkBoxFlag == checkBoxFlag
    fun setCheckBox() { type = type or checkBoxFlag }
    fun clearCheckBox() { type = type and checkBoxFlag.inv() }

    fun isSetMemo() = type and memoFlag == memoFlag
    fun setMemo() { type = type or memoFlag }
    fun clearMemo() { type = type and memoFlag.inv() }

    fun getAlarmText(): String {
        return if(alarmDayOffset != Int.MIN_VALUE) {
            "!!!"
        }else {
            App.context.getString(R.string.unuse)
        }
    }

    override fun toString(): String {
        return "Template(id=$id, title=$title, type=$type, colorKey=$colorKey, style=$style, folder=$folder, tags=$tags, order=$order)"
    }

}