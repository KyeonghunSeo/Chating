package com.hellowo.chating.calendar.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.hellowo.chating.*
import com.hellowo.chating.calendar.model.TimeObject
import com.hellowo.chating.calendar.TimeObjectManager
import com.hellowo.chating.calendar.ViewMode
import com.hellowo.chating.ui.activity.MainActivity
import kotlinx.android.synthetic.main.view_timeobject_detail.view.*
import java.util.*

class TimeObjectDetailView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private val bottomOffset = dpToPx(5).toFloat()
    }

    private var calendarView: CalendarView? = null
    var viewMode = ViewMode.CLOSED
    var mType: TimeObject.Type = TimeObject.Type.EVENT
    var mStyle: TimeObject.Style = TimeObject.Style.DEFAULT
    var testEnd = 0
    var timeObject: TimeObject? = null
    val colors = context.resources.getStringArray(R.array.colors)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_timeobject_detail, this, true)
        //contentLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        //bottomOptionBar.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        backgroundDim.setOnClickListener { if(viewMode == ViewMode.OPENED) { hide() } }
        backgroundDim.visibility = View.INVISIBLE
        backgroundDim.alpha = 0f
        contentLy.setOnClickListener {  }
        contentLy.visibility = View.INVISIBLE

        titleInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) {
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
    }

    fun setCalendarView(view: CalendarView) { calendarView = view }

    fun setData(timeObject: TimeObject) {
        this.timeObject = timeObject
        updateUI()
    }

    private fun updateUI() {
        timeObject?.let {
            titleInput.setText(it.title)
        }
    }

    fun confirm() {
        val time = calendarView?.selectedCal?.timeInMillis ?: System.currentTimeMillis()
        timeObject?.let {
            TimeObjectManager.save(it.apply {
                title = titleInput.text.toString()
                type = mType.ordinal
                style = mStyle.ordinal
                dtStart = time
                color = Color.parseColor(colors[(0 until colors.size).random()])
                if (mType == TimeObject.Type.MEMO) dtEnd = time
                else dtEnd = time + DAY_MILL * (title?.length ?: 0+1)
                timeZone = TimeZone.getDefault().id
            })
        }
        hide()
    }

    fun show(timeObject: TimeObject) {
        viewMode = ViewMode.ANIMATING
        contentLy.visibility = View.VISIBLE
        bottomOptionBar.visibility = View.VISIBLE
        backgroundDim.visibility = View.VISIBLE
        setData(timeObject)
        val animSet = AnimatorSet()
        if(timeObject.isManaged) {
            contentLy.radius = 0f
            contentLy.layoutParams.height = MATCH_PARENT
            contentLy.requestLayout()
        }else {
            contentLy.radius = bottomOffset
            contentLy.layoutParams.height = dpToPx(300)
            contentLy.requestLayout()
        }
        animSet.playTogether(ObjectAnimator.ofFloat(contentLy, "translationY", height.toFloat(), bottomOffset).setDuration(ANIM_DUR),
                ObjectAnimator.ofFloat(bottomOptionBar, "translationY", height.toFloat(), 0f).setDuration(ANIM_DUR),
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = MainActivity.instance?.window!!
            var flags = window.peekDecorView().systemUiVisibility
            flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            window.peekDecorView().systemUiVisibility = flags
            window.statusBarColor = ContextCompat.getColor(context, R.color.blackAlpha)
        }
    }

    fun hide() {
        viewMode = ViewMode.ANIMATING
        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(contentLy, "translationY", bottomOffset, height.toFloat()).setDuration(ANIM_DUR),
                ObjectAnimator.ofFloat(bottomOptionBar, "translationY", 0f, height.toFloat()).setDuration(ANIM_DUR),
                ObjectAnimator.ofFloat(backgroundDim, "alpha", 1f, 0f).setDuration(ANIM_DUR))
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                contentLy.visibility = View.INVISIBLE
                bottomOptionBar.visibility = View.INVISIBLE
                backgroundDim.visibility = View.INVISIBLE
                viewMode = ViewMode.CLOSED

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val window = MainActivity.instance?.window!!
                    var flags = window.peekDecorView().systemUiVisibility
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    window.peekDecorView().systemUiVisibility = flags
                    window.statusBarColor = ContextCompat.getColor(context, R.color.white)
                }
            }
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
        })
        animSet.start()

        hideKeyPad(windowToken, titleInput)
    }
}