package com.hellowo.journey.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import java.util.*

class CalendarPagerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val startMonthPosition = 1000
        private val tempCal = Calendar.getInstance()
        private val targetCalendar = Calendar.getInstance()
    }

    private val viewPager = ViewPager(context)
    private val calendarViews = List(4) { CalendarView(context) }

    init {
        addView(viewPager)
        viewPager.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        viewPager.adapter = CalendarPagerAdapter()
        viewPager.setCurrentItem(startMonthPosition, false)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {

            }
        })
    }

    inner class CalendarPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val calendarView = calendarViews[position % 4]
            tempCal.timeInMillis = System.currentTimeMillis()
            tempCal.set(Calendar.DATE, 1)
            tempCal.add(Calendar.MONTH, position - startMonthPosition)
            calendarView.reserveDraw(tempCal.timeInMillis)
            container.addView(calendarView)
            return calendarView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

        override fun getCount() = Int.MAX_VALUE
    }
}