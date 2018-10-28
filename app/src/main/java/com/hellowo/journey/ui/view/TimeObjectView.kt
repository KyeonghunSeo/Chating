package com.hellowo.journey.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.hellowo.journey.*
import com.hellowo.journey.model.CalendarSkin
import com.hellowo.journey.model.TimeObject

@SuppressLint("ViewConstructor")
class TimeObjectView constructor(context: Context, val timeObject: TimeObject, val cellNum: Int, val length: Int) : TextView(context) {
    companion object {
        val defaulMargin = dpToPx(1) // 뷰간 간격

        val strokeWidth = dpToPx(0.5f)
        val rectRadius = dpToPx(1).toFloat()
        val circleRadius = dpToPx(5).toFloat()
        val checkBoxSize = dpToPx(7).toFloat()
        val defaultPadding = dpToPx(4)
        val normalTypeSize = dpToPx(18)
        val smallTypeSize = dpToPx(15)
        val bigTypeSize = dpToPx(30)
        val memoTypeSize = dpToPx(70)
        val levelMargin = dpToPx(5)

        val leftPadding = dpToPx(9)
        val rightPadding = dpToPx(0)
        val topBottomPadding = dpToPx(2)
        val icon = dpToPx(8)
        val iconSize = dpToPx(8)
        val iconTopYCenter = dpToPx(8).toFloat()

        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    }

    var mTextSize = 9f
    var mLeft = 0
    var mTop = 0
    var mRight = 0
    var mBottom = 0
    var mLine = 0
    var color = timeObject.color
    var leftOpen = false
    var rightOpen = false
    var line = 0

    init {
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, mTextSize)
        text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
        typeface = AppRes.regularFont

        when(TimeObject.Type.values()[timeObject.type]) {
            TimeObject.Type.NOTE -> {

            }
            TimeObject.Type.EVENT -> {
                setPadding(defaultPadding, 0, defaultPadding, 0)
                setTextColor(timeObject.fontColor)
            }
            TimeObject.Type.TASK -> {
                setPadding(leftPadding, 0, rightPadding, 0)
            }
            else -> {
                setPadding(defaultPadding, 0, defaultPadding, 0)
            }
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            paint.isAntiAlias = true
            when(TimeObject.Type.values()[timeObject.type]) {
                TimeObject.Type.NOTE -> {
                    setTextColor(CalendarSkin.dateColor)
                    paint.color = CalendarSkin.dateColor

                    when(TimeObject.Style.values()[timeObject.style]){
                        TimeObject.Style.DEFAULT -> {
                            val centerX = leftPadding / 2f
                            /*
                            AppRes.starDrawable?.setBounds(0, (centerY - iconSize / 2).toInt(), iconSize, (centerY + iconSize / 2).toInt())
                            AppRes.starDrawable?.draw(canvas)
                            */
/*
                            val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                            paint.color = timeObject.color
                            paint.alpha = 255
                            it.drawRoundRect(rect, radius, radius, paint)
*/
                            paint.color = CalendarSkin.dateColor
                            it.drawCircle(centerX, iconTopYCenter, iconSize * 0.2f, paint)
                        }
                    }
                }
                TimeObject.Type.EVENT -> {
                    gravity = Gravity.CENTER_VERTICAL
                    maxLines = 1
                    setSingleLine(true)
                    setHorizontallyScrolling(true)

                    paint.color = color
                    paint.style = Paint.Style.FILL

                    when(TimeObject.Style.values()[timeObject.style]){
                        TimeObject.Style.SHORT -> {
                            setPadding(smallTypeSize, 0, defaultPadding, 0)
                            val centerX = smallTypeSize / 2f
                            val centerY = smallTypeSize / 2f
                            it.drawRect(RectF(centerX - strokeWidth * 0.5f , 0f, centerX + strokeWidth * 0.5f, smallTypeSize.toFloat()), paint)
                            it.drawCircle(centerX, centerY, strokeWidth * 2.5f, paint)
                        }
                        TimeObject.Style.LONG -> {
                        }
                        else -> {
                            var left = 0f
                            var right = width.toFloat()
                            if(leftOpen) {
                                /*
                                left = defaultPadding.toFloat()
                                val path = Path()
                                path.moveTo(defaultPadding + rectRadius, 0f)
                                path.lineTo(defaultPadding.toFloat(), 0f)
                                path.lineTo(0f, height * 0.5f)
                                path.lineTo(defaultPadding.toFloat(), height.toFloat())
                                path.lineTo(defaultPadding.toFloat() + rectRadius, height.toFloat())
                                path.lineTo(defaultPadding + rectRadius, 0f)
                                path.close()
                                it.drawPath(path, paint)
                                */
                                left = defaultPadding.toFloat()
                                val path = Path()
                                path.moveTo(0f, 0f)
                                path.lineTo(0f, height.toFloat() - defaultPadding)
                                path.lineTo(defaultPadding.toFloat(), height.toFloat())
                                path.lineTo(defaultPadding.toFloat(), 0f)
                                path.lineTo(0f, 0f)
                                path.close()
                                it.drawPath(path, paint)
                            }
                            if(rightOpen) {
                                /*
                                right = width.toFloat() - defaultPadding
                                val path = Path()
                                path.moveTo(right - rectRadius, 0f)
                                path.lineTo(right, 0f)
                                path.lineTo(right + defaultPadding, height * 0.5f)
                                path.lineTo(right, height.toFloat())
                                path.lineTo(right - rectRadius, height.toFloat())
                                path.lineTo(right - rectRadius, 0f)
                                path.close()
                                it.drawPath(path, paint)
                                */
                                right = width.toFloat() - defaultPadding
                                val path = Path()
                                path.moveTo(right, 0f)
                                path.lineTo(right, height.toFloat())
                                path.lineTo(right + defaultPadding, height.toFloat())
                                path.lineTo(right + defaultPadding, defaultPadding.toFloat())
                                path.lineTo(right, 0f)
                                path.close()
                                it.drawPath(path, paint)
                            }
                            val rect = RectF(left, 0f, right, height.toFloat())
                            it.drawRoundRect(rect, rectRadius, rectRadius, paint)
                        }
                    }

                }
                TimeObject.Type.TASK -> {
                    setTextColor(color)
                    gravity = Gravity.CENTER_VERTICAL
                    maxLines = 1
                    setSingleLine(true)
                    setHorizontallyScrolling(true)

                    paint.strokeWidth = strokeWidth
                    paint.color = color
                    paint.style = Paint.Style.STROKE

                    val centerY = smallTypeSize / 2f - defaulMargin - strokeWidth
                    val checkRadius = leftPadding / 3f
                    val centerX = checkRadius + strokeWidth
                    val rect = RectF(centerX - checkRadius, centerY - checkRadius, centerX + checkRadius, centerY + checkRadius)
                    if(timeObject.isDone()) {
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        it.drawRect(rect, paint)
                        it.drawLine(centerX - checkRadius * 0.75f, centerY,
                                centerX - checkRadius * 0.25f, centerY + checkRadius * 0.5f, paint)
                        it.drawLine(centerX - checkRadius * 0.25f, centerY + checkRadius * 0.5f,
                                centerX + checkRadius * 0.75f, centerY - checkRadius * 0.5f, paint)
                    }else {
                        it.drawRect(rect, paint)
                    }
                    paint.style = Paint.Style.FILL
                }
                TimeObject.Type.DECORATION -> {
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

                }
                /*
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
                    paint.style = Paint.Style.TOPSTACK
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
        super.onDraw(canvas)
    }

    fun getViewHeight(): Int = when(TimeObject.Type.values()[timeObject.type]) {
        TimeObject.Type.NOTE -> {
            setSingleLine(false)
            maxLines = 5
            gravity = Gravity.TOP
            ellipsize = TextUtils.TruncateAt.END
            setPadding(leftPadding, topBottomPadding, rightPadding, topBottomPadding * 2)
            val width =  mRight - mLeft - defaulMargin
            measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), heightMeasureSpec)
            line = (paint.measureText(text.toString()) / (width - leftPadding)).toInt() + 1
            measuredHeight
        }
        TimeObject.Type.EVENT -> {
            when (TimeObject.Style.values()[timeObject.style]) {
                TimeObject.Style.SHORT -> smallTypeSize
                TimeObject.Style.LONG -> bigTypeSize
                else -> normalTypeSize
            }
        }
        TimeObject.Type.TASK -> smallTypeSize
        TimeObject.Type.DECORATION -> memoTypeSize
        else -> normalTypeSize
    }

    fun setLayout() {
        val lp = FrameLayout.LayoutParams(mRight - mLeft - defaulMargin, mBottom - mTop - defaulMargin)
        lp.setMargins(0, mTop, 0, 0)
        layoutParams = lp
    }

    override fun toString(): String {
        return "TimeObjectView(timeObject=$timeObject, cellNum=$cellNum, length=$length, mTextSize=$mTextSize, mLeft=$mLeft, mTop=$mTop, mRight=$mRight, mBottom=$mBottom, mLine=$mLine)"
    }

}