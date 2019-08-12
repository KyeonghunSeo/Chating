package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.setGlobalTheme
import kotlinx.android.synthetic.main.dialog_base.*


open class BaseDialog(activity: Activity) : Dialog(activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.attributes?.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_base)
        panel.setOnClickListener {  }
        rootLy.setOnClickListener { dismiss() }
    }

    fun setLayout(layoutId: Int, containerWidth: Int) {
        LayoutInflater.from(context).inflate(layoutId, container, true)
        panel.layoutParams.width = containerWidth
        panel.requestLayout()
        setGlobalTheme(rootLy)
    }

    fun hideHeader() {
        headerLy.visibility = View.GONE
    }

    fun hideBottomBtnsLy(){
        bottomBtnsLy.visibility = View.GONE
        (contentLy.layoutParams as FrameLayout.LayoutParams).bottomMargin = dpToPx(25)
    }
}
