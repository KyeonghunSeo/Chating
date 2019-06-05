package com.ayaan.twelvepages.ui.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordListAdapter
import com.ayaan.twelvepages.adapter.util.ListDiffCallback
import com.ayaan.twelvepages.adapter.util.RecordListComparator
import com.ayaan.twelvepages.manager.*
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.dialog.DatePickerDialog
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmResults
import kotlinx.android.synthetic.main.view_day.view.*
import kotlinx.android.synthetic.main.view_selected_bar.view.*
import java.util.*
import java.util.Calendar.SATURDAY
import java.util.Calendar.SUNDAY
import kotlin.collections.ArrayList


class DayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : CardView(context, attrs, defStyleAttr) {

    val targetCal : Calendar = Calendar.getInstance()
    private var recordList: RealmResults<Record>? = null
    private val currentList = ArrayList<Record>()
    private val newList = ArrayList<Record>()
    private val dateInfo = DateInfoManager.DateInfo()

    private val adapter = RecordListAdapter(context, currentList, targetCal) { view, record, action -> }

    var startTime: Long = 0
    var endTime: Long = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.view_day, this, true)
        rootLy.setOnClickListener {}
        setGlobalTheme(rootLy)
        setCardBackgroundColor(CalendarManager.backgroundColor)
        initRecyclerView()
        elevation = 0f
        radius = 0f
        dateText.typeface = AppTheme.regularCFont
        dowText.typeface = AppTheme.regularCFont
        holiText.typeface = AppTheme.regularFont
        dateLy.clipChildren = false
        dateLy.pivotX = 0f
        dateLy.pivotY = 0f
        dowText.pivotX = 0f
        dowText.pivotY = 0f
        holiText.pivotX = 0f
        holiText.pivotY = 0f
        bar.pivotX = 0f
        bar.layoutParams.width = dpToPx(100)
        (dateLy.layoutParams as LayoutParams).gravity = Gravity.NO_GRAVITY
        bar.visibility = View.VISIBLE
        topShadow.visibility = View.GONE
        dateText.setBackgroundResource(AppTheme.selectableItemBackground)
        dateText.setOnClickListener {
            MainActivity.instance?.let {
                showDialog(DatePickerDialog(it, targetCal.timeInMillis) { time ->
                    it.selectDate(time)
                }, true, true, true, false)
            }
        }
        setDateClosedStyle()
    }

    private fun initRecyclerView() {
        timeObjectListView.layoutManager = LinearLayoutManager(context)
        timeObjectListView.adapter = adapter
        timeObjectListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(timeObjectListView.computeVerticalScrollOffset() > 0) topShadow.visibility = View.VISIBLE
                else topShadow.visibility = View.GONE
            }
        })
        adapter.itemTouchHelper?.attachToRecyclerView(timeObjectListView)
    }

    fun notifyDateChanged() {
        startTime = getCalendarTime0(targetCal)
        endTime = getCalendarTime23(targetCal)
        recordList?.removeAllChangeListeners()
        recordList = RecordManager.getRecordList(startTime, endTime, MainActivity.getTargetFolder())
        recordList?.addChangeListener { result, changeSet ->
            val t = System.currentTimeMillis()
            if(changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                updateData(result, currentList)
                adapter.notifyDataSetChanged()
            }else if(changeSet.state == OrderedCollectionChangeSet.State.UPDATE) {
                if(MainActivity.getDayPagerView()?.isOpened() == true) {
                    updateData(result, newList)
                    updateChange(adapter, currentList, newList)
                }
            }
            l("${AppDateFormat.mdDate.format(targetCal.time)} 데이뷰 갱신 : ${(System.currentTimeMillis() - t) / 1000f} 초")
        }
    }

    private fun updateData(data: RealmResults<Record>, list: ArrayList<Record>) {
        list.clear()
        collocateData(data, list)

        OsCalendarManager.getInstances(context, "", startTime, endTime).forEach {
            if(it.dtStart < endTime && it.dtEnd > startTime) list.add(it)
        }

        list.sortWith(RecordListComparator())

        if(list.isNotEmpty()) {
            emptyLy.visibility = View.GONE
        }else {
            emptyLy.visibility = View.VISIBLE
        }
    }

    private fun collocateData(data: RealmResults<Record>, e: ArrayList<Record>) {
        data.forEach { timeObject ->
            try{
                if(timeObject.repeat.isNullOrEmpty()) {
                    e.add(timeObject.makeCopyObject())
                }else {
                    RepeatManager.makeRepeatInstances(timeObject, startTime, endTime).forEach { e.add(it) }
                }
            }catch (e: Exception){ e.printStackTrace() }
        }
    }

    private fun updateChange(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
                             o: ArrayList<Record>, n: ArrayList<Record>) {
        Thread {
            val diffResult = DiffUtil.calculateDiff(ListDiffCallback(o, n))
            Handler(Looper.getMainLooper()).post{
                o.clear()
                o.addAll(n)
                diffResult.dispatchUpdatesTo(adapter)
            }
        }.start()
    }

    fun clear() {
        recordList?.removeAllChangeListeners()
        currentList.clear()
        adapter.notifyDataSetChanged()
        setDateClosedStyle()
    }

    fun initTime(time: Long) {
        targetCal.timeInMillis = time
        setDateText()
    }

    private fun setDateText() {
        dateText.text = String.format("%02d", targetCal.get(Calendar.DATE))
        dowText.text = AppDateFormat.dowfullEng.format(targetCal.time)
        DateInfoManager.getHoliday(dateInfo, targetCal)
        val color = if(dateInfo.holiday?.isHoli == true || targetCal.get(Calendar.DAY_OF_WEEK) == SUNDAY) {
            CalendarManager.sundayColor
        }else if(targetCal.get(Calendar.DAY_OF_WEEK) == SATURDAY) {
            CalendarManager.saturdayColor
        }else {
            CalendarManager.dateColor
        }
        bar.setBackgroundColor(color)
        dateText.setTextColor(color)
        dowText.setTextColor(color)
        holiText.setTextColor(color)
        if(AppStatus.isDowDisplay) dowText.visibility = View.VISIBLE
        else dowText.visibility = View.GONE
        holiText.text = dateInfo.getSelectedString()
    }

    fun show(dayPagerView: DayPagerView) {
        dowText.text = AppDateFormat.dowfullEng.format(targetCal.time).toUpperCase()
        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(dayPagerView, "alpha", 0.9f, 1f),
                ObjectAnimator.ofFloat(dateLy, "scaleX", 1f, headerTextScale),
                ObjectAnimator.ofFloat(dateLy, "scaleY", 1f, headerTextScale),
                ObjectAnimator.ofFloat(dowText, "scaleX", 1f, dowScale),
                ObjectAnimator.ofFloat(dowText, "scaleY", 1f, dowScale),
                ObjectAnimator.ofFloat(holiText, "scaleX", 1f, holiScale),
                ObjectAnimator.ofFloat(holiText, "scaleY", 1f, holiScale),
                ObjectAnimator.ofFloat(dateLy, "translationX", 0f, datePosX),
                ObjectAnimator.ofFloat(dateLy, "translationY", 0f, datePosY),
                ObjectAnimator.ofFloat(dowText, "translationX", 0f, dowPosX),
                ObjectAnimator.ofFloat(dowText, "translationY", 0f, dowPosY),
                ObjectAnimator.ofFloat(holiText, "translationX", 0f, holiPosX),
                ObjectAnimator.ofFloat(holiText, "translationY", 0f, holiPosY),
                ObjectAnimator.ofFloat(MainActivity.getMonthTextLy(), "scaleX", 1f, mainMonthScale),
                ObjectAnimator.ofFloat(MainActivity.getMonthTextLy(), "scaleY", 1f, mainMonthScale),
                ObjectAnimator.ofFloat(MainActivity.getMonthTextLy(), "translationX", 0f, mainMonthX),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "translationX", 0f, mainDateLyX),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "translationY", 0f, mainDateLyY),
                ObjectAnimator.ofFloat(MainActivity.getWeekTextLy(), "translationY", 0f, weekTextY),
                ObjectAnimator.ofFloat(bar, "scaleX", 1f, 1f),
                ObjectAnimator.ofFloat(bar, "scaleY", 1f, 2f),
                ObjectAnimator.ofFloat(bar, "translationX", 0f, barX),
                ObjectAnimator.ofFloat(bar, "translationY", 0f, barY))
        animSet.duration = ANIM_DUR
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
    }

    fun hide(dayPagerView: DayPagerView) {
        dowText.text = AppDateFormat.dowEng.format(targetCal.time).toUpperCase()
        contentLy.visibility = View.GONE
        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(dayPagerView, "alpha", 1f, 0.9f),
                ObjectAnimator.ofFloat(dateLy, "scaleX", headerTextScale, 1f),
                ObjectAnimator.ofFloat(dateLy, "scaleY", headerTextScale, 1f),
                ObjectAnimator.ofFloat(dowText, "scaleX", dowScale, 1f),
                ObjectAnimator.ofFloat(dowText, "scaleY", dowScale, 1f),
                ObjectAnimator.ofFloat(holiText, "scaleX", holiScale, 1f),
                ObjectAnimator.ofFloat(holiText, "scaleY", holiScale, 1f),
                ObjectAnimator.ofFloat(dateLy, "translationX", datePosX, 0f),
                ObjectAnimator.ofFloat(dateLy, "translationY", datePosY, 0f),
                ObjectAnimator.ofFloat(dowText, "translationX", dowPosX, 0f),
                ObjectAnimator.ofFloat(dowText, "translationY", dowPosY, 0f),
                ObjectAnimator.ofFloat(holiText, "translationX", holiPosX, 0f),
                ObjectAnimator.ofFloat(holiText, "translationY", holiPosY, 0f),
                ObjectAnimator.ofFloat(MainActivity.getMonthTextLy(), "scaleX", mainMonthScale, 1f),
                ObjectAnimator.ofFloat(MainActivity.getMonthTextLy(), "scaleY", mainMonthScale, 1f),
                ObjectAnimator.ofFloat(MainActivity.getMonthTextLy(), "translationX", mainMonthX, 0f),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "translationX", mainDateLyX, 0f),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "translationY", mainDateLyY, 0f),
                ObjectAnimator.ofFloat(MainActivity.getWeekTextLy(), "translationY", weekTextY, 0f),
                ObjectAnimator.ofFloat(bar, "scaleX", 1f, 1f),
                ObjectAnimator.ofFloat(bar, "scaleY", 2f, 1f),
                ObjectAnimator.ofFloat(bar, "translationX", barX, 0f),
                ObjectAnimator.ofFloat(bar, "translationY", barY, 0f))
        animSet.duration = ANIM_DUR
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
    }

    fun setDateOpenedStyle() {
        contentLy.visibility = View.VISIBLE
        dateLy.scaleY = headerTextScale
        dateLy.scaleX = headerTextScale
        dowText.scaleY = dowScale
        dowText.scaleX = dowScale
        holiText.scaleY = holiScale
        holiText.scaleX = holiScale
        dateLy.translationX = datePosX
        dateLy.translationY = datePosY
        dowText.translationX = dowPosX
        dowText.translationY = dowPosY
        holiText.translationX = holiPosX
        holiText.translationY = holiPosY
        MainActivity.getMainDateLy()?.let {
            it.translationX = mainDateLyX
            it.translationY = mainDateLyY
        }
        MainActivity.getMonthTextLy()?.let {
            it.scaleX = mainMonthScale
            it.scaleY = mainMonthScale
            it.translationX = mainMonthX
        }
        MainActivity.getWeekTextLy()?.translationY = weekTextY
        bar.scaleX = 1f
        bar.scaleY = 2f
        bar.translationX = barX
        bar.translationY = barY

    }

    private fun setDateClosedStyle() {
        contentLy.visibility = View.GONE
        dateLy.scaleY = 1f
        dateLy.scaleX = 1f
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
        MainActivity.getMainDateLy()?.let {
            it.translationX = 0f
            it.translationY = 0f
        }
        MainActivity.getMonthTextLy()?.let {
            it.scaleX = 1f
            it.scaleY = 1f
            it.translationX = 0f
        }
        MainActivity.getWeekTextLy()?.translationY = 0f
        bar.scaleX = 1f
        bar.scaleY = 1f
        bar.translationX = 0f
        bar.translationY = 0f
    }

    fun targeted() {
        l("[데이뷰 타겟팅] : " + AppDateFormat.ymdeDate.format(targetCal.time) )
        bar.postDelayed({
            val width = MainActivity.getMonthTextLy()?.width ?: 0
            l("너비 : " + width)
            ObjectAnimator.ofFloat(bar, "scaleX", bar.scaleX, width / bar.width.toFloat()).start()
        }, 500)
    }

    companion object {
        const val headerTextScale = 5.5f
        val weekTextY = -dpToPx(3.0f)
        val datePosX = dpToPx(16.0f)
        val datePosY = -dpToPx(1.0f)
        val dowPosX = dpToPx(1.00f) / headerTextScale
        val dowPosY = dpToPx(9.6f)
        val dowScale = 0.310f
        val holiPosX = dpToPx(15.2f)
        val holiPosY = dpToPx(0.95f)
        val holiScale = 0.32f
        val mainDateLyX = dpToPx(80.5f)
        val mainDateLyY = dpToPx(13.5f)
        val mainMonthScale = 0.84f
        val mainMonthX = dpToPx(0.5f)
        val barX = dpToPx(100.0f)
        val barY = dpToPx(23.0f)
    }

}