package com.ayaan.twelvepages.ui.view.base

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.l
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.view.CalendarView
import com.ayaan.twelvepages.ui.view.RecordView
import java.util.*


class DateBgSample @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    val paint = Paint()
    var record: Record? = null

    init {
        setWillNotDraw(false)
    }

    fun setDateBg(record: Record) {
        this.record = record
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        record?.let { draw(canvas, paint, it, width.toFloat(), height.toFloat()) }
        super.onDraw(canvas)
    }

    companion object {
        fun draw(canvas: Canvas?, paint: Paint, record: Record, width: Float, height: Float) {
            record.getBgLink()?.let { link ->
                when(link.intParam0) {
                    0 -> {
                        paint.style = Paint.Style.FILL
                        paint.color = record.getColor()
                        paint.alpha = 100
                        canvas?.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 0f, 0f, paint)
                    }
                    1 -> { // 사선
                        val dashWidth = RecordView.strokeWidth * 4
                        paint.style = Paint.Style.STROKE
                        paint.color = record.getColor()
                        paint.strokeWidth = RecordView.strokeWidth * 1.5f
                        paint.alpha = 100
                        var x = 0f
                        paint.alpha = 50
                        while (x < width + height) {
                            canvas?.drawLine(x, -RecordView.defaulMargin, x - height, height + RecordView.defaulMargin, paint)
                            x += dashWidth * 2
                        }
                    }
                    2 -> { // 굵은사선
                        val dashWidth = RecordView.strokeWidth * 12
                        paint.style = Paint.Style.STROKE
                        paint.color = record.getColor()
                        paint.strokeWidth = RecordView.strokeWidth * 5
                        paint.alpha = 100
                        var x = 0f
                        while (x < width + height) {
                            canvas?.drawLine(x, -RecordView.defaulMargin, x - height, height + RecordView.defaulMargin, paint)
                            x += dashWidth * 2
                        }
                        paint.color = ColorManager.getFontColor(record.getColor())
                    }
                    3 -> { // 땡땡이 무늬
                        paint.isAntiAlias = true
                        paint.style = Paint.Style.FILL
                        paint.color = record.getColor()
                        val circleSize = dpToPx(2.5f)
                        val gap = dpToPx(6f)
                        var cross = true
                        var x = 0f
                        var y = gap
                        paint.alpha = 100
                        while (y < height - circleSize) {
                            while (x < width - circleSize) {
                                canvas?.drawCircle(x + circleSize/2, y + circleSize/2, circleSize/2, paint)
                                x += circleSize + gap
                            }
                            cross = !cross
                            x = 0f + if(cross) 0f else gap
                            y += circleSize + gap
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}