package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.model.Template
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.view.CalendarView
import com.ayaan.twelvepages.ui.view.RecordView
import kotlinx.android.synthetic.main.dialog_incalendar_style.*
import java.util.*

class RecordViewStyleDialog(private val activity: Activity, record: Record?,
                            template: Template?, private val onResult: (Int, Int) -> Unit) : Dialog(activity) {
    private val recordView = RecordView(context, Record(), RecordCalendarAdapter.Formula.TOP_STACK, 0, 0)

    init {
        if(record != null) {
            recordView.record.title = record.getTitleInCalendar()
            recordView.record.style = record.style
            recordView.record.colorKey = record.colorKey
        }else if(template != null) {
            recordView.record.title = template.title
            recordView.record.style = template.style
            recordView.record.colorKey = template.colorKey
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogAnimation
        setContentView(R.layout.dialog_incalendar_style)
        setLayout()
        setOnShowListener {
            drawRecord()
            formulaPicker.scrollToPosition(recordView.record.style % 100)
            shapePickerView.scrollToPosition(recordView.record.style / 100)
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

        recordView.layoutParams = FrameLayout.LayoutParams(dpToPx(50), dpToPx(16)).apply {
            gravity = Gravity.TOP
            topMargin = CalendarView.dataStartYOffset.toInt()
        }
        previewContainer.addView(recordView)

        colorImg.setColorFilter(AppTheme.getColor(recordView.record.colorKey))
        colorBtn.setOnClickListener {
            val location = IntArray(2)
            colorImg.getLocationOnScreen(location)
            showDialog(ColorPickerDialog(activity, recordView.record.colorKey, location) { colorKey ->
                recordView.record.colorKey = colorKey
                colorImg.setColorFilter(AppTheme.getColor(colorKey))
                drawRecord()
            }, true, true, true, false)
        }

        formulaPicker.formula = recordView.record.style % 100
        formulaPicker.onSelected = { formulaNum ->
            recordView.record.setStyleFormulaNumber(formulaNum)
            recordView.formula = RecordCalendarAdapter.getFormula(recordView.record, 0)
            shapePickerView.refresh(recordView.formula)
            drawRecord()
        }
        formulaPicker.adapter?.notifyDataSetChanged()

        shapePickerView.shape = recordView.record.style / 100
        shapePickerView.onSelected = { shape ->
            recordView.record.setStyleShape(shape)
            drawRecord()
        }

        cancelBtn.setOnClickListener { dismiss() }
        confirmBtn.setOnClickListener {
            onResult.invoke(recordView.record.style, recordView.record.colorKey)
            dismiss()
        }
    }

    private fun drawRecord() {
        recordView.setStyle()
        recordView.layoutParams.width = dpToPx(60)
        recordView.layoutParams.height = recordView.getViewHeight()
        recordView.textSpaceWidth = recordView.paint.measureText(recordView.text.toString())
        recordView.invalidate()
        recordView.requestLayout()
    }

}
