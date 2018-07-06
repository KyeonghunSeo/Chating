package com.hellowo.chating.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView
import com.hellowo.chating.l

class SwipeNestedScrollView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : NestedScrollView(context, attrs, defStyleAttr) {
    var dragMode = 0
    var onSwipe: ((Float) -> Unit)? = null
    var onTabUp: ((Int) -> Unit)? = null
    var onSwipeFling: ((Float) -> Unit)? = null
    var deltaX = 0f
    private val detector = GestureDetector(context, object : GestureDetector.OnGestureListener{
        override fun onShowPress(p0: MotionEvent?) {}
        override fun onSingleTapUp(p0: MotionEvent?): Boolean {
            deltaX = 0f
            onTabUp?.invoke(dragMode)
            return dragMode == 2
        }
        override fun onDown(p0: MotionEvent?): Boolean {
            dragMode = 1
            return false
        }
        override fun onLongPress(p0: MotionEvent?) {}
        override fun onFling(p0: MotionEvent?, p1: MotionEvent?, vx: Float, vy: Float): Boolean {
            return dragMode == 2
        }
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, dx: Float, dy: Float): Boolean {
            return when (dragMode) {
                1 -> if(Math.abs(dx) > Math.abs(dy)) {
                    dragMode = 2 // 스와이프
                    true
                }else {
                    dragMode = 3 // 스크롤
                    false
                }
                2 -> {
                    deltaX -= (e2!!.x - e1!!.x)
                    onSwipe?.invoke(deltaX)
                    true
                }
                3 -> false
                else -> false
            }
        }
    })

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return !detector.onTouchEvent(ev) && dragMode != 2 && super.dispatchTouchEvent(ev)
    }
}