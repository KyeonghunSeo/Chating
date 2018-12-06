package com.hellowo.journey.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.hellowo.journey.AUTH_URL
import com.hellowo.journey.R
import com.hellowo.journey.USER_URL
import com.hellowo.journey.l
import io.realm.*
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity : BaseActivity() {
    private var realmAsyncTask: RealmAsyncTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        initTheme(rootLy)
        loginBtn.setOnClickListener { login() }
    }

    private fun login() {
        val credentials = SyncCredentials.usernamePassword(emailEdit.text.toString(),
                passwordEdit.text.toString(), false)
        SyncUser.logInAsync(credentials, AUTH_URL, object: SyncUser.Callback<SyncUser> {
            override fun onError(error: ObjectServerError?) {
                l("[로그인 실패]"+error?.errorCode?.intValue())
                hideProgressDialog()
            }
            override fun onSuccess(result: SyncUser?) {
                l("[로그인 성공]")
                result?.let { initRealm(it) }
            }
        })
        showProgressDialog(getString(R.string.plz_wait))
    }

    private fun initRealm(result: SyncUser) {
        val config = result.createConfiguration(USER_URL)
                .fullSynchronization()
                .waitForInitialRemoteData()
                .build()
        realmAsyncTask = Realm.getInstanceAsync(config, object : Realm.Callback() {
            override fun onSuccess(realm: Realm) {
                if (isDestroyed) {
                    realm.close()
                } else {
                    l("Realm 준비 완료")
                    Realm.setDefaultConfiguration(config)
                    hideProgressDialog()
                    startMainActivity()
                }
            }

            override fun onError(exception: Throwable) {
                super.onError(exception)
                hideProgressDialog()
            }
        })
    }

    private fun startMainActivity() {
        setResult(Activity.RESULT_OK)
        finish()
    }
}