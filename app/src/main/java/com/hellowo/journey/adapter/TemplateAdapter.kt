package com.hellowo.journey.adapter

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellowo.journey.AppRes
import com.hellowo.journey.R
import com.hellowo.journey.l
import com.hellowo.journey.model.Template
import com.hellowo.journey.model.TimeObject
import kotlinx.android.synthetic.main.list_item_template.view.*

class TemplateAdapter(val context: Context, val items: ArrayList<Template>, val adapterInterface: (template: Template) -> Unit)
    : RecyclerView.Adapter<TemplateAdapter.ViewHolder>() {

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_template, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val v = holder.itemView

        v.titleText.text = item.title
        v.iconImg.setImageResource(TimeObject.Type.values()[item.type].iconId)
        v.iconImg.setColorFilter(item.fontColor)
        v.iconImg.setBackgroundColor(item.color)
        v.pinBtn.showPinBtn = false
        v.pinBtn.pin(item.inCalendar)
        v.setOnClickListener { adapterInterface.invoke(item) }
    }
}