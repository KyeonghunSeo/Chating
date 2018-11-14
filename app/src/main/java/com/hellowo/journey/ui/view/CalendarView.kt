package com.hellowo.journey.ui.view

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import android.widget.LinearLayout.HORIZONTAL
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.TransitionManager
import com.hellowo.journey.*
import com.hellowo.journey.manager.CalendarSkin
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.listener.MainDragAndDropListener
import com.hellowo.journey.ui.activity.MainActivity
import me.everything.android.ui.overscroll.IOverScrollState.*
import me.everything.android.ui.overscroll.VerticalOverScrollBounceEffectDecorator
import me.everything.android.ui.overscroll.adapters.ScrollViewOverScrollDecorAdapter
import java.util.*
import kotlin.collections.ArrayList


class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        const val maxCellNum = 42
        const val dateTextSize = 13f
        const val animDur = 250L
        const val columns = 7
        const val selectedDateScale = 2f
        const val outDateAlpha = 0.4f
        val selectedDatePosition = -dpToPx(0f)
        val todayCal: Calendar = Calendar.getInstance()
        val dateArea = dpToPx(30f)
        val weekLyBottomPadding = dpToPx(20)
        val weekSideMargin = dpToPx(10)
        val autoPagingThreshold = dpToPx(30)
        val autoScrollThreshold = dpToPx(70)
        val autoScrollOffset = dpToPx(5)
        val monthPagingThreshold = dpToPx(80)
    }

    private val scrollView = ScrollView(context)
    val calendarLy = LinearLayout(context)
    val weekLys = Array(6) { _ -> FrameLayout(context)}
    private val dateLys = Array(6) { _ -> LinearLayout(context)}
    val dateCells = Array(maxCellNum) { _ -> FrameLayout(context)}
    private val dateHeaders = Array(maxCellNum) { _ ->
        DateHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.view_selected_bar, null, false))}
    private val weekLySideView = LayoutInflater.from(context).inflate(R.layout.view_weekly_side, null, false)
    private val nextMonthHintView = LayoutInflater.from(context).inflate(R.layout.view_next_month_hint, null, false)
    private val fakeImageView = ImageView(context)

    inner class DateHeaderViewHolder(val container: View) {
        val dateText: TextView = container.findViewById(R.id.dateText)
        val bar: View = container.findViewById(R.id.bar)
        val dowText: TextView = container.findViewById(R.id.dowText)
        val flagImg: ImageView = container.findViewById(R.id.flagImg)
        init {
            flagImg.scaleY = 0f
        }
    }

    private var lastSelectDateAnimSet: AnimatorSet? = null
    private var lastUnSelectDateAnimSet: AnimatorSet? = null
    private var lastSelectWeekAnimSet: AnimatorSet? = null
    private var lastUnSelectWeekAnimSet: AnimatorSet? = null

    private val tempCal: Calendar = Calendar.getInstance()
    private val monthCal: Calendar = Calendar.getInstance()
    private val dow = AppRes.dowString

    val targetCal: Calendar = Calendar.getInstance()
    var targetCellNum = -1
    var selectCellNum = -1
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

    private var autoScroll = true

    init {
        CalendarSkin.init(this)
        createViews()
        setLayout()
        callAfterViewDrawed(this, Runnable {
            drawCalendar(System.currentTimeMillis())
            selectDate(todayCellNum, false)
        })
    }

    private fun createViews() {
        addView(scrollView)
        addView(fakeImageView)
        addView(nextMonthHintView)
        scrollView.addView(calendarLy)
    }

    private fun setLayout() {
        scrollView.setBackgroundColor(CalendarSkin.backgroundColor)
        scrollView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        calendarLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        calendarLy.setPadding(0, 0, 0, weekLyBottomPadding)
        calendarLy.orientation = LinearLayout.VERTICAL
        calendarLy.clipChildren = false
        weekLySideView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, dateArea.toInt())
        weekLySideView.alpha = 0f
        weekLySideView.translationY = dateArea
        nextMonthHintView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        nextMonthHintView.alpha = 0f
        fakeImageView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

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
                    MainDragAndDropListener.start(it, MainDragAndDropListener.DragMode.INSERT)
                    return@setOnLongClickListener true
                }
                dateCell.layoutParams = LinearLayout.LayoutParams(0, MATCH_PARENT, 1f)
                dateCell.addView(dateHeaders[cellNum].container)
                dateLy.addView(dateCell)
            }
            calendarLy.addView(weekLy)
        }

        setOverscrollDecor()
    }

    private fun setOverscrollDecor() {
        val decor = VerticalOverScrollBounceEffectDecorator(ScrollViewOverScrollDecorAdapter(scrollView),
                1.7f, // Default is 3
                VerticalOverScrollBounceEffectDecorator.DEFAULT_TOUCH_DRAG_MOVE_RATIO_BCK,
                VerticalOverScrollBounceEffectDecorator.DEFAULT_DECELERATE_FACTOR)
        decor.setOverScrollStateListener { decor, oldState, newState ->
            when (newState) {
                STATE_IDLE -> {}
                STATE_DRAG_START_SIDE -> {
                    tempCal.timeInMillis = monthCal.timeInMillis
                    tempCal.add(Calendar.MONTH, -1)
                    val hint = String.format(context.getString(R.string.next_month_hint), AppRes.mDate.format(tempCal.time))
                    nextMonthHintView.findViewById<TextView>(R.id.nextHintText).text = hint
                }
                STATE_DRAG_END_SIDE -> {
                    tempCal.timeInMillis = monthCal.timeInMillis
                    tempCal.add(Calendar.MONTH, 1)
                    val hint = String.format(context.getString(R.string.next_month_hint), AppRes.mDate.format(tempCal.time))
                    nextMonthHintView.findViewById<TextView>(R.id.nextHintText).text = hint
                }
                STATE_BOUNCE_BACK -> {
                    if(nextMonthHintView.alpha == 1f) {
                        if (oldState == STATE_DRAG_START_SIDE) {
                            moveMonth(-1)
                        } else {
                            moveMonth(1)
                        }
                    }
                    nextMonthHintView.alpha = 0f
                }
            }
        }
        decor.setOverScrollUpdateListener { decor, state, offset ->
            if(state != STATE_BOUNCE_BACK) {
                val alpha = Math.min(1f, Math.abs(offset) / monthPagingThreshold)
                if(nextMonthHintView.alpha != 1f && alpha == 1f) {
                    vibrate(context)
                }
                nextMonthHintView.alpha = alpha
                when {
                    offset > 0 -> {
                        nextMonthHintView.translationY = offset - nextMonthHintView.height
                    }
                    offset < 0 -> {
                        nextMonthHintView.translationY = height + offset - weekLyBottomPadding
                    }
                    else -> {}
                }
            }
        }
    }

    private fun drawCalendar(time: Long) {
        l("==========START drawCalendar=========")
        val t = System.currentTimeMillis()
        todayCal.timeInMillis = System.currentTimeMillis()
        tempCal.timeInMillis = time
        tempCal.set(Calendar.DATE, 1)
        monthCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DATE))
        setCalendarTime0(tempCal)

        todayStatus = (monthCal.get(Calendar.YEAR)*10 + monthCal.get(Calendar.MONTH))
            - (todayCal.get(Calendar.YEAR)*10 + todayCal.get(Calendar.MONTH))

        startCellNum = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        endCellNum = startCellNum + tempCal.getActualMaximum(Calendar.DATE) - 1
        todayCellNum = -1
        targetCellNum = -1
        rows = (endCellNum + 1) / 7 + if ((endCellNum + 1) % 7 > 0) 1 else 0
        minCalendarHeight = height
        minWidth = (width.toFloat() - weekSideMargin * 2) / columns
        minHeight = (minCalendarHeight.toFloat()) / rows

        tempCal.add(Calendar.DATE, -startCellNum)

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.layoutParams.height = minHeight.toInt()
            if(i < rows) {
                weekLy.visibility = View.VISIBLE
                for (j in 0..6){
                    val cellNum = i*7 + j
                    cellTimeMills[cellNum] = tempCal.timeInMillis

                    if(isSameDay(tempCal, targetCal)) {
                        targetCellNum = cellNum
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
                    //dateHeaders[cellNum].bar.setBackgroundColor(color)
                    //dateHeaders[cellNum].bar.alpha = alpha

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
            todayStatus = getDiffToday(targetCal)
        }

        TimeObjectManager.setTimeObjectCalendarAdapter(this)
        scrollView.post { scrollView.scrollTo(0, 0) }
        onDrawed?.invoke(monthCal)

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
        selectDate(cellNum, selectCellNum == cellNum)
    }

    fun unselectDate(cellNum: Int, isFinished: Boolean) {
        val dateText = dateHeaders[cellNum].dateText
        val flagImg = dateHeaders[cellNum].flagImg
        val color = getDateTextColor(cellNum)
        val alpha = if(cellNum in startCellNum..endCellNum) 1f else outDateAlpha

        dateText.typeface = CalendarSkin.dateFont
        dateText.alpha = alpha
        dateHeaders[cellNum].bar.setBackgroundColor(color)

        selectCellNum = -1
        offViewEffect(cellNum)

        lastUnSelectDateAnimSet?.cancel()
        lastUnSelectDateAnimSet = AnimatorSet()
        lastUnSelectDateAnimSet?.let {
            it.addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationCancel(animation: Animator?) {
                    dateText.scaleX = 1f
                    dateText.scaleY = 1f
                    dateText.translationY = 0f
                    flagImg.scaleY = 0f
                }
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

        if(isFinished) {
            TransitionManager.beginDelayedTransition(calendarLy, makeChangeBounceTransition())
            unselectWeek(cellNum / columns)
            onSelected?.invoke(Long.MIN_VALUE, -1, false)
        }
    }

    fun selectDate(cellNum: Int, showDayView: Boolean) {
        if(!showDayView) {
            l("날짜선택 : ${AppRes.ymdDate.format(Date(cellTimeMills[cellNum]))}")
            targetCal.timeInMillis = cellTimeMills[cellNum]
            val week = "${targetCal.get(Calendar.YEAR)}${targetCal.get(Calendar.WEEK_OF_YEAR)}".toInt()
            val weekIndex = cellNum / columns
            if(week != selectedWeek) {
                l("주 선택 : $week")
                TransitionManager.beginDelayedTransition(calendarLy, makeChangeBounceTransition())

                if(selectCellNum >= 0) unselectWeek(selectCellNum / columns)

                selectedWeek = week
                weekLySideView.findViewById<TextView>(R.id.weekNumText).text =
                        String.format(context.getString(R.string.weekNum), targetCal.get(Calendar.WEEK_OF_YEAR).toString())
                calendarLy.addView(weekLySideView, weekIndex)
                lastSelectWeekAnimSet?.cancel()
                lastSelectWeekAnimSet = AnimatorSet()
                lastSelectWeekAnimSet?.let {
                    val animators = ArrayList<Animator>()
                    val a1 = ObjectAnimator.ofFloat(weekLySideView, "translationY", dateArea, 0f)
                    a1.startDelay = 250
                    animators.add(a1)
                    val a2 = ObjectAnimator.ofFloat(weekLySideView, "alpha", 0f, 1f)
                    a2.startDelay = 250
                    animators.add(a2)
                    (0 until columns).forEach { index ->
                        animators.add(ObjectAnimator.ofFloat(
                                dateHeaders[weekIndex * columns + index].bar, "scaleX", 0.2f, 1f)) }
                    it.playTogether(animators)
                    it.addListener(object : AnimatorListenerAdapter(){
                        override fun onAnimationCancel(animation: Animator?) {
                            weekLySideView.alpha = 0f
                            weekLySideView.translationY = dateArea
                            (0 until columns).forEach { index ->
                                dateHeaders[weekIndex * columns + index].bar.scaleX = 0f
                            }
                        }
                    })
                    it.duration = 250
                    it.interpolator = FastOutSlowInInterpolator()
                    it.start()
                }
            }

            if(selectCellNum >= 0) unselectDate(selectCellNum, false)

            selectCellNum = cellNum
            targetCellNum = cellNum

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
            dateHeaders[cellNum].dowText.text = dow[cellNum % columns]

            lastSelectDateAnimSet?.cancel()
            lastSelectDateAnimSet = AnimatorSet()
            lastSelectDateAnimSet?.let {
                it.addListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationCancel(animation: Animator?) {
                        dateText.scaleX = 1f
                        dateText.scaleY = 1f
                        dateText.translationY = 0f
                        flagImg.scaleY = 0f
                    }
                })
                it.playTogether(
                        ObjectAnimator.ofFloat(dateText, "scaleX", 1f, selectedDateScale),
                        ObjectAnimator.ofFloat(dateText, "scaleY", 1f, selectedDateScale),
                        ObjectAnimator.ofFloat(dateText, "translationY", 0f, selectedDatePosition),
                        ObjectAnimator.ofFloat(flagImg, "scaleY", 0f, 1f))
                it.interpolator = FastOutSlowInInterpolator()
                it.duration = animDur
                it.start()
            }

            if(autoScroll) {
                autoScroll = false
                scrollView.post { scrollView.smoothScrollTo(0, weekLys[cellNum / columns].top - dateArea.toInt()) }
            }
            onViewEffect(cellNum)
        }else {
            selectCellNum = cellNum
            targetCellNum = cellNum
        }
        todayStatus = getDiffToday(targetCal)
        onSelected?.invoke(cellTimeMills[selectCellNum], selectCellNum, showDayView)
    }

    var selectedWeek = 0

    private fun unselectWeek(weekNum: Int) {
        weekLySideView.parent?.let {
            (it as LinearLayout).removeView(weekLySideView)
            weekLySideView.alpha = 0f
            weekLySideView.translationY = dateArea
            lastUnSelectWeekAnimSet?.cancel()
            lastUnSelectWeekAnimSet = AnimatorSet()
            lastUnSelectWeekAnimSet?.let {
                val animators = ArrayList<Animator>()
                (0 until columns).forEach { index ->
                    animators.add(ObjectAnimator.ofFloat(
                            dateHeaders[weekNum * columns + index].bar, "scaleX", 1f, 0.2f)) }
                it.playTogether(animators)
                it.addListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationCancel(animation: Animator?) {
                        weekLySideView.alpha = 0f
                        weekLySideView.translationY = dateArea
                        (0 until columns).forEach { index ->
                            dateHeaders[weekNum * columns + index].bar.scaleX = 0.2f
                        }
                    }
                })
                it.duration = 250
                it.interpolator = FastOutSlowInInterpolator()
                it.start()
            }
        }
        selectedWeek = 0
    }

    private fun onViewEffect(cellNum: Int) {
        /*
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
        }*/
    }

    private fun offViewEffect(cellNum: Int) {/*
        TimeObjectManager.timeObjectCalendarAdapter?.getViews(cellNum)?.let {
            it.forEach { view ->
                if(view.timeObject.type == TimeObject.Type.EVENT.ordinal || view.timeObject.type == TimeObject.Type.TASK.ordinal) {
                    view.ellipsize = null
                    view.isSelected = false
                }
            }
        }*/
    }

    fun setOnSwiped(onSwiped: ((Int) -> Unit)) {
        //scrollView.onSwipeStateChanged = onSwiped
    }

    fun setOnTop(onTop: ((Boolean) -> Unit)) {
        //scrollView.onTop = onTop
    }

    fun moveDate(offset: Int, isAutoScroll: Boolean) {
        targetCal.add(Calendar.DATE, offset)
        autoScroll = isAutoScroll
        drawCalendar(targetCal.timeInMillis)
    }

    fun moveDate(time: Long, isAutoScroll: Boolean) {
        targetCal.timeInMillis = time
        autoScroll = isAutoScroll
        drawCalendar(time)
    }

    fun moveMonth(offset: Int) {
        fakeImageView.setImageBitmap(makeViewToBitmap(this))
        fakeImageView.visibility = View.VISIBLE

        monthCal.add(Calendar.MONTH, offset)
        targetCal.timeInMillis = monthCal.timeInMillis
        if(selectCellNum >= 0) unselectDate(selectCellNum, true)
        //autoScroll = true
        drawCalendar(monthCal.timeInMillis)

        val animSet = AnimatorSet()
        if(offset < 0) {
            animSet.playTogether(ObjectAnimator.ofFloat(fakeImageView, "translationY", 0f, height.toFloat()),
                    ObjectAnimator.ofFloat(scrollView.getChildAt(0), "translationY", -height.toFloat(), 0f))
        }else {
            animSet.playTogether(ObjectAnimator.ofFloat(fakeImageView, "translationY", 0f, -height.toFloat()),
                    ObjectAnimator.ofFloat(scrollView.getChildAt(0), "translationY", height.toFloat(), 0f))
        }/*
        if(offset < 0) {
            animSet.playTogether(ObjectAnimator.ofFloat(fakeImageView, "alpha", 1f, 0.5f),
                    ObjectAnimator.ofFloat(fakeImageView, "scaleX", 1f, 0.7f),
                    ObjectAnimator.ofFloat(fakeImageView, "scaleY", 1f, 0.7f),
                    ObjectAnimator.ofFloat(scrollView, "translationX", -width.toFloat(), 0f))
        }else {
            animSet.playTogether(ObjectAnimator.ofFloat(fakeImageView, "alpha", 1f, 0.5f),
                    ObjectAnimator.ofFloat(fakeImageView, "scaleX", 1f, 0.7f),
                    ObjectAnimator.ofFloat(fakeImageView, "scaleY", 1f, 0.7f),
                    ObjectAnimator.ofFloat(scrollView, "translationX", width.toFloat(), 0f))
        }*/
        animSet.addListener(object : AnimatorListenerAdapter(){
            override fun onAnimationStart(animation: Animator?) {}
        })
        animSet.duration = 500
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()

        //startPagingEffectAnimation(offset, scrollView, null)
    }

    fun getSelectedView(): View = dateCells[selectCellNum]

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
                    if(MainDragAndDropListener.dragMode == MainDragAndDropListener.DragMode.INSERT) {
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
}