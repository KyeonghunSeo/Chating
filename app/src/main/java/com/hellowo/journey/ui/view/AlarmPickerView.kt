package com.hellowo.journey.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.AppRes
import com.hellowo.journey.R
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
            Long.MIN_VALUE
    )

    init {
        layoutManager = GridLayoutManager(context, 4)
        AppRes.resources.getStringArray(R.array.alarms).forEachIndexed { index, it ->
            items.add(AlarmItem(it, offsets[index]))
        }
        adapter = PickerAdapter()
    }

    inner class AlarmItem(val title: String, val offset: Long)

    inner class PickerAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                (container.layoutParams as RecyclerView.LayoutParams).let {

                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_alarm_picker, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = items[position]
            val v = holder.itemView
            v.textView.text = item.title

            v.setOnClickListener {
                notifyItemChanged(position)
                onSelected?.invoke(item.offset)
            }
        }
    }
}