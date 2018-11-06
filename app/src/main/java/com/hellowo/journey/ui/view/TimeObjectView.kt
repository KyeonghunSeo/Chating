package com.hellowo.journey.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Typeface.*
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
        val normalTypeSize = dpToPx(17)
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
    var leftOpen = false
    var rightOpen = false
    var textSpaceWidth = 0f
    var childList: ArrayList<TimeObject>? = null
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
                gravity = Gravity.CENTER_VERTICAL
                maxLines = 1
                //setSingleLine(true)
                //setHorizontallyScrolling(true)
                //isHorizontalFadingEdgeEnabled = true
                //val leftSideMargin = if(leftOpen) defaultPadding else 0
                when(timeObject.style){
                    1 -> {
                        setPadding(defaultPadding, 0, defaultPadding, 0)
                        typeface = AppRes.thinFont
                        setTextColor(timeObject.color)
                    }
                    2 -> {
                        setPadding((defaulMargin * 5).toInt(), 0, defaultPadding, 0)
                        typeface = AppRes.thinFont
                        setTextColor(AppRes.primaryText)
                    }
                    3 -> {
                        setPadding(defaultPadding, 0, defaultPadding, 0)
                        typeface = AppRes.regularFont
                        setTextColor(timeObject.fontColor)
                    }
                    else -> {
                        setPadding(iconSize  + defaulMargin.toInt(), 0, defaultPadding, 0)
                        typeface = AppRes.regularFont
                        setTextColor(AppRes.primaryText)
                    }
                }
            }
            TimeObject.Type.TASK -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize - 1)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                typeface = AppRes.regularFont
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
                typeface = AppRes.regularFont
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
            TimeObject.Type.DRAWING -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize + 3)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.empty_note)
                setTypeface(AppRes.textFont, BOLD_ITALIC)
                setLineSpacing(strokeWidth, 1f)
                setPadding(defaultPadding, defaultPadding, defaultPadding, normalTypeSize * 2)
                setTextColor(CalendarSkin.dateColor)
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
                    CalendarSkin.drawEvent(canvas, this)
                    super.onDraw(canvas)
                }
                TimeObject.Type.TASK -> {
                    CalendarSkin.drawTask(canvas, this)
                    super.onDraw(canvas)
                }
                TimeObject.Type.STAMP -> {
                    CalendarSkin.drawStamp(canvas, this)
                }
                TimeObject.Type.NOTE -> {
                    CalendarSkin.drawNote(canvas, this)
                    super.onDraw(canvas)
                }
                TimeObject.Type.MONEY -> {
                    CalendarSkin.drawMoney(canvas, this)
                }
                TimeObject.Type.TERM -> {
                    super.onDraw(canvas)
                    CalendarSkin.drawTerm(canvas, this)
                }
                TimeObject.Type.DRAWING -> {
                    super.onDraw(canvas)
                    CalendarSkin.drawDrawing(canvas, this)
                }
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
            normalTypeSize
        }
        TimeObject.Type.TERM -> {
            textSpaceWidth = paint.measureText(text.toString())
            normalTypeSize
        }
        else -> normalTypeSize
    }

    fun setLayout() {
        var w = mRight - mLeft - defaulMargin
        /*
        if(leftOpen) {
            w += defaultPadding
            translationX = -defaultPadding.toFloat()
        }
        if(rightOpen) w += defaultPadding
        */
        val lp = FrameLayout.LayoutParams(w.toInt(), (mBottom - mTop - defaulMargin).toInt())
        lp.setMargins(0, mTop.toInt(), 0, 0)
        layoutParams = lp
    }

}