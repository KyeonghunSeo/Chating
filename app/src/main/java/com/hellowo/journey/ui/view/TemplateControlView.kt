package com.hellowo.journey.ui.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import com.hellowo.journey.*
import com.hellowo.journey.adapter.TemplateAdapter
import com.hellowo.journey.model.Template
import com.hellowo.journey.ui.activity.MainActivity
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.view_template_control.view.*

class TemplateControlView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    private val itemHeight = dpToPx(120)
    private val itemWidth = dpToPx(100)
    private val collapseSize = dpToPx(36)
    private val scrolOffset = dpToPx(5)
    val layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
    val items = ArrayList<Template>()
    var selectedPosition = 0
    var selectFlag = false
    var clickFlag = false
    var autoPagingFlag = 0
    var autoPagingSpeed = 0
    var isExpanded = false
    var scrollXdelta = 0
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when(msg.what) {
                0 -> expand()
                1 -> {
                    when {
                        autoPagingFlag > 0 -> {
                            recyclerView.scrollBy(scrolOffset * autoPagingSpeed, 0)
                            this.sendEmptyMessageDelayed(1, 1)
                        }
                        autoPagingFlag < 0 -> {
                            recyclerView.scrollBy(-scrolOffset * autoPagingSpeed, 0)
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
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            val yOffset = dpToPx(60f)
            val scaleOffset = 0.1f
            val zOffset = dpToPx(10)

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollXdelta += dx
                (0 until layoutManager.childCount).forEach {
                    layoutManager.getChildAt(it)?.let { view ->
                        val location = IntArray(2)
                        view.getLocationInWindow(location)
                        val distanseScalse = Math.abs(location[0] + (view.width / 2) - width / 2) / (width / 2).toFloat()
                        view.translationY = yOffset * distanseScalse
                        view.scaleX = 1 - scaleOffset * distanseScalse
                        view.scaleY = 1 - scaleOffset * distanseScalse
                        //view.findViewById<FrameLayout>(R.id.contentLy).elevation = zOffset * (1 - distanseScalse)
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState == 0) {
                    if(selectFlag) {
                        selectFlag = false
                    }else {
                        findAndSelectCenterItem()
                    }
                }else {
                    if(selectedLy.alpha == 1f) {
                        //ObjectAnimator.ofFloat(selectedLy, "alpha",1f, 0f).start()
                    }
                }
            }
        })
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
                    if(event.x > 0 && event.x < width && event.y > 0 && event.y < height){
                        if(clickFlag) {
                            if(!isExpanded) MainActivity.instance?.viewModel?.makeNewTimeObject()
                        }else {
                            collapse()
                        }
                    }else {
                        if(clickFlag) {
                            if(event.y < -recyclerView.height + height) {
                                MainActivity.instance?.viewModel?.saveDirectByTemplate()
                                collapse()
                            }
                        }
                    }
                    clickFlag = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if(clickFlag) {
                        val width = touchEventView.width
                        val height = touchEventView.height
                        if(event.x > width / 2 + itemWidth * 1.5) {
                            autoPagingSpeed = 2
                            if(autoPagingFlag <= 0) {
                                autoPagingFlag = 1
                                handler.removeMessages(1)
                                handler.sendEmptyMessage(1)
                            }
                        }else if(event.x < width / 2 - itemWidth * 1.5){
                            autoPagingSpeed = 2
                            if(autoPagingFlag >= 0) {
                                autoPagingFlag = -1
                                handler.removeMessages(1)
                                handler.sendEmptyMessage(1)
                            }
                        }else if(event.x > width / 2 + itemWidth * 0.75){
                            autoPagingSpeed = 1
                            if(autoPagingFlag <= 0) {
                                autoPagingFlag = 1
                                handler.removeMessages(1)
                                handler.sendEmptyMessage(1)
                            }
                        }else if(event.x < width / 2 - itemWidth * 0.75){
                            autoPagingSpeed = 1
                            if(autoPagingFlag >= 0) {
                                autoPagingFlag = -1
                                handler.removeMessages(1)
                                handler.sendEmptyMessage(1)
                            }
                        }else {
                            if(autoPagingFlag != 0) {
                                autoPagingFlag = 0
                                handler.removeMessages(1)
                                findAndSelectCenterItem()
                            }
                        }

                        if(event.y < -recyclerView.height + height) {
                            selectedText.text = context.getString(R.string.add_direct)
                        }else {
                            selectedText.text = context.getString(R.string.selected)
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

        touchEventView.setOnClickListener{}

        callAfterViewDrawed(this, Runnable{ restoreViews() })
    }

    private fun expand() {
        vibrate(context)

        if(scrollXdelta == 0) {
            setScrollCenterSelectedItem()
        }

        listLy.visibility = View.VISIBLE
        controllView.elevation = dpToPx(5f)

        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 45f),
                ObjectAnimator.ofFloat(listLy, "translationY", height.toFloat(), 0f),
                ObjectAnimator.ofFloat(backgroundLy, "alpha",0f, 1f))
        animSet.duration = ANIM_DUR
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()

        isExpanded = true
    }

    fun collapse() {
        controllView.elevation = 0f
        autoPagingFlag = 0

        val animSet = AnimatorSet()
        animSet.playTogether(ObjectAnimator.ofFloat(templateIconImg, "rotation", templateIconImg.rotation, 0f),
                ObjectAnimator.ofFloat(listLy, "translationY", 0f, height.toFloat()),
                ObjectAnimator.ofFloat(backgroundLy, "alpha",1f, 0f))
        animSet.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) { restoreViews() }
            override fun onAnimationCancel(p0: Animator?) { }
            override fun onAnimationStart(p0: Animator?) {}
        })
        animSet.duration = ANIM_DUR
        animSet.interpolator = FastOutSlowInInterpolator()
        animSet.start()
        isExpanded = false
    }

    private fun restoreViews() {
        listLy.visibility = View.INVISIBLE
    }

    fun notify(it: List<Template>) {
        items.clear()
        items.addAll(it)
        scrollXdelta = 0
        it.firstOrNull { it.id == Prefs.getInt("last_template_id", 0) }?.let {
            selectItem(it)
            recyclerView.adapter?.notifyDataSetChanged()
            recyclerView.scrollToPosition(items.size * 50 + selectedPosition)
        }
    }

    private fun setScrollCenterSelectedItem() {
        recyclerView.scrollBy(-(width / 2 - itemWidth / 2), 0)
    }

    private fun findAndSelectCenterItem() {
        selectFlag = true
        selectedLy.visibility = View.VISIBLE
        //ObjectAnimator.ofFloat(selectedLy, "alpha",0f, 1f).start()
        var delta = Int.MAX_VALUE
        var sIndex = 0
        (0 until layoutManager.childCount).forEach {
            layoutManager.getChildAt(it)?.let { view ->
                val location = IntArray(2)
                view.getLocationInWindow(location)
                val d = width / 2 - (location[0] + itemWidth / 2)
                if(Math.abs(d) < Math.abs(delta)) {
                    delta = d
                    sIndex = it
                }
            }
        }
        recyclerView.smoothScrollBy(delta * -1, 0)
        selectedPosition = (layoutManager.findFirstVisibleItemPosition() + sIndex) % items.size
        selectItem(items[selectedPosition])
    }

    private fun selectItem(template: Template) {
        Prefs.putInt("last_template_id", template.id)
        selectedPosition = items.indexOf(template)
        setControlView(template)
        MainActivity.instance?.viewModel?.targetTemplate?.value = template
    }

    private fun setControlView(template: Template) {
        controllBackground.setBackgroundColor(template.color)
        templateIconImg.setColorFilter(template.fontColor)
        templateNameText.text = template.title
    }
}