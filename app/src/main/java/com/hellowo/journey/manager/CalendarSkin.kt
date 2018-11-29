package com.hellowo.journey.manager

import android.graphics.*
import android.view.Gravity
import com.hellowo.journey.App.Companion.resource
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.ui.view.TimeObjectView
import com.hellowo.journey.ui.view.TimeObjectView.Companion.checkSize
import com.hellowo.journey.ui.view.TimeObjectView.Companion.defaulMargin
import com.hellowo.journey.ui.view.TimeObjectView.Companion.defaultPadding
import com.hellowo.journey.ui.view.TimeObjectView.Companion.dotSize
import com.hellowo.journey.ui.view.TimeObjectView.Companion.rectRadius
import com.hellowo.journey.ui.view.TimeObjectView.Companion.stampSize
import com.hellowo.journey.ui.view.TimeObjectView.Companion.strokeWidth

object CalendarSkin {
    var backgroundColor: Int = 0
    var dateColor: Int = 0
    var sundayColor: Int = 0
    var saturdayColor: Int = 0
    var todayDateColor: Int = 0
    var selectedDateColor: Int = 0
    var selectedBackgroundColor: Int = 0
    var greyColor: Int = 0
    var dateFont = AppTheme.regularFont
    var selectFont = AppTheme.boldFont

    init {
        backgroundColor = AppTheme.backgroundColor
        dateColor = resource.getColor(R.color.primaryText)
        sundayColor = resource.getColor(R.color.holiday)
        saturdayColor = resource.getColor(R.color.blue)
        todayDateColor = resource.getColor(R.color.primaryText)
        selectedDateColor = resource.getColor(R.color.primaryText)
        selectedBackgroundColor = resource.getColor(R.color.grey)
        greyColor = resource.getColor(R.color.grey)
    }

    fun drawEvent(canvas: Canvas, view: TimeObjectView) {
        val paint = view.paint
        val width = view.width
        val height = view.height
        paint.color = view.timeObject.getColor()
        when(view.timeObject.style){
            0 -> { // 동그란 점 시작
                val radius = defaulMargin * 1.5f
                val centerX = (checkSize + defaulMargin) / 2f
                val centerY = height / 2f
                canvas.drawCircle(centerX, centerY, radius, paint)
                view.gravity = Gravity.CENTER_VERTICAL
                if(view.length > 1) drawWhiteSpaceLine(view, centerY, paint, canvas)
            }
            1 -> { // 네모난 점 시작
                val radius = defaulMargin
                val centerX = (checkSize + defaulMargin) / 2f
                val centerY = height / 2f
                canvas.drawRect(centerX - dotSize / 2, centerY - radius,
                        centerX + dotSize / 2, centerY + radius, paint)
                if(view.length > 1) drawWhiteSpaceLine(view, centerY, paint, canvas)
            }
            2 -> { // round stroke
                paint.isAntiAlias = true
                paint.style = Paint.Style.STROKE
                val strokeWidth = defaulMargin
                paint.strokeWidth = strokeWidth
                val rect = RectF(strokeWidth / 2, strokeWidth / 2,
                        width.toFloat() - strokeWidth / 2, height.toFloat() - strokeWidth / 2)
                canvas.drawRoundRect(rect, height / 2f, height / 2f, paint)
                paint.style = Paint.Style.FILL

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
            }
            3 -> { // round fill
                paint.isAntiAlias = true
                val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                canvas.drawRoundRect(rect, height / 2f, height / 2f, paint)
            }
            4 -> { // rect stroke
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                val rect = RectF(strokeWidth / 2, strokeWidth / 2,
                        width.toFloat() - strokeWidth / 2, height.toFloat() - strokeWidth / 2)
                canvas.drawRect(rect, paint)
                paint.style = Paint.Style.FILL
            }
            5 -> { // rect fill
                val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                canvas.drawRect(rect, paint)
            }
            6 -> { // round hatched
                val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                canvas.drawRoundRect(rect, rectRadius, rectRadius, paint)

                val dashWidth = strokeWidth * 6
                paint.strokeWidth = strokeWidth * 5
                paint.color = Color.parseColor("#30FFFFFF")
                var x = 0f
                while (x < width + height) {
                    canvas.drawLine(x, -defaulMargin, x - height, height + defaulMargin, paint)
                    x += dashWidth * 2
                }
            }
            7 -> { // rect hatched
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth * 2
                val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                canvas.drawRect(rect, paint)

                val dashWidth = strokeWidth * 2
                var x = 0f
                paint.strokeWidth = strokeWidth
                paint.alpha = 50
                while (x < width + height) {
                    canvas.drawLine(x, -defaulMargin, x - height, height + defaulMargin, paint)
                    x += dashWidth * 2
                }
                paint.alpha = 255
                paint.style = Paint.Style.FILL
            }
            8 -> { // top line
                val rect = RectF(0f, 0f, width.toFloat(), strokeWidth)
                canvas.drawRect(rect, paint)
            }
            9 -> { // bottom line
                val rect = RectF(0f, height.toFloat() - strokeWidth, width.toFloat(), height.toFloat())
                canvas.drawRect(rect, paint)
            }
        }
    }

    private fun drawWhiteSpaceLine(view: TimeObjectView, centerY: Float, paint: Paint, canvas: Canvas) {
        canvas.drawRect(checkSize + defaulMargin + view.textSpaceWidth + defaultPadding,
                centerY - strokeWidth / 2, view.width.toFloat(), centerY + strokeWidth / 2, paint)
        canvas.drawRect(view.width - strokeWidth, centerY - strokeWidth * 2,
                view.width.toFloat(), centerY + strokeWidth * 2, paint)
    }

    fun drawTask(canvas: Canvas, view: TimeObjectView) {
        val paint = view.paint
        val width = view.width
        val height = view.height
        paint.color = view.timeObject.getColor()
        when(view.timeObject.style) {
            0 -> {
                val centerY = height / 2f
                drawRoundCheckBox(view, centerY, canvas)
                if(view.length > 1) drawWhiteSpaceLine(view, centerY, paint, canvas)
            }
            1 -> {
                val centerY = height / 2f
                drawRectCheckBox(view, centerY, canvas)
                if(view.length > 1) drawWhiteSpaceLine(view, centerY, paint, canvas)
            }
            2 -> {
                paint.isAntiAlias = true
                paint.style = Paint.Style.STROKE
                val strokeWidth = defaulMargin
                paint.strokeWidth = strokeWidth
                val rect = RectF(strokeWidth / 2, strokeWidth / 2,
                        width.toFloat() - strokeWidth / 2, height.toFloat() - strokeWidth / 2)
                canvas.drawRoundRect(rect, height / 2f, height / 2f, paint)
                paint.style = Paint.Style.FILL

                val centerY = height / 2f
                drawRoundCheckBox(view, centerY, canvas)
            }
            3 -> { // round fill
                paint.isAntiAlias = true
                val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                canvas.drawRoundRect(rect, height / 2f, height / 2f, paint)
            }
            4 -> { // rect stroke
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                val rect = RectF(strokeWidth / 2, strokeWidth / 2,
                        width.toFloat() - strokeWidth / 2, height.toFloat() - strokeWidth / 2)
                canvas.drawRect(rect, paint)
                paint.style = Paint.Style.FILL
            }
            5 -> { // rect fill
                val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                canvas.drawRect(rect, paint)
            }
            6 -> { // round hatched
                val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                canvas.drawRoundRect(rect, rectRadius, rectRadius, paint)

                val dashWidth = strokeWidth * 6
                paint.strokeWidth = strokeWidth * 5
                paint.color = Color.parseColor("#30FFFFFF")
                var x = 0f
                while (x < width + height) {
                    canvas.drawLine(x, -defaulMargin, x - height, height + defaulMargin, paint)
                    x += dashWidth * 2
                }
            }
            7 -> { // rect hatched
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth * 2
                val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                canvas.drawRect(rect, paint)

                val dashWidth = strokeWidth * 2
                var x = 0f
                paint.strokeWidth = strokeWidth
                paint.alpha = 50
                while (x < width + height) {
                    canvas.drawLine(x, -defaulMargin, x - height, height + defaulMargin, paint)
                    x += dashWidth * 2
                }
                paint.alpha = 255
                paint.style = Paint.Style.FILL
            }
            8 -> { // top line
                val rect = RectF(0f, 0f, width.toFloat(), strokeWidth)
                canvas.drawRect(rect, paint)
            }
            9 -> { // bottom line
                val rect = RectF(0f, height.toFloat() - strokeWidth, width.toFloat(), height.toFloat())
                canvas.drawRect(rect, paint)
            }
        }
    }

    private fun drawRoundCheckBox(view: TimeObjectView, centerY: Float, canvas: Canvas) {
        if(view.timeObject.isDone()) {
            view.paintFlags = view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val check = resource.getDrawable(R.drawable.sharp_check_circle_black_48dp)
            check.setColorFilter(view.timeObject.getColor(), PorterDuff.Mode.SRC_ATOP)
            check.setBounds(defaulMargin.toInt(), (centerY - checkSize / 2f).toInt(),
                    checkSize + defaulMargin.toInt(), (centerY + checkSize / 2f).toInt())
            check.draw(canvas)
        }else {
            val check = resource.getDrawable(R.drawable.sharp_check_circle_outline_black_48dp)
            check.setColorFilter(view.timeObject.getColor(), PorterDuff.Mode.SRC_ATOP)
            check.setBounds(defaulMargin.toInt(), (centerY - checkSize / 2f).toInt(),
                    checkSize + defaulMargin.toInt(), (centerY + checkSize / 2f).toInt())
            check.draw(canvas)
        }
    }

    private fun drawRectCheckBox(view: TimeObjectView, centerY: Float, canvas: Canvas) {
        if(view.timeObject.isDone()) {
            view.paintFlags = view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val check = resource.getDrawable(R.drawable.sharp_check_box_black_48dp)
            check.setColorFilter(view.timeObject.getColor(), PorterDuff.Mode.SRC_ATOP)
            check.setBounds(defaulMargin.toInt(), (centerY - checkSize / 2f).toInt(),
                    checkSize + defaulMargin.toInt(), (centerY + checkSize / 2f).toInt())
            check.draw(canvas)
        }else {
            val check = resource.getDrawable(R.drawable.sharp_check_box_outline_blank_black_48dp)
            check.setColorFilter(view.timeObject.getColor(), PorterDuff.Mode.SRC_ATOP)
            check.setBounds(defaulMargin.toInt(), (centerY - checkSize / 2f).toInt(),
                    checkSize + defaulMargin.toInt(), (centerY + checkSize / 2f).toInt())
            check.draw(canvas)
        }
    }

    fun drawTerm(canvas: Canvas, view: TimeObjectView) {
        val paint = view.paint
        val width = view.width
        val height = view.height
        paint.color = view.timeObject.getColor()
        canvas.translate(view.scrollX.toFloat(), 0f)
        when(view.timeObject.style){
            1 -> { // 양쪽 얇은 화살표
                val periodLine = (strokeWidth * 1.5).toInt()
                val rectl = RectF(periodLine.toFloat(),
                        height / 2f - periodLine / 2,
                        view.width / 2 - view.textSpaceWidth / 2 - defaulMargin,
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectl, paint)

                val rectr = RectF(view.width / 2 + view.textSpaceWidth / 2 + defaulMargin,
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
            2 -> { // 두꺼운 화살표
                paint.alpha = 150
                val periodLine = (strokeWidth * 7.5).toInt()
                val rectl = RectF(periodLine * 2f,
                        height / 2f - periodLine / 2,
                        width / 2 - view.textSpaceWidth / 2 - defaultPadding,
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectl, paint)

                val rectr = RectF(width / 2 + view.textSpaceWidth / 2 + defaultPadding,
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
                val periodLine = (strokeWidth * 3).toInt()
                val rectl = RectF(0f,
                        height / 2f - periodLine / 2,
                        width / 2 - view.textSpaceWidth / 2 - defaultPadding,
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectl, paint)

                val rectr = RectF(width / 2 + view.textSpaceWidth / 2 + defaultPadding,
                        height / 2f - periodLine / 2,
                        width.toFloat(),
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectr, paint)

                canvas.drawRect(0f, height / 2f - periodLine * 2,
                        periodLine.toFloat(), height / 2f + periodLine * 2, paint)

                canvas.drawRect(width - periodLine.toFloat(), height / 2f - periodLine * 2,
                        width.toFloat(), height / 2f + periodLine * 2, paint)
            }
        }
    }

    fun drawStamp(canvas: Canvas, view: TimeObjectView) {
        val margin = defaulMargin.toInt()
        val size = (stampSize - defaulMargin).toInt()
        var top = 0
        var left = 0
        view.childList?.forEachIndexed { index, timeObject ->
            /*
            val circle = resource.getDrawable(R.drawable.circle_fill)
            circle.setColorFilter(CalendarSkin.backgroundColor, PorterDuff.Mode.SRC_ATOP)
            circle.setBounds(left + 1, top + 1, left + size - 1, top + size - 1)
            circle.draw(canvas)
*/
            val stamp = resource.getDrawable(StampManager.stamps[index])
            stamp.setColorFilter(timeObject.getColor(), PorterDuff.Mode.SRC_ATOP)
            stamp.setBounds(left + margin, top + margin, left + size - margin, top + size - margin)
            stamp.draw(canvas)

            val stroke = resource.getDrawable(R.drawable.circle_stroke_1px)
            stroke.setColorFilter(timeObject.getColor(), PorterDuff.Mode.SRC_ATOP)
            stroke.setBounds(left, top, left + size, top + size)
            stroke.draw(canvas)

            left += size + margin
            if(left + size >= view.width) {
                top += stampSize
                left = 0
            }
        }
        /*
        if(totalStampCnt > 0) {
            if(size * totalStampCnt + (margin * (totalStampCnt - 1)) > width) {
                var right = left + width
                val overlap = size - (width - size * totalStampCnt) / (1 - totalStampCnt)
                (totalStampCnt - 1 downTo 0).forEach { index ->
                    view.childList?.get(index)?.let { timeObject ->
                        val circle = resource.getDrawable(R.drawable.circle_fill)
                        circle.setColorFilter(CalendarSkin.backgroundColor, PorterDuff.Mode.SRC_ATOP)
                        circle.setBounds(right - size + 1, 1, right - 1, size - 1)
                        circle.draw(canvas)

                        val stamp = resource.getDrawable(StampManager.stamps[index])
                        stamp.setColorFilter(timeObject.getColor(), PorterDuff.Mode.SRC_ATOP)
                        stamp.setBounds(right - size + margin, margin, right - margin, size - margin)
                        stamp.draw(canvas)

                        val stroke = resource.getDrawable(R.drawable.circle_stroke_1dp)
                        stroke.setColorFilter(timeObject.getColor(), PorterDuff.Mode.SRC_ATOP)
                        stroke.setBounds(right - size, 0, right, size)
                        stroke.draw(canvas)

                        right -= overlap
                    }
                }
            }else {
                view.childList?.forEachIndexed { index, timeObject ->
                    val circle = resource.getDrawable(R.drawable.circle_fill)
                    circle.setColorFilter(CalendarSkin.backgroundColor, PorterDuff.Mode.SRC_ATOP)
                    circle.setBounds(left + 1, 1, left + size - 1, size - 1)
                    circle.draw(canvas)

                    val stamp = resource.getDrawable(StampManager.stamps[index])
                    stamp.setColorFilter(timeObject.getColor(), PorterDuff.Mode.SRC_ATOP)
                    stamp.setBounds(left + margin, margin, left + size - margin, size - margin)
                    stamp.draw(canvas)

                    val stroke = resource.getDrawable(R.drawable.circle_stroke_1dp)
                    stroke.setColorFilter(timeObject.getColor(), PorterDuff.Mode.SRC_ATOP)
                    stroke.setBounds(left, 0, left + size, size)
                    stroke.draw(canvas)

                    left += size + margin
                }
            }
        }
        */
    }

    fun drawNote(canvas: Canvas, view: TimeObjectView) {
        val timeObject = view.timeObject
        val paint = view.paint
        paint.color = timeObject.getColor()
        when(timeObject.style){
            1 -> {
                val rect = RectF(defaulMargin, strokeWidth,
                        defaulMargin * 5,
                        strokeWidth * 3)
                canvas.drawRect(rect, paint)
            }
            else -> {}
        }
    }

    fun drawMoney(canvas: Canvas, view: TimeObjectView) {
        val margin = defaulMargin.toInt()
        val width = (view.width - defaulMargin * 2).toInt()
        val size = (view.height - defaulMargin * 2).toInt()
        val totalStampCnt = view.childList?.size ?: 0
        if(totalStampCnt > 0) {
            if(size * totalStampCnt + (margin * (totalStampCnt - 1)) > width) {
                var left = width - size - margin
                val overlap = ((width - size) / (totalStampCnt))
                (totalStampCnt - 1 downTo 0).forEach { index ->
                    val circle = resource.getDrawable(R.drawable.circle_fill)
                    circle.setColorFilter(view.timeObject.getColor(), PorterDuff.Mode.SRC_ATOP)
                    circle.setBounds(left, 0, left + size, size)
                    circle.draw(canvas)

                    val stamp = resource.getDrawable(StampManager.stamps[index])
                    stamp.setColorFilter(view.timeObject.fontColor, PorterDuff.Mode.SRC_ATOP)
                    stamp.setBounds(left + margin, 0 + margin, left + size - margin, size - margin)
                    stamp.draw(canvas)

                    val stroke = resource.getDrawable(R.drawable.circle_stroke_1dp)
                    stroke.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                    stroke.setBounds(left - margin, -margin, left + size + margin, size + margin)
                    stroke.draw(canvas)

                    left -= overlap
                }
            }else {
                var left = margin
                view.childList?.forEachIndexed { index, timeObject ->
                    val circle = resource.getDrawable(R.drawable.circle_fill)
                    circle.setColorFilter(view.timeObject.getColor(), PorterDuff.Mode.SRC_ATOP)
                    circle.setBounds(left, 0, left + size, size)
                    circle.draw(canvas)

                    val stamp = resource.getDrawable(StampManager.stamps[index])
                    stamp.setColorFilter(view.timeObject.fontColor, PorterDuff.Mode.SRC_ATOP)
                    stamp.setBounds(left + margin, 0 + margin, left + size - margin, size - margin)
                    stamp.draw(canvas)
                    left += size + margin
                }
            }
        }
    }

    /* 메모 모양
                setPadding(defaultPadding * 2, defaultPadding * 2, defaultPadding * 2, defaultPadding * 2)
                    setSingleLine(false)
                    maxLines = 4
                    setTextColor(color)
                    gravity = Gravity.TOP
                    ellipsize = TextUtils.TruncateAt.END

                    paint.strokeWidth = strokeWidth
                    paint.color = color

                    val left = defaultPadding.toFloat()
                    val top = defaultPadding.toFloat()
                    val right = width.toFloat() - defaultPadding
                    val bottom = height.toFloat() - defaultPadding

                    var path = Path()
                    path.moveTo(left, top)
                    path.lineTo(right, top)
                    path.lineTo(right, bottom - circleRadius)
                    path.lineTo(right - circleRadius, bottom - circleRadius)
                    path.lineTo(right - circleRadius, bottom)
                    path.lineTo(left, bottom)
                    path.lineTo(left, top)
                    paint.style = Paint.Style.FILL
                    paint.alpha = 30
                    it.drawPath(path, paint)

                    path = Path()
                    path.moveTo(right, bottom - circleRadius)
                    path.lineTo(right - circleRadius, bottom - circleRadius)
                    path.lineTo(right - circleRadius, bottom)
                    path.lineTo(right, bottom - circleRadius)
                    paint.style = Paint.Style.STROKE
                    paint.alpha = 30
                    it.drawPath(path, paint)

                    path = Path()
                    path.moveTo(right - circleRadius, bottom - circleRadius)
                    path.lineTo(right - circleRadius * 2, bottom)
                    path.lineTo(right - circleRadius, bottom)
                    path.lineTo(right - circleRadius, bottom - circleRadius)
                    path.close()
                    paint.style = Paint.Style.FILL
                    paint.alpha = 100
                    it.drawPath(path, paint)


                3 -> {
                    val paint = Paint()
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = strokeWidth.toFloat()
                    paint.color = color
                    paint.isAntiAlias = true
                    val center = blockTypeSize / 2f
                    val rect = RectF(center - circleRadius, center - circleRadius, center + circleRadius, center + circleRadius)
                    it.drawRoundRect(rect, radius, radius, paint)
                }
                4 -> { // 시계
                    val paint = Paint()
                    paint.style = Paint.Style.TOP_STACK
                    paint.strokeWidth = strokeWidth.toFloat()
                    paint.color = color
                    paint.isAntiAlias = true
                    tempCal.timeInMillis = timeObject.dtStart
                    val degreeH = tempCal.get(Calendar.HOUR_OF_DAY) % 12 * 360 / 12 + 270
                    val sX = normalTypeSize / 2f
                    val sY = height / 2f
                    val hX = Math.cos(Math.toRadians(degreeH.toDouble())) * (circleRadius - strokeWidth)
                    val hY = Math.sin(Math.toRadians(degreeH.toDouble())) * (circleRadius - strokeWidth)

                    it.drawCircle(sX, sY, circleRadius, paint)
                    paint.color = Color.WHITE
                    it.drawLine(sX, sY, (sX + hX).toFloat(), (sY + hY).toFloat(), paint)
                }
                5 -> {
                    val bitmap = BitmapFactory.decodeResource(resource, R.drawable.p1)
                    val bitmapDrawable = BitmapDrawable(resource, bitmap)
                    bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                    bitmapDrawable.setTargetDensity(it)
                    it.drawBitmap(bitmapDrawable.bitmap, 0f, 0f, Paint())
                }
                */
}