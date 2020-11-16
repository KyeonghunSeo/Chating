package com.ayaan.twelvepages.ui.view.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.FrameLayout
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.l
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.view.CalendarView
import com.ayaan.twelvepages.ui.view.RecordView


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
                    0 -> 50
                    1 -> 100
                    else -> 200
                }
                val top = when(link.intParam2){ // 영역
                    0 -> 0f
                    1 -> height - CalendarView.dataStartYOffset
                    else -> 0f
                }
                val h = when(link.intParam2) {
                    0 -> height
                    1 -> CalendarView.dataStartYOffset
                    else -> CalendarView.dataStartYOffset
                }
                val bottom = top + h

                paint.pathEffect = null
                paint.color = record.getColor()
                paint.alpha = alpha
                paint.isAntiAlias = false

                when(link.intParam0) { // 무늬
                    0 -> {
                        paint.style = Paint.Style.FILL
                        canvas?.drawRoundRect(0f, top, width, bottom, 0f, 0f, paint)
                    }
                    1 -> { // 사선
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = RecordView.strokeWidth * 1.5f
                        val dashWidth = RecordView.strokeWidth * 4
                        var x = 0f
                        while (x < width + bottom) {
                            canvas?.drawLine(x, top -RecordView.defaulMargin, x - h, bottom + RecordView.defaulMargin, paint)
                            x += dashWidth * 2
                        }
                    }
                    2 -> { // 체크
                        val gap = RecordView.strokeWidth * 5
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = RecordView.strokeWidth * 4
                        var x = 0f
                        while (x < width) {
                            canvas?.drawLine(x, top, x, bottom, paint)
                            x += gap * 2
                        }

                        var y = top + paint.strokeWidth
                        while (y < bottom) {
                            canvas?.drawLine(0f, y, width, y, paint)
                            y += gap * 2
                        }
                    }
                    3 -> { // 땡땡이 무늬
                        paint.isAntiAlias = true
                        paint.style = Paint.Style.FILL
                        val circleSize = dpToPx(3.5f)
                        val gap = dpToPx(6f)
                        var cross = true
                        var x = circleSize/2
                        var y = top + circleSize
                        while (y < h + top) {
                            while (x < width) {
                                canvas?.drawCircle(x, y, circleSize/2, paint)
                                x += circleSize + gap
                            }
                            cross = !cross
                            x = circleSize/2 + if(cross) 0f else (circleSize + gap) / 2
                            y += circleSize + gap
                        }
                    }
                    4, 5, 6 -> { // 왼쪽 사선
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = RecordView.strokeWidth * 2.0f
                        if(link.intParam0 == 4 || link.intParam0 == 6) canvas?.drawLine(0f, top, width, bottom, paint)
                        if(link.intParam0 == 5 || link.intParam0 == 6) canvas?.drawLine(width, top, 0f, bottom, paint)
                        return@let
                    }
                    7, 8 -> { // 테두리
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = RecordView.strokeWidth * 3.0f
                        if(link.intParam0 == 8) paint.pathEffect = DashPathEffect(floatArrayOf(dpToPx(4.0f), dpToPx(2.0f)), 2f)
                        canvas?.drawRoundRect(0f, top, width, bottom, 0f, 0f, paint)
                    }
                    else -> {}
                }
            }
        }
    }
}