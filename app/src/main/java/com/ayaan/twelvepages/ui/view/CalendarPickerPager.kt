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
    var onSelectedDate: ((Long, Int, Int, Boolean, CalendarPicker) -> Unit)? = null
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

    private fun selectedTargetCalendarView(calendarView: CalendarPicker) {
        targetCalendarView.onSelectedDate = null
        targetCalendarView.onTop = null
        targetCalendarView.unselectDate()
        targetCalendarView = calendarView
        targetCalendarView.onSelectedDate = { time, cellNum, dateColor, isSameSeleted ->
            onSelectedDate?.invoke(time, cellNum, dateColor, isSameSeleted, targetCalendarView)
        }
        targetCalendarView.onTop = onTop
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
                calendarView.onDrawed = {
                    calendarView.postDelayed({calendarView.selectDate(calendarView.todayCellNum)}, 100)
                    calendarView.onDrawed = null
                }
            }else {
                calendarView.onDrawed = null
            }
            calendarView.draw(tempCal.timeInMillis)

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

    private fun getPagePosition(time: Long) : Int {
        val today = System.currentTimeMillis()
        return startPosition + getDiffYear(today, time) * 12 + getDiffMonth(today, time)
    }

    fun redraw() {
        calendarViews.forEach { it.redraw() }
    }
}