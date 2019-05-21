package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import com.hellowo.journey.R
import com.hellowo.journey.model.Record
import com.hellowo.journey.ui.activity.MainActivity
import kotlinx.android.synthetic.main.dialog_style_picker.*

class CalendarBlockStyleDialog(activity: Activity, private val style: Int, private val record: Record?,
                               private val onResult: (Int) -> Unit) : Dialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_style_picker)
        setLayout()
        setOnShowListener {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun setLayout() {
        rootLy.layoutParams.width = MainActivity.getMainPanel()?.width ?: 0
        rootLy.layoutParams.height = MainActivity.getMainPanel()?.height ?: 0
        rootLy.setOnClickListener { dismiss() }


    }

}
