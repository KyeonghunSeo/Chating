package com.hellowo.chating.calendar.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.transition.TransitionManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.hellowo.chating.R
import com.hellowo.chating.calendar.model.Template
import com.hellowo.chating.calendar.model.TimeObject
import com.hellowo.chating.dpToPx
import com.hellowo.chating.l
import com.hellowo.chating.makeChangeBounceTransition
import com.hellowo.chating.ui.activity.MainActivity

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
        val v = LayoutInflater.from(context).inflate(R.layout.item_view_template_indi, null, false) as CardView
        v.layoutParams = LinearLayout.LayoutParams(indiSize, indiSize).apply {
            marginStart = margin
            marginEnd = margin
        }
        v.radius = (indiSize / 2).toFloat()
        v.setCardBackgroundColor(Color.TRANSPARENT)
        v.findViewById<ImageView>(R.id.iconImg).setImageResource(R.drawable.ic_baseline_settings_20px)
        v.findViewById<ImageView>(R.id.iconImg).setColorFilter(Color.GRAY)
        v.findViewById<ImageView>(R.id.iconImg).setPadding(0, 0, 0, 0)
        addView(v)
        views.add(v)
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