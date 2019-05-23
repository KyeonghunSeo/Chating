package com.ayaan.twelvepages.ui.view.base

import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout

class ClipFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    inner class outlineProvider: ViewOutlineProvider() {
        override fun getOutline(p0: View?, outline: Outline?) {
        }
    }

    init {

    }
}