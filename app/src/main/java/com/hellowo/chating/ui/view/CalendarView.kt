package com.hellowo.chating.ui.view

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import com.hellowo.chating.R
import com.hellowo.chating.l
import java.util.*

class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ScrollView(context, attrs, defStyleAttr) {
    companion object {
        val maxCellNum = 42
        val tempCal = Calendar.getInstance()
    }

    private val rootLy = FrameLayout(context)
    private val calendarLy = FrameLayout(context)
    private val dateTexts = Array(maxCellNum, { _ -> TextView(context)})

    private val cal = Calendar.getInstance()

    private var calendarHeight = 0
    private val column = 7
    private var rows = 0
    private var startPos = 0
    private var endPos = 0
    private val cellX = FloatArray(maxCellNum, { _ -> 0f})
    private val cellY = FloatArray(maxCellNum, { _ -> 0f})

    init {
        rootLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        addView(rootLy)
        rootLy.addView(calendarLy)
        dateTexts.forEach { calendarLy.addView(it) }
        viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        drawCalendar()
                    }
                })
    }

    private fun drawCalendar() {
        l("==========drawCalendar=========")
        l(height.toString())

        calendarLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, height)
        setCalendarData()

        val minW = width.toFloat() / column
        val minH = height.toFloat() / rows
        val cellNum = column * rows

        tempCal.add(Calendar.DATE, -(startPos + 1))
        for (i in 0 until maxCellNum) {
            if(i < cellNum) {
                cellX[i] = i % column * minW
                cellY[i] = i / column * minH
                tempCal.add(Calendar.DATE, 1)

                dateTexts[i].visibility = View.VISIBLE
                dateTexts[i].text = tempCal.get(Calendar.DATE).toString()
                dateTexts[i].translationX = cellX[i]
                dateTexts[i].translationY = cellY[i]
            }else {
                dateTexts[i].visibility = View.GONE
            }
        }
    }

    private fun setCalendarData() {
        tempCal.timeInMillis = cal.timeInMillis
        tempCal.set(Calendar.DATE, 1)
        startPos = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        endPos = startPos + tempCal.getActualMaximum(Calendar.DATE) - 1
        rows = (endPos + 1) / 7 + if ((endPos + 1) % 7 > 0) 1 else 0
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        calendarHeight = MeasureSpec.getSize(heightMeasureSpec)
        if(calendarHeight > 0) {
        }
    }
}