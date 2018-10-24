package com.hellowo.journey.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellowo.journey.R
import com.hellowo.journey.model.AppUser
import kotlinx.android.synthetic.main.list_item_normal_check.view.*

class BasicAdapter(val context: Context, val items: List<AppUser>, val adapterInterface: (chatRoom: AppUser) -> Unit)
    : RecyclerView.Adapter<BasicAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_normal_check, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val v = holder.itemView

        v.titleText.text = item.id

        v.setOnClickListener { adapterInterface.invoke(item) }
    }
}