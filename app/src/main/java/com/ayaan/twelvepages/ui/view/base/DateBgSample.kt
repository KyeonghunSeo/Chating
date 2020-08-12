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
                val alpha = when(link.intParam1){ // 진하기
                    0 -> 90
                    else -> 255
                }
                when(link.intParam0) { // 무늬
                    0 -> {
                        paint.style = Paint.Style.FILL
                        paint.color = record.getColor()
                        paint.alpha = alpha
                        canvas?.drawRoundRect(0f, 0f, width, height, 0f, 0f, paint)
                    }
                    1 -> { // 사선
                        val dashWidth = RecordView.strokeWidth * 4
                        paint.style = Paint.Style.STROKE
                        paint.color = record.getColor()
                        paint.strokeWidth = RecordView.strokeWidth * 1.5f
                        paint.alpha = alpha
                        var x = 0f
                        while (x < width + height) {
                            canvas?.drawLine(x, -RecordView.defaulMargin, x - height, height + RecordView.defaulMargin, paint)
                            x += dashWidth * 2
                        }
                    }
                    2 -> { // 체크
                        paint.style = Paint.Style.FILL
                        paint.color = record.getColor()
                        paint.alpha = alpha
                        //canvas?.drawRoundRect(0f, 0f, width, height, 0f, 0f, paint)

                        val h = height - (RecordView.blockTypeSize + dpToPx(10f))
                        val lineWidth = RecordView.strokeWidth * 4
                        val gap = RecordView.strokeWidth * 5
                        paint.style = Paint.Style.STROKE
                        paint.color = record.getColor()
                        paint.strokeWidth = lineWidth
                        paint.alpha = alpha
                        var x = 0f
                        while (x < width + height) {
                            canvas?.drawLine(x, h, x, height, paint)
                            x += gap * 2
                        }

                        var y = height - (RecordView.blockTypeSize + dpToPx(10f)) + lineWidth
                        while (y < height) {
                            canvas?.drawLine(0f, y, width, y, paint)
                            y += gap * 2
                        }
                    }
                    3 -> { // 땡땡이 무늬
                        paint.isAntiAlias = true
                        paint.style = Paint.Style.FILL
                        paint.color = record.getColor()
                        val circleSize = dpToPx(3.5f)
                        val gap = dpToPx(6f)
                        var cross = true
                        var x = -circleSize/2
                        var y = circleSize/2
                        paint.alpha = alpha
                        while (y < height) {
                            while (x < width) {
                                canvas?.drawCircle(x, y, circleSize/2, paint)
                                x += circleSize + gap
                            }
                            cross = !cross
                            x = -circleSize/2 + if(cross) 0f else (circleSize + gap) / 2
                            y += circleSize + gap
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}