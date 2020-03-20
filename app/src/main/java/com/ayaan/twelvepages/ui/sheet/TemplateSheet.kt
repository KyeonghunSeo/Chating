package com.ayaan.twelvepages.ui.sheet

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import com.ayaan.twelvepages.adapter.TemplateAdapter
import com.ayaan.twelvepages.adapter.util.TemplateDiffCallback
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.model.Template
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.activity.RecordActivity
import com.ayaan.twelvepages.ui.activity.TemplateActivity
import com.ayaan.twelvepages.ui.dialog.BottomSheetDialog
import com.ayaan.twelvepages.ui.dialog.StickerPickerDialog
import com.ayaan.twelvepages.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.sheet_template.*
import kotlinx.android.synthetic.main.sheet_template.view.*
import java.util.*
import kotlin.collections.ArrayList


class TemplateSheet(dtStart: Long, dtEnd: Long) : BottomSheetDialog() {
    private val startCal = Calendar.getInstance()
    private val endCal = Calendar.getInstance()
    private val items = ArrayList<Template>()
    private var selectedPosition = 0
    private val adapter = TemplateAdapter(App.context, items) { template, mode ->
        if(template != null) {
            if(mode == 0) {
                selectItem(template)
                MainActivity.getViewModel()?.makeNewTimeObject(startCal.timeInMillis, endCal.timeInMillis)
                dismiss()
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
        startCal.timeInMillis = dtStart
        endCal.timeInMillis = dtEnd
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.sheet_template)
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        setLayout()
        val mainViewModel: MainViewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        mainViewModel.templateList.observe(this, androidx.lifecycle.Observer { notifyListChanged() })
        dialog.setOnShowListener {
            vibrate(requireContext())
        }
    }

    private fun setLayout() {
        root.recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        root.recyclerView.adapter = adapter
        root.recyclerView.post { root.recyclerView.scrollToPosition(0) }
        adapter.itemTouchHelper?.attachToRecyclerView(root.recyclerView)
        root.stickerBtn.setOnClickListener { addSticker() }
        root.datePointBtn.setOnClickListener { addDatePoint() }
        setDate()
        initViews()
    }

    private fun addSticker() {
        MainActivity.instance?.let {
            StickerPickerDialog{ sticker, position ->
                val records = ArrayList<Record>()
                while (startCal <= endCal) {
                    val dtStart = getCalendarTime0(startCal)
                    val dtEnd = getCalendarTime23(startCal)
                    records.add(RecordManager.makeNewRecord(dtStart, dtEnd).apply {
                        id = "sticker_${UUID.randomUUID()}"
                        dtCreated = System.currentTimeMillis()
                        setFormula(RecordCalendarAdapter.Formula.STICKER)
                        setSticker(sticker, position)
                    })
                    startCal.add(Calendar.DATE, 1)
                }
                RecordManager.save(records)
                toast(R.string.saved, R.drawable.done)
                dismiss()
            }.show(it.supportFragmentManager, null)
        }
    }

    private fun addDatePoint() {
        MainActivity.instance?.let {
            StickerPickerDialog{ sticker, position ->
                val records = ArrayList<Record>()
                while (startCal <= endCal) {
                    val dtStart = getCalendarTime0(startCal)
                    val dtEnd = getCalendarTime23(startCal)
                    records.add(RecordManager.makeNewRecord(dtStart, dtEnd).apply {
                        id = "sticker_${UUID.randomUUID()}"
                        dtCreated = System.currentTimeMillis()
                        setFormula(RecordCalendarAdapter.Formula.DATE_POINT)
                        setSticker(sticker, position)
                    })
                    startCal.add(Calendar.DATE, 1)
                }
                RecordManager.save(records)
                toast(R.string.saved, R.drawable.done)
                dismiss()
            }.show(it.supportFragmentManager, null)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDate() {
        val folder = MainActivity.getTargetFolder()
        if(folder.isCalendar()) {
            root.templateDateText.text = makeSheduleText(startCal.timeInMillis, endCal.timeInMillis,
                    false, false, false, true)
        }else {
            root.templateDateText.text = folder.name
        }
    }

    private fun initViews() {
        if(MainActivity.getTargetFolder().id == "calendar") {
            if(adapter.mode == 0) {
                root.decoBtns.visibility = View.VISIBLE
            }else {
                root.decoBtns.visibility = View.GONE
            }
        }else {
            root.decoBtns.visibility = View.GONE
        }
        adapter.notifyDataSetChanged()
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        MainActivity.instance?.clearCalendarHighlight()
    }

    private fun selectItem(template: Template) {
        selectedPosition = items.indexOf(template)
        MainActivity.getViewModel()?.targetTemplate?.value = template
    }

    private fun notifyListChanged() {
        val newItems = ArrayList<Template>()
        filterCurrentFolder(newItems)
        Thread {
            val diffResult = DiffUtil.calculateDiff(TemplateDiffCallback(items, newItems))
            Handler(Looper.getMainLooper()).post{
                items.clear()
                items.addAll(newItems)
                diffResult.dispatchUpdatesTo(adapter)
            }
        }.start()
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

}
