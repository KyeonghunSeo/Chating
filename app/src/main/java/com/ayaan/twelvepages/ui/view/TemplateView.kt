package com.ayaan.twelvepages.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.adapter.TemplateAdapter
import com.ayaan.twelvepages.adapter.util.TemplateDiffCallback
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.model.Template
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.activity.TemplateActivity
import com.ayaan.twelvepages.ui.dialog.StickerPickerDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.view_template.view.*
import java.util.*
import kotlin.collections.ArrayList

class TemplateView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val startCal = Calendar.getInstance()
    private val endCal = Calendar.getInstance()
    private val panelElevation = dpToPx(30f)
    private var behavior: BottomSheetBehavior<View>
    val items = ArrayList<Template>()
    var selectedPosition = 0
    val adapter = TemplateAdapter(context, items) { template, mode ->
        if(template != null) {
            if(mode == 0) {
                selectItem(template)
                MainActivity.getViewModel()?.makeNewTimeObject(startCal.timeInMillis, endCal.timeInMillis)
            }else {
                MainActivity.instance?.let {
                    val intent = Intent(it, TemplateActivity::class.java)
                    intent.putExtra("id", template.id)
                    it.startActivity(intent)
                }
            }
        }else {
            MainActivity.instance?.let { it.startActivity(Intent(it, TemplateActivity::class.java)) }
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_template, this, true)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = adapter
        adapter.itemTouchHelper?.attachToRecyclerView(recyclerView)

        behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.isHideable = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        behavior.skipCollapsed = true
        behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN) {
                    templatePanel.elevation = 0f
                    hiddened()
                }else {
                    templatePanel.elevation = panelElevation
                }
            }
        })

        calendarBtn.setOnClickListener {
            if(MainActivity.getTargetFolder().id == "calendar") {
                MainActivity.getTargetTime()?.let { expand(it, it) }
            }else {
                MainActivity.getViewModel()?.setCalendarFolder()
            }
        }

        keepBtn.setOnClickListener {
            if(MainActivity.getTargetFolder().id == "keep") {
                MainActivity.getTargetTime()?.let { expand(it, it) }
            }else {
                MainActivity.getViewModel()?.setKeepFolder()
            }
        }

        stickerBtn.setOnClickListener { addSticker() }
        datePointBtn.setOnClickListener { addDatePoint() }
    }

    private fun addSticker() {
        MainActivity.instance?.let {
            StickerPickerDialog{ sticker ->
                val records = ArrayList<Record>()
                while (startCal <= endCal) {
                    val dtStart = getCalendarTime0(startCal)
                    val dtEnd = getCalendarTime23(startCal)
                    records.add(RecordManager.makeNewRecord(dtStart, dtEnd).apply {
                        id = "sticker_${UUID.randomUUID()}"
                        dtCreated = System.currentTimeMillis()
                        setFormula(RecordCalendarAdapter.Formula.STICKER)
                        setSticker(sticker)
                    })
                    startCal.add(Calendar.DATE, 1)
                }
                RecordManager.save(records)
                toast(R.string.saved, R.drawable.done)
                collapse()
            }.show(it.supportFragmentManager, null)
        }
    }

    private fun addDatePoint() {
        MainActivity.instance?.let {
            StickerPickerDialog{ sticker ->
                val records = ArrayList<Record>()
                while (startCal <= endCal) {
                    val dtStart = getCalendarTime0(startCal)
                    val dtEnd = getCalendarTime23(startCal)
                    records.add(RecordManager.makeNewRecord(dtStart, dtEnd).apply {
                        id = "sticker_${UUID.randomUUID()}"
                        dtCreated = System.currentTimeMillis()
                        setFormula(RecordCalendarAdapter.Formula.DATE_POINT)
                        setSticker(sticker)
                    })
                    startCal.add(Calendar.DATE, 1)
                }
                RecordManager.save(records)
                toast(R.string.saved, R.drawable.done)
                collapse()
            }.show(it.supportFragmentManager, null)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDate() {
        val folder = MainActivity.getTargetFolder()
        if(folder.isCalendar()) {
            templateDateText.visibility = View.VISIBLE
            templateDateText.text = makeSheduleText(startCal.timeInMillis, endCal.timeInMillis,
                    false, false, false, true)
        }else {
            templateDateText.visibility = View.GONE
        }
    }

    fun expand(dtStart: Long, dtEnd: Long) {
        vibrate(context)
        startCal.timeInMillis = dtStart
        endCal.timeInMillis = dtEnd
        setDate()
        clipLy.visibility = View.GONE
        addLy.visibility = View.VISIBLE
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        if(AppStatus.templateMode == 0) {
            startExpandAnimation()
        }else {
            MainActivity.getViewModel()?.targetTemplate?.value = null
            MainActivity.getViewModel()?.makeNewTimeObject(startCal.timeInMillis, endCal.timeInMillis)
        }
    }

    fun clip(record: Record?) {
        vibrate(context)
        if(record == null) {
            collapse()
        }else {
            if(record.id.isNullOrEmpty()) {
                clipTypeText.text = str(R.string.copy)
            }else {
                clipTypeText.text = str(R.string.cut)
            }
            clipText.text = record.getTitleInCalendar()
            clipIconImg.setColorFilter(record.getColor())
            clipPasteBtn.setOnClickListener {
                MainActivity.getTargetFolder().let { record.folder = Folder(it) }
                record.setDate(MainActivity.getTargetTime() ?: Long.MIN_VALUE)
                if(record.id.isNullOrEmpty()) {
                    if(record.isRepeat()) {
                        record.clearRepeat()
                    }
                    RecordManager.save(record)
                    toast(R.string.copied, R.drawable.copy)
                }else {
                    if(record.isRepeat()) {
                        RecordManager.deleteOnly(record)
                        record.clearRepeat()
                        record.id = null
                        RecordManager.save(record)
                    }else {
                        RecordManager.delete(record)
                        record.id = null
                        RecordManager.save(record)
                    }
                    toast(R.string.moved, R.drawable.change)
                }
                MainActivity.getViewModel()?.clipRecord?.value = null
            }
            clipLy.visibility = View.VISIBLE
            addLy.visibility = View.GONE
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun startExpandAnimation() {}

    fun collapse() {
        MainActivity.instance?.clearCalendarHighlight()
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun hiddened() {
        adapter.mode = 1
        MainActivity.instance?.clearCalendarHighlight()
    }

    private fun selectItem(template: Template) {
        selectedPosition = items.indexOf(template)
        MainActivity.getViewModel()?.targetTemplate?.value = template
    }

    fun notifyListChanged() {
        TransitionManager.beginDelayedTransition(addBtn, makeChangeBounceTransition())
        (addBtn.layoutParams as FrameLayout.LayoutParams).gravity = if(MainActivity.getTargetFolder().id == "calendar") {
            decoBtns.visibility = View.VISIBLE
            Gravity.LEFT
        }else {
            decoBtns.visibility = View.GONE
            Gravity.RIGHT
        }
        addBtn.requestLayout()

        if(isExpanded()) {
            val newItems = ArrayList<Template>()
            filterCurrentFolder(newItems)
            Thread {
                val diffResult = DiffUtil.calculateDiff(TemplateDiffCallback(items, newItems))
                Handler(Looper.getMainLooper()).post{
                    if(isExpanded()) {
                        items.clear()
                        items.addAll(newItems)
                        diffResult.dispatchUpdatesTo(adapter)
                    }
                }
            }.start()
        }else {
            filterCurrentFolder(items)
            adapter.notifyDataSetChanged()
        }
    }

    private fun filterCurrentFolder(result: ArrayList<Template>) {
        result.clear()
        MainActivity.getViewModel()?.templateList?.value?.filter {
            it.folder?.id == MainActivity.getTargetFolder().id
        }?.forEach {
            val template = Template()
            template.copy(it)
            result.add(template)
        }
    }

    fun getAddButton(): CardView? = templatePanel

    fun isExpanded() = behavior.state != BottomSheetBehavior.STATE_HIDDEN
}