package com.ayaan.twelvepages.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.ui.view.base.PagingControlableViewPager
import java.util.*

class CalendarPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    private val startPosition = 1000
    private val viewCount = 3
    private val viewPager = PagingControlableViewPager(context)
    private val calendarViews = List(viewCount) { CalendarView(context) }
    var onSelectedDate: ((Long, Int, Int, Boolean, CalendarView) -> Unit)? = null
    var onTop: ((Boolean, Boolean) -> Unit)? = null

    private val tempCal = Calendar.getInstance()
    private var targetCalendarView : CalendarView = calendarViews[startPosition % viewCount]
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
                l("$position 페이지 선택됨 : ${AppDateFormat.ymdDate.format(targetCalendarView.targetCal.time)}")
            }
        })/*
        viewPager.setPageTransformer(true) { view, position ->
            val pageWidth = view.width
            when {
                position > -1 && position < 0 -> {
                    (view as CalendarView).calendarLy.translationX = pageWidth * position * -0.4f
                }
                else -> {
                    (view as CalendarView).calendarLy.translationX = 0f
                }
            }
        }*/
    }

    private fun selectedTargetCalendarView(calendarView: CalendarView) {
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

    fun redrawAndSelect(){
        redraw()
        targetCalendarView.selectDate(targetCalendarView.startCellNum)
    }

    //↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓드래그 처리 부분↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

    private val autoPagingThreshold = dpToPx(30)
    private var autoPagingFlag = 0
    private val autoPaginglHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            if (autoPagingFlag != 0) {
                if (autoPagingFlag == -1) {
                    viewPager.currentItem = viewPager.currentItem - 1
                } else if (autoPagingFlag == 1) {
                    viewPager.currentItem = viewPager.currentItem + 1
                }
                this.sendEmptyMessageDelayed(0, 700)
            }
        }
    }

    fun onDrag(event: DragEvent) {
        targetCalendarView.onDrag(event)

        when {
            event.x < autoPagingThreshold -> {
                if(autoPagingFlag != -1) {
                    autoPagingFlag = -1
                    autoPaginglHandler.sendEmptyMessageDelayed(0, 700)
                }
            }
            event.x > width - autoPagingThreshold -> {
                if(autoPagingFlag != 1) {
                    autoPagingFlag = 1
                    autoPaginglHandler.sendEmptyMessageDelayed(0, 700)
                }
            }
            else -> {
                if(autoPagingFlag != 0){
                    autoPagingFlag = 0
                    autoPaginglHandler.removeMessages(0)
                }
            }
        }
    }

    fun endDrag() {
        autoPagingFlag = 0
        autoPaginglHandler.removeMessages(0)
        calendarViews.forEach { it.endDrag() }
    }

    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑드래그 처리 부분↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑
}