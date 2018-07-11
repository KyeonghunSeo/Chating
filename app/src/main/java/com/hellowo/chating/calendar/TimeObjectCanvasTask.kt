package com.hellowo.chating.calendar

import android.os.AsyncTask
import com.hellowo.chating.l
import io.realm.RealmResults


class TimeObjectCanvasTask(val timeObjectList : RealmResults<TimeObject>, val calendarView: CalendarView) : AsyncTask<String, String, String?>() {
    override fun doInBackground(vararg args: String): String? {
        return null
    }

    override fun onProgressUpdate(vararg text: String) {
    }

    override fun onPostExecute(result: String?) {
    }
}