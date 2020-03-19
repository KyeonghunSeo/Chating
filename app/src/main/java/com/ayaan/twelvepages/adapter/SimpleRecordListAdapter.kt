package com.ayaan.twelvepages.adapter

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ayaan.twelvepages.AppDateFormat
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.getDiffTodayText
import com.ayaan.twelvepages.model.AppUser
import com.ayaan.twelvepages.model.Record
import kotlinx.android.synthetic.main.list_item_simple_record.view.*
import java.util.*

class SimpleRecordListAdapter(val context: Context, val items: List<Record>, private val adapterInterface: (chatRoom: Record) -> Unit)
    : RecyclerView.Adapter<SimpleRecordListAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_simple_record, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = items[position]
        val v = holder.itemView
        v.colorBar.setCardBackgroundColor(record.getColor())
        v.titleText.text = record.getTitleInCalendar()
        v.subText.text = "${AppDateFormat.ymde.format(Date(record.dtEnd))} [${getDiffTodayText(record.dtStart)}]"
        v.setOnClickListener { adapterInterface.invoke(record) }
    }
}