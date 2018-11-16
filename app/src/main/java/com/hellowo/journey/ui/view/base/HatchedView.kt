package com.hellowo.journey.ui.view.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.dpToPx


class HatchedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    private val strokeWidth = dpToPx(1f)
    private val dashWidth = dpToPx(2f)
    private val hatchAngle = dpToPx(10f)
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
        while (x < width + hatchAngle) {
            canvas?.drawLine(x, -dashWidth, x - hatchAngle, height + dashWidth, paint)
            x += dashWidth * 2
        }
        super.onDraw(canvas)
    }
}