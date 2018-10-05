package com.hellowo.journey.calendar.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.journey.*
import com.hellowo.journey.alarm.AlarmManager
import kotlinx.android.synthetic.main.view_briefing.view.*
import java.util.*

class BriefingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr) {
    companion object

    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_briefing, this, true)
        rootLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        alarmSwitch.setOnCheckedChangeListener { compoundButton, check ->
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
            animSet.playTogether(ObjectAnimator.ofFloat(this@BriefingView,
                    "elevation", elevation, dpToPx(15).toFloat()).setDuration(ANIM_DUR))
            animSet.interpolator = FastOutSlowInInterpolator()
            animSet.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(p0: Animator?) {}
                override fun onAnimationEnd(p0: Animator?) {
                    val transiion = makeChangeBounceTransition()
                    transiion.interpolator = FastOutSlowInInterpolator()
                    transiion.duration = ANIM_DUR
                    transiion.addListener(object : Transition.TransitionListener{
                        override fun onTransitionEnd(transition: Transition) {
                            viewMode = ViewMode.OPENED
                        }
                        override fun onTransitionResume(transition: Transition) {}
                        override fun onTransitionPause(transition: Transition) {}
                        override fun onTransitionCancel(transition: Transition) {}
                        override fun onTransitionStart(transition: Transition) {}
                    })
                    TransitionManager.beginDelayedTransition(this@BriefingView, transiion)
                    layoutParams = CoordinatorLayout.LayoutParams(dpToPx(300), dpToPx(500)).apply {
                        gravity = Gravity.BOTTOM or Gravity.RIGHT
                        setMargins(0, 0, dpToPx(20), dpToPx(20))
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
        transiion.duration = ANIM_DUR
        transiion.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                val animSet = AnimatorSet()
                animSet.playTogether(ObjectAnimator.ofFloat(this@BriefingView,
                        "elevation", elevation, 0f).setDuration(ANIM_DUR))
                animSet.interpolator = FastOutSlowInInterpolator()
                animSet.addListener(object : Animator.AnimatorListener{
                    override fun onAnimationRepeat(p0: Animator?) {}
                    override fun onAnimationEnd(p0: Animator?) {
                        viewMode = ViewMode.CLOSED
                    }
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
        layoutParams = CoordinatorLayout.LayoutParams(dpToPx(50), dpToPx(50)).apply {
            gravity = Gravity.BOTTOM or Gravity.RIGHT
            setMargins(0, 0, dpToPx(10), 0)
        }
    }

    fun refreshTodayView(todayOffset: Int) {
        todayText.text = CalendarView.todayCal.get(Calendar.DATE).toString()
        val animSet = AnimatorSet()
        when {
            todayOffset > 0 -> {
                animSet.playTogether(ObjectAnimator.ofFloat(todayImg, "rotation", todayImg.rotation, 15f),
                        ObjectAnimator.ofFloat(todayText, "rotation",todayText.rotation, 15f))
            }
            todayOffset < 0 -> {
                animSet.playTogether(ObjectAnimator.ofFloat(todayImg, "rotation", todayImg.rotation, -15f),
                        ObjectAnimator.ofFloat(todayText, "rotation",todayText.rotation, -15f))
            }
            else -> {
                animSet.playTogether(ObjectAnimator.ofFloat(todayImg, "rotation", todayImg.rotation, 0f),
                        ObjectAnimator.ofFloat(todayText, "rotation",todayText.rotation, 0f))
            }
        }
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.duration = CalendarView.animDur
        animSet.start()
    }
}