package com.hellowo.journey.calendar.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import com.hellowo.journey.R
import com.hellowo.journey.calendar.model.Template
import com.hellowo.journey.dpToPx

class TemplateControlPagerIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    val margin = dpToPx(1)
    val indiSize = dpToPx(7)
    val views = ArrayList<View>()

    init {
        orientation = HORIZONTAL
    }

    fun notify(it: List<Template>) {
        removeAllViews()
        views.clear()
        it.forEach {
            val v = LayoutInflater.from(context).inflate(R.layout.item_view_template_indi, null, false) as CardView
            v.layoutParams = LinearLayout.LayoutParams(indiSize, indiSize).apply {
                marginStart = margin
                marginEnd = margin
            }
            v.radius = (indiSize / 2).toFloat()
            v.setCardBackgroundColor(Color.TRANSPARENT)
            //v.findViewById<ImageView>(R.id.iconImg).setImageResource(TimeObject.Type.values()[it.type].iconId)
            v.findViewById<ImageView>(R.id.iconImg).setImageResource(R.drawable.circle_fill)
            v.findViewById<ImageView>(R.id.iconImg).setColorFilter(it.color)
            addView(v)
            views.add(v)
        }
    }

    fun focus(realPos: Int) {
        views.forEachIndexed { index, view ->
            if(index == realPos) {
                view.alpha = 1f
                view.scaleX = 1f
                view.scaleY = 1f
            }else {
                view.alpha = 0.3f
                view.scaleX = 1f
                view.scaleY = 1f
            }
        }
    }
}