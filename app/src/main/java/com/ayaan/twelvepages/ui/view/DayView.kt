package com.ayaan.twelvepages.ui.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
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
import com.ayaan.twelvepages.ui.dialog.PopupOptionDialog
import com.ayaan.twelvepages.ui.dialog.SchedulingDialog
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmResults
import kotlinx.android.synthetic.main.view_day.view.*
import kotlinx.android.synthetic.main.view_selected_bar.view.*
import java.util.*
import java.util.Calendar.SATURDAY
import java.util.Calendar.SUNDAY
import kotlin.collections.ArrayList


class DayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    val targetCal : Calendar = Calendar.getInstance()
    private var recordList: RealmResults<Record>? = null
    private val currentList = ArrayList<Record>()
    private val newList = ArrayList<Record>()
    private val dateInfo = DateInfoManager.DateInfo()
    private var color = 0

    private val adapter = RecordListAdapter(context, currentList, targetCal) { view, record, action ->
        MainActivity.instance?.let { activity ->
            showDialog(PopupOptionDialog(activity,
                    arrayOf(PopupOptionDialog.Item(str(R.string.copy), R.drawable.copy, AppTheme.primaryText),
                            PopupOptionDialog.Item(str(R.string.cut), R.drawable.cut, AppTheme.primaryText),
                            PopupOptionDialog.Item(str(R.string.move_date), R.drawable.schedule, AppTheme.primaryText),
                            PopupOptionDialog.Item(str(R.string.delete), R.drawable.delete, AppTheme.red))
                    , view) { index ->
                when(index) {
                    0 -> {
                        record.id = null
                        activity.viewModel.clip(record)
                    }
                    1 -> {
                        activity.viewModel.clip(record)
                    }
                    2 -> {
                        showDialog(SchedulingDialog(activity, record) { sCal, eCal ->
                            record.setSchedule()
                            record.setDateTime(sCal, eCal)
                            if(record.isRepeat()) {
                                RepeatManager.save(activity, record, Runnable { toast(R.string.moved, R.drawable.schedule) })
                            }else {
                                RecordManager.save(record)
                                toast(R.string.moved, R.drawable.schedule)
                            }
                        }, true, true, true, false)
                    }
                    3 -> {
                        RecordManager.delete(context as Activity, record, Runnable { toast(R.string.deleted, R.drawable.delete) })
                    }
                }
            }, true, false, true, false)
        }
    }

    var startTime: Long = 0
    var endTime: Long = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.view_day, this, true)
        rootLy.setOnClickListener {}
        setGlobalTheme(rootLy)
        initRecyclerView()
        clipChildren = false
        dateText.typeface = AppTheme.boldFont
        dowText.setTypeface(AppTheme.boldFont, Typeface.BOLD)
        holiText.setTypeface(AppTheme.boldFont, Typeface.BOLD)
        dateLy.clipChildren = false
        dateLy.pivotX = 0f
        dateLy.pivotY = 0f
        dowText.pivotX = 0f
        dowText.pivotY = 0f
        holiText.pivotX = 0f
        holiText.pivotY = 0f
        (dateLy.layoutParams as LayoutParams).gravity = Gravity.NO_GRAVITY
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
                if(MainActivity.getDayPager()?.isOpened() == true) {
                    updateData(result, newList)
                    updateChange(adapter, currentList, newList)
                }
            }
            l("${AppDateFormat.md.format(targetCal.time)} 데이뷰 갱신 : ${(System.currentTimeMillis() - t) / 1000f} 초")
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
        dowText.text = AppDateFormat.simpleDow.format(targetCal.time)
        DateInfoManager.getHoliday(dateInfo, targetCal)
        color = if(dateInfo.holiday?.isHoli == true || targetCal.get(Calendar.DAY_OF_WEEK) == SUNDAY) {
            CalendarManager.sundayColor
        }else if(targetCal.get(Calendar.DAY_OF_WEEK) == SATURDAY) {
            CalendarManager.saturdayColor
        }else {
            CalendarManager.selectedDateColor
        }
        bar.setBackgroundColor(color)
        dateText.setTextColor(color)
        dowText.setTextColor(color)
        holiText.setTextColor(color)
        if(AppStatus.isDowDisplay) dowText.visibility = View.VISIBLE
        else dowText.visibility = View.GONE
        holiText.text = dateInfo.getSelectedString()
    }

    fun show(dayPager: DayPager) {
        MainActivity.getMainMonthText()?.setTextColor(color)
        MainActivity.getMainYearText()?.setTextColor(color)
        dowText.text = AppDateFormat.simpleDow.format(targetCal.time)
        val animSet = AnimatorSet()
        animSet.playTogether(
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
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "translationX", 1f, mainDateLyX),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "translationY", 1f, mainDateLyY),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "scaleX", 1f, mainDateLyScaleX),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "scaleY", 1f, mainDateLyScaleY),
                ObjectAnimator.ofFloat(MainActivity.getMainYearText(), "scaleX", 1f, yearTextScale),
                ObjectAnimator.ofFloat(MainActivity.getMainYearText(), "scaleY", 1f, yearTextScale),
                ObjectAnimator.ofFloat(bar, "alpha", 1f, 0f))
        animSet.duration = 300L
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
    }

    fun hide(dayPager: DayPager) {
        MainActivity.getMainMonthText()?.setTextColor(CalendarManager.selectedDateColor)
        MainActivity.getMainYearText()?.setTextColor(CalendarManager.selectedDateColor)
        dowText.text = AppDateFormat.simpleDow.format(targetCal.time)
        contentLy.visibility = View.GONE
        val animSet = AnimatorSet()
        animSet.playTogether(
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
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "translationX", mainDateLyX, 1f),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "translationY", mainDateLyY, 1f),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "scaleX", mainDateLyScaleX, 1f),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "scaleY", mainDateLyScaleY, 1f),
                ObjectAnimator.ofFloat(MainActivity.getMainYearText(), "scaleX", yearTextScale, 1f),
                ObjectAnimator.ofFloat(MainActivity.getMainYearText(), "scaleY", yearTextScale, 1f),
                ObjectAnimator.ofFloat(bar, "alpha", 0f, 1f))
        animSet.duration = 300L
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
            it.scaleX = mainDateLyScaleX
            it.scaleY = mainDateLyScaleY
        }
        bar.alpha = 0f
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
            it.translationX = 1f
            it.translationY = 1f
            it.scaleX = 1f
            it.scaleY = 1f
        }
        bar.alpha = 1f
    }

    fun targeted() {
        MainActivity.getMainMonthText()?.setTextColor(color)
        MainActivity.getMainYearText()?.setTextColor(color)
        l("[데이뷰 타겟팅] : " + AppDateFormat.ymde.format(targetCal.time) )
        l("너비 : " + dateText.width)
        MainActivity.getMainDateLy()?.let { v ->
            ObjectAnimator.ofFloat(v, "translationX", v.translationX,
                    dateText.width.toFloat() * headerTextScale).start()
        }
    }

    companion object {
        const val headerTextScale = 6.5f
        val datePosX = dpToPx(22.5f)
        val datePosY = dpToPx(1.0f)
        val dowPosX = dpToPx(1.00f) / headerTextScale
        val dowPosY = dpToPx(9.6f)
        val dowScale = 0.310f
        val holiPosX = dpToPx(0f)
        val holiPosY = dpToPx(0.0f)
        val holiScale = 0.32f
        val mainDateLyX = dpToPx(90.0f)
        val mainDateLyY = dpToPx(5.0f)
        val mainDateLyScaleX = 0.7f
        val mainDateLyScaleY = 0.7f
        val yearTextScale = 2.3f
    }

}