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
import kotlinx.android.synthetic.main.list_item_chip.view.*

class DateBgStylePicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {
    val items = arrayListOf(
            context.getString(R.string.date_bg_color),
            context.getString(R.string.date_bg_hatched),
            context.getString(R.string.date_bg_bold_hatched),
            context.getString(R.string.date_bg_circle_dot_pattern)
    )
    var onSelected : ((Int) -> Unit)? = null
    var selectedPos = 0

    init {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        adapter = Adapter()
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
            v.titleText.text = item

            if(position == selectedPos) {
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
                if(position != selectedPos) {
                    notifyItemChanged(selectedPos)
                    notifyItemChanged(position)
                    selectedPos = position
                    onSelected?.invoke(selectedPos)
                }
            }
        }
    }
}