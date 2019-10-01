package com.ayaan.twelvepages.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.model.Record
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item_date_decoration.view.*

class DecorationItemsAdapter(val context: Context, val items: List<Record>,
                             val adapterInterface: (view: View, record: Record, action: Int) -> Unit)
    : RecyclerView.Adapter<DecorationItemsAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_date_decoration, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = items[position]
        val v = holder.itemView

        record.getSticker()?.let {
            Glide.with(context).load(it.resId).into(v.iconImg)
        }

        v.setOnClickListener { adapterInterface.invoke(v, record, 0) }
    }
}