package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.hellowo.journey.*
import com.hellowo.journey.model.Record
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.view.CalendarView
import com.hellowo.journey.ui.view.RecordView
import kotlinx.android.synthetic.main.dialog_incalendar_style.*
import java.util.*

class InCalendarStyleDialog(private val activity: Activity, private val style: Int, private val record: Record?,
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

        val cal = Calendar.getInstance()
        dateText.text = cal.get(Calendar.DATE).toString()
        dowText.text = AppDateFormat.dowEng.format(cal.time).toUpperCase()
        cal.add(Calendar.DATE, 1)
        dateText2.text = cal.get(Calendar.DATE).toString()
        cal.add(Calendar.DATE, 1)
        dateText3.text = cal.get(Calendar.DATE).toString()

        recordView.record.style = style
        recordView.layoutParams = FrameLayout.LayoutParams(dpToPx(50), dpToPx(16)).apply {
            gravity = Gravity.TOP
            topMargin = CalendarView.dataStartYOffset.toInt()
        }
        previewContainer.addView(recordView)

        colorBtn.setOnClickListener {
            val location = IntArray(2)
            colorImg.getLocationOnScreen(location)
            showDialog(ColorPickerDialog(activity, recordView.record.colorKey, location) { colorKey ->
                recordView.record.colorKey = colorKey
                colorImg.setColorFilter(AppTheme.getColor(colorKey))
                drawRecord()
            }, true, true, true, false)
        }

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
        recordView.record.title = context.getString(R.string.single_line_sample_string)
        recordView.setLookByType()

        recordView.layoutParams.width = dpToPx(60)
        recordView.layoutParams.height = RecordView.blockTypeSize

        recordView.textSpaceWidth = recordView.paint.measureText(recordView.text.toString())
        recordView.invalidate()
        recordView.requestLayout()
    }

}
