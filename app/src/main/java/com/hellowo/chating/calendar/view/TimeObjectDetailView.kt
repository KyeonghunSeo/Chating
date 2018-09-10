package com.hellowo.chating.calendar.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.hellowo.chating.*
import com.hellowo.chating.calendar.TimeObjectManager
import com.hellowo.chating.calendar.ViewMode
import com.hellowo.chating.calendar.dialog.TypePickerDialog
import com.hellowo.chating.calendar.model.TimeObject
import com.hellowo.chating.ui.activity.MainActivity
import kotlinx.android.synthetic.main.view_timeobject_detail.view.*
import java.util.*


class TimeObjectDetailView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
    }

    private var calendarView: CalendarView? = null
    private var originalData: TimeObject? = null
    private var timeObject: TimeObject? = null
    private val colors = context.resources.getStringArray(R.array.colors)
    private val insertModeHeight = dpToPx(250)
    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_timeobject_detail, this, true)
        //contentLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        contentLy.setOnClickListener {}

        deleteBtn.setOnClickListener {
            timeObject?.let { TimeObjectManager.delete(it) }
            timeObject = null
            MainActivity.instance?.viewModel?.targetTimeObject?.value = null
        }

        initType()
        initTitle()
        initDateTime()
    }

    private fun initType() {
        typeBtn.setOnClickListener{ timeObject?.let { TypePickerDialog(it){
            updateUI()
        }.show(MainActivity.instance?.supportFragmentManager, null) } }
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

    }

    private fun showTitleKeyPad() {
        titleInput.isFocusableInTouchMode = true
        if (titleInput.requestFocus()) {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(titleInput, 0)
        }
    }

    private fun setData(data: TimeObject) {
        originalData = data
        timeObject = if(data.isManaged) {
            TimeObjectManager.getCopiedData(data)
        }else {
            TimeObjectManager.makeNewTimeObject(data.dtStart, data.dtEnd)
        }
    }

    private fun updateUI() {
        timeObject?.let {
            l("updateUI")
            titleInput.setText(it.title)
            when(it.type) {
                TimeObject.Type.EVENT.ordinal -> {

                }
                else -> {

                }
            }

            typeImg.setImageResource(TimeObject.Type.values()[it.type].iconId)
            typeTitle.text = context.getString(TimeObject.Type.values()[it.type].titleId)
        }
    }

    fun confirm() {
        timeObject?.let {
            TimeObjectManager.save(it.apply {
                title = titleInput.text.toString()
                color = Color.parseColor(colors[(0 until colors.size).random()])
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

        val transition = makeFromBottomSlideTransition()
        transition.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                setData(timeObject)
                updateUI()
                contentLy.visibility = View.VISIBLE
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
        })
        TransitionManager.beginDelayedTransition(this, transition)
        visibility = View.VISIBLE
        showTitleKeyPad()
    }

    fun hide() {
        l("=======HIDE DetailView=======")
        viewMode = ViewMode.CLOSED
        MainActivity.instance?.let {
            it.offDimDark(true, true)
        }
        val transition = makeFromBottomSlideTransition()
        transition.addListener(object : Transition.TransitionListener{
            override fun onTransitionEnd(transition: Transition) {
                contentLy.visibility = View.INVISIBLE
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
        })
        TransitionManager.beginDelayedTransition(this, transition)
        visibility = View.INVISIBLE
        hideKeyPad(windowToken, titleInput)
    }
}