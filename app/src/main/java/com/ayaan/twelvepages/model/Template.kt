package com.ayaan.twelvepages.model

import com.ayaan.twelvepages.App
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.alarm.AlarmManager
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter.Formula.SINGLE_TEXT
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Template(@PrimaryKey var id: String? = null,
                    var symbol: String? = null,
                    var title: String? = null,
                    var type: Int = 0,
                    var colorKey: Int = 0,
                    var style: Int = SINGLE_TEXT.shapes[0].ordinal * 100 + SINGLE_TEXT.ordinal,
                    var recordTitle: String? = null,
                    var recordTitleSelection: Int = 0,
                    var recordMemo: String? = null,
                    var recordMemoSelection: Int = 0,
                    var alarmDayOffset: Int = Int.MIN_VALUE,
                    var alarmTime: Long = AlarmManager.defaultAlarmTime[0],
                    var folder: Folder? = null,
                    var tags: RealmList<Tag> = RealmList(),
                    var order: Int = 0): RealmObject() {

    companion object {
        const val scheduleFlag = 0b1
        const val titleFlag = 0b10
        const val memoFlag = 0b100
        const val timeFlag = 0b1000
        const val checkBoxFlag = 0b10000
    }

    init {
        setTitle()
    }

    override fun equals(other: Any?): Boolean {
        other as Template

        if (id != other.id) return false
        if (symbol != other.symbol) return false
        if (title != other.title) return false
        if (type != other.type) return false
        if (colorKey != other.colorKey) return false
        if (style != other.style) return false
        if (recordTitle != other.recordTitle) return false
        if (recordTitleSelection != other.recordTitleSelection) return false
        if (recordMemo != other.recordMemo) return false
        if (recordMemoSelection != other.recordMemoSelection) return false
        if (alarmDayOffset != other.alarmDayOffset) return false
        if (alarmTime != other.alarmTime) return false
        if (folder != other.folder) return false
        if (tags.joinToString(",") != other.tags.joinToString(",")) return false

        return true
    }

    fun copy(data: Template) {
        id = data.id
        symbol = data.symbol
        title = data.title
        type = data.type
        colorKey = data.colorKey
        style = data.style
        recordTitle = data.recordTitle
        recordTitleSelection = data.recordTitleSelection
        recordMemo = data.recordMemo
        recordMemoSelection = data.recordMemoSelection
        alarmDayOffset = data.alarmDayOffset
        alarmTime = data.alarmTime
        tags.clear()
        data.tags.forEach { tags.add(Tag(it)) }
        data.folder?.let { folder = Folder(it) }
        order = data.order
    }

    fun isSetTitle() = type and titleFlag == titleFlag
    fun setTitle() { type = type or titleFlag }
    fun clearTitle() {
        type = type and titleFlag.inv()
        recordTitle = null
        recordTitleSelection = 0
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
    fun clearMemo() {
        type = type and memoFlag.inv()
        recordMemo = null
        recordMemoSelection = 0
    }

    fun getAlarmText(): String {
        return if(alarmDayOffset != Int.MIN_VALUE) {
            AlarmManager.getAlarmText(alarmDayOffset, alarmTime)
        }else {
            App.context.getString(R.string.unuse)
        }
    }

    override fun toString(): String {
        return "Template(id=$id, symbol=$symbol, title=$title, type=$type, colorKey=$colorKey, style=$style, recordTitle=$recordTitle, recordTitleSelection=$recordTitleSelection, recordMemo=$recordMemo, recordMemoSelection=$recordMemoSelection, alarmDayOffset=$alarmDayOffset, alarmTime=$alarmTime, folder=$folder, tags=$tags, order=$order)"
    }

}