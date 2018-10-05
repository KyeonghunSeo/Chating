package com.hellowo.journey.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.hellowo.journey.AUTH_URL
import com.hellowo.journey.R
import com.hellowo.journey.USER_URL
import io.realm.*
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        loginBtn.setOnClickListener { login() }
        SyncUser.current()?.let { startMainActivity(it) }
    }

    private fun login() {
        val credentials = SyncCredentials.nickname(nameEdit.text.toString(), false)
        SyncUser.logInAsync(credentials, AUTH_URL, object: SyncUser.Callback<SyncUser> {
            override fun onError(error: ObjectServerError?) {
                Log.e("Login error", error.toString())
            }
            override fun onSuccess(result: SyncUser?) {
                result?.let { startMainActivity(it) }
            }
        })
    }

    private fun startMainActivity(result: SyncUser) {
        val config = SyncConfiguration.Builder(result, USER_URL).partialRealm().build()
        Realm.setDefaultConfiguration(config)
        val mainIntent = Intent(this@WelcomeActivity, MainActivity::class.java)
        startActivity(mainIntent)
    }
}