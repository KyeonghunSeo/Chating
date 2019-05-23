package com.ayaan.twelvepages.ui.view.base

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText

class CustomEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : EditText(context, attrs, defStyleAttr) {
    var onSelectionChanged: ((Int, Int) -> Unit)? = null

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        onSelectionChanged?.invoke(selStart, selEnd)
    }
}