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
    val swipeThreshold = dpToPx(100)
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
                            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?)?.vibrate(50)
                            swipeMode = if(firstX < it.x) 2 else 3
                            onSwipeStateChanged?.invoke(swipeMode)
                        }
                    }else if((swipeMode == 2 || swipeMode == 3) && Math.abs(firstX - it.x) < swipeThreshold) {
                        (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?)?.vibrate(50)
                        swipeMode = 1
                        onSwipeStateChanged?.invoke(swipeMode)
                    }
                }
                ACTION_UP -> {
                    swipeMode = 0
                    onSwipeStateChanged?.invoke(swipeMode)
                }
            }
            return@let
        }
        return super.dispatchTouchEvent(ev)
    }
}