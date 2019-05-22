package com.hellowo.journey.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.R
import com.hellowo.journey.dpToPx
import com.hellowo.journey.model.Record
import com.hellowo.journey.setGlobalTheme
import kotlinx.android.synthetic.main.list_item_normal.view.*
import java.util.*

class InCalendarStyleTypePickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {

    private val items = context.resources.getStringArray(R.array.record_in_calendar_style_types)
    var onSelected : ((Int) -> Unit)? = null
    var type = 0

    init {
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
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
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_normal, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val v = holder.itemView
            v.titleText.text = items[position]
            v.iconImg.setImageResource(R.drawable.menu)

            if(position == type) {
                v.contentLy.alpha = 1f
                v.contentLy.setBackgroundResource(R.drawable.normal_rect_stroke)
            }else {
                v.contentLy.alpha = 0.5f
                v.contentLy.setBackgroundResource(R.drawable.blank)
            }

            v.setOnClickListener {
                if(type != position) {
                    notifyItemChanged(type)
                    notifyItemChanged(position)
                    type = position
                    onSelected?.invoke(position)
                }
            }
        }
    }
}