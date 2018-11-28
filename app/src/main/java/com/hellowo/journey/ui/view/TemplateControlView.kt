package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.*
import com.hellowo.journey.adapter.TemplateAdapter
import com.hellowo.journey.model.Template
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.activity.TemplateEditActivity
import com.hellowo.journey.ui.dialog.TypePickerDialog
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.view_template_control.view.*

class TemplateControlView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val itemWidth = dpToPx(90)
    private val collapseSize = dpToPx(36)
    private val scrolOffset = dpToPx(5)
    val layoutManager = GridLayoutManager(context, 3)
    val items = ArrayList<Template>()
    var selectedPosition = 0
    var clickFlag = false
    var autoScrollFlag = 0
    var autoScrollSpeed = 0f
    var isExpanded = false
    var scrollXdelta = 0
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when(msg.what) {
                0 -> {
                    vibrate(context)
                    clickFlag = false
                    addNew()
                }
                1 -> {
                    when {
                        autoScrollFlag > 0 -> {
                            recyclerView.scrollBy((scrolOffset * autoScrollSpeed).toInt(), 0)
                            this.sendEmptyMessageDelayed(1, 1)
                        }
                        autoScrollFlag < 0 -> {
                            recyclerView.scrollBy((-scrolOffset * autoScrollSpeed).toInt(), 0)
                            this.sendEmptyMessageDelayed(1, 1)
                        }
                    }
                }
                else -> {}
            }
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_template_control, this, true)
        controllView.radius = collapseSize / 2f
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = TemplateAdapter(context, items) {
            selectItem(it)
            MainActivity.instance?.viewModel?.makeNewTimeObject()
            postDelayed({collapse()}, 0)
        }

        touchEventView.setOnTouchListener { view, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if(!isExpanded) {
                        handler.sendEmptyMessageDelayed(0, 200)
                        clickFlag = true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    handler.removeMessages(0)
                    handler.removeMessages(1)
                    if(event.x > 0 && event.x < touchEventView.width &&
                            event.y > 0 && event.y < touchEventView.height){
                        if(clickFlag) {
                            //if(!isExpanded) MainActivity.instance?.viewModel?.makeNewTimeObject()
                            if(!isExpanded) expand()
                        }else {
                            collapse()
                        }
                    }else {
                        if(clickFlag) {
                            when {
                                event.y < -touchEventView.height * 1.5 -> {
                                    MainActivity.instance?.viewModel?.saveDirectByTemplate()
                                    collapse()
                                }
                                event.y < 0 -> {
                                    endScrolling()
                                    MainActivity.instance?.viewModel?.makeNewTimeObject()
                                    collapse()
                                }
                                else -> {
                                    endScrolling()
                                }
                            }
                        }
                    }
                    clickFlag = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if(clickFlag) {
                        if(event.x > touchEventView.width / 2 + itemWidth / 2){
                            autoScrollSpeed = ((event.x - (touchEventView.width / 2 + itemWidth / 2)) / (itemWidth / 2) + 0.5f)
                            if(autoScrollFlag <= 0) {
                                autoScrollFlag = 1
                                handler.removeMessages(1)
                                handler.sendEmptyMessage(1)
                            }
                        }else if(event.x < touchEventView.width / 2 - itemWidth / 2){
                            autoScrollSpeed = (((touchEventView.width / 2 - itemWidth / 2) - event.x) / (itemWidth / 2) + 0.5f)
                            if(autoScrollFlag >= 0) {
                                autoScrollFlag = -1
                                handler.removeMessages(1)
                                handler.sendEmptyMessage(1)
                            }
                        }else {
                            endScrolling()
                        }

                        when {
                            event.y < -touchEventView.height * 1.5 -> {
                            }
                            event.y < 0 -> {
                            }
                            else -> {
                            }
                        }
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    handler.removeMessages(0)
                    handler.removeMessages(1)
                    clickFlag = false
                }
            }
            return@setOnTouchListener false
        }

        editTemplateBtn.setOnClickListener { MainActivity.instance?.let {
            it.startActivity(Intent(it, TemplateEditActivity::class.java)) }}

        addNewBtn.setOnClickListener { addNew() }

        touchEventView.setOnClickListener{}

        callAfterViewDrawed(this, Runnable{ restoreViews() })
    }

    private fun addNew() {
        MainActivity.instance?.let { showDialog(TypePickerDialog(it, TimeObject.Type.EVENT) { type ->
                collapse()
                it.viewModel.makeNewTimeObject(type.ordinal)
            }, true, true, true, false)}
    }

    private fun expand() {
        vibrate(context)

        listLy.visibility = View.VISIBLE
        controllView.elevation = dpToPx(5f)
        backgroundLy.setBackgroundColor(AppTheme.backgroundColor)
        backgroundLy.setOnClickListener { collapse() }
        backgroundLy.isClickable = true

        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 45f),
                ObjectAnimator.ofFloat(listLy, "translationY", height.toFloat(), 0f),
                ObjectAnimator.ofFloat(backgroundLy, "alpha",0f, 0.9f))
        animSet.duration = ANIM_DUR
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()

        isExpanded = true
    }

    fun collapse() {
        controllView.elevation = 0f
        autoScrollFlag = 0

        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 0f),
                ObjectAnimator.ofFloat(listLy, "translationY", 0f, height.toFloat()),
                ObjectAnimator.ofFloat(backgroundLy, "alpha",0.9f, 0f))
        animSet.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) { restoreViews() }
            override fun onAnimationCancel(p0: Animator?) { }
            override fun onAnimationStart(p0: Animator?) {
                backgroundLy.setOnClickListener(null)
                backgroundLy.isClickable = false
            }
        })
        animSet.duration = ANIM_DUR
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
        isExpanded = false
    }

    fun notify(it: List<Template>) {
        items.clear()
        items.addAll(it)
        scrollXdelta = 0
        it.firstOrNull { it.id == Prefs.getInt("last_template_id", 0) }?.let {
            selectItem(it)
            recyclerView.adapter?.notifyDataSetChanged()
            recyclerView.scrollToPosition((items.size + 1) * 50 + selectedPosition)
        }
    }

    private fun selectItem(template: Template) {
        Prefs.putInt("last_template_id", template.id)
        selectedPosition = items.indexOf(template)
        setControlView(template)
        MainActivity.instance?.viewModel?.targetTemplate?.value = template
    }

    private fun setControlView(template: Template) {
    }

    private fun endScrolling() {
        if(autoScrollFlag != 0) {
            autoScrollFlag = 0
            handler.removeMessages(1)
        }
    }

    private fun restoreViews() {
        listLy.visibility = View.INVISIBLE
    }
}