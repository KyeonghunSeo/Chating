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
        val strokeWidth = dpToPx(1)
        val radius = dpToPx(2).toFloat()
        val circleRadius = dpToPx(5).toFloat()
        val defaulMargin = dpToPx(5)
        val leftIconSize = dpToPx(16)
        val eventTypeSize = dpToPx(16)
        val todoTypeSize = dpToPx(13)
        val memoTypeSize = dpToPx(13)
        val tempCal = Calendar.getInstance()
    }

    var mTextSize = 9f
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
                setPadding(defaulMargin, 0, defaulMargin, 0)
                setSingleLine(true)
                setHorizontallyScrolling(true)
                setTextColor(Color.WHITE)
            }
            1, 2, 3 -> {
                setPadding(leftIconSize, 0, defaulMargin, 0)
                setSingleLine(true)
                setHorizontallyScrolling(true)
            }
            else -> {}
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            when(timeObject.type) {
                0 -> {
                    val paint = Paint()
                    paint.style = Paint.Style.FILL
                    paint.color = color
                    paint.isAntiAlias = true
                    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                    it.drawRoundRect(rect, radius, radius, paint)
                }
                1 -> {
                    val paint = Paint()
                    paint.style = Paint.Style.FILL
                    paint.strokeWidth = strokeWidth.toFloat()
                    paint.color = color
                    paint.isAntiAlias = true
                    tempCal.timeInMillis = timeObject.dtStart
                    val degreeH = tempCal.get(Calendar.HOUR_OF_DAY) % 12 * 360 / 12 + 270
                    val sX = leftIconSize / 2f
                    val sY = height / 2f
                    val hX = Math.cos(Math.toRadians(degreeH.toDouble())) * (circleRadius - strokeWidth)
                    val hY = Math.sin(Math.toRadians(degreeH.toDouble())) * (circleRadius - strokeWidth)

                    it.drawCircle(sX, sY, circleRadius, paint)
                    paint.color = Color.WHITE
                    it.drawLine(sX, sY, (sX + hX).toFloat(), (sY + hY).toFloat(), paint)
                }
                2 -> {
                    val paint = Paint()
                    paint.style = Paint.Style.FILL
                    paint.strokeWidth = strokeWidth.toFloat()
                    paint.color = color
                    paint.isAntiAlias = true
                    val center = leftIconSize / 2f
                    val rect = RectF(center - circleRadius, center - circleRadius, center + circleRadius, center + circleRadius)
                    it.drawRoundRect(rect, radius, radius, paint)
                    paint.color = Color.WHITE
                    it.drawLine(center - circleRadius / 2, center - circleRadius / 3, center, center + circleRadius / 3, paint)
                    it.drawLine(center, center + circleRadius / 3, center + circleRadius / 2, center - circleRadius / 3, paint)
                }
                3 -> {
                    val paint = Paint()
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = strokeWidth.toFloat()
                    paint.color = color
                    paint.isAntiAlias = true
                    val center = leftIconSize / 2f
                    val rect = RectF(center - circleRadius, center - circleRadius, center + circleRadius, center + circleRadius)
                    it.drawRoundRect(rect, radius, radius, paint)
                }
                else -> {}
            }
        }
        super.onDraw(canvas)
    }

    fun getTypeHeight(): Int = when(timeObject.type) {
        0 -> eventTypeSize
        1 -> todoTypeSize
        2 -> memoTypeSize
        else -> eventTypeSize
    }

    fun setLayout() {
        val lp = FrameLayout.LayoutParams(mRight - mLeft - strokeWidth, mBottom - mTop)
        lp.setMargins(mLeft, mTop, 0, 0)
        layoutParams = lp
    }

    override fun toString(): String {
        return "TimeObjectView(timeObject=$timeObject, cellNum=$cellNum, Length=$Length, mTextSize=$mTextSize, mLeft=$mLeft, mTop=$mTop, mRight=$mRight, mBottom=$mBottom, mLine=$mLine, mOrder=$mOrder)"
    }

}