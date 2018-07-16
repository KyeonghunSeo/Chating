package com.hellowo.chating.calendar

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.hellowo.chating.*
import com.hellowo.chating.ui.view.SwipeScrollView
import java.util.*

class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private val todayCal: Calendar = Calendar.getInstance()
        private val tempCal: Calendar = Calendar.getInstance()
        private val topPadding = dpToPx(70)
        private val bottomPadding = dpToPx(50)
        const val maxCellNum = 42
        const val dateTextSize = 13f
        const val animDur = 250L
        const val columns = 7
    }

    private val scrollView = SwipeScrollView(context)
    private val rootLy = FrameLayout(context)
    val calendarLy = LinearLayout(context)
    val weekLys = Array(6) { _ -> FrameLayout(context)}
    val dateLys = Array(maxCellNum) { _ -> FrameLayout(context)}
    val dateTexts = Array(maxCellNum) { _ -> TextView(context)}

    val selectedCal = Calendar.getInstance()
    private var selectedCellNum = -1
    private var lastSelectAnimSet: AnimatorSet? = null
    private var lastUnSelectAnimSet: AnimatorSet? = null

    val cellTimeMills = LongArray(maxCellNum) { _ -> Long.MIN_VALUE}
    var calendarStartTime = Long.MAX_VALUE
    var calendarEndTime = Long.MAX_VALUE
    var onDrawed: ((Calendar) -> Unit)? = null
    val dateArea = dpToPx(40)
    var startPos = 0
    var endPos = 0
    var minCalendarHeight = 0
    var minWidth = 0f
    var minHeight = 0f
    var weekLyBottomPadding = dpToPx(10)
    var rows = 0

    init {
        CalendarSkin.init(this)
        createViews()
        setLayout()
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                drawCalendar()
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
        rootLy.setPadding(0, topPadding, 0 ,bottomPadding)
        calendarLy.orientation = LinearLayout.VERTICAL
        calendarLy.clipChildren = false

        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.clipChildren = false
            for (j in 0..6){
                val cellNum = i*7 + j
                val dateLy = dateLys[cellNum]
                dateLy.clipChildren = false
                dateLy.setBackgroundResource(AppRes.selectableItemBackground)

                val dateText = dateTexts[cellNum]
                setDefaultDateTextSkin(dateText)
                setDateTextColor(cellNum)

                dateLy.addView(dateText)
                weekLy.addView(dateLy)
            }
            calendarLy.addView(weekLy)
        }

        scrollView.onSwipeStateChanged = { state ->
            val animSet = AnimatorSet()
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.duration = 250
            when(state) {
                0, 1 -> {
                    animSet.playTogether(ObjectAnimator.ofFloat(scrollView, "translationX", scrollView.translationX, 0f))
                }
                2 -> {
                    animSet.playTogether(ObjectAnimator.ofFloat(scrollView, "translationX", scrollView.translationX, dpToPx(30).toFloat()))
                }
                3 -> {
                    animSet.playTogether(ObjectAnimator.ofFloat(scrollView, "translationX", scrollView.translationX, -dpToPx(30).toFloat()))
                }
            }
            animSet.start()
        }
    }

    private fun drawCalendar() {
        l("==========START drawCalendar=========")
        val t = System.currentTimeMillis()
        todayCal.timeInMillis = System.currentTimeMillis()
        tempCal.timeInMillis = selectedCal.timeInMillis
        tempCal.set(Calendar.DATE, 1)
        setCalendarTime0(tempCal)
        startPos = tempCal.get(Calendar.DAY_OF_WEEK) - 1
        endPos = startPos + tempCal.getActualMaximum(Calendar.DATE) - 1
        rows = (endPos + 1) / 7 + if ((endPos + 1) % 7 > 0) 1 else 0
        minCalendarHeight = height - topPadding
        minWidth = width.toFloat() / columns
        minHeight = minCalendarHeight.toFloat() / rows

        calendarLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, minCalendarHeight)
        tempCal.add(Calendar.DATE, -(startPos + 1))
        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, minHeight.toInt())
            if(i < rows) {
                weekLy.visibility = View.VISIBLE
                for (j in 0..6){
                    tempCal.add(Calendar.DATE, 1)
                    val cellNum = i*7 + j
                    cellTimeMills[cellNum] = tempCal.timeInMillis
                    val dateLy = dateLys[cellNum]
                    dateLy.layoutParams = FrameLayout.LayoutParams(minWidth.toInt(), MATCH_PARENT)
                    dateLy.translationX = cellNum % columns * minWidth
                    dateLy.setOnClickListener { onDateClick(cellNum) }
                    dateTexts[cellNum].text = tempCal.get(Calendar.DATE).toString()

                    if(isSameDay(tempCal, selectedCal)) { selectDate(cellNum, false) }

                    if(cellNum == 0) {
                        calendarStartTime = tempCal.timeInMillis
                    }else if(cellNum == rows * columns - 1) {
                        setCalendarTime23(tempCal)
                        calendarEndTime = tempCal.timeInMillis
                    }
                }
            }else {
                weekLy.visibility = View.GONE
            }
        }

        TimeObjectManager.setTimeObjectListAdapter(this)
        onDrawed?.invoke(selectedCal)

        l(""+ selectedCal.get(Calendar.YEAR) + "년" + (selectedCal.get(Calendar.MONTH) + 1) + "월" + selectedCal.get(Calendar.DATE) + "일")
        l("캘린더 높이 : " + height.toString())
        l("행 : $rows")
        l("걸린시간 : ${(System.currentTimeMillis() - t) / 1000f} 초")
        l("==========END drawCalendar=========")
    }

    private fun onDateClick(cellNum: Int) {
        l("날짜 : " + android.text.format.DateFormat.getDateFormat(context).format(cellTimeMills[cellNum]))
        tempCal.timeInMillis = cellTimeMills[cellNum]
        if(isSameDay(tempCal, selectedCal)) {

        }else {
            selectDate(cellNum,true)
        }
    }

    private fun selectDate(cellNum: Int, anim: Boolean) {
        if(selectedCellNum >= 0) { unselectDate(selectedCellNum, anim) }

        selectedCellNum = cellNum
        selectedCal.timeInMillis = cellTimeMills[cellNum]
        val dateText = dateTexts[selectedCellNum]
        setDateTextColor(cellNum)
        if(anim) {
            lastSelectAnimSet?.cancel()
            lastSelectAnimSet = AnimatorSet()
            lastSelectAnimSet?.let {
                it.playTogether(
                        ObjectAnimator.ofFloat(dateText, "scaleX", 1f, 2f),
                        ObjectAnimator.ofFloat(dateText, "scaleY", 1f, 2f))
                it.interpolator = FastOutSlowInInterpolator()
                it.duration = animDur
                it.start()
            }
        }else {
            dateText.scaleX = 2f
            dateText.scaleY = 2f
        }
    }

    private fun unselectDate(cellNum: Int, anim: Boolean) {
        selectedCellNum = -1
        val dateText = dateTexts[cellNum]
        setDateTextColor(cellNum)

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
                        ObjectAnimator.ofFloat(dateText, "scaleX", 2f, 1f),
                        ObjectAnimator.ofFloat(dateText, "scaleY", 2f, 1f))
                it.interpolator = FastOutSlowInInterpolator()
                it.duration = animDur
                it.start()
            }
        }else {
            dateText.scaleX = 1f
            dateText.scaleY = 1f
        }
    }

    private fun setDateTextColor(cellNum: Int) {
        val dateText = dateTexts[cellNum]
        if(cellNum == selectedCellNum) {
            dateText.setTypeface(null, Typeface.BOLD)
            dateText.setTextColor(CalendarSkin.selectedDateColor)
        }else {
            dateText.setTypeface(null, Typeface.NORMAL)
            if(cellNum % columns == 0) {
                dateText.setTextColor(CalendarSkin.holiDateColor)
            }else {
                dateText.setTextColor(CalendarSkin.dateColor)
            }
        }
    }

    fun moveMonth(offset: Int) {
        selectedCal.add(Calendar.MONTH, offset)
        drawCalendar()

        /*
        val animSet = AnimatorSet()selectedCal
        if(offset < 0) {
            animSet.playTogether(
                    ObjectAnimator.ofFloat(scrollView, "translationY", 0 - height, 0f),
                    ObjectAnimator.ofFloat(scrollView, "alpha", 0f, 1f))
        }else {
            animSet.playTogether(
                    ObjectAnimator.ofFloat(scrollView, "translationY", height + 0, 0f),
                    ObjectAnimator.ofFloat(scrollView, "alpha", 0f, 1f))
        }
        animSet.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
            }
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
        })
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.duration = 2500
        animSet.start()
        */
    }

    fun getSelectedCalendar() = selectedCal

    fun setDefaultDateTextSkin(textView: TextView) {
        val dateTextLayoutParams = FrameLayout.LayoutParams(dateArea, dateArea)
        dateTextLayoutParams.gravity = Gravity.CENTER_HORIZONTAL
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, CalendarView.dateTextSize)
        textView.setTypeface(null, Typeface.BOLD)
        textView.gravity = Gravity.CENTER
        textView.layoutParams = dateTextLayoutParams
    }

    fun startEditMode() {

    }
}