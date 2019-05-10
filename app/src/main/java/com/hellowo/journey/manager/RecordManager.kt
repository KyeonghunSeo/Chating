package com.hellowo.journey.manager

import android.annotation.SuppressLint
import android.widget.Toast
import com.hellowo.journey.*
import com.hellowo.journey.alarm.AlarmManager
import com.hellowo.journey.alarm.RegistedAlarm
import com.hellowo.journey.model.Record
import com.hellowo.journey.model.Folder
import com.hellowo.journey.model.Tag
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

@SuppressLint("StaticFieldLeak")
object RecordManager {

    fun getRecordList(startTime: Long, endTime: Long) : RealmResults<Record> {
        val realm = Realm.getDefaultInstance()
        val result = realm.where(Record::class.java)
                .beginGroup()
                .isNull("folder")
                .greaterThan("dtCreated", 0)
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

    fun getRecordList(query: String, tags: ArrayList<Tag>) : RealmResults<Record> {
        val realm = Realm.getDefaultInstance()
        val q = realm.where(Record::class.java)
                .beginGroup()
                .greaterThan("dtCreated", 0)
                .endGroup()

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
                    .equalTo("tags.id", it.id)
                    .endGroup()
        }

        val result = q.sort("dtCreated", Sort.DESCENDING).findAllAsync()
        realm.close()
        return result
    }

    fun save(record: Record) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction{
            if(record.id.isNullOrEmpty()) {
                record.id = UUID.randomUUID().toString()
                record.dtCreated = System.currentTimeMillis()
            }

            if(record.dtStart > record.dtEnd) {
                val t = record.dtStart
                record.dtStart = record.dtEnd
                record.dtEnd = t
            }

            if(record.alarms.isNotEmpty()) {
                record.alarms.sortBy { it.dtAlarm }
                var registedAlarm = realm.where(RegistedAlarm::class.java)
                        .equalTo("timeObjectId", record.id).findFirst()

                if(registedAlarm != null){
                    AlarmManager.unRegistTimeObjectAlarm(registedAlarm.requestCode)
                }else {
                    registedAlarm = realm.createObject(RegistedAlarm::class.java, record.id)?.apply {
                        val requestCode = realm.where(RegistedAlarm::class.java).max("requestCode")
                        if(requestCode != null) {
                            this.requestCode = requestCode.toInt() + 1
                        }else {
                            this.requestCode = 0
                        }
                    }
                }
                registedAlarm?.let { AlarmManager.registTimeObjectAlarm(record, it) }
            }

            record.dtUpdated = System.currentTimeMillis()
            realm.insertOrUpdate(record)
        }
        realm.close()
    }

    fun done(record: Record) {
        val realm = Realm.getDefaultInstance()
        val id = record.id
        if(record.repeat.isNullOrEmpty()) {
            realm.executeTransaction{ realm ->
                realm.where(Record::class.java).equalTo("id", id).findFirst()?.let {
                    if(it.dtDone == Long.MIN_VALUE) {
                        it.dtDone = System.currentTimeMillis()
                    }else {
                        it.dtDone = Long.MIN_VALUE
                    }
                    it.dtUpdated = it.dtDone
                }
            }
        }else {
            val ymdKey = AppDateFormat.ymdkey.format(Date(record.dtStart))
            realm.executeTransaction{ realm ->
                realm.where(Record::class.java).equalTo("id", id).findFirst()?.let {
                    it.exDates.add(ymdKey)
                    it.dtUpdated = System.currentTimeMillis()
                    val new = Record()
                    new.copy(record)
                    new.id = UUID.randomUUID().toString()
                    new.clearRepeat()
                    if(new.dtDone == Long.MIN_VALUE) {
                        new.dtDone = System.currentTimeMillis()
                    }else {
                        new.dtDone = Long.MIN_VALUE
                    }
                    realm.insertOrUpdate(new)
                }
            }
        }
        realm.close()
    }

    fun deleteOnly(record: Record) {
        val realm = Realm.getDefaultInstance()
        record.id?.let { id ->
            record.repeatKey?.let { ymdKey ->
                realm.executeTransaction{ realm ->
                    realm.where(Record::class.java).equalTo("id", id).findFirst()?.let {
                        it.exDates.add(ymdKey)
                        it.dtUpdated = System.currentTimeMillis()
                    }
                }
            }
        }
        realm.close()
    }

    fun deleteAfter(record: Record) {
        val realm = Realm.getDefaultInstance()
        val id = record.id
        val cal = Calendar.getInstance()
        cal.timeInMillis = record.dtStart

        cal.add(Calendar.DATE, -1)
        realm.executeTransaction{ realm ->
            realm.where(Record::class.java).equalTo("id", id).findFirst()?.let {
                val dtUntil = getCalendarTime23(cal)
                if(dtUntil < it.dtStart) { // 기한보다 작으면 완전히 제거
                    it.deleteFromRealm()
                }else {
                    it.dtUntil = getCalendarTime23(cal)
                    it.dtUpdated = System.currentTimeMillis()
                }
            }
        }
        realm.close()
    }

    fun delete(record: Record) {
        val realm = Realm.getDefaultInstance()
        val id = record.id
        realm.executeTransaction{ realm ->
            realm.where(Record::class.java).equalTo("id", id).findFirst()?.let {
                it.dtCreated = -1
                it.dtUpdated = System.currentTimeMillis()
            }
        }
        realm.close()
    }

    fun makeNewRecord(start: Long, end: Long): Record {
        return Record().apply {
            dtStart = start
            dtEnd = end
            timeZone = TimeZone.getDefault().id
        }
    }

    fun getTimeObjectById(id: String): Record? {
        val realm = Realm.getDefaultInstance()
        val result =  realm.where(Record::class.java)
                .equalTo("id", id)
                .findFirst()
        realm.close()
        return result
    }

    fun reorder(list: List<Record>) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction{ realm ->
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