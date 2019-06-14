package com.ayaan.twelvepages.ui.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.setGlobalTheme

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun initTheme(rootLy: View) {
        setGlobalTheme(rootLy)
        rootLy.setBackgroundColor(AppTheme.background)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = window.peekDecorView().systemUiVisibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            window.peekDecorView().systemUiVisibility = flags
            window.statusBarColor = AppTheme.background
            window.navigationBarColor = AppTheme.background
        }
    }

    fun showProgressDialog(msg: String?) {
        hideProgressDialog()
        progressDialog = ProgressDialog(this)
        progressDialog?.let {
            if(msg.isNullOrEmpty()) it.setMessage(getString(R.string.plz_wait))
            else it.setMessage(msg)
            it.setCancelable(false)
            it.show()
        }
    }

    fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }
}