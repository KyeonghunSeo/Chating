package com.ayaan.twelvepages.ui.view

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.adapter.RecordListAdapter
import com.ayaan.twelvepages.adapter.util.ListDiffCallback
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.manager.RepeatManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.dialog.PopupOptionDialog
import com.ayaan.twelvepages.ui.dialog.SchedulingDialog
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmResults
import kotlinx.android.synthetic.main.view_note.view.*
import java.util.*

class NoteView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private var recordList: RealmResults<Record>? = null
    private val items = ArrayList<Record>()
    private val newItmes = ArrayList<Record>()
    private val adapter = RecordListAdapter(context, items, Calendar.getInstance(), true) { view, item, action ->
        MainActivity.instance?.let { activity ->
            showDialog(PopupOptionDialog(activity,
                    arrayOf(PopupOptionDialog.Item(str(R.string.copy), R.drawable.copy, AppTheme.primaryText),
                            PopupOptionDialog.Item(str(R.string.cut), R.drawable.cut, AppTheme.primaryText),
                            PopupOptionDialog.Item(str(R.string.move_to_calendar), R.drawable.calendar, AppTheme.primaryText),
                            PopupOptionDialog.Item(str(R.string.delete), R.drawable.delete, AppTheme.red)), view, false) { index ->
                val record = Record().apply { copy(item) }
                when(index) {
                    0 -> {
                        record.id = null
                        activity.viewModel.clip(record)
                    }
                    1 -> {
                        activity.viewModel.clip(record)
                    }
                    2 -> {
                        showDialog(SchedulingDialog(activity, record, 0) { sCal, eCal ->
                            record.folder = MainActivity.getViewModel()?.getCalendarFolder()
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

    init {
        LayoutInflater.from(context).inflate(R.layout.view_note, this, true)
        setBackgroundColor(AppTheme.background)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if(recyclerView.computeVerticalScrollOffset() > 0) topShadow.visibility = View.VISIBLE
                else topShadow.visibility = View.GONE
            }
        })
        adapter.itemTouchHelper?.attachToRecyclerView(recyclerView)

/*
        folderTitleText.setOnClickListener {
            TransitionManager.beginDelayedTransition(recyclerView, makeChangeBounceTransition())
            if(layout == 0) {
                layout = 1
                recyclerView.layoutManager = LinearLayoutManager(context)
            }else {
                layout = 0
                recyclerView.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
            }
            recyclerView.adapter?.notifyDataSetChanged()
        }*/
    }

    fun notifyDataChanged() {
        MainActivity.getTargetFolder().let { folder ->
            folderNameText.text = folder.name
            folderNameText.setTypeface(AppTheme.boldFont, Typeface.BOLD)
            recordList?.removeAllChangeListeners()
            recordList = RecordManager.getRecordList(folder)
            recordList?.addChangeListener { result, changeSet ->
                l("==========노트뷰 데이터 변경 시작=========")
                val t = System.currentTimeMillis()
                if(changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                    updateData(result, items)
                    adapter.notifyDataSetChanged()
                }else if(changeSet.state == OrderedCollectionChangeSet.State.UPDATE) {
                    updateData(result, newItmes)
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
                l("==========노트뷰 데이터 변경 종료=========")
            }
        }
    }

    private fun updateData(data: RealmResults<Record>, list: ArrayList<Record>) {
        list.clear()
        data.forEach { list.add(it.makeCopyObject()) }

        if(list.isNotEmpty()) {
            emptyLy.visibility = View.GONE
        }else {
            emptyLy.visibility = View.VISIBLE
        }
    }
}