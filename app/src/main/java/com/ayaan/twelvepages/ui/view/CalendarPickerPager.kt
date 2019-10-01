package com.ayaan.twelvepages.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ayaan.twelvepages.getDiffMonth
import com.ayaan.twelvepages.getDiffYear
import com.ayaan.twelvepages.ui.view.base.PagingControlableViewPager
import java.util.*

class CalendarPickerPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    private val startPosition = 1000
    private val viewCount = 3
    private val viewPager = PagingControlableViewPager(context)
    private val calendarViews = List(viewCount) { CalendarPicker(context) }
    var onTargetedDate: ((Long) -> Unit)? = null
    var onSelectedDate: ((Unit) -> Unit)? = null
    var onTop: ((Boolean, Boolean) -> Unit)? = null

    private val tempCal = Calendar.getInstance()
    private var targetCalendarView : CalendarPicker = calendarViews[startPosition % viewCount]
    private var firstSelectDateFlag = false
    private var selectDateTime = Long.MIN_VALUE

    init {
        addView(viewPager)
        viewPager.setPagingEnabled(true)
        viewPager.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        viewPager.adapter = CalendarPagerAdapter()
        viewPager.setCurrentItem(startPosition, false)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                selectedTargetCalendarView(calendarViews[position % viewCount])
                if(selectDateTime == Long.MIN_VALUE) { // 스와이프로 선택된 경우
                    targetCalendarView.selectDate()
                }else { // 날짜 이동 함수로 선택된 경우
                    targetCalendarView.draw(selectDateTime)
                    targetCalendarView.selectTime(selectDateTime)
                    selectDateTime = Long.MIN_VALUE
                }
            }
        })
    }

    private lateinit var startCal: Calendar
    private lateinit var endCal: Calendar
    private var color: Int = 0
    var mode = 0

    fun setStartEndCalendar(sCal: Calendar, eCal: Calendar) {
        startCal = sCal
        endCal = eCal
    }

    fun setColor(c: Int) {
        color = c
    }

    private fun selectedTargetCalendarView(calendarView: CalendarPicker) {
        targetCalendarView.onSelectedDate = null
        targetCalendarView.unselectDate()
        targetCalendarView = calendarView
        targetCalendarView.onSelectedDate = { time ->
            drawRange()
            onTargetedDate?.invoke(time)
        }
        targetCalendarView.onClicked = { y, m, d ->
            if(mode == 0) {
                startCal.set(y, m, d)
                endCal.set(y, m, d)
                mode = 1
            }else {
                endCal.set(y, m, d)
                if(endCal < startCal) {
                    startCal.set(y, m, d)
                }else {
                    mode = 0
                }
            }
            drawRange()
            onSelectedDate?.invoke(Unit)
        }
    }

    inner class CalendarPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val calendarView = calendarViews[position % viewCount]
            tempCal.timeInMillis = System.currentTimeMillis()
            tempCal.set(Calendar.DATE, 1)
            tempCal.add(Calendar.MONTH, position - startPosition)
            if(!firstSelectDateFlag && targetCalendarView == calendarView) {
                firstSelectDateFlag = true
                selectedTargetCalendarView(calendarView)
            }else {
                calendarView.onDrawed = null
            }
            calendarView.draw(tempCal.timeInMillis)
            calendarView.drawRange(color, startCal.timeInMillis, endCal.timeInMillis)

            if(calendarView.parent == null) container.addView(calendarView)
            return calendarView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount() = 2000
    }

    fun selectDate(time: Long) {
        val pagePos = getPagePosition(time)
        if(viewPager.currentItem == pagePos) {
            targetCalendarView.selectTime(time)
        }else {
            selectDateTime = time
            viewPager.setCurrentItem(pagePos, false)
        }
    }

    fun moveMonth(offset: Int) {
        viewPager.setCurrentItem(viewPager.currentItem + offset, true)
    }

    private fun getPagePosition(time: Long) : Int {
        val today = System.currentTimeMillis()
        return startPosition + getDiffYear(today, time) * 12 + getDiffMonth(today, time)
    }

    private fun drawRange() {
        calendarViews.forEach { it.drawRange(color, startCal.timeInMillis, endCal.timeInMillis) }
    }
}