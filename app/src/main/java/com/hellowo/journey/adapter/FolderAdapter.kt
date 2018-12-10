package com.hellowo.journey.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.dpToPx
import com.hellowo.journey.model.Folder
import com.hellowo.journey.ui.activity.MainActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.list_item_folder.view.*
import java.util.*

class FolderAdapter(val context: Context, private val items: ArrayList<Folder>,
                    private val adapterInterface: (action: Int, folder: Folder?) -> Unit)
    : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {
    private val z = dpToPx(10f)

    var itemTouchHelper: ItemTouchHelper? = null

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    override fun getItemCount(): Int = items.size + 1

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            itemView.contentLy.setCardBackgroundColor(AppTheme.backgroundColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_folder, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val v = holder.itemView
        if(position < items.size) {
            val folder = items[position]
            v.titleText.text = folder.name
            v.setOnClickListener { adapterInterface.invoke(0, folder) }
        }else {
            v.titleText.text = "!!!"
            v.setOnClickListener { adapterInterface.invoke(1, null) }
        }
    }

    private fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if(fromPosition < items.size) {
            Collections.swap(items, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            return true
        }
        return false
    }

    inner class SimpleItemTouchHelperCallback(private val mAdapter: FolderAdapter) : ItemTouchHelper.Callback() {
        private val ALPHA_FULL = 1.0f
        private var reordering = false

        override fun isLongPressDragEnabled(): Boolean = true

        override fun isItemViewSwipeEnabled(): Boolean = true

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
                if (viewHolder is TaskListAdapter.ViewHolder) {
                    // Let the view holder know that this item is being moved or dragged
                    val itemViewHolder = viewHolder as TaskListAdapter.ViewHolder?
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
            (viewHolder as? TaskListAdapter.ViewHolder)?.onItemClear()
        }
    }
}