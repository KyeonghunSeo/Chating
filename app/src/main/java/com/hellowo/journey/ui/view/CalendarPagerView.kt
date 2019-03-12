package com.hellowo.journey.ui.view

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.hellowo.journey.*
import java.util.*

class CalendarPagerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    private val startMonthPosition = 1000

    private val viewPager = ViewPager(context)
    private val calendarViews = List(4) { CalendarView(context) }
    var onSelectedDate: ((Long, Int, Boolean, CalendarView) -> Unit)? = null

    private val tempCal = Calendar.getInstance()
    private var targetCalendarView : CalendarView = calendarViews[startMonthPosition % 4]
    private var firstSelectDateFlag = false

    init {
        addView(viewPager)
        viewPager.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        viewPager.adapter = CalendarPagerAdapter()
        viewPager.setCurrentItem(startMonthPosition, false)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                selectedTargetCalendarView(calendarViews[position % 4])
                targetCalendarView.selectDate(targetCalendarView.startCellNum)
                l("페이지 선택됨:  ${AppDateFormat.ymdDate.format(targetCalendarView.targetCal.time)}")
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
            val calendarView = calendarViews[position % 4]
            tempCal.timeInMillis = System.currentTimeMillis()
            tempCal.set(Calendar.DATE, 1)
            tempCal.add(Calendar.MONTH, position - startMonthPosition)
            calendarView.reserveDraw(tempCal.timeInMillis)
            if(!firstSelectDateFlag && targetCalendarView == calendarView) {
                firstSelectDateFlag = true
                selectedTargetCalendarView(calendarView)
                calendarView.onDrawed = {
                    calendarView.selectDate(calendarView.todayCellNum)
                }
            }else {
                calendarView.onDrawed = null
            }

            if(calendarView.parent == null) container.addView(calendarView)
            return calendarView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount() = 2000
    }

    fun selectDate(time: Long) {
        val pagePos = getPagePosition(time)
        l("$pagePos 페이지로 이동")
        if(viewPager.currentItem == pagePos) {
            targetCalendarView.selectTime(time)
        }else {
            viewPager.setCurrentItem(pagePos, true)
        }
    }

    private fun getPagePosition(time: Long) : Int {
        val today = System.currentTimeMillis()
        return startMonthPosition + getDiffYear(today, time) * 12 + getDiffMonth(today, time)
    }
}