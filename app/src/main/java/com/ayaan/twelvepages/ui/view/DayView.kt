package com.ayaan.twelvepages.ui.view

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.DecorationItemsAdapter
import com.ayaan.twelvepages.adapter.RecordListAdapter
import com.ayaan.twelvepages.adapter.util.ListDiffCallback
import com.ayaan.twelvepages.adapter.util.RecordListComparator
import com.ayaan.twelvepages.manager.*
import com.ayaan.twelvepages.model.Photo
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.dialog.DatePickerDialog
import com.ayaan.twelvepages.ui.dialog.PopupOptionDialog
import com.ayaan.twelvepages.ui.dialog.SchedulingDialog
import com.ayaan.twelvepages.ui.dialog.StickerPickerDialog
import io.realm.OrderedCollectionChangeSet
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.view_date_cell_header.view.*
import kotlinx.android.synthetic.main.view_day.view.*
import java.util.*
import java.util.Calendar.SATURDAY
import java.util.Calendar.SUNDAY
import kotlin.collections.ArrayList


class DayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    val targetCal : Calendar = Calendar.getInstance()
    private var recordList: RealmResults<Record>? = null
    private val mainList = ArrayList<Record>()
    private val decoList = ArrayList<Record>()
    private val newList = ArrayList<Record>()
    private val dateInfo = DateInfoManager.DateInfo()
    private var color = 0
    var startTime: Long = 0
    var endTime: Long = 0

    private val adapter = RecordListAdapter(context, mainList, targetCal, true) { view, item, action ->
        MainActivity.instance?.let { activity ->
            showDialog(PopupOptionDialog(activity,
                    arrayOf(PopupOptionDialog.Item(str(R.string.copy), R.drawable.copy, AppTheme.secondaryText),
                            PopupOptionDialog.Item(str(R.string.cut), R.drawable.cut, AppTheme.secondaryText),
                            PopupOptionDialog.Item(str(R.string.move_date), R.drawable.schedule, AppTheme.secondaryText),
                            PopupOptionDialog.Item(str(R.string.delete), R.drawable.delete, AppTheme.red)), view, false) { index ->
                val record = Record().apply { copy(item) }
                when(index) {
                    0 -> {
                        record.id = null
                        activity.viewModel.clip(record)
                    }
                    1 -> {
                        activity.viewModel.clip(record)
                        cutRecord(item)
                    }
                    2 -> {
                        showDialog(SchedulingDialog(activity, record, 0) { sCal, eCal ->
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
                    4 -> {
                        record.folder = MainActivity.getViewModel()?.getKeepFolder()
                        if(record.isRepeat()) {
                            RepeatManager.save(activity, record, Runnable { toast(R.string.moved, R.drawable.inbox) })
                        }else {
                            RecordManager.save(record)
                            toast(R.string.moved, R.drawable.inbox)
                        }
                    }
                }
            }, true, false, true, false)
        }
    }

    private val decoAdapter = DecorationItemsAdapter(context, decoList) { view, item, action ->
        MainActivity.instance?.let { activity ->
            showDialog(PopupOptionDialog(activity,
                    arrayOf(PopupOptionDialog.Item(str(R.string.edit), R.drawable.pen, AppTheme.secondaryText),
                            PopupOptionDialog.Item(str(R.string.delete), R.drawable.delete, AppTheme.red)), view, false) { index ->
                val record = Record().apply { copy(item) }
                when(index) {
                    0 -> {
                        StickerPickerDialog(record.getStickerLink()?.intParam1 ?: 0){ sticker, position ->
                            record.setSticker(sticker, position)
                            RecordManager.save(record)
                            toast(R.string.saved, R.drawable.done)
                        }.show(activity.supportFragmentManager, null)
                    }
                    1 -> {
                        RecordManager.delete(context as Activity, record, Runnable { toast(R.string.deleted, R.drawable.delete) })
                    }
                }
            }, true, false, true, false)
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_day, this, true)
        rootLy.setOnClickListener {}
        setGlobalTheme(rootLy)
        initRecyclerView()
        clipChildren = false
        dateLy.clipChildren = false
        dateLy.pivotX = 0f
        dateLy.pivotY = 0f
        dowText.pivotX = 0f
        dowText.pivotY = 0f
        holiText.pivotX = 0f
        holiText.pivotY = 0f
        (diffText.layoutParams as LayoutParams).gravity = Gravity.NO_GRAVITY
        (dateLy.layoutParams as LayoutParams).gravity = Gravity.NO_GRAVITY
        dowText.layoutParams.height = dpToPx(28)
        topShadow.visibility = View.VISIBLE
        dateText.setOnClickListener {
            MainActivity.instance?.let {
                showDialog(DatePickerDialog(it, targetCal.timeInMillis) { time ->
                    it.selectDate(time)
                }, true, true, true, false)
            }
        }
        holiText.setOnClickListener {

        }
        setDateClosedStyle()
    }

    private fun initRecyclerView() {
        recordListView.layoutManager = LinearLayoutManager(context)
        recordListView.adapter = adapter
//        recordListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                if(recordListView.computeVerticalScrollOffset() > 0) topShadow.visibility = View.VISIBLE
//                else topShadow.visibility = View.GONE
//            }
//        })
        adapter.itemTouchHelper?.attachToRecyclerView(recordListView)

        decoListView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        decoListView.adapter = decoAdapter
    }

    fun notifyDateChanged() {
        startTime = getCalendarTime0(targetCal)
        endTime = getCalendarTime23(targetCal)
        recordList?.removeAllChangeListeners()
        recordList = RecordManager.getRecordList(startTime, endTime, MainActivity.getTargetFolder())
        recordList?.addChangeListener { result, changeSet ->
            val t = System.currentTimeMillis()
            if(changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                updateData(result, mainList)
                adapter.notifyDataSetChanged()
                decoAdapter.notifyDataSetChanged()
            }else if(changeSet.state == OrderedCollectionChangeSet.State.UPDATE) {
                if(MainActivity.getDayPager()?.isOpened() == true) {
                    updateData(result, newList)
                    updateChange(adapter, mainList, newList)
                }
            }
            l("${AppDateFormat.md.format(targetCal.time)} 데이뷰 갱신 : ${(System.currentTimeMillis() - t) / 1000f} 초")
        }
    }

    private fun updateData(data: RealmResults<Record>, list: ArrayList<Record>) {
        list.clear()
        decoList.clear()
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

        if(decoList.isNotEmpty()) {
            decoListView.visibility = View.VISIBLE
        }else {
            decoListView.visibility = View.GONE
        }
    }

    private fun collocateData(data: RealmResults<Record>, e: ArrayList<Record>) {
        data.forEach { timeObject ->
            try{
                if(timeObject.id?.startsWith("sticker_") == true) {
                    decoList.add(timeObject.makeCopyObject())
                }else {
                    if(timeObject.repeat.isNullOrEmpty()) {
                        e.add(timeObject.makeCopyObject())
                    }else {
                        RepeatManager.makeRepeatInstances(timeObject, startTime, endTime).forEach { e.add(it) }
                    }
                }
            }catch (e: Exception){ e.printStackTrace() }
        }
    }

    private fun updateChange(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
                             o: ArrayList<Record>, n: ArrayList<Record>) {
        decoAdapter.notifyDataSetChanged()
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
        mainList.clear()
        decoList.clear()
        adapter.notifyDataSetChanged()
        setDateClosedStyle()
    }

    fun initTime(time: Long) {
        targetCal.timeInMillis = time
        setHeaderLy()
    }

    @SuppressLint("SetTextI18n")
    fun setHeaderLy() {
        dateText.text = String.format("%02d", targetCal.get(Calendar.DATE))
        DateInfoManager.getHoliday(dateInfo, targetCal)
        color = if(dateInfo.holiday?.isHoli == true || targetCal.get(Calendar.DAY_OF_WEEK) == SUNDAY) {
            CalendarManager.sundayColor
        }else if(targetCal.get(Calendar.DAY_OF_WEEK) == SATURDAY) {
            CalendarManager.saturdayColor
        }else {
            CalendarManager.selectedDateColor
        }

        color = if(color == CalendarManager.dateColor) {
            CalendarManager.selectedDateColor
        }else {
            color
        }

        diffText.translationX = MainActivity.getTargetCalendarView()?.targetDateHolder?.getDiffTextLeft()?.toFloat()?:0f
        dateText.setTextColor(color)
        dowText.setTextColor(CalendarManager.selectedDateColor)
        holiText.setTextColor(color)
        val selectedString = dateInfo.getSelectedString()
        val diffString = dateInfo.getDiffDateString()
        dowText.text = (if(AppStatus.isDisplayDayViewWeekNum) "${String.format(str(R.string.weekNum), targetCal.get(Calendar.WEEK_OF_YEAR))} · " else "") +
                "${AppDateFormat.dow.format(targetCal.time)}\n${diffString}" +
                if(selectedString.isNotBlank()) " · $selectedString" else ""
        diffText.text = diffString
        holiText.text = selectedString
    }

    fun targeted() {
        l("[데이뷰 타겟팅] : " + AppDateFormat.ymde.format(targetCal.time) )
        postDelayed({
            MainActivity.instance?.let { activity ->
                setFooterView(activity)
            }
        }, 0)
    }

    fun unTargeted() {
        footerTask?.cancel(true)
        footerTask = null
        adapter.clearFooterView()
    }

    var footerTask: AsyncTask<String, String, String?>? = null

    @SuppressLint("StaticFieldLeak")
    private fun setFooterView(activity: Activity) {
        footerTask = object : AsyncTask<String, String, String?>() {
            var photos: ArrayList<Photo>? = null
            var beforeYearRecords: List<Record>? = null

            override fun doInBackground(vararg args: String): String? {
                val realm = Realm.getDefaultInstance()

                photos = when(AppStatus.rememberPhoto) {
                    YES -> {
                        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED) {
                            getPhotosByDate(activity, targetCal)
                        }else {
                            null
                        }
                    }
                    NO -> ArrayList()
                    else -> null
                }

                if(AppStatus.rememberBeforeYear == YES) {
                    tempCal.timeInMillis = targetCal.timeInMillis
                    tempCal.add(Calendar.YEAR, -1)
                    val startTime = getCalendarTime0(tempCal)
                    val endTime = getCalendarTime23(tempCal)
                    beforeYearRecords = realm.where(Record::class.java)
                            .notEqualTo("dtCreated", -1L)
                            .greaterThanOrEqualTo("dtEnd", startTime)
                            .lessThanOrEqualTo("dtStart", endTime)
                            .sort("dtStart", Sort.ASCENDING)
                            .findAll().map { realm.copyFromRealm(it) }
                }

                realm.close()
                return null
            }

            override fun onPreExecute() { adapter.readyFooterView() }
            override fun onProgressUpdate(vararg text: String) {}
            override fun onPostExecute(result: String?) {
                if(MainActivity.getDayPager()?.isOpened() == true) {
                    if(!(photos.isNullOrEmpty() && beforeYearRecords.isNullOrEmpty())) {
                        emptyLy.visibility = View.GONE
                    }
                    adapter.setFooterView(photos, beforeYearRecords)
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("SetTextI18n")
    fun show(dataSize: Int) {
        val selectedString = dateInfo.getSelectedString()
        dowText.text = (if(AppStatus.isDisplayDayViewWeekNum) "${String.format(str(R.string.weekNum), targetCal.get(Calendar.WEEK_OF_YEAR))} · " else "") +
                "${AppDateFormat.dow.format(targetCal.time)}\n${dateInfo.getDiffDateString()}" +
                if(selectedString.isNotBlank()) " · $selectedString" else ""
        diffText.translationX = MainActivity.getTargetCalendarView()?.targetDateHolder?.getDiffTextLeft()?.toFloat()?:0f
        val animSet = AnimatorSet()
        animSet.playTogether(
                ObjectAnimator.ofFloat(bar, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(previewDataImg, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(previewDataImg, "translationY", 0f, dpToPx(120f)),
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
                ObjectAnimator.ofFloat(MainActivity.getMainMonthText(), "scaleX", 1f, mainMonthTextScale),
                ObjectAnimator.ofFloat(MainActivity.getMainMonthText(), "scaleY", 1f, mainMonthTextScale),
                ObjectAnimator.ofFloat(MainActivity.getMainMonthText(), "translationY", 1f, mainMonthTextY),
                ObjectAnimator.ofFloat(dowText, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(holiText, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(diffText, "alpha", 1f, 0f))
        animSet.duration = 250L
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
    }

    fun hide(dataSize: Int) {
        contentLy.visibility = View.GONE
        diffText.translationX = MainActivity.getTargetCalendarView()?.targetDateHolder?.getDiffTextLeft()?.toFloat()?:0f
        val animSet = AnimatorSet()
        animSet.playTogether(
                ObjectAnimator.ofFloat(bar, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(previewDataImg, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(previewDataImg, "translationY", dpToPx(120f), 0f),
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
                ObjectAnimator.ofFloat(MainActivity.getMainMonthText(), "scaleX", mainMonthTextScale, 1f),
                ObjectAnimator.ofFloat(MainActivity.getMainMonthText(), "scaleY", mainMonthTextScale, 1f),
                ObjectAnimator.ofFloat(MainActivity.getMainMonthText(), "translationY", mainMonthTextY, 0f),
                ObjectAnimator.ofFloat(dowText, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(holiText, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(diffText, "alpha", 0f, 1f))
        animSet.duration = 250L
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
    }

    fun setDateOpenedStyle() {
        //TransitionManager.beginDelayedTransition(contentLy, makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_IN })
        contentLy.visibility = View.VISIBLE
        dateLy.scaleY = headerTextScale
        dateLy.scaleX = headerTextScale
        dowText.scaleY = dowScale
        dowText.scaleX = dowScale
        dowText.alpha = 1f
        holiText.scaleY = holiScale
        holiText.scaleX = holiScale
        holiText.alpha = 0f
        diffText.alpha = 0f
        dateLy.translationX = datePosX
        dateLy.translationY = datePosY
        dowText.translationX = dowPosX
        dowText.translationY = dowPosY
        holiText.translationX = holiPosX
        holiText.translationY = holiPosY
        MainActivity.getMainMonthText()?.let {
        }
        previewDataImg.alpha = 0f
        previewDataImg.translationY = dpToPx(120f)
        bar.alpha = 0f
    }

    private fun setDateClosedStyle() {
        contentLy.visibility = View.GONE
        dateLy.scaleY = 1f
        dateLy.scaleX = 1f
        dowText.scaleY = 1f
        dowText.scaleX = 1f
        dowText.alpha = 0f
        holiText.scaleY = 1f
        holiText.scaleX = 1f
        holiText.alpha = 1f
        diffText.alpha = 1f
        dateLy.translationX = 0f
        dateLy.translationY = 0f
        dowText.translationX = 0f
        dowText.translationY = 0f
        holiText.translationX = 0f
        holiText.translationY = 0f
        MainActivity.getMainMonthText()?.let {
        }
        previewDataImg.alpha = 0f
        previewDataImg.translationY = 0f
        bar.alpha = 1f
    }

    private fun cutRecord(record: Record) {
        if(mainList.contains(record)) {
            val pos = mainList.indexOf(record)
            mainList.remove(record)
            adapter.notifyItemRemoved(pos)
        }
    }

    fun getDateLy() : LinearLayout = dateLy
    fun getRootLy() : FrameLayout = rootLy
    fun getPreviewDataImg() : View = previewDataImg

    companion object {
        const val headerTextScale = 4.0f
        const val mainMonthTextScale = 0.75f
        private val dp = dpToPx(1f)
        val mainMonthTextY = if(dp == 3.5f) dpToPx(0.0f) else dpToPx(2.5f)

        val datePosX = dpToPx(2.0f)
        val datePosY = -dpToPx(10.0f)

        val dowPosX = dpToPx(5.5f) / headerTextScale
        val holiPosX = dpToPx(0.0f) / headerTextScale

        val subYPos = dpToPx(19.5f) / headerTextScale
        val dowPosY = dpToPx(0.0f) / headerTextScale + subYPos
        val holiPosY = -dpToPx(0.0f) / headerTextScale + subYPos

        val dowScale = 1.90f / headerTextScale
        val holiScale = 1.90f / headerTextScale
    }

}