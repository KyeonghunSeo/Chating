package com.ayaan.twelvepages.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import kotlinx.android.synthetic.main.list_item_chip.view.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter.Formula.*
import com.ayaan.twelvepages.ui.view.RecordView.Shape.*

class ShapePicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {
    lateinit var items: Array<RecordView.Shape>
    var onSelected : ((RecordView.Shape) -> Unit)? = null
    var shape = RECT_FILL_BLUR
    var formula = SINGLE_TEXT

    init {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        adapter = Adapter()
    }

    fun refresh(f: RecordCalendarAdapter.Formula, s: RecordView.Shape) {
        formula = f
        shape = s
        items = formula.shapes
        adapter?.notifyDataSetChanged()
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                container.layoutParams.width = WRAP_CONTENT
                setGlobalTheme(container)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_chip, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder.itemView
            val item = items[position]
            v.titleText.text = context.getString(items[position].nameId)

            if(item == shape) {
                v.titleText.setTextColor(Color.WHITE)
                v.titleText.typeface = AppTheme.boldFont
                v.contentLy.setBackgroundColor(AppTheme.secondaryText)
                v.contentLy.alpha = 1f
            }else {
                v.titleText.setTextColor(AppTheme.secondaryText)
                v.titleText.typeface = AppTheme.regularFont
                v.contentLy.setBackgroundColor(AppTheme.lightLine)
                v.contentLy.alpha = 0.4f
            }

            v.setOnClickListener {
                if(item != shape) {
                    notifyItemChanged(items.indexOf(shape))
                    notifyItemChanged(position)
                    shape = item
                    onSelected?.invoke(shape)
                }
            }
        }
    }
}