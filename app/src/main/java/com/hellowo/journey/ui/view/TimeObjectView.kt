package com.hellowo.journey.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
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
import com.hellowo.journey.manager.CalendarManager
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.model.TimeObject.Type.*
import com.hellowo.journey.model.TimeObject.Style.*

@SuppressLint("ViewConstructor")
class TimeObjectView constructor(context: Context, val timeObject: TimeObject, val cellNum: Int, val length: Int) : TextView(context) {
    companion object {
        var standardTextSize = 9f
        val defaulMargin = dpToPx(1f) // 뷰간 간격
        val strokeWidth = dpToPx(1f) // 선
        val defaultPadding = dpToPx(2)
        val leftPadding = dpToPx(8)
        val rectRadius = dpToPx(1f)
        val stampSize = dpToPx(17)
        val blockTypeSize = dpToPx(15)
        val dotSize = dpToPx(3)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        val defaultTextColor = Color.parseColor("#90000000")
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
    var paintColor = AppTheme.backgroundColor
    var fontColor = AppTheme.primaryText
    //var line = 0

    init {
        //includeFontPadding = false
    }

    fun setLookByType() {
        if(!timeObject.inCalendar) {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize - 2)
            gravity = Gravity.CENTER_VERTICAL
            maxLines = 1
            setSingleLine(true)
            setHorizontallyScrolling(true)
            //isHorizontalFadingEdgeEnabled = true /*성능이슈*/
            typeface = AppTheme.textFont
            setTextColor(AppTheme.primaryText)
            setPadding(defaultPadding, 0, defaultPadding, 0)
            return
        }

        when(TimeObject.Type.values()[timeObject.type]) {
            EVENT, TASK -> {
                typeface = AppTheme.textFont
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                gravity = Gravity.CENTER_VERTICAL
                maxLines = 1
                setSingleLine(true)
                setHorizontallyScrolling(true)
                setPadding((leftPadding + defaulMargin).toInt(), 0, defaultPadding, 0)
                when(TimeObject.Style.values()[timeObject.style]){
                    ROUND_STROKE, RECT_STROKE, HATCHED, TOP_LINE, BOTTOM_LINE -> {
                        paintColor = AppTheme.getColor(timeObject.colorKey)
                        fontColor = AppTheme.getColor(timeObject.colorKey)
                    }
                    ROUND_FILL, RECT_FILL, CANDY -> {
                        paintColor = AppTheme.getColor(timeObject.colorKey)
                        fontColor = AppTheme.getFontColor(timeObject.colorKey)
                    }
                    else -> {
                        paintColor = AppTheme.getColor(timeObject.colorKey)
                        fontColor = AppTheme.getColor(timeObject.colorKey)
                    }
                }
                setTextColor(fontColor)
            }
            NOTE -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.empty_note)
                typeface = AppTheme.textFont
                setLineSpacing(defaulMargin, 1f)
                setPadding((leftPadding + defaulMargin).toInt(), (defaulMargin * 3).toInt(), defaultPadding, 0)
                when(TimeObject.Style.values()[timeObject.style]){
                    RECT_STROKE, HATCHED, TOP_LINE, BOTTOM_LINE -> {
                        paintColor = AppTheme.getColor(timeObject.colorKey)
                        fontColor = AppTheme.getColor(timeObject.colorKey)
                    }
                    RECT_FILL, CANDY -> {
                        paintColor = AppTheme.getColor(timeObject.colorKey)
                        fontColor = AppTheme.getFontColor(timeObject.colorKey)
                    }
                    else -> {
                        paintColor = AppTheme.getColor(timeObject.colorKey)
                        fontColor = AppTheme.primaryText
                    }
                }
                setTextColor(fontColor)

            }
            TERM -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                gravity = Gravity.CENTER_HORIZONTAL
                maxLines = 1
                setSingleLine(true)
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
                        setPadding(defaultPadding * 2, 0, defaultPadding * 2, 0)
                        setTextColor(timeObject.getColor())
                    }
                }
            }
            else -> {
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                typeface = AppTheme.regularFont
                setPadding(defaultPadding, 0, defaultPadding, 0)
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if(timeObject.inCalendar) {
            canvas?.let {
                paint.isAntiAlias = true
                when(TimeObject.Type.values()[timeObject.type]) {
                    EVENT, TASK, NOTE -> {
                        CalendarManager.drawBasicShape(canvas, this)
                        super.onDraw(canvas)
                    }
                    STAMP -> {
                        CalendarManager.drawStamp(canvas, this)
                    }
                    MONEY -> {
                        CalendarManager.drawMoney(canvas, this)
                    }
                    TERM -> {
                        super.onDraw(canvas)
                        CalendarManager.drawTerm(canvas, this)
                    }
                }
            }
        }else {
            super.onDraw(canvas)
        }
    }

    fun getViewHeight(): Int {
        if(timeObject.inCalendar) {
            return when(TimeObject.Type.values()[timeObject.type]) {
                EVENT -> {
                    textSpaceWidth = paint.measureText(text.toString())
                    blockTypeSize
                }
                TASK -> {
                    textSpaceWidth = paint.measureText(text.toString())
                    blockTypeSize
                }
                STAMP -> {
                    val width =  mRight - mLeft - defaulMargin
                    val margin = defaulMargin.toInt()
                    val size = stampSize - defaulMargin
                    val totalStampCnt = childList?.size ?: 0
                    val rows = ((size * totalStampCnt + margin * (totalStampCnt - 1)) / width + 1).toInt()
                    (stampSize * rows)
                }
                NOTE -> {
                    setSingleLine(false)
                    maxLines = 5
                    gravity = Gravity.TOP
                    ellipsize = TextUtils.TruncateAt.END
                    val width =  mRight - mLeft - defaulMargin
                    measure(View.MeasureSpec.makeMeasureSpec(width.toInt(), View.MeasureSpec.EXACTLY), heightMeasureSpec)
                    //l("${timeObject.title} 라인 : "+((paint.measureText(text.toString()) / width).toInt() + 1))
                    measuredHeight + (defaulMargin * 2).toInt()
                }
                TERM -> {
                    textSpaceWidth = paint.measureText(text.toString())
                    blockTypeSize
                }
                else -> blockTypeSize
            }
        }else {
            return blockTypeSize
        }
    }

    fun setLayout() {
        var w = mRight - mLeft - defaulMargin
        var h = mBottom - mTop - defaulMargin
        val m = (defaulMargin / 2).toInt()
        /*
        if(leftOpen) {
            w += defaultPadding
            translationX = -defaultPadding.toFloat()
        }
        if(rightOpen) w += defaultPadding
        */
        val lp = FrameLayout.LayoutParams(w.toInt(), h.toInt())
        lp.setMargins(m, mTop.toInt() + m, 0, 0)
        layoutParams = lp
    }

    fun setNotInCalendarText() {
        childList?.let { list ->
            val s = StringBuilder("+")
            (0 until TimeObject.Type.values().size).forEach { type ->
                val count = list.filter { it.type == type }.size
                if(count > 0) {
                    s.append(" ${context.getString(TimeObject.Type.values()[type].titleId)} $count /")
                }
            }
            if(s.endsWith('/')) {
                s.deleteCharAt(s.length - 1)
            }
            text = s
        }
    }

}