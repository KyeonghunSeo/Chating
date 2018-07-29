package com.hellowo.chating.calendar

import android.annotation.SuppressLint
import com.hellowo.chating.l
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*
import io.realm.OrderedCollectionChangeSet



object TimeObjectManager {
    @SuppressLint("StaticFieldLeak")
    lateinit var realm: Realm
    private var timeObjectList: RealmResults<TimeObject>? = null
    @SuppressLint("StaticFieldLeak")
    var timeObjectAdapter: TimeObjectAdapter? = null
    var lastUpdatedItem: TimeObject? = null
    private var postSelectDate = -1
    private var withAnim = false

    fun init() {
        realm = Realm.getDefaultInstance()
    }

    fun setTimeObjectListAdapter(calendarView: CalendarView) {
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

            timeObjectAdapter?.refresh(result, withAnim) ?: TimeObjectAdapter(result, calendarView).let { timeObjectAdapter = it.apply { draw() } }
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
        Realm.getDefaultInstance().use {
            it.executeTransactionAsync{ realm ->
                if(timeObject.id == null) {
                    timeObject.id = UUID.randomUUID().toString()
                    timeObject.dtUpdated = System.currentTimeMillis()
                    realm.insert(timeObject)
                }
            }
        }
    }

    fun clear() {
        timeObjectList?.removeAllChangeListeners()
        timeObjectList = null
        timeObjectAdapter = null
        realm.close()
    }

    fun postSelectDate(cellNum: Int) {
        postSelectDate = cellNum
    }

}