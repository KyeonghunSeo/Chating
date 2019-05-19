package com.hellowo.journey.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.R
import com.hellowo.journey.dpToPx
import com.hellowo.journey.model.Tag
import com.hellowo.journey.setGlobalTheme
import com.xiaofeng.flowlayoutmanager.Alignment
import com.xiaofeng.flowlayoutmanager.FlowLayoutManager
import kotlinx.android.synthetic.main.list_item_tag.view.*

class TagView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {
    companion object {
        const val MODE_NORMAL = 0
        const val MODE_CHECK = 1
        const val MODE_EDIT = 2
        val tagSize = dpToPx(30)
    }

    var onSelected : ((Tag?, Int) -> Unit)? = null
    val items = ArrayList<Tag>()
    var checkedItems = ArrayList<Tag>()
    var mode = MODE_NORMAL

    init {
        val flowLayoutManager = FlowLayoutManager()
        flowLayoutManager.isAutoMeasureEnabled = true
        flowLayoutManager.setAlignment(Alignment.LEFT)
        flowLayoutManager.isMeasurementCacheEnabled = false
        layoutManager = flowLayoutManager
        adapter = Adapter()
    }

    fun setItems(newList: List<Tag>, checkedList: ArrayList<Tag>?) {
        items.clear()
        checkedItems.clear()
        newList.mapTo(items) { Tag(it.id, it.title, it.order) }
        checkedList?.mapTo(checkedItems) { Tag(it.id, it.title, it.order) }
        adapter?.notifyDataSetChanged()
    }

    fun startEditMode() {
        mode = MODE_EDIT
        adapter?.notifyDataSetChanged()
    }

    fun endEditMode() {
        mode = MODE_CHECK
        adapter?.notifyDataSetChanged()
    }

    fun addNewTag(tag: Tag?) {
        tag?.let {
            val newTag = Tag(it.id, it.title, it.order)
            items.add(newTag)
            checkedItems.add(newTag)
            adapter?.notifyItemInserted(items.size - 1)
        }
    }

    fun changeTag(oldTag: Tag) {
        val index = items.indexOfFirst { it.id == oldTag.id }
        if(index >= 0) {
            items[index].title = oldTag.title
            adapter?.notifyItemChanged(index)
        }
        checkedItems.firstOrNull { it.id == oldTag.id }?.let { it.title = oldTag.title }
    }

    fun deleteTag(oldTag: Tag) {
        val index = items.indexOfFirst { it.id == oldTag.id }
        if(index >= 0) {
            items.removeAt(index)
            adapter?.notifyItemRemoved(index)
        }
        checkedItems.firstOrNull { it.id == oldTag.id }?.let { checkedItems.remove(it) }
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size + if(mode == MODE_EDIT) 1 else 0

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                setGlobalTheme(container)
                container.layoutParams.height = tagSize
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_tag, parent, false))

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder.itemView
            if(position < items.size) {
                val tag = items[position]
                v.contentLy.setBackgroundResource(R.drawable.normal_rect_stroke)
                when(mode) {
                    MODE_NORMAL -> {
                        v.tagText.text = "#${tag.title}"
                        v.contentLy.alpha = 1f
                        v.deleteBtn.visibility = View.GONE
                    }
                    MODE_CHECK, MODE_EDIT -> {
                        if(checkedItems.any { tag.id == it.id }) {
                            v.tagText.text = "#${tag.title}"
                            v.contentLy.alpha = 1f
                        }else {
                            v.tagText.text = "${tag.title}"
                            v.contentLy.alpha = 0.3f
                        }

                        if(mode == MODE_EDIT) {
                            v.deleteBtn.visibility = View.VISIBLE
                            v.deleteBtn.setOnClickListener {
                                onSelected?.invoke(tag, -1)
                            }
                        }else {
                            v.deleteBtn.visibility = View.GONE
                        }
                    }
                }
                v.setOnClickListener {
                    if(mode == MODE_CHECK) {
                        if(checkedItems.any { tag.id == it.id }) {
                            checkedItems.firstOrNull { tag.id == it.id }?.let { checkedItems.remove(it) }
                        }else {
                            checkedItems.add(tag)
                        }
                        notifyItemChanged(position)
                    }
                    onSelected?.invoke(tag, 0)
                }
            }else {
                v.contentLy.setBackgroundResource(R.drawable.edit_dash_rect)
                v.contentLy.alpha = 0.3f
                v.deleteBtn.visibility = View.GONE
                v.tagText.text = context.getString(R.string.new_tag)
                v.setOnClickListener {
                    onSelected?.invoke(null, 0)
                }
            }
        }
    }
}