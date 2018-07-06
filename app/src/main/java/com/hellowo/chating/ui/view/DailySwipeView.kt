package com.hellowo.chating.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.gesture.GestureOverlayView
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.chating.R
import com.hellowo.chating.dpToPx
import com.hellowo.chating.l
import java.util.*

class DailySwipeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        val topMargin = dpToPx(100)
        val sideMargin = dpToPx(30)
        val pagerMargin = (sideMargin * 1.5f)
        val pageCount = 200
        val viewCount = 3
    }
    private val rootLy: FrameLayout = View.inflate(context, R.layout.view_daily_swipe, null) as FrameLayout
    private val insertBtn = rootLy.findViewById<View>(R.id.insertBtn)
    private val scrollViews = arrayOf(rootLy.findViewById<SwipeNestedScrollView>(R.id.child_0),
            rootLy.findViewById(R.id.child_1),
            rootLy.findViewById(R.id.child_2))
    private val cal = Calendar.getInstance()
    private val autoScrollHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == 0) {
            }
        }
    }
    val lp = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    var isScaling = false
    var initScale = 1f
    var position = 1
    var isOpen = false
    var isEditMode = false
    var dragMode = 0

    init {
        addView(rootLy)
        insertBtn.setOnClickListener { hide(true) }
        overScrollMode = View.OVER_SCROLL_ALWAYS
        viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        initViews()
                        arrangeViews()
                    }
                })
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        scrollViews.forEach {
            val topMarginView = it.findViewById<View>(R.id.topMarginView)
            val contentsView = it.findViewById<LinearLayout>(R.id.contentsView)

            topMarginView.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, topMargin)
            lp.setMargins(sideMargin, 0, sideMargin, 0)
            contentsView.layoutParams = lp

            it.onSwipe = {
                swipe(it)
            }

            it.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
                if(scrollY <= topMargin) {
                    initScale = 1f - scrollY / topMargin.toFloat() /* 1 -> 0 */
                    val newMargin = (sideMargin / 2 + initScale * sideMargin / 2).toInt()
                    lp.setMargins(newMargin, 0, newMargin, 0)
                    scrollViews.forEach {
                        it.scrollY = scrollY
                        it.findViewById<LinearLayout>(R.id.contentsView).layoutParams = lp
                    }
                    getLeftView().let {
                        it.translationX = (-width).toFloat() + (pagerMargin * initScale)
                    }
                    getRightView().let {
                        it.translationX = width.toFloat() - (pagerMargin * initScale)
                    }
                    isScaling = false
                }else {
                    if(!isScaling) {
                        lp.setMargins(sideMargin / 2, 0, sideMargin / 2, 0)
                        it.findViewById<LinearLayout>(R.id.contentsView).layoutParams = lp
                        getLeftView().let {
                            it.translationX = (-width).toFloat()
                            it.scrollY = topMargin
                            it.findViewById<LinearLayout>(R.id.contentsView).layoutParams = lp
                        }
                        getRightView().let {
                            it.translationX = width.toFloat()
                            it.scrollY = topMargin
                            it.findViewById<LinearLayout>(R.id.contentsView).layoutParams = lp
                        }
                        isScaling = true
                    }
                }
            }


        }
    }

    private fun swipe(dx: Float) {
        scrollViews.forEach {
            it.translationX = it.translationX - dx
        }
    }

    private fun arrangeViews() {
        getLeftView().translationX = (-width).toFloat() + pagerMargin
        getMidView().translationX = 0f
        getRightView().translationX = width.toFloat() - pagerMargin
    }

    fun show(calendar: Calendar, animation: Boolean) {
        TransitionManager.beginDelayedTransition(this, makeSlideFromBottomTransition())
        isOpen = true
        scrollViews.forEach { it.visibility = View.VISIBLE }
        insertBtn.visibility = View.VISIBLE
        visibility = View.VISIBLE
    }

    fun hide(animation: Boolean) {
        val transition = makeSlideFromBottomTransition()
        transition.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                visibility = View.INVISIBLE
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
        })
        TransitionManager.beginDelayedTransition(this, transition)
        scrollViews.forEach { it.visibility = View.INVISIBLE }
        insertBtn.visibility = View.INVISIBLE
        isOpen = false
    }

    fun makeSlideFromBottomTransition(): Slide {
        val transition = Slide()
        transition.slideEdge = Gravity.BOTTOM
        transition.duration = 400
        transition.interpolator = FastOutSlowInInterpolator()
        return transition
    }

    private fun getLeftView() = scrollViews[(position + 2) % viewCount]
    private fun getMidView() = scrollViews[position]
    private fun getRightView() = scrollViews[(position + 1) % viewCount]
}