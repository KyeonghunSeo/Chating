package com.hellowo.journey.ui.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.*
import com.hellowo.journey.adapter.RecordListAdapter
import com.hellowo.journey.adapter.util.ListDiffCallback
import com.hellowo.journey.adapter.util.RecordListComparator
import com.hellowo.journey.manager.*
import com.hellowo.journey.model.Record
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.dialog.DatePickerDialog
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

    private val adapter = RecordListAdapter(context, currentList, targetCal) { view, timeObject, action ->
        when(action) {
        }
    }

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
        dateText.typeface = AppTheme.bFont
        dowText.typeface = AppTheme.bFont
        holiText.typeface = AppTheme.boldFont
        dateLy.clipChildren = false
        dateLy.pivotX = 0f
        dateLy.pivotY = 0f
        dowText.pivotX = 0f
        dowText.pivotY = 0f
        holiText.pivotX = 0f
        holiText.pivotY = 0f
        bar.pivotX = 0f
        (dateLy.layoutParams as FrameLayout.LayoutParams).gravity = Gravity.NO_GRAVITY
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
                updateData(result, newList)
                updateChange(adapter, currentList, newList)
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
                    RepeatManager.makeRepeatInstance(timeObject, startTime, endTime).forEach { e.add(it) }
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
        dowText.text = AppDateFormat.dowfullEng.format(targetCal.time).toUpperCase()
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
                ObjectAnimator.ofFloat(dowText, "scaleX", 1f, subScale),
                ObjectAnimator.ofFloat(dowText, "scaleY", 1f, subScale),
                ObjectAnimator.ofFloat(holiText, "scaleX", 1f, subScale),
                ObjectAnimator.ofFloat(holiText, "scaleY", 1f, subScale),
                ObjectAnimator.ofFloat(dateLy, "translationX", 0f, datePosX),
                ObjectAnimator.ofFloat(dateLy, "translationY", 0f, datePosY),
                ObjectAnimator.ofFloat(dowText, "translationX", 0f, dowPosX),
                ObjectAnimator.ofFloat(dowText, "translationY", 0f, dowPosY),
                ObjectAnimator.ofFloat(holiText, "translationX", 0f, holiPosX),
                ObjectAnimator.ofFloat(holiText, "translationY", 0f, holiPosY),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "scaleX", 1f, mainDateScale),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "scaleY", 1f, mainDateScale),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "translationX", 0f, mainDateLyX),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "translationY", 0f, mainDateLyY),
                ObjectAnimator.ofFloat(bar, "scaleX", 1f, barScale),
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
                ObjectAnimator.ofFloat(dowText, "scaleX", subScale, 1f),
                ObjectAnimator.ofFloat(dowText, "scaleY", subScale, 1f),
                ObjectAnimator.ofFloat(holiText, "scaleX", subScale, 1f),
                ObjectAnimator.ofFloat(holiText, "scaleY", subScale, 1f),
                ObjectAnimator.ofFloat(dateLy, "translationX", datePosX, 0f),
                ObjectAnimator.ofFloat(dateLy, "translationY", datePosY, 0f),
                ObjectAnimator.ofFloat(dowText, "translationX", dowPosX, 0f),
                ObjectAnimator.ofFloat(dowText, "translationY", dowPosY, 0f),
                ObjectAnimator.ofFloat(holiText, "translationX", holiPosX, 0f),
                ObjectAnimator.ofFloat(holiText, "translationY", holiPosY, 0f),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "scaleX", mainDateScale, 1f),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "scaleY", mainDateScale, 1f),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "translationX", mainDateLyX, 0f),
                ObjectAnimator.ofFloat(MainActivity.getMainDateLy(), "translationY", mainDateLyY, 0f),
                ObjectAnimator.ofFloat(bar, "scaleX", barScale, 1f),
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
        MainActivity.getMainDateLy()?.let {
            it.scaleX = mainDateScale
            it.scaleY = mainDateScale
            it.translationX = mainDateLyX
            it.translationY = mainDateLyY
        }
        bar.scaleX = barScale
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
            it.scaleX = 1f
            it.scaleY = 1f
            it.translationX = 0f
            it.translationY = 0f
        }
        bar.scaleX = 1f
        bar.scaleY = 1f
        bar.translationX = 0f
        bar.translationY = 0f
    }

    companion object {
        const val headerTextScale = 5.5f
        val datePosX = dpToPx(16.0f)
        val datePosY = -dpToPx(1.0f)
        val dowPosX = -dpToPx(0.0f)
        val dowPosY = dpToPx(9.3f)
        val holiPosX = dpToPx(15.2f)
        val holiPosY = -dpToPx(0.2f)
        val subScale = 0.3f
        val mainDateLyX = dpToPx(80.7f)
        val mainDateLyY = dpToPx(15.0f)
        val mainDateScale = 0.80f
        val barX = dpToPx(100.0f)
        val barY = dpToPx(23.0f)
        val barScale = 0.25f
    }

}