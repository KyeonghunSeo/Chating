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

    val swipeThreshold = dpToPx(80)
    var firstX = 0f
    var firstY = 0f
    var swipeMode = 0
    var isTop = true
    var isBottom = false
    var currentDelta = 0f
    var topOverScrollY = 0f
    var bottomOverScrollY = 0f
    var onSwipeStateChanged: ((Int) -> Unit)? = null
    var onTop: ((Boolean) -> Unit)? = null
    var onOverScrolled: ((Int, Float, Boolean) -> Unit)? = null

    init {
        overScrollMode = View.OVER_SCROLL_NEVER
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            /*
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
            }*/

            when (ev.action) {
                ACTION_DOWN -> {
                    checkBottom()

                    if (isTop) {
                        topOverScrollY = it.y
                    }
                    if (isBottom) {
                        bottomOverScrollY = it.y
                    }

                    swipeMode = 1
                    onSwipeStateChanged?.invoke(swipeMode)
                }
                ACTION_MOVE -> {
                    if (swipeMode == 1) {
                        checkBottom()

                        if (isTop && topOverScrollY == 0f) topOverScrollY = it.y
                        if (isBottom && bottomOverScrollY == 0f) bottomOverScrollY = it.y

                        if (isTop && it.y > topOverScrollY) {
                            currentDelta = (it.y - topOverScrollY) * 0.4f
                            onOverScrolled?.invoke(1, currentDelta, Math.abs(currentDelta) > swipeThreshold)
                        } else if (isBottom && it.y < bottomOverScrollY) {
                            currentDelta = (it.y - bottomOverScrollY) * 0.4f
                            onOverScrolled?.invoke(1, currentDelta, Math.abs(currentDelta) > swipeThreshold)
                        } else {
                            if (!isTop) topOverScrollY = 0f
                            if (!isBottom) bottomOverScrollY = 0f
                        }
                    }
                }
                ACTION_UP -> {
                    onOverScrolled?.invoke(0, currentDelta, Math.abs(currentDelta) > swipeThreshold)
                    swipeMode = 0
                    topOverScrollY = 0f
                    bottomOverScrollY = 0f
                    currentDelta = 0f
                }
            }
            return@let
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onScrollChanged(x: Int, y: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(x, y, oldl, oldt)
        if (isTop && y > 0) {
            isTop = false
            onTop?.invoke(false)
        } else if (!isTop && y == 0) {
            isTop = true
            onTop?.invoke(true)
        }
    }

    private fun checkBottom(): Boolean {
        val view = getChildAt(0) as View
        val diff = view.bottom - (height + scrollY)
        isBottom = view.bottom < height || diff == 0
        return isBottom
    }
}