package com.ayaan.twelvepages.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx
import com.ayaan.twelvepages.model.Record
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item_date_decoration.view.*

class DecorationItemsAdapter(val context: Context, val items: List<Record>,
                             val adapterInterface: (view: View, record: Record, action: Int) -> Unit)
    : RecyclerView.Adapter<DecorationItemsAdapter.ViewHolder>() {
    private val bgMargin = dpToPx(7)

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_date_decoration, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = items[position]
        val v = holder.itemView

        if(record.isSticker()) {
            v.iconImg.visibility = View.VISIBLE
            v.datebgView.visibility = View.GONE
            v.iconImg.setPadding(0, 0, 0, 0)
            v.iconImg.clearColorFilter()
            record.getSticker()?.let {
                Glide.with(context).load(it.resId).into(v.iconImg)
            }
        }else {
            v.iconImg.visibility = View.GONE
            v.datebgView.visibility = View.VISIBLE
            v.dateBgSample.setDateBg(record)
            v.iconImg.setPadding(bgMargin, bgMargin, bgMargin, bgMargin)
            v.iconImg.setImageResource(R.drawable.grey_rect_fill_radius_2)
            v.iconImg.setColorFilter(record.getColor())
        }

        v.setOnClickListener { adapterInterface.invoke(v, record, 0) }
    }
}