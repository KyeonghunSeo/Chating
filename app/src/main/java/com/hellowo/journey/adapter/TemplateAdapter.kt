package com.hellowo.journey.adapter

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import com.hellowo.journey.AppRes
import com.hellowo.journey.R
import com.hellowo.journey.dpToPx
import com.hellowo.journey.l
import com.hellowo.journey.model.Template
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.view.TimeObjectView
import kotlinx.android.synthetic.main.list_item_template.view.*
import java.util.*

class TemplateAdapter(val context: Context, val items: ArrayList<Template>, val adapterInterface: (template: Template) -> Unit)
    : RecyclerView.Adapter<TemplateAdapter.ViewHolder>() {
    val previewWidth = dpToPx(80f)
    val calendar = MainActivity.instance?.getCalendarView()?.selectedCal ?: Calendar.getInstance()

    override fun getItemCount(): Int = items.size * 10

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            container.previewContainer.addView(TimeObjectView(context, TimeObject(), 0, 0), 0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_template, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position % items.size]
        val v = holder.itemView

        val timeObjectView = v.previewContainer.getChildAt(0) as TimeObjectView
        timeObjectView.timeObject.type = item.type
        timeObjectView.timeObject.color = item.color
        timeObjectView.timeObject.fontColor = item.fontColor
        timeObjectView.timeObject.title = context.getString(TimeObject.Type.values()[item.type].titleId)
        timeObjectView.setLookByType()
        timeObjectView.mLeft = 0f
        timeObjectView.mRight = previewWidth
        timeObjectView.mTop = 0f
        timeObjectView.mBottom = timeObjectView.getViewHeight().toFloat()
        val lp = FrameLayout.LayoutParams(previewWidth.toInt(), timeObjectView.getViewHeight())
        timeObjectView.layoutParams = lp

        v.titleText.text = item.title
        //v.iconImg.setImageResource(TimeObject.Type.values()[item.type].iconId)
        v.pinBtn.showPinBtn = false
        v.pinBtn.pin(item.inCalendar)
        v.setOnClickListener { adapterInterface.invoke(item) }
    }
}