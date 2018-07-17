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


class SwipeScrollView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ScrollView(context, attrs, defStyleAttr) {
    val swipeThreshold = dpToPx(20)
    var firstX = 0f
    var firstY = 0f
    var swipeMode = 0
    var onSwipeStateChanged: ((Int) -> Unit)? = null
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
                    if(swipeMode == 1) {
                        if(Math.abs(firstX - it.x) > swipeThreshold) {
                            swipeMode = 4
                        }else if(Math.abs(firstY - it.y) > swipeThreshold) {
                            swipeMode = 0
                        }
                    }else if(swipeMode == 4) {
                        if(Math.abs(firstX - it.x) > swipeThreshold * 3) {
                            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?)?.vibrate(20)
                            onSwipeStateChanged?.invoke(if(firstX < it.x) 2 else 3)
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
}