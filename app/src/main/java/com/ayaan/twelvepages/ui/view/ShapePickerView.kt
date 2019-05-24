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
import androidx.transition.TransitionManager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter
import kotlinx.android.synthetic.main.list_item_tab.view.*
import com.ayaan.twelvepages.adapter.RecordCalendarAdapter.Formula.*

class ShapePickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {

    lateinit var items: Array<String>
    var onSelected : ((Int) -> Unit)? = null
    var shape = 0
    var formula = TOP_STACK

    init {
        setItems()
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        adapter = Adapter()
    }

    private fun setItems() {
        if(formula == MULTI_LINE) {
            items = context.resources.getStringArray(R.array.shape_multi_line)
        }else {
            items = context.resources.getStringArray(R.array.shape_single_line)
        }
    }

    fun refresh(formula: RecordCalendarAdapter.Formula) {
        this.formula = formula
        shape = 0
        setItems()
        TransitionManager.beginDelayedTransition(this, makeFromBottomSlideTransition())
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
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_tab, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder.itemView
            v.titleText.text = items[position]
            v.iconImg.setImageResource(R.drawable.menu)

            if(position == shape) {
                v.titleText.setTextColor(Color.WHITE)
                v.titleText.typeface = AppTheme.boldFont
                v.contentLy.setBackgroundColor(AppTheme.primaryColor)
            }else {
                v.titleText.setTextColor(AppTheme.secondaryText)
                v.titleText.typeface = AppTheme.regularFont
                v.contentLy.setBackgroundResource(R.drawable.blank)
            }

            v.setOnClickListener {
                if(shape != position) {
                    notifyItemChanged(shape)
                    notifyItemChanged(position)
                    shape = position
                    onSelected?.invoke(position)
                }
            }
        }
    }
}