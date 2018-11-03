package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.hellowo.journey.*
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
    val visibleViews = ArrayList<View>()
    val queueViews = ArrayList<View>()
    var isCircularMode = false
    var startXoffset = 0f
    var endXoffset = 0f
    var selectedPosition = 0

    fun init(items: ArrayList<Template>, adapterInterface: (template: Template) -> Unit) {
        this.items = items
        this.adapterInterface = adapterInterface
    }

    fun notifyDataSetChanged() {
        removeAllViews()
        itemViews.clear()

        items.forEachIndexed { index, template ->
            val view = LayoutInflater.from(context).inflate(R.layout.list_item_template, null, false)
            view.layoutParams = FrameLayout.LayoutParams(itemWidth.toInt(), WRAP_CONTENT).apply {
                gravity = Gravity.LEFT
            }
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
        val visibleCnt = 1 + ((width / 2 - itemWidth / 2) / itemWidth + 1).toInt() * 2
        l("width: $width, visibleCnt : $visibleCnt")

        startXoffset = -(itemWidth * visibleCnt - width) / 2
        endXoffset = startXoffset + itemWidth * visibleCnt
        isCircularMode = itemWidth * items.size > width

        itemViews.forEachIndexed { index, view ->
            if(index < visibleCnt) {
                visibleViews.add(view)
                view.translationX = startXoffset + itemWidth * index
                l("itemWidth : $itemWidth view.translationX : ${view.translationX}")
            }else {
                queueViews.add(view)
                view.translationY = height.toFloat()
            }
        }
    }

    fun moveTarget(direction: Int, duration: Long) {
        selectedPosition += direction
        if(selectedPosition < 0) selectedPosition = items.size - 1
        else if(selectedPosition == items.size) selectedPosition = 0

        l("direction : $direction duration : $duration, selectedPosition: $selectedPosition")
        val animSet = AnimatorSet()
        val animators = ArrayList<Animator>()

        if(queueViews.size > 0) {
            if(direction < 0) {
                val view = queueViews.first()
                queueViews.remove(view)
                visibleViews.add(visibleViews.size, view)
                view.translationY = 0f
                view.translationX = endXoffset
            }else {
                val view = queueViews.last()
                queueViews.remove(view)
                visibleViews.add(0, view)
                view.translationY = 0f
                view.translationX = startXoffset - itemWidth
            }
        }

        val endX = if(direction < 0) startXoffset - itemWidth else startXoffset

        visibleViews.forEachIndexed { index, view ->
            animators.add(ObjectAnimator.ofFloat(view, "translationX", view.translationX,
                    endX + index * itemWidth))
        }

        animSet.playTogether(animators)
        animSet.duration = Math.min(duration, 250)
        //animSet.interpolator = FastOutSlowInInterpolator()

        animSet.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                if(direction < 0) {
                    val view = visibleViews.first()
                    view.translationY = height.toFloat()
                    visibleViews.remove(view)
                    queueViews.add(queueViews.size, view)
                }else {
                    val view = visibleViews.last()
                    view.translationY = height.toFloat()
                    visibleViews.remove(view)
                    queueViews.add(0, view)
                }
            }
            override fun onAnimationCancel(p0: Animator?) {
                l("캔슬 애니메이션")
            }
            override fun onAnimationStart(p0: Animator?) {}
        })

        animSet.start()
    }
}