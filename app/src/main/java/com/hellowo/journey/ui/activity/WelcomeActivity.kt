package com.hellowo.journey.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.hellowo.journey.AUTH_URL
import com.hellowo.journey.R
import com.hellowo.journey.USER_URL
import com.hellowo.journey.l
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
        val credentials = SyncCredentials.usernamePassword(nameEdit.text.toString(),
                passwordEdit.text.toString(), false)
        SyncUser.logInAsync(credentials, AUTH_URL, object: SyncUser.Callback<SyncUser> {
            override fun onError(error: ObjectServerError?) {
                l("!!!!로그인 에러"+error?.errorCode?.intValue())
                Log.e("Login error", error.toString())
            }
            override fun onSuccess(result: SyncUser?) {
                result?.let { startMainActivity(it) }
            }
        })
    }

    private fun startMainActivity(result: SyncUser) {
        Realm.setDefaultConfiguration(result.createConfiguration(USER_URL).build())
        finish()
    }
}