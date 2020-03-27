package com.ayaan.twelvepages.ui.dialog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.SymbolManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.model.Template
import com.ayaan.twelvepages.ui.view.CalendarView
import com.ayaan.twelvepages.ui.view.RecordView
import kotlinx.android.synthetic.main.container_in_calendar_style_dlg.*
import kotlinx.android.synthetic.main.dialog_base.*
import java.util.*


class RecordViewStyleDialog(private val activity: FragmentActivity, record: Record?,
                            template: Template?, private val onResult: (Int, Int, String?) -> Unit) : BaseDialog(activity) {
    private val recordView = RecordView(context, Record(), RecordCalendarAdapter.Formula.SINGLE_TEXT, 0, 0)
    private val subRecordView = RecordView(context, Record(), RecordCalendarAdapter.Formula.SINGLE_TEXT, 0, 0)
    private var noColor = false
    private var calTitle: String? = null

    init {
        record?.getTitleInCalendar()?.let { calTitle = it }
        recordView.childList = ArrayList()
        recordView.childList?.add(recordView.record)
        recordView.childList?.add(recordView.record)
        recordView.childList?.add(recordView.record)
        subRecordView.childList = ArrayList()
        subRecordView.childList?.add(recordView.record)
        subRecordView.childList?.add(recordView.record)
        when {
            record != null -> {
                recordView.formula = record.getFormula()
                recordView.record.style = record.style
                recordView.record.colorKey = record.colorKey
                recordView.record.symbol = record.symbol
                subRecordView.formula = record.getFormula()
                subRecordView.record.style = record.style
                subRecordView.record.colorKey = record.colorKey
                subRecordView.record.symbol = record.symbol
                if(record.id == "osInstance::") noColor = true
            }
            template != null -> {
                recordView.formula = RecordCalendarAdapter.Formula.styleToFormula(template.style)
                recordView.record.style = template.style
                recordView.record.colorKey = template.colorKey
                recordView.record.symbol = template.symbol
                subRecordView.formula = RecordCalendarAdapter.Formula.styleToFormula(template.style)
                subRecordView.record.style = template.style
                subRecordView.record.colorKey = template.colorKey
                subRecordView.record.symbol = template.symbol
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.container_in_calendar_style_dlg, getScreenSize(context)[0] - dpToPx(50))
        setLayout()
        setOnShowListener {
            drawRecord()
            formulaPicker.scrollToPosition(recordView.record.style % 100)
            shapePicker.scrollToPosition(recordView.record.style / 100)
        }
    }

    private fun setLayout() {
        titleText.text = context.getString(R.string.calendarBlockStyle)
        titleIcon.setImageResource(R.drawable.record_in_calendar_style)

        val cal = Calendar.getInstance()
        dateText.text = cal.get(Calendar.DATE).toString()
        dowText.text = AppDateFormat.dow.format(cal.time)
        dowText.visibility = View.GONE
        holiText.visibility = View.GONE
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
        colorImg.setOnClickListener {
            ColorPickerDialog(recordView.record.colorKey){ colorKey ->
                recordView.record.colorKey = colorKey
                subRecordView.record.colorKey = colorKey
                colorImg.setColorFilter(ColorManager.getColor(colorKey))
                drawRecord()
            }.show(activity.supportFragmentManager, null)
        }


        val symbolRes = SymbolManager.getSymbolResId(recordView.record.symbol)
        if(symbolRes == R.drawable.blank) {
            symbalImg.setColorFilter(AppTheme.disableText)
            symbalImg.setBackgroundResource(R.drawable.circle_stroke_dash)
        }else {
            symbalImg.setColorFilter(AppTheme.secondaryText)
            symbalImg.setBackgroundResource(R.drawable.blank)
        }
        symbalImg.setImageResource(symbolRes)
        symbolBtn.setOnClickListener {
            SymbolPickerDialog(recordView.record.symbol){
                recordView.record.symbol = it.name
                val symbolRes = SymbolManager.getSymbolResId(recordView.record.symbol)
                if(symbolRes == R.drawable.blank) {
                    symbalImg.setColorFilter(AppTheme.disableText)
                    symbalImg.setBackgroundResource(R.drawable.circle_stroke_dash)
                }else {
                    symbalImg.setColorFilter(AppTheme.secondaryText)
                    symbalImg.setBackgroundResource(R.drawable.blank)
                }
                symbalImg.setImageResource(symbolRes)
                drawRecord()
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
            onResult.invoke(recordView.record.style, recordView.record.colorKey, recordView.record.symbol)
            dismiss()
        }
    }

    private val dateWidth = dpToPx(60f)

    private fun drawRecord() {
        if(calTitle.isNullOrEmpty()) {
            when(recordView.formula) {
                RecordCalendarAdapter.Formula.SINGLE_TEXT -> {
                    recordView.record.title = getRandomText(R.array.singleline_texts)
                    subRecordView.record.title = getRandomText(R.array.singleline_texts)
                }
                RecordCalendarAdapter.Formula.MULTI_TEXT -> {
                    recordView.record.title = getRandomText(R.array.multiline_texts)
                    subRecordView.record.title = getRandomText(R.array.multiline_texts)
                }
                RecordCalendarAdapter.Formula.BOTTOM_SINGLE_TEXT -> {
                    recordView.record.title = getRandomText(R.array.range_texts)
                    subRecordView.record.title = getRandomText(R.array.range_texts)
                }
                else -> {}
            }
        }else {
            recordView.record.title = calTitle
            subRecordView.record.title = calTitle
        }

        recordView.setStyle()
        subRecordView.setStyle()

        val topMargin = when {
            recordView.formula == RecordCalendarAdapter.Formula.BOTTOM_SINGLE_TEXT -> CalendarView.dataStartYOffset + dateWidth
            else -> CalendarView.dataStartYOffset
        }

        recordView.length = when(recordView.formula) {
            RecordCalendarAdapter.Formula.SINGLE_TEXT -> 1
            RecordCalendarAdapter.Formula.BOTTOM_SINGLE_TEXT -> 4
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
            RecordCalendarAdapter.Formula.SINGLE_TEXT -> {
                subRecordView.length = 2
                subRecordView.mLeft = 0f
                subRecordView.mRight = dateWidth * 2
                subRecordView.mTop = topMargin
                subRecordView.mBottom = subRecordView.mTop + subRecordView.getViewHeight().toFloat()
                subRecordView.translationX = dateWidth * 2
                subRecordView.translationY = 0f
                shapeLy.visibility = View.VISIBLE
                colorLy.visibility = View.VISIBLE
                symbolBtn.visibility = View.GONE
            }
            RecordCalendarAdapter.Formula.MULTI_TEXT -> {
                subRecordView.length = 1
                subRecordView.mLeft = 0f
                subRecordView.mRight = dateWidth
                subRecordView.mTop = topMargin
                subRecordView.mBottom = subRecordView.mTop + subRecordView.getViewHeight().toFloat()
                subRecordView.translationX = dateWidth * 2
                subRecordView.translationY = 0f
                shapeLy.visibility = View.VISIBLE
                colorLy.visibility = View.VISIBLE
                symbolBtn.visibility = View.GONE
            }
            RecordCalendarAdapter.Formula.BOTTOM_SINGLE_TEXT -> {
                subRecordView.length = 1
                subRecordView.mLeft = 0f
                subRecordView.mRight = dateWidth * 3
                subRecordView.mTop = topMargin - subRecordView.getViewHeight().toFloat()
                subRecordView.mBottom = subRecordView.mTop + subRecordView.getViewHeight().toFloat()
                subRecordView.translationX = 0f
                subRecordView.translationY = 0f
                shapeLy.visibility = View.VISIBLE
                colorLy.visibility = View.VISIBLE
                symbolBtn.visibility = View.GONE
            }
            RecordCalendarAdapter.Formula.DOT, RecordCalendarAdapter.Formula.SYMBOL -> {
                subRecordView.length = 1
                subRecordView.mLeft = 0f
                subRecordView.mRight = dateWidth
                subRecordView.mTop = topMargin
                subRecordView.mBottom = subRecordView.mTop + subRecordView.getViewHeight().toFloat()
                subRecordView.translationX = dateWidth * 2
                subRecordView.translationY = 0f
                shapeLy.visibility = View.GONE
                colorLy.visibility = View.VISIBLE
                if(recordView.formula == RecordCalendarAdapter.Formula.SYMBOL) {
                    symbolBtn.visibility = View.VISIBLE
                }else {
                    symbolBtn.visibility = View.GONE
                }
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
                symbolBtn.visibility = View.GONE
            }
            else -> {}
        }
        if(noColor) {
            colorLy.visibility = View.GONE
            symbolBtn.visibility = View.GONE
        }
        recordView.setLayout()
        subRecordView.setLayout()
        recordView.invalidate()
        subRecordView.invalidate()
    }

}
