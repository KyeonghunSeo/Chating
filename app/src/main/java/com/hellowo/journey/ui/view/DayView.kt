package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import com.hellowo.journey.manager.*
import com.hellowo.journey.util.EventListComparator
import com.hellowo.journey.util.KoreanLunarCalendar
import com.hellowo.journey.util.NoteListComparator
import java.util.Calendar.SATURDAY
import java.util.Calendar.SUNDAY


class DayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : CardView(context, attrs, defStyleAttr) {
    companion object {
        const val headerTextScale = 4f
        val datePosX = dpToPx(9f)
        val datePosY = -dpToPx(13f)
        val dowPosX = -dpToPx(2f)
        val dowPosY = dpToPx(3f)
        val holiPosX = dpToPx(14.2f)
        val holiPosY = -dpToPx(7.5f)
        val startZ = dpToPx(10f)
        val endZ = dpToPx(0f)
        val subScale = 0.4f
    }
    val targetCal = Calendar.getInstance()
    private var timeObjectList: RealmResults<TimeObject>? = null
    private val eventList = ArrayList<TimeObject>()
    private val taskList = ArrayList<TimeObject>()
    private val noteList = ArrayList<TimeObject>()
    private val newEventList = ArrayList<TimeObject>()
    private val newTaskList = ArrayList<TimeObject>()
    private val newNoteList = ArrayList<TimeObject>()

    private val eventAdapter = EventListAdapter(context, eventList, targetCal) { view, timeObject, action ->
        when(action) {
            0 -> onItemClick(view, timeObject)
        }
    }

    private val taskAdapter = TaskListAdapter(context, taskList, targetCal) { view, timeObject, action ->
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
    private val noteAdapter = NoteListAdapter(context, noteList, targetCal) { view, timeObject, action ->
        when(action) {
            0 -> onItemClick(view, timeObject)
        }
    }

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
        holiText.typeface = CalendarManager.selectFont
        dateLy.clipChildren = false
        dateLy.pivotX = 0f
        dateLy.pivotY = 0f
        dowText.pivotX = 0f
        dowText.pivotY = 0f
        holiText.pivotX = 0f
        holiText.pivotY = 0f
        (dateLy.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.NO_GRAVITY
        bar.visibility = View.GONE
        setDateClosedStyle()
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

    fun notifyDateChanged() {
        startTime = getCalendarTime0(targetCal)
        endTime = getCalendarTime23(targetCal)
        timeObjectList?.removeAllChangeListeners()
        timeObjectList = TimeObjectManager.getTimeObjectList(startTime, endTime)
        timeObjectList?.addChangeListener { result, changeSet ->
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
            l("${AppDateFormat.mdDate.format(targetCal.time)} 데이뷰 갱신 : ${(System.currentTimeMillis() - t) / 1000f} 초")
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

        if(e.isNotEmpty()) {
            eventHeaderLy.visibility = View.VISIBLE
        }else {
            eventHeaderLy.visibility = View.GONE
        }

        if(t.isNotEmpty()) {
            taskHeaderLy.visibility = View.VISIBLE
        }else {
            taskHeaderLy.visibility = View.GONE
        }

        if(n.isNotEmpty()) {
            noteHeaderLy.visibility = View.VISIBLE
        }else {
            noteHeaderLy.visibility = View.GONE
        }
    }

    private fun collocateData(data: RealmResults<TimeObject>, e: ArrayList<TimeObject>,
                              t: ArrayList<TimeObject>, n: ArrayList<TimeObject>) {
        data.forEach { timeObject ->
            when(TimeObject.Type.values()[timeObject.type]) {
                TimeObject.Type.EVENT, TimeObject.Type.TERM -> {
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

    fun clear() {
        timeObjectList?.removeAllChangeListeners()
        eventList.clear()
        taskList.clear()
        noteList.clear()
        eventAdapter.notifyDataSetChanged()
        taskAdapter.notifyDataSetChanged()
        noteAdapter.notifyDataSetChanged()
        setDateClosedStyle()
    }

    fun initTime(time: Long) {
        targetCal.timeInMillis = time
        setDateText()
    }

    private fun setDateText() {
        dateText.text = String.format("%02d", targetCal.get(Calendar.DATE))
        dowText.text = AppDateFormat.simpleDow.format(targetCal.time)

        val lunarCalendar = KoreanLunarCalendar.getInstance()
        lunarCalendar.setSolarDate(targetCal.get(Calendar.YEAR),
                targetCal.get(Calendar.MONTH) + 1,
                targetCal.get(Calendar.DATE))

        val holi = HolidayManager.getHoliday(
                String.format("%02d%02d", targetCal.get(Calendar.MONTH) + 1, targetCal.get(Calendar.DATE)),
                lunarCalendar.lunarKey)

        val color = if(!holi.isNullOrEmpty() || targetCal.get(Calendar.DAY_OF_WEEK) == SUNDAY) {
            CalendarManager.sundayColor
        }else if(targetCal.get(Calendar.DAY_OF_WEEK) == SATURDAY) {
            CalendarManager.saturdayColor
        }else {
            CalendarManager.dateColor
        }
        dateText.setTextColor(color)
        dowText.setTextColor(color)
        holiText.setTextColor(color)
        todayIndi.setColorFilter(color)

        if(!holi.isNullOrEmpty()) {
            holiText.text = holi
        }else if(lunarCalendar.lunarDay == 1 || lunarCalendar.lunarDay == 10 || lunarCalendar.lunarDay == 20) {
            holiText.text = lunarCalendar.lunarSimpleFormat
        }else {
            holiText.text = ""
        }

        if(isSameDay(CalendarView.todayCal, targetCal)) {
            todayIndi.visibility = View.VISIBLE
        }else {
            todayIndi.visibility = View.GONE
        }
    }

    fun show(dayPagerView: DayPagerView) {
        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(dayPagerView, "elevation", startZ, endZ),
                ObjectAnimator.ofFloat(dayPagerView, "alpha", 0.85f, 1f),
                ObjectAnimator.ofFloat(dateLy, "scaleX", CalendarView.selectedDateScale, headerTextScale),
                ObjectAnimator.ofFloat(dateLy, "scaleY", CalendarView.selectedDateScale, headerTextScale),
                ObjectAnimator.ofFloat(dowText, "scaleX", 1f, subScale),
                ObjectAnimator.ofFloat(dowText, "scaleY", 1f, subScale),
                ObjectAnimator.ofFloat(holiText, "scaleX", 1f, subScale),
                ObjectAnimator.ofFloat(holiText, "scaleY", 1f, subScale),
                ObjectAnimator.ofFloat(todayIndi, "scaleX", 1f, subScale),
                ObjectAnimator.ofFloat(todayIndi, "scaleY", 1f, subScale),
                ObjectAnimator.ofFloat(dateLy, "translationX", 0f, datePosX),
                ObjectAnimator.ofFloat(dateLy, "translationY", 0f, datePosY),
                ObjectAnimator.ofFloat(dowText, "translationX", 0f, dowPosX),
                ObjectAnimator.ofFloat(dowText, "translationY", 0f, dowPosY),
                ObjectAnimator.ofFloat(holiText, "translationX", 0f, holiPosX),
                ObjectAnimator.ofFloat(holiText, "translationY", 0f, holiPosY))
        animSet.duration = ANIM_DUR
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
    }

    fun hide() {
        contentLy.visibility = View.INVISIBLE
        val animSet = AnimatorSet()
        animSet.playTogether(
                ObjectAnimator.ofFloat(dateLy, "scaleX", headerTextScale, CalendarView.selectedDateScale),
                ObjectAnimator.ofFloat(dateLy, "scaleY", headerTextScale, CalendarView.selectedDateScale),
                ObjectAnimator.ofFloat(dowText, "scaleX", subScale, 1f),
                ObjectAnimator.ofFloat(dowText, "scaleY", subScale, 1f),
                ObjectAnimator.ofFloat(holiText, "scaleX", subScale, 1f),
                ObjectAnimator.ofFloat(holiText, "scaleY", subScale, 1f),
                ObjectAnimator.ofFloat(todayIndi, "scaleX", subScale, 1f),
                ObjectAnimator.ofFloat(todayIndi, "scaleY", subScale, 1f),
                ObjectAnimator.ofFloat(dateLy, "translationX", datePosX, 0f),
                ObjectAnimator.ofFloat(dateLy, "translationY", datePosX, 0f),
                ObjectAnimator.ofFloat(dowText, "translationX", dowPosX, 0f),
                ObjectAnimator.ofFloat(dowText, "translationY", dowPosY, 0f),
                ObjectAnimator.ofFloat(holiText, "translationX", holiPosX, 0f),
                ObjectAnimator.ofFloat(holiText, "translationY", holiPosY, 0f))
        animSet.duration = ANIM_DUR
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
    }

    fun setDateOpenedStyle() {
        contentLy.visibility = View.VISIBLE
        dateLy.scaleY = headerTextScale
        dateLy.scaleX = headerTextScale
        dowText.scaleY = subScale
        dowText.scaleX = subScale
        holiText.scaleY = subScale
        holiText.scaleX = subScale
        todayIndi.scaleY = subScale
        todayIndi.scaleX = subScale
        dateLy.translationX = datePosX
        dateLy.translationY = datePosY
        dowText.translationX = dowPosX
        dowText.translationY = dowPosY
        holiText.translationX = holiPosX
        holiText.translationY = holiPosY
    }

    private fun setDateClosedStyle() {
        contentLy.visibility = View.INVISIBLE
        dateLy.scaleY = CalendarView.selectedDateScale
        dateLy.scaleX = CalendarView.selectedDateScale
        dowText.scaleY = 1f
        dowText.scaleX = 1f
        holiText.scaleY = 1f
        holiText.scaleX = 1f
        todayIndi.scaleY = 1f
        todayIndi.scaleX = 1f
        dateLy.translationX = 0f
        dateLy.translationY = 0f
        dowText.translationX = 0f
        dowText.translationY = 0f
        holiText.translationX = 0f
        holiText.translationY = 0f
    }
}