package com.hellowo.chating.calendar.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellowo.chating.R
import com.hellowo.chating.calendar.model.TimeObject
import kotlinx.android.synthetic.main.list_item_normal_check.view.*

class TimeObjectDayViewAdapter(val context: Context,
                               val items: List<TimeObject>,
                               val adapterInterface: (view: View, timeObject: TimeObject) -> Unit) : RecyclerView.Adapter<TimeObjectDayViewAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_normal_check, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val timeObject = items[position]
        val v = holder.itemView

        v.titleText.text = timeObject.title

        v.setOnClickListener { adapterInterface.invoke(it, timeObject) }
    }
}