package com.hellowo.journey.ui.view.base

import android.content.Context
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class PagingControlableViewPager(context: Context) : ViewPager(context) {
    private var isPagingEnabled: Boolean = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.isPagingEnabled) {
            super.onTouchEvent(event)
        } else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (this.isPagingEnabled) {
            try {
                return super.onInterceptTouchEvent(event)
            } catch (e: Exception) {}
        }
        return false
    }

    fun setPagingEnabled(b: Boolean) {
        this.isPagingEnabled = b
    }
}