package com.ayaan.twelvepages.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.DragEvent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import android.widget.LinearLayout.HORIZONTAL
import androidx.core.widget.NestedScrollView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.listener.MainDragAndDropListener
import com.ayaan.twelvepages.manager.CalendarManager
import com.ayaan.twelvepages.manager.DateInfoManager
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.activity.MainActivity
import io.realm.RealmResults
import java.util.*

class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        const val maxCellNum = 42
        const val animDur = 300L
        const val columns = 7
        val todayCal: Calendar = Calendar.getInstance()
        val dragStartYPos = dpToPx(0f)
        val weekLyBottomPadding = dpToPx(10)
        val calendarPadding = dpToPx(30)
        val autoScrollThreshold = dpToPx(70)
        val autoScrollOffset = dpToPx(5)
        val lineWidth = dpToPx(0.5f)
        val dataStartYOffset = dpToPx(33f)
        val headerHeight = dpToPx(110)
    }

    private val scrollView = NestedScrollView(context)
    val calendarLy = LinearLayout(context)
    val weekLys = Array(6) { FrameLayout(context) }
    private val columnDividers = Array(maxCellNum) { View(context) }
    private val rowDividers = Array(6) { View(context) }
    private val weekText = Array(6) { TextView(context) }
    private val dateLys = Array(6) { LinearLayout(context) }
    val dateCells = Array(maxCellNum) { FrameLayout(context) }
    private val dateHeaders = Array(maxCellNum) {
        DateHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.view_selected_bar, null, false)) }
    val dateInfos = Array(maxCellNum) { DateInfoManager.DateInfo() }
    val headerView = View(context)

    inner class DateHeaderViewHolder(val container: View) {
        val dateText: TextView = container.findViewById(R.id.dateText)
        val bar: FrameLayout = container.findViewById(R.id.bar)
        val dowText: TextView = container.findViewById(R.id.dowText)
        val holiText: TextView = container.findViewById(R.id.holiText)
        val dateLy: LinearLayout = container.findViewById(R.id.dateLy)
        init {
            dateText.typeface = AppTheme.regularFont
            dowText.typeface = AppTheme.regularFont
            holiText.typeface = AppTheme.regularFont
            dowText.visibility = View.GONE
            bar.scaleX = 0f
        }
    }

    private var lastSelectDateAnimSet: AnimatorSet? = null
    private var lastUnSelectDateAnimSet: AnimatorSet? = null

    private val tempCal: Calendar = Calendar.getInstance()
    private val monthCal: Calendar = Calendar.getInstance()
    val targetCal: Calendar = Calendar.getInstance()

    var targetCellNum = -1
    var todayCellNum = -1
    val cellTimeMills = LongArray(maxCellNum) { Long.MIN_VALUE}
    var calendarStartTime = Long.MAX_VALUE
    var calendarEndTime = Long.MAX_VALUE
    var onDrawed: ((Calendar) -> Unit)? = null
    var onSelectedDate: ((Long, Int, Int, Boolean) -> Unit)? = null
    var onTop: ((Boolean, Boolean) -> Unit)? = null
    var startCellNum = 0
    var endCellNum = 0
    var minCalendarHeight = 0f
    var minWidth = 0f
    var minHeight = 0f
    var rows = 0
    var todayStatus = 0
    var sundayPos = 0
    var saturdayPos = 6

    private var selectToScrollFlag = true

    init {
        createViews()
        setLayout()
    }

    private fun createViews() {
        addView(scrollView)
        scrollView.addView(calendarLy)
        addView(headerView)
    }

    private fun setLayout() {
        headerView.layoutParams = LayoutParams(MATCH_PARENT, headerHeight)
        headerView.setBackgroundColor(AppTheme.backgroundAlpha)

        scrollView.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        scrollView.isVerticalScrollBarEnabled = false
        scrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
            onTop?.invoke(scrollY == 0, !v.canScrollVertically(1))
        }

        calendarLy.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        calendarLy.setPadding(0, headerHeight, 0, calendarPadding)
        calendarLy.orientation = LinearLayout.VERTICAL

        rowDividers.forEachIndexed { index, view ->
            view.layoutParams = LayoutParams(MATCH_PARENT, lineWidth.toInt() * 2).apply {
                leftMargin = calendarPadding
                rightMargin = 0
            }
            view.setBackgroundColor(AppTheme.lightLine)
        }

        columnDividers.forEachIndexed { index, view ->
            view.layoutParams = LayoutParams(lineWidth.toInt() * 2, 0)
            view.setBackgroundColor(AppTheme.lightLine)
        }

        for(i in 0..5) {
            val weekLy = weekLys[i]
            val dateLy = dateLys[i]
            dateLy.clipChildren = false
            dateLy.orientation = HORIZONTAL
            dateLy.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
            dateLy.setPadding(calendarPadding, 0, 0, 0)
            weekLy.addView(rowDividers[i])
            weekText[i].layoutParams = LayoutParams((lineWidth * 30).toInt(), (lineWidth * 24).toInt()).apply {
                topMargin = (lineWidth * 4).toInt()
            }
            weekText[i].typeface = AppTheme.boldFont
            weekText[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8f)
            weekText[i].setBackgroundColor(AppTheme.background)
            weekText[i].setTextColor(AppTheme.secondaryText)
            weekText[i].gravity = Gravity.CENTER
            weekLy.addView(weekText[i])
            for (j in 0..6){
                val cellNum = i*7 + j

                weekLy.addView(columnDividers[cellNum])
                if(j == 0) columnDividers[cellNum].visibility = View.GONE

                val dateCell = dateCells[cellNum]
                dateCell.setOnClickListener { onDateClick(cellNum) }
                dateCell.setOnLongClickListener {
                    if(targetCellNum != cellNum) onDateClick(cellNum)
                    MainDragAndDropListener.start(it, MainDragAndDropListener.DragMode.INSERT)
                    return@setOnLongClickListener true
                }
                dateCell.layoutParams = LinearLayout.LayoutParams(0, MATCH_PARENT, 1f)
                dateCell.addView(dateHeaders[cellNum].container)
                dateLy.addView(dateCell)
            }
            weekLy.addView(dateLy)
            calendarLy.addView(weekLy)
        }
    }

    fun draw(time: Long) {
        targetCal.timeInMillis = time
        if(width > 0) {
            drawCalendar(time)
        }else {
            callAfterViewDrawed(this, Runnable { drawCalendar(time) })
        }
    }

    fun redraw() { drawCalendar(targetCal.timeInMillis) }

    private fun drawCalendar(time: Long) {
        val t = System.currentTimeMillis()
        todayCal.timeInMillis = System.currentTimeMillis()
        tempCal.timeInMillis = time
        tempCal.set(Calendar.DATE, 1)
        monthCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DATE))
        setCalendarTime0(tempCal)

        startCellNum = tempCal.get(Calendar.DAY_OF_WEEK) - AppStatus.startDayOfWeek
        if (startCellNum < 0) { startCellNum += 7 }
        endCellNum = startCellNum + tempCal.getActualMaximum(Calendar.DATE) - 1
        todayCellNum = -1
        targetCellNum = -1
        rows = (endCellNum + 1) / 7 + if ((endCellNum + 1) % 7 > 0) 1 else 0
        minCalendarHeight = height.toFloat() - headerHeight - calendarPadding
        minWidth = (width.toFloat() - calendarPadding * 1) / columns
        minHeight = minCalendarHeight / rows
        if(AppStatus.startDayOfWeek == Calendar.SUNDAY) {
            sundayPos = 0
            saturdayPos = 6
        } else {
            sundayPos = 8 - AppStatus.startDayOfWeek
            saturdayPos = 7 - AppStatus.startDayOfWeek
        }

        tempCal.add(Calendar.DATE, -startCellNum)

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.layoutParams.height = minHeight.toInt()

            if(AppStatus.weekLine == 0) rowDividers[i].visibility = View.GONE
            else rowDividers[i].visibility = View.VISIBLE

            if(i < rows) {
                weekLy.visibility = View.VISIBLE
                for (j in 0..6){
                    val cellNum = i*7 + j
                    if(AppStatus.weekLine == 0) columnDividers[cellNum].visibility = View.GONE
                    else columnDividers[cellNum].visibility = View.VISIBLE
                    cellTimeMills[cellNum] = tempCal.timeInMillis

                    weekText[i].text = String.format(str(R.string.weekNum), tempCal.get(Calendar.WEEK_OF_YEAR))
                    weekText[i].visibility = View.GONE

                    val dateInfo = dateInfos[cellNum]
                    DateInfoManager.getHoliday(dateInfo, tempCal)
                    if(isSameDay(tempCal, targetCal)) { targetCellNum = cellNum }
                    if(isSameDay(tempCal, todayCal)) { todayCellNum = cellNum }

                    val holiText = dateHeaders[cellNum].holiText
                    val dateText = dateHeaders[cellNum].dateText
                    val dowText = dateHeaders[cellNum].dowText
                    val bar = dateHeaders[cellNum].bar
                    val color = getDateTextColor(cellNum, dateInfo.holiday?.isHoli == true)
                    val alpha = if(cellNum in startCellNum..endCellNum) 1f else AppStatus.outsideMonthAlpha
                    restoreDateHeader(dateHeaders[cellNum])
                    dateText.alpha = alpha
                    dowText.alpha = alpha
                    holiText.alpha = alpha

                    dateText.text = String.format("%02d", tempCal.get(Calendar.DATE))
                    dateText.setTextColor(color)
                    holiText.setTextColor(color)
                    dowText.setTextColor(color)
                    bar.setBackgroundColor(color)
                    holiText.text = dateInfo.getUnSelectedString()

                    if(cellNum == 0) {
                        calendarStartTime = tempCal.timeInMillis
                    }else if(cellNum == rows * columns - 1) {
                        setCalendarTime23(tempCal)
                        calendarEndTime = tempCal.timeInMillis
                    }
                    tempCal.add(Calendar.DATE, 1)
                    columnDividers[cellNum].translationX = minWidth * j - lineWidth + calendarPadding
                }
            }else {
                weekLy.visibility = View.GONE
            }
        }

        //if(SyncUser.current() != null) RecordManager.setTimeObjectCalendarAdapter(this)
        setTimeObjectCalendarAdapter()
        l("${AppDateFormat.month.format(targetCal.time)} 캘린더 그리기 : ${(System.currentTimeMillis() - t) / 1000f} 초")
    }

    private fun getDateTextColor(cellNum: Int, isHoli: Boolean) : Int {
        return if(isHoli || cellNum % columns == sundayPos) {
            CalendarManager.sundayColor
        }else if(cellNum % columns == saturdayPos) {
            CalendarManager.saturdayColor
        }else {
            CalendarManager.dateColor
        }
    }

    private fun onDateClick(cellNum: Int) {
        if(MainActivity.getDayPager()?.isClosed() == true &&
                (AppStatus.outsideMonthAlpha > 0f || cellNum in startCellNum..endCellNum)) {
            tempCal.timeInMillis = cellTimeMills[cellNum]
            selectToScrollFlag = weekLys[cellNum / columns].top < scrollView.scrollY
            selectDate(cellNum, targetCellNum == cellNum)
        }
    }

    fun unselectDate() {
        if(targetCellNum >= 0) unselectDate(targetCellNum)
    }

    fun unselectDate(cellNum: Int) {
        targetCellNum = -1
        val bar = dateHeaders[cellNum].bar
        val dateText = dateHeaders[cellNum].dateText
        val dowText = dateHeaders[cellNum].dowText
        val holiText = dateHeaders[cellNum].holiText
        dowText.visibility = View.GONE
        dateText.typeface = AppTheme.regularFont
        holiText.text = dateInfos[cellNum].getUnSelectedString()
        dateText.alpha = if(cellNum in startCellNum..endCellNum) 1f else AppStatus.outsideMonthAlpha
        offViewEffect(cellNum)
        lastUnSelectDateAnimSet?.cancel()
        lastUnSelectDateAnimSet = AnimatorSet()
        lastUnSelectDateAnimSet?.let {
            it.addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationCancel(animation: Animator?) { restoreDateHeader(dateHeaders[cellNum]) } })
            it.playTogether(ObjectAnimator.ofFloat(bar, "scaleX", bar.scaleX, 0f))
            it.interpolator = FastOutSlowInInterpolator()
            it.duration = animDur
            it.start()
        }
    }

    fun selectDate() {
        selectDate(if(todayCellNum >= startCellNum) todayCellNum else startCellNum)
    }

    fun selectTime(time: Long) {
        selectToScrollFlag = true
        selectDate(((time - calendarStartTime) / DAY_MILL).toInt(), false)
    }

    fun selectDate(cellNum: Int) {
        selectToScrollFlag = true
        selectDate(cellNum, false)
    }

    @SuppressLint("SetTextI18n")
    fun selectDate(cellNum: Int, showDayView: Boolean) {
        if(!showDayView) {
            l("날짜선택 : ${AppDateFormat.ymd.format(Date(cellTimeMills[cellNum]))}")
            targetCal.timeInMillis = cellTimeMills[cellNum]
            if(targetCellNum >= 0) unselectDate(targetCellNum)
            targetCellNum = cellNum

            (0 until rows).forEach {
                if(it == cellNum / columns && AppStatus.isWeekNumDisplay) {
                    weekText[it].visibility = View.VISIBLE
                }else {
                    weekText[it].visibility = View.GONE
                }
            }

            val bar = dateHeaders[cellNum].bar
            val dateText = dateHeaders[cellNum].dateText
            val dowText = dateHeaders[cellNum].dowText
            val holiText = dateHeaders[cellNum].holiText
            dateText.typeface = AppTheme.boldFont
            holiText.text = dateInfos[cellNum].getSelectedString()
            dowText.text = AppDateFormat.simpleDow.format(targetCal.time)
            dateText.alpha = 1f
            if(AppStatus.isDowDisplay) dowText.visibility = View.VISIBLE
            lastSelectDateAnimSet?.cancel()
            lastSelectDateAnimSet = AnimatorSet()
            lastSelectDateAnimSet?.let {
                it.addListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationCancel(animation: Animator?) { restoreDateHeader(dateHeaders[cellNum]) } })
                it.playTogether(ObjectAnimator.ofFloat(bar, "scaleX", 0f, 1f),
                        ObjectAnimator.ofFloat(dowText, "alpha", 0f, 1f),
                        ObjectAnimator.ofFloat(dowText, "translationX", -autoScrollOffset.toFloat(), 1f),
                        ObjectAnimator.ofFloat(holiText, "alpha", 0f, 1f),
                        ObjectAnimator.ofFloat(holiText, "translationX", -autoScrollOffset.toFloat(), 1f))
                it.interpolator = FastOutSlowInInterpolator()
                it.duration = animDur
                it.start()
            }

            if(selectToScrollFlag /*|| isChangeWeek*/) {
                selectToScrollFlag = false
                scrollView.post { scrollView.scrollTo(0, weekLys[cellNum / columns].top - calendarPadding) }
            }
            scrollView.post{ onTop?.invoke(scrollView.scrollY == 0, !scrollView.canScrollVertically(1)) }
            onViewEffect(cellNum)
        }else {
            targetCellNum = cellNum
        }
        todayStatus = getDiffToday(targetCal)
        onSelectedDate?.invoke(cellTimeMills[targetCellNum], targetCellNum, dateHeaders[cellNum].dateText.currentTextColor, showDayView)
    }

    private fun restoreDateHeader(holder: DateHeaderViewHolder) {
        holder.bar.scaleX = 0f
        holder.holiText.alpha = 1f
        holder.holiText.translationX = 0f
        holder.dowText.visibility = View.GONE
    }

    private fun onViewEffect(cellNum: Int) {
        timeObjectCalendarAdapter.getViews(cellNum).let {
            it.forEach { view ->
                view.ellipsize = TextUtils.TruncateAt.MARQUEE
                view.marqueeRepeatLimit = -1
                view.isFocusable = true
                view.postDelayed({
                    view.isSelected = true
                }, 1000)
            }
        }
    }

    private fun offViewEffect(cellNum: Int) {
        timeObjectCalendarAdapter.getViews(cellNum).let {
            it.forEach { view ->
                view.ellipsize = null
                view.isSelected = false
            }
        }
    }

    fun getSelectedView(): View = dateCells[targetCellNum]

    private fun highlightCells(start: Int, end: Int) {
        val s = if(start < end) start else end
        val e = if(start < end) end else start
        dateCells.forEachIndexed { index, view ->
           if(index in s..e) {
               view.background = AppTheme.hightlightCover
           }else {
               view.background = AppTheme.blankDrawable
           }
        }
    }

    private fun clearHighlight() {
        dateCells.forEachIndexed { index, view ->
            view.background = AppTheme.blankDrawable
        }
    }

    private var recordList: RealmResults<Record>? = null
    private var withAnim = false
    private val timeObjectCalendarAdapter = RecordCalendarAdapter(this)
    var lastUpdatedItem: Record? = null

    private fun setTimeObjectCalendarAdapter() {
        withAnim = false
        recordList?.removeAllChangeListeners()
        recordList = RecordManager.getRecordList(calendarStartTime, calendarEndTime, MainActivity.getTargetFolder()).apply {
            try{
                addChangeListener { result, changeSet ->
                    //l("result.isLoaded ${result.isLoaded}")
                    //l("changeSet ${changeSet.isCompleteResult}")
                    val t = System.currentTimeMillis()
                    changeSet.insertionRanges.firstOrNull()?.let {
                        lastUpdatedItem = result[it.startIndex]
                        l("추가된 데이터 : ${lastUpdatedItem.toString()}")
                    }
                    timeObjectCalendarAdapter.refresh(result, withAnim)
                    withAnim = true
                    l("${AppDateFormat.month.format(targetCal.time)} 오브젝트 그리기 : 데이터 ${result.size} 개 / ${(System.currentTimeMillis() - t) / 1000f} 초")
                    onDrawed?.invoke(monthCal)
                }
            }catch (e: Exception){e.printStackTrace()}
        }
    }

    //↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓드래그 처리 부분↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    var startDragCell = -1
    var startDragTime = Long.MIN_VALUE
    var currentDragCell = -1
    var endDragTime = Long.MIN_VALUE
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

    fun onDrag(event: DragEvent) {
        val xPos = if(MainActivity.isFolderOpen()) event.x - MainActivity.tabSize else event.x
        var cellX = ((xPos - calendarPadding) / minWidth).toInt()
        if(cellX < 0) cellX = 0
        val yPos = event.y - dragStartYPos
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
                        if(endDragTime < startDragTime) {
                            val t = startDragTime
                            startDragTime = endDragTime
                            endDragTime = t
                        }
                        MainActivity.instance?.expandControlView(startDragTime, endDragTime)
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
    }

    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑드래그 처리 부분↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
}