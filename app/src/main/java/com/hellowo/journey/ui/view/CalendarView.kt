package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.hellowo.journey.*
import java.util.*
import android.widget.*
import android.widget.LinearLayout.HORIZONTAL
import com.hellowo.journey.model.CalendarSkin
import com.hellowo.journey.calendar.TimeObjectManager
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.listener.MainDragAndDropListener

class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        const val maxCellNum = 42
        const val dateTextSize = 13f
        const val animDur = 250L
        const val columns = 7
        const val selectedDateScale = 1.5f
        const val outDateAlpha = 0.4f
        val selectedDatePosition = -dpToPx(0f)
        val todayCal: Calendar = Calendar.getInstance()
        val dateSize = dpToPx(20)
        val dateArea = dpToPx(30)
        val weekLyBottomPadding = dpToPx(20)
        val dateMargin = dpToPx(1)
        val weekSideMargin = dpToPx(16)
        val autoPagingThreshold = dpToPx(30)
        val autoScrollThreshold = dpToPx(70)
        val autoScrollOffset = dpToPx(5)
    }

    private val scrollView = SwipeScrollView(context)
    private val rootLy = FrameLayout(context)
    val calendarLy = LinearLayout(context)
    val weekLys = Array(6) { _ -> FrameLayout(context)}
    val dateLys = Array(6) { _ -> LinearLayout(context)}
    val dateCells = Array(maxCellNum) { _ -> FrameLayout(context)}
    val dateHeaders = Array(maxCellNum) { _ ->
        DateHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.view_selected_bar, null, false))}
    val weekLySideView = LayoutInflater.from(context).inflate(R.layout.view_weekly_side, null, false)

    inner class DateHeaderViewHolder(val container: View) {
        val dateText: TextView = container.findViewById(R.id.dateText)
        val bar: View = container.findViewById(R.id.bar)
        val dowText: TextView = container.findViewById(R.id.dowText)
        val flagImg: ImageView = container.findViewById(R.id.flagImg)
        init {
            flagImg.scaleY = 0f
        }
    }

    private var lastSelectAnimSet: AnimatorSet? = null
    private var lastUnSelectAnimSet: AnimatorSet? = null

    private val tempCal: Calendar = Calendar.getInstance()
    private val monthCal: Calendar = Calendar.getInstance()
    private val dow = AppRes.dowString

    val selectedCal = Calendar.getInstance()
    var postSelectedNum = -1
    var selectedCellNum = -1
    var todayCellNum = -1
    val cellTimeMills = LongArray(maxCellNum) { _ -> Long.MIN_VALUE}
    var calendarStartTime = Long.MAX_VALUE
    var calendarEndTime = Long.MAX_VALUE
    var onDrawed: ((Calendar) -> Unit)? = null
    var onSelected: ((Long, Int, Boolean) -> Unit)? = null
    var startCellNum = 0
    var endCellNum = 0
    var minCalendarHeight = 0
    var minWidth = 0f
    var minHeight = 0f
    var rows = 0
    var todayStatus = 0

    init {
        CalendarSkin.init(this)
        createViews()
        setLayout()
        callAfterViewDrawed(this, Runnable { drawCalendar(System.currentTimeMillis(), true) })
    }

    private fun createViews() {
        addView(scrollView)
        scrollView.addView(rootLy)
        rootLy.addView(calendarLy)
    }

    private fun setLayout() {
        setBackgroundColor(CalendarSkin.backgroundColor)
        scrollView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        rootLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        rootLy.setPadding(0, 0, 0 , 0)
        calendarLy.orientation = LinearLayout.VERTICAL
        calendarLy.clipChildren = false

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.clipChildren = false

            val dateLy = dateLys[i]
            dateLy.clipChildren = false
            dateLy.orientation = HORIZONTAL
            dateLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                leftMargin = weekSideMargin
                rightMargin = weekSideMargin
            }
            weekLy.addView(dateLy)

            for (j in 0..6){
                val cellNum = i*7 + j
                val dateCell = dateCells[cellNum]
                //dateCell.setBackgroundResource(AppRes.selectableItemBackground)
                dateCell.setOnClickListener { onDateClick(cellNum) }
                dateCell.setOnLongClickListener {
                    MainDragAndDropListener.start(it, DragMode.INSERT)
                    return@setOnLongClickListener true
                }
                dateCell.layoutParams = LinearLayout.LayoutParams(0, MATCH_PARENT, 1f)
                dateCell.addView(dateHeaders[cellNum].container)
                dateLy.addView(dateCell)
            }
            calendarLy.addView(weekLy)
        }
    }

    private fun drawCalendar(time: Long, isInit: Boolean) {
        l("==========START drawCalendar=========")
        val t = System.currentTimeMillis()
        todayCal.timeInMillis = System.currentTimeMillis()
        tempCal.timeInMillis = time
        tempCal.set(Calendar.DATE, 1)
        monthCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DATE))
        setCalendarTime0(tempCal)

        todayStatus = (monthCal.get(Calendar.YEAR)*10 + monthCal.get(Calendar.MONTH)) - (todayCal.get(Calendar.YEAR)*10 + todayCal.get(Calendar.MONTH))

        startCellNum = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        endCellNum = startCellNum + tempCal.getActualMaximum(Calendar.DATE) - 1
        todayCellNum = -1
        postSelectedNum = -1
        rows = (endCellNum + 1) / 7 + if ((endCellNum + 1) % 7 > 0) 1 else 0
        minCalendarHeight = height
        minWidth = (width.toFloat() - weekSideMargin * 2) / columns
        minHeight = minCalendarHeight.toFloat() / rows

        calendarLy.layoutParams.height = minCalendarHeight
        calendarLy.requestLayout()
        tempCal.add(Calendar.DATE, -startCellNum)

        if(isInit) {
            weekLySideView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, dpToPx(100)).apply {
                //gravity = Gravity.BOTTOM
            }
        }

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.layoutParams.height = minHeight.toInt()
            if(i < rows) {
                weekLy.visibility = View.VISIBLE
                for (j in 0..6){
                    val cellNum = i*7 + j
                    cellTimeMills[cellNum] = tempCal.timeInMillis

                    if(isSameDay(tempCal, selectedCal)) {
                        postSelectedNum = cellNum
                        TimeObjectManager.postSelectDate(cellNum)
                    }
                    if(isSameDay(tempCal, todayCal)) {
                        todayCellNum = cellNum
                        //dateLy.setBackgroundResource(R.drawable.grey_rect_fill_radius_2)
                    }

                    val dateText = dateHeaders[cellNum].dateText
                    val color = getDateTextColor(cellNum)
                    val alpha = if(cellNum in startCellNum..endCellNum) 1f else outDateAlpha
                    dateText.text = tempCal.get(Calendar.DATE).toString()
                    dateText.alpha = alpha
                    dateText.setTextColor(color)
                    dateHeaders[cellNum].bar.setBackgroundColor(color)
                    dateHeaders[cellNum].bar.alpha = alpha

                    if(cellNum == 0) {
                        calendarStartTime = tempCal.timeInMillis
                    }else if(cellNum == rows * columns - 1) {
                        setCalendarTime23(tempCal)
                        calendarEndTime = tempCal.timeInMillis
                    }

                    tempCal.add(Calendar.DATE, 1)
                }
            }else {
                weekLy.visibility = View.GONE
            }
        }

        if(todayStatus == 0) {
            todayStatus = getDiffToday(selectedCal)
        }

        if(postSelectedNum == -1) {
        }

        TimeObjectManager.setTimeObjectCalendarAdapter(this)
        onDrawed?.invoke(monthCal)

        l("${monthCal.get(Calendar.YEAR)}년 ${(monthCal.get(Calendar.MONTH) + 1)}월")
        l("캘린더 높이 : $minCalendarHeight")
        l("행 : $rows")
        l("걸린시간 : ${(System.currentTimeMillis() - t) / 1000f} 초")
        l("==========END drawCalendar=========")
    }

    fun getDateTextColor(cellNum: Int) : Int {
        return if(cellNum == todayCellNum) {
            if(cellNum % columns == 0) {
                CalendarSkin.todayDateColor
            }else {
                CalendarSkin.todayDateColor
            }
        }else {
            if(cellNum % columns == 0) {
                CalendarSkin.holiDateColor
            }else {
                CalendarSkin.dateColor
            }
        }
    }

    private fun onDateClick(cellNum: Int) {
        tempCal.timeInMillis = cellTimeMills[cellNum]
        selectDate(cellNum,true, isSameDay(tempCal, selectedCal))
    }

    private fun unselectDate(cellNum: Int, anim: Boolean) {
        l("선택 해제 : " + android.text.format.DateFormat.getDateFormat(context).format(cellTimeMills[cellNum]))

        val dateText = dateHeaders[cellNum].dateText
        val flagImg = dateHeaders[cellNum].flagImg
        val color = getDateTextColor(cellNum)
        val alpha = if(cellNum in startCellNum..endCellNum) 1f else outDateAlpha

        dateText.typeface = CalendarSkin.dateFont
        dateText.alpha = alpha
        dateHeaders[cellNum].bar.setBackgroundColor(color)
        dateHeaders[cellNum].bar.alpha = alpha
        dateHeaders[cellNum].bar.scaleY = 1f

        selectedCellNum = -1
        //offViewEffect(cellNum)

        if(anim) {
            lastUnSelectAnimSet?.cancel()
            lastUnSelectAnimSet = AnimatorSet()
            lastUnSelectAnimSet?.let {
                it.addListener(object : Animator.AnimatorListener{
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationEnd(p0: Animator?) {}
                    override fun onAnimationCancel(p0: Animator?) {
                        dateText.scaleX = 1f
                        dateText.scaleY = 1f
                        dateText.translationY = 0f
                        flagImg.scaleY = 0f
                    }
                    override fun onAnimationStart(p0: Animator?) {}
                })
                it.playTogether(
                        ObjectAnimator.ofFloat(dateText, "scaleX", selectedDateScale, 1f),
                        ObjectAnimator.ofFloat(dateText, "scaleY", selectedDateScale, 1f),
                        ObjectAnimator.ofFloat(dateText, "translationY", selectedDatePosition, 0f),
                        ObjectAnimator.ofFloat(flagImg, "scaleY", 1f, 0f))
                it.interpolator = FastOutSlowInInterpolator()
                it.duration = animDur
                it.start()
            }
        }else {
            dateText.scaleX = 1f
            dateText.scaleY = 1f
            dateText.translationY = 0f
            flagImg.scaleY = 0f
        }
    }

    fun selectDate(cellNum: Int, anim: Boolean, showDayView: Boolean) {
        if(!showDayView) {
            l("선택한 날짜 : " + android.text.format.DateFormat.getDateFormat(context).format(cellTimeMills[cellNum]))
            selectedCal.timeInMillis = cellTimeMills[cellNum]
            if(cellNum / columns != selectedCellNum / columns) {
                weekLySideView.parent?.let {
                    (it as FrameLayout).removeView(weekLySideView)
                    weekLySideView.translationX = -weekSideMargin.toFloat()
                }
                weekLys[cellNum / columns].addView(weekLySideView, 0)
                weekLySideView.findViewById<TextView>(R.id.weekNumText).text =
                        String.format(context.getString(R.string.weekNum), selectedCal.get(Calendar.WEEK_OF_YEAR).toString())
            }

            if(selectedCellNum >= 0) { unselectDate(selectedCellNum, anim) }

            selectedCellNum = cellNum
            postSelectedNum = cellNum

            val dateText = dateHeaders[cellNum].dateText
            val flagImg = dateHeaders[cellNum].flagImg
            val color = getDateTextColor(cellNum)

            dateText.typeface = CalendarSkin.selectFont
            dateText.alpha = 1f
            flagImg.setColorFilter(color)
            if(cellNum == todayCellNum) {
                flagImg.setImageResource(R.drawable.flag_today)
            }else {
                flagImg.setImageResource(R.drawable.flag_to_bottom)
            }
            dateHeaders[cellNum].bar.setBackgroundColor(color)
            dateHeaders[cellNum].bar.scaleY = 2f
            dateHeaders[cellNum].dowText.text = dow[cellNum % columns]

            if(anim) {
                lastSelectAnimSet?.cancel()
                lastSelectAnimSet = AnimatorSet()
                lastSelectAnimSet?.let {
                    it.addListener(object : Animator.AnimatorListener{
                        override fun onAnimationRepeat(p0: Animator?) {}
                        override fun onAnimationEnd(p0: Animator?) {}
                        override fun onAnimationCancel(p0: Animator?) {
                            dateText.scaleX = 1f
                            dateText.scaleY = 1f
                            dateText.translationY = 0f
                            flagImg.scaleY = 0f
                        }
                        override fun onAnimationStart(p0: Animator?) {}
                    })
                    it.playTogether(
                            ObjectAnimator.ofFloat(dateText, "scaleX", 1f, selectedDateScale),
                            ObjectAnimator.ofFloat(dateText, "scaleY", 1f, selectedDateScale),
                            ObjectAnimator.ofFloat(dateText, "translationY", 0f, selectedDatePosition),
                            ObjectAnimator.ofFloat(flagImg, "scaleY", 0f, 1f),
                            ObjectAnimator.ofFloat(weekLySideView, "translationX", weekLySideView.translationX, 0f))
                    it.interpolator = FastOutSlowInInterpolator()
                    it.duration = animDur
                    it.start()
                }
            }else {
                dateText.scaleX = 1.5f
                dateText.scaleY = 1.5f
                dateText.translationY = selectedDatePosition
                flagImg.scaleY = 1f
            }

            //scrollView.smoothScrollTo(0, weekLys[cellNum / columns].top)
            //onViewEffect(cellNum)
        }else {

        }
        todayStatus = getDiffToday(selectedCal)
        onSelected?.invoke(cellTimeMills[selectedCellNum], selectedCellNum, showDayView)
    }

    private fun onViewEffect(cellNum: Int) {
        TimeObjectManager.timeObjectCalendarAdapter?.getViews(cellNum)?.let {
            it.forEach { view ->
                if(view.timeObject.type == TimeObject.Type.EVENT.ordinal || view.timeObject.type == TimeObject.Type.TASK.ordinal) {
                    view.ellipsize = TextUtils.TruncateAt.MARQUEE
                    view.marqueeRepeatLimit = -1
                    view.isFocusable = true
                    view.postDelayed({
                        view.isSelected = true
                    }, 1000)
                }
            }
        }
    }

    private fun offViewEffect(cellNum: Int) {
        TimeObjectManager.timeObjectCalendarAdapter?.getViews(cellNum)?.let {
            it.forEach { view ->
                if(view.timeObject.type == TimeObject.Type.EVENT.ordinal || view.timeObject.type == TimeObject.Type.TASK.ordinal) {
                    view.ellipsize = null
                    view.isSelected = false
                }
            }
        }
    }

    fun setOnSwiped(onSwiped: ((Int) -> Unit)) {
        scrollView.onSwipeStateChanged = onSwiped
    }

    fun setOnTop(onTop: ((Boolean) -> Unit)) {
        scrollView.onTop = onTop
    }

    fun moveDate(offset: Int) {
        selectedCal.add(Calendar.DATE, offset)
        drawCalendar(selectedCal.timeInMillis, false)
    }

    fun moveDate(time: Long) {
        selectedCal.timeInMillis = time
        drawCalendar(time, false)
    }

    fun moveMonth(offset: Int) {
        monthCal.add(Calendar.MONTH, offset)
        if(selectedCellNum >= 0) {
            lastSelectAnimSet?.cancel()
            unselectDate(selectedCellNum, false)
        }
        scrollView.scrollTo(0, 0)
        drawCalendar(monthCal.timeInMillis, false)
        startPagingEffectAnimation(offset, scrollView, null)
    }

    fun getSelectedView(): View = dateCells[selectedCellNum]

    private fun highlightCells(start: Int, end: Int) {
        val s = if(start < end) start else end
        val e = if(start < end) end else start
        dateCells.forEachIndexed { index, view ->
           if(index in s..e) {
               view.foreground = AppRes.hightlightCover
           }else {
               view.foreground = AppRes.blankDrawable
           }
        }
    }

    private fun clearHighlight() {
        dateCells.forEachIndexed { index, view ->
            view.foreground = AppRes.blankDrawable
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////드래그 처리 부분/////////////////////////////////////////////////

    var startDragCell = -1
    var startDragTime = Long.MIN_VALUE
    var currentDragCell = -1
    var endDragTime = Long.MIN_VALUE
    var autoPagingFlag = 0
    var autoScrollFlag = 0

    private val autoScrollHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            if (autoScrollFlag != 0) {
                if (autoScrollFlag == -1) {
                    scrollView.scrollBy(0, -autoScrollOffset)
                } else if (autoScrollFlag == 1) {
                    scrollView.scrollBy(0, autoScrollOffset)
                }
                this.sendEmptyMessageDelayed(0, 1)
            }
        }
    }

    private val autoPaginglHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            if (autoPagingFlag != 0) {
                if (autoPagingFlag == -1) {
                    moveMonth(-1)
                } else if (autoPagingFlag == 1) {
                    moveMonth(1)
                }
                this.sendEmptyMessageDelayed(0, 1000)
            }
        }
    }

    fun onDrag(event: DragEvent) {
        var cellX = ((event.x - weekSideMargin) / minWidth).toInt()
        if(cellX < 0) cellX = 0
        val yPos = event.y - top - AppRes.statusBarHeight
        val yCalPos = yPos + scrollView.scrollY
        var cellY = -1

        weekLys.forEachIndexed { index, view ->
            if(index < rows && yCalPos > view.top && yCalPos < view.bottom) {
                cellY = index
                return@forEachIndexed
            }
        }

        when {
            yPos < autoScrollThreshold -> {
                if(autoScrollFlag != -1) {
                    autoScrollFlag = -1
                    autoScrollHandler.sendEmptyMessage(0)
                }
            }
            yPos > height - autoScrollThreshold -> {
                if(autoScrollFlag != 1) {
                    autoScrollFlag = 1
                    autoScrollHandler.sendEmptyMessage(0)
                }
            }
            else -> {
                if(autoScrollFlag != 0) {
                    autoScrollFlag = 0
                    autoScrollHandler.removeMessages(0)
                }
            }
        }

        when {
            event.x < autoPagingThreshold -> {
                if(autoPagingFlag != -1) {
                    autoPagingFlag = -1
                    autoPaginglHandler.sendEmptyMessageDelayed(0, 1000)
                }
            }
            event.x > width - autoPagingThreshold -> {
                if(autoPagingFlag != 1) {
                    autoPagingFlag = 1
                    autoPaginglHandler.sendEmptyMessageDelayed(0, 1000)
                }
            }
            else -> {
                if(autoPagingFlag != 0){
                    autoPagingFlag = 0
                    autoPaginglHandler.removeMessages(0)
                }
            }
        }

        if(cellY >= 0) {
            currentDragCell = cellY * columns + cellX
            endDragTime = cellTimeMills[currentDragCell]

            when(event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    startDragCell = currentDragCell
                    startDragTime = cellTimeMills[startDragCell]
                    highlightCells(startDragCell, currentDragCell)
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    highlightCells(startDragCell, currentDragCell)
                }
                DragEvent.ACTION_DROP -> {
                    if(MainDragAndDropListener.dragMode == DragMode.INSERT) {
                        MainActivity.instance?.viewModel?.makeNewTimeObject(startDragTime, endDragTime)
                    }
                }
            }
        }
    }

    fun endDrag() {
        clearHighlight()
        startDragCell = -1
        startDragTime = Long.MIN_VALUE
        currentDragCell = -1
        endDragTime = Long.MIN_VALUE
        autoScrollFlag = 0
        autoScrollHandler.removeMessages(0)
        autoPagingFlag = 0
        autoPaginglHandler.removeMessages(0)
    }
    /////////////////////////////////드래그 처리 부분/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun isTop() = scrollView.isTop
}