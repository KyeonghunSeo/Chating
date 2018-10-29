package com.hellowo.journey.ui.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.journey.*
import com.hellowo.journey.adapter.TemplateAdapter
import com.hellowo.journey.adapter.util.SpacesItemDecoration
import com.hellowo.journey.model.Template
import com.hellowo.journey.ui.activity.MainActivity
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.view_template_control.view.*

class TemplateControlView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val itemHeight = dpToPx(60)
    private val collapseSize = dpToPx(36)
    private val bottomMargin = dpToPx(8)
    val items = ArrayList<Template>()
    var isExpanded = false

    init {
        LayoutInflater.from(context).inflate(R.layout.view_template_control, this, true)
        itemView.radius = collapseSize / 2f
        controllView.radius = collapseSize / 2f
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = TemplateAdapter(context, items) {
            Prefs.putInt("last_template_id", it.id)
            setControlView(it)
            MainActivity.instance?.viewModel?.targetTemplate?.value = it
            MainActivity.instance?.viewModel?.makeNewTimeObject()
            collapse(false)
        }
        recyclerView.addItemDecoration(SpacesItemDecoration(dpToPx(5)))
        recyclerView.visibility = View.GONE

        setOnTouchListener { view, motionEvent ->
            if(!isExpanded) {
                when(motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        readyExpand()
                    }
                    MotionEvent.ACTION_UP -> {
                        collapse(false)
                    }
                }
            }
            return@setOnTouchListener false
        }

        setOnLongClickListener {
            expand()
            return@setOnLongClickListener true
        }

        setOnClickListener {
            if(isExpanded) {
                collapse(true)
            }else {
                MainActivity.instance?.viewModel?.makeNewTimeObject()
            }
        }

        callAfterViewDrawed(this, Runnable{})
    }

    private fun expand() {
        val h = items.size * itemHeight + dpToPx(10)

        val transiion = makeChangeBounceTransition()
        transiion.interpolator = FastOutSlowInInterpolator()
        transiion.duration = ANIM_DUR
        transiion.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                TransitionManager.beginDelayedTransition(itemView, makeFromBottomSlideTransition())
                recyclerView.visibility = View.VISIBLE
                recyclerView.adapter?.notifyDataSetChanged()
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {
                val animSet = AnimatorSet()
                animSet.playTogether(ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 45f),
                        ObjectAnimator.ofFloat(itemView, "radius", itemView.radius, dpToPx(1f)))
                animSet.duration = ANIM_DUR
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.start()
            }
        })
        TransitionManager.beginDelayedTransition(this@TemplateControlView, transiion)

        (controllView.layoutParams as FrameLayout.LayoutParams).let {
            it.width = collapseSize
            it.bottomMargin = bottomMargin
        }

        (itemView.layoutParams as FrameLayout.LayoutParams).let {
            it.width = collapseSize * 6
            it.height = h
            it.bottomMargin = bottomMargin * 7
        }
        itemView.elevation = dpToPx(5f)

        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(templateIconImg, "rotation", 0f, 45f),
                ObjectAnimator.ofFloat(itemView, "radius", itemView.radius, dpToPx(1f)))
        animSet.duration = ANIM_DUR
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()

        controllView.requestLayout()
        MainActivity.instance?.onDimDark(true, true)
        isExpanded = true
    }

    private fun readyExpand() {
        val transiion = makeChangeBounceTransition()
        transiion.interpolator = FastOutSlowInInterpolator()
        transiion.duration = ANIM_DUR
        TransitionManager.beginDelayedTransition(this@TemplateControlView, transiion)

        (controllView.layoutParams as FrameLayout.LayoutParams).let {
            it.width = WRAP_CONTENT
            it.bottomMargin = collapseSize * 2
        }
        controllView.elevation = dpToPx(5f)

        (itemView.layoutParams as FrameLayout.LayoutParams).let {
            it.bottomMargin = collapseSize * 2
        }

        controllView.requestLayout()
    }

    fun collapse(withDimOff: Boolean) {
        val transiion = makeChangeBounceTransition()
        transiion.interpolator = FastOutSlowInInterpolator()
        transiion.duration = ANIM_DUR
        transiion.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                controllView.elevation = 0f
                itemView.elevation = 0f
                recyclerView.visibility = View.GONE
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {
                val animSet = AnimatorSet()
                animSet.playTogether(ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 0f),
                        ObjectAnimator.ofFloat(itemView, "radius", itemView.radius, collapseSize / 2f))
                animSet.duration = ANIM_DUR
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.start()
            }

        })
        TransitionManager.beginDelayedTransition(this@TemplateControlView, transiion)

        (controllView.layoutParams as FrameLayout.LayoutParams).let {
            it.width = collapseSize
            it.bottomMargin = bottomMargin
        }

        (itemView.layoutParams as FrameLayout.LayoutParams).let {
            it.width = collapseSize
            it.height = collapseSize
            it.bottomMargin = bottomMargin
        }

        itemView.requestLayout()
        if(withDimOff) MainActivity.instance?.offDimDark(true, false)
        isExpanded = false
    }

    fun notify(it: List<Template>) {
        items.clear()
        items.addAll(it)
        recyclerView.adapter?.notifyDataSetChanged()
        it.firstOrNull { it.id == Prefs.getInt("last_template_id", 0) }?.let {
            setControlView(it)
        }
    }

    private fun setControlView(template: Template) {
        controllBackground.setBackgroundColor(template.color)
        templateIconImg.setColorFilter(template.fontColor)
        templateNameText.text = template.title
    }
}