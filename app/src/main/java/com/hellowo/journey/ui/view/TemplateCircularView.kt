package com.hellowo.journey.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.TextView
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
import kotlin.collections.ArrayList

class TemplateCircularView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    lateinit var items: ArrayList<Template>
    lateinit var adapterInterface: (template: Template) -> Unit
    val itemWidth = dpToPx(100f)
    val previewWidth = dpToPx(80f)
    val itemViews = ArrayList<View>()
    var isCircularMode = false
    var startXoffset = 0f

    fun init(items: ArrayList<Template>, adapterInterface: (template: Template) -> Unit) {
        this.items = items
        this.adapterInterface = adapterInterface
    }

    fun notifyDataSetChanged() {
        removeAllViews()
        itemViews.clear()

        items.forEachIndexed { index, template ->
            val view = LayoutInflater.from(context).inflate(R.layout.list_item_template, null, false)
            val container = view.findViewById<FrameLayout>(R.id.previewContainer)
            val titleText = view.findViewById<TextView>(R.id.titleText)
            val pinBtn = view.findViewById<PinView>(R.id.pinBtn)
            container.addView(TimeObjectView(context, TimeObject(), 0, 0), 0)
            val timeObjectView = container.getChildAt(0) as TimeObjectView
            timeObjectView.timeObject.type = template.type
            timeObjectView.timeObject.color = template.color
            timeObjectView.timeObject.fontColor = template.fontColor
            timeObjectView.timeObject.title = context.getString(TimeObject.Type.values()[template.type].titleId)
            timeObjectView.setLookByType()
            timeObjectView.mLeft = 0f
            timeObjectView.mRight = previewWidth
            timeObjectView.mTop = 0f
            timeObjectView.mBottom = timeObjectView.getViewHeight().toFloat()
            val lp = FrameLayout.LayoutParams(previewWidth.toInt(), timeObjectView.getViewHeight())
            timeObjectView.layoutParams = lp

            titleText.text = template.title
            //v.iconImg.setImageResource(TimeObject.Type.values()[item.type].iconId)
            pinBtn.showPinBtn = false
            pinBtn.pin(template.inCalendar)
            view.setOnClickListener { adapterInterface.invoke(template) }
            itemViews.add(view)
            addView(view)
        }
    }

    fun arrange() {
        isCircularMode = itemWidth * items.size > width
        startXoffset = width / 2 - itemWidth * items.size / 2 - itemWidth / 2

        l("width : $width startXoffset : $startXoffset")
        itemViews.forEachIndexed { index, view ->
            view.translationX = startXoffset + itemWidth * index
        }
    }
}