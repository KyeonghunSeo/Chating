package com.hellowo.journey.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.hellowo.journey.R
import com.hellowo.journey.l

class ScrapActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrap)
        val intent = intent

        if (intent.action != null && intent.action == Intent.ACTION_SEND) {

            for (key in intent.extras!!.keySet()) {
                val value = intent.extras!!.get(key)
                l("intent send extra : " + String.format("%s / %s (%s)", key,
                        value!!.toString(), value.javaClass.name))
            }

            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
            if (text != null && text.startsWith("http") && subject != null) {

            } else if (text != null) {
            }


        } else {
            finish()
        }
    }
}
