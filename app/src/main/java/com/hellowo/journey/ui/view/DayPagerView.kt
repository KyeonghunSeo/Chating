package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.hellowo.journey.*
import com.hellowo.journey.manager.CalendarManager
import com.hellowo.journey.ui.activity.MainActivity
import java.util.*


class DayPagerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : CardView(context, attrs, defStyleAttr) {
    companion object {
        const val headerTextScale = 5f
        val datePosX = dpToPx(9f)
        val datePosY = -dpToPx(13f)
        val dowPosX = -dpToPx(2f)
        val dowPosY = dpToPx(3f)
        val holiPosX = dpToPx(12.5f)
        val holiPosY = -dpToPx(6.5f)
        val startZ = dpToPx(10f)
        val endZ = dpToPx(0f)
        val subScale = 0.35f
    }

    var viewMode = ViewMode.CLOSED
    var onVisibility: ((Boolean) -> Unit)? = null

    private val initCal = Calendar.getInstance()
    private val startPosition = 1000
    private val viewCount = 3
    private val viewPager = ViewPager(context)
    private val dayViews = List(viewCount) { DayView(context) }

    private var targetDayView : DayView = dayViews[startPosition % viewCount]
    private var selectDateTime = Long.MIN_VALUE

    init {
        setCardBackgroundColor(CalendarManager.backgroundColor)
        elevation = 0f
        radius = 0f
        addView(viewPager)
        viewPager.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        viewPager.adapter = CalendarPagerAdapter()
        viewPager.setCurrentItem(startPosition, false)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                //selectedTargetCalendarView(dayViews[position % viewCount])
            }
        })
    }

    inner class CalendarPagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val dayView = dayViews[position % viewCount]
            dayView.notifyDateChanged(initCal.timeInMillis)
            if(dayView.parent == null) container.addView(dayView)
            return dayView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount() = 2000
    }

    fun notifyDateChanged(offset: Int) {

    }

    private fun setDateText() {
        MainActivity.getTargetCalendarView()?.let {

        }
    }

    fun show() {
        viewMode = ViewMode.ANIMATING
        visibility = View.VISIBLE
        alpha = 0.85f

        setDateText()

        MainActivity.getTargetCalendarView()?.getSelectedView()?.let { dateCell ->
            val location = IntArray(2)
            dateCell.getLocationInWindow(location)
            layoutParams = FrameLayout.LayoutParams(dateCell.width, dateCell.height).apply {
                setMargins(location[0], location[1] - AppDateFormat.statusBarHeight, 0, 0)
            }

            val animSet = AnimatorSet()
            animSet.playTogether(ObjectAnimator.ofFloat(this@DayPagerView, "elevation", 0f, startZ))
            animSet.duration = 150
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(p0: Animator?) {}
                override fun onAnimationEnd(p0: Animator?) {
                    val transiion = makeChangeBounceTransition()
                    transiion.interpolator = FastOutSlowInInterpolator()
                    transiion.duration = ANIM_DUR
                    transiion.addListener(object : Transition.TransitionListener{
                        override fun onTransitionEnd(transition: Transition) {
                            viewMode = ViewMode.OPENED
                            viewPager.visibility = View.VISIBLE
                            onVisibility?.invoke(true)
                        }
                        override fun onTransitionResume(transition: Transition) {}
                        override fun onTransitionPause(transition: Transition) {}
                        override fun onTransitionCancel(transition: Transition) {}
                        override fun onTransitionStart(transition: Transition) {
                            notifyDateChanged(0)
                        }
                    })
                    TransitionManager.beginDelayedTransition(this@DayPagerView, transiion)
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                        setMargins(0, dpToPx(50), 0, dpToPx(40))
                    }
                }
                override fun onAnimationCancel(p0: Animator?) {}
                override fun onAnimationStart(p0: Animator?) {}
            })
            animSet.start()
        }
    }

    fun hide() {
        //timeObjectList?.removeAllChangeListeners()
        MainActivity.getTargetCalendarView()?.getSelectedView()?.let { dateCell ->
            elevation = startZ
            viewMode = ViewMode.ANIMATING

            val location = IntArray(2)
            dateCell.getLocationInWindow(location)
            val transiion = makeChangeBounceTransition()
            transiion.interpolator = FastOutSlowInInterpolator()
            transiion.duration = ANIM_DUR
            transiion.addListener(object : Transition.TransitionListener{
                override fun onTransitionEnd(transition: Transition) {
                    val animSet = AnimatorSet()
                    animSet.playTogether(ObjectAnimator.ofFloat(this@DayPagerView,
                            "elevation", startZ, 0f).setDuration(ANIM_DUR),
                            ObjectAnimator.ofFloat(this@DayPagerView, "alpha", 1f, 0.85f).setDuration(ANIM_DUR))
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.addListener(object : Animator.AnimatorListener{
                        override fun onAnimationRepeat(p0: Animator?) {}
                        override fun onAnimationEnd(p0: Animator?) {
                            viewMode = ViewMode.CLOSED
                            visibility = View.GONE
                            //clearData()
                        }
                        override fun onAnimationCancel(p0: Animator?) {}
                        override fun onAnimationStart(p0: Animator?) {}
                    })
                    animSet.start()
                }
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionCancel(transition: Transition) {}
                override fun onTransitionStart(transition: Transition) {
                    onVisibility?.invoke(false)
                    viewPager.visibility = View.INVISIBLE
                }
            })
            TransitionManager.beginDelayedTransition(this, transiion)
            layoutParams = FrameLayout.LayoutParams(dateCell.width, dateCell.height).apply {
                setMargins(location[0], location[1] - AppDateFormat.statusBarHeight, 0, 0)
            }
        }
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        notifyDateChanged(0)
    }
}