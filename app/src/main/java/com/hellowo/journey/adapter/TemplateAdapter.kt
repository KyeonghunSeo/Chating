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
import com.hellowo.journey.model.Template
import com.hellowo.journey.ui.activity.MainActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.list_item_template.view.*
import java.util.*

class TemplateAdapter(val context: Context, val items: ArrayList<Template>,
                      private val adapterInterface: (template: Template, mode: Int) -> Unit)
    : RecyclerView.Adapter<TemplateAdapter.ViewHolder>() {

    var itemTouchHelper: ItemTouchHelper? = null

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    var mode = 0

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            itemView.contentLy.setCardBackgroundColor(AppTheme.backgroundColor)
            itemView.titleText.setTextColor(AppTheme.primaryText)
            itemView.tagText.setTextColor(AppTheme.primaryText)
            itemView.pinImg.setColorFilter(AppTheme.primaryText)
            itemView.typeImg.setColorFilter(AppTheme.primaryText)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_template, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val v = holder.itemView
        val template = items[position]

        if(mode == 0) {
            v.colorImg.setImageBitmap(null)
        }else {
            v.colorImg.setImageResource(R.drawable.template_edit_forground)
        }

        v.titleText.text = template.title
        v.titleText.setTextColor(AppTheme.getFontColor(template.colorKey))
        v.colorImg.setBackgroundColor(AppTheme.getColor(template.colorKey))
        //v.typeImg.setColorFilter(AppTheme.getColor(template.colorKey))

        if(template.tags.isNotEmpty()) {
            v.tagText.visibility = View.VISIBLE
            v.tagText.text = template.tags.joinToString("") { "#${it.id}" }
        }else {
            v.tagText.visibility = View.GONE
            v.tagText.text = context.getString(R.string.no_tag)
        }

        val folder = MainActivity.instance?.viewModel?.targetFolder?.value
        v.contentLy.cardElevation = dpToPx(3f)
        v.contentLy.alpha = 1f
        v.setOnClickListener { adapterInterface.invoke(template, mode) }
    }

    private fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    inner class SimpleItemTouchHelperCallback(private val mAdapter: TemplateAdapter) : ItemTouchHelper.Callback() {
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

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            // We only want the active item to change
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                if (viewHolder is TemplateEditAdapter.ViewHolder) {
                    // Let the view holder know that this item is being moved or dragged
                    val itemViewHolder = viewHolder as TemplateEditAdapter.ViewHolder?
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
            (viewHolder as? TemplateEditAdapter.ViewHolder)?.onItemClear()
        }
    }
}