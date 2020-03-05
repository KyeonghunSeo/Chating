package com.ayaan.twelvepages.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.TextView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.App.Companion.resource
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.manager.SymbolManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter.Formula.*
import android.graphics.DashPathEffect
import com.ayaan.twelvepages.adapter.util.RecordListComparator
import com.ayaan.twelvepages.manager.ColorManager


@SuppressLint("ViewConstructor")
class RecordView constructor(context: Context, val record: Record, var formula: RecordCalendarAdapter.Formula,
                             val cellNum: Int, var length: Int) : TextView(context) {
    companion object {
        var standardTextSize = 8f
        val baseSize = dpToPx(0.5f)
        val strokeWidth = dpToPx(1f) // 선
        val blockTypeSize = dpToPx(15.0f).toInt() // 블록 크기
        val defaulMargin = dpToPx(1.5f) // 뷰간 간격
        val defaultPadding = dpToPx(2.0f).toInt()
        val normalStickerSize = dpToPx(35f)
        val datePointSize = dpToPx(30)
        val rectRadius = dpToPx(0.0f)
        val dotSize = dpToPx(4)
        val checkboxSize = dpToPx(10)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val dashPath = DashPathEffect(floatArrayOf(dpToPx(5.0f), dpToPx(1.0f)), 2f)
        fun getStyleText(style: Int) : String{
            val formula = RecordCalendarAdapter.Formula.styleToFormula(style)
            val shape = Shape.styleToShape(style)
            return str(formula.nameId) + " / " + str(shape.nameId)
        }
    }

    enum class Shape(val nameId: Int, val isFillColor: Boolean, val isRange: Boolean) {
        BLANK(R.string.shape_blank, false, false),
        TEXT(R.string.shape_text, false, false),
        RECT_FILL(R.string.shape_rect_fill, true, false),
        RECT_STROKE(R.string.shape_rect_stroke, false, false),
        ROUND_FILL(R.string.shape_round_fill, true, true),
        ROUND_STROKE(R.string.shape_round_stroke, false, true),
        BOLD_HATCHED(R.string.shape_bold_hatched, true, false),
        THIN_HATCHED(R.string.shape_thin_hatched, false, false),
        UPPER_LINE(R.string.shape_upper_line, false, false),
        UNDER_LINE(R.string.shape_under_line, false, false),
        NEON_PEN(R.string.shape_neon_pen, false, false),
        RANGE(R.string.shape_range, false, true),
        DASH_RANGE(R.string.shape_dash_range, false, true),
        ARROW(R.string.shape_arrow, false, true),
        DASH_ARROW(R.string.shape_dash_arrow, false, true);

        companion object {
            fun styleToShape(style: Int) = values()[style % 10000 / 100]
        }
    }

    var mLeft = 0f
    var mTop = 0f
    var mRight = 0f
    var mBottom = 0f
    var leftOpen = false
    var rightOpen = false
    var textSpaceWidth = 0f
    var childList: ArrayList<Record>? = null
    var paintColor = AppTheme.background
    var fontColor = AppTheme.primaryText
    var shape = Shape.TEXT

    init {
        //setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize + AppStatus.calTextSize)
        setStyle()
    }

    @SuppressLint("RtlHardcoded")
    fun setStyle() {
        var sPadding = defaultPadding
        var textPadding = 0
        shape = record.getShape()
        typeface = AppTheme.regularFont
        //setTypeface(AppTheme.boldFont, Typeface.BOLD)
        when(formula) {
            SINGLE_TEXT, BOTTOM_SINGLE_TEXT -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize + AppStatus.calTextSize)
                text = record.getTitleInCalendar()
                setSingleLine(true)
                setHorizontallyScrolling(true)
                maxLines = 1
                ellipsize = null
                if(shape.isRange) {
                    sPadding *= 4
                    gravity = Gravity.CENTER
                }else {
                    gravity = Gravity.CENTER_VERTICAL
                }
            }
            MULTI_TEXT -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize + AppStatus.calTextSize - 1)
                text = record.getTitleInCalendar()
                gravity = Gravity.CENTER_VERTICAL
                setSingleLine(false)
                setHorizontallyScrolling(false)
                maxLines = 5
                ellipsize = TextUtils.TruncateAt.END
                textPadding = defaultPadding
            }
            else -> {}
        }

        val leftPadding = if(record.isSetCheckBox) {
            sPadding + checkboxSize
        }else {
            sPadding
        }
        setPadding(leftPadding, textPadding, sPadding, textPadding)

        paintColor = ColorManager.getColor(record.colorKey)
        fontColor = if(shape == Shape.NEON_PEN) {
            AppTheme.primaryText
        }else {
            if(shape.isFillColor) ColorManager.getFontColor(paintColor) else paintColor
        }
        setTextColor(fontColor)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            when(formula) {
                SINGLE_TEXT, MULTI_TEXT, BOTTOM_SINGLE_TEXT -> {
                    if(shape.isRange) {
                        drawRange(canvas)
                    }else {
                        drawBasicShape(canvas)
                    }
                    super.onDraw(canvas)
                }
                SYMBOL -> {
                    drawStamp(canvas)
                }
                DOT -> {
                    drawDot(canvas)
                }
                STICKER -> {
                    drawSticker(canvas)
                }
                DATE_POINT -> {
                    drawDatePoint(canvas)
                }
                else -> {}
            }
        }
    }

    fun getViewHeight(): Int {
        return when(formula) {
            SYMBOL -> {
                val itemSize = blockTypeSize - defaultPadding
                val width =  mRight - mLeft - defaultPadding * 2
                val margin = defaulMargin.toInt()
                val totalCnt = childList?.size ?: 0
                val rows = ((itemSize * totalCnt + margin * (totalCnt - 1)) / width).toInt() + 1
                ((itemSize + margin) * rows) + margin
            }
            DOT -> {
                val itemSize = dotSize
                val width =  mRight - mLeft - defaulMargin * 2 - defaultPadding * 2
                val margin = defaulMargin.toInt()
                val totalCnt = childList?.size ?: 0
                val rows = ((itemSize * totalCnt + margin * (totalCnt - 1)) / width).toInt() + 1
                ((itemSize + margin) * rows) + margin
            }
            MULTI_TEXT -> {
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
                    (measuredHeight + defaulMargin).toInt()
                }
            }
            else -> {
                textSpaceWidth = paint.measureText(text.toString())
                blockTypeSize
            }
        }
    }

    fun setLayout() {
        when(formula) {
            STICKER -> layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            DATE_POINT -> layoutParams = FrameLayout.LayoutParams(datePointSize, datePointSize).apply {
                topMargin = (datePointSize * -0.2f).toInt()
                leftMargin = (datePointSize * -0.2f).toInt()
            }
            else -> {
                layoutParams = FrameLayout.LayoutParams((mRight - mLeft - defaulMargin).toInt(),
                        (mBottom - mTop - defaulMargin).toInt()).apply {
                    topMargin = mTop.toInt()
                    leftMargin = defaultPadding
                }
            }
        }
    }

    private fun drawBasicShape(canvas: Canvas) {
        paint.color = paintColor
        paint.pathEffect = null
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
                paint.strokeWidth = strokeWidth * 1f
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
                if(leftOpen || rightOpen || length > 1) {
                    paint.alpha = 17
                    canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 0f, 0f, paint)
                    paint.alpha = 255
                }
            }
        }

        if(record.isSetCheckBox) {
            drawCheckBox(canvas, (defaultPadding - defaulMargin / 2).toInt())
        }
    }

    private fun drawDot(canvas: Canvas, xOffset: Int) {
        val radius = checkboxSize / 2f
        val centerY = (blockTypeSize - defaulMargin) / 2f
        if(record.isDone()) {
            if(AppStatus.checkedRecordDisplay in 2..3) {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            val check = resource.getDrawable(R.drawable.dot)
            check.setColorFilter(fontColor, PorterDuff.Mode.SRC_ATOP)
            check.setBounds(
                    xOffset,
                    (centerY - radius).toInt(),
                    xOffset + checkboxSize,
                    (centerY + radius).toInt()
            )
            check.draw(canvas)
        }else {
            paint.style = Paint.Style.STROKE
            val check = resource.getDrawable(R.drawable.dot)
            check.setColorFilter(fontColor, PorterDuff.Mode.SRC_ATOP)
            check.setBounds(
                    xOffset,
                    (centerY - radius).toInt(),
                    xOffset + checkboxSize,
                    (centerY + radius).toInt()
            )
            check.draw(canvas)
        }
        paint.style = Paint.Style.FILL
    }

    private fun drawCheckBox(canvas: Canvas, xOffset: Int) {
        val radius = checkboxSize / 2f
        val centerY = (blockTypeSize - defaulMargin) / 2f
        if(record.isDone()) {
            if(AppStatus.checkedRecordDisplay in 2..3) {
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
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

    private fun drawRange(canvas: Canvas) {
        paint.color = paintColor
        paint.pathEffect = null
        canvas.translate(scrollX.toFloat(), 0f)
        val space = textSpaceWidth + if(record.isSetCheckBox) checkboxSize else 0
        val sPadding = defaultPadding * 4

        var textLPos = width / 2 - space / 2 - defaulMargin * 2
        if(textLPos < sPadding) textLPos = sPadding - defaulMargin * 2
        var textRPos = width / 2 + space / 2 + defaulMargin * 2
        if(textRPos > width - sPadding) textRPos = width - sPadding.toFloat() + defaulMargin * 2

        when(shape){
            Shape.ROUND_FILL -> {
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), height / 2f, height / 2f, paint)
            }
            Shape.ROUND_STROKE -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth * 1f
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), height / 2f, height / 2f, paint)
                paint.style = Paint.Style.FILL
            }
            Shape.BOLD_HATCHED -> {
                canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), height / 2f, height / 2f, paint)
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
            Shape.NEON_PEN -> {
                paint.alpha = 70
                canvas.drawRoundRect(0f, height / 2f, width.toFloat(), height.toFloat(), 0f, 0f, paint)
                paint.alpha = 255
            }
            Shape.UPPER_LINE -> {
                canvas.drawRect(0f, 0f, width.toFloat(), strokeWidth, paint)
            }
            Shape.UNDER_LINE -> {
                val arrowSize = (strokeWidth * 4.0f).toInt()
                canvas.drawRect(arrowSize.toFloat(), height.toFloat() - strokeWidth, width.toFloat() - arrowSize, height.toFloat(), paint)
                drawArrow(canvas, 0, height, arrowSize, height - arrowSize, arrowSize, height)
                drawArrow(canvas, width, height, width - arrowSize, height - arrowSize, width - arrowSize, height)
            }
            else -> {
                val periodLine = strokeWidth * 1.8f
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = periodLine
                if(shape == Shape.DASH_RANGE || shape == Shape.DASH_ARROW) {
                    paint.pathEffect = dashPath
                }else {
                    paint.pathEffect = null
                }
                canvas.drawLine(periodLine, height / 2f, textLPos, height / 2f, paint)
                canvas.drawLine(textRPos, height / 2f, width - periodLine, height / 2f, paint)
                paint.style = Paint.Style.FILL

                if(shape == Shape.ARROW || shape == Shape.DASH_ARROW) {
                    val arrowSize = (periodLine * 1.8f).toInt()
                    val arrowWidth = (periodLine * 1.8f).toInt()
                            drawArrow(canvas, 0, height / 2, arrowWidth, height / 2 - arrowSize, arrowWidth, height / 2  + arrowSize)
                    drawArrow(canvas, width, height / 2, width - arrowWidth, height / 2 - arrowSize, width - arrowWidth, height / 2  + arrowSize)
                }else {
                    val dividerSize = periodLine * 2.0f
                    canvas.drawRect(0f, height / 2f - dividerSize, periodLine, height / 2f + dividerSize, paint)
                    canvas.drawRect(width - periodLine, height / 2f - dividerSize, width.toFloat(), height / 2f + dividerSize, paint)
                }
                /*
                val lineY = (periodLine * 1.5f).toInt()
                canvas.drawLine(periodLine, lineY.toFloat(), width - periodLine, lineY.toFloat(), paint)
                paint.style = Paint.Style.FILL

                if(shape == Shape.ARROW || shape == Shape.DASH_ARROW) {
                    val arrowSize = (periodLine * 3.0f).toInt()
                    val arrowWidth = (periodLine * 3.0f).toInt()
                    drawArrow(canvas, 0, lineY, arrowWidth, lineY - arrowSize, arrowWidth, lineY  + arrowSize)
                    drawArrow(canvas, width, lineY, width - arrowWidth, lineY - arrowSize, width - arrowWidth, lineY + arrowSize)
                }else {
                    canvas.drawRect(0f, height / 2f - periodLine * 3.0f, periodLine * 1.2f, height / 2f + periodLine * 3.0f, paint)
                    canvas.drawRect(width - periodLine * 1.2f, height / 2f - periodLine * 3.0f, width.toFloat(), height / 2f + periodLine * 3.0f, paint)
                }
                */
            }
        }
        if(record.isSetCheckBox) {
            drawCheckBox(canvas, (textLPos + defaulMargin).toInt())
        }
        canvas.translate(-scrollX.toFloat(), 0f)
    }

    private fun drawArrow(canvas: Canvas, x1: Int, y1: Int, x2: Int, y2: Int, x3: Int, y3: Int){
        val a = Point(x1, y1)
        val b = Point(x2, y2)
        val c = Point(x3, y3)
        val arrow = Path()
        arrow.fillType = Path.FillType.EVEN_ODD
        arrow.moveTo(a.x.toFloat(), a.y.toFloat())
        arrow.lineTo(b.x.toFloat(), b.y.toFloat())
        arrow.lineTo(c.x.toFloat(), c.y.toFloat())
        arrow.lineTo(a.x.toFloat(), a.y.toFloat())
        arrow.close()
        canvas.drawPath(arrow, paint)
    }

    private fun drawDot(canvas: Canvas) {
        val margin = defaulMargin
        val size = dotSize
        var top = 0f
        var left = defaultPadding + defaulMargin
        val right = width - defaultPadding - defaulMargin
        childList?.sortWith(RecordListComparator())
        childList?.forEach { child ->
            paint.color = child.getColor()
            //canvas.drawRect(left, top, left + size, top + size, paint)
            canvas.drawCircle(left + size/2, top + size/2, (size/2).toFloat(), paint)
            left += size + margin
            if (left + size >= right) {
                top += size + margin
                left = defaultPadding + defaulMargin
            }
        }
    }

    fun drawStamp(canvas: Canvas) {
        val margin = defaulMargin.toInt()
        val size = blockTypeSize - defaultPadding
        var top = 0
        var left = defaultPadding
        val right = width - defaulMargin
        childList?.forEach { child ->
            paint.color = child.getColor()
            canvas.drawCircle((left + size/2).toFloat(), (top + size/2).toFloat(), (size/2).toFloat(), paint)

            val stamp = resource.getDrawable(SymbolManager.getSymbolResId(child.symbol))
            stamp.setColorFilter(ColorManager.getFontColor(child.getColor()), PorterDuff.Mode.SRC_ATOP)
            stamp.setBounds(left + defaultPadding, top + defaultPadding,
                    left + size - defaultPadding, top + size - defaultPadding)
            stamp.draw(canvas)
/*
            val stroke = resource.getDrawable(R.drawable.circle_stroke_1px)
            stroke.setColorFilter(record.getColor(), PorterDuff.Mode.SRC_ATOP)
            stroke.setBounds(left, top, left + size, top + size)
            stroke.draw(canvas)
*/
            left += size + margin
            if(left + size >= right) {
                top += size + margin
                left = defaultPadding
            }
        }
    }

    private fun drawSticker(canvas: Canvas) {
        val childCount = childList?.size ?: 0
        when(childCount) {
            1 -> {
                val size = normalStickerSize.toInt()
                val top = (height - normalStickerSize - defaultPadding).toInt()
                val left = (width - normalStickerSize - defaulMargin).toInt()
                val resId = childList?.get(0)?.getSticker()?.resId ?: R.drawable.help
                resource.getDrawable(resId, null)?.let {
                    it.setBounds(left, top, (left + size), (top + size))
                    it.draw(canvas)
                }
            }
            2 -> {
                val size = (normalStickerSize * 0.55f).toInt()
                val top = (height - size - defaultPadding)
                var left = (width - size * 2 - defaulMargin).toInt()
                var resId = childList?.get(0)?.getSticker()?.resId ?: R.drawable.help
                resource.getDrawable(resId, null)?.let {
                    it.setBounds(left, top, (left + size), (top + size))
                    it.draw(canvas)
                }
                resId = childList?.get(1)?.getSticker()?.resId ?: R.drawable.help
                left += size
                resource.getDrawable(resId, null)?.let {
                    it.setBounds(left, top, (left + size), (top + size))
                    it.draw(canvas)
                }
            }
            3 -> {
                val size = (normalStickerSize * 0.55f).toInt()
                var top = (height - size - defaultPadding).toInt()
                var left = (width - size * 2 - defaulMargin).toInt()
                var resId = childList?.get(0)?.getSticker()?.resId ?: R.drawable.help
                resource.getDrawable(resId, null)?.let {
                    it.setBounds(left, top, (left + size), (top + size))
                    it.draw(canvas)
                }
                resId = childList?.get(1)?.getSticker()?.resId ?: R.drawable.help
                left += size
                resource.getDrawable(resId, null)?.let {
                    it.setBounds(left, top, (left + size), (top + size))
                    it.draw(canvas)
                }
                resId = childList?.get(2)?.getSticker()?.resId ?: R.drawable.help
                left -= (size * 0.5f).toInt()
                top -= (size * 0.8f).toInt()
                resource.getDrawable(resId, null)?.let {
                    it.setBounds(left, top, (left + size), (top + size))
                    it.draw(canvas)
                }
            }
            4 -> {
                val size = (normalStickerSize * 0.50f).toInt()
                var top = (height - size - defaultPadding)
                var left = (width - size * 2 - defaulMargin).toInt()
                var resId = childList?.get(0)?.getSticker()?.resId ?: R.drawable.help
                resource.getDrawable(resId, null)?.let {
                    it.setBounds(left, top, (left + size), (top + size))
                    it.draw(canvas)
                }
                resId = childList?.get(1)?.getSticker()?.resId ?: R.drawable.help
                left += size
                resource.getDrawable(resId, null)?.let {
                    it.setBounds(left, top, (left + size), (top + size))
                    it.draw(canvas)
                }
                resId = childList?.get(2)?.getSticker()?.resId ?: R.drawable.help
                left -= size
                top -= size
                resource.getDrawable(resId, null)?.let {
                    it.setBounds(left, top, (left + size), (top + size))
                    it.draw(canvas)
                }
                resId = childList?.get(3)?.getSticker()?.resId ?: R.drawable.help
                left += size
                resource.getDrawable(resId, null)?.let {
                    it.setBounds(left, top, (left + size), (top + size))
                    it.draw(canvas)
                }
            }
            else -> {
                val size = Math.max(normalStickerSize * 0.33f, normalStickerSize * (1f - 0.05f * childCount))
                var right = width - defaulMargin
                val bottom = height - defaultPadding
                val overlap = size - ((width - defaulMargin - defaultPadding) - (size * childCount)) / (1 - childCount)
                (childCount - 1 downTo 0).forEach { index ->
                    val resId = childList?.get(index)?.getSticker()?.resId ?: R.drawable.help
                    resource.getDrawable(resId, null)?.let {
                        it.setBounds((right - size).toInt(), (bottom - size).toInt(), right.toInt(), bottom)
                        it.draw(canvas)
                    }
                    right -= overlap
                }
            }
        }
    }

    private fun drawDatePoint(canvas: Canvas) {
        val size = datePointSize
        var top = (baseSize * 2).toInt()
        var left = -(baseSize * 4).toInt()
        childList?.forEach { child ->
            val sticker = child.getSticker()
            val circle = resource.getDrawable(sticker?.resId ?: R.drawable.help, null)
            circle.alpha = 150
            circle.setBounds(left, top, (left + size), (top + size))
            circle.draw(canvas)
        }
    }

    fun drawNotInCalendar(canvas: Canvas) {
        val legnth =  baseSize * 10
        val gap = baseSize * 2
        val startY = (blockTypeSize - defaulMargin) / 2f - legnth / 2f
        paint.strokeWidth = gap
        childList?.forEachIndexed { index, timeObject ->
            if(index < 8) {
                val startX = defaultPadding + (index * gap * 2)
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
                    paint.style = Paint.Style.SINGLE_TEXT
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
                val edge = defaultPadding

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