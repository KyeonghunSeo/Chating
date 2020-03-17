package com.ayaan.twelvepages.ui.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.ayaan.twelvepages.AppTheme
import com.ayaan.twelvepages.R
import com.ayaan.twelvepages.callAfterViewDrawed

class LoadingView  @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LottieAnimationView(context, attrs, defStyleAttr) {

    init {
        imageAssetsFolder = "assets/"
        setAnimation("loading.json")
        repeatMode = LottieDrawable.RESTART
        repeatCount = LottieDrawable.INFINITE
    }

    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        playAnimation()
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }
}