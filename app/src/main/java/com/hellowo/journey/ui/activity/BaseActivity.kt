package com.hellowo.journey.ui.activity

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R

open class BaseActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null

    fun initTheme(rootLy: View) {
        rootLy.setBackgroundColor(AppTheme.backgroundColor)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = window.peekDecorView().systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            window.peekDecorView().systemUiVisibility = flags
            window.statusBarColor = AppTheme.backgroundColor
            window.navigationBarColor = AppTheme.backgroundColor
        }
    }

    fun setGlobalFont(view: View?) {
        if (view != null) {
            if (view is ViewGroup) {
                val vg = view as ViewGroup?
                val vgCnt = vg!!.childCount
                for (i in 0 until vgCnt) {
                    val v = vg.getChildAt(i)
                    if (v is TextView) {
                        v.typeface = AppTheme.regularFont
                    }
                    setGlobalFont(v)
                }
            }
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