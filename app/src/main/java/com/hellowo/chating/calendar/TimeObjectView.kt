package com.hellowo.chating.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.hellowo.chating.*
import java.util.*
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.BitmapFactory
import android.graphics.Bitmap

@SuppressLint("ViewConstructor")
class TimeObjectView constructor(context: Context, val timeObject: TimeObject, val cellNum: Int, val Length: Int) : TextView(context) {
    companion object {
        val strokeWidth = dpToPx(1)
        val radius = dpToPx(1).toFloat()
        val circleRadius = dpToPx(5).toFloat()
        val defaulMargin = dpToPx(1)
        val defaultPadding = dpToPx(4)
        val eventTypeSize = dpToPx(17)
        val todoTypeSize = dpToPx(13)
        val memoTypeSize = dpToPx(13)
        val tempCal = Calendar.getInstance()
    }

    var mTextSize = 9f
    var mLeft = 0
    var mTop = 0
    var mRight = 0
    var mBottom = 0
    var mLine = 0
    var mOrder = 0
    var color = CalendarSkin.dateColor
    var leftOpen = false
    var rightOpen = false

    init {
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, mTextSize)
        text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
        gravity = Gravity.CENTER_VERTICAL

        when(timeObject.type) {
            0 -> {
                setPadding(defaultPadding, 0, defaultPadding, 0)
                setSingleLine(true)
                setHorizontallyScrolling(true)
                setTextColor(Color.WHITE)
            }
            1 -> {
                setPadding(defaultPadding, 0, defaultPadding, 0)
                setSingleLine(true)
                setHorizontallyScrolling(true)
                setTextColor(color)
            }
            2 -> {
                setPadding(todoTypeSize, 0, defaultPadding, 0)
                setSingleLine(true)
                setHorizontallyScrolling(true)
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                setTextColor(color)
            }
            3 -> {
                setPadding(todoTypeSize, 0, defaultPadding, 0)
                setSingleLine(true)
                setHorizontallyScrolling(true)
                setTextColor(color)
            }
            else -> {}
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            when(timeObject.type) {
                0 -> {
                    val paint = Paint()
                    paint.style = Paint.Style.FILL
                    paint.color = color
                    paint.isAntiAlias = true
                    var left = 0f
                    var right = width.toFloat()
                    if(leftOpen) {
                        left = defaultPadding.toFloat()
                        val path = Path()
                        path.moveTo(defaultPadding + radius, 0f)
                        path.lineTo(defaultPadding.toFloat(), 0f)
                        path.lineTo(0f, height * 0.5f)
                        path.lineTo(defaultPadding.toFloat(), height.toFloat())
                        path.lineTo(defaultPadding.toFloat() + radius, height.toFloat())
                        path.lineTo(defaultPadding + radius, 0f)
                        path.close()
                        it.drawPath(path, paint)
                    }
                    if(rightOpen) {
                        right = width.toFloat() - defaultPadding
                        val path = Path()
                        path.moveTo(right - radius, 0f)
                        path.lineTo(right, 0f)
                        path.lineTo(right + defaultPadding, height * 0.5f)
                        path.lineTo(right, height.toFloat())
                        path.lineTo(right - radius, height.toFloat())
                        path.lineTo(right - radius, 0f)
                        path.close()
                        it.drawPath(path, paint)
                    }
                    val rect = RectF(left, 0f, right, height.toFloat())
                    it.drawRoundRect(rect, radius, radius, paint)
                }
                1 -> {
                    val paint = Paint()
                    paint.style = Paint.Style.FILL
                    paint.color = color
                    paint.isAntiAlias = true
                    paint.alpha = 30
                    it.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), radius, radius, paint)
                    paint.alpha = 255
                    it.drawRect(RectF(0f, 0f, strokeWidth * 2f, height.toFloat()), paint)
                }
                2 -> {
                    val paint = Paint()
                    paint.style = Paint.Style.FILL
                    paint.strokeWidth = strokeWidth.toFloat()
                    paint.color = color
                    paint.isAntiAlias = true
                    val center = todoTypeSize / 2f
                    val rect = RectF(center - circleRadius, center - circleRadius, center + circleRadius, center + circleRadius)
                    it.drawRoundRect(rect, radius, radius, paint)
                    paint.color = Color.WHITE
                    it.drawLine(center - circleRadius * 0.75f, center,
                            center - circleRadius * 0.25f, center + circleRadius * 0.5f, paint)
                    it.drawLine(center - circleRadius * 0.25f, center + circleRadius * 0.5f,
                            center + circleRadius * 0.75f, center - circleRadius * 0.5f, paint)

                }
                3 -> {
                    val paint = Paint()
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = strokeWidth.toFloat()
                    paint.color = color
                    paint.isAntiAlias = true
                    val center = todoTypeSize / 2f
                    val rect = RectF(center - circleRadius, center - circleRadius, center + circleRadius, center + circleRadius)
                    it.drawRoundRect(rect, radius, radius, paint)
                }
                4 -> {
                    val paint = Paint()
                    paint.style = Paint.Style.FILL
                    paint.strokeWidth = strokeWidth.toFloat()
                    paint.color = color
                    paint.isAntiAlias = true
                    tempCal.timeInMillis = timeObject.dtStart
                    val degreeH = tempCal.get(Calendar.HOUR_OF_DAY) % 12 * 360 / 12 + 270
                    val sX = eventTypeSize / 2f
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
                else -> {}
            }
        }
        super.onDraw(canvas)
    }

    fun getTypeHeight(): Int = when(timeObject.type) {
        0 -> eventTypeSize
        1 -> todoTypeSize
        2 -> memoTypeSize
        else -> eventTypeSize
    }

    fun setLayout() {
        val lp = FrameLayout.LayoutParams(mRight - mLeft - defaulMargin, mBottom - mTop - defaulMargin)
        lp.setMargins(mLeft, mTop, 0, 0)
        layoutParams = lp
    }

    override fun toString(): String {
        return "TimeObjectView(timeObject=$timeObject, cellNum=$cellNum, Length=$Length, mTextSize=$mTextSize, mLeft=$mLeft, mTop=$mTop, mRight=$mRight, mBottom=$mBottom, mLine=$mLine, mOrder=$mOrder)"
    }

}