package com.hellowo.chating.calendar.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.*
import com.hellowo.chating.*
import com.hellowo.chating.calendar.model.TimeObject
import com.hellowo.chating.calendar.TimeObjectManager
import com.hellowo.chating.calendar.ViewMode
import com.hellowo.chating.calendar.dialog.TypePickerDialog
import com.hellowo.chating.ui.activity.MainActivity
import kotlinx.android.synthetic.main.view_timeobject_detail.view.*
import java.lang.reflect.Type
import java.util.*

class TimeObjectDetailView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
    }

    private var calendarView: CalendarView? = null
    private var behavior: BottomSheetBehavior<View>? = null
    private var originalData: TimeObject? = null
    private var timeObject: TimeObject? = null
    private val colors = context.resources.getStringArray(R.array.colors)
    private val bottomSheetPeekHeight = dpToPx(250)
    var viewMode = ViewMode.CLOSED

    init {
        LayoutInflater.from(context).inflate(R.layout.view_timeobject_detail, this, true)
        contentLy.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        contentLy.setOnClickListener {}
        expandBtn.setOnClickListener { behavior?.state = STATE_EXPANDED }

        deleteBtn.setOnClickListener {
            timeObject?.let { TimeObjectManager.delete(it) }
            timeObject = null
            MainActivity.instance?.viewModel?.targetTimeObject?.value = null
        }

        initType()
        initTitle()
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
        titleInput.setOnClickListener {
            titleInput.isFocusableInTouchMode = true
            if (titleInput.requestFocus()) {
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(titleInput, 0)
            }
        }
    }

    fun initBehavior() {
        behavior = BottomSheetBehavior.from(this)
        behavior?.let {
            it.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if(newState == STATE_EXPANDED) {
                    }else if(newState == STATE_COLLAPSED) {
                    }else if(newState == STATE_HIDDEN) {
                        if(viewMode != ViewMode.CLOSED){
                            hide()
                        }
                        confirm()
                    }
                }
            })
            it.isHideable = true
            it.state = STATE_HIDDEN
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
        setData(timeObject)
        updateUI()
        viewMode = ViewMode.OPENED
        MainActivity.instance?.let {
            it.onDimDark(true, true)
        }
        behavior?.let {
            it.state = STATE_COLLAPSED
        }
    }

    fun hide() {
        l("=======HIDE DetailView=======")
        viewMode = ViewMode.CLOSED
        MainActivity.instance?.let {
            it.offDimDark(true, true)
        }
        behavior?.state = STATE_HIDDEN
        hideKeyPad(windowToken, titleInput)
    }
}