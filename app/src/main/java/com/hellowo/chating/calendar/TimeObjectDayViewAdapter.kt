package com.hellowo.chating.calendar

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellowo.chating.R
import com.hellowo.chating.model.ChatRoom
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.list_item_normal_check.view.*

class TimeObjectDayViewAdapter(val context: Context,
                               val items: List<TimeObject>,
                               val adapterInterface: (chatRoom: TimeObject) -> Unit) : RecyclerView.Adapter<TimeObjectDayViewAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_normal_check, parent, false))

    override fun onBindViewHolder(holder: TimeObjectDayViewAdapter.ViewHolder, position: Int) {
        val timeObject = items[position]
        val v = holder.itemView

        v.titleText.text = timeObject.title

        v.setOnClickListener { adapterInterface.invoke(timeObject) }
    }
}