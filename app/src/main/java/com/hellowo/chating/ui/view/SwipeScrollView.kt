package com.hellowo.chating.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView
import com.hellowo.chating.dpToPx
import com.hellowo.chating.l
import androidx.core.content.ContextCompat.getSystemService
import android.os.Vibrator
import android.view.MotionEvent.*
import android.view.View


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
    var onSwipeStateChanged: ((Int) -> Unit)? = null
    var onTop: ((Boolean) -> Unit)? = null

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
            return@let
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if(isTop && t > 0) {
            isTop = false
            onTop?.invoke(false)
        }else if(!isTop && t == 0) {
            isTop = true
            onTop?.invoke(true)
        }
    }
}