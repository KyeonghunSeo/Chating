package com.hellowo.journey.ui.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
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
    private val itemHeight = dpToPx(50)
    private val itemWidth = dpToPx(150)
    private val collapseSize = dpToPx(36)
    private val bottomMargin = dpToPx(8)
    val items = ArrayList<Template>()
    var isExpanded = false
    private val addBtnHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            readyExpand()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_template_control, this, true)
        controllView.radius = collapseSize / 2f
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = TemplateAdapter(context, items) {
            Prefs.putInt("last_template_id", it.id)
            setControlView(it)
            MainActivity.instance?.viewModel?.targetTemplate?.value = it
            MainActivity.instance?.viewModel?.makeNewTimeObject()
            postDelayed({collapse(false)}, 0)
        }
        recyclerView.visibility = View.GONE

        touchEventView.setOnTouchListener { view, motionEvent ->
            if(!isExpanded) {
                when(motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        addBtnHandler.sendEmptyMessageDelayed(0, 50)
                    }
                    MotionEvent.ACTION_UP -> {
                        if(motionEvent.x > 0 && motionEvent.x < width &&
                                motionEvent.y > -itemHeight && motionEvent.y < height){
                            addBtnHandler.removeMessages(0)
                            MainActivity.instance?.viewModel?.makeNewTimeObject()
                        }
                        postDelayed({collapse(false)}, 0)
                    }
                }
            }
            return@setOnTouchListener false
        }

        touchEventView.setOnLongClickListener {
            expand()
            return@setOnLongClickListener true
        }

        callAfterViewDrawed(this, Runnable{})
    }

    private fun expand() {
        val h = items.size * itemHeight + dpToPx(10)

        val transiion = makeChangeBounceTransition()
        transiion.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {
                val animSet = AnimatorSet()
                animSet.playTogether(ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 45f))
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
            it.width = itemWidth
            it.height = h
            it.bottomMargin = bottomMargin * 7
        }
        itemView.elevation = dpToPx(5f)

        recyclerView.visibility = View.VISIBLE
        recyclerView.adapter?.notifyDataSetChanged()

        controllView.requestLayout()
        MainActivity.instance?.onDimDark(true, true)

        touchEventView.setOnClickListener { collapse(true) }

        isExpanded = true
    }

    private fun readyExpand() {
        val transiion = makeChangeBounceTransition()
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
                animSet.playTogether(ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 0f))
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

        touchEventView.setOnClickListener (null)

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