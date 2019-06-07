package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.transition.TransitionManager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.ui.activity.MainActivity
import kotlinx.android.synthetic.main.dialog_popup_option.*


class PopupOptionDialog(activity: Activity, private val items: Array<Item>, private val targetView: View,
                        private val onResult: (Int) -> Unit) : Dialog(activity) {
    private val btns = ArrayList<LinearLayout>()
    private val buttonSize = dpToPx(45)

    class Item(val title: String, val icon: Int, val color: Int = AppTheme.primaryText)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.attributes?.windowAnimations = R.style.DialogFadeAnimation
        setContentView(R.layout.dialog_popup_option)
        setLayout()
        setOnShowListener {
            rootLy.postDelayed({ expand() }, 1)
        }
    }

    private fun setLayout() {
        setGlobalTheme(rootLy)
        rootLy.layoutParams.width = MainActivity.getMainPanel()?.width ?: 0
        rootLy.layoutParams.height = MainActivity.getMainPanel()?.height ?: 0
        rootLy.setOnClickListener { dismiss() }

        btns.add(btn0)
        btns.add(btn1)
        btns.add(btn2)
        btns.add(btn3)
        btns.add(btn4)
        btns.forEachIndexed { index, btn ->
            if(index < items.size) {
                setBtn(btn, items[index])
                btn.setOnClickListener {
                    onResult.invoke(index)
                    dismiss()
                }
            }
            btn.visibility = View.GONE
        }

        val location = IntArray(2)
        targetView.getLocationOnScreen(location)
        val p = location[1] - AppStatus.statusBarHeight
        val h = MainActivity.getMainPanel()?.height ?: 0
        (contentLy.layoutParams as FrameLayout.LayoutParams).let {
            if(p + buttonSize * items.size < h) {
                it.topMargin = p
                it.rightMargin = buttonSize / 2
                it.gravity = Gravity.RIGHT
            }else {
                it.rightMargin = buttonSize / 2
                it.bottomMargin = buttonSize / 8
                it.gravity = Gravity.BOTTOM or Gravity.RIGHT
            }
        }
    }

    private fun setBtn(btn: LinearLayout, item: Item) {
        btn.findViewWithTag<TextView>("title").let {
            it.text = item.title
            it.setTextColor(item.color)
        }
        btn.findViewWithTag<ImageView>("icon").let {
            it.setImageResource(item.icon)
            it.setColorFilter(item.color)
        }
    }

    private fun expand() {
        TransitionManager.beginDelayedTransition(contentLy, makeChangeBounceTransition())
        btns.forEachIndexed { index, btn ->
            if(index < items.size) {
                btn.visibility = View.VISIBLE
            }
        }
    }

}
