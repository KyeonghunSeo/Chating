package com.hellowo.journey.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.R
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
    }

    var onSelected : ((TagItem?, Int) -> Unit)? = null
    val items = ArrayList<TagItem>()
    var checkedItems: ArrayList<Tag>? = null
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
        newList.mapTo(items) {
            TagItem(it.id ?: "", it.order)
        }
        checkedItems = checkedList
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
            items.add(TagItem(it.id ?: "", it.order))
            adapter?.notifyItemInserted(items.size - 1)
        }
    }

    fun changeTag(oldId: String, id: String) {
        val index = items.indexOfFirst { it.title == oldId }
        if(index >= 0) {
            items[index].title = id
            adapter?.notifyItemChanged(index)
        }
    }

    fun deleteTag(oldId: String, id: String) {
        val index = items.indexOfFirst { it.title == oldId }
        if(index >= 0) {
            items[index].title = id
            adapter?.notifyItemChanged(index)
        }
    }

    inner class TagItem(var title: String, var order: Int)

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size + if(mode == MODE_EDIT) 1 else 0

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                setGlobalTheme(container)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_tag, parent, false))

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder.itemView
            if(position < items.size) {
                val tag = items[position]
                v.tagText.text = "#${tag.title}"
                v.contentLy.setBackgroundResource(R.drawable.normal_rect_stroke)

                if(mode == MODE_EDIT) {
                    v.deleteBtn.visibility = View.VISIBLE
                    v.deleteBtn.setOnClickListener {
                        onSelected?.invoke(tag, -1)
                    }
                    v.contentLy.alpha = 1f
                    v.setOnClickListener {
                        onSelected?.invoke(tag, 0)
                    }
                }else {
                    v.deleteBtn.visibility = View.GONE
                    if(mode == MODE_CHECK) {
                        v.contentLy.alpha = 1f
                        v.setOnClickListener {
                            onSelected?.invoke(tag, 0)
                        }
                    }else {
                        v.contentLy.alpha = 1f
                        v.setOnClickListener {
                            onSelected?.invoke(tag, 0)
                        }
                    }
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