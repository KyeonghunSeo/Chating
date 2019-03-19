package com.hellowo.journey.ui.view

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
import android.view.DragEvent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import android.widget.LinearLayout.HORIZONTAL
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.hellowo.journey.*
import com.hellowo.journey.adapter.TimeObjectCalendarAdapter
import com.hellowo.journey.listener.MainDragAndDropListener
import com.hellowo.journey.manager.CalendarManager
import com.hellowo.journey.manager.HolidayManager
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.adapter.util.KoreanLunarCalendar
import io.realm.RealmResults
import java.util.*


class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        const val maxCellNum = 42
        const val animDur = 250L
        const val columns = 7
        const val selectedDateScale = 1f
        val todayCal: Calendar = Calendar.getInstance()
        val dragStartYPos = dpToPx(58f)
        val dateArea = dpToPx(35f)
        val weekLyBottomPadding = dpToPx(10)
        val calendarPadding = dpToPx(10)
        val autoScrollThreshold = dpToPx(70)
        val autoScrollOffset = dpToPx(5)
        val lineWidth = dpToPx(0.5f)
    }

    private val scrollView = ScrollView(context)
    val calendarLy = LinearLayout(context)
    val weekLys = Array(6) { _ -> FrameLayout(context)}
    private val columnDividers = Array(8 * 6) { _ -> View(context)}
    private val rowDividers = Array(6) { _ -> View(context)}
    private val dateLys = Array(6) { _ -> LinearLayout(context)}
    val dateCells = Array(maxCellNum) { _ -> FrameLayout(context)}
    private val dateHeaders = Array(maxCellNum) { _ ->
        DateHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.view_selected_bar, null, false))}
    //private val weekViews = Array(6) { _ ->
    //    WeekViewHolder(LayoutInflater.from(context).inflate(R.layout.view_weekly_side, null, false))}
    private val nextMonthHintView = LayoutInflater.from(context).inflate(R.layout.view_next_month_hint, null, false)
    private val fakeImageView = ImageView(context)
    private val lunarCalendar = KoreanLunarCalendar.getInstance()

    inner class WeekViewHolder(val container: View) {
        val contentLy: FrameLayout = container.findViewById(R.id.contentLy)
        val weekNumText: TextView = container.findViewById(R.id.weekNumText)
        init {
            weekNumText.typeface = CalendarManager.selectFont
            contentLy.visibility = View.GONE
        }
    }

    inner class DateHeaderViewHolder(val container: View) {
        val dateText: TextView = container.findViewById(R.id.dateText)
        val bar: FrameLayout = container.findViewById(R.id.bar)
        val dowText: TextView = container.findViewById(R.id.dowText)
        val holiText: TextView = container.findViewById(R.id.holiText)
        val dateLy: LinearLayout = container.findViewById(R.id.dateLy)
        val todayIndi: ImageView = container.findViewById(R.id.todayIndi)
        init {
            dateText.typeface = CalendarManager.dateFont
            dowText.typeface = CalendarManager.dateFont
            holiText.typeface = CalendarManager.dateFont
            bar.scaleX = 0f
            dowText.visibility = View.GONE
        }
    }

    private var lastSelectDateAnimSet: AnimatorSet? = null
    private var lastUnSelectDateAnimSet: AnimatorSet? = null
    private var lastSelectWeekAnimSet: AnimatorSet? = null

    private val tempCal: Calendar = Calendar.getInstance()
    private val monthCal: Calendar = Calendar.getInstance()

    val targetCal: Calendar = Calendar.getInstance()
    var targetCellNum = -1
    var selectCellNum = -1
    var todayCellNum = -1
    val cellTimeMills = LongArray(maxCellNum) { _ -> Long.MIN_VALUE}
    val cellHoliText = Array(maxCellNum) { _ -> ""}
    var calendarStartTime = Long.MAX_VALUE
    var calendarEndTime = Long.MAX_VALUE
    var onDrawed: ((Calendar) -> Unit)? = null
    var onSelectedDate: ((Long, Int, Boolean) -> Unit)? = null
    var startCellNum = 0
    var endCellNum = 0
    var minCalendarHeight = 0f
    var minWidth = 0f
    var minHeight = 0f
    var rows = 0
    var todayStatus = 0
    var sundayPos = 0
    var saturdayPos = 6

    private var autoScroll = true

    init {
        createViews()
        setLayout()
    }

    private fun createViews() {
        addView(scrollView)
        addView(fakeImageView)
        addView(nextMonthHintView)
        scrollView.addView(calendarLy)
    }

    private fun setLayout() {
        scrollView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        scrollView.isVerticalScrollBarEnabled = false
        calendarLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        calendarLy.setPadding(calendarPadding, 0, calendarPadding, 0)
        calendarLy.orientation = LinearLayout.VERTICAL
        calendarLy.clipChildren = false
        nextMonthHintView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        nextMonthHintView.alpha = 0f
        fakeImageView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        rowDividers.forEachIndexed { index, view ->
            view.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, lineWidth.toInt())
            view.setBackgroundColor(AppTheme.disableText)
        }

        columnDividers.forEachIndexed { index, view ->
            view.layoutParams = FrameLayout.LayoutParams(lineWidth.toInt(), MATCH_PARENT)
            view.setBackgroundColor(AppTheme.backgroundColor)
        }

        for(i in 0..5) {
            val weekLy = weekLys[i]
            val dateLy = dateLys[i]
            dateLy.clipChildren = false
            dateLy.orientation = HORIZONTAL
            dateLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            weekLy.addView(rowDividers[i])
            if(i == 0) {
                rowDividers[i].visibility = View.GONE
            }
            for (j in 0..7){
                weekLy.addView(columnDividers[i*8 + j])
            }
            weekLy.addView(dateLy)

            for (j in 0..6){
                val cellNum = i*7 + j
                val dateCell = dateCells[cellNum]
                //dateCell.setBackgroundResource(AppDateFormat.selectableItemBackground)
                dateCell.setOnClickListener { onDateClick(cellNum) }
                dateCell.setOnLongClickListener {
                    if(selectCellNum != cellNum) onDateClick(cellNum)
                    MainDragAndDropListener.start(it, MainDragAndDropListener.DragMode.INSERT)
                    return@setOnLongClickListener true
                }
                dateCell.layoutParams = LinearLayout.LayoutParams(0, MATCH_PARENT, 1f)
                dateCell.addView(dateHeaders[cellNum].container)
                dateLy.addView(dateCell)
            }
            //calendarLy.addView(weekViews[i].container)
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
        minCalendarHeight = height.toFloat()
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

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.layoutParams.height = minHeight.toInt()
            if(i < rows) {
                weekLy.visibility = View.VISIBLE
                for (j in 0..6){
                    val cellNum = i*7 + j
                    cellTimeMills[cellNum] = tempCal.timeInMillis
                    lunarCalendar.setSolarDate(tempCal.get(Calendar.YEAR),
                            tempCal.get(Calendar.MONTH) + 1,
                            tempCal.get(Calendar.DATE))

                    val holi = HolidayManager.getHoliday(
                            String.format("%02d%02d", tempCal.get(Calendar.MONTH) + 1, tempCal.get(Calendar.DATE)),
                            lunarCalendar.lunarKey)
                    val holiText = dateHeaders[cellNum].holiText
                    val dateText = dateHeaders[cellNum].dateText
                    val dowText = dateHeaders[cellNum].dowText
                    val todayIndi = dateHeaders[cellNum].todayIndi
                    val bar = dateHeaders[cellNum].bar
                    val color = getDateTextColor(cellNum, !holi.isNullOrEmpty())
                    val alpha = if(cellNum in startCellNum..endCellNum) 1f else AppStatus.outsideMonthAlpha
                    dateText.text = String.format("%02d", tempCal.get(Calendar.DATE))
                    dateText.alpha = alpha
                    dateText.setTextColor(color)
                    holiText.setTextColor(color)
                    dowText.setTextColor(color)
                    bar.setBackgroundColor(color)
                    todayIndi.setColorFilter(color)

                    cellHoliText[cellNum] = if(!holi.isNullOrEmpty()) holi!!
                    else if(AppStatus.isLunarDisplay
                            && (lunarCalendar.lunarDay == 1 || lunarCalendar.lunarDay == 10 || lunarCalendar.lunarDay == 20))
                        lunarCalendar.lunarSimpleFormat
                    else ""
                    holiText.text = cellHoliText[cellNum]
                    holiText.alpha = alpha

                    if(isSameDay(tempCal, targetCal)) {
                        targetCellNum = cellNum
                    }

                    if(isSameDay(tempCal, todayCal)) {
                        todayCellNum = cellNum
                        todayIndi.visibility = View.VISIBLE
                        //dateLy.setBackgroundResource(R.drawable.grey_rect_fill_radius_2)
                    }else {
                        todayIndi.visibility = View.GONE
                    }

                    if(cellNum == 0) {
                        calendarStartTime = tempCal.timeInMillis
                    }else if(cellNum == rows * columns - 1) {
                        setCalendarTime23(tempCal)
                        calendarEndTime = tempCal.timeInMillis
                    }
                    tempCal.add(Calendar.DATE, 1)
                    columnDividers[i*8 + j].translationX = minWidth * j
                }
                columnDividers[i*8 + 7].translationX = minWidth * 7 - lineWidth
            }else {
                weekLy.visibility = View.GONE
            }
        }

        //if(SyncUser.current() != null) TimeObjectManager.setTimeObjectCalendarAdapter(this)
        setTimeObjectCalendarAdapter()
        scrollView.post { scrollView.scrollTo(0, 0) }
        onDrawed?.invoke(monthCal)
        l("${AppDateFormat.mDate.format(targetCal.time)} 캘린더 그리기 : ${(System.currentTimeMillis() - t) / 1000f} 초")
    }

    fun getDateTextColor(cellNum: Int, isHoli: Boolean) : Int {
        return if(isHoli || cellNum % columns == sundayPos) {
            CalendarManager.sundayColor
        }else if(cellNum % columns == saturdayPos) {
            CalendarManager.saturdayColor
        }else {
            CalendarManager.dateColor
        }
    }

    private fun onDateClick(cellNum: Int) {
        if(AppStatus.outsideMonthAlpha > 0f || cellNum in startCellNum..endCellNum) {
            tempCal.timeInMillis = cellTimeMills[cellNum]
            selectDate(cellNum, selectCellNum == cellNum)
        }
    }

    fun unselectDate() {
        if(selectCellNum >= 0) unselectDate(selectCellNum)
    }

    fun unselectDate(cellNum: Int) {
        selectCellNum = -1
        val bar = dateHeaders[cellNum].bar
        val dowText = dateHeaders[cellNum].dowText
        val dateText = dateHeaders[cellNum].dateText
        val holiText = dateHeaders[cellNum].holiText
        val alpha = if(cellNum in startCellNum..endCellNum) 1f else AppStatus.outsideMonthAlpha

        dateText.typeface = CalendarManager.dateFont
        dowText.typeface = CalendarManager.dateFont
        holiText.typeface = CalendarManager.dateFont
        dateText.alpha = alpha
        dowText.visibility = View.GONE
        holiText.text = cellHoliText[cellNum]

        offViewEffect(cellNum)
        lastUnSelectDateAnimSet?.cancel()
        lastUnSelectDateAnimSet = AnimatorSet()
        lastUnSelectDateAnimSet?.let {
            it.addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationCancel(animation: Animator?) { restoreDateHeader(dateHeaders[cellNum]) } })
            it.playTogether(
                    ObjectAnimator.ofFloat(bar, "scaleX", 1f, 0f),
                    ObjectAnimator.ofFloat(dateHeaders[cellNum].dateLy, "scaleX", selectedDateScale, 1f),
                    ObjectAnimator.ofFloat(dateHeaders[cellNum].dateLy, "scaleY", selectedDateScale, 1f))
            it.interpolator = FastOutSlowInInterpolator()
            it.duration = animDur
            it.start()
        }
    }

    var selectedWeek = 0
    var selectedWeekIndex = -1

    fun selectTime(time: Long) {
        autoScroll = true
        selectDate(((time - calendarStartTime) / DAY_MILL).toInt(), false)
    }

    fun selectDate(cellNum: Int) {
        autoScroll = true
        selectDate(cellNum, false)
    }

    fun selectDate(cellNum: Int, showDayView: Boolean) {
        if(!showDayView) {
            l("날짜선택 : ${AppDateFormat.ymdDate.format(Date(cellTimeMills[cellNum]))}")
            targetCal.timeInMillis = cellTimeMills[cellNum]
            /*
            val week = "${targetCal.get(Calendar.YEAR)}${targetCal.get(Calendar.WEEK_OF_YEAR)}".toInt()
            val weekIndex = cellNum / columns
            var isChangeWeek = false
            if(week != selectedWeek || weekIndex != selectedWeekIndex) {
                l("Week 선택 : $week")
                isChangeWeek = true

                if(selectedWeekIndex != -1 && weekIndex != selectedWeekIndex) {
                    weekViews[selectedWeekIndex].contentLy.visibility = View.GONE
                }

                selectedWeekIndex = weekIndex
                selectedWeek = week
                val weekView = weekViews[selectedWeekIndex]
                weekView.contentLy.visibility = View.VISIBLE
                weekView.weekNumText.text =
                        String.format(context.getString(R.string.weekNum), targetCal.get(Calendar.WEEK_OF_YEAR).toString())

                lastSelectWeekAnimSet?.cancel()
                lastSelectWeekAnimSet = AnimatorSet()
                lastSelectWeekAnimSet?.let {
                    it.addListener(object : AnimatorListenerAdapter(){
                        override fun onAnimationCancel(animation: Animator?) { weekView.container.alpha = 0f
                        }
                    })
                    it.playTogether(ObjectAnimator.ofFloat(weekView.container, "alpha", 0f, 1f))
                    it.interpolator = FastOutSlowInInterpolator()
                    it.duration = animDur
                    it.start()
                }
            }
*/
            if(selectCellNum >= 0) unselectDate(selectCellNum)

            selectCellNum = cellNum
            targetCellNum = cellNum

            val bar = dateHeaders[cellNum].bar
            val dateText = dateHeaders[cellNum].dateText
            val dowText = dateHeaders[cellNum].dowText
            val holiText = dateHeaders[cellNum].holiText

            dateText.typeface = CalendarManager.selectFont
            dowText.typeface = CalendarManager.selectFont
            holiText.typeface = CalendarManager.selectFont
            dateText.alpha = 1f
            dowText.text = AppDateFormat.simpleDow.format(targetCal.time)
            dowText.visibility = View.VISIBLE

            if(AppStatus.isLunarDisplay && holiText.text.isEmpty()) {
                lunarCalendar.setSolarDate(targetCal.get(Calendar.YEAR),
                        targetCal.get(Calendar.MONTH) + 1, targetCal.get(Calendar.DATE))
                holiText.text = lunarCalendar.lunarSimpleFormat
            }

            lastSelectDateAnimSet?.cancel()
            lastSelectDateAnimSet = AnimatorSet()
            lastSelectDateAnimSet?.let {
                it.addListener(object : AnimatorListenerAdapter(){
                    override fun onAnimationCancel(animation: Animator?) { restoreDateHeader(dateHeaders[cellNum]) } })
                it.playTogether(
                        ObjectAnimator.ofFloat(bar, "scaleX", 0f, 1f),
                        ObjectAnimator.ofFloat(dateHeaders[cellNum].dateLy, "scaleX", 1f, selectedDateScale),
                        ObjectAnimator.ofFloat(dateHeaders[cellNum].dateLy, "scaleY", 1f, selectedDateScale))
                it.interpolator = FastOutSlowInInterpolator()
                it.duration = animDur
                it.start()
            }

            if(autoScroll /*|| isChangeWeek*/) {
                autoScroll = false
                scrollView.post { scrollView.smoothScrollTo(0, weekLys[cellNum / columns].top) }
            }
            onViewEffect(cellNum)
        }else {
            selectCellNum = cellNum
            targetCellNum = cellNum
        }
        todayStatus = getDiffToday(targetCal)
        onSelectedDate?.invoke(cellTimeMills[selectCellNum], selectCellNum, showDayView)
    }

    private fun restoreDateHeader(holder: DateHeaderViewHolder) {
        holder.bar.scaleX = 0f
        holder.dateLy.scaleX = 1f
        holder.dateLy.scaleY = 1f
    }

    private fun onViewEffect(cellNum: Int) {
        timeObjectCalendarAdapter.getViews(cellNum).let {
            it.forEach { view ->
                if(!view.timeObject.inCalendar) {
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
        timeObjectCalendarAdapter.getViews(cellNum).let {
            it.forEach { view ->
                if(!view.timeObject.inCalendar) {
                    view.ellipsize = null
                    view.isSelected = false
                }
            }
        }
    }

    fun moveDate(time: Long, isAutoScroll: Boolean) {
        targetCal.timeInMillis = time
        autoScroll = isAutoScroll
        drawCalendar(targetCal.timeInMillis)
    }

    fun moveMonth(offset: Int) {
        fakeImageView.setImageBitmap(makeViewToBitmap(this))
        fakeImageView.visibility = View.VISIBLE

        monthCal.add(Calendar.MONTH, offset)
        targetCal.timeInMillis = monthCal.timeInMillis
        if(selectCellNum >= 0) unselectDate(selectCellNum)
        autoScroll = true
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
        animSet.duration = 300
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //l("onAttachedToWindow : ${AppDateFormat.ymdDate.format(monthCal.time)}")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        //l("onDetachedFromWindow : ${AppDateFormat.ymdDate.format(monthCal.time)}")
    }

    private var timeObjectList: RealmResults<TimeObject>? = null
    private var withAnim = false
    private val timeObjectCalendarAdapter = TimeObjectCalendarAdapter(this)
    var lastUpdatedItem: TimeObject? = null

    private fun setTimeObjectCalendarAdapter() {
        withAnim = false
        timeObjectList?.removeAllChangeListeners()
        timeObjectList = TimeObjectManager.getTimeObjectList(calendarStartTime, calendarEndTime).apply {
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
                    l("${AppDateFormat.mDate.format(targetCal.time)} 오브젝트 그리기 : 데이터 ${result.size} 개 / ${(System.currentTimeMillis() - t) / 1000f} 초")
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
        var cellX = ((event.x - calendarPadding) / minWidth).toInt()
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