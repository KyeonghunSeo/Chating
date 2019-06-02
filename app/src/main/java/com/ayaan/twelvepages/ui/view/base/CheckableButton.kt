package com.ayaan.twelvepages.ui.view.base

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.dpToPx

class CheckableButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {
    val icon = ImageView(context)
    val text = TextView(context)

    init {
        icon.layoutParams = LayoutParams(dpToPx(13), dpToPx(13)).apply { rightMargin = dpToPx(5) }
        text.layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)

        addView(icon)
        addView(text)
        gravity = CENTER
    }
}