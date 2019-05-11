package com.hellowo.journey.adapter

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.*
import com.hellowo.journey.adapter.util.FolderDiffCallback
import com.hellowo.journey.model.Folder
import com.hellowo.journey.ui.activity.MainActivity
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.list_item_folder.view.*
import java.util.*
import kotlin.collections.ArrayList

class FolderAdapter(val context: Context, private val items: ArrayList<Folder>,
                    private val adapterInterface: (action: Int, folder: Folder) -> Unit)
    : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    val itemWidth = dpToPx(70)
    val maxTextWidth = dpToPx(170)
    val itemSpace = dpToPx(20)
    var itemTouchHelper: ItemTouchHelper? = null

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    override fun getItemCount(): Int = items.size + 1

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            setGlobalTheme(container)
            itemView.layoutParams.width = itemWidth
            itemView.setBackgroundColor(AppTheme.disableText)
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
            v.iconImg.visibility = View.GONE

            val title = if(folder.name.isNullOrBlank()) {
                context.getString(R.string.untitle)
            }else {
                folder.name
            }
            v.titleText.text = title
            //val textWidth = Math.min(maxTextWidth, v.titleText.paint.measureText(title).toInt())
            val textWidth = maxTextWidth
            v.contentLy.layoutParams.width = textWidth
            v.rootLy.layoutParams.height = textWidth + itemSpace
            v.requestLayout()
            setTabViews(v, folder.id == MainActivity.getTargetFolder().id)
            v.setOnClickListener {
                vibrate(context)
                setTabViews(v, true)
                adapterInterface.invoke(0, folder)
            }
        }else {
            v.titleText.text = ""
            v.edgeTop.visibility = View.GONE
            v.edgeBottom.visibility = View.GONE
            v.divider.visibility = View.GONE
            v.contentLy.layoutParams.width = itemWidth
            v.contentLy.cardElevation = dpToPx(0f)
            v.rootLy.layoutParams.height = itemWidth
            v.iconImg.visibility = View.VISIBLE
            v.iconImg.setImageResource(R.drawable.sharp_add_black_48dp)
            v.iconImg.setColorFilter(AppTheme.primaryText)
            v.iconImg.alpha = 0.5f
            v.contentLy.setCardBackgroundColor(AppTheme.disableText)
            v.setOnClickListener { adapterInterface.invoke(1, Folder()) }
        }
    }

    private fun setTabViews(v: View, selected: Boolean) {
        if(selected) {
            v.edgeTop.visibility = View.VISIBLE
            v.edgeBottom.visibility = View.VISIBLE
            v.divider.visibility = View.GONE
            v.contentLy.setCardBackgroundColor(AppTheme.backgroundColor)
            v.contentLy.cardElevation = dpToPx(4f)
            v.iconImg.setColorFilter(AppTheme.primaryText)
            v.titleText.setTextColor(AppTheme.primaryText)
            v.titleText.alpha = 1f
            v.bringToFront()
        }else {
            v.edgeTop.visibility = View.GONE
            v.edgeBottom.visibility = View.GONE
            v.divider.visibility = View.VISIBLE
            v.contentLy.setCardBackgroundColor(AppTheme.disableText)
            v.contentLy.cardElevation = dpToPx(0f)
            v.iconImg.setColorFilter(AppTheme.primaryText)
            v.titleText.setTextColor(AppTheme.primaryText)
            v.titleText.alpha = 0.5f
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
        val newItems = ArrayList<Folder>()
        Realm.getDefaultInstance().use { realm ->
            list.forEach { newItems.add(realm.copyFromRealm(it)) }
        }
        Thread {
            val diffResult = DiffUtil.calculateDiff(FolderDiffCallback(items, newItems))
            items.clear()
            items.addAll(newItems)
            Handler(Looper.getMainLooper()).post{
                diffResult.dispatchUpdatesTo(this)
            }
        }.start()
    }

    private var selectedFolder: Folder? = null

    fun setTargetFolder(newSelectedFolder: Folder?) {
        selectedFolder?.let { selectedFolder->
            items.firstOrNull { it.id == selectedFolder.id }?.let {
                //notifyItemChanged(items.indexOf(it))
            }
        }

        newSelectedFolder?.let { newSelectedFolder ->
            l("폴더 선택 : $newSelectedFolder")
            items.firstOrNull { it.id == newSelectedFolder.id }?.let {
                //notifyItemChanged(items.indexOf(it))
            }
        }

        selectedFolder = newSelectedFolder
        notifyDataSetChanged()
    }

    inner class SimpleItemTouchHelperCallback(private val mAdapter: FolderAdapter) : ItemTouchHelper.Callback() {
        private val ALPHA_FULL = 1.0f
        private var reordering = false

        override fun isLongPressDragEnabled(): Boolean = true

        override fun isItemViewSwipeEnabled(): Boolean = false

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            reordering = reordering or mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            adapterInterface.invoke(-1, items[viewHolder.adapterPosition])
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            // We only want the active item to change
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder is FolderAdapter.ViewHolder) {
                    // Let the view holder know that this item is being moved or dragged
                    val itemViewHolder = viewHolder as FolderAdapter.ViewHolder?
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
            (viewHolder as? FolderAdapter.ViewHolder)?.onItemClear()
        }
    }
}