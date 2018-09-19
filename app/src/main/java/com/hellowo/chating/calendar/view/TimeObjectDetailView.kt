package com.hellowo.chating.calendar.view

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.hellowo.chating.*
import com.hellowo.chating.calendar.TimeObjectManager
import com.hellowo.chating.calendar.ViewMode
import com.hellowo.chating.calendar.dialog.ColorPickerDialog
import com.hellowo.chating.calendar.dialog.DateTimePickerDialog
import com.hellowo.chating.calendar.dialog.TypePickerDialog
import com.hellowo.chating.calendar.model.Template
import com.hellowo.chating.calendar.model.TimeObject
import com.hellowo.chating.ui.activity.MainActivity
import kotlinx.android.synthetic.main.view_timeobject_detail.view.*
import java.util.*


class TimeObjectDetailView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
    }

    private var originalData: TimeObject? = null
    private var timeObject: TimeObject? = null
    private val colors = context.resources.getStringArray(R.array.colors)
    private val insertModeHeight = dpToPx(250)
    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_timeobject_detail, this, true)
        contentLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        styleEditLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        contentPanel.visibility = View.INVISIBLE
        contentLy.setOnClickListener {}

        confirmBtn.setOnClickListener {
            confirm()
            MainActivity.instance?.viewModel?.clearTargetTimeObject()
        }

        deleteBtn.setOnClickListener {
            timeObject?.let { TimeObjectManager.delete(it) }
            MainActivity.instance?.viewModel?.clearTargetTimeObject()
        }

        initControllBtn()
        initType()
        initTitle()
        initDateTime()
    }

    private fun initControllBtn() {
        colorBtn.setOnClickListener {
            TransitionManager.beginDelayedTransition(this, makeFromBottomSlideTransition())
            colorPicker.show()
        }
    }

    private fun initType() {

    }

    private fun initTitle() {
        titleInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == IME_ACTION_DONE) {
            }
            return@setOnEditorActionListener false
        }
        titleInput.isFocusableInTouchMode = false
        titleInput.clearFocus()
        titleInput.setOnClickListener { showTitleKeyPad() }
    }

    private fun initDateTime() {
        timeLy.setOnClickListener {
            val dialog = DateTimePickerDialog(MainActivity.instance!!, 0) { sCal, eCal, allday ->
                timeObject?.let {
                    it.dtStart = sCal.timeInMillis
                    it.dtEnd = eCal.timeInMillis
                }
                updateUI()
            }
            showDialog(dialog, true, true, true, false)
        }
    }

    private fun showTitleKeyPad() {
        titleInput.isFocusableInTouchMode = true
        if (titleInput.requestFocus()) {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(titleInput, 0)
        }
    }

    private fun setData(data: TimeObject) {
        originalData = data
        if(data.isManaged) {
            timeObject = TimeObjectManager.getCopiedData(data)
            deleteBtn.visibility = View.VISIBLE
        }else {
            timeObject = TimeObjectManager.makeNewTimeObject(data.dtStart, data.dtEnd)
            timeObject?.type = data.type
            timeObject?.color = data.color
            deleteBtn.visibility = View.GONE
        }
    }

    private fun updateUI() {
        timeObject?.let {
            titleInput.setText(it.title)
            when(it.type) {
                TimeObject.Type.EVENT.ordinal -> {

                }
                else -> {

                }
            }

            //controlPanel.setCardBackgroundColor(it.color)
        }
    }

    fun confirm() {
        timeObject?.let {
            TimeObjectManager.save(it.apply {
                title = titleInput.text.toString()
            })
        }
    }

    fun show(timeObject: TimeObject) {
        l("=======SHOW DetailView=======\n$timeObject")
        viewMode = ViewMode.OPENED
        MainActivity.instance?.let {
            it.onDimDark(true, true)
        }
        //layoutParams.height = insertModeHeight
        //requestLayout()
        setData(timeObject)
        updateUI()

        val t1 = makeFromBottomSlideTransition()
        t1.addTarget(contentPanel)

        val transitionSet = TransitionSet()
        transitionSet.addTransition(t1)
        transitionSet.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {
                styleEditLy.visibility = View.VISIBLE
                if(!timeObject.isManaged) {
                    showTitleKeyPad()
                }
            }
        })
        TransitionManager.beginDelayedTransition(this, transitionSet)

        contentPanel.visibility = View.VISIBLE
    }

    fun hide() {
        l("=======HIDE DetailView=======")
        viewMode = ViewMode.CLOSED
        MainActivity.instance?.let {
            it.offDimDark(true, true)
        }
        val t1 = makeFromBottomSlideTransition()
        t1.addTarget(contentPanel)

        val transitionSet = TransitionSet()
        transitionSet.addTransition(t1)
        transitionSet.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                hideKeyPad(windowToken, titleInput)
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {
                styleEditLy.visibility = View.GONE
            }
        })
        TransitionManager.beginDelayedTransition(this, transitionSet)

        contentPanel.visibility = View.INVISIBLE
    }
}