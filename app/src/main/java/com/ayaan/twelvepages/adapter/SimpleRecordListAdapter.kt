package com.ayaan.twelvepages.adapter

import android.annotation.SuppressLint
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.model.AppUser
import com.ayaan.twelvepages.model.Record
import kotlinx.android.synthetic.main.list_item_simple_record.view.*
import java.util.*

class SimpleRecordListAdapter(val context: Context, val items: List<Record>, private val adapterInterface: (chatRoom: Record) -> Unit)
    : RecyclerView.Adapter<SimpleRecordListAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            setGlobalTheme(container)
            container.checkBtn.visibility = View.GONE
            container.countdownText.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_simple_record, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = items[position]
        val v = holder.itemView
        v.checkBox.setColorFilter(record.getColor())
        v.titleText.text = record.getTitleInCalendar()
        v.memoText.text = "${AppDateFormat.ymde.format(Date(record.dtEnd))} [${getDiffTodayText(record.dtStart)}]"
        v.setSafeOnClickListener { adapterInterface.invoke(record) }
    }
}