package com.hellowo.chating.calendar

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.TransitionManager
import com.hellowo.chating.ANIM_DUR
import com.hellowo.chating.R
import com.hellowo.chating.makeFromBottomSlideTransition
import kotlinx.android.synthetic.main.view_time_object.view.*
import java.util.*

class EditorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private val tempCal: Calendar = Calendar.getInstance()
    }

    var viewMode = ViewMode.CLOSED
    private var calendarView: CalendarView? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_time_object, this, true)
        rootLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

        titleInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) {
            }
            return@setOnEditorActionListener false
        }
    }

    fun setCalendarView(view: CalendarView) { calendarView = view }

    fun confirm() {
        val time = calendarView?.selectedCal?.timeInMillis ?: System.currentTimeMillis()
        TimeObjectManager.save(TimeObject().apply {
            title = titleInput.text.toString()
            type = 1
            dtStart = time
            dtEnd = time
            timeZone = TimeZone.getDefault().id
        })
        hide()
    }

    fun show() {
        //TransitionManager.beginDelayedTransition(this, makeFromBottomSlideTransition())
        viewMode = ViewMode.ANIMATING
        visibility = View.VISIBLE
        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(this, "translationY", height.toFloat(), 0f).setDuration(ANIM_DUR))
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                viewMode = ViewMode.OPENED
            }
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
        })
        animSet.start()
        /*
        titleInput.requestFocus()
        titleInput.postDelayed({ (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(titleInput, 0) }, 50)*/
    }

    fun hide() {
        //TransitionManager.beginDelayedTransition(this, makeFromBottomSlideTransition())
        viewMode = ViewMode.ANIMATING
        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(this, "translationY", 0f, height.toFloat()).setDuration(ANIM_DUR))
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                visibility = View.INVISIBLE
                viewMode = ViewMode.CLOSED
            }
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
        })
        animSet.start()
        titleInput.postDelayed({
           (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                   .hideSoftInputFromWindow(windowToken, 0) }, 50)
    }
}