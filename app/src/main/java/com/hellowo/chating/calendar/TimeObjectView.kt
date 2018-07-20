package com.hellowo.chating.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.hellowo.chating.*
import java.util.*


@SuppressLint("ViewConstructor")
class TimeObjectView constructor(context: Context, val timeObject: TimeObject, val cellNum: Int, val Length: Int) : TextView(context) {
    companion object {
        val strokeWidth = dpToPx(1).toFloat()
        val circleRadius = dpToPx(5).toFloat()
        val leftMargin = dpToPx(16)
        val eventTypeSize = dpToPx(16)
        val todoTypeSize = dpToPx(16)
        val memoTypeSize = dpToPx(16)
        val tempCal = Calendar.getInstance()
    }

    var mTextSize = 10f
    var mLeft = 0
    var mTop = 0
    var mRight = 0
    var mBottom = 0
    var mLine = 0
    var mOrder = 0
    var color = CalendarSkin.dateColor

    init {
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, mTextSize)
        text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
        gravity = Gravity.CENTER_VERTICAL

        when(timeObject.type) {
            0 -> {
                setPadding(leftMargin, 0, 0, 0)
                setSingleLine(true)
                setHorizontallyScrolling(true)
            }
            1 -> {
                setPadding(leftMargin, 0, 0, 0)
                setSingleLine(true)
                setHorizontallyScrolling(true)
            }
            else -> {}
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            when(timeObject.type) {
                0 -> {
                    val paint = paint
                    paint.style = Paint.Style.FILL
                    paint.color = color
                    paint.isAntiAlias = true
                    it.drawCircle(leftMargin / 2f, height / 2f, leftMargin / 8f, paint)
                }
                1 -> {
                    val paint = paint
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = strokeWidth
                    paint.color = color
                    paint.isAntiAlias = true
                    tempCal.timeInMillis = System.currentTimeMillis()
                    //tempCal.timeInMillis = timeObject.dtStart
                    val degreeH = tempCal.get(Calendar.HOUR_OF_DAY) % 12 * 360 / 12 + 270
                    val sX = leftMargin / 2f
                    val sY = height / 2f
                    val hX = Math.cos(Math.toRadians(degreeH.toDouble())) * (circleRadius - strokeWidth)
                    val hY = Math.sin(Math.toRadians(degreeH.toDouble())) * (circleRadius - strokeWidth)

                    it.drawCircle(sX, sY, circleRadius, paint)
                    it.drawLine(sX, sY, (sX + hX).toFloat(), (sY + hY).toFloat(), paint)
                }
                else -> {}
            }
        }
    }

    fun getTypeHeight(): Int = when(timeObject.type) {
        0 -> eventTypeSize
        1 -> todoTypeSize
        2 -> memoTypeSize
        else -> eventTypeSize
    }

    fun setLayout() {
        val lp = FrameLayout.LayoutParams(mRight - mLeft, mBottom - mTop)
        lp.setMargins(mLeft, mTop, 0, 0)
        layoutParams = lp
    }

    override fun toString(): String {
        return "TimeObjectView(timeObject=$timeObject, cellNum=$cellNum, Length=$Length, mTextSize=$mTextSize, mLeft=$mLeft, mTop=$mTop, mRight=$mRight, mBottom=$mBottom, mLine=$mLine, mOrder=$mOrder)"
    }

}