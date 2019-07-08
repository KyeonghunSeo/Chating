package com.ayaan.twelvepages.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.view.DragEvent
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
import kotlinx.android.synthetic.main.view_selected_date_header.view.*
import io.realm.RealmResults
import java.util.*

class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        const val maxCellNum = 42
        const val animDur = 300L
        const val columns = 7
        val todayCal: Calendar = Calendar.getInstance()
        val dragStartYPos = dpToPx(0f)
        val weekLyBottomPadding = dpToPx(5)
        val calendarPadding = dpToPx(25)
        val autoScrollThreshold = dpToPx(70)
        val autoScrollOffset = dpToPx(5)
        val lineWidth = dpToPx(0.5f)
        val dataStartYOffset = dpToPx(43f)
        val headerHeight = dpToPx(100)
    }

    private val headerView = View(context)
    private val scrollView = NestedScrollView(context)
    val calendarLy = LinearLayout(context)
    val weekLys = Array(6) { FrameLayout(context) }
    private val dateLys = Array(6) { LinearLayout(context) }
    private val columnDividers = Array(maxCellNum) { View(context) }
    private val rowDividers = Array(6) { View(context) }
    private val weekHolders = Array(6) { WeekInfoViewHolder(
            LayoutInflater.from(context).inflate(R.layout.view_selected_week_info, null, false)) }
    val dateHolders = Array(maxCellNum) { index -> DateInfoViewHolder(index,
            LayoutInflater.from(context).inflate(R.layout.view_selected_date_header, null, false) as FrameLayout,
            DateInfoManager.DateInfo()) }

    private var lastSelectDateAnimSet: AnimatorSet? = null
    private var lastUnSelectDateAnimSet: AnimatorSet? = null

    private val tempCal: Calendar = Calendar.getInstance()
    private val monthCal: Calendar = Calendar.getInstance()
    val targetCal: Calendar = Calendar.getInstance()

    var targetHolder: DateInfoViewHolder? = null
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
        addView(headerView)
    }

    private fun setLayout() {
        headerView.layoutParams = LayoutParams(MATCH_PARENT, headerHeight)
        headerView.setBackgroundColor(AppTheme.background)

        scrollView.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        scrollView.isVerticalScrollBarEnabled = false
        scrollView.setOnScrollChangeListener { v: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
            onTop?.invoke(scrollY == 0, !v.canScrollVertically(1))
        }

        calendarLy.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        calendarLy.setPadding(0, headerHeight, 0, calendarPadding)
        calendarLy.orientation = LinearLayout.VERTICAL
        calendarLy.clipChildren = false

        rowDividers.forEachIndexed { index, view ->
            view.layoutParams = LayoutParams(MATCH_PARENT, lineWidth.toInt() * 3).apply {
                leftMargin = calendarPadding
                rightMargin = 0
                topMargin = weekLyBottomPadding
            }
            view.setBackgroundColor(AppTheme.line)
        }

        columnDividers.forEachIndexed { index, view ->
            view.layoutParams = LayoutParams(lineWidth.toInt() * 2, 0)
            view.setBackgroundColor(AppTheme.line)
        }

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.clipChildren = false
            val dateLy = dateLys[i]
            dateLy.clipChildren = false
            dateLy.orientation = HORIZONTAL
            dateLy.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
            dateLy.setPadding(calendarPadding, 0, 0, 0)
            weekLy.addView(rowDividers[i])

            weekHolders[i].container.layoutParams = LayoutParams(calendarPadding, MATCH_PARENT)
            weekLy.addView(weekHolders[i].container)

            for (j in 0..6){
                val cellNum = i*7 + j

                weekLy.addView(columnDividers[cellNum])
                if(j == 0) columnDividers[cellNum].visibility = View.GONE

                val holder = dateHolders[cellNum]
                val dateCell = holder.v
                dateCell.setOnClickListener {
                    if(MainActivity.getDayPager()?.isClosed() == true &&
                            (AppStatus.outsideMonthAlpha > 0f || cellNum in startCellNum..endCellNum)) {
                        if(targetHolder == holder) {
                            onSelectedDate?.invoke(holder, true)
                        }else {
                            selectDate(holder, weekLys[cellNum / columns].top < scrollView.scrollY + headerHeight)
                        }
                    }
                }
                dateCell.setOnLongClickListener {
                    if(targetHolder != holder) {
                        selectDate(holder, weekLys[cellNum / columns].top < scrollView.scrollY + headerHeight)
                    }
                    MainDragAndDropListener.start(it, MainDragAndDropListener.DragMode.INSERT)
                    return@setOnLongClickListener true
                }
                dateCell.layoutParams = LinearLayout.LayoutParams(0, MATCH_PARENT, 1f)
                dateLy.addView(dateCell)
            }
            weekLy.addView(dateLy)
            calendarLy.addView(weekLy)
        }
    }

    inner class DateInfoViewHolder(val cellNum: Int,  val v: FrameLayout, val dateInfo: DateInfoManager.DateInfo) {
        var time: Long = Long.MIN_VALUE
        var isToday: Boolean = false
        var isInMonth: Boolean = false
        var color = 0

        init {
            v.dowText.setTypeface(AppTheme.boldFont, Typeface.BOLD)
        }

        fun setDate(cal : Calendar) {
            time = cal.timeInMillis
            isToday = isSameDay(cal, todayCal)
            isInMonth = cellNum in startCellNum..endCellNum
            DateInfoManager.getHoliday(dateInfo, cal)
            color = getDateTextColor(cellNum, dateInfo.holiday?.isHoli == true)
            val alpha = if(isInMonth) 1f else AppStatus.outsideMonthAlpha
            v.dateLy.alpha = alpha
            v.bar.alpha = alpha
            v.dateText.text = String.format("%01d", tempCal.get(Calendar.DATE))
            v.dateText.setTextColor(color)
            v.holiText.setTextColor(color)
            v.dowText.setTextColor(color)
            v.bar.setBackgroundColor(color)
            unTarget()
        }

        fun target() {
            targetHolder = this

            v.dateText.typeface = AppTheme.regularFont
            //v.dateText.setTypeface(AppTheme.boldFont, Typeface.BOLD)
            v.holiText.setTypeface(AppTheme.boldFont, Typeface.BOLD)
            v.holiText.text = dateInfo.getSelectedString()
            v.dowText.text = AppDateFormat.dow.format(targetCal.time)
            v.lunarText.visibility = View.GONE
            if(AppStatus.isDowDisplay) v.dowText.visibility = View.VISIBLE

            lastSelectDateAnimSet?.cancel()
            lastSelectDateAnimSet = AnimatorSet()
            lastSelectDateAnimSet?.let {
                it.addListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationCancel(animation: Animator?) {}
                })
                val anims = ArrayList<Animator>()
                anims.add(ObjectAnimator.ofFloat(v.bar, "scaleX", 0f, 1f))
                anims.add(ObjectAnimator.ofFloat(v.dowText, "alpha", 0f, 1f))
                anims.add(ObjectAnimator.ofFloat(v.dowText, "translationX", -autoScrollOffset.toFloat(), 1f))
                anims.add(ObjectAnimator.ofFloat(v.holiText, "alpha", 0f, 1f))
                anims.add(ObjectAnimator.ofFloat(v.holiText, "translationX", -autoScrollOffset.toFloat(), 1f))

                val weekHolder = weekHolders[cellNum / columns]
                if(targetWeekHolder != weekHolder) {
                    targetWeekHolder?.let { wh ->
                        anims.add(ObjectAnimator.ofFloat(wh.container, "translationX",
                                wh.container.translationX, -calendarPadding.toFloat()))
                    }
                    anims.add(ObjectAnimator.ofFloat(weekHolder.container, "translationX",
                            -calendarPadding.toFloat(), 0f))
                    targetWeekHolder = weekHolder
                }
                it.playTogether(anims)
                it.interpolator = FastOutSlowInInterpolator()
                it.duration = animDur
                it.start()
            }
            onViewEffect(cellNum)
/*
        if(todayStatus == 0) {
            val startAnimation = AnimationUtils.loadAnimation(context, R.anim.blink)
            dateHolders[cellNum].container.startAnimation(startAnimation)
        }
*/
        }

        fun unTarget() {
            targetHolder = null

            v.dateText.typeface = AppTheme.regularFont
            v.holiText.typeface = AppTheme.regularFont
            v.lunarText.typeface = AppTheme.regularFont
            v.holiText.alpha = 1f
            v.holiText.translationX = 0f
            v.dowText.alpha = 1f
            v.dowText.translationX = 0f
            v.dowText.visibility = View.GONE
            v.lunarText.visibility = View.GONE
            v.holiText.text = dateInfo.getUnSelectedString()

            lastUnSelectDateAnimSet?.cancel()
            lastUnSelectDateAnimSet = AnimatorSet()
            lastUnSelectDateAnimSet?.let {
                it.addListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationCancel(animation: Animator?) {
                        v.bar.scaleX = 0f
                    }
                })
                val anims = ArrayList<Animator>()
                anims.add(ObjectAnimator.ofFloat(v.bar, "scaleX", v.bar.scaleX, 0f))
                it.playTogether(anims)
                it.interpolator = FastOutSlowInInterpolator()
                it.duration = animDur
                it.start()
            }
            offViewEffect(cellNum)
        }
    }

    inner class WeekInfoViewHolder(val container: View) {
        val weeknumText: TextView = container.findViewById(R.id.weeknumText)
        val weekArrow: ImageView = container.findViewById(R.id.weekArrow)
        val indicatorLy: LinearLayout = container.findViewById(R.id.indicatorLy)
        init {
            weeknumText.setTextColor(AppTheme.background)
            weeknumText.typeface = AppTheme.boldFont
            indicatorLy.visibility = View.GONE
            container.translationX = -calendarPadding.toFloat()
            weekArrow.visibility = View.GONE
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

            rowDividers[i].alpha = AppStatus.weekLine

            if(i < rows) {
                weekLy.visibility = View.VISIBLE
                for (j in 0..6){
                    val cellNum = i*7 + j

                    columnDividers[cellNum].alpha = AppStatus.weekLine
                    weekHolders[i].weeknumText.text = tempCal.get(Calendar.WEEK_OF_YEAR).toString()
                    dateHolders[cellNum].setDate(tempCal)

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

    fun selectDate() {
        dateHolders.firstOrNull { it.isInMonth && it.isToday }?.let {
            selectDate(it, true)
            return
        }
        selectDate(dateHolders[startCellNum], true)
    }

    fun selectTime(time: Long) {
        selectDate(((time - calendarStartTime) / DAY_MILL).toInt())
    }

    fun selectDate(cellNum: Int) {
        selectDate(dateHolders[cellNum], true)
    }

    @SuppressLint("SetTextI18n")
    private fun selectDate(holder: DateInfoViewHolder, withScroll: Boolean) {
        l("날짜선택 : ${AppDateFormat.ymd.format(Date(holder.time))}")
        targetCal.timeInMillis = holder.time
        targetHolder?.unTarget()
        holder.target()

        if(withScroll) {
            scrollView.post { scrollView.scrollTo(0, weekLys[holder.cellNum / columns].top - headerHeight) }
        }
        scrollView.post{ onTop?.invoke(scrollView.scrollY == 0, !scrollView.canScrollVertically(1)) }

        todayStatus = getDiffToday(targetCal)
        onSelectedDate?.invoke(holder, false)
    }

    fun unselectDate() {
        targetHolder?.unTarget()
        targetWeekHolder?.let { it.container.translationX = -calendarPadding.toFloat() }
        targetWeekHolder = null
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

    fun getSelectedView(): View = targetHolder?.v ?: dateHolders[0].v

    private fun highlightCells(start: Int, end: Int) {
        val s = if(start < end) start else end
        val e = if(start < end) end else start
        dateHolders.forEachIndexed { index, holder ->
           if(index in s..e) {
               holder.v.background = AppTheme.hightlightCover
           }else {
               holder.v.background = AppTheme.blankDrawable
           }
        }
    }

    private fun clearHighlight() {
        dateHolders.forEachIndexed { index, holder ->
            holder.v.background = AppTheme.blankDrawable
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
            endDragTime = dateHolders[currentDragCell].time

            when(event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    startDragCell = currentDragCell
                    startDragTime = dateHolders[startDragCell].time
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