package com.hellowo.journey.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.hellowo.journey.AppDateFormat
import com.hellowo.journey.getDiffMonth
import com.hellowo.journey.getDiffYear
import com.hellowo.journey.l
import java.util.*

class CalendarPagerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    private val startMonthPosition = 1000
    private val calendarViewCount = 4

    private val viewPager = ViewPager(context)
    private val calendarViews = List(calendarViewCount) { CalendarView(context) }
    var onSelectedDate: ((Long, Int, Boolean, CalendarView) -> Unit)? = null

    private val tempCal = Calendar.getInstance()
    private var targetCalendarView : CalendarView = calendarViews[startMonthPosition % calendarViewCount]
    private var firstSelectDateFlag = false
    private var selectDateTime = Long.MIN_VALUE

    init {
        addView(viewPager)
        viewPager.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        viewPager.adapter = CalendarPagerAdapter()
        viewPager.setCurrentItem(startMonthPosition, false)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                selectedTargetCalendarView(calendarViews[position % calendarViewCount])
                if(selectDateTime == Long.MIN_VALUE) {
                    targetCalendarView.selectDate(targetCalendarView.startCellNum)
                }else {
                    targetCalendarView.draw(selectDateTime)
                    targetCalendarView.selectTime(selectDateTime)
                    selectDateTime = Long.MIN_VALUE
                }
                l("$position 페이지 선택됨 : ${AppDateFormat.ymdDate.format(targetCalendarView.targetCal.time)}")
            }
        })
    }

    private fun selectedTargetCalendarView(calendarView: CalendarView) {
        targetCalendarView.onSelectedDate = null
        targetCalendarView.unselectDate()
        targetCalendarView = calendarView
        targetCalendarView.onSelectedDate = { time, cellNum, isSameSeleted ->
            onSelectedDate?.invoke(time, cellNum, isSameSeleted, targetCalendarView)
        }
    }

    inner class CalendarPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val calendarView = calendarViews[position % calendarViewCount]
            tempCal.timeInMillis = System.currentTimeMillis()
            tempCal.set(Calendar.DATE, 1)
            tempCal.add(Calendar.MONTH, position - startMonthPosition)
            if(!firstSelectDateFlag && targetCalendarView == calendarView) {
                firstSelectDateFlag = true
                selectedTargetCalendarView(calendarView)
                calendarView.onDrawed = {
                    calendarView.selectDate(calendarView.todayCellNum)
                }
            }else {
                calendarView.onDrawed = null
            }
            calendarView.draw(tempCal.timeInMillis)

            l("instantiateItem "+(position % calendarViewCount)+"/"+(viewPager.currentItem % calendarViewCount))
            if(calendarView.parent == null) container.addView(calendarView)
            return calendarView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            l(""+(position % calendarViewCount)+"/"+(viewPager.currentItem % calendarViewCount))
            if(Math.abs((position % calendarViewCount) - (viewPager.currentItem % calendarViewCount)) == 2) {
                l("destroyItem")
                // 중앙에서 2개 벗어난 뷰만 제거
                container.removeView(`object` as View)
            }
        }
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
        return startMonthPosition + getDiffYear(today, time) * 12 + getDiffMonth(today, time)
    }

    fun redraw() { calendarViews.forEach { it.redraw() } }

    fun onDrag(event: DragEvent) {
        targetCalendarView.onDrag(event)
    }

    fun endDrag() { calendarViews.forEach { it.endDrag() } }
}