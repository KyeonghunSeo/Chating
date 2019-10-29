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
import androidx.cardview.widget.CardView
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
        radius = dpToPx(4f)
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
        /*
        viewPager.setPageTransformer(true) { view, position ->
            val pageWidth = view.width
            when {
                position > -1 && position < 0 -> (view as DayView).getRootLy().translationX = pageWidth * position * -0.9f
                else -> restoreView(view as DayView)
            }
        }*/
        viewPager.setPagingEnabled(false)
        visibility = View.GONE
    }

    private fun selectedTargetDayView(dayView: DayView) {
        targetDayView = dayView
        if(isOpened()) {
            dayViews.forEach {
                if(it == targetDayView) {
                    it.targeted()
                }else {
                    it.unTargeted()
                }
            }
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
        dayViews.forEach { restoreView(it) }
    }

    private fun restoreViews() {
        dayViews.forEach { restoreView(it) }
    }

    private fun restoreView(dayView: DayView) {
        dayView.getRootLy().let {
            it.translationX = 0f
            it.scaleX = 1f
            it.scaleY = 1f
            it.alpha = 1f
        }
    }

    fun notifyDateChanged() {
        dayViews.forEach { it.notifyDateChanged() }
    }

    private fun clear() {
        viewPager.setCurrentItem(startPosition, false)
        dayViews.forEach {
            it.clear()
            restoreView(it)
        }
    }

    fun show() {
        vibrate(context)
        viewMode = ViewMode.ANIMATING
        visibility = View.VISIBLE
        initTime(MainActivity.getTargetCal()?.timeInMillis ?: System.currentTimeMillis())
        MainActivity.getTargetCalendarView()?.getSelectedView()?.let { dateCell ->
            val dataSize = MainActivity.getTargetCalendarView()?.getSelectedViewHolders()?.size ?: 0
            val location = IntArray(2)
            dateCell.getLocationInWindow(location)
            layoutParams = LayoutParams(dateCell.width, dateCell.height).apply {
                val xPos = location[0] + if(MainActivity.isFolderOpen()) -MainActivity.tabSize else 0
                val yPos = location[1] - AppStatus.statusBarHeight
                setMargins(xPos, yPos, 0, 0)
            }
            val animSet = AnimatorSet()
            animSet.playTogether(ObjectAnimator.ofFloat(this@DayPager, "elevation", 0f, startZ),
                    ObjectAnimator.ofFloat(this@DayPager, "alpha", 0f, 1f),
                    ObjectAnimator.ofFloat(targetDayView.getPreviewDataImg(), "alpha", 0f, 1f))
            animSet.duration = 200L
            animSet.addListener(object : AnimatorListenerAdapter(){
                override fun onAnimationEnd(p0: Animator?) {
                    val transiion = makeChangeBounceTransition()
                    transiion.duration = 250L
                    transiion.addListener(object : TransitionListenerAdapter(){
                        override fun onTransitionEnd(transition: Transition) {
                            dayViews.forEach { it.setDateOpenedStyle() }
                            targetDayView.targeted()
                            viewMode = ViewMode.OPENED
                            viewPager.setPagingEnabled(true)
                            onVisibility?.invoke(true)
                        }
                        override fun onTransitionStart(transition: Transition) {
                            notifyDateChanged()
                            targetDayView.show(dataSize)
                        }
                    })
                    TransitionManager.beginDelayedTransition(this@DayPager, transiion)
                    (layoutParams as LayoutParams).let {
                        it.width = MATCH_PARENT
                        it.height = MATCH_PARENT
                        it.setMargins(0, 0, 0, 0)
                    }
                    MainActivity.getFakeDateText()?.visibility = View.VISIBLE
                    requestLayout()
                }
            })
            animSet.start()
        }
    }

    fun hide() {
        MainActivity.getTargetCalendarView()?.getSelectedView()?.let { dateCell ->
            val dataSize = MainActivity.getTargetCalendarView()?.getSelectedViewHolders()?.size ?: 0
            elevation = startZ
            viewMode = ViewMode.ANIMATING
            viewPager.setPagingEnabled(false)
            val location = IntArray(2)
            dateCell.getLocationInWindow(location)
            val transiion = makeChangeBounceTransition()
            transiion.duration = 250L
            transiion.addListener(object : TransitionListenerAdapter(){
                override fun onTransitionEnd(transition: Transition) {
                    val animSet = AnimatorSet()
                    animSet.playTogether(ObjectAnimator.ofFloat(this@DayPager, "elevation", startZ, 0f),
                            ObjectAnimator.ofFloat(this@DayPager, "alpha", 1f, 0f),
                            ObjectAnimator.ofFloat(targetDayView.getPreviewDataImg(), "alpha", 1f, 0f))
                    animSet.duration = 200L
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
                    restoreViews()
                    targetDayView.hide(dataSize)
                    targetDayView.unTargeted()
                }
            })
            TransitionManager.beginDelayedTransition(this, transiion)
            (layoutParams as LayoutParams).let {
                it.width = dateCell.width
                it.height = dateCell.height
                val xPos = location[0] + if(MainActivity.isFolderOpen()) -MainActivity.tabSize else 0
                val yPos = location[1] - AppStatus.statusBarHeight
                it.setMargins(xPos, yPos, 0, 0)
            }
            MainActivity.getFakeDateText()?.visibility = View.GONE
            requestLayout()
        }
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
    fun isClosed(): Boolean = viewMode == ViewMode.CLOSED

}