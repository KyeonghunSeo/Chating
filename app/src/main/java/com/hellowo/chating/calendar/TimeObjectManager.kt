package com.hellowo.chating.calendar

import android.util.Log
import com.hellowo.chating.l
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

object TimeObjectManager {
    var timeObjectList: RealmResults<TimeObject>? = null

    fun getTimeObjectList() {

    }

    fun setTimeObjectListData(calendarView: CalendarView) {
        val realm = Realm.getDefaultInstance()
        timeObjectList?.removeAllChangeListeners()
        timeObjectList = realm.where(TimeObject::class.java)
                .greaterThanOrEqualTo("dtEnd", calendarView.calendarStartTime)
                .lessThanOrEqualTo("dtStart", calendarView.calendarEndTime)
                .sort("dtStart", Sort.ASCENDING)
                .findAllAsync()
        timeObjectList?.addChangeListener { result ,changeSet ->
            l("==========START timeObjectdataSetChanged=========")
            l("result.isLoaded ${result.isLoaded}")
            l("changeSet ${changeSet.isCompleteResult}")
            result.forEach {
                l(it.toString())
            }
            l("==========END timeObjectdataSetChanged=========")
        }
    }
}