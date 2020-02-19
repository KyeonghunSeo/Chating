package com.ayaan.twelvepages.ui.view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
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

        bottomSheet.setOnClickListener { collapse() }
        templatePanel.setOnClickListener {}

        setOnTouchListener { _, motionEvent ->
            if(motionEvent.action == MotionEvent.ACTION_DOWN) {
                if(MainActivity.isProfileOpened()) {
                    MainActivity.closeProfileView()
                    return@setOnTouchListener true
                }else if(isExpanded() && addLy.visibility == View.VISIBLE) {
                    collapse()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener super.onTouchEvent(motionEvent)
        }
        //haha
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

        editBtn.setOnClickListener {
            if(adapter.mode == 0) {
                adapter.mode = 1
            }else {
                adapter.mode = 0
            }
            initViews()
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
            templateDateText.text = makeSheduleText(startCal.timeInMillis, endCal.timeInMillis,
                    false, false, false, true)
        }else {
            templateDateText.text = folder.name
        }
    }

    fun expand(dtStart: Long, dtEnd: Long) {
        vibrate(context)
        startCal.timeInMillis = dtStart
        endCal.timeInMillis = dtEnd
        setDate()
        initViews()
        clipLy.visibility = View.GONE
        addLy.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(backgroundLy, "alpha", backgroundLy.alpha, 1f).start()
        MainActivity.instance?.window?.let { dimStatusBar(it) }
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        if(AppStatus.templateMode == 0) {
            startExpandAnimation()
        }else {
            MainActivity.getViewModel()?.targetTemplate?.value = null
            MainActivity.getViewModel()?.makeNewTimeObject(startCal.timeInMillis, endCal.timeInMillis)
        }
    }

    private fun initViews() {
        if(MainActivity.getTargetFolder().id == "calendar") {
            if(adapter.mode == 0) {
                decoBtns.visibility = View.VISIBLE
            }else {
                decoBtns.visibility = View.GONE
            }
        }else {
            decoBtns.visibility = View.GONE
        }

        if(adapter.mode == 0) {
            editBtn.setCardBackgroundColor(AppTheme.lightLine)
            editImg.setImageResource(R.drawable.edit)
            editImg.setColorFilter(AppTheme.primaryText)
            editText.text = str(R.string.edit_template)
            editText.setTextColor(AppTheme.primaryText)
        }else {
            editBtn.setCardBackgroundColor(AppTheme.blue)
            editImg.setImageResource(R.drawable.done)
            editImg.setColorFilter(AppTheme.background)
            editText.text = str(R.string.edit_done)
            editText.setTextColor(AppTheme.background)
        }

        adapter.notifyDataSetChanged()
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
        ObjectAnimator.ofFloat(backgroundLy, "alpha", backgroundLy.alpha, 0f).start()
        MainActivity.instance?.window?.let { removeDimStatusBar(it) }
        MainActivity.instance?.clearCalendarHighlight()
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun hiddened() {
        ObjectAnimator.ofFloat(backgroundLy, "alpha", backgroundLy.alpha, 0f).start()
        MainActivity.instance?.window?.let { removeDimStatusBar(it) }
        adapter.mode = 0
        adapter.notifyDataSetChanged()
        MainActivity.instance?.clearCalendarHighlight()
    }

    private fun selectItem(template: Template) {
        selectedPosition = items.indexOf(template)
        MainActivity.getViewModel()?.targetTemplate?.value = template
    }

    fun notifyListChanged() {
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