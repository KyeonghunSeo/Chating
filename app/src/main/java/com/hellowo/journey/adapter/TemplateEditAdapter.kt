package com.hellowo.journey.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.App
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.model.Template
import com.hellowo.journey.model.Record
import com.hellowo.journey.ui.view.RecordView
import io.realm.Realm
import kotlinx.android.synthetic.main.list_item_edit_template.view.*
import java.util.*

class TemplateEditAdapter(val context: Context, private val items: ArrayList<Template>,
                          private val adapterInterface: (action: Int, template: Template) -> Unit)
    : RecyclerView.Adapter<TemplateEditAdapter.ViewHolder>() {

    var itemTouchHelper: ItemTouchHelper? = null

    init {
        val callback = SimpleItemTouchHelperCallback(this)
        itemTouchHelper = ItemTouchHelper(callback)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            val timeObjectView = RecordView(context, Record(), 0, 0)
            timeObjectView.record.title = context.getString(R.string.contents_example)
            timeObjectView.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            itemView.previewContainer.addView(timeObjectView, 0)
            itemView.contentLy.setCardBackgroundColor(AppTheme.backgroundColor)
        }
        fun onItemSelected() {}
        fun onItemClear() {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_edit_template, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val v = holder.itemView
        val template = items[position]

        v.titleText.hint = context.getString(R.string.template_title)
        v.titleText.text = template.title
        v.titleText.setOnClickListener { adapterInterface.invoke(3, template) }

        v.typeBtn.setOnClickListener { adapterInterface.invoke(4, template) }

        v.colorImg.setColorFilter(AppTheme.getColor(template.colorKey))
        v.colorBtn.setOnClickListener {
            adapterInterface.invoke(0, template)
        }

        if(template.tags.isNotEmpty()) {
            v.tagText.text = template.tags.joinToString("") { "#${it.id}" }
            v.tagText.setTextColor(AppTheme.primaryText)
        }else {
            v.tagText.text = App.context.getString(R.string.no_tag)
            v.tagText.setTextColor(AppTheme.disableText)
        }
        v.tagBtn.setOnClickListener { adapterInterface.invoke(1, template) }

        v.inCalendarBtn.setOnClickListener { adapterInterface.invoke(2, template) }


        (v.previewContainer.getChildAt(0) as RecordView).let {
            it.record.type = template.type
            it.record.style = template.style
            it.record.colorKey = template.colorKey
            it.setLookByType()

            when(it.record.type) {
                2 -> {
                    it.layoutParams.height = WRAP_CONTENT
                }
                else -> {
                    it.layoutParams.height = RecordView.blockTypeSize
                }
            }

            it.textSpaceWidth = it.paint.measureText(it.text.toString())
            it.requestLayout()
            it.invalidate()
        }

        v.styleBtn.setOnClickListener { adapterInterface.invoke(5, template) }
    }

    private fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(items, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    inner class SimpleItemTouchHelperCallback(private val mAdapter: TemplateEditAdapter) : ItemTouchHelper.Callback() {
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
                if (viewHolder is TemplateEditAdapter.ViewHolder) {
                    // Let the view holder know that this item is being moved or dragged
                    val itemViewHolder = viewHolder as TemplateEditAdapter.ViewHolder?
                    itemViewHolder?.onItemSelected()
                }
            }else if(reordering && actionState == ItemTouchHelper.ACTION_STATE_IDLE){
                reordering = false
                val realm = Realm.getDefaultInstance()
                realm.executeTransaction{ _ ->
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