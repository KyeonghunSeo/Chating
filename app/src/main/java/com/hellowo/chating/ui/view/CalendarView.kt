package com.hellowo.chating.ui.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import com.hellowo.chating.R
import com.hellowo.chating.dpToPx
import com.hellowo.chating.l
import java.util.*

class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ScrollView(context, attrs, defStyleAttr) {
    companion object {
        val maxCellNum = 42
        val tempCal = Calendar.getInstance()

        val dateArea = dpToPx(50)
        val dateTextLayoutParams = FrameLayout.LayoutParams(dateArea, dateArea)
        val dateTextSize = 20f
    }

    private val rootLy = FrameLayout(context)
    private val calendarLy = FrameLayout(context)
    private val dateTexts = Array(maxCellNum, { _ -> TextView(context)})

    private val cal = Calendar.getInstance()

    private val column = 7
    private var rows = 0
    private var startPos = 0
    private var endPos = 0
    private val cellX = FloatArray(maxCellNum, { _ -> 0f})
    private val cellY = FloatArray(maxCellNum, { _ -> 0f})
    var onDrawed: ((Calendar) -> Unit)? = null

    init {
        rootLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        addView(rootLy)
        rootLy.addView(calendarLy)
        dateTexts.forEach {
            setDateTextLook(it)
            calendarLy.addView(it)
        }
        viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        drawCalendar()
                    }
                })
    }

    private fun drawCalendar() {
        l("==========START drawCalendar=========")
        l(""+ cal.get(Calendar.YEAR) + "년" + (cal.get(Calendar.MONTH) + 1) + "월" + cal.get(Calendar.DATE) + "일")
        l("캘린더 높이 : " + height.toString())

        setCalendarData()
        calendarLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, height)

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

        onDrawed?.invoke(cal)
        l("==========END drawCalendar=========")
    }

    private fun setCalendarData() {
        tempCal.timeInMillis = cal.timeInMillis
        tempCal.set(Calendar.DATE, 1)
        startPos = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        endPos = startPos + tempCal.getActualMaximum(Calendar.DATE) - 1
        rows = (endPos + 1) / 7 + if ((endPos + 1) % 7 > 0) 1 else 0
    }

    private fun setDateTextLook(textView: TextView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, dateTextSize)
        //textView.setTypeface(null, Typeface.BOLD)
        textView.gravity = Gravity.CENTER
        textView.layoutParams = dateTextLayoutParams
    }

    fun moveMonth(offset: Int) {
        cal.add(Calendar.MONTH, offset)
        drawCalendar()
    }
}