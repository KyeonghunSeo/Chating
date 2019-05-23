package com.ayaan.twelvepages.ui.view.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx


class HatchedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    private val strokeWidth = dpToPx(1f)
    private val dashWidth = dpToPx(2f)
    val paint = Paint()

    init {
        paint.style = Paint.Style.STROKE

        val arr = context.obtainStyledAttributes(attrs, R.styleable.HatchedView)
        val style = arr.getString(R.styleable.HatchedView_style)
        if (style != null) {
            when(style) {
                "normal" -> {
                    paint.strokeWidth = strokeWidth
                    paint.color = AppTheme.primaryText
                }
            }
        }else {
            paint.strokeWidth = strokeWidth
            paint.color = AppTheme.disableText
        }
        arr.recycle()
    }

    override fun onDraw(canvas: Canvas?) {
        var x = 0f
        while (x < width + height * 2) {
            canvas?.drawLine(x, -dashWidth, x - height * 2, height + dashWidth, paint)
            x += dashWidth * 2
        }
        super.onDraw(canvas)
    }
}