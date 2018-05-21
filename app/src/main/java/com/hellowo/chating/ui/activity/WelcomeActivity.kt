package com.hellowo.chating.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.hellowo.chating.AUTH_URL
import com.hellowo.chating.R
import io.realm.ObjectServerError
import io.realm.SyncCredentials
import io.realm.SyncUser
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        loginBtn.setOnClickListener { login() }
        if (SyncUser.current() != null) {
            val mainIntent = Intent(this@WelcomeActivity, MainActivity::class.java)
            startActivity(mainIntent)
        }
    }

    private fun login() {
        val credentials = SyncCredentials.nickname(nameEdit.text.toString(), false)
        SyncUser.logInAsync(credentials, AUTH_URL, object: SyncUser.Callback<SyncUser> {
            override fun onError(error: ObjectServerError?) {
                Log.e("Login error", error.toString())
            }
            override fun onSuccess(result: SyncUser?) {
                val mainIntent = Intent(this@WelcomeActivity, MainActivity::class.java)
                startActivity(mainIntent)
            }
        })
    }
}