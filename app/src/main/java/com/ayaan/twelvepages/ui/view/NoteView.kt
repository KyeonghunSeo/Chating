package com.ayaan.twelvepages.ui.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.adapter.RecordListAdapter
import com.ayaan.twelvepages.adapter.util.ListDiffCallback
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.ui.activity.MainActivity
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmResults
import kotlinx.android.synthetic.main.view_note.view.*
import java.util.*

class NoteView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : CardView(context, attrs, defStyleAttr) {

    private var recordList: RealmResults<Record>? = null
    private val items = ArrayList<Record>()
    private val newItmes = ArrayList<Record>()
    private val adapter = RecordListAdapter(context, items, Calendar.getInstance(), true) { view, timeObject, action ->
        when(action) {
            0 -> {
            }
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_note, this, true)
        setOnClickListener {}

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
            titleText.text = folder.name
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