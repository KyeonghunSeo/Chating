package com.ayaan.twelvepages.adapter

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.util.FolderDiffCallback
import com.ayaan.twelvepages.model.Folder
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.ayaan.twelvepages.ui.dialog.EditFolderDialog
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.list_item_folder.view.*
import java.util.*
import kotlin.collections.ArrayList

class FolderAdapter(val context: Context, private var items: ArrayList<Folder>)
    : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    val itemWidth = dpToPx(40)
    val maxTextWidth = dpToPx(220)
    val edgeSize = dpToPx(3f)
    val backTextColor = AppTheme.secondaryText
    var itemTouchHelper: ItemTouchHelper? = null
    val viewHolders = ArrayList<ViewHolder>()

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    override fun getItemCount(): Int = items.size + 1

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        var id: String? = null
        init {
            setGlobalTheme(container)
            itemView.layoutParams.width = itemWidth
            itemView.layoutParams.height = maxTextWidth
            (itemView.contentLy.layoutParams as FrameLayout.LayoutParams).let {
                it.width = maxTextWidth
                it.height = itemWidth
            }
            viewHolders.add(this)
        }
        fun onItemSelected() {}
        fun onItemClear() {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_folder, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val v = holder.itemView
        if(position < items.size) {
            val folder = items[position]
            holder.id = folder.id

            v.titleText.text = if(folder.name.isNullOrBlank()) context.getString(R.string.untitle) else folder.name
            v.iconImg.visibility = View.VISIBLE
            if(folder.isCalendar()) v.iconImg.setImageResource(R.drawable.calendar)
            else v.iconImg.setImageResource(R.drawable.memo)
            v.divider.visibility = View.VISIBLE
            v.divider.translationX = edgeSize
            v.contentLy.gravity = Gravity.CENTER
            setFolderViews(v, folder.id == MainActivity.getTargetFolder().id)

            v.setOnClickListener {
                vibrate(context)
                if(folder.id != MainActivity.getTargetFolder().id) {
                    selectFolder(folder)
                }else {
                    editFolder(folder)
                }
            }
            v.setOnLongClickListener {
                if(folder.id != MainActivity.getTargetFolder().id) { selectFolder(folder) }
                itemTouchHelper?.startDrag(holder)
                return@setOnLongClickListener true
            }
        }else {
            v.titleText.text = ""
            v.contentLy.setBackgroundColor(Color.TRANSPARENT)
            v.contentLy.alpha = 0.4f
            v.contentLy.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
            v.edgeTop.visibility = View.GONE
            v.edgeBottom.visibility = View.GONE
            v.divider.visibility = View.GONE
            v.iconImg.visibility = View.VISIBLE
            v.iconImg.setImageResource(R.drawable.add)
            v.iconImg.setColorFilter(backTextColor)
            v.contentLy.translationX = edgeSize
            v.edgeTop.translationX = edgeSize
            v.edgeBottom.translationX = edgeSize
            v.setOnClickListener { createFolder() }
        }
    }

    private fun createFolder() {
        val dialog = EditFolderDialog(context as Activity, Folder()) { result -> }
        showDialog(dialog, true, true, true, false)
    }

    private fun editFolder(folder: Folder) {
        val index = items.indexOf(folder)
        val dialog = EditFolderDialog(context as Activity, folder) { result ->
            if(!result) { // 삭제되었을때
                val nextIndex = if(index + 1 < items.size) index + 1 else index - 1
                MainActivity.getViewModel()?.setTargetFolder(items[nextIndex])
            }
        }
        showDialog(dialog, true, true, true, false)
    }

    private fun selectFolder(folder: Folder) {
        MainActivity.getViewModel()?.setTargetFolder(folder)
    }

    private fun setFolderViews(v: View, selected: Boolean) {
        if(selected) {
            v.contentLy.setBackgroundColor(AppTheme.background)
            v.iconImg.setColorFilter(AppTheme.secondaryText)
            v.titleText.setTextColor(AppTheme.secondaryText)
            v.contentLy.alpha = 1f
            v.edgeTop.visibility = View.VISIBLE
            v.edgeBottom.visibility = View.VISIBLE
            v.contentLy.translationX = 0f
            v.edgeTop.translationX = 0f
            v.edgeBottom.translationX = 0f
        }else {
            v.contentLy.setBackgroundColor(Color.TRANSPARENT)
            v.iconImg.setColorFilter(backTextColor)
            v.titleText.setTextColor(backTextColor)
            v.contentLy.alpha = 0.4f
            v.edgeTop.visibility = View.GONE
            v.edgeBottom.visibility = View.GONE
            v.contentLy.translationX = edgeSize
            v.edgeTop.translationX = edgeSize
            v.edgeBottom.translationX = edgeSize
        }
    }

    private fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if(fromPosition < items.size && toPosition < items.size) {
            Collections.swap(items, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            return true
        }
        return false
    }

    fun refresh(list: RealmResults<Folder>) {
        l("[FolderList refresh]")
        val newItems = ArrayList<Folder>()
        list.mapTo(newItems) { Folder(it) }
        if(MainActivity.isFolderOpen()) {
            Thread {
                val diffResult = DiffUtil.calculateDiff(FolderDiffCallback(items, newItems))
                items = newItems
                Handler(Looper.getMainLooper()).post{
                    if(MainActivity.isFolderOpen()) {
                        diffResult.dispatchUpdatesTo(this)
                    }
                }
            }.start()
        }else {
            items = newItems
            notifyDataSetChanged()
        }
    }

    fun setNewItems(list: RealmResults<Folder>) {
        val newItems = ArrayList<Folder>()
        list.mapTo(newItems) { Folder(it) }
        items = newItems
        notifyDataSetChanged()
    }

    private var selectedItemId : String? = null

    fun setTargetFolder(newSelectedFolder: Folder, recyclerView: RecyclerView, panel: FrameLayout) {
        l("[setTargetFolder]")
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val firstPos = layoutManager.findFirstVisibleItemPosition()
        val prevSelectedItemPos = items.indexOfFirst { it.id == selectedItemId }
        val selectedItemPos = items.indexOfFirst { it.id == newSelectedFolder.id }
        selectedItemId = newSelectedFolder.id

        viewHolders.forEach {
            setFolderViews( it.itemView, it.id == MainActivity.getTargetFolder().id)
        }

        if(MainActivity.isFolderOpen()) {
            val animSet = AnimatorSet()
            animSet.duration = ANIM_DUR
            animSet.interpolator = FastOutSlowInInterpolator()
            val animList = ArrayList<Animator>()
            (0 until layoutManager.childCount).forEach { index ->
                val realPos = firstPos + index
                layoutManager.findViewByPosition(realPos)?.let {
                    if(realPos == selectedItemPos) {
                        animList.add(ObjectAnimator.ofFloat(it.contentLy, "translationX", edgeSize, 0f))
                        animList.add(ObjectAnimator.ofFloat(it.edgeTop, "translationX", edgeSize, 0f))
                        animList.add(ObjectAnimator.ofFloat(it.edgeBottom, "translationX", edgeSize, 0f))
                    }else {
                        if(realPos == prevSelectedItemPos) {
                            animList.add(ObjectAnimator.ofFloat(it.contentLy, "translationX", 0f, edgeSize))
                            animList.add(ObjectAnimator.ofFloat(it.edgeTop, "translationX", 0f, edgeSize))
                            animList.add(ObjectAnimator.ofFloat(it.edgeBottom, "translationX", 0f, edgeSize))
                        }
                    }
                    return@let
                }
            }
            animList.add(ObjectAnimator.ofFloat(panel, "translationX", edgeSize * 5, 0f))
            animList.add(ObjectAnimator.ofFloat(panel, "alpha", 0f, 1f))
            animSet.playTogether(animList)
            animSet.start()
        }
    }

    inner class SimpleItemTouchHelperCallback(private val mAdapter: FolderAdapter) : ItemTouchHelper.Callback() {
        private val ALPHA_FULL = 1.0f
        private var reordering = false

        override fun isLongPressDragEnabled(): Boolean = false
        override fun isItemViewSwipeEnabled(): Boolean = false

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            reordering = reordering or mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            // We only want the active item to change
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder is ViewHolder) {
                    // Let the view holder know that this item is being moved or dragged
                    val itemViewHolder = viewHolder as ViewHolder?
                    itemViewHolder?.onItemSelected()
                }
            }else if(reordering && actionState == ItemTouchHelper.ACTION_STATE_IDLE){
                reordering = false
                val realm = Realm.getDefaultInstance()
                realm.executeTransaction{
                    items.forEachIndexed { index, template ->
                        realm.where(Folder::class.java).equalTo("id", template.id).findFirst()?.let{
                            it.order = index
                        }
                    }
                }
                realm.close()
            }

            super.onSelectedChanged(viewHolder, actionState)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            viewHolder.itemView.alpha = ALPHA_FULL
            (viewHolder as? ViewHolder)?.onItemClear()
        }
    }
}