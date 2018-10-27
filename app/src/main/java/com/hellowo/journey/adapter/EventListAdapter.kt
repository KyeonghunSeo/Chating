package com.hellowo.journey.adapter

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellowo.journey.*
import com.hellowo.journey.alarm.AlarmManager
import com.hellowo.journey.model.CalendarSkin
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.repeat.RepeatManager
import kotlinx.android.synthetic.main.list_item_event.view.*
import java.util.*

class EventListAdapter(val context: Context, val items: List<TimeObject>, val currentCal: Calendar,
                       val adapterInterface: (view: View, timeObject: TimeObject, action: Int) -> Unit)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val tempCal = Calendar.getInstance()

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_event, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val timeObject = items[position]
        val v = holder.itemView

        v.titleText.text = if(timeObject.title.isNullOrBlank()) {
            context.getString(R.string.untitle)
        }else {
            timeObject.title
        }

        if(timeObject.repeat.isNullOrBlank()) {
            v.repeatText.visibility = View.GONE
        }else {
            v.repeatText.visibility = View.VISIBLE
            v.repeatText.text = RepeatManager.makeRepeatText(timeObject)
        }

        if(timeObject.location.isNullOrBlank()) {
            v.locationText.visibility = View.GONE
        }else {
            v.locationText.visibility = View.VISIBLE
            v.locationText.text = timeObject.location
        }

        if(timeObject.description.isNullOrBlank()) {
            v.memoText.visibility = View.GONE
        }else {
            v.memoText.visibility = View.VISIBLE
            v.memoText.text = timeObject.description
        }

        if(timeObject.alarms.any{ it.dtAlarm >= System.currentTimeMillis() }) {
            v.alarmIndi.visibility = View.VISIBLE
        }else {
            v.alarmIndi.visibility = View.GONE
        }

        v.dotImg.setColorFilter(timeObject.color)

        if(timeObject.allday || timeObject.dtStart < getCalendarTime0(currentCal)) {
            v.dotImg.setImageResource(R.drawable.circle_fill)
            tempCal.timeInMillis = timeObject.dtStart
            val totalDate = getDiffDate(timeObject.dtStart, timeObject.dtEnd) + 1
            val toDateNum = getDiffDate(tempCal, currentCal)
            if(totalDate > 1) {
                v.timeText.visibility = View.VISIBLE
                v.timeText.text = String.format(context.getString(R.string.date_of_total), "${toDateNum + 1}/$totalDate")
            }else {
                v.timeText.visibility = View.GONE
            }
        }else {
            v.dotImg.setImageResource(R.drawable.circle_stroke_1dp)
            v.timeText.visibility = View.VISIBLE
            v.timeText.text = "${AppRes.time.format(Date(timeObject.dtStart))} ~ ${AppRes.time.format(Date(timeObject.dtEnd))}"
        }

        v.setOnClickListener { adapterInterface.invoke(it, timeObject, 0) }
    }
}