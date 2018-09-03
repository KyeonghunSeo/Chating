package com.hellowo.chating.calendar.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.chating.*
import com.hellowo.chating.calendar.ViewMode
import com.hellowo.chating.ui.activity.MainActivity
import kotlinx.android.synthetic.main.view_edit_controll.view.*

class EditControllView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr) {
    companion object {
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_edit_controll, this, true)
    }

    fun confirm() {}

    fun startControllMode() {
        deleteBtn.visibility = View.VISIBLE
        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(this@EditControllView,
                "radius", dpToPx(25).toFloat(), 0f).setDuration(ANIM_DUR))
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
        /*
        val transiion = makeChangeBounceTransition()
        transiion.interpolator = FastOutSlowInInterpolator()
        transiion.duration = ANIM_DUR
        transiion.addListener(object : androidx.transition.Transition.TransitionListener{
            override fun onTransitionEnd(transition: androidx.transition.Transition) {}
            override fun onTransitionResume(transition: androidx.transition.Transition) {}
            override fun onTransitionPause(transition: androidx.transition.Transition) {}
            override fun onTransitionCancel(transition: androidx.transition.Transition) {}
            override fun onTransitionStart(transition: androidx.transition.Transition) {

            }
        })
        TransitionManager.beginDelayedTransition(this, transiion)
        layoutParams.width = MATCH_PARENT
        layoutParams.height = dpToPx(60)
        (layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = 0
        requestLayout()
        */
    }

    fun collapse() {
        deleteBtn.visibility = View.GONE
        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(this@EditControllView,
                "radius", 0f, dpToPx(25).toFloat()).setDuration(ANIM_DUR))
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
    }
}