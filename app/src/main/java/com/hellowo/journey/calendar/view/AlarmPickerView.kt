package com.hellowo.journey.calendar.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.AppRes
import com.hellowo.journey.R
import com.hellowo.journey.calendar.model.TimeObject
import com.hellowo.journey.calendar.model.TimeObject.Type
import kotlinx.android.synthetic.main.list_item_alarm_picker.view.*

class AlarmPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RecyclerView(context, attrs, defStyleAttr) {
    var onSelected : ((Long) -> Unit)? = null
    val items = ArrayList<AlarmItem>()

    init {
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        adapter = PickerAdapter()
    }

    fun setType(allday: Boolean) {
        items.clear()
        if(allday) {
            val offsets = AppRes.resources.getIntArray(R.array.alarm_offset_allday)
            AppRes.resources.getStringArray(R.array.alarm_allday).forEachIndexed { index, it ->
                items.add(AlarmItem(it, offsets[index].toLong()))
            }
        }else {
            val offsets = AppRes.resources.getIntArray(R.array.alarm_offset_time)
            AppRes.resources.getStringArray(R.array.alarm_time).forEachIndexed { index, it ->
                items.add(AlarmItem(it, offsets[index].toLong()))
            }
        }
        items.add(AlarmItem(AppRes.resources.getString(R.string.custom), Long.MIN_VALUE))
        adapter?.notifyDataSetChanged()
    }

    inner class AlarmItem(val title: String, val offset: Long)

    inner class PickerAdapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {}
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