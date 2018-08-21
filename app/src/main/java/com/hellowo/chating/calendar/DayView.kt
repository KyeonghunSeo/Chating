package com.hellowo.chating.calendar

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.chating.*
import com.hellowo.chating.model.ChatRoom
import com.hellowo.chating.ui.adapter.TimeObjectAdapter
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.view_day.view.*
import java.util.*
import kotlin.collections.ArrayList

class DayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr) {
    companion object{}
    private val currentCal = Calendar.getInstance()
    private var calendarView: CalendarView? = null
    private val items = ArrayList<TimeObject>()
    private var timeObjectList: RealmResults<TimeObject>? = null
    private val timeObjectDayViewAdapter = TimeObjectDayViewAdapter(context, items){ onItemClicked(it) }

    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_day, this, true)
        rootLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        setCardBackgroundColor(Color.WHITE)
        elevation = 0f
        initRecyclerView()
    }

    fun setCalendarView(view: CalendarView) { calendarView = view }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = timeObjectDayViewAdapter
    }

    private fun setData() {
        calendarView?.selectedCal?.let { cal ->
            timeObjectList?.removeAllChangeListeners()
            timeObjectList = TimeObjectManager.realm.where(TimeObject::class.java)
                    .greaterThanOrEqualTo("dtEnd", getCalendarTime0(cal))
                    .lessThanOrEqualTo("dtStart", getCalendarTime23(cal))
                    .sort("dtStart", Sort.ASCENDING)
                    .findAllAsync()
            timeObjectList?.addChangeListener { result, changeSet ->
                changeSet.insertionRanges.forEach {
                    //추가된 데이터
                }
                items.clear()
                items.addAll(result)
                timeObjectDayViewAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun clearData() {
        items.clear()
        timeObjectDayViewAdapter.notifyDataSetChanged()
    }

    private fun onItemClicked(timeObject: TimeObject) {
    }

    private fun setDateText() {
        dateText.text = calendarView?.selectedCal?.get(Calendar.DATE).toString()
    }

    private fun confirm() {}

    fun show() {
        visibility = View.VISIBLE
        viewMode = ViewMode.ANIMATING
        setDateText()

        calendarView?.getSelectedView()?.let { dateLy ->
            val location = IntArray(2)
            dateLy.getLocationInWindow(location)
            layoutParams = FrameLayout.LayoutParams(dateLy.width, dateLy.height).apply {
                setMargins(location[0], location[1] - AppRes.statusBarHeight, 0, 0)
            }

            val animSet = AnimatorSet()
            animSet.playTogether(ObjectAnimator.ofFloat(this@DayView,
                    "elevation", 0f, dpToPx(15).toFloat()).setDuration(ANIM_DUR))
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
                            setData()
                        }
                        override fun onTransitionResume(transition: Transition) {}
                        override fun onTransitionPause(transition: Transition) {}
                        override fun onTransitionCancel(transition: Transition) {}
                        override fun onTransitionStart(transition: Transition) {
                            val animSet = AnimatorSet()
                            animSet.playTogether(ObjectAnimator.ofFloat(this@DayView,
                                    "elevation", dpToPx(15).toFloat(), 0f).setDuration(ANIM_DUR),
                                    ObjectAnimator.ofFloat(dateText, "alpha", 1f, 1f).setDuration(ANIM_DUR))
                            animSet.interpolator = FastOutSlowInInterpolator()
                            animSet.start()
                        }
                    })
                    TransitionManager.beginDelayedTransition(this@DayView, transiion)
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                        setMargins(0, AppRes.bottomBarHeight, 0, AppRes.bottomBarHeight)
                    }
                }
                override fun onAnimationCancel(p0: Animator?) {}
                override fun onAnimationStart(p0: Animator?) {}
            })
            animSet.start()
        }
    }

    fun hide() {
        calendarView?.getSelectedView()?.let { dateLy ->
            val location = IntArray(2)
            dateLy.getLocationInWindow(location)

            viewMode = ViewMode.ANIMATING
            val transiion = makeChangeBounceTransition()
            transiion.interpolator = FastOutSlowInInterpolator()
            transiion.duration = ANIM_DUR
            transiion.addListener(object : Transition.TransitionListener{
                override fun onTransitionEnd(transition: Transition) {
                    val animSet = AnimatorSet()
                    animSet.playTogether(ObjectAnimator.ofFloat(this@DayView,
                            "elevation", dpToPx(15).toFloat(), 0f).setDuration(ANIM_DUR),
                            ObjectAnimator.ofFloat(dateText, "alpha", 1f, 1f).setDuration(ANIM_DUR))
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.addListener(object : Animator.AnimatorListener{
                        override fun onAnimationRepeat(p0: Animator?) {}
                        override fun onAnimationEnd(p0: Animator?) {
                            viewMode = ViewMode.CLOSED
                            visibility = View.GONE
                            clearData()
                        }
                        override fun onAnimationCancel(p0: Animator?) {}
                        override fun onAnimationStart(p0: Animator?) {}
                    })
                    animSet.start()
                }
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionCancel(transition: Transition) {}
                override fun onTransitionStart(transition: Transition) {}
            })
            TransitionManager.beginDelayedTransition(this, transiion)
            layoutParams = FrameLayout.LayoutParams(dateLy.width, dateLy.height).apply {
                setMargins(location[0], location[1] - AppRes.statusBarHeight, 0, 0)
            }
        }
    }
}