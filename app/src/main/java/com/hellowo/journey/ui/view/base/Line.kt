package com.hellowo.journey.ui.view.base

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.hellowo.journey.AppRes
import com.hellowo.journey.manager.CalendarSkin

class Line @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    init {
        setBackgroundColor(AppRes.primaryText)
    }
}