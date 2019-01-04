package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.LayoutTransition.CHANGING
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
import androidx.transition.Fade
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.hellowo.journey.*
import com.hellowo.journey.adapter.TemplateAdapter
import com.hellowo.journey.model.Template
import com.hellowo.journey.model.TimeObject
import com.hellowo.journey.ui.activity.MainActivity
import com.hellowo.journey.ui.activity.TemplateEditActivity
import com.hellowo.journey.ui.dialog.TypePickerDialog
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.view_template_control.view.*
import kotlin.math.exp

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
        controlLy.layoutTransition.enableTransitionType(CHANGING)
        listLy.visibility = View.INVISIBLE
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = TemplateAdapter(context, items) {
            selectItem(it)
            MainActivity.instance?.viewModel?.makeNewTimeObject()
            collapseNoAnim()
        }

        addBtn.setOnClickListener {
            if(isExpanded) {
                collapse()
            }else {
                expand()
            }
        }

        editTemplateBtn.setOnClickListener { MainActivity.instance?.let {
            it.startActivity(Intent(it, TemplateEditActivity::class.java)) }}

        addNewBtn.setOnClickListener { addNew() }

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
        val transitionSet = TransitionSet()
        val t1 = makeFromBottomSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_IN }
        t1.addTarget(listLy)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.visibility = View.VISIBLE
        backgroundLy.setBackgroundColor(AppTheme.backgroundColor)
        backgroundLy.setOnClickListener { collapse() }
        backgroundLy.isClickable = true
        listLy.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 45f).start()
        elevation = dpToPx(10f)
        isExpanded = true
    }

    fun collapse() {
        autoScrollFlag = 0
        val transitionSet = TransitionSet()
        val t1 = makeFromBottomSlideTransition()
        val t2 = makeFadeTransition().apply { (this as Fade).mode = Fade.MODE_OUT }
        t1.addTarget(listLy)
        t2.addTarget(backgroundLy)
        transitionSet.addTransition(t1)
        transitionSet.addTransition(t2)
        TransitionManager.beginDelayedTransition(this, transitionSet)

        backgroundLy.visibility = View.INVISIBLE
        backgroundLy.setOnClickListener(null)
        backgroundLy.isClickable = false
        listLy.visibility = View.INVISIBLE
        ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 0f).start()
        elevation = dpToPx(0f)
        isExpanded = false
    }

    private fun collapseNoAnim() {
        autoScrollFlag = 0
        templateIconImg.rotation = 0f
        backgroundLy.setOnClickListener(null)
        backgroundLy.isClickable = false
        backgroundLy.visibility = View.INVISIBLE
        listLy.visibility = View.INVISIBLE
        elevation = dpToPx(0f)
        isExpanded = false
    }

    fun notify(it: List<Template>) {
        l("[템플릿 뷰 갱신]")
        items.clear()
        items.addAll(it)
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun selectItem(template: Template) {
        Prefs.putInt("last_template_id", template.id)
        selectedPosition = items.indexOf(template)
        MainActivity.instance?.viewModel?.targetTemplate?.value = template
    }

    private fun restoreViews() {

    }

    fun hideDecoBtn() {
        decoBtn.visibility = View.GONE
    }

    fun showDecoBtn() {
        decoBtn.visibility = View.GONE
    }
}