package com.hellowo.journey.calendar

import android.graphics.*
import com.hellowo.journey.AppRes
import com.hellowo.journey.R
import com.hellowo.journey.l
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
        paint.color = view.color
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
        val stamp = AppRes.resources.getDrawable(R.drawable.stamp)
        stamp.setColorFilter(view.color, PorterDuff.Mode.DST)
        stamp.setBounds(0, 0, view.height, view.height)
        stamp.draw(canvas)
/*
        val icon = AppRes.resources.getDrawable(R.drawable.idea)
        icon.setColorFilter(view.color, PorterDuff.Mode.DST)
        icon.setBounds(0, 0, view.height, view.height)
        icon.draw(canvas)*/
    }
}