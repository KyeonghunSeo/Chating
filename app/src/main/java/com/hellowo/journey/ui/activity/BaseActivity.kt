package com.hellowo.journey.ui.activity

import android.app.ProgressDialog
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hellowo.journey.AppTheme

open class BaseActivity : AppCompatActivity() {
    var progressDialog: ProgressDialog? = null

    fun initTheme(rootLy: View) {
        rootLy.setBackgroundColor(AppTheme.backgroundColor)
        window.navigationBarColor = AppTheme.backgroundColor
        window.statusBarColor = AppTheme.backgroundColor
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

    fun showProgressDialog(msg: String) {
        hideProgressDialog()
        progressDialog = ProgressDialog(this)
        progressDialog?.let {
            it.setMessage(msg)
            it.setCancelable(false)
            it.show()
        }
    }

    fun hideProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }
}