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
import com.hellowo.journey.model.Record
import com.hellowo.journey.model.Record.Style.*
import com.hellowo.journey.model.Record.Formula.*

@SuppressLint("ViewConstructor")
class RecordView constructor(context: Context, val record: Record, val cellNum: Int, val length: Int) : TextView(context) {
    companion object {
        var standardTextSize = 9f
        val baseSize = dpToPx(0.5f)
        val defaulMargin = dpToPx(1.5f) // 뷰간 간격
        val strokeWidth = dpToPx(1f) // 선
        val sidePadding = dpToPx(3.0f).toInt()
        val smallTextPadding = dpToPx(2.5f)
        val normalTextPadding = dpToPx(1.5f)
        val bigTextPadding = dpToPx(1f)
        val bottomPadding = dpToPx(3.5f)
        val rectRadius = dpToPx(1f)
        val stampSize = dpToPx(16)
        val blockTypeSize = dpToPx(16.5f).toInt()
        val checkboxSize = dpToPx(10)
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
    var childList: ArrayList<Record>? = null
    var paintColor = AppTheme.backgroundColor
    var fontColor = AppTheme.primaryText
    //var line = 0

    init {
        //includeFontPadding = false
    }

    fun setLookByType() {
        /*
        if(!record.isInCalendar()) {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize + AppStatus.calTextSize)
            gravity = Gravity.CENTER_VERTICAL
            maxLines = 1
            setSingleLine(true)
            setHorizontallyScrolling(true)
            //isHorizontalFadingEdgeEnabled = true /*성능이슈*/
            typeface = AppTheme.thinFont
            setTextColor(AppTheme.primaryText)
            setPadding((defaulMargin).toInt(), 0, sidePadding, 0)
            return
        }
*/
        typeface = AppTheme.regularFont
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize + AppStatus.calTextSize)
        val leftPadding = if(record.isSetCheckBox()) {
            (sidePadding + checkboxSize + defaulMargin).toInt()
        }else {
            sidePadding
        }
        val textPadding =  when(AppStatus.calTextSize) { /*글씨 크기에 따른 패딩 조정*/
            -1 -> smallTextPadding
            1 -> bigTextPadding
            else -> normalTextPadding
        }.toInt()
        setPadding(leftPadding, textPadding, sidePadding, 0)

        when(record.getFormula()) {
            MULTI_LINE_TOP, SINGLE_LINE_BOTTOM_STACK -> {
                text = if(!record.title.isNullOrBlank()) record.title?.replace(System.getProperty("line.separator"), " ")
                else context.getString(R.string.empty_note)
                //setLineSpacing(defaulMargin, 1f)
                when(Record.Style.values()[record.style]){
                    RECT_STROKE, HATCHED, TOP_LINE, BOTTOM_LINE -> {
                        paintColor = AppTheme.getColor(record.colorKey)
                        fontColor = AppTheme.getColor(record.colorKey)
                    }
                    RECT_FILL, CANDY -> {
                        paintColor = AppTheme.getColor(record.colorKey)
                        fontColor = AppTheme.getFontColor(record.colorKey)
                    }
                    else -> {
                        paintColor = AppTheme.getColor(record.colorKey)
                        fontColor = AppTheme.getColor(record.colorKey)
                    }
                }
                setTextColor(fontColor)
            }
            TOP_FLOW -> {
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, standardTextSize + AppStatus.calTextSize)
                text = if(!record.title.isNullOrBlank()) record.title else context.getString(R.string.untitle)
                gravity = Gravity.CENTER_HORIZONTAL
                maxLines = 1
                setSingleLine(true)
                //setHorizontallyScrolling(true)
                when(record.style){
                    1 -> {
                        typeface = AppTheme.regularFont
                        gravity = Gravity.CENTER_HORIZONTAL
                        setPadding(sidePadding, 0, sidePadding, 0)
                        setTextColor(record.getColor())
                    }
                    2 -> {
                        setTypeface(AppTheme.regularFont, ITALIC)
                        gravity = Gravity.CENTER
                        setPadding(sidePadding, 0, sidePadding, 0)
                        setTextColor(record.getColor())
                    }
                    else -> {
                        typeface = AppTheme.regularFont
                        gravity = Gravity.CENTER
                        setPadding(sidePadding * 4, 0, sidePadding * 4, 0)
                        setTextColor(record.getColor())
                    }
                }
            }
            else -> {
                text = if(!record.title.isNullOrBlank()) record.title else context.getString(R.string.untitle)
                maxLines = 1
                setSingleLine(true)
                setHorizontallyScrolling(true)
                when(Record.Style.values()[record.style]){
                    ROUND_STROKE, RECT_STROKE, HATCHED, TOP_LINE, BOTTOM_LINE -> {
                        paintColor = AppTheme.getColor(record.colorKey)
                        fontColor = AppTheme.getColor(record.colorKey)
                    }
                    ROUND_FILL, RECT_FILL, CANDY -> {
                        paintColor = AppTheme.getColor(record.colorKey)
                        fontColor = AppTheme.getFontColor(record.colorKey)
                    }
                    else -> {
                        paintColor = AppTheme.getColor(record.colorKey)
                        fontColor = AppTheme.getColor(record.colorKey)
                    }
                }
                setTextColor(fontColor)
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if(true) {
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
        if(true) {
            return when(record.getFormula()) {
                TOP_FLOW -> {
                    val width =  mRight - mLeft - defaulMargin
                    val margin = defaulMargin.toInt()
                    val size = stampSize - defaulMargin
                    val totalStampCnt = childList?.size ?: 0
                    val rows = ((size * totalStampCnt + margin * (totalStampCnt - 1)) / width + 1).toInt()
                    (stampSize * rows)
                }
                MULTI_LINE_TOP -> {
                    setSingleLine(false)
                    maxLines = 5
                    ellipsize = TextUtils.TruncateAt.END
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
        }else {
            return blockTypeSize
        }
    }

    fun setLayout() {
        layoutParams = FrameLayout.LayoutParams((mRight - mLeft - defaulMargin).toInt(),
                (mBottom - mTop - defaulMargin).toInt()).apply { topMargin = mTop.toInt() }
    }

}