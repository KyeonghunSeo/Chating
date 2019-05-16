package com.hellowo.journey.ui.view

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
import io.realm.RealmList
import kotlinx.android.synthetic.main.list_item_tag.view.*

class TagView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {

    var onSelected : ((TagItem) -> Unit)? = null
    val items = ArrayList<TagItem>()
    var mode = 0

    init {
        val flowLayoutManager = FlowLayoutManager()
        flowLayoutManager.isAutoMeasureEnabled = true
        flowLayoutManager.setAlignment(Alignment.LEFT)
        flowLayoutManager.isMeasurementCacheEnabled = false
        layoutManager = flowLayoutManager
        adapter = Adapter()
    }

    fun setItems(newItems: RealmList<Tag>) {
        items.clear()
        newItems.mapTo(items) {
            TagItem(it?.id ?: "", it.order, true)
        }
        adapter?.notifyDataSetChanged()
    }

    inner class TagItem(var title: String, var order: Int, var checked: Boolean)

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size + if(mode == 0) 0 else 1

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                setGlobalTheme(container)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_tag, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder.itemView
            if(position < items.size) {
                val tag = items[position]
                v.tagText.text = tag.title
                v.setOnClickListener {
                    onSelected?.invoke(tag)
                }
            }else {
                v.tagText.text = context.getString(R.string.new_tag)
            }
        }
    }
}