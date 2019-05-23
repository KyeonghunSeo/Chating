package com.ayaan.twelvepages.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.alarm.AlarmManager
import kotlinx.android.synthetic.main.view_briefing.view.*

class BriefingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_briefing, this, true)
        alarmSwitch.setOnCheckedChangeListener { _, check ->
            if(check) {
                AlarmManager.unRegistBrifingAlarm()
            }
        }
    }

    fun confirm() {}

    fun show() {
        if(viewMode == ViewMode.CLOSED) {
            viewMode = ViewMode.ANIMATING
            val animSet = AnimatorSet()
            animSet.playTogether(
                    ObjectAnimator.ofFloat(this@BriefingView, "elevation", elevation, dpToPx(10f)))
            animSet.duration = 200
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(p0: Animator?) {}
                override fun onAnimationEnd(p0: Animator?) {
                    val transiion = makeChangeBounceTransition()
                    transiion.interpolator = FastOutSlowInInterpolator()
                    transiion.duration = 200
                    transiion.addListener(object : Transition.TransitionListener{
                        override fun onTransitionEnd(transition: Transition) { viewMode = ViewMode.OPENED }
                        override fun onTransitionResume(transition: Transition) {}
                        override fun onTransitionPause(transition: Transition) {}
                        override fun onTransitionCancel(transition: Transition) {}
                        override fun onTransitionStart(transition: Transition) {}
                    })
                    TransitionManager.beginDelayedTransition(this@BriefingView, transiion)
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
                        setMargins(0, 0, 0, 0)
                    }
                }
                override fun onAnimationCancel(p0: Animator?) {}
                override fun onAnimationStart(p0: Animator?) {}
            })
            animSet.start()
        }
    }

    fun hide() {
        viewMode = ViewMode.ANIMATING
        val transiion = makeChangeBounceTransition()
        transiion.interpolator = FastOutSlowInInterpolator()
        transiion.duration = 200
        transiion.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                val animSet = AnimatorSet()
                animSet.playTogether(ObjectAnimator.ofFloat(
                        this@BriefingView, "elevation", elevation, dpToPx(1f)))
                animSet.duration = 200
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.addListener(object : Animator.AnimatorListener{
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationEnd(p0: Animator?) { viewMode = ViewMode.CLOSED }
                    override fun onAnimationCancel(p0: Animator?) {}
                    override fun onAnimationStart(p0: Animator?) {}
                })
                animSet.start()
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
        })
        TransitionManager.beginDelayedTransition(this, transiion)
        layoutParams = FrameLayout.LayoutParams(dpToPx(80), dpToPx(30)).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            setMargins(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
        }
    }
}