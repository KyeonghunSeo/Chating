package com.hellowo.journey.manager

import android.annotation.SuppressLint
import com.hellowo.journey.AppDateFormat
import com.hellowo.journey.alarm.AlarmManager
import com.hellowo.journey.alarm.RegistedAlarm
import com.hellowo.journey.adapter.TimeObjectCalendarAdapter
import com.hellowo.journey.getCalendarTime23
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.view.CalendarView
import com.hellowo.journey.l
import com.hellowo.journey.model.Folder
import com.hellowo.journey.model.Tag
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.lang.Exception
import java.util.*

@SuppressLint("StaticFieldLeak")
object TimeObjectManager {
    private var timeObjectList: RealmResults<TimeObject>? = null
    private var withAnim = false
    var timeObjectCalendarAdapter: TimeObjectCalendarAdapter? = null
    var lastUpdatedItem: TimeObject? = null

    fun init() {}

    fun setTimeObjectCalendarAdapter(calendarView: CalendarView) {
        if(timeObjectCalendarAdapter == null) timeObjectCalendarAdapter = TimeObjectCalendarAdapter(calendarView)

        withAnim = false
        timeObjectList?.removeAllChangeListeners()
        timeObjectList = getTimeObjectList(calendarView.calendarStartTime, calendarView.calendarEndTime).apply {
            try{
                addChangeListener { result, changeSet ->
                    l("==========START timeObjectdataSetChanged=========")
                    l("result.isLoaded ${result.isLoaded}")
                    l("changeSet ${changeSet.isCompleteResult}")
                    l("데이터 : ${result.size} 개")
                    val t = System.currentTimeMillis()

                    changeSet.insertionRanges.firstOrNull()?.let {
                        lastUpdatedItem = result[it.startIndex]
                        l("추가된 데이터 : ${lastUpdatedItem.toString()}")
                    }

                    timeObjectCalendarAdapter?.refresh(result, withAnim)
                    withAnim = true
                    l("걸린시간 : ${(System.currentTimeMillis() - t) / 1000f} 초")
                    l("==========END timeObjectdataSetChanged=========")
                }

                if(isLoaded == false) {
                    timeObjectCalendarAdapter?.refresh(null, false)
                }
            }catch (e: Exception){e.printStackTrace()}
        }
    }

    fun getTimeObjectList(startTime: Long, endTime: Long) : RealmResults<TimeObject> {
        val realm = Realm.getDefaultInstance()
        val result = realm.where(TimeObject::class.java)
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

    fun getTimeObjectList(folder: Folder) : RealmResults<TimeObject> {
        val realm = Realm.getDefaultInstance()
        val result = realm.where(TimeObject::class.java)
                .beginGroup()
                .equalTo("folder.id", folder.id)
                .greaterThan("dtCreated", 0)
                .endGroup()
                .sort("dtCreated", Sort.DESCENDING)
                .findAllAsync()
        realm.close()
        return result
    }

    fun getTimeObjectList(query: String, tags: ArrayList<Tag>) : RealmResults<TimeObject> {
        val realm = Realm.getDefaultInstance()
        val result = realm.where(TimeObject::class.java)
                .beginGroup()
                .greaterThan("dtCreated", 0)
                .endGroup()
                .and()
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
                .sort("dtCreated", Sort.DESCENDING)
                .findAllAsync()
        realm.close()
        return result
    }

    fun save(timeObject: TimeObject) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction{ realm ->
            if(timeObject.id.isNullOrEmpty()) {
                timeObject.id = UUID.randomUUID().toString()
                timeObject.dtCreated = System.currentTimeMillis()
            }

            if(timeObject.dtStart > timeObject.dtEnd) {
                val t = timeObject.dtStart
                timeObject.dtStart = timeObject.dtEnd
                timeObject.dtEnd = t
            }

            if(timeObject.alarms.isNotEmpty()) {
                timeObject.alarms.sortBy { it.dtAlarm }

                var registedAlarm = realm.where(RegistedAlarm::class.java)
                        .equalTo("timeObjectId", timeObject.id).findFirst()

                if(registedAlarm != null){
                    AlarmManager.unRegistTimeObjectAlarm(registedAlarm.requestCode)
                }else {
                    registedAlarm = realm.createObject(RegistedAlarm::class.java, timeObject.id)?.apply {
                        val requestCode = realm.where(RegistedAlarm::class.java).max("requestCode")
                        if(requestCode != null) {
                            this.requestCode = requestCode.toInt() + 1
                        }else {
                            this.requestCode = 0
                        }
                    }
                }

                registedAlarm?.let { AlarmManager.registTimeObjectAlarm(timeObject, it) }
            }

            timeObject.dtUpdated = System.currentTimeMillis()
            realm.insertOrUpdate(timeObject)
        }
        realm.close()
    }

    fun done(timeObject: TimeObject) {
        val realm = Realm.getDefaultInstance()
        val id = timeObject.id
        if(timeObject.repeat.isNullOrEmpty()) {
            realm.executeTransaction{ realm ->
                realm.where(TimeObject::class.java).equalTo("id", id).findFirst()?.let {
                    if(it.dtDone == Long.MIN_VALUE) {
                        it.dtDone = System.currentTimeMillis()
                    }else {
                        it.dtDone = Long.MIN_VALUE
                    }
                    it.dtUpdated = it.dtDone
                }
            }
        }else {
            val ymdKey = AppDateFormat.ymdkey.format(Date(timeObject.dtStart))
            realm.executeTransaction{ realm ->
                realm.where(TimeObject::class.java).equalTo("id", id).findFirst()?.let {
                    it.exDates.add(ymdKey)
                    it.dtUpdated = System.currentTimeMillis()
                    val new = TimeObject()
                    new.copy(timeObject)
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

    fun deleteOnly(timeObject: TimeObject) {
        val realm = Realm.getDefaultInstance()
        val id = timeObject.id
        val ymdKey = AppDateFormat.ymdkey.format(Date(timeObject.dtStart))
        realm.executeTransaction{ realm ->
            realm.where(TimeObject::class.java).equalTo("id", id).findFirst()?.let {
                it.exDates.add(ymdKey)
                it.dtUpdated = System.currentTimeMillis()
            }
        }
        realm.close()
    }

    fun deleteAfter(timeObject: TimeObject) {
        val realm = Realm.getDefaultInstance()
        val id = timeObject.id
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeObject.dtStart

        cal.add(Calendar.DATE, -1)
        realm.executeTransaction{ realm ->
            realm.where(TimeObject::class.java).equalTo("id", id).findFirst()?.let {
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

    fun delete(timeObject: TimeObject) {
        val realm = Realm.getDefaultInstance()
        val id = timeObject.id
        realm.executeTransaction{ realm ->
            realm.where(TimeObject::class.java).equalTo("id", id).findFirst()?.let {
                it.dtCreated = -1
                it.dtUpdated = System.currentTimeMillis()
            }
        }
        realm.close()
    }

    fun clear() {
        timeObjectList?.removeAllChangeListeners()
        timeObjectList = null
        timeObjectCalendarAdapter = null
    }

    fun makeNewTimeObject(start: Long, end: Long): TimeObject {
        return TimeObject().apply {
            dtStart = start
            dtEnd = end
            timeZone = TimeZone.getDefault().id
        }
    }

    fun getTimeObjectById(id: String): TimeObject? {
        val realm = Realm.getDefaultInstance()
        val result =  realm.where(TimeObject::class.java)
                .equalTo("id", id)
                .findFirst()
        realm.close()
        return result
    }

    fun reorder(list: List<TimeObject>) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction{ realm ->
            list.forEachIndexed { index, timeObject ->
                realm.where(TimeObject::class.java).equalTo("id", timeObject.id).findFirst()?.let {
                    it.ordering = index
                    it.dtUpdated = System.currentTimeMillis()
                }
            }
        }
        realm.close()
    }

}