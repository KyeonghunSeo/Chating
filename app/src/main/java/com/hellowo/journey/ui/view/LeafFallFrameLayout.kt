package com.hellowo.journey.ui.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView

import com.hellowo.journey.*
import com.hellowo.journey.R

import java.util.Random
import java.util.Timer
import java.util.TimerTask

class LeafFallFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {
    var isPassTouch = true

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val viewId = Random().nextInt(LEAVES.size)
            val d = resources.getDrawable(LEAVES[viewId])
            val leafImageView = ImageView(context)
            leafImageView.setImageDrawable(d)
            leafImageView.setColorFilter(AppTheme.disableText)
            addView(leafImageView)

            val animationLayout = leafImageView.layoutParams as FrameLayout.LayoutParams
            animationLayout.setMargins(0, -topMargin, 0, 0)
            animationLayout.width = leafSize
            animationLayout.height = leafSize

            startAnimation(leafImageView)
        }
    }

    private var mTimer: Timer? = null

    private inner class AnimTimerTask : TimerTask() {
        override fun run() {
            mHandler.sendEmptyMessage(0x001)
        }
    }

    fun start() {
        mTimer = Timer()
        mTimer!!.schedule(AnimTimerTask(), 0, 1000)
    }

    fun stop() {
        if (mTimer != null) {
            mTimer!!.cancel()
        }
    }

    fun startAnimation(leafImageView: ImageView) {

        leafImageView.pivotX = (leafImageView.width / 2).toFloat()
        leafImageView.pivotY = (leafImageView.height / 2).toFloat()

        val delay = Random().nextInt(2000).toLong()

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 9000
        animator.interpolator = AccelerateInterpolator()
        animator.startDelay = delay

        animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            var width = getWidth()
            var height = getHeight()
            var startx = Random().nextInt(width)
            var movex = Random().nextInt(width) - width / 2
            var angle = 50 + (Math.random() * 101).toInt()

            override fun onAnimationUpdate(animation: ValueAnimator) {
                val value = animation.animatedValue as Float
                leafImageView.rotation = angle * value
                leafImageView.translationX = startx + movex * value
                leafImageView.translationY = (height + topMargin + topMargin) * value
                if (value == 1f) {
                    removeView(leafImageView)
                }
            }
        })

        animator.start()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return !isPassTouch && super.dispatchTouchEvent(ev)
    }

    companion object {
        private val topMargin = dpToPx(25)
        private val leafSize = dpToPx(10)
        private val LEAVES = intArrayOf(R.drawable.sakura0, R.drawable.sakura1)
    }
}
