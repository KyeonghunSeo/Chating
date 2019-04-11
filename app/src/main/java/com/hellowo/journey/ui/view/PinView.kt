package com.hellowo.journey.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import com.hellowo.journey.AppDateFormat
import com.hellowo.journey.AppTheme
import com.hellowo.journey.R
import com.hellowo.journey.callAfterViewDrawed

class PinView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    var isPin = false
    val pinImg = ImageView(context)
    var color = AppTheme.iconColor
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
            pinImg.setColorFilter(AppTheme.iconColor)
        }else {
            pinImg.setColorFilter(AppTheme.lineColor)
        }
    }
}