package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.journey.*
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.adapter.EventListAdapter
import com.hellowo.journey.adapter.TaskListAdapter
import com.hellowo.journey.calendar.util.TaskListComparator
import com.hellowo.journey.calendar.TimeObjectManager
import com.hellowo.journey.calendar.CalendarSkin
import com.hellowo.journey.calendar.RepeatManager
import com.hellowo.journey.ui.activity.MainActivity
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmResults
import kotlinx.android.synthetic.main.view_day.view.*
import kotlinx.android.synthetic.main.view_selected_bar.view.*
import java.util.*
import kotlin.collections.ArrayList
import android.os.Looper
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.hellowo.journey.adapter.NoteListAdapter
import com.hellowo.journey.adapter.util.ListDiffCallback
import com.hellowo.journey.calendar.util.EventListComparator
import com.hellowo.journey.calendar.util.NoteListComparator


class DayView @JvmOverloads constructor(private val calendarView: CalendarView,
                                        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : CardView(context, attrs, defStyleAttr) {
    companion object {
        const val headerTextScale = 2.5f
        val datePosX = dpToPx(15f)
        val datePosY = dpToPx(7f)
    }
    private var timeObjectList: RealmResults<TimeObject>? = null
    private val eventList = ArrayList<TimeObject>()
    private val taskList = ArrayList<TimeObject>()
    private val noteList = ArrayList<TimeObject>()
    private val newEventList = ArrayList<TimeObject>()
    private val newTaskList = ArrayList<TimeObject>()
    private val newNoteList = ArrayList<TimeObject>()

    private val eventAdapter = EventListAdapter(context, eventList, calendarView.selectedCal)
    { view, timeObject, action ->
        when(action) {
            0 -> onItemClick(view, timeObject)
        }
    }
    private val taskAdapter = TaskListAdapter(context, taskList, calendarView.selectedCal)
    { view, timeObject, action ->
        when(action) {
            0 -> onItemClick(view, timeObject)
            1 -> {
                if(!timeObject.isDone() && taskList.filter { !it.isDone() }.size == 1) {
                    taskFinishAnimView.visibility = View.VISIBLE
                    taskFinishAnimView.playAnimation()
                }else {
                    taskFinishAnimView.visibility = View.GONE
                }
                TimeObjectManager.done(timeObject)
            }
        }
    }
    private val noteAdapter = NoteListAdapter(context, noteList, calendarView.selectedCal)
    { view, timeObject, action ->
        when(action) {
            0 -> onItemClick(view, timeObject)
        }
    }

    var viewMode = ViewMode.CLOSED
    var onVisibility: ((Boolean) -> Unit)? = null
    var startTime: Long = 0
    var endTime: Long = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.view_day, this, true)
        //rootLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        rootLy.setOnClickListener {}
        setCardBackgroundColor(Color.WHITE)
        initRecyclerView()
        elevation = 0f
        dateText.typeface = CalendarSkin.selectFont
        dateText.scaleY = CalendarView.selectedDateScale
        dateText.scaleX = CalendarView.selectedDateScale
        dateText.translationY = CalendarView.selectedDatePosition
        (bar.layoutParams as FrameLayout.LayoutParams).let {
            it.marginEnd = normalMargin.toInt()
            it.marginStart = normalMargin.toInt()
        }

        taskFinishAnimView.imageAssetsFolder = "assets/"
        taskFinishAnimView.setAnimation("success.json")
        taskFinishAnimView.visibility = View.GONE

        contentLy.visibility = View.INVISIBLE
    }

    private fun initRecyclerView() {
        eventListView.layoutManager = LinearLayoutManager(context)
        eventListView.adapter = eventAdapter
        eventAdapter.itemTouchHelper?.attachToRecyclerView(eventListView)

        taskListView.layoutManager = LinearLayoutManager(context)
        taskListView.adapter = taskAdapter
        taskAdapter.itemTouchHelper?.attachToRecyclerView(taskListView)

        noteListView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        noteListView.adapter = noteAdapter
        noteAdapter.itemTouchHelper?.attachToRecyclerView(noteListView)
    }

    fun notifyDateChanged(offset: Int) {
        setDateText()

        calendarView.selectedCal.let { cal ->
            startTime = getCalendarTime0(cal)
            endTime = getCalendarTime23(cal)
            timeObjectList?.removeAllChangeListeners()
            timeObjectList = TimeObjectManager.getTimeObjectList(startTime, endTime)
            timeObjectList?.addChangeListener { result, changeSet ->
                l("==========데이뷰 데이터 변경 시작=========")
                val t = System.currentTimeMillis()
                if(changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                    updateData(result, eventList, taskList, noteList)
                    eventAdapter.notifyDataSetChanged()
                    taskAdapter.notifyDataSetChanged()
                    noteAdapter.notifyDataSetChanged()
                }else if(changeSet.state == OrderedCollectionChangeSet.State.UPDATE) {
                    updateData(result, newEventList, newTaskList, newNoteList)
                    updateChange(eventAdapter, eventList, newEventList)
                    updateChange(taskAdapter, taskList, newTaskList)
                    updateChange(noteAdapter, noteList, newNoteList)
                }
                l("걸린시간 : ${(System.currentTimeMillis() - t) / 1000f} 초")
                l("==========데이뷰 데이터 변경 종료=========")
            }
        }

        if(offset != 0) {
            startPagingEffectAnimation(offset, contentLy, null)
            val animSet = AnimatorSet()
            animSet.playTogether(
                    ObjectAnimator.ofFloat(flagImg, "scaleY", 0f, headerTextScale),
                    ObjectAnimator.ofFloat(dowText, "scaleY", 0f, headerTextScale))
            animSet.duration = 500
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.start()
        }
    }

    private fun updateData(data: RealmResults<TimeObject>, e: ArrayList<TimeObject>,
                               t: ArrayList<TimeObject>, n: ArrayList<TimeObject>) {
        e.clear()
        t.clear()
        n.clear()
        collocateData(data, e, t, n)
        e.sortWith(EventListComparator())
        t.sortWith(TaskListComparator())
        n.sortWith(NoteListComparator())
    }

    private fun collocateData(data: RealmResults<TimeObject>, e: ArrayList<TimeObject>,
                              t: ArrayList<TimeObject>, n: ArrayList<TimeObject>) {
        data.forEach { timeObject ->
            when(TimeObject.Type.values()[timeObject.type]) {
                TimeObject.Type.EVENT -> {
                    try{
                        if(timeObject.repeat.isNullOrEmpty()) {
                            e.add(timeObject.makeCopyObject())
                        }else {
                            RepeatManager.makeRepeatInstance(timeObject, startTime, endTime)
                                    .forEach { e.add(it) }
                        }
                    }catch (e: Exception){ e.printStackTrace() }
                }
                TimeObject.Type.TASK -> {
                    try{
                        if(timeObject.repeat.isNullOrEmpty()) {
                            t.add(timeObject.makeCopyObject())
                        }else {
                            RepeatManager.makeRepeatInstance(timeObject, startTime, endTime)
                                    .forEach { t.add(it) }
                        }
                    }catch (e: Exception){ e.printStackTrace() }
                }
                TimeObject.Type.NOTE -> n.add(timeObject.makeCopyObject())
                else -> {
                    n.add(timeObject.makeCopyObject())
                }
            }
        }
    }

    private fun updateChange(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
                             o: ArrayList<TimeObject>, n: ArrayList<TimeObject>) {
        Thread {
            val diffResult = DiffUtil.calculateDiff(ListDiffCallback(o, n))
            o.clear()
            o.addAll(n)
            Handler(Looper.getMainLooper()).post{
                diffResult.dispatchUpdatesTo(adapter)
            }
        }.start()
    }

    private fun onItemClick(view: View?, timeObject: TimeObject) {
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
        calendarView.let {
            if(viewMode == ViewMode.OPENED) {
                dateText.text = AppRes.date.format(it.selectedCal.time)
            }else {
                dateText.text = it.selectedCal.get(Calendar.DATE).toString()
            }
            dowText.text = AppRes.simpleDow.format(it.selectedCal.time)
            val color = it.getDateTextColor(it.postSelectedNum)
            dateText.typeface = AppRes.thinFont
            dowText.typeface = AppRes.thinFont
            dateText.setTextColor(color)
            flagImg.setColorFilter(color)
            if(it.postSelectedNum == it.todayCellNum) {
                flagImg.setImageResource(R.drawable.flag_today)
            }else {
                flagImg.setImageResource(R.drawable.flag_to_bottom)
            }
        }
    }



    fun show() {
        viewMode = ViewMode.ANIMATING
        visibility = View.VISIBLE
        alpha = 0.85f

        setDateText()

        calendarView.getSelectedView().let { dateLy ->
            val location = IntArray(2)
            dateLy.getLocationInWindow(location)
            layoutParams = FrameLayout.LayoutParams(dateLy.width, dateLy.height).apply {
                setMargins(location[0], location[1] - AppRes.statusBarHeight, 0, 0)
            }

            val animSet = AnimatorSet()
            animSet.playTogether(ObjectAnimator.ofFloat(this@DayView, "elevation", 0f, dpToPx(15).toFloat()))
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
                            dateText.text = AppRes.date.format(calendarView.selectedCal.time)
                            viewMode = ViewMode.OPENED
                            contentLy.visibility = View.VISIBLE
                            onVisibility?.invoke(true)
                        }
                        override fun onTransitionResume(transition: Transition) {}
                        override fun onTransitionPause(transition: Transition) {}
                        override fun onTransitionCancel(transition: Transition) {}
                        override fun onTransitionStart(transition: Transition) {
                            notifyDateChanged(0)

                            val animSet = AnimatorSet()
                            animSet.playTogether(ObjectAnimator.ofFloat(this@DayView, "elevation", dpToPx(15).toFloat(), 0f),
                                    ObjectAnimator.ofFloat(this@DayView, "alpha", 0.85f, 1f),
                                    ObjectAnimator.ofFloat(dateText, "scaleX", dateText.scaleX, headerTextScale),
                                    ObjectAnimator.ofFloat(dateText, "scaleY", dateText.scaleY, headerTextScale),
                                    ObjectAnimator.ofFloat(dateText, "translationX", dateText.translationX, datePosX),
                                    ObjectAnimator.ofFloat(dateText, "translationY", dateText.translationY, datePosY),
                                    ObjectAnimator.ofFloat(dowText, "scaleX", dowText.scaleX, headerTextScale),
                                    ObjectAnimator.ofFloat(dowText, "scaleY", dowText.scaleY, headerTextScale),
                                    ObjectAnimator.ofFloat(dowText, "translationX", dowText.translationX, -bigMargin),
                                    ObjectAnimator.ofFloat(flagImg, "scaleX", flagImg.scaleX, headerTextScale),
                                    ObjectAnimator.ofFloat(flagImg, "scaleY", flagImg.scaleY, headerTextScale),
                                    ObjectAnimator.ofFloat(flagImg, "translationX", flagImg.translationX, -bigMargin))
                            animSet.duration = ANIM_DUR
                            animSet.interpolator = FastOutSlowInInterpolator()
                            animSet.start()
                        }
                    })
                    TransitionManager.beginDelayedTransition(this@DayView, transiion)
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                        setMargins(0, mainBarHeight, 0, mainBarHeight)
                    }
                }
                override fun onAnimationCancel(p0: Animator?) {}
                override fun onAnimationStart(p0: Animator?) {}
            })
            animSet.start()
        }
    }

    fun hide() {
        calendarView.getSelectedView().let { dateLy ->
            dateText.text = calendarView.selectedCal?.get(Calendar.DATE).toString()
            elevation = dpToPx(15).toFloat()
            viewMode = ViewMode.ANIMATING

            val location = IntArray(2)
            dateLy.getLocationInWindow(location)
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
                    onVisibility?.invoke(false)
                    contentLy.visibility = View.INVISIBLE
                    val bounds = Rect()
                    dateText.paint.getTextBounds(dateText.text.toString(), 0, dateText.text.length, bounds)
                    val animSet = AnimatorSet()
                    animSet.playTogether(ObjectAnimator.ofFloat(dateText, "alpha", 1f, 1f),
                            ObjectAnimator.ofFloat(dateText, "scaleX", dateText.scaleX, CalendarView.selectedDateScale),
                            ObjectAnimator.ofFloat(dateText, "scaleY", dateText.scaleY, CalendarView.selectedDateScale),
                            ObjectAnimator.ofFloat(dateText, "translationX", dateText.translationX, 0f),
                            ObjectAnimator.ofFloat(dateText, "translationY", dateText.translationY, 0f),
                            ObjectAnimator.ofFloat(dowText, "scaleX", dowText.scaleX, 1f),
                            ObjectAnimator.ofFloat(dowText, "scaleY", dowText.scaleY, 1f),
                            ObjectAnimator.ofFloat(dowText, "translationX", dowText.translationX, 0f),
                            ObjectAnimator.ofFloat(flagImg, "scaleX", flagImg.scaleX, 1f),
                            ObjectAnimator.ofFloat(flagImg, "scaleY", flagImg.scaleY, 1f),
                            ObjectAnimator.ofFloat(flagImg, "translationX", flagImg.translationX, 0f))
                    animSet.duration = ANIM_DUR
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.start()
                }
            })
            TransitionManager.beginDelayedTransition(this, transiion)
            layoutParams = FrameLayout.LayoutParams(dateLy.width, dateLy.height).apply {
                setMargins(location[0], location[1] - AppRes.statusBarHeight, 0, 0)
            }
        }
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}