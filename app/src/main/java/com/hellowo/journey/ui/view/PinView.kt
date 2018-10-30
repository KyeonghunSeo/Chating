package com.hellowo.journey.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import com.hellowo.journey.AppRes
import com.hellowo.journey.R
import com.hellowo.journey.callAfterViewDrawed
import com.hellowo.journey.dpToPx

class PinView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    var isPin = false
    val pinImg = ImageView(context)
    var color = AppRes.primaryText
    var backColor = Color.WHITE
    var showPinBtn = true

    init {
        addView(pinImg)
        pinImg.layoutParams.width = MATCH_PARENT
        pinImg.layoutParams.height = MATCH_PARENT
        pinImg.scaleX = 0.5f
        pinImg.scaleY = 0.5f

        callAfterViewDrawed(this, Runnable{
            pin(isPin)
        })
    }

    fun pin(isPin: Boolean) {
        this.isPin = isPin
        if(isPin) {
            pinImg.setImageBitmap(null)
            pinImg.setColorFilter(color)
            pinImg.translationX = -width/4f
            pinImg.translationY = width/4f
            setBackgroundResource(R.drawable.edge)
        }else {
            if(showPinBtn) pinImg.setImageResource(R.drawable.pin)
            else pinImg.setImageBitmap(null)
            pinImg.setColorFilter(AppRes.disableText)
            pinImg.translationX = 0f
            pinImg.translationY = 0f
            setBackgroundColor(backColor)
        }
    }
}