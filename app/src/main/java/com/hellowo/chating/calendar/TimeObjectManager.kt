package com.hellowo.chating.calendar

import android.annotation.SuppressLint
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
    var timeObjectAdapter: TimeObjectAdapter? = null

    fun init() {
        realm = Realm.getDefaultInstance()
    }

    fun setTimeObjectListAdapter(calendarView: CalendarView) {
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
            result.forEach { l(it.toString()) }
            val t = System.currentTimeMillis()
            timeObjectAdapter?.refresh(result) ?: TimeObjectAdapter(result, calendarView).let { timeObjectAdapter = it.apply { draw() } }
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

}