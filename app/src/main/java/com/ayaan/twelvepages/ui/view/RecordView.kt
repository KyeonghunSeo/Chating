package com.ayaan.twelvepages.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.ayaan.twelvepages.App.Companion.resource
import com.ayaan.twelvepages.AppStatus
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.manager.StampManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter.Formula.*

@SuppressLint("ViewConstructor")
class RecordView constructor(context: Context, val record: Record, var formula: RecordCalendarAdapter.Formula,
                             val cellNum: Int, var length: Int) : TextView(context) {
    companion object {
        var standardTextSize = 9f
        val baseSize = dpToPx(0.5f)
        val defaulMargin = dpToPx(1.5f) // 뷰간 간격
        val strokeWidth = dpToPx(1f) // 선
        val sidePadding = dpToPx(3.0f).toInt()
        val smallTextPadding = dpToPx(1.5f)
        val normalTextPadding = dpToPx(0.0f)
        val bigTextPadding = -dpToPx(1.0f)
        val bottomPadding = dpToPx(3.0f)
        val rectRadius = dpToPx(1f)
        val stampSize = dpToPx(16)
        val blockTypeSize = dpToPx(16.5f).toInt()
        val checkboxSize = dpToPx(10)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    }

    enum class Shape(val fillColor: Boolean) {
        DEFAULT(false),
        RECT_FILL(true),
        RECT_STROKE(false),
        ROUND_FILL(true),
        ROUND_STROKE(false),
        BOLD_HATCHED(true),
        THIN_HATCHED(false),
        UPPER_LINE(false),
        UNDER_LINE(false)
    }

    var mLeft = 0f
    var mTop = 0f
    var mRight = 0f
    var mBottom = 0f
    var mLine = 0
    var leftOpen = false
    var rightOpen = false
    var textSpaceWidth = 0f
    var childList: ArrayList<Record>? = null
    var paintColor = AppTheme.backgroundColor
    var fontColor = AppTheme.primaryText
    var shape = Shape.DEFAULT

    init {
        includeFontPadding = false
        typeface = AppTheme.thinFont
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize + AppStatus.calTextSize)
        val leftPadding = if(record.isSetCheckBox()) {
            (sidePadding + checkboxSize + defaulMargin).toInt()
        }else {
            sidePadding
        }
        val textPadding =  when(AppStatus.calTextSize) { /*글씨 크기에 따른 패딩 조정*/
            -1 -> smallTextPadding
            1 -> bigTextPadding
            else -> normalTextPadding
        }.toInt()
        setPadding(leftPadding, textPadding, sidePadding, 0)
        setStyle()
    }

    @SuppressLint("RtlHardcoded")
    fun setStyle() {
        val shapeNum = record.style / 100
        when(formula) {
            BACKGROUND -> {}
            DEFAULT -> {
                shape = Shape.values().filter { true }[shapeNum]
                text = record.getTitleInCalendar()
                gravity = Gravity.LEFT
                maxLines = 1
                setSingleLine(true)
                setHorizontallyScrolling(true)
                ellipsize = null
            }
            STAMP -> {}
            DOT -> {}
            EXPANDED -> {
                shape = Shape.values().filter { true }[shapeNum]
                text = record.getTitleInCalendar()
                gravity = Gravity.LEFT
                maxLines = 5
                setSingleLine(false)
                setHorizontallyScrolling(false)
                ellipsize = TextUtils.TruncateAt.END
            }
            RANGE -> {
                shape = Shape.values().filter { true }[shapeNum]
                text = record.getTitleInCalendar()
                gravity = Gravity.CENTER_HORIZONTAL
                maxLines = 1
                setSingleLine(true)
                setHorizontallyScrolling(true)
                ellipsize = null
            }
            IMAGE -> {}
        }

        paintColor = AppTheme.getColor(record.colorKey)
        fontColor = if(shape.fillColor) {
            AppTheme.getFontColor(record.colorKey)
        }else {
            AppTheme.getColor(record.colorKey)
        }
        setTextColor(fontColor)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            when(formula) {
                RANGE -> {
                    super.onDraw(canvas)
                    drawRange(it)
                }
                else -> {
                    drawBasicShape(it)
                    super.onDraw(canvas)
                }
            }
        }
    }

    fun getViewHeight(): Int {
        return when(formula) {
            STAMP -> {
                val width =  mRight - mLeft - defaulMargin
                val margin = defaulMargin.toInt()
                val size = stampSize - defaulMargin
                val totalStampCnt = childList?.size ?: 0
                val rows = ((size * totalStampCnt + margin * (totalStampCnt - 1)) / width + 1).toInt()
                (stampSize * rows)
            }
            EXPANDED -> {
                val width =  mRight - mLeft - defaulMargin
                measure(MeasureSpec.makeMeasureSpec(width.toInt(), MeasureSpec.EXACTLY), heightMeasureSpec)
                //l("${record.title} 라인 : "+((paint.measureText(text.toString()) / width).toInt() + 1))
                /* 블럭 사이즈로 맞추기
                var lh = blockTypeSize
                while (lh < measuredHeight) {
                    lh += blockTypeSize
                }
                lh*/
                if(measuredHeight < blockTypeSize) {
                    blockTypeSize
                }else {
                    measuredHeight + bottomPadding.toInt()
                }
            }
            else -> {
                textSpaceWidth = paint.measureText(text.toString())
                blockTypeSize
            }
        }
    }

    fun setLayout() {
        layoutParams = FrameLayout.LayoutParams((mRight - mLeft - defaulMargin).toInt(),
                (mBottom - mTop - defaulMargin).toInt()).apply { topMargin = mTop.toInt() }
    }

    private fun drawBasicShape(canvas: Canvas) {
        paint.isAntiAlias = true
        paint.color = paintColor
        when(shape){
            Shape.RECT_FILL -> {
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 0f, 0f, paint)
                paint.color = fontColor
            }
            Shape.RECT_STROKE -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth * 0.8f
                canvas.drawRect(strokeWidth / 2, strokeWidth / 2,
                        width.toFloat() - strokeWidth / 2, height.toFloat() - strokeWidth / 2, paint)
                paint.style = Paint.Style.FILL
            }
            Shape.ROUND_STROKE -> {
                paint.isAntiAlias = true
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth * 1f
                canvas.drawRoundRect(strokeWidth / 2, strokeWidth / 2,
                        width.toFloat() - strokeWidth / 2, height.toFloat() - strokeWidth / 2,
                        height / 2f, height / 2f, paint)
                paint.style = Paint.Style.FILL
            }
            Shape.ROUND_FILL -> {
                paint.isAntiAlias = true
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), height / 2f, height / 2f, paint)
                paint.color = fontColor
            }
            Shape.BOLD_HATCHED -> {
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), rectRadius, rectRadius, paint)
                val dashWidth = strokeWidth * 6
                paint.strokeWidth = strokeWidth * 5
                paint.color = Color.parseColor("#30FFFFFF")
                var x = 0f
                while (x < width + height) {
                    canvas.drawLine(x, -defaulMargin, x - height, height + defaulMargin, paint)
                    x += dashWidth * 2
                }
                paint.color = fontColor
            }
            Shape.THIN_HATCHED -> {
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
            Shape.UPPER_LINE -> {
                val strokeWidth = strokeWidth * 1f
                canvas.drawRect(0f, 0f, width.toFloat(), strokeWidth, paint)
            }
            Shape.UNDER_LINE -> {
                val strokeWidth = strokeWidth * 0.8f
                canvas.drawRect(0f, height.toFloat() - strokeWidth, width.toFloat(), height.toFloat(), paint)
            }
            else -> {
                if(length > 1) {
                    paint.alpha = 17
                    canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 0f, 0f, paint)
                    paint.alpha = 255
                }
            }
        }

        if(record.isSetCheckBox()) {
            drawCheckBox(canvas, sidePadding)
        }
    }

    private fun drawRange(canvas: Canvas) {
        paint.color = paintColor
        canvas.translate(scrollX.toFloat(), 0f)
        val space = textSpaceWidth + if(record.isSetCheckBox()) (checkboxSize + defaulMargin) else 0f
        var textLPos = width / 2 - space / 2 - defaulMargin
        if(textLPos < sidePadding) textLPos = sidePadding.toFloat()
        var textRPos = width / 2 + space / 2 + defaulMargin
        if(textRPos > width - sidePadding) textRPos = width - sidePadding.toFloat()
        when(record.style){
            1 -> { // ㅣ----ㅣ
                val periodLine = (strokeWidth * 1.5f).toInt()
                val rectl = RectF(periodLine.toFloat(),
                        height / 2f - periodLine / 2,
                        textLPos,
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectl, paint)

                val rectr = RectF(textRPos,
                        height / 2f - periodLine / 2,
                        width - periodLine.toFloat(),
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectr, paint)

                canvas.drawRect(0f, height / 2f - periodLine * 2.5f,
                        periodLine.toFloat(), height / 2f + periodLine * 2, paint)

                canvas.drawRect(width - periodLine.toFloat(), height / 2f - periodLine * 2.5f,
                        width.toFloat(), height / 2f + periodLine * 2, paint)
            }
            2 -> { // <---->
                val periodLine = (strokeWidth * 1.5f).toInt()
                val rectl = RectF(periodLine.toFloat(),
                        height / 2f - periodLine / 2,
                        textLPos,
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectl, paint)

                val rectr = RectF(textRPos,
                        height / 2f - periodLine / 2,
                        width - periodLine.toFloat(),
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
            3 -> { // neon
                paint.alpha = 17
                canvas.drawRoundRect(0f, height / 2f, width.toFloat(), height.toFloat(), 0f, 0f, paint)
                paint.alpha = 255
            }
            else -> {
                val periodLine = (strokeWidth * 1.5f).toInt()
                val rectl = RectF(periodLine.toFloat(),
                        height / 2f - periodLine / 2,
                        textLPos,
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectl, paint)

                val rectr = RectF(textRPos,
                        height / 2f - periodLine / 2,
                        width - periodLine.toFloat(),
                        height / 2f + periodLine / 2)
                canvas.drawRect(rectr, paint)

                val arrowSize = (periodLine * 2.5f).toInt()

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
        if(record.isSetCheckBox()) {
            drawCheckBox(canvas, (textLPos + defaulMargin).toInt())
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

    private fun drawCheckBox(canvas: Canvas, xOffset: Int) {
        val radius = checkboxSize / 2f
        val centerY = (blockTypeSize - defaulMargin) / 2f
        if(record.isDone()) {
            //view.paintFlags = view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val check = resource.getDrawable(R.drawable.check)
            check.setColorFilter(record.getColor(), PorterDuff.Mode.SRC_ATOP)
            check.setBounds(xOffset, (centerY - radius).toInt(),
                    xOffset + checkboxSize, (centerY + radius).toInt())
            check.draw(canvas)
        }else {
            paint.style = Paint.Style.STROKE
            val check = resource.getDrawable(R.drawable.uncheck)
            check.setColorFilter(record.getColor(), PorterDuff.Mode.SRC_ATOP)
            check.setBounds(xOffset, (centerY - radius).toInt(),
                    xOffset + checkboxSize, (centerY + radius).toInt())
            check.draw(canvas)
        }
        paint.style = Paint.Style.FILL
    }

    fun drawNotInCalendar(canvas: Canvas) {
        val legnth =  baseSize * 10
        val gap = baseSize * 2
        val startY = (blockTypeSize - defaulMargin) / 2f - legnth / 2f
        paint.strokeWidth = gap
        childList?.forEachIndexed { index, timeObject ->
            if(index < 8) {
                val startX = sidePadding + (index * gap * 2)
                paint.color = timeObject.getColor()
                canvas.drawLine(startX, startY, startX, startY + legnth, paint)
            }
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
                    paint.style = Paint.Style.DEFAULT
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
                val dashPath = DashPathEffect(floatArrayOf(checkboxSize.toFloat(), defaulMargin * 2), 0f)
                paint.pathEffect = dashPath
                canvas.drawPath(path, paint)
                paint.pathEffect = null
                paint.style = Paint.Style.FILL
            }

                */

}