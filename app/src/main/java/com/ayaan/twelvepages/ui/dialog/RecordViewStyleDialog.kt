package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
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
    private val recordView = RecordView(context, Record(), RecordCalendarAdapter.Formula.STACK, 0, 0)
    private val subRecordView = RecordView(context, Record(), RecordCalendarAdapter.Formula.STACK, 0, 0)

    init {
        recordView.record.title = getSampleText()
        recordView.childList = ArrayList()
        recordView.childList?.add(recordView.record)
        subRecordView.record.title = getSampleText()
        subRecordView.childList = ArrayList()
        subRecordView.childList?.add(subRecordView.record)
        if(record != null) {
            recordView.formula = RecordCalendarAdapter.Formula.values()[record.getFormula()]
            recordView.record.style = record.style
            recordView.record.colorKey = record.colorKey
            subRecordView.formula = RecordCalendarAdapter.Formula.values()[record.getFormula()]
            subRecordView.record.style = record.style
            subRecordView.record.colorKey = record.colorKey
        }else if(template != null) {
            recordView.formula = RecordCalendarAdapter.Formula.values()[template.style % 100]
            recordView.record.style = template.style
            recordView.record.colorKey = template.colorKey
            subRecordView.formula = RecordCalendarAdapter.Formula.values()[template.style % 100]
            subRecordView.record.style = template.style
            subRecordView.record.colorKey = template.colorKey
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.attributes?.windowAnimations = R.style.DialogAnimation
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
        cal.add(Calendar.DATE, 1)
        dateText4.text = cal.get(Calendar.DATE).toString()
        cal.add(Calendar.DATE, 1)
        dateText5.text = cal.get(Calendar.DATE).toString()

        previewContainer.addView(recordView)
        previewContainer.addView(subRecordView)

        colorImg.setColorFilter(AppTheme.getColor(recordView.record.colorKey))
        colorBtn.setOnClickListener {
            val location = IntArray(2)
            colorImg.getLocationOnScreen(location)
            showDialog(ColorPickerDialog(activity, recordView.record.colorKey, location) { colorKey ->
                recordView.record.colorKey = colorKey
                subRecordView.record.colorKey = colorKey
                colorImg.setColorFilter(AppTheme.getColor(colorKey))
                drawRecord()
            }, true, true, true, false)
        }

        formulaPicker.formula = recordView.formula
        formulaPicker.onSelected = { formula ->
            recordView.record.setFormula(formula.ordinal)
            recordView.formula = formula
            subRecordView.record.setFormula(formula.ordinal)
            subRecordView.formula = formula
            shapePickerView.refresh(formula)
            drawRecord()
        }
        formulaPicker.adapter?.notifyDataSetChanged()

        shapePickerView.formula = recordView.formula
        shapePickerView.shape = recordView.record.style / 100
        shapePickerView.onSelected = { shape ->
            recordView.record.setStyleShape(shape)
            subRecordView.record.setStyleShape(shape)
            drawRecord()
        }
        shapePickerView.setItems()

        cancelBtn.setOnClickListener { dismiss() }
        confirmBtn.setOnClickListener {
            onResult.invoke(recordView.record.style, recordView.record.colorKey)
            dismiss()
        }
    }

    private val dateWidth = dpToPx(60f)

    private fun drawRecord() {
        recordView.setStyle()
        subRecordView.setStyle()

        val topMargin = if(recordView.formula == RecordCalendarAdapter.Formula.RANGE) {
            CalendarView.dataStartYOffset + dateWidth
        }else {
            CalendarView.dataStartYOffset
        }

        recordView.length = when(recordView.formula) {
            RecordCalendarAdapter.Formula.STACK -> 1
            RecordCalendarAdapter.Formula.RANGE -> 4
            else -> 1
        }

        recordView.mLeft = 0f
        recordView.mRight = dateWidth * recordView.length
        recordView.mTop = topMargin
        recordView.mBottom = recordView.mTop + recordView.getViewHeight().toFloat()
        recordView.setLayout()
        recordView.invalidate()

        when(recordView.formula) {
            RecordCalendarAdapter.Formula.STACK -> {
                subRecordView.length = 2
                subRecordView.mLeft = 0f
                subRecordView.mRight = dateWidth * 2
                subRecordView.mTop = topMargin
                subRecordView.mBottom = subRecordView.mTop + subRecordView.getViewHeight().toFloat()
                subRecordView.setLayout()
                subRecordView.translationX = dateWidth * 2
                subRecordView.invalidate()
                shapeLy.visibility = View.VISIBLE
                colorLy.visibility = View.VISIBLE
                imageLy.visibility = View.GONE
            }
            RecordCalendarAdapter.Formula.EXPANDED -> {
                subRecordView.length = 1
                subRecordView.mLeft = 0f
                subRecordView.mRight = dateWidth
                subRecordView.mTop = topMargin
                subRecordView.mBottom = subRecordView.mTop + subRecordView.getViewHeight().toFloat()
                subRecordView.setLayout()
                subRecordView.translationX = dateWidth * 2
                subRecordView.invalidate()
                shapeLy.visibility = View.VISIBLE
                colorLy.visibility = View.VISIBLE
                imageLy.visibility = View.GONE
            }
            RecordCalendarAdapter.Formula.RANGE -> {
                subRecordView.length = 1
                subRecordView.mLeft = 0f
                subRecordView.mRight = dateWidth * 3
                subRecordView.mTop = topMargin - subRecordView.getViewHeight().toFloat()
                subRecordView.mBottom = subRecordView.mTop + subRecordView.getViewHeight().toFloat()
                subRecordView.setLayout()
                subRecordView.translationX = 0f
                subRecordView.invalidate()
                shapeLy.visibility = View.VISIBLE
                colorLy.visibility = View.VISIBLE
                imageLy.visibility = View.GONE
            }
            RecordCalendarAdapter.Formula.DOT -> {
                subRecordView.length = 1
                subRecordView.mLeft = 0f
                subRecordView.mRight = dateWidth
                subRecordView.mTop = topMargin
                subRecordView.mBottom = subRecordView.mTop + subRecordView.getViewHeight().toFloat()
                subRecordView.setLayout()
                subRecordView.translationX = dateWidth * 2
                subRecordView.invalidate()
                shapeLy.visibility = View.GONE
                colorLy.visibility = View.VISIBLE
                imageLy.visibility = View.GONE
            }
            RecordCalendarAdapter.Formula.IMAGE -> {
                subRecordView.length = 1
                subRecordView.mLeft = 0f
                subRecordView.mRight = dateWidth
                subRecordView.mTop = topMargin
                subRecordView.mBottom = subRecordView.mTop + subRecordView.getViewHeight().toFloat()
                subRecordView.setLayout()
                subRecordView.translationX = dateWidth * 2
                subRecordView.invalidate()
                shapeLy.visibility = View.GONE
                colorLy.visibility = View.GONE
                imageLy.visibility = View.VISIBLE
            }
        }
    }

}
