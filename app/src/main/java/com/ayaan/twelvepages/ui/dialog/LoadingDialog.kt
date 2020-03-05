package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.setGlobalTheme
import kotlinx.android.synthetic.main.dialog_loading.*


class LoadingDialog(activity: Activity, private val title: String?, private val sub: String?)
    : Dialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.attributes?.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_loading)
        setLayout()
    }

    private fun setLayout() {
        setGlobalTheme(rootLy)

        if(!title.isNullOrEmpty()) {
            titleText.visibility = View.VISIBLE
            titleText.text = title
        }else {
            titleText.visibility = View.GONE
        }

        if(!sub.isNullOrEmpty()) {
            subText.visibility = View.VISIBLE
            subText.text = sub
        }else {
            subText.visibility = View.GONE
        }
    }

}
