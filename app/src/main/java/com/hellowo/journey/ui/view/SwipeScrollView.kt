package com.hellowo.journey.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView
import com.hellowo.journey.dpToPx
import android.view.MotionEvent.*
import android.view.View
import android.opengl.ETC1.getHeight
import com.hellowo.journey.l


class SwipeScrollView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ScrollView(context, attrs, defStyleAttr) {
    companion object {
        const val SWIPE_LEFT = 2
        const val SWIPE_RIGHT = 3
    }

    val swipeThreshold = dpToPx(10)
    var firstX = 0f
    var firstY = 0f
    var swipeMode = 0
    var isTop = true
    var isBottom = false
    var topOverScrollY = 0f
    var bottomOverScrollY = 0f
    var onSwipeStateChanged: ((Int) -> Unit)? = null
    var onTop: ((Boolean) -> Unit)? = null
    var onOverScrolled: ((Float) -> Unit)? = null

    init {
        overScrollMode = View.OVER_SCROLL_ALWAYS
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            when(ev.action) {
                ACTION_DOWN -> {
                    firstX = it.x
                    firstY = it.y
                    swipeMode = 1
                    onSwipeStateChanged?.invoke(swipeMode)
                }
                ACTION_MOVE -> {
                    if(swipeMode != 0 && Math.abs(firstY - it.y) > swipeThreshold * 2) {
                        swipeMode = 0
                    }else if(swipeMode == 1) {
                        if(Math.abs(firstX - it.x) > swipeThreshold) {
                            swipeMode = 4
                        }
                    }else if(swipeMode == 4) {
                        if(Math.abs(firstX - it.x) > swipeThreshold * 5) {
                            onSwipeStateChanged?.invoke(if(firstX < it.x) SWIPE_LEFT else SWIPE_RIGHT)
                            swipeMode = 0
                            return true
                        }
                    }
                }
                ACTION_UP -> {
                    swipeMode = 0
                }
            }
            /*
            when(ev.action) {
                ACTION_DOWN -> {
                    firstY = it.y
                    checkBottom()
                    if(isTop) {
                        topOverScrollY = firstY
                    }
                    if(isBottom) {
                        bottomOverScrollY = firstY
                    }
                    swipeMode = 1
                    onSwipeStateChanged?.invoke(swipeMode)
                }
                ACTION_MOVE -> {
                    if(isTop) {
                        if(topOverScrollY == 0f) topOverScrollY = it.y
                        //onOverScrolled?.invoke(topOverScrollY - it.y)
                    }else {
                        topOverScrollY = 0f
                    }

                    if(isBottom){

                    }
                }
                ACTION_UP -> {
                    swipeMode = 0
                }
            }*/
            return@let
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onScrollChanged(x: Int, y: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(x, y, oldl, oldt)
        if(isTop && y > 0) {
            isTop = false
            onTop?.invoke(false)
        }else if(!isTop && y == 0) {
            isTop = true
            onTop?.invoke(true)
        }
        checkBottom()
    }

    private fun checkBottom(): Boolean {
        val view = getChildAt(0) as View
        val diff = view.bottom - (height + scrollY)
        isBottom = view.bottom < height || diff == 0
        return isBottom
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        l("onOverScrolled $clampedY $scrollY")
        if(scrollY <= 0 && clampedY) {
            isTop = true
            onTop?.invoke(true)
        }else {
            isTop = false
            onTop?.invoke(false)
        }
    }
}