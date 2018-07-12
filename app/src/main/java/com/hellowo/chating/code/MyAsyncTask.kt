package com.hellowo.chating.code

import android.os.AsyncTask
import com.hellowo.chating.calendar.CalendarView
import com.hellowo.chating.calendar.TimeObject
import io.realm.RealmResults


class MyAsyncTask() : AsyncTask<String, String, String?>() {
    override fun doInBackground(vararg args: String): String? {
        return null
    }

    override fun onProgressUpdate(vararg text: String) {
    }

    override fun onPostExecute(result: String?) {
    }
}