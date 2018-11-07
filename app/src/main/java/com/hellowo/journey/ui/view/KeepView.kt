package com.hellowo.journey.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.cardview.widget.CardView
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.journey.*
import com.hellowo.journey.model.Folder

class KeepView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : CardView(context, attrs, defStyleAttr) {
    companion object {}

    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_keep, this, true)
    }

    fun show() {
        viewMode = ViewMode.ANIMATING
        val transiion = makeChangeBounceTransition()
        transiion.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                viewMode = ViewMode.OPENED
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
        })
        TransitionManager.beginDelayedTransition(this@KeepView, transiion)
        layoutParams.height = MATCH_PARENT
        requestLayout()
    }

    fun hide() {
        viewMode = ViewMode.ANIMATING
        val transiion = makeChangeBounceTransition()
        transiion.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                viewMode = ViewMode.CLOSED
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
        })
        TransitionManager.beginDelayedTransition(this, transiion)
        layoutParams.height = 0
        requestLayout()
    }
}