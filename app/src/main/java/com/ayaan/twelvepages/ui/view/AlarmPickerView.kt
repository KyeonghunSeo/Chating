package com.ayaan.twelvepages.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.setGlobalTheme
import kotlinx.android.synthetic.main.list_item_alarm_picker.view.*

class AlarmPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {
    var onSelected : ((Long) -> Unit)? = null
    val items = ArrayList<AlarmItem>()
    private val offsets = arrayOf(
            0,
            1000L * 60 * 60 * 9,
            1000L * 60 * 60 * 12,
            1000L * 60 * 60 * 18,
            -1000L * 60 * 10,
            -1000L * 60 * 30,
            -1000L * 60 * 60,
            -1000L * 60 * 120,
            -1000L * 60 * 60 * 24,
            -1000L * 60 * 60 * 24 * 2,
            -1000L * 60 * 60 * 24 * 7,
            Long.MIN_VALUE)

    init {
        layoutManager = GridLayoutManager(context, 4)
        resources.getStringArray(R.array.alarms).forEachIndexed { index, it ->
            items.add(AlarmItem(it, offsets[index]))
        }
        adapter = PickerAdapter()
    }

    inner class AlarmItem(val title: String, val offset: Long)

    inner class PickerAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                setGlobalTheme(container)
                container.layoutParams.width = dpToPx(70)
                container.layoutParams.height = dpToPx(70)
                container.requestLayout()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_alarm_picker, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            val v = holder.itemView
            v.titleText.text = item.title

            if(false) {
                v.titleText.setTextColor(Color.WHITE)
                v.titleText.typeface = AppTheme.boldFont
                v.contentLy.setBackgroundColor(AppTheme.primaryColor)
                v.contentLy.alpha = 1f
            }else {
                v.titleText.setTextColor(AppTheme.primaryColor)
                v.titleText.typeface = AppTheme.regularFont
                v.contentLy.setBackgroundColor(AppTheme.disableText)
                v.contentLy.alpha = 0.4f
            }

            v.setOnClickListener {
                notifyItemChanged(position)
                onSelected?.invoke(item.offset)
            }
        }
    }
}