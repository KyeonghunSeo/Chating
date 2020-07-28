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
                      private val layoutId: Int = R.layout.list_item_template,
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
            = ViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))

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
            v.backgroundLy.visibility = View.VISIBLE
            v.symbolImg.visibility = View.GONE
            v.cardView.setCardBackgroundColor(AppTheme.background)
            v.cardView.cardElevation = dpToPx(10f)
            v.colorBtn.setImageResource(R.drawable.color_bg)
            v.titleText.text = template.title
            val color = ColorManager.getColor(template.colorKey)
            v.colorBtn.setColorFilter(color)
            v.backColorView.setBackgroundColor(color)

            if(template.tags.isNotEmpty()) {
                v.tagText.visibility = View.GONE
                v.tagText.text = template.tags.joinToString(prefix = "#", separator = "#", transform = { it.title.toString() })
            }else {
                v.tagText.visibility = View.GONE
            }

            if(template.isSetCheckBox()) {
                v.checkImg.visibility = View.VISIBLE
            }else {
                v.checkImg.visibility = View.GONE
            }

            if(template.isSetTime()) {
                v.timeImg.visibility = View.VISIBLE
            }else {
                v.timeImg.visibility = View.GONE
            }

            if(template.alarmDayOffset != Int.MIN_VALUE) {
                v.alarmImg.visibility = View.VISIBLE
            }else {
                v.alarmImg.visibility = View.GONE
            }

            v.setOnClickListener { adapterInterface.invoke(template, mode) }
            v.setOnLongClickListener {
                if(mode == 0) {
                }else {
                    itemTouchHelper?.startDrag(holder)
                }
                return@setOnLongClickListener true
            }
        }else {
            v.backgroundLy.visibility = View.VISIBLE
            v.symbolImg.visibility = View.GONE
            v.tagText.visibility = View.GONE
            v.checkImg.visibility = View.GONE
            v.timeImg.visibility = View.GONE
            v.alarmImg.visibility = View.GONE
            v.cardView.setCardBackgroundColor(AppTheme.background)
            v.cardView.cardElevation = dpToPx(1f)
            v.contentLy.alpha = 0.5f
            if(mode == 0) {
                v.contentLy.setBackgroundResource(R.drawable.blank)
                v.colorBtn.setImageResource(R.drawable.setting)
                v.symbolImg.setImageResource(R.drawable.blank)
                v.titleText.text = context.getString(R.string.edit_template)
            }else {
                v.contentLy.setBackgroundResource(R.drawable.edit_mode_background_dash)
                v.colorBtn.setImageResource(R.drawable.add_rect)
                v.titleText.text = context.getString(R.string.new_template)
            }
            v.backColorView.setBackgroundColor(AppTheme.secondaryText)
            v.colorBtn.setColorFilter(AppTheme.secondaryText)
            v.symbolImg.setColorFilter(AppTheme.secondaryText)
            v.setOnClickListener {
                if(mode == 0) {
                    startEditMode()
                }else {
                    endEditMode()
                    adapterInterface.invoke(null, mode)
                }
            }
            v.setOnLongClickListener {
                return@setOnLongClickListener true
            }
        }
    }

    private fun startEditMode() {
        mode = 1
        notifyDataSetChanged()
        toast(R.string.long_tab_to_move)
    }

    fun endEditMode() {
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
            val dragFlags = ItemTouchHelper.START or ItemTouchHelper.END or ItemTouchHelper.UP or ItemTouchHelper.DOWN
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