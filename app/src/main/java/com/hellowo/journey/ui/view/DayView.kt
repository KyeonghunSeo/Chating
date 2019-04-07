package com.hellowo.journey.ui.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.*
import com.hellowo.journey.model.TimeObject
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
import com.hellowo.journey.adapter.TimeObjectListAdapter
import com.hellowo.journey.adapter.util.ListDiffCallback
import com.hellowo.journey.manager.*
import com.hellowo.journey.adapter.util.TimeObjectListComparator
import com.hellowo.journey.model.KoreanLunarCalendar
import java.util.Calendar.SATURDAY
import java.util.Calendar.SUNDAY


class DayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : CardView(context, attrs, defStyleAttr) {
    companion object {
        const val headerTextScale = 4f
        val datePosX = dpToPx(10f)
        val datePosY = -dpToPx(20f)
        val dowPosX = -dpToPx(0f)
        val dowPosY = dpToPx(3f)
        val holiPosX = dpToPx(15.3f)
        val holiPosY = -dpToPx(7.8f)
        val startZ = dpToPx(8f)
        val endZ = dpToPx(0f)
        val subScale = 0.4f
    }
    val targetCal = Calendar.getInstance()
    private var timeObjectList: RealmResults<TimeObject>? = null
    private val currentList = ArrayList<TimeObject>()
    private val newList = ArrayList<TimeObject>()

    private val eventAdapter = TimeObjectListAdapter(context, currentList, targetCal) { view, timeObject, action ->
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
        setGlobalTheme(rootLy)
        setCardBackgroundColor(CalendarManager.backgroundColor)
        initRecyclerView()
        elevation = 0f
        radius = 0f
        dateText.typeface = AppTheme.boldFont
        dowText.typeface = AppTheme.boldFont
        holiText.typeface = AppTheme.regularFont
        dateLy.clipChildren = false
        dateLy.pivotX = 0f
        dateLy.pivotY = 0f
        dowText.pivotX = 0f
        dowText.pivotY = 0f
        holiText.pivotX = 0f
        holiText.pivotY = 0f
        (dateLy.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.NO_GRAVITY
        bar.visibility = View.GONE
        todayBar.visibility = View.GONE
        todayFlag.visibility = View.GONE
        topShadow.visibility = View.GONE
        setDateClosedStyle()
    }

    private fun initRecyclerView() {
        timeObjectListView.layoutManager = LinearLayoutManager(context)
        timeObjectListView.adapter = eventAdapter
        timeObjectListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(timeObjectListView.computeVerticalScrollOffset() > 0) topShadow.visibility = View.VISIBLE
                else topShadow.visibility = View.GONE
            }
        })
        eventAdapter.itemTouchHelper?.attachToRecyclerView(timeObjectListView)
    }

    fun notifyDateChanged() {
        startTime = getCalendarTime0(targetCal)
        endTime = getCalendarTime23(targetCal)
        timeObjectList?.removeAllChangeListeners()
        timeObjectList = TimeObjectManager.getTimeObjectList(startTime, endTime)
        timeObjectList?.addChangeListener { result, changeSet ->
            val t = System.currentTimeMillis()
            if(changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                updateData(result, currentList)
                eventAdapter.notifyDataSetChanged()
            }else if(changeSet.state == OrderedCollectionChangeSet.State.UPDATE) {
                updateData(result, newList)
                updateChange(eventAdapter, currentList, newList)
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

    private fun updateData(data: RealmResults<TimeObject>, list: ArrayList<TimeObject>) {
        list.clear()
        collocateData(data, list)

        OsCalendarManager.getInstances(context, "", startTime, endTime).forEach {
            if(it.dtStart < endTime && it.dtEnd > startTime) list.add(it)
        }

        list.sortWith(TimeObjectListComparator())

        if(list.isNotEmpty()) {
            emptyLy.visibility = View.GONE
        }else {
            emptyLy.visibility = View.VISIBLE
        }
    }

    private fun collocateData(data: RealmResults<TimeObject>, e: ArrayList<TimeObject>) {
        data.forEach { timeObject ->
            try{
                if(timeObject.repeat.isNullOrEmpty()) {
                    e.add(timeObject.makeCopyObject())
                }else {
                    RepeatManager.makeRepeatInstance(timeObject, startTime, endTime).forEach { e.add(it) }
                }
            }catch (e: Exception){ e.printStackTrace() }
        }
    }

    private fun updateChange(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
                             o: ArrayList<TimeObject>, n: ArrayList<TimeObject>) {
        Thread {
            val diffResult = DiffUtil.calculateDiff(ListDiffCallback(o, n))
            Handler(Looper.getMainLooper()).post{
                o.clear()
                o.addAll(n)
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
        currentList.clear()
        eventAdapter.notifyDataSetChanged()
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

        val holiday = HolidayManager.getHoliday(
                String.format("%02d%02d", targetCal.get(Calendar.MONTH) + 1, targetCal.get(Calendar.DATE)),
                lunarCalendar.lunarKey)

        val color = if(holiday?.isHoli == true || targetCal.get(Calendar.DAY_OF_WEEK) == SUNDAY) {
            CalendarManager.sundayColor
        }else if(targetCal.get(Calendar.DAY_OF_WEEK) == SATURDAY) {
            CalendarManager.saturdayColor
        }else {
            CalendarManager.dateColor
        }
        dateText.setTextColor(color)
        dowText.setTextColor(color)
        holiText.setTextColor(color)
        flag.setColorFilter(color)

        when {
            holiday != null -> holiText.text = holiday.title
            AppStatus.isLunarDisplay -> holiText.text = lunarCalendar.lunarSimpleFormat
            else -> holiText.text = ""
        }

        if(isSameDay(CalendarView.todayCal, targetCal)) {
            flag.visibility = View.VISIBLE
        }else {
            flag.visibility = View.GONE
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
        contentLy.visibility = View.GONE
        val animSet = AnimatorSet()
        animSet.playTogether(
                ObjectAnimator.ofFloat(dateLy, "scaleX", headerTextScale, CalendarView.selectedDateScale),
                ObjectAnimator.ofFloat(dateLy, "scaleY", headerTextScale, CalendarView.selectedDateScale),
                ObjectAnimator.ofFloat(dowText, "scaleX", subScale, 1f),
                ObjectAnimator.ofFloat(dowText, "scaleY", subScale, 1f),
                ObjectAnimator.ofFloat(holiText, "scaleX", subScale, 1f),
                ObjectAnimator.ofFloat(holiText, "scaleY", subScale, 1f),
                ObjectAnimator.ofFloat(dateLy, "translationX", datePosX, 0f),
                ObjectAnimator.ofFloat(dateLy, "translationY", datePosY, 0f),
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
        dateLy.translationX = datePosX
        dateLy.translationY = datePosY
        dowText.translationX = dowPosX
        dowText.translationY = dowPosY
        holiText.translationX = holiPosX
        holiText.translationY = holiPosY
    }

    private fun setDateClosedStyle() {
        contentLy.visibility = View.GONE
        dateLy.scaleY = CalendarView.selectedDateScale
        dateLy.scaleX = CalendarView.selectedDateScale
        dowText.scaleY = 1f
        dowText.scaleX = 1f
        holiText.scaleY = 1f
        holiText.scaleX = 1f
        dateLy.translationX = 0f
        dateLy.translationY = 0f
        dowText.translationX = 0f
        dowText.translationY = 0f
        holiText.translationX = 0f
        holiText.translationY = 0f
    }
}