package com.ayaan.twelvepages.ui.view

import android.animation.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.transition.*
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.adapter.RecordListAdapter
import com.ayaan.twelvepages.adapter.SearchFilterListAdapter
import com.ayaan.twelvepages.adapter.util.ListDiffCallback
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.OsCalendarManager
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.model.SearchFilter
import com.ayaan.twelvepages.model.Tag
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.dialog.ColorPickerDialog
import com.ayaan.twelvepages.ui.dialog.PopupOptionDialog
import com.ayaan.twelvepages.ui.dialog.SchedulingDialog
import com.ayaan.twelvepages.ui.dialog.TagDialog
import io.realm.OrderedCollectionChangeSet
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.view_saerch.view.*
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class SearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    companion object

    var viewMode = ViewMode.CLOSED
    private var recordList: RealmResults<Record>? = null
    private val items = ArrayList<Record>()
    private val newItmes = ArrayList<Record>()
    private val adapter = RecordListAdapter(context, items, Calendar.getInstance(), false) { view, item, action ->
        showDialog(PopupOptionDialog(MainActivity.instance!!,
                arrayOf(PopupOptionDialog.Item(str(R.string.delete), R.drawable.delete, AppTheme.red)), view, false) { index ->
            val record = Record().apply { copy(item) }
            when(index) {
                0 -> {
                    RecordManager.delete(context as Activity, record, Runnable { toast(R.string.deleted, R.drawable.delete) })
                }
            }
        }, true, false, true, false)
    }.apply { isSearchListMode = true }
    private val tags = ArrayList<Tag>()
    private val tagTitles = ArrayList<String>()
    private val startCal = Calendar.getInstance()
    private val endCal = Calendar.getInstance()
    private var startTime = Long.MIN_VALUE
    private var endTime = Long.MIN_VALUE
    private var colorKey = Int.MIN_VALUE
    private var isCheckBox = false
    private var isPhoto = false
    private val record = Record().apply { setDateTime(startCal, endCal) }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_saerch, this, true)
        filtersLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        //recyclerView.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.itemTouchHelper?.attachToRecyclerView(recyclerView)
        filterListView.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateFilterUI()
                notifyDataChanged()
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
        clearBtn.setOnClickListener { clear() }
        saveBtn.setOnClickListener { saveFilters() }
        dateBtn.setOnClickListener { showDateDialog() }
        tagBtn.setOnClickListener { showTagDialog() }
        colorBtn.setOnClickListener { showColorDialog() }
        checkBtn.setOnClickListener { setCheckFilter() }
        photoBtn.setOnClickListener { setPhotoFilter() }
        contentLy.setOnClickListener { }
        updateFilterUI()
    }

    fun notifyDataChanged() {
        if(isSetFilters()) {
            recordList?.removeAllChangeListeners()
            recordList = RecordManager.getRecordList(searchInput.text.toString(),
                    tagTitles,
                    startTime,
                    endTime,
                    colorKey,
                    isCheckBox,
                    isPhoto
            )
            recordList?.addChangeListener { result, changeSet ->
                l("==========서치뷰 데이터 변경 시작=========")
                val t = System.currentTimeMillis()
                adapter.query = searchInput.text.toString()

                if(changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                    items.clear()
                    result.forEach { items.add(it.makeCopyObject()) }
                    if(tagTitles.isEmpty()) {
                        OsCalendarManager.searchEvents(context, searchInput.text.toString()).forEach { items.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }else if(changeSet.state == OrderedCollectionChangeSet.State.UPDATE) {
                    newItmes.clear()
                    result.forEach { newItmes.add(it.makeCopyObject()) }
                    if(tagTitles.isEmpty()) {
                        OsCalendarManager.searchEvents(context, searchInput.text.toString()).forEach { items.add(it) }
                    }
                    Thread {
                        val diffResult = DiffUtil.calculateDiff(ListDiffCallback(items, newItmes))
                        items.clear()
                        items.addAll(newItmes)
                        Handler(Looper.getMainLooper()).post{
                            diffResult.dispatchUpdatesTo(adapter)
                        }
                    }.start()
                }
                l("걸린시간 : ${(System.currentTimeMillis() - t) / 1000f} 초")
                l("==========서치뷰 데이터 변경 종료=========")
            }
        }else {
            items.clear()
            adapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateFilterUI() {
        if(startTime == Long.MIN_VALUE) {
            dateBtn.setBackgroundResource(R.drawable.edit_tag_new)
            dateBtn.alpha = 0.4f
            dateText.text = str(R.string.select_date)
            dateClearBtn.visibility = View.GONE
        }else {
            dateBtn.setBackgroundResource(R.color.light_line)
            dateBtn.alpha = 1f
            dateText.text = "${AppDateFormat.simpleYmdDate.format(Date(startTime))} ~ ${AppDateFormat.simpleYmdDate.format(Date(endTime))}"
            dateClearBtn.visibility = View.VISIBLE
            dateClearBtn.setOnClickListener {
                startCal.timeInMillis = System.currentTimeMillis()
                endCal.timeInMillis = System.currentTimeMillis()
                record.setDateTime(startCal, endCal)
                startTime = Long.MIN_VALUE
                endTime = Long.MIN_VALUE
                updateFilterUI()
                notifyDataChanged()
            }
        }

        if(tagTitles.isEmpty()) {
            tagBtn.setBackgroundResource(R.drawable.edit_tag_new)
            tagBtn.alpha = 0.4f
            tagText.text = str(R.string.search_tag)
            tagClearBtn.visibility = View.GONE
        }else {
            tagBtn.setBackgroundResource(R.color.light_line)
            tagBtn.alpha = 1f
            tagText.text = tagTitles.joinToString(separator = "#", prefix = "#")
            tagClearBtn.visibility = View.VISIBLE
            tagClearBtn.setOnClickListener {
                tagTitles.clear()
                updateFilterUI()
                notifyDataChanged()
            }
        }

        if(colorKey == Int.MIN_VALUE) {
            colorBtn.alpha = 0.4f
            colorBtn.setBackgroundResource(R.drawable.edit_tag_new)
            colorText.setTextColor(AppTheme.secondaryText)
            colorClearBtn.setColorFilter(AppTheme.secondaryText)
            colorText.text = str(R.string.select_color)
            colorClearBtn.visibility = View.GONE
        }else {
            colorBtn.alpha = 1f
            val color = ColorManager.getColor(colorKey)
            val fontColor = ColorManager.getFontColor(colorKey)
            colorBtn.setBackgroundColor(color)
            colorText.setTextColor(fontColor)
            colorClearBtn.setColorFilter(fontColor)
            colorText.text = str(R.string.select_color)
            colorClearBtn.visibility = View.VISIBLE
            colorClearBtn.setOnClickListener {
                colorKey = Int.MIN_VALUE
                record.colorKey = 0
                updateFilterUI()
                notifyDataChanged()
            }
        }

        if(!isCheckBox) {
            checkBtn.alpha = 0.4f
            checkBtn.setBackgroundResource(R.drawable.edit_tag_new)
            checkClearBtn.visibility = View.GONE
        }else {
            checkBtn.setBackgroundResource(R.color.light_line)
            checkBtn.alpha = 1f
            checkClearBtn.visibility = View.VISIBLE
            checkClearBtn.setOnClickListener {
                isCheckBox = false
                updateFilterUI()
                notifyDataChanged()
            }
        }

        if(!isPhoto) {
            photoBtn.alpha = 0.4f
            photoBtn.setBackgroundResource(R.drawable.edit_tag_new)
            photoClearBtn.visibility = View.GONE
        }else {
            photoBtn.setBackgroundResource(R.color.light_line)
            photoBtn.alpha = 1f
            photoClearBtn.visibility = View.VISIBLE
            photoClearBtn.setOnClickListener {
                isPhoto = false
                updateFilterUI()
                notifyDataChanged()
            }
        }

        if(isSetFilters()) {
            showSaveBtn()
        }else {
            hideSaveBtn()
        }
    }

    private fun showDateDialog() {
        val dialog = SchedulingDialog(MainActivity.instance!!, record, 0) { sCal, eCal ->
            startCal.timeInMillis = sCal.timeInMillis
            endCal.timeInMillis = eCal.timeInMillis
            record.setDateTime(startCal, endCal)
            startTime = record.dtStart
            endTime = record.dtEnd
            updateFilterUI()
            notifyDataChanged()
        }
        showDialog(dialog, true, true, true, false)
    }

    private fun showTagDialog() {
        showDialog(TagDialog(MainActivity.instance!!, tags) { result ->
            tags.clear()
            tags.addAll(result)
            tagTitles.clear()
            tags.forEach {
                it.title?.let { tagTitles.add(it) }
            }
            updateFilterUI()
            notifyDataChanged()
        }, true, true, true, false)
    }

    private fun showColorDialog() {
        ColorPickerDialog(record.colorKey){ key ->
            record.colorKey = key
            colorKey = key
            updateFilterUI()
            notifyDataChanged()
        }.show(MainActivity.instance!!.supportFragmentManager, null)
    }

    private fun setCheckFilter() {
        isCheckBox = !isCheckBox
        updateFilterUI()
        notifyDataChanged()
    }

    private fun setPhotoFilter() {
        isPhoto = !isPhoto
        updateFilterUI()
        notifyDataChanged()
    }

    private fun clear() {
        startCal.timeInMillis = System.currentTimeMillis()
        endCal.timeInMillis = System.currentTimeMillis()
        record.setDateTime(startCal, endCal)
        startTime = Long.MIN_VALUE
        endTime = Long.MIN_VALUE
        tagTitles.clear()
        colorKey = Int.MIN_VALUE
        record.colorKey = 0
        isCheckBox = false
        isPhoto = false
        updateFilterUI()
        searchInput.setText("")
    }

    private fun saveFilters() {
        Realm.getDefaultInstance()?.use {
            it.executeTransaction { realm ->
                val filter = JSONObject()
                filter.put("keyword", searchInput.text.toString())
                filter.put("startTime", startTime)
                filter.put("endTime", endTime)
                filter.put("tagTitles", tagTitles.joinToString(separator = "||"))
                filter.put("colorKey", colorKey)
                filter.put("isCheckBox", isCheckBox)
                filter.put("isPhoto", isPhoto)
                val order = realm.where(SearchFilter::class.java).max("order")?.toInt() ?: -1
                val searchFilter = SearchFilter(UUID.randomUUID().toString(), filter.toString(), order + 1)
                realm.insert(searchFilter)
                setSearchFilterList()
                toast(R.string.saved, R.drawable.save)
            }
        }
        clear()
    }

    private fun setSearchFilterList() {
        val filterItems = ArrayList<SearchFilter>()
        Realm.getDefaultInstance()?.use { realm ->
            realm.where(SearchFilter::class.java).sort("order", Sort.DESCENDING).findAll()?.forEach {
                it?.let { filterItems.add(realm.copyFromRealm(it)) }
            }
        }
        TransitionManager.beginDelayedTransition(contentLy, makeChangeBounceTransition())
        filterListView.adapter = SearchFilterListAdapter(context, filterItems) { v, item, filter, action ->
            if(action == 0) {
                try {
                    val kw = filter.getString("keyword")
                    val st = filter.getLong("startTime")
                    val et = filter.getLong("endTime")
                    val tts = filter.getString("tagTitles")
                    val ck = filter.getInt("colorKey")
                    val icb = filter.getBoolean("isCheckBox")
                    val ip = filter.getBoolean("isPhoto")
                    searchInput.setText(kw)
                    startTime = st
                    endTime = et
                    if(st != Long.MIN_VALUE) {
                        startCal.timeInMillis = st
                        endCal.timeInMillis = et
                    }else {
                        startCal.timeInMillis = System.currentTimeMillis()
                        endCal.timeInMillis = System.currentTimeMillis()
                    }
                    record.setDateTime(startCal, endCal)
                    tagTitles.clear()
                    if(tts.isNotEmpty()) {
                        tts.split("||").forEach { tagTitles.add(it) }
                    }
                    colorKey = ck
                    if(colorKey == Int.MIN_VALUE) {
                        record.colorKey = 0
                    }
                    isCheckBox = icb
                    isPhoto = ip
                    updateFilterUI()
                    notifyDataChanged()
                }catch (e: Exception) { e.printStackTrace() }
            }else {
                Realm.getDefaultInstance()?.use { realm ->
                    realm.executeTransaction {
                        realm.where(SearchFilter::class.java).equalTo("id", item.id).findFirst()?.deleteFromRealm()
                    }
                }
            }
        }
    }

    fun show() {
        visibility = View.VISIBLE
        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(contentLy, "elevation", 0f, dpToPx(10f)))
        animSet.duration = 50
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(p0: Animator?) {
                val transitionSet = TransitionSet()
                val t1 = makeChangeBounceTransition()
                val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_IN }
                t1.setPathMotion(ArcMotion())
                t1.addTarget(contentLy)
                t2.addTarget(backgroundLy)
                transitionSet.addTransition(t1)
                transitionSet.addTransition(t2)
                transitionSet.addListener(object : TransitionListenerAdapter(){
                    override fun onTransitionEnd(transition: Transition) {
                        showKeyPad(searchInput)
                        setSearchFilterList()
                    }
                })
                TransitionManager.beginDelayedTransition(this@SearchView, transitionSet)
                backgroundLy.visibility = View.VISIBLE
                backgroundLy.setOnClickListener { hide() }
                (contentLy.layoutParams as LayoutParams).let {
                    it.height = WRAP_CONTENT
                    it.setMargins(0, 0, 0, 0)
                }
                contentLy.requestLayout()
            }
        })
        animSet.start()
        viewMode = ViewMode.OPENED
    }

    fun hide() {
        clear()
        hideKeyPad(searchInput)
        val transitionSet = TransitionSet()
        val t1 = makeChangeBounceTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_OUT }
        t1.setPathMotion(ArcMotion())
        t1.addTarget(contentLy)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        transitionSet.addListener(object : TransitionListenerAdapter(){
            override fun onTransitionEnd(transition: Transition) {
                val animSet = AnimatorSet()
                animSet.playTogether(ObjectAnimator.ofFloat(contentLy, "elevation", 10f, dpToPx(0f)))
                animSet.duration = 50
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(p0: Animator?) {
                        visibility = View.GONE
                    }
                })
                animSet.start()
            }
        })
        TransitionManager.beginDelayedTransition(this@SearchView, transitionSet)
        backgroundLy.visibility = View.GONE
        backgroundLy.setOnClickListener(null)
        (contentLy.layoutParams as LayoutParams).let {
            it.height = 0
            it.setMargins(0, 0, 0, 0)
        }
        contentLy.requestLayout()
        viewMode = ViewMode.CLOSED
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED

    private fun isSetFilters() = searchInput.text.toString().isNotEmpty()
            || tagTitles.isNotEmpty()
            || startTime != Long.MIN_VALUE
            || colorKey != Int.MIN_VALUE
            || isCheckBox
            || isPhoto

    private fun hideSaveBtn() {
        clearBtn.visibility = View.GONE
        saveBtn.visibility = View.GONE
        filterListView.visibility = View.VISIBLE
    }

    private fun showSaveBtn() {
        clearBtn.visibility = View.VISIBLE
        saveBtn.visibility = View.VISIBLE
        filterListView.visibility = View.GONE
    }
}