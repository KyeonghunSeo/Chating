package com.hellowo.chating.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class TouchPassLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    var isPassTouch = true
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return !isPassTouch
    }
}
