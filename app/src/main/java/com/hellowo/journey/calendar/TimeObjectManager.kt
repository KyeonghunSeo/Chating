package com.hellowo.journey.calendar

import android.annotation.SuppressLint
import com.hellowo.journey.AppRes
import com.hellowo.journey.alarm.AlarmManager
import com.hellowo.journey.alarm.RegistedAlarm
import com.hellowo.journey.adapter.TimeObjectCalendarAdapter
import com.hellowo.journey.getCalendarTime23
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.view.CalendarView
import com.hellowo.journey.l
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

object TimeObjectManager {
    @SuppressLint("StaticFieldLeak")
    lateinit var realm: Realm
    private var timeObjectList: RealmResults<TimeObject>? = null
    @SuppressLint("StaticFieldLeak")
    var timeObjectCalendarAdapter: TimeObjectCalendarAdapter? = null
    var lastUpdatedItem: TimeObject? = null
    private var postSelectDate = -1
    private var withAnim = false

    fun init() {
        realm = Realm.getDefaultInstance()
    }

    fun setTimeObjectCalendarAdapter(calendarView: CalendarView) {
        withAnim = false
        timeObjectList?.removeAllChangeListeners()
        timeObjectList = getTimeObjectList(calendarView.calendarStartTime, calendarView.calendarEndTime)
        timeObjectList?.addChangeListener { result, changeSet ->
            l("==========START timeObjectdataSetChanged=========")
            l("result.isLoaded ${result.isLoaded}")
            l("changeSet ${changeSet.isCompleteResult}")
            l("데이터 : ${result.size} 개")
            val t = System.currentTimeMillis()

            changeSet.insertionRanges.firstOrNull()?.let {
                lastUpdatedItem = result[it.startIndex]
                l("추가된 데이터 : ${lastUpdatedItem.toString()}")
            }

            timeObjectCalendarAdapter?.refresh(result, withAnim) ?: TimeObjectCalendarAdapter(result, calendarView).let {
                timeObjectCalendarAdapter = it.apply { draw() }
            }

            if(postSelectDate >= 0) {
                calendarView.selectDate(postSelectDate, true, false)
                postSelectDate = -1
            }

            withAnim = true
            l("걸린시간 : ${(System.currentTimeMillis() - t) / 1000f} 초")
            l("==========END timeObjectdataSetChanged=========")
        }
    }

    fun getTimeObjectList(startTime: Long, endTime: Long) : RealmResults<TimeObject> {
        return realm.where(TimeObject::class.java)
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
    }

    fun save(timeObject: TimeObject) {
        realm.executeTransactionAsync{ realm ->
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
    }

    fun done(timeObject: TimeObject) {
        val id = timeObject.id
        if(timeObject.repeat.isNullOrEmpty()) {
            realm.executeTransactionAsync{ realm ->
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
            val ymdKey = AppRes.ymdkey.format(Date(timeObject.dtStart))
            realm.executeTransactionAsync{ realm ->
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
    }

    fun deleteOnly(timeObject: TimeObject) {
        val id = timeObject.id
        val ymdKey = AppRes.ymdkey.format(Date(timeObject.dtStart))
        realm.executeTransactionAsync{ realm ->
            realm.where(TimeObject::class.java).equalTo("id", id).findFirst()?.let {
                it.exDates.add(ymdKey)
                it.dtUpdated = System.currentTimeMillis()
            }
        }
    }

    fun deleteAfter(timeObject: TimeObject) {
        val id = timeObject.id
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeObject.dtStart

        cal.add(Calendar.DATE, -1)
        realm.executeTransactionAsync{ realm ->
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
    }

    fun delete(timeObject: TimeObject) {
        val id = timeObject.id
        realm.executeTransactionAsync{ realm ->
            realm.where(TimeObject::class.java).equalTo("id", id).findFirst()?.let {
                it.dtCreated = -1
                it.dtUpdated = System.currentTimeMillis()
            }
        }
    }

    fun clear() {
        timeObjectList?.removeAllChangeListeners()
        timeObjectList = null
        timeObjectCalendarAdapter = null
        realm.close()
    }

    fun postSelectDate(cellNum: Int) {
        postSelectDate = cellNum
    }

    fun makeNewTimeObject(start: Long, end: Long): TimeObject {
        return TimeObject().apply {
            type = TimeObject.Type.NOTE.ordinal
            style = TimeObject.Style.DEFAULT.ordinal
            color = CalendarSkin.dateColor
            dtStart = start
            dtEnd = end
            timeZone = TimeZone.getDefault().id
        }
    }

    fun getTimeObjectById(id: String): TimeObject? {
        return realm.where(TimeObject::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    fun reorder(list: List<TimeObject>) {
        realm.executeTransactionAsync{ realm ->
            list.forEachIndexed { index, timeObject ->
                realm.where(TimeObject::class.java).equalTo("id", timeObject.id).findFirst()?.let {
                    it.ordering = index
                    it.dtUpdated = System.currentTimeMillis()
                }
            }
        }
    }

}