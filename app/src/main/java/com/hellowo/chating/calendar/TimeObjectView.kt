package com.hellowo.chating.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.hellowo.chating.R
import com.hellowo.chating.dpToPx


@SuppressLint("ViewConstructor")
class TimeObjectView constructor(context: Context, val timeObject: TimeObject, val cellNum: Int, val Length: Int) : TextView(context) {
    companion object {
        val leftMargin = dpToPx(8)
        val eventTypeSize = dpToPx(16)
        val todoTypeSize = dpToPx(16)
        val memoTypeSize = dpToPx(16)
    }

    var mTextSize = 10f
    var mLeft = 0
    var mTop = 0
    var mRight = 0
    var mBottom = 0
    var mLine = 0
    var mOrder = 0

    init {
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, mTextSize)
        text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
        gravity = Gravity.CENTER_VERTICAL

        when(timeObject.type) {
            0 -> {
                setPadding(leftMargin, 0, 0, 0)
                setLines(1)
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
                    paint.color = timeObject.color
                    paint.isAntiAlias = true
                    it.drawCircle((leftMargin / 2).toFloat(), (height / 2).toFloat(), (leftMargin / 5).toFloat(), paint)
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