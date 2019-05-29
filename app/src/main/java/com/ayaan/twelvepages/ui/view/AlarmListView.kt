package com.ayaan.twelvepages.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ayaan.twelvepages.AppDateFormat
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.alarm.AlarmManager
import com.ayaan.twelvepages.model.Alarm
import com.ayaan.twelvepages.model.Record
import kotlinx.android.synthetic.main.list_item_alarm.view.*
import java.util.*

class AlarmListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {
    var onSelected : ((Alarm) -> Unit)? = null
    val items = ArrayList<Alarm>()

    init {
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        adapter = Adapter()
    }

    fun setTimeObject(record: Record) {
        items.clear()
        items.addAll(record.alarms)
        adapter?.notifyDataSetChanged()
    }

    inner class Adapter : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = items.size

        inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
            init {
                (container.layoutParams as RecyclerView.LayoutParams).let {

                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, position: Int)
                = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_alarm, parent, false))

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val alarm = items[position]
            val v = holder.itemView
            v.titleText.text = AlarmManager.getTimeObjectAlarmText(alarm)

            if(alarm.dtAlarm < System.currentTimeMillis()) {
                v.iconImg.setColorFilter(AppTheme.secondaryText)
                v.titleText.setTextColor(AppTheme.secondaryText)
                v.subText.setTextColor(AppTheme.secondaryText)
                v.subText.text = context.getString(R.string.passed_alarm)
            }else {
                v.iconImg.setColorFilter(AppTheme.primaryText)
                v.titleText.setTextColor(AppTheme.primaryText)
                v.subText.setTextColor(AppTheme.primaryText)
                v.subText.text = String.format(context.getString(R.string.ring_alarm_at_time),
                        AppDateFormat.dateTime.format(Date(alarm.dtAlarm)))
            }

            v.setOnClickListener {
                onSelected?.invoke(alarm)
            }
        }
    }
}