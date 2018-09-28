package com.hellowo.chating.calendar.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.hellowo.chating.*
import com.hellowo.chating.ui.view.SwipeScrollView
import java.util.*
import android.widget.*
import android.widget.LinearLayout.HORIZONTAL
import com.hellowo.chating.calendar.model.CalendarSkin
import com.hellowo.chating.calendar.TimeObjectManager
import com.hellowo.chating.calendar.model.TimeObject
import com.hellowo.chating.ui.listener.MainDragAndDropListener

class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        const val maxCellNum = 42
        const val dateTextSize = 12f
        const val animDur = 250L
        const val columns = 7
        const val selectedDateScale = 1.7f
        val todayCal: Calendar = Calendar.getInstance()
        val dateSize = dpToPx(20)
        val dateArea = dpToPx(40)
        val weekLyBottomPadding = dpToPx(20)
        val dateMargin = dpToPx(3)
        val weekLeftMargin = dpToPx(25)
    }

    private val scrollView = SwipeScrollView(context)
    private val rootLy = FrameLayout(context)
    val calendarLy = LinearLayout(context)
    val weekLys = Array(6) { _ -> FrameLayout(context)}
    val dateLys = Array(6) { _ -> LinearLayout(context)}
    val dateCells = Array(maxCellNum) { _ -> FrameLayout(context)}
    val dateTexts = Array(maxCellNum) { _ -> TextView(context)}
    val selectedBar = LayoutInflater.from(context).inflate(R.layout.view_selected_bar, null, false)
    val weekLySideView = LayoutInflater.from(context).inflate(R.layout.view_weekly_side, null, false)

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
        rootLy.setPadding(0, 0, 0 , dpToPx(50))
        calendarLy.orientation = LinearLayout.VERTICAL
        calendarLy.clipChildren = false

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.clipChildren = false

            val dateLy = dateLys[i]
            dateLy.clipChildren = false
            dateLy.orientation = HORIZONTAL
            dateLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                leftMargin = weekLeftMargin
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

                val dateText = dateTexts[cellNum]
                setDefaultDateTextSkin(dateText)

                dateCell.addView(dateText)
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
        minWidth = (width.toFloat() - weekLeftMargin) / columns
        minHeight = minCalendarHeight.toFloat() / rows

        calendarLy.layoutParams.height = minCalendarHeight
        calendarLy.requestLayout()
        tempCal.add(Calendar.DATE, -startCellNum)

        if(isInit) {
            selectedBar.layoutParams = FrameLayout.LayoutParams(minWidth.toInt(), dateSize)
            selectedBar.pivotY = dateSize.toFloat()
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
                        //dateLy.setBackgroundResource(R.drawable.grey_rect_fill_radius)
                    }

                    val dateText = dateTexts[cellNum]
                    dateText.text = tempCal.get(Calendar.DATE).toString()
                    dateText.alpha = if(cellNum in startCellNum..endCellNum) 1f else 0.1f
                    dateText.setTextColor(getDateTextColor(cellNum))

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
                if(isInit) {
                    for (j in 0..6){
                        setDefaultDateTextSkin(dateTexts[i*7 + j])
                    }
                }
            }
        }

        if(todayStatus == 0) {
            todayStatus = getDiffToday(selectedCal)
        }

        if(postSelectedNum == -1) {
            weekLySideView.translationX = -weekLeftMargin.toFloat()
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
                CalendarSkin.holiDateColor
            }else {
                CalendarSkin.selectedDateColor
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
        if(cellNum in startCellNum..endCellNum){
            selectDate(cellNum,true, isSameDay(tempCal, selectedCal))
        }else {
            moveDate(tempCal.timeInMillis)
        }
    }

    private fun unselectDate(cellNum: Int, anim: Boolean) {
        l("선택 해제 : " + android.text.format.DateFormat.getDateFormat(context).format(cellTimeMills[cellNum]))
        val dateText = dateTexts[cellNum]
        dateText.typeface = CalendarSkin.dateFont
        selectedBar.parent?.let { (it as FrameLayout).removeView(selectedBar) }

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
                    }
                    override fun onAnimationStart(p0: Animator?) {}
                })
                it.playTogether(
                        ObjectAnimator.ofFloat(dateText, "scaleX", selectedDateScale, 1f),
                        ObjectAnimator.ofFloat(dateText, "scaleY", selectedDateScale, 1f))
                it.interpolator = FastOutSlowInInterpolator()
                it.duration = animDur
                it.start()
            }
        }else {
            dateText.scaleX = 1f
            dateText.scaleY = 1f
        }
    }

    fun selectDate(cellNum: Int, anim: Boolean, showDayView: Boolean) {
        if(!showDayView) {
            l("선택한 날짜 : " + android.text.format.DateFormat.getDateFormat(context).format(cellTimeMills[cellNum]))
            selectedCal.timeInMillis = cellTimeMills[cellNum]
            if(cellNum / columns != selectedCellNum / columns) {
                weekLySideView.parent?.let {
                    (it as FrameLayout).removeView(weekLySideView)
                    weekLySideView.translationX = -weekLeftMargin.toFloat()
                }
                weekLys[cellNum / columns].addView(weekLySideView, 0)
                weekLySideView.findViewById<TextView>(R.id.weekNumText).text =
                        String.format(context.getString(R.string.weekNum), selectedCal.get(Calendar.WEEK_OF_YEAR).toString())
            }

            if(selectedCellNum >= 0) { unselectDate(selectedCellNum, anim) }

            selectedCellNum = cellNum
            postSelectedNum = cellNum
            val dateText = dateTexts[selectedCellNum]
            dateText.typeface = CalendarSkin.selectFont

            val color = getDateTextColor(cellNum)
            dateCells[cellNum].addView(selectedBar)
            selectedBar.findViewById<TextView>(R.id.dowText).let {
                it.text = dow[cellNum % columns]
                it.setTextColor(color)
            }
            selectedBar.alpha = if(cellNum in startCellNum..endCellNum) 1f else 0.1f

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
                        }
                        override fun onAnimationStart(p0: Animator?) {}
                    })
                    it.playTogether(
                            ObjectAnimator.ofFloat(dateText, "scaleX", 1f, selectedDateScale),
                            ObjectAnimator.ofFloat(dateText, "scaleY", 1f, selectedDateScale),
                            ObjectAnimator.ofFloat(selectedBar, "scaleY", 0f, 1f),
                            ObjectAnimator.ofFloat(weekLySideView, "translationX", weekLySideView.translationX, 0f))
                    it.interpolator = FastOutSlowInInterpolator()
                    it.duration = animDur
                    it.start()
                }
            }else {
                dateText.scaleX = 1.5f
                dateText.scaleY = 1.5f
            }

            scrollView.post{ scrollView.smoothScrollTo(0, weekLys[cellNum / columns].top) }
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

    fun setDefaultDateTextSkin(textView: TextView) {
        textView.layoutParams = FrameLayout.LayoutParams(dateSize, dateSize).apply {
            topMargin = dateArea - dateSize - dateMargin
            leftMargin = dateMargin
        }
        textView.gravity = Gravity.BOTTOM
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, dateTextSize)
        textView.typeface = CalendarSkin.dateFont
        textView.includeFontPadding = false
        textView.pivotX = 0f
        textView.pivotY = dateSize.toFloat()
    }

    fun setDefaultSelectedBarSkin() {

    }

    fun getSelectedView(): View = dateCells[selectedCellNum]

    fun highlightCells(start: Int, end: Int) {
        val s = if(start < end) start else end
        val e = if(start < end) end else start
        dateCells.forEachIndexed { index, view ->
            if(index in s..e) {
                view.foreground = AppRes.hightlightCover
            }else {

            }
        }
    }

    private fun clearHighlight() {
        dateCells.forEach {

        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////드래그 처리 부분/////////////////////////////////////////////////

    var startDragCell = -1
    var currentDragCell = -1

    fun onDrag(event: DragEvent) {
        var cellX = ((event.x - weekLeftMargin) / minWidth).toInt()
        if(cellX < 0) cellX = 0
        val yPos = (event.y - top - AppRes.statusBarHeight + scrollView.scaleY)
        var cellY = 0
        weekLys.forEachIndexed { index, view ->
            if(yPos > view.top && yPos < view.bottom) {
                cellY = index
                return@forEachIndexed
            }
        }

        currentDragCell = cellY * columns + cellX

        when(event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                startDragCell = currentDragCell
                highlightCells(startDragCell, currentDragCell)
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                highlightCells(startDragCell, currentDragCell)
            }
            DragEvent.ACTION_DROP -> {
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                clearHighlight()
            }
        }

        l("" + cellX +"/"+cellY)

    }
    /////////////////////////////////드래그 처리 부분/////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
}