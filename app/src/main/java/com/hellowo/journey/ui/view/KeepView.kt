package com.hellowo.journey.ui.view

import android.animation.LayoutTransition
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.journey.*
import com.hellowo.journey.adapter.EventListAdapter
import com.hellowo.journey.adapter.FolderAdapter
import com.hellowo.journey.adapter.TimeObjectListAdapter
import com.hellowo.journey.adapter.util.ListDiffCallback
import com.hellowo.journey.manager.TimeObjectManager
import com.hellowo.journey.model.Folder
import com.hellowo.journey.model.Template
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.dialog.CustomDialog
import com.hellowo.journey.ui.dialog.TypePickerDialog
import io.realm.OrderedCollectionChangeSet
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_edit_template.*
import kotlinx.android.synthetic.main.view_keep.view.*
import java.util.*

class KeepView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr) {
    companion object

    var viewMode = ViewMode.CLOSED
    private var timeObjectList: RealmResults<TimeObject>? = null
    private val items = ArrayList<TimeObject>()
    private val newItmes = ArrayList<TimeObject>()
    private val adapter = TimeObjectListAdapter(context, items) { view, timeObject, action ->
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
                MainActivity.instance?.viewModel?.targetFolder?.value = folder
                notifyDataChanged()

            }
            1 -> {
                val dialog = CustomDialog(MainActivity.instance!!,
                        context.getString(R.string.create_folder), null, null) { result, _, title ->
                    if(result) {
                        Realm.getDefaultInstance().use { realm ->
                            realm.executeTransaction {
                                realm.createObject(Folder::class.java, UUID.randomUUID().toString()).apply {
                                    name = title
                                    order = realm.where(Folder::class.java).max("order")?.toInt()?.plus(1) ?: 0
                                }
                            }
                        }
                        folderListView.post { folderListView.smoothScrollToPosition(folderItems.size) }
                    }
                }
                showDialog(dialog, true, true, true, false)
                dialog.showInput(context.getString(R.string.enter_folder_name), "")
            }
        }
    }
    private var layout = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.view_keep, this, true)
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
        recyclerView.adapter = adapter
        adapter.itemTouchHelper?.attachToRecyclerView(recyclerView)

        folderListView.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        folderListView.adapter = folderAdapter
        folderAdapter.itemTouchHelper?.attachToRecyclerView(folderListView)

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
        }

        setCardBackgroundColor(AppTheme.backgroundColor)
    }

    fun notifyDataChanged() {
        MainActivity.instance?.viewModel?.targetFolder?.value?.let { folder ->
            folderTitleText.text = folder.name
            folderAdapter.notifyDataSetChanged()
            timeObjectList?.removeAllChangeListeners()
            timeObjectList = TimeObjectManager.getTimeObjectList(folder)
            timeObjectList?.addChangeListener { result, changeSet ->
                l("==========킵뷰 데이터 변경 시작=========")
                val t = System.currentTimeMillis()
                if(changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                    items.clear()
                    result.forEach { items.add(it.makeCopyObject()) }
                    adapter.notifyDataSetChanged()
                }else if(changeSet.state == OrderedCollectionChangeSet.State.UPDATE) {
                    newItmes.clear()
                    result.forEach { newItmes.add(it.makeCopyObject()) }
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

    fun notifyFolderDataChanged() {
        MainActivity.instance?.viewModel?.folderList?.value?.let { list ->
            folderItems.clear()
            folderItems.addAll(list)
            folderAdapter.notifyDataSetChanged()
        }
    }

    fun show() {
        visibility = View.VISIBLE
        viewMode = ViewMode.OPENED
        notifyDataChanged()
    }

    fun hide() {
        visibility = View.GONE
        viewMode = ViewMode.CLOSED
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}