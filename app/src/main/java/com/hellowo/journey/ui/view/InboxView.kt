package com.hellowo.journey.ui.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.transition.*
import com.hellowo.journey.*
import com.hellowo.journey.R
import com.hellowo.journey.adapter.FolderAdapter
import com.hellowo.journey.adapter.RecordListAdapter
import com.hellowo.journey.adapter.util.ListDiffCallback
import com.hellowo.journey.manager.RecordManager
import com.hellowo.journey.model.Folder
import com.hellowo.journey.model.Record
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.dialog.EditFolderDialog
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmResults
import kotlinx.android.synthetic.main.view_keep.view.*
import java.util.*

class InboxView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : CardView(context, attrs, defStyleAttr) {

    private var recordList: RealmResults<Record>? = null
    private val items = ArrayList<Record>()
    private val newItmes = ArrayList<Record>()
    private val adapter = RecordListAdapter(context, items, Calendar.getInstance()) { view, timeObject, action ->
        when(action) {
            0 -> {
                MainActivity.instance?.viewModel?.let { it.targetTimeObject.value = timeObject }
            }
        }
    }
    private val folderItems = ArrayList<Folder>()
    private val folderAdapter = FolderAdapter(context, folderItems) { action, folder ->
        when(action) {
            0 -> {
                if(MainActivity.instance?.viewModel?.targetFolder?.value == folder) {
                    val dialog = EditFolderDialog(MainActivity.instance!!, folder!!) { result ->
                        if(result) {

                        }else { // deleted
                            MainActivity.instance?.viewModel?.setTargetFolder()
                        }
                        notifyDataChanged()
                    }
                    showDialog(dialog, true, true, true, false)
                }else {
                    MainActivity.instance?.viewModel?.targetFolder?.value = folder
                    notifyDataChanged()
                }
            }
            1 -> {
                val newFolder = Folder()
                val dialog = EditFolderDialog(MainActivity.instance!!, newFolder) { result ->
                    if(result) {
                        folderListView.post { folderListView.smoothScrollToPosition(folderItems.size) }
                    }
                }
                showDialog(dialog, true, true, true, false)
            }
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_keep, this, true)
        setOnClickListener {}

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.itemTouchHelper?.attachToRecyclerView(recyclerView)

        folderListView.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        folderListView.adapter = folderAdapter
        folderAdapter.itemTouchHelper?.attachToRecyclerView(folderListView)

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
        MainActivity.instance?.viewModel?.targetFolder?.value?.let { folder ->
            folderAdapter.notifyDataSetChanged()
            recordList?.removeAllChangeListeners()
            recordList = RecordManager.getRecordList(folder)
            recordList?.addChangeListener { result, changeSet ->
                l("==========킵뷰 데이터 변경 시작=========")
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
                l("==========킵뷰 데이터 변경 종료=========")
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

    fun notifyFolderDataChanged() {
        MainActivity.instance?.viewModel?.folderList?.value?.let { list ->
            folderItems.clear()
            folderItems.addAll(list)
            folderAdapter.notifyDataSetChanged()
        }
    }
}