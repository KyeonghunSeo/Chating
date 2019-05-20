package com.hellowo.journey.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.transition.TransitionManager
import com.hellowo.journey.*
import com.hellowo.journey.ui.activity.MainActivity
import kotlinx.android.synthetic.main.dialog_color_picker.*


class ColorPickerDialog(activity: Activity, private val colorKey: Int, private val location: IntArray,
                        private val onResult: (Int) -> Unit) : Dialog(activity) {
    private val colorBtns = ArrayList<ImageView>()
    private val buttonSize = dpToPx(35)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.attributes.windowAnimations = R.style.DialogFadeAnimation
        setContentView(R.layout.dialog_color_picker)
        setLayout()
        setOnShowListener { rootLy.postDelayed({ expand() }, 100) }
    }

    private fun setLayout() {
        rootLy.layoutParams.width = MainActivity.getMainPanel()?.width ?: 0
        rootLy.layoutParams.height = MainActivity.getMainPanel()?.height ?: 0
        rootLy.setOnClickListener { dismiss() }

        colorBtns.add(colorBtn0)
        colorBtns.add(colorBtn1)
        colorBtns.add(colorBtn2)
        colorBtns.add(colorBtn3)
        colorBtns.add(colorBtn4)
        colorBtns.add(colorBtn5)
        colorBtns.add(colorBtn6)
        colorBtns.add(colorBtn7)
        colorBtns.add(colorBtn8)
        colorBtns.add(colorBtn9)
        colorBtns.add(colorBtn10)
        colorBtns.add(colorBtn11)

        val colors = AppTheme.colors
        colorBtns.forEachIndexed { index, colorBtn ->
            colorBtn.setColorFilter(colors[index])
            colorBtn.setOnClickListener {
                onResult.invoke(index)
                dismiss()
            }
            colorBtn.visibility = View.GONE
        }
        colorBtns[colorKey].visibility = View.VISIBLE

        (contentLy.layoutParams as FrameLayout.LayoutParams).setMargins(
                location[0], location[1] - AppStatus.statusBarHeight, 0 , 0)
    }

    private fun expand() {
        TransitionManager.beginDelayedTransition(contentLy, makeChangeBounceTransition())

        val p = location[1] - AppStatus.statusBarHeight
        val h = MainActivity.getMainPanel()?.height ?: 0
        (contentLy.layoutParams as FrameLayout.LayoutParams).let {
            if(p < h / 2) {
                val top = if(p - (buttonSize * 5) > 0) p - (buttonSize * 5) else 0
                it.topMargin = top
            }else {
                val bottom = if(p + (buttonSize * 5) < h) (h - (p + (buttonSize * 5))) else 0
                it.topMargin = 0
                it.bottomMargin = bottom
                it.gravity = Gravity.BOTTOM
            }
        }

        colorBtns.forEach { it.visibility = View.VISIBLE }
    }

}
