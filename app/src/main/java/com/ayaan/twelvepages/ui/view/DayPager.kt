package com.ayaan.twelvepages.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.LinearLayout.VERTICAL
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.CalendarManager
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.view.base.PagingControlableViewPager
import java.util.*


class DayPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : CardView(context, attrs, defStyleAttr) {
    val startZ = dpToPx(10f)
    var viewMode = ViewMode.CLOSED
    var onVisibility: ((Boolean) -> Unit)? = null

    private val tempCal = Calendar.getInstance()
    private val initCal = Calendar.getInstance()
    private val startPosition = 1000
    private val viewCount = 3
    private val viewPager = PagingControlableViewPager(context)
    private val dayViews = List(viewCount) { DayView(context) }

    private var targetDayView : DayView = dayViews[startPosition % viewCount]

    init {
        setCardBackgroundColor(CalendarManager.backgroundColor)
        elevation = 0f
        radius = 0f
        addView(viewPager)
        viewPager.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        viewPager.adapter = CalendarPagerAdapter()
        viewPager.setCurrentItem(startPosition, false)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                selectedTargetDayView(dayViews[position % viewCount])
                if(isOpened()) {
                    MainActivity.getCalendarPager()?.selectDate(targetDayView.targetCal.timeInMillis)
                }
            }
        })
        viewPager.setPagingEnabled(false)
        visibility = View.GONE
    }

    private fun selectedTargetDayView(dayView: DayView) {
        targetDayView = dayView
        if(isOpened()) {
            targetDayView.targeted()
        }
    }

    inner class CalendarPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val dayView = dayViews[position % viewCount]
            if(viewMode == ViewMode.OPENED) {
                tempCal.timeInMillis = initCal.timeInMillis
                tempCal.add(Calendar.DATE, position - startPosition)
                dayView.initTime(tempCal.timeInMillis)
                dayView.notifyDateChanged()
            }
            if(dayView.parent == null) container.addView(dayView)
            return dayView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount() = 2000
    }

    fun initTime(time: Long) {
        initCal.timeInMillis = time
        viewPager.setCurrentItem(startPosition, false)
        dayViews[(startPosition - 1) % viewCount].initTime(time - DAY_MILL)
        dayViews[(startPosition) % viewCount].initTime(time)
        dayViews[(startPosition + 1) % viewCount].initTime(time + DAY_MILL)
    }

    fun notifyDateChanged() {
        dayViews.forEach { it.notifyDateChanged() }
    }

    private fun clear() {
        dayViews.forEach { it.clear() }
        viewPager.setCurrentItem(startPosition, false)
    }

    fun show() {
        viewMode = ViewMode.ANIMATING
        visibility = View.VISIBLE
        initTime(MainActivity.getTargetCal()?.timeInMillis ?: System.currentTimeMillis())
        MainActivity.getTargetCalendarView()?.getSelectedView()?.let { dateCell ->
            val location = IntArray(2)
            dateCell.getLocationInWindow(location)
            val h = CalendarView.dataStartYOffset.toInt() - CalendarView.weekLyBottomPadding
            layoutParams = LayoutParams(dateCell.width, h).apply {
                val xPos = location[0] + if(MainActivity.isFolderOpen()) -MainActivity.tabSize else 0
                val yPos = location[1] - AppStatus.statusBarHeight + CalendarView.weekLyBottomPadding
                setMargins(xPos, yPos, 0, 0)
            }
            val animSet = AnimatorSet()
            animSet.playTogether(ObjectAnimator.ofFloat(this@DayPager, "elevation", 0f, startZ))
            animSet.duration = 100L
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(p0: Animator?) {
                    val transiion = makeChangeBounceTransition()
                    transiion.duration = 300L
                    transiion.addListener(object : TransitionListenerAdapter(){
                        override fun onTransitionEnd(transition: Transition) {
                            dayViews.forEach { it.setDateOpenedStyle() }
                            viewMode = ViewMode.OPENED
                            viewPager.setPagingEnabled(true)
                            onVisibility?.invoke(true)
                        }
                        override fun onTransitionStart(transition: Transition) {
                            notifyDateChanged()
                            targetDayView.show(this@DayPager)
                        }
                    })
                    TransitionManager.beginDelayedTransition(this@DayPager, transiion)
                    (layoutParams as LayoutParams).let {
                        it.width = MATCH_PARENT
                        it.height = MATCH_PARENT
                        it.setMargins(0, 0, 0, 0)
                    }
                    MainActivity.getMainDateLy()?.orientation = HORIZONTAL
                    targetDayView.getDateLy().orientation = HORIZONTAL
                    requestLayout()
                }
            })
            animSet.start()
        }
    }

    fun hide() {
        MainActivity.getTargetCalendarView()?.getSelectedView()?.let { dateCell ->
            elevation = startZ
            viewMode = ViewMode.ANIMATING
            viewPager.setPagingEnabled(false)
            val location = IntArray(2)
            dateCell.getLocationInWindow(location)
            val transiion = makeChangeBounceTransition()
            transiion.duration = 300L
            transiion.addListener(object : TransitionListenerAdapter(){
                override fun onTransitionEnd(transition: Transition) {
                    val animSet = AnimatorSet()
                    animSet.playTogether(ObjectAnimator.ofFloat(this@DayPager, "elevation", startZ, 0f))
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.duration = 150L
                    animSet.addListener(object : AnimatorListenerAdapter(){
                        override fun onAnimationEnd(p0: Animator?) {
                            viewMode = ViewMode.CLOSED
                            visibility = View.GONE
                            clear()
                        }
                    })
                    animSet.start()
                }
                override fun onTransitionStart(transition: Transition) {
                    onVisibility?.invoke(false)
                    targetDayView.hide(this@DayPager)
                }
            })
            TransitionManager.beginDelayedTransition(this, transiion)
            (layoutParams as LayoutParams).let {
                it.width = dateCell.width
                it.height = CalendarView.dataStartYOffset.toInt() - CalendarView.weekLyBottomPadding
                val xPos = location[0] + if(MainActivity.isFolderOpen()) -MainActivity.tabSize else 0
                val yPos = location[1] - AppStatus.statusBarHeight + CalendarView.weekLyBottomPadding
                it.setMargins(xPos, yPos, 0, 0)
            }
            MainActivity.getMainDateLy()?.orientation = VERTICAL
            targetDayView.getDateLy().orientation = VERTICAL
            requestLayout()
        }
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
    fun isClosed(): Boolean = viewMode == ViewMode.CLOSED

}

/*
package com.ayaan.twelvepages.ui.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.CalendarManager
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.view.base.PagingControlableViewPager
import kotlinx.android.synthetic.main.activity_main.view.*
import java.util.*


class DayPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : CardView(context, attrs, defStyleAttr) {
    val startZ = dpToPx(8f)
    var viewMode = ViewMode.CLOSED
    var onVisibility: ((Boolean) -> Unit)? = null

    private val tempCal = Calendar.getInstance()
    private val initCal = Calendar.getInstance()
    private val startPosition = 1000
    private val viewCount = 3
    private val viewPager = PagingControlableViewPager(context)
    private val dayViews = List(viewCount) { DayView(context) }

    private var targetDayView : DayView = dayViews[startPosition % viewCount]

    init {
        setCardBackgroundColor(CalendarManager.backgroundColor)
        elevation = 0f
        radius = 0f
        addView(viewPager)
        viewPager.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        viewPager.adapter = CalendarPagerAdapter()
        viewPager.setCurrentItem(startPosition, false)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                selectedTargetDayView(dayViews[position % viewCount])
                if(isOpened()) {
                    MainActivity.getCalendarPager()?.selectDate(targetDayView.targetCal.timeInMillis)
                }
            }
        })
        viewPager.setPagingEnabled(false)
        visibility = View.GONE
    }

    private fun selectedTargetDayView(dayView: DayView) {
        targetDayView = dayView
        targetDayView.targeted()
    }

    inner class CalendarPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val dayView = dayViews[position % viewCount]
            if(viewMode == ViewMode.OPENED) {
                tempCal.timeInMillis = initCal.timeInMillis
                tempCal.add(Calendar.DATE, position - startPosition)
                dayView.initTime(tempCal.timeInMillis)
                dayView.notifyDateChanged()
            }
            if(dayView.parent == null) container.addView(dayView)
            return dayView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount() = 2000
    }

    fun initTime(time: Long) {
        viewPager.setCurrentItem(startPosition, false)
        initCal.timeInMillis = time
        dayViews[(startPosition - 1) % viewCount].initTime(time - DAY_MILL)
        dayViews[(startPosition) % viewCount].initTime(time)
        dayViews[(startPosition + 1) % viewCount].initTime(time + DAY_MILL)
    }

    fun notifyDateChanged() {
        dayViews.forEach { it.notifyDateChanged() }
    }

    private fun clear() {
        dayViews.forEach { it.clear() }
        viewPager.setCurrentItem(startPosition, false)
    }

    fun show() {
        viewMode = ViewMode.ANIMATING
        visibility = View.VISIBLE
        alpha = 0.9f
        initTime(MainActivity.getTargetCal()?.timeInMillis ?: System.currentTimeMillis())

        MainActivity.getTargetCalendarView()?.getSelectedView()?.let { dateCell ->
            val location = IntArray(2)
            dateCell.getLocationInWindow(location)
            layoutParams = LayoutParams(dateCell.width, dateCell.height).apply {
                val xPos = location[0] + if(MainActivity.isFolderOpen()) -MainActivity.tabSize else 0
                val yPos = location[1] - AppStatus.statusBarHeight
                setMargins(xPos, yPos, 0, 0)
            }

            val animSet = AnimatorSet()
            animSet.playTogether(ObjectAnimator.ofFloat(this@DayPager, "elevation", 0f, startZ))
            animSet.duration = 100
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(p0: Animator?) {
                    val transitionSet = TransitionSet()
                    transitionSet.addListener(object : TransitionListenerAdapter(){
                        override fun onTransitionEnd(transition: Transition) {
                            dayViews.forEach { it.setDateOpenedStyle() }
                            viewMode = ViewMode.OPENED
                            viewPager.setPagingEnabled(true)
                            onVisibility?.invoke(true)
                        }
                        override fun onTransitionStart(transition: Transition) {
                            notifyDateChanged()
                            targetDayView.show(this@DayPager)
                        }
                    })
                    val transiion = makeChangeBounceTransition()
                    transitionSet.addTransition(transiion)
                    MainActivity.getMainSubDateLy()?.let { transiion.addTarget(it) }
                    transiion.addTarget(this@DayPager)
                    MainActivity.getCalendarLy()?.let { calendarLy ->
                        TransitionManager.beginDelayedTransition(calendarLy, transitionSet)
                        MainActivity.getMainSubDateLy()?.let {
                            it.orientation = LinearLayout.HORIZONTAL
                        }
                        (layoutParams as LayoutParams).let {
                            it.width = MATCH_PARENT
                            it.height = MATCH_PARENT
                            it.setMargins(0, 0, 0, 0)
                        }
                        calendarLy.requestLayout()
                    }
                }
            })
            animSet.start()
        }
    }

    fun hide() {
        MainActivity.getTargetCalendarView()?.getSelectedView()?.let { dateCell ->
            elevation = startZ
            viewMode = ViewMode.ANIMATING
            viewPager.setPagingEnabled(false)

            val location = IntArray(2)
            dateCell.getLocationInWindow(location)
            val transitionSet = TransitionSet()
            transitionSet.addListener(object : TransitionListenerAdapter(){
                override fun onTransitionEnd(transition: Transition) {
                    val animSet = AnimatorSet()
                    animSet.playTogether(ObjectAnimator.ofFloat(this@DayPager, "elevation", startZ, 0f))
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.duration = 150L
                    animSet.addListener(object : AnimatorListenerAdapter(){
                        override fun onAnimationEnd(p0: Animator?) {
                            viewMode = ViewMode.CLOSED
                            visibility = View.GONE
                            clear()
                        }
                    })
                    animSet.start()
                }
                override fun onTransitionStart(transition: Transition) {
                    onVisibility?.invoke(false)
                    targetDayView.hide(this@DayPager)
                }
            })
            val transiion = makeChangeBounceTransition()
            transitionSet.addTransition(transiion)
            MainActivity.getMainSubDateLy()?.let { transiion.addTarget(it) }
            transiion.addTarget(this@DayPager)
            MainActivity.getCalendarLy()?.let { calendarLy ->
                TransitionManager.beginDelayedTransition(this@DayPager, transitionSet)
                MainActivity.getMainSubDateLy()?.let {
                    it.orientation = LinearLayout.VERTICAL
                }
                (this@DayPager.layoutParams as FrameLayout.LayoutParams).let {
                    it.width = dateCell.width
                    it.height = dateCell.height
                    val xPos = location[0] + if(MainActivity.isFolderOpen()) -MainActivity.tabSize else 0
                    val yPos = location[1] - AppStatus.statusBarHeight
                    it.setMargins(xPos, yPos, 0, 0)
                }
                this@DayPager.requestLayout()
            }
        }
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
    fun isClosed(): Boolean = viewMode == ViewMode.CLOSED

}
 */