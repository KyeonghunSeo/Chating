package com.hellowo.chating.calendar

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import com.hellowo.chating.*
import com.hellowo.chating.model.ChatRoom
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.util.*

class CalendarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        val todayCal: Calendar = Calendar.getInstance()
        val tempCal: Calendar = Calendar.getInstance()
        const val maxCellNum = 42
        private val dateArea = dpToPx(40)
        val dateTextLayoutParams = FrameLayout.LayoutParams(dateArea, dateArea)
        const val dateTextSize = 15f
        const val animDur = 250L
    }

    // views
    private val scrollView = ScrollView(context)
    private val rootLy = FrameLayout(context)
    private val calendarLy = FrameLayout(context)
    private val weekLys = Array(6) { _ -> FrameLayout(context)}
    private val dateLys = Array(maxCellNum) { _ -> FrameLayout(context)}
    private val dateTexts = Array(maxCellNum) { _ -> TextView(context)}
    // view 관련
    // calendar 관련
    private val selectedCal = Calendar.getInstance()
    private var selectedCellNum = -1
    private var lastSelectAnimSet: AnimatorSet? = null
    private val column = 7
    private var rows = 0
    private var startPos = 0
    private var endPos = 0
    private var cellW = 0f
    private var cellH = 0f
    private val cellTimeMills = LongArray(maxCellNum) { _ -> Long.MIN_VALUE}
    var calendarStartTime = Long.MAX_VALUE
    var calendarEndTime = Long.MAX_VALUE
    var onDrawed: ((Calendar) -> Unit)? = null

    init {
        CalendarSkin.init(context)
        addView(scrollView)
        scrollView.addView(rootLy)
        rootLy.addView(calendarLy)

        scrollView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        rootLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)

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
                CalendarSkin.setDefaultDateTextSkin(dateText)
                setDateTextColor(cellNum)

                dateLy.addView(dateText)
                weekLy.addView(dateLy)
            }
            calendarLy.addView(weekLy)
        }

        viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        drawCalendar()
                    }
                })
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
        cellW = width.toFloat() / column
        cellH = height.toFloat() / rows

        calendarLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, height)

        tempCal.add(Calendar.DATE, -(startPos + 1))
        for(i in 0..5) {
            val weekLy = weekLys[i]
            weekLy.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, cellH.toInt())
            if(i < rows) {
                weekLy.visibility = View.VISIBLE
                for (j in 0..6){
                    tempCal.add(Calendar.DATE, 1)
                    val cellNum = i*7 + j
                    cellTimeMills[cellNum] = tempCal.timeInMillis
                    weekLy.translationY = cellNum / column * cellH
                    val dateLy = dateLys[cellNum]
                    dateLy.layoutParams = FrameLayout.LayoutParams(cellW.toInt(), MATCH_PARENT)
                    dateLy.translationX = cellNum % column * cellW
                    dateLy.setOnClickListener { onDateClick(cellNum) }
                    dateTexts[cellNum].text = tempCal.get(Calendar.DATE).toString()

                    if(isSameDay(tempCal, selectedCal)) { selectDate(cellNum, false) }

                    if(cellNum == 0) {
                        calendarStartTime = tempCal.timeInMillis
                    }else if(cellNum == rows * column - 1) {
                        setCalendarTime23(tempCal)
                        calendarEndTime = tempCal.timeInMillis
                    }
                }
            }else {
                weekLy.visibility = View.GONE
            }
        }

        TimeObjectManager.setTimeObjectListData(this)
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
        if(selectedCellNum >= 0) { unselectDate(selectedCellNum) }

        selectedCellNum = cellNum
        selectedCal.timeInMillis = cellTimeMills[cellNum]
        val dateText = dateTexts[selectedCellNum]
        setDateTextColor(cellNum)
        if(anim) {
            lastSelectAnimSet = AnimatorSet()
            lastSelectAnimSet?.let {
                it.playTogether(
                        ObjectAnimator.ofFloat(dateText, "translationY", 0f, (-dpToPx(10)).toFloat()),
                        ObjectAnimator.ofFloat(dateText, "scaleX", 1f, 2f),
                        ObjectAnimator.ofFloat(dateText, "scaleY", 1f, 2f))
                it.interpolator = FastOutSlowInInterpolator()
                it.duration = animDur
                it.start()
            }
        }else {
            dateText.scaleX = 2f
            dateText.scaleY = 2f
            dateText.translationY = (-dpToPx(10)).toFloat()
        }
    }

    private fun unselectDate(cellNum: Int) {
        selectedCellNum = -1
        lastSelectAnimSet?.cancel()
        val dateText = dateTexts[cellNum]
        setDateTextColor(cellNum)
        dateText.scaleX = 1f
        dateText.scaleY = 1f
        dateText.translationY = 0f
    }

    private fun setDateTextColor(cellNum: Int) {
        val dateText = dateTexts[cellNum]
        if(cellNum == selectedCellNum) {
            dateText.setTypeface(null, Typeface.BOLD)
            dateText.setTextColor(CalendarSkin.selectedDateColor)
        }else {
            dateText.setTypeface(null, Typeface.NORMAL)
            if(cellNum % column == 0) {
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
}