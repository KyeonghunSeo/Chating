package com.hellowo.chating.calendar.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProviders
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.chating.*
import com.hellowo.chating.calendar.TimeObjectManager
import com.hellowo.chating.calendar.ViewMode
import com.hellowo.chating.calendar.model.TimeObject
import com.hellowo.chating.calendar.view.CalendarView
import com.hellowo.chating.ui.activity.MainActivity
import com.hellowo.chating.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_time_object_detail.*
import java.text.SimpleDateFormat
import java.util.*


class TimeObjectDetailFragment : Fragment() {
    companion object {
        private val bottomOffset = dpToPx(5).toFloat()
    }

    lateinit var viewModel: MainViewModel
    var mType: TimeObject.Type = TimeObject.Type.EVENT
    var mStyle: TimeObject.Style = TimeObject.Style.DEFAULT
    var testEnd = 0
    var timeObject: TimeObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(MainViewModel::class.java)
        timeObject = viewModel.targetTimeObject.value
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_time_object_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //contentLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        //bottomOptionBar.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        backgroundDim.setOnClickListener { hide() }
        contentLy.setOnClickListener {  }

        titleInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
            }
            return@setOnEditorActionListener false
        }

        type1btn.setOnClickListener {
            mType = TimeObject.Type.EVENT
            mStyle = TimeObject.Style.DEFAULT
        }
        type2btn.setOnClickListener {
            mType = TimeObject.Type.EVENT
            mStyle = TimeObject.Style.SHORT
        }
        type3btn.setOnClickListener {
            mType = TimeObject.Type.TODO
            mStyle = TimeObject.Style.DEFAULT
        }
        type4btn.setOnClickListener {
            mType = TimeObject.Type.MEMO
            mStyle = TimeObject.Style.DEFAULT
        }
        deleteBtn.setOnClickListener {
            timeObject?.let { TimeObjectManager.delete(it) }
            MainActivity.instance?.viewModel?.targetTimeObject?.value = null
            hide()
        }
        confirmBtn.setOnClickListener {
            confirm()
        }

        updateUI()

        bottomOptionBar.visibility = View.GONE

        viewModel.targetView.value?.let { view ->
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            (contentLy.layoutParams as FrameLayout.LayoutParams).let {
                it.gravity = Gravity.NO_GRAVITY
                it.width = view.width
                it.height = view.height
                it.topMargin = location[1] - statusBarHeight
                it.leftMargin = location[0]
            }
            contentLy.requestLayout()
            backgroundDim.visibility = View.GONE
            contentLy.visibility = View.VISIBLE
            callAfterViewDrawed(rootLy, Runnable {  expand() })
            return
        }

        contentLy.visibility = View.GONE
        callAfterViewDrawed(rootLy, Runnable {  show() })
    }

    private fun updateUI() {
        timeObject?.let {
            titleInput.setText(it.title)
        }
    }

    fun confirm() {
        val colors = resources.getStringArray(R.array.colors)
        val time = System.currentTimeMillis()
        timeObject?.let {
            TimeObjectManager.save(it.apply {
                title = titleInput.text.toString()
                type = mType.ordinal
                style = mStyle.ordinal
                dtStart = time
                color = Color.parseColor(colors[(0 until colors.size).random()])
                dtEnd = if (mType == TimeObject.Type.MEMO) time
                else time + DAY_MILL * (title?.length ?: 0+1)
                timeZone = TimeZone.getDefault().id
            })
        }
        hide()
    }

    fun show() {
        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(backgroundDim, "alpha", 0f, 1f).setDuration(ANIM_DUR))
        animSet.start()
        TransitionManager.beginDelayedTransition(rootLy, makeFromBottomSlideTransition())
        contentLy.visibility = View.VISIBLE
        bottomOptionBar.visibility = View.VISIBLE
        statusBarBlackAlpah(activity!!)
    }

    fun expand() {
        contentLy.radius = 0f
        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(contentLy, "elevation", 0f, dpToPx(10).toFloat()).setDuration(ANIM_DUR))
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                val transiion = makeChangeBounceTransition()
                transiion.interpolator = FastOutSlowInInterpolator()
                transiion.duration = ANIM_DUR
                transiion.addListener(object : Transition.TransitionListener {
                    override fun onTransitionEnd(transition: Transition) {}
                    override fun onTransitionResume(transition: Transition) {}
                    override fun onTransitionPause(transition: Transition) {}
                    override fun onTransitionCancel(transition: Transition) {}
                    override fun onTransitionStart(transition: Transition) {}
                })
                TransitionManager.beginDelayedTransition(contentLy, transiion)
                (contentLy.layoutParams as FrameLayout.LayoutParams).let {
                    it.width = MATCH_PARENT
                    it.height = MATCH_PARENT
                    it.topMargin = 0
                    it.leftMargin = 0
                }
                contentLy.requestLayout()
            }

            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
        })
        animSet.start()
    }

    fun hide() {
        viewModel.targetView.value?.let { view ->
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val transiion = makeChangeBounceTransition()
            transiion.interpolator = FastOutSlowInInterpolator()
            transiion.duration = ANIM_DUR
            transiion.addListener(object : Transition.TransitionListener {
                override fun onTransitionEnd(transition: Transition) {

                }
                override fun onTransitionResume(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionCancel(transition: Transition) {}
                override fun onTransitionStart(transition: Transition) {}
            })
            TransitionManager.beginDelayedTransition(contentLy, transiion)
            (contentLy.layoutParams as FrameLayout.LayoutParams).let {
                it.gravity = Gravity.NO_GRAVITY
                it.width = view.width
                it.height = view.height
                it.topMargin = location[1] - statusBarHeight
                it.leftMargin = location[0]
            }
            contentLy.requestLayout()
            return
        }


        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(contentLy, "translationY", bottomOffset, rootLy.height.toFloat()).setDuration(ANIM_DUR),
                ObjectAnimator.ofFloat(bottomOptionBar, "translationY", 0f, rootLy.height.toFloat()).setDuration(ANIM_DUR),
                ObjectAnimator.ofFloat(backgroundDim, "alpha", 1f, 0f).setDuration(ANIM_DUR))
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                viewModel.targetTimeObject.value = null
                viewModel.targetView.value = null
                activity?.supportFragmentManager?.beginTransaction()?.remove(this@TimeObjectDetailFragment)?.commit()
            }
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
        })
        animSet.start()
        statusBarWhite(activity!!)
        hideKeyPad(titleInput.windowToken, titleInput)
    }
}