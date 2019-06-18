package com.ayaan.twelvepages.ui.view.base

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.KeyEvent
import android.widget.EditText

class AutoScaleEditText @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : EditText(context, attrs, defStyleAttr) {
    companion object {
        private const val WITHOUT_SIZE = 0
        private const val DEFAULT_TEXT_SCALE = 0.5f
        private const val DEFAULT_ANIMATION_DURATION = 250
        private const val DEFAULT_LINES_LIMIT = 0.8f
    }

    private var textMeasuringText: Paint? = null
    private var originalViewWidth = WITHOUT_SIZE
    private var originalTextSize = WITHOUT_SIZE.toFloat()
    private var resizeInProgress = false
    var linesLimit = DEFAULT_LINES_LIMIT
    var animationDuration = DEFAULT_ANIMATION_DURATION
    var textScale = DEFAULT_TEXT_SCALE
    var onScaleChanged: ((Boolean) -> Unit)? = null

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (!resizeInProgress) {
            val numOfLinesOnScreen = calculateNumberOfLinesNedeed()
            if (numOfLinesOnScreen > linesLimit) {
                resizeTextToSmallSize()
                onScaleChanged?.invoke(false)
            } else {
                resizeTextToNormalSize()
                onScaleChanged?.invoke(true)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_ENTER) {
            true
        } else super.onKeyDown(keyCode, event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (originalViewWidth == WITHOUT_SIZE) {
            originalViewWidth = measuredWidth
            originalViewWidth -= paddingRight + paddingLeft
        }
        if (originalTextSize == WITHOUT_SIZE.toFloat()) {
            originalTextSize = textSize
            initializeTextMeasurerPaint()
        }
    }

    private fun initializeTextMeasurerPaint() {
        textMeasuringText = Paint()
        textMeasuringText!!.typeface = typeface
        textMeasuringText!!.textSize = originalTextSize
    }

    private fun resizeTextToSmallSize() {
        val smallTextSize = originalTextSize * textScale
        val currentTextSize = textSize
        if (currentTextSize > smallTextSize) {
            playAnimation(currentTextSize, smallTextSize)
        }
    }

    private fun resizeTextToNormalSize() {
        val currentTextSize = textSize
        if (currentTextSize < originalTextSize) {
            playAnimation(currentTextSize, originalTextSize)
        }
    }

    private fun calculateNumberOfLinesNedeed(): Float {
        val textSizeInPixels = measureText()
        return textSizeInPixels / originalViewWidth
    }

    private fun measureText(): Float {
        var result = 0f
        if (textMeasuringText != null) {
            result = textMeasuringText!!.measureText(text.toString())
        }
        return result
    }

    override fun setTextSize(size: Float) {
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    private fun playAnimation(origin: Float, destination: Float) {
        val animator = ObjectAnimator.ofFloat(this, "textSize", origin, destination)
        animator.target = this
        animator.duration = animationDuration.toLong()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                resizeInProgress = true
            }

            override fun onAnimationEnd(animation: Animator) {
                resizeInProgress = false
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
    }
}
