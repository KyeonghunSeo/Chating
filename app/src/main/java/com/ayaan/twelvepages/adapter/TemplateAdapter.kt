package com.ayaan.twelvepages.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.manager.SymbolManager
import com.ayaan.twelvepages.model.Template
import io.realm.Realm
import kotlinx.android.synthetic.main.list_item_template.view.*
import java.util.*

class TemplateAdapter(val context: Context, val items: ArrayList<Template>,
                      private val adapterInterface: (template: Template?, mode: Int) -> Unit)
    : RecyclerView.Adapter<TemplateAdapter.ViewHolder>() {

    var itemTouchHelper: ItemTouchHelper? = null

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    var mode = 0

    override fun getItemCount(): Int = items.size + 1

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            setGlobalTheme(container)
        }
        fun onItemSelected() {}
        fun onItemClear() {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_template, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val v = holder.itemView
        if(position < items.size) {
            val template = items[position]
            if(mode == 0) {
                v.contentLy.alpha = 1f
                v.contentLy.setBackgroundResource(R.drawable.blank)
            }else {
                v.contentLy.alpha = 0.5f
                v.contentLy.setBackgroundResource(R.drawable.edit_mode_background_dash)
            }
            v.colorImg.visibility = View.GONE
            v.cardView.setCardBackgroundColor(AppTheme.background)
            v.cardView.cardElevation = dpToPx(10f)
            v.titleText.text = template.title
            val color = ColorManager.getColor(template.colorKey)
            v.colorImg.setImageBitmap(null)
            v.colorBtn.setCardBackgroundColor(color)

            if(template.tags.isNotEmpty()) {
                v.tagText.visibility = View.VISIBLE
                v.tagText.text = template.tags.joinToString(prefix = "#", separator = "#", transform = { it.title.toString() })
            }else {
                v.tagText.visibility = View.GONE
            }

            v.setOnClickListener {
                adapterInterface.invoke(template, mode)
                if(mode == 1) endEditMode()
            }
            v.setOnLongClickListener {
                if(mode == 0) {
                }else {
                    itemTouchHelper?.startDrag(holder)
                }
                return@setOnLongClickListener true
            }
        }else {
            v.cardView.setCardBackgroundColor(AppTheme.background)
            v.cardView.cardElevation = 0f
            v.contentLy.alpha = 0.5f
            v.contentLy.setBackgroundResource(R.drawable.blank)
            v.colorImg.visibility = View.VISIBLE
            v.tagText.visibility = View.GONE
            if(mode == 0) {
                v.colorImg.setImageResource(R.drawable.setting)
                v.titleText.text = context.getString(R.string.edit_template)
            }else {
                v.colorImg.setImageResource(R.drawable.add)
                v.titleText.text = context.getString(R.string.new_template)
            }
            v.colorBtn.setCardBackgroundColor(Color.TRANSPARENT)
            v.colorImg.setColorFilter(AppTheme.primaryText)
            v.setOnClickListener {
                if(mode == 0) {
                    startEditMode()
                }else {
                    endEditMode()
                    adapterInterface.invoke(null, mode)
                }
            }
            v.setOnLongClickListener {
                endEditMode()
                return@setOnLongClickListener true
            }
        }
    }

    private fun startEditMode() {
        mode = 1
        notifyDataSetChanged()
        toast(R.string.long_tab_to_move)
    }

    private fun endEditMode() {
        mode = 0
        notifyDataSetChanged()
    }

    private fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if(fromPosition < items.size && toPosition < items.size) {
            Collections.swap(items, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            return true
        }
        return false
    }

    inner class SimpleItemTouchHelperCallback(private val mAdapter: TemplateAdapter) : ItemTouchHelper.Callback() {
        private val ALPHA_FULL = 1.0f
        private var reordering = false

        override fun isLongPressDragEnabled(): Boolean = false
        override fun isItemViewSwipeEnabled(): Boolean = false

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            val dragFlags = ItemTouchHelper.START or ItemTouchHelper.END
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
                        realm.where(Template::class.java).equalTo("id", template.id).findFirst()?.let{
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