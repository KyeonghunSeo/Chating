package com.hellowo.journey.manager

import android.graphics.*
import com.hellowo.journey.App.Companion.resource
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.view.CalendarView
import com.hellowo.journey.ui.view.TimeObjectView
import com.hellowo.journey.ui.view.TimeObjectView.Companion.defaulMargin
import com.hellowo.journey.ui.view.TimeObjectView.Companion.defaultPadding
import com.hellowo.journey.ui.view.TimeObjectView.Companion.iconSize
import com.hellowo.journey.ui.view.TimeObjectView.Companion.rectRadius
import com.hellowo.journey.ui.view.TimeObjectView.Companion.strokeWidth

object CalendarSkin {
    var backgroundColor: Int = 0
    var dateColor: Int = 0
    var holiDateColor: Int = 0
    var todayDateColor: Int = 0
    var selectedDateColor: Int = 0
    var selectedBackgroundColor: Int = 0
    var greyColor: Int = 0
    var dateFont = AppTheme.textFont
    var noteFont = AppTheme.textFont
    var selectFont = AppTheme.textFont

    fun init(calendarView: CalendarView) {
        val resource = calendarView.context.resources
        backgroundColor = resource.getColor(R.color.calendarBackground)
        dateColor = resource.getColor(R.color.primaryText)
        holiDateColor = resource.getColor(R.color.holiday)
        todayDateColor = resource.getColor(R.color.primaryText)
        selectedDateColor = resource.getColor(R.color.primaryText)
        selectedBackgroundColor = resource.getColor(R.color.grey)
        greyColor = resource.getColor(R.color.grey)
    }

    fun drawEvent(canvas: Canvas, view: TimeObjectView) {
        val paint = view.paint
        val width = view.width
        val height = view.height
        paint.color = view.timeObject.color
        when(view.timeObject.style){
            1 -> { // 스트로크
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = TimeObjectView.strokeWidth * 4f
                val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                canvas.drawRoundRect(rect, TimeObjectView.rectRadius, TimeObjectView.rectRadius, paint)
                paint.style = Paint.Style.FILL
            }
            2 -> { // 블럭
                paint.style = Paint.Style.FILL
                val edge = defaultPadding.toFloat()
                var left = 0f
                var right = width.toFloat() - defaultPadding

                // 화살표 모양
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
                }

                val rect = RectF(left, 0f, right, height.toFloat())
                canvas.drawRoundRect(rect, TimeObjectView.rectRadius, TimeObjectView.rectRadius, paint)
            }
            3 -> {// 동글뱅이
                paint.style = Paint.Style.FILL
                val edge = defaultPadding.toFloat()
                var left = 0f
                var right = width.toFloat()
                if(view.leftOpen) {
                    left = edge
                }
                if(view.rightOpen) {
                    right = width.toFloat() - defaultPadding
                }

                val rect = RectF(left, 0f, right, height.toFloat())
                canvas.drawRoundRect(rect, height / 2.5f, height / 2.5f, paint)
            }
            4 -> { // 기본 꽉찬 블럭
                paint.style = Paint.Style.FILL
                var left = 0f
                var right = width.toFloat()
                val rect = RectF(left, 0f, right, height.toFloat())
                canvas.drawRoundRect(rect, rectRadius, rectRadius, paint)
            }
            5 -> { //빗금
                paint.style = Paint.Style.FILL
                val edge = defaultPadding.toFloat()
                var left = 0f
                var right = width.toFloat()
/*
                if(!view.rightOpen) {
                    right = width.toFloat() - edge
                    val path = Path()
                    path.moveTo(right, 0f)
                    path.lineTo(right + edge, edge)
                    path.lineTo(right + edge, height.toFloat())
                    path.lineTo(right, height.toFloat())
                    path.lineTo(right, 0f)
                    path.close()
                    canvas.drawPath(path, paint)
                }
*/
                val rect = RectF(left, 0f, right, height.toFloat())
                canvas.drawRoundRect(rect, rectRadius, rectRadius, paint)

                val dashWidth = strokeWidth * 10
                val offset = strokeWidth * 15
                paint.strokeWidth = dashWidth
                paint.color = Color.parseColor("#30FFFFFF")
                var x = 0f
                while (x < width + offset) {
                    canvas.drawLine(x, -defaulMargin, x - offset, height + defaulMargin, paint)
                    x += dashWidth * 2
                }
            }
            6 -> { // 작은 직사각형
                paint.style = Paint.Style.FILL

                if(view.length > 1) {
                    paint.alpha = 100
                    val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                    canvas.drawRoundRect(rect, defaulMargin, defaulMargin, paint)
                }

                paint.alpha = 255
                val redius = defaulMargin
                if(view.timeObject.allday) {
                    val rect = RectF(iconSize / 2f - redius, height / 2f - redius * 2.6f,
                            iconSize / 2f + redius, height / 2f + redius * 2.4f)
                    canvas.drawRoundRect(rect, 0f, 0f, paint)
                }else {
                    canvas.drawCircle(iconSize / 2f, height / 2f - strokeWidth, redius, paint)
                }
            }
            else -> {
                paint.style = Paint.Style.FILL
                paint.alpha = 255
                val redius = defaulMargin
                canvas.drawCircle(iconSize / 2f, height / 2f - strokeWidth, redius, paint)
            }
        }
    }

    fun drawTask(canvas: Canvas, view: TimeObjectView) {
        val paint = view.paint
        val width = view.width
        val height = view.height
        paint.color = view.timeObject.color
        when(view.timeObject.style) {
            1 -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth * 2

                val centerY = (TimeObjectView.smallTypeSize - strokeWidth) / 2f - strokeWidth
                val checkRadius = TimeObjectView.iconSize / 2.5f
                val centerX = checkRadius + defaulMargin
                val rect = RectF(centerX - checkRadius, centerY - checkRadius, centerX + checkRadius, centerY + checkRadius)
                if(view.timeObject.isDone()) {
                    view.paintFlags = view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    canvas.drawCircle(centerX, centerY, checkRadius, paint)
                    paint.style = Paint.Style.STROKE
                    paint.color = CalendarSkin.backgroundColor
                    canvas.drawLine(centerX - checkRadius * 0.65f, centerY,
                            centerX - checkRadius * 0.25f, centerY + checkRadius * 0.5f, paint)
                    canvas.drawLine(centerX - checkRadius * 0.25f, centerY + checkRadius * 0.5f,
                            centerX + checkRadius * 0.65f, centerY - checkRadius * 0.5f, paint)
                }else {
                    canvas.drawRect(rect, paint)
                }
                paint.style = Paint.Style.FILL
            }
            else -> {
                paint.strokeWidth = strokeWidth * 2

                val centerY = (TimeObjectView.smallTypeSize - strokeWidth) / 2f - strokeWidth
                val checkRadius = TimeObjectView.iconSize / 2.5f
                val centerX = checkRadius + defaulMargin
                val rect = RectF(centerX - checkRadius, centerY - checkRadius, centerX + checkRadius, centerY + checkRadius)
                if(view.timeObject.isDone()) {
                    view.paintFlags = view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    canvas.drawRect(rect, paint)
                    paint.style = Paint.Style.STROKE
                    paint.color = CalendarSkin.backgroundColor
                    canvas.drawLine(centerX - checkRadius * 0.65f, centerY,
                            centerX - checkRadius * 0.25f, centerY + checkRadius * 0.5f, paint)
                    canvas.drawLine(centerX - checkRadius * 0.25f, centerY + checkRadius * 0.5f,
                            centerX + checkRadius * 0.65f, centerY - checkRadius * 0.5f, paint)
                }else {
                    paint.style = Paint.Style.STROKE
                    canvas.drawRect(rect, paint)
                }
                paint.style = Paint.Style.FILL
            }
        }
    }

    fun drawTerm(canvas: Canvas, view: TimeObjectView) {
        val paint = view.paint
        val width = view.width
        val height = view.height
        paint.color = view.timeObject.color
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
                val periodLine = (strokeWidth * 2).toInt()
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
        }
    }

    fun drawStamp(canvas: Canvas, view: TimeObjectView) {
        val margin = defaulMargin.toInt()
        var left = margin
        val width = view.width - left
        val size = view.height
        val totalStampCnt = view.childList?.size ?: 0
        if(totalStampCnt > 0) {
            if(size * totalStampCnt + (margin * (totalStampCnt - 1)) > width) {
                var right = view.width
                val overlap = size - (width - size * totalStampCnt) / (1 - totalStampCnt)
                (totalStampCnt - 1 downTo 0).forEach { index ->
                    view.childList?.get(index)?.let { timeObject ->
                        /*
                        val circle = resource.getDrawable(R.drawable.circle_fill)
                        circle.setColorFilter(timeObject.fontColor, PorterDuff.Mode.SRC_ATOP)
                        circle.setBounds(right - size + 1, 1, right - 1, size - 1)
                        circle.draw(canvas)
                        */
                        val stamp = resource.getDrawable(StampManager.stamps[index])
                        //stamp.setColorFilter(timeObject.color, PorterDuff.Mode.SRC_ATOP)
                        stamp.setBounds(right - size + margin, margin, right - margin, size - margin)
                        stamp.draw(canvas)
/*
                        val stroke = resource.getDrawable(R.drawable.circle_stroke_1dp)
                        stroke.setColorFilter(timeObject.color, PorterDuff.Mode.SRC_ATOP)
                        stroke.setBounds(right - size, 0, right, size)
                        stroke.draw(canvas)
*/
                        right -= overlap
                    }
                }
            }else {
                view.childList?.forEachIndexed { index, timeObject ->
                    val circle = resource.getDrawable(R.drawable.circle_fill)
                    circle.setColorFilter(timeObject.fontColor, PorterDuff.Mode.SRC_ATOP)
                    circle.setBounds(left + 1, 1, left + size - 1, size - 1)
                    circle.draw(canvas)

                    val stamp = resource.getDrawable(StampManager.stamps[index])
                    //stamp.setColorFilter(timeObject.color, PorterDuff.Mode.SRC_ATOP)
                    stamp.setBounds(left + margin, margin, left + size - margin, size - margin)
                    stamp.draw(canvas)
/*
                    val stroke = resource.getDrawable(R.drawable.circle_stroke_1dp)
                    stroke.setColorFilter(timeObject.color, PorterDuff.Mode.SRC_ATOP)
                    stroke.setBounds(left, 0, left + size, size)
                    stroke.draw(canvas)
*/
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
                val rect = RectF(defaulMargin,
                        strokeWidth,
                        defaulMargin * 6,
                        strokeWidth * 4)
                canvas.drawRect(rect, paint)
            }
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
                    circle.setColorFilter(view.timeObject.color, PorterDuff.Mode.SRC_ATOP)
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
                    circle.setColorFilter(view.timeObject.color, PorterDuff.Mode.SRC_ATOP)
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

    fun drawDrawing(canvas: Canvas, view: TimeObjectView) {
        val margin = defaulMargin.toInt()
        val width = view.width
        val height = view.height
        val frame = resource.getDrawable(R.drawable.frame_2)
        frame.setBounds(0, height - 245 * width / 420, width, height)
        frame.draw(canvas)
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
                    val center = smallTypeSize / 2f
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