package com.hellowo.chating.calendar

import android.os.AsyncTask
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
        Realm.getDefaultInstance().use { realm ->
            timeObjectList?.removeAllChangeListeners()
            timeObjectList = realm.where(TimeObject::class.java)
                    .greaterThanOrEqualTo("dtEnd", calendarView.calendarStartTime)
                    .lessThanOrEqualTo("dtStart", calendarView.calendarEndTime)
                    .sort("dtStart", Sort.ASCENDING)
                    .findAllAsync()
            timeObjectList?.addChangeListener { result ,changeSet ->
                l("result.isLoaded ${result.isLoaded}")
                l("changeSet ${changeSet.isCompleteResult}")
                l("==========START timeObjectdataSetChanged=========")
                val t = System.currentTimeMillis()
                result.forEach {
                    l(it.toString())
                }
                l("걸린시간 : ${(System.currentTimeMillis() - t) / 1000f} 초")
                l("==========END timeObjectdataSetChanged=========")
            }
        }
    }
}