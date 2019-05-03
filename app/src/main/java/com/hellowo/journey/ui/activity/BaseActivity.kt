package com.hellowo.journey.ui.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.setGlobalTheme

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun initTheme(rootLy: View) {
        setGlobalTheme(rootLy)
        rootLy.setBackgroundColor(AppTheme.backgroundColor)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = window.peekDecorView().systemUiVisibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            window.peekDecorView().systemUiVisibility = flags
            window.statusBarColor = AppTheme.backgroundColor
            window.navigationBarColor = AppTheme.backgroundColor
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