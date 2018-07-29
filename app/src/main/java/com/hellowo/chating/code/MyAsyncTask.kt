package com.hellowo.chating.code

import android.os.AsyncTask


class MyAsyncTask() : AsyncTask<String, String, String?>() {
    override fun doInBackground(vararg args: String): String? {
        return null
    }

    override fun onProgressUpdate(vararg text: String) {
    }

    override fun onPostExecute(result: String?) {
    }
}