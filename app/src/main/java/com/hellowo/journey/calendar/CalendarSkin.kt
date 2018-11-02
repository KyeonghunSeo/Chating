package com.hellowo.journey.calendar

import android.graphics.*
import com.hellowo.journey.AppRes
import com.hellowo.journey.R
import com.hellowo.journey.l
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.view.CalendarView
import com.hellowo.journey.ui.view.TimeObjectView

object CalendarSkin {
    var backgroundColor: Int = 0
    var dateColor: Int = 0
    var holiDateColor: Int = 0
    var todayDateColor: Int = 0
    var selectedDateColor: Int = 0
    var selectedBackgroundColor: Int = 0
    var greyColor: Int = 0
    var dateFont = AppRes.regularFont
    var noteFont = AppRes.textFont
    var selectFont = AppRes.regularFont

    fun init(calendarView: CalendarView) {
        val resource = calendarView.context.resources
        backgroundColor = resource.getColor(R.color.calendarBackground)
        dateColor = resource.getColor(R.color.primaryText)
        holiDateColor = resource.getColor(R.color.holiday)
        todayDateColor = resource.getColor(R.color.colorPrimary)
        selectedDateColor = resource.getColor(R.color.primaryText)
        selectedBackgroundColor = resource.getColor(R.color.grey)
        greyColor = resource.getColor(R.color.grey)
    }

    fun drawTerm(canvas: Canvas, view: TimeObjectView) {
        val paint = view.paint
        val width = view.width
        val height = view.height
        paint.color = view.timeObject.color
        canvas.translate(view.scrollX.toFloat(), 0f)
        when(view.timeObject.style){
            1 -> {
                val periodLine = (TimeObjectView.strokeWidth * 2).toInt()
                val rect = RectF(periodLine.toFloat(), height - (periodLine * 4).toFloat(),
                        view.width - periodLine.toFloat(), height - (periodLine * 5).toFloat())
                canvas.drawRect(rect, paint)

                val a = Point(0, height - (periodLine * 4.5f).toInt())
                val b = Point(periodLine * 3, height - periodLine * 2)
                val c = Point(periodLine * 3, height - (periodLine * 7f).toInt())

                val leftArrow = Path()
                leftArrow.fillType = Path.FillType.EVEN_ODD
                leftArrow.moveTo(a.x.toFloat(), a.y.toFloat())
                leftArrow.lineTo(b.x.toFloat(), b.y.toFloat())
                leftArrow.lineTo(c.x.toFloat(), c.y.toFloat())
                leftArrow.lineTo(a.x.toFloat(), a.y.toFloat())
                leftArrow.close()
                canvas.drawPath(leftArrow, paint)

                val e = Point(view.width, height - (periodLine * 4.5f).toInt())
                val f = Point(view.width - periodLine * 3, height - periodLine * 2)
                val g = Point(view.width - periodLine * 3, height - (periodLine * 7f).toInt())

                val rightArrow = Path()
                rightArrow.fillType = Path.FillType.EVEN_ODD
                rightArrow.moveTo(e.x.toFloat(), e.y.toFloat())
                rightArrow.lineTo(f.x.toFloat(), f.y.toFloat())
                rightArrow.lineTo(g.x.toFloat(), g.y.toFloat())
                rightArrow.lineTo(e.x.toFloat(), e.y.toFloat())
                rightArrow.close()
                canvas.drawPath(rightArrow, paint)
            }
            2 -> {
                val periodLine = (TimeObjectView.strokeWidth * 2).toInt()
                val rect = RectF(periodLine.toFloat(), height - (periodLine * 4).toFloat(),
                        view.width - periodLine.toFloat(), height - (periodLine * 5).toFloat())
                canvas.drawRect(rect, paint)

                val a = Point(0, height - (periodLine * 4.5f).toInt())
                val b = Point(periodLine * 3, height - periodLine * 2)
                val c = Point(periodLine * 3, height - (periodLine * 7f).toInt())

                val leftArrow = Path()
                leftArrow.fillType = Path.FillType.EVEN_ODD
                leftArrow.moveTo(a.x.toFloat(), a.y.toFloat())
                leftArrow.lineTo(b.x.toFloat(), b.y.toFloat())
                leftArrow.lineTo(c.x.toFloat(), c.y.toFloat())
                leftArrow.lineTo(a.x.toFloat(), a.y.toFloat())
                leftArrow.close()
                canvas.drawPath(leftArrow, paint)

                val e = Point(view.width, height - (periodLine * 4.5f).toInt())
                val f = Point(view.width - periodLine * 3, height - periodLine * 2)
                val g = Point(view.width - periodLine * 3, height - (periodLine * 7f).toInt())

                val rightArrow = Path()
                rightArrow.fillType = Path.FillType.EVEN_ODD
                rightArrow.moveTo(e.x.toFloat(), e.y.toFloat())
                rightArrow.lineTo(f.x.toFloat(), f.y.toFloat())
                rightArrow.lineTo(g.x.toFloat(), g.y.toFloat())
                rightArrow.lineTo(e.x.toFloat(), e.y.toFloat())
                rightArrow.close()
                canvas.drawPath(rightArrow, paint)
            }
            else -> {
                val periodLine = (TimeObjectView.strokeWidth * 1.5).toInt()
                val rectl = RectF(periodLine.toFloat(),
                        height / 2f - periodLine / 2,
                        view.width / 2 - view.textSpaceWidth / 2 - TimeObjectView.defaulMargin,
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectl, paint)

                val rectr = RectF(view.width / 2 + view.textSpaceWidth / 2 + TimeObjectView.defaulMargin,
                        height / 2f - periodLine / 2,
                        view.width - periodLine.toFloat(),
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectr, paint)

                val arrowSize = periodLine * 5

                if(!view.leftOpen) {
                    val a = Point(0, height / 2)
                    val b = Point(arrowSize, height / 2 - arrowSize)
                    val c = Point(arrowSize, height / 2  + arrowSize)

                    val leftArrow = Path()
                    leftArrow.fillType = Path.FillType.EVEN_ODD
                    leftArrow.moveTo(a.x.toFloat(), a.y.toFloat())
                    leftArrow.lineTo(b.x.toFloat(), b.y.toFloat())
                    leftArrow.lineTo(c.x.toFloat(), c.y.toFloat())
                    leftArrow.lineTo(a.x.toFloat(), a.y.toFloat())
                    leftArrow.close()
                    canvas.drawPath(leftArrow, paint)
                }

                if(!view.rightOpen) {
                    val e = Point(width, height / 2)
                    val f = Point(width - arrowSize, height / 2 - arrowSize)
                    val g = Point(width - arrowSize, height / 2  + arrowSize)

                    val rightArrow = Path()
                    rightArrow.fillType = Path.FillType.EVEN_ODD
                    rightArrow.moveTo(e.x.toFloat(), e.y.toFloat())
                    rightArrow.lineTo(f.x.toFloat(), f.y.toFloat())
                    rightArrow.lineTo(g.x.toFloat(), g.y.toFloat())
                    rightArrow.lineTo(e.x.toFloat(), e.y.toFloat())
                    rightArrow.close()
                    canvas.drawPath(rightArrow, paint)
                }
            }
        }
    }

    fun drawStamp(canvas: Canvas, view: TimeObjectView) {
        val margin = TimeObjectView.defaulMargin.toInt()
        val width = (view.width - TimeObjectView.defaulMargin * 2).toInt()
        val size = (view.height - TimeObjectView.defaulMargin * 2).toInt()
        val totalStampCnt = view.childList?.size ?: 0
        if(totalStampCnt > 0) {
            if(size * totalStampCnt + (margin * (totalStampCnt - 1)) > width) {
                var left = width - size - margin
                val overlap = width / totalStampCnt
                (totalStampCnt - 1 downTo 0).forEach { index ->
                    val circle = AppRes.resources.getDrawable(R.drawable.circle_fill)
                    circle.setColorFilter(view.timeObject.color, PorterDuff.Mode.SRC_ATOP)
                    circle.setBounds(left, 0, left + size, size)
                    circle.draw(canvas)

                    val stamp = AppRes.resources.getDrawable(StampManager.stamps[index])
                    stamp.setColorFilter(view.timeObject.fontColor, PorterDuff.Mode.SRC_ATOP)
                    stamp.setBounds(left + margin, 0 + margin, left + size - margin, size - margin)
                    stamp.draw(canvas)

                    val stroke = AppRes.resources.getDrawable(R.drawable.circle_stroke_1dp)
                    stroke.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                    stroke.setBounds(left - margin, -margin, left + size + margin, size + margin)
                    stroke.draw(canvas)

                    left -= overlap
                }
            }else {
                var left = margin
                view.childList?.forEachIndexed { index, timeObject ->
                    val circle = AppRes.resources.getDrawable(R.drawable.circle_fill)
                    circle.setColorFilter(view.timeObject.color, PorterDuff.Mode.SRC_ATOP)
                    circle.setBounds(left, 0, left + size, size)
                    circle.draw(canvas)

                    val stamp = AppRes.resources.getDrawable(StampManager.stamps[index])
                    stamp.setColorFilter(view.timeObject.fontColor, PorterDuff.Mode.SRC_ATOP)
                    stamp.setBounds(left + margin, 0 + margin, left + size - margin, size - margin)
                    stamp.draw(canvas)
                    left += size + margin
                }
            }
        }
    }

    fun drawNote(canvas: Canvas, view: TimeObjectView) {
        val timeObject = view.timeObject
        val paint = view.paint
        paint.color = timeObject.color
        when(TimeObject.Style.values()[timeObject.style]){
            TimeObject.Style.DEFAULT -> {
                val rect = RectF(TimeObjectView.defaulMargin,
                        TimeObjectView.iconSize / 2f,
                        TimeObjectView.iconSize.toFloat() + TimeObjectView.defaulMargin,
                        TimeObjectView.iconSize / 2f + TimeObjectView.strokeWidth * 2)
                canvas.drawRect(rect, paint)
            }
        }
    }

    fun drawMoney(canvas: Canvas, view: TimeObjectView) {
        val margin = TimeObjectView.defaulMargin.toInt()
        val width = (view.width - TimeObjectView.defaulMargin * 2).toInt()
        val size = (view.height - TimeObjectView.defaulMargin * 2).toInt()
        val totalStampCnt = view.childList?.size ?: 0
        if(totalStampCnt > 0) {
            if(size * totalStampCnt + (margin * (totalStampCnt - 1)) > width) {
                var left = width - size - margin
                val overlap = ((width - size) / (totalStampCnt))
                (totalStampCnt - 1 downTo 0).forEach { index ->
                    val circle = AppRes.resources.getDrawable(R.drawable.circle_fill)
                    circle.setColorFilter(view.timeObject.color, PorterDuff.Mode.SRC_ATOP)
                    circle.setBounds(left, 0, left + size, size)
                    circle.draw(canvas)

                    val stamp = AppRes.resources.getDrawable(StampManager.stamps[index])
                    stamp.setColorFilter(view.timeObject.fontColor, PorterDuff.Mode.SRC_ATOP)
                    stamp.setBounds(left + margin, 0 + margin, left + size - margin, size - margin)
                    stamp.draw(canvas)

                    val stroke = AppRes.resources.getDrawable(R.drawable.circle_stroke_1dp)
                    stroke.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                    stroke.setBounds(left - margin, -margin, left + size + margin, size + margin)
                    stroke.draw(canvas)

                    left -= overlap
                }
            }else {
                var left = margin
                view.childList?.forEachIndexed { index, timeObject ->
                    val circle = AppRes.resources.getDrawable(R.drawable.circle_fill)
                    circle.setColorFilter(view.timeObject.color, PorterDuff.Mode.SRC_ATOP)
                    circle.setBounds(left, 0, left + size, size)
                    circle.draw(canvas)

                    val stamp = AppRes.resources.getDrawable(StampManager.stamps[index])
                    stamp.setColorFilter(view.timeObject.fontColor, PorterDuff.Mode.SRC_ATOP)
                    stamp.setBounds(left + margin, 0 + margin, left + size - margin, size - margin)
                    stamp.draw(canvas)
                    left += size + margin
                }
            }
        }
    }
}