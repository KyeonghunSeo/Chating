package com.ayaan.twelvepages.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.transition.TransitionManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ayaan.twelvepages.*
import com.ayaan.twelvepages.ui.activity.MainActivity
import kotlinx.android.synthetic.main.dialog_color_picker.*
import kotlinx.android.synthetic.main.pager_item_color_palette.view.*


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

        viewPager.adapter = ColorPalettePagerAdapter()
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {

            }
        })
        viewPager.visibility = View.GONE
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

    val ss = arrayOf(R.drawable.color_palette_0, R.drawable.color_palette_1, R.drawable.color_palette_2,
            R.drawable.color_palette_3, R.drawable.color_palette_4)

    inner class ColorPalettePagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val v = LayoutInflater.from(context).inflate(R.layout.pager_item_color_palette, null, false)
            v.imageView.setImageResource(ss[position])
            v.titleText.text = "Spring"
            container.addView(v)
            return v
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount(): Int = 5
    }

}
