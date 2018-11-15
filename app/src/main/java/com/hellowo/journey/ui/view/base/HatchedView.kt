package com.hellowo.journey.ui.view.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.hellowo.journey.AppRes
import com.hellowo.journey.dpToPx
import com.hellowo.journey.l
import com.hellowo.journey.manager.CalendarSkin

class HatchedView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    private val strokeWidth = dpToPx(1f)
    private val dashWidth = dpToPx(3f)
    private val hatchAngle = dpToPx(50f)
    val paint = Paint()

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.color = AppRes.disableText
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