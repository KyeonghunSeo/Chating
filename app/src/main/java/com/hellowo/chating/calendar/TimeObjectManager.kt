package com.hellowo.chating.calendar

import android.annotation.SuppressLint
import com.hellowo.chating.calendar.adapter.TimeObjectCalendarAdapter
import com.hellowo.chating.calendar.model.CalendarSkin
import com.hellowo.chating.calendar.model.TimeObject
import com.hellowo.chating.calendar.view.CalendarView
import com.hellowo.chating.l
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
        timeObjectList = realm.where(TimeObject::class.java)
                .greaterThanOrEqualTo("dtEnd", calendarView.calendarStartTime)
                .lessThanOrEqualTo("dtStart", calendarView.calendarEndTime)
                .sort("dtStart", Sort.ASCENDING)
                .findAllAsync()
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

    fun save(timeObject: TimeObject) {
        realm.executeTransactionAsync{ realm ->
            if(timeObject.id == null) {
                timeObject.id = UUID.randomUUID().toString()
                timeObject.dtUpdated = System.currentTimeMillis()
            }
            realm.insertOrUpdate(timeObject)
        }
    }

    fun delete(timeObject: TimeObject) {
        if(timeObject.isValid) {
            val id = timeObject.id
            realm.executeTransactionAsync{ realm ->
                realm.where(TimeObject::class.java).equalTo("id", id).findFirst()?.deleteFromRealm()
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

    fun getCopiedData(originalData: TimeObject): TimeObject = realm.copyFromRealm(originalData)

}