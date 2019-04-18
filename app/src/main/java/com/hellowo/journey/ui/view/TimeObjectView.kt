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
import com.hellowo.journey.AppStatus
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.dpToPx
import com.hellowo.journey.manager.CalendarManager
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.model.TimeObject.Style.*
import com.hellowo.journey.model.TimeObject.Formula.*

@SuppressLint("ViewConstructor")
class TimeObjectView constructor(context: Context, val timeObject: TimeObject, val cellNum: Int, val length: Int) : TextView(context) {
    companion object {
        var standardTextSize = 9f
        val baseSize = dpToPx(0.5f)
        val defaulMargin = dpToPx(1.5f) // 뷰간 간격
        val strokeWidth = dpToPx(1f) // 선
        val sidePadding = dpToPx(3.5f).toInt()
        val topPadding = dpToPx(1.8f)
        val bottomPadding = dpToPx(3.5f)
        val leftPadding = dpToPx(9)
        val rectRadius = dpToPx(1f)
        val stampSize = dpToPx(16)
        val blockTypeSize = dpToPx(16.5f).toInt()
        val dotSize = dpToPx(4)
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
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize + AppStatus.calTextSize)
            gravity = Gravity.CENTER_VERTICAL
            maxLines = 1
            setSingleLine(true)
            setHorizontallyScrolling(true)
            //isHorizontalFadingEdgeEnabled = true /*성능이슈*/
            typeface = AppTheme.regularFont
            setTextColor(AppTheme.primaryText)
            setPadding((leftPadding + defaulMargin).toInt(), 0, sidePadding, 0)
            return
        }

        typeface = AppTheme.regularFont
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize + AppStatus.calTextSize)
        setPadding(sidePadding,
                (topPadding + (AppStatus.calTextSize * -1.7f)).toInt() /*글씨 크기에 따른 탑 패딩 조정*/,
                sidePadding, 0)

        when(timeObject.getFormula()) {
            TOP_LINEAR, BOTTOM_LINEAR -> {
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title?.replace(System.getProperty("line.separator"), " ")
                else context.getString(R.string.empty_note)
                //setLineSpacing(defaulMargin, 1f)
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
                        fontColor = AppTheme.getColor(timeObject.colorKey)
                    }
                }
                setTextColor(fontColor)
            }
            TOP_FLOW -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize + AppStatus.calTextSize)
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                gravity = Gravity.CENTER_HORIZONTAL
                maxLines = 1
                setSingleLine(true)
                //setHorizontallyScrolling(true)
                when(timeObject.style){
                    1 -> {
                        typeface = AppTheme.regularFont
                        gravity = Gravity.CENTER_HORIZONTAL
                        setPadding(sidePadding, 0, sidePadding, 0)
                        setTextColor(timeObject.getColor())
                    }
                    2 -> {
                        setTypeface(AppTheme.regularFont, ITALIC)
                        gravity = Gravity.CENTER
                        setPadding(sidePadding, 0, sidePadding, 0)
                        setTextColor(timeObject.getColor())
                    }
                    else -> {
                        typeface = AppTheme.regularFont
                        gravity = Gravity.CENTER
                        setPadding(sidePadding * 4, 0, sidePadding * 4, 0)
                        setTextColor(timeObject.getColor())
                    }
                }
            }
            else -> {
                text = if(!timeObject.title.isNullOrBlank()) timeObject.title else context.getString(R.string.untitle)
                maxLines = 1
                setSingleLine(true)
                setHorizontallyScrolling(true)
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
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if(timeObject.inCalendar) {
            canvas?.let {
                paint.isAntiAlias = true
                CalendarManager.drawBasicShape(canvas, this)
                super.onDraw(canvas)
            }
        }else {
            canvas?.let {
                CalendarManager.drawNotInCalendar(this, paint, canvas)
            }
            super.onDraw(canvas)
        }
    }

    fun getViewHeight(): Int {
        if(timeObject.inCalendar) {
            return when(timeObject.getFormula()) {
                TOP_FLOW -> {
                    val width =  mRight - mLeft - defaulMargin
                    val margin = defaulMargin.toInt()
                    val size = stampSize - defaulMargin
                    val totalStampCnt = childList?.size ?: 0
                    val rows = ((size * totalStampCnt + margin * (totalStampCnt - 1)) / width + 1).toInt()
                    (stampSize * rows)
                }
                TOP_LINEAR, BOTTOM_LINEAR -> {
                    setSingleLine(false)
                    maxLines = 5
                    ellipsize = TextUtils.TruncateAt.END
                    val width =  mRight - mLeft - defaulMargin
                    measure(View.MeasureSpec.makeMeasureSpec(width.toInt(), View.MeasureSpec.EXACTLY), heightMeasureSpec)
                    //l("${timeObject.title} 라인 : "+((paint.measureText(text.toString()) / width).toInt() + 1))
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
        }else {
            return blockTypeSize
        }
    }

    fun setLayout() {
        layoutParams = FrameLayout.LayoutParams((mRight - mLeft - defaulMargin).toInt(),
                (mBottom - mTop - defaulMargin).toInt()).apply { topMargin = mTop.toInt() }
    }

}