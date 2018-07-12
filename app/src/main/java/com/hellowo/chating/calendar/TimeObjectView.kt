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
                setLines(1)
            }
            else -> {}
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let {
            val paint = paint
            paint.style = Paint.Style.FILL
            paint.color = timeObject.color
            paint.isAntiAlias = true
        }
    }

    fun getTypeHeight(): Int = when(timeObject.type) {
        0 -> dpToPx(16)
        1 -> dpToPx(20)
        2 -> dpToPx(20)
        else -> dpToPx(20)
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