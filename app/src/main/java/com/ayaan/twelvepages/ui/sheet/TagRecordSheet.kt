package com.ayaan.twelvepages.ui.sheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.adapter.SimpleRecordListAdapter
import com.ayaan.twelvepages.adapter.util.ListDiffCallback
import com.ayaan.twelvepages.manager.RecordManager
import com.ayaan.twelvepages.model.Record
import com.ayaan.twelvepages.model.Tag
import com.ayaan.twelvepages.str
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.dialog.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.realm.OrderedCollectionChangeSet
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.sheet_tag_record.view.*

class TagRecordSheet(private val tag: Tag, private val record: Record?) : BottomSheetDialog() {
    private val realm = Realm.getDefaultInstance()
    private var recordList: RealmResults<Record>? = null
    private val items = ArrayList<Record>()
    private val newItmes = ArrayList<Record>()
    private var isInit = true

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style, R.layout.sheet_tag_record)
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        setLayout()
        dialog.setOnShowListener {}
    }

    @SuppressLint("SetTextI18n")
    private fun setLayout() {
        root.titleText.text = "#${tag.title}"

        val tags = ArrayList<String>()
        tags.add(tag.title!!)

        root.recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = SimpleRecordListAdapter(context!!, items) {
            MainActivity.getViewModel()?.targetRecord?.value = it
        }
        root.recyclerView.adapter = adapter
        root.showMoreBtn.setOnClickListener {
            isInit = false
            root.showMoreBtn.visibility = View.GONE
            loadData(adapter)
        }
        loadData(adapter)
    }

    private fun loadData(adapter: SimpleRecordListAdapter) {
        recordList?.removeAllChangeListeners()
        recordList = RecordManager.getRecordList(tags = ArrayList())
        recordList?.addChangeListener { result, changeSet ->
            root.countText.text = String.format(str(R.string.total_count), result.size)
            if(changeSet.state == OrderedCollectionChangeSet.State.INITIAL) {
                items.clear()
                if(isInit && result.size > 3) {
                    root.showMoreBtn.visibility = View.VISIBLE
                    result.take(3).forEach { items.add(it.makeCopyObject()) }
                }else {
                    root.showMoreBtn.visibility = View.GONE
                    result.forEach { items.add(it.makeCopyObject()) }
                }
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
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        recordList?.removeAllChangeListeners()
        realm.close()
    }
}
