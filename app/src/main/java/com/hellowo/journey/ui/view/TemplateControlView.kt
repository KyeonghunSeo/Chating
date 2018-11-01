package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout.HORIZONTAL
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.journey.*
import com.hellowo.journey.adapter.TemplateAdapter
import com.hellowo.journey.adapter.util.SpacesItemDecoration
import com.hellowo.journey.listener.MainDragAndDropListener
import com.hellowo.journey.model.Template
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.view.circularlayoutmanager.CenterScrollListener
import com.hellowo.journey.ui.view.circularlayoutmanager.CircleLayoutManager
import com.hellowo.journey.ui.view.circularlayoutmanager.ScrollZoomLayoutManager
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.view_template_control.view.*

class TemplateControlView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val itemHeight = dpToPx(50)
    private val itemWidth = dpToPx(150)
    private val collapseSize = dpToPx(36)
    private val bottomMargin = dpToPx(8)
    val items = ArrayList<Template>()
    var selectedPosition = 0
    var clickFlag = false
    var isExpanded = false
    private val addBtnHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            expand()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_template_control, this, true)
        controllView.radius = collapseSize / 2f
        recyclerView.layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        recyclerView.addOnScrollListener(CenterScrollListener())
        recyclerView.adapter = TemplateAdapter(context, items) {
            Prefs.putInt("last_template_id", it.id)
            setControlView(it)
            MainActivity.instance?.viewModel?.targetTemplate?.value = it
            MainActivity.instance?.viewModel?.makeNewTimeObject()
            postDelayed({collapse(false)}, 0)
        }
/*

        touchEventView.setOnLongClickListener { view ->
            val data = ClipData.newPlainText("dragType", "main")
            val shadowBuilder = MainDragAndDropListener.BlankDragShadowBuilder(view)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.startDragAndDrop(data, shadowBuilder, null, 0)
            } else {
                view.startDrag(data, shadowBuilder, null, 0)
            }
        }

        listLy.setOnDragListener { view, dragEvent ->
            when(dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    if(dragEvent.x > view.width / 2) {
                        recyclerView.smoothScrollToPosition(++selectedPosition)
                    }else {
                        recyclerView.smoothScrollToPosition(--selectedPosition)
                    }
                }
                DragEvent.ACTION_DROP -> {
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                }
            }
            return@setOnDragListener true
        }
*/
        touchEventView.setOnTouchListener { view, motionEvent ->
            when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if(!isExpanded) {
                        addBtnHandler.sendEmptyMessageDelayed(0, 200)
                        clickFlag = true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    addBtnHandler.removeMessages(0)
                    if(motionEvent.x > 0 && motionEvent.x < width && motionEvent.y > 0 && motionEvent.y < height){
                        if(clickFlag) {
                            if(!isExpanded) MainActivity.instance?.viewModel?.makeNewTimeObject()
                        }else {
                            collapse(false)
                        }
                    }
                    clickFlag = false
                }
                MotionEvent.ACTION_CANCEL -> clickFlag = false
            }
            return@setOnTouchListener false
        }
        touchEventView.setOnClickListener{}
        callAfterViewDrawed(this, Runnable{})
    }

    private fun expand() {
        vibrate(context)
        listLy.visibility = View.VISIBLE
        controllView.elevation = dpToPx(5f)

        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 45f),
                ObjectAnimator.ofFloat(listLy, "translationY", height.toFloat(), 0f))
        animSet.duration = ANIM_DUR
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()

        isExpanded = true
    }

    fun collapse(withDimOff: Boolean) {
        controllView.elevation = 0f

        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 0f),
                ObjectAnimator.ofFloat(listLy, "translationY", 0f, height.toFloat()))
        animSet.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) { listLy.visibility = View.INVISIBLE }
            override fun onAnimationCancel(p0: Animator?) { }
            override fun onAnimationStart(p0: Animator?) {}
        })
        animSet.duration = ANIM_DUR
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
        isExpanded = false
    }

    private fun readyExpand() {
        val transiion = makeChangeBounceTransition()
        TransitionManager.beginDelayedTransition(this@TemplateControlView, transiion)

        (controllView.layoutParams as FrameLayout.LayoutParams).let {
            it.width = WRAP_CONTENT
            it.bottomMargin = collapseSize * 2
        }
        controllView.elevation = dpToPx(5f)

        controllView.requestLayout()
    }

    fun notify(it: List<Template>) {
        items.clear()
        items.addAll(it)
        recyclerView.adapter?.notifyDataSetChanged()
        selectedPosition = items.size * 5
        recyclerView.scrollToPosition(selectedPosition)
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