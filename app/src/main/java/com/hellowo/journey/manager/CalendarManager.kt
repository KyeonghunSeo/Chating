package com.hellowo.journey.manager

import android.graphics.*
import com.hellowo.journey.App.Companion.resource
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.model.Record
import com.hellowo.journey.ui.view.RecordView
import com.hellowo.journey.ui.view.RecordView.Companion.blockTypeSize
import com.hellowo.journey.ui.view.RecordView.Companion.leftPadding
import com.hellowo.journey.ui.view.RecordView.Companion.defaulMargin
import com.hellowo.journey.ui.view.RecordView.Companion.sidePadding
import com.hellowo.journey.ui.view.RecordView.Companion.dotSize
import com.hellowo.journey.ui.view.RecordView.Companion.rectRadius
import com.hellowo.journey.ui.view.RecordView.Companion.stampSize
import com.hellowo.journey.ui.view.RecordView.Companion.strokeWidth
import com.hellowo.journey.model.Record.Style.*
import com.hellowo.journey.ui.view.RecordView.Companion.baseSize
import com.pixplicity.easyprefs.library.Prefs

object CalendarManager {
    var backgroundColor: Int = 0
    var dateColor: Int = 0
    var sundayColor: Int = 0
    var saturdayColor: Int = 0
    var selectedDateColor: Int = 0

    init {
        backgroundColor = AppTheme.backgroundColor
        dateColor = AppTheme.primaryColor
        sundayColor = Prefs.getInt("sundayColor", AppTheme.redColor)
        saturdayColor = Prefs.getInt("saturdayColor", AppTheme.primaryColor)
        selectedDateColor = AppTheme.primaryColor
    }

    fun drawBasicShape(canvas: Canvas, view: RecordView) {
        val paint = view.paint
        val width = view.width
        val height = view.height
        paint.color = view.paintColor
        when(Record.Style.values()[view.record.style]){
            RECT_FILL -> {
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 0f, 0f, paint)
                paint.color = view.fontColor
            }
            RECT_STROKE -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth * 0.8f
                canvas.drawRect(strokeWidth / 2, strokeWidth / 2,
                        width.toFloat() - strokeWidth / 2, height.toFloat() - strokeWidth / 2, paint)
                paint.style = Paint.Style.FILL
            }
            ROUND_STROKE -> {
                paint.isAntiAlias = true
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth * 1f
                canvas.drawRoundRect(strokeWidth / 2, strokeWidth / 2,
                        width.toFloat() - strokeWidth / 2, height.toFloat() - strokeWidth / 2,
                        height / 2f, height / 2f, paint)
                paint.style = Paint.Style.FILL
            }
            ROUND_FILL -> {
                paint.isAntiAlias = true
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), height / 2f, height / 2f, paint)
                paint.color = view.fontColor
            }
            CANDY -> {
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), rectRadius, rectRadius, paint)
                val dashWidth = strokeWidth * 6
                paint.strokeWidth = strokeWidth * 5
                paint.color = Color.parseColor("#30FFFFFF")
                var x = 0f
                while (x < width + height) {
                    canvas.drawLine(x, -defaulMargin, x - height, height + defaulMargin, paint)
                    x += dashWidth * 2
                }
                paint.color = view.fontColor
            }
            HATCHED -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth * 2
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                paint.strokeWidth = strokeWidth * 1
                val dashWidth = strokeWidth * 2
                var x = 0f
                paint.alpha = 50
                while (x < width + height) {
                    canvas.drawLine(x, -defaulMargin, x - height, height + defaulMargin, paint)
                    x += dashWidth * 2
                }
                paint.alpha = 255
                paint.style = Paint.Style.FILL
            }
            TOP_LINE -> {
                val strokeWidth = strokeWidth * 1f
                canvas.drawRect(0f, 0f, width.toFloat(), strokeWidth, paint)
            }
            BOTTOM_LINE -> {
                val strokeWidth = strokeWidth * 0.8f
                canvas.drawRect(0f, height.toFloat() - strokeWidth, width.toFloat(), height.toFloat(), paint)
            }
            else -> {
                if(view.length > 1) {
                    paint.alpha = 17
                    canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 0f, 0f, paint)
                    paint.alpha = 255
                }
            }
        }
        if(view.record.isSetCheckBox()) {
            drawCheckBox(view, paint, canvas)
        }
    }

    fun drawNote(canvas: Canvas, view: RecordView) {
        val timeObject = view.record
        val paint = view.paint
        val width = view.width
        val height = view.height
        paint.color = timeObject.getColor()
        when(Record.Style.values()[view.record.style]){
            RECT_STROKE -> { // rect stroke
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                canvas.drawRect(rect, paint)
                paint.style = Paint.Style.FILL
            }
            HATCHED -> { // hatched
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                val dashWidth = strokeWidth
                var x = 0f
                paint.strokeWidth = strokeWidth / 2
                paint.alpha = 30
                while (x < width + height) {
                    canvas.drawLine(x, -defaulMargin, x - height, height + defaulMargin, paint)
                    x += dashWidth * 4
                }
                paint.alpha = 255
                paint.style = Paint.Style.FILL
            }

        }
        drawHyphen(view, paint, canvas)
    }

    fun drawTerm(canvas: Canvas, view: RecordView) {
        val paint = view.paint
        val width = view.width
        val height = view.height
        paint.color = view.record.getColor()
        canvas.translate(view.scrollX.toFloat(), 0f)
        when(view.record.style){
            1 -> { // 양쪽 얇은 화살
                val periodLine = (strokeWidth * 3).toInt()
                val rectl = RectF(0f,
                        height / 2f - periodLine / 2,
                        width / 2 - view.textSpaceWidth / 2 - sidePadding,
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectl, paint)

                val rectr = RectF(width / 2 + view.textSpaceWidth / 2 + sidePadding,
                        height / 2f - periodLine / 2,
                        width.toFloat(),
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectr, paint)

                canvas.drawRect(0f, height / 2f - periodLine * 2,
                        periodLine.toFloat(), height / 2f + periodLine * 2, paint)

                canvas.drawRect(width - periodLine.toFloat(), height / 2f - periodLine * 2,
                        width.toFloat(), height / 2f + periodLine * 2, paint)
            }
            2 -> { // 두꺼운 화살표
                paint.alpha = 150
                val periodLine = (strokeWidth * 7.5).toInt()
                val rectl = RectF(periodLine * 2f,
                        height / 2f - periodLine / 2,
                        width / 2 - view.textSpaceWidth / 2 - sidePadding,
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectl, paint)

                val rectr = RectF(width / 2 + view.textSpaceWidth / 2 + sidePadding,
                        height / 2f - periodLine / 2,
                        width - periodLine * 2f,
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectr, paint)

                if(!view.leftOpen) {
                    val a = Point(0, height / 2 + periodLine / 2)
                    val b = Point(periodLine * 2, height / 2 - periodLine)
                    val c = Point(periodLine * 2, height / 2 + periodLine / 2)

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
                    val e = Point(width, height / 2 + periodLine / 2)
                    val f = Point(width - periodLine * 2, height / 2 - periodLine)
                    val g = Point(width - periodLine * 2, height / 2 + periodLine / 2)

                    val rightArrow = Path()
                    rightArrow.fillType = Path.FillType.EVEN_ODD
                    rightArrow.moveTo(e.x.toFloat(), e.y.toFloat())
                    rightArrow.lineTo(f.x.toFloat(), f.y.toFloat())
                    rightArrow.lineTo(g.x.toFloat(), g.y.toFloat())
                    rightArrow.lineTo(e.x.toFloat(), e.y.toFloat())
                    rightArrow.close()
                    canvas.drawPath(rightArrow, paint)
                }
                paint.alpha = 255
            }
            else -> {
                val periodLine = (strokeWidth * 1.5).toInt()

                var textLPos = view.width / 2 - view.textSpaceWidth / 2 - defaulMargin
                if(textLPos < view.paddingLeft) textLPos = view.paddingLeft.toFloat()

                var textRPos = view.width / 2 + view.textSpaceWidth / 2 + defaulMargin
                if(textRPos > view.width - view.paddingRight) textRPos = view.width - view.paddingRight.toFloat()

                val rectl = RectF(periodLine.toFloat(),
                        height / 2f - periodLine / 2,
                        textLPos,
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectl, paint)

                val rectr = RectF(textRPos,
                        height / 2f - periodLine / 2,
                        view.width - periodLine.toFloat(),
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectr, paint)

                val arrowSize = periodLine * 2

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

    fun drawStamp(canvas: Canvas, view: RecordView) {
        val margin = defaulMargin.toInt()
        val size = (stampSize - defaulMargin).toInt()
        var top = 0
        var left = 0
        view.childList?.forEachIndexed { index, timeObject ->

            val circle = resource.getDrawable(R.drawable.primary_rect_fill_radius_1)
            circle.setColorFilter(timeObject.getColor(), PorterDuff.Mode.SRC_ATOP)
            circle.setBounds(left + 1, top + 1, left + size - 1, top + size - 1)
            circle.draw(canvas)

            val stamp = resource.getDrawable(StampManager.stamps[index])
            stamp.setColorFilter(AppTheme.getFontColor(timeObject.colorKey), PorterDuff.Mode.SRC_ATOP)
            stamp.setBounds(left + margin, top + margin, left + size - margin, top + size - margin)
            stamp.draw(canvas)
/*
            val stroke = resource.getDrawable(R.drawable.circle_stroke_1px)
            stroke.setColorFilter(record.getColor(), PorterDuff.Mode.SRC_ATOP)
            stroke.setBounds(left, top, left + size, top + size)
            stroke.draw(canvas)
*/
            left += size + margin
            if(left + size >= view.width) {
                top += stampSize
                left = 0
            }
        }
        /* 겹치기
        if(totalStampCnt > 0) {
            if(size * totalStampCnt + (margin * (totalStampCnt - 1)) > width) {
                var right = left + width
                val overlap = size - (width - size * totalStampCnt) / (1 - totalStampCnt)
                (totalStampCnt - 1 downTo 0).forEach { index ->
                    view.childList?.get(index)?.let { record ->
                        val circle = resource.getDrawable(R.drawable.circle_fill)
                        circle.setColorFilter(CalendarManager.backgroundColor, PorterDuff.Mode.SRC_ATOP)
                        circle.setBounds(right - size + 1, 1, right - 1, size - 1)
                        circle.draw(canvas)

                        val stamp = resource.getDrawable(StampManager.stamps[index])
                        stamp.setColorFilter(record.getColor(), PorterDuff.Mode.SRC_ATOP)
                        stamp.setBounds(right - size + margin, margin, right - margin, size - margin)
                        stamp.draw(canvas)

                        val stroke = resource.getDrawable(R.drawable.circle_stroke_1dp)
                        stroke.setColorFilter(record.getColor(), PorterDuff.Mode.SRC_ATOP)
                        stroke.setBounds(right - size, 0, right, size)
                        stroke.draw(canvas)

                        right -= overlap
                    }
                }
            }
        }
        */
    }

    private fun drawDot(view: RecordView, paint: Paint, canvas: Canvas) {
        val radius = baseSize * 2f
        val centerY = (blockTypeSize - defaulMargin) / 2f
        canvas.drawCircle(radius, centerY, radius, paint)
    }

    private fun drawCheckBox(view: RecordView, paint: Paint, canvas: Canvas) {
        val radius = dotSize / 2f
        val sWidth = strokeWidth / 1.7f
        val centerX = leftPadding / 2f + defaulMargin / 1.3f
        val centerY = (blockTypeSize - defaulMargin) / 2f

        if(view.record.isDone()) {
            view.paintFlags = view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            /*canvas.drawLine(leftPadding.toFloat() / 2f + defaulMargin + radius, centerY - radius,
                    leftPadding.toFloat() / 2f + defaulMargin - radius, centerY + radius, paint)*/
            canvas.drawRect(centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius, paint)
        }else {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = sWidth
            canvas.drawRect(centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius, paint)
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawHyphen(view: RecordView, paint: Paint, canvas: Canvas) {
        val radius = dotSize / 2.3f
        val centerY = blockTypeSize / 2.1f
        canvas.drawRect(leftPadding.toFloat() / 2f + defaulMargin - radius,
                centerY - strokeWidth / 2.0f,
                leftPadding.toFloat() / 2f + defaulMargin + radius,
                centerY + strokeWidth / 2.0f, paint)
    }

    fun drawNotInCalendar(view: RecordView, paint: Paint, canvas: Canvas) {
        val legnth =  baseSize * 10
        val gap = baseSize * 2
        val startY = (blockTypeSize - defaulMargin) / 2f - legnth / 2f
        paint.strokeWidth = gap
        view.childList?.forEachIndexed { index, timeObject ->
            if(index < 8) {
                val startX = sidePadding + (index * gap * 2)
                paint.color = timeObject.getColor()
                canvas.drawLine(startX, startY, startX, startY + legnth, paint)
            }
        }
    }

    private fun drawRectCheckBox(view: RecordView, centerY: Float, canvas: Canvas) {
        if(view.record.isDone()) {
            view.paintFlags = view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val check = resource.getDrawable(R.drawable.check)
            check.setColorFilter(view.record.getColor(), PorterDuff.Mode.SRC_ATOP)
            check.setBounds(defaulMargin.toInt() * 2, (centerY - leftPadding / 2f).toInt(),
                    leftPadding + defaulMargin.toInt() * 2, (centerY + leftPadding / 2f).toInt())
            check.draw(canvas)
        }else {
            val check = resource.getDrawable(R.drawable.uncheck)
            check.setColorFilter(view.record.getColor(), PorterDuff.Mode.SRC_ATOP)
            check.setBounds(defaulMargin.toInt() * 2, (centerY - leftPadding / 2f).toInt(),
                    leftPadding + defaulMargin.toInt() * 2, (centerY + leftPadding / 2f).toInt())
            check.draw(canvas)
        }
    }

    private fun drawWhiteSpaceLine(view: RecordView, centerY: Float, paint: Paint, canvas: Canvas) {
        if(view.length > 1) {
            canvas.drawRect(leftPadding + defaulMargin + view.textSpaceWidth + sidePadding,
                    centerY - strokeWidth / 2, view.width.toFloat(), centerY + strokeWidth / 2, paint)
            canvas.drawRect(view.width - strokeWidth, centerY - strokeWidth * 2,
                    view.width.toFloat(), centerY + strokeWidth * 2, paint)
        }
    }

    /*  // 화살표 모양
    if(view.leftOpen) {
        left = edge
        val path = Path()
        path.moveTo(0f, 0f)
        path.lineTo(edge, 0f)
        path.lineTo(edge, height.toFloat())
        path.lineTo(0f, height.toFloat())
        path.lineTo(edge, height / 2f)
        path.lineTo(0f, 0f)
        path.close()
        canvas.drawPath(path, paint)
    }
    if(view.rightOpen) {
        right = width.toFloat() - edge
        val path = Path()
        path.moveTo(right, 0f)
        path.lineTo(right + edge, height * 0.5f)
        path.lineTo(right, height.toFloat())
        path.lineTo(right, 0f)
        path.close()
        canvas.drawPath(path, paint)
    }*/

    /*
                4 -> { // 시계
                    val paint = Paint()
                    paint.style = Paint.Style.TOP_STACK
                    paint.strokeWidth = strokeWidth.toFloat()
                    paint.color = color
                    paint.isAntiAlias = true
                    tempCal.timeInMillis = record.dtStart
                    val degreeH = tempCal.get(Calendar.HOUR_OF_DAY) % 12 * 360 / 12 + 270
                    val sX = normalTypeSize / 2f
                    val sY = height / 2f
                    val hX = Math.cos(Math.toRadians(degreeH.toDouble())) * (circleRadius - strokeWidth)
                    val hY = Math.sin(Math.toRadians(degreeH.toDouble())) * (circleRadius - strokeWidth)

                    it.drawCircle(sX, sY, circleRadius, paint)
                    paint.color = Color.WHITE
                    it.drawLine(sX, sY, (sX + hX).toFloat(), (sY + hY).toFloat(), paint)
                }
                5 -> { // 타일 모드
                    val bitmap = BitmapFactory.decodeResource(resource, R.drawable.p1)
                    val bitmapDrawable = BitmapDrawable(resource, bitmap)
                    bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                    bitmapDrawable.setTargetDensity(it)
                    it.drawBitmap(bitmapDrawable.bitmap, 0f, 0f, Paint())
                }

                MEMO -> { // memo
                paint.strokeWidth = strokeWidth
                val left = 0f
                val top = 0f
                val right = width.toFloat()
                val bottom = height.toFloat()
                val edge = sidePadding

                var path = Path()
                path.moveTo(left, top)
                path.lineTo(right, top)
                path.lineTo(right, bottom - edge)
                path.lineTo(right - edge, bottom - edge)
                path.lineTo(right - edge, bottom)
                path.lineTo(left, bottom)
                path.lineTo(left, top)
                paint.style = Paint.Style.FILL
                paint.alpha = 30
                canvas.drawPath(path, paint)

                path = Path()
                path.moveTo(right, bottom - edge)
                path.lineTo(right - edge, bottom - edge)
                path.lineTo(right - edge, bottom)
                path.lineTo(right, bottom - edge)
                paint.style = Paint.Style.FILL
                paint.alpha = 255
                canvas.drawPath(path, paint)

                path = Path()
                path.moveTo(right - edge, bottom - edge)
                path.lineTo(right - edge * 2, bottom)
                path.lineTo(right - edge, bottom)
                path.lineTo(right - edge, bottom - edge)
                path.close()
                paint.style = Paint.Style.FILL
                paint.alpha = 50
                canvas.drawPath(path, paint)
                paint.alpha = 255
            }

            RECT_DASH -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                val path = Path()
                path.addRect(strokeWidth / 2, strokeWidth / 2,
                        width.toFloat() - strokeWidth / 2, height.toFloat() - strokeWidth / 2, Path.Direction.CW)
                val dashPath = DashPathEffect(floatArrayOf(dotSize.toFloat(), defaulMargin * 2), 0f)
                paint.pathEffect = dashPath
                canvas.drawPath(path, paint)
                paint.pathEffect = null
                paint.style = Paint.Style.FILL
            }

                */
}