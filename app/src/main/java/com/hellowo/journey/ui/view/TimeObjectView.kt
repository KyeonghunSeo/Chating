package com.hellowo.journey.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface.BOLD_ITALIC
import android.graphics.Typeface.ITALIC
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.dpToPx
import com.hellowo.journey.manager.CalendarSkin
import com.hellowo.journey.model.TimeObject
import java.lang.StringBuilder

@SuppressLint("ViewConstructor")
class TimeObjectView constructor(context: Context, val timeObject: TimeObject, val cellNum: Int, val length: Int) : TextView(context) {
    companion object {
        var standardTextSize = 9f
        val defaulMargin = dpToPx(1.5f) // 뷰간 간격
        val defaultPadding = dpToPx(4)
        val strokeWidth = dpToPx(0.5f) // 선 간격
        val rectRadius = dpToPx(0f)
        val stampSize = dpToPx(24)
        val normalTypeSize = dpToPx(20)
        val smallTypeSize = dpToPx(15)
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
        //setBackgroundColor(AppTheme.almostWhite)
    }

    fun setLookByType() {
        if(!timeObject.inCalendar) {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize)
            gravity = Gravity.CENTER_VERTICAL
            maxLines = 1
            setSingleLine(true)
            setHorizontallyScrolling(true)
            isHorizontalFadingEdgeEnabled = true
            typeface = AppTheme.textFont
            setTextColor(AppTheme.primaryText)
            setPadding(defaultPadding, 0, defaultPadding, 0)
            return
        }

        when(TimeObject.Type.values()[timeObject.type]) {
            TimeObject.Type.EVENT -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                gravity = Gravity.CENTER_VERTICAL
                maxLines = 1
                //setSingleLine(true)
                //setHorizontallyScrolling(true)
                //isHorizontalFadingEdgeEnabled = true
                when(timeObject.style){
                    1 -> {
                        typeface = AppTheme.textFont
                        if(length > 1) {
                            gravity = Gravity.CENTER
                            setPadding(defaultPadding, 0, defaultPadding, 0)
                        }else {
                            setPadding(iconSize + defaultPadding, 0, defaultPadding, 0)
                        }
                        setTextColor(AppTheme.primaryText)
                    }
                    2 -> {
                        setPadding((defaulMargin * 5).toInt(), 0, defaultPadding, 0)
                        typeface = AppTheme.thinFont
                        setTextColor(AppTheme.primaryText)
                    }
                    3 -> {
                        setPadding(defaultPadding, 0, defaultPadding, 0)
                        typeface = AppTheme.regularFont
                        setTextColor(timeObject.fontColor)
                    }
                    4 -> {
                        setPadding(defaultPadding, 0, defaultPadding, 0)
                        typeface = AppTheme.textFont
                        setTextColor(CalendarSkin.backgroundColor)
                    }
                    6 -> {
                        setPadding(iconSize  + defaulMargin.toInt(), 0, defaultPadding, 0)
                        typeface = AppTheme.textFont
                        setTextColor(AppTheme.primaryText)
                    }
                    else -> {
                        setPadding(defaultPadding, 0, defaultPadding, 0)
                        typeface = AppTheme.textFont
                        setTextColor(timeObject.getColor())
                    }
                }
            }
            TimeObject.Type.TASK -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                typeface = AppTheme.textFont
                gravity = Gravity.CENTER_VERTICAL
                maxLines = 1
                //setSingleLine(true)
                //setHorizontallyScrolling(true)
                when(timeObject.style){
                    1 -> {
                        setPadding(iconSize + defaultPadding, 0, defaultPadding, 0)
                        setTextColor(AppTheme.primaryText)
                    }
                    else -> {
                        setPadding(iconSize + defaultPadding, 0, defaultPadding, 0)
                        setTextColor(AppTheme.primaryText)
                    }
                }
            }
            TimeObject.Type.NOTE -> {
                when(timeObject.style){
                    1 -> {
                        setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize - 1)
                        text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.empty_note)
                        typeface = AppTheme.textFont
                        setLineSpacing(strokeWidth * 2, 1f)
                        setPadding(defaulMargin.toInt(), (defaulMargin).toInt() * 4, defaultPadding, 0)
                        setTextColor(CalendarSkin.dateColor)
                    }
                    else -> {
                        setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize - 1)
                        text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.empty_note)
                        typeface = AppTheme.textFont
                        setLineSpacing(strokeWidth * 2, 1f)
                        setPadding(defaulMargin.toInt(), defaulMargin.toInt(), defaultPadding, 0)
                        setTextColor(CalendarSkin.dateColor)
                    }
                }
            }
            TimeObject.Type.TERM -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                gravity = Gravity.CENTER_HORIZONTAL
                maxLines = 1
                //setSingleLine(true)
                //setHorizontallyScrolling(true)
                when(timeObject.style){
                    1 -> {
                        typeface = AppTheme.textFont
                        gravity = Gravity.CENTER_HORIZONTAL
                        setPadding(defaultPadding, 0, defaultPadding, 0)
                        setTextColor(timeObject.getColor())
                    }
                    2 -> {
                        setTypeface(AppTheme.textFont, ITALIC)
                        gravity = Gravity.CENTER
                        setPadding(defaultPadding, 0, defaultPadding, 0)
                        setTextColor(timeObject.getColor())
                    }
                    else -> {
                        typeface = AppTheme.textFont
                        gravity = Gravity.CENTER
                        setPadding(defaultPadding, 0, defaultPadding, 0)
                        setTextColor(timeObject.getColor())
                    }
                }
            }
            TimeObject.Type.DRAWING -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize + 3)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.empty_note)
                setTypeface(AppTheme.textFont, BOLD_ITALIC)
                setLineSpacing(strokeWidth, 1f)
                setPadding(defaultPadding, defaultPadding, defaultPadding, normalTypeSize * 2)
                setTextColor(CalendarSkin.dateColor)
            }
            else -> {
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                typeface = AppTheme.regularFont
                setPadding(defaultPadding, 0, defaultPadding, 0)
            }
        }
    }

    @SuppressLint("DrawAllocation", "SetTextI18n")
    override fun onDraw(canvas: Canvas?) {
        if(timeObject.inCalendar) {
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
                }
            }
        }else {
            text = "··· +${childList?.size}"
            super.onDraw(canvas)
        }
    }

    fun getViewHeight(): Int {
        if(timeObject.inCalendar) {
            return when(TimeObject.Type.values()[timeObject.type]) {
                TimeObject.Type.EVENT -> {
                    textSpaceWidth = paint.measureText(text.toString())
                    smallTypeSize
                }
                TimeObject.Type.TASK -> smallTypeSize
                TimeObject.Type.NOTE -> {
                    setSingleLine(false)
                    maxLines = 7
                    gravity = Gravity.TOP
                    ellipsize = TextUtils.TruncateAt.END
                    val width =  mRight - mLeft - defaulMargin
                    measure(View.MeasureSpec.makeMeasureSpec(width.toInt(), View.MeasureSpec.EXACTLY), heightMeasureSpec)
                    //l("${timeObject.title} 라인 : "+((paint.measureText(text.toString()) / width).toInt() + 1))
                    (measuredHeight + /*폰트 자체 패딩때문에 조금 여유를 줘야함*/defaulMargin).toInt()
                }
                TimeObject.Type.STAMP -> stampSize
                TimeObject.Type.TERM -> {
                    textSpaceWidth = paint.measureText(text.toString())
                    smallTypeSize
                }
                else -> normalTypeSize
            }
        }else {
            return smallTypeSize
        }
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