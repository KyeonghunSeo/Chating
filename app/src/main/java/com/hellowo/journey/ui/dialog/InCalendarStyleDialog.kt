package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.hellowo.journey.R
import com.hellowo.journey.dpToPx
import com.hellowo.journey.model.Record
import com.hellowo.journey.setGlobalTheme
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.view.RecordView
import kotlinx.android.synthetic.main.dialog_incalendar_style.*

class InCalendarStyleDialog(activity: Activity, private val style: Int, private val record: Record?,
                            private val onResult: (Int) -> Unit) : Dialog(activity) {
    private val recordView = RecordView(context, Record(), 0, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_incalendar_style)
        setLayout()
        setOnShowListener {
            drawRecord()
            styleTypePicker.scrollToPosition(style % 100)
        }
    }

    private fun setLayout() {
        setGlobalTheme(rootLy)
        contentLy.setOnClickListener {}
        rootLy.layoutParams.width = MainActivity.getMainPanel()?.width ?: 0
        rootLy.layoutParams.height = WRAP_CONTENT
        rootLy.setOnClickListener { dismiss() }

        recordView.record.style = style
        recordView.layoutParams = FrameLayout.LayoutParams(dpToPx(50), dpToPx(16)).apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        previewContainer.addView(recordView)
        previewContainer.scaleX = 1.6f
        previewContainer.scaleY = 1.6f

        styleTypePicker.type = style % 100
        styleTypePicker.onSelected = { type ->
            recordView.record.setInCalendarType(type)
            drawRecord()
        }
        styleTypePicker.adapter?.notifyDataSetChanged()

        cancelBtn.setOnClickListener { dismiss() }
        confirmBtn.setOnClickListener {
            onResult.invoke(recordView.record.style)
            dismiss()
        }
    }

    private fun drawRecord() {
        recordView.record.title = "1111"
        recordView.record.colorKey = 0
        recordView.setLookByType()

        recordView.layoutParams.width = dpToPx(60)
        recordView.layoutParams.height = WRAP_CONTENT
        recordView.layoutParams.width = (recordView.layoutParams.width * 1.5f + dpToPx(41)).toInt()

        recordView.pivotY = recordView.getViewHeight() / 2f
        recordView.textSpaceWidth = recordView.paint.measureText(recordView.text.toString())
        recordView.invalidate()
        recordView.requestLayout()
    }

}
