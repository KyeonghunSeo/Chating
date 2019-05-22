package com.hellowo.journey.ui.view.base

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import com.hellowo.journey.AppDateFormat
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.callAfterViewDrawed

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