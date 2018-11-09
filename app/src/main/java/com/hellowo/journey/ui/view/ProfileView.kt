package com.hellowo.journey.ui.view

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.journey.*
import kotlinx.android.synthetic.main.view_profile.view.*

class ProfileView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr) {
    companion object

    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_profile, this, true)
    }

    fun show() {
        if(viewMode == ViewMode.CLOSED) {
            viewMode = ViewMode.ANIMATING
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
                override fun onTransitionStart(transition: Transition) {
                    ObjectAnimator.ofFloat(backgroundLy, "alpha",0f, 1f).start()
                    backgroundLy.setOnClickListener {  }
                    backgroundLy.isClickable = true
                }

            })
            TransitionManager.beginDelayedTransition(this@ProfileView, transiion)
            contentLy.layoutParams = FrameLayout.LayoutParams(dpToPx(300), MATCH_PARENT)
            contentLy.requestLayout()
        }
    }

    fun hide() {
        viewMode = ViewMode.ANIMATING
        val transiion = makeChangeBounceTransition()
        transiion.interpolator = FastOutSlowInInterpolator()
        transiion.duration = ANIM_DUR
        transiion.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                viewMode = ViewMode.CLOSED
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {
                ObjectAnimator.ofFloat(backgroundLy, "alpha",1f, 0f).start()
                backgroundLy.setOnClickListener(null)
                backgroundLy.isClickable = false}
        })
        TransitionManager.beginDelayedTransition(this, transiion)
        contentLy.layoutParams = FrameLayout.LayoutParams(dpToPx(50), dpToPx(50))
        contentLy.requestLayout()
    }

    fun isOpened(): Boolean = viewMode == ViewMode.OPENED
}