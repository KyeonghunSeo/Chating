package com.hellowo.chating.calendar.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.view.animation.OvershootInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.hellowo.chating.*
import com.hellowo.chating.ui.view.SwipeScrollView
import java.util.*
import android.widget.*
import com.hellowo.chating.calendar.model.CalendarSkin
import com.hellowo.chating.calendar.TimeObjectManager

class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        const val maxCellNum = 42
        const val dateTextSize = 13f
        const val animDur = 250L
        const val columns = 7
        val dateSize = dpToPx(20)
        val dateArea = dpToPx(30)
        val weekLyBottomPadding = dpToPx(20)
    }

    private val scrollView = SwipeScrollView(context)
    private val rootLy = FrameLayout(context)
    val calendarLy = LinearLayout(context)
    val monthBottomLy = FrameLayout(context)
    val weekLys = Array(6) { _ -> FrameLayout(context)}
    val dateLys = Array(maxCellNum) { _ -> FrameLayout(context)}
    val dateTexts = Array(maxCellNum) { _ -> TextView(context)}
    val selectedBar = LayoutInflater.from(context).inflate(R.layout.view_selected_bar, null, false)

    private var lastSelectAnimSet: AnimatorSet? = null
    private var lastUnSelectAnimSet: AnimatorSet? = null

    private val todayCal: Calendar = Calendar.getInstance()
    private val tempCal: Calendar = Calendar.getInstance()
    private val monthCal: Calendar = Calendar.getInstance()
    private val dow = context.resources.getStringArray(R.array.day_of_weeks)

    val selectedCal = Calendar.getInstance()
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

    init {
        CalendarSkin.init(this)
        createViews()
        setLayout()
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                drawCalendar(System.currentTimeMillis(), true)
            }
        })
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
        rootLy.setPadding(0, 0, 0 , dpToPx(40))
        calendarLy.orientation = LinearLayout.VERTICAL
        calendarLy.clipChildren = false
        selectedBar.layoutParams = FrameLayout.LayoutParams(dateArea, weekLyBottomPadding).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            topMargin = -weekLyBottomPadding
        }

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.clipChildren = false
            for (j in 0..6){
                val cellNum = i*7 + j
                val dateLy = dateLys[cellNum]
                dateLy.clipChildren = false
                dateLy.setBackgroundResource(AppRes.selectableItemBackground)
                dateLy.setOnClickListener { onDateClick(cellNum) }

                val dateText = dateTexts[cellNum]

                dateLy.addView(dateText)
                weekLy.addView(dateLy)
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
        monthCal.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), todayCal.get(Calendar.DATE))
        setCalendarTime0(tempCal)

        startCellNum = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        endCellNum = startCellNum + tempCal.getActualMaximum(Calendar.DATE) - 1
        todayCellNum = -1
        rows = (endCellNum + 1) / 7 + if ((endCellNum + 1) % 7 > 0) 1 else 0
        minCalendarHeight = height
        minWidth = width.toFloat() / columns
        minHeight = minCalendarHeight.toFloat() / rows

        calendarLy.layoutParams.height = minCalendarHeight
        calendarLy.requestLayout()

        tempCal.add(Calendar.DATE, -startCellNum)

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, minHeight.toInt())
            if(i < rows) {
                weekLy.visibility = View.VISIBLE
                for (j in 0..6){
                    val cellNum = i*7 + j
                    cellTimeMills[cellNum] = tempCal.timeInMillis

                    if(isSameDay(tempCal, selectedCal)) { TimeObjectManager.postSelectDate(cellNum) }
                    if(isSameDay(tempCal, todayCal)) { todayCellNum = cellNum }

                    val dateLy = dateLys[cellNum]
                    dateLy.layoutParams = FrameLayout.LayoutParams(minWidth.toInt(), MATCH_PARENT)
                    dateLy.translationX = cellNum % columns * minWidth

                    val dateText = dateTexts[cellNum]
                    if(isInit) { setDefaultDateTextSkin(dateText) }
                    dateText.text = tempCal.get(Calendar.DATE).toString()
                    dateText.alpha = if(cellNum in startCellNum..endCellNum) 1f else 0.0f
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
            }
        }

        TimeObjectManager.setTimeObjectCalendarAdapter(this)
        onDrawed?.invoke(monthCal)

        l("${monthCal.get(Calendar.YEAR)}년 ${(monthCal.get(Calendar.MONTH) + 1)}월")
        l("캘린더 높이 : $minCalendarHeight")
        l("행 : $rows")
        l("걸린시간 : ${(System.currentTimeMillis() - t) / 1000f} 초")
        l("==========END drawCalendar=========")
    }

    private fun getDateTextColor(cellNum: Int) : Int {
        return if(cellNum == todayCellNum) {
            if(cellNum % columns == 0) {
                CalendarSkin.holiDateColor
            }else {
                CalendarSkin.dateColor
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
        l("선택한 날짜 : " + android.text.format.DateFormat.getDateFormat(context).format(cellTimeMills[cellNum]))
        tempCal.timeInMillis = cellTimeMills[cellNum]
        selectDate(cellNum,true, isSameDay(tempCal, selectedCal))
    }

    private fun unselectDate(cellNum: Int, anim: Boolean) {
        selectedCellNum = -1
        offViewEffect(cellNum)
        val dateText = dateTexts[cellNum]
        selectedBar.parent?.let { (it as FrameLayout).removeView(selectedBar) }

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
                    }
                    override fun onAnimationStart(p0: Animator?) {}
                })
                it.playTogether(
                        ObjectAnimator.ofFloat(dateText, "scaleX", 1.5f, 1f),
                        ObjectAnimator.ofFloat(dateText, "scaleY", 1.5f, 1f))
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
            if(selectedCellNum >= 0) { unselectDate(selectedCellNum, anim) }
            selectedCellNum = cellNum
            selectedCal.timeInMillis = cellTimeMills[cellNum]
            val dateText = dateTexts[selectedCellNum]
            setSelectedBar(cellNum)

            if(anim) {
                lastSelectAnimSet?.cancel()
                lastSelectAnimSet = AnimatorSet()
                lastSelectAnimSet?.let {
                    it.playTogether(
                            ObjectAnimator.ofFloat(dateText, "scaleX", 1f, 1.5f),
                            ObjectAnimator.ofFloat(dateText, "scaleY", 1f, 1.5f),
                            ObjectAnimator.ofFloat(selectedBar.findViewById<TextView>(R.id.dowBar), "scaleX", 0f, 1f),
                            ObjectAnimator.ofFloat(selectedBar.findViewById<TextView>(R.id.dowText), "scaleY", 0f, 1f))
                    it.interpolator = FastOutSlowInInterpolator()
                    it.duration = animDur
                    it.start()
                }
            }else {
                dateText.scaleX = 1.5f
                dateText.scaleY = 1.5f
            }

            scrollView.post{ scrollView.smoothScrollTo(0, weekLys[cellNum / columns].top) }
            onViewEffect(cellNum)
        }else {

        }
        onSelected?.invoke(cellTimeMills[selectedCellNum], selectedCellNum, showDayView)
    }

    private fun setSelectedBar(cellNum: Int){
        val color = getDateTextColor(cellNum)
        dateLys[cellNum].addView(selectedBar)
        selectedBar.findViewById<TextView>(R.id.dowText).let {
            it.text = dow[cellNum % columns]
            it.setTextColor(color)
        }
        selectedBar.findViewById<View>(R.id.dowBar).setBackgroundColor(color)
    }

    private fun onViewEffect(cellNum: Int) {
        TimeObjectManager.timeObjectCalendarAdapter?.getViews(cellNum)?.let {
            it.forEach { view ->
                view.ellipsize = TextUtils.TruncateAt.MARQUEE
                view.marqueeRepeatLimit = -1
                view.postDelayed({
                    view.isSelected = true
                }, 100)
            }
        }
    }

    private fun offViewEffect(cellNum: Int) {
        TimeObjectManager.timeObjectCalendarAdapter?.getViews(cellNum)?.let {
            it.forEach { view ->
                view.ellipsize = null
                view.isSelected = false
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
        if(selectedCellNum >= 0) { unselectDate(selectedCellNum, false) }
        scrollView.scrollTo(0, 0)
        drawCalendar(monthCal.timeInMillis, false)
        startPagingEffectAnimation(offset, scrollView, null)
    }

    private fun setDefaultDateTextSkin(textView: TextView) {
        val lp = FrameLayout.LayoutParams(dateSize, dateSize)
        lp.topMargin = (dateArea - dateSize) / 2
        lp.leftMargin = (minWidth / 2 - dateSize / 2).toInt()
        textView.layoutParams = lp
        textView.gravity = Gravity.CENTER
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, dateTextSize)
        //textView.typeface = CalendarSkin.dateFont
        textView.includeFontPadding = false
    }

    fun getSelectedView(): View = dateLys[selectedCellNum]
}