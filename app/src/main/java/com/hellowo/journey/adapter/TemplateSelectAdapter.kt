package com.hellowo.journey.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.R
import com.hellowo.journey.dpToPx
import com.hellowo.journey.model.Template
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.view.TimeObjectView
import kotlinx.android.synthetic.main.list_item_template.view.*
import java.util.*

class TemplateSelectAdapter(val context: Context, val items: ArrayList<Template>, val adapterInterface: (template: Template) -> Unit)
    : RecyclerView.Adapter<TemplateSelectAdapter.ViewHolder>() {
    val previewWidth = dpToPx(60f)
    val calendar = MainActivity.instance?.getCalendarView()?.targetCal ?: Calendar.getInstance()
    val shortText = context.getString(R.string.title)
    val longText = "TEXTTEXTTEXTTEXTTEXTTEXT"

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            container.previewContainer.addView(TimeObjectView(context, TimeObject(), 0, 0), 0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_template_select, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val v = holder.itemView
        val item = items[position]

        v.contentLy.setBackgroundResource(R.drawable.white_rect_fill_radius_1)

        val timeObjectView = v.previewContainer.getChildAt(0) as TimeObjectView
        timeObjectView.visibility = View.VISIBLE

        when(TimeObject.Type.values()[item.type]) {
            TimeObject.Type.TASK -> {
                timeObjectView.timeObject.title = shortText
                timeObjectView.scaleX = 1f
                timeObjectView.scaleY = 1f
            }
            TimeObject.Type.NOTE -> {
                timeObjectView.timeObject.title = longText
                timeObjectView.scaleX = 1f
                timeObjectView.scaleY = 1f
            }
            else -> {
                timeObjectView.timeObject.title = shortText
                timeObjectView.scaleX = 1f
                timeObjectView.scaleY = 1f
            }
        }

        timeObjectView.timeObject.type = item.type
        timeObjectView.timeObject.colorKey = item.colorKey
        timeObjectView.timeObject.fontColor = item.fontColor
        timeObjectView.setLookByType()
        timeObjectView.mLeft = 0f
        timeObjectView.mRight = previewWidth
        timeObjectView.mTop = 0f
        timeObjectView.mBottom = timeObjectView.getViewHeight().toFloat()
        val lp = FrameLayout.LayoutParams(previewWidth.toInt(), timeObjectView.getViewHeight())
        lp.gravity = Gravity.CENTER
        timeObjectView.layoutParams = lp

        v.iconImg.setImageBitmap(null)
        v.titleText.text = item.title
        //v.iconImg.setImageResource(TimeObject.Type.values()[item.type].iconId)
        v.setOnClickListener { adapterInterface.invoke(item) }
    }
}