package com.ayaan.twelvepages.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.fragment.app.FragmentActivity
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.model.Template
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.view.CalendarView
import com.ayaan.twelvepages.ui.view.RecordView
import kotlinx.android.synthetic.main.dialog_incalendar_style.*
import java.util.*


class RecordViewStyleDialog(private val activity: FragmentActivity, record: Record?,
                            template: Template?, private val onResult: (Int, Int) -> Unit) : Dialog(activity) {
    private val recordView = RecordView(context, Record(), RecordCalendarAdapter.Formula.STACK, 0, 0)
    private val subRecordView = RecordView(context, Record(), RecordCalendarAdapter.Formula.STACK, 0, 0)
    private var noColor = false

    init {
        recordView.childList = ArrayList()
        recordView.childList?.add(recordView.record)
        subRecordView.childList = ArrayList()
        subRecordView.childList?.add(subRecordView.record)
        when {
            record != null -> {
                recordView.formula = record.getFormula()
                recordView.record.style = record.style
                recordView.record.colorKey = record.colorKey
                subRecordView.formula = record.getFormula()
                subRecordView.record.style = record.style
                subRecordView.record.colorKey = record.colorKey
                if(record.id == "osInstance::") noColor = true
            }
            template != null -> {
                recordView.formula = RecordCalendarAdapter.Formula.styleToFormula(template.style)
                recordView.record.style = template.style
                recordView.record.colorKey = template.colorKey
                subRecordView.formula = RecordCalendarAdapter.Formula.styleToFormula(template.style)
                subRecordView.record.style = template.style
                subRecordView.record.colorKey = template.colorKey
            }
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
            shapePicker.scrollToPosition(recordView.record.style / 100)
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
        dowText.text = AppDateFormat.dow.format(cal.time)
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

        colorImg.setColorFilter(ColorManager.getColor(recordView.record.colorKey))
        colorBtn.setOnClickListener {
            val location = IntArray(2)
            colorImg.getLocationOnScreen(location)
            showDialog(SmallColorPickerDialog(activity, recordView.record.colorKey, location) { colorKey ->
                recordView.record.colorKey = colorKey
                subRecordView.record.colorKey = colorKey
                colorImg.setColorFilter(ColorManager.getColor(colorKey))
                drawRecord()
            }, true, true, true, false)
        }

        textColorBtn.setOnClickListener {
            if(recordView.record.isTextColored()) {

            }else {

            }
        }

        imageBtn.setOnClickListener {
            StickerPickerDialog{ index ->

            }.show(activity.supportFragmentManager, null)
        }

        formulaPicker.formula = recordView.formula
        formulaPicker.onSelected = { formula ->
            recordView.record.setFormula(formula)
            recordView.formula = formula
            subRecordView.record.setFormula(formula)
            subRecordView.formula = formula
            shapePicker.refresh(formula, formula.shapes[0])
            drawRecord()
        }
        formulaPicker.adapter?.notifyDataSetChanged()

        shapePicker.onSelected = { shape ->
            recordView.record.setShape(shape)
            subRecordView.record.setShape(shape)
            drawRecord()
        }
        shapePicker.refresh(recordView.formula, recordView.record.getShape())

        cancelBtn.setOnClickListener { dismiss() }
        confirmBtn.setOnClickListener {
            onResult.invoke(recordView.record.style, recordView.record.colorKey)
            dismiss()
        }
    }

    private fun setTextColerBtn() {

    }

    private val dateWidth = dpToPx(60f)

    private fun drawRecord() {
        when(recordView.formula) {
            RecordCalendarAdapter.Formula.STACK -> {
                recordView.record.title = getRandomText(R.array.singleline_texts)
                subRecordView.record.title = getRandomText(R.array.singleline_texts)
            }
            RecordCalendarAdapter.Formula.EXPANDED -> {
                recordView.record.title = getRandomText(R.array.multiline_texts)
                subRecordView.record.title = getRandomText(R.array.multiline_texts)
            }
            RecordCalendarAdapter.Formula.RANGE -> {
                recordView.record.title = getRandomText(R.array.range_texts)
                subRecordView.record.title = getRandomText(R.array.range_texts)
            }
            else -> {}
        }

        recordView.setStyle()
        subRecordView.setStyle()

        val topMargin = when {
            recordView.formula == RecordCalendarAdapter.Formula.RANGE -> CalendarView.dataStartYOffset + dateWidth
            else -> CalendarView.dataStartYOffset
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
        if(recordView.formula == RecordCalendarAdapter.Formula.STICKER) {
            recordView.translationY = -dpToPx(10f)
        }else {
            recordView.translationY = 0f
        }

        when(recordView.formula) {
            RecordCalendarAdapter.Formula.STACK -> {
                subRecordView.length = 2
                subRecordView.mLeft = 0f
                subRecordView.mRight = dateWidth * 2
                subRecordView.mTop = topMargin
                subRecordView.mBottom = subRecordView.mTop + subRecordView.getViewHeight().toFloat()
                subRecordView.translationX = dateWidth * 2
                subRecordView.translationY = 0f
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
                subRecordView.translationX = dateWidth * 2
                subRecordView.translationY = 0f
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
                subRecordView.translationX = 0f
                subRecordView.translationY = 0f
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
                subRecordView.translationX = dateWidth * 2
                subRecordView.translationY = 0f
                shapeLy.visibility = View.GONE
                colorLy.visibility = View.VISIBLE
                imageLy.visibility = View.GONE
            }
            RecordCalendarAdapter.Formula.STICKER -> {
                subRecordView.length = 1
                subRecordView.mLeft = 0f
                subRecordView.mRight = dateWidth
                subRecordView.mTop = topMargin
                subRecordView.mBottom = subRecordView.mTop + subRecordView.getViewHeight().toFloat()
                subRecordView.translationX = dateWidth * 2
                subRecordView.translationY = -dpToPx(10f)
                shapeLy.visibility = View.GONE
                colorLy.visibility = View.GONE
                imageLy.visibility = View.VISIBLE
            }
            else -> {}
        }
        if(noColor) colorLy.visibility = View.GONE
        recordView.setLayout()
        subRecordView.setLayout()
        recordView.invalidate()
        subRecordView.invalidate()
    }

}
