package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.journey.*
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.adapter.EventListAdapter
import com.hellowo.journey.adapter.TaskListAdapter
import com.hellowo.journey.calendar.TimeObjectManager
import com.hellowo.journey.model.CalendarSkin
import com.hellowo.journey.repeat.RepeatManager
import com.hellowo.journey.ui.activity.MainActivity
import io.realm.RealmResults
import kotlinx.android.synthetic.main.view_day.view.*
import java.util.*
import kotlin.collections.ArrayList

class DayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : CardView(context, attrs, defStyleAttr) {
    companion object;
    private var timeObjectList: RealmResults<TimeObject>? = null
    private val eventList = ArrayList<TimeObject>()
    private val taskList = ArrayList<TimeObject>()
    private val noteList = ArrayList<TimeObject>()
    private val eventAdapter = EventListAdapter(context, eventList) { view, timeObject ->
        onItemClick(view, timeObject)
    }
    private val taskAdapter = TaskListAdapter(context, taskList) { view, timeObject, action ->
        when(action) {
            0 -> onItemClick(view, timeObject)
            1 -> TimeObjectManager.done(timeObject)
        }

    }
    private val noteAdapter = EventListAdapter(context, noteList) { view, timeObject ->
        onItemClick(view, timeObject)
    }
    private var calendarView: CalendarView? = null

    private var isInit = true
    var viewMode = ViewMode.CLOSED
    var onVisibility: ((Boolean) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_day, this, true)
        //rootLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        setCardBackgroundColor(Color.WHITE)
        elevation = 0f
        initRecyclerView()
        contentLy.visibility = View.INVISIBLE
    }

    fun setCalendarView(view: CalendarView) { calendarView = view }

    private fun initRecyclerView() {
        eventListView.layoutManager = LinearLayoutManager(context)
        eventListView.adapter = eventAdapter

        taskListView.layoutManager = LinearLayoutManager(context)
        taskListView.adapter = taskAdapter
        taskAdapter.itemTouchHelper?.attachToRecyclerView(taskListView)

        noteListView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        noteListView.adapter = noteAdapter
    }

    fun notifyDateChanged(offset: Int) {
        setDateText()

        dowText.translationX = getDowX()

        calendarView?.selectedCal?.let { cal ->
            val startTime = getCalendarTime0(cal)
            val endTime = getCalendarTime23(cal)

            timeObjectList?.removeAllChangeListeners()
            timeObjectList = TimeObjectManager.getTimeObjectList(startTime, endTime)
            timeObjectList?.addChangeListener { result, changeSet ->
                l("==========데이뷰 데이터 변경 시작=========")
                /*
                if(changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {

                }else if(changeSet.state == OrderedCollectionChangeSet.State.UPDATE) {

                }
                changeSet.insertionRanges.forEach {
                    l("추가된 데이터 : ${ result[it.startIndex].toString()}")
                }
                changeSet.deletionRanges.forEach {
                    l("삭제 데이터 : ${ result[it.startIndex].toString()}")
                }
                changeSet.changeRanges.forEach {
                    l("변경된 데이터 : ${ result[it.startIndex].toString()}")
                }
                */
                eventList.clear()
                taskList.clear()
                noteList.clear()
                result.forEach { timeObject ->
                    when(TimeObject.Type.values()[timeObject.type]) {
                        TimeObject.Type.EVENT -> {
                            try{
                                if(timeObject.repeat.isNullOrEmpty()) {
                                    eventList.add(timeObject)
                                }else {
                                    RepeatManager.makeRepeatInstance(timeObject, startTime, endTime)
                                            .forEach { eventList.add(it) }
                                }
                            }catch (e: Exception){ e.printStackTrace() }
                        }
                        TimeObject.Type.TASK -> {
                            try{
                                if(timeObject.repeat.isNullOrEmpty()) {
                                    taskList.add(timeObject)
                                }else {
                                    RepeatManager.makeRepeatInstance(timeObject, startTime, endTime)
                                            .forEach { taskList.add(it) }
                                }
                            }catch (e: Exception){ e.printStackTrace() }
                        }
                        TimeObject.Type.NOTE -> noteList.add(timeObject)
                        else -> {
                        }
                    }
                }
                eventAdapter.notifyDataSetChanged()
                taskAdapter.notifyDataSetChanged()
                noteAdapter.notifyDataSetChanged()
                l("==========데이뷰 데이터 변경 종료=========")
            }
        }
        if(offset != 0) {
            startPagingEffectAnimation(offset, rootLy, null)
        }
    }

    private fun onItemClick(view: View, timeObject: TimeObject) {
        MainActivity.instance?.viewModel?.let {
            it.targetTimeObject.value = timeObject
            it.targetView.value = view
        }
    }

    private fun clearData() {
        eventList.clear()
        taskList.clear()
        noteList.clear()
        eventAdapter.notifyDataSetChanged()
        taskAdapter.notifyDataSetChanged()
        noteAdapter.notifyDataSetChanged()
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

            val bounds = Rect()
            dateText.paint.getTextBounds(dateText.text.toString(), 0, dateText.text.length, bounds)
            dowText.translationX = calendarView!!.minWidth / 2 + bounds.width() / 2 * CalendarView.selectedDateScale + CalendarView.dateMargin
            dowText.translationY = CalendarView.dateMargin.toFloat()

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
                                    ObjectAnimator.ofFloat(dateText, "translationY", 0f, dpToPx(15).toFloat()),
                                    ObjectAnimator.ofFloat(dowText, "scaleX", 1f, 3f),
                                    ObjectAnimator.ofFloat(dowText, "scaleY", 1f, 3f),
                                    ObjectAnimator.ofFloat(dowText, "translationX", dowText.translationX, getDowX()),
                                    ObjectAnimator.ofFloat(dowText, "translationY", dowText.translationY, dpToPx(15).toFloat()))
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
                    contentLy.visibility = View.INVISIBLE
                    val bounds = Rect()
                    dateText.paint.getTextBounds(dateText.text.toString(), 0, dateText.text.length, bounds)
                    val animSet = AnimatorSet()
                    animSet.playTogether(ObjectAnimator.ofFloat(dateText, "scaleX", 4f, CalendarView.selectedDateScale),
                            ObjectAnimator.ofFloat(dateText, "scaleY", 4f, CalendarView.selectedDateScale),
                            ObjectAnimator.ofFloat(dateText, "translationY", dpToPx(15).toFloat(), 0f),
                            ObjectAnimator.ofFloat(dowText, "scaleX", 3f, 1f),
                            ObjectAnimator.ofFloat(dowText, "scaleY", 3f, 1f),
                            ObjectAnimator.ofFloat(dowText, "translationX", getDowX(),
                                    calendarView!!.minWidth / 2 + bounds.width() / 2 * CalendarView.selectedDateScale + CalendarView.dateMargin),
                            ObjectAnimator.ofFloat(dowText, "translationY", dpToPx(15).toFloat(), CalendarView.dateMargin.toFloat()))
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.start()
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