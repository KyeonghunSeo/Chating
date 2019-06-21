package com.ayaan.twelvepages.ui.dialog

import android.animation.ObjectAnimator
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
import com.ayaan.twelvepages.manager.ColorManager
import com.ayaan.twelvepages.ui.activity.MainActivity
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.dialog_small_color_picker.*
import kotlinx.android.synthetic.main.pager_item_color_palette.view.*


class SmallColorPickerDialog(activity: Activity, private val colorKey: Int, private val location: IntArray,
                             private val onResult: (Int) -> Unit) : Dialog(activity) {
    private val colorBtns = ArrayList<ImageView>()
    private val buttonSize = dpToPx(35)
    private val colorPaletteSize = 10
    private var colorPalette = ColorManager.ColorPack.values()[colorKey / colorPaletteSize]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.attributes?.windowAnimations = R.style.DialogFadeAnimation
        setContentView(R.layout.dialog_small_color_picker)
        setLayout()
        setOnShowListener {
            viewPager.currentItem = colorPalette.ordinal
            rootLy.postDelayed({ expand() }, 100)
        }
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
        colorBtns.forEach { it.visibility = View.GONE }
        colorBtns[colorKey % colorPaletteSize].visibility = View.VISIBLE
        setColorBtns()

        (contentLy.layoutParams as FrameLayout.LayoutParams).setMargins(
                location[0], location[1] - AppStatus.statusBarHeight, 0 , 0)

        viewPager.adapter = ColorPalettePagerAdapter()
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                colorPalette = ColorManager.ColorPack.values()[position]
                setColorBtns()
                ObjectAnimator.ofFloat(btnsLy, "translationY", dpToPx(10f), 0f).start()
                ObjectAnimator.ofFloat(btnsLy, "alpha", 0f, 1f).start()
            }
        })
        pagerIndicator.setViewPager(viewPager)
        pagerLy.visibility = View.GONE
        changeBtn.visibility = View.GONE

        changeBtn.setOnClickListener {
            TransitionManager.beginDelayedTransition(contentLy, makeChangeBounceTransition())
            changeBtn.visibility = View.GONE
            pagerLy.visibility = View.VISIBLE
        }
    }

    private fun setColorBtns() {
        colorBtns.forEachIndexed { index, colorBtn ->
            colorBtn.setColorFilter(colorPalette.items[index])
            colorBtn.setOnClickListener {
                Prefs.putInt("primaryColorPack", colorPalette.ordinal)
                ColorManager.primaryColorPack = colorPalette
                onResult.invoke(colorPalette.ordinal * colorPaletteSize + index)
                dismiss()
            }
        }
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
        changeBtn.visibility = View.VISIBLE
    }

    inner class ColorPalettePagerAdapter : PagerAdapter() {
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val v = LayoutInflater.from(context).inflate(R.layout.pager_item_color_palette, null, false)
            val palette = ColorManager.ColorPack.values()[position]
            v.imageView.setImageResource(palette.coverImgId)
            v.titleText.text = context.getString(palette.titleId)
            container.addView(v)
            return v
        }
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`
        override fun getCount(): Int = ColorManager.ColorPack.values().size
    }

}
