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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import androidx.transition.*
import com.hellowo.journey.*
import com.hellowo.journey.R
import com.hellowo.journey.adapter.FolderAdapter
import com.hellowo.journey.adapter.TimeObjectListAdapter
import com.hellowo.journey.adapter.util.ListDiffCallback
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.Folder
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.dialog.EditFolderDialog
import io.realm.OrderedCollectionChangeSet
import io.realm.RealmResults
import kotlinx.android.synthetic.main.view_keep.view.*
import java.util.*

class KeepView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    var viewMode = ViewMode.CLOSED
    private var timeObjectList: RealmResults<TimeObject>? = null
    private val items = ArrayList<TimeObject>()
    private val newItmes = ArrayList<TimeObject>()
    private val adapter = TimeObjectListAdapter(context, items, Calendar.getInstance()) { view, timeObject, action ->
        when(action) {
            0 -> {
                MainActivity.instance?.viewModel?.let {
                    it.targetTimeObject.value = timeObject
                    it.targetView.value = view
                }
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
    private var layout = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.view_keep, this, true)
        contentLy.setBackgroundColor(AppTheme.backgroundColor)
        contentLy.visibility = View.GONE
        contentLy.setOnClickListener {}
        expandBtn.visibility = View.GONE

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.itemTouchHelper?.attachToRecyclerView(recyclerView)

        folderListView.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        folderListView.adapter = folderAdapter
        folderAdapter.itemTouchHelper?.attachToRecyclerView(folderListView)

        expandBtn.setOnClickListener {
            expand()
        }
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

    private fun notifyDataChanged() {
        MainActivity.instance?.viewModel?.targetFolder?.value?.let { folder ->
            folderAdapter.notifyDataSetChanged()
            timeObjectList?.removeAllChangeListeners()
            timeObjectList = TimeObjectManager.getTimeObjectList(folder)
            timeObjectList?.addChangeListener { result, changeSet ->
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

    private fun updateData(data: RealmResults<TimeObject>, list: ArrayList<TimeObject>) {
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

    private fun expand() {
        val transiion = makeChangeBounceTransition()
        transiion.addListener(object : TransitionListenerAdapter(){
            override fun onTransitionEnd(transition: Transition) {}
        })
        TransitionManager.beginDelayedTransition(contentLy, transiion)
        (contentLy.layoutParams as FrameLayout.LayoutParams).let {
            it.width = MATCH_PARENT
        }
        contentLy.requestLayout()
    }

    fun show() {
        val transitionSet = TransitionSet()
        val t1 = makeFromRightSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_IN }
        t1.addTarget(contentLy)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        transitionSet.duration = 300L
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.setBackgroundColor(AppTheme.primaryText)
        backgroundLy.setOnClickListener { MainActivity.getViewModel()?.clearTargetFolder() }
        backgroundLy.isClickable = true
        backgroundLy.visibility = View.VISIBLE
        contentLy.visibility = View.VISIBLE
        expandBtn.visibility = View.VISIBLE
        viewMode = ViewMode.OPENED
        notifyDataChanged()
    }

    fun hide() {
        val transitionSet = TransitionSet()
        val t1 = makeFromRightSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_OUT }
        t1.addTarget(contentLy)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        transitionSet.duration = 300L
        transitionSet.addListener(object : TransitionListenerAdapter(){
            override fun onTransitionEnd(transition: Transition) {
                super.onTransitionEnd(transition)
                contentLy.layoutParams.width = dpToPx(250)
                expandBtn.visibility = View.GONE
            }
        })
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.setOnClickListener(null)
        backgroundLy.isClickable = false
        backgroundLy.visibility = View.GONE
        contentLy.visibility = View.GONE
        viewMode = ViewMode.CLOSED
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}