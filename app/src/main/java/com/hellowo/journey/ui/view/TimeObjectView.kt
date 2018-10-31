package com.hellowo.journey.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Typeface.ITALIC
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.hellowo.journey.*
import com.hellowo.journey.calendar.CalendarSkin
import com.hellowo.journey.model.TimeObject
import java.time.format.TextStyle

@SuppressLint("ViewConstructor")
class TimeObjectView constructor(context: Context, val timeObject: TimeObject, val cellNum: Int, val length: Int) : TextView(context) {
    companion object {
        var standardTextSize = 9f
        val defaulMargin = dpToPx(1.5f) // 뷰간 간격
        val defaultPadding = dpToPx(4)
        val strokeWidth = dpToPx(0.5f) // 선 간격
        val rectRadius = dpToPx(0f)
        val circleRadius = dpToPx(5f)
        val normalTypeSize = dpToPx(18)
        val smallTypeSize = dpToPx(13)
        val bigTypeSize = dpToPx(25)
        val iconSize = dpToPx(8)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    }
    var mLeft = 0f
    var mTop = 0f
    var mRight = 0f
    var mBottom = 0f
    var mLine = 0
    var color = timeObject.color
    var leftOpen = false
    var rightOpen = false
    var textSpaceWidth = 0f
    //var line = 0

    init {
        includeFontPadding = false
        //setBackgroundColor(AppRes.almostWhite)
    }

    fun setLookByType() {
        when(TimeObject.Type.values()[timeObject.type]) {
            TimeObject.Type.EVENT -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                typeface = AppRes.thinFont
                gravity = Gravity.CENTER_VERTICAL
                maxLines = 1
                setSingleLine(true)
                setHorizontallyScrolling(true)
                val leftSideMargin = if(leftOpen) defaultPadding else 0
                when(timeObject.style){
                    1 -> {
                        setPadding(defaultPadding + leftSideMargin, 0, defaultPadding, 0)
                        setTextColor(timeObject.color)
                    }
                    2 -> {
                        setPadding((defaulMargin * 5 + leftSideMargin).toInt(), 0, defaultPadding, 0)
                        setTextColor(AppRes.primaryText)
                    }
                    else -> {
                        setPadding(defaultPadding + leftSideMargin, 0, defaultPadding, 0)
                        setTextColor(timeObject.fontColor)
                    }
                }
            }
            TimeObject.Type.TASK -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize - 1)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                typeface = AppRes.thinFont
                gravity = Gravity.CENTER_VERTICAL
                maxLines = 1
                setSingleLine(true)
                setHorizontallyScrolling(true)
                when(timeObject.style){
                    1 -> {
                        setPadding(defaultPadding, 0, defaultPadding, 0)
                        setTextColor(AppRes.primaryText)
                    }
                    else -> {
                        setPadding(iconSize + defaultPadding, 0, defaultPadding, 0)
                        setTextColor(AppRes.primaryText)
                    }
                }
            }
            TimeObject.Type.NOTE -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize - 1)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.empty_note)
                typeface = AppRes.textFont
                setLineSpacing(strokeWidth, 1f)
                setPadding(defaulMargin.toInt(), (iconSize + defaulMargin).toInt(), defaultPadding, 0)
                setTextColor(CalendarSkin.dateColor)
            }
            TimeObject.Type.TERM -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                gravity = Gravity.CENTER_HORIZONTAL
                maxLines = 1
                setSingleLine(true)
                setHorizontallyScrolling(true)
                when(timeObject.style){
                    1 -> {
                        setTypeface(AppRes.regularFont, ITALIC)
                        gravity = Gravity.CENTER_HORIZONTAL
                        setPadding(defaultPadding, 0, defaultPadding, 0)
                        setTextColor(timeObject.color)
                    }
                    2 -> {
                        setPadding(defaulMargin.toInt(), 0, defaultPadding, 0)
                        setTextColor(AppRes.primaryText)
                    }
                    else -> {
                        setTypeface(AppRes.regularFont, ITALIC)
                        gravity = Gravity.CENTER
                        setPadding(defaultPadding, 0, defaultPadding, 0)
                        setTextColor(timeObject.color)
                    }
                }
            }
            else -> {
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                typeface = AppRes.regularFont
                setPadding(defaultPadding, 0, defaultPadding, 0)
            }
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            paint.isAntiAlias = true
            when(TimeObject.Type.values()[timeObject.type]) {
                TimeObject.Type.EVENT -> {
                    paint.color = color
                    when(timeObject.style){
                        1 -> {
                            paint.style = Paint.Style.STROKE
                            paint.strokeWidth = strokeWidth * 4f
                            val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                            it.drawRoundRect(rect, rectRadius, rectRadius, paint)
                            paint.style = Paint.Style.FILL
                        }
                        2 -> {
                            paint.alpha = 15
                            val bgrect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                            it.drawRoundRect(bgrect, rectRadius, rectRadius, paint)
                            paint.alpha = 255
                            val rect = if(length > 1) {
                                RectF(0f, 0f, strokeWidth * 4, height.toFloat())
                            }else {
                                RectF(0f, 0f, strokeWidth * 4, height.toFloat())
                            }
                            if(!leftOpen) it.drawRect(rect, paint)
                        }
                        else -> {
                            paint.style = Paint.Style.FILL
                            val edge = defaultPadding.toFloat()
                            var left = 0f
                            var right = width.toFloat()
                            if(leftOpen) {
                                left = defaultPadding.toFloat()
                                val path = Path()
                                path.moveTo(0f, 0f)
                                path.lineTo(edge, 0f)
                                path.lineTo(edge, height.toFloat())
                                path.lineTo(0f, height.toFloat())
                                path.lineTo(edge, height / 2f)
                                path.lineTo(0f, 0f)
                                path.close()
                                it.drawPath(path, paint)
                            }
                            if(rightOpen) {
                                right = width.toFloat() - defaultPadding
                                val path = Path()
                                path.moveTo(right, 0f)
                                path.lineTo(right + edge, height * 0.5f)
                                path.lineTo(right, height.toFloat())
                                path.lineTo(right, 0f)
                                path.close()
                                it.drawPath(path, paint)
                            }
                            val rect = RectF(left, 0f, right, height.toFloat())
                            it.drawRoundRect(rect, rectRadius, rectRadius, paint)
                        }
                    }
                    super.onDraw(canvas)
                }
                TimeObject.Type.TASK -> {
                    when(timeObject.style) {
                        1 -> {
                            if(timeObject.isDone()) {
                                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                            }
                            it.drawRect(RectF(0f, height.toFloat(), width.toFloat(), height.toFloat()), paint)
                        }
                        else -> {
                            paint.strokeWidth = strokeWidth
                            paint.color = color
                            paint.style = Paint.Style.STROKE

                            val centerY = (smallTypeSize - defaulMargin) / 2f - strokeWidth
                            val checkRadius = iconSize / 2.5f
                            val centerX = checkRadius + defaulMargin
                            val rect = RectF(centerX - checkRadius, centerY - checkRadius, centerX + checkRadius, centerY + checkRadius)
                            if(timeObject.isDone()) {
                                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                it.drawRect(rect, paint)
                                it.drawLine(centerX - checkRadius * 0.65f, centerY,
                                        centerX - checkRadius * 0.25f, centerY + checkRadius * 0.5f, paint)
                                it.drawLine(centerX - checkRadius * 0.25f, centerY + checkRadius * 0.5f,
                                        centerX + checkRadius * 0.65f, centerY - checkRadius * 0.5f, paint)
                            }else {
                                it.drawRect(rect, paint)
                            }
                            paint.style = Paint.Style.FILL
                        }
                    }
                    super.onDraw(canvas)
                }
                TimeObject.Type.NOTE -> {
                    paint.color = CalendarSkin.dateColor
                    when(TimeObject.Style.values()[timeObject.style]){
                        TimeObject.Style.DEFAULT -> {
                            if(false) {
                                AppRes.ideaDrawable.setBounds(0, 0, iconSize, iconSize)
                                AppRes.ideaDrawable.draw(canvas)
                            }else {
                                val rect = RectF(defaulMargin, iconSize / 2f,
                                        iconSize.toFloat() + defaulMargin, iconSize / 2f + strokeWidth * 2)
                                it.drawRect(rect, paint)
                            }
                        }
                    }
                    super.onDraw(canvas)
                }
                TimeObject.Type.STAMP -> {
                    CalendarSkin.drawStamp(canvas, this)
                }
                TimeObject.Type.TERM -> {
                    super.onDraw(canvas)
                    CalendarSkin.drawTerm(canvas, this)
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
                    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.p1)
                    val bitmapDrawable = BitmapDrawable(resources, bitmap)
                    bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
                    bitmapDrawable.setTargetDensity(it)
                    it.drawBitmap(bitmapDrawable.bitmap, 0f, 0f, Paint())
                }
                */
                else -> {}
            }
        }
    }

    fun getViewHeight(): Int = when(TimeObject.Type.values()[timeObject.type]) {
        TimeObject.Type.EVENT -> {
            when (TimeObject.Style.values()[timeObject.style]) {
                TimeObject.Style.SHORT -> smallTypeSize
                else -> normalTypeSize
            }
        }
        TimeObject.Type.TASK -> smallTypeSize
        TimeObject.Type.NOTE -> {
            setSingleLine(false)
            maxLines = 10
            gravity = Gravity.TOP
            ellipsize = TextUtils.TruncateAt.END
            val width =  mRight - mLeft - defaulMargin
            measure(View.MeasureSpec.makeMeasureSpec(width.toInt(), View.MeasureSpec.EXACTLY), heightMeasureSpec)
            //line = (paint.measureText(text.toString()) / width).toInt() + 1
            measuredHeight + /*폰트 자체 패딩때문에 조금 여유를 줘야함*/defaultPadding
        }
        TimeObject.Type.STAMP -> {
            bigTypeSize
        }
        TimeObject.Type.TERM -> {
            textSpaceWidth = paint.measureText(text.toString())
            normalTypeSize
        }
        else -> normalTypeSize
    }

    fun setLayout() {
        var w = mRight - mLeft - defaulMargin
        if(leftOpen) {
            w += defaultPadding
            translationX = -defaultPadding.toFloat()
        }
        if(rightOpen) w += defaultPadding

        val lp = FrameLayout.LayoutParams(w.toInt(), (mBottom - mTop - defaulMargin).toInt())
        lp.setMargins(0, mTop.toInt(), 0, 0)
        layoutParams = lp
    }

}