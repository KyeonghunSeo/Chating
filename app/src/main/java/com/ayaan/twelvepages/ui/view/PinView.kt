package com.ayaan.twelvepages.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.callAfterViewDrawed

class PinView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    var isPin = false
    val pinImg = ImageView(context)
    var color = AppTheme.icon
    var backColor = Color.WHITE
    var showPinBtn = true

    init {
        addView(pinImg)
        pinImg.layoutParams.width = MATCH_PARENT
        pinImg.layoutParams.height = MATCH_PARENT
        pinImg.scaleX = 0.5f
        pinImg.scaleY = 0.5f
        pinImg.setImageResource(R.drawable.pin)

        callAfterViewDrawed(this, Runnable{
            pin(isPin)
        })
    }

    fun pin(isPin: Boolean) {
        this.isPin = isPin
        if(isPin) {
            pinImg.setColorFilter(AppTheme.icon)
        }else {
            pinImg.setColorFilter(AppTheme.line)
        }
    }
}