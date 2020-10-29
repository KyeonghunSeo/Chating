package com.ayaan.twelvepages.manager

import android.annotation.SuppressLint
import android.app.Activity
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.alarm.AlarmManager
import com.ayaan.twelvepages.alarm.RegistedAlarm
import com.ayaan.twelvepages.model.*
import com.ayaan.twelvepages.ui.activity.MainActivity
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

@SuppressLint("StaticFieldLeak")
object RecordManager {
    var isChanged = false

    fun getRecordList(startTime: Long, endTime: Long, folder: Folder) : RealmResults<Record> {
        val realm = Realm.getDefaultInstance()
        val result = realm.where(Record::class.java)
                .beginGroup()
                .equalTo("folder.id", folder.id)
                .notEqualTo("dtCreated", -1L)
                .endGroup()
                .and()
                .beginGroup()
                .beginGroup()
                .greaterThanOrEqualTo("dtEnd", startTime)
                .lessThanOrEqualTo("dtStart", endTime)
                .endGroup()
                .or()
                .beginGroup()
                .isNotNull("repeat")
                .equalTo("dtUntil", Long.MIN_VALUE)
                .endGroup()
                .or()
                .beginGroup()
                .isNotNull("repeat")
                .notEqualTo("dtUntil", Long.MIN_VALUE)
                .greaterThan("dtUntil", startTime)
                .endGroup()
                .endGroup()
                .sort("dtStart", Sort.ASCENDING)
                .findAllAsync()
        realm.close()
        return result
    }

    fun getRecordList(folder: Folder) : RealmResults<Record> {
        val realm = Realm.getDefaultInstance()
        val result = realm.where(Record::class.java)
                .beginGroup()
                .equalTo("folder.id", folder.id)
                .greaterThan("dtCreated", 0)
                .endGroup()
                .sort("dtCreated", Sort.DESCENDING)
                .findAllAsync()
        realm.close()
        return result
    }

    fun getRecordList(query: String = "",
                      tags: ArrayList<String> = ArrayList(),
                      startTime: Long = Long.MIN_VALUE,
                      endTime: Long = Long.MIN_VALUE,
                      colorKey: Int = Int.MIN_VALUE,
                      isCheckBox: Boolean = false,
                      isPhoto: Boolean = false) : RealmResults<Record> {

        val realm = Realm.getDefaultInstance()
        val q = realm.where(Record::class.java)
                .beginGroup()
                .notEqualTo("dtCreated", -1L)
                .endGroup()

        if(startTime != Long.MIN_VALUE) {
            q.and()
                    .beginGroup()
                    .greaterThanOrEqualTo("dtEnd", startTime)
                    .lessThanOrEqualTo("dtStart", endTime)
                    .endGroup()
        }

        if(tags.isEmpty() || query.isNotEmpty()) {
            q.and()
                    .beginGroup()
                    .beginGroup()
                    .contains("title", query, Case.INSENSITIVE)
                    .endGroup()
                    .or()
                    .beginGroup()
                    .contains("description", query, Case.INSENSITIVE)
                    .endGroup()
                    .or()
                    .beginGroup()
                    .contains("location", query, Case.INSENSITIVE)
                    .endGroup()
                    .endGroup()
        }

        tags.forEach {
            q.and()
                    .beginGroup()
                    .equalTo("tags.title", it)
                    .endGroup()
        }

        if(colorKey != Int.MIN_VALUE) {
            q.and()
                    .beginGroup()
                    .equalTo("colorKey", colorKey)
                    .endGroup()
        }

        if(isCheckBox) {
            q.and()
                    .beginGroup()
                        .beginGroup()
                        .equalTo("isSetCheckBox", true)
                        .endGroup()
                        .or()
                        .beginGroup()
                        .equalTo("links.type", Link.Type.CHECKLIST.ordinal)
                        .endGroup()
                    .endGroup()
        }

        if(isPhoto) {
            q.and()
                    .beginGroup()
                    .equalTo("links.type", Link.Type.IMAGE.ordinal)
                    .endGroup()
        }

        val result = q.sort("dtStart", Sort.DESCENDING).findAllAsync()
        realm.close()
        return result
    }

    fun save(record: Record) {
        isChanged = true
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction{
            commonSave(realm, record)
            realm.insertOrUpdate(record)
        }
        realm.close()
    }

    fun save(records: List<Record>) {
        isChanged = true
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction{
            records.forEach {
                commonSave(realm, it)
                realm.insertOrUpdate(it)
            }
        }
        realm.close()
    }

    private fun commonSave(realm: Realm, record: Record) {
        if(record.id.isNullOrEmpty()) {
            record.id = UUID.randomUUID().toString()
            record.dtCreated = System.currentTimeMillis()
        }
        if(record.dtStart > record.dtEnd) {
            val t = record.dtStart
            record.dtStart = record.dtEnd
            record.dtEnd = t
        }
        AlarmManager.registRecordAlarm(realm, record)
        record.dtUpdated = System.currentTimeMillis()
    }

    fun done(record: Record) {
        isChanged = true
        val realm = Realm.getDefaultInstance()
        val id = record.id
        if(record.isRepeat()) {
            val ymdKey = AppDateFormat.ymdkey.format(Date(record.dtStart))
            realm.executeTransaction{
                realm.where(Record::class.java).equalTo("id", id).findFirst()?.let {
                    it.exDates.add(ymdKey)
                    commonSave(realm, it)

                    val new = Record()
                    new.copy(record)
                    new.id = UUID.randomUUID().toString()
                    new.clearRepeat()
                    if(new.dtDone == Long.MIN_VALUE) {
                        new.dtDone = System.currentTimeMillis()
                    }else {
                        new.dtDone = Long.MIN_VALUE
                    }
                    commonSave(realm, new)
                    realm.insertOrUpdate(new)
                }
            }
        }else {
            realm.executeTransaction{
                realm.where(Record::class.java).equalTo("id", id).findFirst()?.let {
                    if(it.dtDone == Long.MIN_VALUE) {
                        it.dtDone = System.currentTimeMillis()
                    }else {
                        it.dtDone = Long.MIN_VALUE
                    }
                    commonSave(realm, it)
                }
            }
        }
        realm.close()
    }

    fun deleteOnly(record: Record) {
        isChanged = true
        val realm = Realm.getDefaultInstance()
        record.id?.let { id ->
            record.repeatKey?.let { ymdKey ->
                realm.executeTransaction{ realm ->
                    realm.where(Record::class.java).equalTo("id", id).findFirst()?.let {
                        it.exDates.add(ymdKey)
                        commonSave(realm, it)
                    }
                }
            }
        }
        realm.close()
    }

    fun deleteAfter(record: Record) {
        isChanged = true
        val realm = Realm.getDefaultInstance()
        val id = record.id
        val cal = Calendar.getInstance()
        cal.timeInMillis = record.dtStart

        cal.add(Calendar.DATE, -1)
        realm.executeTransaction{
            realm.where(Record::class.java).equalTo("id", id).findFirst()?.let {
                val dtUntil = getCalendarTime23(cal)
                if(dtUntil < it.dtStart) { // 기한보다 작으면 완전히 제거
                    it.deleteFromRealm()
                }else {
                    it.dtUntil = getCalendarTime23(cal)
                    commonSave(realm, it)
                }
            }
        }
        realm.close()
    }

    fun delete(activity: Activity, record: Record, callback: Runnable) {
        isChanged = true
        if(record.isRepeat()) {
            RepeatManager.delete(activity, record, callback)
        }else {
            delete(record)
            callback.run()
        }
    }

    fun delete(record: Record) {
        isChanged = true
        val realm = Realm.getDefaultInstance()
        val id = record.id
        realm.executeTransaction{
            realm.where(Record::class.java).equalTo("id", id).findFirst()?.let {
                it.dtCreated = -1
                commonSave(realm, it)
            }
        }
        realm.close()
    }

    fun makeNewRecord(start: Long, end: Long): Record {
        return Record().apply {
            dtStart = start
            dtEnd = end
            timeZone = TimeZone.getDefault().id
            folder = MainActivity.getTargetFolder()
        }
    }

    fun getRecordById(id: String): Record? {
        val realm = Realm.getDefaultInstance()
        val result =  realm.where(Record::class.java)
                .equalTo("id", id)
                .findFirst()
        realm.close()
        return result
    }

    fun reorder(list: List<Record>) {
        isChanged = true
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction{
            list.forEachIndexed { index, timeObject ->
                realm.where(Record::class.java).equalTo("id", timeObject.id).findFirst()?.let {
                    it.ordering = index
                    it.dtUpdated = System.currentTimeMillis()
                }
            }
        }
        realm.close()
    }

    fun deleteAllRecord() {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction{
            realm.where(Record::class.java).findAll()?.deleteAllFromRealm()
        }
        realm.close()
    }
}