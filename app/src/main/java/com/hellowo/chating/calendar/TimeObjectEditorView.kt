package com.hellowo.chating.calendar

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.transition.TransitionManager
import com.hellowo.chating.R
import com.hellowo.chating.makeFromBottomSlideTransition
import kotlinx.android.synthetic.main.view_time_object.view.*
import java.util.*

class TimeObjectEditorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        private val tempCal: Calendar = Calendar.getInstance()
    }

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

    fun onClick() {
        if(visibility == View.VISIBLE) {
            confirm()
        }else{
            show()
        }
    }

    private fun confirm() {
        val time = calendarView?.selectedCal?.timeInMillis ?: System.currentTimeMillis()
        TimeObjectManager.save(TimeObject().apply {
            title = titleInput.text.toString()
            dtStart = time
            dtEnd = time
            timeZone = TimeZone.getDefault().id
        })
        hide()
    }

    private fun show() {
        //TransitionManager.beginDelayedTransition(this, makeFromBottomSlideTransition())
        visibility = View.VISIBLE
        titleInput.requestFocus()
        titleInput.postDelayed({ (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(titleInput, 0) }, 50)
    }

    private fun hide() {
        //TransitionManager.beginDelayedTransition(this, makeFromBottomSlideTransition())
        visibility = View.GONE
        titleInput.postDelayed({
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(windowToken, 0) }, 50)
    }
}