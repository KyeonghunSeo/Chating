package com.hellowo.journey.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.AppRes
import com.hellowo.journey.R
import com.hellowo.journey.model.Alarm
import com.hellowo.journey.model.TimeObject
import kotlinx.android.synthetic.main.list_item_alarm.view.*
import java.util.*

class AlarmListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RecyclerView(context, attrs, defStyleAttr) {
    var onSelected : ((Alarm) -> Unit)? = null
    val items = ArrayList<Alarm>()
    private val alarms = AppRes.resources.getStringArray(R.array.alarms)

    init {
        layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        adapter = Adapter()
    }

    fun setTimeObject(timeObject: TimeObject) {
        items.clear()
        items.addAll(timeObject.alarms)
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
            v.titleText.text = getTitle(alarm.offset)

            if(alarm.dtAlarm < System.currentTimeMillis()) {
                v.iconImg.setColorFilter(AppRes.secondaryText)
                v.titleText.setTextColor(AppRes.secondaryText)
                v.subText.setTextColor(AppRes.secondaryText)
                v.subText.text = context.getString(R.string.passed_alarm)
            }else {
                v.iconImg.setColorFilter(AppRes.primaryText)
                v.titleText.setTextColor(AppRes.primaryText)
                v.subText.setTextColor(AppRes.primaryText)
                v.subText.text = String.format(context.getString(R.string.ring_alarm_at_time),
                        AppRes.dateTime.format(Date(alarm.dtAlarm)))
            }

            v.setOnClickListener {
                notifyItemChanged(position)
                onSelected?.invoke(alarm)
            }
        }
    }

    fun getTitle(offset: Long) : String = when(offset) {
        0L -> alarms[0]
        1000L * 60 * 60 * 9 -> alarms[1]
        1000L * 60 * 60 * 12 -> alarms[2]
        1000L * 60 * 60 * 18 -> alarms[3]
        -1000L * 60 * 10 -> alarms[4]
        -1000L * 60 * 30 -> alarms[5]
        -1000L * 60 * 60 -> alarms[6]
        -1000L * 60 * 120 -> alarms[7]
        -1000L * 60 * 60 * 24 -> alarms[8]
        -1000L * 60 * 60 * 24 * 2 -> alarms[9]
        -1000L * 60 * 60 * 24 * 7 -> alarms[10]
        else -> context.getString(R.string.custom)
    }
}