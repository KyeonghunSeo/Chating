package com.hellowo.chating.calendar.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.chating.*
import com.hellowo.chating.calendar.model.TimeObject
import com.hellowo.chating.calendar.adapter.TimeObjectDayViewAdapter
import com.hellowo.chating.calendar.TimeObjectManager
import com.hellowo.chating.calendar.model.CalendarSkin
import com.hellowo.chating.calendar.model.Template
import com.hellowo.chating.ui.activity.MainActivity
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.view_day.view.*
import java.util.*
import kotlin.collections.ArrayList

class DayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr) {
    companion object{}
    private var calendarView: CalendarView? = null
    private val items = ArrayList<TimeObject>()
    private var timeObjectList: RealmResults<TimeObject>? = null
    private val timeObjectDayViewAdapter = TimeObjectDayViewAdapter(context, items) { view, timeObject ->
        MainActivity.instance?.viewModel?.targetTimeObject?.value = timeObject
        MainActivity.instance?.viewModel?.targetView?.value = view
    }

    private var isInit = true
    var viewMode = ViewMode.CLOSED
    var onVisibility: ((Boolean) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_day, this, true)
        rootLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        setCardBackgroundColor(Color.WHITE)
        elevation = 0f
        initRecyclerView()
        contentLy.visibility = View.INVISIBLE
    }

    fun setCalendarView(view: CalendarView) { calendarView = view }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = timeObjectDayViewAdapter
    }

    fun notifyDateChanged(offset: Int) {
        setDateText()

        dowText.translationX = getDowX()

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
        if(offset != 0) {
            startPagingEffectAnimation(offset, rootLy, null)
        }
    }

    private fun clearData() {
        items.clear()
        timeObjectDayViewAdapter.notifyDataSetChanged()
    }

    private fun setDateText() {
        calendarView?.let {
            dateText.text = it.selectedCal.get(Calendar.DATE).toString()
            dowText.text = AppRes.simpleDow.format(it.selectedCal.time)
            val color = it.getDateTextColor(it.postSelectedNum)
            dateText.setTextColor(color)
            dowText.setTextColor(color)
        }
    }

    private fun getDowX() : Float {
        return if(dateText.text.length == 1) dpToPx(55).toFloat()
        else dpToPx(80).toFloat()
    }

    fun show() {
        viewMode = ViewMode.ANIMATING
        visibility = View.VISIBLE
        alpha = 0.85f

        if(isInit) {
            isInit = false
            calendarView?.setDefaultDateTextSkin(dateText)
            dateText.typeface = CalendarSkin.selectFont
            dateText.scaleY = CalendarView.selectedDateScale
            dateText.scaleX = CalendarView.selectedDateScale
        }

        setDateText()

        calendarView?.getSelectedView()?.let { dateLy ->
            val location = IntArray(2)
            dateLy.getLocationInWindow(location)
            layoutParams = CoordinatorLayout.LayoutParams(dateLy.width, dateLy.height).apply {
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
                            contentLy.visibility = View.VISIBLE
                        }
                        override fun onTransitionResume(transition: Transition) {}
                        override fun onTransitionPause(transition: Transition) {}
                        override fun onTransitionCancel(transition: Transition) {}
                        override fun onTransitionStart(transition: Transition) {
                            notifyDateChanged(0)
                            val animSet = AnimatorSet()
                            animSet.playTogether(ObjectAnimator.ofFloat(this@DayView,
                                    "elevation", dpToPx(15).toFloat(), 0f).setDuration(ANIM_DUR),
                                    ObjectAnimator.ofFloat(this@DayView, "alpha", 0.85f, 1f).setDuration(ANIM_DUR),
                                    ObjectAnimator.ofFloat(dateText, "scaleX", CalendarView.selectedDateScale, 4f),
                                    ObjectAnimator.ofFloat(dateText, "scaleY", CalendarView.selectedDateScale, 4f),
                                    ObjectAnimator.ofFloat(dowText, "scaleX", 1f, 1.5f),
                                    ObjectAnimator.ofFloat(dowText, "scaleY", 1f, 1.5f),
                                    ObjectAnimator.ofFloat(dowText, "translationX", 0f, getDowX()),
                                    ObjectAnimator.ofFloat(dowText, "translationY", 0f, dpToPx(15).toFloat()))
                            animSet.interpolator = FastOutSlowInInterpolator()
                            animSet.start()
                        }
                    })
                    TransitionManager.beginDelayedTransition(this@DayView, transiion)
                    layoutParams = CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                        setMargins(0, mainBarHeight, 0, mainBarHeight)
                    }
                }
                override fun onAnimationCancel(p0: Animator?) {}
                override fun onAnimationStart(p0: Animator?) {}
            })
            animSet.start()
            onVisibility?.invoke(true)
        }
    }

    fun hide() {
        calendarView?.getSelectedView()?.let { dateLy ->
            val location = IntArray(2)
            dateLy.getLocationInWindow(location)

            elevation = dpToPx(15).toFloat()
            viewMode = ViewMode.ANIMATING
            val transiion = makeChangeBounceTransition()
            transiion.interpolator = FastOutSlowInInterpolator()
            transiion.duration = ANIM_DUR
            transiion.addListener(object : Transition.TransitionListener{
                override fun onTransitionEnd(transition: Transition) {
                    val animSet = AnimatorSet()
                    animSet.playTogether(ObjectAnimator.ofFloat(this@DayView,
                            "elevation", dpToPx(15).toFloat(), 0f).setDuration(ANIM_DUR),
                            ObjectAnimator.ofFloat(this@DayView, "alpha", 1f, 0.85f).setDuration(ANIM_DUR))
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
                override fun onTransitionStart(transition: Transition) {
                    val animSet = AnimatorSet()
                    animSet.playTogether(ObjectAnimator.ofFloat(dateText, "scaleX", 4f, CalendarView.selectedDateScale),
                            ObjectAnimator.ofFloat(dateText, "scaleY", 4f, CalendarView.selectedDateScale),
                            ObjectAnimator.ofFloat(dowText, "scaleX", 1.5f, 1f),
                            ObjectAnimator.ofFloat(dowText, "scaleY", 1.5f, 1f),
                            ObjectAnimator.ofFloat(dowText, "translationX", getDowX(), 0f),
                            ObjectAnimator.ofFloat(dowText, "translationY", dpToPx(15).toFloat(), 0f))
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.start()
                    contentLy.visibility = View.INVISIBLE
                }
            })
            TransitionManager.beginDelayedTransition(this, transiion)
            layoutParams = CoordinatorLayout.LayoutParams(dateLy.width, dateLy.height).apply {
                setMargins(location[0], location[1] - AppRes.statusBarHeight, 0, 0)
            }
            onVisibility?.invoke(false)
        }
    }
}