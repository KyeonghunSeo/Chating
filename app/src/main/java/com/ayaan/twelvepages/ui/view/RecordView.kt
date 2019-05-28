package com.ayaan.twelvepages.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.TextView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.App.Companion.resource
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.manager.StampManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter.Formula.*
import android.graphics.DashPathEffect



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
        val rectRadius = dpToPx(0.5f)
        val blockTypeSize = dpToPx(16.5f).toInt()
        val dotSize = dpToPx(5)
        val checkboxSize = dpToPx(10)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val dashPath = DashPathEffect(floatArrayOf(dpToPx(3.0f), dpToPx(1.0f)), 2f)
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
        UNDER_LINE(false),
        NEON_PEN(false),
        DASH(false),
        ARROW(false),
        DASH_ARROW(false)
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
        typeface = AppTheme.regularFont
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize + AppStatus.calTextSize)
        setStyle()
    }

    @SuppressLint("RtlHardcoded")
    fun setStyle() {
        var sPadding = sidePadding

        when(formula) {
            BACKGROUND -> {}
            STACK -> {
                setStackShape()
                text = record.getTitleInCalendar()
                gravity = Gravity.LEFT
                setSingleLine(true)
                setHorizontallyScrolling(true)
                maxLines = 1
                ellipsize = null
            }
            STAMP -> {}
            DOT -> {
                gravity = Gravity.LEFT
            }
            EXPANDED -> {
                setExpandShape()
                text = record.getTitleInCalendar()
                gravity = Gravity.LEFT
                setSingleLine(false)
                setHorizontallyScrolling(false)
                maxLines = 5
                ellipsize = TextUtils.TruncateAt.END
            }
            RANGE -> {
                setRangeShape()
                text = record.getTitleInCalendar()
                gravity = Gravity.CENTER_HORIZONTAL
                setSingleLine(true)
                setHorizontallyScrolling(true)
                maxLines = 1
                ellipsize = null
                sPadding *= 3
            }
            IMAGE -> {
                gravity = Gravity.LEFT
            }
        }

        val leftPadding = if(record.isSetCheckBox()) {
            (sPadding + checkboxSize + defaulMargin).toInt()
        }else {
            sPadding
        }
        val textPadding =  when(AppStatus.calTextSize) { /*글씨 크기에 따른 패딩 조정*/
            -1 -> smallTextPadding
            1 -> bigTextPadding
            else -> normalTextPadding
        }.toInt()
        setPadding(leftPadding, textPadding, sPadding, 0)

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
                    drawRange(canvas)
                }
                DOT -> {
                    drawDot(canvas)
                }
                IMAGE -> {
                    drawImage(canvas)
                }
                else -> {
                    drawBasicShape(canvas)
                    super.onDraw(canvas)
                }
            }
        }
    }

    private fun setStackShape() {
        shape = when(record.style / 100) {
            1 -> Shape.DEFAULT
            2 -> Shape.RECT_STROKE
            3 -> Shape.THIN_HATCHED
            4 -> Shape.BOLD_HATCHED
            5 -> Shape.NEON_PEN
            6 -> Shape.UPPER_LINE
            7 -> Shape.UNDER_LINE
            else -> Shape.RECT_FILL
        }
    }

    private fun setExpandShape() {
        shape = when(record.style / 100) {
            1 -> Shape.RECT_FILL
            2 -> Shape.RECT_STROKE
            3 -> Shape.THIN_HATCHED
            4 -> Shape.BOLD_HATCHED
            5 -> Shape.UPPER_LINE
            6 -> Shape.UNDER_LINE
            else -> Shape.DEFAULT
        }
    }

    private fun setRangeShape() {
        shape = when(record.style / 100) {
            1 -> Shape.DASH
            2 -> Shape.ARROW
            3 -> Shape.DASH_ARROW
            4 -> Shape.RECT_FILL
            5 -> Shape.NEON_PEN
            6 -> Shape.UPPER_LINE
            7 -> Shape.UNDER_LINE
            else -> Shape.DEFAULT
        }
    }

    fun getViewHeight(): Int {
        return when(formula) {
            STAMP, DOT -> {
                val itemSize = if(formula == DOT) dotSize else blockTypeSize
                val width =  mRight - mLeft - defaulMargin
                val margin = defaulMargin.toInt()
                val size = itemSize - defaulMargin
                val totalCnt = childList?.size ?: 0
                val rows = ((size * totalCnt + margin * (totalCnt - 1)) / width + 1).toInt()
                (itemSize * rows)
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
        if(formula == IMAGE) {
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }else {
            layoutParams = FrameLayout.LayoutParams((mRight - mLeft - defaulMargin).toInt(),
                    (mBottom - mTop - defaulMargin).toInt()).apply { topMargin = mTop.toInt() }
        }
    }

    private fun drawBasicShape(canvas: Canvas) {
        paint.color = paintColor
        when(shape){
            Shape.RECT_FILL -> {
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), rectRadius, rectRadius, paint)
                paint.color = fontColor
            }
            Shape.ROUND_FILL -> {
                paint.isAntiAlias = true
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), height / 2f, height / 2f, paint)
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
            Shape.BOLD_HATCHED -> {
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), rectRadius, rectRadius, paint)
                val dashWidth = strokeWidth * 6
                paint.strokeWidth = strokeWidth * 5
                paint.color = Color.parseColor("#40FFFFFF")
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
                //canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

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
                canvas.drawRect(0f, 0f, width.toFloat(), strokeWidth, paint)
            }
            Shape.UNDER_LINE -> {
                canvas.drawRect(0f, height.toFloat() - strokeWidth, width.toFloat(), height.toFloat(), paint)
            }
            Shape.NEON_PEN -> {
                paint.alpha = 70
                canvas.drawRoundRect(0f, height / 2f, width.toFloat(), height.toFloat(), 0f, 0f, paint)
                paint.alpha = 255
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
        val sPadding = sidePadding * 3
        if(textLPos < sPadding) textLPos = sPadding.toFloat()
        var textRPos = width / 2 + space / 2 + defaulMargin
        if(textRPos > width - sPadding) textRPos = width - sPadding.toFloat()
        when(shape){
            Shape.RECT_FILL -> {
                paint.alpha = 17
                canvas.drawRoundRect(0f, height / 2f, width.toFloat(), height.toFloat(), 0f, 0f, paint)
                paint.alpha = 255
            }
            Shape.NEON_PEN -> {
                paint.alpha = 17
                canvas.drawRoundRect(0f, height / 2f, width.toFloat(), height.toFloat(), 0f, 0f, paint)
                paint.alpha = 255
            }
            Shape.UPPER_LINE -> {
                paint.alpha = 17
                canvas.drawRoundRect(0f, height / 2f, width.toFloat(), height.toFloat(), 0f, 0f, paint)
                paint.alpha = 255
            }
            Shape.UNDER_LINE -> {
                paint.alpha = 17
                canvas.drawRoundRect(0f, height / 2f, width.toFloat(), height.toFloat(), 0f, 0f, paint)
                paint.alpha = 255
            }
            else -> {
                val periodLine = strokeWidth * 1.5f
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = periodLine
                if(shape == Shape.DASH || shape == Shape.DASH_ARROW) {
                    paint.pathEffect = dashPath
                }else {
                    paint.pathEffect = null
                }
                canvas.drawLine(periodLine, height / 2f, textLPos, height / 2f, paint)
                canvas.drawLine(textRPos, height / 2f, width - periodLine, height / 2f, paint)
                paint.style = Paint.Style.FILL

                if(shape == Shape.ARROW || shape == Shape.DASH_ARROW) {
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
                }else {
                    canvas.drawRect(0f, height / 2f - periodLine * 2.5f,
                            periodLine, height / 2f + periodLine * 2, paint)
                    canvas.drawRect(width - periodLine, height / 2f - periodLine * 2.5f,
                            width.toFloat(), height / 2f + periodLine * 2, paint)
                }
            }
        }
        if(record.isSetCheckBox()) {
            drawCheckBox(canvas, (textLPos + defaulMargin).toInt())
        }
    }

    private fun drawDot(canvas: Canvas) {
        val margin = defaulMargin
        val size = dotSize - defaulMargin
        var top = 0f
        var left = sidePadding + margin
        childList?.forEach { child ->
            paint.color = child.getColor()
            canvas.drawRect(left, top, left + size, top + size, paint)
            left += size + margin
            if (left + size >= width) {
                top += dotSize
                left = sidePadding + margin
            }
        }
    }

    fun drawStamp(canvas: Canvas) {
        val margin = defaulMargin.toInt()
        val size = (dotSize - defaulMargin).toInt()
        var top = 0
        var left = sidePadding
        childList?.forEach { child ->
            val circle = resource.getDrawable(R.drawable.primary_rect_fill_radius_1)
            circle.setColorFilter(child.getColor(), PorterDuff.Mode.SRC_ATOP)
            circle.setBounds(left + 1, top + 1, left + size - 1, top + size - 1)
            circle.draw(canvas)

            val stamp = resource.getDrawable(StampManager.stamps[0])
            stamp.setColorFilter(AppTheme.getFontColor(child.colorKey), PorterDuff.Mode.SRC_ATOP)
            stamp.setBounds(left + margin, top + margin, left + size - margin, top + size - margin)
            stamp.draw(canvas)
/*
            val stroke = resource.getDrawable(R.drawable.circle_stroke_1px)
            stroke.setColorFilter(record.getColor(), PorterDuff.Mode.SRC_ATOP)
            stroke.setBounds(left, top, left + size, top + size)
            stroke.draw(canvas)
*/
            left += size + margin
            if(left + size >= width) {
                top += dotSize
                left = sidePadding
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

    private fun drawImage(canvas: Canvas) {
        val size = blockTypeSize * 2
        var top = height - size - sidePadding
        var left = sidePadding
        childList?.forEach { child ->
            val circle = resource.getDrawable(R.drawable.s_coin)
            circle.setBounds(left, top, (left + size), (top + size))
            circle.draw(canvas)
        }
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
            check.setColorFilter(fontColor, PorterDuff.Mode.SRC_ATOP)
            check.setBounds(xOffset, (centerY - radius).toInt(),
                    xOffset + checkboxSize, (centerY + radius).toInt())
            check.draw(canvas)
        }else {
            paint.style = Paint.Style.STROKE
            val check = resource.getDrawable(R.drawable.uncheck)
            check.setColorFilter(fontColor, PorterDuff.Mode.SRC_ATOP)
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
                    paint.style = Paint.Style.STACK
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