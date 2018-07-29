package com.hellowo.chating.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import com.hellowo.chating.*
import java.util.*

@SuppressLint("ViewConstructor")
class TimeObjectView constructor(context: Context, val timeObject: TimeObject, val cellNum: Int, val Length: Int) : TextView(context) {
    companion object {
        val strokeWidth = dpToPx(1)
        val radius = dpToPx(1).toFloat()
        val circleRadius = dpToPx(5).toFloat()
        val checkBoxSize = dpToPx(7).toFloat()
        val defaulMargin = dpToPx(1)
        val defaultPadding = dpToPx(4)
        val normalTypeSize = dpToPx(17)
        val smallTypeSize = dpToPx(13)
        val bigTypeSize = dpToPx(27)
        val memoTypeSize = dpToPx(70)
        val levelMargin = dpToPx(5)
        val tempCal = Calendar.getInstance()
    }

    var mTextSize = 9f
    var mLeft = 0
    var mTop = 0
    var mRight = 0
    var mBottom = 0
    var mLine = 0
    var color = Color.parseColor("#757575")
    var leftOpen = false
    var rightOpen = false

    init {
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, mTextSize)
        text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        canvas?.let {
            val paint = Paint()
            paint.isAntiAlias = true
            when(TimeObject.Type.values()[timeObject.type]) {
                TimeObject.Type.EVENT -> {
                    setSingleLine(true)
                    gravity = Gravity.CENTER_VERTICAL
                    setHorizontallyScrolling(true)

                    paint.color = color
                    paint.style = Paint.Style.FILL

                    when(TimeObject.Style.values()[timeObject.style]){
                        TimeObject.Style.SHORT -> {
                            setPadding(smallTypeSize, 0, defaultPadding, 0)
                            setTextColor(color)
                            val centerX = smallTypeSize / 2f
                            val centerY = smallTypeSize / 2f
                            it.drawRect(RectF(centerX - strokeWidth * 0.5f , 0f, centerX + strokeWidth * 0.5f, smallTypeSize.toFloat()), paint)
                            it.drawCircle(centerX, centerY, strokeWidth * 2.5f, paint)
                        }
                        else -> {
                            setPadding(defaultPadding, 0, defaultPadding, 0)
                            setTextColor(Color.WHITE)
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
                    }

                }
                TimeObject.Type.TODO -> {
                    setPadding(smallTypeSize, 0, defaultPadding, 0)
                    setTextColor(color)
                    setSingleLine(true)
                    gravity = Gravity.CENTER_VERTICAL
                    setHorizontallyScrolling(true)

                    paint.strokeWidth = strokeWidth.toFloat()
                    paint.color = color
                    paint.style = Paint.Style.FILL

                    val center = smallTypeSize / 2f
                    val checkRadius = checkBoxSize / 2
                    val rect = RectF(center - checkRadius, center - checkRadius,
                            center + checkRadius, center + checkRadius)
                    if(true) {
                        //alpha = 0.5f
                        //paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                        it.drawRect(rect, paint)
                        paint.color = Color.WHITE
                        it.drawLine(center - checkRadius * 0.75f, center,
                                center - checkRadius * 0.25f, center + checkRadius * 0.5f, paint)
                        it.drawLine(center - checkRadius * 0.25f, center + checkRadius * 0.5f,
                                center + checkRadius * 0.75f, center - checkRadius * 0.5f, paint)
                    }else {
                        paint.style = Paint.Style.STROKE
                        it.drawRoundRect(rect, radius, radius, paint)
                    }
                }
                TimeObject.Type.MEMO -> {
                    setPadding(defaultPadding * 2, defaultPadding * 2, defaultPadding * 2, defaultPadding * 2)
                    setSingleLine(false)
                    maxLines = 4
                    setTextColor(color)
                    gravity = Gravity.TOP
                    ellipsize = TextUtils.TruncateAt.END

                    paint.strokeWidth = strokeWidth.toFloat()
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
                4 -> {
                    val paint = Paint()
                    paint.style = Paint.Style.FILL
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
        TimeObject.Type.EVENT -> {
            when (TimeObject.Style.values()[timeObject.style]) {
                TimeObject.Style.SHORT -> smallTypeSize
                TimeObject.Style.LONG -> bigTypeSize
                else -> normalTypeSize
            }
        }
        TimeObject.Type.TODO -> smallTypeSize
        TimeObject.Type.MEMO -> memoTypeSize
        else -> normalTypeSize
    }

    fun setLayout() {
        val lp = FrameLayout.LayoutParams(mRight - mLeft - defaulMargin, mBottom - mTop - defaulMargin)
        lp.setMargins(mLeft, mTop, 0, 0)
        layoutParams = lp
    }

    override fun toString(): String {
        return "TimeObjectView(timeObject=$timeObject, cellNum=$cellNum, Length=$Length, mTextSize=$mTextSize, mLeft=$mLeft, mTop=$mTop, mRight=$mRight, mBottom=$mBottom, mLine=$mLine)"
    }

}