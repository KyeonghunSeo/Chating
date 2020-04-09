package com.ayaan.twelvepages.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator.REVERSE
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.view.DragEvent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AnimationUtils
import android.widget.*
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
import com.ayaan.twelvepages.ui.view.base.CalendarBackground
import kotlinx.android.synthetic.main.view_date_cell_header.view.*
import io.realm.RealmResults
import java.util.*

class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        const val maxCellNum = 42
        const val animDur = 300L
        const val columns = 7
        val todayCal: Calendar = Calendar.getInstance()
        val dragStartYPos = dpToPx(0f)
        val calendarPadding = dpToPx(15)
        val calendarTopPadding = dpToPx(2)
        val calendarBottomPadding = dpToPx(42)
        val autoScrollThreshold = dpToPx(70)
        val autoScrollOffset = dpToPx(5)
        val lineWidth = dpToPx(0.5f)
        val dataStartYOffset = dpToPx(33f)
    }

    private val scrollView = NestedScrollView(context)
    val calendarLy = CalendarBackground(context)
    val weekLys = Array(6) { FrameLayout(context) }
    private val columnDividers = Array(maxCellNum) { View(context) }
    private val topDivider = View(context)
    private val bottomDivider = View(context)
    private val rowDividers = Array(5) { View(context) }
    private val weekViewHolders = Array(6) { WeekInfoViewHolder(
            LayoutInflater.from(context).inflate(R.layout.view_week_info, null, false)) }
    val dateCellHolders = Array(maxCellNum) { index -> DateInfoViewHolder(index,
            LayoutInflater.from(context).inflate(R.layout.view_date_cell_header, null, false) as FrameLayout,
            DateInfoManager.DateInfo()) }
    val recordsViews = Array(maxCellNum) { FrameLayout(context) }

    private var lastSelectDateAnimSet: AnimatorSet? = null
    private var lastUnSelectDateAnimSet: AnimatorSet? = null

    private val tempCal: Calendar = Calendar.getInstance()
    private val monthCal: Calendar = Calendar.getInstance()
    val targetCal: Calendar = Calendar.getInstance()

    var targetDateHolder: DateInfoViewHolder? = null
    var targetWeekHolder: WeekInfoViewHolder? = null
    var calendarStartTime = Long.MAX_VALUE
    var calendarEndTime = Long.MAX_VALUE
    var onDrawed: ((Calendar) -> Unit)? = null
    var onSelectedDate: ((DateInfoViewHolder, Boolean) -> Unit)? = null
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

    init {
        createViews()
        setLayout()
    }

    private fun createViews() {
        addView(scrollView)
        scrollView.addView(calendarLy)
        addView(topDivider)
        //addView(bottomDivider)
    }

    private fun setLayout() {
        scrollView.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        scrollView.isVerticalScrollBarEnabled = false
        scrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
            val isTop = scrollY == 0
            if(isTop) {
                topDivider.alpha = AppStatus.weekLine
                (topDivider.layoutParams as LayoutParams).let {
                    it.leftMargin = calendarPadding
                    it.rightMargin = calendarPadding
                }
            }else {
                if(AppStatus.weekLine == 0f) {
                    topDivider.alpha = 0.2f
                }else {
                    topDivider.alpha = AppStatus.weekLine
                }
                (topDivider.layoutParams as LayoutParams).let {
                    it.leftMargin = calendarPadding
                    it.rightMargin = calendarPadding
                }
            }
            topDivider.requestLayout()
            onTop?.invoke(isTop, !v.canScrollVertically(1))
        }

        calendarLy.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        calendarLy.setPadding(0, calendarTopPadding, 0, calendarBottomPadding)
        calendarLy.orientation = LinearLayout.VERTICAL
        calendarLy.clipChildren = false
        calendarLy.clipToPadding = false

        topDivider.setBackgroundResource(R.drawable.dashed_line_bold)
        topDivider.layoutParams = LayoutParams(MATCH_PARENT, lineWidth.toInt() * 3).apply {
            leftMargin = calendarPadding
            rightMargin = calendarPadding
        }

        bottomDivider.setBackgroundResource(R.drawable.dashed_line_bold)
        bottomDivider.layoutParams = LayoutParams(MATCH_PARENT, lineWidth.toInt() * 3).apply {
            gravity = Gravity.BOTTOM
        }

        rowDividers.forEachIndexed { index, view ->
            view.layoutParams = LayoutParams(MATCH_PARENT, lineWidth.toInt() * 3).apply {
                leftMargin = calendarPadding
                rightMargin = calendarPadding
            }
            view.setBackgroundResource(R.drawable.dashed_line_bold)
        }

        columnDividers.forEachIndexed { index, view ->
            view.layoutParams = LayoutParams(lineWidth.toInt(), 0)
            view.setBackgroundColor(AppTheme.secondaryText)
        }

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.clipChildren = false

            if(i > 0) {
                weekLy.addView(rowDividers[i - 1]) // 로우 디바이더 추가
            }

            for (j in 0..6) { // 컬럼 디바이더 추가
                columnDividers[i * 7 + j].let {
                    weekLy.addView(it)
                    if (j == 0) it.visibility = View.GONE
                }
            }

            for (j in 0..6) { // 날짜 셀 추가
                val cellNum = i * 7 + j
                val dateInfoViewHolder = dateCellHolders[cellNum]
                val dateCell = dateInfoViewHolder.v
                dateCell.setOnClickListener {
                    if (MainActivity.getDayPager()?.isClosed() == true &&
                            (AppStatus.outsideMonthAlpha > 0f || dateInfoViewHolder.isInMonth)) {
                        if (targetDateHolder == dateInfoViewHolder) {
                            onSelectedDate?.invoke(dateInfoViewHolder, true)
                        } else {
                            selectDate(dateInfoViewHolder, weekLys[cellNum / columns].top < scrollView.scrollY)
                        }
                        calendarLy.clearDragPoint()
                    }
                }
                dateCell.setOnLongClickListener {
                    if (targetDateHolder != dateInfoViewHolder) {
                        selectDate(dateInfoViewHolder, weekLys[cellNum / columns].top < scrollView.scrollY)
                    }
                    MainDragAndDropListener.start(it, MainDragAndDropListener.DragMode.INSERT)
                    return@setOnLongClickListener true
                }
                dateCell.layoutParams = LinearLayout.LayoutParams(0, MATCH_PARENT)
                weekLy.addView(dateCell)
            }

            weekLy.addView(weekViewHolders[i].container) // 주간 뷰 추가
            weekViewHolders[i].container.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)

            for (j in 0..6) { // 기록 홀더 추가
                recordsViews[i * 7 + j].let {
                    it.layoutParams = LinearLayout.LayoutParams(0, MATCH_PARENT)
                    weekLy.addView(it)
                }
            }

            calendarLy.addView(weekLy)
        }
    }

    inner class WeekInfoViewHolder(val container: View) {
        val weeknumText: TextView = container.findViewById(R.id.weeknumText)
        init {
            weeknumText.setTextColor(AppTheme.lightLine)
            weeknumText.typeface = AppTheme.boldFont
            unTarget()
        }

        fun unTarget() {
            container.visibility = View.GONE
        }

        fun target() {
            if(AppStatus.isWeekNumDisplay) {
                container.visibility = View.VISIBLE
            }else {
                container.visibility = View.GONE
            }
            targetWeekHolder = this
        }
    }

    inner class DateInfoViewHolder(val cellNum: Int,  val v: FrameLayout, val dateInfo: DateInfoManager.DateInfo) {
        var time: Long = Long.MIN_VALUE
        var isToday: Boolean = false
        var isInMonth: Boolean = false
        var color = 0

        init {
            v.clipChildren = false
            v.dateText.typeface = CalendarManager.dateFont
            v.dowText.typeface = AppTheme.regularFont
            v.holiText.typeface = AppTheme.regularFont
            v.diffText.typeface = AppTheme.regularFont
        }

        fun setDate(cal : Calendar) {
            time = cal.timeInMillis
            isToday = isSameDay(cal, todayCal)
            isInMonth = cellNum in startCellNum..endCellNum
            DateInfoManager.getHoliday(dateInfo, cal)
            val alpha = if(isInMonth) 1f else AppStatus.outsideMonthAlpha
            v.dateLy.alpha = alpha
            v.dateText.text = String.format("%02d", cal.get(Calendar.DATE))
            v.dowText.tag = AppDateFormat.simpleDow.format(cal.time)
            v.bar.scaleX = 0.95f
            v.bar.scaleY = 0.95f
            v.bar.alpha = 0f
            initViews()
        }

        fun target() {
            targetDateHolder?.unTarget()
            targetDateHolder = this
            v.holiText.text = dateInfo.getSelectedString()
            v.diffText.text = dateInfo.getDiffDateString()

            color = getDateTextColor(cellNum, dateInfo.holiday?.isHoli == true, true)
            v.dateText.setTextColor(color)
            v.holiText.setTextColor(color)

            val weekHolder = weekViewHolders[cellNum / columns]
            if(targetWeekHolder != weekHolder) {
                targetWeekHolder?.unTarget()
                weekHolder.target()
            }

            lastSelectDateAnimSet?.cancel()
            lastSelectDateAnimSet = AnimatorSet()
            lastSelectDateAnimSet?.let {
                it.addListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationCancel(animation: Animator?) {
                        v.bar.scaleX = 0.95f
                        v.bar.scaleY = 0.95f
                        v.bar.alpha = 0f
                    }
                })
                val anims = ArrayList<Animator>()
                anims.add(ObjectAnimator.ofFloat(v.bar, "scaleX", v.bar.scaleX, 1f))
                anims.add(ObjectAnimator.ofFloat(v.bar, "scaleY", v.bar.scaleY, 1f))
                anims.add(ObjectAnimator.ofFloat(v.bar, "alpha", v.bar.alpha, 1f))
                anims.add(ObjectAnimator.ofFloat(v.holiText, "alpha", 0f, 1f))
                anims.add(ObjectAnimator.ofFloat(v.diffText, "alpha", 0f, 1f))
                it.playTogether(anims)
                it.interpolator = FastOutSlowInInterpolator()
                it.duration = animDur
                it.start()
            }
            onViewEffect(cellNum)

            if(true) {
                val view = View(context)
                view.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
                view.setBackgroundColor(AppTheme.lightLine)
                v.addView(view, 1) // bar 위에
                val anim = AnimatorSet()
                anim.addListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) { v.removeView(view) }
                    override fun onAnimationCancel(animation: Animator?) { v.removeView(view) }
                })
                val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
                alpha.repeatCount = 1
                alpha.repeatMode = REVERSE
                anim.playTogether(alpha)
                anim.start()
            }

        }

        fun unTarget() {
            targetDateHolder = null
            initViews()
            lastUnSelectDateAnimSet?.cancel()
            lastUnSelectDateAnimSet = AnimatorSet()
            lastUnSelectDateAnimSet?.let {
                it.addListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationCancel(animation: Animator?) {
                        v.bar.scaleX = 0.95f
                        v.bar.scaleY = 0.95f
                        v.bar.alpha = 0f
                    }
                })
                val anims = ArrayList<Animator>()
                anims.add(ObjectAnimator.ofFloat(v.bar, "scaleX", v.bar.scaleX, 0.95f))
                anims.add(ObjectAnimator.ofFloat(v.bar, "scaleY", v.bar.scaleY, 0.95f))
                anims.add(ObjectAnimator.ofFloat(v.bar, "alpha", v.bar.alpha, 0f))
                it.playTogether(anims)
                it.interpolator = FastOutSlowInInterpolator()
                it.duration = animDur
                it.start()
            }
            offViewEffect(cellNum)
        }

        private fun initViews() {
            color = getDateTextColor(cellNum, dateInfo.holiday?.isHoli == true, false)
            v.dowText.visibility = View.GONE
            v.dateText.setTextColor(color)
            v.holiText.setTextColor(color)
            v.diffText.setTextColor(CalendarManager.dateColor)
            v.holiText.alpha = 1f
            v.diffText.alpha = 1f
            v.holiText.text = dateInfo.getUnSelectedString()
            v.diffText.text = ""
            if(isToday) {
                v.dateText.paintFlags = v.dateText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            }else {
                v.dateText.paintFlags = v.dateText.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
        }

        fun getDowText(): String = v.dowText?.tag as String? ?:""
        fun getDiffTextLeft() = v.diffText.left
    }

    private fun getDateTextColor(cellNum: Int, isHoli: Boolean, isSelected: Boolean) : Int {
        val color =  if(isHoli || cellNum % columns == sundayPos) {
            CalendarManager.sundayColor
        }else if(cellNum % columns == saturdayPos) {
            CalendarManager.saturdayColor
        }else {
            CalendarManager.dateColor
        }

        return if(color == CalendarManager.dateColor && isSelected) {
            CalendarManager.selectedDateColor
        }else {
            color
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
        rows = (endCellNum + 1) / 7 + if ((endCellNum + 1) % 7 > 0) 1 else 0
        minCalendarHeight = height.toFloat() - calendarTopPadding - calendarBottomPadding
        minWidth = (width.toFloat() - calendarPadding * 2) / columns
        minHeight = minCalendarHeight / rows

        if(AppStatus.startDayOfWeek == Calendar.SUNDAY) {
            sundayPos = 0
            saturdayPos = 6
        } else {
            sundayPos = 8 - AppStatus.startDayOfWeek
            saturdayPos = 7 - AppStatus.startDayOfWeek
        }

        tempCal.add(Calendar.DATE, -startCellNum)

        topDivider.alpha = AppStatus.weekLine
        bottomDivider.alpha = AppStatus.weekLine

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.layoutParams.height = minHeight.toInt()

            if(i > 0) {
                rowDividers[i - 1].alpha = AppStatus.weekLine
            }

            if(i < rows) {
                weekLy.visibility = View.VISIBLE
                for (j in 0..6){
                    val cellNum = i*7 + j
                    if(cellNum == 0) {
                        calendarStartTime = tempCal.timeInMillis
                    }else if(cellNum == rows * columns - 1) {
                        setCalendarTime23(tempCal)
                        calendarEndTime = tempCal.timeInMillis
                    }

                    columnDividers[cellNum].alpha = AppStatus.weekLine
                    columnDividers[cellNum].translationX = minWidth * j - lineWidth + calendarPadding
                    weekViewHolders[i].weeknumText.text = String.format(str(R.string.weekNum), tempCal.get(Calendar.WEEK_OF_YEAR))
                            //.toList().joinToString("")

                    dateCellHolders[cellNum].let {
                        it.setDate(tempCal)
                        it.v.translationX = minWidth * j + calendarPadding
                        it.v.layoutParams.width = minWidth.toInt() + 1 /* 나누기 유격 커버 */
                    }
                    recordsViews[cellNum].let {
                        it.translationX = minWidth * j + calendarPadding
                        it.layoutParams.width = minWidth.toInt()
                    }
                    tempCal.add(Calendar.DATE, 1)
                }
            }else {
                weekLy.visibility = View.GONE
            }
        }

        //if(SyncUser.current() != null) RecordManager.setTimeObjectCalendarAdapter(this)
        setTimeObjectCalendarAdapter()
        l("${AppDateFormat.month.format(targetCal.time)} 캘린더 그리기 : ${(System.currentTimeMillis() - t) / 1000f} 초")
    }

    fun selectDate() {
        dateCellHolders.firstOrNull { it.isInMonth && it.isToday }?.let {
            selectDate(it, true)
            return
        }
        selectDate(dateCellHolders[startCellNum], true)
    }

    fun selectTime(time: Long) {
        selectDate(getCellNumByTime(time))
    }

    fun selectDate(cellNum: Int) {
        selectDate(dateCellHolders[cellNum], true)
    }

    @SuppressLint("SetTextI18n")
    private fun selectDate(holder: DateInfoViewHolder, withScroll: Boolean) {
        l("날짜선택 : ${AppDateFormat.ymd.format(Date(holder.time))}")
        targetCal.timeInMillis = holder.time
        holder.target()
        if(withScroll) {
            scrollView.post { scrollView.scrollTo(0, weekLys[holder.cellNum / columns].top) }
        }
        scrollView.post{ onTop?.invoke(scrollView.scrollY == 0, !scrollView.canScrollVertically(1)) }
        todayStatus = getDiffToday(targetCal)
        onSelectedDate?.invoke(holder, false)
    }

    fun unselectDate() {
        targetDateHolder?.unTarget()
        targetWeekHolder?.unTarget()
        targetWeekHolder = null
    }

    private fun onViewEffect(cellNum: Int) {
        timeObjectCalendarAdapter.getViewsAtStart(cellNum).let {
            it.forEach { view ->
                //view.setTypeface(AppTheme.boldFont, Typeface.BOLD)
                view.ellipsize = TextUtils.TruncateAt.MARQUEE
                view.marqueeRepeatLimit = -1
                view.isFocusable = true
                view.postDelayed({
                    view.isSelected = true
                }, 200)
            }
        }
    }

    private fun offViewEffect(cellNum: Int) {
        timeObjectCalendarAdapter.getViewsAtStart(cellNum).let {
            it.forEach { view ->
                //view.typeface = AppTheme.regularFont
                view.ellipsize = null
                view.isSelected = false
            }
        }
    }

    fun getSelectedView(): View = targetDateHolder?.v ?: dateCellHolders[0].v
    fun getSelectedViewHolders(): List<RecordCalendarAdapter.RecordViewHolder>? {
        targetDateHolder?.let {
            return timeObjectCalendarAdapter.getViewHolders(it.cellNum)
        }
        return null
    }

    private fun highlightCells(start: Int, end: Int) {
        val s = if(start < end) start else end
        val e = if(start < end) end else start
        calendarLy.setDragPoint(s, e, weekLys, minWidth)
    }

    fun clearHighlight() {
        targetDateHolder?.v?.bar?.visibility = View.VISIBLE
        calendarLy.clearDragPoint()
    }

    private var recordList: RealmResults<Record>? = null
    private var withAnim = false
    private val timeObjectCalendarAdapter = RecordCalendarAdapter(this)
    var lastUpdatedItem: Record? = null

    private fun setTimeObjectCalendarAdapter() {
        withAnim = false
        recordList?.removeAllChangeListeners()
        try{
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
                }catch (e: Exception){ e.printStackTrace() }
            }
        }catch (e: Exception){ e.printStackTrace() }
    }

    private fun getCellNumByTime(time: Long) : Int {
        val cellnum = (time - calendarStartTime) / DAY_MILL
        return when{
            cellnum < 0 -> 0
            cellnum >= columns * rows -> return columns * rows - 1
            else -> cellnum.toInt()
        }
    }

    //↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓드래그 처리 부분↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

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

    fun onDrag(event: DragEvent, x: Float, y: Float) {
        val drag = MainDragAndDropListener
        val xPos = if(MainActivity.isFolderOpen()) x - MainActivity.tabSize else x
        var cellX = ((xPos - calendarPadding) / minWidth).toInt()
        if(cellX < 0) cellX = 0
        val yPos = y - dragStartYPos
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
            val currentCell = cellY * columns + cellX
            drag.currentTime = dateCellHolders[currentCell].time

            when(event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    drag.startTime = dateCellHolders[currentCell].time
                    highlightCells(currentCell, currentCell)
                    targetDateHolder?.v?.bar?.visibility = View.GONE
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    highlightCells(getCellNumByTime(drag.startTime), currentCell)
                }
            }
        }
    }

    fun endDrag() {
        autoScrollFlag = 0
        autoScrollHandler.removeMessages(0)
    }

    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑드래그 처리 부분↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
}