package com.ayaan.twelvepages.ui.view.base

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.ayaan.twelvepages.R

class Line @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    val colorFlag: Int

    init {
        val a = getContext().obtainStyledAttributes(attrs, R.styleable.Line, defStyleAttr, 0)
        colorFlag = a.getInt(R.styleable.Line_colorFlag, 0)
        a.recycle()
    }
}