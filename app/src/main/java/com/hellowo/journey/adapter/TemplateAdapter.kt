package com.hellowo.journey.adapter

import android.content.Context
import android.graphics.Color
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

class TemplateAdapter(val context: Context, val items: ArrayList<Template>, val adapterInterface: (template: Template) -> Unit)
    : RecyclerView.Adapter<TemplateAdapter.ViewHolder>() {
    val previewWidth = dpToPx(60f)
    val calendar = MainActivity.instance?.getCalendarView()?.targetCal ?: Calendar.getInstance()
    val shortText = context.getString(R.string.title)
    val longText = "TEXTTEXTTEXTTEXTTEXTTEXT"

    override fun getItemCount(): Int = (items.size + 1) * 100

    inner class ViewHolder(container: View) : RecyclerView.ViewHolder(container) {
        init {
            container.previewContainer.addView(TimeObjectView(context, TimeObject(), 0, 0), 0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int)
            = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_template, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val v = holder.itemView
        val pos = position % (items.size + 1)
        if(pos < items.size) {
            val item = items[pos]

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
        }else {
            v.previewContainer.getChildAt(0).visibility = View.GONE
            v.contentLy.setBackgroundColor(Color.TRANSPARENT)
            v.iconImg.setImageResource(R.drawable.sharp_add_black_48dp)
            v.titleText.text = context.getString(R.string.make_new)
        }
    }
}