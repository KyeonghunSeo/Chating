package com.hellowo.journey.adapter

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.adapter.util.FolderDiffCallback
import com.hellowo.journey.dpToPx
import com.hellowo.journey.model.Folder
import com.hellowo.journey.setGlobalTheme
import com.hellowo.journey.ui.activity.MainActivity
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.list_item_folder.view.*
import java.util.*
import kotlin.collections.ArrayList

class FolderAdapter(val context: Context, private val items: ArrayList<Folder>,
                    private val adapterInterface: (action: Int, folder: Folder) -> Unit)
    : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    val itemHeight = dpToPx(50)
    var itemTouchHelper: ItemTouchHelper? = null

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    override fun getItemCount(): Int = items.size + 1

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            setGlobalTheme(container)
            itemView.rootLy.layoutParams.height = itemHeight
            itemView.rootLy.requestLayout()
        }
        fun onItemSelected() {}
        fun onItemClear() {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_folder, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val v = holder.itemView
        if(position < items.size) {
            v.titleText.visibility = View.VISIBLE
            v.iconImg.visibility = View.GONE
            val folder = items[position]
            if(folder.name.isNullOrBlank()) {
                v.titleText.text = context.getString(R.string.untitle)
            }else {
                v.titleText.text = folder.name
            }
            if(folder.id == MainActivity.instance?.viewModel?.targetFolder?.value?.id) {
                v.rootLy.setBackgroundColor(AppTheme.backgroundColor)
                v.titleText.setTextColor(AppTheme.primaryText)
            }else {
                v.rootLy.setBackgroundColor(Color.TRANSPARENT)
                v.titleText.setTextColor(AppTheme.disableText)
            }
            v.setOnClickListener { adapterInterface.invoke(0, folder) }
        }else {
            v.titleText.visibility = View.GONE
            v.iconImg.visibility = View.VISIBLE
            v.iconImg.setImageResource(R.drawable.sharp_add_black_48dp)
            v.iconImg.setColorFilter(AppTheme.disableText)
            v.rootLy.setBackgroundColor(Color.TRANSPARENT)
            v.setOnClickListener { adapterInterface.invoke(1, Folder()) }
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

    inner class SimpleItemTouchHelperCallback(private val mAdapter: FolderAdapter) : ItemTouchHelper.Callback() {
        private val ALPHA_FULL = 1.0f
        private var reordering = false

        override fun isLongPressDragEnabled(): Boolean = true

        override fun isItemViewSwipeEnabled(): Boolean = true

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
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
                realm.executeTransaction{ _ ->
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