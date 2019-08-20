package com.ayaan.twelvepages.ui.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.adapter.TemplateAdapter
import com.ayaan.twelvepages.adapter.util.TemplateDiffCallback
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.manager.StickerManager
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.model.Link
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.model.Template
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.activity.TemplateActivity
import com.ayaan.twelvepages.ui.dialog.StickerPickerDialog
import io.realm.Realm
import kotlinx.android.synthetic.main.view_template.view.*
import java.util.*
import kotlin.collections.ArrayList

class TemplateView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val startCal = Calendar.getInstance()
    private val endCal = Calendar.getInstance()
    val items = ArrayList<Template>()
    var selectedPosition = 0
    var isExpanded = false
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
        initViews()
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recyclerView.adapter = adapter
        adapter.itemTouchHelper?.attachToRecyclerView(recyclerView)
        addBtn.setOnLongClickListener {
            return@setOnLongClickListener false
        }
        templateIconImg.setOnClickListener {
            if(isExpanded) {
                collapse()
                /*
                if(adapter.mode == 0) {
                    adapter.mode = 1
                    adapter.notifyItemInserted(items.size)
                }else {
                    adapter.mode = 0
                    adapter.notifyDataSetChanged()
                }
                */
            }else {
                MainActivity.getTargetTime()?.let { expand(it, it) }
            }
        }

        stickerBtn.setOnClickListener {
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

        datePointBtn.setOnClickListener {
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
    }

    @SuppressLint("SetTextI18n")
    private fun setDate() {
        val folder = MainActivity.getTargetFolder()
        if(folder.isCalendar()) {
            templateFolderText.text = folder.name
            templateDateText.text = makeSheduleText(startCal.timeInMillis, endCal.timeInMillis,
                    false, false, false, true)
        }else {
            templateFolderText.text = folder.name
            templateDateText.text = ""
        }
    }

    fun expand(dtStart: Long, dtEnd: Long) {
        vibrate(context)
        startCal.timeInMillis = dtStart
        endCal.timeInMillis = dtEnd
        if(AppStatus.templateMode == 0) {
            val transitionSet = TransitionSet()
            val t1 = makeChangeBounceTransition()
            val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_IN }
            t1.addTarget(addBtn)
            t2.addTarget(backgroundLy)
            transitionSet.addTransition(t1)
            transitionSet.addTransition(t2)
            transitionSet.duration = 200L
            TransitionManager.beginDelayedTransition(this, transitionSet)
            setDate()
            notifyListChanged()
            backgroundLy.setBackgroundColor(AppTheme.background)
            backgroundLy.setOnClickListener { collapse() }
            backgroundLy.isClickable = true
            backgroundLy.visibility = View.VISIBLE
            addLy.visibility = View.VISIBLE
            addBtn.layoutParams.let {
                it.height = WRAP_CONTENT
            }
            startExpandAnimation()
            isExpanded = true
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

            val transitionSet = TransitionSet()
            val t1 = makeChangeBounceTransition()
            t1.addTarget(addBtn)
            transitionSet.addTransition(t1)
            transitionSet.duration = 200L
            TransitionManager.beginDelayedTransition(this, transitionSet)
            clipLy.visibility = View.VISIBLE
            addBtn.layoutParams.let {
                it.height = WRAP_CONTENT
            }
            startExpandAnimation()
            isExpanded = true
        }
    }

    private fun startExpandAnimation() {
        val animSet = AnimatorSet()
        animSet.playTogether(
                ObjectAnimator.ofFloat(templateIconImg, "alpha", templateIconImg.alpha, 0f),
                ObjectAnimator.ofFloat(addBtn, "elevation", addBtn.elevation, dpToPx(8f)))
        animSet.start()
    }

    fun collapse() {
        val transitionSet = TransitionSet()
        val t1 = makeChangeBounceTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_OUT }
        t1.addTarget(addBtn)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        transitionSet.duration = 200L
        TransitionManager.beginDelayedTransition(this, transitionSet)
        initViews()
        MainActivity.instance?.clearCalendarHighlight()
        val animSet = AnimatorSet()
        animSet.playTogether(
                ObjectAnimator.ofFloat(templateIconImg, "alpha", templateIconImg.alpha, 1f),
                ObjectAnimator.ofFloat(addBtn, "elevation", addBtn.elevation, 0f))
        animSet.start()
    }

    fun collapseNoAnim() {
        templateIconImg.alpha = 1f
        addBtn.radius = 0f
        addBtn.elevation = 0f
        initViews()
        MainActivity.instance?.clearCalendarHighlight()
    }

    private fun initViews() {
        clearTemplate()
        backgroundLy.setOnClickListener(null)
        backgroundLy.isClickable = false
        clipLy.visibility = View.GONE
        addLy.visibility = View.GONE
        backgroundLy.visibility = View.GONE

        addBtn.layoutParams.let {
            it.height = dpToPx(40)
        }

        adapter.mode = 0
        isExpanded = false
    }

    private fun selectItem(template: Template) {
        selectedPosition = items.indexOf(template)
        MainActivity.getViewModel()?.targetTemplate?.value = template
    }

    private fun clearTemplate() {
        items.clear()
        adapter.notifyDataSetChanged()
    }

    fun notifyListChanged() {
        if(isExpanded) {
            val newItems = ArrayList<Template>()
            filterCurrentFolder(newItems)
            Thread {
                val diffResult = DiffUtil.calculateDiff(TemplateDiffCallback(items, newItems))
                Handler(Looper.getMainLooper()).post{
                    if(isExpanded) {
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

    fun getAddButton() = addBtn
}