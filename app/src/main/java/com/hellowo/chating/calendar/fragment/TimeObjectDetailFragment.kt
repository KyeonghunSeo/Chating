package com.hellowo.chating.calendar.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProviders
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
    var viewMode = ViewMode.CLOSED
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
        backgroundDim.setOnClickListener { if(viewMode == ViewMode.OPENED) { hide() } }
        backgroundDim.visibility = View.INVISIBLE
        backgroundDim.alpha = 0f
        contentLy.setOnClickListener {  }
        contentLy.visibility = View.INVISIBLE

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
        show()
    }

    fun setData() {
        viewModel.targetTimeObject.value?.let { updateUI() }
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
        viewMode = ViewMode.ANIMATING
        contentLy.visibility = View.VISIBLE
        bottomOptionBar.visibility = View.VISIBLE
        backgroundDim.visibility = View.VISIBLE
        setData()
        val animSet = AnimatorSet()
        if(timeObject!!.isManaged) {
            contentLy.radius = 0f
            contentLy.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            contentLy.requestLayout()
        }else {
            contentLy.radius = bottomOffset
            contentLy.layoutParams.height = dpToPx(300)
            contentLy.requestLayout()
        }
        animSet.playTogether(ObjectAnimator.ofFloat(contentLy, "translationY", rootLy.height.toFloat(), bottomOffset).setDuration(ANIM_DUR),
                ObjectAnimator.ofFloat(bottomOptionBar, "translationY", rootLy.height.toFloat(), 0f).setDuration(ANIM_DUR),
                ObjectAnimator.ofFloat(backgroundDim, "alpha", 0f, 1f).setDuration(ANIM_DUR))
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
    }

    fun hide() {
        viewMode = ViewMode.ANIMATING
        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(contentLy, "translationY", bottomOffset, rootLy.height.toFloat()).setDuration(ANIM_DUR),
                ObjectAnimator.ofFloat(bottomOptionBar, "translationY", 0f, rootLy.height.toFloat()).setDuration(ANIM_DUR),
                ObjectAnimator.ofFloat(backgroundDim, "alpha", 1f, 0f).setDuration(ANIM_DUR))
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                contentLy.visibility = View.INVISIBLE
                bottomOptionBar.visibility = View.INVISIBLE
                backgroundDim.visibility = View.INVISIBLE
                viewMode = ViewMode.CLOSED
                activity?.supportFragmentManager?.beginTransaction()?.remove(this@TimeObjectDetailFragment)?.commit()
            }
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
        })
        animSet.start()

        hideKeyPad(titleInput.windowToken, titleInput)
    }
}