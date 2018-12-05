package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ContentUris
import android.content.Context
import android.content.Intent
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
import com.hellowo.journey.util.TaskListComparator
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.manager.CalendarManager
import com.hellowo.journey.manager.RepeatManager
import com.hellowo.journey.ui.activity.MainActivity
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmResults
import kotlinx.android.synthetic.main.view_day.view.*
import kotlinx.android.synthetic.main.view_selected_bar.view.*
import java.util.*
import kotlin.collections.ArrayList
import android.os.Looper
import android.provider.CalendarContract
import android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME
import android.provider.CalendarContract.EXTRA_EVENT_END_TIME
import android.view.Gravity
import android.widget.FrameLayout
import com.hellowo.journey.adapter.NoteListAdapter
import com.hellowo.journey.adapter.util.ListDiffCallback
import com.hellowo.journey.manager.OsCalendarManager
import com.hellowo.journey.util.EventListComparator
import com.hellowo.journey.util.NoteListComparator


class DayView @JvmOverloads constructor(private val calendarView: CalendarView,
                                        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : CardView(context, attrs, defStyleAttr) {
    companion object {
        const val headerTextScale = 2.5f
        val datePosX = dpToPx(7f)
        val datePosY = dpToPx(7f)
        val startZ = dpToPx(10f)
        val endZ = dpToPx(0f)
    }
    private var timeObjectList: RealmResults<TimeObject>? = null
    private val eventList = ArrayList<TimeObject>()
    private val taskList = ArrayList<TimeObject>()
    private val noteList = ArrayList<TimeObject>()
    private val newEventList = ArrayList<TimeObject>()
    private val newTaskList = ArrayList<TimeObject>()
    private val newNoteList = ArrayList<TimeObject>()

    private val eventAdapter = EventListAdapter(context, eventList, calendarView.targetCal) { view, timeObject, action ->
        when(action) {
            0 -> onItemClick(view, timeObject)
        }
    }
    private val taskAdapter = TaskListAdapter(context, taskList, calendarView.targetCal) { view, timeObject, action ->
        when(action) {
            0 -> onItemClick(view, timeObject)
            1 -> {
                if(!timeObject.isDone() && taskList.filter { !it.isDone() }.size == 1) {
                }else {
                }
                vibrate(context)
                TimeObjectManager.done(timeObject)
            }
        }
    }
    private val noteAdapter = NoteListAdapter(context, noteList, calendarView.targetCal) { view, timeObject, action ->
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
        setCardBackgroundColor(CalendarManager.backgroundColor)
        initRecyclerView()
        elevation = 0f
        radius = 0f
        dateText.typeface = CalendarManager.selectFont
        dowText.typeface = CalendarManager.selectFont
        dateLy.clipChildren = false
        dateLy.scaleY = CalendarView.selectedDateScale
        dateLy.scaleX = CalendarView.selectedDateScale
        (dateLy.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.NO_GRAVITY
        bar.visibility = View.GONE
/*
        taskFinishAnimView.imageAssetsFolder = "assets/"
        taskFinishAnimView.setAnimation("success.json")
        taskFinishAnimView.visibility = View.GONE
*/
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

        calendarView.targetCal.let { cal ->
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
/*
                val imageItem = result.firstOrNull { item -> item.links.any{ it.type == Link.Type.IMAGE.ordinal } }

                if(imageItem != null) {
                    val imageLink = imageItem.links.first { it.type == Link.Type.IMAGE.ordinal }
                    Glide.with(context).load(imageLink.data).into(headerCoverImg)
                    headerCoverImg.setColorFilter(resource.getColor(R.color.transitionDimWhite), PorterDuff.Mode.SRC_OVER)
                }else {
                    headerCoverImg.setImageBitmap(null)
                }*/
                l("걸린시간 : ${(System.currentTimeMillis() - t) / 1000f} 초")
                l("==========데이뷰 데이터 변경 종료=========")
            }
        }

        if(offset != 0) {
            startPagingEffectAnimation(offset, contentLy, null)
            val animSet = AnimatorSet()
            animSet.playTogether()
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

        OsCalendarManager.getInstances(context, "", startTime, endTime).forEach {
            if(it.dtStart < endTime && it.dtEnd > startTime) e.add(it)
        }

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
            if(timeObject.id?.startsWith("osInstance::") == true) {
                val eventId = timeObject.id!!.substring("osInstance::".length, timeObject.id!!.length).toLong()
                val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
                val intent = Intent(Intent.ACTION_VIEW).setData(uri)
                if(timeObject.allday) {
                    intent.putExtra(EXTRA_EVENT_BEGIN_TIME, timeObject.dtUpdated)
                    intent.putExtra(EXTRA_EVENT_END_TIME, timeObject.dtCreated)
                }else {
                    intent.putExtra(EXTRA_EVENT_BEGIN_TIME, timeObject.dtStart)
                    intent.putExtra(EXTRA_EVENT_END_TIME, timeObject.dtEnd)
                }
                MainActivity.instance?.startActivityForResult(intent, RC_OS_CALENDAR)
            }else {
                it.targetTimeObject.value = timeObject
                it.targetView.value = view
            }
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
            dateText.text = it.targetCal.get(Calendar.DATE).toString()
            dowText.text = AppDateFormat.dow.format(it.targetCal.time)
            val color = it.getDateTextColor(it.targetCellNum)
            dateText.setTextColor(color)
            dowText.setTextColor(color)
        }
    }

    fun show() {
        viewMode = ViewMode.ANIMATING
        visibility = View.VISIBLE
        alpha = 0.85f

        setDateText()

        calendarView.getSelectedView().let { dateCell ->
            val location = IntArray(2)
            dateCell.getLocationInWindow(location)
            layoutParams = FrameLayout.LayoutParams(dateCell.width, dateCell.height).apply {
                setMargins(location[0], location[1] - AppDateFormat.statusBarHeight, 0, 0)
            }

            val animSet = AnimatorSet()
            animSet.playTogether(ObjectAnimator.ofFloat(this@DayView, "elevation", 0f, startZ))
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
                            contentLy.visibility = View.VISIBLE
                            onVisibility?.invoke(true)
                        }
                        override fun onTransitionResume(transition: Transition) {}
                        override fun onTransitionPause(transition: Transition) {}
                        override fun onTransitionCancel(transition: Transition) {}
                        override fun onTransitionStart(transition: Transition) {
                            notifyDateChanged(0)
                            val animSet = AnimatorSet()
                            animSet.playTogether(ObjectAnimator.ofFloat(this@DayView, "elevation", startZ, endZ),
                                    ObjectAnimator.ofFloat(this@DayView, "alpha", 0.85f, 1f),
                                    ObjectAnimator.ofFloat(dateLy, "scaleX", dateLy.scaleX, headerTextScale),
                                    ObjectAnimator.ofFloat(dateLy, "scaleY", dateLy.scaleY, headerTextScale),
                                    ObjectAnimator.ofFloat(dateLy, "translationX", dateLy.translationX, datePosX),
                                    ObjectAnimator.ofFloat(dateLy, "translationY", dateLy.translationY, datePosY))
                            animSet.duration = ANIM_DUR
                            animSet.interpolator = FastOutSlowInInterpolator()
                            animSet.start()
                        }
                    })
                    TransitionManager.beginDelayedTransition(this@DayView, transiion)
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                        setMargins(0, dpToPx(40), 0, mainBarHeight)
                    }
                }
                override fun onAnimationCancel(p0: Animator?) {}
                override fun onAnimationStart(p0: Animator?) {}
            })
            animSet.start()
        }
    }

    fun hide() {
        timeObjectList?.removeAllChangeListeners()
        calendarView.getSelectedView().let { dateCell ->
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
                    animSet.playTogether(ObjectAnimator.ofFloat(this@DayView,
                            "elevation", startZ, 0f).setDuration(ANIM_DUR),
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
                    val animSet = AnimatorSet()
                    animSet.playTogether(
                            ObjectAnimator.ofFloat(dateLy, "scaleX", dateLy.scaleX, CalendarView.selectedDateScale),
                            ObjectAnimator.ofFloat(dateLy, "scaleY", dateLy.scaleY, CalendarView.selectedDateScale),
                            ObjectAnimator.ofFloat(dateLy, "translationX", dateLy.translationX, 0f),
                            ObjectAnimator.ofFloat(dateLy, "translationY", dateLy.translationY, 0f))
                    animSet.duration = ANIM_DUR
                    animSet.interpolator = FastOutSlowInInterpolator()
                    animSet.start()
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